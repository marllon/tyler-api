package com.tylerproject.config

import com.tylerproject.providers.*

object Config {
    val firebaseProjectId: String = System.getenv("FIREBASE_PROJECT_ID") ?: "projeto-tyler"
    val region: String = System.getenv("FIREBASE_REGION") ?: "southamerica-east1"

    // Auth
    val useFirebaseAuth: Boolean = System.getenv("USE_FIREBASE_AUTH")?.toBoolean() ?: true
    val adminBasicAuth: String? = System.getenv("ADMIN_BASIC_AUTH")

    // Payment
    val paymentProvider: String = System.getenv("PAYMENT_PROVIDER") ?: "stripe"

    // Stripe
    val stripeSecretKey: String = System.getenv("STRIPE_SECRET_KEY") ?: ""
    val stripeWebhookSecret: String = System.getenv("STRIPE_WEBHOOK_SECRET") ?: ""

    // Mercado Pago
    val mpAccessToken: String = System.getenv("MP_ACCESS_TOKEN") ?: ""
    val mpWebhookSecret: String = System.getenv("MP_WEBHOOK_SECRET") ?: ""

    // Email
    val emailProvider: String = System.getenv("EMAIL_PROVIDER") ?: "sendgrid"
    val emailApiKey: String = System.getenv("EMAIL_API_KEY") ?: ""
    val emailFrom: String = System.getenv("EMAIL_FROM") ?: "noreply@tylerlimaeler.org"
    val emailFromName: String = System.getenv("EMAIL_FROM_NAME") ?: "Projeto Tyler Lima Eler"

    // Site
    val publicSiteUrl: String = System.getenv("PUBLIC_SITE_URL") ?: "http://localhost:5173"
    val defaultCurrency: String = System.getenv("DEFAULT_CURRENCY") ?: "BRL"
    val allowedOrigins: List<String> =
            System.getenv("ALLOWED_ORIGINS")?.split(",") ?: listOf("http://localhost:5173")

    // Raffle
    val raffleCommitSalt: String = System.getenv("RAFFLE_COMMIT_SALT") ?: "change-this-salt"

    // Get Payment Provider instance
    fun getPaymentProvider(): PaymentProvider {
        return when (paymentProvider.lowercase()) {
            "stripe" -> StripeProvider(stripeSecretKey, stripeWebhookSecret)
            "mercadopago" -> MercadoPagoProvider(mpAccessToken, mpWebhookSecret)
            else -> throw IllegalStateException("Payment provider inválido: $paymentProvider")
        }
    }
}
