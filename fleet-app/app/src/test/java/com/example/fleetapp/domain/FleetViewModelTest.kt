package com.example.fleetapp.ui

import com.example.fleetapp.data.FakeDeviceRepository
import com.example.fleetapp.domain.model.Device
import com.example.fleetapp.domain.model.DeviceStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FleetViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepository: FakeDeviceRepository
    private lateinit var viewModel: FleetViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        val testDevices = listOf(
            Device(
                id = "DEV-01",
                name = "Pixel 6a",
                batteryLevel = 10,
                batteryCapacityMah = 4410,
                energyConsumedMah = 980.0,
                screenOnMinutes = 214,
                status = DeviceStatus.CRITICO,
                justifications = listOf("Bateria abaixo de 15%"),
                isFavorite = false
            ),
            Device(
                id = "DEV-02",
                name = "Galaxy S21",
                batteryLevel = 85,
                batteryCapacityMah = 4000,
                energyConsumedMah = 500.0,
                screenOnMinutes = 120,
                status = DeviceStatus.OK,
                justifications = listOf("Métricas operacionais normais"),
                isFavorite = true
            )
        )

        fakeRepository = FakeDeviceRepository(testDevices)
        viewModel = FleetViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `quando o ViewModel e inicializado, calcula as contagens de status corretamente`() = runTest(testDispatcher) {
        backgroundScope.launch {
            viewModel.uiState.collect {}
        }

        val state = viewModel.uiState.value
        assertTrue("O estado deveria ser Success mas foi $state", state is FleetUiState.Success)

        val successState = state as FleetUiState.Success
        assertEquals(2, successState.counts.all)
        assertEquals(1, successState.counts.critical)
        assertEquals(1, successState.counts.ok)
        assertEquals(1, successState.counts.favorites)
    }

    @Test
    fun `quando o filtro CRITICO e selecionado, retorna apenas dispositivos criticos`() = runTest(testDispatcher) {
        backgroundScope.launch {
            viewModel.uiState.collect {}
        }

        viewModel.onFilterSelected(FilterOption.CRITICO)

        val state = viewModel.uiState.value as FleetUiState.Success
        assertEquals(1, state.devices.size)
        assertEquals("DEV-01", state.devices.first().id)
    }

    @Test
    fun `quando o filtro FAVORITOS e selecionado, lista apenas dispositivos favoritados`() = runTest(testDispatcher) {
        backgroundScope.launch {
            viewModel.uiState.collect {}
        }

        viewModel.onFilterSelected(FilterOption.FAVORITES)

        val state = viewModel.uiState.value as FleetUiState.Success
        assertEquals(1, state.devices.size)
        assertEquals("DEV-02", state.devices.first().id)
    }

    @Test
    fun `quando a acao toggleFavorite e executada, altera o estado do favorito`() = runTest(testDispatcher) {
        backgroundScope.launch {
            viewModel.uiState.collect {}
        }

        viewModel.toggleFavorite("DEV-01")

        val device = fakeRepository.getDevicesFlow().first().find { it.id == "DEV-01" }
        assertTrue(device?.isFavorite == true)
    }
}