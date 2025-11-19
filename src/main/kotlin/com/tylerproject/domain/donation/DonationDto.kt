package com.tylerproject.domain.donation

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.*

data class CreateDonationRequest
@JsonCreator
constructor(
        @JsonProperty("donationType")
        @field:NotNull(message = "Tipo de doação é obrigatório")
        val donationType: DonationType,
        @JsonProperty("targetId")
        @field:NotBlank(message = "ID do destino é obrigatório")
        val targetId: String,
        @JsonProperty("amount")
        @field:NotNull(message = "Valor é obrigatório")
        @field:DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        val amount: Double,
        @JsonProperty("paymentMethod")
        @field:NotNull(message = "Método de pagamento é obrigatório")
        val paymentMethod: PaymentMethod,
        @JsonProperty("donorName") val donorName: String? = null,
        @JsonProperty("donorEmail")
        @field:Email(message = "Email inválido")
        val donorEmail: String? = null,
        @JsonProperty("donorPhone") val donorPhone: String? = null,
        @JsonProperty("donorDocument") val donorDocument: String? = null,
        @JsonProperty("isAnonymous") val isAnonymous: Boolean = false,
        @JsonProperty("message")
        @field:Size(max = 500, message = "Mensagem deve ter no máximo 500 caracteres")
        val message: String? = null,
        @JsonProperty("metadata") val metadata: Map<String, String>? = null
)

data class DonationResponse(
        val id: String,
        val donationType: DonationType,
        val donationTypeDescription: String, // Descrição amigável do tipo
        val targetId: String,
        val targetDescription: String,
        val amount: Double,
        val status: DonationStatus,
        val paymentMethod: PaymentMethod? = null,
        val pagbankChargeId: String? = null,
        val qrCodeText: String? = null,
        val qrCodeImageBase64: String? = null,
        val expiresAt: String? = null,
        val donorName: String? = null,
        val donorEmail: String? = null,
        val isAnonymous: Boolean = false,
        val message: String? = null,
        val paidAt: String? = null,
        val createdAt: String,
        val updatedAt: String
) {
        companion object {
                fun fromEntity(donation: Donation): DonationResponse {
                        return DonationResponse(
                                id = donation.id,
                                donationType = donation.donationType,
                                donationTypeDescription = donation.getTypeDescription(),
                                targetId = donation.targetId,
                                targetDescription = donation.getTargetDescription(),
                                amount = donation.amount,
                                status = donation.status,
                                paymentMethod = donation.paymentMethod,
                                pagbankChargeId = donation.pagbankChargeId,
                                qrCodeText = donation.qrCodeText,
                                qrCodeImageBase64 = donation.qrCodeImageBase64,
                                expiresAt = donation.expiresAt,
                                donorName =
                                        if (donation.isAnonymous) "Anônimo" else donation.donorName,
                                donorEmail = donation.donorEmail,
                                isAnonymous = donation.isAnonymous,
                                message = donation.message,
                                paidAt = donation.paidAt,
                                createdAt = donation.createdAt,
                                updatedAt = donation.updatedAt
                        )
                }
        }
}

data class DonationPageResponse(
        val donations: List<DonationResponse>,
        val page: Int,
        val pageSize: Int,
        val totalElements: Long,
        val totalPages: Int,
        val hasNext: Boolean,
        val hasPrevious: Boolean
)

// DTO para doação simples
data class SimpleDonationRequest
@JsonCreator
constructor(
        @JsonProperty("amount")
        @field:NotNull(message = "Valor é obrigatório")
        @field:DecimalMin(value = "1.00", message = "Valor mínimo da doação é R$ 1,00")
        val amount: Double, // Valor em REAIS (padrão brasileiro: 1.50 = R$ 1,50)
        @JsonProperty("anonymous") val anonymous: Boolean = false,
        @JsonProperty("message")
        @field:Size(max = 500, message = "Mensagem deve ter no máximo 500 caracteres")
        val message: String? = null,
        @JsonProperty("donor") val donor: DonorInfo? = null
)

data class DonorInfo
@JsonCreator
constructor(
        @JsonProperty("name") val name: String? = null,
        @JsonProperty("email") val email: String? = null,
        @JsonProperty("document") val document: String? = null,
        @JsonProperty("phone") val phone: String? = null
)

data class SimpleDonationResponse(
        val id: String,
        val paymentId: String,
        val amount: Double, // Valor em REAIS
        val qrCode: String,
        val qrCodeImage: String? = null,
        val status: String,
        val expiresAt: String? = null
)

data class PagBankWebhookPayload
@JsonCreator
constructor(
        @JsonProperty("id") val id: String? = null,
        @JsonProperty("reference_id") val referenceId: String? = null,
        @JsonProperty("charges") val charges: List<PagBankCharge>? = null,
        @JsonProperty("notification_id") val notificationId: String? = null,
        @JsonProperty("created_at") val createdAt: String? = null
)

data class PagBankCharge
@JsonCreator
constructor(
        @JsonProperty("id") val id: String? = null,
        @JsonProperty("reference_id") val referenceId: String? = null,
        @JsonProperty("status") val status: String? = null,
        @JsonProperty("amount") val amount: PagBankAmount? = null,
        @JsonProperty("paid_at") val paidAt: String? = null,
        @JsonProperty("payment_method") val paymentMethod: PagBankPaymentMethod? = null
)

data class PagBankAmount
@JsonCreator
constructor(
        @JsonProperty("value") val value: Int? = null,
        @JsonProperty("currency") val currency: String? = null
)

data class PagBankPaymentMethod
@JsonCreator
constructor(
        @JsonProperty("type") val type: String? = null,
        @JsonProperty("pix") val pix: PagBankPix? = null
)

data class PagBankPix
@JsonCreator
constructor(
        @JsonProperty("qr_code") val qrCode: String? = null,
        @JsonProperty("qr_code_base64") val qrCodeBase64: String? = null,
        @JsonProperty("expires_at") val expiresAt: String? = null
)

data class CreatePixChargeResponse(
        val donationId: String,
        val chargeId: String,
        val qrCodeText: String,
        val qrCodeImageBase64: String,
        val expiresAt: String,
        val amount: Double,
        val targetDescription: String
)

data class WebhookProcessingResult(
        val success: Boolean,
        val donationId: String?,
        val previousStatus: DonationStatus?,
        val newStatus: DonationStatus?,
        val processed: Boolean,
        val message: String
)

data class ProcessDonationResponse(
        val success: Boolean,
        val message: String,
        val donationId: String
)
