package com.tylerproject.controllers
import com.tylerproject.providers.PagBankProvider
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = ["*"])
@Tag(
        name = "💳 Payments",
        description = "API de pagamentos PIX via PagBank para doações e checkout"
)
class PaymentController @Autowired constructor(private val pagBankProvider: PagBankProvider) {
        private val logger = LoggerFactory.getLogger(PaymentController::class.java)
        @PostMapping("/checkout")
        @Operation(
                summary = "💰 Criar checkout PIX para doação",
                description = "Cria um checkout PIX via PagBank para processar doações",
                tags = ["PIX", "Checkout"]
        )
        @ApiResponses(
                value =
                        [
                                ApiResponse(
                                        responseCode = "200",
                                        description = "✅ Checkout criado com sucesso"
                                ),
                                ApiResponse(
                                        responseCode = "400",
                                        description = "❌ Dados inválidos"
                                ),
                                ApiResponse(
                                        responseCode = "500",
                                        description = "❌ Erro no processamento"
                                )]
        )
        fun createCheckout(@RequestBody request: Map<String, Any>): ResponseEntity<*> {
                return try {
                        val amount =
                                (request["amount"] as? Number)?.toLong()
                                        ?: throw IllegalArgumentException("Valor inválido")
                        logger.info("🎯 Criando doação PIX para valor: R$ ${amount / 100.0}")
                        @Suppress("UNCHECKED_CAST")
                        val response = runBlocking { pagBankProvider.createPixTransaction(request) }
                        logger.info("✅ Doação criada com sucesso via PagBank API")
                        ResponseEntity.ok(response)
                } catch (e: Exception) {
                        logger.error("❌ Erro ao criar doação: ${e.message}", e)
                        ResponseEntity.badRequest()
                                .body(
                                        mapOf(
                                                "error" to "donation_failed",
                                                "message" to "Erro ao criar doação: ${e.message}"
                                        )
                                )
                }
        }
        @GetMapping("/{paymentId}/status")
        fun getPaymentStatus(@PathVariable paymentId: String): ResponseEntity<*> {
                return try {
                        logger.info("📊 Consultando status da doação: $paymentId")
                        val response = runBlocking {
                                pagBankProvider.getTransactionStatus(paymentId)
                        }
                        logger.info("✅ Status da doação consultado via PagBank")
                        ResponseEntity.ok(response)
                } catch (e: Exception) {
                        logger.error("❌ Erro ao consultar status: ${e.message}", e)
                        ResponseEntity.badRequest()
                                .body(
                                        mapOf(
                                                "error" to "status_check_failed",
                                                "message" to
                                                        "Erro ao consultar status: ${e.message}"
                                        )
                                )
                }
        }
        @PostMapping("/webhook")
        fun handleWebhook(
                @RequestBody payload: String,
                @RequestHeader("X-PagBank-Signature", required = false) signature: String?
        ): ResponseEntity<*> {
                return try {
                        logger.info("🔔 Webhook recebido do PagBank")
                        val response = runBlocking {
                                pagBankProvider.processWebhook(payload, signature ?: "")
                        }
                        logger.info("✅ Webhook PagBank processado")
                        ResponseEntity.ok(response)
                } catch (e: Exception) {
                        logger.error("❌ Erro ao processar webhook: ${e.message}", e)
                        ResponseEntity.badRequest()
                                .body(
                                        mapOf(
                                                "error" to "webhook_failed",
                                                "message" to
                                                        "Erro ao processar webhook: ${e.message}"
                                        )
                                )
                }
        }
}
