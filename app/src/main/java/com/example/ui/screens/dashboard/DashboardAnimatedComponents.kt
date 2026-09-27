package com.example.ui.screens.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.WarningAmber
import com.example.util.FormatUtils
import java.util.Calendar
import kotlin.math.sin

/**
 * Animated number counter that smoothly rolls up to target amount.
 */
@Composable
fun AnimatedCurrencyText(
    targetAmount: Double,
    currency: String,
    fontSize: TextUnit = 32.sp,
    fontWeight: FontWeight = FontWeight.ExtraBold,
    color: Color = Color.White,
    isBalanceHidden: Boolean = false,
    modifier: Modifier = Modifier
) {
    var animatedValue by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(targetAmount) {
        val anim = Animatable(animatedValue)
        anim.animateTo(
            targetValue = targetAmount.toFloat(),
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        ) {
            animatedValue = value
        }
    }

    AnimatedContent(
        targetState = isBalanceHidden,
        transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
        label = "BalanceVisibilityAnimation",
        modifier = modifier
    ) { hidden ->
        if (hidden) {
            Text(
                text = "••••••••",
                fontSize = fontSize,
                fontWeight = fontWeight,
                color = color.copy(alpha = 0.85f),
                letterSpacing = 4.sp
            )
        } else {
            Text(
                text = FormatUtils.formatCurrency(animatedValue.toDouble(), currency),
                fontSize = fontSize,
                fontWeight = fontWeight,
                color = color
            )
        }
    }
}

/**
 * Tactile spring press modifier for delightful button physics.
 */
fun Modifier.bouncyClick(onClick: () -> Unit): Modifier = this.pointerInput(Unit) {
    val scale = Animatable(1f)
    while (true) {
        awaitPointerEventScope {
            awaitFirstDown(requireUnconsumed = false)
            // Pressed state - shrink gently
        }
        scale.animateTo(
            targetValue = 0.93f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh)
        )
        // Wait for release
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
        onClick()
    }
}

/**
 * Animated Pulsing Live Dot / Radar Ripple Indicator.
 */
@Composable
fun AnimatedLivePulseIndicator(
    color: Color = IncomeGreen,
    size: Dp = 10.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    Box(
        modifier = Modifier.size(size * 2),
        contentAlignment = Alignment.Center
    ) {
        // Expanding Ripple Ring
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                    alpha = pulseAlpha
                }
                .background(color, CircleShape)
        )
        // Core Solid Dot
        Box(
            modifier = Modifier
                .size(size)
                .background(color, CircleShape)
        )
    }
}

/**
 * Dynamic Time-based animated greeting with sun/moon icon.
 */
@Composable
fun AnimatedTimeGreeting(
    userName: String,
    isGujarati: Boolean
) {
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }

    val (greetingText, iconVector, iconColor) = remember(currentHour, isGujarati) {
        when (currentHour) {
            in 5..11 -> {
                val text = if (isGujarati) "સુપ્રભાત" else "Good Morning"
                Triple(text, Icons.Default.WbSunny, Color(0xFFFBBF24))
            }
            in 12..16 -> {
                val text = if (isGujarati) "શુભ બપોર" else "Good Afternoon"
                Triple(text, Icons.Default.Brightness4, Color(0xFFF59E0B))
            }
            in 17..21 -> {
                val text = if (isGujarati) "શુભ સાંજ" else "Good Evening"
                Triple(text, Icons.Default.WbSunny, Color(0xFFFB923C))
            }
            else -> {
                val text = if (isGujarati) "શુભ રાત્રી" else "Good Night"
                Triple(text, Icons.Default.NightsStay, Color(0xFF818CF8))
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "GreetingFloating")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatGreetingIcon"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$greetingText, $userName",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = iconVector,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer { translationY = floatOffset }
        )
    }
}

/**
 * Animated Cashflow Sparkline Wave Canvas.
 * Generates an animated gradient wave chart showing financial momentum.
 */
@Composable
fun AnimatedCashflowWave(
    income: Double,
    expense: Double,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(income, expense) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "WaveShimmer")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )

    val waveColor = if (income >= expense) IncomeGreen else WarningAmber

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        if (width <= 0 || height <= 0) return@Canvas

        val progress = animProgress.value
        val path = Path()
        val fillPath = Path()

        val pointsCount = 24
        val step = width / (pointsCount - 1)

        val baseRatio = if (income + expense > 0) {
            (income / (income + expense)).toFloat().coerceIn(0.2f, 0.8f)
        } else {
            0.5f
        }

        var startY = 0f
        for (i in 0 until pointsCount) {
            val x = i * step
            val normalizedX = (i.toFloat() / pointsCount) * (2 * Math.PI).toFloat()
            // Dynamic multi-frequency wave
            val sinVal = sin(normalizedX + waveOffset) * 0.25f + sin(normalizedX * 2 - waveOffset * 0.5f) * 0.15f
            val rawY = height * (1f - (baseRatio + sinVal.toFloat()))
            val y = height * (1f - progress) + rawY * progress

            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
                startY = y
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(width, height)
        fillPath.close()

        // Draw gradient area below wave
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    waveColor.copy(alpha = 0.25f * progress),
                    waveColor.copy(alpha = 0.0f)
                ),
                startY = 0f,
                endY = height
            )
        )

        // Draw line stroke
        drawPath(
            path = path,
            color = waveColor.copy(alpha = 0.9f),
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        // Draw pulsing indicator point at the latest data point
        val lastX = width * progress
        val lastY = startY // approximated end
        drawCircle(
            color = Color.White,
            radius = 4.dp.toPx(),
            center = Offset(width, height * (1f - baseRatio))
        )
        drawCircle(
            color = waveColor,
            radius = 2.dp.toPx(),
            center = Offset(width, height * (1f - baseRatio))
        )
    }
}

/**
 * Animated Financial Health Bar with sweep progress.
 */
@Composable
fun AnimatedFinancialHealthScore(
    income: Double,
    expense: Double,
    isGujarati: Boolean,
    modifier: Modifier = Modifier
) {
    val total = income + expense
    val ratio = if (total > 0) ((income / total) * 100).toInt().coerceIn(0, 100) else 50
    val targetProgress = ratio / 100f

    val animProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "HealthProgress"
    )

    val (statusLabel, statusColor) = when {
        ratio >= 65 -> {
            (if (isGujarati) "ઉત્તમ કેશફ્લો (મજબૂત નફો)" else "Strong Cashflow (Profitable)") to IncomeGreen
        }
        ratio >= 45 -> {
            (if (isGujarati) "સરેરાશ સંતુલન (સ્થિર)" else "Balanced Ledger (Stable)") to WarningAmber
        }
        else -> {
            (if (isGujarati) "ખર્ચ વધી રહ્યો છે (સાવધાન)" else "High Expense Alert") to Color(0xFFEF4444)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = Color(0xFF0F172A).copy(alpha = 0.6f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isGujarati) "નાણાકીય સ્થિતિ" else "Financial Health",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFCBD5E1)
                    )
                }

                Text(
                    text = "$ratio% ${if (income >= expense) "↑" else "↓"}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Animated Gradient Progress Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animProgress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF2DD4BF),
                                    statusColor
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = statusLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = statusColor
            )
        }
    }
}
