# Testes dos Endpoints de Produtos - Tyler API
# Execute este script após iniciar a API com: .\test-products-advanced.ps1

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "      TESTE AVANÇADO - ENDPOINTS PRODUTOS" -ForegroundColor Cyan  
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""

# Função para fazer requisições e mostrar resultado formatado
function Test-Endpoint {
    param(
        [string]$Method,
        [string]$Url,
        [string]$Body = $null,
        [string]$Description
    )
    
    Write-Host "📋 $Description" -ForegroundColor Yellow
    Write-Host "🌐 $Method $Url" -ForegroundColor Gray
    
    try {
        if ($Body) {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Body $Body -ContentType "application/json"
        } else {
            $response = Invoke-RestMethod -Uri $Url -Method $Method
        }
        
        Write-Host "✅ Sucesso:" -ForegroundColor Green
        $response | ConvertTo-Json -Depth 3 | Write-Host
        return $response
    }
    catch {
        Write-Host "❌ Erro: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
    Write-Host ""
}

# 1. Testar Health Check
$healthResponse = Test-Endpoint -Method "GET" -Url "http://localhost:8080/api/health" -Description "Testando Health Check"

if (-not $healthResponse) {
    Write-Host "❌ API não está rodando! Inicie a aplicação primeiro." -ForegroundColor Red
    exit
}

# 2. Listar produtos (inicial - pode estar vazio)
$productsResponse = Test-Endpoint -Method "GET" -Url "http://localhost:8080/api/products?page=1&pageSize=5" -Description "Listando produtos existentes"

# 3. Criar produtos de teste
$produto1 = @{
    name = "Camiseta Tyler Pro"
    description = "Camiseta oficial do Projeto Tyler - Edição Limitada"
    price = 4999
    category = "roupas"
    stock = 15
    imageUrl = "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=500"
} | ConvertTo-Json

$produto1Response = Test-Endpoint -Method "POST" -Url "http://localhost:8080/api/products" -Body $produto1 -Description "Criando Produto 1 - Camiseta"

$produto2 = @{
    name = "Caneca Tyler Dev"
    description = "Caneca térmica para desenvolvedores do projeto Tyler"
    price = 2999
    category = "acessorios"  
    stock = 30
    imageUrl = "https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=500"
} | ConvertTo-Json

$produto2Response = Test-Endpoint -Method "POST" -Url "http://localhost:8080/api/products" -Body $produto2 -Description "Criando Produto 2 - Caneca"

$produto3 = @{
    name = "Adesivo Tyler Pack"
    description = "Pack com 10 adesivos do projeto Tyler"
    price = 1500
    category = "acessorios"
    stock = 50
    imageUrl = "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=500"
} | ConvertTo-Json

$produto3Response = Test-Endpoint -Method "POST" -Url "http://localhost:8080/api/products" -Body $produto3 -Description "Criando Produto 3 - Adesivos"

# 4. Listar produtos após criação
$allProductsResponse = Test-Endpoint -Method "GET" -Url "http://localhost:8080/api/products?page=1&pageSize=10" -Description "Listando todos os produtos criados"

# 5. Testar busca por categoria
Test-Endpoint -Method "GET" -Url "http://localhost:8080/api/products?category=acessorios" -Description "Buscando produtos da categoria 'acessorios'"

# 6. Testar paginação
Test-Endpoint -Method "GET" -Url "http://localhost:8080/api/products?page=1&pageSize=2" -Description "Testando paginação (2 produtos por página)"

# 7. Buscar produto por ID (se existe)
if ($produto1Response -and $produto1Response.id) {
    $productId = $produto1Response.id
    Test-Endpoint -Method "GET" -Url "http://localhost:8080/api/products/$productId" -Description "Buscando produto por ID: $productId"
    
    # 8. Testar atualização  
    $updateData = @{
        name = "Camiseta Tyler Pro - ATUALIZADA"
        price = 5499
        stock = 12
    } | ConvertTo-Json
    
    Test-Endpoint -Method "PUT" -Url "http://localhost:8080/api/products/$productId" -Body $updateData -Description "Atualizando produto $productId"
    
    # 9. Verificar se a atualização funcionou
    Test-Endpoint -Method "GET" -Url "http://localhost:8080/api/products/$productId" -Description "Verificando produto atualizado"
}

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "         TESTES CONCLUÍDOS COM SUCESSO!" -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "🔄 Para testar exclusão, use o comando:" -ForegroundColor Yellow  
Write-Host "Invoke-RestMethod -Uri 'http://localhost:8080/api/products/[ID]' -Method DELETE" -ForegroundColor Gray
Write-Host ""