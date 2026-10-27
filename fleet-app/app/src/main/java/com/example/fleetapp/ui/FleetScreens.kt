package com.example.fleetapp.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fleetapp.domain.model.Device
import com.example.fleetapp.domain.model.DeviceStatus

// Paleta Dark Blue & Shadow High-Contrast
private val DarkBlueBackground = Color(0xFF0F172A) // Fundo Principal (Slate 900)
private val DarkBlueSurface = Color(0xFF1E293B)    // Fundo de Cards (Slate 800)
private val DarkBlueTopBar = Color(0xFF020617)     // Top Bar (Slate 950)
private val TextPrimary = Color(0xFFF8FAFC)        // Texto de alto contraste
private val TextSecondary = Color(0xFF94A3B8)      // Texto de apoio
private val AccentBlue = Color(0xFF38BDF8)         // Destaque Azul Celeste

private data class StatusStyle(
    val containerColor: Color,
    val contentColor: Color,
    val icon: ImageVector,
    val label: String
)

@Composable
fun StatusBadge(status: DeviceStatus) {
    val style = when (status) {
        DeviceStatus.OK -> StatusStyle(
            containerColor = Color(0xFF064E3B),
            contentColor = Color(0xFF34D399),
            icon = Icons.Default.CheckCircle,
            label = "OK"
        )
        DeviceStatus.ATENCAO -> StatusStyle(
            containerColor = Color(0xFF78350F),
            contentColor = Color(0xFFFBBF24),
            icon = Icons.Default.Warning,
            label = "ATENÇÃO"
        )
        DeviceStatus.CRITICO -> StatusStyle(
            containerColor = Color(0xFF7F1D1D),
            contentColor = Color(0xFFF87171),
            icon = Icons.Default.Error,
            label = "CRÍTICO"
        )
    }

    Surface(
        color = style.containerColor,
        contentColor = style.contentColor,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, style.contentColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = style.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DeviceCard(
    device: Device,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val batteryColor = when (device.status) {
        DeviceStatus.CRITICO -> Color(0xFFF87171)
        DeviceStatus.ATENCAO -> Color(0xFFFBBF24)
        DeviceStatus.OK -> Color(0xFF34D399)
    }

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = CardDefaults.shape,
                ambientColor = Color.Black,
                spotColor = AccentBlue.copy(alpha = 0.5f)
            ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = DarkBlueSurface,
            contentColor = TextPrimary
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "ID: ${device.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                StatusBadge(status = device.status)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { (device.batteryLevel / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                    color = batteryColor,
                    trackColor = batteryColor.copy(alpha = 0.2f)
                )

                Text(
                    text = "${device.batteryLevel}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = batteryColor
                )

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (device.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (device.isFavorite) Color(0xFFEF4444) else TextSecondary
                    )
                }
            }
        }
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
        containerColor = DarkBlueBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Monitoramento de Frota",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBlueTopBar
                )
            )
        }
    ) { padding ->
        when (val s = state) {
            is FleetUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            }
            is FleetUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = s.message,
                        color = Color(0xFFF87171)
                    )
                }
            }
            is FleetUiState.Success -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    Surface(
                        color = DarkBlueSurface.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${s.counts.all} dispositivos lidos de Assets",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(FilterOption.entries.toTypedArray()) { option ->
                            val countText = when (option) {
                                FilterOption.ALL -> " (${s.counts.all})"
                                FilterOption.FAVORITES -> " (${s.counts.favorites})"
                                FilterOption.CRITICO -> " (${s.counts.critical})"
                                FilterOption.ATENCAO -> " (${s.counts.warning})"
                                FilterOption.OK -> " (${s.counts.ok})"
                            }

                            FilterChip(
                                selected = s.selectedFilter == option,
                                onClick = { viewModel.onFilterSelected(option) },
                                label = { Text("${option.label}$countText") },
                                leadingIcon = if (s.selectedFilter == option) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = DarkBlueSurface,
                                    labelColor = TextSecondary,
                                    selectedContainerColor = AccentBlue,
                                    selectedLabelColor = DarkBlueTopBar
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = DarkBlueSurface)

                    if (s.devices.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhum dispositivo encontrado para o filtro selecionado.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(s.devices, key = { it.id }) { device ->
                                DeviceCard(
                                    device = device,
                                    onClick = { onDeviceClick(device.id) },
                                    onToggleFavorite = { viewModel.toggleFavorite(device.id) }
                                )
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
    BackHandler(onBack = onBack)

    val state by viewModel.uiState.collectAsState()
    val device = (state as? FleetUiState.Success)?.devices?.find { it.id == deviceId }

    Scaffold(
        containerColor = DarkBlueBackground,
        topBar = {
            TopAppBar(
                title = { Text(device?.name ?: "Detalhes do Dispositivo", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBlueTopBar
                )
            )
        }
    ) { padding ->
        if (device == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Dispositivo não encontrado.", color = TextSecondary)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header com Status e Favorito
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, CardDefaults.shape, spotColor = AccentBlue.copy(alpha = 0.3f)),
                    colors = CardDefaults.elevatedCardColors(containerColor = DarkBlueSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Estado Operacional",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                            StatusBadge(status = device.status)
                        }

                        IconButton(onClick = { viewModel.toggleFavorite(device.id) }) {
                            Icon(
                                imageVector = if (device.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favoritar",
                                tint = if (device.isFavorite) Color(0xFFEF4444) else TextSecondary
                            )
                        }
                    }
                }

                // Card de Métricas de Hardware / Energia
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, CardDefaults.shape, spotColor = AccentBlue.copy(alpha = 0.3f)),
                    colors = CardDefaults.elevatedCardColors(containerColor = DarkBlueSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Métricas da Bateria",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue
                        )
                        HorizontalDivider(color = DarkBlueBackground)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Identificador:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(device.id, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Modelo:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(device.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Capacidade Nominal:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("${device.batteryCapacityMah} mAh", fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Consumo Normalizado:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("${String.format("%.1f", device.energyConsumedMah)} mAh", fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tempo de Tela Ligada:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("${device.screenOnMinutes} min", fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Nível Restante:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("${device.batteryLevel}%", fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }

                // Card de Justificativa
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, CardDefaults.shape, spotColor = AccentBlue.copy(alpha = 0.3f)),
                    colors = CardDefaults.elevatedCardColors(containerColor = DarkBlueSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Análise de Diagnóstico",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue
                        )
                        HorizontalDivider(color = DarkBlueBackground)

                        device.justifications.forEach { reason ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("•", fontWeight = FontWeight.Bold, color = AccentBlue)
                                Text(
                                    text = reason,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}