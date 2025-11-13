package com.tylerproject.models
import kotlinx.serialization.Serializable
@Serializable
data class CreateProductRequest(
        val name: String,
        val description: String,
        val price: Long, // em centavos
        val imageUrl: String? = null,
        val category: String? = null,
        val stock: Int? = null
)
@Serializable
data class UpdateProductRequest(
        val name: String? = null,
        val description: String? = null,
        val price: Long? = null,
        val imageUrl: String? = null,
        val active: Boolean? = null,
        val category: String? = null,
        val stock: Int? = null
)
@Serializable
data class ProductResponse(
        val success: Boolean,
        val product: Product? = null,
        val error: String? = null
)
@Serializable
data class ProductsListResponse(
        val success: Boolean,
        val products: List<Product> = emptyList(),
        val total: Int = 0,
        val page: Int = 1,
        val pageSize: Int = 20,
        val error: String? = null
)
@Serializable data class OrderItemRequest(val productId: String, val quantity: Int = 1)
@Serializable
data class CreateOrderRequest(
        val items: List<OrderItemRequest>,
        val buyer: BuyerInfo,
        val goalId: String? = null,
        val notes: String? = null
)
@Serializable
data class OrderCheckoutResponse(
        val success: Boolean,
        val orderId: String? = null,
        val referenceId: String? = null,
        val totalAmount: Long? = null,
        val status: String? = null,
        val paymentInfo: PaymentInfo? = null,
        val qrCode: String? = null,
        val qrCodeBase64: String? = null,
        val pixCopyPaste: String? = null,
        val expirationDate: String? = null,
        val error: String? = null
)
@Serializable
data class OrderResponse(val success: Boolean, val order: Order? = null, val error: String? = null)
@Serializable
data class OrdersListResponse(
        val success: Boolean,
        val orders: List<Order> = emptyList(),
        val total: Int = 0,
        val page: Int = 1,
        val pageSize: Int = 20,
        val error: String? = null
)
@Serializable
data class CreateGoalRequest(
        val title: String,
        val description: String,
        val targetAmount: Long,
        val deadline: String? = null,
        val imageUrl: String? = null,
        val category: String? = null
)
@Serializable
data class UpdateGoalRequest(
        val title: String? = null,
        val description: String? = null,
        val targetAmount: Long? = null,
        val deadline: String? = null,
        val active: Boolean? = null,
        val imageUrl: String? = null,
        val category: String? = null
)
@Serializable
data class GoalResponse(
        val success: Boolean,
        val goal: Goal? = null,
        val stats: GoalStats? = null,
        val error: String? = null
)
@Serializable
data class GoalsListResponse(
        val success: Boolean,
        val goals: List<Goal> = emptyList(),
        val total: Int = 0,
        val page: Int = 1,
        val pageSize: Int = 20,
        val error: String? = null
)
@Serializable
data class CreateDonationRequest(
        val amount: Long,
        val message: String? = null,
        val donor: BuyerInfo,
        val goalId: String? = null,
        val anonymous: Boolean = false
)
@Serializable
data class DonationCheckoutResponse(
        val success: Boolean,
        val donationId: String? = null,
        val referenceId: String? = null,
        val amount: Long? = null,
        val status: String? = null,
        val paymentInfo: PaymentInfo? = null,
        val qrCode: String? = null,
        val qrCodeBase64: String? = null,
        val pixCopyPaste: String? = null,
        val expirationDate: String? = null,
        val error: String? = null
)
@Serializable
data class DonationResponse(
        val success: Boolean,
        val donation: Donation? = null,
        val error: String? = null
)
@Serializable
data class DonationsListResponse(
        val success: Boolean,
        val donations: List<Donation> = emptyList(),
        val total: Int = 0,
        val page: Int = 1,
        val pageSize: Int = 20,
        val error: String? = null
)
@Serializable
data class CreateRaffleRequest(
        val title: String,
        val description: String,
        val prize: String,
        val prizeImageUrl: String? = null,
        val ticketPrice: Long,
        val totalTickets: Int,
        val deadline: String,
        val goalId: String? = null
)
@Serializable
data class UpdateRaffleRequest(
        val title: String? = null,
        val description: String? = null,
        val prize: String? = null,
        val prizeImageUrl: String? = null,
        val ticketPrice: Long? = null,
        val deadline: String? = null,
        val status: String? = null,
        val goalId: String? = null
)
@Serializable
data class RaffleTicketRequest(
        val ticketNumbers: List<Int>? = null, // se null, aloca automaticamente
        val quantity: Int = 1,
        val buyer: BuyerInfo
)
@Serializable
data class RaffleCheckoutResponse(
        val success: Boolean,
        val raffleId: String? = null,
        val ticketNumbers: List<Int> = emptyList(),
        val totalAmount: Long? = null,
        val referenceId: String? = null,
        val status: String? = null,
        val paymentInfo: PaymentInfo? = null,
        val qrCode: String? = null,
        val qrCodeBase64: String? = null,
        val pixCopyPaste: String? = null,
        val expirationDate: String? = null,
        val error: String? = null
)
@Serializable
data class RaffleResponse(
        val success: Boolean,
        val raffle: Raffle? = null,
        val availableTickets: List<Int> = emptyList(),
        val soldTickets: List<Int> = emptyList(),
        val error: String? = null
)
@Serializable
data class RafflesListResponse(
        val success: Boolean,
        val raffles: List<Raffle> = emptyList(),
        val total: Int = 0,
        val page: Int = 1,
        val pageSize: Int = 20,
        val error: String? = null
)
@Serializable
data class RaffleDrawRequest(
        val randomSeed: String, // para transparência do sorteio
        val witnessHash: String? = null
)
@Serializable
data class RaffleDrawResponse(
        val success: Boolean,
        val raffleId: String? = null,
        val winnerTicketNumber: Int? = null,
        val winnerName: String? = null,
        val drawHash: String? = null,
        val drawDate: String? = null,
        val error: String? = null
)
@Serializable
data class CreateEventRequest(
        val title: String,
        val description: String,
        val date: String,
        val endDate: String? = null,
        val location: String,
        val coverImageUrl: String? = null,
        val maxParticipants: Int? = null,
        val registrationRequired: Boolean = false,
        val registrationDeadline: String? = null
)
@Serializable
data class UpdateEventRequest(
        val title: String? = null,
        val description: String? = null,
        val date: String? = null,
        val endDate: String? = null,
        val location: String? = null,
        val status: String? = null,
        val coverImageUrl: String? = null,
        val gallery: List<String>? = null,
        val maxParticipants: Int? = null,
        val registrationRequired: Boolean? = null,
        val registrationDeadline: String? = null
)
@Serializable
data class EventRegistrationRequest(val participant: BuyerInfo, val notes: String? = null)
@Serializable
data class EventResponse(
        val success: Boolean,
        val event: Event? = null,
        val participants: List<EventParticipant> = emptyList(),
        val availableSpots: Int? = null,
        val error: String? = null
)
@Serializable
data class EventsListResponse(
        val success: Boolean,
        val events: List<Event> = emptyList(),
        val total: Int = 0,
        val page: Int = 1,
        val pageSize: Int = 20,
        val error: String? = null
)
@Serializable
data class AuditLogResponse(
        val success: Boolean,
        val logs: List<AuditLog> = emptyList(),
        val total: Int = 0,
        val page: Int = 1,
        val pageSize: Int = 50,
        val error: String? = null
)
@Serializable
data class AuditLogRequest(
        val entity: String? = null,
        val entityId: String? = null,
        val action: String? = null,
        val userId: String? = null,
        val fromDate: String? = null,
        val toDate: String? = null,
        val page: Int = 1,
        val pageSize: Int = 50
)
@Serializable
data class DashboardResponse(
        val success: Boolean,
        val stats: DashboardStats? = null,
        val recentOrders: List<Order> = emptyList(),
        val recentDonations: List<Donation> = emptyList(),
        val activeGoals: List<Goal> = emptyList(),
        val activeRaffles: List<Raffle> = emptyList(),
        val upcomingEvents: List<Event> = emptyList(),
        val error: String? = null
)
@Serializable
data class GenericResponse(
        val success: Boolean,
        val message: String? = null,
        val data: String? = null, // JSON string for flexible data
        val error: String? = null
)
@Serializable
data class SuccessResponse(
        val success: Boolean = true,
        val message: String,
        val id: String? = null
)
@Serializable
data class TylerErrorResponse(
        val success: Boolean = false,
        val error: String,
        val details: String? = null
)
@Serializable
data class PaginationRequest(
        val page: Int = 1,
        val pageSize: Int = 20,
        val sortBy: String? = null,
        val sortOrder: String = "desc", // asc, desc
        val filter: String? = null
)
@Serializable
data class UploadResponse(
        val success: Boolean,
        val url: String? = null,
        val fileName: String? = null,
        val fileSize: Long? = null,
        val error: String? = null
)
