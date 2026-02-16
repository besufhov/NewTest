package com.kaankivancdilli.summary.ui.viewmodel.sub.fullanything

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaankivancdilli.summary.data.model.local.SaveAnything
import com.kaankivancdilli.summary.data.repository.main.anything.AnythingScreenRepository
import com.kaankivancdilli.summary.network.rest.RestApiManager
import com.kaankivancdilli.summary.ui.screens.sub.summary.type.ActionType
import com.kaankivancdilli.summary.core.state.FreeUsageTracker
import com.kaankivancdilli.summary.core.domain.SubscriptionChecker
import com.kaankivancdilli.summary.core.domain.fullanything.FullAnythingEditedResponseUpdater
import com.kaankivancdilli.summary.ui.viewmodel.sub.subscription.SubscriptionViewModel
import com.kaankivancdilli.summary.core.domain.handler.fullanything.FullAnythingResponseHandler
import com.kaankivancdilli.summary.core.domain.handler.SubscriptionHandler
import com.kaankivancdilli.summary.core.mapper.fullanything.SavedAnythingMapper
import com.kaankivancdilli.summary.ui.state.network.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FullAnythingScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val anythingScreenRepository: AnythingScreenRepository,
    private val subscriptionChecker: SubscriptionChecker,
    savedStateHandle: SavedStateHandle,
    private val freeUsageTracker: FreeUsageTracker,
    private val handler: FullAnythingResponseHandler,
    private val subscriptionHandler: SubscriptionHandler,
    private val restApiManager: RestApiManager
    ) : ViewModel() {

    private val _saveAnything = MutableStateFlow<List<SaveAnything>>(emptyList())
    val saveAnything: StateFlow<List<SaveAnything>> = _saveAnything

    private val _results = MutableStateFlow<Map<String, String>>(emptyMap())
    val results: StateFlow<Map<String, String>> = _results.asStateFlow()

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

    private var originalSummarizedText: String = ""

    private val _showSubscribeDialog = MutableStateFlow(false)
    val showSubscribeDialog: StateFlow<Boolean> = _showSubscribeDialog

    private var pendingContent: String? = null
    private var pendingSummarizedText: String? = null
    private var pendingActionType: ActionType? = null

    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed

    private val _processingAction = MutableStateFlow<String?>(null)
    val processingAction: StateFlow<String?> = _processingAction

    private val _completedActions = MutableStateFlow<Set<String>>(emptySet())
    val completedActions: StateFlow<Set<String>> = _completedActions.asStateFlow()

    fun setProcessingAction(action: String?) {
        _processingAction.value = action
    }

    fun updateResultsAndCompletedActions(resultsMap: Map<String, String>, completedSet: Set<String>) {
        _results.value = resultsMap
        _completedActions.value = completedSet
    }

    fun saveResultForAction(actionKey: String, text: String) {
        _results.value = _results.value.toMutableMap()
            .also { it[actionKey] = text }
        _completedActions.value = _completedActions.value + actionKey
    }

    private var _continueAfterAd: (() -> Unit)? = null
    private var skipInterstitialOnceAfterReset = false

    private val _showInterstitialAd = MutableStateFlow(false)
    val showInterstitialAd: StateFlow<Boolean> = _showInterstitialAd


    init {
        savedStateHandle.get<Int>("messageId")?.let { loadSavedAnything(it) }
        observeTexts()
    }

    fun sendMessage(content: String, summarizedText: String, actionType: ActionType?) {
        if (content.isBlank()) return

        viewModelScope.launch {
            val usageCount = freeUsageTracker.getCount()
            Log.d("FullAnythingVM", "Current free usage count: $usageCount")

            if (usageCount >= 20) {
                val isSubscribed = subscriptionChecker.isUserSubscribed()
                Log.d("FullAnythingVM", "Is user subscribed: $isSubscribed")

            if (!isSubscribed) {
                pendingContent = content
                pendingSummarizedText = summarizedText
                pendingActionType = actionType
                _showSubscribeDialog.value = true
                return@launch
            }

            }
            sendHTTPRequest(content, summarizedText, actionType)
        }
    }

    private fun sendHTTPRequest(content: String, summarizedText: String, actionType: ActionType?) {
        originalSummarizedText = summarizedText
        _isSummarizedText.value = actionType == ActionType.SUMMARIZE
        _isParaphrasedText.value = actionType == ActionType.PARAPHRASE
        _isRephrasedText.value = actionType == ActionType.REPHRASE
        _isExpandedText.value = actionType == ActionType.EXPAND
        _isBulletpointedText.value = actionType == ActionType.BULLETPOINT

        restApiManager.sendMessage(content)
    }

    fun subscribeUser(activity: Activity, subscriptionViewModel: SubscriptionViewModel) {
        subscriptionHandler.subscribeUser(
            activity = activity,
            viewModelScope = viewModelScope,
            pendingContent = pendingContent,
            pendingSummarizedText = pendingSummarizedText,
            pendingActionType = pendingActionType,
            setSubscribed = { _isSubscribed.value = it },
            hideDialog = { _showSubscribeDialog.value = false },
            resetProcessing = { _processingAction.value = null },
            sendMessage = { content, summarizedText, actionType ->
                sendHTTPRequest(content, summarizedText, actionType)
            },
            clearPending = { clearPendingMessage() }
        )
    }

    fun hideSubscribeDialog() {
        _showSubscribeDialog.value = false
    }

    private fun clearPendingMessage() {
        pendingContent = null
        pendingSummarizedText = null
        pendingActionType = null
    }

    fun loadSavedAnything(textId: Int) {
        viewModelScope.launch {
            anythingScreenRepository.getAnythingTextById(textId)?.let { saved ->
                _saveAnything.value = listOf(saved)

                val mapper = SavedAnythingMapper(context)
                val bundle = mapper.map(saved)

                originalSummarizedText = bundle.originalSummarizedText
                updateResultsAndCompletedActions(bundle.results, bundle.completed)
            }
        }
    }

    fun resetInterstitialAdTrigger() {
        _showInterstitialAd.value = false
    }

    fun rewardUserWithReset() {
        viewModelScope.launch {
            freeUsageTracker.resetCount()
            Log.d("AnythingViewModel", "Free usage count reset after rewarded ad")

            skipInterstitialOnceAfterReset = true

            hideSubscribeDialog()

            if (pendingContent != null && _isSummarizedText.value) {
                sendHTTPRequest(pendingContent!!, pendingSummarizedText!!, pendingActionType!!)
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

    fun updateEditedResponse(action: ActionType, editedText: String, originalId: Int?) {
        val existing = _saveAnything.value.firstOrNull() ?: return

        val updater = FullAnythingEditedResponseUpdater(context)
        val result = updater.update(existing, action, editedText)

        saveResultForAction(result.actionKey, editedText)

        viewModelScope.launch {
            anythingScreenRepository.saveAnything(result.updatedSaveAnything)
            _saveAnything.value = listOf(result.updatedSaveAnything)
        }
    }

    private fun observeTexts() {
        viewModelScope.launch {
            restApiManager.texts.collect { result ->
                when (result) {
                    is ResultState.Success -> handler.handleSuccess(
                        saveAnythingFlow = _saveAnything,
                        isSummarizedText = _isSummarizedText,
                        isParaphrasedText = _isParaphrasedText,
                        isRephrasedText = _isRephrasedText,
                        isExpandedText = _isExpandedText,
                        isBulletpointedText = _isBulletpointedText,
                        extractedText = result.data
                    )
                    is ResultState.Error -> _errorMessage.value = result.message
                    ResultState.Loading, is ResultState.Idle -> {}
                }
            }
        }
    }
}