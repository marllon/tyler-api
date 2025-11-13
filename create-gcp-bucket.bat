@echo off
echo =================================
echo 📦 CRIANDO BUCKET NO GCP
echo =================================

echo 🔐 Autenticando no GCP...
gcloud auth login

echo 📂 Criando bucket para imagens...
gsutil mb -p tyler-dev-c2420 -c STANDARD -l southamerica-east1 gs://tyler-product-images

echo 🔒 Configurando permissões do bucket (privado)...
gsutil iam ch allUsers:objectViewer gs://tyler-product-images

echo ✅ Bucket criado com sucesso!
echo.
echo 📌 Configurações:
echo    - Nome: tyler-product-images
echo    - Projeto: tyler-dev-c2420
echo    - Região: southamerica-east1
echo    - Acesso: URLs assinadas
pause