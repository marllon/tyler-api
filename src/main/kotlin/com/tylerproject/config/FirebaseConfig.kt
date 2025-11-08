package com.tylerproject.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.cloud.FirestoreClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource

@Configuration
class FirebaseConfig {

    private val logger = LoggerFactory.getLogger(FirebaseConfig::class.java)

    @Value("\${firebase.service-account-key:firebase-admin-sdk.json}")
    private lateinit var serviceAccountKeyPath: String

    @Value("\${firebase.project-id:tyler-dev}")
    private lateinit var firebaseProjectId: String

    @Bean
    fun firebaseApp(): FirebaseApp? {
        return try {
            if (FirebaseApp.getApps().isEmpty()) {
                val serviceAccount = ClassPathResource(serviceAccountKeyPath).inputStream
                val options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId(firebaseProjectId)
                    .build()

                val app = FirebaseApp.initializeApp(options)
                logger.info("Firebase inicializado com sucesso - Projeto: $firebaseProjectId")
                app
            } else {
                FirebaseApp.getInstance()
            }
        } catch (e: Exception) {
            logger.error("Erro ao inicializar Firebase: ${e.message}", e)
            null
        }
    }

    @Bean
    fun firebaseAuth(firebaseApp: FirebaseApp?): FirebaseAuth? {
        return try {
            if (firebaseApp != null) {
                FirebaseAuth.getInstance(firebaseApp)
            } else {
                logger.warn("Firebase não inicializado, autenticação não disponível")
                null
            }
        } catch (e: Exception) {
            logger.error("Erro ao inicializar Firebase Auth: ${e.message}", e)
            null
        }
    }

    @Bean
    fun firestore(firebaseApp: FirebaseApp?): Firestore? {
        return try {
            if (firebaseApp != null) {
                FirestoreClient.getFirestore()
            } else {
                logger.warn("Firebase não inicializado, Firestore não disponível")
                null
            }
        } catch (e: Exception) {
            logger.error("Erro ao inicializar Firestore: ${e.message}", e)
            null
        }
    }
}
