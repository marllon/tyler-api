@echo off
REM 🧪 Teste PIX Manual - Tyler Project (Windows)
REM Script para testar PIX via API do Mercado Pago diretamente

echo 🪙 Teste PIX Manual - Projeto Tyler
echo ====================================

REM Verificar se .env existe
if not exist .env (
    echo ❌ Arquivo .env não encontrado!
    echo Crie o arquivo .env com suas configurações
    pause
    exit /b 1
)

REM Ler MP_ACCESS_TOKEN do .env (método simples)
for /f "tokens=2 delims==" %%a in ('type .env ^| findstr "MP_ACCESS_TOKEN"') do set MP_ACCESS_TOKEN=%%a

if "%MP_ACCESS_TOKEN%"=="" (
    echo ❌ MP_ACCESS_TOKEN não encontrado no .env
    pause
    exit /b 1
)

echo ✅ Access Token carregado: %MP_ACCESS_TOKEN:~0,10%...
echo.
echo 💳 Testando criação de PIX via Mercado Pago...

REM Criar arquivo JSON para o request
echo {> temp_pix_request.json
echo   "transaction_amount": 25.00,>> temp_pix_request.json
echo   "description": "Teste PIX Tyler - Produto Windows",>> temp_pix_request.json
echo   "payment_method_id": "pix",>> temp_pix_request.json
echo   "payer": {>> temp_pix_request.json
echo     "email": "joao.teste@email.com",>> temp_pix_request.json
echo     "first_name": "João",>> temp_pix_request.json
echo     "last_name": "Teste",>> temp_pix_request.json
echo     "identification": {>> temp_pix_request.json
echo       "type": "CPF",>> temp_pix_request.json
echo       "number": "19119119100">> temp_pix_request.json
echo     }>> temp_pix_request.json
echo   }>> temp_pix_request.json
echo }>> temp_pix_request.json

REM Fazer request para Mercado Pago
curl -s -X POST "https://api.mercadopago.com/v1/payments" ^
  -H "Authorization: Bearer %MP_ACCESS_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -H "X-Idempotency-Key: tyler-test-%RANDOM%-%TIME:~-5%" ^
  -d @temp_pix_request.json > temp_pix_response.json

echo.
echo 📄 Resposta do Mercado Pago:
type temp_pix_response.json

echo.
echo ✅ Teste PIX direto concluído!
echo.
echo 📋 Para verificar status do pagamento:
echo curl -H "Authorization: Bearer %MP_ACCESS_TOKEN%" https://api.mercadopago.com/v1/payments/PAYMENT_ID
echo.
echo 🎯 Se recebeu um JSON com "id" e "qr_code", o PIX está funcionando!

REM Limpar arquivos temporários
del temp_pix_request.json >nul 2>&1
del temp_pix_response.json >nul 2>&1

pause