package com.kaankivancdilli.summary.ui.utils.detection.state

import android.speech.tts.TextToSpeech
import java.util.Locale

data class TextToSpeechState(
    val tts: TextToSpeech?,
    val isSpeaking: Boolean,
    val lastSpokenText: String?,
    val lastSpokenPosition: Int,
    val selectedLanguage: Locale,
    val startSpeaking: (String, Locale) -> Unit, // <-- here
    val pauseSpeaking: () -> Unit,
    val resumeSpeaking: () -> Unit,
    val stopSpeaking: () -> Unit
)
