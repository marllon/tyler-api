package com.tylerproject.domain.pagbank

import com.tylerproject.domain.donation.DonationService
import com.tylerproject.domain.donation.PagBankWebhookPayload
import com.tylerproject.domain.raffle.RaffleService
import com.tylerproject.domain.order.OrderService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/webhooks")
@CrossOrigin(origins = ["*"])
@Tag(
        name = "🔔 Webhooks",
        description = "Endpoints para receber notificações de pagamento do PagBank"
)
class PagBankWebhookController(
        private val donationService: DonationService,
        private val raffleService: RaffleService,
        private val orderService: OrderService
) {
    private val logger = LoggerFactory.getLogger(PagBankWebhookController::class.java)

    @PostMapping("/pagbank")
    @Operation(
            summary = "Webhook do PagBank",
            description =
                    "Endpoint chamado pelo PagBank quando há mudança no status de um pagamento. Processa automaticamente doações, rifas, pedidos e outros pagamentos."
    )
    fun handlePagBankWebhook(
            @RequestBody payload: PagBankWebhookPayload
    ): ResponseEntity<WebhookResponse> {
        return try {
            logger.info("📨 Received PagBank webhook - notificationId: ${payload.notificationId}")
            logger.debug("Webhook payload: $payload")

            // Identificar tipo de pagamento pelo reference_id
            val referenceId = payload.referenceId ?: payload.charges?.firstOrNull()?.referenceId

            val result =
                    when {
                        referenceId?.startsWith("ORD-") == true -> {
                            logger.info("🛒 Processing order payment webhook - orderNumber: $referenceId")
                            processOrderWebhook(payload, referenceId)
                        }
                        referenceId?.startsWith("raffle_") == true -> {
                            logger.info("🎰 Processing raffle payment webhook")
                            raffleService.processWebhook(payload)
                        }
                        else -> {
                            logger.info("❤️ Processing donation webhook")
                            donationService.processWebhook(payload)
                        }
                    }

            val response =
                    WebhookResponse(
                            success = result.success,
                            message = result.message,
                            donationId = result.donationId,
                            previousStatus = result.previousStatus?.name,
                            newStatus = result.newStatus?.name,
                            processed = result.processed
                    )

            if (result.success) {
                logger.info(
                        "✅ Webhook processed successfully - entity: ${result.donationId}, status: ${result.previousStatus?.name} -> ${result.newStatus?.name}"
                )
                ResponseEntity.ok(response)
            } else {
                logger.warn("⚠️ Webhook processing failed: ${result.message}")
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
            }
        } catch (e: Exception) {
            logger.error("❌ Error processing PagBank webhook: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            WebhookResponse(
                                    success = false,
                                    message = "Internal error: ${e.message}"
                            )
                    )
        }
    }

    private fun processOrderWebhook(
            payload: PagBankWebhookPayload,
            orderNumber: String
    ): com.tylerproject.domain.donation.WebhookProcessingResult {
        val charge = payload.charges?.firstOrNull()
        val status = charge?.status ?: ""
        val paymentId = charge?.id

        return try {
            if (status.uppercase() == "PAID" && paymentId != null) {
                // OrderService.processOrderPayment usa paymentId para encontrar e processar o pedido
                val processed = orderService.processOrderPayment(paymentId)
                
                com.tylerproject.domain.donation.WebhookProcessingResult(
                        success = true,
                        donationId = orderNumber,
                        previousStatus = com.tylerproject.domain.donation.DonationStatus.PENDING,
                        newStatus = com.tylerproject.domain.donation.DonationStatus.PAID,
                        processed = processed,
                        message = "Order payment processed successfully"
                )
            } else {
                logger.info("Order $orderNumber status: $status (no action needed)")
                com.tylerproject.domain.donation.WebhookProcessingResult(
                        success = true,
                        donationId = orderNumber,
                        previousStatus = null,
                        newStatus = null,
                        processed = false,
                        message = "Status not processed: $status"
                )
            }
        } catch (e: Exception) {
            logger.error("Error processing order webhook: ${e.message}", e)
            com.tylerproject.domain.donation.WebhookProcessingResult(
                    success = false,
                    donationId = orderNumber,
                    previousStatus = null,
                    newStatus = null,
                    processed = false,
                    message = "Error: ${e.message}"
            )
        }
    }

    @GetMapping("/pagbank/health")
    @Operation(
            summary = "Health check do webhook",
            description = "Endpoint para verificar se o webhook está funcionando"
    )
    fun webhookHealth(): ResponseEntity<HealthCheckResponse> {
        return ResponseEntity.ok(
                HealthCheckResponse(
                        status = "healthy",
                        webhook = "pagbank",
                        timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                )
        )
    }
}
