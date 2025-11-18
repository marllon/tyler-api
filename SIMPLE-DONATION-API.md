# 💰 API de Doação Simples - Documentação

## 📋 Visão Geral

Endpoint para doações simples sem vínculo com metas, rifas ou produtos. Retorna QR Code PIX para pagamento imediato.

**Endpoint:** `POST /api/donations/simple`

---

## ✅ Caso de Uso 1: Doação Não Anônima Completa

### Request

```bash
curl -X POST http://localhost:8080/api/donations/simple \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 5000,
    "anonymous": false,
    "message": "Apoiando a causa!",
    "donor": {
      "name": "João Silva",
      "email": "joao@email.com",
      "phone": "+5511988887777",
      "document": "12345678900"
    }
  }'
```

**Parâmetros:**

- `amount`: 5000 (R$ 50,00 em centavos)
- `anonymous`: false
- `message`: "Apoiando a causa!" (opcional)
- `donor.name`: "João Silva" (obrigatório se não anônimo)
- `donor.email`: "joao@email.com" (obrigatório se não anônimo)
- `donor.phone`: "+5511988887777" (opcional)
- `donor.document`: "12345678900" (opcional)

### Response (200 OK)

```json
{
  "paymentId": "CHAR_ABC123XYZ",
  "qrCode": "00020126580014br.gov.bcb.pix2563qrcodepix.sejaefi.com.br/v2/2c5c784a26f14c1497c4c20a5b0e3e72520400005303986540550.005802BR5925Tyler Beneficente LTDA6009SAO PAULO62070503***6304A1B2",
  "qrCodeImage": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
  "status": "PENDING",
  "expiresAt": "2025-11-18T18:30:00Z"
}
```

---

## ✅ Caso de Uso 2: Doação Anônima

### Request

```bash
curl -X POST http://localhost:8080/api/donations/simple \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 2500,
    "anonymous": true,
    "message": "Que Deus abençoe este trabalho!"
  }'
```

**Parâmetros:**

- `amount`: 2500 (R$ 25,00 em centavos)
- `anonymous`: true
- `message`: opcional
- `donor`: não é necessário quando anônimo

### Response (200 OK)

```json
{
  "paymentId": "CHAR_DEF456ZYX",
  "qrCode": "00020126580014br.gov.bcb.pix...",
  "qrCodeImage": "data:image/png;base64,...",
  "status": "PENDING",
  "expiresAt": "2025-11-18T18:45:00Z"
}
```

---

## ✅ Caso de Uso 3: Doação Sem Mensagem

### Request

```bash
curl -X POST http://localhost:8080/api/donations/simple \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 10000,
    "anonymous": false,
    "donor": {
      "name": "Maria Santos",
      "email": "maria@email.com"
    }
  }'
```

**Mensagem é opcional e pode ser omitida completamente.**

---

## ❌ Caso de Erro 1: Valor Inválido (< R$ 1,00)

### Request

```bash
curl -X POST http://localhost:8080/api/donations/simple \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 50,
    "anonymous": false,
    "donor": {
      "name": "Teste",
      "email": "teste@email.com"
    }
  }'
```

### Response (400 Bad Request)

```json
{
  "error": "Valor mínimo da doação é R$ 1,00"
}
```

---

## ❌ Caso de Erro 2: Dados Obrigatórios Faltando

### Request

```bash
curl -X POST http://localhost:8080/api/donations/simple \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 5000,
    "anonymous": false
  }'
```

### Response (400 Bad Request)

```json
{
  "error": "Nome e email são obrigatórios para doações não anônimas"
}
```

---

## ❌ Caso de Erro 3: Email Inválido

### Request

```bash
curl -X POST http://localhost:8080/api/donations/simple \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 5000,
    "anonymous": false,
    "donor": {
      "name": "João",
      "email": "email-invalido"
    }
  }'
```

### Response (400 Bad Request)

```json
{
  "error": "Email inválido"
}
```

---

## ❌ Caso de Erro 4: Erro ao Gerar QR Code

### Response (500 Internal Server Error)

```json
{
  "error": "Erro ao processar pagamento PIX"
}
```

Este erro pode ocorrer quando há problemas na comunicação com o PagBank ou configuração incorreta.

---

## 📊 Validações Automáticas

### Backend Valida:

1. ✅ **Valor mínimo:** >= R$ 1,00 (100 centavos)
2. ✅ **Email formato:** Regex de validação
3. ✅ **Dados obrigatórios:** Nome + email quando não anônimo
4. ✅ **Tamanho da mensagem:** Máximo 500 caracteres

### Frontend Deve Validar:

- CPF (formato e dígitos verificadores)
- Telefone (formato brasileiro)
- Valor máximo (se houver limite)
- Confirmação antes de enviar

---

## 🔄 Fluxo Completo

### 1. Frontend Envia Request

```javascript
const response = await fetch("http://localhost:8080/api/donations/simple", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    amount: 5000, // R$ 50,00
    anonymous: false,
    message: "Apoiando a causa!",
    donor: {
      name: "João Silva",
      email: "joao@email.com",
    },
  }),
});

const data = await response.json();
```

### 2. Backend Processa

1. Valida dados recebidos
2. Cria registro de doação no Firestore (status: PENDING)
3. Gera cobrança PIX no PagBank
4. Retorna QR Code e payment ID

### 3. Frontend Exibe QR Code

```javascript
if (response.ok) {
  // Exibir modal com QR Code
  showPixModal({
    qrCodeImage: data.qrCodeImage,
    qrCodeText: data.qrCode,
    amount: 50.0,
    expiresAt: data.expiresAt,
  });

  // Iniciar polling para verificar pagamento
  startPaymentPolling(data.paymentId);
}
```

### 4. Webhook Confirma Pagamento

Quando usuário paga, PagBank envia webhook automático:

- Backend atualiza status da doação para PAID
- Frontend polling detecta mudança
- Exibe mensagem de sucesso

---

## 🔐 Segurança

### Dados Sensíveis

- Email é armazenado mas não exposto publicamente
- CPF é opcional e nunca exposto
- Telefone é opcional

### Doações Anônimas

- Nome salvo como "Doador Anônimo"
- Email genérico: "anonimo@tyler.org"
- Mensagem ainda pode ser exibida (se fornecida)

---

## 📝 Campos do Request

| Campo            | Tipo    | Obrigatório    | Descrição                                    | Exemplo          |
| ---------------- | ------- | -------------- | -------------------------------------------- | ---------------- |
| `amount`         | Integer | ✅ Sim         | Valor em centavos (min: 100)                 | 5000 = R$ 50,00  |
| `anonymous`      | Boolean | ✅ Sim         | Se é doação anônima                          | true/false       |
| `message`        | String  | ❌ Não         | Mensagem do doador (max: 500 chars)          | "Apoiando!"      |
| `donor`          | Object  | ⚠️ Condicional | Dados do doador (obrigatório se não anônimo) | -                |
| `donor.name`     | String  | ⚠️ Condicional | Nome completo                                | "João Silva"     |
| `donor.email`    | String  | ⚠️ Condicional | Email válido                                 | "joao@email.com" |
| `donor.phone`    | String  | ❌ Não         | Telefone                                     | "+5511988887777" |
| `donor.document` | String  | ❌ Não         | CPF                                          | "12345678900"    |

---

## 📝 Campos do Response

| Campo         | Tipo   | Descrição                                    |
| ------------- | ------ | -------------------------------------------- |
| `paymentId`   | String | ID da cobrança no PagBank                    |
| `qrCode`      | String | Código PIX copia e cola                      |
| `qrCodeImage` | String | Imagem QR Code em base64                     |
| `status`      | String | Status atual (sempre "PENDING" inicialmente) |
| `expiresAt`   | String | Data/hora de expiração do QR Code (ISO-8601) |

---

## 🎯 Códigos de Status HTTP

| Código | Significado           | Quando Ocorre                                                   |
| ------ | --------------------- | --------------------------------------------------------------- |
| 200    | Success               | Doação criada e QR Code gerado com sucesso                      |
| 400    | Bad Request           | Validação falhou (valor mínimo, email inválido, dados faltando) |
| 500    | Internal Server Error | Erro ao gerar PIX no PagBank                                    |

---

## 🧪 Testando Localmente

### PowerShell (Windows)

```powershell
# Doação não anônima
Invoke-RestMethod -Uri "http://localhost:8080/api/donations/simple" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{
    "amount": 5000,
    "anonymous": false,
    "message": "Teste",
    "donor": {
      "name": "João Silva",
      "email": "joao@email.com"
    }
  }'

# Doação anônima
Invoke-RestMethod -Uri "http://localhost:8080/api/donations/simple" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{
    "amount": 2500,
    "anonymous": true
  }'
```

### CMD (Windows)

```cmd
curl -X POST http://localhost:8080/api/donations/simple ^
  -H "Content-Type: application/json" ^
  -d "{\"amount\": 5000, \"anonymous\": false, \"message\": \"Apoiando a causa!\", \"donor\": {\"name\": \"João Silva\", \"email\": \"joao@email.com\"}}"
```

---

## 📌 Notas Importantes

1. **Valor sempre em centavos:** R$ 50,00 = 5000 centavos
2. **Webhook automático:** Não precisa implementar lógica de confirmação manual
3. **QR Code expira:** Padrão do PagBank é ~60 minutos
4. **Doação anônima não precisa de donor:** Pode enviar `null` ou omitir completamente
5. **Mensagem opcional:** Pode ser string vazia `""` ou `null`

---

## ✅ Pronto para Usar!

O endpoint está funcional e pronto para integração frontend. Todas as validações e tratamentos de erro estão implementados.
