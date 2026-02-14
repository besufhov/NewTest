package com.kaankivancdilli.summary.ui.viewmodel.sub.fullanything

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.data.model.local.anything.SaveAnything
import com.kaankivancdilli.summary.data.repository.main.anything.AnythingScreenRepository
import com.kaankivancdilli.summary.network.rest.RestApiManager
import com.kaankivancdilli.summary.ui.screens.sub.summary.type.ActionType
import com.kaankivancdilli.summary.core.state.FreeUsageTracker
import com.kaankivancdilli.summary.core.domain.SubscriptionChecker
import com.kaankivancdilli.summary.ui.viewmodel.sub.subscription.SubscriptionViewModel
import com.kaankivancdilli.summary.core.billing.manager.BillingManager
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
    private val billingManager: BillingManager,
    savedStateHandle: SavedStateHandle,
    private val freeUsageTracker: FreeUsageTracker,
    private val saveHandler: FullAnythingSaveHandler

    ) : ViewModel() {

    private val restApiManager = RestApiManager()

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
            sendToWebSocket(content, summarizedText, actionType)
        }
    }

    private suspend fun sendToWebSocket(content: String, summarizedText: String, actionType: ActionType?) {
        originalSummarizedText = summarizedText
        _isSummarizedText.value = actionType == ActionType.SUMMARIZE
        _isParaphrasedText.value = actionType == ActionType.PARAPHRASE
        _isRephrasedText.value = actionType == ActionType.REPHRASE
        _isExpandedText.value = actionType == ActionType.EXPAND
        _isBulletpointedText.value = actionType == ActionType.BULLETPOINT

        restApiManager.sendMessage(content)
    }

    fun subscribeUser(activity: Activity, subscriptionViewModel: SubscriptionViewModel) {
        billingManager.startConnection {
            billingManager.setPurchaseAcknowledgedListener {
                viewModelScope.launch {
                    val isNowSubscribed = subscriptionChecker.isUserSubscribed()
                    _isSubscribed.value = isNowSubscribed
                    if (isNowSubscribed) {
                        if (pendingContent != null) {
                            sendToWebSocket(pendingContent!!, pendingSummarizedText!!, pendingActionType)
                            clearPendingMessage()
                        }
                    }
                    _showSubscribeDialog.value = false
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

            // Set flag to skip interstitial ad once after reset
            skipInterstitialOnceAfterReset = true

            hideSubscribeDialog()

            if (pendingContent != null && _isSummarizedText.value) {
                sendToWebSocket(pendingContent!!, pendingSummarizedText!!, pendingActionType!!)
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
                    is ResultState.Success -> {
                            val extractedText = result.data
                            val existing = _saveAnything.value.firstOrNull()
                            val updated = existing?.copy(
                                summarize = if (_isSummarizedText.value) extractedText else existing.summarize,
                                paraphrase = if (_isParaphrasedText.value) extractedText else existing.paraphrase,
                                rephrase = if (_isRephrasedText.value) extractedText else existing.rephrase,
                                expand = if (_isExpandedText.value) extractedText else existing.expand,
                                bulletpoint = if (_isBulletpointedText.value) extractedText else existing.bulletpoint,
                            )


                            if (updated != null) {
                                _saveAnything.value = listOf(updated)
                                saveHandler.saveAnything(
                                    id = updated.id.takeIf { it > 0 },
                                    summarize = updated.summarize,
                                    paraphrase = updated.paraphrase,
                                    rephrase = updated.rephrase,
                                    expand = updated.expand,
                                    bulletpoint = updated.bulletpoint,
                                    isUserMessage = false
                                )

                            }

                        _isSummarizedText.value = false
                        _isParaphrasedText.value = false
                        _isRephrasedText.value = false
                        _isExpandedText.value = false
                        _isBulletpointedText.value = false
                    }

                    is ResultState.Error -> _errorMessage.value = result.message
                    ResultState.Loading, is ResultState.Idle -> {}
                }
            }
        }
    }

    fun updateEditedResponse(action: ActionType, editedText: String, originalId: Int?) {
        val existing = _saveAnything.value.firstOrNull() ?: return
        val updated = when (action) {
            ActionType.SUMMARIZE -> existing.copy(summarize = editedText)
            ActionType.PARAPHRASE -> existing.copy(paraphrase = editedText)
            ActionType.REPHRASE -> existing.copy(rephrase = editedText)
            ActionType.EXPAND -> existing.copy(expand = editedText)
            ActionType.BULLETPOINT -> existing.copy(bulletpoint = editedText)
            else -> existing
        }

        val actionKey = TextAction.fromActionType(action, context)?.label ?: "Original"


        saveResultForAction(actionKey, editedText)

        viewModelScope.launch {
            anythingScreenRepository.saveAnything(updated)
            _saveAnything.value = listOf(updated)
        }
    }
}

class FullAnythingSaveHandler @Inject constructor(
    private val repository: AnythingScreenRepository
) {

    suspend fun saveAnything(
        id: Int?,
        summarize: String,
        rephrase: String,
        paraphrase: String,
        expand: String,
        bulletpoint: String,
        isUserMessage: Boolean
    ) {

        if (id == null || id == 0) {

            val newText = SaveAnything(
                summarize = summarize,
                paraphrase = paraphrase,
                rephrase = rephrase,
                expand = expand,
                bulletpoint = bulletpoint,
                isUserMessage = isUserMessage,
                type = "",
                name = "",
                season = "",
                episode = "",
                author = "",
                chapter = "",
                director = "",
                year = "",
                birthday = "",
                source = ""
            )

            repository.saveAnything(newText)

        } else {

            val existingText = repository.getAnythingTextById(id)

            if (existingText != null) {

                val updatedText = existingText.copy(
                    summarize = if (summarize.isNotEmpty()) summarize else existingText.summarize,
                    paraphrase = if (paraphrase.isNotEmpty()) paraphrase else existingText.paraphrase,
                    rephrase = if (rephrase.isNotEmpty()) rephrase else existingText.rephrase,
                    expand = if (expand.isNotEmpty()) expand else existingText.expand,
                    bulletpoint = if (bulletpoint.isNotEmpty()) bulletpoint else existingText.bulletpoint,
                )

                repository.saveAnything(updatedText)
            }
        }
    }
}

sealed class TextAction(val label: String) {

    class Summarize(context: Context) : TextAction(context.getString(R.string.summarize))
    class Paraphrase(context: Context) : TextAction(context.getString(R.string.paraphrase))
    class Rephrase(context: Context) : TextAction(context.getString(R.string.rephrase))
    class Expand(context: Context) : TextAction(context.getString(R.string.expand))
    class Bulletpoint(context: Context) : TextAction(context.getString(R.string.bullet_point))

    companion object {
        fun fromActionType(action: ActionType, context: Context): TextAction? = when(action) {
            ActionType.SUMMARIZE -> Summarize(context)
            ActionType.PARAPHRASE -> Paraphrase(context)
            ActionType.REPHRASE -> Rephrase(context)
            ActionType.EXPAND -> Expand(context)
            ActionType.BULLETPOINT -> Bulletpoint(context)
            else -> null
        }
    }
}

class SavedAnythingMapper(private val context: Context) {

    data class ResultsBundle(
        val results: Map<String, String>,
        val completed: Set<String>,
        val originalSummarizedText: String
    )

    fun map(saved: SaveAnything): ResultsBundle {
        val results = mutableMapOf<String, String>()
        val completed = mutableSetOf<String>()

        val actions = listOf(
            TextAction.Summarize(context),
            TextAction.Paraphrase(context),
            TextAction.Rephrase(context),
            TextAction.Expand(context),
            TextAction.Bulletpoint(context)
        )

        actions.forEach { action ->
            val text = when (action) {
                is TextAction.Summarize -> saved.summarize
                is TextAction.Paraphrase -> saved.paraphrase
                is TextAction.Rephrase -> saved.rephrase
                is TextAction.Expand -> saved.expand
                is TextAction.Bulletpoint -> saved.bulletpoint
            }

            if (text.isNotBlank()) {
                results[action.label] = text
                completed += action.label
            }
        }

        return ResultsBundle(
            results = results,
            completed = completed,
            originalSummarizedText = saved.summarize
        )
    }
}


