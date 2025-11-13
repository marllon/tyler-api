@echo off
echo =================================
echo 🧪 TESTE DE UPLOAD DE IMAGENS
echo =================================

echo 📸 Testando upload de imagem para produto...

curl -X POST "http://localhost:8080/products/test-product/images" ^
  -F "file=@test-image.jpg" ^
  -F "isPrimary=true" ^
  -H "Content-Type: multipart/form-data"

echo.
echo ✅ Teste concluído!
pause