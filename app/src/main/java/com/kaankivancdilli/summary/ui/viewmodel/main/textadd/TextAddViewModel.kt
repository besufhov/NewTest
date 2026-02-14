package com.kaankivancdilli.summary.ui.viewmodel.main.textadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaankivancdilli.summary.data.local.dao.textedit.TextEditDao
import com.kaankivancdilli.summary.data.model.local.textedit.SaveEditTexts
import com.kaankivancdilli.summary.network.ws.WebSocketManager
import com.kaankivancdilli.summary.ui.state.subscription.SubscriptionChecker
import com.kaankivancdilli.summary.ui.state.network.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TextAddViewModel @Inject constructor(
    private val dao: TextEditDao,
    val subscriptionChecker: SubscriptionChecker,
) : ViewModel() {

    private val webSocketManager = WebSocketManager()

    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed

    private val _textState = MutableStateFlow("")
    val textState: StateFlow<String> = _textState.asStateFlow()

    private val _summaryState = MutableStateFlow<ResultState<String>>(ResultState.Idle)
    val summaryState: StateFlow<ResultState<String>> = _summaryState.asStateFlow()

    init {

        viewModelScope.launch {
            val subscribed = subscriptionChecker.isUserSubscribed()
            _isSubscribed.value = subscribed
        }

        viewModelScope.launch {
            dao.getSavedText().collect { savedText ->
                _textState.value = savedText?.content ?: ""
            }
        }
    }

    fun setSubscriptionStatus(value: Boolean) {
        _isSubscribed.value = value
    }

    fun saveText(text: String) {
        viewModelScope.launch {
            dao.saveText(SaveEditTexts(id = 1, content = text, isUserMessage = true))
        }
    }

    fun updateTextState(newText: String) {
        _textState.value = newText
    }

    suspend fun summarizeText(text: String? = null) {
        val currentText = text ?: _textState.value  // Use passed text or the current _textState value

        if (currentText.isNotBlank()) {
            _summaryState.value = ResultState.Loading
            webSocketManager.sendMessage("Summarize this: $currentText")

            // Collect once per request
            viewModelScope.launch {
                webSocketManager.texts
                    .filterNotNull() // Optional: avoid nulls if any
                    .collect {
                        _summaryState.value = it
                    }
            }
        }
    }

    fun clearText() {
        _textState.value = ""
        _summaryState.value = ResultState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        webSocketManager.disconnect() // ✅ Disconnect when ViewModel is destroyed
    }

    fun resetSummary() {
        _summaryState.value = ResultState.Idle
    }
}