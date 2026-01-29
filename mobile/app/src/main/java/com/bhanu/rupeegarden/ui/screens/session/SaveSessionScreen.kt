package com.bhanu.rupeegarden.ui.screens.session

import androidx.compose.animation.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.bhanu.rupeegarden.audio.LocalSoundManager
import com.bhanu.rupeegarden.audio.SoundEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bhanu.rupeegarden.data.model.PlantState
import com.bhanu.rupeegarden.ui.components.TimerRing
import com.bhanu.rupeegarden.ui.theme.GreenPrimary
import com.bhanu.rupeegarden.ui.theme.XpGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveSessionScreen(
    onEndDay: () -> Unit,
    onBack: () -> Unit,
    viewModel: SessionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val soundManager = LocalSoundManager.current

    // Play sound on plant state transition
    LaunchedEffect(uiState.plantState) {
        val previousState = uiState.previousPlantState
        if (previousState != null && previousState != uiState.plantState) {
            val sound = when (uiState.plantState) {
                PlantState.SEED -> SoundEffect.PLANT_SEED
                PlantState.SPROUT -> SoundEffect.PLANT_SPROUT
                PlantState.YOUNG -> SoundEffect.PLANT_YOUNG
                PlantState.FULL -> SoundEffect.PLANT_FULL
                PlantState.WITHERED -> null
            }
            sound?.let { soundManager?.play(it) }
        }
    }

    // Play initial seed sound when session starts
    LaunchedEffect(Unit) {
        soundManager?.play(SoundEffect.PLANT_SEED)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Growing Session") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = getPlantStateMessage(uiState.plantState),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = getPlantStateDescription(uiState.plantState),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Timer Ring with Plant
                TimerRing(
                    elapsedSeconds = uiState.elapsedSeconds,
                    plantState = uiState.plantState,
                    size = 280.dp
                )

                // Bottom section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // XP indicator
                    AnimatedVisibility(
                        visible = uiState.isFullGrown,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = XpGold.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = "Your tree is fully grown! You can end the day now.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = XpGold,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    // End Day Button
                    Button(
                        onClick = onEndDay,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isFullGrown) GreenPrimary else GreenPrimary.copy(alpha = 0.7f)
                        )
                    ) {
                        Text(
                            text = "End Day",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (uiState.isFullGrown) {
                            "Ready for a great ending!"
                        } else {
                            "You can end early, but wait for full growth for best results"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun getPlantStateMessage(state: PlantState): String {
    return when (state) {
        PlantState.SEED -> "Planting your seed..."
        PlantState.SPROUT -> "A sprout emerges!"
        PlantState.YOUNG -> "Growing stronger..."
        PlantState.FULL -> "Fully grown!"
        PlantState.WITHERED -> "Tree withered"
    }
}

private fun getPlantStateDescription(state: PlantState): String {
    return when (state) {
        PlantState.SEED -> "Your savings journey begins. Watch it grow!"
        PlantState.SPROUT -> "Your seed is sprouting. Keep going!"
        PlantState.YOUNG -> "Your plant is getting stronger every second"
        PlantState.FULL -> "Your tree has reached full growth. Time to decide!"
        PlantState.WITHERED -> "This tree has seen better days"
    }
}
