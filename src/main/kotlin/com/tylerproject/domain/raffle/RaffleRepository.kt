package com.tylerproject.domain.raffle

import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Query
import com.google.cloud.firestore.QueryDocumentSnapshot
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import org.springframework.stereotype.Repository

@Repository
class RaffleRepository(private val firestore: Firestore) {
    private val collection = "raffles"

    fun save(raffle: Raffle): Raffle {
        val docRef =
                if (raffle.id.isBlank()) {
                    firestore.collection(collection).document()
                } else {
                    firestore.collection(collection).document(raffle.id)
                }

        val raffleToSave = raffle.copy(id = docRef.id)
        docRef.set(raffleToSave).get()
        return raffleToSave
    }

    fun findById(id: String): Raffle? {
        val docRef = firestore.collection(collection).document(id)
        val snapshot = docRef.get().get()
        return snapshot.toObject(Raffle::class.java)?.copy(id = snapshot.id)
    }

    fun findAll(
            page: Int,
            pageSize: Int,
            status: RaffleStatus? = null,
            activeOnly: Boolean = false,
            sortBy: String = "createdAt",
            sortDirection: String = "DESC"
    ): Pair<List<Raffle>, Long> {
        var query: Query = firestore.collection(collection)

        if (activeOnly) {
            query = query.whereEqualTo("active", true)
        }

        status?.let { query = query.whereEqualTo("status", it.name) }

        val direction =
                if (sortDirection == "ASC") Query.Direction.ASCENDING
                else Query.Direction.DESCENDING
        query = query.orderBy(sortBy, direction)

        val countSnapshot = query.get().get()
        val totalElements = countSnapshot.size().toLong()

        val dataSnapshot = query.offset(page * pageSize).limit(pageSize).get().get()

        val raffles =
                dataSnapshot.documents.mapNotNull { doc: QueryDocumentSnapshot ->
                    doc.toObject(Raffle::class.java).copy(id = doc.id)
                }

        return Pair(raffles, totalElements)
    }

    fun update(id: String, updates: Map<String, Any?>): Raffle? {
        val docRef = firestore.collection(collection).document(id)
        docRef.update(updates).get()
        return findById(id)
    }

    fun delete(id: String) {
        firestore.collection(collection).document(id).delete().get()
    }

    fun softDelete(id: String): Raffle? {
        return update(id, mapOf("active" to false))
    }

    fun findByStatus(status: RaffleStatus): List<Raffle> {
        val snapshot =
                firestore.collection(collection).whereEqualTo("status", status.name).get().get()

        return snapshot.documents.mapNotNull { doc: QueryDocumentSnapshot ->
            doc.toObject(Raffle::class.java).copy(id = doc.id)
        }
    }

    fun findActiveRaffles(): List<Raffle> {
        val snapshot = firestore.collection(collection).whereEqualTo("active", true).get().get()

        return snapshot.documents.mapNotNull { doc: QueryDocumentSnapshot ->
            doc.toObject(Raffle::class.java).copy(id = doc.id)
        }
    }

    /**
     * Busca rifas expiradas que ainda estão ativas Para job agendado atualizar status
     * automaticamente
     */
    fun findExpiredRaffles(): List<Raffle> {
        val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)

        val snapshot =
                firestore
                        .collection(collection)
                        .whereEqualTo("status", RaffleStatus.ACTIVE.name)
                        .whereLessThan("deadline", now)
                        .get()
                        .get()

        return snapshot.documents.mapNotNull { doc: QueryDocumentSnapshot ->
            doc.toObject(Raffle::class.java).copy(id = doc.id)
        }
    }

    fun findByGoalId(goalId: String): List<Raffle> {
        val snapshot = firestore.collection(collection).whereEqualTo("goalId", goalId).get().get()

        return snapshot.documents.mapNotNull { doc: QueryDocumentSnapshot ->
            doc.toObject(Raffle::class.java).copy(id = doc.id)
        }
    }

    fun search(
            searchTerm: String,
            status: RaffleStatus?,
            activeOnly: Boolean,
            page: Int,
            pageSize: Int
    ): Pair<List<Raffle>, Long> {
        // Firestore não tem full-text search nativo
        // Buscar todos e filtrar em memória (para datasets pequenos)
        // Para produção com muitos dados, usar Algolia ou Elasticsearch

        val allRaffles = findAll(0, 10000, status, activeOnly, "createdAt", "DESC").first

        val filtered =
                allRaffles.filter { raffle ->
                    val term = searchTerm.lowercase()
                    raffle.title.lowercase().contains(term) ||
                            raffle.description.lowercase().contains(term) ||
                            raffle.prize.lowercase().contains(term)
                }

        val totalElements = filtered.size.toLong()
        val paginatedResults = filtered.drop(page * pageSize).take(pageSize)

        return Pair(paginatedResults, totalElements)
    }
}
