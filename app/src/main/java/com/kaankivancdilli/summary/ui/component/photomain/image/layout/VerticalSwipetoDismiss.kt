package com.kaankivancdilli.summary.ui.component.photomain.image.layout

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch

@Composable
fun VerticalSwipeToDismiss(
    onSwipeAttempt: () -> Unit,
    animateBack: Boolean = false,
    content: @Composable () -> Unit
) {
    val dismissThreshold = 300f
    val animatableOffsetY = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(0, animatableOffsetY.value.toInt()) }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->
                        if (dragAmount < 0) {
                            coroutineScope.launch {
                                animatableOffsetY.snapTo(animatableOffsetY.value + dragAmount)
                            }
                        }
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            if (animatableOffsetY.value < -dismissThreshold) {
                                // Only call attempt, do NOT animate off-screen yet
                                onSwipeAttempt()

                                // Optionally bring it back for now
                                if (animateBack) {
                                    animatableOffsetY.animateTo(
                                        0f,
                                        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
                                    )
                                }
                            } else {
                                animatableOffsetY.animateTo(0f, animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing))
                            }
                        }
                    }
                )
            }
    ) {
        var zoomed by remember { mutableStateOf(false) }

        Box(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        zoomed = !zoomed
                    }
                )
            }
            .graphicsLayer(
                scaleX = if (zoomed) 2f else 1f,
                scaleY = if (zoomed) 2f else 1f
            )
        ) {
            content()
        }
    }
}
