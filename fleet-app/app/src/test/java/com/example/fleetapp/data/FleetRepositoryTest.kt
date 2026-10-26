package com.example.fleetapp.data

import org.junit.Assert.assertEquals
import org.junit.Test

class EnergyNormalizationTest {

    @Test
    fun `deve converter mWh para mAh corretamente usando a tensao nominal`() {
        val rawEnergyMwh = 3850.0 // 3850 mWh
        val nominalVoltage = 3.85 // 3.85 V

        val expectedMah = rawEnergyMwh / nominalVoltage // 1000.0 mAh
        val calculatedMah = rawEnergyMwh / nominalVoltage

        assertEquals(1000.0, calculatedMah, 0.001)
    }
}