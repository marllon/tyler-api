package com.tylerproject.domain.transaction

import kotlinx.serialization.Serializable

/**
 * CustomerInfo - Informações do cliente na transação
 */
@Serializable
data class CustomerInfo(
    val userId: String,                     // Firebase UID
    val name: String,
    val email: String,
    val phone: String? = null,
    val document: String? = null,           // CPF/CNPJ
    val isAnonymous: Boolean = false        // Se doação é anônima
)

/**
 * PaymentInfo - Informações de pagamento
 */
@Serializable
data class PaymentInfo(
    val method: TransactionPaymentMethod,   // PIX, CREDIT_CARD, etc
    val paymentId: String,                  // ID do payment no PagBank/gateway
    val qrCodeText: String? = null,         // PIX: código copia-e-cola
    val qrCodeImageBase64: String? = null,  // PIX: imagem QR code em base64
    val expiresAt: String? = null,          // Quando o pagamento expira
    val paidAt: String? = null,             // Quando foi pago
    val installments: Int? = null,          // Número de parcelas (cartão)
    val authorizationCode: String? = null,  // Código de autorização
    val nsu: String? = null,                // NSU da transação
    val tid: String? = null                 // TID da transação
)

/**
 * Transaction - Entidade Central do Sistema Tyler
 * 
 * TODAS as operações financeiras são transações:
 * - Doações simples
 * - Contribuições para metas
 * - Compras de rifas
 * - Pedidos de produtos
 * 
 * Esta é a single source of truth para rastreamento financeiro.
 */
@Serializable
data class Transaction(
    val id: String = "",
    
    // Tipo e classificação
    val type: TransactionType,
    val status: TransactionStatus = TransactionStatus.PENDING,
    
    // Valores
    val amount: Double,                     // Valor total da transação
    val currency: String = "BRL",
    
    // Cliente
    val customer: CustomerInfo,
    
    // Pagamento
    val payment: PaymentInfo? = null,
    
    // Detalhes específicos por tipo (apenas UM será preenchido baseado no type)
    val simpleDonationDetails: SimpleDonationDetails? = null,
    val goalContributionDetails: GoalContributionDetails? = null,
    val rafflePurchaseDetails: RafflePurchaseDetails? = null,
    val productOrderDetails: ProductOrderDetails? = null,
    
    // Vinculação (opcional)
    val targetId: String? = null,           // ID da meta/rifa/etc (se aplicável)
    val targetType: String? = null,         // "goal", "raffle", etc
    
    // Metadata e rastreamento
    val notes: String? = null,
    val webhookData: String? = null,
    
    // Timestamps
    val createdAt: String = "",
    val updatedAt: String = "",
    val paidAt: String? = null,
    val completedAt: String? = null,
    val cancelledAt: String? = null,
    val refundedAt: String? = null,
    
    // Cancelamento/Reembolso
    val cancelReason: String? = null,
    val refundReason: String? = null
) {
    /**
     * Verifica se a transação foi paga
     */
    fun isPaid(): Boolean = status == TransactionStatus.PAID || 
                           status == TransactionStatus.PROCESSING ||
                           status == TransactionStatus.COMPLETED
    
    /**
     * Verifica se pode ser cancelada
     */
    fun canBeCancelled(): Boolean = status.canBeCancelled()
    
    /**
     * Verifica se requer reembolso ao cancelar
     */
    fun requiresRefund(): Boolean = status.requiresRefund() && isPaid()
    
    /**
     * Retorna descrição legível do tipo
     */
    fun getTypeDescription(): String = type.getDescription()
    
    /**
     * Retorna descrição legível do status
     */
    fun getStatusDescription(): String = status.getDescription()
    
    /**
     * Verifica se é uma doação (qualquer tipo exceto pedido de produto)
     */
    fun isDonation(): Boolean = type != TransactionType.PRODUCT_ORDER
    
    /**
     * Verifica se é um pedido de produto
     */
    fun isOrder(): Boolean = type == TransactionType.PRODUCT_ORDER
    
    /**
     * Verifica se é uma compra de rifa
     */
    fun isRafflePurchase(): Boolean = type == TransactionType.RAFFLE_PURCHASE
    
    /**
     * Verifica se é contribuição para meta
     */
    fun isGoalContribution(): Boolean = type == TransactionType.GOAL_CONTRIBUTION
    
    /**
     * Retorna os detalhes tipados baseado no tipo da transação
     */
    fun getDetails(): TransactionDetails? {
        return when (type) {
            TransactionType.SIMPLE_DONATION -> simpleDonationDetails
            TransactionType.GOAL_CONTRIBUTION -> goalContributionDetails
            TransactionType.RAFFLE_PURCHASE -> rafflePurchaseDetails
            TransactionType.PRODUCT_ORDER -> productOrderDetails
        }
    }
}
