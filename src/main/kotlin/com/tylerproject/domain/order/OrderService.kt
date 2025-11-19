package com.tylerproject.domain.order

import com.tylerproject.domain.donation.DonationRepository
import com.tylerproject.domain.donation.DonationStatus
import com.tylerproject.domain.donation.DonationType
import com.tylerproject.domain.donation.PaymentMethod
import com.tylerproject.domain.product.ProductRepository
import com.tylerproject.providers.PagBankProvider
import com.tylerproject.utils.QrCodeGenerator
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

interface OrderService {
    fun createOrder(userId: String, userEmail: String, request: CreateOrderRequest): CreateOrderResponse
    fun getOrderById(userId: String, orderId: String): OrderResponse?
    fun listUserOrders(userId: String, status: OrderStatus?, limit: Int?, cursor: String?): ListOrdersResponse
    fun cancelOrder(userId: String, orderId: String, reason: String?): CancelOrderResponse
    fun getOrderDetails(userId: String, orderId: String): OrderDetailsResponse?
    fun processOrderPayment(orderId: String): Boolean
}

@Service
class OrderServiceImpl(
        private val orderRepository: OrderRepository,
        private val productRepository: ProductRepository,
        private val donationRepository: DonationRepository,
        private val pagBankProvider: PagBankProvider
) : OrderService {

    private val logger = LoggerFactory.getLogger(OrderServiceImpl::class.java)

    override fun createOrder(
            userId: String,
            userEmail: String,
            request: CreateOrderRequest
    ): CreateOrderResponse = runBlocking {
        logger.info("Creating order for user $userId with ${request.items.size} items")

        // 1. Validar e buscar produtos
        val productItems = mutableListOf<OrderItem>()
        var subtotal = 0.0

        for (item in request.items) {
            val product = productRepository.findById(item.productId)
                    ?: throw IllegalArgumentException("Produto não encontrado: ${item.productId}")

            if (!product.active) {
                throw IllegalArgumentException("Produto inativo: ${product.name}")
            }

            val productStock = product.stock ?: 0
            if (productStock < item.quantity) {
                throw IllegalArgumentException(
                        "Estoque insuficiente para ${product.name}. Disponível: $productStock, Solicitado: ${item.quantity}"
                )
            }

            val itemSubtotal = product.price * item.quantity
            subtotal += itemSubtotal

            productItems.add(
                    OrderItem(
                            productId = product.id,
                            productName = product.name,
                            quantity = item.quantity,
                            unitPrice = product.price,
                            subtotal = itemSubtotal,
                            imageUrl = product.images.firstOrNull()?.url
                    )
            )
        }

        // 2. Calcular frete
        val shippingCost = ShippingMethod.getShippingCost(request.shippingMethod)
        val total = subtotal + shippingCost

        // 3. Gerar número único do pedido
        val orderNumber = generateOrderNumber()

        // 4. Criar pedido
        val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)

        val order =
                Order(
                        id = "",
                        orderNumber = orderNumber,
                        userId = userId,
                        userEmail = userEmail,
                        status = OrderStatus.PENDING,
                        items = productItems,
                        subtotal = subtotal,
                        shippingCost = shippingCost,
                        total = total,
                        paymentMethod = request.paymentMethod,
                        paymentStatus = DonationStatus.PENDING,
                        shippingMethod = request.shippingMethod,
                        shippingAddress = request.shippingAddress.toShippingAddress(),
                        notes = request.notes,
                        createdAt = now,
                        updatedAt = now
                )

        val savedOrder = orderRepository.save(order)
        logger.info("Order created: ${savedOrder.orderNumber} (ID: ${savedOrder.id})")

        // 5. Criar pagamento se for PIX
        val paymentDetails = if (request.paymentMethod == PaymentMethod.PIX) {
            createPixPayment(savedOrder)
        } else {
            null
        }

        CreateOrderResponse(order = OrderResponse.fromEntity(savedOrder), paymentDetails = paymentDetails)
    }

    private suspend fun createPixPayment(order: Order): PaymentDetailsResponse? {
        return try {
            logger.info("Creating PIX payment for order ${order.orderNumber}")

            val amountInCents = (order.total * 100).toLong()

            // Criar request para PagBank
            val pagBankRequest = mapOf(
                    "amount" to amountInCents,
                    "description" to "Pedido ${order.orderNumber}",
                    "reference_id" to order.orderNumber, // Usar orderNumber como reference_id
                    "payer" to mapOf(
                            "name" to order.shippingAddress.name,
                            "email" to order.userEmail,
                            "document" to "12345678909" // CPF padrão para teste
                    ),
                    "notification_urls" to listOf("${getWebhookUrl()}/api/webhooks/pagbank")
            )

            val response = pagBankProvider.createPixTransaction(pagBankRequest)

            val chargeId = response["transaction_id"] as? String
                    ?: throw IllegalStateException("PagBank não retornou transaction_id")

            val qrCodeText = response["pix_code"] as? String ?: ""
            val expiresAt = response["expires_at"] as? String ?: ""

            // Gerar QR Code localmente
            val qrCodeImage = if (qrCodeText.isNotEmpty()) {
                QrCodeGenerator.generateQrCodeBase64(qrCodeText, 300)
            } else {
                ""
            }

            // Atualizar pedido com informações de pagamento
            orderRepository.update(
                    order.id,
                    mapOf(
                            "paymentId" to chargeId,
                            "updatedAt" to LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
                    )
            )

            logger.info("PIX payment created for order ${order.orderNumber}, paymentId: $chargeId")

            PaymentDetailsResponse(
                    qrCode = qrCodeText,
                    qrCodeImage = qrCodeImage,
                    paymentId = chargeId,
                    expiresAt = expiresAt
            )
        } catch (e: Exception) {
            logger.error("Error creating PIX payment for order ${order.orderNumber}: ${e.message}", e)
            null
        }
    }

    override fun getOrderById(userId: String, orderId: String): OrderResponse? {
        val order = orderRepository.findById(orderId) ?: return null

        // Verificar se o pedido pertence ao usuário
        if (order.userId != userId) {
            logger.warn("User $userId attempted to access order $orderId belonging to ${order.userId}")
            return null
        }

        return OrderResponse.fromEntity(order)
    }

    override fun listUserOrders(
            userId: String,
            status: OrderStatus?,
            limit: Int?,
            cursor: String?
    ): ListOrdersResponse {
        val orders = if (status != null) {
            val list = orderRepository.findByUserIdAndStatus(userId, status, limit)
            Pair(list, null)
        } else {
            orderRepository.findByUserId(userId, limit, cursor)
        }

        return ListOrdersResponse(
                orders = orders.first.map { OrderResponse.fromEntity(it) },
                hasNext = orders.second != null,
                nextCursor = orders.second
        )
    }

    override fun cancelOrder(userId: String, orderId: String, reason: String?): CancelOrderResponse {
        val order = orderRepository.findById(orderId)
                ?: throw IllegalArgumentException("Pedido não encontrado: $orderId")

        if (order.userId != userId) {
            throw IllegalArgumentException("Pedido não pertence ao usuário")
        }

        if (!order.canBeCancelled()) {
            throw IllegalStateException(
                    "Pedido não pode ser cancelado no status atual: ${order.status}"
            )
        }

        val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)

        val updates = mutableMapOf<String, Any>(
                "status" to OrderStatus.CANCELLED.name,
                "cancelledAt" to now,
                "updatedAt" to now
        )

        if (reason != null) {
            updates["cancelReason"] = reason
        }

        // Se já foi pago, marcar para reembolso
        if (order.isPaid()) {
            updates["paymentStatus"] = DonationStatus.REFUNDED.name
            logger.warn("Order ${order.orderNumber} was paid, requires manual refund processing")
        }

        orderRepository.update(orderId, updates)

        val updatedOrder = orderRepository.findById(orderId)!!
        logger.info("Order ${order.orderNumber} cancelled by user $userId")

        return CancelOrderResponse(success = true, order = OrderResponse.fromEntity(updatedOrder))
    }

    override fun getOrderDetails(userId: String, orderId: String): OrderDetailsResponse? {
        val order = orderRepository.findById(orderId) ?: return null

        if (order.userId != userId) {
            return null
        }

        val paymentDetails = if (order.paymentId != null) {
            PaymentDetailsResponse(paymentId = order.paymentId)
        } else {
            null
        }

        val tracking = if (order.trackingCode != null) {
            TrackingResponse(
                    code = order.trackingCode,
                    carrier = order.carrier,
                    estimatedDelivery = null,
                    events = order.trackingEvents.map { TrackingEventResponse.fromEntity(it) }
            )
        } else {
            null
        }

        return OrderDetailsResponse(
                order = OrderResponse.fromEntity(order),
                paymentDetails = paymentDetails,
                tracking = tracking
        )
    }

    override fun processOrderPayment(orderId: String): Boolean = runBlocking {
        logger.info("Processing payment for order: $orderId")

        try {
            val order = orderRepository.findById(orderId)
                    ?: throw IllegalArgumentException("Order not found: $orderId")

            if (order.status != OrderStatus.PENDING) {
                logger.warn("Order ${order.orderNumber} is not in PENDING status: ${order.status}")
                return@runBlocking false
            }

            // Verificar estoque disponível
            for (item in order.items) {
                val product = productRepository.findById(item.productId)
                val productStock = product?.stock ?: 0
                if (product == null || productStock < item.quantity) {
                    logger.error("Insufficient stock for product ${item.productId} in order ${order.orderNumber}")
                    
                    // Cancelar pedido e marcar para reembolso
                    orderRepository.update(
                            orderId,
                            mapOf(
                                    "status" to OrderStatus.CANCELLED.name,
                                    "paymentStatus" to DonationStatus.REFUNDED.name,
                                    "cancelReason" to "Estoque insuficiente",
                                    "cancelledAt" to LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT),
                                    "updatedAt" to LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
                            )
                    )
                    return@runBlocking false
                }
            }

            // Decrementar estoque
            for (item in order.items) {
                productRepository.decrementStock(item.productId, item.quantity)
                logger.info("Decremented stock for product ${item.productId}: -${item.quantity}")
            }

            // Atualizar status do pedido
            val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
            orderRepository.update(
                    orderId,
                    mapOf(
                            "status" to OrderStatus.CONFIRMED.name,
                            "paymentStatus" to DonationStatus.PAID.name,
                            "paidAt" to now,
                            "updatedAt" to now
                    )
            )

            logger.info("Order ${order.orderNumber} payment processed successfully")
            true
        } catch (e: Exception) {
            logger.error("Error processing payment for order $orderId: ${e.message}", e)
            false
        }
    }

    private fun generateOrderNumber(): String {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd")
        val dateStr = now.format(dateFormat)

        // Gerar número sequencial baseado em timestamp
        val sequence = (now.atZone(java.time.ZoneId.systemDefault()).toEpochSecond() % 10000).toString().padStart(4, '0')

        return "ORD-$dateStr-$sequence"
    }

    private fun getWebhookUrl(): String {
        return System.getenv("WEBHOOK_BASE_URL") ?: "https://tyler-api-production.com"
    }
}
