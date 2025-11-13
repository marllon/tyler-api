@echo off
echo =================================
echo 🚀 INICIANDO SERVIDOR TYLER-API
echo =================================

echo 📋 Verificando configurações...
echo ✅ Service Account: src/main/resources/tyler-storage-credentials.json
echo ✅ Bucket: tyler-product-images
echo ✅ Projeto: tyler-dev-c2420

echo.
echo 🔥 Iniciando aplicação Spring Boot...
mvn spring-boot:run -Dspring-boot.run.profiles=local