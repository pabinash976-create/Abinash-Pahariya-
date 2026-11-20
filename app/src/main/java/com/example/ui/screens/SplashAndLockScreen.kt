package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.WolfLogoEmblem
import com.example.ui.theme.BrightGold
import com.example.ui.theme.BrushedSilver
import com.example.ui.theme.BurnishedGold
import com.example.ui.theme.CoralError
import com.example.ui.theme.DeepMidnightNavy
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WolfSplashScreen(
    onFinished: () -> Unit
) {
    var completed by remember { mutableStateOf(false) }
    val finishOnce = {
        if (!completed) {
            completed = true
            onFinished()
        }
    }

    val logoScale = remember { Animatable(0.70f) }
    val logoAlpha = remember { Animatable(0f) }
    val haloSweep = remember { Animatable(0f) }
    val pulseRadius = remember { Animatable(0.65f) }
    val pulseAlpha = remember { Animatable(0.75f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleTracking = remember { Animatable(10f) }
    val dividerProgress = remember { Animatable(0f) }
    val mottoAlpha = remember { Animatable(0f) }
    val mottoOffsetY = remember { Animatable(24f) }
    val sheenOffset = remember { Animatable(-1.4f) }
    val railProgress = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_ambient")
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_angle"
    )
    val emberPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ember_phase"
    )

    LaunchedEffect(Unit) {
        launch {
            logoAlpha.animateTo(1f, tween(durationMillis = 480, easing = FastOutSlowInEasing))
        }
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            haloSweep.animateTo(1f, tween(durationMillis = 950, easing = FastOutSlowInEasing))
        }
        launch {
            delay(220L)
            pulseRadius.animateTo(1.55f, tween(durationMillis = 1050, easing = FastOutSlowInEasing))
        }
        launch {
            delay(220L)
            pulseAlpha.animateTo(0f, tween(durationMillis = 1050, easing = FastOutSlowInEasing))
        }
        launch {
            delay(360L)
            titleAlpha.animateTo(1f, tween(durationMillis = 520, easing = FastOutSlowInEasing))
        }
        launch {
            delay(360L)
            titleTracking.animateTo(4.5f, tween(durationMillis = 680, easing = FastOutSlowInEasing))
        }
        launch {
            delay(560L)
            dividerProgress.animateTo(1f, tween(durationMillis = 500, easing = FastOutSlowInEasing))
        }
        launch {
            delay(740L)
            mottoAlpha.animateTo(1f, tween(durationMillis = 550, easing = FastOutSlowInEasing))
        }
        launch {
            delay(740L)
            mottoOffsetY.animateTo(0f, tween(durationMillis = 550, easing = FastOutSlowInEasing))
        }
        launch {
            delay(850L)
            sheenOffset.animateTo(1.6f, tween(durationMillis = 800, easing = FastOutSlowInEasing))
        }
        launch {
            railProgress.animateTo(1f, tween(durationMillis = 2250, easing = FastOutSlowInEasing))
        }
        delay(2350L)
        finishOnce()
    }

    val embers = remember {
        listOf(
            Triple(0.14f, 0.85f, 2.5f),
            Triple(0.28f, 0.92f, 1.8f),
            Triple(0.42f, 0.78f, 2.2f),
            Triple(0.58f, 0.95f, 3.0f),
            Triple(0.73f, 0.88f, 2.0f),
            Triple(0.86f, 0.80f, 2.6f),
            Triple(0.20f, 0.45f, 1.6f),
            Triple(0.81f, 0.40f, 1.9f),
            Triple(0.50f, 0.22f, 2.1f)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF142E54), Color(0xFF0A1B34), DeepMidnightNavy),
                    radius = 1350f
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = finishOnce
            )
            .safeDrawingPadding()
            .testTag("wolf_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Cinematic ambient gold & silver ember field
        Canvas(modifier = Modifier.fillMaxSize()) {
            embers.forEachIndexed { idx, (xFrac, yStart, radiusDp) ->
                val drift = ((yStart - emberPhase * 0.35f + idx * 0.11f) % 1f + 1f) % 1f
                val alphaFactor = kotlin.math.sin(drift * kotlin.math.PI).toFloat().coerceIn(0f, 1f)
                val emberColor = if (idx % 2 == 0) BrightGold else BrushedSilver
                drawCircle(
                    color = emberColor.copy(alpha = 0.28f * alphaFactor * logoAlpha.value),
                    radius = radiusDp.dp.toPx(),
                    center = Offset(size.width * xFrac, size.height * drift)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Original Wolf Crest Shield centered inside spotlight & counter-rotating orbital arcs
            Box(
                modifier = Modifier.size(268.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val baseRadius = size.minDimension * 0.41f

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                BrightGold.copy(alpha = 0.16f * logoAlpha.value),
                                Color(0xFF1A3A6A).copy(alpha = 0.28f * logoAlpha.value),
                                Color.Transparent
                            ),
                            center = center,
                            radius = baseRadius * 1.45f
                        ),
                        center = center,
                        radius = baseRadius * 1.45f
                    )

                    if (pulseAlpha.value > 0.01f) {
                        drawCircle(
                            color = BrightGold.copy(alpha = pulseAlpha.value * 0.55f),
                            radius = baseRadius * pulseRadius.value * 0.92f,
                            center = center,
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                    }

                    val outerArcSize = Size(baseRadius * 2.24f, baseRadius * 2.24f)
                    val outerTopLeft = Offset(center.x - outerArcSize.width / 2f, center.y - outerArcSize.height / 2f)
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Color.Transparent, BurnishedGold.copy(alpha = 0.65f), BrightGold, Color.Transparent),
                            center = center
                        ),
                        startAngle = orbitAngle - 90f,
                        sweepAngle = 210f * haloSweep.value,
                        useCenter = false,
                        topLeft = outerTopLeft,
                        size = outerArcSize,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )

                    val innerArcSize = Size(baseRadius * 1.98f, baseRadius * 1.98f)
                    val innerTopLeft = Offset(center.x - innerArcSize.width / 2f, center.y - innerArcSize.height / 2f)
                    drawArc(
                        color = BrushedSilver.copy(alpha = 0.35f * haloSweep.value),
                        startAngle = -orbitAngle + 45f,
                        sweepAngle = 150f * haloSweep.value,
                        useCenter = false,
                        topLeft = innerTopLeft,
                        size = innerArcSize,
                        style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Box(
                    modifier = Modifier
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                        .size(196.dp)
                        .clip(RoundedCornerShape(36.dp))
                        .background(Color(0xFF102039))
                        .border(
                            width = 2.2.dp,
                            brush = Brush.linearGradient(listOf(BrightGold, BrushedSilver, BurnishedGold)),
                            shape = RoundedCornerShape(36.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_wolf_logo),
                        contentDescription = stringResource(R.string.cd_wolf_logo),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        contentScale = ContentScale.Fit
                    )

                    // Diagonal metallic light sheen sweeping once across the shield frame
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val xCenter = size.width * sheenOffset.value
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.18f),
                                    BrightGold.copy(alpha = 0.12f),
                                    Color.Transparent
                                ),
                                start = Offset(xCenter - size.width * 0.45f, 0f),
                                end = Offset(xCenter + size.width * 0.45f, size.height)
                            ),
                            cornerRadius = CornerRadius(36.dp.toPx(), 36.dp.toPx())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Iconic Brand Display
            Text(
                text = stringResource(R.string.brand_title),
                style = MaterialTheme.typography.displayLarge.copy(
                    letterSpacing = titleTracking.value.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                modifier = Modifier.alpha(titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Expanding Gold & Silver Horizon Divider
            Box(
                modifier = Modifier
                    .width(210.dp * dividerProgress.value)
                    .height(2.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, BrushedSilver, BrightGold, BrushedSilver, Color.Transparent)
                        )
                    )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Motivational Identity Reveal
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    alpha = mottoAlpha.value
                    translationY = mottoOffsetY.value
                }
            ) {
                Text(
                    text = stringResource(R.string.brand_motto),
                    style = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = 1.8.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = BrightGold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.brand_tagline),
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.2.sp),
                    color = BrushedSilver,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Sleek Cinematic Progress Rail + Tap Hint
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .alpha(titleAlpha.value)
            ) {
                Box(
                    modifier = Modifier
                        .width(132.dp)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(BrushedSilver.copy(alpha = 0.20f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(railProgress.value.coerceIn(0f, 1f))
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(listOf(BurnishedGold, BrightGold, BrushedSilver))
                            )
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "TAP TO ENTER",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.6.sp),
                    color = BrushedSilver.copy(alpha = 0.65f)
                )
            }
        }
    }
}

@Composable
fun PrivacyPinLockScreen(
    onVerifyPin: (String) -> Boolean
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepMidnightNavy)
            .safeDrawingPadding()
            .padding(24.dp)
            .testTag("privacy_pin_lock_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        WolfLogoEmblem(size = 84.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = BrightGold,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "WOLF STUDY VAULT",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Enter your 4-digit study PIN",
            style = MaterialTheme.typography.bodyMedium,
            color = BrushedSilver
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) { index ->
                val filled = index < enteredPin.length
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (filled) BrightGold else NavySurface)
                        .border(1.5.dp, if (filled) BrightGold else BrushedSilver, CircleShape)
                )
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage!!,
                style = MaterialTheme.typography.bodySmall,
                color = CoralError
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "DEL")
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    row.forEach { key ->
                        when (key) {
                            "" -> Spacer(modifier = Modifier.size(72.dp))
                            "DEL" -> FilledTonalButton(
                                onClick = {
                                    if (enteredPin.isNotEmpty()) {
                                        enteredPin = enteredPin.dropLast(1)
                                        errorMessage = null
                                    }
                                },
                                modifier = Modifier
                                    .size(72.dp)
                                    .testTag("pin_key_backspace"),
                                shape = CircleShape,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = NavySurfaceElevated,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace"
                                )
                            }
                            else -> FilledTonalButton(
                                onClick = {
                                    if (enteredPin.length < 4) {
                                        val next = enteredPin + key
                                        enteredPin = next
                                        errorMessage = null
                                        if (next.length == 4) {
                                            if (!onVerifyPin(next)) {
                                                errorMessage = "Incorrect PIN. Try again."
                                                enteredPin = ""
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(72.dp)
                                    .testTag("pin_key_$key"),
                                shape = CircleShape,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = NavySurface,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.headlineMedium,
                                    shapeCheck = Unit
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Text(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    shapeCheck: Unit
) {
    androidx.compose.material3.Text(text = text, style = style)
}
