package com.tylerproject.domain.event

import kotlinx.serialization.Serializable

@Serializable
data class CreateEventRequest(
    val name: String,
    val description: String,
    val category: String,
    val tags: List<String>? = null
)

@Serializable
data class EventImageUploadResponse(
    val id: String,
    val url: String,
    val filename: String,
    val contentType: String,
    val size: Long,
    val isPrimary: Boolean
)

@Serializable
data class EventWithImagesRequest(
    val name: String,
    val description: String,
    val tags: List<String>? = null,
    val primaryImageIndex: Int = 0 // Índice da imagem principal nas imagens enviadas
)

@Serializable
data class UpdateEventRequest(
    val name: String? = null,
    val description: String? = null,
    val category: String? = null,
    val tags: List<String>? = null
)

@Serializable
data class EventResponse(
    val id: String,
    val name: String,
    val description: String,
    val category: String? = null,
    val images: List<EventImageUploadResponse> = emptyList(),
    val primaryImageUrl: String? = null,
    val tags: List<String>? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
@Deprecated("Use EventPageResponse with cursor-based pagination")
data class EventListResponse(
    val events: List<EventResponse>,
    val totalEvents: Long, // ❌ Custoso no NoSQL
    val currentPage: Int, // ❌ Conceito inválido com cursors
    val totalPages: Int, // ❌ Custoso no NoSQL
    val pageSize: Int
)

@Serializable
data class EventPageResponse(
    val events: List<EventResponse>,
    val pageSize: Int,
    val hasNext: Boolean,
    val nextCursor: String? = null,
    val hasPrevious: Boolean = false,
    val previousCursor: String? = null,
    val isEmpty: Boolean = events.isEmpty(),
    val count: Int = events.size
)

@Serializable
data class EventDeletedResponse(
    val message: String,
    val deletedEventId: String,
    val timestamp: String
)

