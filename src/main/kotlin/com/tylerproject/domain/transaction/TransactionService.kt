package com.tylerproject.domain.transaction

import com.tylerproject.providers.PagBankProvider
import com.tylerproject.utils.QrCodeGenerator
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

interface TransactionService {
    /**
     * Cria uma transação e processa o pagamento
     */
    fun createTransaction(request: CreateTransactionRequest): Transaction
    
    /**
     * Busca transação por ID
     */
    fun findById(transactionId: String): Transaction?
    
    /**
     * Busca transação por paymentId (para webhooks)
     */
    fun findByPaymentId(paymentId: String): Transaction?
    
    /**
     * Atualiza status da transação (webhooks)
     */
    fun updatePaymentStatus(transactionId: String, status: TransactionStatus, paidAt: String? = null): Boolean
}

@Service
class TransactionServiceImpl(
    private val transactionRepository: TransactionRepository,
    private val pagBankProvider: PagBankProvider,
    @Value("\${app.webhook-url:http://localhost:8080}") private val webhookUrl: String
) : TransactionService {
    
    private val logger = LoggerFactory.getLogger(TransactionServiceImpl::class.java)
    
    override fun createTransaction(request: CreateTransactionRequest): Transaction = runBlocking {
        logger.info("Creating transaction type=${request.type} amount=${request.amount}")
        
        // Validar request
        request.validate()
        
        // Criar transaction inicial (sem payment ainda)
        val now = getCurrentTimestamp()
        
        val transaction = Transaction(
            id = "",
            type = request.type,
            status = TransactionStatus.PENDING,
            amount = request.amount,
            currency = "BRL",
            customer = request.customer,
            payment = null, // Será preenchido após criar no PagBank
            simpleDonationDetails = request.simpleDonationDetails,
            goalContributionDetails = request.goalContributionDetails,
            rafflePurchaseDetails = request.rafflePurchaseDetails,
            productOrderDetails = request.productOrderDetails,
            targetId = request.targetId,
            targetType = request.targetType,
            notes = request.notes,
            createdAt = now,
            updatedAt = now
        )
        
        // Salvar no Firestore para obter ID
        val savedTransaction = transactionRepository.save(transaction)
        logger.info("Transaction saved with ID: ${savedTransaction.id}")
        
        // Criar pagamento no PagBank
        val transactionWithPayment = if (request.paymentMethod == TransactionPaymentMethod.PIX) {
            createPixPayment(savedTransaction, request)
        } else {
            // TODO: Implementar outros métodos de pagamento
            throw UnsupportedOperationException("Método de pagamento ${request.paymentMethod} não implementado")
        }
        
        // Atualizar transaction com dados do pagamento no Firestore
        transactionRepository.update(
            savedTransaction.id,
            mapOf(
                "payment" to mapOf(
                    "method" to transactionWithPayment.payment!!.method.name,
                    "paymentId" to transactionWithPayment.payment!!.paymentId,
                    "qrCodeText" to transactionWithPayment.payment!!.qrCodeText,
                    "qrCodeImageBase64" to transactionWithPayment.payment!!.qrCodeImageBase64,
                    "expiresAt" to transactionWithPayment.payment!!.expiresAt
                ),
                "updatedAt" to transactionWithPayment.updatedAt
            )
        )
        logger.info("Transaction updated with payment info: paymentId=${transactionWithPayment.payment?.paymentId}")
        
        // Retornar objeto em memória (não buscar do Firestore)
        transactionWithPayment
    }
    
    override fun findById(transactionId: String): Transaction? {
        return transactionRepository.findById(transactionId)
    }
    
    override fun findByPaymentId(paymentId: String): Transaction? {
        return transactionRepository.findByPaymentId(paymentId)
    }
    
    override fun updatePaymentStatus(transactionId: String, status: TransactionStatus, paidAt: String?): Boolean {
        val transaction = transactionRepository.findById(transactionId) ?: return false
        
        val now = getCurrentTimestamp()
        val updates = mutableMapOf<String, Any>(
            "status" to status.name,
            "updatedAt" to now
        )
        
        // Adicionar paidAt se fornecido ou se status for PAID
        val paidAtValue = paidAt ?: if (status == TransactionStatus.PAID) now else transaction.paidAt
        if (paidAtValue != null) {
            updates["paidAt"] = paidAtValue
        }
        
        transactionRepository.update(transactionId, updates)
        
        logger.info("Transaction $transactionId status updated to $status")
        
        return true
    }
    
    // ==================== PRIVATE HELPERS ====================
    
    private suspend fun createPixPayment(transaction: Transaction, request: CreateTransactionRequest): Transaction {
        logger.info("Creating PIX payment for transaction ${transaction.id}")
        
        val amountInCents = (transaction.amount * 100).toLong()
        
        // Montar descrição baseada no tipo
        val description = buildPaymentDescription(transaction)
        
        // Criar request para PagBank
        val pagBankRequest = mapOf(
            "amount" to amountInCents,
            "description" to description,
            "reference_id" to transaction.id, // ✅ CRÍTICO: usar transaction.id para vincular
            "payer" to mapOf(
                "name" to transaction.customer.name,
                "email" to transaction.customer.email,
                "document" to (transaction.customer.document ?: "12345678909") // CPF padrão para teste
            ),
            "notification_urls" to listOf("$webhookUrl/api/webhooks/pagbank")
        )
        
        val response = pagBankProvider.createPixTransaction(pagBankRequest)
        
        val chargeId = response["transaction_id"] as? String
            ?: throw IllegalStateException("PagBank não retornou transaction_id")
        
        val qrCodeText = response["pix_code"] as? String ?: ""
        val expiresAt = response["expires_at"] as? String ?: ""
        
        // Gerar QR Code localmente
        val qrCodeImage = if (qrCodeText.isNotEmpty()) {
            QrCodeGenerator.generateQrCodeBase64(qrCodeText, 300)
        } else {
            null
        }
        
        // Criar PaymentInfo
        val paymentInfo = PaymentInfo(
            method = request.paymentMethod,
            paymentId = chargeId,
            qrCodeText = qrCodeText,
            qrCodeImageBase64 = qrCodeImage,
            expiresAt = expiresAt
        )
        
        return transaction.copy(
            payment = paymentInfo,
            updatedAt = getCurrentTimestamp()
        )
    }
    
    private fun buildPaymentDescription(transaction: Transaction): String {
        return when (transaction.type) {
            TransactionType.SIMPLE_DONATION -> "Doação Tyler"
            TransactionType.GOAL_CONTRIBUTION -> 
                "Contribuição: ${transaction.goalContributionDetails?.goalTitle ?: "Meta"}"
            TransactionType.RAFFLE_PURCHASE -> 
                "Rifa: ${transaction.rafflePurchaseDetails?.raffleTitle ?: "Rifa"}"
            TransactionType.PRODUCT_ORDER -> 
                "Pedido ${transaction.productOrderDetails?.orderNumber ?: transaction.id}"
        }
    }
    
    private fun getCurrentTimestamp(): String {
        return LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
    }
}

/**
 * Request para criar transação
 */
data class CreateTransactionRequest(
    val type: TransactionType,
    val amount: Double,
    val customer: CustomerInfo,
    val paymentMethod: TransactionPaymentMethod,
    
    // Detalhes específicos (apenas UM deve ser preenchido)
    val simpleDonationDetails: SimpleDonationDetails? = null,
    val goalContributionDetails: GoalContributionDetails? = null,
    val rafflePurchaseDetails: RafflePurchaseDetails? = null,
    val productOrderDetails: ProductOrderDetails? = null,
    
    // Vinculação
    val targetId: String? = null,
    val targetType: String? = null,
    
    val notes: String? = null
) {
    fun validate() {
        require(amount > 0) { "Amount must be positive" }
        
        // Validar que apenas um details foi preenchido
        val detailsCount = listOfNotNull(
            simpleDonationDetails,
            goalContributionDetails,
            rafflePurchaseDetails,
            productOrderDetails
        ).size
        
        require(detailsCount == 1) { "Exactly one details object must be provided" }
        
        // Validar consistência type <-> details
        when (type) {
            TransactionType.SIMPLE_DONATION -> require(simpleDonationDetails != null) { 
                "simpleDonationDetails required for SIMPLE_DONATION" 
            }
            TransactionType.GOAL_CONTRIBUTION -> require(goalContributionDetails != null) { 
                "goalContributionDetails required for GOAL_CONTRIBUTION" 
            }
            TransactionType.RAFFLE_PURCHASE -> require(rafflePurchaseDetails != null) { 
                "rafflePurchaseDetails required for RAFFLE_PURCHASE" 
            }
            TransactionType.PRODUCT_ORDER -> require(productOrderDetails != null) { 
                "productOrderDetails required for PRODUCT_ORDER" 
            }
        }
        
        // Validar targetId para tipos que requerem
        if (type.requiresTarget()) {
            require(!targetId.isNullOrBlank()) { "targetId required for ${type.name}" }
        }
    }
}
