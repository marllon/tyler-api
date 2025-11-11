package com.tylerproject.service

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.tylerproject.domain.product.ImageUploadResponse
import com.tylerproject.domain.product.ProductImage
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ImageUploadService(
        private val storage: Storage,
        @Qualifier("bucketName") private val bucketName: String,
        @Value("\${app.gcp.base-url}") private val baseUrl: String
) {

    private val logger = LoggerFactory.getLogger(ImageUploadService::class.java)

    companion object {
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024L // 10MB
        private const val MAX_IMAGES_PER_PRODUCT = 10
        private val ALLOWED_CONTENT_TYPES =
                setOf("image/jpeg", "image/jpg", "image/png", "image/webp")
    }

    /** Upload múltiplas imagens para um produto */
    fun uploadProductImages(
            productId: String,
            files: Array<MultipartFile>,
            primaryImageIndex: Int = 0
    ): List<ProductImage> {
        validateFiles(files, primaryImageIndex)

        return files.mapIndexed { index, file ->
            val isPrimary = index == primaryImageIndex
            uploadSingleImage(productId, file, isPrimary)
        }
    }

    /** Upload de uma única imagem */
    fun uploadSingleImage(
            productId: String,
            file: MultipartFile,
            isPrimary: Boolean = false
    ): ProductImage {
        try {
            val filename = generateUniqueFilename(productId, file.originalFilename ?: "image")
            val objectPath = "products/$productId/images/$filename"

            logger.info("Uploading image: $filename for product: $productId")

            val blobId = BlobId.of(bucketName, objectPath)
            val blobInfo =
                    BlobInfo.newBuilder(blobId)
                            .setContentType(file.contentType)
                            .setCacheControl("public, max-age=31536000") // 1 year cache
                            .build()

            // Upload para Google Cloud Storage
            storage.create(blobInfo, file.bytes)

            // Gerar Signed URL (válida por 7 dias)
            val signedUrl = generateSignedUrl(objectPath)

            logger.info("Image uploaded successfully with signed URL generated")

            return ProductImage(
                    id = UUID.randomUUID().toString(),
                    url = signedUrl,
                    filename = filename,
                    contentType = file.contentType ?: "application/octet-stream",
                    size = file.size,
                    isPrimary = isPrimary,
                    uploadedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
        } catch (e: Exception) {
            logger.error("Error uploading image for product $productId: ${e.message}", e)
            throw IOException("Failed to upload image: ${e.message}", e)
        }
    }

    /** Remove uma imagem do storage */
    fun deleteImage(productId: String, filename: String): Boolean {
        return try {
            val objectPath = "products/$productId/images/$filename"
            val blobId = BlobId.of(bucketName, objectPath)

            val deleted = storage.delete(blobId)
            logger.info("Image deletion result for $filename: $deleted")

            deleted
        } catch (e: Exception) {
            logger.error("Error deleting image $filename for product $productId: ${e.message}", e)
            false
        }
    }

    /** Remove todas as imagens de um produto */
    fun deleteAllProductImages(productId: String): Boolean {
        return try {
            val prefix = "products/$productId/images/"
            val blobs = storage.list(bucketName, Storage.BlobListOption.prefix(prefix))

            var allDeleted = true
            blobs.iterateAll().forEach { blob ->
                val deleted = storage.delete(blob.blobId)
                if (!deleted) {
                    allDeleted = false
                    logger.warn("Failed to delete blob: ${blob.name}")
                }
            }

            logger.info("Product $productId images deletion result: $allDeleted")
            allDeleted
        } catch (e: Exception) {
            logger.error("Error deleting all images for product $productId: ${e.message}", e)
            false
        }
    }

    /** Converte ProductImage para ImageUploadResponse */
    fun toImageUploadResponse(image: ProductImage): ImageUploadResponse {
        return ImageUploadResponse(
                id = image.id,
                url = image.url,
                filename = image.filename,
                contentType = image.contentType,
                size = image.size,
                isPrimary = image.isPrimary
        )
    }

    private fun validateFiles(files: Array<MultipartFile>, primaryImageIndex: Int) {
        if (files.isEmpty()) {
            throw IllegalArgumentException("At least one image is required")
        }

        if (files.size > MAX_IMAGES_PER_PRODUCT) {
            throw IllegalArgumentException(
                    "Maximum $MAX_IMAGES_PER_PRODUCT images allowed per product"
            )
        }

        if (primaryImageIndex < 0 || primaryImageIndex >= files.size) {
            throw IllegalArgumentException("Invalid primary image index: $primaryImageIndex")
        }

        files.forEach { file -> validateSingleFile(file) }
    }

    private fun validateSingleFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw IllegalArgumentException("File cannot be empty")
        }

        if (file.size > MAX_FILE_SIZE) {
            throw IllegalArgumentException(
                    "File size exceeds maximum allowed size of ${MAX_FILE_SIZE / (1024 * 1024)}MB"
            )
        }

        val contentType = file.contentType
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.lowercase())) {
            throw IllegalArgumentException(
                    "Invalid file type. Allowed types: ${ALLOWED_CONTENT_TYPES.joinToString(", ")}"
            )
        }
    }

    private fun generateUniqueFilename(productId: String, originalFilename: String): String {
        val timestamp = System.currentTimeMillis()
        val randomId = UUID.randomUUID().toString().substring(0, 8)
        val extension = originalFilename.substringAfterLast('.', "jpg")

        return "${productId}_${timestamp}_${randomId}.$extension"
    }

    /** Gera Signed URL para acesso seguro e temporário à imagem URLs válidas por 7 dias */
    private fun generateSignedUrl(objectPath: String): String {
        return try {
            val blobId = BlobId.of(bucketName, objectPath)
            val blob =
                    storage.get(blobId)
                            ?: throw IllegalArgumentException("Object not found: $objectPath")

            val signedUrl = blob.signUrl(7, TimeUnit.DAYS)
            logger.debug("Generated signed URL for $objectPath (expires in 7 days)")

            signedUrl.toString()
        } catch (e: Exception) {
            logger.error("Error generating signed URL for $objectPath: ${e.message}", e)
            // Fallback para URL direta (se bucket for público)
            "$baseUrl/$bucketName/$objectPath"
        }
    }

    /** Regenera Signed URLs para imagens existentes (útil para renovação) */
    fun refreshImageUrls(images: List<ProductImage>): List<ProductImage> {
        return images.map { image ->
            try {
                val objectPath = extractObjectPathFromUrl(image.url)
                val newSignedUrl = generateSignedUrl(objectPath)
                image.copy(url = newSignedUrl)
            } catch (e: Exception) {
                logger.warn("Failed to refresh URL for image ${image.id}: ${e.message}")
                image // Retorna a imagem original em caso de erro
            }
        }
    }

    private fun extractObjectPathFromUrl(url: String): String {
        // Extrai o caminho do objeto a partir da URL (signed ou direta)
        return when {
            url.contains("googleapis.com") -> {
                val parts = url.split("/$bucketName/")
                if (parts.size > 1) {
                    parts[1].split("?")[0] // Remove query parameters se existirem
                } else {
                    throw IllegalArgumentException("Invalid GCS URL format: $url")
                }
            }
            else -> throw IllegalArgumentException("Unsupported URL format: $url")
        }
    }
}
