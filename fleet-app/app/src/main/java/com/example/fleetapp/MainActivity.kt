package com.example.fleetapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fleetapp.data.DeviceMapper
import com.example.fleetapp.data.local.FavoritesDataStore
import com.example.fleetapp.data.repository.FavoriteDeviceRepositoryImpl
import com.example.fleetapp.data.repository.FleetRepositoryImpl
import com.example.fleetapp.domain.DeviceClassifier
import com.example.fleetapp.domain.repository.DeviceRepository
import com.example.fleetapp.domain.repository.FavoriteDeviceRepository
import com.example.fleetapp.ui.DeviceDetailScreen
import com.example.fleetapp.ui.FleetListScreen
import com.example.fleetapp.ui.FleetViewModel
import com.example.fleetapp.ui.FleetViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val favoritesDataStore = FavoritesDataStore(applicationContext)
        val repository: DeviceRepository = FleetRepositoryImpl(
            context = applicationContext,
            mapper = DeviceMapper(DeviceClassifier),
            favoriteRepository = FavoriteDeviceRepositoryImpl(favoritesDataStore)
        )
        val favoriteRepository: FavoriteDeviceRepository = FavoriteDeviceRepositoryImpl(favoritesDataStore)

        setContent {
            val viewModel: FleetViewModel = viewModel(
                factory = FleetViewModelFactory(repository, favoriteRepository)
            )

            var selectedDeviceId by remember { mutableStateOf<String?>(null) }

            if (selectedDeviceId == null) {
                FleetListScreen(
                    viewModel = viewModel,
                    onDeviceClick = { deviceId -> selectedDeviceId = deviceId }
                )
            } else {
                DeviceDetailScreen(
                    deviceId = selectedDeviceId!!,
                    viewModel = viewModel,
                    onBack = { selectedDeviceId = null }
                )
            }
        }
    }
}