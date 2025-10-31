@echo off
echo.
echo ========================================================
echo    🏦 CONFIGURACAO PAGBANK API - TYLER API
echo ========================================================
echo.

echo 🎯 Configurando Tyler API com PagBank API oficial!
echo.

echo ✅ CHECKLIST - Você já tem:
echo    □ Conta criada em: https://portaldev.pagbank.com.br/
echo    □ Token obtido na aba "Tokens"
echo    □ Token copiado (formato: Bearer xxxxxxx...)
echo.

set /p "token_sandbox=🔧 Cole aqui seu Token PagBank: "

if "%token_sandbox%"=="" (
    echo.
    echo ❌ Token não pode estar vazio!
    pause
    exit /b 1
)

echo.
echo ========================================================
echo    CONFIGURANDO AMBIENTE
echo ========================================================
echo.

echo 🔧 Configurando variáveis de ambiente para esta sessão...
set "PAGBANK_TOKEN_SANDBOX=%token_sandbox%"

echo ✅ Token configurado: %token_sandbox:~0,20%...
echo.

echo ========================================================
echo    TESTANDO COMPILAÇÃO
echo ========================================================
echo.

echo 🔍 Compilando projeto com PagBank API...
mvn compile -q

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Erro na compilação!
    pause
    exit /b 1
)

echo ✅ Compilação OK!
echo.

echo ========================================================
echo    INICIANDO TYLER API
echo ========================================================
echo.

echo 🚀 Iniciando Tyler API com PagBank...
echo.

echo ⚡ A API vai iniciar em: http://localhost:8080
echo.
echo 📋 Endpoints PagBank disponíveis:
echo    POST http://localhost:8080/api/payments/checkout
echo    GET  http://localhost:8080/api/payments/{id}/status  
echo    GET  http://localhost:8080/actuator/health
echo.

echo 💡 Para testar PIX:
echo    curl -X POST http://localhost:8080/api/payments/checkout \
echo      -H "Content-Type: application/json" \
echo      -d '{"amount":1000,"description":"Doação teste","payer":{"name":"João","email":"joao@email.com","document":"12345678900"}}'
echo.

echo 🐳 Para deploy em container:
echo    docker run -e PAGBANK_TOKEN_SANDBOX=%token_sandbox% tyler-api
echo.

mvn spring-boot:run

echo.
echo ========================================================
echo    🎉 TYLER API RODANDO COM PAGBANK!
echo ========================================================
echo.

pause