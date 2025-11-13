package com.tylerproject.domain.product
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = ["*"])
@Tag(
        name = "🛍️ Products",
        description =
                "API de Gerenciamento de Produtos com upload de imagens, paginação otimizada e filtros avançados"
)
class ProductController(private val productService: ProductService) {
        private val logger = LoggerFactory.getLogger(ProductController::class.java)
        @GetMapping
        @Operation(
                summary = "📋 Listar produtos (paginação tradicional)",
                description =
                        "Lista produtos com paginação tradicional. RECOMENDADO: Use /paginated para melhor performance.",
                tags = ["Listagem", "Depreciado"]
        )
        @ApiResponses(
                value =
                        [
                                ApiResponse(
                                        responseCode = "200",
                                        description = "✅ Lista de produtos retornada com sucesso",
                                        content =
                                                [
                                                        Content(
                                                                mediaType = "application/json",
                                                                schema =
                                                                        Schema(
                                                                                implementation =
                                                                                        ProductListResponse::class
                                                                        ),
                                                                examples =
                                                                        [
                                                                                io.swagger.v3.oas
                                                                                        .annotations
                                                                                        .media
                                                                                        .ExampleObject(
                                                                                                name =
                                                                                                        "Exemplo de resposta",
                                                                                                value =
                                                                                                        """{
                                        "products": [
                                            {
                                                "id": "product_123",
                                                "name": "Smartphone XYZ",
                                                "price": 899.99,
                                                "category": "Eletronicos",
                                                "stock": 50,
                                                "active": true
                                            }
                                        ],
                                        "currentPage": 1,
                                        "totalPages": 10,
                                        "pageSize": 10
                                    }"""
                                                                                        )]
                                                        )]
                                ),
                                ApiResponse(
                                        responseCode = "400",
                                        description = "❌ Parâmetros inválidos"
                                ),
                                ApiResponse(
                                        responseCode = "500",
                                        description = "❌ Erro interno do servidor"
                                )]
        )
        fun getAllProducts(
                @Parameter(description = "Número da página (inicia em 1)", example = "1")
                @RequestParam(defaultValue = "1")
                page: Int,
                @Parameter(description = "Quantidade de itens por página", example = "10")
                @RequestParam(defaultValue = "10")
                pageSize: Int,
                @Parameter(description = "Filtrar apenas produtos ativos", example = "true")
                @RequestParam(defaultValue = "true")
                activeOnly: Boolean,
                @Parameter(
                        description = "Filtrar por categoria específica",
                        example = "Eletronicos"
                )
                @RequestParam(required = false)
                category: String?
        ): ResponseEntity<ProductListResponse> {
                return try {
                        logger.info(
                                "Listing products - page: $page, pageSize: $pageSize, activeOnly: $activeOnly, category: $category"
                        )
                        val pageResponse =
                                productService.getProductsPaginated(
                                        limit = pageSize,
                                        cursor = null,
                                        activeOnly = activeOnly,
                                        category = category
                                )
                        @Suppress("DEPRECATION")
                        val response =
                                ProductListResponse(
                                        products = pageResponse.products,
                                        totalPages = -1,
                                        currentPage = page,
                                        totalProducts = -1L,
                                        pageSize = pageResponse.pageSize
                                )
                        ResponseEntity.ok(response)
                } catch (e: Exception) {
                        logger.error("Error listing products: ${e.message}", e)
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                }
        }
        @GetMapping("/{id}")
        @Operation(
                summary = "Buscar produto por ID",
                description = "Retorna um produto específico pelo seu identificador único"
        )
        @ApiResponses(
                ApiResponse(responseCode = "200", description = "Produto encontrado"),
                ApiResponse(responseCode = "404", description = "Produto não encontrado"),
                ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        )
        fun getProductById(
                @Parameter(
                        description = "ID único do produto",
                        example = "550e8400-e29b-41d4-a716-446655440000"
                )
                @PathVariable
                id: String
        ): ResponseEntity<ProductResponse> {
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
        @GetMapping("/paginated")
        @Operation(
                summary = "🚀 Listar produtos (cursor pagination) - RECOMENDADO",
                description =
                        "Versão otimizada para NoSQL com cursor-based pagination. Performance O(1) vs O(n) da paginação tradicional.",
                tags = ["Listagem", "Recomendado"]
        )
        @ApiResponses(
                value =
                        [
                                ApiResponse(
                                        responseCode = "200",
                                        description = "Lista de produtos com cursor pagination",
                                        content =
                                                [
                                                        Content(
                                                                mediaType = "application/json",
                                                                schema =
                                                                        Schema(
                                                                                implementation =
                                                                                        ProductPageResponse::class
                                                                        )
                                                        )]
                                ),
                                ApiResponse(
                                        responseCode = "400",
                                        description = "Parâmetros inválidos"
                                ),
                                ApiResponse(
                                        responseCode = "500",
                                        description = "Erro interno do servidor"
                                )]
        )
        fun getProductsPaginated(
                @Parameter(description = "Quantidade de itens por página", example = "20")
                @RequestParam(defaultValue = "20")
                limit: Int,
                @Parameter(
                        description = "Cursor para próxima/anterior página",
                        example = "product_id_123"
                )
                @RequestParam(required = false)
                cursor: String?,
                @Parameter(description = "Direção da paginação", example = "NEXT")
                @RequestParam(defaultValue = "NEXT")
                direction: com.tylerproject.infrastructure.repository.PageDirection,
                @Parameter(description = "Campo de ordenação", example = "CREATED_AT")
                @RequestParam(defaultValue = "CREATED_AT")
                sortBy: ProductSortField,
                @Parameter(description = "Direção da ordenação", example = "DESC")
                @RequestParam(defaultValue = "DESC")
                sortDirection: com.tylerproject.infrastructure.repository.SortDirection,
                @Parameter(description = "Filtrar apenas produtos ativos", example = "true")
                @RequestParam(defaultValue = "true")
                activeOnly: Boolean,
                @Parameter(description = "Filtrar por categoria", example = "smartphones")
                @RequestParam(required = false)
                category: String?
        ): ResponseEntity<ProductPageResponse> {
                return try {
                        logger.info(
                                "Getting products with cursor pagination - limit: $limit, cursor: $cursor"
                        )
                        val response =
                                productService.getProductsPaginated(
                                        limit,
                                        cursor,
                                        direction,
                                        sortBy,
                                        sortDirection,
                                        activeOnly,
                                        category
                                )
                        ResponseEntity.ok(response)
                } catch (e: IllegalArgumentException) {
                        logger.error("Invalid pagination parameters: ${e.message}", e)
                        ResponseEntity.badRequest().build()
                } catch (e: Exception) {
                        logger.error("Error getting paginated products: ${e.message}", e)
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                }
        }
        @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
        @Operation(
                summary = "➕ Criar produto (com imagens opcionais) - UNIFICADO",
                description =
                        "Endpoint principal para criar produtos. Suporta até 10 imagens simultaneamente. Reduz número de requests HTTP.",
                tags = ["CRUD", "Upload", "Recomendado"],
                security = [SecurityRequirement(name = "Bearer")]
        )
        @ApiResponses(
                value =
                        [
                                ApiResponse(
                                        responseCode = "201",
                                        description = "Product created successfully with images",
                                        content =
                                                [
                                                        Content(
                                                                mediaType = "application/json",
                                                                schema =
                                                                        Schema(
                                                                                implementation =
                                                                                        ProductResponse::class
                                                                        )
                                                        )]
                                ),
                                ApiResponse(
                                        responseCode = "400",
                                        description = "Invalid request data or image files"
                                ),
                                ApiResponse(
                                        responseCode = "413",
                                        description = "File size too large"
                                ),
                                ApiResponse(
                                        responseCode = "500",
                                        description = "Internal server error"
                                )]
        )
        fun createProductWithImages(
                @RequestParam("productData")
                @Parameter(description = "Product data as JSON string", required = true)
                productDataJson: String,
                @RequestParam(value = "images", required = false)
                @Parameter(
                        description = "Product images (optional, max 10 files, 10MB each)",
                        required = false
                )
                images: Array<MultipartFile>?
        ): ResponseEntity<ProductResponse> {
                return try {
                        val objectMapper = ObjectMapper()
                        val request =
                                objectMapper.readValue(
                                        productDataJson,
                                        ProductWithImagesRequest::class.java
                                )
                        logger.info(
                                "Creating product: ${request.name} - ${images?.size ?: 0} images"
                        )
                        val product =
                                if (images != null && images.isNotEmpty()) {
                                        productService.createProductWithImages(request, images)
                                } else {
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
                                        productService.createProduct(basicRequest)
                                }
                        ResponseEntity.status(HttpStatus.CREATED).body(product)
                } catch (e: IllegalArgumentException) {
                        logger.error("Invalid request for product with images: ${e.message}", e)
                        ResponseEntity.badRequest().build()
                } catch (e: Exception) {
                        logger.error("Error creating product with images: ${e.message}", e)
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                }
        }
        @PutMapping("/{id}")
        @Operation(
                summary = "✏️ Atualizar produto existente",
                description =
                        "Atualiza dados de um produto existente. Não inclui upload de imagens.",
                tags = ["CRUD"],
                security = [SecurityRequirement(name = "Bearer")]
        )
        @ApiResponses(
                value =
                        [
                                ApiResponse(
                                        responseCode = "200",
                                        description = "Product updated successfully",
                                        content =
                                                [
                                                        Content(
                                                                mediaType = "application/json",
                                                                schema =
                                                                        Schema(
                                                                                implementation =
                                                                                        ProductResponse::class
                                                                        )
                                                        )]
                                ),
                                ApiResponse(
                                        responseCode = "404",
                                        description = "Product not found"
                                ),
                                ApiResponse(
                                        responseCode = "400",
                                        description = "Invalid request data"
                                ),
                                ApiResponse(
                                        responseCode = "500",
                                        description = "Internal server error"
                                )]
        )
        fun updateProduct(
                @PathVariable @Parameter(description = "Product ID", required = true) id: String,
                @RequestBody
                @Parameter(description = "Product update data", required = true)
                request: UpdateProductRequest
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
        @Operation(
                summary = "🗑️ Deletar produto",
                description = "Remove permanentemente um produto do sistema. Ação irreversível.",
                tags = ["CRUD"],
                security = [SecurityRequirement(name = "Bearer")]
        )
        @ApiResponses(
                value =
                        [
                                ApiResponse(
                                        responseCode = "200",
                                        description = "Product deleted successfully",
                                        content =
                                                [
                                                        Content(
                                                                mediaType = "application/json",
                                                                schema =
                                                                        Schema(
                                                                                implementation =
                                                                                        ProductDeletedResponse::class
                                                                        )
                                                        )]
                                ),
                                ApiResponse(
                                        responseCode = "404",
                                        description = "Product not found"
                                ),
                                ApiResponse(
                                        responseCode = "500",
                                        description = "Internal server error"
                                )]
        )
        fun deleteProduct(
                @PathVariable
                @Parameter(description = "Product ID to delete", required = true)
                id: String
        ): ResponseEntity<ProductDeletedResponse> {
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
        @PostMapping("/{id}/images")
        @Operation(
                summary = "📸 Upload de imagem para produto existente",
                description =
                        "Adiciona uma imagem a um produto já criado. Use 'isPrimary=true' para definir como imagem principal.",
                tags = ["Upload"],
                security = [SecurityRequirement(name = "Bearer")]
        )
        @ApiResponses(
                ApiResponse(responseCode = "200", description = "Imagem enviada com sucesso"),
                ApiResponse(responseCode = "400", description = "Arquivo inválido"),
                ApiResponse(responseCode = "404", description = "Produto não encontrado"),
                ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        )
        fun uploadProductImage(
                @PathVariable @Parameter(description = "ID do produto", required = true) id: String,
                @RequestParam("file")
                @Parameter(description = "Arquivo de imagem (JPG, PNG, WEBP)")
                file: MultipartFile,
                @RequestParam(defaultValue = "false")
                @Parameter(description = "Definir como imagem primária")
                isPrimary: Boolean
        ): ResponseEntity<ImageUploadResponse> {
                return try {
                        logger.info("Uploading image for product: $id, isPrimary: $isPrimary")
                        val response = productService.uploadProductImage(id, file, isPrimary)
                        ResponseEntity.ok(response)
                } catch (e: Exception) {
                        logger.error("Error uploading image for product $id: ${e.message}", e)
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                }
        }
        @DeleteMapping("/{id}/images/{imageId}")
        @Operation(
                summary = "🗑️ Remover imagem do produto",
                description =
                        "Remove uma imagem específica de um produto. Útil para corrigir uploads incorretos.",
                tags = ["Upload"],
                security = [SecurityRequirement(name = "Bearer")]
        )
        fun removeProductImage(
                @PathVariable id: String,
                @PathVariable imageId: String
        ): ResponseEntity<Unit> {
                return try {
                        logger.info("Removing image $imageId from product: $id")
                        productService.removeProductImage(id, imageId)
                        ResponseEntity.ok().build()
                } catch (e: Exception) {
                        logger.error(
                                "Error removing image $imageId from product $id: ${e.message}",
                                e
                        )
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                }
        }
}
