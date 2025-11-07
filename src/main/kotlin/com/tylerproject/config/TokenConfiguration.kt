package com.tylerproject.config

import com.tylerproject.providers.PagBankProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 🔐 Configuração de Tokens e Providers
 *
 * Bean único que sempre pega da variável de ambiente PAGBANK_TOKEN. Configura PagBankProvider com
 * injeção de dependências.
 *
 * Como a variável é populada:
 * - 🛠️ Local: Via application.yml (hardcoded para desenvolvimento)
 * - 🚀 Produção: Via Secret Manager (injetado no ambiente Cloud Run)
 */
@Configuration
class TokenConfiguration {

    /**
     * 🏦 Token PagBank único
     *
     * A variável PAGBANK_TOKEN será populada diferentemente por ambiente:
     * - Local: application-local.yml define via PAGBANK_TOKEN
     * - Produção: Cloud Run injeta via Secret Manager
     */
    @Bean("pagbankToken")
    fun pagBankToken(@Value("\${pagbank.token}") token: String): String {
        return token
    }

    /** 🏦 PagBank Provider configurado com ApplicationContext */
    @Bean
    fun pagBankProvider(
            @Value("\${pagbank.token}") token: String,
            applicationContext: ApplicationContext
    ): PagBankProvider {
        return PagBankProvider(token, applicationContext)
    }
}
