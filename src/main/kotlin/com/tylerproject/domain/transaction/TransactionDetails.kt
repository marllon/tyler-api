package com.tylerproject.domain.transaction

import kotlinx.serialization.Serializable

/**
 * Interface base para detalhes específicos de cada tipo de transação
 */
sealed interface TransactionDetails

/**
 * Detalhes de doação simples
 */
@Serializable
data class SimpleDonationDetails(
    val message: String? = null,
    val isAnonymous: Boolean = false
) : TransactionDetails

/**
 * Detalhes de contribuição para meta
 */
@Serializable
data class GoalContributionDetails(
    val goalId: String,
    val goalTitle: String,  // Snapshot no momento da contribuição
    val message: String? = null,
    val isAnonymous: Boolean = false
) : TransactionDetails

/**
 * Detalhes de compra de rifa
 */
@Serializable
data class RafflePurchaseDetails(
    val raffleId: String,
    val raffleTitle: String,     // Snapshot
    val rafflePrize: String,      // Snapshot
    val ticketPrice: Double,      // Snapshot (preço unitário)
    val ticketNumbers: List<Int>,
    val ticketCount: Int = ticketNumbers.size,
    val isAnonymous: Boolean = false
) : TransactionDetails

/**
 * Item de pedido (snapshot do produto)
 */
@Serializable
data class OrderItem(
    val productId: String,
    val productName: String,
    val productImage: String? = null,
    val quantity: Int,
    val unitPrice: Double,      // Preço unitário no momento da compra
    val subtotal: Double        // quantity * unitPrice
)

/**
 * Endereço de entrega
 */
@Serializable
data class ShippingAddress(
    val recipientName: String,
    val recipientPhone: String,
    val street: String,
    val number: String,
    val complement: String? = null,
    val neighborhood: String,
    val city: String,
    val state: String,
    val zipCode: String
) {
    fun toFormattedString(): String {
        val complementPart = complement?.let { ", $it" } ?: ""
        return "$street, $number$complementPart - $neighborhood, $city - $state, CEP: $zipCode"
    }
}

/**
 * Método de envio
 */
enum class ShippingMethod {
    COLLECT_ON_DELIVERY,  // Retirar no local (frete R$ 0)
    SEDEX,                // Sedex (frete calculado)
    PAC,                  // PAC (frete calculado)
    CUSTOM;               // Outro método
    
    fun getDescription(): String {
        return when (this) {
            COLLECT_ON_DELIVERY -> "Retirar no Local"
            SEDEX -> "Sedex"
            PAC -> "PAC"
            CUSTOM -> "Outro"
        }
    }
    
    fun getShippingCost(): Double {
        return when (this) {
            COLLECT_ON_DELIVERY -> 0.0
            SEDEX -> 30.0  // Valor fixo por enquanto
            PAC -> 20.0
            CUSTOM -> 0.0
        }
    }
}

/**
 * Evento de rastreamento
 */
@Serializable
data class TrackingEvent(
    val timestamp: String,
    val status: String,
    val description: String,
    val location: String? = null
)

/**
 * Detalhes de pedido de produtos
 */
@Serializable
data class ProductOrderDetails(
    val orderNumber: String,                    // ORD-20251119-0001
    val items: List<OrderItem>,
    val subtotal: Double,                       // Soma dos subtotals
    val shippingMethod: ShippingMethod,
    val shippingCost: Double,
    val shippingAddress: ShippingAddress,
    val trackingCode: String? = null,
    val carrier: String? = null,
    val trackingEvents: List<TrackingEvent> = emptyList(),
    val notes: String? = null,
    val shippedAt: String? = null,
    val deliveredAt: String? = null,
    val estimatedDelivery: String? = null       // Data estimada de entrega
) : TransactionDetails {
    val total: Double
        get() = subtotal + shippingCost
        
    fun hasTracking(): Boolean = trackingCode != null
    
    fun isShipped(): Boolean = shippedAt != null
    
    fun isDelivered(): Boolean = deliveredAt != null
}
