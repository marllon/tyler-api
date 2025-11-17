package com.tylerproject.domain.raffle

import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.QueryDocumentSnapshot
import org.springframework.stereotype.Repository

@Repository
class RaffleTicketRepository(private val firestore: Firestore) {
    private val collection = "raffle_tickets"

    fun save(ticket: RaffleTicket): RaffleTicket {
        val docRef =
                if (ticket.id.isBlank()) {
                    firestore.collection(collection).document()
                } else {
                    firestore.collection(collection).document(ticket.id)
                }

        val ticketToSave = ticket.copy(id = docRef.id)
        docRef.set(ticketToSave).get()
        return ticketToSave
    }

    fun saveAll(tickets: List<RaffleTicket>): List<RaffleTicket> {
        return tickets.map { save(it) }
    }

    fun findById(id: String): RaffleTicket? {
        val docRef = firestore.collection(collection).document(id)
        val snapshot = docRef.get().get()
        return snapshot.toObject(RaffleTicket::class.java)?.copy(id = snapshot.id)
    }

    fun findByRaffleId(raffleId: String): List<RaffleTicket> {
        val snapshot =
                firestore
                        .collection(collection)
                        .whereEqualTo("raffleId", raffleId)
                        .orderBy("ticketNumber")
                        .get()
                        .get()

        return snapshot.documents.mapNotNull { doc: QueryDocumentSnapshot ->
            doc.toObject(RaffleTicket::class.java).copy(id = doc.id)
        }
    }

    fun findByRaffleIdAndTicketNumber(raffleId: String, ticketNumber: Int): RaffleTicket? {
        val snapshot =
                firestore
                        .collection(collection)
                        .whereEqualTo("raffleId", raffleId)
                        .whereEqualTo("ticketNumber", ticketNumber)
                        .limit(1)
                        .get()
                        .get()

        return snapshot.documents.firstOrNull()?.let { doc: QueryDocumentSnapshot ->
            doc.toObject(RaffleTicket::class.java).copy(id = doc.id)
        }
    }

    fun existsByRaffleIdAndTicketNumber(raffleId: String, ticketNumber: Int): Boolean {
        val snapshot =
                firestore
                        .collection(collection)
                        .whereEqualTo("raffleId", raffleId)
                        .whereEqualTo("ticketNumber", ticketNumber)
                        .limit(1)
                        .get()
                        .get()

        return !snapshot.isEmpty
    }

    fun countByRaffleIdAndPaymentStatus(raffleId: String, paymentStatus: String): Long {
        val snapshot =
                firestore
                        .collection(collection)
                        .whereEqualTo("raffleId", raffleId)
                        .whereEqualTo("paymentStatus", paymentStatus)
                        .get()
                        .get()

        return snapshot.size().toLong()
    }

    fun findTicketNumbersByRaffleId(raffleId: String): List<Int> {
        val snapshot =
                firestore
                        .collection(collection)
                        .whereEqualTo("raffleId", raffleId)
                        .orderBy("ticketNumber")
                        .get()
                        .get()

        return snapshot.documents.mapNotNull { doc -> doc.getLong("ticketNumber")?.toInt() }
    }

    fun findByBuyerEmail(buyerEmail: String): List<RaffleTicket> {
        val snapshot =
                firestore
                        .collection(collection)
                        .whereEqualTo("buyerEmail", buyerEmail)
                        .orderBy("purchasedAt")
                        .get()
                        .get()

        return snapshot.documents.mapNotNull { doc: QueryDocumentSnapshot ->
            doc.toObject(RaffleTicket::class.java).copy(id = doc.id)
        }
    }

    fun findByPaymentId(paymentId: String): List<RaffleTicket> {
        val snapshot =
                firestore.collection(collection).whereEqualTo("paymentId", paymentId).get().get()

        return snapshot.documents.mapNotNull { doc: QueryDocumentSnapshot ->
            doc.toObject(RaffleTicket::class.java).copy(id = doc.id)
        }
    }

    fun update(id: String, updates: Map<String, Any?>): RaffleTicket? {
        val docRef = firestore.collection(collection).document(id)
        docRef.update(updates).get()
        return findById(id)
    }

    /**
     * Busca próximo número de bilhete disponível para uma rifa Retorna números que ainda não foram
     * vendidos
     */
    fun getAvailableTicketNumbers(raffleId: String, totalTickets: Int, quantity: Int): List<Int> {
        val soldNumbers = findTicketNumbersByRaffleId(raffleId).toSet()
        val availableNumbers = (1..totalTickets).filter { it !in soldNumbers }
        return availableNumbers.take(quantity)
    }

    /** Verifica se tickets específicos estão disponíveis */
    fun areTicketsAvailable(raffleId: String, ticketNumbers: List<Int>): Boolean {
        val soldNumbers = findTicketNumbersByRaffleId(raffleId).toSet()
        return ticketNumbers.none { it in soldNumbers }
    }

    fun delete(id: String) {
        firestore.collection(collection).document(id).delete().get()
    }

    fun deleteByRaffleId(raffleId: String) {
        val tickets = findByRaffleId(raffleId)
        tickets.forEach { delete(it.id) }
    }
}
