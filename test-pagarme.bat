@echo off
echo ========================================
echo         TESTE PAGARME PIX - TYLER API
echo ========================================
echo.

echo 🔧 Configurações necessárias:
echo    1. Configure PAGARME_API_KEY no arquivo .env
echo    2. Defina PAYMENT_PROVIDER=pagarme no .env
echo    3. Cadastre-se em: https://dashboard.pagar.me/
echo.

echo 🔍 Testando endpoint /health...
powershell -Command "try { $response = Invoke-RestMethod -Uri 'http://localhost:8080/health' -Method GET; Write-Host '✅ Health OK:'; $response | ConvertTo-Json -Depth 3 } catch { Write-Host '❌ Erro:' $_.Exception.Message }"

echo.
echo 🚀 Testando criação PIX via Pagarme...
powershell -Command "try { $body = @{ amount = 50.00; description = 'Teste PIX Pagarme Tyler'; payer = @{ name = 'Maria Silva'; email = 'maria@teste.com'; cpf = '19119119100' } } | ConvertTo-Json -Depth 3; Write-Host 'Enviando:' $body; $response = Invoke-RestMethod -Uri 'http://localhost:8080/api/checkout' -Method POST -Body $body -ContentType 'application/json'; Write-Host '✅ PIX Pagarme criado:'; $response | ConvertTo-Json -Depth 3 } catch { Write-Host '❌ Erro:' $_.Exception.Message }"

echo.
echo 📊 Testando consulta de status...
set /p PAYMENT_ID="Digite o Payment ID retornado acima (ou pressione Enter para usar 'test'): "
if "%PAYMENT_ID%"=="" set PAYMENT_ID=test

powershell -Command "try { $response = Invoke-RestMethod -Uri 'http://localhost:8080/api/payment/status/%PAYMENT_ID%' -Method GET; Write-Host '✅ Status consultado:'; $response | ConvertTo-Json -Depth 3 } catch { Write-Host '❌ Erro:' $_.Exception.Message }"

echo.
echo 🧪 Testando webhook (simulação)...
powershell -Command "try { $body = @{ event = 'transaction.paid'; data = @{ id = 'tran_test123'; status = 'paid'; amount = 5000 } } | ConvertTo-Json -Depth 3; $response = Invoke-RestMethod -Uri 'http://localhost:8080/api/webhooks/payment' -Method POST -Body $body -ContentType 'application/json'; Write-Host '✅ Webhook processado:'; $response | ConvertTo-Json -Depth 3 } catch { Write-Host '❌ Erro:' $_.Exception.Message }"

echo.
echo ========================================
echo         INFORMAÇÕES IMPORTANTES
echo ========================================
echo.
echo 📋 API Pagarme:
echo    • Documentação: https://docs.pagar.me/
echo    • Dashboard: https://dashboard.pagar.me/
echo    • Taxa PIX: 0,99%%
echo    • Sandbox: Mesmo endpoint da produção
echo.
echo 🔑 Para configurar:
echo    1. Crie conta em: https://dashboard.pagar.me/
echo    2. Copie sua API Key de teste
echo    3. Configure no arquivo .env:
echo       PAGARME_API_KEY=ak_test_SUA_CHAVE_AQUI
echo       PAYMENT_PROVIDER=pagarme
echo.
echo ✨ Vantagens do Pagarme:
echo    • API moderna e bem documentada
echo    • Muito usado por fintechs
echo    • Suporte robusto
echo    • Webhooks em tempo real
echo.
echo ========================================
pause