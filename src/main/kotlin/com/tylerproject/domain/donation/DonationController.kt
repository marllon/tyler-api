package com.tylerproject.domain.donation

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
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/donations")
@CrossOrigin(origins = ["*"])
@Tag(
        name = "💰 Donations",
        description =
                "API de Gerenciamento de Doações - vincula pagamentos do PagBank com Metas/Rifas/Pedidos"
)
class DonationController(private val donationService: DonationService) {
    private val logger = LoggerFactory.getLogger(DonationController::class.java)

    @PostMapping
    @Operation(
            summary = "Criar nova doação",
            description =
                    "Cria uma nova doação vinculada a uma meta, rifa ou pedido. Retorna os dados da doação criada."
    )
    @ApiResponses(
            value =
                    [
                            ApiResponse(
                                    responseCode = "201",
                                    description = "Doação criada com sucesso"
                            ),
                            ApiResponse(responseCode = "400", description = "Dados inválidos"),
                            ApiResponse(
                                    responseCode = "404",
                                    description = "Meta/Rifa/Pedido não encontrado"
                            ),
                            ApiResponse(
                                    responseCode = "500",
                                    description = "Erro interno do servidor"
                            )]
    )
    fun createDonation(
            @Valid @RequestBody request: CreateDonationRequest
    ): ResponseEntity<DonationResponse> {
        return try {
            logger.info(
                    "Creating donation - type: ${request.donationType}, targetId: ${request.targetId}"
            )
            val donation = donationService.createDonation(request)
            ResponseEntity.status(HttpStatus.CREATED).body(donation)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid data creating donation: ${e.message}")
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error creating donation: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PostMapping("/{id}/create-pix-charge")
    @Operation(
            summary = "Gerar QR Code PIX para doação",
            description =
                    "Cria uma cobrança PIX no PagBank e retorna o QR Code para pagamento. Só funciona para doações com método PIX e status PENDING."
    )
    @ApiResponses(
            value =
                    [
                            ApiResponse(
                                    responseCode = "200",
                                    description = "QR Code gerado com sucesso"
                            ),
                            ApiResponse(
                                    responseCode = "400",
                                    description =
                                            "Doação não está em status válido ou método não é PIX"
                            ),
                            ApiResponse(
                                    responseCode = "404",
                                    description = "Doação não encontrada"
                            ),
                            ApiResponse(
                                    responseCode = "500",
                                    description = "Erro ao gerar QR Code no PagBank"
                            )]
    )
    fun createPixCharge(
            @Parameter(description = "ID único da doação") @PathVariable id: String
    ): ResponseEntity<CreatePixChargeResponse> {
        return try {
            logger.info("Creating PIX charge for donation: $id")
            val response = donationService.createPixCharge(id)
            if (response != null) {
                ResponseEntity.ok(response)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid request for PIX charge: ${e.message}")
            ResponseEntity.badRequest().build()
        } catch (e: IllegalStateException) {
            logger.error("Invalid state for PIX charge: ${e.message}")
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error creating PIX charge: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping
    @Operation(
            summary = "Listar todas as doações",
            description =
                    "Lista todas as doações com paginação e filtros opcionais por status e tipo"
    )
    @ApiResponses(
            value =
                    [
                            ApiResponse(
                                    responseCode = "200",
                                    description = "Lista de doações com paginação",
                                    content =
                                            [
                                                    Content(
                                                            mediaType = "application/json",
                                                            schema =
                                                                    Schema(
                                                                            implementation =
                                                                                    DonationPageResponse::class
                                                                    )
                                                    )]
                            ),
                            ApiResponse(
                                    responseCode = "500",
                                    description = "Erro interno do servidor"
                            )]
    )
    fun listDonations(
            @Parameter(description = "Número da página (começa em 0)", example = "0")
            @RequestParam(defaultValue = "0")
            page: Int,
            @Parameter(description = "Tamanho da página", example = "20")
            @RequestParam(defaultValue = "20")
            pageSize: Int,
            @Parameter(
                    description = "Filtrar por status (PENDING, PAID, CANCELLED, REFUNDED, FAILED)"
            )
            @RequestParam(required = false)
            status: DonationStatus?,
            @Parameter(description = "Filtrar por tipo (GOAL, RAFFLE, ORDER)")
            @RequestParam(required = false)
            donationType: DonationType?,
            @Parameter(description = "Campo de ordenação", example = "createdAt")
            @RequestParam(defaultValue = "createdAt")
            sortBy: String,
            @Parameter(description = "Direção da ordenação (ASC ou DESC)", example = "DESC")
            @RequestParam(defaultValue = "DESC")
            sortDirection: String
    ): ResponseEntity<DonationPageResponse> {
        return try {
            logger.info(
                    "Listing donations - page: $page, pageSize: $pageSize, status: $status, type: $donationType"
            )
            val response =
                    donationService.listDonations(
                            page,
                            pageSize,
                            status,
                            donationType,
                            sortBy,
                            sortDirection
                    )
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Error listing donations: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar doação por ID",
            description = "Retorna uma doação específica pelo seu identificador único"
    )
    @ApiResponses(
            value =
                    [
                            ApiResponse(responseCode = "200", description = "Doação encontrada"),
                            ApiResponse(
                                    responseCode = "404",
                                    description = "Doação não encontrada"
                            ),
                            ApiResponse(
                                    responseCode = "500",
                                    description = "Erro interno do servidor"
                            )]
    )
    fun getDonationById(
            @Parameter(description = "ID único da doação") @PathVariable id: String
    ): ResponseEntity<DonationResponse> {
        return try {
            logger.info("Getting donation by id: $id")
            val donation = donationService.getDonationById(id)
            if (donation != null) {
                ResponseEntity.ok(donation)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            logger.error("Error getting donation $id: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/target/{targetId}")
    @Operation(
            summary = "Listar doações por destino",
            description = "Lista todas as doações vinculadas a uma meta, rifa ou pedido específico"
    )
    @ApiResponses(
            value =
                    [
                            ApiResponse(responseCode = "200", description = "Lista de doações"),
                            ApiResponse(
                                    responseCode = "500",
                                    description = "Erro interno do servidor"
                            )]
    )
    fun getDonationsByTargetId(
            @Parameter(description = "ID do destino (meta/rifa/pedido)")
            @PathVariable
            targetId: String
    ): ResponseEntity<List<DonationResponse>> {
        return try {
            logger.info("Getting donations by targetId: $targetId")
            val donations = donationService.getDonationsByTargetId(targetId)
            ResponseEntity.ok(donations)
        } catch (e: Exception) {
            logger.error("Error getting donations by targetId $targetId: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PostMapping("/{id}/process")
    @Operation(
            summary = "Processar doação paga manualmente",
            description =
                    "Força o processamento de uma doação paga (adiciona valor à meta/rifa/pedido). Uso administrativo.",
            security = [SecurityRequirement(name = "firebase-auth")]
    )
    @ApiResponses(
            value =
                    [
                            ApiResponse(
                                    responseCode = "200",
                                    description = "Doação processada com sucesso"
                            ),
                            ApiResponse(
                                    responseCode = "400",
                                    description = "Doação não está paga ou já foi processada"
                            ),
                            ApiResponse(
                                    responseCode = "404",
                                    description = "Doação não encontrada"
                            ),
                            ApiResponse(
                                    responseCode = "500",
                                    description = "Erro ao processar doação"
                            )]
    )
    fun processDonation(
            @Parameter(description = "ID único da doação") @PathVariable id: String
    ): ResponseEntity<ProcessDonationResponse> {
        return try {
            logger.info("Manually processing donation: $id")
            val processed = donationService.processPaidDonation(id)

            val response =
                    ProcessDonationResponse(
                            success = processed,
                            message =
                                    if (processed) {
                                        "Donation processed successfully"
                                    } else {
                                        "Donation could not be processed (already processed or not paid)"
                                    },
                            donationId = id
                    )

            if (processed) {
                ResponseEntity.ok(response)
            } else {
                ResponseEntity.badRequest().body(response)
            }
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid donation for processing: ${e.message}")
            ResponseEntity.badRequest()
                    .body(
                            ProcessDonationResponse(
                                    success = false,
                                    message = e.message ?: "Invalid request",
                                    donationId = id
                            )
                    )
        } catch (e: Exception) {
            logger.error("Error processing donation $id: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            ProcessDonationResponse(
                                    success = false,
                                    message = "Internal error: ${e.message}",
                                    donationId = id
                            )
                    )
        }
    }
}
