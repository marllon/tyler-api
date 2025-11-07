package com.tylerproject.services

import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import com.tylerproject.models.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * 🛍️ Product Service - Gerenciamento de Produtos
 *
 * Funcionalidades:
 * - CRUD de produtos
 * - Busca e filtros
 * - Controle de estoque
 * - Categorização
 */
@Service
class ProductService {

    private val logger = LoggerFactory.getLogger(ProductService::class.java)
    private val firestore: Firestore by lazy { FirestoreClient.getFirestore() }
    private val collection = "products"

    fun createProduct(request: CreateProductRequest, createdBy: String? = null): Product {
        val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
        val id = UUID.randomUUID().toString()

        val product =
                Product(
                        id = id,
                        name = request.name,
                        description = request.description,
                        price = request.price,
                        imageUrl = request.imageUrl,
                        active = true,
                        category = request.category,
                        stock = request.stock,
                        createdAt = now,
                        updatedAt = now,
                        createdBy = createdBy
                )

        firestore.collection(collection).document(id).set(product).get()

        // auditService.logAction - será reativado quando AuditService voltar

        logger.info("✅ Produto criado: ${product.name} (${product.id})")
        return product
    }

    fun getProduct(id: String): Product? {
        val doc = firestore.collection(collection).document(id).get().get()
        return if (doc.exists()) {
            doc.toObject(Product::class.java)
        } else null
    }

    fun getAllProducts(
            page: Int = 1,
            pageSize: Int = 20,
            activeOnly: Boolean = true,
            category: String? = null
    ): Pair<List<Product>, Int> {
        var query: com.google.cloud.firestore.Query = firestore.collection(collection)

        if (activeOnly) {
            query = query.whereEqualTo("active", true)
        }

        if (category != null) {
            query = query.whereEqualTo("category", category)
        }

        val totalDocs = query.get().get().documents.size

        val offset = (page - 1) * pageSize
        val products =
                query.orderBy("createdAt", com.google.cloud.firestore.Query.Direction.DESCENDING)
                        .offset(offset)
                        .limit(pageSize)
                        .get()
                        .get()
                        .documents
                        .mapNotNull { document -> document.toObject(Product::class.java) }

        return Pair(products, totalDocs)
    }

    fun updateProduct(
            id: String,
            request: UpdateProductRequest,
            updatedBy: String? = null
    ): Product? {
        val existing = getProduct(id) ?: return null
        val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)

        val updated =
                existing.copy(
                        name = request.name ?: existing.name,
                        description = request.description ?: existing.description,
                        price = request.price ?: existing.price,
                        imageUrl = request.imageUrl ?: existing.imageUrl,
                        active = request.active ?: existing.active,
                        category = request.category ?: existing.category,
                        stock = request.stock ?: existing.stock,
                        updatedAt = now
                )

        firestore.collection(collection).document(id).set(updated).get()

        // auditService.logAction - será reativado quando AuditService voltar

        logger.info("✅ Produto atualizado: ${updated.name} (${updated.id})")
        return updated
    }

    fun deleteProduct(id: String, deletedBy: String? = null): Boolean {
        val existing = getProduct(id) ?: return false

        firestore.collection(collection).document(id).delete().get()

        // auditService.logAction - será reativado quando AuditService voltar

        logger.info("🗑️ Produto deletado: ${existing.name} (${existing.id})")
        return true
    }

    fun updateStock(productId: String, quantity: Int): Boolean {
        val product = getProduct(productId) ?: return false

        if (product.stock == null) return true // unlimited stock

        val newStock = product.stock - quantity
        if (newStock < 0) return false // insufficient stock

        val updated =
                product.copy(
                        stock = newStock,
                        updatedAt =
                                LocalDateTime.now()
                                        .atOffset(ZoneOffset.UTC)
                                        .format(DateTimeFormatter.ISO_INSTANT)
                )

        firestore.collection(collection).document(productId).set(updated).get()

        logger.info("📦 Estoque atualizado: ${product.name} - ${product.stock} → $newStock")
        return true
    }

    fun getProductsByIds(ids: List<String>): List<Product> {
        if (ids.isEmpty()) return emptyList()

        return firestore
                .collection(collection)
                .whereIn("id", ids)
                .get()
                .get()
                .documents
                .mapNotNull { document -> document.toObject(Product::class.java) }
    }

    // Método simples para listar todos os produtos
    fun getAllProducts(): List<Product> {
        return try {
            val documents = firestore.collection(collection)
                    .whereEqualTo("active", true)
                    .get()
                    .get()
                    .documents

            documents.mapNotNull { doc ->
                try {
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    logger.error("Erro ao converter documento ${doc.id}: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            logger.error("Erro ao buscar produtos: ${e.message}")
            emptyList()
        }
    }
}
