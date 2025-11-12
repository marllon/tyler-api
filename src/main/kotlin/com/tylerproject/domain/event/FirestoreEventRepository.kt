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
        TODO("Not yet implemented")
    }

    override fun findAll(
        request: PageRequest,
        category: String?
    ): PageResult<Event> {
        val filters = buildMap { category?.let { put("category", it) } }
        return super.findAll(request, filters)
    }

    override fun findByCategory(
        category: String,
        limit: Int
    ): List<Event> {
        TODO("Not yet implemented")
    }

    override fun countByCategory(category: String?): Int {
        TODO("Not yet implemented")
    }

    override fun countTotal(): Int {
        TODO("Not yet implemented")
    }

    override fun searchByName(
        searchTerm: String,
        limit: Int
    ): List<Event> {
        TODO("Not yet implemented")
    }

}


