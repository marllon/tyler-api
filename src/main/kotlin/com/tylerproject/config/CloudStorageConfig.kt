package com.tylerproject.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource

@Configuration
class CloudStorageConfig {

    private val logger = LoggerFactory.getLogger(CloudStorageConfig::class.java)

    @Value("\${app.gcp.bucket-name}") private lateinit var bucketName: String

    @Value("\${app.gcp.project-id}") private lateinit var projectId: String

    @Value("\${app.gcp.storage-credentials-path:tyler-storage-credentials.json}")
    private lateinit var storageCredentialsPath: String

    @Bean
    fun googleStorageCredentials(): GoogleCredentials {
        return try {
            logger.info("Loading Google Cloud Storage credentials from: $storageCredentialsPath")
            val resource = ClassPathResource(storageCredentialsPath)
            GoogleCredentials.fromStream(resource.inputStream)
        } catch (e: Exception) {
            logger.error("Error loading Google Cloud Storage credentials: ${e.message}", e)
            throw e
        }
    }

    @Bean
    fun storage(googleStorageCredentials: GoogleCredentials): Storage {
        return StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(googleStorageCredentials)
                .build()
                .service
    }

    @Bean
    fun bucketName(): String {
        return bucketName
    }
}
