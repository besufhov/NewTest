package com.kaankivancdilli.summary.ui.screens.sub.history.screen

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.ui.screens.sub.history.layout.FullAnythingResultCard
import com.kaankivancdilli.summary.ui.screens.sub.summary.type.ActionType
import com.kaankivancdilli.summary.ui.viewmodel.sub.fullanything.FullAnythingScreenViewModel
import com.kaankivancdilli.summary.ui.viewmodel.sub.subscription.SubscriptionViewModel
import com.kaankivancdilli.summary.ui.component.audio.tts.remembertts.rememberTextToSpeech
import com.kaankivancdilli.summary.ui.viewmodel.main.history.TextHistoryViewModel
import com.kaankivancdilli.summary.ui.component.reusable.popup.SubscribeDialog
import java.util.Locale
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kaankivancdilli.summary.ui.screens.sub.history.layout.FullAnythingResultFullScreen
import com.kaankivancdilli.summary.core.detection.autoDetectLanguage
import com.kaankivancdilli.summary.ui.component.admob.adhandler.InterstitialAdHandler
import com.kaankivancdilli.summary.ui.component.reusable.bar.CustomHalfCurvedTopBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FullAnythingScreen(
    text: String?,
    messageId: Int,
    initialSection: String,
    navController: NavController,
    textHistoryViewModel: TextHistoryViewModel = hiltViewModel(),
    fullAnythingScreenViewModel: FullAnythingScreenViewModel = hiltViewModel()
) {

    val ttsState = rememberTextToSpeech(LocalContext.current)

    val savedAnything by textHistoryViewModel.saveAnything.collectAsState()
    val message = savedAnything.find { it.id == messageId }
    val scrollState = rememberScrollState()

    val currentSummarizedText by rememberUpdatedState(newValue = message?.summarize ?: "")

    val anythingLabel = stringResource(R.string.anything)

    var lastAction by rememberSaveable { mutableStateOf("") }

    val results by fullAnythingScreenViewModel.results.collectAsState()
    val processingAction by fullAnythingScreenViewModel.processingAction.collectAsState()

    var selectedLanguage by rememberSaveable { mutableStateOf(Locale.getDefault()) }

    val context = LocalContext.current

    val activity = LocalActivity.current
    val subscriptionViewModel: SubscriptionViewModel = hiltViewModel()

    val showSubscribeDialog by fullAnythingScreenViewModel.showSubscribeDialog.collectAsState()

    val summarizeLabel = stringResource(R.string.summarize)
    val paraphraseLabel = stringResource(R.string.paraphrase)
    val rephraseLabel = stringResource(R.string.rephrase)
    val expandLabel = stringResource(R.string.expand)
    val bulletPointLabel = stringResource(R.string.bullet_point)
    val textLabel = stringResource(R.string.text)
    val summarizeRequest = stringResource(R.string.summarize_request, currentSummarizedText)
    val paraphraseRequest = stringResource(R.string.paraphrase_request, currentSummarizedText)
    val rephraseRequest = stringResource(R.string.rephrase_request, currentSummarizedText)
    val expandRequest = stringResource(R.string.expand_request, currentSummarizedText)
    val bulletPointRequest = stringResource(R.string.bulletpoint_request, currentSummarizedText)

    val nameLabel = stringResource(R.string.name)
    val seasonLabel = stringResource(R.string.season)
    val episodeLabel = stringResource(R.string.episode)
    val authorLabel = stringResource(R.string.author)
    val chapterLabel = stringResource(R.string.chapter)
    val directorLabel = stringResource(R.string.director)
    val yearLabel = stringResource(R.string.year)
    val sourceLabel = stringResource(R.string.source)
    val birthdayLabel = stringResource(R.string.birthday)

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()


    val localizedActions = mapOf(
        summarizeLabel to summarizeLabel,
        paraphraseLabel to paraphraseLabel,
        rephraseLabel to rephraseLabel,
        expandLabel to expandLabel,
        bulletPointLabel to bulletPointLabel
    )

    val actions = localizedActions.keys.toList()

    val summarizeRequester = remember { BringIntoViewRequester() }
    val paraphraseRequester = remember { BringIntoViewRequester() }
    val rephraseRequester = remember { BringIntoViewRequester() }
    val expandRequester = remember { BringIntoViewRequester() }
    val bulletpointRequester = remember { BringIntoViewRequester() }



    LaunchedEffect(fullAnythingScreenViewModel.saveAnything) {
        fullAnythingScreenViewModel.saveAnything.collect { messages ->
            if (messages.isNotEmpty()) {
                val lastMessage = messages.last()
                val newMessage = when (lastAction) {
                    summarizeLabel -> lastMessage.summarize
                    paraphraseLabel -> lastMessage.paraphrase
                    rephraseLabel -> lastMessage.rephrase
                    expandLabel -> lastMessage.expand
                    bulletPointLabel -> lastMessage.bulletpoint
                    else -> ""
                }

                fullAnythingScreenViewModel.saveResultForAction(lastAction, newMessage)
                fullAnythingScreenViewModel.setProcessingAction(null)

            }
        }
    }

    LaunchedEffect(showSubscribeDialog) {
        if (showSubscribeDialog) {
            coroutineScope.launch {
                bringIntoViewRequester.bringIntoView()
            }
        }
    }

    LaunchedEffect(initialSection) {
        delay(300)
        when (initialSection.lowercase()) {
            summarizeLabel.lowercase() -> summarizeRequester.bringIntoView()
            paraphraseLabel.lowercase() -> paraphraseRequester.bringIntoView()
            rephraseLabel.lowercase() -> rephraseRequester.bringIntoView()
            expandLabel.lowercase() -> expandRequester.bringIntoView()
            bulletPointLabel.lowercase() -> bulletpointRequester.bringIntoView()
        }
    }

    val fields = listOf(
        nameLabel to message?.name,
        seasonLabel to message?.season,
        episodeLabel to message?.episode,
        authorLabel to message?.author,
        chapterLabel to message?.chapter,
        directorLabel to message?.director,
        yearLabel to message?.year,
        sourceLabel to message?.source,
        birthdayLabel to message?.birthday
    ).filter { !it.second.isNullOrBlank() }

    fun sendActionRequest(action: String, retryCount: Int = 0, maxRetries: Int = 5) {
        val originalAction = localizedActions[action] ?: return

        lastAction = originalAction
        fullAnythingScreenViewModel.setProcessingAction(originalAction)

        val formattedRequest: String
        val actionType: ActionType?

        when (originalAction) {
            summarizeLabel -> {
                formattedRequest = summarizeRequest
                actionType = ActionType.SUMMARIZE
            }
            paraphraseLabel -> {
                formattedRequest = paraphraseRequest
                actionType = ActionType.PARAPHRASE
            }
            rephraseLabel -> {
                formattedRequest = rephraseRequest
                actionType = ActionType.REPHRASE
            }
            expandLabel -> {
                formattedRequest = expandRequest
                actionType = ActionType.EXPAND
            }
            bulletPointLabel -> {
                formattedRequest = bulletPointRequest
                actionType = ActionType.BULLETPOINT
            }
            else -> {
                formattedRequest = "$originalAction: $currentSummarizedText"
                actionType = null
            }
        }

        fullAnythingScreenViewModel.sendMessage(formattedRequest, currentSummarizedText, actionType)

        CoroutineScope(Dispatchers.Main).launch {
            delay(6000L)

            val isStillProcessing = fullAnythingScreenViewModel.processingAction.value == originalAction
            val noResponseYet = fullAnythingScreenViewModel.results.value[originalAction].isNullOrBlank()

            if (isStillProcessing && noResponseYet) {
                if (retryCount < maxRetries) {
                    Log.w("RetryLogic", "Retrying [$originalAction] - Attempt ${retryCount + 2}")
                    sendActionRequest(action, retryCount + 1, maxRetries)
                } else {
                    Log.e("RetryLogic", "Max retries reached for [$originalAction]. Giving up.")
                    fullAnythingScreenViewModel.setProcessingAction(null)
                    Toast.makeText(context, "Please retry", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CustomHalfCurvedTopBar(
                title = anythingLabel,
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(scrollState)
                .padding(8.dp)
        ) {

            if (fields.isNotEmpty()) {
                Column {
                    fields.forEach { (label, value) ->
                        Text(
                            text = "$label: $value",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            var expandedKey by remember { mutableStateOf<String?>(null) }
            var expandedType by remember { mutableStateOf<ActionType?>(null) }

            actions.forEach { displayAction ->
                val localizedLabel = localizedActions[displayAction] ?: displayAction.toString()
                Button(
                    onClick = { sendActionRequest(displayAction) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .padding(vertical = 8.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = MaterialTheme.shapes.extraLarge,
                            clip = false
                        )
                        .border(
                            width = 0.05.dp,
                            color = Color.LightGray,
                            shape = MaterialTheme.shapes.extraLarge
                        ),
                    enabled = results[displayAction].isNullOrEmpty() && processingAction != displayAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        disabledContainerColor = Color(0xFFFAFAFA)
                    ),
                    shape = MaterialTheme.shapes.extraLarge,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    if (processingAction == displayAction) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            displayAction,
                            color = Color.Black,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium)
                    }
                }

                results[displayAction]?.let { resultText ->
                    if (resultText.isNotEmpty()) {

                        LaunchedEffect(resultText) {
                            val detectedLocale = autoDetectLanguage(resultText)
                            selectedLanguage = detectedLocale
                            ttsState.tts?.language = detectedLocale
                        }

                        val actionType = when (displayAction) {
                            summarizeLabel -> ActionType.SUMMARIZE
                            paraphraseLabel -> ActionType.PARAPHRASE
                            rephraseLabel -> ActionType.REPHRASE
                            expandLabel -> ActionType.EXPAND
                            bulletPointLabel -> ActionType.BULLETPOINT
                            else -> null
                        }

                        val requester = when (displayAction) {
                            summarizeLabel -> summarizeRequester
                            paraphraseLabel -> paraphraseRequester
                            rephraseLabel -> rephraseRequester
                            expandLabel -> expandRequester
                            bulletPointLabel -> bulletpointRequester
                            else -> remember { BringIntoViewRequester() }
                        }
                        actionType?.let {
                            Box(modifier = Modifier.bringIntoViewRequester(requester)) {
                                FullAnythingResultCard(
                                    action = it,
                                    resultText = resultText,
                                    enableEditing = true,
                                    ttsState = ttsState,
                                    onSaveEdit = { act, text ->
                                        fullAnythingScreenViewModel.updateEditedResponse(
                                                act,
                                                text,
                                            originalId = messageId
                                            )
                                    },
                                    onExpand = {
                                        expandedKey = localizedActions[displayAction]
                                        expandedType = actionType
                                    }
                                )
                            }
                        }

                    }
                }
                expandedKey?.let { key ->
                    val type = expandedType!!
                    val latestText = results[key] ?: ""
                    FullAnythingResultFullScreen(
                        resultText = latestText,
                        fileName = "Expanded_${System.currentTimeMillis()}",
                        ttsState = ttsState,
                        selectedLanguage = selectedLanguage,
                        onDismiss = { expandedKey = null },
                        onSaveEdit = { act, text ->
                            fullAnythingScreenViewModel.updateEditedResponse(
                                act,
                                text,
                                originalId = messageId
                            )
                        },
                        actionType = type
                    )
                }
            }
        }
    }

    val showAd by fullAnythingScreenViewModel.showInterstitialAd.collectAsState()

    if (activity != null) {
        InterstitialAdHandler(
            showAd = showAd,
            activity = activity,
            onAdDismissed = {
                fullAnythingScreenViewModel.continueAfterAd()
            },
            onAdHandled = {
                fullAnythingScreenViewModel.resetInterstitialAdTrigger()
            }
        )
    }

    if (showSubscribeDialog) {
        Log.d("fullAnythingScreenViewModel", "Displaying Subscribe Dialog")

        val activity = LocalActivity.current

        Dialog(
            onDismissRequest = {
                fullAnythingScreenViewModel.hideSubscribeDialog()
                fullAnythingScreenViewModel.setProcessingAction(null)
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {

            if (activity != null) {
                SubscribeDialog(
                    onDismiss = {
                        fullAnythingScreenViewModel.hideSubscribeDialog()
                        fullAnythingScreenViewModel.setProcessingAction(null)
                    },
                    onSubscribeClick = {
                        fullAnythingScreenViewModel.subscribeUser(activity, subscriptionViewModel)
                    },
                    onWatchAdsClick = {

                        fullAnythingScreenViewModel.rewardUserWithReset()

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