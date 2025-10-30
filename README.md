# Tyler Project - Backend

Kotlin-based backend server for the Tyler charitable platform with Firestore integration and payment processing.

## 🎯 Overview

This is the backend API built with Kotlin, providing RESTful endpoints for the Tyler platform. It handles authentication, payment processing (Stripe & Mercado Pago), and data management with Google Cloud Firestore.

## 🛠️ Tech Stack

- **Language**: Kotlin 1.9+
- **Runtime**: JVM (Java 17+)
- **Build Tool**: Gradle 8.x
- **Database**: Google Cloud Firestore
- **Storage**: Firebase Storage
- **Authentication**: Firebase Auth (planned)
- **Payment Providers**:
  - Stripe (International payments)
  - Mercado Pago (Brazil)
- **HTTP Server**: Custom implementation (Ktor-like)

## 📁 Project Structure

```
backend/
├── src/main/kotlin/com/tylerproject/
│   ├── Main.kt                     # Application entry point
│   │
│   ├── config/
│   │   └── Config.kt               # Configuration management
│   │
│   ├── handlers/
│   │   └── Handlers.kt             # HTTP route handlers
│   │
│   ├── middleware/
│   │   └── CorsMiddleware.kt       # CORS handling
│   │
│   ├── models/
│   │   ├── Models.kt               # Domain models
│   │   └── DTOs.kt                 # Data Transfer Objects
│   │
│   └── providers/
│       ├── PaymentProvider.kt      # Payment interface
│       ├── StripeProvider.kt       # Stripe integration
│       └── MercadoPagoProvider.kt  # Mercado Pago integration
│
├── bin/                            # Compiled classes
├── build.gradle.kts                # Gradle build configuration
├── firestore.indexes.json          # Firestore index definitions
├── firestore.rules                 # Firestore security rules
├── storage.rules                   # Storage security rules
├── package.json                    # Node.js scripts (Firebase CLI)
└── README.md                       # This file
```

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK)**: 17 or higher
- **Gradle**: 8.x (or use wrapper)
- **Firebase Project**: With Firestore and Storage enabled
- **Firebase Admin SDK**: Service account credentials
- **Payment Accounts**: Stripe and/or Mercado Pago

### Installation

1. **Navigate to backend directory**
   ```bash
   cd backend
   ```

2. **Set up Firebase credentials**
   
   Download your Firebase Admin SDK private key:
   - Go to Firebase Console → Project Settings → Service Accounts
   - Click "Generate New Private Key"
   - Save as `firebase-admin-sdk.json` in the `config/` folder

3. **Set up environment variables**
   
   Create `.env` file:
   ```env
   FIREBASE_PROJECT_ID=your-project-id
   STRIPE_SECRET_KEY=sk_test_...
   MERCADOPAGO_ACCESS_TOKEN=APP_USR-...
   ADMIN_EMAIL=admin@tylerproject.com
   ADMIN_PASSWORD=secure_password
   PORT=8080
   ```

4. **Install dependencies**
   ```bash
   ./gradlew build
   ```

### Development

```bash
# Run the server
./gradlew run

# Server will start on http://localhost:8080
```

### Testing

```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests "SpecificTest"

# Run with coverage
./gradlew test jacocoTestReport
```

## 📡 API Endpoints

### Base URL

```
Development: http://localhost:8080
Production: https://api.tylerproject.com
```

### Authentication

```http
POST   /api/auth/login      # Admin login
POST   /api/auth/logout     # Admin logout
GET    /api/auth/me         # Get current user
```

### Products

```http
GET    /api/products        # List all active products
GET    /api/products/:id    # Get product details
POST   /api/products        # Create product (admin)
PUT    /api/products/:id    # Update product (admin)
DELETE /api/products/:id    # Delete product (admin)
```

### Fundraising Goals

```http
GET    /api/goals           # List all active goals
GET    /api/goals/:id       # Get goal details
POST   /api/goals           # Create goal (admin)
PUT    /api/goals/:id       # Update goal (admin)
DELETE /api/goals/:id       # Delete goal (admin)
POST   /api/goals/:id/donate # Make donation
```

### Raffles

```http
GET    /api/raffles         # List all raffles
GET    /api/raffles/:id     # Get raffle details
POST   /api/raffles         # Create raffle (admin)
PUT    /api/raffles/:id     # Update raffle (admin)
DELETE /api/raffles/:id     # Delete raffle (admin)
POST   /api/raffles/:id/buy # Purchase tickets
POST   /api/raffles/:id/draw # Conduct draw (admin)
GET    /api/raffles/:id/verify # Verify fairness
```

### Events

```http
GET    /api/events          # List all events
GET    /api/events/:id      # Get event details
POST   /api/events          # Create event (admin)
PUT    /api/events/:id      # Update event (admin)
DELETE /api/events/:id      # Delete event (admin)
POST   /api/events/:id/register # Register participant
```

### Orders

```http
GET    /api/orders          # List orders (admin)
GET    /api/orders/:id      # Get order details
PUT    /api/orders/:id/status # Update status (admin)
```

### Donations

```http
GET    /api/donations       # List donations (admin)
GET    /api/donations/:id   # Get donation details
POST   /api/donations       # Create donation
```

### Response Format

All endpoints return this structure:

```json
{
  "success": true,
  "data": { ... },
  "error": null,
  "meta": {
    "page": 1,
    "limit": 20,
    "total": 100
  }
}
```

Error response:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human readable error message"
  }
}
```

## 🗄️ Data Models

### Product

```kotlin
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val category: String,
    val imageUrl: String,
    val active: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
```

### Goal

```kotlin
data class Goal(
    val id: String,
    val title: String,
    val description: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val progress: Int,
    val deadline: String? = null,
    val imageUrl: String? = null,
    val active: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
```

### Raffle

```kotlin
data class Raffle(
    val id: String,
    val title: String,
    val description: String,
    val prize: String,
    val ticketPrice: Double,
    val totalTickets: Int,
    val soldTickets: Int,
    val status: RaffleStatus, // ACTIVE, ENDED, DRAWN
    val imageUrl: String? = null,
    val images: List<String>? = null,
    val drawDate: String? = null,
    val committedEntropy: String,
    val revealEntropy: String? = null,
    val winnerTicketNumber: Int? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

enum class RaffleStatus {
    ACTIVE, ENDED, DRAWN
}
```

### Event

```kotlin
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val imageUrl: String,
    val gallery: List<String>? = null,
    val registeredParticipants: Int,
    val maxParticipants: Int,
    val completed: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
```

## 💳 Payment Integration

### Stripe

```kotlin
class StripeProvider : PaymentProvider {
    override suspend fun createPaymentIntent(
        amount: Double,
        currency: String,
        metadata: Map<String, String>
    ): PaymentIntent {
        // Create Stripe payment intent
    }
    
    override suspend fun confirmPayment(
        paymentIntentId: String
    ): PaymentStatus {
        // Confirm payment
    }
}
```

### Mercado Pago

```kotlin
class MercadoPagoProvider : PaymentProvider {
    override suspend fun createPaymentIntent(
        amount: Double,
        currency: String,
        metadata: Map<String, String>
    ): PaymentIntent {
        // Create Mercado Pago preference
    }
    
    override suspend fun confirmPayment(
        paymentIntentId: String
    ): PaymentStatus {
        // Confirm payment via webhook
    }
}
```

### Payment Flow

1. **Client requests checkout**
   ```http
   POST /api/payments/checkout
   {
     "type": "product" | "donation" | "raffle",
     "itemId": "123",
     "amount": 50.00,
     "provider": "stripe" | "mercadopago"
   }
   ```

2. **Server creates payment intent**
   - Validates amount and availability
   - Creates payment with selected provider
   - Returns client secret

3. **Client completes payment**
   - User redirected to payment page
   - Completes payment via Stripe or Mercado Pago

4. **Webhook receives confirmation**
   ```http
   POST /api/webhooks/payments
   ```
   - Verifies webhook signature
   - Updates order/donation status
   - Updates goal progress
   - Assigns raffle tickets
   - Sends confirmation email

## 🎲 Raffle System - Commit-Reveal

Provably fair raffle implementation:

### Commit Phase

```kotlin
fun createRaffle(raffle: Raffle): String {
    val salt = generateRandomSalt()
    val timestamp = System.currentTimeMillis()
    val committedEntropy = sha256("$salt-$timestamp")
    
    // Store raffle with committed entropy
    // Salt kept secret until draw
    return committedEntropy
}
```

### Draw Phase

```kotlin
fun drawRaffle(raffleId: String): Int {
    val raffle = getRaffle(raffleId)
    val revealEntropy = generateRandomString()
    
    // Calculate winner
    val combinedHash = sha256(
        "${raffle.committedEntropy}-$revealEntropy"
    )
    val winnerNumber = combinedHash.toInt() % raffle.totalTickets
    
    // Update raffle
    updateRaffle(raffleId, mapOf(
        "revealEntropy" to revealEntropy,
        "winnerTicketNumber" to winnerNumber,
        "status" to RaffleStatus.DRAWN
    ))
    
    return winnerNumber
}
```

### Verification

Anyone can verify the draw:

```kotlin
fun verifyDraw(
    committedEntropy: String,
    revealEntropy: String,
    totalTickets: Int
): Int {
    val hash = sha256("$committedEntropy-$revealEntropy")
    return hash.toInt() % totalTickets
}
```

## 🔐 Security

### Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Helper functions
    function isAdmin() {
      return request.auth != null && 
             request.auth.token.admin == true;
    }
    
    // Products
    match /products/{productId} {
      allow read: if resource.data.active == true;
      allow write: if isAdmin();
    }
    
    // Goals
    match /goals/{goalId} {
      allow read: if resource.data.active == true;
      allow write: if isAdmin();
    }
    
    // Raffles
    match /raffles/{raffleId} {
      allow read: if true;
      allow write: if isAdmin();
    }
    
    // Events
    match /events/{eventId} {
      allow read: if true;
      allow write: if isAdmin();
    }
    
    // Orders - admin only
    match /orders/{orderId} {
      allow read, write: if isAdmin();
    }
    
    // Donations - admin only
    match /donations/{donationId} {
      allow read, write: if isAdmin();
    }
  }
}
```

### Storage Security Rules

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Images - public read, admin write
    match /images/{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null && 
                      request.auth.token.admin == true;
    }
  }
}
```

## 🔧 Configuration

### Gradle Build (build.gradle.kts)

```kotlin
plugins {
    kotlin("jvm") version "1.9.0"
    application
}

dependencies {
    implementation("com.google.firebase:firebase-admin:9.2.0")
    implementation("com.stripe:stripe-java:24.0.0")
    implementation("com.mercadopago:sdk-java:2.1.0")
    implementation("com.google.code.gson:gson:2.10.1")
}

application {
    mainClass.set("com.tylerproject.MainKt")
}
```

### Firebase Configuration

1. **Initialize Firebase Admin**
   ```kotlin
   val serviceAccount = FileInputStream("config/firebase-admin-sdk.json")
   val options = FirebaseOptions.builder()
       .setCredentials(GoogleCredentials.fromStream(serviceAccount))
       .build()
   FirebaseApp.initializeApp(options)
   ```

2. **Get Firestore instance**
   ```kotlin
   val db = FirestoreClient.getFirestore()
   ```

## 📦 Deployment

### Google Cloud Run

```bash
# Build Docker image
docker build -t tyler-backend .

# Tag image
docker tag tyler-backend gcr.io/PROJECT_ID/tyler-backend

# Push to Container Registry
docker push gcr.io/PROJECT_ID/tyler-backend

# Deploy to Cloud Run
gcloud run deploy tyler-backend \
  --image gcr.io/PROJECT_ID/tyler-backend \
  --platform managed \
  --region southamerica-east1 \
  --allow-unauthenticated
```

### Firebase Functions

```bash
# Deploy functions
firebase deploy --only functions
```

### Environment Variables (Production)

Set in Cloud Run or Functions:

```bash
FIREBASE_PROJECT_ID=production-project-id
STRIPE_SECRET_KEY=sk_live_...
MERCADOPAGO_ACCESS_TOKEN=APP_USR_...
ADMIN_EMAIL=admin@tylerproject.com
ADMIN_PASSWORD_HASH=bcrypt_hash...
```

## 🧪 Testing

### Unit Tests

```kotlin
class RaffleServiceTest {
    @Test
    fun `should create raffle with committed entropy`() {
        val raffle = createRaffle(testRaffle)
        assertNotNull(raffle.committedEntropy)
        assertEquals(64, raffle.committedEntropy.length)
    }
    
    @Test
    fun `should draw winner within ticket range`() {
        val winner = drawRaffle(raffleId)
        assertTrue(winner >= 0 && winner < totalTickets)
    }
}
```

Run tests:

```bash
./gradlew test
```

## 📊 Monitoring

### Logging

```kotlin
import java.util.logging.Logger

val logger = Logger.getLogger("TylerBackend")

logger.info("Payment processed: $paymentId")
logger.warning("Low stock alert: $productId")
logger.severe("Payment failed: ${error.message}")
```

### Error Handling

```kotlin
try {
    processPayment(paymentIntent)
} catch (e: StripeException) {
    logger.severe("Stripe error: ${e.message}")
    return ErrorResponse(
        code = "PAYMENT_FAILED",
        message = "Payment processing failed"
    )
}
```

## 🤝 Contributing

1. Follow Kotlin coding conventions
2. Use meaningful variable and function names
3. Add KDoc comments for public APIs
4. Write unit tests for business logic
5. Keep functions small and focused
6. Use data classes for DTOs
7. Leverage Kotlin features (null safety, coroutines)

## 📚 Additional Documentation

- [Main README](../README.md) - Overall project documentation
- [Frontend README](../frontend/README.md) - Frontend documentation
- [API Documentation](../docs/API.md) - Complete API reference (planned)

## 📄 License

MIT License - See [LICENSE](../LICENSE) for details.

---

**Built with ❤️ using Kotlin and Firebase**
