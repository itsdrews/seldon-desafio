package com.example.fleetapp.domain.model
data class Device(
    val id: String,
    val name: String,
    val batteryLevel: Int,
    val lastMaintenanceDaysAgo: Int,
    val temperatureCelsius: Double,
    val status: DeviceStatus,
    val justifications: List<String>,
    val isFavorite: Boolean = false
)