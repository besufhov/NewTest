package com.kaankivancdilli.summary.utils.reusable.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

@Composable
fun CustomTopBar(title: String) {
    val cornerRadius = 12.dp
    val borderColor = Color.LightGray
    val borderWidth = 0.75.dp
    val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = Modifier
            .zIndex(1f)
            .fillMaxWidth()
           // .statusBarsPadding()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius),
                clip = false
            )
            .clip(RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius))

            .background(Color.White)
            .drawBehind {
                val strokeWidthPx = borderWidth.toPx()
                val cornerRadiusPx = cornerRadius.toPx()

                val outlinePath = Path().apply {
                    // Start from bottom-left with curve
                    moveTo(0f, size.height - cornerRadiusPx)
                    quadraticTo(
                        0f,
                        size.height,
                        cornerRadiusPx,
                        size.height
                    )
                    lineTo(size.width - cornerRadiusPx, size.height)
                    quadraticTo(
                        size.width,
                        size.height,
                        size.width,
                        size.height - cornerRadiusPx
                    )
                }

                drawPath(
                    path = outlinePath,
                    color = borderColor,
                    style = Stroke(width = strokeWidthPx)
                )
            }
            .padding(top = topPadding, bottom = 12.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.align(Alignment.Center),
            style = TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = Color.Black
            )
        )
    }
}