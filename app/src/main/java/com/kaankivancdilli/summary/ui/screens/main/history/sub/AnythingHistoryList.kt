package com.kaankivancdilli.summary.ui.screens.main.history.sub

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
fun AnythingHistoryList(
    navController: NavController,
    viewModel: TextHistoryViewModel,
    listState: LazyListState
) {
    val savedAnything by viewModel.saveAnything.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
        itemsIndexed(savedAnything, key = { index, item -> "anything_${item.id}" }) { index, message ->
            val formattedDate = remember(message.timestamp) {
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(message.timestamp))
            }

            DismissibleHistoryItem(
                message = message,
                onClick = { section ->
                    navController.navigate("full_anything/${message.id}/$section")
                },
                onDelete = { viewModel.deleteAnything(message) },
                timestamp = formattedDate,
                isFirstBox = index == 0
            )
        }
    }
}