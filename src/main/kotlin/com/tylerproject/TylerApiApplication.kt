package com.tylerproject

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * 🚀 Tyler API - Spring Boot Application
 *
 * Backend moderno para o Projeto Tyler com:
 * - 🏦 Integração PIX via PagBank (oficial)
 * - 🔐 Firebase Authentication & Firestore
 * - 🌐 RESTful API endpoints
 * - 📊 Monitoramento com Actuator
 * - ☁️ Deploy Google Cloud Run
 */
@SpringBootApplication class TylerApiApplication

fun main(args: Array<String>) {
    runApplication<TylerApiApplication>(*args)
}
