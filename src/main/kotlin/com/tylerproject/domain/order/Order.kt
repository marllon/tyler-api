package com.tylerproject.domain.order

import com.tylerproject.domain.donation.DonationStatus
import com.tylerproject.domain.donation.PaymentMethod
import kotlinx.serialization.Serializable

@Serializable
data class ShippingAddress(
        val name: String,
        val phone: String,
        val zipCode: String,
        val street: String,
        val number: String,
        val complement: String? = null,
        val neighborhood: String,
        val city: String,
        val state: String
) {
        fun toFormattedString(): String {
                val complementPart = complement?.let { ", $it" } ?: ""
                return "$street, $number$complementPart - $neighborhood, $city - $state, CEP: $zipCode"
        }
}

@Serializable
data class OrderItem(
        val productId: String,
        val productName: String, // Snapshot do nome no momento da compra
        val quantity: Int,
        val unitPrice: Double, // Snapshot do preço em REAIS
        val subtotal: Double, // quantity * unitPrice
        val imageUrl: String? = null // Snapshot da imagem
)

@Serializable data class TrackingEvent(val date: String, val status: String, val location: String)

@Serializable
data class Order(
        val id: String = "",
        val orderNumber: String = "", // ORD-20250118-001 (único, gerado automaticamente)
        val userId: String, // UID do Firebase Auth
        val userEmail: String,
        val status: OrderStatus = OrderStatus.PENDING,
        val items: List<OrderItem>,
        val subtotal: Double, // Soma dos subtotals dos items
        val shippingCost: Double = 0.0, // Custo de frete (0 para COLLECT_ON_DELIVERY)
        val total: Double, // subtotal + shippingCost
        val paymentMethod: PaymentMethod,
        val paymentStatus: DonationStatus = DonationStatus.PENDING,
        val paymentId: String? = null, // ID do pagamento no gateway (PagBank)
        val donationId: String? = null, // ID da donation criada (tipo ORDER)
        val shippingMethod: ShippingMethod,
        val shippingAddress: ShippingAddress,
        val trackingCode: String? = null,
        val carrier: String? = null,
        val notes: String? = null,
        val createdAt: String = "",
        val updatedAt: String = "",
        val paidAt: String? = null,
        val shippedAt: String? = null,
        val deliveredAt: String? = null,
        val cancelledAt: String? = null,
        val cancelReason: String? = null,
        val trackingEvents: List<TrackingEvent> = emptyList()
) {
        fun isPaid(): Boolean = paymentStatus == DonationStatus.PAID

        fun canBeCancelled(): Boolean = OrderStatus.canBeCancelled(status)

        fun requiresRefund(): Boolean =
                status != OrderStatus.PENDING && paymentStatus == DonationStatus.PAID

        fun getStatusDescription(): String = OrderStatus.getDescription(status)

        fun getShippingMethodDescription(): String = ShippingMethod.getDescription(shippingMethod)
}
