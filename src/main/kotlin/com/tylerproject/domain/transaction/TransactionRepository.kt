package com.tylerproject.domain.transaction

import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Query
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

interface TransactionRepository {
    fun save(transaction: Transaction): Transaction
    fun update(transactionId: String, updates: Map<String, Any>): Boolean
    fun findById(id: String): Transaction?
    fun findByPaymentId(paymentId: String): Transaction?
    fun findByUserId(userId: String, limit: Int? = null, cursor: String? = null): Pair<List<Transaction>, String?>
    fun findByUserIdAndType(userId: String, type: TransactionType, limit: Int? = null): List<Transaction>
    fun findByUserIdAndStatus(userId: String, status: TransactionStatus, limit: Int? = null): List<Transaction>
    fun findByTargetId(targetId: String): List<Transaction>
    fun findAll(limit: Int? = null, cursor: String? = null): Pair<List<Transaction>, String?>
}

@Repository
class FirestoreTransactionRepository(private val firestore: Firestore) : TransactionRepository {
    
    private val logger = LoggerFactory.getLogger(FirestoreTransactionRepository::class.java)
    private val collection = firestore.collection("transactions")
    
    override fun save(transaction: Transaction): Transaction {
        return try {
            val docRef = if (transaction.id.isBlank()) {
                collection.document()
            } else {
                collection.document(transaction.id)
            }
            
            val transactionWithId = transaction.copy(id = docRef.id)
            val dto = TransactionFirestoreDto.fromDomain(transactionWithId)
            docRef.set(dto).get()
            
            logger.info("Transaction saved: ${transactionWithId.id} - ${transactionWithId.type}")
            transactionWithId
        } catch (e: Exception) {
            logger.error("Error saving transaction: ${e.message}", e)
            throw RuntimeException("Failed to save transaction", e)
        }
    }
    
    override fun update(transactionId: String, updates: Map<String, Any>): Boolean {
        return try {
            collection.document(transactionId).update(updates).get()
            logger.info("Transaction updated: $transactionId")
            true
        } catch (e: Exception) {
            logger.error("Error updating transaction: ${e.message}", e)
            false
        }
    }
    
    override fun findById(id: String): Transaction? {
        return try {
            val snapshot = collection.document(id).get().get()
            if (snapshot.exists()) {
                val dto = snapshot.toObject(TransactionFirestoreDto::class.java)
                dto?.toDomain()
            } else {
                null
            }
        } catch (e: Exception) {
            logger.error("Error finding transaction by id $id: ${e.message}", e)
            null
        }
    }
    
    override fun findByPaymentId(paymentId: String): Transaction? {
        return try {
            val query = collection
                .whereEqualTo("payment.paymentId", paymentId)
                .limit(1)
            
            val snapshot = query.get().get()
            val dto = snapshot.documents.firstOrNull()?.toObject(TransactionFirestoreDto::class.java)
            dto?.toDomain()
        } catch (e: Exception) {
            logger.error("Error finding transaction by paymentId $paymentId: ${e.message}", e)
            null
        }
    }
    
    override fun findByUserId(userId: String, limit: Int?, cursor: String?): Pair<List<Transaction>, String?> {
        return try {
            var query: Query = collection
                .whereEqualTo("customer.userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
            
            if (limit != null) {
                query = query.limit(limit)
            }
            
            if (cursor != null) {
                val cursorDoc = collection.document(cursor).get().get()
                if (cursorDoc.exists()) {
                    query = query.startAfter(cursorDoc)
                }
            }
            
            val snapshot = query.get().get()
            val transactions = snapshot.documents.mapNotNull { 
                it.toObject(TransactionFirestoreDto::class.java)?.toDomain() 
            }
            
            val nextCursor = if (transactions.isNotEmpty() && limit != null && transactions.size >= limit) {
                transactions.last().id
            } else {
                null
            }
            
            Pair(transactions, nextCursor)
        } catch (e: Exception) {
            logger.error("Error finding transactions by userId $userId: ${e.message}", e)
            Pair(emptyList(), null)
        }
    }
    
    override fun findByUserIdAndType(userId: String, type: TransactionType, limit: Int?): List<Transaction> {
        return try {
            var query = collection
                .whereEqualTo("customer.userId", userId)
                .whereEqualTo("type", type.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)
            
            if (limit != null) {
                query = query.limit(limit)
            }
            
            val snapshot = query.get().get()
            snapshot.documents.mapNotNull { it.toObject(TransactionFirestoreDto::class.java)?.toDomain() }
        } catch (e: Exception) {
            logger.error("Error finding transactions by userId and type: ${e.message}", e)
            emptyList()
        }
    }
    
    override fun findByUserIdAndStatus(userId: String, status: TransactionStatus, limit: Int?): List<Transaction> {
        return try {
            var query = collection
                .whereEqualTo("customer.userId", userId)
                .whereEqualTo("status", status.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)
            
            if (limit != null) {
                query = query.limit(limit)
            }
            
            val snapshot = query.get().get()
            snapshot.documents.mapNotNull { it.toObject(TransactionFirestoreDto::class.java)?.toDomain() }
        } catch (e: Exception) {
            logger.error("Error finding transactions by userId and status: ${e.message}", e)
            emptyList()
        }
    }
    
    override fun findByTargetId(targetId: String): List<Transaction> {
        return try {
            val query = collection
                .whereEqualTo("targetId", targetId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
            
            val snapshot = query.get().get()
            snapshot.documents.mapNotNull { it.toObject(TransactionFirestoreDto::class.java)?.toDomain() }
        } catch (e: Exception) {
            logger.error("Error finding transactions by targetId $targetId: ${e.message}", e)
            emptyList()
        }
    }
    
    override fun findAll(limit: Int?, cursor: String?): Pair<List<Transaction>, String?> {
        return try {
            var query: Query = collection
                .orderBy("createdAt", Query.Direction.DESCENDING)
            
            if (limit != null) {
                query = query.limit(limit)
            }
            
            if (cursor != null) {
                val cursorDoc = collection.document(cursor).get().get()
                if (cursorDoc.exists()) {
                    query = query.startAfter(cursorDoc)
                }
            }
            
            val snapshot = query.get().get()
            val transactions = snapshot.documents.mapNotNull { 
                it.toObject(TransactionFirestoreDto::class.java)?.toDomain() 
            }
            
            val nextCursor = if (transactions.isNotEmpty() && limit != null && transactions.size >= limit) {
                transactions.last().id
            } else {
                null
            }
            
            Pair(transactions, nextCursor)
        } catch (e: Exception) {
            logger.error("Error finding all transactions: ${e.message}", e)
            Pair(emptyList(), null)
        }
    }
}
