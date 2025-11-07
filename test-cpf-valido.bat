@echo off
echo ====== Teste com CPF Válido ======
curl -X POST "http://localhost:8080/api/payments/checkout" ^
  -H "Content-Type: application/json" ^
  -d "{\"amount\": 1000, \"description\": \"Doacao teste Tyler API\", \"payer\": {\"name\": \"João Silva\", \"email\": \"joao@teste.com\", \"document\": \"11144477735\"}}"