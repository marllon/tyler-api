package com.tylerproject.domain.product

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = ["*"])
class ProductController(private val productService: ProductService) {

    private val logger = LoggerFactory.getLogger(ProductController::class.java)

    @GetMapping
    fun getAllProducts(
            @RequestParam(defaultValue = "1") page: Int,
            @RequestParam(defaultValue = "10") pageSize: Int,
            @RequestParam(defaultValue = "true") activeOnly: Boolean,
            @RequestParam(required = false) category: String?
    ): ResponseEntity<ProductListResponse> {
        return try {
            logger.info(
                    "Listing products - page: $page, pageSize: $pageSize, activeOnly: $activeOnly, category: $category"
            )

            val response = productService.getAllProducts(page, pageSize, activeOnly, category)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Error listing products: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: String): ResponseEntity<ProductResponse> {
        return try {
            logger.info("Getting product by id: $id")

            val product = productService.getProductById(id)

            if (product != null) {
                ResponseEntity.ok(product)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            logger.error("Error getting product $id: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PostMapping
    fun createProduct(@RequestBody request: CreateProductRequest): ResponseEntity<ProductResponse> {
        return try {
            logger.info("Creating product: ${request.name}")

            val product = productService.createProduct(request)
            ResponseEntity.status(HttpStatus.CREATED).body(product)
        } catch (e: Exception) {
            logger.error("Error creating product: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @PutMapping("/{id}")
    fun updateProduct(
            @PathVariable id: String,
            @RequestBody request: UpdateProductRequest
    ): ResponseEntity<ProductResponse> {
        return try {
            logger.info("Updating product: $id")

            val product = productService.updateProduct(id, request)

            if (product != null) {
                ResponseEntity.ok(product)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            logger.error("Error updating product $id: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: String): ResponseEntity<ProductDeletedResponse> {
        return try {
            logger.info("Deleting product: $id")

            val result = productService.deleteProduct(id)

            if (result != null) {
                ResponseEntity.ok(result)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            logger.error("Error deleting product $id: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}
