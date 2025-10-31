package com.tylerproject.controllers

import com.tylerproject.providers.PagBankProvider
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 💳 Payment Controller - Endpoints para doações PIX via CPF
 *
 * Endpoints disponíveis:
 * - POST /api/payments/checkout - Criar checkout PIX para doação
 * - GET /api/payments/{paymentId}/status - Status da doação
 * - POST /api/payments/webhook - Webhook do PagBank
 */
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = ["*"]) // Configure conforme necessário
class PaymentController @Autowired constructor(private val pagBankProvider: PagBankProvider) {

    private val logger = LoggerFactory.getLogger(PaymentController::class.java)

    /** � Criar Checkout PIX para doação - PagBank API OFICIAL */
    @PostMapping("/checkout")
    fun createCheckout(@RequestBody request: Map<String, Any>): ResponseEntity<*> {
        return try {
            val amount =
                    (request["amount"] as? Number)?.toLong()
                            ?: throw IllegalArgumentException("Valor inválido")
            val description = request["description"] as? String ?: "Doação para caridade"
            val payerMap =
                    request["payer"] as? Map<String, Any>
                            ?: throw IllegalArgumentException("Dados do doador inválidos")

            logger.info("� Criando doação PIX para valor: R$ ${amount / 100.0}")

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

    /** 📊 Consultar Status da Doação - PagBank API */
    @GetMapping("/{paymentId}/status")
    fun getPaymentStatus(@PathVariable paymentId: String): ResponseEntity<*> {
        return try {
            logger.info("📊 Consultando status da doação: $paymentId")

            val response = runBlocking { pagBankProvider.getTransactionStatus(paymentId) }

            logger.info("✅ Status da doação consultado via PagBank")
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("❌ Erro ao consultar status: ${e.message}", e)
            ResponseEntity.badRequest()
                    .body(
                            mapOf(
                                    "error" to "status_check_failed",
                                    "message" to "Erro ao consultar status: ${e.message}"
                            )
                    )
        }
    }

    /** 🔔 Webhook do PagBank - Notificações de doação */
    @PostMapping("/webhook")
    fun handleWebhook(
            @RequestBody payload: String,
            @RequestHeader("X-PagBank-Signature", required = false) signature: String?
    ): ResponseEntity<*> {
        return try {
            logger.info("🔔 Webhook recebido do PagBank")

            val response = pagBankProvider.processWebhook(payload, signature ?: "")

            logger.info("✅ Webhook PagBank processado")
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("❌ Erro ao processar webhook: ${e.message}", e)
            ResponseEntity.badRequest()
                    .body(
                            mapOf(
                                    "error" to "webhook_failed",
                                    "message" to "Erro ao processar webhook: ${e.message}"
                            )
                    )
        }
    }
}
