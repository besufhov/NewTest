package com.kaankivancdilli.summary.utils.keyboard

import android.graphics.Rect
import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp

@Composable
fun rememberKeyboardHeightDp(): Dp {
    val view = LocalView.current
    val density = LocalDensity.current
    val keyboardHeight = remember { mutableStateOf(0) }

    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val r = Rect()
            view.getWindowVisibleDisplayFrame(r)
            val screenHeight = view.rootView.height
            val heightDiff = screenHeight - r.height()

            // Only consider it keyboard if height diff is significant
            if (heightDiff > screenHeight * 0.15) {
                keyboardHeight.value = heightDiff
            } else {
                keyboardHeight.value = 0
            }
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }
    return with(density) { keyboardHeight.value.toDp() }
}