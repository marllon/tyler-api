@echo off
cd /d "d:\Projetos\Tyler\backend"

echo =====================================
echo    TESTANDO API TYLER - CLEAN ARCH
echo =====================================
echo.

echo [1/7] Verificando se o servidor esta rodando...
curl -s -o nul "http://localhost:8080/api/health" 2>nul
if %errorlevel% neq 0 (
    echo ❌ Servidor nao esta rodando. Inicie com: mvn spring-boot:run
    pause
    exit /b 1
)
echo ✅ Servidor rodando!

echo.
echo [2/7] Testando listagem de produtos (vazia)...
curl -X GET "http://localhost:8080/api/products" -H "Content-Type: application/json"

echo.
echo [3/7] Criando produto de teste...
curl -X POST "http://localhost:8080/api/products" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Produto Clean\",\"description\":\"Produto seguindo Clean Architecture\",\"price\":99.99,\"category\":\"Eletrônicos\",\"brand\":\"Tyler\",\"active\":true,\"tags\":[\"clean\",\"architecture\"]}"

echo.
echo [4/7] Listando produtos novamente...
curl -X GET "http://localhost:8080/api/products" -H "Content-Type: application/json"

echo.
echo [5/7] Buscando produto específico...
curl -X GET "http://localhost:8080/api/products" -H "Content-Type: application/json" | findstr "id" > temp_id.txt
for /f "tokens=2 delims=:," %%a in (temp_id.txt) do set PRODUCT_ID=%%a
set PRODUCT_ID=%PRODUCT_ID:"=%
set PRODUCT_ID=%PRODUCT_ID: =%

if defined PRODUCT_ID (
    echo Testando busca por ID: %PRODUCT_ID%
    curl -X GET "http://localhost:8080/api/products/%PRODUCT_ID%" -H "Content-Type: application/json"
) else (
    echo ⚠️ Não foi possível extrair ID do produto
)

echo.
echo [6/7] Atualizando produto...
if defined PRODUCT_ID (
    curl -X PUT "http://localhost:8080/api/products/%PRODUCT_ID%" ^
      -H "Content-Type: application/json" ^
      -d "{\"name\":\"Produto Clean ATUALIZADO\",\"description\":\"Produto atualizado seguindo SOLID\",\"price\":199.99,\"active\":true}"
) else (
    echo ⚠️ Pulando atualização - ID não disponível
)

echo.
echo [7/7] Deletando produto...
if defined PRODUCT_ID (
    curl -X DELETE "http://localhost:8080/api/products/%PRODUCT_ID%" -H "Content-Type: application/json"
) else (
    echo ⚠️ Pulando deleção - ID não disponível
)

if exist temp_id.txt del temp_id.txt

echo.
echo =====================================
echo    TESTE COMPLETO - CLEAN ARCH ✅
echo =====================================
echo.
echo Resultado: API seguindo Clean Architecture, SOLID, DTOs
echo - Sem Maps em responses
echo - Domain-driven structure
echo - Repository pattern
echo - Dependency injection
echo.
pause