package com.tylerproject.domain.order

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Endpoints para gestão de pedidos (e-commerce)")
class OrderController(private val orderService: OrderService) {

    private val logger = LoggerFactory.getLogger(OrderController::class.java)

    @PostMapping
    @Operation(
            summary = "Criar novo pedido",
            description =
                    "Cria um novo pedido com os produtos do carrinho. Requer autenticação Firebase."
    )
    @ApiResponses(
            value =
                    [
                            ApiResponse(
                                    responseCode = "201",
                                    description = "Pedido criado com sucesso",
                                    content =
                                            [
                                                    Content(
                                                            schema =
                                                                    Schema(
                                                                            implementation =
                                                                                    CreateOrderResponse::class
                                                                    )
                                                    )]
                            ),
                            ApiResponse(responseCode = "400", description = "Dados inválidos"),
                            ApiResponse(responseCode = "401", description = "Não autenticado"),
                            ApiResponse(
                                    responseCode = "500",
                                    description = "Erro interno do servidor"
                            )]
    )
    fun createOrder(
            @Parameter(description = "Token JWT do Firebase", required = true)
            @RequestHeader("Authorization")
            authToken: String,
            @Valid @RequestBody request: CreateOrderRequest
    ): ResponseEntity<Any> {
        return try {
            val user = validateFirebaseToken(authToken)

            val response =
                    orderService.createOrder(
                            userId = user.uid,
                            userEmail = user.email ?: "",
                            request = request
                    )

            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid order request: ${e.message}", e)
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Invalid request")))
        } catch (e: Exception) {
            logger.error("Error creating order: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(mapOf("error" to "Internal server error", "details" to e.message))
        }
    }

    @GetMapping
    @Operation(
            summary = "Listar pedidos do usuário",
            description = "Lista todos os pedidos do usuário autenticado com paginação e filtros"
    )
    @ApiResponses(
            value =
                    [
                            ApiResponse(
                                    responseCode = "200",
                                    description = "Lista de pedidos retornada com sucesso",
                                    content =
                                            [
                                                    Content(
                                                            schema =
                                                                    Schema(
                                                                            implementation =
                                                                                    ListOrdersResponse::class
                                                                    )
                                                    )]
                            ),
                            ApiResponse(responseCode = "401", description = "Não autenticado")]
    )
    fun listOrders(
            @Parameter(description = "Token JWT do Firebase", required = true)
            @RequestHeader("Authorization")
            authToken: String,
            @Parameter(description = "Filtrar por status")
            @RequestParam(required = false)
            status: OrderStatus?,
            @Parameter(description = "Limite de resultados")
            @RequestParam(required = false, defaultValue = "20")
            limit: Int,
            @Parameter(description = "Cursor para paginação")
            @RequestParam(required = false)
            cursor: String?
    ): ResponseEntity<ListOrdersResponse> {
        return try {
            val user = validateFirebaseToken(authToken)

            val response =
                    orderService.listUserOrders(
                            userId = user.uid,
                            status = status,
                            limit = limit,
                            cursor = cursor
                    )

            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Error listing orders: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/{orderId}")
    @Operation(
            summary = "Obter detalhes de um pedido",
            description = "Retorna os detalhes completos de um pedido específico"
    )
    @ApiResponses(
            value =
                    [
                            ApiResponse(
                                    responseCode = "200",
                                    description = "Detalhes do pedido retornados com sucesso",
                                    content =
                                            [
                                                    Content(
                                                            schema =
                                                                    Schema(
                                                                            implementation =
                                                                                    OrderDetailsResponse::class
                                                                    )
                                                    )]
                            ),
                            ApiResponse(
                                    responseCode = "404",
                                    description = "Pedido não encontrado"
                            ),
                            ApiResponse(responseCode = "401", description = "Não autenticado")]
    )
    fun getOrderDetails(
            @Parameter(description = "Token JWT do Firebase", required = true)
            @RequestHeader("Authorization")
            authToken: String,
            @Parameter(description = "ID do pedido") @PathVariable orderId: String
    ): ResponseEntity<OrderDetailsResponse> {
        return try {
            val user = validateFirebaseToken(authToken)

            val response = orderService.getOrderDetails(userId = user.uid, orderId = orderId)

            if (response != null) {
                ResponseEntity.ok(response)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            logger.error("Error getting order details: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(
            summary = "Cancelar pedido",
            description =
                    "Cancela um pedido. Apenas pedidos em PENDING ou CONFIRMED podem ser cancelados."
    )
    @ApiResponses(
            value =
                    [
                            ApiResponse(
                                    responseCode = "200",
                                    description = "Pedido cancelado com sucesso",
                                    content =
                                            [
                                                    Content(
                                                            schema =
                                                                    Schema(
                                                                            implementation =
                                                                                    CancelOrderResponse::class
                                                                    )
                                                    )]
                            ),
                            ApiResponse(
                                    responseCode = "400",
                                    description = "Pedido não pode ser cancelado"
                            ),
                            ApiResponse(
                                    responseCode = "404",
                                    description = "Pedido não encontrado"
                            ),
                            ApiResponse(responseCode = "401", description = "Não autenticado")]
    )
    fun cancelOrder(
            @Parameter(description = "Token JWT do Firebase", required = true)
            @RequestHeader("Authorization")
            authToken: String,
            @Parameter(description = "ID do pedido") @PathVariable orderId: String,
            @RequestBody(required = false) request: CancelOrderRequest?
    ): ResponseEntity<CancelOrderResponse> {
        return try {
            val user = validateFirebaseToken(authToken)

            val response =
                    orderService.cancelOrder(
                            userId = user.uid,
                            orderId = orderId,
                            reason = request?.reason
                    )

            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid cancel request: ${e.message}")
            ResponseEntity.badRequest().build()
        } catch (e: IllegalStateException) {
            logger.error("Cannot cancel order: ${e.message}")
            ResponseEntity.badRequest().build()
        } catch (e: Exception) {
            logger.error("Error canceling order: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    private fun validateFirebaseToken(authHeader: String): FirebaseToken {
        if (!authHeader.startsWith("Bearer ")) {
            throw IllegalArgumentException("Invalid authorization header format")
        }

        val token = authHeader.substring(7)

        return try {
            FirebaseAuth.getInstance().verifyIdToken(token)
        } catch (e: Exception) {
            logger.error("Invalid Firebase token: ${e.message}")
            throw IllegalArgumentException("Invalid or expired token")
        }
    }
}
