package com.tylerproject.config
import com.tylerproject.providers.PagBankProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
@Configuration
class TokenConfiguration {
    @Bean("pagbankToken")
    fun pagBankToken(@Value("\${pagbank.token:}") token: String): String {
        return token
    }
    @Bean
    fun pagBankProvider(
            @Value("\${pagbank.token:}") token: String,
            applicationContext: ApplicationContext
    ): PagBankProvider {
        if (token.isBlank()) {
            println("⚠️ PAGBANK_TOKEN está vazio, PagBank Provider será criado mas pode falhar em operações reais")
        }
        return PagBankProvider(token, applicationContext)
    }
}
