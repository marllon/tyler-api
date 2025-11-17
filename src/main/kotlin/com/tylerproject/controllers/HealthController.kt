package com.tylerproject.controllers
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import java.time.LocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.web.bind.annotation.*
@RestController
@RequestMapping("/api")
@Tag(name = "🏥 Health", description = "Endpoints de monitoramento e status da API")
class HealthController : HealthIndicator {
    private val logger = LoggerFactory.getLogger(HealthController::class.java)
    @GetMapping("/health")
    @Operation(
            summary = "🏥 Health check da API",
            description =
                    "Verifica se a API está funcionando corretamente e retorna informações do sistema",
            tags = ["Monitoramento"]
    )
    @ApiResponses(
            value =
                    [
                            ApiResponse(
                                    responseCode = "200",
                                    description = "✅ API funcionando normalmente",
                                    content =
                                            [
                                                    Content(
                                                            mediaType = "application/json",
                                                            schema =
                                                                    Schema(
                                                                            example =
                                                                                    """{
                        "status": "healthy",
                        "message": "Tyler API Spring Boot está funcionando perfeitamente! ✅",
                        "timestamp": "2024-01-15T10:30:00",
                        "version": "2.1.0",
                        "environment": "development"
                    }"""
                                                                    )
                                                    )]
                            )]
    )
    fun healthCheck(): Map<String, Any> {
        logger.info("🏥 Health check solicitado")
        return mapOf(
                "status" to "healthy",
                "message" to "Tyler API Spring Boot está funcionando perfeitamente! ✅",
                "timestamp" to LocalDateTime.now().toString(),
                "version" to "2.0.0-spring-boot"
        )
    }
    override fun health(): Health {
        return try {
            Health.up()
                    .withDetail("service", "Tyler API")
                    .withDetail("version", "2.0.0-spring-boot")
                    .withDetail("status", "running")
                    .build()
        } catch (e: Exception) {
            logger.error("Health check failed", e)
            Health.down().withDetail("error", e.message).build()
        }
    }
}
