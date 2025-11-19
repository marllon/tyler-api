package com.tylerproject.domain.order

/** Status do pedido no sistema Tyler */
enum class OrderStatus {
    PENDING, // Aguardando pagamento
    CONFIRMED, // Pagamento confirmado, aguardando processamento
    PROCESSING, // Em processamento/separação
    SHIPPED, // Enviado
    DELIVERED, // Entregue
    CANCELLED; // Cancelado

    companion object {
        fun getDescription(status: OrderStatus): String {
            return when (status) {
                PENDING -> "Aguardando Pagamento"
                CONFIRMED -> "Pagamento Confirmado"
                PROCESSING -> "Em Processamento"
                SHIPPED -> "Enviado"
                DELIVERED -> "Entregue"
                CANCELLED -> "Cancelado"
            }
        }

        fun canBeCancelled(status: OrderStatus): Boolean {
            return status in listOf(PENDING, CONFIRMED)
        }

        fun requiresRefund(status: OrderStatus): Boolean {
            return status == CONFIRMED || status == PROCESSING
        }
    }
}

/** Método de envio do pedido */
enum class ShippingMethod {
    COLLECT_ON_DELIVERY, // Retirar no local (frete zero)
    SEDEX, // Correios SEDEX
    PAC, // Correios PAC
    CUSTOM; // Transportadora customizada

    companion object {
        fun getDescription(method: ShippingMethod): String {
            return when (method) {
                COLLECT_ON_DELIVERY -> "Retirar no Local"
                SEDEX -> "SEDEX"
                PAC -> "PAC"
                CUSTOM -> "Transportadora"
            }
        }

        fun getShippingCost(method: ShippingMethod): Double {
            return when (method) {
                COLLECT_ON_DELIVERY -> 0.0
                SEDEX -> 0.0 // Será calculado posteriormente pelo admin
                PAC -> 0.0 // Será calculado posteriormente pelo admin
                CUSTOM -> 0.0 // Será calculado posteriormente pelo admin
            }
        }
    }
}
