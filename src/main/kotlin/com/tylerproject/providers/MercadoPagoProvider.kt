package com.tylerproject.providers

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class MercadoPagoProvider(private val accessToken: String, private val webhookSecret: String) :
        PaymentProvider {

    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private val baseUrl = "https://api.mercadopago.com"

    override suspend fun createCheckout(
            amount: Double,
            currency: String,
            description: String,
            metadata: Map<String, String>,
            customerEmail: String,
            successUrl: String,
            cancelUrl: String
    ): PaymentResult =
            withContext(Dispatchers.IO) {
                try {
                    val preference =
                            MPPreferenceRequest(
                                    items =
                                            listOf(
                                                    MPItem(
                                                            title = description,
                                                            quantity = 1,
                                                            unitPrice = amount,
                                                            currencyId = currency
                                                    )
                                            ),
                                    payer = MPPayer(email = customerEmail),
                                    backUrls =
                                            MPBackUrls(
                                                    success = successUrl,
                                                    failure = cancelUrl,
                                                    pending = successUrl
                                            ),
                                    autoReturn = "approved",
                                    externalReference = metadata["orderId"]
                                                    ?: metadata["donationId"] ?: "",
                                    metadata = metadata,
                                    paymentMethods =
                                            MPPaymentMethods(
                                                    excludedPaymentTypes = emptyList(),
                                                    installments = 12
                                            )
                            )

                    val requestBody =
                            json.encodeToString(MPPreferenceRequest.serializer(), preference)
                                    .toRequestBody("application/json".toMediaType())

                    val request =
                            Request.Builder()
                                    .url("$baseUrl/checkout/preferences")
                                    .header("Authorization", "Bearer $accessToken")
                                    .post(requestBody)
                                    .build()

                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string() ?: throw Exception("Empty response")

                    if (response.isSuccessful) {
                        val mpResponse = json.decodeFromString<MPPreferenceResponse>(responseBody)

                        PaymentResult(
                                success = true,
                                checkoutUrl = mpResponse.initPoint,
                                paymentIntentId = mpResponse.id
                        )
                    } else {
                        PaymentResult(
                                success = false,
                                error = "Erro ao criar checkout MP: $responseBody"
                        )
                    }
                } catch (e: Exception) {
                    PaymentResult(
                            success = false,
                            error = e.message ?: "Erro ao criar checkout Mercado Pago"
                    )
                }
            }

    override suspend fun getPaymentStatus(paymentIntentId: String): PaymentStatus =
            withContext(Dispatchers.IO) {
                try {
                    val request =
                            Request.Builder()
                                    .url("$baseUrl/v1/payments/$paymentIntentId")
                                    .header("Authorization", "Bearer $accessToken")
                                    .get()
                                    .build()

                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string() ?: throw Exception("Empty response")

                    if (response.isSuccessful) {
                        val payment = json.decodeFromString<MPPayment>(responseBody)

                        PaymentStatus(
                                status =
                                        when (payment.status) {
                                            "approved" -> "succeeded"
                                            "pending", "in_process", "in_mediation" -> "pending"
                                            "cancelled", "rejected" -> "cancelled"
                                            else -> "failed"
                                        },
                                amount = payment.transactionAmount,
                                currency = payment.currencyId,
                                metadata = payment.metadata ?: emptyMap()
                        )
                    } else {
                        PaymentStatus(
                                status = "failed",
                                amount = 0.0,
                                currency = "BRL",
                                metadata = emptyMap()
                        )
                    }
                } catch (e: Exception) {
                    PaymentStatus(
                            status = "failed",
                            amount = 0.0,
                            currency = "BRL",
                            metadata = emptyMap()
                    )
                }
            }

    override fun verifyWebhook(payload: String, signature: String): WebhookEvent? {
        return try {
            // Verificar assinatura HMAC
            if (!verifySignature(payload, signature)) {
                println("Assinatura inválida no webhook MP")
                return null
            }

            val notification = json.decodeFromString<MPWebhookNotification>(payload)

            when (notification.type) {
                "payment" -> {
                    // Buscar detalhes do pagamento
                    val paymentId = notification.data?.id ?: return null

                    // Nota: em produção, buscar os dados do pagamento via API
                    // Por simplicidade, retornando evento básico
                    WebhookEvent(
                            type = "payment.succeeded",
                            paymentIntentId = paymentId,
                            amount = 0.0, // Buscar da API
                            currency = "BRL",
                            metadata = emptyMap()
                    )
                }
                else -> null
            }
        } catch (e: Exception) {
            println("Erro ao verificar webhook MP: ${e.message}")
            null
        }
    }

    override suspend fun refundPayment(paymentIntentId: String, amount: Double?): RefundResult =
            withContext(Dispatchers.IO) {
                try {
                    val refundData =
                            if (amount != null) {
                                mapOf("amount" to amount)
                            } else {
                                emptyMap()
                            }

                    val requestBody =
                            json.encodeToString(
                                            kotlinx.serialization.builtins.MapSerializer(
                                                    kotlinx.serialization.builtins.serializer<
                                                            String>(),
                                                    kotlinx.serialization.builtins.serializer<
                                                            Double>()
                                            ),
                                            refundData
                                    )
                                    .toRequestBody("application/json".toMediaType())

                    val request =
                            Request.Builder()
                                    .url("$baseUrl/v1/payments/$paymentIntentId/refunds")
                                    .header("Authorization", "Bearer $accessToken")
                                    .post(requestBody)
                                    .build()

                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string() ?: throw Exception("Empty response")

                    if (response.isSuccessful) {
                        val refund = json.decodeFromString<MPRefund>(responseBody)

                        RefundResult(success = true, refundId = refund.id.toString())
                    } else {
                        RefundResult(success = false, error = "Erro ao reembolsar: $responseBody")
                    }
                } catch (e: Exception) {
                    RefundResult(
                            success = false,
                            error = e.message ?: "Erro ao reembolsar pagamento MP"
                    )
                }
            }

    private fun verifySignature(payload: String, signature: String): Boolean {
        return try {
            val mac = Mac.getInstance("HmacSHA256")
            val secretKeySpec = SecretKeySpec(webhookSecret.toByteArray(), "HmacSHA256")
            mac.init(secretKeySpec)
            val hash = mac.doFinal(payload.toByteArray())
            val computed = hash.joinToString("") { "%02x".format(it) }
            computed == signature
        } catch (e: Exception) {
            false
        }
    }
}

// Mercado Pago DTOs

@Serializable
private data class MPPreferenceRequest(
        val items: List<MPItem>,
        val payer: MPPayer,
        val backUrls: MPBackUrls,
        val autoReturn: String,
        val externalReference: String,
        val metadata: Map<String, String>,
        val paymentMethods: MPPaymentMethods
)

@Serializable
private data class MPItem(
        val title: String,
        val quantity: Int,
        val unitPrice: Double,
        val currencyId: String
)

@Serializable private data class MPPayer(val email: String)

@Serializable
private data class MPBackUrls(val success: String, val failure: String, val pending: String)

@Serializable
private data class MPPaymentMethods(val excludedPaymentTypes: List<String>, val installments: Int)

@Serializable private data class MPPreferenceResponse(val id: String, val initPoint: String)

@Serializable
private data class MPPayment(
        val id: Long,
        val status: String,
        val transactionAmount: Double,
        val currencyId: String,
        val metadata: Map<String, String>? = null
)

@Serializable
private data class MPWebhookNotification(val type: String, val data: MPWebhookData? = null)

@Serializable private data class MPWebhookData(val id: String)

@Serializable private data class MPRefund(val id: Long)
