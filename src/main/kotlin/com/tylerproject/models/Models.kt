package com.tylerproject.models

import kotlinx.serialization.Serializable
import com.google.cloud.Timestamp

@Serializable
data class Product(
    val id: String = "",
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String = "merchandise", // merchandise, clothing, etc
    val stock: Int = 0,
    val active: Boolean = true,
    @Serializable(with = TimestampSerializer::class)
    val createdAt: Timestamp = Timestamp.now(),
    @Serializable(with = TimestampSerializer::class)
    val updatedAt: Timestamp = Timestamp.now()
)

@Serializable
data class Goal(
    val id: String = "",
    val title: String,
    val description: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val imageUrl: String? = null,
    @Serializable(with = TimestampSerializer::class)
    val deadline: Timestamp? = null,
    val active: Boolean = true,
    @Serializable(with = TimestampSerializer::class)
    val createdAt: Timestamp = Timestamp.now(),
    @Serializable(with = TimestampSerializer::class)
    val updatedAt: Timestamp = Timestamp.now()
)

@Serializable
data class Raffle(
    val id: String = "",
    val title: String,
    val description: String,
    val prize: String,
    val imageUrl: String? = null,
    val ticketPrice: Double,
    val totalTickets: Int,
    val soldTickets: Int = 0,
    @Serializable(with = TimestampSerializer::class)
    val deadline: Timestamp,
    val status: RaffleStatus = RaffleStatus.ACTIVE,
    val committedEntropy: String? = null,
    val revealedEntropy: String? = null,
    val winnerTicketNumber: Int? = null,
    val goalId: String? = null, // Meta associada (opcional)
    @Serializable(with = TimestampSerializer::class)
    val createdAt: Timestamp = Timestamp.now(),
    @Serializable(with = TimestampSerializer::class)
    val updatedAt: Timestamp = Timestamp.now()
)

@Serializable
enum class RaffleStatus {
    ACTIVE,    // Vendendo bilhetes
    ENDED,     // Vendas encerradas, aguardando sorteio
    DRAWN,     // Sorteado
    CANCELLED  // Cancelada
}

@Serializable
data class RaffleTicket(
    val id: String = "",
    val raffleId: String,
    val number: Int,
    val buyer: BuyerInfo,
    val status: String = "paid",
    val paymentIntentId: String,
    @Serializable(with = TimestampSerializer::class)
    val createdAt: Timestamp = Timestamp.now()
)

@Serializable
data class Event(
    val id: String = "",
    val title: String,
    val description: String,
    @Serializable(with = TimestampSerializer::class)
    val date: Timestamp,
    val location: String,
    val coverImageUrl: String,
    val gallery: List<String> = emptyList(),
    val status: EventStatus = EventStatus.UPCOMING,
    @Serializable(with = TimestampSerializer::class)
    val createdAt: Timestamp = Timestamp.now(),
    @Serializable(with = TimestampSerializer::class)
    val updatedAt: Timestamp = Timestamp.now()
)

@Serializable
enum class EventStatus {
    UPCOMING,
    PAST,
    CANCELLED
}

@Serializable
data class Order(
    val id: String = "",
    val userId: String? = null,
    val items: List<OrderItem>,
    val totalAmount: Double,
    val status: OrderStatus = OrderStatus.PENDING,
    val paymentProvider: String,
    val paymentIntentId: String,
    val buyer: BuyerInfo,
    val goalId: String? = null, // Meta para onde vai a arrecadação
    @Serializable(with = TimestampSerializer::class)
    val createdAt: Timestamp = Timestamp.now(),
    @Serializable(with = TimestampSerializer::class)
    val updatedAt: Timestamp = Timestamp.now()
)

@Serializable
data class OrderItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double
)

@Serializable
enum class OrderStatus {
    PENDING,
    PAID,
    CANCELLED,
    REFUNDED
}

@Serializable
data class Donation(
    val id: String = "",
    val userId: String? = null,
    val goalId: String,
    val amount: Double,
    val status: DonationStatus = DonationStatus.PENDING,
    val paymentProvider: String,
    val paymentIntentId: String,
    val donor: DonorInfo,
    val message: String? = null,
    val anonymous: Boolean = false,
    @Serializable(with = TimestampSerializer::class)
    val createdAt: Timestamp = Timestamp.now(),
    @Serializable(with = TimestampSerializer::class)
    val updatedAt: Timestamp = Timestamp.now()
)

@Serializable
enum class DonationStatus {
    PENDING,
    COMPLETED,
    CANCELLED,
    REFUNDED
}

@Serializable
data class BuyerInfo(
    val name: String,
    val email: String,
    val phone: String? = null,
    val document: String? = null // CPF para Brasil
)

@Serializable
data class DonorInfo(
    val name: String,
    val email: String,
    val phone: String? = null
)

@Serializable
data class ContactMessage(
    val id: String = "",
    val name: String,
    val email: String,
    val phone: String? = null,
    val subject: String,
    val message: String,
    val status: String = "new", // new, read, replied
    @Serializable(with = TimestampSerializer::class)
    val createdAt: Timestamp = Timestamp.now()
)

@Serializable
data class Admin(
    val id: String = "",
    val email: String,
    val name: String,
    val role: String = "admin", // admin, super_admin
    @Serializable(with = TimestampSerializer::class)
    val createdAt: Timestamp = Timestamp.now()
)

// Serializer para Timestamp do Firestore
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object TimestampSerializer : KSerializer<Timestamp> {
    override val descriptor: SerialDescriptor = 
        PrimitiveSerialDescriptor("Timestamp", PrimitiveKind.LONG)
    
    override fun serialize(encoder: Encoder, value: Timestamp) {
        encoder.encodeLong(value.seconds)
    }
    
    override fun deserialize(decoder: Decoder): Timestamp {
        return Timestamp.ofTimeSecondsAndNanos(decoder.decodeLong(), 0)
    }
}
