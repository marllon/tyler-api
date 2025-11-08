@echo off
echo ========================================
echo TESTE CRUD COMPLETO - PRODUTOS API
echo Clean Architecture com DTOs
echo ========================================

set BASE_URL=http://localhost:8080/api

echo.
echo 1. Testando Health Check...
curl -s -w "Status: %%{http_code}\n" "%BASE_URL%/health" || echo ERRO: Servidor nao esta rodando

echo.
echo 2. Criando Produto 1...
curl -X POST "%BASE_URL%/products" ^
-H "Content-Type: application/json" ^
-d "{\"name\":\"Smartphone Samsung Galaxy\",\"description\":\"Smartphone Android com 128GB\",\"price\":1299.99,\"category\":\"Eletronicos\",\"stock\":50,\"active\":true}" ^
-w "Status: %%{http_code}\n"

echo.
echo 3. Criando Produto 2...
curl -X POST "%BASE_URL%/products" ^
-H "Content-Type: application/json" ^
-d "{\"name\":\"Notebook Dell Inspiron\",\"description\":\"Notebook para trabalho com Intel i5\",\"price\":2499.90,\"category\":\"Informatica\",\"stock\":25,\"active\":true}" ^
-w "Status: %%{http_code}\n"

echo.
echo 4. Criando Produto 3...
curl -X POST "%BASE_URL%/products" ^
-H "Content-Type: application/json" ^
-d "{\"name\":\"Fone Bluetooth JBL\",\"description\":\"Fone sem fio com cancelamento de ruido\",\"price\":299.90,\"category\":\"Audio\",\"stock\":100,\"active\":true}" ^
-w "Status: %%{http_code}\n"

echo.
echo 5. Listando todos os produtos (paginacao)...
curl -s "%BASE_URL%/products?page=1&pageSize=10" | python -m json.tool 2>nul || curl -s "%BASE_URL%/products?page=1&pageSize=10"

echo.
echo 6. Buscando produto por categoria (Eletronicos)...
curl -s "%BASE_URL%/products?category=Eletronicos&page=1&pageSize=5" | python -m json.tool 2>nul || curl -s "%BASE_URL%/products?category=Eletronicos&page=1&pageSize=5"

echo.
echo 7. Testando busca por produto especifico (primeiro produto)...
echo Digite o ID do primeiro produto criado:
set /p PRODUCT_ID="ID do Produto: "

if not "%PRODUCT_ID%"=="" (
    echo Buscando produto %PRODUCT_ID%...
    curl -s "%BASE_URL%/products/%PRODUCT_ID%" | python -m json.tool 2>nul || curl -s "%BASE_URL%/products/%PRODUCT_ID%"
    
    echo.
    echo 8. Atualizando produto %PRODUCT_ID%...
    curl -X PUT "%BASE_URL%/products/%PRODUCT_ID%" ^
    -H "Content-Type: application/json" ^
    -d "{\"name\":\"Smartphone Samsung Galaxy UPDATED\",\"description\":\"Smartphone Android com 256GB ATUALIZADO\",\"price\":1499.99,\"category\":\"Eletronicos\",\"stock\":75}" ^
    -w "Status: %%{http_code}\n"
    
    echo.
    echo 9. Verificando atualizacao...
    curl -s "%BASE_URL%/products/%PRODUCT_ID%" | python -m json.tool 2>nul || curl -s "%BASE_URL%/products/%PRODUCT_ID%"
    
    echo.
    echo 10. Deletando produto %PRODUCT_ID%...
    curl -X DELETE "%BASE_URL%/products/%PRODUCT_ID%" ^
    -w "Status: %%{http_code}\n"
    
    echo.
    echo 11. Verificando se produto foi deletado...
    curl -s -w "Status: %%{http_code}\n" "%BASE_URL%/products/%PRODUCT_ID%"
) else (
    echo ID nao informado, pulando testes individuais...
)

echo.
echo 12. Listagem final de produtos...
curl -s "%BASE_URL%/products?page=1&pageSize=10" | python -m json.tool 2>nul || curl -s "%BASE_URL%/products?page=1&pageSize=10"

echo.
echo ========================================
echo TESTE CRUD COMPLETO FINALIZADO
echo ========================================
pause