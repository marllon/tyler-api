# 🧪 Comandos de Teste - Windows PowerShell

## 🔧 URLs Base

- Health: http://localhost:8080/api/health
- Actuator: http://localhost:8080/api/actuator/health
- PIX: http://localhost:8080/api/payments/checkout

## 📋 Testes PowerShell

### 1. Health Check

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/health"
```

### 2. Info da Aplicação

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/info"
```

### 3. Criar PIX (método alternativo)

```powershell
$body = @{
    amount = 1000
    description = "Teste PIX Local"
    payer = @{
        name = "Teste Tyler"
        email = "teste@tyler.com"
        document = "12345678901"
    }
} | ConvertTo-Json -Depth 3

Invoke-RestMethod -Uri "http://localhost:8080/api/payments/checkout" -Method POST -ContentType "application/json" -Body $body
```

## 🌐 Teste no Navegador

Abra: http://localhost:8080/api/health

## 🐛 Debug

Se der erro 400, verifique logs da aplicação no terminal onde rodou `mvn spring-boot:run`

## 📞 Teste com cURL (se tiver instalado)

```bash
curl -X POST http://localhost:8080/api/payments/checkout \
  -H "Content-Type: application/json" \
  -d "{\"amount\": 1000, \"description\": \"Teste\", \"payer\": {\"name\": \"Teste\", \"email\": \"test@test.com\", \"document\": \"12345678901\"}}"
```
