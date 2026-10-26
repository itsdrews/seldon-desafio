package com.example.fleetapp.data.repository

import android.content.Context
import com.example.fleetapp.data.local.FavoritesDataStore
import com.example.fleetapp.domain.DeviceClassifier
import com.example.fleetapp.domain.model.Device
import com.example.fleetapp.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class FleetResponseDto(
    val devices: List<DeviceDto>? = null
)

@Serializable
private data class DeviceDto(
    val id: String? = null,
    val model: String? = null,
    val batteryCapacityMah: Int? = null,
    val batteryLevelPct: Int? = null,
    val screenOnMinutes: Int? = null,
    val energyConsumed: Double? = null,
    val energyUnit: String? = null,
    val nominalVoltage: Double? = null
)

class FleetRepositoryImpl(
    private val context: Context,
    private val favoritesDataStore: FavoritesDataStore
) : DeviceRepository {

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    override fun getDevicesFlow(): Flow<List<Device>> {
        val rawDevicesFlow = flow {
            val jsonText = context.assets.open("frota.json").bufferedReader().use { it.readText() }
            val response = jsonParser.decodeFromString<FleetResponseDto>(jsonText)
            val dtoList = response.devices.orEmpty()

            val parsedDevices = dtoList.mapNotNull { dto ->
                val id = dto.id?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
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
                val (status, justifications) = DeviceClassifier.classify(
                    batteryCapacityMah = capacityMah,
                    batteryLevelPercent = calculatedBatteryLevel,
                    screenOnMinutes = screenOn,
                    hasZeroCapacityDefect = capacityMah <= 0
                )

                Device(
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
            emit(parsedDevices)
        }

        return combine(rawDevicesFlow, favoritesDataStore.favoritesFlow) { devices, favSet ->
            devices.map { device ->
                device.copy(isFavorite = favSet.contains(device.id))
            }
        }
    }

    override suspend fun toggleFavorite(deviceId: String) {
        favoritesDataStore.toggleFavorite(deviceId)
    }
}