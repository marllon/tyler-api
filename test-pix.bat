@echo off
REM 🧪 Script de Teste PIX - Tyler Project (Windows)
REM Execute: test-pix.bat

echo 🪙 Testando Integração PIX - Projeto Tyler
echo ===========================================

REM Verificar se a API está rodando
echo 📡 Verificando se a API está online...
curl -s http://localhost:8080/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ API está rodando em localhost:8080
) else (
    echo ❌ API não está rodando. Execute: gradlew.bat run
    pause
    exit /b 1
)

echo.
echo 💳 Criando pagamento PIX de teste...

REM Criar arquivo JSON temporário
echo {> temp_request.json
echo   "type": "product",>> temp_request.json
echo   "amount": 15.00,>> temp_request.json
echo   "quantity": 1,>> temp_request.json
echo   "description": "Teste PIX Automatizado Windows",>> temp_request.json
echo   "customer": {>> temp_request.json
echo     "name": "João Teste",>> temp_request.json
echo     "lastName": "Silva",>> temp_request.json
echo     "email": "joao.teste@email.com",>> temp_request.json
echo     "document": "12345678901",>> temp_request.json
echo     "phone": "11987654321">> temp_request.json
echo   },>> temp_request.json
echo   "productId": "test-product-pix">> temp_request.json
echo }>> temp_request.json

REM Fazer request
curl -s -X POST http://localhost:8080/api/checkout -H "Content-Type: application/json" -d @temp_request.json > temp_response.json

echo 📄 Resposta da API:
type temp_response.json

echo.
echo 🎯 Teste concluído!
echo.
echo 📋 Para testar manualmente:
echo 1. Abra o arquivo test-pix.html no navegador
echo 2. Clique em 'Criar PIX'
echo 3. Use o QR Code gerado para testar
echo.
echo 📱 Para mais testes, consulte o TESTING_GUIDE.md

REM Limpar arquivos temporários
del temp_request.json >nul 2>&1
del temp_response.json >nul 2>&1

pause