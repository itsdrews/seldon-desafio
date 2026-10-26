package com.example.fleetapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fleetapp.domain.model.Device
import com.example.fleetapp.domain.model.DeviceStatus
import com.example.fleetapp.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class FilterOption(val label: String) {
    ALL("Todos"),
    FAVORITES("Favoritos"),
    CRITICO("Críticos"),
    ATENCAO("Atenção"),
    OK("OK")
}

data class StatusCounts(
    val all: Int = 0,
    val favorites: Int = 0,
    val critical: Int = 0,
    val warning: Int = 0,
    val ok: Int = 0
)

sealed interface FleetUiState {
    object Loading : FleetUiState
    data class Success(
        val devices: List<Device>,
        val counts: StatusCounts,
        val selectedFilter: FilterOption
    ) : FleetUiState
    data class Error(val message: String) : FleetUiState
}

class FleetViewModel(private val repository: DeviceRepository) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(FilterOption.ALL)

    val uiState: StateFlow<FleetUiState> = combine(
        repository.getDevicesFlow(),
        _selectedFilter
    ) { devices, filter ->
        val counts = StatusCounts(
            all = devices.size,
            favorites = devices.count { it.isFavorite },
            critical = devices.count { it.status == DeviceStatus.CRITICO },
            warning = devices.count { it.status == DeviceStatus.ATENCAO },
            ok = devices.count { it.status == DeviceStatus.OK }
        )

        val filteredList = devices.filter { device ->
            when (filter) {
                FilterOption.ALL -> true
                FilterOption.FAVORITES -> device.isFavorite
                FilterOption.CRITICO -> device.status == DeviceStatus.CRITICO
                FilterOption.ATENCAO -> device.status == DeviceStatus.ATENCAO
                FilterOption.OK -> device.status == DeviceStatus.OK
            }
        }

        FleetUiState.Success(
            devices = filteredList,
            counts = counts,
            selectedFilter = filter
        ) as FleetUiState
    }.catch { exception ->
        emit(FleetUiState.Error("Erro ao carregar dados: ${exception.localizedMessage}"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FleetUiState.Loading
    )

    fun onFilterSelected(filter: FilterOption) {
        _selectedFilter.value = filter
    }

    fun toggleFavorite(deviceId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(deviceId)
        }
    }
}
