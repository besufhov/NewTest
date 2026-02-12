package com.kaankivancdilli.summary.ui.viewmodel.sub.summary

import android.app.Activity
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.data.model.local.image.ImageEntity
import com.kaankivancdilli.summary.data.model.local.text.SaveTexts
import com.kaankivancdilli.summary.utils.state.network.ResultState
import com.kaankivancdilli.summary.data.repository.sub.summary.SummaryScreenRepository
import com.kaankivancdilli.summary.ui.screens.sub.summary.type.ActionType
import com.kaankivancdilli.summary.utils.billing.BillingManager
import com.kaankivancdilli.summary.utils.state.subscription.SubscriptionChecker
import com.kaankivancdilli.summary.ui.viewmodel.sub.subscription.SubscriptionViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.kaankivancdilli.summary.network.rest.RestApiManager
import com.kaankivancdilli.summary.utils.state.usage.FreeUsageTracker
import dagger.hilt.android.qualifiers.ApplicationContext


@HiltViewModel
class SummaryScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val summaryScreenRepository: SummaryScreenRepository,
    // private val webSocketManager: WebSocketManager,
    private val subscriptionChecker: SubscriptionChecker,
    private val billingManager: BillingManager,
    savedStateHandle: SavedStateHandle,
    val freeUsageTracker: FreeUsageTracker,

    ) : ViewModel() {

    private val restApiManager = RestApiManager()

    private var currentId: Int = 0

    private val _saveTexts = MutableStateFlow<List<SaveTexts>>(emptyList())
    val saveTexts: StateFlow<List<SaveTexts>> = _saveTexts

    private val _results = MutableStateFlow<Map<String, String>>(emptyMap())
    val results: StateFlow<Map<String, String>> = _results.asStateFlow()

    fun updateResults(lastAction: String, newMessage: String) {
        _results.value = _results.value.toMutableMap().apply {
            this[lastAction] = newMessage
        }
    }

    private var _continueAfterAd: (() -> Unit)? = null

    // Flag to skip interstitial once after reset from rewarded ad
    private var skipInterstitialOnceAfterReset = false

    private val _showInterstitialAd = MutableStateFlow(false)
    val showInterstitialAd: StateFlow<Boolean> = _showInterstitialAd

    private val textLabel = context.getString(R.string.text)
    private val summarizeLabel    = context.getString(R.string.summarize)
    private val paraphraseLabel   = context.getString(R.string.paraphrase)
    private val rephraseLabel   = context.getString(R.string.rephrase)
    private val expandLabel   = context.getString(R.string.expand)
    private val bulletpointLabel   = context.getString(R.string.bullet_point)

    private val _completedActions = MutableStateFlow<Set<String>>(emptySet())
    val completedActions: StateFlow<Set<String>> = _completedActions.asStateFlow()

    fun updateResultsAndCompletedActions(resultsMap: Map<String, String>, completedSet: Set<String>) {
        _results.value = resultsMap
        _completedActions.value = completedSet
    }

    fun addResult(action: String, text: String) {
        val updated = _results.value.toMutableMap()
        updated[action] = text
        _results.value = updated
    }

    fun saveResultForAction(actionKey: String, text: String) {
        // update results map
        _results.value = _results.value.toMutableMap()
            .also { it[actionKey] = text }

        // mark this action completed
        _completedActions.value = _completedActions.value + actionKey
    }

    private val _textWithImages = MutableStateFlow<Pair<SaveTexts?, List<ImageEntity>>?>(null)
    val textWithImages: StateFlow<Pair<SaveTexts?, List<ImageEntity>>?> = _textWithImages

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Track if text is summarized
    private val _isSummarizedText = MutableStateFlow(false)
    val isSummarizedText: StateFlow<Boolean> = _isSummarizedText

    // Track if text is paraphrased
    private val _isParaphrasedText = MutableStateFlow(false)
    val isParaphrasedText: StateFlow<Boolean> = _isParaphrasedText

    private val _isRephrasedText = MutableStateFlow(false)
    val isRephrasedText: StateFlow<Boolean> = _isRephrasedText

    private val _isExpandedText = MutableStateFlow(false)
    val isExpandedText: StateFlow<Boolean> = _isExpandedText

    private val _isBulletpointedText = MutableStateFlow(false)
    val isBulletpointedText: StateFlow<Boolean> = _isBulletpointedText

    // Store the original OCR text
    private var originalOcrText: String = ""

    private var imageTriples: List<Triple<String, Bitmap, String>>? = null

    fun setImageTriples(images: List<Triple<String, Bitmap, String>>?) {
        imageTriples = images
    }

    val matchedBitmap: Bitmap?
        get() = imageTriples?.firstOrNull { it.third == originalOcrText }?.second

    val imageBytes: ByteArray?
        get() = matchedBitmap?.let { bitmapToByteArray(it) }


    private val _showSubscribeDialog = MutableStateFlow(false)
    val showSubscribeDialog: StateFlow<Boolean> = _showSubscribeDialog

    private var pendingContent: String? = null
    private var pendingOcrText: String? = null
    private var pendingActionType: ActionType? = null

    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed

    private val _processingAction = MutableStateFlow<String?>(null)
    val processingAction: StateFlow<String?> = _processingAction

    fun setProcessingAction(action: String?) {
        _processingAction.value = action
    }


    init {
        savedStateHandle.get<Int>("messageId")?.let { loadSavedText(it) }
        observeTexts()
    }



    fun sendMessage(content: String, ocrText: String, actionType: ActionType?) {
        if (content.isBlank()) return

        viewModelScope.launch {
            val usageCount = freeUsageTracker.getCount()
            Log.d("SummaryVM", "Current free usage count: $usageCount")

            if (usageCount >= 20) {
                val isSubscribed = subscriptionChecker.isUserSubscribed()
                Log.d("SummaryVM", "Is user subscribed: $isSubscribed")

                if (!isSubscribed) {
                    pendingContent = content
                    pendingOcrText = ocrText
                    pendingActionType = actionType
                    _showSubscribeDialog.value = true
                    return@launch
                }
            }

            sendToWebSocket(content, ocrText, actionType)
        }
    }


    fun subscribeUser(activity: Activity, subscriptionViewModel: SubscriptionViewModel) {
        billingManager.startConnection {
            billingManager.setPurchaseAcknowledgedListener {
                viewModelScope.launch {
                    val isNowSubscribed = subscriptionChecker.isUserSubscribed()
                    _isSubscribed.value = isNowSubscribed
                    Log.d("SummaryScreenViewModel", "Is user subscribed updated: ${_isSubscribed.value}")
                    if (isNowSubscribed) {
                        if (pendingContent != null) {
                            sendToWebSocket(pendingContent!!, pendingOcrText!!, pendingActionType)
                            clearPendingMessage()
                        }
                    }
                    _showSubscribeDialog.value = false
                    Log.d("SummaryScreenViewModel", "Show SubscribeDialog: ${_showSubscribeDialog.value}")
                }
            }

            billingManager.setSubscriptionListener { isSubscribed ->
                // This block runs if the user either successfully subscribes or cancels
                if (!isSubscribed) {
                    // Reset loading/spinner state
                    _processingAction.value = null
                    _showSubscribeDialog.value = false
                }
            }

            billingManager.launchPurchase(activity, "monthly_subscription")
        }
    }


    private suspend fun sendToWebSocket(content: String, ocrText: String, actionType: ActionType?) {
        originalOcrText = ocrText
        _isSummarizedText.value = actionType == ActionType.SUMMARIZE
        _isParaphrasedText.value = actionType == ActionType.PARAPHRASE
        _isRephrasedText.value = actionType == ActionType.REPHRASE
        _isExpandedText.value = actionType == ActionType.EXPAND
        _isBulletpointedText.value = actionType == ActionType.BULLETPOINT

        restApiManager.sendMessage(content)
        Log.d("SummaryScreenViewModel", "Message sent to WebSocket")
    }

    fun hideSubscribeDialog() {
        _showSubscribeDialog.value = false
        Log.d("SummaryScreenViewModel", "Spinner manually hidden")
    }

    private fun clearPendingMessage() {
        pendingContent = null
        pendingOcrText = null
        pendingActionType = null
        Log.d("SummaryScreenViewModel", "Pending message cleared")
    }


    fun loadSavedText(textId: Int) {
        currentId = textId
        Log.d("SummaryScreen", "Fetching saved text+images for ID: $textId")
        viewModelScope.launch {
            summaryScreenRepository
                .getTextWithImages(textId)
                .collect { (textRow, images) ->
                    if (textRow == null) {
                        Log.d("SummaryScreen", "No text found for ID $textId")
                        _saveTexts.value = emptyList()
                        _textWithImages.value = null to images
                        return@collect
                    }

                    Log.d("SummaryScreen", "Loaded text: $textRow with ${images.size} images")

                    _saveTexts.value = listOf(textRow)
                    _textWithImages.value = textRow to images

                    val textLabel = context.getString(R.string.text)
                    val initialResults = mutableMapOf<String, String>()
                    val initialCompleted = mutableSetOf<String>()

                    // Only add non-blank transformations
                    if (textRow.summarize.isNotBlank()) {
                        initialResults[summarizeLabel] = textRow.summarize
                        initialCompleted.add(summarizeLabel)
                    }
                    if (textRow.paraphrase.isNotBlank()) {
                        initialResults[paraphraseLabel] = textRow.paraphrase
                        initialCompleted.add(paraphraseLabel)
                    }
                    if (textRow.rephrase.isNotBlank()) {
                        initialResults[rephraseLabel] = textRow.rephrase
                        initialCompleted.add(rephraseLabel)
                    }
                    if (textRow.expand.isNotBlank()) {
                        initialResults[expandLabel] = textRow.expand
                        initialCompleted.add(expandLabel)
                    }
                    if (textRow.bulletpoint.isNotBlank()) {
                        initialResults[bulletpointLabel] = textRow.bulletpoint
                        initialCompleted.add(bulletpointLabel)
                    }

                    updateResultsAndCompletedActions(initialResults, initialCompleted)
                    originalOcrText = textRow.ocrText
                }

        }
    }


    fun updateEditedResponse(action: ActionType, editedText: String, originalId: Int?) {
        viewModelScope.launch {
            if (originalId == null || originalId == 0) return@launch

            val existing = summaryScreenRepository.getSavedTextById(originalId)
            if (existing == null) {
                Log.e("updateEditedResponse", "No matching SaveTexts entry found for ID: $originalId")
                return@launch
            }

            val updated = when (action) {
                ActionType.SUMMARIZE -> existing.copy(summarize = editedText)
                ActionType.PARAPHRASE -> existing.copy(paraphrase = editedText)
                ActionType.REPHRASE -> existing.copy(rephrase = editedText)
                ActionType.EXPAND -> existing.copy(expand = editedText)
                ActionType.BULLETPOINT -> existing.copy(bulletpoint = editedText)
                ActionType.ORIGINAL -> existing.copy(ocrText = editedText) // Assuming you have ocrText in the entity
            }

            // ✅ This updates the UI map!
            val actionKey = when (action) {
                ActionType.SUMMARIZE    -> summarizeLabel
                ActionType.PARAPHRASE  -> paraphraseLabel
                ActionType.REPHRASE    -> rephraseLabel
                ActionType.EXPAND      -> expandLabel
                ActionType.BULLETPOINT -> bulletpointLabel
                ActionType.ORIGINAL    -> "Original"
            }

            saveResultForAction(actionKey, editedText) // This updates the UI!

            summaryScreenRepository.saveText(updated)
            _saveTexts.value = listOf(updated)
            Log.d("updateEditedResponse", "Saved updated result for $action: $editedText")
        }
    }

    fun triggerInterstitialAd() {
        if (skipInterstitialOnceAfterReset) {
            // Skip showing interstitial this time, reset the flag
            skipInterstitialOnceAfterReset = false
            Log.d("AnythingViewModel", "Skipping interstitial ad due to recent reset")
            return
        }
        _showInterstitialAd.value = true
    }

    fun resetInterstitialAdTrigger() {
        _showInterstitialAd.value = false
    }

    fun rewardUserWithReset() {
        viewModelScope.launch {
            freeUsageTracker.resetCount()
            Log.d("AnythingViewModel", "Free usage count reset after rewarded ad")

            // Set flag to skip interstitial ad once after reset
            skipInterstitialOnceAfterReset = true

            hideSubscribeDialog()

            if (pendingContent != null && _isSummarizedText.value) {
                sendToWebSocket(pendingContent!!, pendingOcrText!!, pendingActionType!!)
                clearPendingMessage()
            } else {
                Log.d("RewardReset", "Skipped resend because response was already processed")
            }
        }
    }

    fun rewardSingleUsage() {
        viewModelScope.launch {
            //   freeUsageTracker.incrementAndGet()
            Log.d("AnythingViewModel", "Granted 1 free usage after rewarded interstitial")
        }
    }

    fun continueAfterAd() {
        _continueAfterAd?.invoke()
        _continueAfterAd = null
    }


    private fun observeTexts() {
        viewModelScope.launch {
            restApiManager.texts.collect { result ->
                when (result) {
                    is ResultState.Idle -> {

                    }

                    is ResultState.Success -> {
                        val extractedText = result.data

                        // Only save if the received text is summarized
                        if (_isSummarizedText.value) {

                                val usageCount = freeUsageTracker.getCount()
                                if (usageCount <= 20) {
                                    freeUsageTracker.incrementAndGet()
                                    Log.d("AnythingVM", "Free usage incremented to ${usageCount + 1}")
                                }

                                saveTexts(
                                    id = _saveTexts.value.firstOrNull()?.id?.takeIf { it > 0 },
                                    summarize = extractedText,
                                    originalOcrText,
                                    "",
                                    "",
                                    "",
                                    "",
                                    isUserMessage = false,
                                    bitmap = matchedBitmap
                                )
                                _saveTexts.update { currentList ->
                                    currentList + SaveTexts(
                                        summarize = extractedText,
                                        ocrText = originalOcrText,
                                        paraphrase = "",
                                        rephrase = "",
                                        expand = "",
                                        bulletpoint = "",
                                        isUserMessage = false,
                                        //   imageData = imageBytes
                                    )
                                }
                                val isSubbed = subscriptionChecker.isUserSubscribed()
                            if (!isSubbed && usageCount > 0 && usageCount % 6 == 0) {
                                triggerInterstitialAd()
                            }

                            _isSummarizedText.value = false  // Reset after saving
                        } else if (_isParaphrasedText.value) {


                           //     val usageCount = freeUsageTracker.getCount()
                           //     if (usageCount <= 20) {
                          //          freeUsageTracker.incrementAndGet()
                         //           Log.d(
                         //               "AnythingVM",
                        //                "Free usage incremented to ${usageCount + 1}"
                        //            )
                        //        }

                                saveTexts(
                                    id = _saveTexts.value.firstOrNull()?.id?.takeIf { it > 0 },
                                    "",
                                    originalOcrText,
                                    "",
                                    paraphrase =
                                    extractedText,
                                    "",
                                    "",
                                    isUserMessage = false,
                                    bitmap = matchedBitmap
                                )
                                _saveTexts.update { currentList ->
                                    currentList + SaveTexts(
                                        summarize = "",
                                        ocrText = originalOcrText,
                                        paraphrase = extractedText,
                                        rephrase = "",
                                        expand = "",
                                        bulletpoint = "",
                                        isUserMessage = false,
                                        //   imageData = imageBytes
                                    )
                                }

                       //         val isSubbed = subscriptionChecker.isUserSubscribed()
                      //      if (!isSubbed && usageCount % 3 == 0) { // 0, 2, 4... → before incrementing, so acts on 1st, 3rd, 5th...
                      //          triggerInterstitialAd()
                      //      }


                            _isParaphrasedText.value = false // Reset after saving
                        } else if (_isRephrasedText.value) {

                         //       val usageCount = freeUsageTracker.getCount()
                        //        if (usageCount <= 20) {
                        //            freeUsageTracker.incrementAndGet()
                        //            Log.d(
                        //                "AnythingVM",
                        //                "Free usage incremented to ${usageCount + 1}"
                         //           )
                        //        }

                                saveTexts(
                                    id = _saveTexts.value.firstOrNull()?.id?.takeIf { it > 0 },
                                    "",
                                    originalOcrText,
                                    rephrase = extractedText,
                                    "",
                                    "",
                                    "",
                                    isUserMessage = false,
                                    bitmap = matchedBitmap
                                )
                                _saveTexts.update { currentList ->
                                    currentList + SaveTexts(
                                        summarize = "",
                                        ocrText = originalOcrText,
                                        rephrase = extractedText,
                                        paraphrase = "",
                                        expand = "",
                                        bulletpoint = "",
                                        isUserMessage = false,
                                        //     imageData = imageBytes
                                    )
                                }

                         //       val isSubbed = subscriptionChecker.isUserSubscribed()

                       //     if (!isSubbed && usageCount % 3 == 0) { // 0, 2, 4... → before incrementing, so acts on 1st, 3rd, 5th...
                       //         triggerInterstitialAd()
                       //     }

                            _isRephrasedText.value = false // Reset after saving
                        } else if (_isExpandedText.value) {


                           //     val usageCount = freeUsageTracker.getCount()
                           //     if (usageCount <= 20) {
                          //          freeUsageTracker.incrementAndGet()
                         //           Log.d(
                         //               "AnythingVM",
                         //               "Free usage incremented to ${usageCount + 1}"
                         //           )
                        //        }

                                saveTexts(
                                    id = _saveTexts.value.firstOrNull()?.id?.takeIf { it > 0 },
                                    "",
                                    originalOcrText,
                                    "",
                                    "",
                                    expand = extractedText,
                                    "",
                                    isUserMessage = false,
                                    bitmap = matchedBitmap
                                )
                                _saveTexts.update { currentList ->
                                    currentList + SaveTexts(
                                        summarize = "",
                                        ocrText = originalOcrText,
                                        rephrase = "",
                                        paraphrase = "",
                                        expand = extractedText,
                                        bulletpoint = "",
                                        isUserMessage = false,
                                        //     imageData = imageBytes
                                    )
                                }

                       //         val isSubbed = subscriptionChecker.isUserSubscribed()

                       //     if (!isSubbed && usageCount % 3 == 0) { // 0, 2, 4... → before incrementing, so acts on 1st, 3rd, 5th...
                        //        triggerInterstitialAd()
                      //      }
                            _isExpandedText.value = false // Reset after saving
                        } else if (_isBulletpointedText.value) {


                        //        val usageCount = freeUsageTracker.getCount()
                        //        if (usageCount <= 20) {
                        //            freeUsageTracker.incrementAndGet()
                        //            Log.d(
                        //                "AnythingVM",
                       //                 "Free usage incremented to ${usageCount + 1}"
                        //            )
                       //         }

                                saveTexts(
                                    id = _saveTexts.value.firstOrNull()?.id?.takeIf { it > 0 },
                                    "",
                                    originalOcrText,
                                    "",
                                    "",
                                    "",
                                    extractedText,
                                    isUserMessage = false,
                                    bitmap = matchedBitmap
                                )
                                _saveTexts.update { currentList ->
                                    currentList + SaveTexts(
                                        summarize = "",
                                        ocrText = originalOcrText,
                                        rephrase = "",
                                        paraphrase = "",
                                        expand = "",
                                        bulletpoint = extractedText,
                                        isUserMessage = false,
                                        //      imageData = imageBytes
                                    )
                                }

                            //    val isSubbed = subscriptionChecker.isUserSubscribed()

                        //    if (!isSubbed && usageCount % 3 == 0) { // 0, 2, 4... → before incrementing, so acts on 1st, 3rd, 5th...
                        //        triggerInterstitialAd()
                      //      }

                            _isBulletpointedText.value = false // Reset after saving
                        } else {
                            // Add OCR text without saving (not summarized)
                            _saveTexts.update { currentList ->
                                currentList + SaveTexts(
                                    summarize = "",
                                    ocrText = originalOcrText,
                                    paraphrase = "",
                                    rephrase = "",
                                    expand = "",
                                    bulletpoint = "",
                                    isUserMessage = false,
                                    //     imageData = imageBytes
                                )
                            }
                        }
                    }
                    is ResultState.Error -> {
                        _errorMessage.value = result.message
                    }
                    is ResultState.Loading -> {}
                }
            }
        }
    }




    private suspend fun saveTexts(
        id: Int?,
        summarize: String,
        ocrText: String,
        rephrase: String,
        paraphrase: String,
        expand: String,
        bulletpoint: String,
        isUserMessage: Boolean,
        bitmap: Bitmap?
    ) {
        // 1) Gather & (optionally) compress your passed bitmap images
        val triples = imageTriples ?: emptyList()
        // (You could compress each Triple’s Bitmap here if you like)

        if (id == null || id == 0) {
            // ➤ INSERT NEW ROW + IMAGES
            val newText = SaveTexts(
                summarize     = summarize,
                paraphrase    = paraphrase,
                rephrase      = rephrase,
                expand        = expand,
                bulletpoint   = bulletpoint,
                ocrText       = ocrText,
                isUserMessage = isUserMessage
            )

            // This does: insert text row → deleteByTextId(…) → insert each image → returns new textId
            val newId = summaryScreenRepository
                .saveTextWithImages(newText, triples)
                .toInt()

            // Immediately reload that new entry (text + images):
            loadSavedText(newId)
        } else {
            // ➤ UPDATE EXISTING TEXT FIELDS
            val existing = summaryScreenRepository.getSavedTextById(id) ?: return

            val updated = existing.copy(
                summarize   = summarize.ifBlank   { existing.summarize },
                paraphrase  = paraphrase.ifBlank  { existing.paraphrase },
                rephrase    = rephrase.ifBlank    { existing.rephrase },
                expand      = expand.ifBlank      { existing.expand },
                bulletpoint = bulletpoint.ifBlank { existing.bulletpoint }
                // we leave ocrText/isUserMessage unchanged
            )

            // Update only the textual fields:
            summaryScreenRepository.saveText(updated)


            // If you provided new images, replace them too:
            if (triples.isNotEmpty()) {
                summaryScreenRepository.saveTextWithImages(updated, triples)
            }

            // And reload for UI:
            loadSavedText(id)
        }
    }

}

// Your composables above...

fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()

    // Scale down aggressively to ~25% original size
    val scaledBitmap = Bitmap.createScaledBitmap(
        bitmap,
        bitmap.width / 3,  // Adjust these divisors as needed
        bitmap.height / 3,
        true
    )

    // Compress to lower JPEG quality (30% is a good sweet spot)
    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 30, stream)

    return stream.toByteArray()
}








