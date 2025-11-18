# 🎰 PROMPT COMPLETO - Implementação do Módulo de Rifas no Frontend Tyler

## 📋 Contexto do Projeto

Você está trabalhando no **frontend do Tyler**, uma aplicação para gerenciar doações, metas e rifas beneficentes. O backend já está **100% implementado e testado** com sistema de rifas verificáveis usando criptografia (commit-reveal scheme).

**Tecnologias do Frontend Tyler:**

- Framework: **Vue.js 3** (Composition API)
- TypeScript
- Axios para requisições HTTP
- Vue Router para navegação
- Pinia/Vuex para state management
- TailwindCSS/CSS para estilização
- Firebase Auth para autenticação

**API Base URL:**

- Desenvolvimento: `http://localhost:8080`
- Produção: `https://tyler-api-production.com` (conforme configuração)

---

## 🎯 Objetivo

Implementar **módulo completo de Rifas** no frontend, incluindo:

### Para Usuários Públicos:

1. ✅ Listagem de rifas ativas (com paginação)
2. ✅ Visualização detalhada de uma rifa
3. ✅ Compra de bilhetes com PIX
4. ✅ Verificação de sorteio (transparência)
5. ✅ Consulta de bilhetes disponíveis

### Para Administradores:

6. ✅ Criação de rifas
7. ✅ Edição de rifas
8. ✅ Upload de múltiplas imagens (até 10)
9. ✅ Realização de sorteio
10. ✅ Cancelamento de rifas
11. ✅ Dashboard administrativo

---

## 📚 Documentação da API Disponível

Leia atentamente o arquivo **`RAFFLES-API-GUIDE.md`** localizado em `backend/RAFFLES-API-GUIDE.md`. Ele contém:

- ✅ Todos os 12 endpoints documentados
- ✅ Exemplos de requests/responses
- ✅ Explicação do sistema de sorteio verificável
- ✅ Fluxo de compra de bilhetes com PIX
- ✅ Validação de valores pelo webhook
- ✅ Integração com PagBank

**Endpoints Principais:**

**PÚBLICOS:**

- `GET /api/raffles` - Listar rifas (paginado)
- `GET /api/raffles/{id}` - Detalhes de uma rifa
- `GET /api/raffles/{id}/tickets` - Listar bilhetes da rifa
- `GET /api/raffles/{id}/tickets/available` - Números disponíveis
- `POST /api/raffles/{id}/purchase` - Comprar bilhetes
- `GET /api/raffles/{id}/verify-draw` - Verificar sorteio

**ADMIN (requer Firebase Auth Token):**

- `POST /api/raffles` - Criar rifa
- `PUT /api/raffles/{id}` - Atualizar rifa
- `DELETE /api/raffles/{id}` - Deletar rifa
- `POST /api/raffles/{id}/upload-images` - Upload de imagens
- `DELETE /api/raffles/{id}/images/{imageIndex}` - Deletar imagem
- `POST /api/raffles/{id}/draw` - Realizar sorteio
- `POST /api/raffles/{id}/cancel` - Cancelar rifa

---

## 🏗️ Arquitetura do Frontend Existente

### Estrutura de Pastas Atual (Goals como Referência)

```
src/
├── views/
│   ├── Goals/
│   │   ├── GoalsList.vue           # Lista paginada de metas
│   │   ├── GoalDetail.vue          # Detalhes de uma meta
│   │   └── GoalAdmin.vue           # Painel admin de metas
│   ├── Donations/
│   │   └── DonationsList.vue
│   └── Home.vue
├── components/
│   ├── Goals/
│   │   ├── GoalCard.vue            # Card de meta individual
│   │   ├── GoalForm.vue            # Formulário criar/editar
│   │   ├── GoalProgress.vue        # Barra de progresso
│   │   └── ImageUpload.vue         # Upload de imagem
│   ├── Common/
│   │   ├── Pagination.vue          # Componente de paginação reutilizável
│   │   ├── LoadingSpinner.vue
│   │   └── PixQRCode.vue           # Exibe QR Code PIX
│   └── Layout/
│       ├── Navbar.vue
│       └── Footer.vue
├── services/
│   ├── api.ts                      # Configuração Axios
│   ├── goalService.ts              # Chamadas API de metas
│   ├── donationService.ts
│   └── authService.ts              # Firebase Auth
├── stores/
│   ├── authStore.ts                # Pinia store (usuário logado)
│   ├── goalStore.ts                # Estado global de metas
│   └── cartStore.ts
├── types/
│   ├── Goal.ts                     # Interfaces TypeScript
│   ├── Donation.ts
│   └── User.ts
├── utils/
│   ├── formatters.ts               # Formatação de moeda, datas
│   ├── validators.ts               # Validação de CPF, email
│   └── constants.ts
└── router/
    └── index.ts                    # Vue Router config
```

### Padrões de Código Existentes

#### 1. **Services (Exemplo: goalService.ts)**

```typescript
import api from "./api";
import type { Goal, CreateGoalRequest, GoalPageResponse } from "@/types/Goal";

export const goalService = {
  // Listar metas com paginação
  async listGoals(params: {
    page?: number;
    pageSize?: number;
    status?: string;
    activeOnly?: boolean;
    sortBy?: string;
    sortDirection?: string;
  }): Promise<GoalPageResponse> {
    const response = await api.get("/goals", { params });
    return response.data;
  },

  // Buscar meta por ID
  async getGoalById(id: string): Promise<Goal> {
    const response = await api.get(`/goals/${id}`);
    return response.data;
  },

  // Criar meta (admin)
  async createGoal(data: CreateGoalRequest): Promise<Goal> {
    const response = await api.post("/goals", data);
    return response.data;
  },

  // Upload de imagem
  async uploadImage(goalId: string, file: File): Promise<{ imageUrl: string }> {
    const formData = new FormData();
    formData.append("file", file);
    const response = await api.post(`/goals/${goalId}/upload-image`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    return response.data;
  },
};
```

#### 2. **Types (Exemplo: Goal.ts)**

```typescript
export interface Goal {
  id: string;
  title: string;
  description: string;
  targetAmount: number;
  currentAmount: number;
  progress: number;
  startDate: string;
  endDate: string;
  status: "ACTIVE" | "PAUSED" | "COMPLETED" | "CANCELLED";
  imageUrl: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
}

export interface GoalPageResponse {
  content: Goal[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

export interface CreateGoalRequest {
  title: string;
  description: string;
  targetAmount: number;
  startDate: string;
  endDate: string;
}
```

#### 3. **Components (Exemplo: GoalCard.vue)**

```vue
<template>
  <div class="goal-card">
    <img v-if="goal.imageUrl" :src="goal.imageUrl" :alt="goal.title" />
    <div class="goal-content">
      <h3>{{ goal.title }}</h3>
      <p>{{ goal.description }}</p>

      <div class="progress-section">
        <GoalProgress
          :current="goal.currentAmount"
          :target="goal.targetAmount"
        />
        <p>
          {{ formatCurrency(goal.currentAmount) }} de
          {{ formatCurrency(goal.targetAmount) }}
        </p>
      </div>

      <div class="goal-actions">
        <button @click="$emit('donate', goal.id)">💖 Doar</button>
        <router-link :to="`/goals/${goal.id}`">Ver Detalhes</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Goal } from "@/types/Goal";
import GoalProgress from "./GoalProgress.vue";
import { formatCurrency } from "@/utils/formatters";

defineProps<{
  goal: Goal;
}>();

defineEmits<{
  donate: [goalId: string];
}>();
</script>
```

#### 4. **Views (Exemplo: GoalsList.vue)**

```vue
<template>
  <div class="goals-list-page">
    <h1>🎯 Metas Ativas</h1>

    <LoadingSpinner v-if="loading" />

    <div v-else class="goals-grid">
      <GoalCard
        v-for="goal in goals"
        :key="goal.id"
        :goal="goal"
        @donate="handleDonate"
      />
    </div>

    <Pagination
      v-if="totalPages > 1"
      :current-page="currentPage"
      :total-pages="totalPages"
      @page-change="loadGoals"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { goalService } from "@/services/goalService";
import type { Goal } from "@/types/Goal";
import GoalCard from "@/components/Goals/GoalCard.vue";
import Pagination from "@/components/Common/Pagination.vue";
import LoadingSpinner from "@/components/Common/LoadingSpinner.vue";

const goals = ref<Goal[]>([]);
const loading = ref(false);
const currentPage = ref(0);
const totalPages = ref(0);

const loadGoals = async (page = 0) => {
  loading.value = true;
  try {
    const response = await goalService.listGoals({
      page,
      pageSize: 20,
      activeOnly: true,
      sortBy: "createdAt",
      sortDirection: "DESC",
    });
    goals.value = response.content;
    currentPage.value = response.currentPage;
    totalPages.value = response.totalPages;
  } catch (error) {
    console.error("Erro ao carregar metas:", error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => loadGoals());
</script>
```

#### 5. **API Configuration (api.ts)**

```typescript
import axios from "axios";
import { useAuthStore } from "@/stores/authStore";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
  headers: {
    "Content-Type": "application/json",
  },
});

// Interceptor para adicionar token de autenticação
api.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore();
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor para tratamento de erros
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Redirecionar para login
      const authStore = useAuthStore();
      authStore.logout();
    }
    return Promise.reject(error);
  }
);

export default api;
```

---

## 🎰 Implementação Solicitada - Módulo de Rifas

### 📝 Tarefas Detalhadas

#### **PARTE 1: Estrutura Base**

**1.1 Criar Types (types/Raffle.ts)**

```typescript
export enum RaffleStatus {
  ACTIVE = "ACTIVE",
  ENDED = "ENDED",
  DRAWN = "DRAWN",
  CANCELLED = "CANCELLED",
}

export interface Raffle {
  id: string;
  title: string;
  description: string;
  imageUrls: string[]; // Até 10 imagens
  ticketPrice: number;
  totalTickets: number;
  soldTickets: number;
  availableTickets: number;
  status: RaffleStatus;
  drawDate: string; // ISO-8601
  expiresAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface RaffleTicket {
  id: string;
  raffleId: string;
  ticketNumber: number;
  buyerName: string;
  buyerEmail: string;
  buyerPhone: string;
  status: "RESERVED" | "PAID" | "EXPIRED" | "REFUNDED";
  purchasedAt: string;
}

export interface TicketPurchaseRequest {
  quantity: number;
  ticketNumbers?: number[]; // Opcional: usuário escolhe ou sistema aloca
  buyerName: string;
  buyerEmail: string;
  buyerPhone: string;
  buyerDocument: string; // CPF
}

export interface TicketPurchaseResponse {
  success: boolean;
  tickets: RaffleTicket[];
  payment: {
    id: string;
    qrCode: string;
    qrCodeImage: string;
    expiresAt: string;
    amount: number;
    status: string;
  };
  reservationExpiresAt: string;
}

export interface RafflePageResponse {
  content: Raffle[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

export interface DrawVerificationResponse {
  raffleId: string;
  committedEntropyHash: string;
  revealedEntropy: string;
  winnerTicketNumber: number;
  verificationPassed: boolean;
  message: string;
}

export interface CreateRaffleRequest {
  title: string;
  description: string;
  ticketPrice: number;
  totalTickets: number;
  drawDate: string;
  expiresAt?: string;
}
```

**1.2 Criar Service (services/raffleService.ts)**

```typescript
import api from "./api";
import type {
  Raffle,
  RafflePageResponse,
  RaffleTicket,
  TicketPurchaseRequest,
  TicketPurchaseResponse,
  DrawVerificationResponse,
  CreateRaffleRequest,
} from "@/types/Raffle";

export const raffleService = {
  // ============ ENDPOINTS PÚBLICOS ============

  // Listar rifas
  async listRaffles(params: {
    page?: number;
    pageSize?: number;
    status?: string;
    sortBy?: string;
    sortDirection?: string;
  }): Promise<RafflePageResponse> {
    const response = await api.get("/raffles", { params });
    return response.data;
  },

  // Buscar rifa por ID
  async getRaffleById(id: string): Promise<Raffle> {
    const response = await api.get(`/raffles/${id}`);
    return response.data;
  },

  // Listar tickets de uma rifa
  async getRaffleTickets(
    raffleId: string,
    status?: string
  ): Promise<RaffleTicket[]> {
    const params = status ? { status } : {};
    const response = await api.get(`/raffles/${raffleId}/tickets`, { params });
    return response.data;
  },

  // Buscar números disponíveis
  async getAvailableTickets(raffleId: string): Promise<{
    raffleId: string;
    totalTickets: number;
    availableTickets: number;
    availableNumbers: number[];
  }> {
    const response = await api.get(`/raffles/${raffleId}/tickets/available`);
    return response.data;
  },

  // Comprar bilhetes (gera PIX)
  async purchaseTickets(
    raffleId: string,
    data: TicketPurchaseRequest
  ): Promise<TicketPurchaseResponse> {
    const response = await api.post(`/raffles/${raffleId}/purchase`, data);
    return response.data;
  },

  // Verificar sorteio (transparência)
  async verifyDraw(raffleId: string): Promise<DrawVerificationResponse> {
    const response = await api.get(`/raffles/${raffleId}/verify-draw`);
    return response.data;
  },

  // ============ ENDPOINTS ADMIN ============

  // Criar rifa
  async createRaffle(data: CreateRaffleRequest): Promise<Raffle> {
    const response = await api.post("/raffles", data);
    return response.data;
  },

  // Atualizar rifa
  async updateRaffle(
    id: string,
    data: Partial<CreateRaffleRequest>
  ): Promise<Raffle> {
    const response = await api.put(`/raffles/${id}`, data);
    return response.data;
  },

  // Upload de múltiplas imagens (até 10)
  async uploadImages(
    raffleId: string,
    files: File[]
  ): Promise<{ uploadedImages: string[] }> {
    const formData = new FormData();
    files.forEach((file) => formData.append("files", file));

    const response = await api.post(
      `/raffles/${raffleId}/upload-images`,
      formData,
      {
        headers: { "Content-Type": "multipart/form-data" },
      }
    );
    return response.data;
  },

  // Deletar imagem específica
  async deleteImage(raffleId: string, imageIndex: number): Promise<void> {
    await api.delete(`/raffles/${raffleId}/images/${imageIndex}`);
  },

  // Realizar sorteio (admin revela entropia)
  async drawRaffle(
    raffleId: string,
    revealEntropy: string
  ): Promise<{
    raffleId: string;
    winnerTicketNumber: number;
    winnerName: string;
    winnerEmail: string;
    winnerPhone: string;
    drawnAt: string;
    isVerified: boolean;
  }> {
    const response = await api.post(`/raffles/${raffleId}/draw`, {
      revealEntropy,
    });
    return response.data;
  },

  // Cancelar rifa
  async cancelRaffle(raffleId: string): Promise<void> {
    await api.post(`/raffles/${raffleId}/cancel`, {});
  },

  // Deletar rifa
  async deleteRaffle(raffleId: string): Promise<void> {
    await api.delete(`/raffles/${raffleId}`);
  },
};
```

---

#### **PARTE 2: Componentes**

**2.1 RaffleCard.vue** (similar a GoalCard.vue)

- Exibir imagem principal (primeira do array)
- Título, descrição resumida
- Preço do bilhete
- Barra de progresso de vendas (soldTickets / totalTickets)
- Status da rifa (badge)
- Botão "Comprar Bilhetes" ou "Ver Detalhes"

**2.2 RaffleImageGallery.vue**

- Carousel/Gallery para múltiplas imagens
- Thumbnails clicáveis
- Navegação (setas esquerda/direita)
- Lightbox ao clicar (opcional)

**2.3 TicketSelector.vue**

- Escolher quantidade de bilhetes
- Opção de escolher números manualmente OU deixar sistema alocar
- Grid de números disponíveis (estilo loteria)
- Validação: não permitir números já vendidos
- Resumo: quantidade × preço = total

**2.4 PixPaymentModal.vue** (reutilizar ou adaptar de Donations)

- Exibir QR Code gerado
- Copiar código PIX
- Countdown de expiração
- Polling de status do pagamento (a cada 5 segundos)
- Feedback visual quando pago

**2.5 RaffleForm.vue** (Admin - Criar/Editar)

- Campos: título, descrição, preço, quantidade total, data do sorteio, data de expiração
- Validações
- Preview de dados antes de salvar

**2.6 RaffleImageUploader.vue** (Admin)

- Drag & Drop ou File Input
- Suportar múltiplas imagens (até 10)
- Preview das imagens antes do upload
- Botão de deletar cada imagem
- Progress bar do upload

**2.7 DrawVerificationPanel.vue**

- Exibir dados do sorteio
- Número vencedor
- Hash commitado vs entropia revelada
- Cálculo reproduzível (SHA-256)
- Status de verificação (✅ ou ❌)

---

#### **PARTE 3: Views**

**3.1 RafflesList.vue** (Página Pública)

- Grid de RaffleCard
- Filtros: status (ACTIVE, ENDED, DRAWN)
- Paginação
- Loading states
- Empty state ("Nenhuma rifa ativa no momento")

**3.2 RaffleDetail.vue** (Página Pública)

- RaffleImageGallery
- Informações completas
- TicketSelector
- Botão "Comprar Bilhetes"
- Lista de bilhetes vendidos (opcional: últimos 10)
- Seção "Verificar Sorteio" (se status = DRAWN)

**3.3 RaffleAdmin.vue** (Painel Admin)

- Tabs:
  - **Rifas Ativas**: Lista com ações rápidas
  - **Criar Nova Rifa**: RaffleForm
  - **Histórico**: Rifas finalizadas/canceladas
- Ações por rifa:
  - Editar
  - Upload de imagens
  - Realizar Sorteio (se ENDED)
  - Cancelar
  - Deletar

**3.4 RaffleCheckout.vue**

- Resumo da compra
- Formulário de dados do comprador
- TicketSelector
- Botão "Gerar PIX"
- PixPaymentModal

---

#### **PARTE 4: Fluxo de Compra de Bilhetes**

**Sequência:**

1. Usuário acessa `RaffleDetail.vue`
2. Seleciona quantidade de bilhetes via `TicketSelector`
3. Preenche dados (nome, email, telefone, CPF)
4. Clica em "Comprar Bilhetes"
5. Frontend chama `raffleService.purchaseTickets()`
6. Backend:
   - Valida disponibilidade
   - Gera carga PIX no PagBank
   - Reserva bilhetes (status = RESERVED)
   - Retorna QR Code + dados do PIX
7. Frontend exibe `PixPaymentModal` com QR Code
8. Inicia **polling** (a cada 5 segundos):
   ```typescript
   const pollPaymentStatus = async (raffleId: string, paymentId: string) => {
     const interval = setInterval(async () => {
       const tickets = await raffleService.getRaffleTickets(raffleId);
       const paidTickets = tickets.filter(
         (t) => t.status === "PAID" && t.purchasedAt === paymentId
       );

       if (paidTickets.length > 0) {
         clearInterval(interval);
         showSuccessMessage("Pagamento confirmado! Bilhetes confirmados.");
       }
     }, 5000);
   };
   ```
9. Quando webhook do PagBank confirmar pagamento:
   - Backend atualiza bilhetes para `status = PAID`
   - Polling detecta mudança
   - Frontend exibe mensagem de sucesso

---

#### **PARTE 5: Sistema de Sorteio Verificável**

**Para Administradores:**

1. Quando rifa expira (status = ENDED), admin acessa `RaffleAdmin.vue`
2. Clica em "Realizar Sorteio"
3. Backend retorna `revealEntropy` (que estava oculto)
4. Frontend exibe modal confirmando:
   - Número total de bilhetes
   - Entropia revelada
   - Botão "Confirmar Sorteio"
5. Chama `raffleService.drawRaffle(raffleId, revealEntropy)`
6. Backend:
   - Verifica se SHA-256(revealEntropy) == committedEntropy
   - Calcula vencedor deterministicamente
   - Retorna número vencedor + dados do comprador
7. Frontend exibe vencedor

**Para Público (Verificação):**

1. Qualquer pessoa acessa `RaffleDetail.vue` de uma rifa sorteada
2. Seção "Verificar Sorteio" visível
3. Exibe `DrawVerificationPanel`
4. Chama `raffleService.verifyDraw(raffleId)`
5. Exibe:
   - Hash commitado (no momento da criação)
   - Entropia revelada (no momento do sorteio)
   - Cálculo do vencedor
   - Status: ✅ Verificado ou ❌ Falha

**Opcional: Cálculo Local (Transparência Total)**

```typescript
import { sha256 } from "crypto-hash"; // ou biblioteca similar

async function verifyDrawLocally(
  revealedEntropy: string,
  committedHash: string,
  raffleId: string,
  totalTickets: number
): Promise<{ isValid: boolean; calculatedWinner: number }> {
  // 1. Verificar hash
  const calculatedHash = await sha256(revealedEntropy);
  const isValid = calculatedHash === committedHash;

  // 2. Recalcular vencedor
  const seed = revealedEntropy + raffleId + totalTickets;
  const winnerHash = await sha256(seed);
  const calculatedWinner =
    (parseInt(winnerHash.substring(0, 8), 16) % totalTickets) + 1;

  return { isValid, calculatedWinner };
}
```

---

#### **PARTE 6: Rotas (router/index.ts)**

```typescript
import RafflesList from "@/views/Raffles/RafflesList.vue";
import RaffleDetail from "@/views/Raffles/RaffleDetail.vue";
import RaffleAdmin from "@/views/Raffles/RaffleAdmin.vue";
import { useAuthStore } from "@/stores/authStore";

const routes = [
  // ... rotas existentes

  // RIFAS PÚBLICAS
  {
    path: "/raffles",
    name: "RafflesList",
    component: RafflesList,
    meta: { title: "Rifas Ativas" },
  },
  {
    path: "/raffles/:id",
    name: "RaffleDetail",
    component: RaffleDetail,
    props: true,
    meta: { title: "Detalhes da Rifa" },
  },

  // RIFAS ADMIN (protegido)
  {
    path: "/admin/raffles",
    name: "RaffleAdmin",
    component: RaffleAdmin,
    meta: {
      requiresAuth: true,
      requiresAdmin: true,
      title: "Gerenciar Rifas",
    },
    beforeEnter: (to, from, next) => {
      const authStore = useAuthStore();
      if (!authStore.isAdmin) {
        next({ name: "Home" });
      } else {
        next();
      }
    },
  },
];
```

---

#### **PARTE 7: Store (stores/raffleStore.ts)** [Opcional - se usar Pinia]

```typescript
import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { raffleService } from "@/services/raffleService";
import type { Raffle } from "@/types/Raffle";

export const useRaffleStore = defineStore("raffle", () => {
  const raffles = ref<Raffle[]>([]);
  const currentRaffle = ref<Raffle | null>(null);
  const loading = ref(false);

  const activeRaffles = computed(() =>
    raffles.value.filter((r) => r.status === "ACTIVE")
  );

  async function fetchRaffles(params = {}) {
    loading.value = true;
    try {
      const response = await raffleService.listRaffles(params);
      raffles.value = response.content;
      return response;
    } finally {
      loading.value = false;
    }
  }

  async function fetchRaffleById(id: string) {
    loading.value = true;
    try {
      currentRaffle.value = await raffleService.getRaffleById(id);
      return currentRaffle.value;
    } finally {
      loading.value = false;
    }
  }

  return {
    raffles,
    currentRaffle,
    loading,
    activeRaffles,
    fetchRaffles,
    fetchRaffleById,
  };
});
```

---

## 🎨 Requisitos de UI/UX

### Design Patterns (seguir padrões existentes de Goals)

1. **Cards de Rifa:**

   - Imagem em destaque
   - Badge de status (colorido: ACTIVE=verde, ENDED=amarelo, DRAWN=azul, CANCELLED=vermelho)
   - Progresso visual de vendas
   - Preço destacado
   - CTA claro ("Comprar Bilhetes")

2. **Página de Detalhes:**

   - Hero section com gallery de imagens
   - Sidebar com:
     - Resumo (preço, total de bilhetes, vendidos)
     - TicketSelector
     - Botão de compra
   - Tabs:
     - Informações
     - Bilhetes Vendidos
     - Verificação (se sorteado)

3. **Feedback Visual:**

   - Loading spinners durante requisições
   - Mensagens de sucesso/erro (toast/alert)
   - Validação de formulários em tempo real
   - Skeleton loaders (placeholders)

4. **Responsividade:**
   - Mobile-first
   - Grid adaptativo
   - Modais fullscreen em mobile

---

## ✅ Checklist de Implementação

### Fase 1: Estrutura Base

- [ ] Criar `types/Raffle.ts` com todas as interfaces
- [ ] Criar `services/raffleService.ts` com todos os métodos
- [ ] Configurar rotas em `router/index.ts`
- [ ] (Opcional) Criar `stores/raffleStore.ts`

### Fase 2: Componentes Básicos

- [ ] `RaffleCard.vue` - Card de rifa
- [ ] `RaffleImageGallery.vue` - Gallery de imagens
- [ ] `TicketSelector.vue` - Seletor de bilhetes
- [ ] `RaffleProgress.vue` - Barra de progresso de vendas

### Fase 3: Views Públicas

- [ ] `RafflesList.vue` - Listagem com paginação
- [ ] `RaffleDetail.vue` - Detalhes completos
- [ ] Implementar fluxo de compra de bilhetes
- [ ] Integrar `PixPaymentModal` (adaptar de Donations)
- [ ] Implementar polling de status de pagamento

### Fase 4: Painel Administrativo

- [ ] `RaffleForm.vue` - Formulário criar/editar
- [ ] `RaffleImageUploader.vue` - Upload de múltiplas imagens
- [ ] `RaffleAdmin.vue` - Dashboard admin
- [ ] Implementar ações: criar, editar, deletar, cancelar

### Fase 5: Sistema de Sorteio

- [ ] `DrawVerificationPanel.vue` - Painel de verificação
- [ ] Implementar modal de realização de sorteio (admin)
- [ ] Implementar verificação pública
- [ ] (Opcional) Cálculo local SHA-256 para transparência total

### Fase 6: Polimento

- [ ] Validações de formulários
- [ ] Mensagens de erro amigáveis
- [ ] Loading states em todos os componentes
- [ ] Responsividade mobile
- [ ] Testes manuais de todos os fluxos

---

## 🚀 Exemplo de Fluxo Completo

### Usuário Comprando Bilhetes:

1. Acessa `/raffles`
2. Vê lista de rifas ativas
3. Clica em uma rifa → `/raffles/rifa-123`
4. Visualiza imagens, descrição, preço
5. Seleciona 3 bilhetes no `TicketSelector`
6. Escolhe números manualmente: 7, 13, 42
7. Preenche dados: Nome, Email, Telefone, CPF
8. Clica em "Comprar Bilhetes"
9. Sistema valida e exibe modal com QR Code PIX
10. Usuário paga via app bancário
11. Após ~10 segundos, polling detecta pagamento confirmado
12. Modal exibe "✅ Pagamento confirmado! Seus bilhetes: 7, 13, 42"
13. Usuário recebe email de confirmação (backend)

### Admin Realizando Sorteio:

1. Acessa `/admin/raffles`
2. Vê rifa com status ENDED
3. Clica em "Realizar Sorteio"
4. Sistema carrega entropia revelada (armazenada no backend)
5. Modal exibe:
   - "Tem certeza? Esta ação é irreversível."
   - Entropia: `a3f2b8c4...`
   - Total de bilhetes: 100
6. Confirma
7. Backend calcula vencedor deterministicamente
8. Sistema exibe: "🎉 Vencedor: Bilhete #42 - João Silva"
9. Status da rifa muda para DRAWN
10. Frontend público agora mostra vencedor e permite verificação

---

## 📖 Recursos e Referências

### Documentação da API

- **Principal:** `backend/RAFFLES-API-GUIDE.md` ⭐
- Complementar: `backend/FRONTEND-INTEGRATION.md`
- Complementar: `backend/GOALS-API-GUIDE.md` (referência de padrões)

### Bibliotecas Recomendadas (Instalação Opcional)

```bash
# QR Code
npm install qrcode-vue3

# Formatação de moeda
npm install @maskito/vue

# Carousel de imagens
npm install swiper

# Criptografia (SHA-256)
npm install crypto-hash

# Validação de CPF
npm install @fnando/cpf
```

### Exemplos de Código Úteis

**Formatação de Moeda:**

```typescript
export const formatCurrency = (value: number): string => {
  return new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
  }).format(value);
};
```

**Validação de CPF:**

```typescript
import { isValid } from "@fnando/cpf";

export const validateCPF = (cpf: string): boolean => {
  return isValid(cpf.replace(/\D/g, ""));
};
```

**Formatação de Data:**

```typescript
export const formatDate = (isoDate: string): string => {
  return new Date(isoDate).toLocaleDateString("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};
```

---

## 🎯 Prioridades de Implementação

### Alta Prioridade (MVP):

1. ✅ Listagem de rifas
2. ✅ Detalhes de rifa
3. ✅ Compra de bilhetes com PIX
4. ✅ Criação de rifa (admin)
5. ✅ Upload de imagens (admin)

### Média Prioridade:

6. ✅ Realização de sorteio (admin)
7. ✅ Verificação pública de sorteio
8. ✅ Edição de rifas (admin)
9. ✅ Cancelamento de rifas (admin)

### Baixa Prioridade (Nice to Have):

10. ✅ Dashboard com estatísticas (admin)
11. ✅ Histórico de rifas finalizadas
12. ✅ Filtros avançados na listagem
13. ✅ Exportação de dados (Excel/PDF)
14. ✅ Notificações push quando rifa próxima do fim

---

## 🛡️ Segurança e Validações

### Frontend Validations:

- ✅ CPF válido (regex + algoritmo de validação)
- ✅ Email válido (regex)
- ✅ Telefone brasileiro (formato: +55 XX XXXXX-XXXX)
- ✅ Quantidade de bilhetes > 0 e <= disponíveis
- ✅ Números escolhidos dentro do range (1 a totalTickets)
- ✅ Formulários obrigatórios preenchidos

### Backend Validations (já implementadas):

- ✅ Validação de valor pago vs quantidade × preço
- ✅ Verificação de disponibilidade de bilhetes
- ✅ Autenticação admin via Firebase Token
- ✅ Validação de entropia no sorteio
- ✅ Prevenção de double-spending (tickets já vendidos)

---

## 📞 Suporte e Dúvidas

Se encontrar problemas:

1. **Consulte a documentação da API**: `RAFFLES-API-GUIDE.md`
2. **Teste endpoints com curl/Postman**: Exemplos na documentação
3. **Verifique logs do backend**: Console do Spring Boot
4. **Verifique Network tab**: Chrome DevTools
5. **Consulte logs do Firebase**: Console do Firebase Auth

**Lembre-se**: A API está **100% funcional e testada**. Se algo não funcionar, o problema está no frontend (chamada incorreta, headers faltando, etc).

---

## 🎉 Resultado Esperado

Ao final da implementação, você terá:

✅ **Módulo completo de Rifas** funcionando  
✅ **Sistema de compra de bilhetes com PIX** integrado  
✅ **Sorteio criptograficamente verificável** (transparência total)  
✅ **Painel administrativo** para gerenciar rifas  
✅ **Interface responsiva e intuitiva** para usuários  
✅ **Integração perfeita** com arquitetura existente do Tyler

**Boa implementação! 🚀🎰**
