# Monitoramento de Frota

Aplicativo Android para monitoramento de saúde de dispositivos de frota operacional, desenvolvido em **Clean / Hexagonal Architecture**, **Jetpack Compose**, **Kotlin Coroutines/Flow** e esteira de **CI/CD no GitHub Actions** com checagem automatizada de segurança.

![CI Status](https://github.com/itsdrews/seldon-desafio/actions/workflows/ci.yml/badge.svg)

---

## Capturas de Tela

| Lista de Frota (Dark Blue & Filtros) | Detalhe do Dispositivo & Diagnóstico |
| :---: | :---: |
| ![Lista de Frota](/screenshots/fleet_list.png) | ![Detalhes do Dispositivo](/screenshots/device_detail.png) |

---

## Diferenciais do Projeto

1. **Domínio 100% Puro (Hexagonal Architecture)**:
   - A camada `domain` não possui nenhuma dependência do Android SDK (`import android.*`), facilitando a execução de suítes de testes em milissegundos na JVM local.
2. **Esteira de CI Automatizada com Guardrails**:
   - Validação estática via script bash (`grep`) executado no GitHub Actions que falha a compilação caso algum import do Android seja inserido indevidamente no pacote `domain`.
3. **Trilha B de Segurança e Automação**:
   - Hardening do `AndroidManifest.xml` (`allowBackup="false"`, `usesCleartextTraffic="false"`).
   - Script de auditoria estática contra vazamento de identificadores únicos (`DEV-01`, `DEV-02`) em logs (`Log.*` / `println`).
   - Tratamento inteligente de falsos positivos via anotação/tag inline `// sec-ignore`.
4. **Interface Moderna em Jetpack Compose & Material 3**:
   - Tema escuro de alto contraste (*Slate & Dark Blue*) com cartões elevados (`ElevatedCard`), barras de progresso visuais e sombreamento com ponto de luz customizado (`.shadow`).
5. **Resiliência a Defectos de Dados**:
   - Conversão de unidades de energia ($mWh \to mAh$) e tratamento robusto de registros incompletos/zerados sem exceções em runtime ou geração de valores `NaN`.

---

## Registro de Uso de Assistentes de IA

Este projeto contou com a colaboração continuada de assistente de Inteligência Artificial (**Gemini / Google AI**) para:
- Structuring da arquitetura Hexagonal e desacoplamento do repositório (`DeviceRepository`).
- Escrita e otimização da suíte de testes unitários com `UnconfinedTestDispatcher`.
- Criação e depuração do script de auditoria de segurança em Shell Script (`scripts/security_check.sh`).
- Refatoração da interface gráfica para diretrizes modernizadas do Material 3 com sombras projetadas.

---

## Decisões de Arquitetura

```text
app/src/main/java/com/example/fleetapp/
├── domain/                  # REGRA DE NEGÓCIO PURA (Zero Android SDK)
│   ├── model/               # Entidades (Device, DeviceStatus)
│   ├── repository/          # Porta de Saída / Interface (DeviceRepository)
│   └── DeviceClassifier.kt  # Algoritmo de diagnóstico puro
├── data/                    # ADAPTADOR DE DADOS E PERSISTÊNCIA
│   ├── local/               # Jetpack DataStore (Favoritos)
│   ├── model/               # DTOs de JSON
│   └── FleetRepositoryImpl.kt # Normalização (mWh->mAh) e Parsing
└── ui/                      # ADAPTADOR DE APRESENTAÇÃO (Jetpack Compose)
    ├── FleetViewModel.kt    # Gerenciamento de Estado UI (StateFlow)
    └── FleetScreens.kt      # Composables Material 3 & Design Dark Blue
    └── FleetViewModelFactory.kt      # Factory para ViewModel

## Execução de Testes

```bash
# No Windows
.\gradlew.bat testDebugUnitTest

# No Linux / macOS
./gradlew testDebugUnitTest
```
## Auditoria de segurança

```bash
./scripts/security_check.sh
```

## Como executar o aplicativo
1. Abra o projeto no Android Studio (Ladybug / Jellyfish ou superior).

2. Sincronize o projeto com os arquivos Gradle (Sync Project with Gradle Files).

3. Inicie um emulador Android ou conecte um dispositivo físico via USB Debugging.

4. Execute o comando de instalação:

```bash
.\gradlew.bat installDebug
```

## O que faria em seguida: 
1. Barra de Pesquisa Dinâmica: Adicionar campo de busca textual no topo da lista para filtragem instantânea por nome ou ID do dispositivo.

2. Atualização em Tempo Real: Conectar a camada de dados a uma API de WebSockets/gRPC para streaming de dados telemáticos contínuos dos dispositivos.

3. Testes de UI Automatizados: Implementar suíte de testes de tela com ComposeTestRule em src/androidTest/ para simular cliques nos filtros e navegação de telas.


##  Discriminação do Uso de IA

O desenvolvimento deste projeto contou com a colaboração continuada do assistente de Inteligência Artificial (**Gemini / Google AI**). A atuação esteve estruturada da seguinte forma:

| Etapa do Projeto | Atuação da IA | Escopo e Contribuição Específica | Validação Humana / Supervisão |
| :--- | :--- | :--- | :--- |
| **1. Arquitetura & Domínio** | Estruturação de pacotes segundo a Arquitetura Hexagonal (Clean Architecture) e garantia da pureza da camada `domain` (zero dependências do SDK Android). | Verificação manual da ausência de `import android.*` no pacote de domínio. |
| **2. Tratamento de Defeitos** | Modelagem da conversão de unidades ($mWh \to mAh$) e implementação do *fallback* seguro para registros de bateria incompletos/zerados sem geração de `NaN`. | Validação das fórmulas de conversão e das justificativas textuais emitidas. |
| **3. Testes Unitários (JVM)** | Criação de casos de teste em `DeviceClassifierTest` cobrindo fronteiras exatas ($14\%, 15\%, 30\%, 31\%$) e mocks com `UnconfinedTestDispatcher` em `FleetViewModelTest`. |
| **4. Interface & Design System** | Redesign da UI com Jetpack Compose Material 3 (Dark Blue, `ElevatedCard`, `LinearProgressIndicator`, badges de status e sombras dinâmicas)
| **5. Segurança (Trilha B)** | Escrita do script `scripts/security_check.sh` em Shell Script para auditoria estática do Manifest e detecção de vazamento de IDs em logs com suporte a `// sec-ignore`. | Teste prático de falso positivo provocando falha proposital no pipeline e aplicando a supressão inline. |
| **6. CI/CD (GitHub Actions)** | Criação do workflow `.github/workflows/ci.yml`, depuração do isolamento da pasta `fleet-app/` (`working-directory`) e atualização para ações v5. | Inspeção dos logs de execução no GitHub Actions até obter a esteira 100% verde. |
| **7. Documentação Técnica** | Redação & Estruturação | Apresentação e formatação do `PROTOCOLO.md` e do `README.md` com instruções de execução, decisões arquiteturais e limitações de validade. 