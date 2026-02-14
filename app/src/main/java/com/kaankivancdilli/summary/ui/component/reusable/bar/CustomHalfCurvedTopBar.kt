package com.kaankivancdilli.summary.ui.component.reusable.bar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
fun CustomHalfCurvedTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    val cornerRadius = 12.dp
    val borderColor = Color.LightGray
    val borderWidth = 0.75.dp
    val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = Modifier
            .zIndex(1f)
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(bottomEnd = cornerRadius),
                clip = false
            )
            .clip(RoundedCornerShape(bottomEnd = cornerRadius))
            .background(Color.White)
            .drawBehind {
                val strokeWidthPx = borderWidth.toPx()
                val cornerRadiusPx = cornerRadius.toPx()

                val outlinePath = Path().apply {

                    moveTo(0f, size.height)
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
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
                .size(20.dp)
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
        }

        Text(
            text = title,
            style = TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = Color.Black
            ),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}