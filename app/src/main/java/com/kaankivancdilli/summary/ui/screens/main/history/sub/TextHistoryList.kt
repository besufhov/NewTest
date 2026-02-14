package com.kaankivancdilli.summary.ui.screens.main.history.sub

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.kaankivancdilli.summary.ui.component.history.dismiss.DismissibleHistoryItem
import com.kaankivancdilli.summary.ui.viewmodel.main.history.TextHistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TextHistoryList(
    navController: NavController,
    viewModel: TextHistoryViewModel,
    listState: LazyListState
) {
    val saveTexts by viewModel.saveTexts.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        state = listState) {
        val reversedTexts = saveTexts.reversed()
        itemsIndexed(reversedTexts, key = { _, it -> "text_${it.id}" }) { index, message ->
            val formattedDate = remember(message.timestamp) {
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(message.timestamp))
            }

            DismissibleHistoryItem(
                message = message,
                onClick = { section ->
                    navController.navigate("summaryScreen/${message.id}/${Uri.encode(message.ocrText)}/$section")
                },
                onDelete = { viewModel.deleteMessage(message) },
                timestamp = formattedDate,
                isFirstBox = index == 0
            )
        }
    }
}