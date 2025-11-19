package com.tylerproject.domain.transaction

/**
 * Tipo de transação no sistema Tyler
 * Todas as operações financeiras são transações (doações, pedidos, rifas, metas)
 */
enum class TransactionType {
    SIMPLE_DONATION,    // Doação livre sem vínculo
    GOAL_CONTRIBUTION,  // Contribuição para meta específica
    RAFFLE_PURCHASE,    // Compra de números de rifa
    PRODUCT_ORDER;      // Pedido de produtos (e-commerce)
    
    fun getDescription(): String {
        return when (this) {
            SIMPLE_DONATION -> "Doação Simples"
            GOAL_CONTRIBUTION -> "Contribuição para Meta"
            RAFFLE_PURCHASE -> "Compra de Rifa"
            PRODUCT_ORDER -> "Pedido de Produtos"
        }
    }
    
    fun requiresTarget(): Boolean {
        return when (this) {
            SIMPLE_DONATION, PRODUCT_ORDER -> false
            GOAL_CONTRIBUTION, RAFFLE_PURCHASE -> true
        }
    }
}

/**
 * Status de pagamento da transação
 */
enum class TransactionStatus {
    PENDING,     // Aguardando pagamento
    PAID,        // Pago e confirmado
    PROCESSING,  // Processando (ex: separando produtos)
    COMPLETED,   // Concluído (ex: produto entregue)
    CANCELLED,   // Cancelado
    REFUNDED;    // Reembolsado
    
    fun getDescription(): String {
        return when (this) {
            PENDING -> "Aguardando Pagamento"
            PAID -> "Pago"
            PROCESSING -> "Em Processamento"
            COMPLETED -> "Concluído"
            CANCELLED -> "Cancelado"
            REFUNDED -> "Reembolsado"
        }
    }
    
    fun canBeCancelled(): Boolean {
        return this == PENDING || this == PAID
    }
    
    fun requiresRefund(): Boolean {
        return this == PAID || this == PROCESSING
    }
}

/**
 * Método de pagamento
 */
enum class TransactionPaymentMethod {
    PIX,
    CREDIT_CARD,
    DEBIT_CARD,
    BANK_SLIP;
    
    fun getDescription(): String {
        return when (this) {
            PIX -> "PIX"
            CREDIT_CARD -> "Cartão de Crédito"
            DEBIT_CARD -> "Cartão de Débito"
            BANK_SLIP -> "Boleto Bancário"
        }
    }
}
