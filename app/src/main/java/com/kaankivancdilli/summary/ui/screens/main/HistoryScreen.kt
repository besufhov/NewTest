package com.kaankivancdilli.summary.ui.screens.main


import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.ui.component.history.layout.DismissibleHistoryItem
import com.kaankivancdilli.summary.ui.component.history.layout.HistoryItem
import com.kaankivancdilli.summary.ui.viewmodel.TextHistoryViewModel
import com.kaankivancdilli.summary.utils.admob.AdMobBanner
import com.kaankivancdilli.summary.utils.reusable.CustomTabRow
import com.kaankivancdilli.summary.utils.reusable.CustomTopBar
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












