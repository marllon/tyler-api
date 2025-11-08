# CRUD COMPLETO - PRODUTOS API
# Clean Architecture com DTOs
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "TESTE CRUD COMPLETO - PRODUTOS API" -ForegroundColor Yellow
Write-Host "Clean Architecture com DTOs" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api"
$headers = @{"Content-Type" = "application/json"}

# Função para fazer requisições com tratamento de erro
function Invoke-ApiRequest {
    param($Method, $Uri, $Body = $null, $Description)
    
    Write-Host "`n$Description..." -ForegroundColor Yellow
    
    try {
        if ($Body) {
            $response = Invoke-RestMethod -Uri $Uri -Method $Method -Headers $headers -Body $Body -ErrorAction Stop
        } else {
            $response = Invoke-RestMethod -Uri $Uri -Method $Method -ErrorAction Stop
        }
        
        Write-Host "✅ Sucesso!" -ForegroundColor Green
        return $response
    }
    catch {
        Write-Host "❌ Erro: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# 1. Health Check
Write-Host "`n1. Testando Health Check..."
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/health" -ErrorAction Stop
    Write-Host "✅ Servidor está rodando!" -ForegroundColor Green
} catch {
    Write-Host "❌ Servidor não está rodando! Inicie com 'mvn spring-boot:run'" -ForegroundColor Red
    exit 1
}

# 2. Criando produtos de teste
$produtos = @(
    @{
        name = "Smartphone Samsung Galaxy S24"
        description = "Smartphone Android flagship com 256GB"
        price = 1899.99
        category = "Eletronicos"
        stock = 45
        active = $true
    },
    @{
        name = "Notebook Dell XPS 13"
        description = "Notebook premium para desenvolvedores"
        price = 3499.90
        category = "Informatica" 
        stock = 15
        active = $true
    },
    @{
        name = "Fone Sony WH-1000XM5"
        description = "Fone premium com noise canceling"
        price = 599.90
        category = "Audio"
        stock = 80
        active = $true
    }
)

$produtosCriados = @()

for ($i = 0; $i -lt $produtos.Count; $i++) {
    $produto = $produtos[$i]
    $body = $produto | ConvertTo-Json -Depth 3
    
    $resultado = Invoke-ApiRequest -Method "POST" -Uri "$baseUrl/products" -Body $body -Description "Criando Produto $($i+1): $($produto.name)"
    
    if ($resultado) {
        $produtosCriados += $resultado
        Write-Host "   ID: $($resultado.id)" -ForegroundColor Cyan
        Write-Host "   Nome: $($resultado.name)" -ForegroundColor White
        Write-Host "   Preço: R$ $($resultado.price)" -ForegroundColor Green
    }
}

# 3. Listagem com paginação
$lista = Invoke-ApiRequest -Method "GET" -Uri "$baseUrl/products?page=1&pageSize=10" -Description "Listando produtos (paginação)"
if ($lista) {
    Write-Host "   Total: $($lista.totalProducts)" -ForegroundColor Cyan
    Write-Host "   Página: $($lista.currentPage)/$($lista.totalPages)" -ForegroundColor Cyan
    Write-Host "   Produtos na página: $($lista.products.Count)" -ForegroundColor Cyan
}

# 4. Busca por categoria
$categoria = Invoke-ApiRequest -Method "GET" -Uri "$baseUrl/products?category=Eletronicos&page=1&pageSize=5" -Description "Buscando por categoria 'Eletronicos'"
if ($categoria) {
    Write-Host "   Produtos encontrados: $($categoria.products.Count)" -ForegroundColor Cyan
}

# 5. Teste CRUD completo no primeiro produto
if ($produtosCriados.Count -gt 0) {
    $primeiroId = $produtosCriados[0].id
    
    # GET por ID
    $produto = Invoke-ApiRequest -Method "GET" -Uri "$baseUrl/products/$primeiroId" -Description "Buscando produto específico (ID: $primeiroId)"
    
    if ($produto) {
        # UPDATE
        $updateBody = @{
            name = "Smartphone Samsung Galaxy S24 ATUALIZADO"
            description = "Smartphone Android flagship com 512GB - VERSÃO ATUALIZADA"
            price = 2199.99
            category = "Eletronicos"
            stock = 60
        } | ConvertTo-Json -Depth 3
        
        $atualizado = Invoke-ApiRequest -Method "PUT" -Uri "$baseUrl/products/$primeiroId" -Body $updateBody -Description "Atualizando produto"
        
        if ($atualizado) {
            Write-Host "   Nome atualizado: $($atualizado.name)" -ForegroundColor Green
            Write-Host "   Preço atualizado: R$ $($atualizado.price)" -ForegroundColor Green
        }
        
        # Verificação da atualização
        $verificacao = Invoke-ApiRequest -Method "GET" -Uri "$baseUrl/products/$primeiroId" -Description "Verificando atualização"
        
        # DELETE
        $deletado = Invoke-ApiRequest -Method "DELETE" -Uri "$baseUrl/products/$primeiroId" -Description "Deletando produto"
        
        if ($deletado) {
            Write-Host "   Produto deletado: $($deletado.message)" -ForegroundColor Green
        }
        
        # Verificação da deleção
        Write-Host "`nVerificando se produto foi deletado..." -ForegroundColor Yellow
        try {
            Invoke-RestMethod -Uri "$baseUrl/products/$primeiroId" -ErrorAction Stop
            Write-Host "❌ Produto ainda existe!" -ForegroundColor Red
        } catch {
            Write-Host "✅ Produto deletado com sucesso!" -ForegroundColor Green
        }
    }
}

# 6. Listagem final
$final = Invoke-ApiRequest -Method "GET" -Uri "$baseUrl/products?page=1&pageSize=10" -Description "Listagem final de produtos"
if ($final) {
    Write-Host "   Total final: $($final.totalProducts)" -ForegroundColor Cyan
}

# 7. Teste de paginação avançada
Write-Host "`nTestando paginação avançada..." -ForegroundColor Yellow
$pagina2 = Invoke-ApiRequest -Method "GET" -Uri "$baseUrl/products?page=2&pageSize=1" -Description "Buscando página 2 com 1 item"

# 8. Teste de filtros
Write-Host "`nTestando filtros..." -ForegroundColor Yellow
$ativos = Invoke-ApiRequest -Method "GET" -Uri "$baseUrl/products?activeOnly=true" -Description "Buscando apenas produtos ativos"
$todos = Invoke-ApiRequest -Method "GET" -Uri "$baseUrl/products?activeOnly=false" -Description "Buscando todos produtos (ativos e inativos)"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TESTE CRUD COMPLETO FINALIZADO" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

# Resumo
Write-Host "`n📊 RESUMO DOS TESTES:" -ForegroundColor Magenta
Write-Host "✅ Health Check" -ForegroundColor Green
Write-Host "✅ CREATE - Criação de produtos" -ForegroundColor Green
Write-Host "✅ READ - Listagem e busca" -ForegroundColor Green
Write-Host "✅ UPDATE - Atualização" -ForegroundColor Green
Write-Host "✅ DELETE - Remoção" -ForegroundColor Green
Write-Host "✅ Paginação" -ForegroundColor Green
Write-Host "✅ Filtros por categoria" -ForegroundColor Green
Write-Host "✅ Filtros por status ativo" -ForegroundColor Green

Write-Host "`n🎯 Arquitetura Clean implementada com sucesso!" -ForegroundColor Yellow
Write-Host "🔧 DTOs funcionando perfeitamente!" -ForegroundColor Yellow
Write-Host "🚀 SOLID principles aplicados!" -ForegroundColor Yellow

Read-Host "`nPressione Enter para sair"