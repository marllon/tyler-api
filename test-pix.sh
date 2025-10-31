#!/bin/bash

# 🧪 Script de Teste PIX - Tyler Project
# Execute: chmod +x test-pix.sh && ./test-pix.sh

echo "🪙 Testando Integração PIX - Projeto Tyler"
echo "==========================================="

# Verificar se a API está rodando
echo "📡 Verificando se a API está online..."
if curl -s http://localhost:8080/health > /dev/null 2>&1; then
    echo "✅ API está rodando em localhost:8080"
else
    echo "❌ API não está rodando. Execute: ./gradlew run"
    exit 1
fi

echo ""
echo "💳 Criando pagamento PIX de teste..."

# Criar pagamento PIX
RESPONSE=$(curl -s -X POST http://localhost:8080/api/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "type": "product",
    "amount": 15.00,
    "quantity": 1,
    "description": "Teste PIX Automatizado",
    "customer": {
      "name": "João Teste",
      "lastName": "Silva", 
      "email": "joao.teste@email.com",
      "document": "12345678901",
      "phone": "11987654321"
    },
    "productId": "test-product-pix"
  }')

echo "📄 Resposta da API:"
echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"

# Extrair paymentId se possível
PAYMENT_ID=$(echo "$RESPONSE" | grep -o '"paymentId":"[^"]*"' | cut -d'"' -f4)

if [ ! -z "$PAYMENT_ID" ]; then
    echo ""
    echo "🔍 Payment ID encontrado: $PAYMENT_ID"
    echo "📊 Consultando status do pagamento..."
    
    sleep 2
    
    STATUS_RESPONSE=$(curl -s http://localhost:8080/api/payment/status/$PAYMENT_ID)
    echo "📄 Status do pagamento:"
    echo "$STATUS_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$STATUS_RESPONSE"
else
    echo "⚠️  Não foi possível extrair o Payment ID da resposta"
fi

echo ""
echo "🎯 Teste concluído!"
echo ""
echo "📋 Para testar manualmente:"
echo "1. Abra o arquivo test-pix.html no navegador"
echo "2. Clique em 'Criar PIX'"
echo "3. Use o QR Code gerado para testar"
echo ""
echo "📱 Para simular pagamento (ambiente de teste):"
echo "curl -X PUT https://api.mercadopago.com/v1/payments/$PAYMENT_ID \\"
echo "  -H 'Authorization: Bearer SEU_ACCESS_TOKEN' \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -d '{\"status\": \"approved\"}'"