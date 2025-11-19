# 🛒 Order API - Exemplos de Uso

## Índice
- [Autenticação](#autenticação)
- [Criar Pedido](#criar-pedido)
- [Listar Pedidos](#listar-pedidos)
- [Detalhes do Pedido](#detalhes-do-pedido)
- [Cancelar Pedido](#cancelar-pedido)
- [Webhook de Pagamento](#webhook-de-pagamento)
- [Fluxo Completo E2E](#fluxo-completo-e2e)

---

## Autenticação

Todos os endpoints de pedidos requerem autenticação Firebase JWT.

### Como obter o token (Frontend - Vue.js):
```javascript
import { getAuth } from 'firebase/auth'

const auth = getAuth()
const user = auth.currentUser
const token = await user.getIdToken()

// Usar no header Authorization
const headers = {
  'Authorization': `Bearer ${token}`,
  'Content-Type': 'application/json'
}
```

### Validação no Backend:
O backend valida o token usando `FirebaseAuth.getInstance().verifyIdToken(token)` e extrai:
- `uid` (ID do usuário)
- `email` (Email do usuário)

---

## Criar Pedido

Cria um novo pedido com PIX e retorna QR Code para pagamento.

### Endpoint
```http
POST /api/orders
Authorization: Bearer <firebase-jwt-token>
Content-Type: application/json
```

### Request Body
```json
{
  "items": [
    {
      "productId": "product123",
      "quantity": 2
    },
    {
      "productId": "product456",
      "quantity": 1
    }
  ],
  "shippingAddress": {
    "street": "Rua das Flores",
    "number": "123",
    "complement": "Apto 45",
    "neighborhood": "Centro",
    "city": "São Paulo",
    "state": "SP",
    "zipCode": "01310-100"
  },
  "shippingMethod": "SEDEX",
  "notes": "Entregar no período da manhã"
}
```

### Campos Obrigatórios
- `items`: Lista de produtos (mínimo 1)
  - `productId`: ID do produto (não vazio)
  - `quantity`: Quantidade (mínimo 1)
- `shippingAddress`:
  - `street`, `number`, `neighborhood`, `city`, `state`: não vazios
  - `zipCode`: formato brasileiro (XXXXX-XXX)
- `shippingMethod`: COLLECT_ON_DELIVERY | SEDEX | PAC | CUSTOM

### Campos Opcionais
- `shippingAddress.complement`: Complemento do endereço
- `notes`: Observações (máximo 500 caracteres)

### Response - 201 Created
```json
{
  "order": {
    "id": "order_abc123",
    "orderNumber": "ORD-20250118-1234",
    "userId": "firebase_uid_123",
    "userEmail": "user@example.com",
    "items": [
      {
        "productId": "product123",
        "productName": "Produto Exemplo",
        "quantity": 2,
        "unitPrice": 99.90,
        "subtotal": 199.80,
        "imageUrl": "https://storage.googleapis.com/products/image.jpg"
      }
    ],
    "subtotal": 299.70,
    "shippingCost": 15.00,
    "total": 314.70,
    "paymentMethod": "PIX",
    "paymentStatus": "PENDING",
    "status": "PENDING",
    "shippingMethod": "SEDEX",
    "shippingAddress": {
      "street": "Rua das Flores",
      "number": "123",
      "complement": "Apto 45",
      "neighborhood": "Centro",
      "city": "São Paulo",
      "state": "SP",
      "zipCode": "01310-100"
    },
    "createdAt": "2025-01-18T20:30:00Z",
    "updatedAt": "2025-01-18T20:30:00Z"
  },
  "payment": {
    "qrCode": "00020126580014br.gov.bcb.pix...",
    "qrCodeImage": "data:image/png;base64,iVBORw0KGgoAAAANSUh...",
    "paymentId": "ORDE_ABC123XYZ",
    "expiresAt": "2025-01-18T21:30:00Z",
    "amount": 314.70
  }
}
```

### Curl Example
```bash
# 1. Obter token do Firebase (simular - normalmente vem do frontend)
FIREBASE_TOKEN="eyJhbGciOiJSUzI1NiIsImtpZCI6..."

# 2. Criar pedido
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $FIREBASE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {"productId": "product123", "quantity": 2}
    ],
    "shippingAddress": {
      "street": "Rua das Flores",
      "number": "123",
      "neighborhood": "Centro",
      "city": "São Paulo",
      "state": "SP",
      "zipCode": "01310-100"
    },
    "shippingMethod": "SEDEX"
  }'
```

### Regras de Negócio
✅ **Validações:**
- Todos os produtos devem existir e estar ativos
- Estoque suficiente para todos os itens
- Endereço de entrega completo e válido
- CEP no formato XXXXX-XXX

🔢 **Cálculo de Totais:**
- `subtotal` = soma de (preço × quantidade) de todos os itens
- `shippingCost` = custo do frete baseado no método de envio
- `total` = subtotal + shippingCost

📦 **Snapshot de Produtos:**
- O pedido salva o nome, preço e imagem do produto no momento da compra
- Mudanças futuras no produto não afetam pedidos já criados

💳 **Pagamento PIX:**
- QR Code válido por 1 hora
- Webhook do PagBank notifica quando pagamento é confirmado
- Status inicial: PENDING

---

## Listar Pedidos

Lista todos os pedidos do usuário autenticado com paginação.

### Endpoint
```http
GET /api/orders?limit=10&cursor=order_abc123&status=PENDING
Authorization: Bearer <firebase-jwt-token>
```

### Query Parameters (Opcionais)
- `limit`: Número de pedidos por página (padrão: 20, máximo: 100)
- `cursor`: ID do último pedido da página anterior (para paginação)
- `status`: Filtrar por status (PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED)

### Response - 200 OK
```json
{
  "orders": [
    {
      "id": "order_abc123",
      "orderNumber": "ORD-20250118-1234",
      "status": "PENDING",
      "paymentStatus": "PENDING",
      "total": 314.70,
      "itemCount": 2,
      "createdAt": "2025-01-18T20:30:00Z"
    },
    {
      "id": "order_def456",
      "orderNumber": "ORD-20250117-5678",
      "status": "CONFIRMED",
      "paymentStatus": "PAID",
      "total": 159.90,
      "itemCount": 1,
      "createdAt": "2025-01-17T15:20:00Z"
    }
  ],
  "hasNext": true,
  "nextCursor": "order_def456"
}
```

### Curl Example
```bash
# Listar todos os pedidos (primeira página)
curl -X GET "http://localhost:8080/api/orders?limit=20" \
  -H "Authorization: Bearer $FIREBASE_TOKEN"

# Listar próxima página
curl -X GET "http://localhost:8080/api/orders?limit=20&cursor=order_def456" \
  -H "Authorization: Bearer $FIREBASE_TOKEN"

# Filtrar por status
curl -X GET "http://localhost:8080/api/orders?status=PENDING" \
  -H "Authorization: Bearer $FIREBASE_TOKEN"
```

### Paginação
- **Cursor-based**: Mais eficiente que offset/limit para grandes datasets
- **hasNext**: indica se há mais páginas
- **nextCursor**: ID do último pedido da página atual (usar no próximo request)

---

## Detalhes do Pedido

Retorna informações completas de um pedido específico.

### Endpoint
```http
GET /api/orders/{orderId}
Authorization: Bearer <firebase-jwt-token>
```

### Response - 200 OK
```json
{
  "order": {
    "id": "order_abc123",
    "orderNumber": "ORD-20250118-1234",
    "userId": "firebase_uid_123",
    "userEmail": "user@example.com",
    "items": [
      {
        "productId": "product123",
        "productName": "Produto Exemplo",
        "quantity": 2,
        "unitPrice": 99.90,
        "subtotal": 199.80,
        "imageUrl": "https://storage.googleapis.com/products/image.jpg"
      }
    ],
    "subtotal": 299.70,
    "shippingCost": 15.00,
    "total": 314.70,
    "paymentMethod": "PIX",
    "paymentStatus": "PAID",
    "paymentId": "ORDE_ABC123XYZ",
    "status": "CONFIRMED",
    "shippingMethod": "SEDEX",
    "shippingAddress": {
      "street": "Rua das Flores",
      "number": "123",
      "complement": "Apto 45",
      "neighborhood": "Centro",
      "city": "São Paulo",
      "state": "SP",
      "zipCode": "01310-100"
    },
    "trackingCode": null,
    "trackingUrl": null,
    "createdAt": "2025-01-18T20:30:00Z",
    "updatedAt": "2025-01-18T20:35:00Z",
    "paidAt": "2025-01-18T20:35:00Z"
  },
  "payment": {
    "id": "ORDE_ABC123XYZ",
    "status": "PAID",
    "amount": 314.70,
    "createdAt": "2025-01-18T20:30:00Z"
  },
  "tracking": []
}
```

### Curl Example
```bash
curl -X GET http://localhost:8080/api/orders/order_abc123 \
  -H "Authorization: Bearer $FIREBASE_TOKEN"
```

### Segurança
- ✅ Usuário só pode ver seus próprios pedidos
- ❌ Retorna 404 se tentar acessar pedido de outro usuário

---

## Cancelar Pedido

Cancela um pedido e processa reembolso se já foi pago.

### Endpoint
```http
POST /api/orders/{orderId}/cancel
Authorization: Bearer <firebase-jwt-token>
Content-Type: application/json
```

### Request Body
```json
{
  "reason": "Produto não está mais disponível"
}
```

### Response - 200 OK
```json
{
  "orderId": "order_abc123",
  "orderNumber": "ORD-20250118-1234",
  "status": "CANCELLED",
  "refundStatus": "PENDING",
  "message": "Pedido cancelado com sucesso. Reembolso será processado em até 7 dias úteis."
}
```

### Curl Example
```bash
curl -X POST http://localhost:8080/api/orders/order_abc123/cancel \
  -H "Authorization: Bearer $FIREBASE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Mudei de ideia"
  }'
```

### Regras de Cancelamento

✅ **Pode cancelar:**
- Status: PENDING (aguardando pagamento)
- Status: CONFIRMED (pago, mas ainda não enviado)

❌ **Não pode cancelar:**
- Status: PROCESSING (sendo preparado)
- Status: SHIPPED (em transporte)
- Status: DELIVERED (entregue)
- Status: CANCELLED (já cancelado)

💰 **Reembolso:**
- Se **não pago** (PENDING): cancelamento simples, sem reembolso
- Se **já pago** (CONFIRMED): marca para reembolso
  - `refundStatus` = PENDING
  - `cancelReason` salva a justificativa
  - Reembolso processado pelo sistema de pagamento em até 7 dias úteis

---

## Webhook de Pagamento

O PagBank notifica o backend quando o pagamento é confirmado.

### Como Funciona

1. **Cliente cria pedido** → Backend gera QR Code PIX
2. **Cliente paga** via PIX
3. **PagBank envia webhook** → Backend processa pagamento
4. **Backend atualiza pedido** → Status PENDING → CONFIRMED
5. **Backend decrementa estoque** dos produtos

### Webhook Payload (PagBank → Backend)
```http
POST /api/webhooks/pagbank
Content-Type: application/json
```

```json
{
  "id": "CHAR_ABC123",
  "reference_id": "ORD-20250118-1234",
  "charges": [
    {
      "id": "CHAR_ABC123",
      "reference_id": "ORD-20250118-1234",
      "status": "PAID",
      "created_at": "2025-01-18T20:35:00-03:00",
      "paid_at": "2025-01-18T20:35:00-03:00",
      "amount": {
        "value": 31470,
        "currency": "BRL"
      }
    }
  ]
}
```

### Processamento do Webhook

```kotlin
// PagBankWebhookController.kt
when {
    referenceId?.startsWith("ORD-") == true -> {
        // Webhook de pedido
        orderService.processOrderPayment(paymentId)
    }
    referenceId?.startsWith("raffle_") == true -> {
        // Webhook de rifa
        raffleService.processWebhook(payload)
    }
    else -> {
        // Webhook de doação
        donationService.processWebhook(payload)
    }
}
```

### OrderService.processOrderPayment()

1. **Busca pedido** pelo `paymentId`
2. **Valida status** (deve ser PENDING)
3. **Verifica estoque** de todos os produtos
4. **Se estoque insuficiente:**
   - Cancela pedido
   - Marca para reembolso
   - Retorna false
5. **Se estoque OK:**
   - Decrementa estoque de cada produto
   - Atualiza pedido: status → CONFIRMED, paymentStatus → PAID
   - Salva `paidAt` timestamp
   - Retorna true

### Testando Webhook Localmente

```bash
# Simular webhook do PagBank
curl -X POST http://localhost:8080/api/webhooks/pagbank \
  -H "Content-Type: application/json" \
  -d '{
    "id": "CHAR_TEST123",
    "reference_id": "ORD-20250118-1234",
    "charges": [
      {
        "id": "CHAR_TEST123",
        "reference_id": "ORD-20250118-1234",
        "status": "PAID",
        "created_at": "2025-01-18T20:35:00-03:00",
        "paid_at": "2025-01-18T20:35:00-03:00",
        "amount": {
          "value": 31470,
          "currency": "BRL"
        }
      }
    ]
  }'
```

---

## Fluxo Completo E2E

### Cenário: Compra bem-sucedida

```bash
# 1. CRIAR PEDIDO
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $FIREBASE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {"productId": "product123", "quantity": 2}
    ],
    "shippingAddress": {
      "street": "Rua das Flores",
      "number": "123",
      "neighborhood": "Centro",
      "city": "São Paulo",
      "state": "SP",
      "zipCode": "01310-100"
    },
    "shippingMethod": "SEDEX"
  }' | jq

# Response: Salvar orderNumber e paymentId
# orderNumber: ORD-20250118-1234
# paymentId: ORDE_ABC123XYZ

# 2. CLIENTE PAGA VIA PIX (simular)
# Cliente escaneia QR Code e paga

# 3. WEBHOOK (PagBank → Backend)
curl -X POST http://localhost:8080/api/webhooks/pagbank \
  -H "Content-Type: application/json" \
  -d '{
    "reference_id": "ORD-20250118-1234",
    "charges": [{
      "id": "ORDE_ABC123XYZ",
      "reference_id": "ORD-20250118-1234",
      "status": "PAID"
    }]
  }'

# 4. VERIFICAR STATUS DO PEDIDO
curl -X GET http://localhost:8080/api/orders/order_abc123 \
  -H "Authorization: Bearer $FIREBASE_TOKEN" | jq

# Esperado:
# - status: CONFIRMED
# - paymentStatus: PAID
# - paidAt: preenchido

# 5. VERIFICAR ESTOQUE FOI DECREMENTADO
curl -X GET http://localhost:8080/api/products/product123 | jq '.stock'
# Estoque deve ter diminuído em 2 unidades
```

### Cenário: Estoque insuficiente após pagamento

```bash
# 1. CRIAR PEDIDO (com 10 unidades)
# 2. OUTRO CLIENTE COMPRA AS ÚLTIMAS UNIDADES (estoque = 0)
# 3. WEBHOOK de pagamento chega

# Backend detecta estoque insuficiente:
# - Cancela pedido automaticamente
# - Status: CANCELLED
# - refundStatus: PENDING
# - cancelReason: "Insufficient stock for product..."

# 4. VERIFICAR PEDIDO FOI CANCELADO
curl -X GET http://localhost:8080/api/orders/order_abc123 \
  -H "Authorization: Bearer $FIREBASE_TOKEN" | jq

# Esperado:
# - status: CANCELLED
# - refundStatus: PENDING
```

### Cenário: Cliente cancela antes de pagar

```bash
# 1. CRIAR PEDIDO
# 2. CANCELAR PEDIDO
curl -X POST http://localhost:8080/api/orders/order_abc123/cancel \
  -H "Authorization: Bearer $FIREBASE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason": "Mudei de ideia"}'

# Response:
# - status: CANCELLED
# - refundStatus: null (não havia pagamento)
```

---

## Códigos de Erro

### 400 Bad Request
```json
{
  "error": "Invalid request",
  "message": "Estoque insuficiente para Produto X. Disponível: 5, Solicitado: 10"
}
```

### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Token de autenticação inválido ou expirado"
}
```

### 404 Not Found
```json
{
  "error": "Not found",
  "message": "Pedido não encontrado"
}
```

### 500 Internal Server Error
```json
{
  "error": "Internal server error",
  "message": "Erro ao processar pedido"
}
```

---

## Status do Pedido

### Fluxo Normal
```
PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
```

### Estados

| Status | Descrição | Pode Cancelar? |
|--------|-----------|----------------|
| `PENDING` | Aguardando pagamento | ✅ Sim |
| `CONFIRMED` | Pagamento confirmado | ✅ Sim |
| `PROCESSING` | Pedido sendo preparado | ❌ Não |
| `SHIPPED` | Em transporte | ❌ Não |
| `DELIVERED` | Entregue | ❌ Não |
| `CANCELLED` | Cancelado | - |

### Status de Pagamento

| PaymentStatus | Descrição |
|---------------|-----------|
| `PENDING` | Aguardando pagamento |
| `PAID` | Pago |
| `REFUNDED` | Reembolsado |
| `CANCELLED` | Cancelado |

---

## Métodos de Envio

| ShippingMethod | Descrição | Custo |
|----------------|-----------|-------|
| `COLLECT_ON_DELIVERY` | Retirar no local | R$ 0,00 |
| `SEDEX` | Correios SEDEX | R$ 15,00 |
| `PAC` | Correios PAC | R$ 10,00 |
| `CUSTOM` | Personalizado | Variável |

---

## Segurança

### Firebase JWT
- ✅ Todos os endpoints requerem autenticação
- ✅ Token validado via `FirebaseAuth.getInstance().verifyIdToken()`
- ✅ Extração automática de `uid` e `email`

### Isolamento de Dados
- ✅ Usuário só acessa seus próprios pedidos
- ✅ OrderService valida `userId` em todas as operações
- ✅ Retorna 404 para pedidos de outros usuários

### Validações
- ✅ Jakarta Bean Validation nos DTOs
- ✅ Estoque validado antes de criar pedido
- ✅ Estoque validado novamente no webhook (race conditions)
- ✅ CEP no formato brasileiro obrigatório
- ✅ Quantidade mínima: 1

---

## Firestore Collections

### orders
```javascript
{
  "id": "order_abc123",
  "orderNumber": "ORD-20250118-1234",
  "userId": "firebase_uid_123",
  "userEmail": "user@example.com",
  "items": [...],
  "subtotal": 299.70,
  "shippingCost": 15.00,
  "total": 314.70,
  "paymentMethod": "PIX",
  "paymentStatus": "PAID",
  "paymentId": "ORDE_ABC123XYZ",
  "status": "CONFIRMED",
  "shippingMethod": "SEDEX",
  "shippingAddress": {...},
  "createdAt": "2025-01-18T20:30:00Z",
  "paidAt": "2025-01-18T20:35:00Z"
}
```

### Índices Necessários
```json
{
  "indexes": [
    {
      "collectionGroup": "orders",
      "queryScope": "COLLECTION",
      "fields": [
        {"fieldPath": "userId", "order": "ASCENDING"},
        {"fieldPath": "createdAt", "order": "DESCENDING"}
      ]
    },
    {
      "collectionGroup": "orders",
      "queryScope": "COLLECTION",
      "fields": [
        {"fieldPath": "userId", "order": "ASCENDING"},
        {"fieldPath": "status", "order": "ASCENDING"},
        {"fieldPath": "createdAt", "order": "DESCENDING"}
      ]
    },
    {
      "collectionGroup": "orders",
      "queryScope": "COLLECTION",
      "fields": [
        {"fieldPath": "orderNumber", "order": "ASCENDING"}
      ]
    },
    {
      "collectionGroup": "orders",
      "queryScope": "COLLECTION",
      "fields": [
        {"fieldPath": "paymentId", "order": "ASCENDING"}
      ]
    }
  ]
}
```

---

## Próximos Passos

1. **Frontend Integration**
   - Implementar carrinho de compras (já existe no frontend)
   - Integrar API de pedidos
   - Exibir QR Code PIX
   - Polling para verificar status de pagamento

2. **Notificações**
   - Enviar email ao criar pedido
   - Notificar quando pagamento confirmado
   - Alertas de tracking de envio

3. **Admin Panel**
   - Dashboard de pedidos
   - Processar pedidos (CONFIRMED → PROCESSING)
   - Adicionar código de rastreamento
   - Marcar como enviado/entregue

4. **Melhorias**
   - Cálculo dinâmico de frete (integrar com Correios API)
   - Múltiplos métodos de pagamento (cartão de crédito)
   - Cupons de desconto
   - Sistema de pontos/cashback
