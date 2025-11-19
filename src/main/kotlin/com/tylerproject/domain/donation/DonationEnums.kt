package com.tylerproject.domain.donation

/**
 * Tipo de doação no sistema Tyler
 *
 * - SIMPLE: Doação livre sem vínculo com metas, rifas ou produtos
 * - GOAL: Contribuição para uma meta específica
 * - RAFFLE: Compra de bilhetes de rifa
 * - ORDER: Pedido de produtos (e-commerce)
 */
enum class DonationType {
    SIMPLE, // Doação simples sem vínculo
    GOAL, // Contribuição para meta
    RAFFLE, // Compra de rifa
    ORDER; // Pedido de produtos

    /**
     * Retorna o targetId apropriado para este tipo de doação
     * - SIMPLE: retorna "simple-donation" (valor fixo)
     * - Outros tipos: null (targetId deve ser fornecido externamente)
     */
    fun getTargetId(): String? {
        return when (this) {
            SIMPLE -> SIMPLE_DONATION_TARGET_ID
            GOAL, RAFFLE, ORDER -> null
        }
    }

    companion object {
        /**
         * Valor padrão de targetId para doações simples Doações SIMPLE sempre usam este targetId
         * fixo
         */
        const val SIMPLE_DONATION_TARGET_ID = "simple-donation"

        /** Valida se um targetId é compatível com o tipo de doação */
        fun validateTargetId(type: DonationType, targetId: String): Boolean {
            return when (type) {
                SIMPLE -> targetId == SIMPLE_DONATION_TARGET_ID
                GOAL, RAFFLE, ORDER ->
                        targetId.isNotBlank() && targetId != SIMPLE_DONATION_TARGET_ID
            }
        }

        /** Retorna descrição amigável do tipo de doação */
        fun getDescription(type: DonationType): String {
            return when (type) {
                SIMPLE -> "Doação Livre"
                GOAL -> "Contribuição para Meta"
                RAFFLE -> "Compra de Rifa"
                ORDER -> "Pedido de Produtos"
            }
        }
    }
}

enum class DonationStatus {
    PENDING,
    PAID,
    CANCELLED,
    REFUNDED,
    FAILED
}

enum class PaymentMethod {
    PIX,
    CREDIT_CARD,
    DEBIT_CARD,
    BOLETO
}
