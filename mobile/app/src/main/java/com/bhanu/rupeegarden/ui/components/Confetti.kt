package com.bhanu.rupeegarden.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlinx.coroutines.delay
import kotlin.random.Random

data class ConfettiPiece(
    val x: Float,
    val y: Float,
    val rotation: Float,
    val color: Color,
    val size: Float,
    val speedY: Float,
    val speedX: Float,
    val rotationSpeed: Float
)

@Composable
fun ConfettiAnimation(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onComplete: () -> Unit = {}
) {
    if (!isPlaying) return

    val colors = listOf(
        Color(0xFF5C9E4A), // Green
        Color(0xFFD4A854), // Gold
        Color(0xFF4CAF50), // Bright Green
        Color(0xFFFFD700), // Yellow
        Color(0xFFFF6B6B), // Red
        Color(0xFF4ECDC4), // Teal
        Color(0xFFAE7DDD)  // Purple
    )

    var pieces by remember {
        mutableStateOf(
            List(80) {
                ConfettiPiece(
                    x = Random.nextFloat(),
                    y = Random.nextFloat() * -1f, // Start above screen
                    rotation = Random.nextFloat() * 360f,
                    color = colors.random(),
                    size = Random.nextFloat() * 12f + 6f,
                    speedY = Random.nextFloat() * 0.015f + 0.008f,
                    speedX = Random.nextFloat() * 0.006f - 0.003f,
                    rotationSpeed = Random.nextFloat() * 8f - 4f
                )
            }
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_progress"
    )

    // Update positions
    LaunchedEffect(progress) {
        pieces = pieces.map { piece ->
            piece.copy(
                y = piece.y + piece.speedY,
                x = piece.x + piece.speedX,
                rotation = piece.rotation + piece.rotationSpeed
            )
        }
    }

    // Auto-complete after animation
    LaunchedEffect(Unit) {
        delay(3000)
        onComplete()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        pieces.forEach { piece ->
            if (piece.y <= 1.2f) { // Only draw if on screen
                val x = piece.x * size.width
                val y = piece.y * size.height

                rotate(piece.rotation, pivot = Offset(x, y)) {
                    drawRect(
                        color = piece.color,
                        topLeft = Offset(x - piece.size / 2, y - piece.size / 2),
                        size = Size(piece.size, piece.size * 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun CelebrationConfetti(
    show: Boolean,
    onDismiss: () -> Unit
) {
    if (show) {
        ConfettiAnimation(
            isPlaying = true,
            onComplete = onDismiss
        )
    }
}
