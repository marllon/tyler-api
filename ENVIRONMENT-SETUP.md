# 🏦 Tyler API - Configuração de Ambiente

## 🎯 **Variáveis de Ambiente (SEM .env)**

Esta aplicação **NÃO** carrega arquivos `.env`. Todas as configurações são feitas via **variáveis de ambiente** padrão do Spring Boot.

---

## 🔧 **Como Configurar**

### **1. Desenvolvimento Local**

**Windows CMD:**

```cmd
set PAGBANK_TOKEN_SANDBOX=seu_token_aqui
mvn spring-boot:run
```

**PowerShell:**

```powershell
$env:PAGBANK_TOKEN_SANDBOX="seu_token_aqui"
mvn spring-boot:run
```

**Linux/Mac:**

```bash
export PAGBANK_TOKEN_SANDBOX=seu_token_aqui
mvn spring-boot:run
```

### **2. IntelliJ IDEA**

```
1. Run → Edit Configurations
2. Environment Variables → Add:
   PAGBANK_TOKEN_SANDBOX = seu_token_aqui
3. Apply → OK → Run
```

### **3. Docker Container**

```dockerfile
# Dockerfile
ENV PAGBANK_TOKEN_SANDBOX=seu_token_aqui

# ou via docker run
docker run -e PAGBANK_TOKEN_SANDBOX=seu_token_aqui tyler-api
```

### **4. Google Cloud Run**

```yaml
# cloud-run.yaml
apiVersion: serving.knative.dev/v1
kind: Service
spec:
  template:
    spec:
      containers:
        - image: gcr.io/project/tyler-api
          env:
            - name: PAGBANK_TOKEN
              value: "seu_token_producao"
```

---

## 📋 **Variáveis Disponíveis**

### **🔑 Obrigatórias**

```bash
# Token PagBank (sandbox para testes)
PAGBANK_TOKEN_SANDBOX=your_sandbox_token_here

# Token PagBank (produção)
PAGBANK_TOKEN=your_production_token_here
```

### **⚙️ Opcionais**

```bash
# Porta do servidor (padrão: 8080)
SERVER_PORT=8080

# Ambiente PagBank (sandbox/production)
PAGBANK_ENVIRONMENT=sandbox

# Webhook PagBank
PAGBANK_WEBHOOK_SECRET=seu_webhook_secret
PAGBANK_WEBHOOK_URL=https://seu-dominio.com/api/payments/webhook

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_TYLERPROJECT=DEBUG
```

---

## 🚀 **Scripts Prontos**

### **Configuração Rápida:**

```cmd
configure-pagbank-api.bat
```

### **Teste Rápido:**

```cmd
test-pagbank-api.bat
```

---

## 🔒 **Segurança**

- ✅ **Não use .env** - Sempre variáveis de ambiente
- ✅ **Nunca commite tokens** - Use .gitignore
- ✅ **Sandbox para testes** - Produção apenas após homologação
- ✅ **Container ready** - Configuração via env vars

---

## 📝 **Exemplo Completo**

```cmd
# Configurar todas as variáveis
set PAGBANK_TOKEN_SANDBOX=seu_token_sandbox
set SERVER_PORT=8080
set LOGGING_LEVEL_COM_TYLERPROJECT=DEBUG

# Iniciar aplicação
mvn spring-boot:run

# Testar
curl http://localhost:8080/actuator/health
```

**Pronto para deploy em qualquer ambiente!** 🎯
