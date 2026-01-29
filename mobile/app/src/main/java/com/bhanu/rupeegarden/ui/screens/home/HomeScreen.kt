package com.bhanu.rupeegarden.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bhanu.rupeegarden.ui.components.*
import com.bhanu.rupeegarden.ui.theme.GreenPrimary
import com.bhanu.rupeegarden.ui.theme.SpentRed
import com.bhanu.rupeegarden.ui.theme.XpGold
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartSession: () -> Unit,
    onNavigateToGarden: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onResumeSession: () -> Unit,
    onNavigateToPauseSpend: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    // Navigate to session when session is created
    LaunchedEffect(uiState.navigateToSession) {
        if (uiState.navigateToSession) {
            onStartSession()
            viewModel.onNavigatedToSession()
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp, top = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateToGarden, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.Park,
                        contentDescription = "Garden",
                        tint = GreenPrimary
                    )
                }
                IconButton(onClick = onNavigateToInsights, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.Insights,
                        contentDescription = "Insights",
                        tint = XpGold
                    )
                }
                IconButton(onClick = onNavigateToSettings, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
        },
        floatingActionButton = {
            if (!uiState.isLoading) {
                PauseSpendFab(
                    onClick = onNavigateToPauseSpend,
                    showLabel = uiState.impulseStats.totalChecks < 3
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Simple Budget Display - Just "₹X remaining"
                uiState.monthlySpending?.let { spending ->
                    SimpleBudgetDisplay(
                        spent = spending.totalSpent,
                        budget = spending.budget
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Daily Check-in (Secondary Action)
                when {
                    uiState.hasActiveSession -> {
                        DailyCheckInButton(
                            text = "Resume Check-in",
                            subtitle = "You have an active session",
                            onClick = onResumeSession
                        )
                    }
                    uiState.canStartNewSession -> {
                        DailyCheckInButton(
                            text = "Daily Check-in",
                            subtitle = "Record today's spending decision",
                            onClick = {
                                viewModel.startSession()
                            }
                        )
                    }
                    else -> {
                        // Already completed today
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = GreenPrimary.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Garden updated for today",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = GreenPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Pause Spend is always available if you need it.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }
}

/**
 * Ultra-minimal budget display - just shows remaining amount
 */
@Composable
private fun SimpleBudgetDisplay(
    spent: Double,
    budget: Double
) {
    val remaining = (budget - spent).coerceAtLeast(0.0)
    val isOverBudget = spent > budget
    val progress = (spent / budget).coerceIn(0.0, 1.0).toFloat()

    val format = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("en").setRegion("IN").build())
    format.maximumFractionDigits = 0

    val progressColor = when {
        isOverBudget -> SpentRed
        progress > 0.8f -> XpGold
        else -> GreenPrimary
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main amount
        Text(
            text = if (isOverBudget) {
                "Over budget"
            } else {
                format.format(remaining)
            },
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = if (isOverBudget) SpentRed else GreenPrimary
        )

        Text(
            text = if (isOverBudget) {
                "by ${format.format(spent - budget)}"
            } else {
                "remaining"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(progressColor.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(4.dp))
                    .background(progressColor)
            )
        }
    }
}

/**
 * Daily Check-in button - visually demoted compared to Pause Spend FAB
 */
@Composable
private fun DailyCheckInButton(
    text: String,
    subtitle: String,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.Park,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
