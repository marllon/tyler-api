## 🧪 **TESTE RÁPIDO DA APLICAÇÃO**

### ✅ **1. Health Check (FUNCIONANDO)**

```
URL: http://localhost:8080/api/health
Status: ✅ OK
```

### 🔍 **2. Diagnóstico do Erro 400**

O erro 400 pode ser por:

1. **Token PagBank inválido**
2. **Payload malformado**
3. **API PagBank indisponível**

### 🛠️ **Debug Steps:**

#### Passo 1: Verificar Logs

Olhe no terminal onde rodou `mvn spring-boot:run` e procure por:

- `❌ Erro ao criar doação:`
- `🛠️ Configurando PagBank`
- Qualquer stack trace

#### Passo 2: Testar Navegador

Abra: http://localhost:8080/api/health
**Esperado**: `{"status":"healthy","message":"Tyler API Spring Boot está funcionando perfeitamente!"}`

#### Passo 3: Verificar Token

No YAML local você tem: `PAGBANK_TOKEN: "sandbox_test_12345"`
Este é um **token fake** para desenvolvimento.

**🔧 Para teste real, você precisa:**

1. Criar conta PagBank
2. Gerar token sandbox real
3. Atualizar `application-local.yml`

### 📞 **Contato API PagBank**

```bash
# Teste básico da API PagBank
curl -I https://sandbox.api.pagseguro.com/orders
```

### 🎯 **Próximos Passos**

1. **Verifique os logs** da aplicação
2. **Confirme o token** PagBank real
3. **Teste conectividade** com PagBank API

**A aplicação está rodando corretamente!** O problema é específico do PIX/PagBank.
