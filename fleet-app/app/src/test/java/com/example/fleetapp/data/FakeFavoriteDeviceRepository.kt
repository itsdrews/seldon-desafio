package com.example.fleetapp.data

import com.example.fleetapp.domain.repository.FavoriteDeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow

class FakeFavoriteDeviceRepository(
    private val initialFavorites: Set<String> = emptySet()
) : FavoriteDeviceRepository {

    private val favoritesFlow = MutableStateFlow(initialFavorites)

    override suspend fun toggleFavorite(deviceId: String) {
        favoritesFlow.value = if (favoritesFlow.value.contains(deviceId)) {
            favoritesFlow.value - deviceId
        } else {
            favoritesFlow.value + deviceId
        }
    }

    override fun favoritesFlow(): Flow<Set<String>> = favoritesFlow

    override suspend fun isFavorite(deviceId: String): Boolean = favoritesFlow.value.contains(deviceId)
}
