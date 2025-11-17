package com.tylerproject.domain.pagbank

import com.tylerproject.providers.PagBankProvider
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
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
        fun createCheckout(
                @Valid @RequestBody request: CreateCheckoutRequest
        ): ResponseEntity<CheckoutResponse> {
                return try {
                        logger.info("🎯 Criando checkout PIX para valor: R$ ${request.amount}")

                        val pagBankRequest =
                                buildMap<String, Any> {
                                        put("amount", (request.amount * 100).toLong())
                                        request.description?.let { put("description", it) }
                                        request.referenceId?.let { put("reference_id", it) }

                                        val customer =
                                                buildMap<String, String> {
                                                        request.customerName?.let {
                                                                put("name", it)
                                                        }
                                                        request.customerEmail?.let {
                                                                put("email", it)
                                                        }
                                                        request.customerPhone?.let {
                                                                put("phone", it)
                                                        }
                                                        request.customerDocument?.let {
                                                                put("tax_id", it)
                                                        }
                                                }
                                        if (customer.isNotEmpty()) {
                                                put("customer", customer)
                                        }
                                }

                        val pagBankResponse = runBlocking {
                                pagBankProvider.createPixTransaction(pagBankRequest)
                        }

                        val response =
                                CheckoutResponse(
                                        id = pagBankResponse["id"] as? String ?: "",
                                        status = pagBankResponse["status"] as? String ?: "PENDING",
                                        qrCodeText = extractQrCodeText(pagBankResponse),
                                        qrCodeImageUrl = extractQrCodeImageUrl(pagBankResponse),
                                        expiresAt = extractExpiresAt(pagBankResponse),
                                        amount = request.amount,
                                        description = request.description ?: "Doação"
                                )

                        logger.info("✅ Checkout criado com sucesso - ID: ${response.id}")
                        ResponseEntity.ok(response)
                } catch (e: IllegalArgumentException) {
                        logger.error("❌ Dados inválidos: ${e.message}")
                        ResponseEntity.badRequest().build()
                } catch (e: Exception) {
                        logger.error("❌ Erro ao criar checkout: ${e.message}", e)
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                }
        }

        @GetMapping("/{paymentId}/status")
        @Operation(
                summary = "📊 Consultar status do pagamento",
                description = "Retorna o status atual de um pagamento no PagBank"
        )
        @ApiResponses(
                value =
                        [
                                ApiResponse(
                                        responseCode = "200",
                                        description = "✅ Status consultado com sucesso"
                                ),
                                ApiResponse(
                                        responseCode = "404",
                                        description = "❌ Pagamento não encontrado"
                                ),
                                ApiResponse(
                                        responseCode = "500",
                                        description = "❌ Erro ao consultar status"
                                )]
        )
        fun getPaymentStatus(
                @PathVariable paymentId: String
        ): ResponseEntity<PaymentStatusResponse> {
                return try {
                        logger.info("📊 Consultando status do pagamento: $paymentId")

                        val pagBankResponse = runBlocking {
                                pagBankProvider.getTransactionStatus(paymentId)
                        }

                        val response =
                                PaymentStatusResponse(
                                        id = pagBankResponse["id"] as? String ?: paymentId,
                                        status = pagBankResponse["status"] as? String ?: "UNKNOWN",
                                        amount = extractAmount(pagBankResponse),
                                        paidAt = pagBankResponse["paid_at"] as? String,
                                        createdAt = pagBankResponse["created_at"] as? String ?: "",
                                        updatedAt = pagBankResponse["updated_at"] as? String ?: ""
                                )

                        logger.info("✅ Status consultado - Status: ${response.status}")
                        ResponseEntity.ok(response)
                } catch (e: Exception) {
                        logger.error("❌ Erro ao consultar status: ${e.message}", e)
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                }
        }

        private fun extractQrCodeText(response: Map<String, Any>): String {
                val qrCodes = response["qr_codes"] as? List<*>
                val qrCode = qrCodes?.firstOrNull() as? Map<*, *>
                return qrCode?.get("text") as? String ?: ""
        }

        private fun extractQrCodeImageUrl(response: Map<String, Any>): String {
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

        private fun extractAmount(response: Map<String, Any>): Double {
                val amount = response["amount"] as? Map<*, *>
                val value = amount?.get("value") as? Number
                return (value?.toLong() ?: 0L) / 100.0
        }
}
