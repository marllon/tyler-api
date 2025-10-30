package com.tylerproject.providers

import com.stripe.Stripe
import com.stripe.model.PaymentIntent
import com.stripe.model.Refund
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import com.stripe.param.RefundCreateParams
import com.stripe.param.checkout.SessionCreateParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StripeProvider(private val secretKey: String, private val webhookSecret: String) :
        PaymentProvider {

    init {
        Stripe.apiKey = secretKey
    }

    override suspend fun createCheckout(
            amount: Double,
            currency: String,
            description: String,
            metadata: Map<String, String>,
            customerEmail: String,
            successUrl: String,
            cancelUrl: String
    ): PaymentResult =
            withContext(Dispatchers.IO) {
                try {
                    // Stripe usa centavos
                    val amountInCents = (amount * 100).toLong()

                    val params =
                            SessionCreateParams.builder()
                                    .setMode(SessionCreateParams.Mode.PAYMENT)
                                    .setSuccessUrl(successUrl)
                                    .setCancelUrl(cancelUrl)
                                    .setCustomerEmail(customerEmail)
                                    .addLineItem(
                                            SessionCreateParams.LineItem.builder()
                                                    .setPriceData(
                                                            SessionCreateParams.LineItem.PriceData
                                                                    .builder()
                                                                    .setCurrency(
                                                                            currency.lowercase()
                                                                    )
                                                                    .setUnitAmount(amountInCents)
                                                                    .setProductData(
                                                                            SessionCreateParams
                                                                                    .LineItem
                                                                                    .PriceData
                                                                                    .ProductData
                                                                                    .builder()
                                                                                    .setName(
                                                                                            description
                                                                                    )
                                                                                    .build()
                                                                    )
                                                                    .build()
                                                    )
                                                    .setQuantity(1)
                                                    .build()
                                    )
                                    .putAllMetadata(metadata)
                                    .addPaymentMethodType(
                                            SessionCreateParams.PaymentMethodType.CARD
                                    )
                                    .build()

                    val session = Session.create(params)

                    PaymentResult(
                            success = true,
                            checkoutUrl = session.url,
                            paymentIntentId = session.paymentIntent
                    )
                } catch (e: Exception) {
                    PaymentResult(
                            success = false,
                            error = e.message ?: "Erro ao criar checkout Stripe"
                    )
                }
            }

    override suspend fun getPaymentStatus(paymentIntentId: String): PaymentStatus =
            withContext(Dispatchers.IO) {
                try {
                    val intent = PaymentIntent.retrieve(paymentIntentId)

                    PaymentStatus(
                            status =
                                    when (intent.status) {
                                        "succeeded" -> "succeeded"
                                        "processing" -> "pending"
                                        "requires_payment_method" -> "pending"
                                        "requires_confirmation" -> "pending"
                                        "requires_action" -> "pending"
                                        "canceled" -> "cancelled"
                                        else -> "failed"
                                    },
                            amount = intent.amount / 100.0,
                            currency = intent.currency.uppercase(),
                            metadata = intent.metadata
                    )
                } catch (e: Exception) {
                    PaymentStatus(
                            status = "failed",
                            amount = 0.0,
                            currency = "BRL",
                            metadata = emptyMap()
                    )
                }
            }

    override fun verifyWebhook(payload: String, signature: String): WebhookEvent? {
        return try {
            val event = Webhook.constructEvent(payload, signature, webhookSecret)

            when (event.type) {
                "checkout.session.completed", "payment_intent.succeeded" -> {
                    val session = event.dataObjectDeserializer.`object`.orElse(null) as? Session
                    val paymentIntent = session?.paymentIntent ?: return null

                    WebhookEvent(
                            type = "payment.succeeded",
                            paymentIntentId = paymentIntent,
                            amount = (session.amountTotal ?: 0) / 100.0,
                            currency = session.currency?.uppercase() ?: "BRL",
                            metadata = session.metadata ?: emptyMap()
                    )
                }
                "payment_intent.payment_failed" -> {
                    val intent =
                            event.dataObjectDeserializer.`object`.orElse(null) as? PaymentIntent

                    WebhookEvent(
                            type = "payment.failed",
                            paymentIntentId = intent?.id ?: "",
                            amount = (intent?.amount ?: 0) / 100.0,
                            currency = intent?.currency?.uppercase() ?: "BRL",
                            metadata = intent?.metadata ?: emptyMap()
                    )
                }
                else -> null
            }
        } catch (e: Exception) {
            println("Erro ao verificar webhook Stripe: ${e.message}")
            null
        }
    }

    override suspend fun refundPayment(paymentIntentId: String, amount: Double?): RefundResult =
            withContext(Dispatchers.IO) {
                try {
                    val params =
                            RefundCreateParams.builder()
                                    .setPaymentIntent(paymentIntentId)
                                    .apply { amount?.let { setAmount((it * 100).toLong()) } }
                                    .build()

                    val refund = Refund.create(params)

                    RefundResult(success = true, refundId = refund.id)
                } catch (e: Exception) {
                    RefundResult(
                            success = false,
                            error = e.message ?: "Erro ao reembolsar pagamento"
                    )
                }
            }
}
