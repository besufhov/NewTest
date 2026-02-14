package com.kaankivancdilli.summary.ui.screens.main.anything.screen

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaankivancdilli.summary.ui.component.audio.tts.remembertts.rememberTextToSpeech
import com.kaankivancdilli.summary.ui.viewmodel.main.anything.AnythingViewModel
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.ui.viewmodel.sub.subscription.SubscriptionViewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kaankivancdilli.summary.ui.screens.main.anything.action.ActionSection
import com.kaankivancdilli.summary.ui.screens.main.anything.fields.getLocalizedActionFields
import com.kaankivancdilli.summary.ui.screens.main.anything.request.sendRequest
import com.kaankivancdilli.summary.ui.component.admob.ad.AdMobBanner
import com.kaankivancdilli.summary.ui.component.admob.adhandler.InterstitialAdHandler
import com.kaankivancdilli.summary.core.review.requestInAppReview
import com.kaankivancdilli.summary.ui.component.reusable.bar.CustomTopBar
import com.kaankivancdilli.summary.ui.component.reusable.popup.SubscribeDialog
import com.kaankivancdilli.summary.ui.screens.main.anything.values.updateInputValues
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun AnythingScreen(anythingViewModel: AnythingViewModel = hiltViewModel()) {

    val isSubscribed by anythingViewModel.isSubscribed.collectAsState()

    LaunchedEffect(Unit) {
        val actuallySubscribed = anythingViewModel.subscriptionChecker.isUserSubscribed()
        if (actuallySubscribed != isSubscribed) {
            anythingViewModel.setSubscriptionStatus(actuallySubscribed)
        }
    }

    var inputValues by rememberSaveable { mutableStateOf(mutableMapOf<String, MutableMap<String, String>>()) }
    var responseTexts by rememberSaveable { mutableStateOf(mutableMapOf<String, String>()) }
    var selectedLanguage by rememberSaveable { mutableStateOf(Locale.getDefault()) }
    var loadingStates by rememberSaveable { mutableStateOf(mapOf<String, Boolean>()) }

    val summary = stringResource(R.string.summary)
    val anythingScreenLabel = stringResource(R.string.anything_screen_title)
    val ttsState = rememberTextToSpeech(LocalContext.current)
    val latestResponse by anythingViewModel.saveAnything.collectAsState()
    val countdownMap by anythingViewModel.countdownTimers.collectAsState()
    val activity = LocalActivity.current
    val subscriptionViewModel: SubscriptionViewModel = hiltViewModel()
    val showSubscribeDialog by anythingViewModel.showSubscribeDialog.collectAsState()
    val processingAction by anythingViewModel.selectedActionType.collectAsState()
    val actionFields = getLocalizedActionFields()
    val actionKeys = actionFields.keys.toList()
    val showAd by anythingViewModel.showInterstitialAd.collectAsState()


    if (activity != null) {
        InterstitialAdHandler(
            showAd = showAd,
            activity = activity,
            onAdDismissed = {
                anythingViewModel.continueAfterAd()
            },
            onAdHandled = {
                anythingViewModel.resetInterstitialAdTrigger()
            }
        )
    }

    LaunchedEffect(latestResponse) {
        if (latestResponse.isNotEmpty()) {
            val responseContent = latestResponse.last().summarize

            if (anythingViewModel.isNewResponse(responseContent)) {

                val selectedAction = anythingViewModel.selectedActionType.value
                if (selectedAction != null) {
                    responseTexts = responseTexts.toMutableMap().apply {
                        put(selectedAction, responseContent)
                    }

                    anythingViewModel.setSelectedAction(null)
                    anythingViewModel.startCountdownForAction(selectedAction)
                }
            }

            val usageCount = anythingViewModel.freeUsageTracker.getCount()
            if (usageCount == 17) {
                activity?.let { requestInAppReview(it) }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            CustomTopBar(title = anythingScreenLabel)

        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(start = 16.dp, end = 16.dp)
                .verticalScroll(rememberScrollState())

        ) {
            var selectedActionIndex by rememberSaveable { mutableStateOf(0) }
            val listState = rememberLazyListState()
            val pagerState = rememberPagerState(
                initialPage = selectedActionIndex,
                initialPageOffsetFraction = 0F,
                pageCount = { actionKeys.size },
            )
            LaunchedEffect(pagerState.currentPage) {
                snapshotFlow { pagerState.currentPage }
                    .distinctUntilChanged()
                    .collect { page ->
                        selectedActionIndex = page
                        listState.animateScrollToItem(page)
                    }
            }
            val scrollStates = remember {
                actionKeys.indices.associateWith { ScrollState(0) }
            }

            LaunchedEffect(pagerState.currentPage) {
                scrollStates[pagerState.currentPage]?.let { scrollState ->
                    scrollState.animateScrollTo(0)
                }
            }
            val tabPositions = remember { mutableStateMapOf<Int, Pair<Int, Int>>() }
            val coroutineScope = rememberCoroutineScope()

            if (!isSubscribed) {
                Spacer(modifier = Modifier.height(6.dp))
                AdMobBanner()
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clipToBounds()
            ) {
            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(actionKeys) { index, action ->
                    val isSelected = index == selectedActionIndex
                    Button(
                        onClick = {
                            selectedActionIndex = index
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                                listState.animateScrollToItem(index)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color.White else Color.White,
                            contentColor = if (isSelected) Color.Black else Color.Gray
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier
                            .onGloballyPositioned { coords ->
                                val x = coords.positionInParent().x.roundToInt()
                                val width = coords.size.width
                                tabPositions[index] = x to width
                            }
                    ) {
                        Text(
                            text = action,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            }

            val lazyRowState = rememberLazyListState()
            val pageHeights = remember { mutableStateListOf<Int>().apply {
                repeat(actionKeys.size) { add(0) }
                }
            }

            LaunchedEffect(selectedActionIndex) {
                lazyRowState.animateScrollToItem(selectedActionIndex)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 600.dp, max = 1200.dp)
            )
            {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 600.dp, max = 1200.dp),
                    verticalAlignment = Alignment.Top
                ) { page ->
                    val action = actionKeys[page]
                    val scrollState = scrollStates[page] ?: rememberScrollState()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp)
                            .onGloballyPositioned { coords ->
                                val h = coords.size.height
                                if (pageHeights[page] != h) {
                                    pageHeights[page] = h
                                }
                            },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                        ) {
                            ActionSection(
                                action = action,
                                fields = actionFields[action]?.map { it.first } ?: emptyList(),
                                inputValues = inputValues,
                                responseTexts = responseTexts,
                                onInputChange = { actionKey, field, value ->
                                    inputValues = updateInputValues(inputValues, actionKey, field, value)
                                },
                                onSend = {
                                    anythingViewModel.setSelectedAction(action)
                                    sendRequest(
                                        action = action,
                                        inputValues = inputValues,
                                        actionFields = actionFields.mapValues { entry -> entry.value.map { it.first } },
                                        summary = summary,
                                        viewModel = anythingViewModel,
                                        setLoading = { act, isLoading ->
                                            loadingStates = loadingStates.toMutableMap().apply {
                                                put(act, isLoading)
                                            }
                                        }
                                    )
                                },
                                processingAction = processingAction,
                                ttsState = ttsState,
                                countdownTimers = countdownMap
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSubscribeDialog) {
        Log.d("AnythingScreen", "Displaying Subscribe Dialog")

        val activity = LocalActivity.current

        Dialog(
            onDismissRequest = {
                anythingViewModel.hideSubscribeDialog()
                anythingViewModel.setSelectedAction(null)
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            if (activity != null) {
                SubscribeDialog(
                    onDismiss = {
                        anythingViewModel.hideSubscribeDialog()
                        anythingViewModel.setSelectedAction(null)
                    },
                    onSubscribeClick = {
                        anythingViewModel.subscribeUser(activity, subscriptionViewModel)
                    },
                    onWatchAdsClick = {
                        anythingViewModel.rewardUserWithReset()
                        Log.d("SubscribeDialog", "User earned reward from watching ad")
                    },
                    activity = activity
                )
            } else {
                Log.e("SubscribeDialog", "Activity is null, cannot show dialog properly")
            }
        }
    }
}