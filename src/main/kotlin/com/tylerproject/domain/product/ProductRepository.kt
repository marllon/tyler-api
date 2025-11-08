package com.tylerproject.domain.product

interface ProductRepository {
    fun save(product: Product): Product
    fun findById(id: String): Product?
    fun findAll(
            page: Int,
            pageSize: Int,
            activeOnly: Boolean,
            category: String?
    ): Pair<List<Product>, Int>
    fun update(id: String, product: Product): Product?
    fun deleteById(id: String): Boolean
    fun existsById(id: String): Boolean
    fun countByCategory(category: String?): Int
}
