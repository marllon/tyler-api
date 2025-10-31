@echo off
cls
echo ====================================
echo 🚀 TYLER API - SETUP PAGARME
echo ====================================
echo.

echo Este script irá te ajudar a configurar o Pagarme no Tyler API
echo.

:input_key
echo 🔑 1. Configuração da API Key
echo.
echo Para continuar, você precisa da sua Pagarme Secret Key Test.
echo.
echo 📝 Como obter:
echo    1. Acesse: https://dashboard.pagar.me/signup
echo    2. Faça login ou cadastre-se
echo    3. Vá em Configurações → API Keys
echo    4. Copie a "Secret Key Test" (começa com sk_test_)
echo.

set /p api_key="Digite sua Pagarme Secret Key Test: "

if "%api_key%"=="" (
    echo ❌ API Key não pode estar vazia!
    echo.
    goto :input_key
)

echo %api_key% | findstr /r "^sk_test_" >nul
if %errorlevel% neq 0 (
    echo ❌ API Key deve começar com 'sk_test_'
    echo.
    goto :input_key
)

echo.
echo ✅ API Key válida: %api_key:~0,15%...
echo.

echo 💾 2. Configurando Environment Variable...
echo.

REM Configurar environment variable temporariamente
set PAGARME_API_KEY=%api_key%
echo ✅ PAGARME_API_KEY configurada para esta sessão

echo.
echo 📝 Para configurar permanentemente escolha uma opção:
echo.
echo A) PowerShell (Recomendado):
echo    $env:PAGARME_API_KEY="%api_key%"
echo    [Environment]::SetEnvironmentVariable("PAGARME_API_KEY", "%api_key%", "User")
echo.
echo B) IntelliJ IDEA:
echo    Run → Edit Configurations → Environment Variables
echo    Name: PAGARME_API_KEY
echo    Value: %api_key%
echo.

echo 🏗️ 3. Compilando aplicação...
echo.
call mvn clean compile
if errorlevel 1 (
    echo ❌ Erro na compilação!
    goto :error
)

echo.
echo ✅ Compilação bem-sucedida!
echo.

echo 🚀 4. Iniciando Tyler API...
echo.
echo A aplicação será iniciada com sua API key configurada.
echo Aguarde a mensagem "Started TylerApiApplicationKt"
echo.

set /p start_app="Deseja iniciar a aplicação agora? (s/n): "
if /i "%start_app%"=="s" (
    echo.
    echo 🌟 Iniciando Tyler API...
    echo ⏳ Aguarde alguns segundos...
    echo.
    start cmd /k "echo Tyler API Starting... && mvn spring-boot:run"
    
    echo.
    echo 🎯 A aplicação está sendo iniciada em uma nova janela.
    echo.
    echo 📋 Após inicializar, você pode testar:
    echo.
    echo ✅ Health Check:
    echo    http://localhost:8080/api/health
    echo.
    echo 💰 PIX Checkout:
    echo    POST http://localhost:8080/api/payments/checkout
    echo    {
    echo      "amount": 1000,
    echo      "description": "Teste PIX",
    echo      "payer": {
    echo        "name": "João Silva",
    echo        "email": "joao@teste.com",
    echo        "document": "12345678901"
    echo      }
    echo    }
)

echo.
echo 🎉 Setup concluído com sucesso!
echo.
echo 📋 Resumo da configuração:
echo    ✅ API Key configurada
echo    ✅ Aplicação compilada
echo    ✅ Pronta para uso
echo.
echo 🔗 Links úteis:
echo    - Dashboard Pagarme: https://dashboard.pagar.me
echo    - Documentação: https://docs.pagar.me
echo    - API Local: http://localhost:8080/api/health
echo.
goto :end

:error
echo.
echo ❌ Erro durante o setup!
echo.
echo 💡 Verifique:
echo    - Conexão com internet
echo    - Java 21 instalado
echo    - Maven configurado
echo.

:end
echo.
echo 📞 Precisa de ajuda?
echo    - Verifique o arquivo: PAGARME-SETUP-GUIDE.md
echo    - Execute: check-pagarme-config.bat
echo.
pause