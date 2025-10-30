package com.tylerproject.models

import kotlinx.serialization.Serializable

// DTOs for API requests and responses

@Serializable
data class CreateProductRequest(
        val name: String,
        val description: String,
        val price: Double,
        val imageUrl: String,
        val category: String = "merchandise",
        val stock: Int = 0,
        val active: Boolean = true
)

@Serializable
data class UpdateProductRequest(
        val name: String? = null,
        val description: String? = null,
        val price: Double? = null,
        val imageUrl: String? = null,
        val category: String? = null,
        val stock: Int? = null,
        val active: Boolean? = null
)

@Serializable
data class CreateGoalRequest(
        val title: String,
        val description: String,
        val targetAmount: Double,
        val imageUrl: String? = null,
        val deadline: Long? = null, // Timestamp in seconds
        val active: Boolean = true
)

@Serializable
data class UpdateGoalRequest(
        val title: String? = null,
        val description: String? = null,
        val targetAmount: Double? = null,
        val imageUrl: String? = null,
        val deadline: Long? = null,
        val active: Boolean? = null
)

@Serializable
data class CreateRaffleRequest(
        val title: String,
        val description: String,
        val prize: String,
        val imageUrl: String? = null,
        val ticketPrice: Double,
        val totalTickets: Int,
        val deadline: Long, // Timestamp in seconds
        val goalId: String? = null
)

@Serializable
data class CreateEventRequest(
        val title: String,
        val description: String,
        val date: Long, // Timestamp in seconds
        val location: String,
        val coverImageUrl: String,
        val gallery: List<String> = emptyList()
)

@Serializable
data class UpdateEventRequest(
        val title: String? = null,
        val description: String? = null,
        val date: Long? = null,
        val location: String? = null,
        val coverImageUrl: String? = null,
        val gallery: List<String>? = null,
        val status: String? = null
)

@Serializable
data class CheckoutRequest(
        val type: String, // "order", "donation", "raffle"
        val items: List<CheckoutItem>? = null, // For orders
        val donationAmount: Double? = null, // For donations
        val goalId: String? = null,
        val raffleId: String? = null,
        val ticketQuantity: Int? = null, // For raffles
        val buyer: BuyerInfo,
        val message: String? = null,
        val anonymous: Boolean = false
)

@Serializable data class CheckoutItem(val productId: String, val quantity: Int)

@Serializable
data class CheckoutResponse(
        val success: Boolean,
        val checkoutUrl: String? = null,
        val paymentIntentId: String? = null,
        val orderId: String? = null,
        val donationId: String? = null,
        val ticketNumbers: List<Int>? = null,
        val error: String? = null
)

@Serializable data class DrawRaffleRequest(val revealEntropy: String)

@Serializable
data class DrawRaffleResponse(
        val success: Boolean,
        val winnerTicketNumber: Int? = null,
        val winnerInfo: BuyerInfo? = null,
        val proof: String? = null, // Hash proof
        val error: String? = null
)

@Serializable data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(
        val success: Boolean,
        val token: String? = null,
        val admin: AdminInfo? = null,
        val error: String? = null
)

@Serializable
data class AdminInfo(val id: String, val email: String, val name: String, val role: String)

@Serializable
data class StatsResponse(
        val totalRevenue: Double,
        val totalDonations: Double,
        val totalOrders: Int,
        val totalDonationsCount: Int,
        val activeGoals: Int,
        val activeRaffles: Int,
        val upcomingEvents: Int,
        val recentOrders: List<Order>,
        val recentDonations: List<Donation>,
        val goalProgress: List<GoalProgress>
)

@Serializable
data class GoalProgress(
        val goalId: String,
        val title: String,
        val targetAmount: Double,
        val currentAmount: Double,
        val percentage: Double
)

@Serializable
data class ContactRequest(
        val name: String,
        val email: String,
        val phone: String? = null,
        val subject: String,
        val message: String
)

@Serializable
data class ApiResponse<T>(
        val success: Boolean,
        val data: T? = null,
        val error: String? = null,
        val message: String? = null
)

@Serializable
data class PaginatedResponse<T>(
        val success: Boolean,
        val data: List<T>,
        val total: Int,
        val page: Int,
        val pageSize: Int,
        val totalPages: Int
)

@Serializable
data class ErrorResponse(
        val success: Boolean = false,
        val error: String,
        val code: String? = null,
        val details: Map<String, String>? = null
)
