package com.example.fleetapp.data.repository

import android.content.Context
import com.example.fleetapp.data.DeviceMapper
import com.example.fleetapp.domain.model.Device
import com.example.fleetapp.domain.repository.DeviceRepository
import com.example.fleetapp.domain.repository.FavoriteDeviceRepository
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
data class DeviceDto(
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
    private val mapper: DeviceMapper,
    private val favoriteRepository: FavoriteDeviceRepository
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
                mapper.map(dto)
            }
            emit(parsedDevices)
        }

        return combine(rawDevicesFlow, favoriteRepository.favoritesFlow()) { devices, favSet ->
            devices.map { device ->
                device.copy(isFavorite = favSet.contains(device.id))
            }
        }
    }
}