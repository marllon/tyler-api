package com.tylerproject.domain.product

import com.tylerproject.infrastructure.repository.PageDirection
import com.tylerproject.infrastructure.repository.PageRequest
import com.tylerproject.infrastructure.repository.SortDirection
import com.tylerproject.service.ImageUploadService
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ProductService(
        private val productRepository: ProductRepository,
        private val imageUploadService: ImageUploadService
) {

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
                        images = emptyList(),
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

    fun createProductWithImages(
            request: ProductWithImagesRequest,
            images: Array<MultipartFile>,
            createdBy: String? = null
    ): ProductResponse {
        val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
        val id = UUID.randomUUID().toString()

        // Upload das imagens primeiro
        val uploadedImages =
                imageUploadService.uploadProductImages(
                        productId = id,
                        files = images,
                        primaryImageIndex = request.primaryImageIndex
                )

        val product =
                Product(
                        id = id,
                        name = request.name,
                        description = request.description,
                        price = request.price,
                        images = uploadedImages,
                        active = request.active,
                        category = request.category,
                        stock = request.stock,
                        brand = request.brand,
                        model = request.model,
                        weight = request.weight,
                        dimensions = request.dimensions,
                        color = request.color,
                        warranty = request.warranty,
                        tags = request.tags,
                        createdAt = now,
                        updatedAt = now,
                        createdBy = createdBy
                )

        val savedProduct = productRepository.save(product)
        logger.info(
                "Product with images created: ${savedProduct.name} (${savedProduct.id}) - ${uploadedImages.size} images"
        )

        return savedProduct.toResponse()
    }

    fun getProductById(id: String): ProductResponse? {
        val product = productRepository.findById(id)
        return product?.toResponse()
    }

    // ✅ Método deprecated - manter para compatibilidade
    @Deprecated("Use getProductsPaginated() with cursor-based pagination")
    fun getAllProducts(
            page: Int = 1,
            pageSize: Int = 20,
            activeOnly: Boolean = true,
            category: String? = null
    ): ProductListResponse {
        // Converter para nova API
        val request =
                PageRequest(
                        limit = pageSize,
                        cursor = null,
                        sortBy = ProductSortField.CREATED_AT.fieldName,
                        sortDirection = SortDirection.DESC
                )

        val productPage = productRepository.findAll(request, activeOnly, category)

        return ProductListResponse(
                products = productPage.items.map { it.toResponse() },
                totalPages = -1,
                currentPage = page,
                totalProducts = -1L,
                pageSize = productPage.pageSize
        )
    }

    // ✅ Nova API otimizada para NoSQL
    fun getProductsPaginated(
            limit: Int = 20,
            cursor: String? = null,
            direction: PageDirection = PageDirection.NEXT,
            sortBy: ProductSortField = ProductSortField.CREATED_AT,
            sortDirection: SortDirection = SortDirection.DESC,
            activeOnly: Boolean = true,
            category: String? = null
    ): ProductPageResponse {
        val request = PageRequest(limit, cursor, direction, sortBy.fieldName, sortDirection)
        val productPage = productRepository.findAll(request, activeOnly, category)

        return ProductPageResponse(
                products = productPage.items.map { it.toResponse() },
                pageSize = productPage.pageSize,
                hasNext = productPage.hasNext,
                nextCursor = productPage.nextCursor,
                hasPrevious = productPage.hasPrevious,
                previousCursor = productPage.previousCursor
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
                        active = request.active ?: existing.active,
                        category = request.category ?: existing.category,
                        stock = request.stock ?: existing.stock,
                        brand = request.brand ?: existing.brand,
                        model = request.model ?: existing.model,
                        weight = request.weight ?: existing.weight,
                        dimensions = request.dimensions ?: existing.dimensions,
                        color = request.color ?: existing.color,
                        warranty = request.warranty ?: existing.warranty,
                        tags = request.tags ?: existing.tags,
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
                    timestamp =
                            LocalDateTime.now()
                                    .atOffset(ZoneOffset.UTC)
                                    .format(DateTimeFormatter.ISO_INSTANT)
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
                images = images.map(imageUploadService::toImageUploadResponse),
                primaryImageUrl = primaryImageUrl,
                active = active,
                category = category,
                stock = stock,
                brand = brand,
                model = model,
                weight = weight,
                dimensions = dimensions,
                color = color,
                warranty = warranty,
                tags = tags,
                createdAt = createdAt,
                updatedAt = updatedAt
        )
    }

    // ✅ Métodos específicos para NoSQL - aproveitam queries otimizadas

    fun getProductsByCategory(category: String, limit: Int = 20): List<ProductResponse> {
        logger.info("Getting products by category: $category (limit: $limit)")
        return productRepository.findByCategory(category, limit).map { it.toResponse() }
    }

    fun getProductsByPriceRange(minPrice: Double, maxPrice: Double): List<ProductResponse> {
        logger.info("Getting products by price range: $minPrice - $maxPrice")
        return productRepository.findByPriceRange(minPrice, maxPrice).map { it.toResponse() }
    }

    fun searchProductsByName(searchTerm: String, limit: Int = 20): List<ProductResponse> {
        logger.info("Searching products by name: $searchTerm")
        return productRepository.searchByName(searchTerm, limit).map { it.toResponse() }
    }

    fun getInactiveProducts(): List<ProductResponse> {
        logger.info("Getting inactive products")
        return productRepository.findByActiveStatus(false).map { it.toResponse() }
    }

    fun getTotalProductsCount(): Int {
        return productRepository.countTotal()
    }
}
