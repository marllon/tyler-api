package com.tylerproject.domain.product

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ProductService(private val productRepository: ProductRepository) {

    private val logger = LoggerFactory.getLogger(ProductService::class.java)

    fun createProduct(request: CreateProductRequest, createdBy: String? = null): ProductResponse {
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

        val savedProduct = productRepository.save(product)
        logger.info("Product created: ${savedProduct.name} (${savedProduct.id})")

        return savedProduct.toResponse()
    }

    fun getProductById(id: String): ProductResponse? {
        val product = productRepository.findById(id)
        return product?.toResponse()
    }

    fun getAllProducts(
            page: Int = 1,
            pageSize: Int = 20,
            activeOnly: Boolean = true,
            category: String? = null
    ): ProductListResponse {
        val (products, total) = productRepository.findAll(page, pageSize, activeOnly, category)
        val totalPages = (total + pageSize - 1) / pageSize

        return ProductListResponse(
                products = products.map { it.toResponse() },
                totalPages = totalPages,
                currentPage = page,
                totalProducts = total.toLong(),
                pageSize = pageSize
        )
    }

    fun updateProduct(id: String, request: UpdateProductRequest): ProductResponse? {
        val existing = productRepository.findById(id) ?: return null
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

        val savedProduct = productRepository.update(id, updated)
        logger.info("Product updated: ${updated.name} (${updated.id})")

        return savedProduct?.toResponse()
    }

    fun deleteProduct(id: String): ProductDeletedResponse? {
        if (!productRepository.existsById(id)) {
            return null
        }

        val deleted = productRepository.deleteById(id)

        return if (deleted) {
            logger.info("Product deleted: $id")
            ProductDeletedResponse(
                message = "Produto removido com sucesso", 
                deletedProductId = id,
                timestamp = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
            )
        } else {
            null
        }
    }

    private fun Product.toResponse(): ProductResponse {
        return ProductResponse(
                id = id,
                name = name,
                description = description,
                price = price,
                imageUrl = imageUrl,
                active = active,
                category = category,
                stock = stock,
                createdAt = createdAt,
                updatedAt = updatedAt
        )
    }
}
