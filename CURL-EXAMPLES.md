# 📸 **Exemplos de CURL - POST Produto com Imagens**

## **🚀 Endpoint Unificado**

- **URL**: `POST /api/products`
- **Content-Type**: `multipart/form-data`
- **Parâmetros**: `productData` (JSON) + `images` (arquivos)

---

## **📋 Exemplos Práticos**

### **1. 🛍️ Produto com 1 Imagem**

```bash
curl -X POST "http://localhost:8080/api/products" \
  -F "productData={
    \"name\": \"Smartphone Galaxy Pro\",
    \"description\": \"Smartphone premium com câmera de 108MP\",
    \"price\": 1299.99,
    \"category\": \"Eletronicos\",
    \"stock\": 25,
    \"active\": true,
    \"brand\": \"Samsung\",
    \"model\": \"Galaxy Pro 2024\",
    \"weight\": 180.5,
    \"dimensions\": \"15.7x7.1x0.8cm\",
    \"color\": \"Azul\",
    \"warranty\": 24,
    \"tags\": [\"smartphone\", \"5g\", \"camera\", \"premium\"],
    \"primaryImageIndex\": 0
  }" \
  -F "images=@smartphone-front.jpg"
```

### **2. 🛍️ Produto com Múltiplas Imagens**

```bash
curl -X POST "http://localhost:8080/api/products" \
  -F "productData={
    \"name\": \"Notebook Gaming Ultimate\",
    \"description\": \"Notebook gamer com RTX 4070 e Intel i7\",
    \"price\": 4999.99,
    \"category\": \"Informatica\",
    \"stock\": 10,
    \"active\": true,
    \"brand\": \"ASUS\",
    \"model\": \"ROG Strix G15\",
    \"weight\": 2500.0,
    \"dimensions\": \"35.4x25.9x2.7cm\",
    \"color\": \"Preto\",
    \"warranty\": 12,
    \"tags\": [\"notebook\", \"gaming\", \"rtx\", \"i7\"],
    \"primaryImageIndex\": 0
  }" \
  -F "images=@notebook-front.jpg" \
  -F "images=@notebook-side.jpg" \
  -F "images=@notebook-keyboard.jpg" \
  -F "images=@notebook-ports.jpg"
```

### **3. 🛍️ Produto SEM Imagens**

```bash
curl -X POST "http://localhost:8080/api/products" \
  -F "productData={
    \"name\": \"Mouse Gamer RGB\",
    \"description\": \"Mouse óptico com 12000 DPI\",
    \"price\": 159.99,
    \"category\": \"Acessorios\",
    \"stock\": 50,
    \"active\": true,
    \"brand\": \"Logitech\",
    \"model\": \"G502 Hero\",
    \"weight\": 121.0,
    \"color\": \"Preto\",
    \"warranty\": 24,
    \"tags\": [\"mouse\", \"gaming\", \"rgb\", \"dpi\"]
  }"
```

### **4. 🛍️ Produto Mínimo (Campos Obrigatórios)**

```bash
curl -X POST "http://localhost:8080/api/products" \
  -F "productData={
    \"name\": \"Produto Teste\",
    \"description\": \"Descrição básica do produto\",
    \"price\": 99.99,
    \"category\": \"Teste\",
    \"stock\": 5
  }" \
  -F "images=@test-image.png"
```

---

## **🔐 Com Autenticação JWT**

### **Usando Token Firebase:**

```bash
curl -X POST "http://localhost:8080/api/products" \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -F "productData={
    \"name\": \"Produto Autenticado\",
    \"description\": \"Produto criado com token JWT\",
    \"price\": 199.99,
    \"category\": \"Seguro\",
    \"stock\": 15,
    \"active\": true
  }" \
  -F "images=@secure-product.jpg"
```

---

## **📱 Usando PowerShell (Windows)**

### **Exemplo com Invoke-RestMethod:**

```powershell
$uri = "http://localhost:8080/api/products"
$productData = @{
    name = "Produto PowerShell"
    description = "Criado via PowerShell"
    price = 299.99
    category = "Windows"
    stock = 8
    active = $true
} | ConvertTo-Json

$form = @{
    productData = $productData
    images = Get-Item "C:\caminho\para\imagem.jpg"
}

Invoke-RestMethod -Uri $uri -Method Post -Form $form
```

---

## **📋 Estrutura do JSON productData**

### **✅ Campos Obrigatórios:**

```json
{
  "name": "string", // Nome do produto
  "description": "string", // Descrição
  "price": 99.99, // Preço (double)
  "category": "string", // Categoria
  "stock": 10 // Estoque (int)
}
```

### **🔧 Campos Opcionais:**

```json
{
  "active": true, // Ativo (default: true)
  "brand": "string", // Marca
  "model": "string", // Modelo
  "weight": 100.5, // Peso em gramas
  "dimensions": "10x5x2cm", // Dimensões
  "color": "string", // Cor
  "warranty": 12, // Garantia em meses
  "tags": ["tag1", "tag2"], // Lista de tags
  "primaryImageIndex": 0 // Índice da imagem principal (0-9)
}
```

---

## **📸 Formatos de Imagem Aceitos**

- ✅ **JPG/JPEG**: `image.jpg`
- ✅ **PNG**: `image.png`
- ✅ **WEBP**: `image.webp`
- ❌ **Limite**: 10MB por imagem
- ❌ **Máximo**: 10 imagens por produto

---

## **✅ Resposta de Sucesso (201 Created)**

### **JSON Retornado:**

```json
{
  "id": "product_abc123",
  "name": "Smartphone Galaxy Pro",
  "description": "Smartphone premium com câmera de 108MP",
  "price": 1299.99,
  "category": "Eletronicos",
  "stock": 25,
  "active": true,
  "brand": "Samsung",
  "model": "Galaxy Pro 2024",
  "weight": 180.5,
  "dimensions": "15.7x7.1x0.8cm",
  "color": "Azul",
  "warranty": 24,
  "tags": ["smartphone", "5g", "camera", "premium"],
  "images": [
    {
      "id": "img_def456",
      "url": "https://storage.googleapis.com/tyler-bucket/products/img_def456.jpg",
      "filename": "smartphone-front.jpg",
      "contentType": "image/jpeg",
      "size": 2048576,
      "isPrimary": true
    }
  ],
  "primaryImageUrl": "https://storage.googleapis.com/tyler-bucket/products/img_def456.jpg",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

---

## **❌ Erros Comuns**

### **400 Bad Request:**

```bash
# JSON inválido no productData
curl -X POST "http://localhost:8080/api/products" \
  -F "productData={name: sem-aspas}" \  # ❌ JSON inválido
  -F "images=@image.jpg"
```

### **413 Payload Too Large:**

```bash
# Arquivo muito grande (>10MB)
curl -X POST "http://localhost:8080/api/products" \
  -F "productData={\"name\":\"test\",\"price\":99}" \
  -F "images=@huge-image-15mb.jpg"  # ❌ Muito grande
```

### **415 Unsupported Media Type:**

```bash
# Formato não suportado
curl -X POST "http://localhost:8080/api/products" \
  -F "productData={\"name\":\"test\",\"price\":99}" \
  -F "images=@document.pdf"  # ❌ PDF não aceito
```

---

## **🧪 Teste Rápido**

### **1. Criar arquivo de teste:**

```bash
# Criar JSON em arquivo separado
echo '{
  "name": "Produto Teste cURL",
  "description": "Testando API via cURL",
  "price": 123.45,
  "category": "Teste",
  "stock": 1,
  "active": true
}' > product-data.json

# Usar arquivo no curl
curl -X POST "http://localhost:8080/api/products" \
  -F "productData=<product-data.json" \
  -F "images=@test-image.jpg"
```

### **2. Verificar se API está rodando:**

```bash
curl -X GET "http://localhost:8080/api/health"
# Resposta esperada: {"status":"healthy",...}
```

---

**🎯 Escolha o exemplo que melhor se adapta ao seu caso de uso!**
