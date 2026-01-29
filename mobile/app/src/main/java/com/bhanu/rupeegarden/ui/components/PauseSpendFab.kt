package com.bhanu.rupeegarden.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhanu.rupeegarden.ui.theme.GreenPrimary

@Composable
fun PauseSpendFab(
    onClick: () -> Unit,
    showLabel: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        // Microcopy label - shows when showLabel is true
        AnimatedVisibility(
            visible = showLabel,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 2.dp,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "Most impulse spends fade in 30 seconds",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        // FAB with label
        ExtendedFloatingActionButton(
            onClick = onClick,
            containerColor = GreenPrimary,
            contentColor = Color.White,
            icon = {
                Text(
                    text = "\u23F3",
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "Pause",
                    fontWeight = FontWeight.SemiBold
                )
            }
        )
    }
}
