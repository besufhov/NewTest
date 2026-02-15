package com.kaankivancdilli.summary.ui.viewmodel.main.anything

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaankivancdilli.summary.data.model.local.SaveAnything
import com.kaankivancdilli.summary.ui.state.network.ResultState
import com.kaankivancdilli.summary.network.rest.RestApiManager
import com.kaankivancdilli.summary.core.state.FreeUsageTracker
import com.kaankivancdilli.summary.core.domain.SubscriptionChecker
import com.kaankivancdilli.summary.ui.viewmodel.sub.subscription.SubscriptionViewModel
import com.kaankivancdilli.summary.core.billing.manager.BillingManager
import com.kaankivancdilli.summary.core.controller.anything.AdController
import com.kaankivancdilli.summary.core.controller.anything.MessageDispatcher
import com.kaankivancdilli.summary.core.controller.anything.SubscriptionController
import com.kaankivancdilli.summary.core.manager.CountdownManager
import com.kaankivancdilli.summary.core.domain.handler.anything.AnythingResponseHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AnythingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val subscriptionChecker: SubscriptionChecker,
    private val billingManager: BillingManager,
    val freeUsageTracker: FreeUsageTracker,
    private val countdownManager: CountdownManager,
    private val responseHandler: AnythingResponseHandler,
) : ViewModel() {

    private val restApiManager = RestApiManager()

    private val adController = AdController(freeUsageTracker)
    private val subscriptionController = SubscriptionController(subscriptionChecker, billingManager)
    private val dispatcher = MessageDispatcher(context, restApiManager)

    private var _continueAfterAd: (() -> Unit)? = null

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

    private val _countdownTimers = MutableStateFlow<Map<String, Int>>(emptyMap())
    val countdownTimers: StateFlow<Map<String, Int>> = _countdownTimers

    private val _showSubscribeDialog = MutableStateFlow(false)
    val showSubscribeDialog: StateFlow<Boolean> = _showSubscribeDialog

    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed

    private val _processedResponses = mutableSetOf<String>()

    private var _latestInputFields: Map<String, String> = emptyMap()

    private var pendingContent: String? = null
    private var pendingActionType: String? = null
    private var pendingInputFields: Map<String, String> = emptyMap()

    init {
        observeTexts()
        checkInitialSubscriptionStatus()
    }

    fun setSelectedAction(action: String?) {
        _selectedActionType.value = action
    }

    fun startCountdownForAction(action: String) {
        viewModelScope.launch {
            countdownManager.startCountdown(action, _countdownTimers)
        }
    }

    fun continueAfterAd() {
        _continueAfterAd?.invoke()
        _continueAfterAd = null
    }


    fun isNewResponse(content: String): Boolean {
        val id = content.hashCode().toString()
        return _processedResponses.add(id)
    }

    fun triggerInterstitialAd() {
        if (adController.shouldSkipInterstitial()) return
        _showInterstitialAd.value = true
    }

    fun resetInterstitialAdTrigger() {
        _showInterstitialAd.value = false
    }

    fun rewardUserWithReset() {
        viewModelScope.launch {
            adController.onRewardEarned()
            hideSubscribeDialog()

            pendingContent?.let {
                sendToWebSocket(it, pendingActionType, pendingInputFields)
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

            if (usageCount >= 20 && !isSubbed) {
                pendingContent = content
                pendingActionType = type
                pendingInputFields = _latestInputFields
                _showSubscribeDialog.value = true
                return@launch
            }

            dispatcher.sendToServer(content)
        }
    }

    fun subscribeUser(activity: Activity, subscriptionViewModel: SubscriptionViewModel) {
        subscriptionController.subscribe(
            activity = activity,
            scope = viewModelScope,
            onSubscribed = {
                _isSubscribed.value = true
                pendingContent?.let {
                    sendToWebSocket(it, pendingActionType, pendingInputFields)
                    clearPendingMessage()
                }
            },
            onDialogDismiss = { _showSubscribeDialog.value = false }
        )
    }

    fun setSubscriptionStatus(value: Boolean) {
        _isSubscribed.value = value
    }

    private fun sendToWebSocket(content: String, type: String?, fields: Map<String, String>) {
        dispatcher.sendToWebSocket(type, content)
    }

    fun hideSubscribeDialog() {
        _showSubscribeDialog.value = false
    }

    private fun clearPendingMessage() {
        pendingContent = null
        pendingActionType = null
        pendingInputFields = emptyMap()
    }

    private fun checkInitialSubscriptionStatus() {
        viewModelScope.launch {
            _isSubscribed.value = subscriptionChecker.isUserSubscribed()
        }
    }

    private fun observeTexts() {
        viewModelScope.launch {
            restApiManager.texts.collect { result ->
                when (result) {
                    is ResultState.Success -> {
                        responseHandler.handleSuccess(
                            extractedText = result.data,
                            selectedActionType = _selectedActionType.value,
                            latestInputFields = _latestInputFields,
                            isSummarized = _isSummarizedText.value,
                            onMessageBuilt = { message ->
                                _saveAnything.update { it + message }
                            },
                            onSummarizedStateReset = {
                                _isSummarizedText.value = false
                            },
                            onTriggerInterstitial = {
                                triggerInterstitialAd()
                            }
                        )
                    }

                    is ResultState.Error -> {
                        responseHandler.handleError(
                            setError = { _errorMessage.value = it },
                            message = result.message
                        )
                    }

                    else -> Unit
                }
            }
        }
    }
}
