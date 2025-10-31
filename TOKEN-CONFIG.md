# 🔐 Configuração de Tokens Simplificada

## Arquitetura

### ✅ **Abordagem Atual (Simplificada)**

```
TokenConfiguration.kt
    ↓
@Bean("pagbankToken")
    ↓
@Value("${PAGBANK_TOKEN}")
    ↓
┌─── Local ────┐         ┌─── Produção ────┐
│              │         │                 │
│ YAML define  │         │ Cloud Run       │
│ PAGBANK_TOKEN│         │ injeta via      │
│ hardcoded    │         │ Secret Manager  │
└──────────────┘         └─────────────────┘
```

## Configuração por Ambiente

### 🛠️ Local (Profile: local)

**application-local.yml**

```yaml
PAGBANK_TOKEN: "sandbox_test_12345"
```

### 🚀 Produção (Profile: production)

**application-production.yml**

```yaml
PAGBANK_TOKEN: ${PAGBANK_TOKEN}
```

**Cloud Run Service**

```yaml
env:
  - name: PAGBANK_TOKEN
    valueFrom:
      secretKeyRef:
        name: pagbank-token
        key: latest
```

## Vantagens

✅ **Um único Bean** - Simplicidade máxima  
✅ **Sempre usa PAGBANK_TOKEN** - Consistência  
✅ **Fonte diferente por ambiente** - Flexibilidade  
✅ **Sem dependência Secret Manager** - Menos complexidade  
✅ **Cloud Run gerencia secrets** - Padrão GCP

## Deploy Commands

### Local

```bash
mvn spring-boot:run -Dspring.profiles.active=local
```

### Produção (Cloud Run)

```bash
gcloud run deploy tyler-api \
  --source . \
  --set-env-vars="SPRING_PROFILES_ACTIVE=production" \
  --update-secrets="PAGBANK_TOKEN=pagbank-token:latest"
```

## Tokens

- **Local**: `sandbox_test_12345` (desenvolvimento)
- **Produção**: Token real do PagBank via Secret Manager GCP
