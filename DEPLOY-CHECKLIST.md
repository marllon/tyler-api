# ✅ **Deploy Tyler API - Checklist Console GCP**

## 🎯 **Opção 1: Cloud Build Automático (Mais Fácil)**

### **📤 1. Preparar Arquivos**

```
□ Ir para: d:\Projetos\Tyler\backend
□ Selecionar todos os arquivos (Ctrl+A)
□ Botão direito > "Enviar para" > "Pasta compactada"
□ Renomear para: tyler-api.zip
```

### **🌐 2. Acessar Google Cloud**

```
□ Abrir: console.cloud.google.com
□ Selecionar seu projeto (canto superior)
□ Se não tem projeto: "Novo Projeto" > dar nome
```

### **🔧 3. Habilitar APIs**

```
□ Menu ☰ > "APIs e serviços" > "Biblioteca"
□ Pesquisar: "Cloud Build API" > Habilitar
□ Pesquisar: "Cloud Run API" > Habilitar
□ Pesquisar: "Container Registry API" > Habilitar
```

### **🏗️ 4. Cloud Build**

```
□ Menu ☰ > "Cloud Build"
□ Clicar em "Histórico" (lateral esquerda)
□ Clicar em "ENVIAR BUILD" (botão azul)

Configurações:
□ Origem: "Upload local de arquivos (.zip)"
□ Clique em "PROCURAR" > Selecionar tyler-api.zip
□ Tipo de build: "Cloud Build configuration file (yaml or json)"
□ Nome do arquivo: cloudbuild.yaml
□ Clicar em "ENVIAR"
```

### **⏰ 5. Aguardar Build**

```
□ Acompanhar progresso (5-10 minutos)
□ Ícone verde = Sucesso ✅
□ Ícone vermelho = Erro ❌ (verificar logs)
```

---

## 🎯 **Opção 2: Cloud Shell (Interface Web)**

### **☁️ 1. Abrir Cloud Shell**

```
□ No console GCP, clicar no ícone ">_" (canto superior direito)
□ Aguardar terminal carregar
□ Clicar em "Abrir Editor" (ícone de pasta)
```

### **📤 2. Upload do Projeto**

```
□ No editor, "Arquivo" > "Fazer upload de pasta"
□ Selecionar pasta: d:\Projetos\Tyler\backend
□ Aguardar upload completo
```

### **💻 3. Comandos no Terminal**

```
□ Digitar: cd backend
□ Digitar: gcloud builds submit --tag gcr.io/$GOOGLE_CLOUD_PROJECT/tyler-api
□ Aguardar build (5-10 minutos)

□ Depois:
gcloud run deploy tyler-api \
  --image gcr.io/$GOOGLE_CLOUD_PROJECT/tyler-api \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated

□ Confirmar com: y
```

---

## ⚙️ **Configurar Variáveis de Ambiente**

### **🔗 1. Acessar Cloud Run**

```
□ Menu ☰ > "Cloud Run"
□ Clicar no serviço "tyler-api"
□ Clicar em "EDITAR E IMPLANTAR NOVA REVISÃO"
```

### **📝 2. Adicionar Variáveis**

```
□ Aba "Variáveis e secrets"
□ Em "Variáveis de ambiente", clicar "+ADICIONAR VARIÁVEL"

Adicionar uma por vez:
□ Nome: SPRING_PROFILES_ACTIVE | Valor: production
□ Nome: GCP_PROJECT_ID | Valor: seu-project-id
□ Nome: GCP_BUCKET_NAME | Valor: tyler-products-images

□ Clicar em "IMPLANTAR" (botão azul)
```

---

## 🔐 **Configurar Secrets (Opcional)**

### **🗝️ 1. Secret Manager**

```
□ Menu ☰ > "Secret Manager"
□ Se aparecer "Habilitar API" > Clicar
□ Clicar em "CRIAR SECRET"

Para PagBank:
□ Nome: pagbank-token
□ Valor do secret: SEU_TOKEN_PAGBANK
□ Clicar "CRIAR SECRET"
```

### **🔗 2. Conectar ao Cloud Run**

```
□ Voltar para Cloud Run > tyler-api > "EDITAR"
□ Aba "Variáveis e secrets"
□ Clicar "+ADICIONAR VARIÁVEL"
□ Selecionar "Fazer referência a um secret"
□ Nome: PAGBANK_TOKEN
□ Secret: pagbank-token
□ Versão: latest
□ Clicar "IMPLANTAR"
```

---

## 🧪 **Testar a API**

### **✅ 1. Obter URL**

```
□ Cloud Run > tyler-api
□ Copiar URL (algo como: https://tyler-api-xxx.a.run.app)
```

### **🏥 2. Testar Endpoints**

```
□ Abrir nova aba do navegador
□ Testar: SUA_URL/api/health
□ Deve retornar: {"status":"healthy",...}

□ Swagger: SUA_URL/swagger-ui.html
□ Deve carregar interface do Swagger
```

---

## 🚨 **Se Algo der Errado**

### **📊 Verificar Logs**

```
□ Cloud Run > tyler-api > aba "LOGS"
□ Procurar mensagens de erro em vermelho
□ Filtrar por "severity: ERROR"
```

### **🔧 Problemas Comuns**

```
Build falhou:
□ Cloud Build > Histórico > Clicar no build vermelho
□ Verificar logs de erro
□ Pode ser falta de memória ou dependências

Serviço não responde:
□ Cloud Run > tyler-api > "EDITAR"
□ Aumentar "Memory allocated" para 2Gi
□ Aumentar "Request timeout" para 900
```

---

## 🎉 **Sucesso!**

### **URLs Funcionais:**

```
✅ Health Check: https://tyler-api-xxx.a.run.app/api/health
✅ Swagger UI: https://tyler-api-xxx.a.run.app/swagger-ui.html
✅ API Products: https://tyler-api-xxx.a.run.app/api/products
```

### **Próximos Passos:**

```
□ Configurar bucket do Google Cloud Storage
□ Fazer upload das credenciais Firebase
□ Configurar token PagBank
□ Testar upload de produtos com imagem
```

**🎯 Escolha a Opção 1 (Cloud Build) se quer algo mais automatizado!**
**🎯 Escolha a Opção 2 (Cloud Shell) se quer mais controle!**
