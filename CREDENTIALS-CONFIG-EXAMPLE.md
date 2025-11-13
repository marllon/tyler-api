# Exemplo Prático - Configuração de Credenciais

Este arquivo mostra exemplos reais de como configurar as credenciais JSON nas variáveis de ambiente.

## 📋 Exemplo de FIREBASE_CREDENTIALS_JSON

```json
{
  "type": "service_account",
  "project_id": "tyler-prod-123456",
  "private_key_id": "abcd1234...",
  "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvgIBADANB...\n-----END PRIVATE KEY-----\n",
  "client_email": "firebase-adminsdk-xyz@tyler-prod-123456.iam.gserviceaccount.com",
  "client_id": "123456789012345678901",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-xyz%40tyler-prod-123456.iam.gserviceaccount.com"
}
```

## 📋 Exemplo de GCP_STORAGE_CREDENTIALS_JSON

```json
{
  "type": "service_account",
  "project_id": "tyler-prod-123456",
  "private_key_id": "efgh5678...",
  "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvAIBADANB...\n-----END PRIVATE KEY-----\n",
  "client_email": "storage-admin@tyler-prod-123456.iam.gserviceaccount.com",
  "client_id": "987654321098765432109",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/storage-admin%40tyler-prod-123456.iam.gserviceaccount.com"
}
```

## 🔧 Como Configurar no Cloud Run Console

### Passo 1: Obter o JSON das Credenciais

1. Acesse o [Console GCP](https://console.cloud.google.com)
2. Vá para **IAM & Admin** → **Service Accounts**
3. Clique na conta de serviço desejada
4. Vá para a aba **Keys**
5. Clique **Add Key** → **Create New Key**
6. Selecione **JSON**
7. Baixe o arquivo e **copie todo o conteúdo**

### Passo 2: Configurar no Cloud Run

1. Vá para **Cloud Run** → Seu serviço
2. Clique **Edit & Deploy New Revision**
3. Vá para **Variables & Secrets**
4. Clique **Add Variable**
5. Configure:
   - **Name**: `FIREBASE_CREDENTIALS_JSON`
   - **Value**: Cole o JSON completo (uma linha só)

### Passo 3: Formato da Variável de Ambiente

⚠️ **IMPORTANTE**: O JSON deve ser **uma linha só**, sem quebras:

```bash
# ❌ Errado - com quebras de linha
FIREBASE_CREDENTIALS_JSON={
  "type": "service_account",
  "project_id": "tyler-prod"
}

# ✅ Correto - uma linha só
FIREBASE_CREDENTIALS_JSON={"type":"service_account","project_id":"tyler-prod","private_key":"-----BEGIN PRIVATE KEY-----\nMIIE...","client_email":"firebase@tyler.iam.gserviceaccount.com"}
```

## 🔒 Usando Secret Manager (Recomendado)

### Para dados sensíveis, use o Secret Manager:

#### 1. Criar o Secret

```bash
# Salvar o JSON em um arquivo temporário
echo '{"type":"service_account"...}' > /tmp/firebase-creds.json

# Criar o secret
gcloud secrets create firebase-credentials --data-file=/tmp/firebase-creds.json

# Limpar arquivo temporário
rm /tmp/firebase-creds.json
```

#### 2. Configurar no Cloud Run

1. Na seção **Variables & Secrets**
2. Clique **Reference a Secret**
3. Selecione `firebase-credentials`
4. Configure:
   - **Expose as**: Environment Variable
   - **Name**: `FIREBASE_CREDENTIALS_JSON`

## 🧪 Teste Local

Para testar localmente, crie um arquivo `.env`:

```bash
# .env
SPRING_PROFILES_ACTIVE=local
GCP_PROJECT_ID=tyler-dev-123456
GCP_BUCKET_NAME=tyler-images-dev
FIREBASE_CREDENTIALS_JSON={"type":"service_account","project_id":"tyler-dev"...}
GCP_STORAGE_CREDENTIALS_JSON={"type":"service_account","project_id":"tyler-dev"...}
PAGBANK_TOKEN=seu-token-sandbox
PAGBANK_WEBHOOK_URL=http://localhost:8080/api/payments/webhook
```

## ⚡ Validação

Após configurar, verifique os logs:

```bash
gcloud logs read --service=tyler-api --limit=10
```

Procure por:

```
✅ Carregando credenciais Firebase do JSON da variável de ambiente
✅ Carregando credenciais Google Cloud Storage do JSON da variável de ambiente
✅ Firebase inicializado com sucesso - Projeto: tyler-prod-123456
```

---

💡 **Dica**: Use sempre Secret Manager para produção e nunca commite credenciais no código!
