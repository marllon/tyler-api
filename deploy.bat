@echo off
REM Deploy Tyler API para Google Cloud Run (Windows)
REM Uso: deploy.bat [PROJECT_ID]

setlocal EnableDelayedExpansion

REM Configurações
set PROJECT_ID=%1
if "%PROJECT_ID%"=="" set PROJECT_ID=seu-project-id
set IMAGE_NAME=tyler-api
set SERVICE_NAME=tyler-api
set REGION=us-central1
set IMAGE_URL=gcr.io/%PROJECT_ID%/%IMAGE_NAME%

echo 🚀 Iniciando deploy da Tyler API...
echo 📋 Projeto: %PROJECT_ID%
echo 📋 Região: %REGION%
echo 📋 Imagem: %IMAGE_URL%

REM Verificar se gcloud está instalado
gcloud --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Google Cloud SDK não encontrado. Instale primeiro.
    pause
    exit /b 1
)

REM Configurar projeto
echo ⚙️  Configurando projeto...
gcloud config set project %PROJECT_ID%

REM Habilitar APIs necessárias
echo 🔧 Habilitando APIs necessárias...
gcloud services enable cloudbuild.googleapis.com
gcloud services enable run.googleapis.com
gcloud services enable containerregistry.googleapis.com

REM Configurar Docker para GCR
echo 🐳 Configurando Docker...
gcloud auth configure-docker --quiet

REM Build da imagem
echo 🏗️  Buildando imagem Docker...
docker build -t %IMAGE_NAME% .
if errorlevel 1 (
    echo ❌ Erro no build da imagem
    pause
    exit /b 1
)

REM Tag para GCR
echo 🏷️  Taggeando imagem...
docker tag %IMAGE_NAME% %IMAGE_URL%

REM Push para GCR
echo ⬆️  Enviando imagem para Container Registry...
docker push %IMAGE_URL%
if errorlevel 1 (
    echo ❌ Erro no push da imagem
    pause
    exit /b 1
)

REM Deploy no Cloud Run
echo ☁️  Fazendo deploy no Cloud Run...
gcloud run deploy %SERVICE_NAME% ^
    --image %IMAGE_URL% ^
    --platform managed ^
    --region %REGION% ^
    --allow-unauthenticated ^
    --memory 1Gi ^
    --cpu 1 ^
    --timeout 300 ^
    --concurrency 80 ^
    --min-instances 0 ^
    --max-instances 10 ^
    --set-env-vars="SPRING_PROFILES_ACTIVE=production" ^
    --set-env-vars="GCP_PROJECT_ID=%PROJECT_ID%"

if errorlevel 1 (
    echo ❌ Erro no deploy
    pause
    exit /b 1
)

REM Obter URL do serviço
for /f %%i in ('gcloud run services describe %SERVICE_NAME% --region %REGION% --format="value(status.url)"') do set SERVICE_URL=%%i

echo.
echo ✅ Deploy concluído com sucesso!
echo 🌐 URL do serviço: %SERVICE_URL%
echo 🏥 Health check: %SERVICE_URL%/api/health
echo 📚 Swagger UI: %SERVICE_URL%/swagger-ui.html
echo.
echo 📋 Próximos passos:
echo 1. Configurar variáveis de ambiente necessárias
echo 2. Configurar secrets para credenciais
echo 3. Testar endpoints da API
echo.
echo 🔍 Para ver logs:
echo    gcloud run services logs read %SERVICE_NAME% --region %REGION%

pause