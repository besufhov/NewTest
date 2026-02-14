package com.kaankivancdilli.summary.ui.component.audio.tts.configuration

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

fun configureTts(
    tts: TextToSpeech?,
    selectedLanguage: Locale,
    onSpeakingStateChanged: (Boolean) -> Unit,
    onPositionChanged: (Int) -> Unit
) {
    val languageResult = tts?.setLanguage(selectedLanguage)
    if (languageResult == TextToSpeech.LANG_MISSING_DATA || languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
        Log.e("TTS", "Language not supported: $selectedLanguage")
    } else {
        Log.d("TTS", "Language set successfully: $selectedLanguage")
    }

    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            onSpeakingStateChanged(true)
        }

        override fun onDone(utteranceId: String?) {
            onSpeakingStateChanged(false)
        }

        override fun onError(utteranceId: String?) {
            onSpeakingStateChanged(false)
        }

        override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
            onPositionChanged(start)
        }
    })
}