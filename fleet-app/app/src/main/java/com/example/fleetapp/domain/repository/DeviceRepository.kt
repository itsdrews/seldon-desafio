package com.example.fleetapp.domain.repository

import com.example.fleetapp.domain.model.Device
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun getDevicesFlow(): Flow<List<Device>>
    suspend fun toggleFavorite(deviceId: String)
}