# 🪙 Guia de Integração PIX - Projeto Tyler

## 🎯 Resumo da Implementação

Implementei a integração completa do PIX como método de pagamento principal para o projeto Tyler utilizando a API do Mercado Pago. O PIX é o método de pagamento instantâneo mais utilizado no Brasil.

## 🏗️ Arquitetura PIX Implementada

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Frontend      │────│   Tyler API      │────│  Mercado Pago   │
│   (QR Code)     │    │   (PixProvider)  │    │   (PIX API)     │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │
                                │
                       ┌──────────────────┐
                       │   Firestore      │
                       │   (Orders)       │
                       └──────────────────┘
```

## 📁 Arquivos Criados/Modificados

### ✅ Criados:

- `src/providers/PixProvider.kt` - Provider específico para PIX
- `SETUP_GUIDE.md` - Guia de configuração completo

### ✅ Modificados:

- `src/config/Config.kt` - PIX como padrão
- `src/providers/PaymentProvider.kt` - Campos PIX adicionados
- `src/models/DTOs.kt` - DTOs para checkout PIX
- `src/handlers/Handlers.kt` - CheckoutHandler e WebhookHandler

## 🔧 Como Funciona

### 1. **Criação do Pagamento PIX**

**Endpoint:** `POST /api/checkout`

**Request:**

```json
{
  "type": "product", // "product", "raffle", "donation"
  "amount": 49.9,
  "quantity": 1,
  "description": "Camiseta Tyler",
  "customer": {
    "name": "João Silva",
    "email": "joao@email.com",
    "document": "12345678901", // CPF
    "phone": "11987654321"
  },
  "productId": "prod123" // ou raffleId ou goalId
}
```

**Response:**

```json
{
  "success": true,
  "orderId": "order123",
  "paymentId": "mp_payment456",
  "amount": 49.9,
  "pixQrCode": "00020126580014br.gov.bcb.pix...",
  "pixQrCodeBase64": "iVBORw0KGgoAAAANSUhEUgAA...",
  "pixCopyPaste": "00020126580014br.gov.bcb.pix...",
  "expirationDate": "2024-01-01T23:59:59Z",
  "instructions": "Abra seu app de banco, escaneie o QR Code..."
}
```

### 2. **Processamento do Webhook**

**Endpoint:** `POST /api/webhooks/payment`

Quando o PIX é pago, o Mercado Pago envia uma notificação que:

1. ✅ Verifica a assinatura
2. ✅ Atualiza o status do pedido
3. ✅ Processa ações específicas (gerar tickets, atualizar estoque, etc.)

## 🚀 Configuração Necessária

### 1. **Mercado Pago Setup**

```bash
# 1. Criar conta no Mercado Pago
https://mercadopago.com.br/developers

# 2. Criar aplicação
- Nome: "Tyler Projeto"
- Categoria: "Caridade e ONGs"

# 3. Pegar credenciais
```

### 2. **Variáveis de Ambiente**

```env
# PIX via Mercado Pago
PAYMENT_PROVIDER=pix
MP_ACCESS_TOKEN=APP_USR-1234567890-123456-abcdef...
MP_WEBHOOK_SECRET=seu-webhook-secret

# URLs
PUBLIC_SITE_URL=https://tylerlimaeler.org
ALLOWED_ORIGINS=https://tylerlimaeler.org,http://localhost:5173
```

### 3. **Webhook Configuration**

Configure no painel do Mercado Pago:

- **URL:** `https://sua-api.com/api/webhooks/payment`
- **Eventos:** `payment.created`, `payment.updated`

## 💳 Fluxo Completo PIX

### Frontend → Backend:

1. **Usuário** clica em "Comprar com PIX"
2. **Frontend** envia dados para `/api/checkout`
3. **Backend** cria pagamento no Mercado Pago
4. **Backend** retorna QR Code e dados PIX
5. **Frontend** exibe QR Code para o usuário

### Pagamento → Confirmação:

6. **Usuário** paga PIX no banco
7. **Mercado Pago** envia webhook para backend
8. **Backend** verifica e atualiza pedido
9. **Backend** processa ação (gerar ticket, etc.)

## 📊 Vantagens do PIX

### ✅ **Para o Projeto:**

- **Instantâneo** - Confirmação em segundos
- **Baixo custo** - Tarifas menores que cartão
- **Brasileiro** - Método preferido no Brasil
- **Simples** - QR Code + celular

### ✅ **Para o Usuário:**

- **Rápido** - Não precisa digitar dados
- **Seguro** - Criptografia bancária
- **24/7** - Funciona sempre
- **Gratuito** - Sem custo para pessoa física

## 🔒 Segurança Implementada

### ✅ **Validações:**

- Verificação de assinatura HMAC no webhook
- Validação de valores mínimos/máximos
- Verificação de CPF/CNPJ
- Idempotência para evitar duplicatas

### ✅ **Monitoramento:**

- Logs de todas as transações
- Alertas para falhas de pagamento
- Backup automático de pedidos

## 📝 Endpoints da API PIX

### **Criar Pagamento PIX**

```http
POST /api/checkout
Content-Type: application/json

{
  "type": "product",
  "amount": 49.90,
  "description": "Produto Tyler",
  "customer": {
    "name": "Nome",
    "email": "email@domain.com",
    "document": "12345678901"
  },
  "productId": "prod123"
}
```

### **Consultar Status**

```http
GET /api/payment/status/{paymentId}
```

### **Webhook (Mercado Pago)**

```http
POST /api/webhooks/payment
X-Signature: {signature}

{
  "action": "payment.updated",
  "data": {
    "id": "123456789"
  }
}
```

## 🚀 Próximos Passos

### 1. **Configurar Mercado Pago**

- Criar conta de desenvolvedor
- Gerar Access Token
- Configurar webhook

### 2. **Deploy**

```bash
# Build da aplicação
./gradlew shadowJar

# Deploy
gcloud run deploy tyler-api --source .
```

### 3. **Testar**

```bash
# Ambiente de testes do Mercado Pago
# Use cartões de teste para simular PIX
```

## 🎮 Testando a Integração

### **1. Ambiente Local**

```bash
# 1. Configure as variáveis
export MP_ACCESS_TOKEN="TEST-..."
export PAYMENT_PROVIDER="pix"

# 2. Execute a API
./gradlew run

# 3. Teste o endpoint
curl -X POST http://localhost:8080/api/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "type": "product",
    "amount": 10.00,
    "description": "Teste PIX",
    "customer": {
      "name": "Teste",
      "email": "teste@email.com",
      "document": "12345678901"
    },
    "productId": "test123"
  }'
```

### **2. Interface Frontend**

```javascript
// Exemplo de integração no frontend
async function createPixPayment(orderData) {
  const response = await fetch("/api/checkout", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(orderData),
  });

  const result = await response.json();

  if (result.success) {
    // Exibir QR Code
    document.getElementById(
      "qr-code"
    ).src = `data:image/png;base64,${result.pixQrCodeBase64}`;

    // Código para copiar
    document.getElementById("pix-code").value = result.pixCopyPaste;

    // Polling para verificar pagamento
    checkPaymentStatus(result.paymentId);
  }
}
```

## 🎯 Resultado Final

Com essa implementação, o projeto Tyler agora tem:

✅ **PIX como método principal** de pagamento
✅ **QR Code dinâmico** gerado em tempo real  
✅ **Webhook automático** para confirmações
✅ **Integração completa** com Firestore
✅ **Segurança bancária** via Mercado Pago
✅ **Processamento instantâneo** de pedidos

**O PIX é agora o coração do sistema de pagamentos do Tyler! 🚀**
