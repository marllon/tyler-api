# 📋 **API de Produtos - Endpoints Disponíveis**

## **🔧 Configuração Base**

- **URL Base**: `http://localhost:8080/api/products`
- **Porta padrão**: 8080 (configurável)

---

## **📋 LISTA DE ENDPOINTS**

### **1. 📋 Listar Produtos (Paginação Tradicional)**

- **Método**: `GET /api/products`
- **Descrição**: Lista produtos com paginação tradicional. Para compatibilidade com sistemas legados.
- **Uso**: Interface simples, compatibilidade com frontends antigos
- **Parâmetros**: page, pageSize, activeOnly, category

```bash
curl -X GET "http://localhost:8080/api/products?page=1&pageSize=10&activeOnly=true&category=Eletronicos"
```

### **2. 🚀 Listar Produtos (Cursor Pagination) - RECOMENDADO**

- **Método**: `GET /api/products/paginated`
- **Descrição**: ✅ Versão otimizada para NoSQL com cursor-based pagination. Melhor performance.
- **Uso**: **Preferir este para novas implementações** - Performance superior no Firestore
- **Parâmetros**: limit, cursor, direction, sortBy, sortDirection, activeOnly, category

```bash
# Primeira página
curl -X GET "http://localhost:8080/api/products/paginated?limit=20&sortBy=CREATED_AT&sortDirection=DESC&activeOnly=true"

# Próxima página (usar cursor retornado)
curl -X GET "http://localhost:8080/api/products/paginated?limit=20&cursor=product_id_123&direction=NEXT"
```

### **3. 🔍 Buscar Produto por ID**

- **Método**: `GET /api/products/{id}`
- **Descrição**: Retorna um produto específico pelo ID único
- **Uso**: Visualizar detalhes de produto, páginas de produto individual

```bash
curl -X GET "http://localhost:8080/api/products/550e8400-e29b-41d4-a716-446655440000"
```

### **4. ➕ Criar Produto (com Imagens Opcionais) - UNIFICADO**

- **Método**: `POST /api/products`
- **Content-Type**: `multipart/form-data`
- **Descrição**: ✅ Endpoint unificado para criar produto com ou sem imagens
- **Uso**: **Endpoint principal** para criação - suporta até 10 imagens simultaneamente

```bash
# Criar produto sem imagens
curl -X POST "http://localhost:8080/api/products" \
  -F "productData={
    \"name\": \"Smartphone XYZ\",
    \"description\": \"Smartphone moderno\",
    \"price\": 899.99,
    \"category\": \"Eletronicos\",
    \"stock\": 50,
    \"active\": true,
    \"brand\": \"TechBrand\",
    \"model\": \"XYZ-2024\"
  }"

# Criar produto com imagens
curl -X POST "http://localhost:8080/api/products" \
  -F "productData={
    \"name\": \"Smartphone ABC\",
    \"description\": \"Smartphone premium\",
    \"price\": 1299.99,
    \"category\": \"Eletronicos\",
    \"stock\": 25,
    \"active\": true
  }" \
  -F "images=@image1.jpg" \
  -F "images=@image2.png"
```

### **5. ✏️ Atualizar Produto**

- **Método**: `PUT /api/products/{id}`
- **Content-Type**: `application/json`
- **Descrição**: Atualiza dados de um produto existente (não inclui imagens)
- **Uso**: Editar informações do produto, alterar preço/estoque

```bash
curl -X PUT "http://localhost:8080/api/products/550e8400-e29b-41d4-a716-446655440000" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Smartphone XYZ Pro",
    "price": 999.99,
    "stock": 30,
    "description": "Versão atualizada"
  }'
```

### **6. 🗑️ Deletar Produto**

- **Método**: `DELETE /api/products/{id}`
- **Descrição**: Remove um produto permanentemente do sistema
- **Uso**: Remover produtos descontinuados ou com erro

```bash
curl -X DELETE "http://localhost:8080/api/products/550e8400-e29b-41d4-a716-446655440000"
```

### **7. 📸 Upload de Imagem para Produto Existente**

- **Método**: `POST /api/products/{id}/images`
- **Content-Type**: `multipart/form-data`
- **Descrição**: Adiciona uma imagem a um produto já criado
- **Uso**: Adicionar imagens posteriormente, definir imagem primária

```bash
curl -X POST "http://localhost:8080/api/products/550e8400-e29b-41d4-a716-446655440000/images" \
  -F "file=@nova-imagem.jpg" \
  -F "isPrimary=true"
```

### **8. 🗑️ Remover Imagem do Produto**

- **Método**: `DELETE /api/products/{id}/images/{imageId}`
- **Descrição**: Remove uma imagem específica de um produto
- **Uso**: Limpar imagens desnecessárias, corrigir uploads errados

```bash
curl -X DELETE "http://localhost:8080/api/products/550e8400-e29b-41d4-a716-446655440000/images/image_abc123"
```

---

## **🎯 RECOMENDAÇÕES DE USO**

### **✅ Para Novas Implementações:**

1. **Use `/paginated`** para listagem (melhor performance)
2. **Use `POST /api/products`** para criação (unificado, menos requests)
3. **Upload de imagens junto com criação** quando possível

### **🔄 Para Sistemas Legados:**

1. **Use `GET /api/products`** se já integrado
2. **Migre gradualmente** para cursor pagination

### **⚡ Performance Tips:**

- **Cursor pagination**: O(1) vs O(n) da paginação tradicional
- **Upload unificado**: Reduz número de requests HTTP
- **Filtros específicos**: Use `category` e `activeOnly` para otimizar

---

## **📝 Exemplos de Respostas**

### **Listagem Paginada:**

```json
{
  "products": [
    {
      "id": "product_123",
      "name": "Smartphone XYZ",
      "description": "Smartphone moderno",
      "price": 899.99,
      "category": "Eletronicos",
      "stock": 50,
      "active": true,
      "images": [
        {
          "id": "img_001",
          "url": "https://storage.googleapis.com/...",
          "isPrimary": true
        }
      ],
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ],
  "pageSize": 20,
  "hasNext": true,
  "nextCursor": "product_124",
  "hasPrevious": false
}
```

### **Produto Individual:**

```json
{
  "id": "product_123",
  "name": "Smartphone XYZ",
  "description": "Smartphone moderno com tecnologia avançada",
  "price": 899.99,
  "category": "Eletronicos",
  "stock": 50,
  "active": true,
  "brand": "TechBrand",
  "model": "XYZ-2024",
  "weight": "180g",
  "dimensions": "15x7x0.8cm",
  "color": "Preto",
  "warranty": "12 meses",
  "tags": ["smartphone", "tecnologia", "5g"],
  "images": [
    {
      "id": "img_001",
      "url": "https://storage.googleapis.com/tyler-bucket/products/img_001.jpg",
      "isPrimary": true,
      "uploadedAt": "2024-01-15T10:30:00Z"
    }
  ],
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

### **Criação com Sucesso:**

```json
{
  "id": "product_new_123",
  "name": "Produto Criado",
  "message": "Produto criado com sucesso",
  "imagesUploaded": 2
}
```

---

## **⚠️ Códigos de Erro Comuns**

- **400 Bad Request**: Dados inválidos, arquivo muito grande
- **404 Not Found**: Produto não encontrado
- **413 Payload Too Large**: Arquivo de imagem muito grande (>10MB)
- **500 Internal Server Error**: Erro interno do servidor

---

## **🔧 Configurações Importantes**

- **Limite de imagens por produto**: 10 arquivos
- **Tamanho máximo por imagem**: 10MB
- **Formatos aceitos**: JPG, PNG, WEBP
- **Timeout de upload**: 30 segundos
- **Limite de paginação**: 100 itens por página
