@echo off
echo ========================================
echo         TESTE DA TYLER API (MAVEN)
echo ========================================
echo.

echo 🔍 Testando endpoint /health...
powershell -Command "try { $response = Invoke-RestMethod -Uri 'http://localhost:8080/health' -Method GET; Write-Host '✅ Health OK:'; $response | ConvertTo-Json -Depth 3 } catch { Write-Host '❌ Erro:' $_.Exception.Message }"

echo.
echo 🚀 Testando endpoint /api/checkout (PIX)...
powershell -Command "try { $body = @{ amount = 25.00; description = 'Teste PIX Maven'; payer = @{ name = 'João Silva'; email = 'joao@teste.com'; cpf = '19119119100' } } | ConvertTo-Json -Depth 3; $response = Invoke-RestMethod -Uri 'http://localhost:8080/api/checkout' -Method POST -Body $body -ContentType 'application/json'; Write-Host '✅ Checkout PIX OK:'; $response | ConvertTo-Json -Depth 3 } catch { Write-Host '❌ Erro:' $_.Exception.Message }"

echo.
echo 📊 Testando endpoint /api/payment/status...
powershell -Command "try { $response = Invoke-RestMethod -Uri 'http://localhost:8080/api/payment/status/test123' -Method GET; Write-Host '✅ Status OK:'; $response | ConvertTo-Json -Depth 3 } catch { Write-Host '❌ Erro:' $_.Exception.Message }"

echo.
echo ========================================
echo         TESTES CONCLUÍDOS
echo ========================================
pause