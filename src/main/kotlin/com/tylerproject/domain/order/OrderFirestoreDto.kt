package com.tylerproject.domain.order

import com.tylerproject.domain.donation.DonationStatus
import com.tylerproject.domain.donation.PaymentMethod

/**
 * DTOs para Firestore - DEVEM ter construtor no-argument e propriedades mutáveis Firestore usa
 * reflexão e precisa disso para desserialização
 */
data class ShippingAddressDto(
        var name: String = "",
        var phone: String = "",
        var zipCode: String = "",
        var street: String = "",
        var number: String = "",
        var complement: String? = null,
        var neighborhood: String = "",
        var city: String = "",
        var state: String = ""
) {
    fun toDomain(): ShippingAddress {
        return ShippingAddress(
                name = name,
                phone = phone,
                zipCode = zipCode,
                street = street,
                number = number,
                complement = complement,
                neighborhood = neighborhood,
                city = city,
                state = state
        )
    }

    companion object {
        fun fromDomain(address: ShippingAddress): ShippingAddressDto {
            return ShippingAddressDto(
                    name = address.name,
                    phone = address.phone,
                    zipCode = address.zipCode,
                    street = address.street,
                    number = address.number,
                    complement = address.complement,
                    neighborhood = address.neighborhood,
                    city = address.city,
                    state = address.state
            )
        }
    }
}

data class OrderItemDto(
        var productId: String = "",
        var productName: String = "",
        var quantity: Int = 0,
        var unitPrice: Double = 0.0,
        var subtotal: Double = 0.0,
        var imageUrl: String? = null
) {
    fun toDomain(): OrderItem {
        return OrderItem(
                productId = productId,
                productName = productName,
                quantity = quantity,
                unitPrice = unitPrice,
                subtotal = subtotal,
                imageUrl = imageUrl
        )
    }

    companion object {
        fun fromDomain(item: OrderItem): OrderItemDto {
            return OrderItemDto(
                    productId = item.productId,
                    productName = item.productName,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    subtotal = item.subtotal,
                    imageUrl = item.imageUrl
            )
        }
    }
}

data class TrackingEventDto(
        var date: String = "",
        var status: String = "",
        var location: String = ""
) {
    fun toDomain(): TrackingEvent {
        return TrackingEvent(date = date, status = status, location = location)
    }

    companion object {
        fun fromDomain(event: TrackingEvent): TrackingEventDto {
            return TrackingEventDto(
                    date = event.date,
                    status = event.status,
                    location = event.location
            )
        }
    }
}

/**
 * DTO para salvar/ler Order do Firestore Usa propriedades mutáveis (var) e tem construtor no-arg
 */
data class OrderFirestoreDto(
        var id: String = "",
        var orderNumber: String = "",
        var userId: String = "",
        var userEmail: String = "",
        var status: String = "PENDING",
        var items: List<OrderItemDto> = emptyList(),
        var subtotal: Double = 0.0,
        var shippingCost: Double = 0.0,
        var total: Double = 0.0,
        var paymentMethod: String = "PIX",
        var paymentStatus: String = "PENDING",
        var paymentId: String? = null,
        var donationId: String? = null,
        var shippingMethod: String = "COLLECT_ON_DELIVERY",
        var shippingAddress: ShippingAddressDto = ShippingAddressDto(),
        var trackingCode: String? = null,
        var carrier: String? = null,
        var notes: String? = null,
        var createdAt: String = "",
        var updatedAt: String = "",
        var paidAt: String? = null,
        var shippedAt: String? = null,
        var deliveredAt: String? = null,
        var cancelledAt: String? = null,
        var cancelReason: String? = null,
        var trackingEvents: List<TrackingEventDto> = emptyList()
) {
    fun toDomain(): Order {
        return Order(
                id = id,
                orderNumber = orderNumber,
                userId = userId,
                userEmail = userEmail,
                status = OrderStatus.valueOf(status),
                items = items.map { it.toDomain() },
                subtotal = subtotal,
                shippingCost = shippingCost,
                total = total,
                paymentMethod = PaymentMethod.valueOf(paymentMethod),
                paymentStatus = DonationStatus.valueOf(paymentStatus),
                paymentId = paymentId,
                donationId = donationId,
                shippingMethod = ShippingMethod.valueOf(shippingMethod),
                shippingAddress = shippingAddress.toDomain(),
                trackingCode = trackingCode,
                carrier = carrier,
                notes = notes,
                createdAt = createdAt,
                updatedAt = updatedAt,
                paidAt = paidAt,
                shippedAt = shippedAt,
                deliveredAt = deliveredAt,
                cancelledAt = cancelledAt,
                cancelReason = cancelReason,
                trackingEvents = trackingEvents.map { it.toDomain() }
        )
    }

    companion object {
        fun fromDomain(order: Order): OrderFirestoreDto {
            return OrderFirestoreDto(
                    id = order.id,
                    orderNumber = order.orderNumber,
                    userId = order.userId,
                    userEmail = order.userEmail,
                    status = order.status.name,
                    items = order.items.map { OrderItemDto.fromDomain(it) },
                    subtotal = order.subtotal,
                    shippingCost = order.shippingCost,
                    total = order.total,
                    paymentMethod = order.paymentMethod.name,
                    paymentStatus = order.paymentStatus.name,
                    paymentId = order.paymentId,
                    donationId = order.donationId,
                    shippingMethod = order.shippingMethod.name,
                    shippingAddress = ShippingAddressDto.fromDomain(order.shippingAddress),
                    trackingCode = order.trackingCode,
                    carrier = order.carrier,
                    notes = order.notes,
                    createdAt = order.createdAt,
                    updatedAt = order.updatedAt,
                    paidAt = order.paidAt,
                    shippedAt = order.shippedAt,
                    deliveredAt = order.deliveredAt,
                    cancelledAt = order.cancelledAt,
                    cancelReason = order.cancelReason,
                    trackingEvents = order.trackingEvents.map { TrackingEventDto.fromDomain(it) }
            )
        }
    }
}
