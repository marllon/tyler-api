package com.tylerproject.domain.donation

import kotlinx.serialization.Serializable

@Serializable
data class Donation(
        val id: String = "",
        val donationType: DonationType = DonationType.GOAL,
        val targetId: String =
                "", // Para SIMPLE sempre será "simple-donation", demais tipos: ID da entidade
        val amount: Double = 0.0,
        val status: DonationStatus = DonationStatus.PENDING,
        val paymentMethod: PaymentMethod? = null,
        val pagbankChargeId: String? = null,
        val pagbankTransactionId: String? = null,
        val qrCodeText: String? = null,
        val qrCodeImageBase64: String? = null,
        val expiresAt: String? = null,
        val donorName: String? = null,
        val donorEmail: String? = null,
        val donorPhone: String? = null,
        val donorDocument: String? = null,
        val isAnonymous: Boolean = false,
        val message: String? = null,
        val metadata: Map<String, String>? = null,
        val paidAt: String? = null,
        val cancelledAt: String? = null,
        val refundedAt: String? = null,
        val createdAt: String = "",
        val updatedAt: String = "",
        val processedAt: String? = null,
        val webhookData: String? = null
) {
    fun isPaid(): Boolean = status == DonationStatus.PAID

    fun canBeProcessed(): Boolean = status == DonationStatus.PAID && processedAt == null

    fun isSimpleDonation(): Boolean = donationType == DonationType.SIMPLE

    fun getTargetDescription(): String {
        return when (donationType) {
            DonationType.SIMPLE -> "Doação Livre"
            DonationType.GOAL -> "Meta ID: $targetId"
            DonationType.RAFFLE -> "Rifa ID: $targetId"
            DonationType.ORDER -> "Pedido ID: $targetId"
        }
    }

    fun getTypeDescription(): String = DonationType.getDescription(donationType)
}
