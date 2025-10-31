# 🚀 Tyler API - Setup IntelliJ IDEA

## 📋 Pré-requisitos

- ✅ **IntelliJ IDEA 2023.1+** (Community ou Ultimate)
- ✅ **Java 17+** instalado
- ✅ **Maven** (integrado no IntelliJ)
- ✅ **Kotlin Plugin** (vem por padrão no IntelliJ)

## 🔧 Passo a Passo - Setup Completo

### 1️⃣ **Abrir o Projeto**

1. **Abra o IntelliJ IDEA**
2. **File** → **Open**
3. **Navegue até:** `d:\Projetos\Tyler\backend`
4. **Selecione o arquivo:** `pom.xml`
5. **Clique:** "Open as Project"
6. **Aguarde** o Maven sincronizar (pode demorar alguns minutos)

### 2️⃣ **Configurar SDK**

1. **File** → **Project Structure** (Ctrl+Alt+Shift+S)
2. **Project Settings** → **Project**
3. **Project SDK:** Selecione Java 17 (se não aparecer, clique "Add SDK" → "Download JDK")
4. **Project language level:** 17
5. **Clique:** OK

### 3️⃣ **Configurar Maven**

1. **File** → **Settings** (Ctrl+Alt+S)
2. **Build, Execution, Deployment** → **Build Tools** → **Maven**
3. **Verificar se está assim:**
   - Maven home path: Use bundled (Maven 3)
   - User settings file: Default
   - Local repository: Default
4. **Apply** → **OK**

### 4️⃣ **Configurar Variáveis de Ambiente**

1. **Copie o arquivo `.env.example` para `.env`:**

   ```bash
   cp .env.example .env
   ```

2. **Edite o arquivo `.env`:**

   ```env
   # Firebase
   FIREBASE_PROJECT_ID=projeto-tyler

   # Payment Provider
   PAYMENT_PROVIDER=pagarme

   # Pagarme (para teste real)
   PAGARME_API_KEY=ak_test_SUA_CHAVE_AQUI
   PAGARME_WEBHOOK_SECRET=seu_webhook_secret

   # Servidor
   PORT=8080
   PUBLIC_SITE_URL=http://localhost:5173
   ```

### 5️⃣ **Configurar Run Configuration**

1. **Método 1: Via Maven (Recomendado)**

   - **View** → **Tool Windows** → **Maven**
   - **Expanda:** tyler-api → Plugins → exec
   - **Clique duplo em:** `exec:java`

2. **Método 2: Via Run Configuration**

   - **Run** → **Edit Configurations**
   - **+** → **Application**
   - **Name:** Tyler API
   - **Main class:** `com.tylerproject.MainKt`
   - **Working directory:** `d:\Projetos\Tyler\backend`
   - **Use classpath of module:** tyler-api
   - **Apply** → **OK**

3. **Método 3: Via Maven Run Configuration**
   - **Run** → **Edit Configurations**
   - **+** → **Maven**
   - **Name:** Tyler API Maven
   - **Working directory:** `d:\Projetos\Tyler\backend`
   - **Command line:** `exec:java`
   - **Apply** → **OK**

## ▶️ Como Executar

### 🟢 **Opção 1: Maven Tool Window (Mais Fácil)**

1. **View** → **Tool Windows** → **Maven**
2. **tyler-api** → **Plugins** → **exec** → **Duplo clique em `exec:java`**

### 🟡 **Opção 2: Via Terminal IntelliJ**

1. **View** → **Tool Windows** → **Terminal**
2. **Digite:** `mvn exec:java`

### 🔵 **Opção 3: Run Configuration**

1. **Selecione** a configuração "Tyler API" no dropdown
2. **Clique** no botão ▶️ Run

## 🧪 Testando a API

Após executar, você verá:

```
🚀 Iniciando Tyler API (Maven + PIX)
====================================
✅ Firebase inicializado
✅ Tyler API iniciada com sucesso!
🌐 Servidor rodando em: http://localhost:8080
💳 Provider ativo: PAGARME
🔑 Pagarme API Key: ✅ Configurada (ou ❌ Não configurada)
🔥 Firebase conectado ao projeto: projeto-tyler
```

**Teste rapidamente:**

- **Abra:** http://localhost:8080
- **Execute:** `test-pagarme.bat` (Windows)

## 🔧 Debugging

### **Para Debug:**

1. **Clique** no ícone 🐛 (Debug) ao invés do ▶️ (Run)
2. **Defina breakpoints** clicando na margem esquerda do código
3. **Execute requisições** e o código pausará nos breakpoints

### **Hot Reload:**

- **IntelliJ** detecta mudanças automaticamente
- **Para aplicar:** Recompile com **Ctrl+F9**
- **Para restart completo:** Pare (Ctrl+F2) e execute novamente

## 📁 Estrutura do Projeto

```
backend/
├── pom.xml                 # Configuração Maven
├── .env                    # Variáveis de ambiente (criar)
├── .env.example           # Exemplo de configuração
├── src/main/kotlin/com/tylerproject/
│   ├── Main.kt            # Classe principal
│   └── providers/
│       └── PagarmeProvider.kt  # Integração Pagarme
├── test-pagarme.bat       # Script de teste
└── test-pix.html         # Interface de teste
```

## 🐛 Problemas Comuns

### **Erro: "Cannot resolve symbol 'Main'"**

- **Solução:** File → Invalidate Caches → Invalidate and Restart

### **Erro: "Module not specified"**

- **Solução:** File → Project Structure → Modules → Verificar se tyler-api está presente

### **Erro: "Cannot find or load main class"**

- **Solução:** Build → Rebuild Project

### **Erro Maven: "Plugin execution not covered"**

- **Solução:** Ignore ou instale "Maven Integration Extension"

## 🎯 Plugins Recomendados

1. **Kotlin** (já incluído)
2. **Maven Helper**
3. **Rainbow Brackets**
4. **GitToolBox**
5. **Database Navigator** (para futuras integrações)

## 🚀 Próximos Passos

Depois que estiver rodando:

1. **Configure** sua API Key do Pagarme real
2. **Teste** a criação de PIX
3. **Integre** com seu frontend
4. **Configure** webhooks para produção

---

## 📞 Suporte

Se tiver problemas:

1. **Verifique** se Java 17+ está instalado
2. **Reimporte** o projeto Maven
3. **Limpe** o cache do IntelliJ
4. **Execute** `mvn clean compile` no terminal

**✨ Agora você está pronto para desenvolver com a Tyler API no IntelliJ!**
