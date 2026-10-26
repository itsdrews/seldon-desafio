package com.example.fleetapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "fleet_favorites_prefs")

class FavoritesDataStore(private val context: Context) {
    private val FAVORITES_KEY = stringSetPreferencesKey("favorite_ids")

    val favoritesFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[FAVORITES_KEY] ?: emptySet()
    }

    suspend fun toggleFavorite(deviceId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[FAVORITES_KEY] ?: emptySet()
            if (current.contains(deviceId)) {
                prefs[FAVORITES_KEY] = current - deviceId
            } else {
                prefs[FAVORITES_KEY] = current + deviceId
            }
        }
    }
}