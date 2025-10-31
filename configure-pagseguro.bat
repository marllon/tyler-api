@echo off
echo.
echo ========================================================
echo    🎁 ASSISTENTE CONFIGURACAO PAGSEGURO
echo ========================================================
echo.

echo 📝 Este assistente vai te ajudar a configurar sua conta
echo    PagSeguro para receber doações via PIX com CPF!
echo.

pause

echo.
echo ========================================================
echo    PASSO 1: CRIAR CONTA PAGSEGURO
echo ========================================================
echo.

echo 🌐 Vamos abrir o site do PagSeguro para você...
echo.
pause

start https://pagseguro.uol.com.br

echo.
echo ✅ CHECKLIST - Criação da Conta:
echo    □ Clicar em "Criar conta"
echo    □ Preencher com seus dados PESSOAIS (CPF)
echo    □ Confirmar email
echo    □ Fazer login
echo.
echo 🚨 IMPORTANTE: Use seus dados PESSOAIS (CPF)
echo    Não precisa de CNPJ para doações!
echo.

set /p criou_conta="Você já criou a conta PagSeguro? (s/n): "
if /i "%criou_conta%"=="n" (
    echo.
    echo 📋 Vá no site aberto e crie sua conta primeiro.
    echo    Depois execute este script novamente.
    pause
    exit /b 0
)

echo.
echo ========================================================
echo    PASSO 2: OBTER CREDENCIAIS
echo ========================================================
echo.

echo 🔑 Agora vamos pegar suas credenciais...
echo.
pause

start https://pagseguro.uol.com.br

echo.
echo ✅ CHECKLIST - Obter Token:
echo    □ Fazer login no PagSeguro
echo    □ Menu "Minha Conta" → "Credenciais"
echo    □ Copiar o "Token de Aplicação"
echo.

set /p "token_sandbox=🔧 Cole aqui seu Token SANDBOX: "

if "%token_sandbox%"=="" (
    echo.
    echo ❌ Token não pode estar vazio!
    pause
    exit /b 1
)

echo.
echo ========================================================
echo    PASSO 3: CONFIGURAR AMBIENTE
echo ========================================================
echo.

echo 🔧 Configurando variáveis de ambiente...
echo.

set "PAGSEGURO_TOKEN_SANDBOX=%token_sandbox%"

echo ✅ Token configurado: %token_sandbox%
echo.

echo 📝 Salvando configuração no arquivo .env...
(
echo # Tyler API - PagSeguro Configuration
echo PAGSEGURO_TOKEN_SANDBOX=%token_sandbox%
echo PAGSEGURO_ENVIRONMENT=sandbox
) > .env

echo ✅ Arquivo .env criado!
echo.

echo ========================================================
echo    PASSO 4: TESTAR INTEGRAÇÃO
echo ========================================================
echo.

echo 🚀 Vamos testar se tudo está funcionando...
echo.

echo 🔍 Compilando projeto...
mvn compile -q

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Erro na compilação!
    pause
    exit /b 1
)

echo ✅ Compilação OK!
echo.

echo 🚀 Iniciando Tyler API com PagSeguro...
echo.

echo ⚡ A API vai iniciar em: http://localhost:8080
echo.
echo 📋 Endpoints disponíveis:
echo    POST http://localhost:8080/api/payments/checkout
echo    GET  http://localhost:8080/api/payments/{id}/status  
echo    GET  http://localhost:8080/actuator/health
echo.

mvn spring-boot:run

echo.
echo ========================================================
echo    🎉 CONFIGURAÇÃO CONCLUÍDA!
echo ========================================================
echo.

echo ✅ Tyler API está rodando com PagSeguro!
echo ✅ Conta configurada com CPF (sem CNPJ)
echo ✅ Pronto para receber doações via PIX
echo.

pause