# ========================================

# 🏦 TYLER API - VARIÁVEIS DE AMBIENTE

# ========================================

#

# 📋 DOCUMENTAÇÃO APENAS - Este arquivo não é carregado pelo código

#

# Para configurar a aplicação, use uma das opções:

#

# 🔧 DESENVOLVIMENTO LOCAL:

# set PAGBANK_TOKEN_SANDBOX=seu_token_aqui

# mvn spring-boot:run

#

# 🐳 DOCKER/CONTAINER:

# docker run -e PAGBANK_TOKEN_SANDBOX=seu_token tyler-api

#

# ☁️ CLOUD RUN:

# Configure as variáveis no painel do Cloud Run

#

# 💻 INTELLIJ IDEA:

# Run Configuration → Environment Variables

# ===== VARIÁVEIS OBRIGATÓRIAS =====

# Token PagBank (obter em: https://portaldev.pagbank.com.br/)

PAGBANK_TOKEN_SANDBOX=your_sandbox_token_here
PAGBANK_TOKEN=your_production_token_here

# ===== VARIÁVEIS OPCIONAIS =====

# Ambiente (sandbox/production)

PAGBANK_ENVIRONMENT=sandbox

# Webhook (para notificações)

PAGBANK_WEBHOOK_SECRET=seu_webhook_secret_aqui
PAGBANK_WEBHOOK_URL=https://seu-dominio.com/api/payments/webhook

# Server port (padrão: 8080)

SERVER_PORT=8080

# Logging level

LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_TYLERPROJECT=DEBUG

# ========================================

# 📋 EXEMPLOS DE USO

# ========================================

# Desenvolvimento local:

# set PAGBANK_TOKEN_SANDBOX=your_token_here && mvn spring-boot:run

# Docker:

# docker run -e PAGBANK_TOKEN_SANDBOX=your_token_here tyler-api

# Cloud Run (yaml):

# env:

# - name: PAGBANK_TOKEN

# value: "your_production_token_here"

# IntelliJ Run Configuration:

# Environment Variables: PAGBANK_TOKEN_SANDBOX=your_token_here
