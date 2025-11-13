package com.tylerproject.domain.product
import kotlinx.serialization.Serializable
@Serializable
data class ProductImage(
        val id: String = "",
        val url: String = "",
        val filename: String = "",
        val contentType: String = "",
        val size: Long = 0L,
        val isPrimary: Boolean = false,
        val uploadedAt: String = ""
)
@Serializable
data class Product(
        val id: String = "",
        val name: String = "",
        val description: String = "",
        val price: Double = 0.0,
        val images: List<ProductImage> = emptyList(),
        val active: Boolean = true,
        val category: String? = null,
        val stock: Int? = null,
        val brand: String? = null,
        val model: String? = null,
        val weight: Double? = null,
        val dimensions: String? = null,
        val color: String? = null,
        val warranty: Int? = null,
        val tags: List<String>? = null,
        val createdAt: String = "",
        val updatedAt: String = "",
        val createdBy: String? = null
) {
        val primaryImageUrl: String?
                get() = images.find { it.isPrimary }?.url ?: images.firstOrNull()?.url
}
