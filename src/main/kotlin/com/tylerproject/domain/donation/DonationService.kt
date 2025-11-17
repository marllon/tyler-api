package com.tylerproject.domain.donation

import com.tylerproject.domain.goal.GoalService
import com.tylerproject.providers.PagBankProvider
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.ceil

interface DonationService {
    fun createDonation(request: CreateDonationRequest): DonationResponse
    fun getDonationById(id: String): DonationResponse?
    fun listDonations(
        page: Int,
        pageSize: Int,
        status: DonationStatus?,
        donationType: DonationType?,
        sortBy: String,
        sortDirection: String
    ): DonationPageResponse
    fun getDonationsByTargetId(targetId: String): List<DonationResponse>
    fun processWebhook(payload: PagBankWebhookPayload): WebhookProcessingResult
    fun createPixCharge(donationId: String): CreatePixChargeResponse?
    fun processPaidDonation(donationId: String): Boolean
}

@Service
class DonationServiceImpl(
    private val donationRepository: DonationRepository,
    private val goalService: GoalService,
    private val pagBankProvider: PagBankProvider
) : DonationService {

    private val logger = LoggerFactory.getLogger(DonationServiceImpl::class.java)

    override fun createDonation(request: CreateDonationRequest): DonationResponse = runBlocking {
        logger.info("Creating donation - type: ${request.donationType}, targetId: ${request.targetId}, amount: ${request.amount}")

        validateTarget(request.donationType, request.targetId)

        val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)

        val donation = Donation(
            id = "",
            donationType = request.donationType,
            targetId = request.targetId,
            amount = request.amount,
            status = DonationStatus.PENDING,
            paymentMethod = request.paymentMethod,
            donorName = request.donorName,
            donorEmail = request.donorEmail,
            donorPhone = request.donorPhone,
            donorDocument = request.donorDocument,
            isAnonymous = request.isAnonymous,
            message = request.message,
            metadata = request.metadata,
            createdAt = now,
            updatedAt = now
        )

        val savedDonation = donationRepository.save(donation)
        logger.info("Donation created successfully: ${savedDonation.id}")

        DonationResponse.fromEntity(savedDonation)
    }

    override fun getDonationById(id: String): DonationResponse? = runBlocking {
        val donation = donationRepository.findById(id)
        donation?.let { DonationResponse.fromEntity(it) }
    }

    override fun listDonations(
        page: Int,
        pageSize: Int,
        status: DonationStatus?,
        donationType: DonationType?,
        sortBy: String,
        sortDirection: String
    ): DonationPageResponse = runBlocking {
        val (donations, totalElements) = donationRepository.findAll(
            page = page,
            pageSize = pageSize,
            status = status,
            donationType = donationType,
            sortBy = sortBy,
            sortDirection = sortDirection
        )

        val totalPages = ceil(totalElements.toDouble() / pageSize).toInt()

        DonationPageResponse(
            donations = donations.map { DonationResponse.fromEntity(it) },
            page = page,
            pageSize = pageSize,
            totalElements = totalElements,
            totalPages = totalPages,
            hasNext = page < totalPages - 1,
            hasPrevious = page > 0
        )
    }

    override fun getDonationsByTargetId(targetId: String): List<DonationResponse> = runBlocking {
        val donations = donationRepository.findByTargetId(targetId)
        donations.map { DonationResponse.fromEntity(it) }
    }

    override fun createPixCharge(donationId: String): CreatePixChargeResponse? = runBlocking {
        logger.info("Creating PIX charge for donation: $donationId")

        val donation = donationRepository.findById(donationId)
            ?: throw IllegalArgumentException("Donation not found: $donationId")

        if (donation.status != DonationStatus.PENDING) {
            throw IllegalStateException("Donation is not in PENDING status: ${donation.status}")
        }

        if (donation.paymentMethod != PaymentMethod.PIX) {
            throw IllegalArgumentException("Payment method is not PIX: ${donation.paymentMethod}")
        }

        try {
            val targetDescription = getTargetName(donation.donationType, donation.targetId)
            
            val pagBankRequest = mapOf(
                "amount" to (donation.amount * 100).toLong(),
                "description" to "Doação para: $targetDescription",
                "reference_id" to donationId,
                "customer" to buildCustomerData(donation),
                "notification_urls" to listOf("${getWebhookUrl()}/api/webhooks/pagbank")
            )

            val response = pagBankProvider.createPixTransaction(pagBankRequest)
            
            val chargeId = response["id"] as? String
                ?: throw IllegalStateException("PagBank did not return charge ID")
            
            val qrCodeText = extractQrCodeText(response)
            val qrCodeBase64 = extractQrCodeBase64(response)
            val expiresAt = extractExpiresAt(response)

            val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
            
            donationRepository.update(
                donationId,
                mapOf(
                    "pagbankChargeId" to chargeId,
                    "qrCodeText" to qrCodeText,
                    "qrCodeImageBase64" to qrCodeBase64,
                    "expiresAt" to expiresAt,
                    "updatedAt" to now
                )
            )

            logger.info("PIX charge created successfully - donationId: $donationId, chargeId: $chargeId")

            CreatePixChargeResponse(
                donationId = donationId,
                chargeId = chargeId,
                qrCodeText = qrCodeText,
                qrCodeImageBase64 = qrCodeBase64,
                expiresAt = expiresAt,
                amount = donation.amount,
                targetDescription = targetDescription
            )
        } catch (e: Exception) {
            logger.error("Error creating PIX charge for donation $donationId: ${e.message}", e)
            throw e
        }
    }

    override fun processWebhook(payload: PagBankWebhookPayload): WebhookProcessingResult = runBlocking {
        logger.info("Processing PagBank webhook - notificationId: ${payload.notificationId}")

        try {
            val charge = payload.charges?.firstOrNull()
                ?: return@runBlocking WebhookProcessingResult(
                    success = false,
                    donationId = null,
                    previousStatus = null,
                    newStatus = null,
                    processed = false,
                    message = "No charge data in webhook payload"
                )

            val chargeId = charge.id
                ?: return@runBlocking WebhookProcessingResult(
                    success = false,
                    donationId = null,
                    previousStatus = null,
                    newStatus = null,
                    processed = false,
                    message = "No charge ID in webhook payload"
                )

            val donation = donationRepository.findByPagbankChargeId(chargeId)
                ?: return@runBlocking WebhookProcessingResult(
                    success = false,
                    donationId = null,
                    previousStatus = null,
                    newStatus = null,
                    processed = false,
                    message = "Donation not found for chargeId: $chargeId"
                )

            val previousStatus = donation.status
            val newStatus = mapPagBankStatus(charge.status ?: "")
            
            if (previousStatus == newStatus) {
                logger.info("Donation ${donation.id} already has status $newStatus, skipping update")
                return@runBlocking WebhookProcessingResult(
                    success = true,
                    donationId = donation.id,
                    previousStatus = previousStatus,
                    newStatus = newStatus,
                    processed = false,
                    message = "Status unchanged"
                )
            }

            val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
            val updates = mutableMapOf<String, Any>(
                "status" to newStatus.name,
                "updatedAt" to now,
                "webhookData" to payload.toString()
            )

            if (newStatus == DonationStatus.PAID) {
                updates["paidAt"] = charge.paidAt ?: now
            }

            if (newStatus == DonationStatus.CANCELLED) {
                updates["cancelledAt"] = now
            }

            if (newStatus == DonationStatus.REFUNDED) {
                updates["refundedAt"] = now
            }

            donationRepository.update(donation.id, updates)

            var processed = false
            if (newStatus == DonationStatus.PAID && donation.processedAt == null) {
                processed = processPaidDonation(donation.id)
            }

            logger.info("Webhook processed - donationId: ${donation.id}, ${previousStatus.name} -> ${newStatus.name}, processed: $processed")

            WebhookProcessingResult(
                success = true,
                donationId = donation.id,
                previousStatus = previousStatus,
                newStatus = newStatus,
                processed = processed,
                message = "Webhook processed successfully"
            )
        } catch (e: Exception) {
            logger.error("Error processing webhook: ${e.message}", e)
            WebhookProcessingResult(
                success = false,
                donationId = null,
                previousStatus = null,
                newStatus = null,
                processed = false,
                message = "Error: ${e.message}"
            )
        }
    }

    override fun processPaidDonation(donationId: String): Boolean = runBlocking {
        logger.info("Processing paid donation: $donationId")

        try {
            val donation = donationRepository.findById(donationId)
                ?: throw IllegalArgumentException("Donation not found: $donationId")

            if (donation.status != DonationStatus.PAID) {
                throw IllegalStateException("Donation is not PAID: ${donation.status}")
            }

            if (donation.processedAt != null) {
                logger.warn("Donation $donationId already processed at ${donation.processedAt}")
                return@runBlocking false
            }

            when (donation.donationType) {
                DonationType.GOAL -> processGoalDonation(donation)
                DonationType.RAFFLE -> processRaffleDonation(donation)
                DonationType.ORDER -> processOrderDonation(donation)
            }

            val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
            donationRepository.update(donationId, mapOf("processedAt" to now))

            logger.info("Donation $donationId processed successfully")
            true
        } catch (e: Exception) {
            logger.error("Error processing paid donation $donationId: ${e.message}", e)
            false
        }
    }

    private fun processGoalDonation(donation: Donation) {
        logger.info("Processing GOAL donation - goalId: ${donation.targetId}, amount: ${donation.amount}")
        goalService.addAmount(donation.targetId, donation.amount)
    }

    private fun processRaffleDonation(donation: Donation) {
        logger.info("Processing RAFFLE donation - raffleId: ${donation.targetId}, amount: ${donation.amount}")
        logger.warn("RAFFLE processing not implemented yet")
    }

    private fun processOrderDonation(donation: Donation) {
        logger.info("Processing ORDER donation - orderId: ${donation.targetId}, amount: ${donation.amount}")
        logger.warn("ORDER processing not implemented yet")
    }

    private suspend fun validateTarget(donationType: DonationType, targetId: String) {
        when (donationType) {
            DonationType.GOAL -> {
                val goal = goalService.getById(targetId)
                    ?: throw IllegalArgumentException("Goal not found: $targetId")
                if (!goal.active) {
                    throw IllegalArgumentException("Goal is not active: $targetId")
                }
            }
            DonationType.RAFFLE -> {
                logger.warn("RAFFLE validation not implemented yet")
            }
            DonationType.ORDER -> {
                logger.warn("ORDER validation not implemented yet")
            }
        }
    }

    private fun getTargetName(donationType: DonationType, targetId: String): String {
        return when (donationType) {
            DonationType.GOAL -> {
                goalService.getById(targetId)?.title ?: "Meta $targetId"
            }
            DonationType.RAFFLE -> "Rifa $targetId"
            DonationType.ORDER -> "Pedido $targetId"
        }
    }

    private fun buildCustomerData(donation: Donation): Map<String, Any> {
        val customer = mutableMapOf<String, Any>()
        
        donation.donorName?.let { customer["name"] = it }
        donation.donorEmail?.let { customer["email"] = it }
        donation.donorPhone?.let { customer["phone"] = it }
        donation.donorDocument?.let { customer["tax_id"] = it }
        
        return customer
    }

    private fun extractQrCodeText(response: Map<String, Any>): String {
        val qrCodes = response["qr_codes"] as? List<*>
        val qrCode = qrCodes?.firstOrNull() as? Map<*, *>
        return qrCode?.get("text") as? String ?: ""
    }

    private fun extractQrCodeBase64(response: Map<String, Any>): String {
        val qrCodes = response["qr_codes"] as? List<*>
        val qrCode = qrCodes?.firstOrNull() as? Map<*, *>
        val links = qrCode?.get("links") as? List<*>
        val pngLink = links?.firstOrNull() as? Map<*, *>
        return pngLink?.get("href") as? String ?: ""
    }

    private fun extractExpiresAt(response: Map<String, Any>): String {
        val qrCodes = response["qr_codes"] as? List<*>
        val qrCode = qrCodes?.firstOrNull() as? Map<*, *>
        return qrCode?.get("expiration_date") as? String ?: ""
    }

    private fun mapPagBankStatus(status: String): DonationStatus {
        return when (status.uppercase()) {
            "PAID" -> DonationStatus.PAID
            "WAITING" -> DonationStatus.PENDING
            "DECLINED", "CANCELED" -> DonationStatus.CANCELLED
            "REFUNDED" -> DonationStatus.REFUNDED
            else -> DonationStatus.FAILED
        }
    }

    private fun getWebhookUrl(): String {
        return System.getenv("WEBHOOK_BASE_URL") ?: "https://tyler-api-production.com"
    }
}
