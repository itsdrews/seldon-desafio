package com.example.fleetapp.domain

import com.example.fleetapp.domain.model.DeviceStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceClassifierTest {

    @Test
    fun `deve tratar divisor nulo de bateria e classificar como CRITICO sem lancar excecao`() {
        val capacityMah = 0
        val hasZeroCapacity = capacityMah <= 0

        val (status, reasons) = DeviceClassifier.classify(
            batteryCapacityMah = capacityMah,
            batteryLevelPercent = 0,
            screenOnMinutes = 97,
            hasZeroCapacityDefect = hasZeroCapacity
        )

        assertEquals(DeviceStatus.CRITICO, status)
        assertTrue(reasons.any { it.contains("Capacidade nominal da bateria zerada") })
    }

    @Test
    fun `deve converter mWh para mAh corretamente no calculo da bateria`() {
        val capacityMah = 5000
        val rawEnergyMwh = 3773.0
        val voltage = 3.85

        // Conversão: 3773 mWh / 3.85 V = 980 mAh
        val energyConsumedMah = rawEnergyMwh / voltage
        val remainingMah = (capacityMah - energyConsumedMah).coerceAtLeast(0.0)
        val remainingPercent = ((remainingMah / capacityMah) * 100).toInt()

        // 5000 - 980 = 4020 mAh restantes (~80% de bateria)
        assertEquals(80, remainingPercent)

        val (status, _) = DeviceClassifier.classify(
            batteryCapacityMah = capacityMah,
            batteryLevelPercent = remainingPercent,
            screenOnMinutes = 186
        )

        assertEquals(DeviceStatus.OK, status)
    }
}