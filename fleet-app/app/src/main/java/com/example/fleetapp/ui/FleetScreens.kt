package com.example.fleetapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.fleetapp.domain.model.Device
import com.example.fleetapp.domain.model.DeviceStatus

@Composable
fun StatusBadge(status: DeviceStatus) {
    val (color, text) = when (status) {
        DeviceStatus.OK -> Color(0xFF2E7D32) to "OK"
        DeviceStatus.ATENCAO -> Color(0xFFED6C02) to "ATENÇÃO"
        DeviceStatus.CRITICO -> Color(0xFFD32F2F) to "CRÍTICO"
    }
    Surface(color = color, shape = MaterialTheme.shapes.small) {
        Text(
            text = text,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetListScreen(
    viewModel: FleetViewModel,
    onDeviceClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Monitoramento de Frota") }) }
    ) { padding ->
        when (val s = state) {
            is FleetUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            is FleetUiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(s.message) }
            is FleetUiState.Success -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    // Barra de Busca por Texto
                    OutlinedTextField(
                        value = s.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Buscar por modelo ou ID...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                        trailingIcon = {
                            if (s.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpar")
                                }
                            }
                        },
                        singleLine = true
                    )

                    // Carrossel de Chips de Filtro (Todos, Favoritos, Críticos, Atenção, OK)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        items(FilterOption.entries.toTypedArray()) { option ->
                            FilterChip(
                                selected = s.selectedFilter == option,
                                onClick = { viewModel.onFilterSelected(option) },
                                label = { Text(option.label) }
                            )
                        }
                    }

                    HorizontalDivider()

                    // Lista de Dispositivos ou Mensagem Vazia
                    if (s.devices.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhum dispositivo encontrado.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(s.devices, key = { it.id }) { device ->
                                ListItem(
                                    headlineContent = { Text(device.name) },
                                    supportingContent = { Text("Bateria Restante: ${device.batteryLevel}%") },
                                    trailingContent = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            StatusBadge(device.status)
                                            IconButton(onClick = { viewModel.toggleFavorite(device.id) }) {
                                                Icon(
                                                    imageVector = if (device.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                    contentDescription = "Favorito",
                                                    tint = if (device.isFavorite) Color.Red else Color.Gray
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier.clickable { onDeviceClick(device.id) }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    deviceId: String,
    viewModel: FleetViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val device = (state as? FleetUiState.Success)?.devices?.find { it.id == deviceId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(device?.name ?: "Detalhes do Dispositivo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (device == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Dispositivo não encontrado.") }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(device.status)
                    IconButton(onClick = { viewModel.toggleFavorite(device.id) }) {
                        Icon(
                            imageVector = if (device.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favoritar",
                            tint = if (device.isFavorite) Color.Red else Color.Gray
                        )
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Métricas Energéticas", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("• Dispositivo ID: ${device.id}")
                        Text("• Bateria Calculada: ${device.batteryLevel}%")
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Justificativa do Status", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        device.justifications.forEach { reason ->
                            Text("• $reason", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}