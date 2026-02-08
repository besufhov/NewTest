package com.kaankivancdilli.summary.utils.reusable

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

@Composable
fun CustomTabRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    titles: List<String>
) {
    val tabWidth = remember { mutableStateOf(0f) }

    val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val horizontalPadding = 16.dp
    val density = LocalDensity.current

    val tabWidthInPx = remember(tabWidth.value) {
        with(density) {
            (tabWidth.value - horizontalPadding.toPx() * 2 / titles.size).coerceAtLeast(0f)
        }
    }

    val animatedOffsetX by animateDpAsState(
        targetValue = with(density) { (tabWidthInPx * selectedTabIndex).toDp() },
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "TabOffsetAnimation"
    )

    val cornerRadius = 12.dp
    val borderColor = Color.LightGray
    val borderWidth = 0.75.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f)
            //  .height(40.dp)
            .statusBarsPadding()
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
           // .padding(top = topPadding, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier
              //  .fillMaxSize()
                .fillMaxWidth()
                .padding(top = topPadding, bottom = 12.dp)
        ) {
            titles.forEachIndexed { index, title ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                      //  .fillMaxHeight()
                        .onGloballyPositioned { coordinates ->
                            tabWidth.value = coordinates.size.width.toFloat()
                        }
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            onTabSelected(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = if (selectedTabIndex == index) Color.Black else Color.Gray
                    )
                }
            }
        }

        // ✅ Place indicator INSIDE bottom of this Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(horizontal = horizontalPadding)
                .height(3.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Box(
                modifier = Modifier
                    .width(with(density) { tabWidthInPx.toDp() })
                    .height(3.dp)
                    .offset { IntOffset(animatedOffsetX.roundToPx(), 0) }
                    .background(Color.Black, RoundedCornerShape(50))
            )
        }
    }
}