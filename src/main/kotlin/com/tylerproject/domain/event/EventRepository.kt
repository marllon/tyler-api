package com.tylerproject.domain.event

import com.tylerproject.infrastructure.repository.PageRequest
import com.tylerproject.infrastructure.repository.PageResult

interface EventRepository {
    fun save(event: Event): Event
    fun findById(id: String): Event?
    fun update(id: String, event: Event): Event?
    fun deleteById(id: String): Boolean
    fun existsById(id: String): Boolean
    fun findAll(
        request: PageRequest,
        category: String? = null
    ): PageResult<Event>
    fun findByCategory(category: String, limit: Int = 20): List<Event>
    fun countByCategory(category: String?): Int
    fun countTotal(): Int
    fun searchByName(searchTerm: String, limit: Int = 20): List<Event>
}

enum class EventSortField(val fieldName: String) {
    CREATED_AT("createdAt"),
    UPDATED_AT("updatedAt"),
    NAME("name"),
}
