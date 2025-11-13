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
        // 1. Criar produto básico primeiro
        val basicRequest =
                CreateProductRequest(
                        name = request.name,
                        description = request.description,
                        price = request.price,
                        category = request.category,
                        stock = request.stock,
                        active = request.active,
                        brand = request.brand,
                        model = request.model,
                        weight = request.weight,
                        dimensions = request.dimensions,
                        color = request.color,
                        warranty = request.warranty,
                        tags = request.tags
                )

        val productResponse = createProduct(basicRequest, createdBy)

        // 2. Adicionar imagens se fornecidas
        images.forEachIndexed { index, file ->
            val isPrimary = index == request.primaryImageIndex
            uploadProductImage(productResponse.id, file, isPrimary)
        }

        // 3. Retornar produto atualizado com imagens
        return getProductById(productResponse.id) ?: productResponse
    }

    fun getProductById(id: String): ProductResponse? {
        val product = productRepository.findById(id)
        return product?.toResponse()
    }

    // ✅ API otimizada para NoSQL com cursor-based pagination
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

    fun uploadProductImage(
            productId: String,
            file: MultipartFile,
            isPrimary: Boolean
    ): ImageUploadResponse {
        logger.info("Uploading image for product: $productId, isPrimary: $isPrimary")

        val product =
                productRepository.findById(productId)
                        ?: throw IllegalArgumentException("Product not found: $productId")

        val newImage = imageUploadService.uploadSingleImage(productId, file, isPrimary)

        val updatedImages =
                if (isPrimary) {
                    product.images.map { it.copy(isPrimary = false) } +
                            newImage.copy(isPrimary = true)
                } else {
                    product.images + newImage
                }

        val updatedProduct = product.copy(images = updatedImages)
        logger.info("Saving updated product with ${updatedImages.size} images")
        val savedProduct = productRepository.save(updatedProduct)
        logger.info(
                "Product saved successfully. Saved product has ${savedProduct.images.size} images"
        )

        val uploadResponse = imageUploadService.toImageUploadResponse(newImage)

        logger.info(
                "Image uploaded and product updated successfully for product: $productId, imageId: ${uploadResponse.id}"
        )
        return uploadResponse
    }

    fun removeProductImage(productId: String, imageId: String) {
        logger.info("Removing image $imageId from product: $productId")

        val product =
                productRepository.findById(productId)
                        ?: throw IllegalArgumentException("Product not found: $productId")

        if (product.images.none { it.id == imageId }) {
            throw IllegalArgumentException("Image not found: $imageId")
        }

        imageUploadService.removeProductImage(product, imageId)

        val updatedImages = product.images.filter { it.id != imageId }
        val updatedProduct = product.copy(images = updatedImages)
        productRepository.save(updatedProduct)

        logger.info(
                "Image removed and product updated successfully for product: $productId, imageId: $imageId"
        )
    }
}
