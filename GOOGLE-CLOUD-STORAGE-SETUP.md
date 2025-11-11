# Configuração do Google Cloud Storage para Imagens de Produtos

## 📋 **Pré-requisitos**

1. **Google Cloud Project**: Certifique-se de ter o projeto `tyler-dev-c2420` (ou `tyler-prd` para produção)
2. **Google Cloud SDK**: Instale o CLI do Google Cloud
3. **Permissões**: Sua conta deve ter permissão para criar buckets e objetos

## 🗂️ **Criação do Bucket**

### 1. **Via Google Cloud Console:**

1. Acesse: https://console.cloud.google.com/storage
2. Selecione o projeto: `tyler-dev-c2420`
3. Clique em "Create Bucket"
4. Configure:
   - **Nome**: `tyler-product-images`
   - **Localização**: `us-central1` (mais próximo e econômico)
   - **Storage class**: `Standard`
   - **Access control**: `Uniform` (bucket-level permissions)

### 2. **Via CLI (gcloud):**

```bash
# Autenticar (se necessário)
gcloud auth login

# Definir projeto
gcloud config set project tyler-dev-c2420

# Criar bucket
gsutil mb -p tyler-dev-c2420 -c standard -l us-central1 gs://tyler-product-images

# Configurar CORS (permitir acesso via web)
echo '[
  {
    "origin": ["*"],
    "method": ["GET", "HEAD"],
    "responseHeader": ["Content-Type", "Access-Control-Allow-Origin"],
    "maxAgeSeconds": 3600
  }
]' > cors.json

gsutil cors set cors.json gs://tyler-product-images

# ❌ NÃO execute esta linha - mantém o bucket privado para segurança
# gsutil iam ch allUsers:objectViewer gs://tyler-product-images
```

## 🔐 **Service Account para Storage (Recomendado)**

### **1. Criar Service Account via Console:**

1. Acesse: https://console.cloud.google.com/iam-admin/serviceaccounts
2. Selecione projeto: `tyler-dev-c2420`
3. Clique **"Create Service Account"**
4. Configure:
   - **Name**: `tyler-storage-service`
   - **Description**: `Service account for product images storage access`
   - **Service account ID**: `tyler-storage-service`

### **2. Configurar Permissões:**

1. Na lista de Service Accounts, clique na conta criada
2. Aba **"Permissions"** → **"Grant Access"**
3. Adicionar papéis:
   - `Storage Object Admin` (para criar/deletar objetos)
   - `Storage Legacy Bucket Reader` (para listar objetos)

### **3. Gerar Chave JSON:**

1. Aba **"Keys"** → **"Add Key"** → **"Create new key"**
2. Tipo: **JSON**
3. Salvar como: `tyler-storage-credentials.json`
4. **⚠️ IMPORTANTE**: Não commitar este arquivo no Git!

### **4. Via CLI (Alternativa):**

```bash
# 1. Criar Service Account
gcloud iam service-accounts create tyler-storage-service \
  --display-name="Tyler Storage Service Account" \
  --description="Service account for product images storage access"

# 2. Adicionar permissões ao bucket
gsutil iam ch serviceAccount:tyler-storage-service@tyler-dev-c2420.iam.gserviceaccount.com:objectAdmin \
  gs://tyler-product-images

# 3. Gerar chave JSON
gcloud iam service-accounts keys create tyler-storage-credentials.json \
  --iam-account=tyler-storage-service@tyler-dev-c2420.iam.gserviceaccount.com

# 4. Verificar permissões
gsutil iam get gs://tyler-product-images
```

### **5. Para Produção (Repetir processo):**

```bash
# Mesmo processo no projeto de produção
gcloud config set project tyler-prd
# ... repetir comandos acima ...
```

## 💰 **Estimativa de Custos**

### **Google Cloud Storage Pricing (us-central1):**

- **Armazenamento Standard**: $0.020 por GB/mês
- **Operações Class A** (upload): $0.05 por 10.000 operações
- **Operações Class B** (download): $0.004 por 10.000 operações
- **Transferência de dados**: Gratuita até 1GB/mês, depois $0.12/GB

### **Exemplo prático:**

- **1000 produtos** com 3 imagens cada (2MB médio por imagem)
- **Armazenamento**: 6GB × $0.020 = **$0.12/mês**
- **Uploads mensais**: 500 × $0.05/10.000 = **$0.0025/mês**
- **Downloads mensais**: 10.000 × $0.004/10.000 = **$0.004/mês**
- **Total estimado**: **~$0.13/mês** para 1000 produtos

## 🌐 **URLs das Imagens**

### **🔐 Signed URLs (Recomendado - Seguro)**

As imagens são acessíveis via **Signed URLs** temporárias:

```
https://storage.googleapis.com/tyler-product-images/products/{productId}/images/{filename}?X-Goog-Algorithm=...
```

- ✅ **Seguras**: Expiram em 7 dias automaticamente
- ✅ **Controle**: Apenas usuários autorizados acessam
- ✅ **Auditoria**: Logs detalhados de acesso

### **🌐 URLs Públicas (Alternativa - Menos Seguro)**

Se preferir URLs diretas permanentes:

```bash
# ⚠️ APENAS se necessário - torna o bucket público
gsutil iam ch allUsers:objectViewer gs://tyler-product-images
```

URLs ficam: `https://storage.googleapis.com/tyler-product-images/products/{productId}/images/{filename}`

## �️ **Comparação de Segurança**

### **Bucket Privado + Signed URLs (Implementado)**

```
✅ Controle total de acesso
✅ URLs temporárias (7 dias)
✅ Auditoria completa
✅ Sem risco de hotlinking
✅ Renovação automática pelo sistema
❌ Ligeiramente mais complexo
```

### **Bucket Público**

```
✅ Simplicidade máxima
✅ URLs permanentes
❌ Qualquer pessoa pode acessar
❌ Risco de hotlinking/abuso
❌ Possível indexação por buscadores
❌ Sem controle de acesso
```

**💡 Recomendação**: Use bucket privado para dados sensíveis ou comerciais.

## �🚀 **Testando a Configuração**

Após criar o bucket, teste o upload:

```bash
# Fazer upload de teste
echo "teste" > test.txt
gsutil cp test.txt gs://tyler-product-images/

# Verificar se foi criado
gsutil ls gs://tyler-product-images/

# Remover arquivo de teste
gsutil rm gs://tyler-product-images/test.txt
```

## ⚙️ **Configuração da Aplicação**

### **1. Estrutura de Arquivos de Credenciais:**

```
src/main/resources/
├── firebase-admin-sdk.json          # Para Firestore
├── tyler-storage-credentials.json   # Para Cloud Storage (DEV)
├── tyler-storage-credentials-prd.json # Para Cloud Storage (PROD)
└── application.yml
```

### **2. Variáveis de Ambiente:**

```bash
# Desenvolvimento
export GCP_PROJECT_ID=tyler-dev-c2420
export GCP_BUCKET_NAME=tyler-product-images
export GCP_STORAGE_CREDENTIALS_PATH=tyler-storage-credentials.json

# Produção
export GCP_PROJECT_ID=tyler-prd
export GCP_BUCKET_NAME=tyler-product-images-prd
export GCP_STORAGE_CREDENTIALS_PATH=tyler-storage-credentials-prd.json
```

### **3. Profiles do Spring (Recomendado):**

```yaml
# application-local.yml
app:
  gcp:
    storage-credentials-path: tyler-storage-credentials.json

# application-production.yml
app:
  gcp:
    storage-credentials-path: tyler-storage-credentials-prd.json
```

## 🔧 **Troubleshooting**

### **Erro de permissão da Service Account:**

```bash
# Verificar se a service account existe
gcloud iam service-accounts list --project=tyler-dev-c2420

# Verificar permissões no bucket
gsutil iam get gs://tyler-product-images

# Adicionar permissão se necessário
gsutil iam ch serviceAccount:tyler-storage-service@tyler-dev-c2420.iam.gserviceaccount.com:objectAdmin \
  gs://tyler-product-images
```

### **Testar credenciais:**

```bash
# Ativar a service account localmente
gcloud auth activate-service-account --key-file=tyler-storage-credentials.json

# Testar acesso ao bucket
gsutil ls gs://tyler-product-images

# Voltar para sua conta pessoal
gcloud auth login
```

### **Verificar configuração da aplicação:**

```bash
# Verificar se o arquivo de credenciais existe
ls src/main/resources/tyler-storage-credentials.json

# Verificar estrutura do JSON
cat src/main/resources/tyler-storage-credentials.json | jq .client_email
```

### **Problemas comuns:**

1. **Arquivo de credenciais não encontrado:**

   - Verifique se `tyler-storage-credentials.json` está em `src/main/resources/`
   - Confirme que não está no `.gitignore` (deve estar!)

2. **Permissões insuficientes:**

   - Service Account precisa de `Storage Object Admin` no bucket
   - Verificar se o bucket existe no projeto correto

3. **Projeto incorreto:**
   - Confirmar `project-id` no `application.yml`
   - Service Account deve pertencer ao mesmo projeto do bucket
