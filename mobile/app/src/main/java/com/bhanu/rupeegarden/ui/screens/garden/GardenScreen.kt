package com.bhanu.rupeegarden.ui.screens.garden

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bhanu.rupeegarden.data.model.DayEntry
import com.bhanu.rupeegarden.ui.components.FilterPeriod
import com.bhanu.rupeegarden.ui.components.PeriodFilterChips
import com.bhanu.rupeegarden.ui.theme.*
import com.bhanu.rupeegarden.util.DateUtils
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GardenScreen(
    onBack: () -> Unit,
    onNavigateToPauseSpend: () -> Unit,
    viewModel: GardenViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            com.bhanu.rupeegarden.ui.components.PauseSpendFab(
                onClick = onNavigateToPauseSpend,
                showLabel = false // No label needed on Garden screen
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { scaffoldPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(scaffoldPadding)
    ) {
        // Compact header with back button, month navigation and filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }

            // Month navigation
            IconButton(
                onClick = { viewModel.previousMonth() },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous month"
                )
            }

            Text(
                text = uiState.periodLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            IconButton(
                onClick = { viewModel.nextMonth() },
                enabled = uiState.canGoNext,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next month",
                    tint = if (uiState.canGoNext) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.3f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Period filter chips
            PeriodFilterChips(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodSelected = { viewModel.selectPeriod(it) }
            )
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else if (uiState.entries.isEmpty()) {
            EmptyGarden(
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Isometric garden view
                IsometricGarden(
                    entries = uiState.entries,
                    onEntryClick = { viewModel.selectEntry(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .padding(horizontal = 16.dp)
                )

                // Stats summary
                GardenStats(
                    entries = uiState.entries,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Entry list
                EntryList(
                    entries = uiState.entries,
                    onEntryClick = { viewModel.selectEntry(it) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    } // End Scaffold

    // Entry detail bottom sheet
    uiState.selectedEntry?.let { entry ->
        EntryDetailSheet(
            entry = entry,
            onDismiss = { viewModel.dismissEntryDetails() }
        )
    }
}

@Composable
private fun IsometricGarden(
    entries: List<DayEntry>,
    onEntryClick: (DayEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayEntries = entries.take(16) // Max 4x4 grid

    // Store tree positions for tap detection
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    // Calculate tree positions based on canvas size
    fun getTreePositions(): List<Pair<Int, Offset>> {
        if (canvasSize == Size.Zero) return emptyList()
        val positions = mutableListOf<Pair<Int, Offset>>()
        displayEntries.forEachIndexed { index, _ ->
            val row = index / 4
            val col = index % 4
            // Spread across full platform
            val isoX = canvasSize.width * 0.5f + (col - 1.5f) * canvasSize.width * 0.09f - (row - 1.5f) * canvasSize.width * 0.09f
            val isoY = canvasSize.height * 0.15f + (col + row) * canvasSize.height * 0.07f
            positions.add(index to Offset(isoX, isoY))
        }
        return positions
    }

    // Find which tree was tapped
    fun findTappedTree(tapOffset: Offset): Int? {
        val hitRadius = canvasSize.width * 0.07f // Tap area around tree
        val positions = getTreePositions()

        // Check trees from front to back (reverse order for correct z-ordering)
        for ((index, position) in positions.reversed()) {
            val distance = kotlin.math.sqrt(
                (tapOffset.x - position.x) * (tapOffset.x - position.x) +
                (tapOffset.y - position.y) * (tapOffset.y - position.y)
            )
            if (distance < hitRadius) {
                return index
            }
        }
        return null
    }

    Canvas(
        modifier = modifier
            .pointerInput(displayEntries) {
                detectTapGestures { tapOffset ->
                    val tappedIndex = findTappedTree(tapOffset)
                    tappedIndex?.let { index ->
                        if (index < displayEntries.size) {
                            onEntryClick(displayEntries[index])
                        }
                    }
                }
            }
    ) {
        canvasSize = size
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Draw grass platform - sized to fit the tree grid
        val platformPath = Path().apply {
            // Isometric diamond shape
            moveTo(canvasWidth * 0.5f, canvasHeight * 0.05f) // Top
            lineTo(canvasWidth * 0.95f, canvasHeight * 0.38f) // Right
            lineTo(canvasWidth * 0.5f, canvasHeight * 0.71f) // Bottom
            lineTo(canvasWidth * 0.05f, canvasHeight * 0.38f) // Left
            close()
        }
        drawPath(platformPath, GrassGreen)

        // Draw soil sides
        val leftSidePath = Path().apply {
            moveTo(canvasWidth * 0.05f, canvasHeight * 0.38f)
            lineTo(canvasWidth * 0.5f, canvasHeight * 0.71f)
            lineTo(canvasWidth * 0.5f, canvasHeight * 0.82f)
            lineTo(canvasWidth * 0.05f, canvasHeight * 0.49f)
            close()
        }
        drawPath(leftSidePath, SoilBrown)

        val rightSidePath = Path().apply {
            moveTo(canvasWidth * 0.95f, canvasHeight * 0.38f)
            lineTo(canvasWidth * 0.5f, canvasHeight * 0.71f)
            lineTo(canvasWidth * 0.5f, canvasHeight * 0.82f)
            lineTo(canvasWidth * 0.95f, canvasHeight * 0.49f)
            close()
        }
        drawPath(rightSidePath, SoilBrownDark)

        // Draw trees in isometric grid - spread across full platform
        displayEntries.forEachIndexed { index, entry ->
            val row = index / 4
            val col = index % 4

            // Calculate isometric position - full platform spread
            val isoX = canvasWidth * 0.5f + (col - 1.5f) * canvasWidth * 0.09f - (row - 1.5f) * canvasWidth * 0.09f
            val isoY = canvasHeight * 0.15f + (col + row) * canvasHeight * 0.07f

            val saved = entry.saved ?: false
            val treeColor = if (saved) TreeGreen else WitheredBrown
            val trunkColor = if (saved) TrunkBrown else WitheredBrownDark

            // Draw tree trunk
            val trunkWidth = canvasWidth * 0.014f
            val trunkHeight = canvasHeight * 0.05f
            drawRect(
                color = trunkColor,
                topLeft = Offset(isoX - trunkWidth / 2, isoY),
                size = Size(trunkWidth, trunkHeight)
            )

            // Draw tree crown (circles) - smaller for better spacing
            val crownRadius = canvasWidth * 0.022f
            drawCircle(
                color = treeColor,
                radius = crownRadius,
                center = Offset(isoX, isoY - crownRadius * 0.5f)
            )
            drawCircle(
                color = treeColor,
                radius = crownRadius * 0.8f,
                center = Offset(isoX - crownRadius * 0.5f, isoY)
            )
            drawCircle(
                color = treeColor,
                radius = crownRadius * 0.8f,
                center = Offset(isoX + crownRadius * 0.5f, isoY)
            )
        }
    }
}

@Composable
private fun GardenStats(
    entries: List<DayEntry>,
    modifier: Modifier = Modifier
) {
    val savedCount = entries.count { it.saved == true }
    val spentCount = entries.count { it.saved == false }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = savedCount.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary
                )
                Text(
                    text = "Saved Days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = spentCount.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = SpentRed
                )
                Text(
                    text = "Spent Days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = entries.size.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Total Trees",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EntryList(
    entries: List<DayEntry>,
    onEntryClick: (DayEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Recent Entries",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        entries.forEach { entry ->
            EntryListItem(
                entry = entry,
                onClick = { onEntryClick(entry) }
            )
        }
    }
}

@Composable
private fun EntryListItem(
    entry: DayEntry,
    onClick: () -> Unit
) {
    val date = LocalDate.parse(entry.date)
    val saved = entry.saved ?: false

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (saved) GreenPrimary.copy(alpha = 0.1f) else SpentRed.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = DateUtils.getRelativeDay(date),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (saved) "Saved" else "Spent${entry.spentCategory?.let { " - ${it.displayName}" } ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${entry.xpEarned} XP",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = XpGold
                )
                if (!saved && entry.spentAmount != null) {
                    Text(
                        text = "₹${String.format("%.0f", entry.spentAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SpentRed
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyGarden(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Your garden is empty",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Start a session to plant your first tree!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntryDetailSheet(
    entry: DayEntry,
    onDismiss: () -> Unit
) {
    val date = LocalDate.parse(entry.date)
    val saved = entry.saved ?: false

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp, top = 8.dp)
        ) {
            // Header row with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date and day of week
                Column {
                    Text(
                        text = DateUtils.formatDisplayDate(date),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Status card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (saved) GreenPrimary.copy(alpha = 0.1f) else SpentRed.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (saved) GreenPrimary else SpentRed
                        ) {
                            Text(
                                text = if (saved) "Saved" else "Spent",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // XP earned
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "+${entry.xpEarned}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = XpGold
                        )
                        Text(
                            text = "XP earned",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Spending details (only for spent days)
            if (!saved) {
                Spacer(modifier = Modifier.height(16.dp))

                // Amount - prominent display
                entry.spentAmount?.let { amount ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Amount",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹${String.format("%,.0f", amount)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = SpentRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category and Note
                entry.spentCategory?.let { category ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.emoji,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Category",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                entry.spentDescription?.let { description ->
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Note",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
