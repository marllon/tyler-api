package com.tylerproject.handlers

import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import com.google.firebase.cloud.FirestoreClient
import com.tylerproject.Main
import com.tylerproject.models.*
import kotlinx.serialization.encodeToString

object ProductsHandler {
    fun handle(request: HttpRequest, response: HttpResponse) {
        val db = FirestoreClient.getFirestore()

        when (request.method) {
            "GET" -> {
                // Lista todos produtos ativos
                val products =
                        db.collection("products")
                                .whereEqualTo("active", true)
                                .get()
                                .get()
                                .documents
                                .map { it.toObject(Product::class.java).copy(id = it.id) }

                response.setStatusCode(200)
                response.writer.write(
                        Main.json.encodeToString(ApiResponse(success = true, data = products))
                )
            }
            else -> {
                response.setStatusCode(405)
                response.writer.write("""{"success": false, "error": "Method not allowed"}""")
            }
        }
    }
}

object GoalsHandler {
    fun handle(request: HttpRequest, response: HttpResponse) {
        val db = FirestoreClient.getFirestore()

        when (request.method) {
            "GET" -> {
                val goals =
                        db.collection("goals")
                                .whereEqualTo("active", true)
                                .get()
                                .get()
                                .documents
                                .map { it.toObject(Goal::class.java).copy(id = it.id) }

                response.setStatusCode(200)
                response.writer.write(
                        Main.json.encodeToString(ApiResponse(success = true, data = goals))
                )
            }
            else -> {
                response.setStatusCode(405)
                response.writer.write("""{"success": false, "error": "Method not allowed"}""")
            }
        }
    }
}

object RafflesHandler {
    fun handle(request: HttpRequest, response: HttpResponse) {
        val db = FirestoreClient.getFirestore()

        when (request.method) {
            "GET" -> {
                val raffles =
                        db.collection("raffles")
                                .whereIn("status", listOf("ACTIVE", "ENDED", "DRAWN"))
                                .get()
                                .get()
                                .documents
                                .map { it.toObject(Raffle::class.java).copy(id = it.id) }

                response.setStatusCode(200)
                response.writer.write(
                        Main.json.encodeToString(ApiResponse(success = true, data = raffles))
                )
            }
            else -> {
                response.setStatusCode(405)
                response.writer.write("""{"success": false, "error": "Method not allowed"}""")
            }
        }
    }
}

object EventsHandler {
    fun handle(request: HttpRequest, response: HttpResponse) {
        val db = FirestoreClient.getFirestore()

        when (request.method) {
            "GET" -> {
                val events =
                        db.collection("events")
                                .orderBy(
                                        "date",
                                        com.google.cloud.firestore.Query.Direction.DESCENDING
                                )
                                .get()
                                .get()
                                .documents
                                .map { it.toObject(Event::class.java).copy(id = it.id) }

                response.setStatusCode(200)
                response.writer.write(
                        Main.json.encodeToString(ApiResponse(success = true, data = events))
                )
            }
            else -> {
                response.setStatusCode(405)
                response.writer.write("""{"success": false, "error": "Method not allowed"}""")
            }
        }
    }
}

object CheckoutHandler {
    fun handle(request: HttpRequest, response: HttpResponse) {
        // Implementação completa do checkout será feita posteriormente
        // Por ora, retorna estrutura básica
        response.setStatusCode(501)
        response.writer.write("""{"success": false, "error": "Not implemented yet"}""")
    }
}

object WebhookHandler {
    fun handle(request: HttpRequest, response: HttpResponse) {
        // Implementação completa do webhook será feita posteriormente
        response.setStatusCode(501)
        response.writer.write("""{"success": false, "error": "Not implemented yet"}""")
    }
}

object ContactHandler {
    fun handle(request: HttpRequest, response: HttpResponse) {
        // Implementação completa do contato será feita posteriormente
        response.setStatusCode(501)
        response.writer.write("""{"success": false, "error": "Not implemented yet"}""")
    }
}

object AdminAuthHandler {
    fun handleLogin(request: HttpRequest, response: HttpResponse) {
        // Implementação básica de autenticação
        response.setStatusCode(501)
        response.writer.write("""{"success": false, "error": "Not implemented yet"}""")
    }
}

object AdminProductsHandler {
    fun handle(request: HttpRequest, response: HttpResponse) {
        response.setStatusCode(501)
        response.writer.write(
                """{"success": false, "error": "Admin endpoint - not implemented yet"}"""
        )
    }
}

object AdminGoalsHandler {
    fun handle(request: HttpRequest, response: HttpResponse) {
        response.setStatusCode(501)
        response.writer.write(
                """{"success": false, "error": "Admin endpoint - not implemented yet"}"""
        )
    }
}

object AdminRafflesHandler {
    fun handle(request: HttpRequest, response: HttpResponse) {
        response.setStatusCode(501)
        response.writer.write(
                """{"success": false, "error": "Admin endpoint - not implemented yet"}"""
        )
    }
}

object AdminEventsHandler {
    fun handle(request: HttpRequest, response: HttpResponse) {
        response.setStatusCode(501)
        response.writer.write(
                """{"success": false, "error": "Admin endpoint - not implemented yet"}"""
        )
    }
}

object AdminStatsHandler {
    fun handle(request: HttpRequest, response: HttpResponse) {
        response.setStatusCode(501)
        response.writer.write(
                """{"success": false, "error": "Admin endpoint - not implemented yet"}"""
        )
    }
}

object AdminOrdersHandler {
    fun handle(request: HttpRequest, response: HttpResponse) {
        response.setStatusCode(501)
        response.writer.write(
                """{"success": false, "error": "Admin endpoint - not implemented yet"}"""
        )
    }
}

object AdminDonationsHandler {
    fun handle(request: HttpRequest, response: HttpResponse) {
        response.setStatusCode(501)
        response.writer.write(
                """{"success": false, "error": "Admin endpoint - not implemented yet"}"""
        )
    }
}
