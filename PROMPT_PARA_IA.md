# 📋 PROMPT PARA IA - Estado Atual da Tyler API

**Data de Referência**: Outubro 2025  
**Contexto**: API Backend completa e funcional para projeto de caridade

## 🎯 **OBJETIVO DO PROJETO**

Você está assumindo o desenvolvimento de uma **API backend moderna** para o **Projeto Tyler**, uma plataforma de caridade focada em doações via PIX. A API está **100% funcional** e pronta para produção, com integração completa ao PagBank para pagamentos PIX.

## 🏗️ **ARQUITETURA ATUAL**

### **Stack Tecnológica**

- **Framework**: Spring Boot 3.2.5 com Kotlin 1.9.21
- **Runtime**: Java 21 com Virtual Threads
- **Build**: Maven (migrado do Gradle para compatibilidade Windows)
- **Database**: Google Cloud Firestore
- **Authentication**: Firebase Authentication
- **Pagamentos**: PagBank API Oficial (Orders API)
- **Deploy Target**: Google Cloud Run (containerizado)

### **Estrutura do Projeto**

```
backend/
├── src/main/kotlin/com/tylerproject/
│   ├── TylerApiApplication.kt          # Entry point Spring Boot
│   ├── config/
│   │   └── TokenConfiguration.kt       # Configuração centralizada de tokens
│   ├── controllers/
│   │   └── PaymentController.kt        # Endpoints PIX (/api/payments/*)
│   ├── models/
│   │   ├── Models.kt                   # Domain models
│   │   ├── DTOs.kt                     # Request/Response DTOs
│   │   └── PagBankModels.kt           # Modelos específicos PagBank
│   └── providers/
│       ├── PaymentProvider.kt          # Interface genérica
│       └── PagBankProvider.kt         # Implementação PIX oficial
├── src/main/resources/
│   ├── application.yml                 # Config principal
│   ├── application-local.yml           # Profile desenvolvimento
│   └── application-production.yml      # Profile produção
├── config/
│   └── firebase-admin-sdk.json        # Credenciais Firebase (IGNORADO pelo git)
├── test-files/
│   └── test-pix-valido.json           # Casos de teste funcionais
└── pom.xml                            # Maven dependencies
```

## 💳 **FUNCIONALIDADES IMPLEMENTADAS**

### **1. Sistema de Pagamentos PIX**

- ✅ **Integração PagBank** completa e funcional
- ✅ **API Orders oficial** (não legacy)
- ✅ **Aceita CPF** (ideal para caridade, não precisa CNPJ)
- ✅ **QR Codes PIX** gerados automaticamente
- ✅ **Sandbox e Produção** configurados via Spring Profiles
- ✅ **Webhooks** implementados para status de pagamento

### **2. Endpoints Disponíveis**

```http
POST /api/payments/checkout     # Criar doação PIX
GET  /api/payments/{id}/status  # Status da doação
POST /api/payments/webhook      # Webhook PagBank
GET  /actuator/health          # Health check
```

### **3. Autenticação e Segurança**

- ✅ **Firebase Authentication** configurado
- ✅ **Token management** centralizado
- ✅ **CORS** configurado para desenvolvimento
- ✅ **Credenciais** excluídas do repositório (segurança)

## 🔧 **CONFIGURAÇÃO ATUAL**

### **Profiles Spring Boot**

- **Local**: Token hardcoded, logs verbosos, CORS permissivo
- **Production**: Token via Environment Variable, logs otimizados, Secret Manager do GCP

### **Variáveis de Ambiente**

```yaml
PAGBANK_TOKEN: "Token do PagBank (sandbox ou produção)"
```

### **Exemplo de Request PIX Funcional**

```json
{
  "amount": 1000,
  "description": "Doação para caridade",
  "payer": {
    "name": "João Silva",
    "document": "11144477735",
    "email": "joao@email.com"
  }
}
```

### **Exemplo de Response PIX**

```json
{
  "id": "ORDE_123ABC",
  "reference_id": "donation_001",
  "status": "WAITING",
  "created_at": "2025-10-31T10:30:00Z",
  "qr_codes": [
    {
      "id": "QRCO_456DEF",
      "text": "00020126580014BR.GOV.BCB.PIX0136...",
      "links": [
        {
          "rel": "SELF",
          "href": "https://api.pagseguro.com/qr_codes/QRCO_456DEF",
          "media": "application/json",
          "type": "GET"
        }
      ]
    }
  ],
  "amount": {
    "value": 1000,
    "currency": "BRL"
  }
}
```

## 🚀 **STATUS TÉCNICO**

### **✅ FUNCIONANDO PERFEITAMENTE**

- PIX transactions com QR codes
- Validação de CPF automática
- Serialização JSON com Kotlin Serialization
- Response completo com `qr_codes` array
- Logging estruturado
- Git repository limpo (sem credenciais)

### **🔄 PRONTO PARA**

- Deploy no Google Cloud Run
- Ambiente de produção
- Scaling horizontal
- Monitoramento com Actuator

### **📊 TESTES REALIZADOS**

- ✅ Criação de PIX com CPF válido
- ✅ Geração de QR codes funcionais
- ✅ Response format correto
- ✅ Error handling robusto
- ✅ API authentication funcionando

## 💡 **PRÓXIMOS PASSOS SUGERIDOS**

1. **Deploy Produção**: Configurar Cloud Run com Secret Manager
2. **Frontend Integration**: Conectar com app mobile/web
3. **Analytics**: Implementar tracking de doações
4. **Monitoring**: Configurar alertas e métricas
5. **Features**: Adicionar novos tipos de pagamento se necessário

## 🔑 **INFORMAÇÕES CRÍTICAS**

- **PagBank** usa formato `qr_codes` array (não `charges`)
- **encodeDefaults=true** obrigatório no Kotlinx Serialization
- **CPF validation** automática na API
- **Token** centralizado em Bean único
- **Git** limpo sem credenciais sensíveis

## 🛠️ **COMANDOS ÚTEIS**

### **Build e Run Local**

```bash
mvn clean compile
mvn spring-boot:run
```

### **Teste da API**

```bash
curl -X POST http://localhost:8080/api/payments/checkout \
  -H "Content-Type: application/json" \
  -d @test-files/test-pix-valido.json
```

### **Deploy**

```bash
# Build da imagem
docker build -t tyler-api .

# Deploy Cloud Run
gcloud run deploy tyler-api --source .
```

## 📦 **DEPENDÊNCIAS PRINCIPAIS**

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Kotlin -->
    <dependency>
        <groupId>org.jetbrains.kotlin</groupId>
        <artifactId>kotlin-stdlib</artifactId>
    </dependency>

    <!-- Firebase -->
    <dependency>
        <groupId>com.google.firebase</groupId>
        <artifactId>firebase-admin</artifactId>
    </dependency>

    <!-- HTTP Client -->
    <dependency>
        <groupId>com.squareup.okhttp3</groupId>
        <artifactId>okhttp</artifactId>
    </dependency>

    <!-- JSON Serialization -->
    <dependency>
        <groupId>org.jetbrains.kotlinx</groupId>
        <artifactId>kotlinx-serialization-json</artifactId>
    </dependency>
</dependencies>
```

---

**📝 INSTRUÇÕES**: Este projeto está **production-ready**. A API PIX está completamente funcional. Você pode focar em deploy, frontend integration, ou novas features. Todo o código está testado e validado.

**🔗 Repository**: https://github.com/marllon/tyler-api  
**📧 Contexto**: API para caridade com foco em doações PIX via CPF
