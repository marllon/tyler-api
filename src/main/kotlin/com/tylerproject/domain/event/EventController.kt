package com.tylerproject.domain.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.tylerproject.domain.product.ProductResponse
import com.tylerproject.domain.product.ProductWithImagesRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/Events")
@CrossOrigin(origins = ["*"])
@Tag(name = "Events", description = "API de gerenciamento de Eventos")
class EventController(private val eventService: EventService) {
    private val logger = LoggerFactory.getLogger(EventController::class.java)

    @GetMapping
    @Operation(
            summary = "Listar eventos",
            description = "Retorna lista paginada de eventos"
    )
    @ApiResponses(
        ApiResponse(
                responseCode = "200",
                description = "Lista de eventos retornada com sucesso"
        ),
        ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    )
    fun getAllEvents(
        @Parameter(description = "Número da página (inicia em 1)", example = "1")
        @RequestParam(defaultValue = "1")
        page: Int,
        @Parameter(description = "Quantidade de itens por página", example = "10")
        @RequestParam(defaultValue = "10")
        pageSize: Int,
        @Parameter(description = "Filtrar por categoria específica", example = "Aniversário")
        @RequestParam(required = false)
        category: String?
    ): ResponseEntity<EventListResponse> {
        return try {
            logger.info(
                    "Listing events - page: $page, pageSize: $pageSize, category: $category"
            )

            val response = eventService.getAllEvents(page, pageSize, category)
            ResponseEntity.ok(response)
        } catch (e: Exception){
            logger.error("Error listing event: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }

    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar evento por ID",
        description = "Retorna um evento específico pelo seu identificador único"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Evento encontrado"),
        ApiResponse(responseCode = "404", description = "Evento não encontrado"),
        ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    )
    fun getEventById(
        @Parameter(
            description = "ID único do evento",
            example = "550e8400-e29b-41d4-a716-446655440000"
        )
        @PathVariable
        id: String
    ): ResponseEntity<EventResponse> {
        return try {
            logger.info("Getting product by id: $id")

            val event = eventService.getEventById(id)

            if (event != null) {
                ResponseEntity.ok(event)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            logger.error("Error getting product $id: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PostMapping
    @Operation(
        summary = "Create new Event",
        description = "Creates a new event with the provided information"
    )
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "201",
                    description = "Event created successfully",
                    content =
                        [
                            Content(
                                mediaType = "application/json",
                                schema =
                                    Schema(
                                        implementation =
                                            EventResponse::class
                                    )
                            )]
                ),
                ApiResponse(responseCode = "400", description = "Invalid request data"),
                ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
                )]
    )
    fun createEvent(
        @RequestBody
        @Parameter(description = "Event creation data", required = true)
        request: CreateEventRequest
    ): ResponseEntity<EventResponse> {
        return try {
            logger.info("Creating event: ${request.name}")

            val event = eventService.createEvent(request)
            ResponseEntity.status(HttpStatus.CREATED).body(event)
        } catch (e: Exception) {
            logger.error("Error creating event: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update existing event",
        description = "Updates an existing event with the provided information"
    )
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Event updated successfully",
                    content =
                        [
                            Content(
                                mediaType = "application/json",
                                schema =
                                    Schema(
                                        implementation =
                                            EventResponse::class
                                    )
                            )]
                ),
                ApiResponse(responseCode = "404", description = "Event not found"),
                ApiResponse(responseCode = "400", description = "Invalid request data"),
                ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
                )]
    )
    fun updateEvent(
        @PathVariable @Parameter(description = "Event ID", required = true) id: String,
        @RequestBody
        @Parameter(description = "Event update data", required = true)
        request: UpdateEventRequest
    ): ResponseEntity<EventResponse> {
        return try {
            logger.info("Updating product: $id")

            val event = eventService.updateEvent(id, request)

            if (event != null) {
                ResponseEntity.ok(event)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            logger.error("Error updating event $id: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete event", description = "Deletes an existing event by its ID")
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Event deleted successfully",
                    content =
                        [
                            Content(
                                mediaType = "application/json",
                                schema =
                                    Schema(
                                        implementation =
                                            EventDeletedResponse::class
                                    )
                            )]
                ),
                ApiResponse(responseCode = "404", description = "Event not found"),
                ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
                )]
    )
    fun deleteEvent(
        @PathVariable
        @Parameter(description = "Event ID to delete", required = true)
        id: String
    ): ResponseEntity<EventDeletedResponse> {
        return try {
            logger.info("Deleting event: $id")

            val result = eventService.deleteEvent(id)

            if (result != null) {
                ResponseEntity.ok(result)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            logger.error("Error deleting event $id: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PostMapping("/with-images", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "Create event with images",
        description =
            "Creates a new Event with uploaded images. At least one image is required."
    )
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "201",
                    description = "Event created successfully with images",
                    content =
                        [
                            Content(
                                mediaType = "application/json",
                                schema =
                                    Schema(
                                        implementation =
                                            ProductResponse::class
                                    )
                            )]
                ),
                ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or image files"
                ),
                ApiResponse(responseCode = "413", description = "File size too large"),
                ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
                )]
    )
    fun createEventWithImages(
        @RequestParam("eventData")
        @Parameter(description = "Event data as JSON string", required = true)
       eventDataJson: String,
        @RequestParam("images")
        @Parameter(description = "Event images (max 10 files, 10MB each)", required = true)
        images: Array<MultipartFile>
    ): ResponseEntity<EventResponse> {
        return try {
            val objectMapper = ObjectMapper()
            val request =
                objectMapper.readValue(eventDataJson, EventWithImagesRequest::class.java)

            logger.info("Creating event with images: ${request.name} - ${images.size} images")

            val event = eventService.createEventWithImages(request, images)
            ResponseEntity.status(HttpStatus.CREATED).body(event)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request for event with images: ${e.message}", e)
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error creating event with images: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}

