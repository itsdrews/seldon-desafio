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

        // Defeito crítico de hardware / leitura
        if (hasZeroCapacityDefect || batteryCapacityMah <= 0) {
            criticalReasons.add("Erro crítico de telemetria: Capacidade nominal da bateria zerada ou indefinida.")
        }

        if (batteryLevelPercent < 15) {
            criticalReasons.add("Bateria restante abaixo do limite seguro ($batteryLevelPercent% < 15%).")
        }

        if (criticalReasons.isNotEmpty()) {
            return DeviceStatus.CRITICO to criticalReasons
        }

        if (batteryLevelPercent in 15..30) {
            warningReasons.add("Nível de bateria em alerta ($batteryLevelPercent%).")
        }

        if (warningReasons.isNotEmpty()) {
            return DeviceStatus.ATENCAO to warningReasons
        }

        return DeviceStatus.OK to listOf("Métricas energéticas operacionais dentro do padrão normativo.")
    }
}