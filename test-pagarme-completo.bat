@echo off
echo ====================================
echo 🚀 TESTE PAGARME PIX - TYLER API
echo ====================================
echo.

REM Definindo a URL base da API
set API_URL=http://localhost:8080/api

echo 📊 1. Testando Health Check...
curl -X GET "%API_URL%/health" -H "Content-Type: application/json"
echo.
echo.

echo 💰 2. Testando Checkout PIX com Pagarme...
echo JSON sendo enviado:
echo {
echo   "amount": 5000,
echo   "description": "Teste PIX Pagarme - R$ 50,00",
echo   "payer": {
echo     "name": "João Silva",
echo     "email": "joao@teste.com", 
echo     "document": "12345678901"
echo   }
echo }
echo.

curl -X POST "%API_URL%/payments/checkout" ^
  -H "Content-Type: application/json" ^
  -d "{\"amount\":5000,\"description\":\"Teste PIX Pagarme - R$ 50,00\",\"payer\":{\"name\":\"João Silva\",\"email\":\"joao@teste.com\",\"document\":\"12345678901\"}}"
echo.
echo.

echo 📊 3. Testando Status de Pagamento...
curl -X GET "%API_URL%/payments/test_transaction_123/status" -H "Content-Type: application/json"
echo.
echo.

echo 🔔 4. Testando Webhook...
curl -X POST "%API_URL%/payments/webhook" ^
  -H "Content-Type: application/json" ^
  -d "{\"type\":\"order.paid\",\"data\":{\"id\":\"test_order_123\",\"status\":\"paid\"}}"
echo.
echo.

echo ✅ Testes concluídos!
echo.
echo 📋 Endpoints testados:
echo   - GET  %API_URL%/health
echo   - POST %API_URL%/payments/checkout  
echo   - GET  %API_URL%/payments/{id}/status
echo   - POST %API_URL%/payments/webhook
echo.
echo 🔑 Para usar com API key real:
echo   1. Crie conta em: https://dashboard.pagar.me/signup
echo   2. Copie sua Secret Key Test
echo   3. Execute: set PAGARME_SECRET_KEY_TEST=sk_test_sua_key_aqui
echo   4. Reinicie a aplicação
echo.
pause