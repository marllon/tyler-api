package com.tylerproject.domain.pagbank

data class CreateCheckoutRequest(
    val amount: Double,
    val description: String? = null,
    val customerName: String? = null,
    val customerEmail: String? = null,
    val customerPhone: String? = null,
    val customerDocument: String? = null,
    val referenceId: String? = null
)

data class CheckoutResponse(
    val id: String,
    val status: String,
    val qrCodeText: String,
    val qrCodeImageUrl: String,
    val expiresAt: String,
    val amount: Double,
    val description: String
)

data class PaymentStatusResponse(
    val id: String,
    val status: String,
    val amount: Double,
    val paidAt: String? = null,
    val createdAt: String,
    val updatedAt: String
)

data class WebhookResponse(
    val success: Boolean,
    val message: String,
    val donationId: String? = null,
    val previousStatus: String? = null,
    val newStatus: String? = null,
    val processed: Boolean = false
)

data class HealthCheckResponse(
    val status: String,
    val webhook: String,
    val timestamp: String
)
