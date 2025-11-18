# 🎰 Rifas - Exemplos de Curl Completos (Casos de Uso)

## 📋 Configuração Inicial

```bash
# Variáveis de ambiente
export API_URL="http://localhost:8080/api"
export ADMIN_TOKEN="seu_firebase_token_admin_aqui"

# OU para Windows CMD:
set API_URL=http://localhost:8080/api
set ADMIN_TOKEN=seu_firebase_token_admin_aqui

# OU para Windows PowerShell:
$API_URL="http://localhost:8080/api"
$ADMIN_TOKEN="seu_firebase_token_admin_aqui"
```

---

## 🟢 CASO DE USO 1: Usuário Público Lista Rifas Ativas

**Contexto:** Visitante acessa site e quer ver rifas disponíveis para participar.

### 1.1 Listar Todas as Rifas Ativas (Primeira Página)

```bash
curl -X GET "${API_URL}/raffles?page=0&pageSize=20&status=ACTIVE&sortBy=createdAt&sortDirection=DESC" \
  -H "Accept: application/json"
```

**Response Esperado:**

```json
{
  "content": [
    {
      "id": "rifa-001",
      "title": "Rifa do iPhone 15 Pro Max",
      "description": "Concorra a um iPhone 15 Pro Max novo na caixa!",
      "imageUrls": [
        "https://storage.googleapis.com/.../raffles/rifa-001/image_0.jpg",
        "https://storage.googleapis.com/.../raffles/rifa-001/image_1.jpg"
      ],
      "ticketPrice": 10.0,
      "totalTickets": 1000,
      "soldTickets": 342,
      "availableTickets": 658,
      "status": "ACTIVE",
      "drawDate": "2025-12-25T20:00:00Z",
      "expiresAt": "2025-12-25T19:00:00Z",
      "createdAt": "2025-11-01T10:00:00Z",
      "updatedAt": "2025-11-17T15:30:00Z"
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "currentPage": 0,
  "pageSize": 20
}
```

### 1.2 Buscar Rifas Específicas (Filtro)

```bash
# Rifas com status específico
curl -X GET "${API_URL}/raffles?status=ENDED" \
  -H "Accept: application/json"

# Rifas sorteadas (vencedores já definidos)
curl -X GET "${API_URL}/raffles?status=DRAWN" \
  -H "Accept: application/json"

# Paginação (página 2)
curl -X GET "${API_URL}/raffles?page=1&pageSize=10" \
  -H "Accept: application/json"
```

---

## 🟢 CASO DE USO 2: Usuário Visualiza Detalhes de Uma Rifa

**Contexto:** Usuário clicou em uma rifa e quer ver informações completas.

### 2.1 Buscar Detalhes da Rifa

```bash
curl -X GET "${API_URL}/raffles/rifa-001" \
  -H "Accept: application/json"
```

**Response:**

```json
{
  "id": "rifa-001",
  "title": "Rifa do iPhone 15 Pro Max",
  "description": "Concorra a um iPhone 15 Pro Max 256GB novo na caixa, com nota fiscal e garantia de 1 ano!",
  "imageUrls": [
    "https://storage.googleapis.com/.../raffles/rifa-001/image_0.jpg",
    "https://storage.googleapis.com/.../raffles/rifa-001/image_1.jpg",
    "https://storage.googleapis.com/.../raffles/rifa-001/image_2.jpg"
  ],
  "ticketPrice": 10.0,
  "totalTickets": 1000,
  "soldTickets": 342,
  "availableTickets": 658,
  "status": "ACTIVE",
  "drawDate": "2025-12-25T20:00:00Z",
  "expiresAt": "2025-12-25T19:00:00Z",
  "createdAt": "2025-11-01T10:00:00Z",
  "updatedAt": "2025-11-17T15:30:00Z"
}
```

### 2.2 Consultar Números Disponíveis

```bash
curl -X GET "${API_URL}/raffles/rifa-001/tickets/available" \
  -H "Accept: application/json"
```

**Response:**

```json
{
  "raffleId": "rifa-001",
  "totalTickets": 1000,
  "availableTickets": 658,
  "availableNumbers": [1, 2, 3, 5, 6, 8, 9, 10, 11, 12, 14, 15, ...]
}
```

### 2.3 Ver Bilhetes Já Vendidos (PAID)

```bash
curl -X GET "${API_URL}/raffles/rifa-001/tickets?status=PAID" \
  -H "Accept: application/json"
```

**Response:**

```json
[
  {
    "id": "ticket-001",
    "raffleId": "rifa-001",
    "ticketNumber": 7,
    "buyerName": "João Silva",
    "buyerEmail": "joao@email.com",
    "buyerPhone": "+5511988887777",
    "status": "PAID",
    "purchasedAt": "2025-11-15T14:20:00Z"
  },
  {
    "id": "ticket-002",
    "raffleId": "rifa-001",
    "ticketNumber": 13,
    "buyerName": "Maria Santos",
    "buyerEmail": "maria@email.com",
    "buyerPhone": "+5511999998888",
    "status": "PAID",
    "purchasedAt": "2025-11-16T10:30:00Z"
  }
]
```

---

## 🟡 CASO DE USO 3: Usuário Compra Bilhetes (Fluxo Completo)

**Contexto:** Usuário decide comprar 3 bilhetes da rifa.

### 3.1 Comprar Bilhetes - Sistema Aloca Números Automaticamente

```bash
curl -X POST "${API_URL}/raffles/rifa-001/purchase" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "quantity": 3,
    "buyerName": "Carlos Oliveira",
    "buyerEmail": "carlos@email.com",
    "buyerPhone": "+5511977776666",
    "buyerDocument": "12345678900"
  }'
```

**Response:**

```json
{
  "success": true,
  "tickets": [
    {
      "id": "ticket-100",
      "raffleId": "rifa-001",
      "ticketNumber": 15,
      "buyerName": "Carlos Oliveira",
      "buyerEmail": "carlos@email.com",
      "buyerPhone": "+5511977776666",
      "status": "RESERVED",
      "purchasedAt": "2025-11-17T16:00:00Z"
    },
    {
      "id": "ticket-101",
      "raffleId": "rifa-001",
      "ticketNumber": 16,
      "buyerName": "Carlos Oliveira",
      "buyerEmail": "carlos@email.com",
      "buyerPhone": "+5511977776666",
      "status": "RESERVED",
      "purchasedAt": "2025-11-17T16:00:00Z"
    },
    {
      "id": "ticket-102",
      "raffleId": "rifa-001",
      "ticketNumber": 17,
      "buyerName": "Carlos Oliveira",
      "buyerEmail": "carlos@email.com",
      "buyerPhone": "+5511977776666",
      "status": "RESERVED",
      "purchasedAt": "2025-11-17T16:00:00Z"
    }
  ],
  "payment": {
    "id": "CHAR_ABC123XYZ",
    "qrCode": "00020126580014br.gov.bcb.pix2563qrcodepix.sejaefi.com.br/v2/2c5c784a26f14c1497c4c20a5b0e3e72520400005303986540530.005802BR5925Tyler Beneficente LTDA6009SAO PAULO62070503***6304A1B2",
    "qrCodeImage": "https://api.pagbank.com/qrcode/CHAR_ABC123XYZ.png",
    "expiresAt": "2025-11-17T17:00:00Z",
    "amount": 30.0,
    "status": "PENDING"
  },
  "reservationExpiresAt": "2025-11-17T17:00:00Z"
}
```

### 3.2 Comprar Bilhetes - Usuário Escolhe Números Específicos

```bash
curl -X POST "${API_URL}/raffles/rifa-001/purchase" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "quantity": 3,
    "ticketNumbers": [7, 13, 42],
    "buyerName": "Ana Paula",
    "buyerEmail": "ana@email.com",
    "buyerPhone": "+5511966665555",
    "buyerDocument": "98765432100"
  }'
```

### 3.3 Simular Webhook do PagBank (Pagamento Confirmado)

**⚠️ Este endpoint é chamado automaticamente pelo PagBank. Apenas para testes locais:**

```bash
curl -X POST "${API_URL}/webhooks/pagbank" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "ORDE_XYZ789",
    "reference_id": "raffle_rifa-001",
    "charges": [{
      "id": "CHAR_ABC123XYZ",
      "status": "PAID",
      "amount": {
        "value": 3000,
        "currency": "BRL"
      },
      "paid_at": "2025-11-17T16:05:00.000-03:00"
    }]
  }'
```

**Response:**

```json
{
  "success": true,
  "donationId": "rifa-001",
  "previousStatus": "PENDING",
  "newStatus": "PAID",
  "processed": true,
  "message": "Raffle webhook processed - 3 tickets updated"
}
```

### 3.4 Verificar Status dos Bilhetes Após Pagamento

```bash
# Buscar bilhetes do comprador (via email)
curl -X GET "${API_URL}/raffles/rifa-001/tickets" \
  -H "Accept: application/json" \
  | grep -A 20 "carlos@email.com"
```

**Agora os tickets devem estar com `status: "PAID"`**

---

## 🔴 CASO DE USO 4: Admin Cria Nova Rifa (Fluxo Completo)

**Contexto:** Administrador quer criar uma nova rifa de carro.

### 4.1 Criar Rifa Base

```bash
curl -X POST "${API_URL}/raffles" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -d '{
    "title": "Rifa do Carro 0km - Fiat Argo",
    "description": "Concorra a um Fiat Argo 0km, ano 2025, quitado e com IPVA pago!",
    "ticketPrice": 50.00,
    "totalTickets": 5000,
    "drawDate": "2026-01-31T20:00:00Z",
    "expiresAt": "2026-01-31T19:00:00Z"
  }'
```

**Response:**

```json
{
  "id": "rifa-002",
  "title": "Rifa do Carro 0km - Fiat Argo",
  "description": "Concorra a um Fiat Argo 0km, ano 2025, quitado e com IPVA pago!",
  "imageUrls": [],
  "ticketPrice": 50.0,
  "totalTickets": 5000,
  "soldTickets": 0,
  "availableTickets": 5000,
  "status": "ACTIVE",
  "drawDate": "2026-01-31T20:00:00Z",
  "expiresAt": "2026-01-31T19:00:00Z",
  "createdAt": "2025-11-17T16:30:00Z",
  "updatedAt": "2025-11-17T16:30:00Z"
}
```

**⚠️ Importante:** O backend gera automaticamente:

- `committedEntropy`: Hash SHA-256 da entropia (público)
- `revealEntropy`: Entropia original (oculta até sorteio)

### 4.2 Upload de Múltiplas Imagens (até 10)

```bash
# Upload de 3 imagens
curl -X POST "${API_URL}/raffles/rifa-002/upload-images" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -F "files=@/path/to/carro-foto1.jpg" \
  -F "files=@/path/to/carro-foto2.jpg" \
  -F "files=@/path/to/carro-foto3.jpg"
```

**Response:**

```json
{
  "raffleId": "rifa-002",
  "uploadedImages": [
    "https://storage.googleapis.com/.../raffles/rifa-002/image_0.jpg",
    "https://storage.googleapis.com/.../raffles/rifa-002/image_1.jpg",
    "https://storage.googleapis.com/.../raffles/rifa-002/image_2.jpg"
  ]
}
```

### 4.3 Atualizar Informações da Rifa

```bash
curl -X PUT "${API_URL}/raffles/rifa-002" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -d '{
    "title": "Rifa do Carro 0km - Fiat Argo Drive 1.0 - ÚLTIMA SEMANA!",
    "description": "Concorra a um Fiat Argo Drive 1.0, 0km, ano 2025, completo, quitado e com IPVA pago! Apenas 1000 bilhetes restantes!"
  }'
```

### 4.4 Deletar Uma Imagem Específica

```bash
# Deletar a segunda imagem (index 1)
curl -X DELETE "${API_URL}/raffles/rifa-002/images/1" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}"
```

**Response:** `204 No Content`

---

## 🟣 CASO DE USO 5: Rifa Expira e Admin Realiza Sorteio

**Contexto:** Rifa atingiu data de expiração. Sistema mudou status para `ENDED`. Admin vai sortear o vencedor.

### 5.1 Verificar Rifas Expiradas (Automático)

**⚠️ Executado automaticamente a cada 15 minutos pelo backend via `@Scheduled`**

```bash
# Listar rifas que expiraram
curl -X GET "${API_URL}/raffles?status=ENDED" \
  -H "Accept: application/json"
```

### 5.2 Admin Busca Entropia para Realizar Sorteio

**⚠️ O backend armazena a `revealEntropy` internamente. No frontend, basta chamar o endpoint de sorteio:**

```bash
curl -X POST "${API_URL}/raffles/rifa-001/draw" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -d '{
    "revealEntropy": "a3f2b8c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2"
  }'
```

**Response:**

```json
{
  "raffleId": "rifa-001",
  "winnerTicketNumber": 342,
  "winnerName": "João Silva",
  "winnerEmail": "joao@email.com",
  "winnerPhone": "+5511988887777",
  "drawnAt": "2025-12-25T20:05:00Z",
  "isVerified": true
}
```

**O que acontece internamente:**

1. Backend verifica: `SHA-256(revealEntropy) == committedEntropy`
2. Calcula vencedor: `SHA-256(revealEntropy + raffleId + totalTickets) % totalTickets + 1`
3. Busca ticket com esse número
4. Retorna dados do vencedor
5. Atualiza status da rifa para `DRAWN`

---

## 🟢 CASO DE USO 6: Público Verifica Sorteio (Transparência)

**Contexto:** Qualquer pessoa quer confirmar que o sorteio foi justo.

### 6.1 Verificar Sorteio Publicamente

```bash
curl -X GET "${API_URL}/raffles/rifa-001/verify-draw" \
  -H "Accept: application/json"
```

**Response:**

```json
{
  "raffleId": "rifa-001",
  "committedEntropyHash": "7d9c4f2ab8e3c1d5f6a9b2c8e4f1a7d3b9c5e2f8a4b6c9d1e5f2a8b3c7d4e9f1",
  "revealedEntropy": "a3f2b8c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2",
  "winnerTicketNumber": 342,
  "verificationPassed": true,
  "message": "✅ Sorteio verificado com sucesso! O resultado é auditável e transparente."
}
```

**Explicação:**

- `committedEntropyHash`: Hash SHA-256 gerado na **criação** da rifa (público desde o início)
- `revealedEntropy`: Entropia original revelada no **momento do sorteio**
- `verificationPassed`: `true` se `SHA-256(revealedEntropy) == committedEntropyHash`
- Qualquer pessoa pode recalcular localmente e confirmar o vencedor

### 6.2 Recalcular Vencedor Localmente (Auditoria Independente)

```bash
# Pseudo-código para auditoria manual:
# 1. Pegar revealedEntropy, raffleId, totalTickets da resposta acima
# 2. Calcular: seed = revealedEntropy + raffleId + totalTickets
# 3. Calcular: hash = SHA256(seed)
# 4. Calcular: winnerNumber = (parseInt(hash[0:8], 16) % totalTickets) + 1
# 5. Comparar com winnerTicketNumber retornado pela API

# Exemplo em bash com openssl:
REVEALED_ENTROPY="a3f2b8c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2"
RAFFLE_ID="rifa-001"
TOTAL_TICKETS="1000"

# Calcular seed
SEED="${REVEALED_ENTROPY}${RAFFLE_ID}${TOTAL_TICKETS}"

# Calcular SHA-256
HASH=$(echo -n "$SEED" | openssl dgst -sha256 | awk '{print $2}')

# Extrair primeiros 8 caracteres e calcular módulo
HASH_INT=$((0x${HASH:0:8}))
WINNER=$((($HASH_INT % $TOTAL_TICKETS) + 1))

echo "Vencedor calculado localmente: $WINNER"
# Deve ser: 342 (mesmo resultado da API)
```

---

## 🔴 CASO DE USO 7: Admin Cancela Rifa

**Contexto:** Por algum motivo, admin precisa cancelar uma rifa em andamento.

### 7.1 Cancelar Rifa

```bash
curl -X POST "${API_URL}/raffles/rifa-002/cancel" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -d '{}'
```

**Response:** `200 OK`

**O que acontece:**

- Status da rifa muda para `CANCELLED`
- `active` muda para `false`
- Bilhetes **não são reembolsados automaticamente** (conforme requisito)

### 7.2 Verificar Rifa Cancelada

```bash
curl -X GET "${API_URL}/raffles/rifa-002" \
  -H "Accept: application/json"
```

**Response:**

```json
{
  "id": "rifa-002",
  "status": "CANCELLED",
  "active": false,
  ...
}
```

---

## 🔴 CASO DE USO 8: Admin Deleta Rifa

**Contexto:** Admin quer remover completamente uma rifa do sistema (soft delete).

### 8.1 Deletar Rifa

```bash
curl -X DELETE "${API_URL}/raffles/rifa-002" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}"
```

**Response:** `204 No Content`

**⚠️ Cuidado:** Esta operação:

- Define `active = false` no Firestore (soft delete)
- Não remove imagens do Storage
- Não remove tickets associados

---

## 🟡 CASO DE USO 9: Fluxo Completo Ponta a Ponta (Happy Path)

**Cenário:** Do início ao fim - criar rifa, vender bilhetes, sortear, verificar.

### Passo 1: Admin Cria Rifa

```bash
curl -X POST "${API_URL}/raffles" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -d '{
    "title": "Rifa Beneficente - Notebook Dell",
    "description": "Notebook Dell Inspiron 15, i7, 16GB RAM, SSD 512GB",
    "ticketPrice": 20.00,
    "totalTickets": 500,
    "drawDate": "2025-12-01T20:00:00Z",
    "expiresAt": "2025-12-01T19:00:00Z"
  }'
```

**Salvar `id` da resposta:** `rifa-003`

### Passo 2: Admin Faz Upload de Imagens

```bash
curl -X POST "${API_URL}/raffles/rifa-003/upload-images" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -F "files=@notebook1.jpg" \
  -F "files=@notebook2.jpg"
```

### Passo 3: Usuário 1 Compra 5 Bilhetes

```bash
curl -X POST "${API_URL}/raffles/rifa-003/purchase" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 5,
    "buyerName": "Pedro Alves",
    "buyerEmail": "pedro@email.com",
    "buyerPhone": "+5511955554444",
    "buyerDocument": "11122233344"
  }'
```

**Copiar `payment.id` da resposta:** `CHAR_XYZ123`

### Passo 4: Simular Pagamento do Usuário 1

```bash
curl -X POST "${API_URL}/webhooks/pagbank" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "ORDE_ABC",
    "reference_id": "raffle_rifa-003",
    "charges": [{
      "id": "CHAR_XYZ123",
      "status": "PAID",
      "amount": { "value": 10000 },
      "paid_at": "2025-11-17T17:00:00.000-03:00"
    }]
  }'
```

### Passo 5: Usuário 2 Compra 3 Bilhetes (Números Específicos)

```bash
curl -X POST "${API_URL}/raffles/rifa-003/purchase" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 3,
    "ticketNumbers": [7, 77, 777],
    "buyerName": "Fernanda Costa",
    "buyerEmail": "fernanda@email.com",
    "buyerPhone": "+5511944443333",
    "buyerDocument": "55566677788"
  }'
```

### Passo 6: Simular Pagamento do Usuário 2

```bash
curl -X POST "${API_URL}/webhooks/pagbank" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "ORDE_DEF",
    "reference_id": "raffle_rifa-003",
    "charges": [{
      "id": "CHAR_ABC456",
      "status": "PAID",
      "amount": { "value": 6000 },
      "paid_at": "2025-11-17T17:10:00.000-03:00"
    }]
  }'
```

### Passo 7: Verificar Total de Bilhetes Vendidos

```bash
curl -X GET "${API_URL}/raffles/rifa-003" \
  -H "Accept: application/json" \
  | grep -E "soldTickets|availableTickets"
```

**Deve mostrar:** `"soldTickets": 8, "availableTickets": 492`

### Passo 8: Aguardar Expiração (Simulação)

**⚠️ Em produção, aguardar até `expiresAt`. Para testes, ajustar data no banco:**

```bash
# Atualizar rifa para status ENDED manualmente (apenas testes)
# Normalmente o @Scheduled faz isso automaticamente
```

### Passo 9: Admin Realiza Sorteio

```bash
curl -X POST "${API_URL}/raffles/rifa-003/draw" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -d '{
    "revealEntropy": "entropia_gerada_automaticamente_pelo_backend"
  }'
```

**⚠️ Nota:** Em produção, o frontend recupera `revealEntropy` do backend antes de chamar este endpoint.

### Passo 10: Público Verifica Resultado

```bash
curl -X GET "${API_URL}/raffles/rifa-003/verify-draw" \
  -H "Accept: application/json"
```

**Resultado:**

```json
{
  "raffleId": "rifa-003",
  "winnerTicketNumber": 77,
  "verificationPassed": true,
  "message": "✅ Sorteio verificado com sucesso!"
}
```

**Vencedor:** Fernanda Costa (número 77)

---

## 🔍 CASO DE USO 10: Testes de Validação e Erros

### 10.1 Tentar Comprar Bilhetes Indisponíveis

```bash
curl -X POST "${API_URL}/raffles/rifa-003/purchase" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 2,
    "ticketNumbers": [77, 99],
    "buyerName": "Teste",
    "buyerEmail": "teste@email.com",
    "buyerPhone": "+5511900000000",
    "buyerDocument": "00000000000"
  }'
```

**Response Esperado:** `400 Bad Request`

```json
{
  "error": "ticket_unavailable",
  "message": "Bilhete 77 já foi vendido"
}
```

### 10.2 Tentar Comprar Mais Bilhetes que Disponíveis

```bash
curl -X POST "${API_URL}/raffles/rifa-003/purchase" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 600,
    "buyerName": "Teste",
    "buyerEmail": "teste@email.com",
    "buyerPhone": "+5511900000000",
    "buyerDocument": "00000000000"
  }'
```

**Response Esperado:** `400 Bad Request`

```json
{
  "error": "insufficient_tickets",
  "message": "Apenas 492 bilhetes disponíveis, solicitado: 600"
}
```

### 10.3 CPF Inválido

```bash
curl -X POST "${API_URL}/raffles/rifa-003/purchase" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 1,
    "buyerName": "Teste",
    "buyerEmail": "teste@email.com",
    "buyerPhone": "+5511900000000",
    "buyerDocument": "11111111111"
  }'
```

**Response Esperado:** `400 Bad Request`

```json
{
  "error": "invalid_document",
  "message": "CPF inválido"
}
```

### 10.4 Webhook com Valor Incorreto

```bash
curl -X POST "${API_URL}/webhooks/pagbank" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "ORDE_TEST",
    "reference_id": "raffle_rifa-003",
    "charges": [{
      "id": "CHAR_TEST",
      "status": "PAID",
      "amount": { "value": 1000 },
      "paid_at": "2025-11-17T18:00:00.000-03:00"
    }]
  }'
```

**Response Esperado:** `400 Bad Request`

```json
{
  "success": false,
  "message": "Payment amount mismatch - paid: R$ 10.0, expected: R$ 20.0"
}
```

### 10.5 Tentar Criar Rifa Sem Autenticação

```bash
curl -X POST "${API_URL}/raffles" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Teste",
    "ticketPrice": 10.00,
    "totalTickets": 100
  }'
```

**Response Esperado:** `401 Unauthorized`

### 10.6 Tentar Sortear Rifa que Ainda Está Ativa

```bash
curl -X POST "${API_URL}/raffles/rifa-001/draw" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -d '{ "revealEntropy": "test" }'
```

**Response Esperado:** `400 Bad Request`

```json
{
  "error": "raffle_not_ended",
  "message": "Rifa ainda está ativa. Aguarde expiração."
}
```

---

## 📊 CASO DE USO 11: Estatísticas e Consultas Avançadas

### 11.1 Buscar Todas as Rifas Sorteadas

```bash
curl -X GET "${API_URL}/raffles?status=DRAWN&page=0&pageSize=50" \
  -H "Accept: application/json"
```

### 11.2 Buscar Bilhetes de Um Comprador Específico

```bash
# Buscar todos os tickets e filtrar por email (lado cliente)
curl -X GET "${API_URL}/raffles/rifa-003/tickets" \
  -H "Accept: application/json" \
  | jq '.[] | select(.buyerEmail=="fernanda@email.com")'
```

### 11.3 Verificar Quantidade de Rifas Ativas

```bash
curl -X GET "${API_URL}/raffles?status=ACTIVE&pageSize=1" \
  -H "Accept: application/json" \
  | jq '.totalElements'
```

### 11.4 Listar Rifas Próximas do Sorteio (Order by drawDate)

```bash
curl -X GET "${API_URL}/raffles?status=ACTIVE&sortBy=drawDate&sortDirection=ASC" \
  -H "Accept: application/json"
```

---

## 🛠️ Utilitários e Scripts

### Script Bash: Compra Automatizada de Bilhetes

```bash
#!/bin/bash

RAFFLE_ID="rifa-003"
API_URL="http://localhost:8080/api"

# Comprar 10 bilhetes para 10 pessoas diferentes
for i in {1..10}; do
  echo "Comprando bilhete $i..."

  curl -X POST "${API_URL}/raffles/${RAFFLE_ID}/purchase" \
    -H "Content-Type: application/json" \
    -d "{
      \"quantity\": 1,
      \"buyerName\": \"Comprador $i\",
      \"buyerEmail\": \"comprador$i@email.com\",
      \"buyerPhone\": \"+551199999${i}${i}${i}${i}\",
      \"buyerDocument\": \"${i}${i}${i}${i}${i}${i}${i}${i}${i}${i}${i}\"
    }"

  sleep 2
done

echo "✅ Compra de 10 bilhetes concluída!"
```

### Script PowerShell: Verificar Status de Múltiplas Rifas

```powershell
$raffleIds = @("rifa-001", "rifa-002", "rifa-003")
$apiUrl = "http://localhost:8080/api"

foreach ($id in $raffleIds) {
    $response = Invoke-RestMethod -Uri "$apiUrl/raffles/$id" -Method Get
    Write-Host "Rifa: $($response.title)"
    Write-Host "Status: $($response.status)"
    Write-Host "Vendidos: $($response.soldTickets) / $($response.totalTickets)"
    Write-Host "----"
}
```

---

## 📝 Notas Importantes

### Validações do Backend

1. **CPF:** Validado via algoritmo oficial (dígitos verificadores)
2. **Valor PIX:** Validado automaticamente no webhook (valor pago vs quantidade × preço)
3. **Disponibilidade:** Backend verifica se números ainda estão disponíveis
4. **Entropia:** SHA-256 do `revealEntropy` deve bater com `committedEntropy`
5. **Status:** Apenas rifas com status `ENDED` podem ser sorteadas

### Timeouts e Expiração

- **Reserva de bilhetes:** Expira automaticamente conforme `reservationExpiresAt`
- **QR Code PIX:** Expira conforme `payment.expiresAt` (geralmente 1 hora)
- **Rifas:** Expiram conforme `expiresAt`, status muda para `ENDED` via `@Scheduled`

### Polling no Frontend

```javascript
// Exemplo de polling para verificar status do pagamento
const checkPaymentStatus = async (raffleId, paymentId) => {
  const interval = setInterval(async () => {
    const tickets = await fetch(`${API_URL}/raffles/${raffleId}/tickets`).then(
      (r) => r.json()
    );

    const paidTickets = tickets.filter(
      (t) => t.paymentId === paymentId && t.status === "PAID"
    );

    if (paidTickets.length > 0) {
      clearInterval(interval);
      console.log("✅ Pagamento confirmado!");
    }
  }, 5000); // A cada 5 segundos
};
```

---

## 🎉 Conclusão

Este documento cobre **todos os casos de uso** do módulo de Rifas:

✅ Listagem pública  
✅ Detalhes e consulta de disponibilidade  
✅ Compra de bilhetes com PIX  
✅ Webhook de confirmação de pagamento  
✅ Criação e gerenciamento (admin)  
✅ Upload de múltiplas imagens  
✅ Realização de sorteio verificável  
✅ Verificação pública (transparência)  
✅ Cancelamento e deleção  
✅ Testes de validação e erros

**Pronto para integração frontend!** 🚀
