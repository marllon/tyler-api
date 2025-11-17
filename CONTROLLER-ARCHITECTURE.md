# 📋 Separação de Responsabilidades - Controllers PagBank

## ✅ Arquitetura Final

### 1️⃣ **PaymentController** (`/api/payments`)

**Propósito**: Interação direta com PagBank (checkout genérico)

**Endpoints**:

- `POST /api/payments/checkout` - Criar checkout PIX genérico
- `GET /api/payments/{id}/status` - Consultar status de pagamento

**Responsabilidades**:

- Criar checkouts PIX sem vínculo com doações
- Consultar status de transações no PagBank
- Interface de baixo nível com PagBankProvider

**Uso**:

- Pagamentos avulsos/testes
- Integração direta com PagBank
- **NÃO** gerencia doações/metas/rifas

---

### 2️⃣ **PagBankWebhookController** (`/api/webhooks`)

**Propósito**: Receber notificações do PagBank e rotear para destinos

**Endpoints**:

- `POST /api/webhooks/pagbank` - Webhook do PagBank (chamado automaticamente)
- `GET /api/webhooks/pagbank/health` - Health check

**Responsabilidades**:

- Receber notificações de mudança de status (PAID, CANCELLED, etc)
- Processar webhook via DonationService
- Rotear pagamento para destino correto (Goal/Raffle/Order)
- Atualizar status da doação
- Adicionar valor à meta automaticamente

**Uso**:

- Configurado no painel PagBank
- URL: `https://seu-dominio.com/api/webhooks/pagbank`
- Processamento automático de pagamentos

---

### 3️⃣ **DonationController** (`/api/donations`)

**Propósito**: Gerenciar doações vinculadas a metas/rifas/pedidos

**Endpoints**:

- `POST /api/donations` - Criar doação vinculada
- `POST /api/donations/{id}/create-pix-charge` - Gerar QR Code PIX
- `GET /api/donations` - Listar doações (admin)
- `GET /api/donations/{id}` - Buscar doação específica
- `GET /api/donations/target/{targetId}` - Doações de uma meta
- `POST /api/donations/{id}/process` - Reprocessar manualmente (admin)

**Responsabilidades**:

- CRUD completo de doações
- Vincular pagamentos a Goals/Raffles/Orders
- Gerar QR Codes PIX via PagBank
- Rastreamento de transações
- Relatórios e auditoria

**Uso**:

- Frontend cria doação vinculada a meta
- Frontend solicita QR Code PIX
- Admin consulta histórico de doações

---

## 🔄 Fluxo Completo de Doação

```
1. Frontend → POST /api/donations
   {
     "donationType": "GOAL",
     "targetId": "goal-123",
     "amount": 50.00,
     "paymentMethod": "PIX"
   }
   ← Retorna donationId

2. Frontend → POST /api/donations/{donationId}/create-pix-charge
   ← Retorna QR Code PIX

3. Usuário escaneia QR Code e paga

4. PagBank → POST /api/webhooks/pagbank (automático)
   → DonationService.processWebhook()
   → Atualiza donation.status = PAID
   → GoalService.addAmount(goalId, amount)
   → Meta atualizada automaticamente!

5. Frontend consulta GET /api/goals/{goalId}
   ← Meta com currentAmount atualizado
```

---

## 🚫 O que foi REMOVIDO

### ❌ Webhook do PaymentController

- **Removido**: `POST /api/payments/webhook`
- **Motivo**: Duplicado e sem lógica de roteamento
- **Substituído por**: `POST /api/webhooks/pagbank` (PagBankWebhookController)

---

## 🎯 Quando usar cada Controller?

| Cenário                        | Controller               | Endpoint                                     |
| ------------------------------ | ------------------------ | -------------------------------------------- |
| Criar doação para meta         | DonationController       | `POST /api/donations`                        |
| Gerar QR Code PIX              | DonationController       | `POST /api/donations/{id}/create-pix-charge` |
| Listar doações (admin)         | DonationController       | `GET /api/donations`                         |
| Receber notificação PagBank    | PagBankWebhookController | `POST /api/webhooks/pagbank` (automático)    |
| Checkout genérico (sem doação) | PaymentController        | `POST /api/payments/checkout`                |
| Consultar status PagBank       | PaymentController        | `GET /api/payments/{id}/status`              |

---

## ✅ Benefícios da Separação

1. **Responsabilidade Única**: Cada controller tem propósito claro
2. **Manutenibilidade**: Fácil entender onde está cada lógica
3. **Escalabilidade**: Adicionar Raffles/Orders sem modificar webhook
4. **Testabilidade**: Controllers menores e focados
5. **Type Safety**: Sem `Map<String, Any>` - DTOs tipados

---

## 📝 DTOs Criados

### PagBankDto.kt

- `CreateCheckoutRequest` - Criar checkout genérico
- `CheckoutResponse` - Resposta com QR Code
- `PaymentStatusResponse` - Status de pagamento
- `WebhookResponse` - Resposta do webhook
- `HealthCheckResponse` - Health check

### DonationDto.kt

- `ProcessDonationResponse` - Resultado de processamento manual

---

## 🔒 Segurança

- **Webhook**: Endpoint público (PagBank precisa acessar)
- **Donations**: Endpoints protegidos com autenticação
- **Admin**: Processamento manual requer permissões
