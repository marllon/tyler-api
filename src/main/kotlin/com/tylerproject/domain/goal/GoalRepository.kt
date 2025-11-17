package com.tylerproject.domain.goal

import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Query
import com.google.cloud.firestore.QueryDocumentSnapshot
import org.springframework.stereotype.Repository

@Repository
class GoalRepository(private val firestore: Firestore) {
    private val collection = "goals"

    fun findAll(
            page: Int,
            pageSize: Int,
            status: GoalStatus? = null,
            activeOnly: Boolean = false,
            sortBy: String = "createdAt",
            sortDirection: String = "DESC"
    ): Pair<List<Goal>, Long> {
        var query: Query = firestore.collection(collection)

        if (activeOnly) {
            query = query.whereEqualTo("active", true)
        }

        if (status != null) {
            query = query.whereEqualTo("status", status.name)
        }

        val direction =
                if (sortDirection == "ASC") Query.Direction.ASCENDING
                else Query.Direction.DESCENDING
        query = query.orderBy(sortBy, direction)

        val countSnapshot = query.get().get()
        val totalElements = countSnapshot.size().toLong()

        val dataSnapshot = query.offset(page * pageSize).limit(pageSize).get().get()

        val goals =
                dataSnapshot.documents.mapNotNull { doc: QueryDocumentSnapshot ->
                    doc.toObject(Goal::class.java).copy(id = doc.id)
                }

        return Pair(goals, totalElements)
    }

    fun findById(id: String): Goal? {
        val docRef = firestore.collection(collection).document(id)
        val snapshot = docRef.get().get()
        return snapshot.toObject(Goal::class.java)?.copy(id = snapshot.id)
    }

    fun save(goal: Goal): Goal {
        val docRef =
                if (goal.id.isBlank()) {
                    firestore.collection(collection).document()
                } else {
                    firestore.collection(collection).document(goal.id)
                }

        val goalToSave = goal.copy(id = docRef.id)
        docRef.set(goalToSave).get()
        return goalToSave
    }

    fun update(id: String, updates: Map<String, Any?>): Goal? {
        val docRef = firestore.collection(collection).document(id)
        docRef.update(updates).get()
        return findById(id)
    }

    fun delete(id: String) {
        firestore.collection(collection).document(id).delete().get()
    }

    fun softDelete(id: String): Goal? {
        return update(id, mapOf("active" to false))
    }

    fun findByStatus(status: GoalStatus): List<Goal> {
        val snapshot =
                firestore.collection(collection).whereEqualTo("status", status.name).get().get()

        return snapshot.documents.mapNotNull { doc: QueryDocumentSnapshot ->
            doc.toObject(Goal::class.java).copy(id = doc.id)
        }
    }

    fun findByStatusAndActive(status: GoalStatus, active: Boolean): List<Goal> {
        val snapshot =
                firestore
                        .collection(collection)
                        .whereEqualTo("status", status.name)
                        .whereEqualTo("active", active)
                        .get()
                        .get()

        return snapshot.documents.mapNotNull { doc: QueryDocumentSnapshot ->
            doc.toObject(Goal::class.java).copy(id = doc.id)
        }
    }

    fun findByCreatedBy(uid: String): List<Goal> {
        val snapshot = firestore.collection(collection).whereEqualTo("createdBy", uid).get().get()

        return snapshot.documents.mapNotNull { doc: QueryDocumentSnapshot ->
            doc.toObject(Goal::class.java).copy(id = doc.id)
        }
    }

    fun findByEndDateBeforeAndStatus(endDate: String, status: GoalStatus): List<Goal> {
        val snapshot =
                firestore
                        .collection(collection)
                        .whereEqualTo("status", status.name)
                        .whereLessThan("endDate", endDate)
                        .get()
                        .get()

        return snapshot.documents.mapNotNull { doc: QueryDocumentSnapshot ->
            doc.toObject(Goal::class.java).copy(id = doc.id)
        }
    }

    fun findActiveGoals(): List<Goal> {
        val snapshot = firestore.collection(collection).whereEqualTo("active", true).get().get()

        return snapshot.documents.mapNotNull { doc: QueryDocumentSnapshot ->
            doc.toObject(Goal::class.java).copy(id = doc.id)
        }
    }
}
