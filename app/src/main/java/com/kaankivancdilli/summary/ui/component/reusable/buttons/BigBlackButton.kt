package com.kaankivancdilli.summary.ui.component.reusable.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BigBlackButton(
    text: String,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp,
    horizontalPadding: Dp = 0.dp,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = horizontalPadding, vertical = 2.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.DarkGray,
            disabledContainerColor = Color(0xFFFAFAFA),
            disabledContentColor = Color.LightGray
        ),
        shape = MaterialTheme.shapes.large,
        elevation = ButtonDefaults.buttonElevation(1.dp),
        border = BorderStroke(0.05.dp, Color.LightGray)
    ) {
        Text(
            text = text,
            fontSize = 21.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )
    }
}