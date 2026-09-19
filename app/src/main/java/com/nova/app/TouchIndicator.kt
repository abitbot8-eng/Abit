package com.nova.app

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.unit.dp

/**
 * Optional Touch Visualization Mode.
 *
 * Draws a green dotted circle at your finger inside Nova. It only observes touches on Nova's own
 * screens (it never consumes them and never reads other apps), so no overlay permission is needed.
 * For all apps, Android's own "Show taps" developer option is the supported way.
 */
@Composable
fun TouchIndicatorHost(settings: SettingsStore, content: @Composable () -> Unit) {
    val enabled = settings.touchEnabled
    val anim = LocalAnimations.current
    var target by remember { mutableStateOf(Offset.Zero) }
    var shown by remember { mutableStateOf(Offset.Zero) }
    var down by remember { mutableStateOf(false) }

    LaunchedEffect(enabled) { if (!enabled) down = false }

    // Smoothly glide the circle toward the finger.
    LaunchedEffect(enabled, settings.touchSpeed, anim) {
        if (enabled) {
            val f = if (anim) 0.08f + 0.9f * settings.touchSpeed else 1f
            while (true) {
                withFrameNanos { _ ->
                    val d = target - shown
                    if (d.getDistance() > 0.4f) {
                        shown = shown + d * f
                    } else if (shown != target) {
                        shown = target
                    }
                }
            }
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (down && enabled) settings.touchOpacity else 0f,
        animationSpec = if (anim) tween<Float>(180) else snap<Float>(),
        label = "touchAlpha",
    )
    val grow by animateFloatAsState(
        targetValue = if (down) 1f else 0.55f,
        animationSpec = if (anim) spring<Float>(dampingRatio = 0.55f, stiffness = 500f) else snap<Float>(),
        label = "touchScale",
    )
    val rt = rememberInfiniteTransition(label = "touchRotation")
    val duration = (7000 - 6000 * settings.touchSpeed).toInt().coerceAtLeast(600)
    val rot by rt.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween<Float>(duration, easing = LinearEasing)),
        label = "touchRot",
    )

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(enabled) {
                if (enabled) {
                    awaitPointerEventScope {
                        while (true) {
                            // Initial pass: we only watch. Nothing is consumed, so buttons still work.
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val active = event.changes.firstOrNull { it.pressed }
                            if (active != null) {
                                if (!down) shown = active.position
                                target = active.position
                                down = true
                            } else {
                                down = false
                            }
                        }
                    }
                }
            }
    ) {
        content()
        if (enabled) {
            Canvas(Modifier.fillMaxSize()) {
                if (alpha > 0.01f) {
                    val radius = settings.touchSize.dp.toPx() / 2f * grow
                    val stroke = 4.dp.toPx()
                    rotate(degrees = rot, pivot = shown) {
                        drawCircle(
                            color = TouchGreen.copy(alpha = alpha),
                            radius = radius,
                            center = shown,
                            style = Stroke(
                                width = stroke,
                                cap = StrokeCap.Round,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(1f, 11.dp.toPx())),
                            ),
                        )
                    }
                    drawCircle(color = TouchGreen.copy(alpha = alpha * 0.08f), radius = radius, center = shown)
                    drawCircle(color = TouchGreen.copy(alpha = alpha), radius = 3.dp.toPx(), center = shown)
                }
            }
        }
    }
}
