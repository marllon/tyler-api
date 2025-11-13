# 🚀 **Deploy Tyler API no Google Cloud Run - Guia Completo**

## 📋 **Pré-requisitos**

### **1. Ferramentas Necessárias:**
- ✅ Google Cloud SDK (gcloud)
- ✅ Docker Desktop
- ✅ Conta Google Cloud ativa
- ✅ Projeto GCP configurado

### **2. Verificar Instalações:**
```bash
# Verificar Google Cloud SDK
gcloud --version

# Verificar Docker
docker --version

# Login no Google Cloud
gcloud auth login
gcloud config set project SEU_PROJECT_ID
```

---

## 📁 **1. Criar Dockerfile**

### **Dockerfile Otimizado para Spring Boot + Kotlin:**
```dockerfile
# Multi-stage build para otimizar tamanho
FROM maven:3.9.4-openjdk-21 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build da aplicação
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:21-jre-slim

WORKDIR /app

# Instalar curl para health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copiar JAR da aplicação
COPY --from=builder /app/target/tyler-api-*.jar app.jar

# Criar usuário não-root para segurança
RUN adduser --disabled-password --gecos '' appuser && chown appuser /app
USER appuser

# Configurações JVM otimizadas
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport"

# Expor porta
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/health || exit 1

# Comando de inicialização
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### **Criar .dockerignore:**
```
target/
.mvn/
*.log
*.tmp
.git/
.gitignore
README.md
*.md
.env
.env.local
node_modules/
*.iml
.idea/
.vscode/
```

---

## ⚙️ **2. Configurar application.yml para Produção**

### **Atualizar src/main/resources/application.yml:**
```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:production}

# Configuração para produção
---
spring:
  config:
    activate:
      on-profile: production
  application:
    name: tyler-api
    
server:
  port: ${PORT:8080}
  forward-headers-strategy: framework

# Google Cloud Storage
app:
  gcp:
    project-id: ${GCP_PROJECT_ID}
    bucket-name: ${GCP_BUCKET_NAME}
    storage-credentials-path: ${GCP_STORAGE_CREDENTIALS_PATH:/app/config/storage-credentials.json}

# Firebase
firebase:
  credentials-path: ${FIREBASE_CREDENTIALS_PATH:/app/config/firebase-credentials.json}

# PagBank
pagbank:
  token: ${PAGBANK_TOKEN}
  webhook-url: ${PAGBANK_WEBHOOK_URL}

# Logging
logging:
  level:
    com.tylerproject: INFO
    org.springframework.web: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

# Actuator para health checks
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
```

---

## 🔐 **3. Configurar Credenciais**

### **3.1. Service Account para Cloud Storage:**
```bash
# Criar service account
gcloud iam service-accounts create tyler-storage-sa \
  --description="Service account para Tyler API storage" \
  --display-name="Tyler Storage SA"

# Dar permissões
gcloud projects add-iam-policy-binding SEU_PROJECT_ID \
  --member="serviceAccount:tyler-storage-sa@SEU_PROJECT_ID.iam.gserviceaccount.com" \
  --role="roles/storage.admin"

# Criar chave
gcloud iam service-accounts keys create storage-credentials.json \
  --iam-account=tyler-storage-sa@SEU_PROJECT_ID.iam.gserviceaccount.com
```

### **3.2. Firebase Admin SDK:**
```bash
# Baixar do Firebase Console:
# 1. Ir para Firebase Console -> Configurações do Projeto
# 2. Aba "Contas de serviço" 
# 3. "Gerar nova chave privada"
# 4. Salvar como firebase-credentials.json
```

---

## 🐳 **4. Build e Test Local**

### **4.1. Build da imagem Docker:**
```bash
# No diretório do projeto
docker build -t tyler-api .

# Testar localmente
docker run -p 8080:8080 \
  -e GCP_PROJECT_ID=seu-project-id \
  -e GCP_BUCKET_NAME=seu-bucket-name \
  -e PAGBANK_TOKEN=seu-token \
  tyler-api
```

### **4.2. Testar health check:**
```bash
curl http://localhost:8080/api/health
# Deve retornar: {"status":"healthy",...}
```

---

## ☁️ **5. Deploy no Cloud Run**

### **5.1. Configurar Google Cloud:**
```bash
# Autenticar Docker com GCR
gcloud auth configure-docker

# Definir variáveis
export PROJECT_ID=seu-project-id
export IMAGE_NAME=tyler-api
export SERVICE_NAME=tyler-api
export REGION=us-central1
```

### **5.2. Build e Push da imagem:**
```bash
# Tag para Google Container Registry
docker tag tyler-api gcr.io/$PROJECT_ID/$IMAGE_NAME

# Push para GCR
docker push gcr.io/$PROJECT_ID/$IMAGE_NAME
```

### **5.3. Deploy no Cloud Run:**
```bash
gcloud run deploy $SERVICE_NAME \
  --image gcr.io/$PROJECT_ID/$IMAGE_NAME \
  --platform managed \
  --region $REGION \
  --allow-unauthenticated \
  --memory 1Gi \
  --cpu 1 \
  --timeout 300 \
  --concurrency 100 \
  --min-instances 0 \
  --max-instances 10 \
  --set-env-vars="SPRING_PROFILES_ACTIVE=production" \
  --set-env-vars="GCP_PROJECT_ID=$PROJECT_ID" \
  --set-env-vars="GCP_BUCKET_NAME=tyler-products-images" \
  --set-env-vars="PAGBANK_TOKEN=$PAGBANK_TOKEN" \
  --set-env-vars="PAGBANK_WEBHOOK_URL=https://tyler-api-xxx.a.run.app/api/payments/webhook"
```

---

## 🔐 **6. Configurar Secrets (Recomendado)**

### **6.1. Usar Google Secret Manager:**
```bash
# Criar secrets para credenciais
gcloud secrets create firebase-credentials --data-file=firebase-credentials.json
gcloud secrets create storage-credentials --data-file=storage-credentials.json
gcloud secrets create pagbank-token --data-file=- <<< "$PAGBANK_TOKEN"

# Dar permissão ao Cloud Run
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:$PROJECT_ID@appspot.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"
```

### **6.2. Deploy com Secrets:**
```bash
gcloud run deploy $SERVICE_NAME \
  --image gcr.io/$PROJECT_ID/$IMAGE_NAME \
  --platform managed \
  --region $REGION \
  --allow-unauthenticated \
  --memory 1Gi \
  --cpu 1 \
  --set-env-vars="SPRING_PROFILES_ACTIVE=production,GCP_PROJECT_ID=$PROJECT_ID" \
  --set-secrets="FIREBASE_CREDENTIALS=firebase-credentials:latest" \
  --set-secrets="STORAGE_CREDENTIALS=storage-credentials:latest" \
  --set-secrets="PAGBANK_TOKEN=pagbank-token:latest"
```

---

## 🌐 **7. Configurar Domínio Customizado**

### **7.1. Mapear domínio:**
```bash
# Mapear domínio
gcloud run domain-mappings create \
  --service $SERVICE_NAME \
  --domain api.tyler.com \
  --region $REGION
```

### **7.2. Configurar DNS:**
```
# No seu provedor DNS, criar registro CNAME:
api.tyler.com -> ghs.googlehosted.com
```

---

## 🔍 **8. Monitoramento e Logs**

### **8.1. Ver logs:**
```bash
gcloud run services logs read $SERVICE_NAME --region $REGION
```

### **8.2. Ver métricas:**
```bash
gcloud run services describe $SERVICE_NAME --region $REGION
```

### **8.3. Health check:**
```bash
curl https://tyler-api-xxx.a.run.app/api/health
```

---

## 📊 **9. Configurações de Performance**

### **9.1. Otimizações Cloud Run:**
```bash
# Deploy otimizado
gcloud run deploy $SERVICE_NAME \
  --image gcr.io/$PROJECT_ID/$IMAGE_NAME \
  --platform managed \
  --region $REGION \
  --memory 2Gi \
  --cpu 2 \
  --timeout 900 \
  --concurrency 80 \
  --min-instances 1 \
  --max-instances 20 \
  --cpu-throttling \
  --execution-environment gen2
```

### **9.2. Configurar Load Balancer (Opcional):**
```bash
# Para múltiplas regiões
gcloud run deploy $SERVICE_NAME --region us-east1
gcloud run deploy $SERVICE_NAME --region europe-west1
```

---

## 🚨 **10. Troubleshooting**

### **Problemas Comuns:**

#### **Build Failed:**
```bash
# Verificar logs do build
gcloud builds log $(gcloud builds list --limit=1 --format="value(id)")
```

#### **Service não responde:**
```bash
# Verificar logs do serviço
gcloud run services logs read $SERVICE_NAME --region $REGION --limit 50
```

#### **Timeout:**
```bash
# Aumentar timeout
gcloud run services update $SERVICE_NAME --timeout 900 --region $REGION
```

#### **Out of Memory:**
```bash
# Aumentar memória
gcloud run services update $SERVICE_NAME --memory 2Gi --region $REGION
```

---

## ✅ **11. Checklist Final**

- [ ] ✅ Dockerfile criado e testado localmente
- [ ] ✅ application.yml configurado para produção
- [ ] ✅ Credenciais configuradas (Firebase, Storage, PagBank)
- [ ] ✅ Imagem buildada e enviada para GCR
- [ ] ✅ Serviço deployado no Cloud Run
- [ ] ✅ Variáveis de ambiente configuradas
- [ ] ✅ Health check funcionando
- [ ] ✅ Domínio mapeado (se aplicável)
- [ ] ✅ Logs sendo gerados corretamente
- [ ] ✅ API respondendo nas rotas esperadas

---

## 🎯 **URLs Finais**

Após o deploy, sua API estará disponível em:
- **Health Check**: `https://tyler-api-xxx.a.run.app/api/health`
- **Swagger UI**: `https://tyler-api-xxx.a.run.app/swagger-ui.html`
- **API Products**: `https://tyler-api-xxx.a.run.app/api/products`

**🎉 Sua Tyler API estará rodando no Google Cloud Run com escalabilidade automática!**