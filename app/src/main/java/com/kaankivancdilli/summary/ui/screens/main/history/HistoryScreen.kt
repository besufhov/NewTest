package com.kaankivancdilli.summary.ui.screens.main.history


import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.ui.component.history.layout.DismissibleHistoryItem
import com.kaankivancdilli.summary.ui.viewmodel.main.history.TextHistoryViewModel
import com.kaankivancdilli.summary.utils.admob.ad.AdMobBanner
import com.kaankivancdilli.summary.utils.reusable.design.CustomTabRow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale





@Composable
fun HistoryScreen(navController: NavController, viewModel: TextHistoryViewModel = hiltViewModel()) {

    val isSubscribed by viewModel.isSubscribed.collectAsState()

    // ✅ This makes sure subscription status is refreshed when screen is shown
    LaunchedEffect(Unit) {
        val actuallySubscribed = viewModel.subscriptionChecker.isUserSubscribed()
        if (actuallySubscribed != isSubscribed) {
            viewModel.setSubscriptionStatus(actuallySubscribed)
        }
    }

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val tabTitles = listOf(
        stringResource(R.string.anything_history),
        stringResource(R.string.text_history)
    )

    // 🧠 Scroll state per tab
    val textHistoryListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val anythingHistoryListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White, // ✅ Prevent background glitches
        topBar = {
            CustomTabRow(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it },
                titles = tabTitles
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
               // .padding(start = 8.dp, end = 8.dp)
            //    .verticalScroll(rememberScrollState())

        ) {
            if (!isSubscribed) {
                Spacer(modifier = Modifier.height(6.dp))
                AdMobBanner()

            } else {
                Spacer(modifier = Modifier.height(0.dp))
            }
            // Content based on tab selection
            when (selectedTabIndex) {

                0 -> AnythingHistoryList(navController, viewModel, listState = anythingHistoryListState)
                1 -> TextHistoryList(navController, viewModel, listState = textHistoryListState)
            }
        }
    }
}



@Composable
fun TextHistoryList(
    navController: NavController,
    viewModel: TextHistoryViewModel,
    listState: LazyListState
) {
    val saveTexts by viewModel.saveTexts.collectAsState()

    LazyColumn(
        modifier = Modifier
       // .padding(horizontal = 10.dp)
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
                isFirstBox = index == 0 // ✅ add this
            )
        }
    }

}



@Composable
fun AnythingHistoryList(
    navController: NavController,
    viewModel: TextHistoryViewModel,
    listState: LazyListState
) {
    val savedAnything by viewModel.saveAnything.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
      //  val reversedTexts = savedAnything.reversed()
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
                isFirstBox = index == 0 // ✅ Pass first item flag
            )
        }
    }

}












