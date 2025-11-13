# Variáveis de Ambiente - Cloud Run

Este guia mostra como configurar todas as variáveis de ambiente necessárias para deploy no Google Cloud Run.

## 📋 Lista Completa de Variáveis

### Variáveis Obrigatórias

| Variável                       | Descrição                     | Exemplo                         | Valor para Produção   |
| ------------------------------ | ----------------------------- | ------------------------------- | --------------------- |
| `SPRING_PROFILES_ACTIVE`       | Profile do Spring Boot        | `production`                    | `production`          |
| `GCP_PROJECT_ID`               | ID do projeto GCP             | `tyler-prod-123456`             | `<seu-project-id>`    |
| `GCP_BUCKET_NAME`              | Nome do bucket Cloud Storage  | `tyler-products-images`         | `<seu-bucket>`        |
| `GCP_STORAGE_CREDENTIALS_JSON` | JSON das credenciais GCS      | `{"type":"service_account"...}` | `<json-completo>`     |
| `FIREBASE_CREDENTIALS_JSON`    | JSON das credenciais Firebase | `{"type":"service_account"...}` | `<json-completo>`     |
| `PAGBANK_TOKEN`                | Token da API PagBank          | `12345...ABC`                   | `<seu-token-pagbank>` |

### ⚠️ Sobre Webhooks PagBank

**Como funciona na prática:**

1. **Você configura** no painel PagBank: `https://SUA-URL-CLOUD-RUN/api/payments/webhook`
2. **PagBank envia** notificações para sua API quando pagamentos mudam de status
3. **Sua API recebe** via endpoint `/api/payments/webhook` (já implementado)
4. **Não precisa** configurar nenhuma URL nas variáveis de ambiente

### Variáveis Automáticas do Cloud Run

| Variável | Descrição          | Valor               |
| -------- | ------------------ | ------------------- |
| `PORT`   | Porta do container | `8080` (automático) |

## 🏗️ Configuração no Console GCP

### Passo 1: Acessar Cloud Run

1. Abra o [Console GCP](https://console.cloud.google.com)
2. Navegue para **Cloud Run**
3. Clique no seu serviço ou **Deploy** se for novo

### Passo 2: Configurar Variáveis de Ambiente

#### Método 1: Durante o Deploy

1. Na seção **Environment Variables**, clique em **Add Variable**
2. Configure cada variável da tabela acima:

```
Name: SPRING_PROFILES_ACTIVE
Value: production

Name: GCP_PROJECT_ID
Value: seu-project-id

Name: GCP_BUCKET_NAME
Value: seu-bucket-name

Name: GCP_STORAGE_CREDENTIALS_JSON
Value: {"type":"service_account","project_id":"seu-project"...}

Name: FIREBASE_CREDENTIALS_JSON
Value: {"type":"service_account","project_id":"seu-project"...}

Name: PAGBANK_TOKEN
Value: seu-token-pagbank
```

#### Método 2: Editando Serviço Existente

1. Clique no serviço existente
2. Clique em **Edit & Deploy New Revision**
3. Vá para a aba **Variables & Secrets**
4. Adicione as variáveis

## 🔒 Gerenciamento de Secrets

### Para Dados Sensíveis (Recomendado)

Para tokens e credenciais sensíveis, use o **Secret Manager**:

#### 1. Criar Secrets

```bash
# Token PagBank
gcloud secrets create pagbank-token --data-file=token.txt

# Credenciais Firebase (JSON completo)
gcloud secrets create firebase-credentials --data-file=firebase-admin-sdk.json

# Credenciais Storage (JSON completo)
gcloud secrets create storage-credentials --data-file=tyler-storage-credentials.json
```

#### 2. Configurar no Cloud Run

No console, na seção **Variables & Secrets**:

**Opção A: Como Variáveis de Ambiente (Recomendado)**

- Clique em **Reference a Secret**
- Selecione o secret criado
- Configure como variável de ambiente:
  - Firebase: `FIREBASE_CREDENTIALS_JSON`
  - Storage: `GCP_STORAGE_CREDENTIALS_JSON`

**Opção B: Como Arquivos Montados (Fallback)**

- Mount path para fallback:
  - Firebase: `/app/config/firebase-credentials.json`
  - Storage: `/app/config/storage-credentials.json`

## 📝 Template de Configuração

### Arquivo .env (para desenvolvimento local)

```bash
SPRING_PROFILES_ACTIVE=local
GCP_PROJECT_ID=tyler-dev-123456
GCP_BUCKET_NAME=tyler-products-images-dev
GCP_STORAGE_CREDENTIALS_JSON={"type":"service_account","project_id":"tyler-dev"...}
FIREBASE_CREDENTIALS_JSON={"type":"service_account","project_id":"tyler-dev"...}
PAGBANK_TOKEN=seu-token-dev
```

### Variáveis para Produção

```bash
SPRING_PROFILES_ACTIVE=production
GCP_PROJECT_ID=tyler-prod-123456
GCP_BUCKET_NAME=tyler-products-images-prod
GCP_STORAGE_CREDENTIALS_JSON={"type":"service_account","project_id":"tyler-prod"...}
FIREBASE_CREDENTIALS_JSON={"type":"service_account","project_id":"tyler-prod"...}
PAGBANK_TOKEN=seu-token-prod
```

## 🔍 Como Obter os Valores

### GCP_PROJECT_ID

```bash
gcloud config get-value project
```

Ou no console: **IAM & Admin** → **Settings** → **Project ID**

### GCP_BUCKET_NAME

1. Vá para **Cloud Storage**
2. Crie ou use um bucket existente
3. Anote o nome do bucket

### GCP_STORAGE_CREDENTIALS_JSON / FIREBASE_CREDENTIALS_JSON

1. Vá para **IAM & Admin** → **Service Accounts**
2. Clique na conta de serviço
3. Vá para **Keys** → **Add Key** → **Create New Key**
4. Escolha **JSON**
5. **Copie todo o conteúdo do arquivo JSON** para a variável de ambiente

### PAGBANK_TOKEN

1. Acesse o painel PagBank/PagSeguro
2. Gere um token de produção
3. **⚠️ Importante**: Use Secret Manager para este valor

### Configuração de Webhooks (Manual)

1. **No painel PagBank**: Configure a URL `https://SUA-URL-CLOUD-RUN/api/payments/webhook`
2. **Eventos**: Selecione notificações de pagamento
3. **Teste**: Simule um pagamento para verificar se chegam as notificações

## ✅ Verificação

### 1. Health Check

Após o deploy, teste:

```bash
curl https://sua-url/api/health
```

### 2. Logs

Verifique os logs no console:

```bash
gcloud logs read --service=seu-servico --limit=50
```

### 3. Variáveis Carregadas

Os logs devem mostrar:

```
Spring active profile: production
GCP Project: seu-project-id
Bucket configured: seu-bucket
```

## 🚨 Solução de Problemas

### Erro: "Credentials not found"

- Verifique se as credenciais estão no caminho correto
- Confirme as permissões IAM
- Use Secret Manager para arquivos de credenciais

### Erro: "PagBank token invalid"

- Verifique se o token está correto
- Confirme se é o token de produção/sandbox correto
- Use Secret Manager para o token

### Erro: "Bucket not found"

- Verifique se o bucket existe
- Confirme as permissões de acesso
- Verifique a região do bucket

## 📖 Próximos Passos

1. **[CLOUD-RUN-DEPLOY.md](CLOUD-RUN-DEPLOY.md)** - Guia completo de deploy
2. **[TESTING_GUIDE.md](TESTING_GUIDE.md)** - Como testar após deploy
3. **[FRONTEND-INTEGRATION.md](FRONTEND-INTEGRATION.md)** - Integração com frontend

---

💡 **Dica**: Use sempre Secret Manager para dados sensíveis em produção!
