# 🪙 Tyler API - Guia do Desenvolvedor

> **API REST moderna para pagamentos PIX com Pagarme + Firebase**

## 🚀 Quick Start (IntelliJ IDEA)

### 1. **Abrir Projeto**

```bash
# No IntelliJ IDEA:
File → Open → Selecionar: d:\Projetos\Tyler\backend\pom.xml
```

### 2. **Configurar Ambiente**

```bash
# Copiar configurações
cp .env.example .env

# Editar .env com suas credenciais
PAGARME_API_KEY=ak_test_SUA_CHAVE_AQUI
```

### 3. **Executar**

```bash
# Opção 1: Maven Tool Window
Maven → tyler-api → Plugins → exec → exec:java (duplo clique)

# Opção 2: Terminal
mvn exec:java

# Opção 3: Run Configuration "Tyler API"
Clique no botão ▶️ Run
```

### 4. **Testar**

- 🌐 **Browser:** http://localhost:8080
- 🧪 **Script:** `test-pagarme.bat`
- 📋 **Health:** http://localhost:8080/health

## 🏗️ Arquitetura

```
Tyler API
├── 🎯 Main.kt              # Servidor HTTP + Roteamento
├── 💳 PagarmeProvider.kt   # Integração PIX Pagarme
├── 🔥 Firebase             # Autenticação + Database
└── 📊 Endpoints REST       # API para Frontend
```

## 📡 Endpoints

| Método | Endpoint                   | Descrição             |
| ------ | -------------------------- | --------------------- |
| `GET`  | `/`                        | Página inicial da API |
| `GET`  | `/health`                  | Status da API         |
| `POST` | `/api/checkout`            | Criar pagamento PIX   |
| `GET`  | `/api/payment/status/{id}` | Consultar status      |
| `POST` | `/api/webhooks/payment`    | Webhook Pagarme       |

## 🧪 Exemplo - Criar PIX

```bash
curl -X POST http://localhost:8080/api/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 50.00,
    "description": "Produto Tyler",
    "payer": {
      "name": "João Silva",
      "email": "joao@teste.com",
      "cpf": "19119119100"
    }
  }'
```

**Resposta:**

```json
{
  "success": true,
  "paymentId": "tran_abc123",
  "amount": 50.0,
  "pixQrCode": "00020126580014br.gov.bcb.pix...",
  "pixCopyPaste": "00020126580014br.gov.bcb.pix...",
  "status": "pending",
  "expirationDate": "2025-10-30T23:59:59Z",
  "provider": "pagarme"
}
```

## 🔧 Configuração

### **Pagarme Setup**

1. **Cadastro:** https://dashboard.pagar.me/
2. **API Key:** Dashboard → Configurações → API Keys
3. **Teste:** Use `ak_test_...` para desenvolvimento
4. **Produção:** Use `ak_live_...` para produção

### **Firebase Setup**

1. **Console:** https://console.firebase.google.com/
2. **Projeto:** Criar projeto "tyler-project"
3. **Firestore:** Ativar banco de dados
4. **Admin SDK:** Baixar `firebase-admin-sdk.json`

### **Variáveis (.env)**

```env
# Core
FIREBASE_PROJECT_ID=projeto-tyler
PAYMENT_PROVIDER=pagarme
PORT=8080

# Pagarme
PAGARME_API_KEY=ak_test_SUA_CHAVE
PAGARME_WEBHOOK_SECRET=seu_secret

# URLs
PUBLIC_SITE_URL=http://localhost:5173
```

## 🛠️ Desenvolvimento

### **Estrutura de Pastas**

```
src/main/kotlin/com/tylerproject/
├── Main.kt                    # 🎯 Servidor principal
└── providers/
    └── PagarmeProvider.kt     # 💳 Provider PIX
```

### **Tecnologias**

- **Kotlin 1.9.21** - Linguagem principal
- **Maven** - Build system
- **OkHttp** - Cliente HTTP
- **Firebase Admin SDK** - Backend Firebase
- **Kotlinx Serialization** - JSON
- **Java HttpServer** - Servidor HTTP nativo

### **Hot Reload**

1. **Fazer alterações** no código
2. **Recompilar:** Ctrl+F9 (IntelliJ)
3. **Restart:** Ctrl+F2 + ▶️

## 🧪 Testes

### **Automatizados**

```bash
# Windows
test-pagarme.bat

# Manual
curl -X GET http://localhost:8080/health
```

### **Interface Web**

- **Abrir:** `test-pix.html` no navegador
- **Testar:** Criação de PIX interativo

## 🚀 Deploy

### **JAR Executável**

```bash
mvn clean package
java -jar target/tyler-api-1.0.0.jar
```

### **Docker** (futuro)

```bash
docker build -t tyler-api .
docker run -p 8080:8080 tyler-api
```

## 🐛 Debugging

### **Logs**

- ✅ Todas as operações são logadas
- 📤 **Request:** Payload recebido
- 📥 **Response:** Resposta da Pagarme
- ❌ **Errors:** Stack trace completo

### **IntelliJ Debug**

1. **Breakpoint:** Clique na margem esquerda
2. **Debug Mode:** 🐛 ao invés de ▶️
3. **Variáveis:** Inspect na aba Debug

## 📈 Performance

- **Startup:** ~3-5 segundos
- **PIX Creation:** ~200-500ms
- **Memory:** ~100-200MB RAM
- **Concurrent:** Suporta múltiplas requisições

## 🔐 Segurança

- ✅ **API Keys** via variáveis de ambiente
- ✅ **CORS** configurado
- ✅ **Webhook signature** validation
- ⚠️ **HTTPS** necessário em produção

## 📞 Suporte

### **Problemas Comuns**

- **"Cannot find MainKt":** Rebuild Project
- **"Port already in use":** Matar processo na porta 8080
- **"Firebase not found":** Verificar FIREBASE_PROJECT_ID

### **Links Úteis**

- 📖 **Pagarme Docs:** https://docs.pagar.me/
- 🔥 **Firebase Docs:** https://firebase.google.com/docs
- 🏗️ **Maven Docs:** https://maven.apache.org/guides/

---

## 🎯 Roadmap

- [x] ✅ **PIX Pagarme** - Integração completa
- [x] ✅ **Maven Build** - Sistema de build
- [x] ✅ **IntelliJ Setup** - Configuração IDE
- [ ] 🔄 **Frontend Integration** - Conectar com UI
- [ ] 📡 **Webhooks** - Notificações em tempo real
- [ ] 🚀 **Deploy AWS** - Produção cloud

**🪙 Happy Coding com Tyler API!**
