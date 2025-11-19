package com.tylerproject.domain.transaction

/**
 * DTOs para desserialização Firestore
 * 
 * Firestore requer:
 * - Propriedades mutáveis (var)
 * - Construtor sem argumentos
 * - Não funciona com data classes Kotlin imutáveis
 */

data class CustomerInfoDto(
    var userId: String = "",
    var name: String = "",
    var email: String = "",
    var phone: String? = null,
    var document: String? = null,
    var isAnonymous: Boolean = false
) {
    fun toDomain(): CustomerInfo {
        return CustomerInfo(
            userId = userId,
            name = name,
            email = email,
            phone = phone,
            document = document,
            isAnonymous = isAnonymous
        )
    }

    companion object {
        fun fromDomain(customer: CustomerInfo): CustomerInfoDto {
            return CustomerInfoDto(
                userId = customer.userId,
                name = customer.name,
                email = customer.email,
                phone = customer.phone,
                document = customer.document,
                isAnonymous = customer.isAnonymous
            )
        }
    }
}

data class PaymentInfoDto(
    var method: String = "",
    var paymentId: String = "",
    var qrCodeText: String? = null,
    var qrCodeImageBase64: String? = null,
    var expiresAt: String? = null,
    var paidAt: String? = null,
    var installments: Int? = null,
    var authorizationCode: String? = null,
    var nsu: String? = null,
    var tid: String? = null
) {
    fun toDomain(): PaymentInfo {
        return PaymentInfo(
            method = TransactionPaymentMethod.valueOf(method),
            paymentId = paymentId,
            qrCodeText = qrCodeText,
            qrCodeImageBase64 = qrCodeImageBase64,
            expiresAt = expiresAt,
            paidAt = paidAt,
            installments = installments,
            authorizationCode = authorizationCode,
            nsu = nsu,
            tid = tid
        )
    }

    companion object {
        fun fromDomain(payment: PaymentInfo): PaymentInfoDto {
            return PaymentInfoDto(
                method = payment.method.name,
                paymentId = payment.paymentId,
                qrCodeText = payment.qrCodeText,
                qrCodeImageBase64 = payment.qrCodeImageBase64,
                expiresAt = payment.expiresAt,
                paidAt = payment.paidAt,
                installments = payment.installments,
                authorizationCode = payment.authorizationCode,
                nsu = payment.nsu,
                tid = payment.tid
            )
        }
    }
}

data class TransactionFirestoreDto(
    var id: String = "",
    var type: String = "",
    var status: String = "",
    var amount: Double = 0.0,
    var currency: String = "BRL",
    var customer: CustomerInfoDto? = null,
    var payment: PaymentInfoDto? = null,
    var simpleDonationDetails: Map<String, Any>? = null,
    var goalContributionDetails: Map<String, Any>? = null,
    var rafflePurchaseDetails: Map<String, Any>? = null,
    var productOrderDetails: Map<String, Any>? = null,
    var targetId: String? = null,
    var targetType: String? = null,
    var notes: String? = null,
    var webhookData: String? = null,
    var createdAt: String = "",
    var updatedAt: String = "",
    var paidAt: String? = null,
    var completedAt: String? = null,
    var cancelledAt: String? = null,
    var refundedAt: String? = null,
    var cancelReason: String? = null,
    var refundReason: String? = null
) {
    fun toDomain(): Transaction {
        return Transaction(
            id = id,
            type = TransactionType.valueOf(type),
            status = TransactionStatus.valueOf(status),
            amount = amount,
            currency = currency,
            customer = customer?.toDomain() ?: CustomerInfo("", "", ""),
            payment = payment?.toDomain(),
            simpleDonationDetails = simpleDonationDetails?.let { mapToSimpleDonationDetails(it) },
            goalContributionDetails = goalContributionDetails?.let { mapToGoalContributionDetails(it) },
            rafflePurchaseDetails = rafflePurchaseDetails?.let { mapToRafflePurchaseDetails(it) },
            productOrderDetails = productOrderDetails?.let { mapToProductOrderDetails(it) },
            targetId = targetId,
            targetType = targetType,
            notes = notes,
            webhookData = webhookData,
            createdAt = createdAt,
            updatedAt = updatedAt,
            paidAt = paidAt,
            completedAt = completedAt,
            cancelledAt = cancelledAt,
            refundedAt = refundedAt,
            cancelReason = cancelReason,
            refundReason = refundReason
        )
    }

    companion object {
        fun fromDomain(transaction: Transaction): TransactionFirestoreDto {
            return TransactionFirestoreDto(
                id = transaction.id,
                type = transaction.type.name,
                status = transaction.status.name,
                amount = transaction.amount,
                currency = transaction.currency,
                customer = transaction.customer.let { CustomerInfoDto.fromDomain(it) },
                payment = transaction.payment?.let { PaymentInfoDto.fromDomain(it) },
                simpleDonationDetails = transaction.simpleDonationDetails?.let { simpleDonationDetailsToMap(it) as Map<String, Any>? },
                goalContributionDetails = transaction.goalContributionDetails?.let { goalContributionDetailsToMap(it) as Map<String, Any>? },
                rafflePurchaseDetails = transaction.rafflePurchaseDetails?.let { rafflePurchaseDetailsToMap(it) as Map<String, Any>? },
                productOrderDetails = transaction.productOrderDetails?.let { productOrderDetailsToMap(it) as Map<String, Any>? },
                targetId = transaction.targetId,
                targetType = transaction.targetType,
                notes = transaction.notes,
                webhookData = transaction.webhookData,
                createdAt = transaction.createdAt,
                updatedAt = transaction.updatedAt,
                paidAt = transaction.paidAt,
                completedAt = transaction.completedAt,
                cancelledAt = transaction.cancelledAt,
                refundedAt = transaction.refundedAt,
                cancelReason = transaction.cancelReason,
                refundReason = transaction.refundReason
            )
        }

        // Helper methods para converter details (usando Map temporariamente por simplicidade)
        private fun mapToSimpleDonationDetails(map: Map<String, Any>): SimpleDonationDetails {
            return SimpleDonationDetails(
                message = map["message"] as? String,
                isAnonymous = (map["isAnonymous"] as? Boolean) ?: false
            )
        }

        private fun mapToGoalContributionDetails(map: Map<String, Any>): GoalContributionDetails {
            return GoalContributionDetails(
                goalId = map["goalId"] as String,
                goalTitle = map["goalTitle"] as String,
                message = map["message"] as? String,
                isAnonymous = (map["isAnonymous"] as? Boolean) ?: false
            )
        }

        private fun mapToRafflePurchaseDetails(map: Map<String, Any>): RafflePurchaseDetails {
            @Suppress("UNCHECKED_CAST")
            val ticketNumbersData = map["ticketNumbers"] as? List<Number> ?: emptyList()
            return RafflePurchaseDetails(
                raffleId = map["raffleId"] as String,
                raffleTitle = map["raffleTitle"] as String,
                rafflePrize = map["rafflePrize"] as String,
                ticketPrice = (map["ticketPrice"] as Number).toDouble(),
                ticketNumbers = ticketNumbersData.map { it.toInt() },
                ticketCount = (map["ticketCount"] as? Number)?.toInt() ?: ticketNumbersData.size,
                isAnonymous = (map["isAnonymous"] as? Boolean) ?: false
            )
        }

        private fun mapToProductOrderDetails(map: Map<String, Any>): ProductOrderDetails {
            @Suppress("UNCHECKED_CAST")
            val itemsData = map["items"] as? List<Map<String, Any>> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val trackingEventsData = map["trackingEvents"] as? List<Map<String, Any>> ?: emptyList()
            val shippingAddressData = map["shippingAddress"] as? Map<String, Any>

            return ProductOrderDetails(
                orderNumber = map["orderNumber"] as String,
                items = itemsData.map { item ->
                    OrderItem(
                        productId = item["productId"] as String,
                        productName = item["productName"] as String,
                        productImage = item["productImage"] as? String,
                        quantity = (item["quantity"] as Number).toInt(),
                        unitPrice = (item["unitPrice"] as Number).toDouble(),
                        subtotal = (item["subtotal"] as Number).toDouble()
                    )
                },
                subtotal = (map["subtotal"] as Number).toDouble(),
                shippingCost = (map["shippingCost"] as Number).toDouble(),
                shippingMethod = ShippingMethod.valueOf(map["shippingMethod"] as String),
                shippingAddress = shippingAddressData?.let {
                    ShippingAddress(
                        recipientName = it["recipientName"] as? String ?: "",
                        recipientPhone = it["recipientPhone"] as? String ?: "",
                        street = it["street"] as? String ?: "",
                        number = it["number"] as? String ?: "",
                        complement = it["complement"] as? String,
                        neighborhood = it["neighborhood"] as? String ?: "",
                        city = it["city"] as? String ?: "",
                        state = it["state"] as? String ?: "",
                        zipCode = it["zipCode"] as? String ?: ""
                    )
                } ?: ShippingAddress("", "", "", "", null, "", "", "", ""),
                trackingCode = map["trackingCode"] as? String,
                carrier = map["carrier"] as? String,
                trackingEvents = trackingEventsData.map { event ->
                    TrackingEvent(
                        timestamp = event["timestamp"] as? String ?: "",
                        status = event["status"] as? String ?: "",
                        description = event["description"] as? String ?: "",
                        location = event["location"] as? String
                    )
                },
                notes = map["notes"] as? String,
                shippedAt = map["shippedAt"] as? String,
                deliveredAt = map["deliveredAt"] as? String,
                estimatedDelivery = map["estimatedDelivery"] as? String
            )
        }

        private fun simpleDonationDetailsToMap(details: SimpleDonationDetails): Map<String, Any?> {
            return mapOf(
                "message" to details.message,
                "isAnonymous" to details.isAnonymous
            )
        }

        private fun goalContributionDetailsToMap(details: GoalContributionDetails): Map<String, Any?> {
            return mapOf(
                "goalId" to details.goalId,
                "goalTitle" to details.goalTitle,
                "message" to details.message,
                "isAnonymous" to details.isAnonymous
            )
        }

        private fun rafflePurchaseDetailsToMap(details: RafflePurchaseDetails): Map<String, Any?> {
            return mapOf(
                "raffleId" to details.raffleId,
                "raffleTitle" to details.raffleTitle,
                "rafflePrize" to details.rafflePrize,
                "ticketPrice" to details.ticketPrice,
                "ticketNumbers" to details.ticketNumbers,
                "ticketCount" to details.ticketCount,
                "isAnonymous" to details.isAnonymous
            )
        }

        private fun productOrderDetailsToMap(details: ProductOrderDetails): Map<String, Any?> {
            return mapOf(
                "orderNumber" to details.orderNumber,
                "items" to details.items.map { item ->
                    mapOf(
                        "productId" to item.productId,
                        "productName" to item.productName,
                        "productImage" to item.productImage,
                        "quantity" to item.quantity,
                        "unitPrice" to item.unitPrice,
                        "subtotal" to item.subtotal
                    )
                },
                "subtotal" to details.subtotal,
                "shippingCost" to details.shippingCost,
                "shippingMethod" to details.shippingMethod.name,
                "shippingAddress" to mapOf(
                    "recipientName" to details.shippingAddress.recipientName,
                    "recipientPhone" to details.shippingAddress.recipientPhone,
                    "zipCode" to details.shippingAddress.zipCode,
                    "street" to details.shippingAddress.street,
                    "number" to details.shippingAddress.number,
                    "complement" to details.shippingAddress.complement,
                    "neighborhood" to details.shippingAddress.neighborhood,
                    "city" to details.shippingAddress.city,
                    "state" to details.shippingAddress.state
                ),
                "trackingCode" to details.trackingCode,
                "carrier" to details.carrier,
                "trackingEvents" to details.trackingEvents.map { event ->
                    mapOf(
                        "timestamp" to event.timestamp,
                        "status" to event.status,
                        "description" to event.description,
                        "location" to event.location
                    )
                },
                "notes" to details.notes,
                "shippedAt" to details.shippedAt,
                "deliveredAt" to details.deliveredAt,
                "estimatedDelivery" to details.estimatedDelivery
            )
        }
    }
}
