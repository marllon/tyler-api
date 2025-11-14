package com.tylerproject.domain.event

import com.tylerproject.infrastructure.repository.PageDirection
import com.tylerproject.infrastructure.repository.PageRequest
import com.tylerproject.infrastructure.repository.SortDirection
import com.tylerproject.service.ImageUploadService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.collections.map

@Service
class EventService(
    private val eventRepository: EventRepository,
    private val imageUploadService: ImageUploadService
) {

    private val logger = LoggerFactory.getLogger(EventService::class.java)

    fun getEventById(id: String): EventResponse? {
        val event = eventRepository.findById(id)
        return event?.toResponse()
    }

    fun getAllEvents(
        page: Int = 1,
        pageSize: Int = 20,
        category: String? = null
    ): EventListResponse {
        val request =
            PageRequest(
                limit = pageSize,
                cursor = null,
                sortBy = EventSortField.CREATED_AT.fieldName,
                sortDirection = SortDirection.DESC
            )

        val eventPage = eventRepository.findAll(request, category)

        return EventListResponse(
            events = eventPage.items.map { it.toResponse() },
            totalPages = -1,
            currentPage = page,
            totalEvents = -1L,
            pageSize = eventPage.pageSize
        )
    }
    private fun Event.toResponse(): EventResponse {
        return EventResponse(
            id = id,
            name = name,
            description = description,
            images = images.map(imageUploadService::toImageUploadResponseEvent),
            primaryImageUrl = primaryImageUrl,
            category = category,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun createEvent(request: CreateEventRequest, createdBy: String? = null): EventResponse {
        val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
        val id = UUID.randomUUID().toString()

        val event =
            Event(
                id = id,
                name = request.name,
                description = request.description,
                images = emptyList(),
                category = request.category,
                createdAt = now,
                updatedAt = now,
                createdBy = createdBy
            )

        val savedEvent = eventRepository.save(event)
        logger.info("Event created: ${savedEvent.name} (${savedEvent.id})")

        return savedEvent.toResponse()
    }

    fun updateEvent(id: String, request: UpdateEventRequest): EventResponse? {
        val existing = eventRepository.findById(id) ?: return null
        val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)

        val updated =
            existing.copy(
                name = request.name ?: existing.name,
                description = request.description ?: existing.description,
                category = request.category ?: existing.category,
                tags = request.tags ?: existing.tags,
                updatedAt = now
            )

        val savedEvent = eventRepository.update(id, updated)
        logger.info("Event updated: ${updated.name} (${updated.id})")

        return savedEvent?.toResponse()
    }

    fun deleteEvent(id: String): EventDeletedResponse? {
        if (!eventRepository.existsById(id)) {
            return null
        }

        val deleted = eventRepository.deleteById(id)

        return if (deleted) {
            logger.info("Event deleted: $id")
            EventDeletedResponse(
                message = "Evento removido com sucesso",
                deletedEventId = id,
                timestamp =
                    LocalDateTime.now()
                        .atOffset(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_INSTANT)
            )
        } else {
            null
        }
    }

    fun createEventWithImages(
        request: EventWithImagesRequest,
        images: Array<MultipartFile>,
        createdBy: String? = null
    ): EventResponse {
        val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
        val id = UUID.randomUUID().toString()

        // Upload das imagens primeiro
        val uploadedImages =
            imageUploadService.uploadEventImages(
                eventId = id,
                files = images,
                primaryImageIndex = request.primaryImageIndex
            )

        val event =
            Event(
                id = id,
                name = request.name,
                description = request.description,
                images = uploadedImages,
                tags = request.tags,
                createdAt = now,
                updatedAt = now,
                createdBy = createdBy
            )

        val savedEvent = eventRepository.save(event)
        logger.info(
            "Event with images created: ${savedEvent.name} (${savedEvent.id}) - ${uploadedImages.size} images"
        )

        return savedEvent.toResponse()
    }

    fun getEventsPaginated(
        limit: Int = 20,
        cursor: String? = null,
        direction: PageDirection = PageDirection.NEXT,
        sortBy: EventSortField = EventSortField.CREATED_AT,
        sortDirection: SortDirection = SortDirection.DESC,
        category: String? = null
    ): EventPageResponse {
        val request = PageRequest(limit, cursor, direction, sortBy.fieldName, sortDirection)
        val eventPage = eventRepository.findAll(request, category)

        return EventPageResponse(
            events = eventPage.items.map { it.toResponse() },
            pageSize = eventPage.pageSize,
            hasNext = eventPage.hasNext,
            nextCursor = eventPage.nextCursor,
            hasPrevious = eventPage.hasPrevious,
            previousCursor = eventPage.previousCursor
        )
    }

}
