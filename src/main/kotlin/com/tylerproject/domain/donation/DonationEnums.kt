package com.tylerproject.domain.donation

enum class DonationType {
    GOAL,
    RAFFLE,
    ORDER
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
