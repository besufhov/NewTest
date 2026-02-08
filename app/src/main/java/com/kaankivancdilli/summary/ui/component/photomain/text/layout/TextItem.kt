package com.kaankivancdilli.summary.ui.component.photomain.text.layout

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaankivancdilli.summary.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TextItem(
    text: String,
    index: Int,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val density = LocalDensity.current
    val screenPx = with(density) { screenWidth.toPx() }
    val thresholdPx = screenPx * 0.75f // 75% swipe required

    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { thresholdPx },
        confirmValueChange = { newValue ->
            if (newValue != SwipeToDismissBoxValue.Settled) onDelete()
            newValue == SwipeToDismissBoxValue.Settled
        }
    )

    val copiedLabel = stringResource(R.string.copied)

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            if (dismissState.dismissDirection != SwipeToDismissBoxValue.Settled) {
                Box(

                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)

                    )
                }
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(1.dp)
                .background(Color.White)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                color = Color.Black,
                maxLines = Int.MAX_VALUE, // 🔥 Allows unlimited lines
                softWrap = true // 🔥 Enables wrapping
            )


            // ✅ Copy Button (Inside Gray Background)
            IconButton(onClick = {
                clipboardManager.setText(AnnotatedString(text))
                Toast.makeText(context, copiedLabel, Toast.LENGTH_SHORT).show()
            }) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy, // ✅ Corrected here
                    contentDescription = "Copy",
                    tint = Color.Black
                )
            }
        }
    }
}
