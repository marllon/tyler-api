package com.tylerproject.domain.pagbank

import com.tylerproject.domain.donation.DonationService
import com.tylerproject.domain.donation.PagBankWebhookPayload
import com.tylerproject.domain.raffle.RaffleService
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
        private val raffleService: RaffleService
) {
    private val logger = LoggerFactory.getLogger(PagBankWebhookController::class.java)

    @PostMapping("/pagbank")
    @Operation(
            summary = "Webhook do PagBank",
            description =
                    "Endpoint chamado pelo PagBank quando há mudança no status de um pagamento. Processa automaticamente doações, rifas e outros pagamentos."
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
