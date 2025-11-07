# 🔗 Tyler API - Guia de Integração Frontend

**Documentação completa para integração do frontend com a Tyler API**

---

## 📋 **Informações Básicas da API**

### 🌐 **Base URLs**

```
Desenvolvimento: http://localhost:8080
Produção:        https://tyler-api.herokuapp.com (quando deploy)
```

### 🔧 **Configuração Inicial**

- **Protocolo:** HTTP/HTTPS + JSON
- **Content-Type:** `application/json`
- **CORS:** Habilitado para desenvolvimento
- **Rate Limit:** Não configurado (implementar se necessário)

---

## ✅ **APIs ATIVAS (Prontas para Uso)**

### 🏥 **Health Check & Status**

```http
GET /api/health
```

**Resposta:**

```json
{
  "status": "healthy",
  "message": "Tyler API Spring Boot está funcionando perfeitamente! ✅",
  "timestamp": "2025-11-07T19:26:42.255577300",
  "version": "2.0.0-spring-boot"
}
```

---

### 💳 **Sistema de Pagamentos PIX (PagBank)**

#### **1. Criar Checkout PIX**

```http
POST /api/payments/checkout
Content-Type: application/json

{
  "amount": 1000,
  "description": "Doação para meta X",
  "payer": {
    "name": "João Silva",
    "email": "joao@exemplo.com",
    "document": "11144477735"
  }
}
```

**Resposta de Sucesso:**

```json
{
  "id": "ORDE_12345",
  "qr_codes": [
    {
      "id": "qr_123",
      "text": "00020126360014BR.GOV.BCB.PIX...",
      "links": [
        {
          "media": "image/png",
          "href": "https://api.pagbank.com/qrcode/qr_123.png"
        }
      ]
    }
  ],
  "status": "WAITING_PAYMENT",
  "amount": {
    "value": 1000,
    "currency": "BRL"
  },
  "created_at": "2025-11-07T19:30:00Z"
}
```

**Resposta de Erro:**

```json
{
  "error": "donation_failed",
  "message": "Erro ao criar doação: CPF inválido"
}
```

#### **2. Consultar Status do Pagamento**

```http
GET /api/payments/{transactionId}/status
```

**Resposta:**

```json
{
  "id": "ORDE_12345",
  "status": "PAID",
  "amount": {
    "value": 1000,
    "currency": "BRL"
  },
  "paid_at": "2025-11-07T19:35:00Z"
}
```

#### **3. Webhook (Backend Only)**

```http
POST /api/payments/webhook
X-PagBank-Signature: signature_hash

[Payload automático do PagBank]
```

---

## ⏸️ **APIs PREPARADAS (Aguardando Ativação)**

### 🛍️ **Produtos & Catálogo**

#### **Listar Produtos**

```http
GET /api/products?page=1&pageSize=20&activeOnly=true&category=roupas
```

#### **Detalhes do Produto**

```http
GET /api/products/{productId}
```

#### **Criar Produto (Admin)**

```http
POST /api/products
Authorization: Bearer {admin_token}

{
  "name": "Camiseta Solidária",
  "description": "Camiseta 100% algodão para arrecadação",
  "price": 2500,
  "imageUrl": "https://exemplo.com/imagem.jpg",
  "category": "roupas",
  "stock": 100
}
```

---

### 🛒 **Pedidos & Checkout**

#### **Criar Pedido Multi-Produtos**

```http
POST /api/orders/checkout

{
  "items": [
    {
      "productId": "prod_123",
      "quantity": 2,
      "price": 2500
    },
    {
      "productId": "prod_456",
      "quantity": 1,
      "price": 5000
    }
  ],
  "customer": {
    "name": "Maria Silva",
    "email": "maria@exemplo.com",
    "document": "12345678901",
    "phone": "+5511999999999"
  },
  "goalId": "meta_123"
}
```

#### **Status do Pedido**

```http
GET /api/orders/{orderId}
```

---

### 🎯 **Metas de Arrecadação**

#### **Listar Metas Ativas**

```http
GET /api/goals?active=true
```

**Resposta Esperada:**

```json
{
  "goals": [
    {
      "id": "meta_123",
      "title": "Reforma do Orfanato",
      "description": "Ajude a reformar o orfanato local",
      "targetAmount": 50000,
      "currentAmount": 15750,
      "percentage": 31.5,
      "endDate": "2025-12-31T23:59:59Z",
      "status": "ACTIVE",
      "imageUrl": "https://exemplo.com/orfanato.jpg"
    }
  ],
  "pagination": {
    "page": 1,
    "totalPages": 3,
    "totalItems": 15
  }
}
```

#### **Detalhes da Meta**

```http
GET /api/goals/{goalId}
```

#### **Criar Meta (Admin)**

```http
POST /api/goals
Authorization: Bearer {admin_token}

{
  "title": "Nova Campanha",
  "description": "Descrição da campanha",
  "targetAmount": 100000,
  "endDate": "2025-12-31T23:59:59Z",
  "imageUrl": "https://exemplo.com/campanha.jpg"
}
```

---

### 💝 **Doações Diretas**

#### **Fazer Doação PIX**

```http
POST /api/donations

{
  "amount": 5000,
  "goalId": "meta_123",
  "anonymous": false,
  "message": "Boa sorte com a campanha!",
  "donor": {
    "name": "Ana Costa",
    "email": "ana@exemplo.com",
    "document": "98765432100"
  }
}
```

#### **Histórico de Doações**

```http
GET /api/donations?goalId=meta_123&page=1
```

---

### 🎰 **Rifas Solidárias**

#### **Rifas Ativas**

```http
GET /api/raffles?status=ACTIVE
```

#### **Comprar Bilhetes**

```http
POST /api/raffles/{raffleId}/tickets

{
  "quantity": 5,
  "buyer": {
    "name": "Carlos Silva",
    "email": "carlos@exemplo.com",
    "document": "11122233344",
    "phone": "+5511888888888"
  }
}
```

#### **Meus Bilhetes**

```http
GET /api/raffles/{raffleId}/tickets?buyerEmail=carlos@exemplo.com
```

---

### 🎪 **Eventos Solidários**

#### **Listar Eventos**

```http
GET /api/events?upcoming=true
```

#### **Detalhes do Evento**

```http
GET /api/events/{eventId}
```

#### **Inscrever no Evento**

```http
POST /api/events/{eventId}/register

{
  "participant": {
    "name": "Pedro Santos",
    "email": "pedro@exemplo.com",
    "phone": "+5511777777777"
  },
  "ticketType": "VIP"
}
```

---

## 🔐 **Autenticação (Firebase)**

### **Headers Necessários (Admin)**

```http
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

### **Endpoints Protegidos**

- `POST /api/products` (Criar produto)
- `PUT /api/products/{id}` (Atualizar produto)
- `DELETE /api/products/{id}` (Deletar produto)
- `POST /api/goals` (Criar meta)
- `PUT /api/goals/{id}` (Atualizar meta)
- `GET /api/admin/*` (Todos os endpoints admin)

---

## 📊 **Admin Dashboard**

### **Dashboard Completo**

```http
GET /api/admin/dashboard
Authorization: Bearer {admin_token}
```

**Resposta:**

```json
{
  "summary": {
    "totalDonations": 125750,
    "totalOrders": 89,
    "activeGoals": 5,
    "totalUsers": 1247
  },
  "recentTransactions": [...],
  "goalProgress": [...],
  "topProducts": [...]
}
```

### **Logs de Auditoria**

```http
GET /api/admin/audit?page=1&entity=Product&action=CREATE
Authorization: Bearer {admin_token}
```

### **Relatório de Transparência**

```http
GET /api/admin/transparency
Authorization: Bearer {admin_token}
```

---

## 🗃️ **Modelos de Dados Principais**

### **Product**

```typescript
interface Product {
  id: string;
  name: string;
  description: string;
  price: number; // em centavos
  imageUrl?: string;
  active: boolean;
  category: string;
  stock?: number; // null = estoque ilimitado
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
}
```

### **Goal (Meta)**

```typescript
interface Goal {
  id: string;
  title: string;
  description: string;
  targetAmount: number;
  currentAmount: number;
  startDate: string;
  endDate: string;
  status: "ACTIVE" | "PAUSED" | "COMPLETED" | "CANCELLED";
  imageUrl?: string;
  createdAt: string;
  updatedAt: string;
}
```

### **Donation**

```typescript
interface Donation {
  id: string;
  amount: number;
  goalId?: string;
  anonymous: boolean;
  message?: string;
  donor: {
    name?: string;
    email?: string;
    document?: string;
  };
  paymentId: string;
  status: PaymentStatus;
  createdAt: string;
}
```

### **Order**

```typescript
interface Order {
  id: string;
  items: OrderItem[];
  customer: Customer;
  totalAmount: number;
  status: PaymentStatus;
  paymentId?: string;
  goalId?: string;
  createdAt: string;
  updatedAt: string;
}

interface OrderItem {
  productId: string;
  quantity: number;
  price: number;
}
```

---

## ⚠️ **Códigos de Erro Comuns**

| Status | Código             | Significado                        |
| ------ | ------------------ | ---------------------------------- |
| `400`  | `validation_error` | Dados de entrada inválidos         |
| `400`  | `payment_failed`   | Erro no processamento do pagamento |
| `404`  | `not_found`        | Recurso não encontrado             |
| `401`  | `unauthorized`     | Token inválido ou expirado         |
| `403`  | `forbidden`        | Sem permissão para a operação      |
| `500`  | `internal_error`   | Erro interno do servidor           |

---

## 🧪 **Exemplos de Teste**

### **Teste Básico de Conectividade**

```bash
curl http://localhost:8080/api/health
```

### **Teste de Checkout PIX**

```bash
curl -X POST http://localhost:8080/api/payments/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000,
    "description": "Teste de integração",
    "payer": {
      "name": "Test User",
      "email": "test@exemplo.com",
      "document": "11144477735"
    }
  }'
```

---

## 🚀 **Fluxos Principais para Frontend**

### **1. Fluxo de Doação Simples**

1. `GET /api/goals` → Listar metas
2. `POST /api/donations` → Criar doação
3. Processar resposta do PIX
4. `GET /api/payments/{id}/status` → Verificar status

### **2. Fluxo de Compra de Produtos**

1. `GET /api/products` → Listar produtos
2. `POST /api/orders/checkout` → Criar pedido
3. Processar pagamento PIX
4. `GET /api/orders/{id}` → Status do pedido

### **3. Fluxo de Rifa**

1. `GET /api/raffles` → Rifas ativas
2. `POST /api/raffles/{id}/tickets` → Comprar bilhetes
3. `GET /api/raffles/{id}/tickets` → Meus bilhetes

---

## 🔄 **Estados dos Pagamentos**

```
NEW → WAITING_PAYMENT → PAID
  ↓         ↓              ↑
CANCELLED  FAILED      EXPIRED
```

- **NEW:** Pagamento criado
- **WAITING_PAYMENT:** Aguardando pagamento PIX
- **PAID:** Pagamento confirmado
- **FAILED:** Erro no pagamento
- **CANCELLED:** Cancelado pelo usuário
- **EXPIRED:** Expirou sem pagamento

---

## 📱 **Integração com Frontend**

### **React/Next.js Exemplo**

```typescript
// api.ts
const API_BASE = "http://localhost:8080/api";

export const api = {
  health: () => fetch(`${API_BASE}/health`),

  createDonation: (data: DonationRequest) =>
    fetch(`${API_BASE}/payments/checkout`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    }),

  getPaymentStatus: (id: string) => fetch(`${API_BASE}/payments/${id}/status`),

  getGoals: () => fetch(`${API_BASE}/goals`),

  getProducts: (params?: ProductFilters) =>
    fetch(`${API_BASE}/products?${new URLSearchParams(params)}`),
};
```

---

## 🛠️ **Status Atual dos Services**

| Service                | Status                               | Endpoints Ativos |
| ---------------------- | ------------------------------------ | ---------------- |
| **PaymentController**  | ✅ Ativo                             | 3 endpoints PIX  |
| **HealthController**   | ✅ Ativo                             | 1 endpoint       |
| **ProductController**  | ⏸️ Service ativo, Controller inativo | 0 (preparados)   |
| **OrderController**    | ⏸️ Inativo                           | 0 (preparados)   |
| **GoalController**     | ⏸️ Inativo                           | 0 (preparados)   |
| **DonationController** | ⏸️ Inativo                           | 0 (preparados)   |
| **RaffleController**   | ⏸️ Inativo                           | 0 (preparados)   |
| **EventController**    | ⏸️ Inativo                           | 0 (preparados)   |
| **AdminController**    | ⏸️ Inativo                           | 0 (preparados)   |

**📋 Total: 4 endpoints ativos, ~25 endpoints preparados**

---

## ⏭️ **Próximos Passos**

1. **Testar APIs ativas** (Health + Payment)
2. **Reativar controllers gradualmente**
3. **Implementar autenticação frontend**
4. **Desenvolver componentes de PIX**
5. **Dashboard administrativo**

---

## 📞 **Suporte**

Para dúvidas sobre integração, consulte:

- Este documento
- Logs da aplicação: `/actuator/health`
- Testes de API: Use os exemplos curl acima

**🎯 A Tyler API está preparada para ser uma plataforma de caridade completa e transparente!**
