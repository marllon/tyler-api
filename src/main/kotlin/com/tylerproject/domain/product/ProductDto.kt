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
        val brand: String? = null,
        val model: String? = null,
        val weight: Double? = null,
        val dimensions: String? = null,
        val color: String? = null,
        val warranty: Int? = null,
        val tags: List<String>? = null
)

@Serializable
data class ImageUploadResponse(
        val id: String,
        val url: String,
        val filename: String,
        val contentType: String,
        val size: Long,
        val isPrimary: Boolean
)

@Serializable
data class ProductWithImagesRequest(
        val name: String,
        val description: String,
        val price: Double,
        val category: String,
        val stock: Int,
        val active: Boolean = true,
        val brand: String? = null,
        val model: String? = null,
        val weight: Double? = null,
        val dimensions: String? = null,
        val color: String? = null,
        val warranty: Int? = null,
        val tags: List<String>? = null,
        val primaryImageIndex: Int = 0 // Índice da imagem principal nas imagens enviadas
)

@Serializable
data class UpdateProductRequest(
        val name: String? = null,
        val description: String? = null,
        val price: Double? = null,
        val category: String? = null,
        val stock: Int? = null,
        val active: Boolean? = null,
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
        val images: List<ImageUploadResponse> = emptyList(),
        val primaryImageUrl: String? = null,
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
@Deprecated("Use ProductPageResponse with cursor-based pagination")
data class ProductListResponse(
        val products: List<ProductResponse>,
        val totalProducts: Long, // ❌ Custoso no NoSQL
        val currentPage: Int, // ❌ Conceito inválido com cursors
        val totalPages: Int, // ❌ Custoso no NoSQL
        val pageSize: Int
)

@Serializable
data class ProductPageResponse(
        val products: List<ProductResponse>,
        val pageSize: Int,
        val hasNext: Boolean,
        val nextCursor: String? = null,
        val hasPrevious: Boolean = false,
        val previousCursor: String? = null,

        // ✅ Metadados úteis para UI
        val isEmpty: Boolean = products.isEmpty(),
        val count: Int = products.size
)

@Serializable
data class ProductDeletedResponse(
        val message: String,
        val deletedProductId: String,
        val timestamp: String
)
