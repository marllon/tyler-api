# 🔑 GUIA COMPLETO - CONFIGURAÇÃO CONTA PAGARME

## 📋 CHECKLIST DE CONFIGURAÇÃO

### ✅ **PASSO 1: Criar Conta Pagarme**

- [ ] Acesse: https://dashboard.pagar.me/signup
- [ ] Cadastre-se com seus dados pessoais/empresariais
- [ ] Confirme email de verificação
- [ ] Complete processo de verificação (documentos)
- [ ] Aguarde aprovação da conta

### ✅ **PASSO 2: Obter API Keys**

- [ ] Acesse Dashboard → Configurações → API Keys
- [ ] Copie Secret Key Test: `sk_test_...`
- [ ] Copie Secret Key Live: `sk_live_...` (para produção)
- [ ] Guarde as keys em local seguro

### ✅ **PASSO 3: Configurar Aplicação**

#### **3.1 Environment Variables (Escolha uma opção):**

**Opção A: PowerShell (Recomendado)**

```powershell
$env:PAGARME_API_KEY="sk_test_SUA_KEY_AQUI"
```

**Opção B: CMD**

```cmd
set PAGARME_API_KEY=sk_test_SUA_KEY_AQUI
```

**Opção C: IntelliJ IDEA**

```
Run → Edit Configurations → Environment Variables
Name: PAGARME_API_KEY
Value: sk_test_SUA_KEY_AQUI
```

#### **3.2 Verificar Configuração:**

```bash
# No terminal/PowerShell
echo $env:PAGARME_API_KEY  # PowerShell
echo %PAGARME_API_KEY%     # CMD
```

### ✅ **PASSO 4: Testar Integração**

#### **4.1 Executar Aplicação:**

```bash
cd "d:\Projetos\Tyler\backend"
mvn spring-boot:run
```

#### **4.2 Testar Health Check:**

```bash
curl http://localhost:8080/api/health
```

#### **4.3 Testar PIX Checkout:**

```bash
curl -X POST http://localhost:8080/api/payments/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000,
    "description": "Teste PIX",
    "payer": {
      "name": "João Silva",
      "email": "joao@teste.com",
      "document": "12345678901"
    }
  }'
```

## 🏦 **CONFIGURAÇÕES BANCÁRIAS**

### **Para Receber Pagamentos:**

1. **Dashboard** → **Conta** → **Dados Bancários**
2. **Cadastre** sua conta bancária
3. **Configure** cronograma de repasses
4. **Defina** taxas de antecipação (se necessário)

### **Taxas Pagarme PIX:**

- **PIX**: 0,99% por transação
- **Prazo**: D+1 (próximo dia útil)
- **Mínimo**: R$ 0,50 por transação

## 🔔 **CONFIGURAR WEBHOOKS (Opcional)**

### **Para Notificações Automáticas:**

1. **Dashboard** → **Configurações** → **Webhooks**
2. **URL Endpoint:** `https://sua-api.com/api/payments/webhook`
3. **Eventos:** Selecione `transaction.paid`, `transaction.failed`
4. **Teste** o webhook

## 🧪 **AMBIENTE DE TESTE**

### **Cartões de Teste PIX:**

```json
{
  "customer": {
    "name": "João Silva",
    "email": "joao@teste.com",
    "document": "12345678901"
  }
}
```

### **Status de Teste:**

- ✅ **Sucesso:** Qualquer valor terminado em 00 (ex: R$ 10,00)
- ❌ **Falha:** Qualquer valor terminado em 99 (ex: R$ 10,99)
- ⏳ **Pendente:** Outros valores

## 🚀 **MIGRAR PARA PRODUÇÃO**

### **Quando sua aplicação estiver pronta:**

1. **Complete** verificação da conta
2. **Substitua** `sk_test_...` por `sk_live_...`
3. **Configure** webhook de produção
4. **Teste** em ambiente real
5. **Monitore** transações no dashboard

## ❌ **TROUBLESHOOTING**

### **Erro: "Authorization has been denied"**

```
Solução:
- Verifique se API key está correta
- Confirme environment variable configurada
- Teste com curl: echo $env:PAGARME_API_KEY
```

### **Erro: "Invalid API key"**

```
Solução:
- Copie nova API key do dashboard
- Verifique se é Secret Key (não Public Key)
- Confirme ambiente (test vs live)
```

### **Erro: "Account not verified"**

```
Solução:
- Complete verificação da conta
- Envie documentos pendentes
- Aguarde aprovação (1-3 dias)
```

## 📞 **SUPORTE**

### **Canais de Suporte Pagarme:**

- 📧 **Email:** suporte@pagar.me
- 💬 **Chat:** Dashboard → Suporte
- 📚 **Docs:** https://docs.pagar.me
- 🐛 **GitHub:** https://github.com/pagarme

### **Horário de Atendimento:**

- **Segunda a Sexta:** 9h às 18h
- **Sábado:** 9h às 15h
- **Domingo:** Apenas suporte técnico

---

**🎯 PRÓXIMOS PASSOS:**

1. Configure sua API key
2. Execute a aplicação Tyler
3. Teste o endpoint de checkout
4. Monitore no dashboard Pagarme
5. Complete verificação para produção
