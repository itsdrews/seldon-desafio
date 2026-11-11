package com.example.fleetapp.data

import com.example.fleetapp.data.repository.DeviceDto
import com.example.fleetapp.domain.DeviceClassifier
import com.example.fleetapp.domain.model.Device
import com.example.fleetapp.domain.model.DeviceStatus

class DeviceMapper(private val classifier: DeviceClassifier) {

    fun map(dto: DeviceDto): Device? {
        val id = dto.id?.takeIf { it.isNotBlank() } ?: return null
        val displayName = dto.model?.takeIf { it.isNotBlank() } ?: "Dispositivo $id"

        val capacityMah = (dto.batteryCapacityMah ?: 0).coerceAtLeast(0)
        val rawEnergy = (dto.energyConsumed ?: 0.0).coerceAtLeast(0.0)
        val voltage = (dto.nominalVoltage ?: 3.85).takeIf { it > 0.0 } ?: 3.85
        val screenOn = (dto.screenOnMinutes ?: 0).coerceAtLeast(0)

        // Conversão de unidade de infraestrutura (mWh -> mAh)
        val energyConsumedMah = if (dto.energyUnit.equals("mWh", ignoreCase = true)) {
            rawEnergy / voltage
        } else {
            rawEnergy
        }

        // Cálculo da porcentagem restante
        val calculatedBatteryLevel = if (capacityMah <= 0) {
            0
        } else {
            dto.batteryLevelPct ?: run {
                val remainingMah = (capacityMah - energyConsumedMah).coerceAtLeast(0.0)
                ((remainingMah / capacityMah) * 100).toInt().coerceIn(0, 100)
            }
        }

        // Invocação da Regra de Negócio de Domínio
        val (status, justifications) = classifier.classify(
            batteryCapacityMah = capacityMah,
            batteryLevelPercent = calculatedBatteryLevel,
            screenOnMinutes = screenOn,
            hasZeroCapacityDefect = capacityMah <= 0
        )

        return Device(
            id = id,
            name = displayName,
            batteryLevel = calculatedBatteryLevel,
            batteryCapacityMah = capacityMah,
            energyConsumedMah = energyConsumedMah,
            screenOnMinutes = screenOn,
            status = status,
            justifications = justifications
        )
    }
}
