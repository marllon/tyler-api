package com.tylerproject.models
import kotlinx.serialization.Serializable
@Serializable
data class PagSeguroPixRequest(
        val reference_id: String,
        val customer: PagSeguroCustomer,
        val items: List<PagSeguroItem>,
        val charges: List<PagSeguroCharge>,
        val notification_urls: List<String> = emptyList()
)
@Serializable
data class PagSeguroCustomer(
        val name: String,
        val email: String,
        val tax_id: String, // CPF sem pontuação
        val phones: List<PagSeguroPhone>? = null
)
@Serializable
data class PagSeguroPhone(
        val country: String = "55",
        val area: String = "11",
        val number: String = "999999999"
)
@Serializable
data class PagSeguroItem(
        val reference_id: String,
        val name: String,
        val quantity: Int = 1,
        val unit_amount: Long // Valor em centavos
)
@Serializable
data class PagSeguroCharge(
        val reference_id: String,
        val description: String,
        val amount: PagSeguroAmount,
        val payment_method: PagSeguroPaymentMethod
)
@Serializable
data class PagSeguroAmount(
        val value: Long, // Valor em centavos
        val currency: String = "BRL"
)
@Serializable
data class PagSeguroPaymentMethod(val type: String = "PIX", val pix: PagSeguroPix = PagSeguroPix())
@Serializable
data class PagSeguroPix(
        val expiration_date: String? = null, // ISO 8601 format
        val qrcode_text: String? = null, // Código PIX Copia e Cola
        val qrcode_base64: String? = null, // QR Code em base64
        val expires_at: String? = null // Data de expiração alternativa
)
@Serializable
data class PagSeguroPixResponse(
        val id: String,
        val reference_id: String,
        val status: String,
        val created_at: String,
        val customer: PagSeguroCustomer,
        val items: List<PagSeguroItem>,
        val charges: List<PagSeguroChargeResponse>,
        val links: List<PagSeguroLink>? = null
)
@Serializable
data class PagSeguroChargeResponse(
        val id: String,
        val reference_id: String,
        val status: String,
        val created_at: String,
        val description: String,
        val amount: PagSeguroAmount,
        val payment_method: PagSeguroPaymentMethodResponse,
        val links: List<PagSeguroLink>? = null
)
@Serializable
data class PagSeguroPaymentMethodResponse(val type: String, val pix: PagSeguroPix? = null)
@Serializable
data class PagSeguroPixResponseData(
        val qr_code: String,
        val formatted_code: String,
        val expiration_date: String? = null
)
@Serializable
data class PagSeguroLink(val rel: String, val href: String, val media: String, val type: String)
@Serializable
data class PagSeguroWebhookPayload(
        val id: String,
        val reference_id: String,
        val status: String,
        val charges: List<PagSeguroChargeResponse>
)
@Serializable data class PagSeguroErrorResponse(val error_messages: List<PagSeguroError>)
@Serializable
data class PagSeguroError(
        val code: String,
        val description: String,
        val parameter_name: String? = null
)
