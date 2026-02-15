package com.kaankivancdilli.summary.ui.viewmodel.sub.summary

import android.app.Activity
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaankivancdilli.summary.data.model.local.image.ImageEntity
import com.kaankivancdilli.summary.data.model.local.text.SaveTexts
import com.kaankivancdilli.summary.ui.state.network.ResultState
import com.kaankivancdilli.summary.ui.screens.sub.summary.type.ActionType
import com.kaankivancdilli.summary.core.domain.SubscriptionChecker
import com.kaankivancdilli.summary.ui.viewmodel.sub.subscription.SubscriptionViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.kaankivancdilli.summary.core.domain.handler.SubscriptionHandler
import com.kaankivancdilli.summary.core.domain.handler.summary.SummaryScreenResponseHandler
import com.kaankivancdilli.summary.core.domain.summary.SummaryEditedResponseUpdater
import com.kaankivancdilli.summary.core.domain.summary.SaveTextsHandler
import com.kaankivancdilli.summary.core.mapper.summary.SavedTextMapper
import com.kaankivancdilli.summary.network.rest.RestApiManager
import com.kaankivancdilli.summary.core.state.FreeUsageTracker
import dagger.hilt.android.qualifiers.ApplicationContext


@HiltViewModel
class SummaryScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val subscriptionChecker: SubscriptionChecker,
    savedStateHandle: SavedStateHandle,
    val freeUsageTracker: FreeUsageTracker,
    private val savedTextMapper: SavedTextMapper,
    private val summaryEditedResponseUpdater: SummaryEditedResponseUpdater,
    private val saveTextsHandler: SaveTextsHandler,
    private val subscriptionHandler: SubscriptionHandler

    ) : ViewModel() {

    private val restApiManager = RestApiManager()

    private var currentId: Int = 0

    private val _saveTexts = MutableStateFlow<List<SaveTexts>>(emptyList())
    val saveTexts: StateFlow<List<SaveTexts>> = _saveTexts

    private val _results = MutableStateFlow<Map<String, String>>(emptyMap())
    val results: StateFlow<Map<String, String>> = _results.asStateFlow()

    private var _continueAfterAd: (() -> Unit)? = null

    private var skipInterstitialOnceAfterReset = false

    private val _showInterstitialAd = MutableStateFlow(false)
    val showInterstitialAd: StateFlow<Boolean> = _showInterstitialAd


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
        _results.value = _results.value.toMutableMap()
            .also { it[actionKey] = text }
        _completedActions.value = _completedActions.value + actionKey
    }

    private val _textWithImages = MutableStateFlow<Pair<SaveTexts?, List<ImageEntity>>?>(null)
    val textWithImages: StateFlow<Pair<SaveTexts?, List<ImageEntity>>?> = _textWithImages

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isSummarizedText = MutableStateFlow(false)
    val isSummarizedText: StateFlow<Boolean> = _isSummarizedText

    private val _isParaphrasedText = MutableStateFlow(false)
    val isParaphrasedText: StateFlow<Boolean> = _isParaphrasedText

    private val _isRephrasedText = MutableStateFlow(false)
    val isRephrasedText: StateFlow<Boolean> = _isRephrasedText

    private val _isExpandedText = MutableStateFlow(false)
    val isExpandedText: StateFlow<Boolean> = _isExpandedText

    private val _isBulletpointedText = MutableStateFlow(false)
    val isBulletpointedText: StateFlow<Boolean> = _isBulletpointedText

    private var originalOcrText: String = ""

    private var imageTriples: List<Triple<String, Bitmap, String>>? = null

    fun setImageTriples(images: List<Triple<String, Bitmap, String>>?) {
        imageTriples = images
    }

    val matchedBitmap: Bitmap?
        get() = imageTriples?.firstOrNull { it.third == originalOcrText }?.second

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
        subscriptionHandler.subscribeUser(
            activity = activity,
            viewModelScope = viewModelScope,
            pendingContent = pendingContent,
            pendingSummarizedText = pendingOcrText,
            pendingActionType = pendingActionType,
            setSubscribed = { _isSubscribed.value = it },
            hideDialog = { _showSubscribeDialog.value = false },
            resetProcessing = { _processingAction.value = null },
            sendMessage = { content, summarizedText, actionType ->
                sendToWebSocket(content, summarizedText, actionType)
            },
            clearPending = { clearPendingMessage() }
        )
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
            savedTextMapper.load(
                textId = textId,
                onResult = { textRow, images ->
                    _saveTexts.value = textRow?.let { listOf(it) } ?: emptyList()
                    _textWithImages.value = textRow to images
                    if (textRow != null) originalOcrText = textRow.ocrText
                },
                onUpdateResults = { results, completed ->
                    updateResultsAndCompletedActions(results, completed)
                }
            )
        }
    }

    fun updateEditedResponse(action: ActionType, editedText: String, originalId: Int?) {
        summaryEditedResponseUpdater.updateEditedResponse(
            scope = viewModelScope,
            action = action,
            editedText = editedText,
            originalId = originalId,
            onResultKey = { actionKey, text -> saveResultForAction(actionKey, text) },
            onUpdate = { updated -> _saveTexts.value = listOf(updated) }
        )
    }

    fun triggerInterstitialAd() {
        if (skipInterstitialOnceAfterReset) {
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

    fun continueAfterAd() {
        _continueAfterAd?.invoke()
        _continueAfterAd = null
    }

    private fun observeTexts() {
        viewModelScope.launch {
            restApiManager.texts.collect { result ->
                when (result) {
                    is ResultState.Idle -> Unit
                    is ResultState.Loading -> Unit
                    is ResultState.Error -> _errorMessage.value = result.message
                    is ResultState.Success -> {
                        val extractedText = result.data
                        SummaryScreenResponseHandler(saveTextsHandler, subscriptionChecker)
                            .handleSuccess(
                                saveTextsFlow = _saveTexts,
                                isSummarizedText = _isSummarizedText,
                                isParaphrasedText = _isParaphrasedText,
                                isRephrasedText = _isRephrasedText,
                                isExpandedText = _isExpandedText,
                                isBulletpointedText = _isBulletpointedText,
                                extractedText = extractedText,
                                ocrText = originalOcrText,
                                imageTriples = imageTriples,
                                scope = viewModelScope,
                                onLoadSavedText = { loadedId -> loadSavedText(loadedId) },
                                triggerInterstitialAd = { triggerInterstitialAd() }
                            )
                    }
                }
            }
        }
    }
}