package com.tylerproject.domain.goal

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/goals")
@CrossOrigin(origins = ["*"])
@Tag(
    name = "🎯 Goals",
    description = "API de Gerenciamento de Metas de Arrecadação com upload de imagens, paginação e filtros"
)
class GoalController(
    private val goalService: GoalService
) {
    private val logger = LoggerFactory.getLogger(GoalController::class.java)

    @GetMapping
    @Operation(
        summary = "Listar todas as metas",
        description = "Lista todas as metas com paginação e filtros opcionais por status e visibilidade"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Lista de metas com paginação",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = GoalPageResponse::class)
                )]
            ),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    fun listGoals(
        @Parameter(description = "Número da página (começa em 0)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,

        @Parameter(description = "Tamanho da página", example = "20")
        @RequestParam(defaultValue = "20") pageSize: Int,

        @Parameter(description = "Filtrar por status (ACTIVE, PAUSED, COMPLETED, CANCELLED)")
        @RequestParam(required = false) status: GoalStatus?,

        @Parameter(description = "Exibir apenas metas ativas", example = "false")
        @RequestParam(defaultValue = "false") activeOnly: Boolean,

        @Parameter(description = "Campo de ordenação", example = "createdAt")
        @RequestParam(defaultValue = "createdAt") sortBy: String,

        @Parameter(description = "Direção da ordenação (ASC ou DESC)", example = "DESC")
        @RequestParam(defaultValue = "DESC") sortDirection: String
    ): ResponseEntity<GoalPageResponse> {
        return try {
            logger.info("Listing goals - page: $page, pageSize: $pageSize, status: $status, activeOnly: $activeOnly")
            val response = goalService.listGoals(page, pageSize, status, activeOnly, sortBy, sortDirection)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Error listing goals: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar meta por ID",
        description = "Retorna uma meta específica pelo seu identificador único"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Meta encontrada"),
            ApiResponse(responseCode = "404", description = "Meta não encontrada"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    fun getGoalById(
        @Parameter(description = "ID único da meta")
        @PathVariable id: String
    ): ResponseEntity<GoalResponse> {
        return try {
            logger.info("Getting goal by id: $id")
            val goal = goalService.getById(id)
            if (goal != null) {
                ResponseEntity.ok(goal)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            logger.error("Error getting goal $id: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PostMapping
    @Operation(
        summary = "Criar nova meta",
        description = "Cria uma nova meta de arrecadação. Requer autenticação de administrador.",
        security = [SecurityRequirement(name = "firebase-auth")]
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Meta criada com sucesso"),
            ApiResponse(responseCode = "400", description = "Dados inválidos"),
            ApiResponse(responseCode = "401", description = "Não autenticado"),
            ApiResponse(responseCode = "403", description = "Sem permissão de administrador"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    fun createGoal(
        @Valid @RequestBody request: CreateGoalRequest,
        @RequestHeader(value = "Authorization", required = false) authToken: String?
    ): ResponseEntity<GoalResponse> {
        return try {
            logger.info("Creating goal: ${request.title}")
            val createdBy = authToken?.let { extractUidFromToken(it) }
            val goal = goalService.create(request, createdBy)
            ResponseEntity.status(HttpStatus.CREATED).body(goal)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid data creating goal: ${e.message}")
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error creating goal: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar meta existente",
        description = "Atualiza uma meta existente. Apenas campos não nulos são atualizados. Requer autenticação.",
        security = [SecurityRequirement(name = "firebase-auth")]
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Meta atualizada com sucesso"),
            ApiResponse(responseCode = "400", description = "Dados inválidos"),
            ApiResponse(responseCode = "401", description = "Não autenticado"),
            ApiResponse(responseCode = "404", description = "Meta não encontrada"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    fun updateGoal(
        @Parameter(description = "ID único da meta")
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateGoalRequest,
        @RequestHeader(value = "Authorization", required = false) authToken: String?
    ): ResponseEntity<GoalResponse> {
        return try {
            logger.info("Updating goal: $id")
            val goal = goalService.update(id, request)
            if (goal != null) {
                ResponseEntity.ok(goal)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid data updating goal: ${e.message}")
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error updating goal $id: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PatchMapping("/{id}/add-amount")
    @Operation(
        summary = "Adicionar valor à meta",
        description = "Incrementa o valor arrecadado da meta. Usado ao processar doações. Atualiza status para COMPLETED se atingir o valor alvo."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Valor adicionado com sucesso"),
            ApiResponse(responseCode = "400", description = "Valor inválido"),
            ApiResponse(responseCode = "404", description = "Meta não encontrada"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    fun addAmountToGoal(
        @Parameter(description = "ID único da meta")
        @PathVariable id: String,
        
        @Parameter(description = "Valor a adicionar (deve ser maior que zero)", example = "150.00")
        @RequestParam amount: Double
    ): ResponseEntity<GoalResponse> {
        return try {
            logger.info("Adding amount $amount to goal $id")
            val goal = goalService.addAmount(id, amount)
            if (goal != null) {
                ResponseEntity.ok(goal)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid amount for goal $id: ${e.message}")
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error adding amount to goal $id: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Excluir meta (soft delete)",
        description = "Remove uma meta (soft delete - marca como inativa). Requer autenticação.",
        security = [SecurityRequirement(name = "firebase-auth")]
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Meta excluída com sucesso"),
            ApiResponse(responseCode = "401", description = "Não autenticado"),
            ApiResponse(responseCode = "404", description = "Meta não encontrada"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    fun deleteGoal(
        @Parameter(description = "ID único da meta")
        @PathVariable id: String,
        @RequestHeader(value = "Authorization", required = false) authToken: String?
    ): ResponseEntity<Void> {
        return try {
            logger.info("Deleting goal: $id")
            val deleted = goalService.delete(id)
            if (deleted) {
                ResponseEntity.noContent().build()
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            logger.error("Error deleting goal $id: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PostMapping("/{id}/upload-image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "Upload de imagem da meta",
        description = "Faz upload de uma imagem para a meta no Firebase Storage. Requer autenticação.",
        security = [SecurityRequirement(name = "firebase-auth")]
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Imagem enviada com sucesso"),
            ApiResponse(responseCode = "400", description = "Arquivo inválido"),
            ApiResponse(responseCode = "401", description = "Não autenticado"),
            ApiResponse(responseCode = "404", description = "Meta não encontrada"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    fun uploadGoalImage(
        @Parameter(description = "ID único da meta")
        @PathVariable id: String,
        
        @Parameter(description = "Arquivo de imagem (JPG, PNG, WebP - máx 5MB)")
        @RequestParam("file") file: MultipartFile,
        
        @RequestHeader(value = "Authorization", required = false) authToken: String?
    ): ResponseEntity<ImageUploadResponse> {
        return try {
            logger.info("Uploading image for goal: $id")
            
            if (file.isEmpty) {
                return ResponseEntity.badRequest().build()
            }
            
            val imageUrl = goalService.uploadImage(id, file)
            if (imageUrl != null) {
                ResponseEntity.ok(ImageUploadResponse(imageUrl))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid file for goal $id: ${e.message}")
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error uploading image for goal $id: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @DeleteMapping("/{id}/image")
    @Operation(
        summary = "Remover imagem da meta",
        description = "Remove a imagem da meta do Firebase Storage. Requer autenticação.",
        security = [SecurityRequirement(name = "firebase-auth")]
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Imagem removida com sucesso"),
            ApiResponse(responseCode = "401", description = "Não autenticado"),
            ApiResponse(responseCode = "404", description = "Meta não encontrada ou sem imagem"),
            ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        ]
    )
    fun deleteGoalImage(
        @Parameter(description = "ID único da meta")
        @PathVariable id: String,
        @RequestHeader(value = "Authorization", required = false) authToken: String?
    ): ResponseEntity<Void> {
        return try {
            logger.info("Deleting image for goal: $id")
            val deleted = goalService.deleteImage(id)
            if (deleted) {
                ResponseEntity.noContent().build()
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalArgumentException) {
            logger.error("Error deleting image for goal $id: ${e.message}")
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Error deleting image for goal $id: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    private fun extractUidFromToken(authToken: String): String? {
        return try {
            null
        } catch (e: Exception) {
            logger.error("Error extracting UID from token: ${e.message}")
            null
        }
    }
}
