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

/**
 * 🏦 PagBank Provider - API OFICIAL REAL
 *
 * Features:
 * - ✅ API oficial PagBank (Orders API)
 * - ✅ Aceita CPF (não precisa CNPJ)
 * - ✅ PIX com boa taxa
 * - ✅ Ideal para caridade e doações
 * - ✅ Sandbox → Homologação → Produção
 *
 * Documentação: https://developer.pagbank.com.br/
 */
class PagBankProvider(private val token: String) {

    companion object {
        // URLs OFICIAIS da API PagBank
        private const val SANDBOX_URL = "https://sandbox.api.pagseguro.com"
        private const val PRODUCTION_URL = "https://api.pagseguro.com"
    }

    private val logger = LoggerFactory.getLogger(PagBankProvider::class.java)
    private val client = OkHttpClient()

    // 🛠️ Para desenvolvimento, sempre usar SANDBOX
    // TODO: Em produção, detectar automaticamente baseado no token
    private val baseUrl = SANDBOX_URL

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true // ✅ Incluir valores padrão (quantity = 1)
    }

    /** 🔐 Headers para autenticação PagBank */
    private fun getAuthHeaders(): Headers {
        return Headers.Builder()
                .add("Authorization", "Bearer $token")
                .add("Content-Type", "application/json")
                .add("Accept", "application/json")
                .build()
    }

    /** 🎁 Criar Order PIX para DOAÇÃO - API OFICIAL */
    suspend fun createPixTransaction(request: Map<String, Any>): Map<String, Any> =
            withContext(Dispatchers.IO) {
                try {
                    logger.info("🏦 Criando doação PIX via PagBank API oficial")

                    // Extrair dados do request
                    val amount =
                            (request["amount"] as? Number)?.toLong()
                                    ?: throw IllegalArgumentException("Valor inválido")
                    val description = request["description"] as? String ?: "Doação para caridade"
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

                    // Gerar IDs únicos
                    val referenceId = "DOA_${System.currentTimeMillis()}"
                    val itemId = "ITEM_${System.currentTimeMillis()}"
                    val chargeId = "CHARGE_${System.currentTimeMillis()}"

                    // Data de expiração PIX (24 horas)
                    val expirationDate =
                            LocalDateTime.now()
                                    .plusHours(24)
                                    .atOffset(ZoneOffset.of("-03:00"))
                                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

                    // Criar payload para PagBank (Orders API com QR_CODES para PIX)
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
                                    // ✅ PIX usa QR_CODES, não CHARGES!
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

                            // Para PIX, usar qr_codes ao invés de charges
                            val qrCode = pagBankResponse.qr_codes?.firstOrNull()
                            val qrCodePng = qrCode?.links?.find { it.media == "image/png" }?.href
                            val qrCodeBase64 =
                                    qrCode?.links?.find { it.media == "text/plain" }?.href

                            // Converter para formato esperado pela API Tyler
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
                            // Fallback: tentar parsear como erro
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

                        // Tentar parsear erro específico
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

    /** 📊 Consultar status da Order/Doação */
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

                            // Para PIX, usar qr_codes ao invés de charges
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
                            // Fallback
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

    /** 🔔 Processar webhook do PagBank */
    fun processWebhook(payload: String, signature: String): Map<String, Any> {
        return try {
            logger.info("🔔 Processando webhook do PagBank")
            logger.debug("📥 Payload: $payload")

            try {
                val webhookData = json.decodeFromString<PagBankWebhookPayload>(payload)

                logger.info("✅ Webhook processado: ${webhookData.status} para ${webhookData.id}")

                mapOf(
                        "success" to true,
                        "eventType" to "payment_status_change",
                        "transactionId" to webhookData.id,
                        "status" to mapPagBankStatus(webhookData.status),
                        "message" to "Webhook PagBank processado com sucesso",
                        "timestamp" to LocalDateTime.now().toString(),
                        "reference_id" to webhookData.reference_id,
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

    /** 🔄 Mapear status do PagBank para padrão da API Tyler */
    private fun mapPagBankStatus(status: String): String {
        return when (status.uppercase()) {
            "PAID" -> "paid"
            "AUTHORIZED" -> "pending"
            "WAITING" -> "pending"
            "IN_ANALYSIS" -> "pending"
            "CANCELED" -> "failed"
            "DECLINED" -> "failed"
            "EXPIRED" -> "failed"
            else -> "pending"
        }
    }
}
