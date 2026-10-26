package com.example.fleetapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fleetapp.data.repository.FleetRepository

class FleetViewModelFactory(
    private val repository: FleetRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FleetViewModel::class.java)) {
            return FleetViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
}