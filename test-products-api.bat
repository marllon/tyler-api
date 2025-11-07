@echo off
echo ===================================================
echo        TESTE DOS ENDPOINTS DE PRODUTOS
echo ===================================================
echo.

echo [1] Testando endpoint de health...
curl -X GET http://localhost:8080/api/health
echo.
echo.

echo [2] Testando listagem de produtos (GET /api/products)...
curl -X GET "http://localhost:8080/api/products?page=1&pageSize=5" -H "Content-Type: application/json"
echo.
echo.

echo [3] Criando produto de teste (POST /api/products)...
curl -X POST http://localhost:8080/api/products ^
  -H "Content-Type: application/json" ^
  -d "{\"name\": \"Camiseta Tyler\", \"description\": \"Camiseta oficial do projeto Tyler\", \"price\": 4500, \"category\": \"roupas\", \"stock\": 10, \"imageUrl\": \"https://example.com/camiseta.jpg\"}"
echo.
echo.

echo [4] Criando segundo produto de teste...
curl -X POST http://localhost:8080/api/products ^
  -H "Content-Type: application/json" ^
  -d "{\"name\": \"Caneca Tyler\", \"description\": \"Caneca personalizada do projeto Tyler\", \"price\": 2500, \"category\": \"acessorios\", \"stock\": 25, \"imageUrl\": \"https://example.com/caneca.jpg\"}"
echo.
echo.

echo [5] Listando produtos novamente para ver os criados...
curl -X GET "http://localhost:8080/api/products?page=1&pageSize=10" -H "Content-Type: application/json"
echo.
echo.

echo [6] Testando busca por categoria...
curl -X GET "http://localhost:8080/api/products?category=roupas" -H "Content-Type: application/json"
echo.
echo.

echo ===================================================
echo Agora execute os testes individuais abaixo:
echo.
echo Para testar busca por ID - substitua {ID} pelo ID real:
echo curl -X GET http://localhost:8080/api/products/{ID}
echo.
echo Para testar atualização - substitua {ID} pelo ID real:
echo curl -X PUT http://localhost:8080/api/products/{ID} -H "Content-Type: application/json" -d "{\"name\": \"Camiseta Tyler Atualizada\", \"price\": 5000}"
echo.
echo Para testar remoção - substitua {ID} pelo ID real:
echo curl -X DELETE http://localhost:8080/api/products/{ID}
echo ===================================================

pause