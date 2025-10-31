@echo off
echo ====================================
echo 🚀 TESTANDO TYLER API - SPRING BOOT
echo ====================================
echo.

REM Definindo a URL base da API Spring Boot
set API_URL=http://localhost:8080/api

echo 📊 1. Testando Health Check...
curl -X GET "%API_URL%/health" -H "Content-Type: application/json"
echo.
echo.

echo 💰 2. Testando Checkout PIX...
curl -X POST "%API_URL%/payments/checkout" ^
  -H "Content-Type: application/json" ^
  -d "{\"amount\":5000,\"description\":\"Teste Spring Boot PIX - R$ 50,00\",\"payer\":{\"name\":\"João Silva\",\"email\":\"joao@teste.com\",\"document\":\"12345678901\"}}"
echo.
echo.

echo 📊 3. Testando Actuator Health...
curl -X GET "http://localhost:8081/actuator/health" -H "Content-Type: application/json"
echo.
echo.

echo ✅ Testes concluídos!
echo.
echo 📋 Endpoints disponíveis:
echo   - GET  %API_URL%/health
echo   - POST %API_URL%/payments/checkout  
echo   - GET  %API_URL%/payments/{id}/status
echo   - POST %API_URL%/payments/webhook
echo   - GET  http://localhost:8081/actuator/health
echo.
pause