package com.tylerproject.domain.donation

import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Query
import com.google.cloud.firestore.QueryDocumentSnapshot
import org.springframework.stereotype.Repository

@Repository
class DonationRepository(
    private val firestore: Firestore
) {
    private val collection = "donations"

    fun save(donation: Donation): Donation {
        val docRef = if (donation.id.isBlank()) {
            firestore.collection(collection).document()
        } else {
            firestore.collection(collection).document(donation.id)
        }

        val donationToSave = donation.copy(id = docRef.id)
        docRef.set(donationToSave).get()
        return donationToSave
    }

    fun findById(id: String): Donation? {
        val docRef = firestore.collection(collection).document(id)
        val snapshot = docRef.get().get()
        return snapshot.toObject(Donation::class.java)?.copy(id = snapshot.id)
    }

    fun findByPagbankChargeId(chargeId: String): Donation? {
        val snapshot = firestore.collection(collection)
            .whereEqualTo("pagbankChargeId", chargeId)
            .limit(1)
            .get()
            .get()

        return snapshot.documents.firstOrNull()?.let { doc: QueryDocumentSnapshot ->
            doc.toObject(Donation::class.java).copy(id = doc.id)
        }
    }

    fun findByPagbankTransactionId(transactionId: String): Donation? {
        val snapshot = firestore.collection(collection)
            .whereEqualTo("pagbankTransactionId", transactionId)
            .limit(1)
            .get()
            .get()

        return snapshot.documents.firstOrNull()?.let { doc: QueryDocumentSnapshot ->
            doc.toObject(Donation::class.java).copy(id = doc.id)
        }
    }

    fun findByTargetId(targetId: String, donationType: DonationType? = null): List<Donation> {
        var query: Query = firestore.collection(collection)
            .whereEqualTo("targetId", targetId)

        donationType?.let {
            query = query.whereEqualTo("donationType", it.name)
        }

        val snapshot = query.get().get()
        return snapshot.documents.mapNotNull { doc: QueryDocumentSnapshot ->
            doc.toObject(Donation::class.java).copy(id = doc.id)
        }
    }

    fun findAll(
        page: Int,
        pageSize: Int,
        status: DonationStatus? = null,
        donationType: DonationType? = null,
        sortBy: String = "createdAt",
        sortDirection: String = "DESC"
    ): Pair<List<Donation>, Long> {
        var query: Query = firestore.collection(collection)

        status?.let {
            query = query.whereEqualTo("status", it.name)
        }

        donationType?.let {
            query = query.whereEqualTo("donationType", it.name)
        }

        val direction = if (sortDirection == "ASC") Query.Direction.ASCENDING else Query.Direction.DESCENDING
        query = query.orderBy(sortBy, direction)

        val countSnapshot = query.get().get()
        val totalElements = countSnapshot.size().toLong()

        val dataSnapshot = query
            .offset(page * pageSize)
            .limit(pageSize)
            .get()
            .get()

        val donations = dataSnapshot.documents.mapNotNull { doc: QueryDocumentSnapshot ->
            doc.toObject(Donation::class.java).copy(id = doc.id)
        }

        return Pair(donations, totalElements)
    }

    fun update(id: String, updates: Map<String, Any?>): Donation? {
        val docRef = firestore.collection(collection).document(id)
        docRef.update(updates).get()
        return findById(id)
    }

    fun findPendingDonations(): List<Donation> {
        val snapshot = firestore.collection(collection)
            .whereEqualTo("status", DonationStatus.PENDING.name)
            .get()
            .get()

        return snapshot.documents.mapNotNull { doc: QueryDocumentSnapshot ->
            doc.toObject(Donation::class.java).copy(id = doc.id)
        }
    }

    fun findUnprocessedPaidDonations(): List<Donation> {
        val snapshot = firestore.collection(collection)
            .whereEqualTo("status", DonationStatus.PAID.name)
            .whereEqualTo("processedAt", null)
            .get()
            .get()

        return snapshot.documents.mapNotNull { doc: QueryDocumentSnapshot ->
            doc.toObject(Donation::class.java).copy(id = doc.id)
        }
    }

    fun countByTargetId(targetId: String, status: DonationStatus? = null): Long {
        var query: Query = firestore.collection(collection)
            .whereEqualTo("targetId", targetId)

        status?.let {
            query = query.whereEqualTo("status", it.name)
        }

        val snapshot = query.get().get()
        return snapshot.size().toLong()
    }

    fun sumAmountByTargetId(targetId: String, status: DonationStatus): Double {
        val donations = findByTargetId(targetId)
        return donations
            .filter { it.status == status }
            .sumOf { it.amount }
    }
}
