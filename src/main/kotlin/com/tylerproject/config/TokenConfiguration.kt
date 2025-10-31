package com.tylerproject.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 🔐 Configuração de Tokens PagBank
 *
 * Bean único que sempre pega da variável de ambiente PAGBANK_TOKEN.
 *
 * Como a variável é populada:
 * - 🛠️ Local: Via YAML (hardcoded para desenvolvimento)
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
    fun pagBankToken(@Value("\${PAGBANK_TOKEN}") token: String): String {
        return token
    }
}
