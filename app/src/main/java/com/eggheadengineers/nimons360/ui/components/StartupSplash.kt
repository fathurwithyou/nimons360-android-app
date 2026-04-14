package com.eggheadengineers.nimons360.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.eggheadengineers.nimons360.ui.theme.TextPrimary
import kotlinx.coroutines.delay

@Composable
fun StartupSplash(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit,
) {
    var entered by remember { mutableStateOf(false) }
    var exiting by remember { mutableStateOf(false) }

    val textScale by animateFloatAsState(
        targetValue = if (entered) 1f else 0.9f,
        animationSpec = tween(durationMillis = 820, easing = FastOutSlowInEasing),
        label = "startupTextScale",
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(durationMillis = 640, easing = LinearOutSlowInEasing),
        label = "startupTextAlpha",
    )
    val overlayAlpha by animateFloatAsState(
        targetValue = if (exiting) 0f else 1f,
        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
        label = "startupOverlayAlpha",
        finishedListener = { value ->
            if (value == 0f) onFinished()
        },
    )

    LaunchedEffect(Unit) {
        entered = true
        delay(1000)
        exiting = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1ECE5))
            .graphicsLayer(alpha = overlayAlpha),
        contentAlignment = Alignment.Center,
    ) {
        NimonsLogo(
            modifier = Modifier.graphicsLayer(
                scaleX = textScale,
                scaleY = textScale,
                alpha = textAlpha,
            ),
        )
    }
}
