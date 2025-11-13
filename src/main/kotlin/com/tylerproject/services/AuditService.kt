package com.tylerproject.services
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import com.tylerproject.models.AuditLog
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
@Service
class AuditService {
    private val logger = LoggerFactory.getLogger(AuditService::class.java)
    private val firestore: Firestore by lazy { FirestoreClient.getFirestore() }
    private val collection = "audit_logs"
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    fun logAction(
            entity: String,
            entityId: String,
            action: String,
            before: Any? = null,
            after: Any? = null,
            userId: String? = null,
            userEmail: String? = null,
            ipAddress: String? = null,
            userAgent: String? = null,
            details: Map<String, String> = emptyMap()
    ) {
        try {
            val now =
                    LocalDateTime.now()
                            .atOffset(ZoneOffset.UTC)
                            .format(DateTimeFormatter.ISO_INSTANT)
            val auditId = UUID.randomUUID().toString()
            val auditLog =
                    AuditLog(
                            id = auditId,
                            entity = entity,
                            entityId = entityId,
                            action = action,
                            before = before?.let { json.encodeToString(it) },
                            after = after?.let { json.encodeToString(it) },
                            userId = userId,
                            userEmail = userEmail,
                            ipAddress = ipAddress,
                            userAgent = userAgent,
                            timestamp = now,
                            details = details
                    )
            firestore.collection(collection).document(auditId).set(auditLog).get()
            logger.debug("📝 Audit log criado: $entity.$action ($entityId)")
        } catch (e: Exception) {
            logger.error("❌ Erro ao criar audit log: ${e.message}", e)
        }
    }
    fun getAuditLogs(
            entity: String? = null,
            entityId: String? = null,
            action: String? = null,
            userId: String? = null,
            fromDate: String? = null,
            toDate: String? = null,
            page: Int = 1,
            pageSize: Int = 50
    ): Pair<List<AuditLog>, Int> {
        try {
            var query: com.google.cloud.firestore.Query = firestore.collection(collection)
            if (entity != null) {
                query = query.whereEqualTo("entity", entity)
            }
            if (entityId != null) {
                query = query.whereEqualTo("entityId", entityId)
            }
            if (action != null) {
                query = query.whereEqualTo("action", action)
            }
            if (userId != null) {
                query = query.whereEqualTo("userId", userId)
            }
            if (fromDate != null) {
                query = query.whereGreaterThanOrEqualTo("timestamp", fromDate)
            }
            if (toDate != null) {
                query = query.whereLessThanOrEqualTo("timestamp", toDate)
            }
            val totalDocs = query.get().get().documents.size
            val offset = (page - 1) * pageSize
            val logs =
                    query.orderBy(
                                    "timestamp",
                                    com.google.cloud.firestore.Query.Direction.DESCENDING
                            )
                            .offset(offset)
                            .limit(pageSize)
                            .get()
                            .get()
                            .documents
                            .mapNotNull { it.toObject(AuditLog::class.java) }
            return Pair(logs, totalDocs)
        } catch (e: Exception) {
            logger.error("❌ Erro ao buscar audit logs: ${e.message}", e)
            return Pair(emptyList(), 0)
        }
    }
    fun getEntityHistory(entity: String, entityId: String): List<AuditLog> {
        return try {
            firestore
                    .collection(collection)
                    .whereEqualTo("entity", entity)
                    .whereEqualTo("entityId", entityId)
                    .orderBy("timestamp", com.google.cloud.firestore.Query.Direction.ASCENDING)
                    .get()
                    .get()
                    .documents
                    .mapNotNull { it.toObject(AuditLog::class.java) }
        } catch (e: Exception) {
            logger.error("❌ Erro ao buscar histórico da entidade: ${e.message}", e)
            emptyList()
        }
    }
    fun getUserActions(userId: String, limit: Int = 100): List<AuditLog> {
        return try {
            firestore
                    .collection(collection)
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", com.google.cloud.firestore.Query.Direction.DESCENDING)
                    .limit(limit)
                    .get()
                    .get()
                    .documents
                    .mapNotNull { it.toObject(AuditLog::class.java) }
        } catch (e: Exception) {
            logger.error("❌ Erro ao buscar ações do usuário: ${e.message}", e)
            emptyList()
        }
    }
    fun getRecentPayments(limit: Int = 20): List<AuditLog> {
        return try {
            firestore
                    .collection(collection)
                    .whereIn("action", listOf("PAYMENT_STATUS_UPDATE", "TICKETS_PAID"))
                    .whereIn("entity", listOf("Order", "Donation", "Raffle"))
                    .orderBy("timestamp", com.google.cloud.firestore.Query.Direction.DESCENDING)
                    .limit(limit)
                    .get()
                    .get()
                    .documents
                    .mapNotNull { it.toObject(AuditLog::class.java) }
        } catch (e: Exception) {
            logger.error("❌ Erro ao buscar pagamentos recentes: ${e.message}", e)
            emptyList()
        }
    }
    fun getTransparencyReport(fromDate: String? = null, toDate: String? = null): Map<String, Any> {
        return try {
            var query =
                    firestore
                            .collection(collection)
                            .whereIn(
                                    "action",
                                    listOf(
                                            "PAYMENT_STATUS_UPDATE",
                                            "AMOUNT_INCREMENT",
                                            "TICKETS_PAID"
                                    )
                            )
            if (fromDate != null) {
                query = query.whereGreaterThanOrEqualTo("timestamp", fromDate)
            }
            if (toDate != null) {
                query = query.whereLessThanOrEqualTo("timestamp", toDate)
            }
            val logs =
                    query.orderBy(
                                    "timestamp",
                                    com.google.cloud.firestore.Query.Direction.DESCENDING
                            )
                            .get()
                            .get()
                            .documents
                            .mapNotNull { it.toObject(AuditLog::class.java) }
            val donationLogs =
                    logs.filter { it.entity == "Donation" && it.action == "PAYMENT_STATUS_UPDATE" }
            val orderLogs =
                    logs.filter { it.entity == "Order" && it.action == "PAYMENT_STATUS_UPDATE" }
            val raffleLogs = logs.filter { it.entity == "Raffle" && it.action == "TICKETS_PAID" }
            val goalLogs = logs.filter { it.entity == "Goal" && it.action == "AMOUNT_INCREMENT" }
            mapOf(
                    "period" to mapOf("from" to fromDate, "to" to toDate),
                    "summary" to
                            mapOf(
                                    "totalTransactions" to logs.size,
                                    "donations" to donationLogs.size,
                                    "orders" to orderLogs.size,
                                    "raffleTickets" to raffleLogs.size,
                                    "goalUpdates" to goalLogs.size
                            ),
                    "transactions" to logs.take(50), // Últimas 50 transações
                    "lastUpdated" to
                            LocalDateTime.now()
                                    .atOffset(ZoneOffset.UTC)
                                    .format(DateTimeFormatter.ISO_INSTANT)
            )
        } catch (e: Exception) {
            logger.error("❌ Erro ao gerar relatório de transparência: ${e.message}", e)
            mapOf(
                    "error" to "Erro ao gerar relatório",
                    "message" to (e.message ?: "Erro desconhecido")
            )
        }
    }
    fun cleanOldLogs(daysOld: Int = 365): Int {
        return try {
            val cutoffDate =
                    LocalDateTime.now()
                            .minusDays(daysOld.toLong())
                            .atOffset(ZoneOffset.UTC)
                            .format(DateTimeFormatter.ISO_INSTANT)
            val oldLogs =
                    firestore
                            .collection(collection)
                            .whereLessThan("timestamp", cutoffDate)
                            .get()
                            .get()
            var deletedCount = 0
            oldLogs.documents.forEach { doc ->
                doc.reference.delete().get()
                deletedCount++
            }
            logger.info(
                    "🧹 Limpeza de audit logs: $deletedCount registros removidos (> $daysOld dias)"
            )
            deletedCount
        } catch (e: Exception) {
            logger.error("❌ Erro na limpeza de audit logs: ${e.message}", e)
            0
        }
    }
}
