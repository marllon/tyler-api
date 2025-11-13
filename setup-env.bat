@echo off
REM Configurar variáveis de ambiente para Tyler API no Cloud Run
REM Uso: setup-env.bat PROJECT_ID

setlocal EnableDelayedExpansion

set PROJECT_ID=%1
if "%PROJECT_ID%"=="" (
    echo ❌ Uso: setup-env.bat PROJECT_ID
    pause
    exit /b 1
)

set SERVICE_NAME=tyler-api
set REGION=us-central1

echo 🔧 Configurando variáveis de ambiente para Tyler API...
echo 📋 Projeto: %PROJECT_ID%
echo 📋 Serviço: %SERVICE_NAME%

REM Solicitar informações do usuário
echo.
echo 📝 Por favor, forneça as seguintes informações:
echo.

set /p GCP_BUCKET_NAME="Nome do bucket do Google Cloud Storage (ex: tyler-products-images): "
set /p PAGBANK_TOKEN="Token do PagBank: "
set /p WEBHOOK_URL="URL do webhook PagBank (deixe vazio para auto-gerar): "

REM Obter URL do serviço se webhook_url estiver vazio
if "%WEBHOOK_URL%"=="" (
    for /f %%i in ('gcloud run services describe %SERVICE_NAME% --region %REGION% --format="value(status.url)"') do (
        set SERVICE_URL=%%i
        set WEBHOOK_URL=!SERVICE_URL!/api/payments/webhook
    )
)

echo.
echo 🔐 Configurando secrets no Secret Manager...

REM Criar secrets para credenciais sensíveis
echo Criando secret para PagBank token...
echo %PAGBANK_TOKEN% | gcloud secrets create pagbank-token --data-file=-

echo.
echo 📝 IMPORTANTE: Você precisa fazer upload manual dos arquivos de credenciais:
echo 1. Firebase Admin SDK JSON
echo 2. Google Cloud Storage Service Account JSON
echo.
echo Exemplo:
echo gcloud secrets create firebase-credentials --data-file=firebase-admin-sdk.json
echo gcloud secrets create storage-credentials --data-file=storage-service-account.json

echo.
echo ⚙️  Atualizando serviço Cloud Run com variáveis de ambiente...

gcloud run services update %SERVICE_NAME% ^
    --region %REGION% ^
    --set-env-vars="SPRING_PROFILES_ACTIVE=production" ^
    --set-env-vars="GCP_PROJECT_ID=%PROJECT_ID%" ^
    --set-env-vars="GCP_BUCKET_NAME=%GCP_BUCKET_NAME%" ^
    --set-env-vars="PAGBANK_WEBHOOK_URL=%WEBHOOK_URL%" ^
    --set-secrets="PAGBANK_TOKEN=pagbank-token:latest"

if errorlevel 1 (
    echo ❌ Erro ao atualizar serviço
    pause
    exit /b 1
)

echo.
echo ✅ Configuração concluída!
echo.
echo 📋 Variáveis configuradas:
echo - SPRING_PROFILES_ACTIVE=production
echo - GCP_PROJECT_ID=%PROJECT_ID%
echo - GCP_BUCKET_NAME=%GCP_BUCKET_NAME%
echo - PAGBANK_WEBHOOK_URL=%WEBHOOK_URL%
echo - PAGBANK_TOKEN=****** (via secret)
echo.
echo 🔍 Para verificar configuração:
echo gcloud run services describe %SERVICE_NAME% --region %REGION%

pause