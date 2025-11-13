package com.tylerproject.config
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import java.io.ByteArrayInputStream
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
    @Value("\${app.gcp.storage-credentials-json:}")
    private lateinit var storageCredentialsJson: String
    @Value("\${app.gcp.storage-credentials-path:tyler-storage-credentials.json}")
    private lateinit var storageCredentialsPath: String
    @Bean
    fun googleStorageCredentials(): GoogleCredentials {
        return try {
            if (storageCredentialsJson.isNotBlank()) {
                logger.info(
                        "Carregando credenciais Google Cloud Storage do JSON da variável de ambiente"
                )
                GoogleCredentials.fromStream(
                        ByteArrayInputStream(storageCredentialsJson.toByteArray())
                )
            } else {
                try {
                    logger.info(
                            "Carregando credenciais Google Cloud Storage do arquivo: $storageCredentialsPath"
                    )
                    val resource = ClassPathResource(storageCredentialsPath)
                    GoogleCredentials.fromStream(resource.inputStream)
                } catch (e: Exception) {
                    logger.warn(
                            "⚠️ Credenciais GCS não encontradas, usando credenciais padrão do ambiente"
                    )
                    GoogleCredentials.getApplicationDefault()
                }
            }
        } catch (e: Exception) {
            logger.error("Erro ao carregar credenciais Google Cloud Storage: ${e.message}", e)
            try {
                logger.warn("Tentando usar credenciais padrão como último recurso")
                GoogleCredentials.getApplicationDefault()
            } catch (defaultError: Exception) {
                logger.error("Falha ao carregar qualquer credencial: ${defaultError.message}")
                throw e
            }
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
