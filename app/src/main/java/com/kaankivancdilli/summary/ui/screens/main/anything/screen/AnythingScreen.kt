package com.kaankivancdilli.summary.ui.screens.main.anything.screen

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.kaankivancdilli.summary.utils.tts.rememberTextToSpeech
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kaankivancdilli.summary.ui.screens.main.anything.action.ActionSection
import com.kaankivancdilli.summary.ui.screens.main.anything.fields.getLocalizedActionFields
import com.kaankivancdilli.summary.ui.screens.main.anything.request.sendRequest
import com.kaankivancdilli.summary.utils.admob.ad.AdMobBanner
import com.kaankivancdilli.summary.utils.admob.adhandler.InterstitialAdHandler
import com.kaankivancdilli.summary.utils.rate.requestInAppReview
import com.kaankivancdilli.summary.utils.reusable.design.CustomTopBar
import com.kaankivancdilli.summary.utils.reusable.popup.SubscribeDialog
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

import java.util.Locale
import kotlin.math.roundToInt



@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AnythingScreen(anythingViewModel: AnythingViewModel = hiltViewModel()) {

    val isSubscribed by anythingViewModel.isSubscribed.collectAsState()

    // ✅ This makes sure subscription status is refreshed when screen is shown
    LaunchedEffect(Unit) {
        val actuallySubscribed = anythingViewModel.subscriptionChecker.isUserSubscribed()
        if (actuallySubscribed != isSubscribed) {
            anythingViewModel.setSubscriptionStatus(actuallySubscribed)
        }
    }

    var inputValues by rememberSaveable { mutableStateOf(mutableMapOf<String, MutableMap<String, String>>()) }
    var responseTexts by rememberSaveable { mutableStateOf(mutableMapOf<String, String>()) }
    var selectedLanguage by rememberSaveable { mutableStateOf(Locale.getDefault()) }
    val summary = stringResource(R.string.summary)

    val anythingScreenLabel = stringResource(R.string.anything_screen_title)

    var loadingStates by rememberSaveable { mutableStateOf(mapOf<String, Boolean>()) }

    // Detect device language
    val currentLanguage = Locale.getDefault().language

    val ttsState = rememberTextToSpeech(LocalContext.current) // Initialize TTS
    val latestResponse by anythingViewModel.saveAnything.collectAsState()
    val countdownMap by anythingViewModel.countdownTimers.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    var countdownTimers by remember { mutableStateOf(mapOf<String, Int>()) }

    val activity = LocalActivity.current // ✅ MODERN WAY/ <-- Get activity
    val subscriptionViewModel: SubscriptionViewModel = hiltViewModel()

    val showSubscribeDialog by anythingViewModel.showSubscribeDialog.collectAsState()

    val processingAction by anythingViewModel.selectedActionType.collectAsState()

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val actionFields = getLocalizedActionFields()
    val actionKeys = actionFields.keys.toList()

    val showAd by anythingViewModel.showInterstitialAd.collectAsState()


    if (activity != null) {
        InterstitialAdHandler(
            showAd = showAd,
            activity = activity,
         //   onUserEarnedReward = {
                // Just one free usage logic:
           //     anythingViewModel.rewardSingleUsage()
         //   },
            onAdDismissed = {
                anythingViewModel.continueAfterAd()
            },
            onAdHandled = {
                anythingViewModel.resetInterstitialAdTrigger()
            }
        )
    }


    // Update response when a new message is received
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
        containerColor = Color.White, // ✅ Prevent background glitches
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
                    .distinctUntilChanged() // optional, prevents redundant scrolls
                    .collect { page ->
                        selectedActionIndex = page
                        listState.animateScrollToItem(page)
                    }
            }
            val scrollStates = remember {
                actionKeys.indices.associateWith { ScrollState(0) }
            }

            LaunchedEffect(pagerState.currentPage) {
                // Scroll the new page's scroll state to the top
                scrollStates[pagerState.currentPage]?.let { scrollState ->
                    scrollState.animateScrollTo(0)
                }
            }
            val tabPositions = remember { mutableStateMapOf<Int, Pair<Int, Int>>() } // index -> (x, width)
            val density = LocalDensity.current
            val coroutineScope = rememberCoroutineScope()

            // ADMOBBANNER
            if (!isSubscribed) {
                Spacer(modifier = Modifier.height(6.dp))
                AdMobBanner()
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                   // .clip(RoundedCornerShape(1.dp))
                    .clipToBounds()
                //    .border(
                      //  width = 0.5.dp,
                    //    color = Color.LightGray, // Color(0xFFB3B3B3)
                  //      shape = MaterialTheme.shapes.large
                //    )
                 //   .padding(vertical = 8.dp)

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
                            //   toggleDropdown(action, inputValues, expandedAction) { expandedAction = it }
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
                            fontSize = 20.sp, // Larger font
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }


                // 1. Figure out which indices are visible right now
                val layoutInfo = listState.layoutInfo
                val firstVisible = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

// 2. Only draw when selected index is in that range
                /*
                   if (selectedActionIndex in firstVisible..lastVisible) {
                       // 3. We already have a correct tabPositions[selectedActionIndex]
                       val (targetOffset, targetWidth) = tabPositions[selectedActionIndex] ?: (0 to 0)

                       val animatedOffsetX by animateDpAsState(
                           targetValue = with(density) { targetOffset.toDp() },
                           animationSpec = tween(150, easing = FastOutSlowInEasing)
                       )
                       val animatedWidth by animateDpAsState(
                           targetValue = with(density) { targetWidth.toDp() },
                           animationSpec = tween(150, easing = FastOutSlowInEasing)
                       )

                       Box(
                           modifier = Modifier
                               .offset(x = animatedOffsetX)
                               .width(animatedWidth)
                               .height(1.75.dp)
                               .align(Alignment.BottomStart)
                               .background(Color.Black, RoundedCornerShape(50))
                       )
                   }
   */
            }
            val lazyRowState = rememberLazyListState()


// Track heights per page for dynamic height adjustment
            val pageHeights = remember { mutableStateListOf<Int>().apply {
                repeat(actionKeys.size) { add(0) }
                }
            }


// Scroll to selected page when index changes
            LaunchedEffect(selectedActionIndex) {
                lazyRowState.animateScrollToItem(selectedActionIndex)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 600.dp, max = 1200.dp) // ✅ Upper bound prevents infinity
                   // .padding(vertical = 16.dp)
            )
            {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 600.dp, max = 1200.dp),
                     //   .padding(vertical = 16.dp),
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
                    //    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                             //   .padding(16.dp)
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
                                selectedLanguage = selectedLanguage,
                                onLanguageSelected = { selectedLanguage = it },
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
            // Pass activity safely with null check
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
                        // Your logic when rewarded ad completed
                        anythingViewModel.rewardUserWithReset() // ← call the reset logic
                        Log.d("SubscribeDialog", "User earned reward from watching ad")
                        // e.g., grant some temporary unlock or credits
                    },
                    activity = activity
                )
            } else {
                // Optional: show a fallback UI or log error if activity is null
                Log.e("SubscribeDialog", "Activity is null, cannot show dialog properly")
            }
        }
    }

                }

fun updateInputValues(
    inputValues: MutableMap<String, MutableMap<String, String>>,
    action: String,
    field: String,
    value: String
): MutableMap<String, MutableMap<String, String>> {
    return inputValues.toMutableMap().apply {
        val updatedFields = getOrDefault(action, mutableMapOf()).toMutableMap()
        updatedFields[field] = value
        put(action, updatedFields)
    }
}

fun toggleDropdown(
    action: String,
    inputValues: MutableMap<String, MutableMap<String, String>>,
    expandedAction: String?,
    setExpandedAction: (String?) -> Unit
) {
    setExpandedAction(if (expandedAction == action) null else action)

    inputValues.putIfAbsent(action, mutableMapOf())
}



























