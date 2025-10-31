# 🧪 Testes da API Tyler - PagBank PIX

## 📋 Endpoints Disponíveis

### 1. 🏥 Health Check

```bash
curl http://localhost:8080/api/health
```

### 2. 📊 Actuator Info

```bash
curl http://localhost:8080/api/info
```

### 3. 💳 Criar PIX (Checkout)

```bash
curl -X POST http://localhost:8080/api/payments/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000,
    "description": "Doação para caridade - Teste Local",
    "payer": {
      "name": "João Silva",
      "email": "joao@teste.com",
      "document": "12345678901"
    }
  }'
```

### 4. 📋 Status do Pagamento

```bash
# Substitua {PAYMENT_ID} pelo ID retornado no checkout
curl http://localhost:8080/api/payments/{PAYMENT_ID}/status
```

## 🎯 Teste Completo Passo-a-Passo

### Passo 1: Health Check

```powershell
curl http://localhost:8080/api/health
```

**Esperado**: Status 200, `"status":"healthy"`

### Passo 2: Criar PIX de R$ 10,00

```powershell
curl -X POST http://localhost:8080/api/payments/checkout `
  -H "Content-Type: application/json" `
  -d '{
    "amount": 1000,
    "description": "Teste PIX Local - R$ 10,00",
    "payer": {
      "name": "Teste Tyler",
      "email": "teste@tyler.com",
      "document": "12345678901"
    }
  }'
```

**Esperado**:

- Status 200
- `transaction_id` (ID do PagBank)
- `pix_code` (código PIX para copiar/colar)
- `qr_code_url` (URL da imagem QR Code)

### Passo 3: Verificar Status

```powershell
# Use o transaction_id retornado no passo 2
curl http://localhost:8080/api/payments/TRANSACTION_ID_AQUI/status
```

**Esperado**: Status da transação PagBank

## 🔍 Validações Importantes

### ✅ **Cenários de Sucesso**

1. **Health Check** → 200 OK
2. **PIX válido** → 200 + QR Code
3. **Status válido** → 200 + Status PagBank

### ❌ **Cenários de Erro**

1. **Payload inválido** → 400 Bad Request
2. **Valor zerado** → 400 Bad Request
3. **Token inválido** → 401/403 PagBank

## 📱 Como Usar o PIX Gerado

1. **Copie o `pix_code`** do response
2. **Abra seu banco** (app ou internet banking)
3. **PIX → Pagar → Pix Copia e Cola**
4. **Cole o código** e confirme
5. **Verifique o status** com o endpoint `/status`

## 🛠️ Troubleshooting

### Token PagBank

```bash
# Verificar se token está sendo carregado
curl http://localhost:8080/api/actuator/env | grep PAGBANK
```

### Logs da Aplicação

- Verifique logs no terminal onde rodou `mvn spring-boot:run`
- Procure por: `🛠️ Configurando PagBank`

### Connectivity

```bash
# Testar conectividade com PagBank
curl https://sandbox.api.pagseguro.com/orders -I
```
