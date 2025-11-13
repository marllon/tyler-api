# 📸 **API de Upload de Imagens - Documentação**

## 🔗 **Endpoints Disponíveis**

### **1. Upload de Imagem**

```http
POST /api/products/{productId}/images
Content-Type: multipart/form-data

Parâmetros:
- file: Arquivo de imagem (JPG, PNG, WEBP)
- isPrimary: boolean (opcional, default: false)
```

**Exemplo cURL:**

```bash
curl -X POST "http://localhost:8080/api/products/123/images" \
  -F "file=@imagem.jpg" \
  -F "isPrimary=true"
```

**Resposta:**

```json
{
  "id": "img-123",
  "url": "https://storage.googleapis.com/tyler-product-images/...",
  "filename": "produto-123-20241111-143022-abc.jpg",
  "contentType": "image/jpeg",
  "size": 245760,
  "isPrimary": true
}
```

### **2. Remover Imagem**

```http
DELETE /api/products/{productId}/images/{imageId}
```

**Exemplo cURL:**

```bash
curl -X DELETE "http://localhost:8080/api/products/123/images/img-123"
```

## 🔧 **Configuração Necessária**

### **Service Account**

- Arquivo: `src/main/resources/tyler-storage-credentials.json`
- Permissões: Storage Object Admin, Storage Legacy Bucket Reader

### **Bucket GCS**

- Nome: `tyler-product-images`
- Projeto: `tyler-dev-c2420`
- Região: `southamerica-east1`
- Acesso: URLs assinadas (7 dias)

### **Variáveis de Ambiente**

```yaml
app:
  gcp:
    project-id: tyler-dev-c2420
    bucket-name: tyler-product-images
    storage-credentials-path: tyler-storage-credentials.json
```

## ✅ **Funcionalidades Implementadas**

- ✅ Upload de múltiplas imagens por produto
- ✅ Identificação de imagem primária
- ✅ URLs assinadas para segurança
- ✅ Validação de tipo de arquivo
- ✅ Nomes únicos com timestamp
- ✅ Cleanup automático em caso de erro
- ✅ Service Account separado do Firebase
- ✅ Integração com repository genérico

## 🧪 **Como Testar**

1. Coloque o arquivo JSON da service account
2. Execute: `create-gcp-bucket.bat`
3. Execute: `start-server.bat`
4. Execute: `test-upload-complete.bat`
