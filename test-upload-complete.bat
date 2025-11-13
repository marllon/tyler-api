@echo off
echo =================================
echo 🧪 TESTE COMPLETO DE UPLOAD
echo =================================

echo 📸 1. Criando produto de teste...
curl -X POST "http://localhost:8080/api/products" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Produto Teste\",\"description\":\"Produto para teste de upload\",\"price\":99.99,\"category\":\"Teste\",\"stock\":10}"

echo.
echo 📁 2. Testando upload de imagem...
echo Copie uma imagem JPG para este diretório e renomeie para 'test-image.jpg'
pause

curl -X POST "http://localhost:8080/api/products/SEU_PRODUCT_ID_AQUI/images" ^
  -F "file=@test-image.jpg" ^
  -F "isPrimary=true"

echo.
echo ✅ Teste concluído!
pause