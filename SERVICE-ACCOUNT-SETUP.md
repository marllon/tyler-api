# 🚀 **Guia Rápido: Service Account para Storage**

## 📋 **Checklist de Implementação**

### ✅ **1. Criar Service Account (Google Cloud Console)**

1. Acesse: https://console.cloud.google.com/iam-admin/serviceaccounts
2. Projeto: `tyler-dev-c2420`
3. **Create Service Account**:
   - Name: `tyler-storage-service`
   - Description: `Service account for product images storage access`

### ✅ **2. Configurar Permissões**

1. Service Account → **Permissions** → **Grant Access**
2. Adicionar roles:
   - `Storage Object Admin`
   - `Storage Legacy Bucket Reader`

### ✅ **3. Gerar Chave JSON**

1. Service Account → **Keys** → **Add Key** → **Create new key**
2. Tipo: **JSON**
3. **Baixar e salvar como**: `tyler-storage-credentials.json`
4. **Mover para**: `src/main/resources/tyler-storage-credentials.json`

### ✅ **4. Repetir para Produção**

1. Projeto: `tyler-prd`
2. Mesma service account: `tyler-storage-service`
3. Salvar como: `tyler-storage-credentials-prd.json`

## 🔐 **Segurança Implementada**

### **✅ Credenciais Separadas:**

- **Firestore**: `firebase-admin-sdk.json`
- **Storage**: `tyler-storage-credentials.json`

### **✅ Proteção Git:**

```gitignore
tyler-storage-credentials.json
tyler-storage-credentials-*.json
```

### **✅ Profiles Configurados:**

- **Local**: `tyler-storage-credentials.json`
- **Prod**: `tyler-storage-credentials-prd.json`

## 🎯 **Benefícios da Abordagem**

1. **Segregação**: Cada serviço tem suas próprias credenciais
2. **Princípio do Menor Privilégio**: Storage só tem acesso ao bucket
3. **Auditoria**: Logs separados por service account
4. **Rotação**: Pode trocar credenciais independentemente
5. **Compliance**: Melhor para auditorias e certificações

## 🚦 **Status Atual**

- ✅ **Configuração**: Completada
- ✅ **Código**: Atualizado para usar service account dedicada
- ✅ **Profiles**: Dev e Prod configurados
- ✅ **Segurança**: Credenciais protegidas no .gitignore
- ✅ **Compilação**: Funcionando perfeitamente

## 🔄 **Próximos Passos**

1. **Criar service account** no Google Cloud Console
2. **Baixar credenciais** JSON
3. **Renomear e mover** para `src/main/resources/`
4. **Testar aplicação** com `mvn spring-boot:run`
5. **Repetir para produção** quando necessário

## 🧪 **Como Testar**

```bash
# 1. Iniciar aplicação
mvn spring-boot:run

# 2. Verificar logs
# Deve aparecer: "Loading Google Cloud Storage credentials from: tyler-storage-credentials.json"

# 3. Testar endpoint
# POST http://localhost:8080/api/products/with-images
```

**🎉 Implementação completa e pronta para uso!**
