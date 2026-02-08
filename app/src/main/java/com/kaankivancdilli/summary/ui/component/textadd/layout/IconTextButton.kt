package com.kaankivancdilli.summary.ui.component.textadd.layout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun IconTextButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center
) {


    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black,
            disabledContentColor = Color.Gray,
            disabledContainerColor = Color.White
        ),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(0.05.dp, Color.LightGray),
        elevation = ButtonDefaults.buttonElevation(1.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = label) // ← removed tint = Color.Black
            Spacer(modifier = Modifier.width(6.dp))
            Text(label) // ← removed color = Color.Black
        }
    }

}

