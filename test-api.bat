@echo off
echo Testando Tyler API - Checkout PIX
echo.

echo ====== 1. Health Check ======
curl "http://localhost:8080/api/health"
echo.
echo.

echo ====== 2. Checkout PIX ======
curl -X POST "http://localhost:8080/api/payments/checkout" ^
  -H "Content-Type: application/json" ^
  -d "{\"amount\": 1000, \"description\": \"Doacao teste Tyler API\", \"payer\": {\"name\": \"João Silva\", \"email\": \"joao@teste.com\", \"document\": \"12345678901\"}}"

echo.
echo.
echo ====== Teste concluído ======