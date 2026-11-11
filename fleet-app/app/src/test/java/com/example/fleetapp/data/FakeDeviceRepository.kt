package com.example.fleetapp.data

import com.example.fleetapp.domain.model.Device
import com.example.fleetapp.domain.model.DeviceStatus
import com.example.fleetapp.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeDeviceRepository(
    initialDevices: List<Device> = emptyList()
) : DeviceRepository {

    private val devicesFlow = MutableStateFlow(initialDevices)

    override fun getDevicesFlow(): Flow<List<Device>> = devicesFlow

    fun setDevices(devices: List<Device>) {
        devicesFlow.value = devices
    }
}