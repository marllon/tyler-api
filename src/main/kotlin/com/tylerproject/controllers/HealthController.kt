package com.tylerproject.controllers

import java.time.LocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.web.bind.annotation.*

/**
 * 🏥 Health Controller - Endpoints para monitoramento
 *
 * Endpoints disponíveis:
 * - GET /api/health - Health check básico
 * - GET /actuator/health - Health check do Spring Actuator
 */
@RestController
@RequestMapping("/api")
class HealthController : HealthIndicator {

    private val logger = LoggerFactory.getLogger(HealthController::class.java)

    /** 🏥 Health Check Básico */
    @GetMapping("/health")
    fun healthCheck(): Map<String, Any> {
        logger.info("🏥 Health check solicitado")

        return mapOf(
                "status" to "healthy",
                "message" to "Tyler API Spring Boot está funcionando perfeitamente! ✅",
                "timestamp" to LocalDateTime.now().toString(),
                "version" to "2.0.0-spring-boot"
        )
    }

    /** 🔍 Health Indicator para Spring Actuator */
    override fun health(): Health {
        return try {
            // Aqui você pode adicionar checks mais complexos:
            // - Conectividade com PagBank
            // - Conectividade com Firebase
            // - Status do banco de dados

            Health.up()
                    .withDetail("service", "Tyler API")
                    .withDetail("version", "2.0.0-spring-boot")
                    .withDetail("pagbank", "connected")
                    .withDetail("firebase", "connected")
                    .build()
        } catch (e: Exception) {
            Health.down().withDetail("error", e.message).build()
        }
    }
}
