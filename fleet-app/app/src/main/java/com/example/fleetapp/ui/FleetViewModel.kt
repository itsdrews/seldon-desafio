package com.example.fleetapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fleetapp.data.repository.FleetRepository
import com.example.fleetapp.domain.model.Device
import com.example.fleetapp.domain.model.DeviceStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class FilterOption(val label: String) {
    ALL("Todos"),
    FAVORITES("Favoritos"),
    CRITICO("Críticos"),
    ATENCAO("Atenção"),
    OK("OK")
}

sealed interface FleetUiState {
    object Loading : FleetUiState
    data class Success(
        val devices: List<Device>,
        val searchQuery: String,
        val selectedFilter: FilterOption
    ) : FleetUiState
    data class Error(val message: String) : FleetUiState
}

class FleetViewModel(private val repository: FleetRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow(FilterOption.ALL)

    val uiState: StateFlow<FleetUiState> = combine(
        repository.getDevicesFlow(),
        _searchQuery,
        _selectedFilter
    ) { devices, query, filter ->
        val filteredList = devices.filter { device ->
            val matchesQuery = device.name.contains(query, ignoreCase = true) ||
                    device.id.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                FilterOption.ALL -> true
                FilterOption.FAVORITES -> device.isFavorite
                FilterOption.CRITICO -> device.status == DeviceStatus.CRITICO
                FilterOption.ATENCAO -> device.status == DeviceStatus.ATENCAO
                FilterOption.OK -> device.status == DeviceStatus.OK
            }

            matchesQuery && matchesFilter
        }

        FleetUiState.Success(
            devices = filteredList,
            searchQuery = query,
            selectedFilter = filter
        ) as FleetUiState
    }.catch { exception ->
        emit(FleetUiState.Error("Erro ao carregar dados: ${exception.localizedMessage}"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FleetUiState.Loading
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onFilterSelected(filter: FilterOption) {
        _selectedFilter.value = filter
    }

    fun toggleFavorite(deviceId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(deviceId)
        }
    }
}