package com.tylerproject.controllers

import com.tylerproject.models.*
import com.tylerproject.services.ProductService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = ["*"])
class ProductController(@Autowired private val productService: ProductService) {

    private val logger = LoggerFactory.getLogger(ProductController::class.java)

    @GetMapping
    fun getAllProducts(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "true") activeOnly: Boolean,
        @RequestParam(required = false) category: String?
    ): ResponseEntity<Map<String, Any>> {
        return try {
            logger.info("GET /api/products - page: $page, pageSize: $pageSize, activeOnly: $activeOnly, category: $category")
            
            val (products: List<Product>, total: Int) = productService.getAllProducts(page, pageSize, activeOnly, category)
            
            val totalPages = (total + pageSize - 1) / pageSize
            val hasNext = page < totalPages
            
            val response = mapOf(
                "products" to products,
                "totalPages" to totalPages,
                "currentPage" to page,
                "totalItems" to total,
                "hasNext" to hasNext
            )
            
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Erro ao listar produtos: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Erro interno do servidor"))
        }
    }

    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: String): ResponseEntity<Any> {
        return try {
            logger.info("GET /api/products/$id")
            
            val product = productService.getProduct(id)
            if (product != null) {
                ResponseEntity.ok(product)
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(mapOf("error" to "Produto não encontrado"))
            }
        } catch (e: Exception) {
            logger.error("Erro ao buscar produto $id: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Erro interno do servidor"))
        }
    }

    @PostMapping
    fun createProduct(@RequestBody request: CreateProductRequest): ResponseEntity<Any> {
        return try {
            logger.info("POST /api/products - criando produto: ${request.name}")
            
            val createdProduct = productService.createProduct(request)
            ResponseEntity.status(HttpStatus.CREATED).body(createdProduct)
        } catch (e: Exception) {
            logger.error("Erro ao criar produto: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Erro interno do servidor"))
        }
    }

    @PutMapping("/{id}")
    fun updateProduct(
        @PathVariable id: String,
        @RequestBody request: UpdateProductRequest
    ): ResponseEntity<Any> {
        return try {
            logger.info("PUT /api/products/$id - atualizando produto")
            
            val updatedProduct = productService.updateProduct(id, request)
            if (updatedProduct != null) {
                ResponseEntity.ok(updatedProduct)
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(mapOf("error" to "Produto não encontrado"))
            }
        } catch (e: Exception) {
            logger.error("Erro ao atualizar produto $id: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Erro interno do servidor"))
        }
    }

    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: String): ResponseEntity<Any> {
        return try {
            logger.info("DELETE /api/products/$id")
            
            val deleted = productService.deleteProduct(id)
            if (deleted) {
                ResponseEntity.ok(mapOf("message" to "Produto removido com sucesso"))
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(mapOf("error" to "Produto não encontrado"))
            }
        } catch (e: Exception) {
            logger.error("Erro ao deletar produto $id: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Erro interno do servidor"))
        }
    }
}
