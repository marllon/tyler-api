package com.tylerproject.domain.event

import com.tylerproject.infrastructure.repository.GenericFirestoreRepository

@Repository
class EventRepository: GenericFirestoreRepository<Event>("Events", Event::class.java){
    override fun extractId(entity: Event): String {
        return "Not yet implemented"
    }

    override fun updateEntity(
        entity: Event,
        updates: Map<String, Any>
    ): Event {
        return save(entity)
    }

}