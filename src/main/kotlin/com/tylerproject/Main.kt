package com.tylerproject

import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.tylerproject.config.Config
import com.tylerproject.handlers.*
import com.tylerproject.middleware.CorsMiddleware
import kotlinx.serialization.json.Json

class Main : HttpFunction {
    companion object {
        private var initialized = false
        private lateinit var config: Config
        val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = false
        }

        fun initialize() {
            if (!initialized) {
                // Initialize Firebase
                val options =
                        FirebaseOptions.builder()
                                .setProjectId(
                                        System.getenv("FIREBASE_PROJECT_ID") ?: "projeto-tyler"
                                )
                                .build()

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options)
                }

                config = Config()
                initialized = true
            }
        }
    }

    override fun service(request: HttpRequest, response: HttpResponse) {
        initialize()

        // CORS
        CorsMiddleware.handle(request, response)
        if (request.method == "OPTIONS") {
            response.setStatusCode(204)
            return
        }

        try {
            val path = request.path
            val method = request.method

            when {
                // Public endpoints
                path.startsWith("/api/products") -> ProductsHandler.handle(request, response)
                path.startsWith("/api/goals") -> GoalsHandler.handle(request, response)
                path.startsWith("/api/raffles") -> RafflesHandler.handle(request, response)
                path.startsWith("/api/events") -> EventsHandler.handle(request, response)
                path.startsWith("/api/checkout") -> CheckoutHandler.handle(request, response)
                path.startsWith("/api/contact") -> ContactHandler.handle(request, response)

                // Webhooks
                path.startsWith("/api/webhooks/payments") ->
                        WebhookHandler.handle(request, response)

                // Admin endpoints
                path.startsWith("/admin/login") -> AdminAuthHandler.handleLogin(request, response)
                path.startsWith("/admin/products") -> AdminProductsHandler.handle(request, response)
                path.startsWith("/admin/goals") -> AdminGoalsHandler.handle(request, response)
                path.startsWith("/admin/raffles") -> AdminRafflesHandler.handle(request, response)
                path.startsWith("/admin/events") -> AdminEventsHandler.handle(request, response)
                path.startsWith("/admin/stats") -> AdminStatsHandler.handle(request, response)
                path.startsWith("/admin/orders") -> AdminOrdersHandler.handle(request, response)
                path.startsWith("/admin/donations") ->
                        AdminDonationsHandler.handle(request, response)
                else -> {
                    response.setStatusCode(404)
                    response.writer.write(
                            """{"success": false, "error": "Endpoint não encontrado"}"""
                    )
                }
            }
        } catch (e: Exception) {
            response.setStatusCode(500)
            response.writer.write("""{"success": false, "error": "${e.message}"}""")
            e.printStackTrace()
        }
    }
}
