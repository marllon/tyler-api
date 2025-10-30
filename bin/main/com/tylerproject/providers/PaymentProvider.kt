package com.tylerproject.providers

interface PaymentProvider {
    /**
     * Cria uma sessão de checkout
     * @return URL de checkout ou Payment Intent ID
     */
    suspend fun createCheckout(
            amount: Double,
            currency: String,
            description: String,
            metadata: Map<String, String>,
            customerEmail: String,
            successUrl: String,
            cancelUrl: String
    ): PaymentResult

    /** Verifica o status de um pagamento */
    suspend fun getPaymentStatus(paymentIntentId: String): PaymentStatus

    /**
     * Processa webhook de pagamento confirmado
     * @return true se o webhook foi processado com sucesso
     */
    fun verifyWebhook(payload: String, signature: String): WebhookEvent?

    /** Cancela/reembolsa um pagamento */
    suspend fun refundPayment(paymentIntentId: String, amount: Double? = null): RefundResult
}

data class PaymentResult(
        val success: Boolean,
        val checkoutUrl: String? = null,
        val paymentIntentId: String? = null,
        val error: String? = null
)

data class PaymentStatus(
        val status: String, // pending, succeeded, failed, cancelled
        val amount: Double,
        val currency: String,
        val metadata: Map<String, String>
)

data class WebhookEvent(
        val type: String, // payment.succeeded, payment.failed, etc
        val paymentIntentId: String,
        val amount: Double,
        val currency: String,
        val metadata: Map<String, String>
)

data class RefundResult(
        val success: Boolean,
        val refundId: String? = null,
        val error: String? = null
)
