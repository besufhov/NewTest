package com.kaankivancdilli.summary.ui.viewmodel.main.history


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaankivancdilli.summary.data.model.local.anything.SaveAnything
import com.kaankivancdilli.summary.data.model.local.text.SaveTexts
import com.kaankivancdilli.summary.data.repository.anything.AnythingScreenRepository
import com.kaankivancdilli.summary.data.repository.sub.summary.SummaryScreenRepository
import com.kaankivancdilli.summary.utils.state.subscription.SubscriptionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TextHistoryViewModel @Inject constructor(
    private val summaryScreenRepository: SummaryScreenRepository,
    private val anythingScreenRepository: AnythingScreenRepository,
    val subscriptionChecker: SubscriptionChecker,
) : ViewModel() {

    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed

    private val _saveTexts = MutableStateFlow<List<SaveTexts>>(emptyList())
    val saveTexts: StateFlow<List<SaveTexts>> = _saveTexts

    private val _saveAnything = MutableStateFlow<List<SaveAnything>>(emptyList())
    val saveAnything: StateFlow<List<SaveAnything>> = _saveAnything

    init {
        loadTextHistory()

        viewModelScope.launch {
            val subscribed = subscriptionChecker.isUserSubscribed()
            _isSubscribed.value = subscribed
        }

    }

    fun setSubscriptionStatus(value: Boolean) {
        _isSubscribed.value = value
    }

    private fun loadTextHistory() {
        viewModelScope.launch {
            combine(
                summaryScreenRepository.getTextHistory(),
                anythingScreenRepository.getAllAnything()
            ) { texts, anythingList ->
                texts to anythingList
            }.collect { (texts, anythingList) ->
                _saveTexts.value = texts
                _saveAnything.value = anythingList
                Log.d("HistoryViewModel", "Loaded texts: ${texts.size}")
                Log.d("HistoryViewModel", "Loaded anything: ${anythingList.size}")
            }
        }
    }


    fun deleteMessage(message: SaveTexts) {
        viewModelScope.launch {
            summaryScreenRepository.deleteText(message) // Delete from Room
            _saveTexts.value = _saveTexts.value.filterNot { it.id == message.id } // Update UI
        }
    }

    fun deleteAnything(message: SaveAnything) {
        viewModelScope.launch {
            anythingScreenRepository.deleteAnything(message) // Delete from Room
            _saveAnything.value = _saveAnything.value.filterNot { it.id == message.id } // Update UI
        }
    }


}