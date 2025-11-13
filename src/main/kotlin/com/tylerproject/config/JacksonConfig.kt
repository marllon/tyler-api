package com.tylerproject.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class JacksonConfig {

    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerModule(
                    KotlinModule.Builder()
                            .withReflectionCacheSize(512)
                            .configure(KotlinFeature.NullToEmptyCollection, false)
                            .configure(KotlinFeature.NullToEmptyMap, false)
                            .configure(KotlinFeature.NullIsSameAsDefault, true)
                            .configure(KotlinFeature.SingletonSupport, false)
                            .configure(KotlinFeature.StrictNullChecks, false)
                            .build()
            )
        }
    }
}
