package com.example.fleetapp.data.repository

import android.content.Context
import com.example.fleetapp.data.local.FavoritesDataStore
import com.example.fleetapp.domain.DeviceClassifier
import com.example.fleetapp.domain.model.Device
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// DTO do Objeto Raiz do JSON
@Serializable
private data class FleetResponseDto(
    val schemaVersion: String? = null,
    val exportedAt: String? = null,
    val fleetId: String? = null,
    val deviceCount: Int? = null,
    val devices: List<DeviceDto>? = null
)

// DTO de cada Dispositivo no JSON
@Serializable
private data class DeviceDto(
    val id: String? = null,
    val model: String? = null,
    val soc: String? = null,
    val tier: String? = null,
    val ramMb: Int? = null,
    val androidApi: Int? = null,
    val batteryCapacityMah: Int? = null,
    val batteryLevelPct: Int? = null,
    val screenOnMinutes: Int? = null,
    val energyConsumed: Double? = null,
    val energyUnit: String? = null,
    val nominalVoltage: Double? = null,
    val jankFramesPct: Double? = null,
    val crashCount7d: Int? = null,
    val lastSeen: String? = null
)

class FleetRepository(
    private val context: Context,
    private val favoritesDataStore: FavoritesDataStore
) {
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun getDevicesFlow(): Flow<List<Device>> {
        val rawDevicesFlow = flow {
            val jsonText = context.assets.open("frota.json").bufferedReader().use { it.readText() }
            
            // Decodifica o objeto raiz
            val response = jsonParser.decodeFromString<FleetResponseDto>(jsonText)
            val dtoList = response.devices.orEmpty()

            val parsedDevices = dtoList.mapNotNull { dto ->
                // Filtro: Descarte de registros sem ID válido
                val id = dto.id?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val displayName = dto.model?.takeIf { it.isNotBlank() } ?: "Dispositivo $id"

                // Metricas sanitizadas e defensivas
                val capacityMah = (dto.batteryCapacityMah ?: 0).coerceAtLeast(0)
                val rawEnergy = (dto.energyConsumed ?: 0.0).coerceAtLeast(0.0)
                val voltage = (dto.nominalVoltage ?: 3.85).takeIf { it > 0.0 } ?: 3.85

                // TRATAMENTO DEFEITO 1: Normalização de unidade (mWh -> mAh)
                val energyConsumedMah = if (dto.energyUnit.equals("mWh", ignoreCase = true)) {
                    rawEnergy / voltage
                } else {
                    rawEnergy
                }

                // TRATAMENTO DEFEITO 2: Proteção contra capacidade zerada / divisão por zero
                val calculatedBatteryLevel = if (capacityMah <= 0) {
                    0
                } else {
                    dto.batteryLevelPct ?: run {
                        val remainingMah = (capacityMah - energyConsumedMah).coerceAtLeast(0.0)
                        ((remainingMah / capacityMah) * 100).toInt().coerceIn(0, 100)
                    }
                }

                val (status, justifications) = DeviceClassifier.classify(
                    batteryCapacityMah = capacityMah,
                    batteryLevelPercent = calculatedBatteryLevel,
                    screenOnMinutes = dto.screenOnMinutes ?: 0,
                    hasZeroCapacityDefect = capacityMah <= 0
                )

                Device(
                    id = id,
                    name = "$displayName ($id)",
                    batteryLevel = calculatedBatteryLevel,
                    lastMaintenanceDaysAgo = 0,
                    temperatureCelsius = 25.0,
                    status = status,
                    justifications = justifications
                )
            }
            emit(parsedDevices)
        }

        return combine(rawDevicesFlow, favoritesDataStore.favoritesFlow) { devices, favSet ->
            devices.map { device ->
                device.copy(isFavorite = favSet.contains(device.id))
            }
        }
    }

    suspend fun toggleFavorite(deviceId: String) {
        favoritesDataStore.toggleFavorite(deviceId)
    }
}