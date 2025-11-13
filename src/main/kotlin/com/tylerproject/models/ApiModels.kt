package com.tylerproject.models
import kotlinx.serialization.Serializable
@Serializable
data class CheckoutRequest(
        val amount: Double,
        val description: String,
        val payer: PayerInfo,
        val orderId: String? = null
)
@Serializable data class PayerInfo(val name: String, val email: String, val cpf: String)
@Serializable
data class CheckoutResponse(
        val success: Boolean,
        val orderId: String? = null,
        val paymentId: String? = null,
        val amount: Double? = null,
        val status: String? = null,
        val pixQrCode: String? = null,
        val pixQrCodeBase64: String? = null,
        val pixCopyPaste: String? = null,
        val expirationDate: String? = null,
        val provider: String? = null,
        val instructions: String? = null,
        val error: String? = null
)
@Serializable
data class PaymentStatusResponse(
        val success: Boolean,
        val paymentId: String? = null,
        val status: String? = null,
        val amount: Double? = null,
        val currency: String? = null,
        val paymentMethod: String? = null,
        val provider: String? = null,
        val pixEndToEndId: String? = null,
        val expirationDate: String? = null,
        val message: String? = null,
        val error: String? = null
)
@Serializable
data class WebhookResponse(
        val success: Boolean,
        val message: String? = null,
        val transactionId: String? = null,
        val status: String? = null,
        val event: String? = null,
        val provider: String? = null,
        val error: String? = null
)
@Serializable
data class HealthResponse(
        val status: String,
        val service: String,
        val version: String,
        val timestamp: Long,
        val pix: String,
        val firebase: String,
        val provider: String
)
@Serializable data class ErrorResponse(val success: Boolean = false, val error: String)
