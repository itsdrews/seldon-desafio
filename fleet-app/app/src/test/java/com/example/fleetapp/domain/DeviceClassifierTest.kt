package com.example.fleetapp.domain

import com.example.fleetapp.domain.model.DeviceStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceClassifierTest {

    @Test
    fun `deve retornar CRITICO se a capacidade nominal da bateria for zerada`() {
        val (status, justifications) = DeviceClassifier.classify(
            batteryCapacityMah = 0,
            batteryLevelPercent = 50,
            screenOnMinutes = 100,
            hasZeroCapacityDefect = true
        )

        assertEquals(DeviceStatus.CRITICO, status)
        assertTrue(justifications.any { it.contains("Capacidade nominal da bateria zerada") })
    }

    @Test
    fun `deve retornar CRITICO se a bateria estiver abaixo de 15%`() {
        val (status, justifications) = DeviceClassifier.classify(
            batteryCapacityMah = 4000,
            batteryLevelPercent = 14,
            screenOnMinutes = 60
        )

        assertEquals(DeviceStatus.CRITICO, status)
        assertTrue(justifications.any { it.contains("14%") })
    }

    @Test
    fun `deve retornar ATENCAO nos limites exatos entre 15% e 30%`() {
        val (status1, _) = DeviceClassifier.classify(4000, 15, 60)
        val (status2, _) = DeviceClassifier.classify(4000, 30, 60)

        assertEquals(DeviceStatus.ATENCAO, status1)
        assertEquals(DeviceStatus.ATENCAO, status2)
    }

    @Test
    fun `deve retornar OK quando a bateria estiver acima de 30%`() {
        val (status, _) = DeviceClassifier.classify(4000, 31, 60)

        assertEquals(DeviceStatus.OK, status)
    }
}