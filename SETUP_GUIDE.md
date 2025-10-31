# 🚀 Guia de Configuração - Projeto Tyler

## 1. 🔐 Configuração do Firebase Authentication

### Passo 1: Console Firebase

1. Acesse [Firebase Console](https://console.firebase.google.com)
2. Selecione seu projeto `projeto-tyler`
3. Vá em **Authentication** → **Sign-in method**
4. Ative os provedores necessários:
   - ✅ Email/Password (principal)
   - ✅ Google (recomendado)
   - ❓ Facebook (opcional)

### Passo 2: Configurar SDK no Frontend

```javascript
// firebase-config.js
import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";

const firebaseConfig = {
  projectId: "projeto-tyler",
  authDomain: "projeto-tyler.firebaseapp.com",
  // ... outras configs
};

export const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
```

### Passo 3: Configurar Service Account

1. Firebase Console → **Project Settings** → **Service Accounts**
2. **Generate New Private Key** → Salvar como `firebase-admin-sdk.json`
3. Colocar na pasta `config/` do backend

## 2. 🗄️ Configuração do Firestore

### Passo 1: Habilitar Firestore

1. Firebase Console → **Firestore Database**
2. **Create database** → **Start in production mode**
3. Escolher região: `southamerica-east1` (São Paulo)

### Passo 2: Deploy das Regras de Segurança

```bash
# No diretório backend/
firebase deploy --only firestore:rules
```

### Passo 3: Criar Índices

```bash
firebase deploy --only firestore:indexes
```

### Passo 4: Criar Primeiro Admin

```bash
# Via Firebase Console ou CLI
firebase auth:import admin-user.json
```

## 3. 💳 Configuração de Pagamentos

### Stripe (Internacional)

1. Criar conta em [stripe.com](https://stripe.com)
2. Pegar as chaves em **Developers** → **API Keys**
3. Configurar webhooks para: `https://sua-api.com/webhooks/stripe`

### Mercado Pago (Brasil)

1. Criar conta em [mercadopago.com.br/developers](https://mercadopago.com.br/developers)
2. Criar aplicação
3. Pegar Access Token
4. Configurar webhooks para: `https://sua-api.com/webhooks/mercadopago`

## 4. 🌍 Variáveis de Ambiente

### Arquivo `.env`

```env
# Firebase
FIREBASE_PROJECT_ID=projeto-tyler
FIREBASE_REGION=southamerica-east1
USE_FIREBASE_AUTH=true

# Pagamentos
PAYMENT_PROVIDER=stripe
STRIPE_SECRET_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
MP_ACCESS_TOKEN=APP_USR-...
MP_WEBHOOK_SECRET=...

# Email
EMAIL_PROVIDER=sendgrid
EMAIL_API_KEY=SG...
EMAIL_FROM=noreply@tylerlimaeler.org

# Site
PUBLIC_SITE_URL=https://tylerlimaeler.org
DEFAULT_CURRENCY=BRL
ALLOWED_ORIGINS=https://tylerlimaeler.org,http://localhost:5173

# Segurança
RAFFLE_COMMIT_SALT=mude-este-salt-por-algo-seguro-e-unico
```

## 5. 🚀 Deploy e Execução

### Desenvolvimento Local

```bash
# 1. Instalar dependências
./gradlew build

# 2. Configurar variáveis de ambiente
# Editar .env ou configurar no sistema

# 3. Executar
./gradlew run
```

### Deploy (Cloud Run/App Engine)

```bash
# 1. Build da aplicação
./gradlew shadowJar

# 2. Deploy
gcloud run deploy tyler-api \
  --source . \
  --platform managed \
  --region southamerica-east1 \
  --allow-unauthenticated
```

## 6. 🗄️ Estrutura de Dados

### Collections Firestore

```
projeto-tyler/
├── products/          # Produtos da loja
├── goals/            # Metas de arrecadação
├── raffles/          # Rifas
│   └── tickets/      # Subcoleção: bilhetes
├── events/           # Eventos
├── orders/           # Pedidos de compra
├── donations/        # Doações
└── admins/           # Administradores
```

### Exemplo de Documento

```json
// products/prod123
{
  "name": "Camiseta Tyler",
  "description": "Camiseta oficial do projeto",
  "price": 49.9,
  "category": "clothing",
  "stock": 100,
  "active": true,
  "imageUrl": "https://...",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

## 7. 📊 Monitoramento

### Firebase Analytics

- Ativar no console Firebase
- Acompanhar conversões e eventos

### Logs

- Google Cloud Logging (automático)
- Sentry (erro tracking)

### Métricas

- Cloud Monitoring
- Alertas para falhas de pagamento
- Monitoramento de performance

## 8. 🔒 Segurança

### Checklist

- ✅ HTTPS obrigatório
- ✅ Regras Firestore restritivas
- ✅ Validação de entrada
- ✅ Rate limiting
- ✅ Logs de auditoria
- ✅ Backup automático

### Firestore Rules (já implementadas)

- Usuários só veem seus próprios dados
- Admins têm acesso administrativo
- Validação de tipos e formatos
- Prevenção de escalação de privilégios
