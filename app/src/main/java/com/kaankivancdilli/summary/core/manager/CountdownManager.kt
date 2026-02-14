package com.kaankivancdilli.summary.core.manager

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class CountdownManager @Inject constructor() {

    suspend fun startCountdown(
        action: String,
        countdownState: MutableStateFlow<Map<String, Int>>
    ) {
        countdownState.update { it + (action to 5) }

        for (i in 4 downTo 0) {
            delay(1000L)
            countdownState.update { it + (action to i) }
        }

        countdownState.update { it - action }
    }
}