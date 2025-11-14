package com.tylerproject.domain.event

import com.tylerproject.infrastructure.repository.GenericFirestoreRepository
import com.tylerproject.infrastructure.repository.PageRequest
import com.tylerproject.infrastructure.repository.PageResult
import org.springframework.stereotype.Repository

@Repository
class FirestoreEventRepository :
        GenericFirestoreRepository<Event>("Events", Event::class.java), EventRepository {
    override fun extractId(entity: Event): String = entity.id

    override fun updateEntity(
        entity: Event,
        updates: Map<String, Any>
    ): Event {
        return entity.copy(
            id = updates["id"] as? String ?: entity.id,
            createdAt = updates["createdAt"] as? String ?: entity.createdAt,
            updatedAt = updates["updatedAt"] as? String ?: entity.updatedAt
        )
    }

    override fun update(
        id: String,
        event: Event
    ): Event? {
        val existing = findById(id) ?: return null
        val updated =
            updateEntity(
                event.copy(id = id, createdAt = existing.createdAt),
                mapOf("updatedAt" to currentTimestamp())
            )
        return save(updated)
    }

    override fun findAll(
        request: PageRequest,
        category: String?
    ): PageResult<Event> {
        val filters = buildMap { category?.let { put("category", it) } }
        return super.findAll(request, filters)
    }

    override fun findByCategory(category: String, limit: Int): List<Event> =
        findByField("category", category, limit)


    override fun countByCategory(category: String?): Int =
        if (category != null) countByField("category", category) else count()

    override fun countTotal(): Int = count()

    override fun searchByName(searchTerm: String, limit: Int): List<Event> =
        searchByField("name", searchTerm, limit)

    private fun currentTimestamp(): String {
        return java.time.LocalDateTime.now()
            .atOffset(java.time.ZoneOffset.UTC)
            .format(java.time.format.DateTimeFormatter.ISO_INSTANT)
    }

}


