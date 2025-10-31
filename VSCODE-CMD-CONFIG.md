# 🖥️ Configurar VS Code - CMD como Terminal Padrão

## ⚙️ **Método 1: Via Settings UI**

1. **Abra Settings**: `Ctrl + ,`
2. **Procure**: "terminal integrated default profile"
3. **Selecione**: "Terminal › Integrated › Default Profile: Windows"
4. **Escolha**: "Command Prompt" ao invés de "PowerShell"

## ⚙️ **Método 2: Via settings.json**

1. **Abra Command Palette**: `Ctrl + Shift + P`
2. **Digite**: "Preferences: Open Settings (JSON)"
3. **Adicione**:

```json
{
  "terminal.integrated.defaultProfile.windows": "Command Prompt"
}
```

## ⚙️ **Método 3: Via Configuração Completa**

**settings.json completo**:

```json
{
  "terminal.integrated.defaultProfile.windows": "Command Prompt",
  "terminal.integrated.profiles.windows": {
    "Command Prompt": {
      "path": "cmd.exe",
      "args": [],
      "icon": "terminal-cmd"
    },
    "PowerShell": {
      "path": "powershell.exe",
      "args": ["-NoLogo"],
      "icon": "terminal-powershell"
    }
  }
}
```

## 🔄 **Aplicar Mudanças**

1. **Feche todos os terminais** no VS Code
2. **Reabra novo terminal**: `` Ctrl + ` ``
3. **Confirme**: Deve mostrar `C:\>` (CMD) ao invés de `PS>`

## 🎯 **Verificar**

**Terminal novo deve mostrar**:

```cmd
Microsoft Windows [Version XX.X.XXXXX.XXX]
(c) Microsoft Corporation. All rights reserved.

C:\Projetos\Tyler\backend>
```

## 🚀 **Comandos que funcionam melhor no CMD**

```cmd
REM Maven
mvn clean compile
mvn spring-boot:run

REM cURL (se instalado)
curl -X POST http://localhost:8080/api/payments/checkout -H "Content-Type: application/json" -d "{\"amount\": 1000}"

REM Navegação
cd "d:\Projetos\Tyler\backend"
dir
```

**Agora você pode usar comandos Unix-style no VS Code terminal! 🎉**
