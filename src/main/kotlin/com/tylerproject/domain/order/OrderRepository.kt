package com.tylerproject.domain.order

import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Query
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

interface OrderRepository {
    fun save(order: Order): Order
    fun update(orderId: String, updates: Map<String, Any>)
    fun findById(id: String): Order?
    fun findByOrderNumber(orderNumber: String): Order?
    fun findByUserId(
            userId: String,
            limit: Int? = null,
            startAfter: String? = null
    ): Pair<List<Order>, String?>
    fun findByUserIdAndStatus(userId: String, status: OrderStatus, limit: Int? = null): List<Order>
    fun findByPaymentId(paymentId: String): Order?
}

@Repository
class FirestoreOrderRepository(private val firestore: Firestore) : OrderRepository {

    private val logger = LoggerFactory.getLogger(FirestoreOrderRepository::class.java)
    private val collection = firestore.collection("orders")

    override fun save(order: Order): Order {
        return try {
            val docRef =
                    if (order.id.isBlank()) {
                        collection.document()
                    } else {
                        collection.document(order.id)
                    }

            val orderWithId = order.copy(id = docRef.id)

            // Converter para DTO antes de salvar
            val dto = OrderFirestoreDto.fromDomain(orderWithId)
            docRef.set(dto).get()

            logger.info("Order saved successfully: ${orderWithId.id}")
            orderWithId
        } catch (e: Exception) {
            logger.error("Error saving order: ${e.message}", e)
            throw RuntimeException("Failed to save order", e)
        }
    }

    override fun update(orderId: String, updates: Map<String, Any>) {
        try {
            collection.document(orderId).update(updates).get()
            logger.info("Order updated successfully: $orderId")
        } catch (e: Exception) {
            logger.error("Error updating order $orderId: ${e.message}", e)
            throw RuntimeException("Failed to update order", e)
        }
    }

    override fun findById(id: String): Order? {
        return try {
            val docSnapshot = collection.document(id).get().get()
            if (docSnapshot.exists()) {
                val dto = docSnapshot.toObject(OrderFirestoreDto::class.java)
                dto?.toDomain()
            } else {
                null
            }
        } catch (e: Exception) {
            logger.error("Error finding order by id $id: ${e.message}", e)
            null
        }
    }

    override fun findByOrderNumber(orderNumber: String): Order? {
        return try {
            val querySnapshot =
                    collection.whereEqualTo("orderNumber", orderNumber).limit(1).get().get()

            if (!querySnapshot.isEmpty) {
                val dto = querySnapshot.documents.first().toObject(OrderFirestoreDto::class.java)
                dto?.toDomain()
            } else {
                null
            }
        } catch (e: Exception) {
            logger.error("Error finding order by orderNumber $orderNumber: ${e.message}", e)
            null
        }
    }

    override fun findByUserId(
            userId: String,
            limit: Int?,
            startAfter: String?
    ): Pair<List<Order>, String?> {
        return try {
            var query: Query =
                    collection
                            .whereEqualTo("userId", userId)
                            .orderBy("createdAt", Query.Direction.DESCENDING)

            // Paginação
            if (startAfter != null) {
                val startDoc = collection.document(startAfter).get().get()
                if (startDoc.exists()) {
                    query = query.startAfter(startDoc)
                }
            }

            if (limit != null) {
                query = query.limit(limit + 1) // +1 para verificar se tem próxima página
            }

            val querySnapshot = query.get().get()
            val documents = querySnapshot.documents

            val orders =
                    if (limit != null && documents.size > limit) {
                        documents.take(limit).mapNotNull {
                            it.toObject(OrderFirestoreDto::class.java)?.toDomain()
                        }
                    } else {
                        documents.mapNotNull {
                            it.toObject(OrderFirestoreDto::class.java)?.toDomain()
                        }
                    }

            val nextCursor =
                    if (limit != null && documents.size > limit) {
                        documents[limit - 1].id
                    } else {
                        null
                    }

            Pair(orders, nextCursor)
        } catch (e: Exception) {
            logger.error("Error finding orders by userId $userId: ${e.message}", e)
            Pair(emptyList(), null)
        }
    }

    override fun findByUserIdAndStatus(
            userId: String,
            status: OrderStatus,
            limit: Int?
    ): List<Order> {
        return try {
            var query: Query =
                    collection
                            .whereEqualTo("userId", userId)
                            .whereEqualTo("status", status.name)
                            .orderBy("createdAt", Query.Direction.DESCENDING)

            if (limit != null) {
                query = query.limit(limit)
            }

            val querySnapshot = query.get().get()
            querySnapshot.documents.mapNotNull {
                it.toObject(OrderFirestoreDto::class.java)?.toDomain()
            }
        } catch (e: Exception) {
            logger.error(
                    "Error finding orders by userId $userId and status $status: ${e.message}",
                    e
            )
            emptyList()
        }
    }

    override fun findByPaymentId(paymentId: String): Order? {
        return try {
            val querySnapshot = collection.whereEqualTo("paymentId", paymentId).limit(1).get().get()

            if (!querySnapshot.isEmpty) {
                val dto = querySnapshot.documents.first().toObject(OrderFirestoreDto::class.java)
                dto?.toDomain()
            } else {
                null
            }
        } catch (e: Exception) {
            logger.error("Error finding order by paymentId $paymentId: ${e.message}", e)
            null
        }
    }
}
