package com.tylerproject.domain.raffle

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/raffles")
@CrossOrigin(origins = ["*"])
@Tag(
        name = "🎟️ Raffles",
        description =
                "API de Gerenciamento de Rifas Beneficentes com sorteio verificável, múltiplas imagens e sistema transparente"
)
class RaffleController(private val raffleService: RaffleService) {
    private val logger = LoggerFactory.getLogger(RaffleController::class.java)

    // ==================== ENDPOINTS PÚBLICOS ====================

    @GetMapping
    @Operation(
            summary = "Listar todas as rifas",
            description = "Lista rifas com paginação, filtros e busca"
    )
    @ApiResponses(
            value =
                    [
                            ApiResponse(responseCode = "200", description = "Lista de rifas"),
                            ApiResponse(responseCode = "500", description = "Erro interno")]
    )
    fun getRaffles(
            @Parameter(description = "Número da página", example = "0")
            @RequestParam(defaultValue = "0")
            page: Int,
            @Parameter(description = "Tamanho da página", example = "20")
            @RequestParam(defaultValue = "20")
            pageSize: Int,
            @Parameter(description = "Filtrar por status (ACTIVE, ENDED, DRAWN, CANCELLED)")
            @RequestParam(required = false)
            status: RaffleStatus?,
            @Parameter(description = "Exibir apenas rifas ativas", example = "false")
            @RequestParam(defaultValue = "false")
            activeOnly: Boolean,
            @Parameter(description = "Campo de ordenação", example = "createdAt")
            @RequestParam(defaultValue = "createdAt")
            sortBy: String,
            @Parameter(description = "Direção (ASC/DESC)", example = "DESC")
            @RequestParam(defaultValue = "DESC")
            sortDirection: String,
            @Parameter(description = "Termo de busca (título, descrição, prêmio)")
            @RequestParam(required = false)
            searchTerm: String?
    ): ResponseEntity<RafflePageResponse> {
        return try {
            logger.info("Listing raffles - page: $page, status: $status")
            val response =
                    raffleService.listRaffles(
                            page,
                            pageSize,
                            status,
                            activeOnly,
                            sortBy,
                            sortDirection,
                            searchTerm
                    )
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Error listing raffles: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar rifa por ID",
            description = "Retorna uma rifa específica com todos os detalhes"
    )
    @ApiResponses(
            value =
                    [
                            ApiResponse(responseCode = "200", description = "Rifa encontrada"),
                            ApiResponse(responseCode = "404", description = "Rifa não encontrada")]
    )
    fun getRaffleById(
            @Parameter(description = "ID da rifa") @PathVariable id: String
    ): ResponseEntity<RaffleResponse> {
        return try {
            logger.info("Getting raffle by id: $id")
            val raffle = raffleService.getRaffleById(id)
            if (raffle != null) {
                ResponseEntity.ok(raffle)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            logger.error("Error getting raffle: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/{id}/tickets")
    @Operation(
            summary = "Listar bilhetes de uma rifa",
            description = "Retorna todos os bilhetes vendidos (informação pública)"
    )
    fun getRaffleTickets(@PathVariable id: String): ResponseEntity<List<RaffleTicketResponse>> {
        return try {
            val tickets = raffleService.getRaffleTickets(id)
            ResponseEntity.ok(tickets)
        } catch (e: Exception) {
            logger.error("Error getting raffle tickets: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/{id}/tickets/available")
    @Operation(
            summary = "Listar bilhetes disponíveis",
            description = "Retorna números de bilhetes ainda não vendidos"
    )
    fun getAvailableTickets(@PathVariable id: String): ResponseEntity<List<Int>> {
        return try {
            val available = raffleService.getAvailableTickets(id)
            ResponseEntity.ok(available)
        } catch (e: Exception) {
            logger.error("Error getting available tickets: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PostMapping("/{id}/purchase")
    @Operation(summary = "Comprar bilhetes", description = "Compra bilhetes e gera pagamento PIX")
    @ApiResponses(
            value =
                    [
                            ApiResponse(
                                    responseCode = "200",
                                    description = "Compra criada com sucesso"
                            ),
                            ApiResponse(
                                    responseCode = "400",
                                    description = "Dados inválidos ou bilhetes insuficientes"
                            ),
                            ApiResponse(responseCode = "404", description = "Rifa não encontrada")]
    )
    fun purchaseTickets(
            @PathVariable id: String,
            @Valid @RequestBody request: TicketPurchaseRequest
    ): ResponseEntity<TicketPurchaseResponse> {
        return try {
            logger.info("Purchase request for raffle $id: ${request.quantity} tickets")
            val response = raffleService.purchaseTickets(id, request)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            logger.warn("Purchase failed - Invalid argument: ${e.message}")
            ResponseEntity.badRequest().build()
        } catch (e: IllegalStateException) {
            logger.warn("Purchase failed - Invalid state: ${e.message}")
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error purchasing tickets: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/{id}/verify-draw")
    @Operation(
            summary = "Verificar sorteio",
            description = "Endpoint público para auditoria do sorteio"
    )
    fun verifyDraw(@PathVariable id: String): ResponseEntity<DrawVerificationResponse> {
        return try {
            val verification = raffleService.verifyDraw(id)
            ResponseEntity.ok(verification)
        } catch (e: IllegalStateException) {
            logger.warn("Verification failed: ${e.message}")
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error verifying draw: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    // ==================== ENDPOINTS ADMIN (Protegidos) ====================

    @PostMapping
    @Operation(
            summary = "[ADMIN] Criar rifa",
            description = "Cria nova rifa com entropy comprometida para sorteio verificável"
    )
    fun createRaffle(
            @Valid @RequestBody request: CreateRaffleRequest,
            @RequestHeader("Authorization", required = false) authToken: String?
    ): ResponseEntity<RaffleResponse> {
        return try {
            // TODO: Extrair UID do Firebase token
            val createdByUid = "admin-uid" // Placeholder

            logger.info("Creating raffle: ${request.title}")
            val raffle = raffleService.createRaffle(request, createdByUid)
            ResponseEntity.status(HttpStatus.CREATED).body(raffle)
        } catch (e: Exception) {
            logger.error("Error creating raffle: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "[ADMIN] Atualizar rifa", description = "Atualiza dados da rifa (parcial)")
    fun updateRaffle(
            @PathVariable id: String,
            @Valid @RequestBody request: UpdateRaffleRequest
    ): ResponseEntity<RaffleResponse> {
        return try {
            val updated = raffleService.updateRaffle(id, request)
            if (updated != null) {
                ResponseEntity.ok(updated)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalArgumentException) {
            logger.warn("Update failed: ${e.message}")
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error updating raffle: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "[ADMIN] Deletar rifa",
            description = "Soft delete (apenas se sem bilhetes vendidos)"
    )
    fun deleteRaffle(@PathVariable id: String): ResponseEntity<Void> {
        return try {
            val deleted = raffleService.deleteRaffle(id)
            if (deleted) {
                ResponseEntity.noContent().build()
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalStateException) {
            logger.warn("Delete failed: ${e.message}")
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        } catch (e: Exception) {
            logger.error("Error deleting raffle: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PostMapping("/{id}/upload-images")
    @Operation(
            summary = "[ADMIN] Upload de imagens",
            description = "Adiciona até 10 imagens à rifa"
    )
    fun uploadImages(
            @PathVariable id: String,
            @RequestParam("files") files: Array<MultipartFile>
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val imageUrls = raffleService.uploadImages(id, files)
            ResponseEntity.ok(mapOf("imageUrls" to imageUrls, "totalImages" to imageUrls.size))
        } catch (e: IllegalArgumentException) {
            logger.warn("Upload failed: ${e.message}")
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error uploading images: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @DeleteMapping("/{id}/images/{imageIndex}")
    @Operation(
            summary = "[ADMIN] Deletar imagem",
            description = "Remove imagem específica pelo índice"
    )
    fun deleteImage(@PathVariable id: String, @PathVariable imageIndex: Int): ResponseEntity<Void> {
        return try {
            raffleService.deleteImage(id, imageIndex)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            logger.warn("Delete image failed: ${e.message}")
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error deleting image: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PostMapping("/{id}/draw")
    @Operation(
            summary = "[ADMIN] Sortear rifa",
            description = "Realiza sorteio verificável usando commit-reveal scheme"
    )
    fun drawRaffle(
            @PathVariable id: String,
            @Valid @RequestBody request: RaffleDrawRequest,
            @RequestHeader("Authorization", required = false) authToken: String?
    ): ResponseEntity<RaffleDrawResponse> {
        return try {
            val adminUid = "admin-uid" // TODO: Extrair do token
            val result = raffleService.drawRaffle(id, request, adminUid)
            ResponseEntity.ok(result)
        } catch (e: IllegalStateException) {
            logger.warn("Draw failed: ${e.message}")
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error drawing raffle: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PostMapping("/{id}/cancel")
    @Operation(
            summary = "[ADMIN] Cancelar rifa",
            description = "Cancela rifa e inicia processo de reembolso"
    )
    fun cancelRaffle(
            @PathVariable id: String,
            @RequestHeader("Authorization", required = false) authToken: String?
    ): ResponseEntity<Void> {
        return try {
            val adminUid = "admin-uid" // TODO: Extrair do token
            raffleService.cancelRaffle(id, adminUid)
            ResponseEntity.noContent().build()
        } catch (e: IllegalStateException) {
            logger.warn("Cancel failed: ${e.message}")
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error cancelling raffle: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}
