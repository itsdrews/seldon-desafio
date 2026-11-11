package com.example.fleetapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fleetapp.domain.repository.DeviceRepository
import com.example.fleetapp.domain.repository.FavoriteDeviceRepository

class FleetViewModelFactory(
    private val repository: DeviceRepository,
    private val favoriteRepository: FavoriteDeviceRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FleetViewModel::class.java)) {
            return FleetViewModel(repository, favoriteRepository) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
}