package com.tylerproject.domain.product

import com.tylerproject.infrastructure.repository.PageRequest
import com.tylerproject.infrastructure.repository.PageResult

interface ProductRepository {
    fun save(product: Product): Product
    fun findById(id: String): Product?
    fun update(id: String, product: Product): Product?
    fun deleteById(id: String): Boolean
    fun existsById(id: String): Boolean
    fun findAll(
            request: PageRequest,
            activeOnly: Boolean = true,
            category: String? = null
    ): PageResult<Product>
    fun findByCategory(category: String, limit: Int = 20): List<Product>
    fun findByPriceRange(minPrice: Double, maxPrice: Double): List<Product>
    fun findByActiveStatus(active: Boolean): List<Product>
    fun countByCategory(category: String?): Int
    fun countTotal(): Int
    fun searchByName(searchTerm: String, limit: Int = 20): List<Product>
}

enum class ProductSortField(val fieldName: String) {
    CREATED_AT("createdAt"),
    UPDATED_AT("updatedAt"),
    NAME("name"),
    PRICE("price")
}
