# 🎁 GUIA PAGSEGURO - Doações via CPF

## 🎯 Por que PagSeguro?

### ✅ **IDEAL PARA CARIDADE**

- **ACEITA CPF** - Não precisa de CNPJ
- **Taxa baixa** - PIX apenas 0,99%
- **Confiável** - Empresa consolidada no Brasil
- **Suporte nacional** - Atendimento em português

### 🚫 **Problemas dos outros:**

- Pagarme: Exige CNPJ para PIX
- Woovi: Exige CNPJ
- Mercado Pago: Taxas altas para pessoa física

---

## 🔧 Configuração Inicial

### 1. **Criar Conta PagSeguro**

```
🌐 Site: https://pagseguro.uol.com.br/
📝 Cadastro: Use seu CPF pessoal
⚡ Ativação: Imediata
```

### 2. **Obter Credenciais**

```
1. Login no PagSeguro
2. Menu "Minha Conta" → "Credenciais"
3. Copiar "Token de Aplicação"
4. Anotar o token para sandbox e produção
```

### 3. **Configurar Variáveis de Ambiente**

#### Windows (Permanente):

```batch
# Abrir variáveis de ambiente
Windows + R → sysdm.cpl → Avançado → Variáveis de Ambiente

# Adicionar variáveis:
PAGSEGURO_TOKEN_SANDBOX=seu_token_sandbox_aqui
PAGSEGURO_TOKEN=seu_token_producao_aqui
```

#### Temporário (apenas desenvolvimento):

```batch
set PAGSEGURO_TOKEN_SANDBOX=seu_token_aqui
```

---

## 📋 API Reference

### **POST /api/payments/checkout** - Criar Doação PIX

```json
{
  "amount": 1000,
  "description": "Doação para caridade",
  "payer": {
    "name": "João Doador",
    "email": "joao@email.com",
    "document": "123.456.789-00"
  }
}
```

**Response:**

```json
{
  "transaction_id": "pgs_1234567890",
  "pix_code": "00020126420014BR.GOV.BCB.PIX...",
  "qr_code_url": "data:image/png;base64,iVBORw0KGgo...",
  "amount": 1000,
  "status": "pending",
  "expires_at": "2024-01-15T23:59:59Z"
}
```

### **GET /api/payments/{id}/status** - Status da Doação

```json
{
  "transaction_id": "pgs_1234567890",
  "status": "paid",
  "amount": 1000,
  "created_at": "2024-01-15T10:00:00Z"
}
```

### **POST /api/payments/webhook** - Webhooks

```json
{
  "success": true,
  "eventType": "payment_status_change",
  "transactionId": "pgs_1234567890",
  "status": "paid"
}
```

---

## 🧪 Testes

### **1. Testar Checkout**

```bash
curl -X POST http://localhost:8080/api/payments/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000,
    "description": "Doação teste",
    "payer": {
      "name": "Teste Doador",
      "email": "teste@email.com",
      "document": "12345678900"
    }
  }'
```

### **2. Testar Status**

```bash
curl http://localhost:8080/api/payments/pgs_1234567890/status
```

### **3. Testar Health Check**

```bash
curl http://localhost:8080/actuator/health
```

---

## ⚡ Rodando o Projeto

### **Método 1: Script Automático**

```batch
# Execute o script de configuração
setup-pagseguro.bat
```

### **Método 2: Manual**

```batch
# Configurar token
set PAGSEGURO_TOKEN_SANDBOX=seu_token

# Rodar aplicação
mvnw spring-boot:run
```

### **Método 3: IntelliJ IDEA**

```
1. Abrir projeto no IntelliJ
2. Configurar variáveis de ambiente na Run Configuration
3. Executar TylerApiApplication.kt
```

---

## 🔗 URLs de Desenvolvimento

| Endpoint         | URL                                                  |
| ---------------- | ---------------------------------------------------- |
| **API Base**     | `http://localhost:8080`                              |
| **Health Check** | `http://localhost:8080/actuator/health`              |
| **Checkout PIX** | `POST http://localhost:8080/api/payments/checkout`   |
| **Status**       | `GET http://localhost:8080/api/payments/{id}/status` |
| **Webhook**      | `POST http://localhost:8080/api/payments/webhook`    |

---

## 🚀 Próximos Passos

### **1. Frontend Integration**

```javascript
// Exemplo de integração frontend
const createDonation = async (amount, donor) => {
  const response = await fetch("/api/payments/checkout", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      amount: amount * 100, // Converter para centavos
      description: "Doação para caridade",
      payer: donor,
    }),
  });
  return response.json();
};
```

### **2. Deploy na Google Cloud Run**

```bash
# Build da imagem
docker build -t tyler-api .

# Deploy no Cloud Run
gcloud run deploy tyler-api \
  --image gcr.io/PROJECT_ID/tyler-api \
  --platform managed \
  --region southamerica-east1 \
  --set-env-vars PAGSEGURO_TOKEN=token_producao
```

### **3. Configurar Webhooks em Produção**

```
URL do Webhook: https://sua-api.com/api/payments/webhook
Eventos: payment.paid, payment.refused
```

---

## 📞 Suporte

- **PagSeguro**: https://dev.pagseguro.uol.com.br/
- **Documentação**: https://dev.pagseguro.uol.com.br/docs
- **Suporte**: Conta PagSeguro → Atendimento

---

## 🎉 Conclusão

✅ **Setup completo** para doações via PIX  
✅ **Aceita CPF** - Perfeito para caridade  
✅ **Taxa baixa** - Apenas 0,99%  
✅ **API moderna** - Spring Boot + Java 21  
✅ **Pronto para produção**

**Agora você pode receber doações via PIX sem precisar de CNPJ!** 🎁
