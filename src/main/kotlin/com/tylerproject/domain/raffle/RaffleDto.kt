package com.tylerproject.domain.raffle

import jakarta.validation.constraints.*

// ==================== CREATE ====================

data class CreateRaffleRequest(
        @field:NotBlank(message = "Título é obrigatório")
        @field:Size(min = 3, max = 255, message = "Título deve ter entre 3 e 255 caracteres")
        val title: String,
        @field:NotBlank(message = "Descrição é obrigatória") val description: String,
        @field:NotBlank(message = "Prêmio é obrigatório") val prize: String,
        @field:NotNull(message = "Preço do bilhete é obrigatório")
        @field:DecimalMin(value = "0.01", message = "Preço mínimo do bilhete é R$ 0.01")
        val ticketPrice: Double,
        @field:NotNull(message = "Total de bilhetes é obrigatório")
        @field:Min(value = 1, message = "Deve haver pelo menos 1 bilhete")
        @field:Max(value = 100000, message = "Máximo de 100.000 bilhetes por rifa")
        val totalTickets: Int,
        @field:NotBlank(message = "Data limite é obrigatória")
        val deadline: String, // ISO-8601 format
        val goalId: String? = null,
        val active: Boolean = true
)

// ==================== UPDATE ====================

data class UpdateRaffleRequest(
        val title: String? = null,
        val description: String? = null,
        val prize: String? = null,
        @field:DecimalMin(value = "0.01", message = "Preço mínimo do bilhete é R$ 0.01")
        val ticketPrice: Double? = null,
        @field:Min(value = 1, message = "Deve haver pelo menos 1 bilhete")
        val totalTickets: Int? = null,
        val deadline: String? = null,
        val status: RaffleStatus? = null,
        val goalId: String? = null,
        val active: Boolean? = null
)

// ==================== RESPONSE ====================

data class RaffleResponse(
        val id: String,
        val title: String,
        val description: String,
        val prize: String,
        val ticketPrice: Double,
        val totalTickets: Int,
        val soldTickets: Int,
        val remainingTickets: Int,
        val progressPercentage: Int,
        val deadline: String,
        val status: RaffleStatus,
        val images: List<String>,
        val committedEntropy: String?,
        val revealEntropy: String?,
        val winnerTicketNumber: Int?,
        val goalId: String?,
        val active: Boolean,
        val createdAt: String,
        val updatedAt: String,
        val createdBy: String?
) {
    companion object {
        fun fromEntity(raffle: Raffle): RaffleResponse {
            return RaffleResponse(
                    id = raffle.id,
                    title = raffle.title,
                    description = raffle.description,
                    prize = raffle.prize,
                    ticketPrice = raffle.ticketPrice,
                    totalTickets = raffle.totalTickets,
                    soldTickets = raffle.soldTickets,
                    remainingTickets = raffle.remainingTickets,
                    progressPercentage = raffle.progressPercentage,
                    deadline = raffle.deadline,
                    status = raffle.status,
                    images = raffle.images,
                    committedEntropy = raffle.committedEntropy,
                    revealEntropy = raffle.revealEntropy,
                    winnerTicketNumber = raffle.winnerTicketNumber,
                    goalId = raffle.goalId,
                    active = raffle.active,
                    createdAt = raffle.createdAt,
                    updatedAt = raffle.updatedAt,
                    createdBy = raffle.createdBy
            )
        }
    }
}

data class RafflePageResponse(
        val raffles: List<RaffleResponse>,
        val page: Int,
        val pageSize: Int,
        val totalElements: Long,
        val totalPages: Int,
        val hasNext: Boolean,
        val hasPrevious: Boolean
)

// ==================== DRAW (SORTEIO) ====================

data class RaffleDrawRequest(
        @field:NotBlank(message = "Entropy revelada é obrigatória")
        @field:Size(
                min = 64,
                max = 64,
                message = "Entropy deve ter exatamente 64 caracteres (SHA256)"
        )
        val revealEntropy: String
)

data class RaffleDrawResponse(
        val raffleId: String,
        val winnerTicketNumber: Int,
        val winnerTicket: RaffleTicketResponse?,
        val verificationHash: String,
        val drawnAt: String
)

// ==================== TICKET ====================

data class TicketPurchaseRequest(
        @field:NotNull(message = "Quantidade é obrigatória")
        @field:Min(value = 1, message = "Quantidade mínima é 1 bilhete")
        @field:Max(value = 100, message = "Máximo de 100 bilhetes por compra")
        val quantity: Int,
        @field:NotBlank(message = "Nome é obrigatório") val buyerName: String,
        @field:NotBlank(message = "Email é obrigatório")
        @field:Email(message = "Email inválido")
        val buyerEmail: String,
        val buyerPhone: String? = null,
        val buyerDocument: String? = null
)

data class TicketPurchaseResponse(
        val purchaseId: String,
        val raffleId: String,
        val ticketNumbers: List<Int>,
        val totalAmount: Double,
        val qrCodeText: String,
        val qrCodeImageBase64: String,
        val paymentId: String,
        val expiresAt: String
)

data class RaffleTicketResponse(
        val id: String,
        val raffleId: String,
        val ticketNumber: Int,
        val buyerName: String,
        val buyerEmail: String,
        val buyerPhone: String?,
        val buyerDocument: String?,
        val paymentId: String,
        val paymentStatus: String,
        val purchasedAt: String
) {
    companion object {
        fun fromEntity(ticket: RaffleTicket): RaffleTicketResponse {
            return RaffleTicketResponse(
                    id = ticket.id,
                    raffleId = ticket.raffleId,
                    ticketNumber = ticket.ticketNumber,
                    buyerName = ticket.buyerName,
                    buyerEmail = ticket.buyerEmail,
                    buyerPhone = ticket.buyerPhone,
                    buyerDocument = ticket.buyerDocument,
                    paymentId = ticket.paymentId,
                    paymentStatus = ticket.paymentStatus,
                    purchasedAt = ticket.purchasedAt
            )
        }
    }
}

// ==================== IMAGE UPLOAD ====================

data class ImageUploadResponse(val imageUrl: String, val imageIndex: Int, val totalImages: Int)

// ==================== VERIFICATION ====================

data class DrawVerificationResponse(
        val raffleId: String,
        val committedEntropyHash: String,
        val revealedEntropy: String,
        val winnerTicketNumber: Int,
        val verificationPassed: Boolean,
        val message: String
)
