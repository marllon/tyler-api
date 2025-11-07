package com.tylerproject.models

import kotlinx.serialization.Serializable

/**
 * 🏗️ Tyler API - Domain Models
 *
 * Modelos de domínio para toda a plataforma de caridade:
 * - Products & Orders (Produtos e Pedidos)
 * - Goals & Donations (Metas de Arrecadação e Doações)
 * - Raffles & Tickets (Rifas Solidárias e Bilhetes)
 * - Events (Eventos Solidários)
 * - AuditLog (Auditoria e Transparência)
 */

// ================== COMMON TYPES ==================

enum class PaymentStatus {
    NEW,
    WAITING_PAYMENT,
    PAID,
    FAILED,
    CANCELLED,
    EXPIRED
}

enum class PaymentProvider {
    PAGBANK
}

enum class PaymentMethod {
    PIX
}

@Serializable
data class PaymentInfo(
        val provider: String = "PAGBANK",
        val method: String = "PIX",
        val status: String = "NEW",
        val providerOrderId: String? = null,
        val providerTransactionId: String? = null,
        val qrCode: String? = null,
        val qrCodeBase64: String? = null,
        val pixCopyPaste: String? = null,
        val pixEndToEndId: String? = null,
        val expirationDate: String? = null,
        val paidAt: String? = null,
        val amount: Long,
        val currency: String = "BRL"
)

@Serializable
data class BuyerInfo(
        val name: String,
        val email: String,
        val document: String, // CPF
        val phone: String? = null
)

// ================== PRODUCTS & ORDERS ==================

@Serializable
data class Product(
        val id: String,
        val name: String,
        val description: String,
        val price: Long, // em centavos
        val imageUrl: String? = null,
        val active: Boolean = true,
        val category: String? = null,
        val stock: Int? = null, // null = unlimited
        val createdAt: String,
        val updatedAt: String,
        val createdBy: String? = null
)

@Serializable
data class OrderItem(
        val productId: String,
        val productName: String,
        val productPrice: Long,
        val quantity: Int = 1,
        val subtotal: Long // quantity * productPrice
)

@Serializable
data class Order(
        val id: String,
        val referenceId: String, // order_${timestamp}
        val items: List<OrderItem>,
        val buyer: BuyerInfo,
        val totalAmount: Long,
        val goalId: String? = null, // vinculado a uma meta
        val status: String = "NEW",
        val paymentInfo: PaymentInfo,
        val notes: String? = null,
        val createdAt: String,
        val updatedAt: String
)

// ================== GOALS & DONATIONS ==================

@Serializable
data class Goal(
        val id: String,
        val title: String,
        val description: String,
        val targetAmount: Long, // em centavos
        val currentAmount: Long = 0,
        val deadline: String? = null,
        val active: Boolean = true,
        val imageUrl: String? = null,
        val category: String? = null,
        val progress: Double = 0.0, // calculated: currentAmount / targetAmount
        val createdAt: String,
        val updatedAt: String,
        val createdBy: String? = null
)

@Serializable
data class Donation(
        val id: String,
        val referenceId: String, // donation_${timestamp}
        val amount: Long,
        val message: String? = null,
        val donor: BuyerInfo,
        val goalId: String? = null,
        val anonymous: Boolean = false,
        val status: String = "NEW",
        val paymentInfo: PaymentInfo,
        val createdAt: String,
        val updatedAt: String
)

// ================== RAFFLES & TICKETS ==================

enum class RaffleStatus {
    DRAFT,
    ACTIVE,
    SOLD_OUT,
    DRAWN,
    CANCELLED
}

@Serializable
data class Raffle(
        val id: String,
        val title: String,
        val description: String,
        val prize: String,
        val prizeImageUrl: String? = null,
        val ticketPrice: Long, // em centavos
        val totalTickets: Int,
        val soldTickets: Int = 0,
        val deadline: String,
        val status: String = "DRAFT",
        val drawDate: String? = null,
        val winnerTicketNumber: Int? = null,
        val winnerName: String? = null,
        val drawHash: String? = null, // SHA256 para transparência
        val goalId: String? = null, // vinculado a uma meta
        val createdAt: String,
        val updatedAt: String,
        val createdBy: String? = null
)

@Serializable
data class Ticket(
        val id: String,
        val raffleId: String,
        val number: Int,
        val buyer: BuyerInfo,
        val paymentIntentId: String, // reference para o pagamento
        val status: String = "RESERVED", // RESERVED, PAID, CANCELLED
        val reservedAt: String,
        val paidAt: String? = null,
        val createdAt: String
)

// ================== EVENTS ==================

enum class EventStatus {
    DRAFT,
    PUBLISHED,
    CANCELLED,
    COMPLETED
}

@Serializable
data class Event(
        val id: String,
        val title: String,
        val description: String,
        val date: String,
        val endDate: String? = null,
        val location: String,
        val status: String = "DRAFT",
        val coverImageUrl: String? = null,
        val gallery: List<String> = emptyList(), // URLs das fotos
        val maxParticipants: Int? = null,
        val currentParticipants: Int = 0,
        val registrationRequired: Boolean = false,
        val registrationDeadline: String? = null,
        val createdAt: String,
        val updatedAt: String,
        val createdBy: String? = null
)

@Serializable
data class EventParticipant(
        val id: String,
        val eventId: String,
        val participant: BuyerInfo,
        val registeredAt: String,
        val attended: Boolean = false,
        val notes: String? = null
)

// ================== AUDIT LOG ==================

@Serializable
data class AuditLog(
        val id: String,
        val entity: String, // "Order", "Goal", "Raffle", etc.
        val entityId: String,
        val action: String, // "CREATE", "UPDATE", "DELETE", "PAYMENT_RECEIVED", etc.
        val before: String? = null, // JSON snapshot before
        val after: String? = null, // JSON snapshot after
        val userId: String? = null, // Firebase UID
        val userEmail: String? = null,
        val ipAddress: String? = null,
        val userAgent: String? = null,
        val timestamp: String,
        val details: Map<String, String> = emptyMap()
)

// ================== STATISTICS ==================

@Serializable
data class DashboardStats(
        val totalDonations: Long,
        val totalOrders: Long,
        val totalRevenue: Long,
        val activeGoals: Int,
        val completedGoals: Int,
        val activeRaffles: Int,
        val completedRaffles: Int,
        val upcomingEvents: Int,
        val recentTransactions: Int,
        val lastUpdated: String
)

@Serializable
data class GoalStats(
        val goalId: String,
        val totalDonations: Long,
        val totalOrders: Long,
        val totalAmount: Long,
        val donorsCount: Int,
        val ordersCount: Int,
        val averageDonation: Long,
        val lastDonationAt: String?
)
