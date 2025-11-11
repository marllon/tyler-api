package com.tylerproject.infrastructure.repository

import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Query
import com.google.firebase.cloud.FirestoreClient
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

abstract class GenericFirestoreRepository<T : Any>(
        private val collectionName: String,
        private val entityClass: Class<T>
) {

    protected val firestore: Firestore by lazy { FirestoreClient.getFirestore() }

    fun save(entity: T): T {
        val id = extractId(entity)
        val entityToSave =
                if (id.isEmpty()) {
                    updateEntity(
                            entity,
                            mapOf(
                                    "id" to UUID.randomUUID().toString(),
                                    "createdAt" to currentTimestamp()
                            )
                    )
                } else {
                    updateEntity(entity, mapOf("updatedAt" to currentTimestamp()))
                }

        val finalId = extractId(entityToSave)
        firestore.collection(collectionName).document(finalId).set(entityToSave).get()
        return entityToSave
    }

    fun findById(id: String): T? {
        val document = firestore.collection(collectionName).document(id).get().get()
        return if (document.exists()) document.toObject(entityClass) else null
    }

    fun deleteById(id: String): Boolean {
        return try {
            firestore.collection(collectionName).document(id).delete().get()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun existsById(id: String): Boolean {
        return firestore.collection(collectionName).document(id).get().get().exists()
    }

    fun findAll(request: PageRequest, filters: Map<String, Any> = emptyMap()): PageResult<T> {
        var query: Query = firestore.collection(collectionName)

        filters.forEach { (field, value) -> query = query.whereEqualTo(field, value) }

        val firestoreDirection =
                when (request.sortDirection) {
                    SortDirection.ASC -> Query.Direction.ASCENDING
                    SortDirection.DESC -> Query.Direction.DESCENDING
                }
        query = query.orderBy(request.sortBy, firestoreDirection)

        request.cursor?.let { cursor ->
            when (request.direction) {
                PageDirection.NEXT -> {
                    val cursorDoc =
                            firestore.collection(collectionName).document(cursor).get().get()
                    if (cursorDoc.exists()) {
                        query = query.startAfter(cursorDoc)
                    }
                }
                PageDirection.PREVIOUS -> {
                    val cursorDoc =
                            firestore.collection(collectionName).document(cursor).get().get()
                    if (cursorDoc.exists()) {
                        query = query.endBefore(cursorDoc)
                    }
                }
            }
        }

        val documents = query.limit(request.limit + 1).get().get()
        val allEntities =
                documents.documents.mapNotNull { doc ->
                    if (doc.exists()) doc.toObject(entityClass) else null
                }

        val hasNext = allEntities.size > request.limit
        val entities = if (hasNext) allEntities.take(request.limit) else allEntities

        val nextCursor = if (hasNext && entities.isNotEmpty()) extractId(entities.last()) else null
        val previousCursor =
                if (entities.isNotEmpty() && request.cursor != null) extractId(entities.first())
                else null

        return PageResult(
                items = entities,
                pageSize = request.limit,
                hasNext = hasNext,
                nextCursor = nextCursor,
                hasPrevious = request.cursor != null,
                previousCursor = previousCursor
        )
    }

    fun findByField(fieldName: String, value: Any, limit: Int = 20): List<T> {
        val documents =
                firestore
                        .collection(collectionName)
                        .whereEqualTo(fieldName, value)
                        .limit(limit)
                        .get()
                        .get()

        return documents.documents.mapNotNull { doc ->
            if (doc.exists()) doc.toObject(entityClass) else null
        }
    }

    fun findByFieldRange(fieldName: String, minValue: Any, maxValue: Any): List<T> {
        val documents =
                firestore
                        .collection(collectionName)
                        .whereGreaterThanOrEqualTo(fieldName, minValue)
                        .whereLessThanOrEqualTo(fieldName, maxValue)
                        .orderBy(fieldName)
                        .get()
                        .get()

        return documents.documents.mapNotNull { doc ->
            if (doc.exists()) doc.toObject(entityClass) else null
        }
    }

    fun searchByField(fieldName: String, searchTerm: String, limit: Int = 20): List<T> {
        val documents =
                firestore
                        .collection(collectionName)
                        .whereGreaterThanOrEqualTo(fieldName, searchTerm)
                        .whereLessThan(fieldName, searchTerm + "\uf8ff")
                        .limit(limit)
                        .get()
                        .get()

        return documents.documents.mapNotNull { doc ->
            if (doc.exists()) doc.toObject(entityClass) else null
        }
    }

    fun count(): Int {
        return firestore.collection(collectionName).get().get().documents.size
    }

    fun countByField(fieldName: String, value: Any): Int {
        return firestore
                .collection(collectionName)
                .whereEqualTo(fieldName, value)
                .get()
                .get()
                .documents
                .size
    }

    protected abstract fun extractId(entity: T): String
    protected abstract fun updateEntity(entity: T, updates: Map<String, Any>): T

    private fun currentTimestamp(): String {
        return LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
    }
}

data class PageRequest(
        val limit: Int = 20,
        val cursor: String? = null,
        val direction: PageDirection = PageDirection.NEXT,
        val sortBy: String = "createdAt",
        val sortDirection: SortDirection = SortDirection.DESC
)

data class PageResult<T>(
        val items: List<T>,
        val pageSize: Int,
        val hasNext: Boolean,
        val nextCursor: String? = null,
        val hasPrevious: Boolean = false,
        val previousCursor: String? = null
)

enum class PageDirection {
    NEXT,
    PREVIOUS
}

enum class SortDirection {
    ASC,
    DESC
}
