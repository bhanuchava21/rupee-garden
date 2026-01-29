package com.bhanu.rupeegarden.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bhanu.rupeegarden.data.model.PlantState
import com.bhanu.rupeegarden.ui.theme.*

@Composable
fun PlantVisual(
    plantState: PlantState,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    isWithered: Boolean = false
) {
    val leafColor = if (isWithered) WitheredBrown else TreeGreen
    val trunkColor = if (isWithered) WitheredBrownDark else TrunkBrown

    Canvas(modifier = modifier.size(size)) {
        val centerX = this.size.width / 2
        val bottomY = this.size.height

        when (plantState) {
            PlantState.SEED -> drawSeed(centerX, bottomY, trunkColor)
            PlantState.SPROUT -> drawSprout(centerX, bottomY, leafColor, trunkColor)
            PlantState.YOUNG -> drawYoungPlant(centerX, bottomY, leafColor, trunkColor)
            PlantState.FULL -> drawFullTree(centerX, bottomY, leafColor, trunkColor)
            PlantState.WITHERED -> drawFullTree(centerX, bottomY, WitheredBrown, WitheredBrownDark)
        }
    }
}

private fun DrawScope.drawSeed(centerX: Float, bottomY: Float, color: Color) {
    // Draw a simple seed shape
    val seedWidth = size.width * 0.15f
    val seedHeight = size.height * 0.12f
    val seedY = bottomY - seedHeight - size.height * 0.05f

    drawOval(
        color = color,
        topLeft = Offset(centerX - seedWidth, seedY),
        size = androidx.compose.ui.geometry.Size(seedWidth * 2, seedHeight)
    )

    // Small sprout emerging
    drawLine(
        color = TreeGreen,
        start = Offset(centerX, seedY),
        end = Offset(centerX, seedY - size.height * 0.05f),
        strokeWidth = 3f
    )
}

private fun DrawScope.drawSprout(centerX: Float, bottomY: Float, leafColor: Color, trunkColor: Color) {
    val stemHeight = size.height * 0.25f
    val stemBottom = bottomY - size.height * 0.05f

    // Draw stem
    drawLine(
        color = trunkColor,
        start = Offset(centerX, stemBottom),
        end = Offset(centerX, stemBottom - stemHeight),
        strokeWidth = 4f
    )

    // Draw two small leaves
    val leafSize = size.width * 0.15f
    val leafY = stemBottom - stemHeight

    // Left leaf
    val leftLeafPath = Path().apply {
        moveTo(centerX, leafY)
        quadraticTo(
            centerX - leafSize * 1.5f, leafY - leafSize * 0.5f,
            centerX - leafSize, leafY - leafSize
        )
        quadraticTo(
            centerX - leafSize * 0.5f, leafY - leafSize * 0.5f,
            centerX, leafY
        )
        close()
    }
    drawPath(leftLeafPath, leafColor)

    // Right leaf
    val rightLeafPath = Path().apply {
        moveTo(centerX, leafY)
        quadraticTo(
            centerX + leafSize * 1.5f, leafY - leafSize * 0.5f,
            centerX + leafSize, leafY - leafSize
        )
        quadraticTo(
            centerX + leafSize * 0.5f, leafY - leafSize * 0.5f,
            centerX, leafY
        )
        close()
    }
    drawPath(rightLeafPath, leafColor)
}

private fun DrawScope.drawYoungPlant(centerX: Float, bottomY: Float, leafColor: Color, trunkColor: Color) {
    val stemHeight = size.height * 0.45f
    val stemBottom = bottomY - size.height * 0.05f

    // Draw stem
    drawLine(
        color = trunkColor,
        start = Offset(centerX, stemBottom),
        end = Offset(centerX, stemBottom - stemHeight),
        strokeWidth = 6f
    )

    // Draw multiple leaf clusters
    val leafRadius = size.width * 0.2f

    // Bottom leaves
    drawCircle(
        color = leafColor,
        radius = leafRadius,
        center = Offset(centerX - leafRadius * 0.8f, stemBottom - stemHeight * 0.4f)
    )
    drawCircle(
        color = leafColor,
        radius = leafRadius,
        center = Offset(centerX + leafRadius * 0.8f, stemBottom - stemHeight * 0.4f)
    )

    // Top leaves
    drawCircle(
        color = leafColor,
        radius = leafRadius * 1.2f,
        center = Offset(centerX, stemBottom - stemHeight - leafRadius * 0.5f)
    )
}

private fun DrawScope.drawFullTree(centerX: Float, bottomY: Float, leafColor: Color, trunkColor: Color) {
    val trunkHeight = size.height * 0.35f
    val trunkWidth = size.width * 0.12f
    val trunkBottom = bottomY - size.height * 0.05f

    // Draw trunk
    drawRect(
        color = trunkColor,
        topLeft = Offset(centerX - trunkWidth, trunkBottom - trunkHeight),
        size = androidx.compose.ui.geometry.Size(trunkWidth * 2, trunkHeight)
    )

    // Draw foliage (multiple overlapping circles for a tree crown)
    val crownCenterY = trunkBottom - trunkHeight - size.height * 0.15f
    val baseRadius = size.width * 0.25f

    // Bottom row of foliage
    drawCircle(
        color = leafColor,
        radius = baseRadius,
        center = Offset(centerX - baseRadius * 0.7f, crownCenterY + baseRadius * 0.3f)
    )
    drawCircle(
        color = leafColor,
        radius = baseRadius,
        center = Offset(centerX + baseRadius * 0.7f, crownCenterY + baseRadius * 0.3f)
    )

    // Middle row
    drawCircle(
        color = leafColor,
        radius = baseRadius * 1.1f,
        center = Offset(centerX, crownCenterY)
    )

    // Top
    drawCircle(
        color = leafColor,
        radius = baseRadius * 0.8f,
        center = Offset(centerX, crownCenterY - baseRadius * 0.6f)
    )
}

@Composable
fun MiniTreeIcon(
    saved: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    PlantVisual(
        plantState = if (saved) PlantState.FULL else PlantState.WITHERED,
        modifier = modifier,
        size = size,
        isWithered = !saved
    )
}
