package com.kaankivancdilli.summary.ui.screens.main.history


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.ui.viewmodel.main.history.TextHistoryViewModel
import com.kaankivancdilli.summary.ui.component.admob.ad.AdMobBanner
import com.kaankivancdilli.summary.ui.component.reusable.bar.CustomTabRow
import com.kaankivancdilli.summary.ui.screens.main.history.sub.AnythingHistoryList
import com.kaankivancdilli.summary.ui.screens.main.history.sub.TextHistoryList

@Composable
fun HistoryScreen(navController: NavController, viewModel: TextHistoryViewModel = hiltViewModel()) {

    val isSubscribed by viewModel.isSubscribed.collectAsState()

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

    val textHistoryListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val anythingHistoryListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
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
        ) {
            if (!isSubscribed) {
                Spacer(modifier = Modifier.height(6.dp))
                AdMobBanner()

            } else {
                Spacer(modifier = Modifier.height(0.dp))
            }
            when (selectedTabIndex) {

                0 -> AnythingHistoryList(navController, viewModel, listState = anythingHistoryListState)
                1 -> TextHistoryList(navController, viewModel, listState = textHistoryListState)
            }
        }
    }
}