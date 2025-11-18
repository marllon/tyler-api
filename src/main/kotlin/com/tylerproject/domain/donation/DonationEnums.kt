package com.tylerproject.domain.donation

enum class DonationType {
    GOAL,
    RAFFLE,
    ORDER,
    SIMPLE // Doação simples sem vínculo
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
