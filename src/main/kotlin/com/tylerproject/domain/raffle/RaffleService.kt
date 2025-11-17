package com.tylerproject.domain.raffle

import com.tylerproject.domain.donation.PagBankWebhookPayload
import com.tylerproject.domain.donation.WebhookProcessingResult
import org.springframework.web.multipart.MultipartFile

interface RaffleService {
    fun createRaffle(request: CreateRaffleRequest, createdByUid: String): RaffleResponse
    fun updateRaffle(id: String, request: UpdateRaffleRequest): RaffleResponse?
    fun getRaffleById(id: String): RaffleResponse?
    fun listRaffles(
            page: Int,
            pageSize: Int,
            status: RaffleStatus?,
            activeOnly: Boolean,
            sortBy: String,
            sortDirection: String,
            searchTerm: String?
    ): RafflePageResponse
    fun deleteRaffle(id: String): Boolean
    fun uploadImages(id: String, files: Array<MultipartFile>): List<String>
    fun deleteImage(id: String, imageIndex: Int): Boolean
    fun purchaseTickets(raffleId: String, request: TicketPurchaseRequest): TicketPurchaseResponse
    fun getRaffleTickets(raffleId: String): List<RaffleTicketResponse>
    fun getAvailableTickets(raffleId: String): List<Int>
    fun drawRaffle(
            raffleId: String,
            request: RaffleDrawRequest,
            adminUid: String
    ): RaffleDrawResponse
    fun cancelRaffle(raffleId: String, adminUid: String): Boolean
    fun verifyDraw(raffleId: String): DrawVerificationResponse
    fun updateExpiredRaffles(): Int
    fun processWebhook(payload: PagBankWebhookPayload): WebhookProcessingResult
}
