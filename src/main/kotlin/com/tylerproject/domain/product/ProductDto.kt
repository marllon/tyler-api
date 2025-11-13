package com.tylerproject.domain.product
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
data class CreateProductRequest
@JsonCreator
constructor(
        @JsonProperty("name") val name: String,
        @JsonProperty("description") val description: String,
        @JsonProperty("price") val price: Double,
        @JsonProperty("category") val category: String,
        @JsonProperty("stock") val stock: Int,
        @JsonProperty("active") val active: Boolean = true,
        @JsonProperty("brand") val brand: String? = null,
        @JsonProperty("model") val model: String? = null,
        @JsonProperty("weight") val weight: Double? = null,
        @JsonProperty("dimensions") val dimensions: String? = null,
        @JsonProperty("color") val color: String? = null,
        @JsonProperty("warranty") val warranty: Int? = null,
        @JsonProperty("tags") val tags: List<String>? = null
)
data class ImageUploadResponse(
        val id: String,
        val url: String,
        val filename: String,
        val contentType: String,
        val size: Long,
        val isPrimary: Boolean
)
data class ProductWithImagesRequest
@JsonCreator
constructor(
        @JsonProperty("name") val name: String,
        @JsonProperty("description") val description: String,
        @JsonProperty("price") val price: Double,
        @JsonProperty("category") val category: String,
        @JsonProperty("stock") val stock: Int,
        @JsonProperty("active") val active: Boolean = true,
        @JsonProperty("brand") val brand: String? = null,
        @JsonProperty("model") val model: String? = null,
        @JsonProperty("weight") val weight: Double? = null,
        @JsonProperty("dimensions") val dimensions: String? = null,
        @JsonProperty("color") val color: String? = null,
        @JsonProperty("warranty") val warranty: Int? = null,
        @JsonProperty("tags") val tags: List<String>? = null,
        @JsonProperty("primaryImageIndex")
        val primaryImageIndex: Int = 0 // Índice da imagem principal nas imagens enviadas
)
data class UpdateProductRequest
@JsonCreator
constructor(
        @JsonProperty("name") val name: String? = null,
        @JsonProperty("description") val description: String? = null,
        @JsonProperty("price") val price: Double? = null,
        @JsonProperty("category") val category: String? = null,
        @JsonProperty("stock") val stock: Int? = null,
        @JsonProperty("active") val active: Boolean? = null,
        @JsonProperty("brand") val brand: String? = null,
        @JsonProperty("model") val model: String? = null,
        @JsonProperty("weight") val weight: Double? = null,
        @JsonProperty("dimensions") val dimensions: String? = null,
        @JsonProperty("color") val color: String? = null,
        @JsonProperty("warranty") val warranty: Int? = null,
        @JsonProperty("tags") val tags: List<String>? = null
)
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
@Deprecated("Use ProductPageResponse with cursor-based pagination")
data class ProductListResponse(
        val products: List<ProductResponse>,
        val totalProducts: Long, // ❌ Custoso no NoSQL
        val currentPage: Int, // ❌ Conceito inválido com cursors
        val totalPages: Int, // ❌ Custoso no NoSQL
        val pageSize: Int
)
data class ProductPageResponse(
        val products: List<ProductResponse>,
        val pageSize: Int,
        val hasNext: Boolean,
        val nextCursor: String? = null,
        val hasPrevious: Boolean = false,
        val previousCursor: String? = null,
        val isEmpty: Boolean = products.isEmpty(),
        val count: Int = products.size
)
data class ProductDeletedResponse(
        val message: String,
        val deletedProductId: String,
        val timestamp: String
)
