package com.bhanu.rupeegarden.ui.screens.impulse

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bhanu.rupeegarden.ui.components.QuestionCard
import com.bhanu.rupeegarden.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Reassurance phrases - non-directive, supportive
private val reassurancePhrases = listOf(
    "This feeling will pass.",
    "You don't have to decide right now.",
    "Waiting is enough."
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImpulseCheckScreen(
    onComplete: (resisted: Boolean, xpEarned: Int, amountSaved: Double) -> Unit,
    onBack: () -> Unit,
    viewModel: ImpulseCheckViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle completion - navigate back silently
    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        AnimatedContent(
            targetState = uiState.currentStep,
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) togetherWith
                        fadeOut(animationSpec = tween(400))
            },
            label = "step_transition"
        ) { step ->
            when (step) {
                ImpulseCheckStep.BREATHING -> BreathingStep(
                    timerSeconds = uiState.timerSeconds,
                    isComplete = uiState.isTimerComplete,
                    onContinue = viewModel::onTimerComplete,
                    modifier = Modifier.padding(paddingValues)
                )

                ImpulseCheckStep.FIRST_CHECK -> FirstCheckStep(
                    onResisted = viewModel::onResistedEarly,
                    onStillConsidering = viewModel::onStillConsidering,
                    modifier = Modifier.padding(paddingValues)
                )

                ImpulseCheckStep.QUESTIONS -> QuestionsStep(
                    isEssential = uiState.isEssential,
                    ownsSimilar = uiState.ownsSimilar,
                    canWait = uiState.canWait,
                    onIsEssentialChange = viewModel::updateIsEssential,
                    onOwnsSimilarChange = viewModel::updateOwnsSimilar,
                    onCanWaitChange = viewModel::updateCanWait,
                    onContinue = viewModel::onQuestionsComplete,
                    allAnswered = uiState.areAllQuestionsAnswered,
                    modifier = Modifier.padding(paddingValues)
                )

                ImpulseCheckStep.FEEDBACK -> FeedbackStep(
                    message = uiState.feedbackMessage,
                    onContinue = viewModel::onFeedbackAcknowledged,
                    modifier = Modifier.padding(paddingValues)
                )

                ImpulseCheckStep.FINAL_CHECK -> FinalCheckStep(
                    onResisted = viewModel::onFinalResisted,
                    onSpent = viewModel::onFinalSpent,
                    isLoading = uiState.isLoading,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun BreathingStep(
    timerSeconds: Int,
    isComplete: Boolean,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (30 - timerSeconds) / 30f

    // Random reassurance phrase (stable per session)
    val reassurancePhrase = remember { reassurancePhrases.random() }

    // Breathing animation - 10 seconds per cycle (~6 breaths per minute)
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    // Subtle background glow animation - very slow (20 seconds per cycle)
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.03f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Subtle radial glow background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2 - 40.dp.toPx())
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GreenPrimary.copy(alpha = glowAlpha),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.minDimension * 0.6f
                ),
                center = center,
                radius = size.minDimension * 0.6f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // "I feel like spending" - smaller, softer
            Text(
                text = "I feel like spending",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Timer - main focus with breathing rhythm
            Box(
                modifier = Modifier.size((220 * breathScale).dp),
                contentAlignment = Alignment.Center
            ) {
                CalmTimer(
                    progress = progress,
                    seconds = timerSeconds,
                    breathScale = breathScale,
                    modifier = Modifier.size(220.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Reassurance copy - subtle, not instructional
            Text(
                text = reassurancePhrase,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Continue button - only after timer completes
            AnimatedVisibility(
                visible = isComplete,
                enter = fadeIn(animationSpec = tween(600)),
                exit = fadeOut()
            ) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary
                    )
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CalmTimer(
    progress: Float,
    seconds: Int,
    breathScale: Float,
    modifier: Modifier = Modifier
) {
    // Smooth the progress animation
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = EaseInOutSine),
        label = "smooth_progress"
    )

    val sweepAngle = animatedProgress * 360f

    // Hide numeric countdown in last 10 seconds
    val showSeconds = seconds > 10

    // De-emphasized text color for the countdown
    val textAlpha = if (showSeconds) 0.4f else 0f

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 6.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            // Background circle - very subtle
            drawCircle(
                color = Color.Gray.copy(alpha = 0.1f),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Progress arc - the main visual indicator
            drawArc(
                color = GreenPrimary.copy(alpha = 0.8f),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Smooth clock hand with eased movement
            val handAngle = (sweepAngle - 90) * (PI / 180).toFloat()
            val handLength = radius * 0.65f
            val handEnd = Offset(
                center.x + handLength * cos(handAngle),
                center.y + handLength * sin(handAngle)
            )

            drawLine(
                color = GreenPrimary.copy(alpha = 0.7f),
                start = center,
                end = handEnd,
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Center dot
            drawCircle(
                color = GreenPrimary.copy(alpha = 0.8f),
                radius = 5.dp.toPx(),
                center = center
            )
        }

        // De-emphasized countdown - fades out in last 10 seconds
        AnimatedVisibility(
            visible = showSeconds,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            Text(
                text = "${seconds}s",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha),
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
private fun FirstCheckStep(
    onResisted: () -> Unit,
    onStillConsidering: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Do you still want to buy?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onResisted,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary
            )
        ) {
            Text(
                text = "No, I resisted",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onStillConsidering,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "I'm still considering",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun QuestionsStep(
    isEssential: Boolean?,
    ownsSimilar: Boolean?,
    canWait: Boolean?,
    onIsEssentialChange: (Boolean) -> Unit,
    onOwnsSimilarChange: (Boolean) -> Unit,
    onCanWaitChange: (Boolean) -> Unit,
    onContinue: () -> Unit,
    allAnswered: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        QuestionCard(
            question = "Is this essential?",
            yesLabel = "Yes",
            noLabel = "No",
            selectedAnswer = isEssential,
            onAnswerSelected = onIsEssentialChange
        )

        QuestionCard(
            question = "Do you already own something similar?",
            selectedAnswer = ownsSimilar,
            onAnswerSelected = onOwnsSimilarChange
        )

        QuestionCard(
            question = "Can this wait a week?",
            selectedAnswer = canWait,
            onAnswerSelected = onCanWaitChange
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onContinue,
            enabled = allAnswered,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary
            )
        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FeedbackStep(
    message: String,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary
            )
        ) {
            Text(
                text = "Okay",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FinalCheckStep(
    onResisted: () -> Unit,
    onSpent: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "What did you decide?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onResisted,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "I resisted",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onSpent,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "I still spent",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
