package com.example.fleetapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fleetapp.domain.repository.DeviceRepository

class FleetViewModelFactory(
    private val repository: DeviceRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FleetViewModel::class.java)) {
            return FleetViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
}