@echo off
echo Testando se a API Tyler está rodando...
echo.

curl -s -o nul -w "Status: %%{http_code}\n" http://localhost:8080/api/health

if %ERRORLEVEL% EQU 0 (
    echo ✅ API está ONLINE!
    echo.
    echo Testando endpoint de produtos...
    curl -X GET "http://localhost:8080/api/products?page=1&pageSize=3" -H "Content-Type: application/json"
) else (
    echo ❌ API não está respondendo. Verifique se ela está rodando na porta 8080.
)

echo.
pause