package com.tylerproject.domain.raffle

import com.tylerproject.domain.donation.PagBankWebhookPayload
import com.tylerproject.domain.donation.WebhookProcessingResult
import com.tylerproject.providers.PagBankProvider
import com.tylerproject.service.ImageUploadService
import com.tylerproject.util.CryptoUtils
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.ceil
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class RaffleServiceImpl(
        private val raffleRepository: RaffleRepository,
        private val raffleTicketRepository: RaffleTicketRepository,
        private val imageUploadService: ImageUploadService,
        private val pagBankProvider: PagBankProvider,
        @Value("\${app.base-url:http://localhost:8080}") private val baseUrl: String
) : RaffleService {

    private val logger = LoggerFactory.getLogger(RaffleServiceImpl::class.java)

    override fun createRaffle(request: CreateRaffleRequest, createdByUid: String): RaffleResponse {
        logger.info("Creating raffle: ${request.title}")

        val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)

        // Gerar entropy e commitment
        val revealEntropy = CryptoUtils.generateRandomEntropy()
        val committedEntropy = CryptoUtils.sha256(revealEntropy)

        val raffle =
                Raffle(
                        id = "",
                        title = request.title,
                        description = request.description,
                        prize = request.prize,
                        ticketPrice = request.ticketPrice,
                        totalTickets = request.totalTickets,
                        soldTickets = 0,
                        deadline = request.deadline,
                        status = RaffleStatus.ACTIVE,
                        images = emptyList(),
                        committedEntropy = committedEntropy,
                        revealEntropy = null, // Mantém secreto até sorteio
                        winnerTicketNumber = null,
                        goalId = request.goalId,
                        active = request.active,
                        createdAt = now,
                        updatedAt = now,
                        createdBy = createdByUid
                )

        val savedRaffle = raffleRepository.save(raffle)

        // IMPORTANTE: Salvar revealEntropy em local seguro (arquivo, secrets manager)
        // Por enquanto, logando apenas para admin poder recuperar
        logger.warn(
                "ADMIN: Store this reveal entropy securely for raffle ${savedRaffle.id}: $revealEntropy"
        )

        logger.info("Raffle created: ${savedRaffle.id}")
        return RaffleResponse.fromEntity(savedRaffle)
    }

    override fun updateRaffle(id: String, request: UpdateRaffleRequest): RaffleResponse? {
        logger.info("Updating raffle: $id")

        val raffle = raffleRepository.findById(id) ?: return null

        val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
        val updates = mutableMapOf<String, Any>("updatedAt" to now)

        request.title?.let { updates["title"] = it }
        request.description?.let { updates["description"] = it }
        request.prize?.let { updates["prize"] = it }
        request.ticketPrice?.let { updates["ticketPrice"] = it }
        request.totalTickets?.let {
            if (it >= raffle.soldTickets) {
                updates["totalTickets"] = it
            } else {
                throw IllegalArgumentException(
                        "Não pode reduzir totalTickets abaixo de soldTickets (${raffle.soldTickets})"
                )
            }
        }
        request.deadline?.let { updates["deadline"] = it }
        request.status?.let { updates["status"] = it.name }
        request.goalId?.let { updates["goalId"] = it }
        request.active?.let { updates["active"] = it }

        val updatedRaffle = raffleRepository.update(id, updates)
        logger.info("Raffle updated: $id")
        return updatedRaffle?.let { RaffleResponse.fromEntity(it) }
    }

    override fun getRaffleById(id: String): RaffleResponse? {
        val raffle = raffleRepository.findById(id)
        return raffle?.let { RaffleResponse.fromEntity(it) }
    }

    override fun listRaffles(
            page: Int,
            pageSize: Int,
            status: RaffleStatus?,
            activeOnly: Boolean,
            sortBy: String,
            sortDirection: String,
            searchTerm: String?
    ): RafflePageResponse {
        val (raffles, totalElements) =
                if (searchTerm.isNullOrBlank()) {
                    raffleRepository.findAll(
                            page,
                            pageSize,
                            status,
                            activeOnly,
                            sortBy,
                            sortDirection
                    )
                } else {
                    raffleRepository.search(searchTerm, status, activeOnly, page, pageSize)
                }

        val totalPages = ceil(totalElements.toDouble() / pageSize).toInt()

        return RafflePageResponse(
                raffles = raffles.map { RaffleResponse.fromEntity(it) },
                page = page,
                pageSize = pageSize,
                totalElements = totalElements,
                totalPages = totalPages,
                hasNext = page < totalPages - 1,
                hasPrevious = page > 0
        )
    }

    override fun deleteRaffle(id: String): Boolean {
        logger.info("Soft deleting raffle: $id")

        val raffle = raffleRepository.findById(id) ?: return false

        // Se tem bilhetes vendidos, não pode deletar - deve cancelar
        if (raffle.soldTickets > 0) {
            throw IllegalStateException(
                    "Rifa com bilhetes vendidos não pode ser deletada. Use o endpoint de cancelamento."
            )
        }

        raffleRepository.softDelete(id)
        logger.info("Raffle soft deleted: $id")
        return true
    }

    override fun uploadImages(id: String, files: Array<MultipartFile>): List<String> {
        logger.info("Uploading ${files.size} images for raffle: $id")

        val raffle =
                raffleRepository.findById(id)
                        ?: throw IllegalArgumentException("Raffle not found: $id")

        if (files.size > 10) {
            throw IllegalArgumentException("Máximo de 10 imagens permitidas por rifa")
        }

        if (raffle.images.size + files.size > 10) {
            throw IllegalArgumentException(
                    "Esta rifa já tem ${raffle.images.size} imagens. Máximo total: 10"
            )
        }

        val newImageUrls =
                files.mapIndexed { index, file ->
                    val currentImageCount = raffle.images.size
                    imageUploadService.uploadRaffleImage(id, file, currentImageCount + index).url
                }

        val allImages = raffle.images + newImageUrls
        val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)

        raffleRepository.update(id, mapOf("images" to allImages, "updatedAt" to now))

        logger.info("Images uploaded successfully for raffle: $id")
        return newImageUrls
    }

    override fun deleteImage(id: String, imageIndex: Int): Boolean {
        logger.info("Deleting image $imageIndex from raffle: $id")

        val raffle =
                raffleRepository.findById(id)
                        ?: throw IllegalArgumentException("Raffle not found: $id")

        if (imageIndex < 0 || imageIndex >= raffle.images.size) {
            throw IllegalArgumentException("Índice de imagem inválido: $imageIndex")
        }

        val imageUrl = raffle.images[imageIndex]
        imageUploadService.deleteGoalImageByUrl(imageUrl) // Reutiliza método de Goals

        val updatedImages = raffle.images.toMutableList().apply { removeAt(imageIndex) }
        val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)

        raffleRepository.update(id, mapOf("images" to updatedImages, "updatedAt" to now))

        logger.info("Image deleted successfully from raffle: $id")
        return true
    }

    override fun purchaseTickets(
            raffleId: String,
            request: TicketPurchaseRequest
    ): TicketPurchaseResponse {
        logger.info(
                "Processing ticket purchase for raffle: $raffleId, quantity: ${request.quantity}"
        )

        val raffle =
                raffleRepository.findById(raffleId)
                        ?: throw IllegalArgumentException("Rifa não encontrada")

        // Validações
        if (raffle.status != RaffleStatus.ACTIVE) {
            throw IllegalStateException(
                    "Esta rifa não está mais aceitando compras (status: ${raffle.status})"
            )
        }

        val now = LocalDateTime.now()
        val deadline =
                LocalDateTime.parse(
                        raffle.deadline,
                        DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC)
                )
        if (now.isAfter(deadline)) {
            throw IllegalStateException("O prazo para compra de bilhetes desta rifa expirou")
        }

        if (request.quantity > raffle.remainingTickets) {
            throw IllegalArgumentException("Apenas ${raffle.remainingTickets} bilhetes disponíveis")
        }

        // Alocar números de bilhetes
        val ticketNumbers =
                raffleTicketRepository.getAvailableTicketNumbers(
                        raffleId,
                        raffle.totalTickets,
                        request.quantity
                )

        if (ticketNumbers.size < request.quantity) {
            throw IllegalStateException(
                    "Não foi possível alocar ${request.quantity} bilhetes sequenciais"
            )
        }

        // Criar cobrança PIX para o total
        val totalAmount = raffle.ticketPrice * request.quantity
        val purchaseId = java.util.UUID.randomUUID().toString()

        val pixResponse = createPixChargeForTickets(raffle, purchaseId, totalAmount, request)

        // Criar tickets com status PENDING
        val nowIso = now.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
        val tickets =
                ticketNumbers.map { ticketNumber ->
                    RaffleTicket(
                            id = "",
                            raffleId = raffleId,
                            ticketNumber = ticketNumber,
                            buyerName = request.buyerName,
                            buyerEmail = request.buyerEmail,
                            buyerPhone = request.buyerPhone,
                            buyerDocument = request.buyerDocument,
                            paymentId = pixResponse["chargeId"] as String,
                            paymentStatus = "PENDING",
                            purchasedAt = nowIso
                    )
                }

        raffleTicketRepository.saveAll(tickets)

        logger.info("Tickets purchased successfully - raffle: $raffleId, tickets: $ticketNumbers")

        return TicketPurchaseResponse(
                purchaseId = purchaseId,
                raffleId = raffleId,
                ticketNumbers = ticketNumbers,
                totalAmount = totalAmount,
                qrCodeText = pixResponse["qrCodeText"] as String,
                qrCodeImageBase64 = pixResponse["qrCodeBase64"] as String,
                paymentId = pixResponse["chargeId"] as String,
                expiresAt = pixResponse["expiresAt"] as String
        )
    }

    override fun getRaffleTickets(raffleId: String): List<RaffleTicketResponse> {
        val tickets = raffleTicketRepository.findByRaffleId(raffleId)
        return tickets.map { RaffleTicketResponse.fromEntity(it) }
    }

    override fun getAvailableTickets(raffleId: String): List<Int> {
        val raffle =
                raffleRepository.findById(raffleId)
                        ?: throw IllegalArgumentException("Rifa não encontrada")

        val soldNumbers = raffleTicketRepository.findTicketNumbersByRaffleId(raffleId).toSet()
        return (1..raffle.totalTickets).filter { it !in soldNumbers }
    }

    override fun drawRaffle(
            raffleId: String,
            request: RaffleDrawRequest,
            adminUid: String
    ): RaffleDrawResponse {
        logger.info("Drawing raffle: $raffleId")

        val raffle =
                raffleRepository.findById(raffleId)
                        ?: throw IllegalArgumentException("Rifa não encontrada")

        // Validações
        if (raffle.status != RaffleStatus.ENDED) {
            throw IllegalStateException(
                    "Apenas rifas com status ENDED podem ser sorteadas. Status atual: ${raffle.status}"
            )
        }

        if (raffle.winnerTicketNumber != null) {
            throw IllegalStateException(
                    "Esta rifa já foi sorteada! Vencedor: bilhete #${raffle.winnerTicketNumber}"
            )
        }

        // Verificar entropy
        val committedHash =
                raffle.committedEntropy
                        ?: throw IllegalStateException("Rifa sem entropy comprometida")

        if (!CryptoUtils.verifyEntropy(request.revealEntropy, committedHash)) {
            throw IllegalStateException(
                    "Entropy revelada não corresponde ao hash comprometido! Possível fraude detectada."
            )
        }

        // Calcular vencedor deterministicamente
        val winnerNumber =
                CryptoUtils.calculateWinnerNumber(
                        request.revealEntropy,
                        raffleId,
                        raffle.totalTickets
                )

        // Buscar ticket vencedor
        val winnerTicket =
                raffleTicketRepository.findByRaffleIdAndTicketNumber(raffleId, winnerNumber)

        // Atualizar rifa
        val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
        raffleRepository.update(
                raffleId,
                mapOf(
                        "status" to RaffleStatus.DRAWN.name,
                        "winnerTicketNumber" to winnerNumber,
                        "revealEntropy" to request.revealEntropy,
                        "updatedAt" to now
                )
        )

        logger.info("Raffle drawn successfully - raffle: $raffleId, winner: $winnerNumber")

        return RaffleDrawResponse(
                raffleId = raffleId,
                winnerTicketNumber = winnerNumber,
                winnerTicket = winnerTicket?.let { RaffleTicketResponse.fromEntity(it) },
                verificationHash = committedHash,
                drawnAt = now
        )
    }

    override fun cancelRaffle(raffleId: String, adminUid: String): Boolean {
        logger.info("Cancelling raffle: $raffleId")

        val raffle =
                raffleRepository.findById(raffleId)
                        ?: throw IllegalArgumentException("Rifa não encontrada")

        if (raffle.status == RaffleStatus.DRAWN) {
            throw IllegalStateException("Rifa já sorteada não pode ser cancelada")
        }

        if (raffle.status == RaffleStatus.CANCELLED) {
            throw IllegalStateException("Rifa já está cancelada")
        }

        // Atualizar status
        val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
        raffleRepository.update(
                raffleId,
                mapOf(
                        "status" to RaffleStatus.CANCELLED.name,
                        "active" to false,
                        "updatedAt" to now
                )
        )

        logger.info("Raffle cancelled: $raffleId (tickets will not be refunded)")
        return true
    }

    override fun verifyDraw(raffleId: String): DrawVerificationResponse {
        val raffle =
                raffleRepository.findById(raffleId)
                        ?: throw IllegalArgumentException("Rifa não encontrada")

        if (raffle.status != RaffleStatus.DRAWN) {
            throw IllegalStateException("Rifa ainda não foi sorteada")
        }

        val revealedEntropy =
                raffle.revealEntropy ?: throw IllegalStateException("Entropy não foi revelada")

        val committedHash =
                raffle.committedEntropy
                        ?: throw IllegalStateException("Hash comprometido não encontrado")

        val verificationPassed = CryptoUtils.verifyEntropy(revealedEntropy, committedHash)

        val recalculatedWinner =
                CryptoUtils.calculateWinnerNumber(revealedEntropy, raffleId, raffle.totalTickets)

        val message =
                if (verificationPassed && recalculatedWinner == raffle.winnerTicketNumber) {
                    "✅ Sorteio verificado com sucesso! O resultado é auditável e transparente."
                } else {
                    "❌ ALERTA: Sorteio falhou na verificação! Possível manipulação detectada."
                }

        return DrawVerificationResponse(
                raffleId = raffleId,
                committedEntropyHash = committedHash,
                revealedEntropy = revealedEntropy,
                winnerTicketNumber = raffle.winnerTicketNumber ?: 0,
                verificationPassed =
                        verificationPassed && recalculatedWinner == raffle.winnerTicketNumber,
                message = message
        )
    }

    @Scheduled(cron = "0 */15 * * * *") // A cada 15 minutos
    override fun updateExpiredRaffles(): Int {
        logger.info("Running scheduled task: updateExpiredRaffles")

        val expiredRaffles = raffleRepository.findExpiredRaffles()

        expiredRaffles.forEach { raffle ->
            val now =
                    LocalDateTime.now()
                            .atOffset(ZoneOffset.UTC)
                            .format(DateTimeFormatter.ISO_INSTANT)
            raffleRepository.update(
                    raffle.id,
                    mapOf("status" to RaffleStatus.ENDED.name, "updatedAt" to now)
            )
            logger.info("Raffle ${raffle.id} status updated to ENDED (deadline expired)")
        }

        logger.info("Updated ${expiredRaffles.size} expired raffles")
        return expiredRaffles.size
    }

    // ==================== PRIVATE HELPERS ====================

    private fun createPixChargeForTickets(
            raffle: Raffle,
            purchaseId: String,
            totalAmount: Double,
            request: TicketPurchaseRequest
    ): Map<String, Any> = runBlocking {
        val pagBankRequest =
                mapOf(
                        "amount" to (totalAmount * 100).toLong(),
                        "description" to "Rifa: ${raffle.title} - ${request.quantity} bilhete(s)",
                        "reference_id" to purchaseId,
                        "customer" to
                                mapOf(
                                        "name" to request.buyerName,
                                        "email" to request.buyerEmail,
                                        "tax_id" to (request.buyerDocument ?: ""),
                                        "phones" to
                                                listOfNotNull(
                                                        request.buyerPhone?.let {
                                                            mapOf(
                                                                    "country" to "55",
                                                                    "area" to it.substring(0, 2),
                                                                    "number" to it.substring(2)
                                                            )
                                                        }
                                                )
                                ),
                        "notification_urls" to listOf("$baseUrl/api/webhooks/pagbank/raffle")
                )

        val response = pagBankProvider.createPixTransaction(pagBankRequest)

        val chargeId =
                response["id"] as? String
                        ?: throw IllegalStateException("PagBank não retornou charge ID")

        mapOf(
                "chargeId" to chargeId,
                "qrCodeText" to extractQrCodeText(response),
                "qrCodeBase64" to extractQrCodeBase64(response),
                "expiresAt" to extractExpiresAt(response)
        )
    }

    private fun extractQrCodeText(response: Map<String, Any>): String {
        val qrCodes = response["qr_codes"] as? List<Map<String, Any>>
        return qrCodes?.firstOrNull()?.get("text") as? String
                ?: throw IllegalStateException("QR Code text not found")
    }

    private fun extractQrCodeBase64(response: Map<String, Any>): String {
        val qrCodes = response["qr_codes"] as? List<Map<String, Any>>
        val links = qrCodes?.firstOrNull()?.get("links") as? List<Map<String, Any>>
        val base64Link = links?.find { it["media"] == "image/png" }
        return base64Link?.get("href") as? String
                ?: throw IllegalStateException("QR Code base64 not found")
    }

    private fun extractExpiresAt(response: Map<String, Any>): String {
        return response["expires_at"] as? String
                ?: LocalDateTime.now()
                        .plusHours(24)
                        .atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_INSTANT)
    }

    override fun processWebhook(payload: PagBankWebhookPayload): WebhookProcessingResult {
        logger.info("🎰 Processing raffle webhook - notificationId: ${payload.notificationId}")

        try {
            val charge =
                    payload.charges?.firstOrNull()
                            ?: return WebhookProcessingResult(
                                    success = false,
                                    donationId = null,
                                    previousStatus = null,
                                    newStatus = null,
                                    processed = false,
                                    message = "No charge data in webhook payload"
                            )

            val chargeId =
                    charge.id
                            ?: return WebhookProcessingResult(
                                    success = false,
                                    donationId = null,
                                    previousStatus = null,
                                    newStatus = null,
                                    processed = false,
                                    message = "No charge ID in webhook payload"
                            )

            // Buscar tickets pelo paymentId
            val tickets = raffleTicketRepository.findByPaymentId(chargeId)
            if (tickets.isEmpty()) {
                return WebhookProcessingResult(
                        success = false,
                        donationId = null,
                        previousStatus = null,
                        newStatus = null,
                        processed = false,
                        message = "No tickets found for chargeId: $chargeId"
                )
            }

            val raffleId = tickets.first().raffleId
            val raffle =
                    raffleRepository.findById(raffleId)
                            ?: return WebhookProcessingResult(
                                    success = false,
                                    donationId = null,
                                    previousStatus = null,
                                    newStatus = null,
                                    processed = false,
                                    message = "Raffle not found: $raffleId"
                            )

            // Validar valor pago vs quantidade de tickets
            val amountPaid = (charge.amount?.value ?: 0) / 100.0 // PagBank envia em centavos
            val expectedAmount = tickets.size * raffle.ticketPrice
            val amountDifference =
                    if (amountPaid > expectedAmount) amountPaid - expectedAmount
                    else expectedAmount - amountPaid

            if (amountDifference > 0.01) { // Tolerância de 1 centavo
                logger.error("❌ Amount mismatch - paid: $amountPaid, expected: $expectedAmount")
                return WebhookProcessingResult(
                        success = false,
                        donationId = raffleId,
                        previousStatus = null,
                        newStatus = null,
                        processed = false,
                        message =
                                "Payment amount mismatch - paid: R$ $amountPaid, expected: R$ $expectedAmount"
                )
            }

            val chargeStatus = charge.status ?: ""
            val previousPaymentStatus = tickets.first().paymentStatus
            val newPaymentStatus = mapPagBankStatusToPaymentStatus(chargeStatus)

            if (previousPaymentStatus == newPaymentStatus) {
                logger.info("Tickets already have status $newPaymentStatus, skipping update")
                return WebhookProcessingResult(
                        success = true,
                        donationId = raffleId,
                        previousStatus = mapPaymentStatusToDonationStatus(previousPaymentStatus),
                        newStatus = mapPaymentStatusToDonationStatus(newPaymentStatus),
                        processed = false,
                        message = "Status unchanged"
                )
            }

            // Atualizar todos os tickets
            val now =
                    LocalDateTime.now()
                            .atOffset(ZoneOffset.UTC)
                            .format(DateTimeFormatter.ISO_INSTANT)
            tickets.forEach { ticket ->
                raffleTicketRepository.update(
                        ticket.id,
                        mapOf(
                                "paymentStatus" to newPaymentStatus,
                                "purchasedAt" to
                                        (if (newPaymentStatus == "PAID") (charge.paidAt ?: now)
                                        else ticket.purchasedAt)
                        )
                )
            }

            logger.info(
                    "✅ Raffle webhook processed - raffleId: $raffleId, ${tickets.size} tickets updated: $previousPaymentStatus -> $newPaymentStatus"
            )

            return WebhookProcessingResult(
                    success = true,
                    donationId = raffleId,
                    previousStatus = mapPaymentStatusToDonationStatus(previousPaymentStatus),
                    newStatus = mapPaymentStatusToDonationStatus(newPaymentStatus),
                    processed = newPaymentStatus == "PAID",
                    message = "Raffle webhook processed - ${tickets.size} tickets updated"
            )
        } catch (e: Exception) {
            logger.error("Error processing raffle webhook: ${e.message}", e)
            return WebhookProcessingResult(
                    success = false,
                    donationId = null,
                    previousStatus = null,
                    newStatus = null,
                    processed = false,
                    message = "Error: ${e.message}"
            )
        }
    }

    private fun mapPagBankStatusToPaymentStatus(pagBankStatus: String): String {
        return when (pagBankStatus.uppercase()) {
            "PAID" -> "PAID"
            "WAITING" -> "PENDING"
            "DECLINED", "CANCELED" -> "FAILED"
            else -> {
                logger.warn("Unknown PagBank status: $pagBankStatus, defaulting to PENDING")
                "PENDING"
            }
        }
    }

    private fun mapPaymentStatusToDonationStatus(
            paymentStatus: String
    ): com.tylerproject.domain.donation.DonationStatus {
        return when (paymentStatus.uppercase()) {
            "PAID" -> com.tylerproject.domain.donation.DonationStatus.PAID
            "PENDING" -> com.tylerproject.domain.donation.DonationStatus.PENDING
            "FAILED" -> com.tylerproject.domain.donation.DonationStatus.FAILED
            "REFUNDED" -> com.tylerproject.domain.donation.DonationStatus.REFUNDED
            else -> com.tylerproject.domain.donation.DonationStatus.PENDING
        }
    }
}
