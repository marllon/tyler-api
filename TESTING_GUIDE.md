# 🧪 Guia de Testes - PIX Integration Tyler

## 🎯 O que você precisa fazer para testar

### 1. 📋 **Pré-requisitos**

✅ **Java 17+** instalado
✅ **Gradle** (ou usar o wrapper)
✅ **Conta Mercado Pago** (pode ser de teste)
✅ **Firebase projeto** configurado

### 2. 🔑 **Configurar Mercado Pago (AMBIENTE DE TESTE)**

#### Passo 1: Criar conta de desenvolvedor

```bash
# 1. Acesse:
https://mercadopago.com.br/developers

# 2. Crie uma conta (gratuita)
# 3. Vá em "Suas integrações" → "Criar aplicação"
# 4. Nome: "Tyler Test"
# 5. Selecione: "Pagamentos online"
```

#### Passo 2: Pegar credenciais de TESTE

```bash
# No painel do Mercado Pago:
# → Suas integrações → Tyler Test → Credenciais

# Copie o ACCESS TOKEN de TESTE (começa com TEST-)
# Exemplo: TEST-1234567890123456-103112-abcdef1234567890-123456789
```

### 3. ⚙️ **Configurar Projeto**

#### Criar arquivo `.env` na pasta `backend/`

```env
# Firebase
FIREBASE_PROJECT_ID=projeto-tyler
FIREBASE_REGION=southamerica-east1
USE_FIREBASE_AUTH=true

# PIX via Mercado Pago (TESTE)
PAYMENT_PROVIDER=pix
MP_ACCESS_TOKEN=TEST-1234567890123456-103112-abcdef1234567890-123456789
MP_WEBHOOK_SECRET=test-webhook-secret-123

# URLs
PUBLIC_SITE_URL=http://localhost:5173
ALLOWED_ORIGINS=http://localhost:5173,http://localhost:8080

# Outros
DEFAULT_CURRENCY=BRL
```

#### Configurar Firebase Admin SDK

```bash
# 1. Firebase Console → Project Settings → Service Accounts
# 2. "Generate New Private Key"
# 3. Salvar como: backend/config/firebase-admin-sdk.json
```

### 4. 🚀 **Executar o Projeto**

```bash
# 1. Navegar para a pasta backend
cd backend

# 2. Instalar dependências e compilar
./gradlew build

# 3. Executar a API
./gradlew run

# Deve aparecer algo como:
# > Tyler API running on port 8080
# > PIX Provider initialized with TEST credentials
```

### 5. 🧪 **Testes Manuais**

#### Teste 1: Criar pagamento PIX

```bash
# Terminal/CMD:
curl -X POST http://localhost:8080/api/checkout \
  -H "Content-Type: application/json" \
  -d "{
    \"type\": \"product\",
    \"amount\": 10.50,
    \"quantity\": 1,
    \"description\": \"Teste PIX Tyler\",
    \"customer\": {
      \"name\": \"João Teste\",
      \"lastName\": \"Silva\",
      \"email\": \"joao@teste.com\",
      \"document\": \"12345678901\",
      \"phone\": \"11987654321\"
    },
    \"productId\": \"test-product-123\"
  }"
```

#### Resposta esperada:

```json
{
  "success": true,
  "orderId": "generated-order-id",
  "paymentId": "mp-payment-id",
  "amount": 10.5,
  "pixQrCode": "00020126580014br.gov.bcb.pix...",
  "pixQrCodeBase64": "iVBORw0KGgoAAAANSUhEUgAA...",
  "pixCopyPaste": "00020126580014br.gov.bcb.pix...",
  "expirationDate": "2024-12-31T23:59:59Z",
  "instructions": "Abra seu app de banco, escaneie o QR Code..."
}
```

### 6. 📱 **Testar PIX (Ambiente de Teste)**

#### Opção A: Simular pagamento via API

```bash
# Pegar o paymentId da resposta anterior
# Simular aprovação do pagamento:

curl -X PUT "https://api.mercadopago.com/v1/payments/PAYMENT_ID" \
  -H "Authorization: Bearer SEU_ACCESS_TOKEN_TESTE" \
  -H "Content-Type: application/json" \
  -d '{"status": "approved"}'
```

#### Opção B: App Mercado Pago Teste

```bash
# 1. Baixar "Mercado Pago Point" (Android/iOS)
# 2. Login com conta de teste
# 3. Escanear QR Code gerado
# 4. Simular pagamento
```

### 7. 🔍 **Verificar Resultado**

#### Teste 2: Consultar status do pagamento

```bash
curl -X GET http://localhost:8080/api/payment/status/PAYMENT_ID
```

#### Resposta esperada:

```json
{
  "success": true,
  "orderId": "order-id",
  "paymentId": "payment-id",
  "status": "approved", // ou "pending", "cancelled"
  "amount": 10.5,
  "currency": "BRL",
  "paymentMethod": "pix",
  "paidAt": "2024-01-01T12:00:00Z",
  "pixEndToEndId": "E12345678901234567890123456789012"
}
```

### 8. 🎮 **Interface de Teste (HTML)**

Criar arquivo `test-pix.html` para testar visualmente:

```html
<!DOCTYPE html>
<html>
  <head>
    <title>Teste PIX Tyler</title>
    <style>
      body {
        font-family: Arial;
        margin: 40px;
      }
      .container {
        max-width: 600px;
      }
      button {
        padding: 10px 20px;
        background: #00d4aa;
        color: white;
        border: none;
        cursor: pointer;
      }
      .qr-code {
        margin: 20px 0;
      }
      .pix-code {
        width: 100%;
        padding: 10px;
        margin: 10px 0;
      }
    </style>
  </head>
  <body>
    <div class="container">
      <h1>🪙 Teste PIX - Tyler</h1>

      <h3>1. Criar Pagamento</h3>
      <button onclick="createPixPayment()">Criar PIX R$ 10,50</button>

      <div id="result" style="margin-top: 20px;"></div>

      <script>
        async function createPixPayment() {
          const orderData = {
            type: "product",
            amount: 10.5,
            quantity: 1,
            description: "Teste PIX Tyler",
            customer: {
              name: "João Teste",
              lastName: "Silva",
              email: "joao@teste.com",
              document: "12345678901",
              phone: "11987654321",
            },
            productId: "test-product-123",
          };

          try {
            const response = await fetch("http://localhost:8080/api/checkout", {
              method: "POST",
              headers: {
                "Content-Type": "application/json",
              },
              body: JSON.stringify(orderData),
            });

            const result = await response.json();

            if (result.success) {
              document.getElementById("result").innerHTML = `
                        <h3>✅ PIX Criado com Sucesso!</h3>
                        <p><strong>Order ID:</strong> ${result.orderId}</p>
                        <p><strong>Payment ID:</strong> ${result.paymentId}</p>
                        <p><strong>Valor:</strong> R$ ${result.amount}</p>
                        
                        <h4>QR Code:</h4>
                        <img src="data:image/png;base64,${result.pixQrCodeBase64}" alt="QR Code PIX" class="qr-code">
                        
                        <h4>Código PIX (Copiar e Colar):</h4>
                        <textarea class="pix-code" readonly>${result.pixCopyPaste}</textarea>
                        
                        <h4>Instruções:</h4>
                        <p>${result.instructions}</p>
                        
                        <button onclick="checkPaymentStatus('${result.paymentId}')">Verificar Status</button>
                    `;
            } else {
              document.getElementById("result").innerHTML = `
                        <h3>❌ Erro ao criar PIX</h3>
                        <p>${result.error}</p>
                    `;
            }
          } catch (error) {
            document.getElementById("result").innerHTML = `
                    <h3>❌ Erro de conexão</h3>
                    <p>${error.message}</p>
                    <p>Verifique se a API está rodando em localhost:8080</p>
                `;
          }
        }

        async function checkPaymentStatus(paymentId) {
          try {
            const response = await fetch(
              `http://localhost:8080/api/payment/status/${paymentId}`
            );
            const result = await response.json();

            alert(`Status: ${result.status}\nValor: R$ ${result.amount}`);
          } catch (error) {
            alert("Erro ao verificar status: " + error.message);
          }
        }
      </script>
    </div>
  </body>
</html>
```

### 9. ✅ **Checklist de Testes**

#### Testes Básicos:

- [ ] API inicia sem erros
- [ ] Endpoint `/api/checkout` responde
- [ ] QR Code é gerado
- [ ] Order é criada no Firestore
- [ ] Webhook processa corretamente

#### Testes de Erro:

- [ ] Valor inválido (0 ou negativo)
- [ ] Email inválido
- [ ] CPF inválido
- [ ] Produto inexistente

#### Testes de Integração:

- [ ] Pagamento aprovado atualiza status
- [ ] Estoque é reduzido (produtos)
- [ ] Meta é atualizada (doações)
- [ ] Tickets são gerados (rifas)

### 10. 🐛 **Troubleshooting Comum**

#### Erro: "Access Token inválido"

```bash
# Verifique se o token está correto e é de TESTE
# Deve começar com: TEST-
```

#### Erro: "Firebase não configurado"

```bash
# Verifique se firebase-admin-sdk.json está na pasta config/
# Verifique se FIREBASE_PROJECT_ID está correto
```

#### Erro: "Port 8080 já em uso"

```bash
# Matar processo na porta 8080:
# Windows:
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac:
lsof -ti:8080 | xargs kill -9
```

#### Erro: "CORS"

```bash
# Adicionar localhost nas ALLOWED_ORIGINS:
ALLOWED_ORIGINS=http://localhost:5173,http://localhost:8080,http://localhost:3000
```

### 11. 📊 **Logs para Acompanhar**

Durante os testes, acompanhe os logs da aplicação:

```bash
# Console deve mostrar:
[INFO] Tyler API iniciada na porta 8080
[INFO] PIX Provider configurado com token TEST-***
[INFO] Firestore conectado ao projeto: projeto-tyler
[INFO] POST /api/checkout - Criando pagamento PIX
[INFO] PIX criado com sucesso: payment_id_123
[INFO] Order salva no Firestore: order_id_456
[INFO] Webhook recebido para payment_id_123
[INFO] Payment aprovado - Status atualizado
```

### 🎯 **Resultado Esperado**

Se tudo estiver funcionando:

1. ✅ API roda em `localhost:8080`
2. ✅ POST para `/api/checkout` retorna QR Code
3. ✅ QR Code pode ser escaneado
4. ✅ Pagamento de teste é aprovado
5. ✅ Status é atualizado via webhook
6. ✅ Order fica com status "paid" no Firestore

**Pronto! Seu PIX está funcionando! 🚀**
