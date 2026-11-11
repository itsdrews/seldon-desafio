package com.example.fleetapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavoriteDeviceRepository {
    suspend fun toggleFavorite(deviceId: String)
    fun favoritesFlow(): Flow<Set<String>>
    suspend fun isFavorite(deviceId: String): Boolean
}
