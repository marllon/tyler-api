@echo off
cls
echo ====================================
echo 🔑 VERIFICADOR CONFIGURAÇÃO PAGARME
echo ====================================
echo.

echo 🔍 1. Verificando Environment Variables...
echo.

REM Verificar se PAGARME_API_KEY está definida
if defined PAGARME_API_KEY (
    echo ✅ PAGARME_API_KEY encontrada
    echo    Valor: %PAGARME_API_KEY:~0,15%...
    echo.
) else (
    echo ❌ PAGARME_API_KEY não encontrada!
    echo.
    echo 💡 Para configurar execute um dos comandos:
    echo.
    echo    PowerShell:
    echo    $env:PAGARME_API_KEY="sk_test_sua_key_aqui"
    echo.
    echo    CMD:
    echo    set PAGARME_API_KEY=sk_test_sua_key_aqui
    echo.
    goto :end
)

REM Verificar formato da API key
echo %PAGARME_API_KEY% | findstr /r "^sk_test_" >nul
if %errorlevel% == 0 (
    echo ✅ Formato correto: Secret Key Test
    echo.
) else (
    echo %PAGARME_API_KEY% | findstr /r "^sk_live_" >nul
    if %errorlevel% == 0 (
        echo ⚠️  Formato: Secret Key Live (PRODUÇÃO)
        echo    Certifique-se que quer usar ambiente de produção!
        echo.
    ) else (
        echo ❌ Formato incorreto da API key
        echo    Deve começar com 'sk_test_' ou 'sk_live_'
        echo.
        goto :end
    )
)

echo 🧪 2. Testando conectividade com Pagarme...
echo.

REM Teste básico de conectividade
echo    Fazendo request para API Pagarme...
curl -s -w "Status: %%{http_code}\n" -H "Authorization: Basic %PAGARME_API_KEY%:" https://api.pagar.me/core/v5/orders | findstr /r "Status: [0-9][0-9][0-9]"

if %errorlevel% == 0 (
    echo ✅ Conectividade OK
    echo.
) else (
    echo ❌ Erro de conectividade
    echo    Verifique sua conexão com internet
    echo.
)

echo 🚀 3. Verificando aplicação Tyler...
echo.

REM Verificar se aplicação está rodando
curl -s http://localhost:8080/api/health >nul 2>&1
if %errorlevel% == 0 (
    echo ✅ Tyler API está rodando na porta 8080
    echo.
    
    echo 💰 4. Testando endpoint PIX...
    echo.
    
    REM Teste do endpoint PIX
    curl -X POST http://localhost:8080/api/payments/checkout ^
         -H "Content-Type: application/json" ^
         -d "{\"amount\":1000,\"description\":\"Teste Configuração\",\"payer\":{\"name\":\"João Silva\",\"email\":\"joao@teste.com\",\"document\":\"12345678901\"}}"
    echo.
    echo.
    
) else (
    echo ❌ Tyler API não está rodando
    echo.
    echo 💡 Para iniciar a aplicação:
    echo    cd "d:\Projetos\Tyler\backend"
    echo    mvn spring-boot:run
    echo.
)

echo ✅ Verificação concluída!
echo.
echo 📋 Status da Configuração:
echo    - Environment Variable: %PAGARME_API_KEY:~0,15%...
echo    - Formato: OK
echo    - API Tyler: Verificar acima

:end
echo.
echo 🔗 Links úteis:
echo    - Dashboard: https://dashboard.pagar.me
echo    - Documentação: https://docs.pagar.me
echo    - Suporte: suporte@pagar.me
echo.
pause