package com.kaankivancdilli.summary.ui.viewmodel.main.anything

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaankivancdilli.summary.data.model.local.anything.SaveAnything
import com.kaankivancdilli.summary.data.repository.main.anything.AnythingScreenRepository
import com.kaankivancdilli.summary.ui.state.network.ResultState
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.network.rest.RestApiManager
import com.kaankivancdilli.summary.ui.state.usage.FreeUsageTracker
import com.kaankivancdilli.summary.ui.state.subscription.SubscriptionChecker
import com.kaankivancdilli.summary.ui.viewmodel.sub.subscription.SubscriptionViewModel
import com.kaankivancdilli.summary.core.billing.manager.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnythingViewModel @Inject constructor(
    private val anythingScreenRepository: AnythingScreenRepository,
    @ApplicationContext private val context: Context,
    val subscriptionChecker: SubscriptionChecker,
    private val billingManager: BillingManager,
    val freeUsageTracker: FreeUsageTracker,

    ) : ViewModel() {

    private val restApiManager = RestApiManager()

    private var _continueAfterAd: (() -> Unit)? = null

    private var skipInterstitialOnceAfterReset = false

    private val _showInterstitialAd = MutableStateFlow(false)
    val showInterstitialAd: StateFlow<Boolean> = _showInterstitialAd

    private val _saveAnything = MutableStateFlow<List<SaveAnything>>(emptyList())
    val saveAnything: StateFlow<List<SaveAnything>> = _saveAnything

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isSummarizedText = MutableStateFlow(false)
    val isSummarizedText: StateFlow<Boolean> = _isSummarizedText

    private val _selectedActionType = MutableStateFlow<String?>(null)
    val selectedActionType: StateFlow<String?> = _selectedActionType

    private val _processedResponses = mutableSetOf<String>()

    private val _countdownTimers = MutableStateFlow<Map<String, Int>>(emptyMap())
    val countdownTimers: StateFlow<Map<String, Int>> = _countdownTimers

    private val _showSubscribeDialog = MutableStateFlow(false)
    val showSubscribeDialog: StateFlow<Boolean> = _showSubscribeDialog

    private var _latestInputFields: Map<String, String> = emptyMap()

    private var pendingContent: String? = null
    private var pendingActionType: String? = null
    private var pendingInputFields: Map<String, String> = emptyMap()

    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed

    fun setSelectedAction(action: String?) {
        _selectedActionType.value = action
    }

    fun startCountdownForAction(action: String) {
        viewModelScope.launch {
            _countdownTimers.update { it + (action to 5) }
            for (i in 4 downTo 0) {
                delay(1000L)
                _countdownTimers.update { it + (action to i) }
            }
            _countdownTimers.update { it - action }
        }
    }


    fun isNewResponse(content: String): Boolean {
        val id = content.hashCode().toString()
        return _processedResponses.add(id)
    }


    init {
        observeTexts()
        checkInitialSubscriptionStatus()
    }

    fun getEnglishKeyFromType(translatedType: String): String {
        return when (translatedType) {
            context.getString(R.string.tv_show) -> "tv_show"
            context.getString(R.string.book) -> "book"
            context.getString(R.string.movie) -> "movie"
            context.getString(R.string.article) -> "article"
            context.getString(R.string.biography) -> "biography"
            context.getString(R.string.anime) -> "anime"
            else -> "unknown"
        }
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

            skipInterstitialOnceAfterReset = true

            hideSubscribeDialog()

            if (pendingContent != null) {
                sendToWebSocket(pendingContent!!, pendingActionType, pendingInputFields)
                clearPendingMessage()
            }
        }
    }

    fun sendMessage(content: String, type: String, fields: Map<String, String>) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _selectedActionType.value = type
            _isSummarizedText.value = true
            _latestInputFields = fields.mapKeys { it.key.lowercase() }

            val usageCount = freeUsageTracker.getCount()
            val isSubbed = subscriptionChecker.isUserSubscribed()

            Log.d("AnythingVM", "Current free usage count: $usageCount")

            if (usageCount >= 20 && !isSubbed) {
                pendingContent = content
                pendingActionType = type
                pendingInputFields = fields.mapKeys { it.key.lowercase() }
                _showSubscribeDialog.value = true
                return@launch
            }

            sendToServer(content)
        }
    }

    private fun sendToServer(content: String) {
        restApiManager.sendMessage(context.getString(R.string.add_title) + content)
    }

    fun continueAfterAd() {
        _continueAfterAd?.invoke()
        _continueAfterAd = null
    }

    fun setSubscriptionStatus(value: Boolean) {
        _isSubscribed.value = value
    }

    fun subscribeUser(activity: Activity, subscriptionViewModel: SubscriptionViewModel) {
        billingManager.startConnection {
            billingManager.setPurchaseAcknowledgedListener {
                viewModelScope.launch {
                    val isNowSubscribed = subscriptionChecker.isUserSubscribed()
                    _isSubscribed.value = isNowSubscribed
                    Log.d("AnythingScreenViewModel", "Is user subscribed updated: ${_isSubscribed.value}")
                    if (isNowSubscribed) {
                        if (pendingContent != null) {
                            sendToWebSocket(pendingContent!!, pendingActionType, pendingInputFields)
                            clearPendingMessage()
                        }
                    }
                    _showSubscribeDialog.value = false
                    Log.d("AnythingScreenViewModel", "Show SubscribeDialog: ${_showSubscribeDialog.value}")
                }
            }

            billingManager.setSubscriptionListener { isSubscribed ->

                if (!isSubscribed) {
                    // Reset loading/spinner state
                    _selectedActionType.value = null
                    _showSubscribeDialog.value = false
                }
            }

            billingManager.launchPurchase(activity, "monthly_subscription")
        }
    }


    private fun sendToWebSocket(content: String, type: String?, fields: Map<String, String>) {
        _isSummarizedText.value = true
        pendingInputFields = fields.mapKeys { it.key.lowercase() }
        restApiManager.sendMessage(type + content)
        Log.d("AnythingScreenViewModel", "Message sent to WebSocket")
    }


    fun hideSubscribeDialog() {
        _showSubscribeDialog.value = false
        Log.d("AnythingScreenViewModel", "Spinner manually hidden")
    }

    private fun clearPendingMessage() {
        pendingContent = null
        pendingActionType = null
        pendingInputFields = emptyMap()
        Log.d("AnythingScreenViewModel", "Pending message cleared")
    }

    private fun checkInitialSubscriptionStatus() {
        viewModelScope.launch {
            val isNowSubscribed = subscriptionChecker.isUserSubscribed()
            _isSubscribed.value = isNowSubscribed
            Log.d("AnythingViewModel", "Initial subscription check: $isNowSubscribed")
        }
    }

    private fun observeTexts() {
        viewModelScope.launch {
            restApiManager.texts.collect { result ->
                when (result) {
                    is ResultState.Idle -> {}

                    is ResultState.Success -> {
                        val extractedText = result.data
                        val actionType = _selectedActionType.value ?: "unknown"
                        val englishKey = getEnglishKeyFromType(actionType)

                        val fields = _latestInputFields

                        Log.d("SaveAnything", "Saving fields: ${fields["name"]}, ${fields["season"]}, ${fields["episode"]}, ${fields["author"]}, ${fields["chapter"]}, ${fields["director"]}, ${fields["year"]}, ${fields["source"]}, ${fields["birthday"]}")


                        val message = SaveAnything(
                            type = englishKey,
                            summarize = extractedText,
                            paraphrase = if (englishKey == "paraphrase") extractedText else "",
                            rephrase = if (englishKey == "rephrase") extractedText else "",
                            expand = if (englishKey == "expand") extractedText else "",
                            bulletpoint = if (englishKey == "bulletpoint") extractedText else "",
                            name = fields["name"] ?: "",
                            season = fields["season"] ?: "",
                            episode = fields["episode"] ?: "",
                            author = fields["author"] ?: "",
                            chapter = fields["chapter"] ?: "",
                            director = fields["director"] ?: "",
                            year = fields["year"] ?: "",
                            source = fields["source"] ?: "",
                            birthday = fields["birthday"] ?: "",
                            isUserMessage = false
                        )


                        if (_isSummarizedText.value) {
                            viewModelScope.launch {
                                val usageCount = freeUsageTracker.getCount()
                                if (usageCount <= 20) {
                                    freeUsageTracker.incrementAndGet()
                                    Log.d("AnythingVM", "Free usage incremented to ${usageCount + 1}")
                                }

                                saveAnything(
                                    summary = extractedText,
                                    type = englishKey,
                                    isUserMessage = false,
                                    fields = fields
                                )
                                _saveAnything.update { it + message }
                                _isSummarizedText.value = false

                                val isSubbed = subscriptionChecker.isUserSubscribed()

                                if (!isSubbed && usageCount > 0 && usageCount % 6 == 0) {
                                    triggerInterstitialAd()
                                }
                            }
                        } else {
                            _saveAnything.update { it + message }
                        }

                        Log.d("AnythingViewModel", "Updated saveAnything list: $message")
                    }

                    is ResultState.Error -> {
                        _errorMessage.value = result.message
                    }

                    is ResultState.Loading -> {}
                }
            }
        }
    }

    private suspend fun saveAnything(
        summary: String,
        type: String,
        isUserMessage: Boolean,
        fields: Map<String, String>
    ) {
        anythingScreenRepository.saveAnything(
            SaveAnything(
                type = type,
                summarize = summary,
                paraphrase = if (type == "paraphrase") summary else "",
                rephrase = if (type == "rephrase") summary else "",
                expand = if (type == "expand") summary else "",
                bulletpoint = if (type == "bulletpoint") summary else "",
                name = fields["name"] ?: "",
                season = fields["season"] ?: "",
                episode = fields["episode"] ?: "",
                author = fields["author"] ?: "",
                chapter = fields["chapter"] ?: "",
                director = fields["director"] ?: "",
                year = fields["year"] ?: "",
                source = fields["source"] ?: "",
                birthday = fields["birthday"] ?: "",
                isUserMessage = isUserMessage
            )
        )
    }
}