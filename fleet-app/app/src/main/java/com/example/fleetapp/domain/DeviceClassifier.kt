package com.example.fleetapp.domain

import com.example.fleetapp.domain.model.DeviceStatus

object DeviceClassifier {

    fun classify(
        batteryCapacityMah: Int,
        batteryLevelPercent: Int,
        screenOnMinutes: Int,
        hasZeroCapacityDefect: Boolean = false
    ): Pair<DeviceStatus, List<String>> {
        val criticalReasons = mutableListOf<String>()
        val warningReasons = mutableListOf<String>()

        if (hasZeroCapacityDefect || batteryCapacityMah <= 0) {
            criticalReasons.add("Erro crítico de telemetria: Capacidade nominal da bateria zerada/inválida (${batteryCapacityMah} mAh).")
        }

        if (batteryLevelPercent < 15) {
            criticalReasons.add("Nível de bateria restante (${batteryLevelPercent}%) está abaixo do limite de segurança (15%).")
        }

        if (criticalReasons.isNotEmpty()) {
            return DeviceStatus.CRITICO to criticalReasons
        }

        if (batteryLevelPercent in 15..30) {
            warningReasons.add("Nível de bateria em estado de atenção (${batteryLevelPercent}% na faixa de alerta 15%-30%).")
        }

        if (warningReasons.isNotEmpty()) {
            return DeviceStatus.ATENCAO to warningReasons
        }

        return DeviceStatus.OK to listOf("Métricas energéticas operacionais normais (Nível restante em ${batteryLevelPercent}%).")
    }
}