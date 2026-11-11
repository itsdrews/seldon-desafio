package com.example.fleetapp.data.repository

import android.content.Context
import com.example.fleetapp.data.local.FavoritesDataStore
import com.example.fleetapp.domain.repository.FavoriteDeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FavoriteDeviceRepositoryImpl(
    private val favoritesDataStore: FavoritesDataStore
) : FavoriteDeviceRepository {

    override suspend fun toggleFavorite(deviceId: String) {
        favoritesDataStore.toggleFavorite(deviceId)
    }

    override fun favoritesFlow(): Flow<Set<String>> = favoritesDataStore.favoritesFlow

    override suspend fun isFavorite(deviceId: String): Boolean =
        favoritesDataStore.favoritesFlow.first().contains(deviceId)
}
