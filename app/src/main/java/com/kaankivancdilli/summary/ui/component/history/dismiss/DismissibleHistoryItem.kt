package com.kaankivancdilli.summary.ui.component.history.dismiss

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.data.model.local.SaveAnything
import com.kaankivancdilli.summary.ui.component.history.HistoryItem
import com.kaankivancdilli.summary.ui.screens.sub.history.type.Type

@Composable
fun <T> DismissibleHistoryItem(
    message: T,
    onClick: (String) -> Unit,
    onDelete: () -> Unit,
    timestamp: String,
    isFirstBox: Boolean = false
) where T : Any {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val density = LocalDensity.current
    val screenPx = with(density) { screenWidth.toPx() }
    val thresholdPx = screenPx * 0.75f

    val deleteHistoryTextLabel = stringResource(R.string.delete_text)
    val deleteHistoryTextAskLabel = stringResource(R.string.delete_ask_text)

    val yesLabel = stringResource(R.string.yes)
    val noLabel = stringResource(R.string.no)

    var showDialog by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { thresholdPx },
        confirmValueChange = { newValue ->
            if (newValue == SwipeToDismissBoxValue.EndToStart) {
                showDialog = true
                false
            } else {
                true
            }
        }
    )

    val typeText = when (message) {
        is SaveAnything -> message.type
        else -> null
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = Color.LightGray,
            title = { Text(text = deleteHistoryTextLabel, color = Color.Black) },
            text = { Text(text = deleteHistoryTextAskLabel, color = Color.Black) },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    onDelete()
                },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                )
                    ) {
                    Text(yesLabel)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                 },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                        ) {
                    Text(noLabel)
                }
            }
        )
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    ) {
        val enumType = try {
            Type.valueOf(typeText?.uppercase()?.replace("_", "") ?: "")
        } catch (e: IllegalArgumentException) {
            Type.TVSHOW
        }

        HistoryItem(
            message = message,
            onClick = { section -> onClick(section) },
            timestamp = timestamp,
            enumType = enumType,
            isFirstBox = isFirstBox,
        )
    }
}