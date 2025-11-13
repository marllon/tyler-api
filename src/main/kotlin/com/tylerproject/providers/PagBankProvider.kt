package com.tylerproject.providers
import com.tylerproject.models.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
@Component
class PagBankProvider(
        private val token: String,
        @Autowired private val applicationContext: ApplicationContext
) {
    companion object {
        private const val SANDBOX_URL = "https://sandbox.api.pagseguro.com"
        private const val PRODUCTION_URL = "https://api.pagseguro.com"
    }
    private val logger = LoggerFactory.getLogger(PagBankProvider::class.java)
    private val client = OkHttpClient()
    private val baseUrl = SANDBOX_URL
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true // ✅ Incluir valores padrão (quantity = 1)
    }
    private fun getAuthHeaders(): Headers {
        return Headers.Builder()
                .add("Authorization", "Bearer $token")
                .add("Content-Type", "application/json")
                .add("Accept", "application/json")
                .build()
    }
    suspend fun createPixTransaction(request: Map<String, Any>): Map<String, Any> =
            withContext(Dispatchers.IO) {
                try {
                    logger.info("🏦 Criando doação PIX via PagBank API oficial")
                    val amount =
                            (request["amount"] as? Number)?.toLong()
                                    ?: throw IllegalArgumentException("Valor inválido")
                    val description = request["description"] as? String ?: "Doação para caridade"
                    @Suppress("UNCHECKED_CAST")
                    val payerMap =
                            request["payer"] as? Map<String, Any>
                                    ?: throw IllegalArgumentException("Dados do doador inválidos")
                    val payerName =
                            payerMap["name"] as? String
                                    ?: throw IllegalArgumentException("Nome do doador inválido")
                    val payerEmail =
                            payerMap["email"] as? String
                                    ?: throw IllegalArgumentException("Email do doador inválido")
                    val payerDocument =
                            payerMap["document"] as? String
                                    ?: throw IllegalArgumentException("CPF do doador inválido")
                    val referenceId = "DOA_${System.currentTimeMillis()}"
                    val itemId = "ITEM_${System.currentTimeMillis()}"
                    val expirationDate =
                            LocalDateTime.now()
                                    .plusHours(24)
                                    .atOffset(ZoneOffset.of("-03:00"))
                                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    val pagBankRequest =
                            PagBankOrderRequest(
                                    reference_id = referenceId,
                                    customer =
                                            PagBankCustomer(
                                                    name = payerName,
                                                    email = payerEmail,
                                                    tax_id =
                                                            payerDocument.replace(
                                                                    Regex("[^0-9]"),
                                                                    ""
                                                            ) // Remove pontuação do CPF
                                            ),
                                    items =
                                            listOf(
                                                    PagBankItem(
                                                            reference_id = itemId,
                                                            name = description,
                                                            quantity = 1,
                                                            unit_amount = amount
                                                    )
                                            ),
                                    qr_codes =
                                            listOf(
                                                    PagBankQrCode(
                                                            amount = PagBankAmount(value = amount),
                                                            expiration_date = expirationDate
                                                    )
                                            )
                            )
                    val requestBody =
                            json.encodeToString(pagBankRequest)
                                    .toRequestBody("application/json".toMediaType())
                    val httpRequest =
                            Request.Builder()
                                    .url("$baseUrl/orders")
                                    .headers(getAuthHeaders())
                                    .post(requestBody)
                                    .build()
                    logger.info("📤 Enviando request para PagBank Orders API")
                    logger.debug("🔍 Payload: ${json.encodeToString(pagBankRequest)}")
                    val response = client.newCall(httpRequest).execute()
                    val responseBody = response.body?.string() ?: ""
                    logger.info("📥 Response PagBank [${response.code}]: $responseBody")
                    if (response.isSuccessful) {
                        try {
                            val pagBankResponse =
                                    json.decodeFromString<PagBankOrderResponse>(responseBody)
                            val qrCode = pagBankResponse.qr_codes?.firstOrNull()
                            val qrCodePng = qrCode?.links?.find { it.media == "image/png" }?.href
                            val qrCodeBase64 =
                                    qrCode?.links?.find { it.media == "text/plain" }?.href
                            mapOf(
                                    "transaction_id" to pagBankResponse.id,
                                    "pix_code" to (qrCode?.text ?: ""),
                                    "qr_code_url" to (qrCodePng ?: ""),
                                    "qr_code_base64_url" to (qrCodeBase64 ?: ""),
                                    "amount" to amount,
                                    "status" to "WAITING",
                                    "expires_at" to (qrCode?.expiration_date ?: expirationDate),
                                    "created_at" to pagBankResponse.created_at,
                                    "reference_id" to pagBankResponse.reference_id,
                                    "qr_code_id" to (qrCode?.id ?: ""),
                                    "provider" to "pagbank"
                            )
                        } catch (parseError: Exception) {
                            logger.error(
                                    "❌ Erro ao parsear response PagBank: ${parseError.message}"
                            )
                            try {
                                val errorResponse =
                                        json.decodeFromString<PagBankErrorResponse>(responseBody)
                                val errorMsg =
                                        errorResponse.error_messages.joinToString("; ") {
                                            "${it.code}: ${it.description}"
                                        }
                                throw Exception("Erro PagBank: $errorMsg")
                            } catch (errorParseError: Exception) {
                                throw Exception(
                                        "Erro ao parsear response PagBank. Response: $responseBody"
                                )
                            }
                        }
                    } else {
                        logger.error("❌ Erro PagBank [${response.code}]: $responseBody")
                        if (responseBody.isNotEmpty()) {
                            try {
                                val errorResponse =
                                        json.decodeFromString<PagBankErrorResponse>(responseBody)
                                val errorMsg =
                                        errorResponse.error_messages.joinToString("; ") {
                                            "${it.code}: ${it.description}"
                                        }
                                throw Exception("Erro PagBank [${response.code}]: $errorMsg")
                            } catch (errorParseError: Exception) {
                                throw Exception("Erro PagBank [${response.code}]: $responseBody")
                            }
                        } else {
                            throw Exception("Erro PagBank [${response.code}]: Resposta vazia")
                        }
                    }
                } catch (e: Exception) {
                    logger.error("💥 Erro ao criar doação PIX: ${e.message}", e)
                    throw e
                }
            }
    suspend fun getTransactionStatus(transactionId: String): Map<String, Any> =
            withContext(Dispatchers.IO) {
                try {
                    logger.info("📊 Consultando status da doação: $transactionId")
                    val httpRequest =
                            Request.Builder()
                                    .url("$baseUrl/orders/$transactionId")
                                    .headers(getAuthHeaders())
                                    .get()
                                    .build()
                    val response = client.newCall(httpRequest).execute()
                    val responseBody = response.body?.string() ?: ""
                    logger.info("📥 Status response [${response.code}]: $responseBody")
                    if (response.isSuccessful) {
                        try {
                            val pagBankResponse =
                                    json.decodeFromString<PagBankOrderResponse>(responseBody)
                            val qrCode = pagBankResponse.qr_codes?.firstOrNull()
                            val charge = pagBankResponse.charges?.firstOrNull()
                            mapOf(
                                    "transaction_id" to pagBankResponse.id,
                                    "status" to (pagBankResponse.status ?: "WAITING"),
                                    "amount" to
                                            (qrCode?.amount?.value ?: charge?.amount?.value ?: 0L),
                                    "created_at" to pagBankResponse.created_at,
                                    "updated_at" to LocalDateTime.now().toString(),
                                    "reference_id" to pagBankResponse.reference_id,
                                    "charge_status" to (charge?.status ?: "unknown"),
                                    "provider" to "pagbank"
                            )
                        } catch (parseError: Exception) {
                            logger.error("❌ Erro ao parsear status response: ${parseError.message}")
                            mapOf(
                                    "transaction_id" to transactionId,
                                    "status" to "pending",
                                    "amount" to 0L,
                                    "created_at" to LocalDateTime.now().toString(),
                                    "updated_at" to LocalDateTime.now().toString(),
                                    "raw_response" to responseBody,
                                    "provider" to "pagbank"
                            )
                        }
                    } else {
                        logger.error("❌ Erro ao consultar status [${response.code}]: $responseBody")
                        throw Exception(
                                "Erro ao consultar status: HTTP ${response.code}: $responseBody"
                        )
                    }
                } catch (e: Exception) {
                    logger.error("💥 Erro ao consultar status: ${e.message}", e)
                    throw e
                }
            }
    suspend fun processWebhook(
            payload: String,
            @Suppress("UNUSED_PARAMETER") signature: String
    ): Map<String, Any> {
        return try {
            logger.info("🔔 Processando webhook do PagBank")
            logger.debug("📥 Payload: $payload")
            try {
                val webhookData = json.decodeFromString<PagBankWebhookPayload>(payload)
                val referenceId = webhookData.reference_id
                val status = webhookData.status
                logger.info(
                        "✅ Webhook processado: $status para ${webhookData.id}, ref: $referenceId"
                )
                val (type, success) = "webhook" to true
                mapOf(
                        "success" to success,
                        "eventType" to "payment_status_change",
                        "transactionId" to webhookData.id,
                        "status" to mapPagBankStatus(status),
                        "type" to type,
                        "message" to "Webhook PagBank processado: $type $status",
                        "timestamp" to LocalDateTime.now().toString(),
                        "reference_id" to referenceId,
                        "provider" to "pagbank"
                )
            } catch (parseError: Exception) {
                logger.warn(
                        "⚠️ Não foi possível parsear webhook específico, processando como genérico"
                )
                mapOf(
                        "success" to true,
                        "eventType" to "generic_webhook",
                        "message" to "Webhook genérico PagBank processado",
                        "timestamp" to LocalDateTime.now().toString(),
                        "raw_payload" to payload,
                        "provider" to "pagbank"
                )
            }
        } catch (e: Exception) {
            logger.error("💥 Erro ao processar webhook: ${e.message}", e)
            mapOf(
                    "success" to false,
                    "eventType" to "error",
                    "message" to "Erro ao processar webhook: ${e.message}",
                    "timestamp" to LocalDateTime.now().toString(),
                    "provider" to "pagbank"
            )
        }
    }
    private fun mapPagBankStatus(status: String): String {
        return when (status.uppercase()) {
            "PAID" -> "PAID"
            "AUTHORIZED" -> "WAITING_PAYMENT"
            "WAITING" -> "WAITING_PAYMENT"
            "IN_ANALYSIS" -> "WAITING_PAYMENT"
            "CANCELED" -> "CANCELLED"
            "DECLINED" -> "FAILED"
            "EXPIRED" -> "EXPIRED"
            else -> "WAITING_PAYMENT"
        }
    }
}
