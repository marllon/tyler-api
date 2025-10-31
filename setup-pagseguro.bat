@echo off
echo.
echo ===================================================
echo    🎁 CONFIGURACAO PAGSEGURO - DOAÇÕES VIA CPF
echo ===================================================
echo.

echo 🔧 Configurando variáveis de ambiente do PagSeguro...
echo.

echo ⚙️ Para usar o PagSeguro, você precisa configurar:
echo.
echo   PAGSEGURO_TOKEN_SANDBOX=seu_token_sandbox_aqui
echo   PAGSEGURO_TOKEN=seu_token_producao_aqui (opcional)
echo.

echo 📋 Como obter os tokens:
echo   1. Acesse: https://pagseguro.uol.com.br/
echo   2. Faça login na sua conta
echo   3. Vá em "Minha Conta" > "Credenciais"
echo   4. Copie o Token de Aplicação
echo.

echo 💡 VANTAGENS DO PAGSEGURO:
echo   ✅ Aceita cadastro com CPF (não precisa CNPJ)
echo   ✅ Ideal para doações e caridade
echo   ✅ PIX com taxa de 0,99%
echo   ✅ Suporte brasileiro
echo   ✅ API simples e confiável
echo.

echo 🛠️ Para configurar as variáveis de ambiente:
echo.
echo   Método 1 - Permanente (Recomendado):
echo   - Windows Key + R
echo   - Digite: sysdm.cpl
echo   - Aba "Avançado" > "Variáveis de Ambiente"
echo   - Adicione as variáveis PAGSEGURO_TOKEN_SANDBOX
echo.
echo   Método 2 - Temporário (apenas esta sessão):
set /p token_sandbox="Digite seu token SANDBOX do PagSeguro: "
if not "%token_sandbox%"=="" (
    set PAGSEGURO_TOKEN_SANDBOX=%token_sandbox%
    echo ✅ Token sandbox configurado: %token_sandbox%
) else (
    echo ⚠️ Token não configurado
)
echo.

echo 🧪 Testando conexão com PagSeguro...
echo.

echo ⚡ Iniciando Tyler API com PagSeguro...
echo.

cd /d "%~dp0"
if exist ".\mvnw.cmd" (
    echo 🚀 Usando Maven Wrapper...
    .\mvnw.cmd spring-boot:run
) else if exist "mvn.exe" (
    echo 🚀 Usando Maven global...
    mvn spring-boot:run
) else (
    echo ❌ Maven não encontrado!
    echo    Instale o Maven ou use o wrapper
    pause
    exit /b 1
)

echo.
echo ✅ Tyler API com PagSeguro rodando!
echo    Endpoints disponíveis:
echo    - POST http://localhost:8080/api/payments/checkout
echo    - GET  http://localhost:8080/api/payments/{id}/status
echo    - POST http://localhost:8080/api/payments/webhook
echo.
pause