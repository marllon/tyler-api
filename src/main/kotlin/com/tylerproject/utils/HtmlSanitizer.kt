package com.tylerproject.utils

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Utilitário para sanitização e validação de HTML em descrições de produtos.
 *
 * Permite tags HTML seguras, quebras de linha, imagens inline (base64 ou URLs), mas remove scripts,
 * iframes e outros elementos perigosos.
 */
@Component
class HtmlSanitizer {

    private val logger = LoggerFactory.getLogger(HtmlSanitizer::class.java)

    companion object {
        // Tags HTML permitidas para formatação de texto
        private val ALLOWED_TAGS =
                setOf(
                        "p",
                        "br",
                        "strong",
                        "b",
                        "em",
                        "i",
                        "u",
                        "s",
                        "strike",
                        "h1",
                        "h2",
                        "h3",
                        "h4",
                        "h5",
                        "h6",
                        "ul",
                        "ol",
                        "li",
                        "a",
                        "img",
                        "div",
                        "span",
                        "blockquote",
                        "code",
                        "pre",
                        "table",
                        "thead",
                        "tbody",
                        "tr",
                        "th",
                        "td",
                        "hr"
                )

        // Atributos permitidos por tag
        private val ALLOWED_ATTRIBUTES =
                mapOf(
                        "a" to setOf("href", "title", "target", "rel"),
                        "img" to setOf("src", "alt", "title", "width", "height", "style"),
                        "div" to setOf("class", "style"),
                        "span" to setOf("class", "style"),
                        "p" to setOf("class", "style"),
                        "td" to setOf("colspan", "rowspan", "style"),
                        "th" to setOf("colspan", "rowspan", "style")
                )

        // Protocolos permitidos para URLs
        private val ALLOWED_PROTOCOLS = setOf("http", "https", "data")

        // Tamanho máximo para imagens base64 (500KB)
        private const val MAX_BASE64_IMAGE_SIZE = 500 * 1024
    }

    /**
     * Sanitiza HTML removendo elementos perigosos mas preservando formatação.
     *
     * @param html O HTML a ser sanitizado
     * @return HTML seguro para armazenamento e exibição
     */
    fun sanitize(html: String?): String {
        if (html.isNullOrBlank()) return ""

        var sanitized = html

        // Remover scripts e elementos perigosos
        sanitized = removeUnsafeElements(sanitized)

        // Validar e sanitizar tags permitidas
        sanitized = sanitizeAllowedTags(sanitized)

        // Validar imagens base64 (tamanho)
        sanitized = validateBase64Images(sanitized)

        // Sanitizar atributos de estilo (remover JavaScript)
        sanitized = sanitizeStyleAttributes(sanitized)

        return sanitized.trim()
    }

    /** Remove elementos HTML perigosos (script, iframe, object, embed, etc.) */
    private fun removeUnsafeElements(html: String): String {
        val unsafeTags =
                listOf(
                        "script",
                        "iframe",
                        "object",
                        "embed",
                        "applet",
                        "form",
                        "input",
                        "button",
                        "select",
                        "textarea",
                        "link",
                        "meta",
                        "base"
                )

        var result = html

        // Remover tags perigosas (case-insensitive)
        unsafeTags.forEach { tag ->
            val pattern =
                    Regex(
                            "<$tag[^>]*>.*?</$tag>",
                            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
                    )
            result = pattern.replace(result, "")

            // Remover tags self-closing
            val selfClosingPattern = Regex("<$tag[^>]*/>", RegexOption.IGNORE_CASE)
            result = selfClosingPattern.replace(result, "")
        }

        // Remover event handlers (onclick, onload, onerror, etc.)
        val onEventPattern1 = Regex("\\s*on\\w+\\s*=\\s*[\"'][^\"']*[\"']", RegexOption.IGNORE_CASE)
        result = onEventPattern1.replace(result, "")

        val onEventPattern2 = Regex("\\s*on\\w+\\s*=\\s*\\S+", RegexOption.IGNORE_CASE)
        result = onEventPattern2.replace(result, "")

        // Remover javascript: em URLs
        val jsPattern = Regex("javascript:", RegexOption.IGNORE_CASE)
        result = jsPattern.replace(result, "")

        // Remover data:text/html
        val dataHtmlPattern = Regex("data:text/html", RegexOption.IGNORE_CASE)
        result = dataHtmlPattern.replace(result, "")

        return result
    }

    /** Valida e sanitiza tags permitidas, removendo tags não autorizadas */
    private fun sanitizeAllowedTags(html: String): String {
        // Esta é uma implementação básica
        // Para produção, considere usar uma biblioteca como jsoup ou OWASP Java HTML Sanitizer

        var result = html

        // Remover tags não permitidas preservando conteúdo
        val tagPattern = Regex("<(/?)([a-zA-Z][a-zA-Z0-9]*)[^>]*>")

        result =
                tagPattern.replace(result) { matchResult ->
                    val closingSlash = matchResult.groupValues[1]
                    val tagName = matchResult.groupValues[2].lowercase()
                    val fullMatch = matchResult.value

                    if (ALLOWED_TAGS.contains(tagName)) {
                        // Tag permitida - validar atributos
                        sanitizeTagAttributes(fullMatch, tagName)
                    } else {
                        // Tag não permitida - remover mas manter conteúdo
                        ""
                    }
                }

        return result
    }

    /** Sanitiza atributos de uma tag específica */
    private fun sanitizeTagAttributes(tag: String, tagName: String): String {
        val allowedAttrs = ALLOWED_ATTRIBUTES[tagName] ?: emptySet()

        if (allowedAttrs.isEmpty()) {
            // Tag sem atributos permitidos - retornar tag básica
            return if (tag.startsWith("</")) {
                "</$tagName>"
            } else {
                "<$tagName>"
            }
        }

        // Permitir todos os atributos por enquanto
        // Em produção, você deve validar cada atributo individualmente
        return tag
    }

    /** Valida imagens base64 para garantir que não excedem tamanho máximo */
    private fun validateBase64Images(html: String): String {
        val base64Pattern = Regex("data:image/[^;]+;base64,([A-Za-z0-9+/=]+)")

        return base64Pattern.replace(html) { matchResult ->
            val base64Data = matchResult.groupValues[1]
            val estimatedSize = (base64Data.length * 3) / 4 // Tamanho aproximado em bytes

            if (estimatedSize > MAX_BASE64_IMAGE_SIZE) {
                logger.warn(
                        "Base64 image too large: $estimatedSize bytes (max: $MAX_BASE64_IMAGE_SIZE)"
                )
                // Substituir por placeholder
                "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCI+PHRleHQ+SW1hZ2VtIG11aXRvIGdyYW5kZTwvdGV4dD48L3N2Zz4="
            } else {
                matchResult.value
            }
        }
    }

    /** Sanitiza atributos style para remover JavaScript */
    private fun sanitizeStyleAttributes(html: String): String {
        var result = html

        // Remover expression() do Internet Explorer
        val expressionPattern = Regex("expression\\s*\\([^)]*\\)", RegexOption.IGNORE_CASE)
        result = expressionPattern.replace(result, "")

        // Remover comportamentos CSS perigosos
        val behaviorPattern = Regex("behavior\\s*:", RegexOption.IGNORE_CASE)
        result = behaviorPattern.replace(result, "")

        val mozBindingPattern = Regex("-moz-binding\\s*:", RegexOption.IGNORE_CASE)
        result = mozBindingPattern.replace(result, "")

        // Remover import em CSS
        val importPattern = Regex("@import", RegexOption.IGNORE_CASE)
        result = importPattern.replace(result, "")

        return result
    }

    /** Valida se o HTML sanitizado está dentro dos limites aceitáveis */
    fun validate(html: String): ValidationResult {
        val sanitized = sanitize(html)

        // Limite de tamanho total (1MB)
        val maxSize = 1024 * 1024
        if (sanitized.length > maxSize) {
            return ValidationResult(
                    valid = false,
                    message = "Descrição muito grande. Máximo: ${maxSize / 1024}KB",
                    sanitizedHtml = sanitized.take(maxSize)
            )
        }

        // Contar imagens base64
        val base64ImageCount = Regex("data:image/[^;]+;base64,").findAll(sanitized).count()
        if (base64ImageCount > 10) {
            return ValidationResult(
                    valid = false,
                    message = "Muitas imagens inline. Máximo: 10 imagens base64",
                    sanitizedHtml = sanitized
            )
        }

        return ValidationResult(valid = true, message = "HTML válido", sanitizedHtml = sanitized)
    }
}

/** Resultado da validação de HTML */
data class ValidationResult(val valid: Boolean, val message: String, val sanitizedHtml: String)
