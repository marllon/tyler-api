@echo off
echo.
echo ========================================================
echo    🧪 TESTE RAPIDO PAGBANK API
echo ========================================================
echo.

echo 🔧 Configurando token temporário...
set /p "token=Cole seu token PagBank: "

if "%token%"=="" (
    echo ❌ Token obrigatório!
    pause
    exit /b 1
)

set "PAGBANK_TOKEN_SANDBOX=%token%"

echo.
echo 🚀 Iniciando Tyler API para teste...
echo.

start mvn spring-boot:run

echo ⏳ Aguardando API iniciar (30 segundos)...
timeout /t 30 /nobreak

echo.
echo 🧪 Testando Health Check...
curl -s http://localhost:8080/actuator/health

echo.
echo.
echo 💰 Testando criação de PIX...
curl -X POST http://localhost:8080/api/payments/checkout ^
  -H "Content-Type: application/json" ^
  -d "{\"amount\":1000,\"description\":\"Doação teste PagBank\",\"payer\":{\"name\":\"João Teste\",\"email\":\"teste@email.com\",\"document\":\"12345678900\"}}"

echo.
echo.
echo ✅ Teste concluído!
echo.
pause