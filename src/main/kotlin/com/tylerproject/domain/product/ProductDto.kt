package com.tylerproject.domain.product

import kotlinx.serialization.Serializable

@Serializable
data class CreateProductRequest(
        val name: String,
        val description: String,
        val price: Double,
        val category: String,
        val stock: Int,
        val active: Boolean = true,
        val imageUrl: String? = null,
        val brand: String? = null,
        val model: String? = null,
        val weight: Double? = null,
        val dimensions: String? = null,
        val color: String? = null,
        val warranty: Int? = null,
        val tags: List<String>? = null
)

@Serializable
data class UpdateProductRequest(
        val name: String? = null,
        val description: String? = null,
        val price: Double? = null,
        val category: String? = null,
        val stock: Int? = null,
        val active: Boolean? = null,
        val imageUrl: String? = null,
        val brand: String? = null,
        val model: String? = null,
        val weight: Double? = null,
        val dimensions: String? = null,
        val color: String? = null,
        val warranty: Int? = null,
        val tags: List<String>? = null
)

@Serializable
data class ProductResponse(
        val id: String,
        val name: String,
        val description: String,
        val price: Double,
        val category: String? = null,
        val stock: Int? = null,
        val active: Boolean? = null,
        val imageUrl: String? = null,
        val brand: String? = null,
        val model: String? = null,
        val weight: Double? = null,
        val dimensions: String? = null,
        val color: String? = null,
        val warranty: Int? = null,
        val tags: List<String>? = null,
        val createdAt: String? = null,
        val updatedAt: String? = null
)

@Serializable
data class ProductListResponse(
        val products: List<ProductResponse>,
        val totalProducts: Long,
        val currentPage: Int,
        val totalPages: Int,
        val pageSize: Int
)

@Serializable
data class ProductDeletedResponse(
        val message: String,
        val deletedProductId: String,
        val timestamp: String
)
