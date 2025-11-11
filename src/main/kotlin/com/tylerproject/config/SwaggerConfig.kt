package com.tylerproject.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Value("\${server.port:8080}") private val serverPort: String = "8080"

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
                .info(
                        Info().title("Tyler API")
                                .description(
                                        "API completa para sistema de doações e eventos beneficentes com Clean Architecture"
                                )
                                .version("2.0.0")
                                .contact(
                                        Contact()
                                                .name("Tyler Project Team")
                                                .email("contato@tyler.org")
                                                .url("https://tyler.org")
                                )
                                .license(
                                        License()
                                                .name("MIT License")
                                                .url("https://opensource.org/licenses/MIT")
                                )
                )
                .servers(
                        listOf(
                                Server().url("http://localhost:$serverPort")
                                        .description("Desenvolvimento Local"),
                                Server().url("https://tyler-api.herokuapp.com")
                                        .description("Produção")
                        )
                )
    }
}
