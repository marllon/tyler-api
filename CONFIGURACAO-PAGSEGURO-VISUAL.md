# 🎁 GUIA VISUAL - Configuração PagSeguro

## 🎯 **POR QUE PAGSEGURO?**

```
✅ ACEITA CPF (não precisa CNPJ)
✅ Ideal para CARIDADE e DOAÇÕES
✅ Taxa PIX: apenas 0,99%
✅ Empresa confiável (UOL)
✅ API simples
```

---

## 📋 **PASSO 1: Criar Conta**

### 🌐 **1.1 - Acessar Site**

```
URL: https://pagseguro.uol.com.br
Botão: "Criar conta"
```

### 📝 **1.2 - Dados Necessários**

```
📄 CPF (seus dados pessoais)
👤 Nome completo
📧 Email
📱 Telefone
🎂 Data nascimento
```

### ✅ **1.3 - Ativação**

```
1. Confirmar email recebido
2. Clicar no link de ativação
3. Fazer primeiro login
```

---

## 🔑 **PASSO 2: Obter Credenciais**

### 🔐 **2.1 - Acessar Credenciais**

```
1. Login no PagSeguro
2. Menu "Minha Conta"
3. Opção "Credenciais"
4. Seção "Integração"
```

### 📋 **2.2 - Copiar Token**

```
🎯 Token de Aplicação
📝 Formato: pagseguro_xxxxxxx...
💾 Copiar e guardar em local seguro
```

---

## ⚙️ **PASSO 3: Configurar Projeto**

### 🔧 **3.1 - Método Automático**

```batch
# Execute o assistente:
configure-pagseguro.bat
```

### 🛠️ **3.2 - Método Manual**

#### **Windows CMD:**

```cmd
set PAGSEGURO_TOKEN_SANDBOX=seu_token_aqui
mvn spring-boot:run
```

#### **PowerShell:**

```powershell
$env:PAGSEGURO_TOKEN_SANDBOX="seu_token_aqui"
mvn spring-boot:run
```

#### **IntelliJ IDEA:**

```
1. Run → Edit Configurations
2. Environment Variables
3. Adicionar: PAGSEGURO_TOKEN_SANDBOX = seu_token
4. Apply → OK
5. Run TylerApiApplication
```

---

## 🧪 **PASSO 4: Testar Integração**

### 🚀 **4.1 - Iniciar API**

```bash
mvn spring-boot:run
```

### 📡 **4.2 - Testar Health Check**

```bash
curl http://localhost:8080/actuator/health
```

**Response esperado:**

```json
{ "status": "UP" }
```

### 💰 **4.3 - Testar Checkout PIX**

```bash
curl -X POST http://localhost:8080/api/payments/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000,
    "description": "Doação teste",
    "payer": {
      "name": "João Doador",
      "email": "joao@email.com",
      "document": "12345678900"
    }
  }'
```

**Response esperado:**

```json
{
  "transaction_id": "pgs_123456789",
  "pix_code": "00020126420014BR.GOV.BCB.PIX...",
  "qr_code_url": "data:image/png;base64,iVBORw0KGgo...",
  "amount": 1000,
  "status": "pending",
  "expires_at": "2024-12-01T23:59:59Z"
}
```

---

## 🔧 **TROUBLESHOOTING**

### ❌ **Erro: Token inválido**

```
Problema: Token incorreto ou ambiente errado
Solução:
1. Verificar se copiou token correto
2. Usar token SANDBOX para testes
3. Verificar se variável está configurada
```

### ❌ **Erro: Compilação falha**

```
Problema: Dependências ou configuração
Solução:
1. mvn clean compile
2. Verificar Java 21 instalado
3. Verificar internet para dependencies
```

### ❌ **Erro: Porta ocupada**

```
Problema: Porta 8080 em uso
Solução:
1. Fechar outras aplicações
2. Ou usar: mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

---

## 🎉 **SUCESSO!**

### ✅ **Quando estiver funcionando:**

```
🚀 API rodando em: http://localhost:8080
✅ Health check: {"status":"UP"}
✅ Checkout PIX funcionando
✅ Pronto para receber doações!
```

### 🎁 **Vantagens obtidas:**

```
✅ CPF aceito (sem CNPJ)
✅ Taxa baixa (0,99%)
✅ Ideal para caridade
✅ API moderna (Spring Boot)
✅ Escalável (Cloud Run ready)
```

---

## 📞 **Suporte**

- **PagSeguro**: https://dev.pagseguro.uol.com.br/
- **Documentação**: Incluída no projeto
- **Scripts**: configure-pagseguro.bat, setup-pagseguro.bat
