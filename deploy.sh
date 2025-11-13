#!/bin/bash

# Deploy Tyler API para Google Cloud Run
# Uso: ./deploy.sh [PROJECT_ID]

set -e

# Configurações
PROJECT_ID=${1:-"seu-project-id"}
IMAGE_NAME="tyler-api"
SERVICE_NAME="tyler-api"
REGION="us-central1"
IMAGE_URL="gcr.io/$PROJECT_ID/$IMAGE_NAME"

echo "🚀 Iniciando deploy da Tyler API..."
echo "📋 Projeto: $PROJECT_ID"
echo "📋 Região: $REGION"
echo "📋 Imagem: $IMAGE_URL"

# Verificar se está logado no gcloud
echo "🔐 Verificando autenticação..."
gcloud auth list --filter=status:ACTIVE --format="value(account)" | head -n1
if [ $? -ne 0 ]; then
    echo "❌ Faça login no gcloud primeiro: gcloud auth login"
    exit 1
fi

# Configurar projeto
echo "⚙️  Configurando projeto..."
gcloud config set project $PROJECT_ID

# Habilitar APIs necessárias
echo "🔧 Habilitando APIs necessárias..."
gcloud services enable cloudbuild.googleapis.com
gcloud services enable run.googleapis.com
gcloud services enable containerregistry.googleapis.com

# Configurar Docker para GCR
echo "🐳 Configurando Docker..."
gcloud auth configure-docker --quiet

# Build da imagem
echo "🏗️  Buildando imagem Docker..."
docker build -t $IMAGE_NAME .

# Tag para GCR
echo "🏷️  Taggeando imagem..."
docker tag $IMAGE_NAME $IMAGE_URL

# Push para GCR
echo "⬆️  Enviando imagem para Container Registry..."
docker push $IMAGE_URL

# Deploy no Cloud Run
echo "☁️  Fazendo deploy no Cloud Run..."
gcloud run deploy $SERVICE_NAME \
    --image $IMAGE_URL \
    --platform managed \
    --region $REGION \
    --allow-unauthenticated \
    --memory 1Gi \
    --cpu 1 \
    --timeout 300 \
    --concurrency 80 \
    --min-instances 0 \
    --max-instances 10 \
    --set-env-vars="SPRING_PROFILES_ACTIVE=production" \
    --set-env-vars="GCP_PROJECT_ID=$PROJECT_ID"

# Obter URL do serviço
SERVICE_URL=$(gcloud run services describe $SERVICE_NAME --region $REGION --format="value(status.url)")

echo ""
echo "✅ Deploy concluído com sucesso!"
echo "🌐 URL do serviço: $SERVICE_URL"
echo "🏥 Health check: $SERVICE_URL/api/health"
echo "📚 Swagger UI: $SERVICE_URL/swagger-ui.html"
echo ""
echo "📋 Próximos passos:"
echo "1. Configurar variáveis de ambiente necessárias"
echo "2. Configurar secrets para credenciais"
echo "3. Testar endpoints da API"
echo ""
echo "🔍 Para ver logs:"
echo "   gcloud run services logs read $SERVICE_NAME --region $REGION"