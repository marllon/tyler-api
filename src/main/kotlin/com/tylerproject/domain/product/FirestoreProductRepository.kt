package com.tylerproject.domain.product
import com.tylerproject.infrastructure.repository.GenericFirestoreRepository
import com.tylerproject.infrastructure.repository.PageRequest
import com.tylerproject.infrastructure.repository.PageResult
import org.springframework.stereotype.Repository
@Repository
class FirestoreProductRepository :
        GenericFirestoreRepository<Product>("products", Product::class.java), ProductRepository {
    override fun extractId(entity: Product): String = entity.id
    override fun updateEntity(entity: Product, updates: Map<String, Any>): Product {
        return entity.copy(
                id = updates["id"] as? String ?: entity.id,
                createdAt = updates["createdAt"] as? String ?: entity.createdAt,
                updatedAt = updates["updatedAt"] as? String ?: entity.updatedAt
        )
    }
    override fun update(id: String, product: Product): Product? {
        val existing = findById(id) ?: return null
        val updated =
                updateEntity(
                        product.copy(id = id, createdAt = existing.createdAt),
                        mapOf("updatedAt" to currentTimestamp())
                )
        return save(updated)
    }
    override fun findAll(
            request: PageRequest,
            activeOnly: Boolean,
            category: String?
    ): PageResult<Product> {
        val filters = buildMap {
            if (activeOnly) put("active", true)
            category?.let { put("category", it) }
        }
        return super.findAll(request, filters)
    }
    override fun findByCategory(category: String, limit: Int): List<Product> =
            findByField("category", category, limit)
    override fun findByPriceRange(minPrice: Double, maxPrice: Double): List<Product> =
            findByFieldRange("price", minPrice, maxPrice)
    override fun findByActiveStatus(active: Boolean): List<Product> = findByField("active", active)
    override fun countByCategory(category: String?): Int =
            if (category != null) countByField("category", category) else count()
    override fun countTotal(): Int = count()
    override fun searchByName(searchTerm: String, limit: Int): List<Product> =
            searchByField("name", searchTerm, limit)
    private fun currentTimestamp(): String {
        return java.time.LocalDateTime.now()
                .atOffset(java.time.ZoneOffset.UTC)
                .format(java.time.format.DateTimeFormatter.ISO_INSTANT)
    }
}
