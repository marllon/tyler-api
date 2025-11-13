package com.tylerproject.config
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
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
                                                """
                                        🚀 **API completa para sistema de doações e eventos beneficentes**
                                        ## Características:
                                        - **Clean Architecture**: Arquitetura limpa e modular
                                        - **Firebase**: Autenticação e Firestore NoSQL
                                        - **Google Cloud Storage**: Upload seguro de imagens
                                        - **Cursor Pagination**: Performance otimizada para NoSQL
                                        - **PIX Integration**: Pagamentos via PIX
                                        ## Autenticação:
                                        Para endpoints protegidos, use o botão "Authorize" e insira o token JWT do Firebase.
                                        **Formato**: `Bearer your-firebase-jwt-token`
                                        """.trimIndent()
                                        )
                                        .version("2.1.0")
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
                                                .description("🔧 Desenvolvimento Local"),
                                        Server().url("https://tyler-api.herokuapp.com")
                                                .description("🚀 Produção")
                                )
                        )
                        .components(
                                Components()
                                        .addSecuritySchemes(
                                                "Bearer",
                                                SecurityScheme()
                                                        .type(SecurityScheme.Type.HTTP)
                                                        .scheme("bearer")
                                                        .bearerFormat("JWT")
                                                        .name("Authorization")
                                                        .description(
                                                                "Token JWT do Firebase Auth. Formato: Bearer your-token"
                                                        )
                                        )
                        )
                        .addSecurityItem(SecurityRequirement().addList("Bearer"))
        }
}
