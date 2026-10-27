# Protocolo de Decisões Arquiteturais e Diagnóstico de Frota

## 1. Regra Adotada no Domínio (`DeviceClassifier`)
A classificação de saúde dos dispositivos de frota é calculada pela classe de domínio pura `DeviceClassifier`, isolada de qualquer biblioteca do Android SDK (`import android.*`) ou leitores de JSON:

* **CRÍTICO**:
  * Nível de bateria restante $< 15\%$; **OU**
  * Presença de erro estrutural no dispositivo (ex: capacidade nominal da bateria igual a $0\text{ mAh}$ ou dados corrompidos/incompletos).
* **ATENÇÃO**:
  * Nível de bateria restante entre $15\%$ e $30\%$ (inclusive); **OU**
  * Tempo de tela ligada elevado com consumo proporcionalmente alto.
* **OK**:
  * Bateria restante $> 30\%$ com métricas operacionais normais.

---

## 2. Tratamento dos Dois Defectos de Dados

### Defeito 1: Heterogeneidade de Unidades de Energia (`mWh` vs `mAh`)
* **Problema**: O arquivo de dados original envia valores de consumo expressos em milivolt-hora (`mWh`), enquanto a capacidade da bateria é medida em miliampere-hora (`mAh`).
* **Tratamento**: A conversão é realizada na camada `data` (`FleetRepositoryImpl`) durante a fase de parsing/mapeamento, antes de enviar a entidade ao domínio:
  $$\text{Consumo (mAh)} = \frac{\text{Energia (mWh)}}{\text{Tensão Nominal (V)}}$$
* **Garantia**: O domínio recebe apenas valores normalizados em `mAh`, evitando erros de comparação.

### Defeito 2: Registros Incompletos / Capacidade Zerada
* **Problema**: Dispositivos com campos ausentes ou capacidade nominal zerada (`batteryCapacityMah == 0`) geravam exceções de divisão por zero (`ArithmeticException`) ou valores indefinidos (`NaN`).
* **Tratamento**: A camada `data` intercepta os DTOs com valores inconsistentes ou zerados e sinaliza a flag `hasZeroCapacityDefect = true`. O `DeviceClassifier` captura o estado, classifica preventivamente o dispositivo como **CRÍTICO** e injeta a justificativa textual explícita: *"Capacidade nominal da bateria zerada / dados de bateria incompletos"*.
* **Garantia**: A aplicação não sofre *crashes*, não propaga `NaN` e informa com clareza a falha de telemetria.

---

## 3. Ameaça à Validade

* **Limitação Identificada**: O diagnóstico é executado sobre um conjunto de dados estático (`assets/frota.json`) sem integração a sensores de hardware em tempo real via rede ou Bluetooth/IoT.
* **Impacto na Validade**: Em um cenário de frota real, flutuações de temperatura da bateria, desgaste químico (ciclos de carga) e perdas instantâneas de sinal de GPS alteram o comportamento da bateria de forma dinâmica. A dependência de um instantâneo estático impede a detecção de anomalias transitórias.
* **Mitigação Futura**: Inversão da porta de entrada do repositório para consumir dados via rotinas de segundo plano com WorkManager ou streaming via WebSockets/gRPC.