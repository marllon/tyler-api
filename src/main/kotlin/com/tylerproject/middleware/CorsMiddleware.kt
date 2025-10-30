package com.tylerproject.middleware

import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import com.tylerproject.config.Config

object CorsMiddleware {
    fun handle(request: HttpRequest, response: HttpResponse) {
        val origin = request.getFirstHeader("Origin").orElse("*")

        // Verificar se origem é permitida
        val allowedOrigin =
                if (Config.allowedOrigins.contains(origin) || Config.allowedOrigins.contains("*")) {
                    origin
                } else {
                    Config.allowedOrigins.firstOrNull() ?: "*"
                }

        response.appendHeader("Access-Control-Allow-Origin", allowedOrigin)
        response.appendHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        response.appendHeader(
                "Access-Control-Allow-Headers",
                "Content-Type, Authorization, X-Requested-With"
        )
        response.appendHeader("Access-Control-Max-Age", "3600")
        response.appendHeader("Access-Control-Allow-Credentials", "true")
    }
}
