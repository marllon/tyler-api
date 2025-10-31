# 🏦 GUIA COMPLETO - Configuração PagBank API

## 🎯 **SITUAÇÃO ATUAL: PagSeguro = PagBank**

```
✅ PagSeguro virou PagBank
✅ API completa disponível
✅ PIX integração oficial
✅ Aceita CPF (pessoa física)
✅ Ideal para doações
```

---

## 📋 **PASSO 1: Criar Conta de Desenvolvedor**

### 🌐 **1.1 - Portal do Desenvolvedor**

```
URL: https://portaldev.pagbank.com.br/
Ação: "Create account" / "Criar conta"
```

### 📝 **1.2 - Dados Necessários**

```
📄 CPF (seus dados pessoais)
👤 Nome completo
📧 Email
📱 Telefone
🏢 Nome da empresa/projeto
💼 Tipo: Desenvolvedor
```

### ✅ **1.3 - Confirmação**

```
1. Confirmar email recebido
2. Fazer login no portal
3. Aceitar os termos
```

---

## 🔑 **PASSO 2: Obter Token de Autenticação**

### 🧪 **2.1 - Token SANDBOX (para testes)**

```
1. Login em: https://portaldev.pagbank.com.br/
2. Aba "Tokens"
3. Copiar o Token de Autenticação
4. Formato: Bearer {seu_token_aqui}
```

### 🚀 **2.2 - Token PRODUÇÃO (quando aprovado)**

```
1. Login em: https://acesso.pagseguro.uol.com.br/
2. Menu lateral: "Vender online"
3. Opção: "Integrações"
4. Botão: "Gerar Token"
```

---

## ⚙️ **PASSO 3: Configurar Tyler API**

### 🔧 **3.1 - Atualizar Variáveis de Ambiente**

**Windows CMD:**

```cmd
set PAGBANK_TOKEN_SANDBOX=seu_token_sandbox_aqui
set PAGBANK_TOKEN=seu_token_producao_aqui
mvn spring-boot:run
```

**PowerShell:**

```powershell
$env:PAGBANK_TOKEN_SANDBOX="seu_token_sandbox_aqui"
$env:PAGBANK_TOKEN="seu_token_producao_aqui"
mvn spring-boot:run
```

**IntelliJ IDEA:**

```
1. Run → Edit Configurations
2. Environment Variables → Add:
   - PAGBANK_TOKEN_SANDBOX = seu_token_sandbox
   - PAGBANK_TOKEN = seu_token_producao
3. Apply → OK → Run
```

---

## 🔄 **PASSO 4: Atualizar Tyler API para PagBank**

### 📝 **4.1 - Estrutura da API PagBank**

**Endpoint Base:**

```
Sandbox: https://sandbox.api.pagseguro.com
Produção: https://api.pagseguro.com
```

**Headers de Autenticação:**

```
Authorization: Bearer {seu_token}
Content-Type: application/json
Accept: application/json
```

**PIX via Orders API:**

```
POST /orders
{
  "reference_id": "DOA_123456789",
  "customer": {
    "name": "João Doador",
    "email": "joao@email.com",
    "tax_id": "12345678900"
  },
  "items": [
    {
      "reference_id": "ITEM_001",
      "name": "Doação para caridade",
      "quantity": 1,
      "unit_amount": 1000
    }
  ],
  "charges": [
    {
      "reference_id": "CHARGE_001",
      "description": "Doação PIX",
      "amount": {
        "value": 1000,
        "currency": "BRL"
      },
      "payment_method": {
        "type": "PIX",
        "pix": {
          "expiration_date": "2024-12-01T23:59:59-03:00"
        }
      }
    }
  ]
}
```

---

## 🧪 **PASSO 5: Testar Integração**

### 🚀 **5.1 - Iniciar Tyler API**

```bash
cd "d:\Projetos\Tyler\backend"
mvn spring-boot:run
```

### 📡 **5.2 - Teste Health Check**

```bash
curl http://localhost:8080/actuator/health
```

### 💰 **5.3 - Teste Checkout PIX**

```bash
curl -X POST http://localhost:8080/api/payments/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000,
    "description": "Doação teste PagBank",
    "payer": {
      "name": "João Doador",
      "email": "joao@email.com",
      "document": "12345678900"
    }
  }'
```

---

## 🔧 **PASSO 6: Atualizar Provider para PagBank**

Precisamos atualizar nosso código para usar a **API real do PagBank**:

### 📝 **6.1 - URL Base Correta**

```kotlin
// Sandbox
private const val SANDBOX_URL = "https://sandbox.api.pagseguro.com"
// Produção
private const val PRODUCTION_URL = "https://api.pagseguro.com"
```

### 🔐 **6.2 - Headers de Autenticação**

```kotlin
private fun getAuthHeaders(): Headers {
    return Headers.Builder()
        .add("Authorization", "Bearer $token")
        .add("Content-Type", "application/json")
        .add("Accept", "application/json")
        .build()
}
```

### 💰 **6.3 - Endpoint PIX**

```kotlin
// POST /orders para criar PIX
val httpRequest = Request.Builder()
    .url("$baseUrl/orders")
    .headers(getAuthHeaders())
    .post(requestBody)
    .build()
```

---

## 📞 **PASSO 7: Suporte e Homologação**

### 🆘 **7.1 - Se Precisar de Ajuda**

```
🌐 Portal: https://developer.pagbank.com.br/
📞 Suporte: https://app.pipefy.com/public/form/sBlh9Nq6
📚 Documentação: https://dev.pagbank.uol.com.br/
💬 Fórum: https://dev.pagbank.uol.com.br/discuss
```

### ✅ **7.2 - Processo de Homologação**

```
1. Testar tudo no SANDBOX
2. Documentar integrações
3. Solicitar homologação
4. Aguardar aprovação do PagBank
5. Receber acesso à PRODUÇÃO
```

---

## 🎉 **RESUMO - Como Configurar PagBank**

### **OPÇÃO 1: Rápido** ⚡

1. **Criar conta**: https://portaldev.pagbank.com.br/
2. **Pegar token**: Aba "Tokens"
3. **Configurar**: `set PAGBANK_TOKEN_SANDBOX=seu_token`
4. **Testar**: `mvn spring-boot:run`

### **OPÇÃO 2: Completo** 🔄

1. **Atualizar código** para API real PagBank
2. **Testar no sandbox**
3. **Solicitar homologação**
4. **Deploy em produção**

---

## 🚨 **PRÓXIMOS PASSOS**

1. **🔄 Atualizar PagSeguroProvider** para usar API real PagBank
2. **🧪 Testar integração** com token real
3. **📝 Documentar** processo de homologação
4. **🚀 Preparar deploy** para produção

---

## 💡 **VANTAGENS DO PAGBANK**

```
✅ API oficial e documentada
✅ Aceita CPF (pessoa física)
✅ PIX com boa taxa
✅ Suporte técnico disponível
✅ Processo de homologação claro
✅ Ideal para doações/caridade
```

**Agora temos o caminho correto para integrar com PagBank!** 🎯
