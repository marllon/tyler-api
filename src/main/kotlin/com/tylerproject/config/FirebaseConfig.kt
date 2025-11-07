package com.tylerproject.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import java.io.FileInputStream
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 🔐 Firebase Configuration
 *
 * Configuração do Firebase Admin SDK para:
 * - Authentication (Firebase Auth)
 * - Firestore (Database)
 * - Cloud Storage (Futuramente)
 */
@Configuration
class FirebaseConfig {

    private val logger = LoggerFactory.getLogger(FirebaseConfig::class.java)

    @Value("\${firebase.service-account-key:config/firebase-admin-sdk.json}")
    private lateinit var serviceAccountKeyPath: String

    @Bean
    fun firebaseApp(): FirebaseApp? {
        return try {
            if (FirebaseApp.getApps().isEmpty()) {
                val serviceAccount = FileInputStream(serviceAccountKeyPath)
                val options =
                        FirebaseOptions.builder()
                                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                                .setProjectId(
                                        "tyler-project-dev"
                                ) // TODO: Usar variável de ambiente
                                .build()

                val app = FirebaseApp.initializeApp(options)
                logger.info("✅ Firebase inicializado com sucesso")
                app
            } else {
                FirebaseApp.getInstance()
            }
        } catch (e: Exception) {
            logger.error("❌ Erro ao inicializar Firebase: ${e.message}", e)
            null
        }
    }

    @Bean
    fun firebaseAuth(firebaseApp: FirebaseApp?): FirebaseAuth? {
        return try {
            if (firebaseApp != null) {
                FirebaseAuth.getInstance(firebaseApp)
            } else {
                logger.warn("⚠️ Firebase não inicializado, autenticação não disponível")
                null
            }
        } catch (e: Exception) {
            logger.error("❌ Erro ao inicializar Firebase Auth: ${e.message}", e)
            null
        }
    }
}
