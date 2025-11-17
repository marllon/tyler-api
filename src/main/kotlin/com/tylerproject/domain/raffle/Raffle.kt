package com.tylerproject.domain.raffle

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
enum class RaffleStatus {
    ACTIVE, // Rifa ativa, aceitando compras
    ENDED, // Prazo encerrado, aguardando sorteio
    DRAWN, // Sorteada, vencedor definido
    CANCELLED // Cancelada (reembolso automático)
}

@Serializable
data class Raffle(
        val id: String = "",
        val title: String = "",
        val description: String = "",
        val prize: String = "",
        val ticketPrice: Double = 0.0,
        val totalTickets: Int = 0,
        val soldTickets: Int = 0,
        val deadline: String = "", // ISO-8601 timestamp
        val status: RaffleStatus = RaffleStatus.ACTIVE,
        val images: List<String> = emptyList(), // URLs das imagens
        val committedEntropy: String? = null, // SHA256 do revealEntropy
        val revealEntropy: String? = null, // Revelado após sorteio
        val winnerTicketNumber: Int? = null,
        val goalId: String? = null, // Meta vinculada (opcional)
        val active: Boolean = true,
        val createdAt: String = "",
        val updatedAt: String = "",
        val createdBy: String? = null // Firebase UID do admin
) {
    @Transient
    val remainingTickets: Int
        get() = (totalTickets - soldTickets).coerceAtLeast(0)

    @Transient
    val progressPercentage: Int
        get() {
            if (totalTickets <= 0) return 0
            return ((soldTickets.toDouble() / totalTickets) * 100).toInt().coerceIn(0, 100)
        }
}

@Serializable
data class RaffleTicket(
        val id: String = "",
        val raffleId: String = "",
        val ticketNumber: Int = 0,
        val buyerName: String = "",
        val buyerEmail: String = "",
        val buyerPhone: String? = null,
        val buyerDocument: String? = null, // CPF
        val paymentId: String = "", // ID da transação PagBank
        val paymentStatus: String = "PENDING", // PENDING, PAID, FAILED, REFUNDED
        val purchasedAt: String = "" // ISO-8601 timestamp
)
