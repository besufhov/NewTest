package com.kaankivancdilli.summary.ui.screens.sub.summary.screen


import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaankivancdilli.summary.ui.viewmodel.sub.summary.SummaryScreenViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.ui.viewmodel.sub.sharedimage.SharedImageViewModel
import com.kaankivancdilli.summary.ui.screens.sub.summary.layout.SummaryResultCard
import com.kaankivancdilli.summary.ui.screens.sub.summary.type.ActionType
import com.kaankivancdilli.summary.ui.viewmodel.sub.subscription.SubscriptionViewModel
import com.kaankivancdilli.summary.ui.utils.tts.rememberTextToSpeech
import com.kaankivancdilli.summary.ui.utils.detection.autoDetectLanguage
import com.kaankivancdilli.summary.utils.reusable.popup.SubscribeDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kaankivancdilli.summary.ui.component.photomain.image.ImagePreviewRow
import com.kaankivancdilli.summary.ui.screens.sub.summary.layout.SummaryResultFullScreen
import com.kaankivancdilli.summary.utils.admob.adhandler.InterstitialAdHandler
import com.kaankivancdilli.summary.utils.rate.requestInAppReview
import com.kaankivancdilli.summary.utils.reusable.design.CustomHalfCurvedTopBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SummaryScreen(
    text: String,
    messageId: Int?,
    section: String,
    onBack: () -> Unit,
    sharedImageViewModel: SharedImageViewModel,
    summaryScreenViewModel: SummaryScreenViewModel = hiltViewModel()
) {

    // 1) Collect the Pair<SaveTexts,List<ImageEntity>>? from your VM
    val textWithImages by summaryScreenViewModel.textWithImages.collectAsState()

    val cameraImages by sharedImageViewModel.imageData.collectAsState()

    val ttsState = rememberTextToSpeech(LocalContext.current)

    val ocrText = remember { text }
    //val actions = listOf("Summarize", "Paraphrase", "Rephrase", "Expand", "Bulletpoint")

    var lastAction by rememberSaveable { mutableStateOf("") }

    val results by summaryScreenViewModel.results.collectAsState()
    val completedActions by summaryScreenViewModel.completedActions.collectAsState()
    val processingAction by summaryScreenViewModel.processingAction.collectAsState()
    val errorMessage by summaryScreenViewModel.errorMessage.collectAsState()

    var selectedLanguage by rememberSaveable { mutableStateOf(Locale.getDefault()) }

    val activity = LocalActivity.current // ✅ MODERN WAY/ <-- Get activity
    val subscriptionViewModel: SubscriptionViewModel = hiltViewModel()

    val showSubscribeDialog by summaryScreenViewModel.showSubscribeDialog.collectAsState()


    val summarizeLabel = stringResource(R.string.summarize)
    val paraphraseLabel = stringResource(R.string.paraphrase)
    val rephraseLabel = stringResource(R.string.rephrase)
    val expandLabel = stringResource(R.string.expand)
    val bulletPointLabel = stringResource(R.string.bullet_point)
    val textLabel = stringResource(R.string.text)
    val summarizeRequest = stringResource(R.string.summarize_request, ocrText)
    val paraphraseRequest = stringResource(R.string.paraphrase_request, ocrText)
    val rephraseRequest = stringResource(R.string.rephrase_request, ocrText)
    val expandRequest = stringResource(R.string.expand_request, ocrText)
    val bulletPointRequest = stringResource(R.string.bulletpoint_request, ocrText)

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    val showPreview = remember { mutableStateOf(false) }
    val selectedImageId = remember { mutableStateOf<String?>(null) }
    val previewSource = remember { mutableStateOf<String?>(null) }

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

    LaunchedEffect(summaryScreenViewModel.saveTexts) {
        summaryScreenViewModel.saveTexts.collect { messages ->
            if (messages.isNotEmpty()) {
                val lastMessage = messages.last()
                val newMessage = when (lastAction) {
                    summarizeLabel -> lastMessage.summarize
                    paraphraseLabel -> lastMessage.paraphrase
                    rephraseLabel -> lastMessage.rephrase // If "Rephrase" should store paraphrased text
                    expandLabel -> lastMessage.expand // If "Expand" should store summarized text
                    bulletPointLabel -> lastMessage.bulletpoint
                    else -> ""
                }
                summaryScreenViewModel.saveResultForAction(lastAction, newMessage)
                summaryScreenViewModel.setProcessingAction(null)
            }
        }
    }

    LaunchedEffect(showSubscribeDialog) {
        if (showSubscribeDialog) {
            //delay(150) // small delay helps avoid premature jump
            coroutineScope.launch {
                bringIntoViewRequester.bringIntoView()
            }
        }
    }

    LaunchedEffect(section) {
        delay(300) // wait for Composables to draw

        when (section.lowercase()) {
            summarizeLabel.lowercase() -> summarizeRequester.bringIntoView()
            paraphraseLabel.lowercase() -> paraphraseRequester.bringIntoView()
            rephraseLabel.lowercase() -> rephraseRequester.bringIntoView()
            expandLabel.lowercase() -> expandRequester.bringIntoView()
            bulletPointLabel.lowercase() -> bulletpointRequester.bringIntoView()
        }

    }

    LaunchedEffect(cameraImages) {
        val safeImages = cameraImages
        if (!safeImages.isNullOrEmpty()) {
            Log.d("SummaryScreen", "Images received from SharedImageModel: ${safeImages.size}")
            safeImages.forEachIndexed { index, (name, _, recognizedText) ->
                Log.d("SummaryScreen", "Image #$index: name=$name, text=$recognizedText")
            }
        } else {
            Log.d("SummaryScreen", "No images received from SharedImageModel")
        }
    }


    fun sendActionRequest(action: String, retryCount: Int = 0, maxRetries: Int = 5) {
        val originalAction = localizedActions[action] ?: return

        lastAction = originalAction
        summaryScreenViewModel.setProcessingAction(originalAction)

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
                formattedRequest = "$originalAction: $ocrText"
                actionType = null
            }
        }

        summaryScreenViewModel.setImageTriples(cameraImages)
        summaryScreenViewModel.sendMessage(formattedRequest, ocrText, actionType)
        sharedImageViewModel.clearImageData()

        CoroutineScope(Dispatchers.Main).launch {
            delay(6000L)

            if (summaryScreenViewModel.processingAction.value == originalAction &&
                summaryScreenViewModel.results.value[originalAction].isNullOrBlank()
            ) {
                if (retryCount < maxRetries) {
                    println("❗ Retry $originalAction, attempt ${retryCount + 1}")
                    sendActionRequest(action, retryCount + 1, maxRetries)
                } else {
                    println("❌ No response after $maxRetries retries — unlocking button.")

                    summaryScreenViewModel.setProcessingAction(null)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CustomHalfCurvedTopBar(
                title = textLabel,
                onBackClick = { onBack() }
            )
        }
    )
    { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            if (showPreview.value && selectedImageId.value != null) {
                val screenHeight = LocalConfiguration.current.screenHeightDp.dp
                Dialog(
                    onDismissRequest = {
                        showPreview.value = false
                        selectedImageId.value = null
                    },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    ) {
                        when (previewSource.value) {
                            "camera" -> {
                                cameraImages?.let {
                                    ImagePreviewRow(
                                        capturedImages = it,
                                        globalCalculatedCameraHeight = screenHeight,
                                        onDismiss = {
                                            showPreview.value = false
                                            selectedImageId.value = null
                                        },
                                        selectedImageId = selectedImageId.value!!
                                    )
                                }
                            }

                            "room" -> {
                                textWithImages?.second?.let { imageEntities ->
                                    val capturedImages = imageEntities.map { entity ->
                                        val bmp = BitmapFactory.decodeByteArray(entity.imageData, 0, entity.imageData.size)
                                        Triple(entity.name, bmp, entity.recognizedText)
                                    }

                                    ImagePreviewRow(
                                        capturedImages = capturedImages,
                                        globalCalculatedCameraHeight = screenHeight,
                                        onDismiss = {
                                            showPreview.value = false
                                            selectedImageId.value = null
                                        },
                                        selectedImageId = selectedImageId.value!!
                                    )
                                }
                            }
                        }
                    }
                }
            }
          //  val buttonColor = if (results[textLabel].isNullOrEmpty()) Color(0xFFFAFAFA) else Color.Green
            Button(
                onClick = {
                    summaryScreenViewModel.addResult(textLabel, ocrText)
                },
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
                        color = Color.LightGray, // Color(0xFFB3B3B3)
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                enabled = results[textLabel].isNullOrEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    disabledContainerColor = Color(0xFFFAFAFA)
                ),
                shape = MaterialTheme.shapes.extraLarge,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Text(
                    textLabel,
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium)
            }
            // 3) Somewhere near the top, show the images you loaded from Room:
            textWithImages?.let { (_, imageEntities) ->
                if (imageEntities.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(imageEntities) { entity ->
                            // turn bytearray → Bitmap → ImageBitmap
                            val bitmap = remember(entity.imageData) {
                                BitmapFactory.decodeByteArray(
                                    entity.imageData,
                                    0,
                                    entity.imageData.size
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(120.dp)
                            ) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = entity.recognizedText,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedImageId.value = entity.name
                                            previewSource.value = "room"
                                            showPreview.value = true
                                        }
                                )
                                Text(
                                    text = entity.recognizedText,
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4) If you still want to show the original camera images
            cameraImages?.let { triples ->
                if (triples.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                   // Text("Camera-scanned images:", fontWeight = FontWeight.SemiBold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(triples) { (name, bmp, recognized) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = recognized,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedImageId.value = name
                                            previewSource.value = "camera"
                                            showPreview.value = true
                                        }
                                )
                            }
                        }
                    }
                }
            }


            var expandedText by remember { mutableStateOf<String?>(null) }

            results[textLabel]?.let { resultText ->
                if (resultText.isNotEmpty()) {

                    LaunchedEffect(resultText) {
                        val detectedLocale = autoDetectLanguage(resultText)
                        selectedLanguage = detectedLocale
                        ttsState.tts?.language = detectedLocale

                        val usageCount = summaryScreenViewModel.freeUsageTracker.getCount()
                        if (usageCount == 17) {
                            activity?.let { requestInAppReview(it) }
                        }
                    }

                    SummaryResultCard(

                        action = ActionType.ORIGINAL, // ?
                        resultText = resultText,
                        fileName = "${textLabel}_${System.currentTimeMillis()}",
                        ttsState = ttsState,
                        selectedLanguage = selectedLanguage,
                        enableEditing = false, // because no editing button in this case
                        onSaveEdit = { act, text ->  // <-- add this block
                            summaryScreenViewModel.updateEditedResponse(
                                act,
                                text,
                                originalId = messageId
                            )
                        },
                        onExpand = { expandedText = resultText }, // NEW
                    )
                }
            }

            expandedText?.let { fullText ->
                SummaryResultFullScreen(
                    resultText = fullText,
                    fileName = "Expanded_${System.currentTimeMillis()}",
                    ttsState = ttsState,
                    selectedLanguage = selectedLanguage,
                    onDismiss = { expandedText = null },
                    onSaveEdit = { act, text ->
                        summaryScreenViewModel.updateEditedResponse(
                            act,
                            text,
                            originalId = messageId
                        )
                    },
                    actionType = ActionType.ORIGINAL // pass the correct one here
                )
            }

            // ✅ new: just capture the key + type
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
                                color = Color.LightGray, // Color(0xFFB3B3B3)
                                shape = MaterialTheme.shapes.extraLarge
                            ),
                        enabled = displayAction !in completedActions && processingAction != displayAction,
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
                            Text(localizedLabel,
                                color = Color.Black,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium)
                        }
                    }


                    results[localizedActions[displayAction]]?.let { resultText ->
                        if (resultText.isNotEmpty()) {

                            LaunchedEffect(resultText) {
                                val detectedLocale = autoDetectLanguage(resultText)
                                selectedLanguage = detectedLocale
                                ttsState.tts?.language = detectedLocale
                            }

                            val actionType = when (localizedActions[displayAction]) {
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
                                else -> remember { BringIntoViewRequester() } // Optional fallback
                            }

                            actionType?.let {
                                Box(modifier = Modifier.bringIntoViewRequester(requester)) {
                                    SummaryResultCard(
                                        action = it,
                                        resultText = resultText,
                                        fileName = "${displayAction}_${System.currentTimeMillis()}",
                                        ttsState = ttsState,
                                        selectedLanguage = selectedLanguage,
                                        enableEditing = true,
                                        onSaveEdit = { act, text ->
                                            summaryScreenViewModel.updateEditedResponse(
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
                        SummaryResultFullScreen(
                            resultText = latestText,
                            fileName = "Expanded_${System.currentTimeMillis()}",
                            ttsState = ttsState,
                            selectedLanguage = selectedLanguage,
                            onDismiss = { expandedKey = null },
                            onSaveEdit = { act, text ->
                                summaryScreenViewModel.updateEditedResponse(
                                    act,
                                    text,
                                    originalId = messageId
                                )
                                // no need to touch expandedKey — reading from `results[key]` will now give you the new text
                            },
                            actionType = type
                        )
                    }
                }
            }
    }

    // Show the Subscribe Dialog when the flag is true
    val showAd by summaryScreenViewModel.showInterstitialAd.collectAsState()

    if (activity != null) {
        InterstitialAdHandler(
            showAd = showAd,
            activity = activity,
           // onUserEarnedReward = {
                // Just one free usage logic:
          //      summaryScreenViewModel.rewardSingleUsage()
          //  },
            onAdDismissed = {
                summaryScreenViewModel.continueAfterAd()
            },
            onAdHandled = {
                summaryScreenViewModel.resetInterstitialAdTrigger()
            }
        )
    }

    if (showSubscribeDialog) {
        Log.d("fullAnythingScreenViewModel", "Displaying Subscribe Dialog")

        val activity = LocalActivity.current

        Dialog(
            onDismissRequest = {
                summaryScreenViewModel.hideSubscribeDialog()
                summaryScreenViewModel.setProcessingAction(null)
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            // Pass activity safely with null check
            if (activity != null) {
                SubscribeDialog(
                    onDismiss = {
                        summaryScreenViewModel.hideSubscribeDialog()
                        summaryScreenViewModel.setProcessingAction(null)
                    },
                    onSubscribeClick = {
                        summaryScreenViewModel.subscribeUser(activity, subscriptionViewModel)
                    },
                    onWatchAdsClick = {

                        summaryScreenViewModel.rewardUserWithReset()

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