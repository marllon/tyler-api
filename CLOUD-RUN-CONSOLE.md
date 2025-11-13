# 🌐 **Deploy Tyler API via Console GCP - Guia Visual**

## 🎯 **Visão Geral**

Este guia mostra como fazer deploy da Tyler API usando apenas o console web do Google Cloud Platform, sem precisar usar linha de comando.

---

## 📋 **Pré-requisitos**

### **1. ✅ Conta Google Cloud:**
- Conta do Google ativa
- Projeto GCP criado
- Billing habilitado

### **2. ✅ Código Preparado:**
- Tyler API funcionando localmente
- Dockerfile criado (já está no projeto)
- Variáveis de ambiente identificadas

---

## 🚀 **Método 1: Cloud Build + Cloud Run (Recomendado)**

### **Passo 1: Preparar o Código**

#### **1.1 Comprimir o projeto:**
```
1. Vá para a pasta: d:\Projetos\Tyler\backend
2. Selecione todos os arquivos (Ctrl+A)
3. Clique com botão direito > "Enviar para" > "Pasta compactada"
4. Renomeie para: tyler-api-source.zip
```

#### **1.2 Verificar se Dockerfile existe:**
- ✅ `Dockerfile` (já criado)
- ✅ `.dockerignore` (já criado)

### **Passo 2: Acessar Cloud Build**

#### **2.1 Ir para Cloud Build:**
```
1. Abra console.cloud.google.com
2. Selecione seu projeto
3. Menu hambúrguer ☰ > "Cloud Build"
4. Clique em "Histórico" na lateral esquerda
```

#### **2.2 Habilitar APIs:**
Se aparecer aviso sobre APIs:
```
1. Clique em "Habilitar API"
2. Aguarde alguns minutos
3. Recarregue a página
```

### **Passo 3: Criar Build**

#### **3.1 Upload do código:**
```
1. Clique em "Criar Gatilho" ou "Triggers"
2. Clique em "+ CRIAR GATILHO"
3. Nome: "tyler-api-build"
4. Em "Fonte":
   - Selecione "Cloud Source Repositories"
   - Clique em "Conectar novo repositório"
   - Escolha "Subir arquivo ZIP"
   - Faça upload do tyler-api-source.zip
```

#### **3.2 Configurar Build:**
```
Configuração:
- Tipo: Cloud Build configuration file (yaml or json)
- Local: Repository (cloudbuild.yaml)
```

#### **3.3 Criar cloudbuild.yaml:**
No console, clique em "Editor Online" e crie o arquivo:

```yaml
steps:
  # Build da imagem Docker
  - name: 'gcr.io/cloud-builders/docker'
    args: [
      'build',
      '-t', 'gcr.io/$PROJECT_ID/tyler-api',
      '.'
    ]

  # Push da imagem para Container Registry
  - name: 'gcr.io/cloud-builders/docker'
    args: [
      'push',
      'gcr.io/$PROJECT_ID/tyler-api'
    ]

  # Deploy no Cloud Run
  - name: 'gcr.io/cloud-builders/gcloud'
    args: [
      'run', 'deploy', 'tyler-api',
      '--image', 'gcr.io/$PROJECT_ID/tyler-api',
      '--region', 'us-central1',
      '--platform', 'managed',
      '--allow-unauthenticated',
      '--memory', '1Gi',
      '--cpu', '1',
      '--max-instances', '10'
    ]

images:
  - 'gcr.io/$PROJECT_ID/tyler-api'

options:
  machineType: 'E2_HIGHCPU_8'
```

### **Passo 4: Executar Build**

#### **4.1 Iniciar build:**
```
1. Clique em "Executar gatilho" ou "RUN"
2. Aguarde o build (5-10 minutos)
3. Acompanhe logs na tela
```

#### **4.2 Verificar sucesso:**
```
✅ Build bem-sucedido = ícone verde
❌ Build falhou = ícone vermelho (verificar logs)
```

---

## 🛠️ **Método 2: Cloud Shell Editor**

### **Passo 1: Abrir Cloud Shell**

#### **1.1 Ativar Cloud Shell:**
```
1. No console GCP, clique no ícone ">_" no topo direito
2. Aguarde o Cloud Shell inicializar
3. Clique em "Abrir Editor" (ícone de pasta)
```

### **Passo 2: Upload do Projeto**

#### **2.1 Upload via interface:**
```
1. No editor, clique em "Arquivo" > "Fazer upload de pasta"
2. Selecione a pasta: d:\Projetos\Tyler\backend
3. Aguarde upload completo
```

### **Passo 3: Deploy via Terminal**

#### **3.1 No terminal do Cloud Shell:**
```bash
# Ir para pasta do projeto
cd backend

# Verificar arquivos
ls -la

# Build da imagem
gcloud builds submit --tag gcr.io/$GOOGLE_CLOUD_PROJECT/tyler-api

# Deploy no Cloud Run
gcloud run deploy tyler-api \
  --image gcr.io/$GOOGLE_CLOUD_PROJECT/tyler-api \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated
```

---

## ⚙️ **Método 3: Interface Cloud Run Direta**

### **Passo 1: Preparar Imagem**

#### **1.1 Build via Cloud Build (simplificado):**
```
1. Console GCP > Cloud Build
2. "Submit a build" 
3. Upload tyler-api-source.zip
4. Build Type: "Dockerfile"
5. Image name: "gcr.io/SEU_PROJECT/tyler-api"
6. Clique em "Build"
```

### **Passo 2: Cloud Run Interface**

#### **2.1 Acessar Cloud Run:**
```
1. Console GCP > Cloud Run
2. Clique em "CRIAR SERVIÇO"
```

#### **2.2 Configurar Serviço:**
```
Configuração básica:
- Nome do serviço: tyler-api
- Região: us-central1
- Container image URL: gcr.io/SEU_PROJECT/tyler-api

CPU e Memória:
- CPU: 1
- Memória: 1 GiB
- Request timeout: 300 seconds

Autoscaling:
- Minimum instances: 0
- Maximum instances: 10
```

#### **2.3 Configurar Variáveis:**
```
Na seção "Variáveis de ambiente":

Clique em "ADICIONAR VARIÁVEL":
- Nome: SPRING_PROFILES_ACTIVE
- Valor: production

- Nome: GCP_PROJECT_ID  
- Valor: seu-project-id

- Nome: GCP_BUCKET_NAME
- Valor: tyler-products-images
```

#### **2.4 Configurar Tráfego:**
```
Authentication:
☑️ Allow unauthenticated invocations
```

### **Passo 3: Deploy**

#### **3.1 Finalizar:**
```
1. Clique em "CRIAR"
2. Aguarde deploy (2-5 minutos)
3. Copie a URL gerada
```

---

## 🔐 **Configurar Secrets (Console)**

### **Passo 1: Secret Manager**

#### **1.1 Acessar Secret Manager:**
```
1. Console GCP > Secret Manager
2. Habilitar API se necessário
3. Clique em "CRIAR SECRET"
```

#### **1.2 Criar Secrets:**

**PagBank Token:**
```
- Nome: pagbank-token
- Valor do secret: SEU_TOKEN_PAGBANK
- Clique em "CRIAR SECRET"
```

**Firebase Credentials:**
```
- Nome: firebase-credentials
- Upload de arquivo: firebase-admin-sdk.json
- Clique em "CRIAR SECRET"
```

### **Passo 2: Conectar ao Cloud Run**

#### **2.1 Editar serviço:**
```
1. Cloud Run > tyler-api > "EDITAR E IMPLANTAR NOVA REVISÃO"
2. Vá para aba "Variáveis e secrets"
3. Clique em "ADICIONAR VOLUME" > "Secret"
```

#### **2.2 Configurar volumes:**
```
Secret volume:
- Nome: firebase-secret
- Secret: firebase-credentials
- Mount path: /app/config/firebase-credentials.json

Secret como variável:
- Nome: PAGBANK_TOKEN
- Secret: pagbank-token
- Versão: latest
```

---

## 📊 **Monitoramento via Console**

### **Dashboard Cloud Run:**
```
1. Cloud Run > tyler-api
2. Aba "MÉTRICAS" - ver CPU, memória, requests
3. Aba "LOGS" - ver logs da aplicação
4. Aba "REVISÕES" - histórico de deploys
```

### **Teste da API:**
```
1. Copie a URL do serviço
2. Teste: https://TYLER-API-URL/api/health
3. Swagger: https://TYLER-API-URL/swagger-ui.html
```

---

## 🚨 **Troubleshooting via Console**

### **Problemas Comuns:**

#### **Build falha:**
```
1. Cloud Build > Histórico
2. Clique no build vermelho
3. Veja logs detalhados
4. Verifique Dockerfile e dependências
```

#### **Serviço não responde:**
```
1. Cloud Run > tyler-api > "LOGS"
2. Filtrar por severity: ERROR
3. Verificar stack traces
4. Ajustar memória/CPU se necessário
```

#### **Timeout:**
```
1. Cloud Run > tyler-api > "EDITAR"
2. Aumentar "Request timeout"
3. Verificar "Memory allocated"
```

---

## ✅ **Checklist Final**

### **Antes do Deploy:**
- [ ] ✅ Projeto GCP criado e billing ativo
- [ ] ✅ APIs habilitadas (Cloud Build, Cloud Run)
- [ ] ✅ Código compactado em ZIP
- [ ] ✅ Dockerfile verificado

### **Durante o Deploy:**
- [ ] ✅ Build executado com sucesso
- [ ] ✅ Imagem criada no Container Registry
- [ ] ✅ Serviço Cloud Run criado
- [ ] ✅ Variáveis de ambiente configuradas

### **Pós Deploy:**
- [ ] ✅ URL funcionando
- [ ] ✅ Health check retornando 200
- [ ] ✅ Swagger UI carregando
- [ ] ✅ Logs sem erros críticos

---

## 🎯 **URLs Finais**

Após deploy bem-sucedido:
```
🏠 Aplicação: https://tyler-api-xxx.a.run.app
🏥 Health: https://tyler-api-xxx.a.run.app/api/health  
📚 Swagger: https://tyler-api-xxx.a.run.app/swagger-ui.html
🛍️ API: https://tyler-api-xxx.a.run.app/api/products
```

---

## 💡 **Dicas Importantes**

### **Para Desenvolvimento:**
- Use Cloud Shell para testes rápidos
- Monitore custos no dashboard
- Configure alertas de billing

### **Para Produção:**
- Use Secret Manager para credenciais
- Configure domínio personalizado
- Implemente monitoring com alertas

**🎉 Sua Tyler API estará rodando no Google Cloud Run via console web!**