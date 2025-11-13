Add-Type -AssemblyName System.Net.Http

$client = New-Object System.Net.Http.HttpClient
$form = New-Object System.Net.Http.MultipartFormDataContent

# Adicionar arquivo
$fileContent = [System.IO.File]::ReadAllBytes("test-image.png")
$fileStream = New-Object System.IO.MemoryStream($fileContent)
$streamContent = New-Object System.Net.Http.StreamContent($fileStream)
$streamContent.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse("image/png")
$form.Add($streamContent, "file", "test-image.png")

# Adicionar parâmetro isPrimary
$isPrimaryContent = New-Object System.Net.Http.StringContent("true")
$form.Add($isPrimaryContent, "isPrimary")

# Fazer a requisição
$url = "http://localhost:8080/api/products/fda06869-3fad-472d-b6d7-a32691ff22da/images"
try {
    $response = $client.PostAsync($url, $form).Result
    $content = $response.Content.ReadAsStringAsync().Result
    Write-Host "Status: $($response.StatusCode)"
    Write-Host "Response: $content"
} catch {
    Write-Host "Error: $($_.Exception.Message)"
} finally {
    $client.Dispose()
    $form.Dispose()
    $streamContent.Dispose()
    $fileStream.Dispose()
}