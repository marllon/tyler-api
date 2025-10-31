package com.tylerproject.models

import kotlinx.serialization.Serializable

/**
 * 🏦 PagBank API Models - API OFICIAL
 *
 * Baseado na documentação oficial: https://developer.pagbank.com.br/docs/pedidos-e-pagamentos-order
 */

// ===================================
// 📤 REQUEST MODELS (PARA PAGBANK)
// ===================================

@Serializable
data class PagBankOrderRequest(
        val reference_id: String,
        val customer: PagBankCustomer,
        val items: List<PagBankItem>,
        val charges: List<PagBankCharge>? = null,
        val qr_codes: List<PagBankQrCode>? = null,
        val notification_urls: List<String>? = null
)

@Serializable
data class PagBankCustomer(
        val name: String,
        val email: String,
        val tax_id: String // CPF sem pontuação
)

@Serializable
data class PagBankItem(
        val reference_id: String,
        val name: String,
        val quantity: Int = 1,
        val unit_amount: Long // Valor em centavos
)

@Serializable
data class PagBankCharge(
        val reference_id: String,
        val description: String,
        val amount: PagBankAmount,
        val payment_method: PagBankPaymentMethod
)

@Serializable
data class PagBankAmount(
        val value: Long, // Valor em centavos
        val currency: String = "BRL"
)

@Serializable
data class PagBankPaymentMethod(val type: String = "PIX", val pix: PagBankPix? = null)

@Serializable
data class PagBankPix(
        val expiration_date: String? = null // ISO 8601 format
)

@Serializable data class PagBankQrCode(val amount: PagBankAmount, val expiration_date: String)

@Serializable
data class PagBankQrCodeResponse(
        val id: String,
        val expiration_date: String,
        val amount: PagBankAmount,
        val text: String,
        val arrangements: List<String>? = null,
        val links: List<PagBankLink>? = null
)

// ===================================
// 📥 RESPONSE MODELS (DO PAGBANK)
// ===================================

@Serializable
data class PagBankOrderResponse(
        val id: String,
        val reference_id: String,
        val status: String? = null,
        val created_at: String,
        val customer: PagBankCustomer,
        val items: List<PagBankItem>,
        val charges: List<PagBankChargeResponse>? = null,
        val qr_codes: List<PagBankQrCodeResponse>? = null,
        val links: List<PagBankLink>? = null
)

@Serializable
data class PagBankChargeResponse(
        val id: String,
        val reference_id: String,
        val status: String,
        val created_at: String,
        val amount: PagBankAmount,
        val payment_method: PagBankPaymentMethodResponse,
        val links: List<PagBankLink>? = null
)

@Serializable
data class PagBankPaymentMethodResponse(val type: String, val pix: PagBankPixResponse? = null)

@Serializable
data class PagBankPixResponse(
        val qr_code: String? = null, // Código PIX Copia e Cola
        val qr_code_base64: String? = null, // QR Code em base64
        val expires_at: String? = null // Data de expiração
)

@Serializable
data class PagBankLink(
        val rel: String,
        val href: String,
        val media: String? = null,
        val type: String? = null
)

// ===================================
// 🔔 WEBHOOK MODELS
// ===================================

@Serializable
data class PagBankWebhookPayload(
        val id: String,
        val reference_id: String,
        val status: String,
        val charges: List<PagBankChargeResponse>
)

// ===================================
// ❌ ERROR MODELS
// ===================================

@Serializable data class PagBankErrorResponse(val error_messages: List<PagBankError>)

@Serializable
data class PagBankError(
        val code: String,
        val description: String,
        val parameter_name: String? = null
)
