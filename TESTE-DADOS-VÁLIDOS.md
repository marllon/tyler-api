# 🧪 Teste PIX com Dados Válidos

## 📋 Comando Correto PowerShell

```powershell
$body = @{
    amount = 1000
    description = "Teste PIX Local - Dados Válidos"
    payer = @{
        name = "Teste Tyler"
        email = "teste@tyler.com"
        document = "11144477735"  # CPF válido para teste
    }
} | ConvertTo-Json -Depth 3

Invoke-RestMethod -Uri "http://localhost:8080/api/payments/checkout" -Method POST -ContentType "application/json" -Body $body
```

## 🔍 Problemas Identificados

### ✅ **Corrigido**: URL SANDBOX

- Antes: Token sandbox → URL produção → 401
- Agora: Token sandbox → URL sandbox → 200 ✅

### 🔧 **Em Correção**: Payload

1. **CPF inválido**: `12345678901` → `11144477735` (válido)
2. **Quantity**: Adicionando explicitamente `quantity = 1`
3. **Tipo PIX**: Verificando se "PIX" é o valor correto

## 🎯 Próximos Passos

1. **Recompile**: `mvn compile -q`
2. **Reinicie** a aplicação
3. **Teste** com CPF válido
4. **Verifique logs** para novos erros

## 📖 CPF de Teste Válido

- **11144477735** - CPF algoritmo válido para testes
