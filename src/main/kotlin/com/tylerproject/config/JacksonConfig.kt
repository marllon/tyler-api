package com.tylerproject.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class JacksonConfig {
    @Bean
    fun objectMapper():ObjectMapper = ObjectMapper().registerModule(JavaTimeModule)
}
