package com.tylerproject.domain.order

import com.fasterxml.jackson.annotation.JsonProperty
import com.tylerproject.domain.donation.DonationStatus
import com.tylerproject.domain.donation.PaymentMethod
import jakarta.validation.Valid
import jakarta.validation.constraints.*

// ==================== REQUEST DTOs ====================

data class CreateOrderItemRequest(
        @JsonProperty("productId")
        @field:NotBlank(message = "Product ID é obrigatório")
        val productId: String,
        @JsonProperty("quantity")
        @field:Min(value = 1, message = "Quantidade mínima é 1")
        val quantity: Int
)

data class CreateOrderShippingAddressRequest(
        @JsonProperty("name") @field:NotBlank(message = "Nome é obrigatório") val name: String,
        @JsonProperty("phone") @field:NotBlank(message = "Telefone é obrigatório") val phone: String,
        @JsonProperty("zipCode") @field:NotBlank(message = "CEP é obrigatório")
        @field:Pattern(regexp = "\\d{8}", message = "CEP deve conter 8 dígitos")
        val zipCode: String,
        @JsonProperty("street") @field:NotBlank(message = "Rua é obrigatória") val street: String,
        @JsonProperty("number") @field:NotBlank(message = "Número é obrigatório") val number: String,
        @JsonProperty("complement") val complement: String? = null,
        @JsonProperty("neighborhood") @field:NotBlank(message = "Bairro é obrigatório")
        val neighborhood: String,
        @JsonProperty("city") @field:NotBlank(message = "Cidade é obrigatória") val city: String,
        @JsonProperty("state")
        @field:NotBlank(message = "Estado é obrigatório")
        @field:Size(min = 2, max = 2, message = "Estado deve ter 2 letras")
        val state: String
) {
    fun toShippingAddress(): ShippingAddress {
        return ShippingAddress(
                name = name,
                phone = phone,
                zipCode = zipCode,
                street = street,
                number = number,
                complement = complement,
                neighborhood = neighborhood,
                city = city,
                state = state.uppercase()
        )
    }
}

data class CreateOrderRequest(
        @JsonProperty("items")
        @field:Valid
        @field:NotEmpty(message = "Items não pode estar vazio")
        val items: List<CreateOrderItemRequest>,
        @JsonProperty("shippingAddress") @field:Valid val shippingAddress:
                CreateOrderShippingAddressRequest,
        @JsonProperty("paymentMethod") @field:NotNull(message = "Método de pagamento é obrigatório")
        val paymentMethod: PaymentMethod,
        @JsonProperty("shippingMethod")
        @field:NotNull(message = "Método de envio é obrigatório")
        val shippingMethod: ShippingMethod,
        @JsonProperty("notes") @field:Size(max = 1000, message = "Notas deve ter no máximo 1000 caracteres")
        val notes: String? = null
)

data class CancelOrderRequest(@JsonProperty("reason") val reason: String? = null)

// ==================== RESPONSE DTOs ====================

data class OrderItemResponse(
        val productId: String,
        val productName: String,
        val quantity: Int,
        val unitPrice: Double,
        val subtotal: Double,
        val imageUrl: String?
) {
    companion object {
        fun fromEntity(item: OrderItem): OrderItemResponse {
            return OrderItemResponse(
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

data class OrderResponse(
        val id: String,
        val orderNumber: String,
        val userId: String,
        val userEmail: String,
        val status: OrderStatus,
        val statusDescription: String,
        val items: List<OrderItemResponse>,
        val subtotal: Double,
        val shippingCost: Double,
        val total: Double,
        val paymentMethod: PaymentMethod,
        val paymentStatus: DonationStatus,
        val shippingMethod: ShippingMethod,
        val shippingMethodDescription: String,
        val shippingAddress: ShippingAddress,
        val trackingCode: String?,
        val carrier: String?,
        val notes: String?,
        val createdAt: String,
        val updatedAt: String,
        val paidAt: String?,
        val shippedAt: String?,
        val deliveredAt: String?,
        val cancelledAt: String?,
        val cancelReason: String?
) {
    companion object {
        fun fromEntity(order: Order): OrderResponse {
            return OrderResponse(
                    id = order.id,
                    orderNumber = order.orderNumber,
                    userId = order.userId,
                    userEmail = order.userEmail,
                    status = order.status,
                    statusDescription = order.getStatusDescription(),
                    items = order.items.map { OrderItemResponse.fromEntity(it) },
                    subtotal = order.subtotal,
                    shippingCost = order.shippingCost,
                    total = order.total,
                    paymentMethod = order.paymentMethod,
                    paymentStatus = order.paymentStatus,
                    shippingMethod = order.shippingMethod,
                    shippingMethodDescription = order.getShippingMethodDescription(),
                    shippingAddress = order.shippingAddress,
                    trackingCode = order.trackingCode,
                    carrier = order.carrier,
                    notes = order.notes,
                    createdAt = order.createdAt,
                    updatedAt = order.updatedAt,
                    paidAt = order.paidAt,
                    shippedAt = order.shippedAt,
                    deliveredAt = order.deliveredAt,
                    cancelledAt = order.cancelledAt,
                    cancelReason = order.cancelReason
            )
        }
    }
}

data class PaymentDetailsResponse(
        val qrCode: String? = null,
        val qrCodeImage: String? = null,
        val paymentId: String? = null,
        val expiresAt: String? = null,
        val boletoUrl: String? = null,
        val boletoBarcode: String? = null,
        val redirectUrl: String? = null
)

data class CreateOrderResponse(
        val order: OrderResponse,
        val paymentDetails: PaymentDetailsResponse? = null
)

data class ListOrdersResponse(
        val orders: List<OrderResponse>,
        val hasNext: Boolean,
        val nextCursor: String? = null
)

data class TrackingEventResponse(val date: String, val status: String, val location: String) {
    companion object {
        fun fromEntity(event: TrackingEvent): TrackingEventResponse {
            return TrackingEventResponse(
                    date = event.date,
                    status = event.status,
                    location = event.location
            )
        }
    }
}

data class TrackingResponse(
        val code: String?,
        val carrier: String?,
        val estimatedDelivery: String?,
        val events: List<TrackingEventResponse>
)

data class OrderDetailsResponse(
        val order: OrderResponse,
        val paymentDetails: PaymentDetailsResponse?,
        val tracking: TrackingResponse?
)

data class CancelOrderResponse(val success: Boolean, val order: OrderResponse)
