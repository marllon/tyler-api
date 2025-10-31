#!/bin/bash

# 🧪 Teste PIX Manual - Tyler Project
# Script para testar PIX via API do Mercado Pago diretamente

echo "🪙 Teste PIX Manual - Projeto Tyler"
echo "===================================="

# Ler configurações do .env
if [ -f .env ]; then
    export $(cat .env | grep -v '#' | awk '/=/ {print $1}')
    echo "✅ Configurações carregadas do .env"
else
    echo "❌ Arquivo .env não encontrado!"
    exit 1
fi

echo ""
echo "💳 Testando criação de PIX via Mercado Pago..."
echo "Access Token: ${MP_ACCESS_TOKEN:0:10}..."

# Criar pagamento PIX diretamente no Mercado Pago
IDEMPOTENCY_KEY="tyler-test-$(date +%s)-$(shuf -i 1000-9999 -n 1)"

RESPONSE=$(curl -s -X POST "https://api.mercadopago.com/v1/payments" \
  -H "Authorization: Bearer $MP_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: $IDEMPOTENCY_KEY" \
  -d '{
    "transaction_amount": 25.00,
    "description": "Teste PIX Tyler - Produto",
    "payment_method_id": "pix",
    "payer": {
      "email": "joao.teste@email.com",
      "first_name": "João",
      "last_name": "Teste",
      "identification": {
        "type": "CPF",
        "number": "19119119100"
      }
    }
  }')

echo ""
echo "📄 Resposta do Mercado Pago:"
echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"

# Extrair dados importantes
PAYMENT_ID=$(echo "$RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
QR_CODE=$(echo "$RESPONSE" | grep -o '"qr_code":"[^"]*"' | cut -d'"' -f4)

if [ ! -z "$PAYMENT_ID" ]; then
    echo ""
    echo "✅ PIX criado com sucesso!"
    echo "🆔 Payment ID: $PAYMENT_ID"
    
    if [ ! -z "$QR_CODE" ]; then
        echo "📱 QR Code PIX disponível (copie e cole no seu banco):"
        echo "$QR_CODE"
    fi
    
    echo ""
    echo "🔍 Para verificar status:"
    echo "curl -H 'Authorization: Bearer $MP_ACCESS_TOKEN' https://api.mercadopago.com/v1/payments/$PAYMENT_ID"
    
else
    echo ""
    echo "❌ Erro ao criar PIX. Verifique:"
    echo "1. Access Token do Mercado Pago"
    echo "2. Conexão com internet"
    echo "3. Configurações da aplicação MP"
fi

echo ""
echo "🎯 Teste manual concluído!"