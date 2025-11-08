package com.tylerproject.domain.product

import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

@Repository
class FirestoreProductRepository : ProductRepository {

    private val firestore: Firestore by lazy { FirestoreClient.getFirestore() }
    private val collection = "products"

    override fun save(product: Product): Product {
        val productToSave = if (product.id.isEmpty()) {
            product.copy(
                id = UUID.randomUUID().toString(),
                createdAt = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
            )
        } else {
            product.copy(
                updatedAt = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
            )
        }
        
        firestore.collection(collection).document(productToSave.id).set(productToSave).get()
        return productToSave
    }

    override fun findById(id: String): Product? {
        val document = firestore.collection(collection).document(id).get().get()
        return if (document.exists()) {
            document.toObject(Product::class.java)
        } else null
    }

    override fun findAll(page: Int, pageSize: Int, activeOnly: Boolean, category: String?): Pair<List<Product>, Int> {
        var query = firestore.collection(collection)
            .orderBy("createdAt", com.google.cloud.firestore.Query.Direction.DESCENDING)

        if (activeOnly) {
            query = query.whereEqualTo("active", true)
        }
        
        if (category != null) {
            query = query.whereEqualTo("category", category)
        }

        val countQuery = query.get().get()
        val total = countQuery.documents.size

        val offset = (page - 1) * pageSize
        val pagedQuery = query.offset(offset).limit(pageSize)
        
        val documents = pagedQuery.get().get()
        val products = documents.documents.mapNotNull { doc ->
            if (doc.exists()) doc.toObject(Product::class.java) else null
        }

        return Pair(products, total)
    }

    override fun update(id: String, product: Product): Product? {
        val existing = findById(id) ?: return null
        
        val updated = product.copy(
            id = id,
            createdAt = existing.createdAt,
            updatedAt = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
        )
        
        firestore.collection(collection).document(id).set(updated).get()
        return updated
    }

    override fun deleteById(id: String): Boolean {
        return try {
            firestore.collection(collection).document(id).delete().get()
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun existsById(id: String): Boolean {
        val document = firestore.collection(collection).document(id).get().get()
        return document.exists()
    }

    override fun countByCategory(category: String?): Int {
        val query = if (category != null) {
            firestore.collection(collection).whereEqualTo("category", category)
        } else {
            firestore.collection(collection)
        }
        
        return query.get().get().documents.size
    }
}