package com.tylerproject.config

import com.tylerproject.providers.PagBankProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * ⚙️ Configuração da Aplicação Spring Boot
 *
 * Configurações incluem:
 * - CORS para desenvolvimento
 * - Beans dos providers de pagamento
 * - Configurações de segurança
 */
@Configuration
class AppConfig : WebMvcConfigurer {

    /** 🌐 Configuração CORS */
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
    }

    /** 🏦 Bean do PagBank Provider - Token via Profile */
    @Bean
    fun pagBankProvider(@Qualifier("pagbankToken") token: String): PagBankProvider {
        return PagBankProvider(token)
    }
}
