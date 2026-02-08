package com.kaankivancdilli.summary.ui.screens.sub.history.fullanything

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.data.model.local.SaveAnything
import com.kaankivancdilli.summary.data.repository.AnythingScreenRepository
import com.kaankivancdilli.summary.network.RestApiManager
import com.kaankivancdilli.summary.network.WebSocketManager
import com.kaankivancdilli.summary.ui.screens.sub.summary.type.ActionType
import com.kaankivancdilli.summary.ui.screens.sub.viewmodel.subscription.FreeUsageTracker
import com.kaankivancdilli.summary.ui.screens.sub.viewmodel.subscription.checker.SubscriptionChecker
import com.kaankivancdilli.summary.ui.screens.sub.viewmodel.subscription.viewmodel.SubscriptionViewModel
import com.kaankivancdilli.summary.utils.billing.BillingManager
import com.kaankivancdilli.summary.utils.state.ResultState
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
   // private val webSocketManager: WebSocketManager,
    private val subscriptionChecker: SubscriptionChecker,
    private val billingManager: BillingManager,
    savedStateHandle: SavedStateHandle,
    private val freeUsageTracker: FreeUsageTracker,

) : ViewModel() {

    // ✅ Create a fresh instance yourself (not via Hilt)
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

    private var _continueAfterAd: (() -> Unit)? = null

    // Flag to skip interstitial once after reset from rewarded ad
    private var skipInterstitialOnceAfterReset = false

    private val _showInterstitialAd = MutableStateFlow(false)
    val showInterstitialAd: StateFlow<Boolean> = _showInterstitialAd

    private val summarizeLabel    = context.getString(R.string.summarize)
    private val paraphraseLabel   = context.getString(R.string.paraphrase)
    private val rephraseLabel   = context.getString(R.string.rephrase)
    private val expandLabel   = context.getString(R.string.expand)
    private val bulletpointLabel   = context.getString(R.string.bullet_point)

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

                // 1) build your map + completed set
                val initialResults   = mutableMapOf<String,String>()
                val initialCompleted = mutableSetOf<String>()

                if (saved.summarize.isNotBlank()) {
                    initialResults[summarizeLabel] = saved.summarize
                    initialCompleted += summarizeLabel
                }
                if (saved.paraphrase.isNotBlank()) {
                    initialResults[paraphraseLabel] = saved.paraphrase
                    initialCompleted += paraphraseLabel
                }

                if (saved.rephrase.isNotBlank()) {
                    initialResults[rephraseLabel] = saved.rephrase
                    initialCompleted += rephraseLabel
                }

                if (saved.expand.isNotBlank()) {
                    initialResults[expandLabel] = saved.expand
                    initialCompleted += expandLabel
                }

                if (saved.bulletpoint.isNotBlank()) {
                    initialResults[bulletpointLabel] = saved.bulletpoint
                    initialCompleted += bulletpointLabel
                }

                originalSummarizedText = saved.summarize
                // … same for rephrase, expand, bulletpoint …

                // 2) atomically push into your StateFlows
                updateResultsAndCompletedActions(initialResults, initialCompleted)
            }
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
                sendToWebSocket(pendingContent!!, pendingSummarizedText!!, pendingActionType!!)
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
                    is ResultState.Success -> {

                       //     val usageCount = freeUsageTracker.getCount()
                     //       if (usageCount <= 30) {
                     //           freeUsageTracker.incrementAndGet()
                     //           Log.d("AnythingVM", "Free usage incremented to ${usageCount + 1}")
                      //      }

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
                                saveAnything(
                                    id = updated.id.takeIf { it > 0 },
                                    summarize = updated.summarize,
                                    paraphrase = updated.paraphrase,
                                    rephrase = updated.rephrase,
                                    expand = updated.expand,
                                    bulletpoint = updated.bulletpoint,
                                    isUserMessage = false
                                )
                            }

                      //      val isSubbed = subscriptionChecker.isUserSubscribed()
                     //   if (!isSubbed && usageCount % 6 == 0) { // 0, 2, 4... → before incrementing, so acts on 1st, 3rd, 5th...
                     //       triggerInterstitialAd()
                    //    }

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

        viewModelScope.launch {
            anythingScreenRepository.saveAnything(updated)
            _saveAnything.value = listOf(updated)
        }
    }



    private suspend fun saveAnything(
        id: Int?, summarize: String, rephrase: String, paraphrase: String, expand: String, bulletpoint: String, isUserMessage: Boolean
    ) {
        if (id == null || id == 0) {
            // No ID → Insert new
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
            anythingScreenRepository.saveAnything(newText) // Insert
        } else {
            // ID exists → Update existing entry
            val existingText = anythingScreenRepository.getAnythingTextById(id)
            if (existingText != null) {
                val updatedText = existingText.copy(
                    summarize = if (summarize.isNotEmpty()) summarize else existingText.summarize,
                    paraphrase = if (paraphrase.isNotEmpty()) paraphrase else existingText.paraphrase,
                    rephrase = if (rephrase.isNotEmpty()) rephrase else existingText.rephrase,
                    expand = if (expand.isNotEmpty()) expand else existingText.expand,
                    bulletpoint = if (bulletpoint.isNotEmpty()) bulletpoint else existingText.bulletpoint,
                )
                anythingScreenRepository.saveAnything(updatedText) // Update
            }
        }
    }

}