package com.kaankivancdilli.summary.utils.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.ExperimentalUnitApi
import com.kaankivancdilli.summary.utils.state.tts.TextToSpeechState
import java.util.Locale


@OptIn(ExperimentalUnitApi::class) // Enable experimental feature for unit conversions

@Composable
fun rememberTextToSpeech(context: Context): TextToSpeechState {
    var lastSpokenText by remember { mutableStateOf<String?>(null) }
    var lastSpokenPosition by remember { mutableStateOf(0) }
    var isSpeaking by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(Locale.US) }

    val ttsEnginePackage = "com.google.android.tts"
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    // Used to remember if initialization succeeded
    val initialized = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            // Try initializing with Google TTS
            tts = TextToSpeech(context, { status ->
                if (status == TextToSpeech.SUCCESS) {
                    initialized.value = true
                    configureTts(
                        tts,
                        selectedLanguage,
                        onSpeakingStateChanged = { isSpeaking = it },
                        onPositionChanged = { lastSpokenPosition = it }  // <- this is what was missing
                    )
                } else {
                    // Fallback to default engine
                    tts = TextToSpeech(context) { fallbackStatus ->
                        if (fallbackStatus == TextToSpeech.SUCCESS) {
                            initialized.value = true
                            configureTts(
                                tts,
                                selectedLanguage,
                                onSpeakingStateChanged = { isSpeaking = it },
                                onPositionChanged = { lastSpokenPosition = it }  // <- this is what was missing
                            )
                        } else {
                            Log.e("TTS", "TTS fallback initialization failed")
                        }
                    }
                }
            }, ttsEnginePackage)
        } catch (e: Exception) {
            Log.e("TTS", "Exception during TTS init: ${e.message}")
            // Fallback to default engine
            tts = TextToSpeech(context) { fallbackStatus ->
                if (fallbackStatus == TextToSpeech.SUCCESS) {
                    initialized.value = true
                    configureTts(
                        tts,
                        selectedLanguage,
                        onSpeakingStateChanged = { isSpeaking = it },
                        onPositionChanged = { lastSpokenPosition = it }  // <- this is what was missing
                    )
                } else {
                    Log.e("TTS", "TTS fallback initialization failed")
                }
            }
        }
    }

    val stopSpeaking: () -> Unit = {
        if (tts?.isSpeaking == true) tts?.stop()
        lastSpokenText = null
        lastSpokenPosition = 0
        isSpeaking = false
    }

    val startSpeaking: (String, Locale) -> Unit = run {
        { text, locale ->
            if (!initialized.value) {
                Log.w("TTS", "TTS is not initialized yet.")
            } else {
                if (isSpeaking) stopSpeaking()

                lastSpokenText = text
                lastSpokenPosition = 0

                val result = tts?.setLanguage(locale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported: $locale")
                }

                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UTTERANCE_ID")
                isSpeaking = true
            }
        }
    }


    val pauseSpeaking: () -> Unit = {
        if (tts?.isSpeaking == true) {
            tts?.stop()
            isSpeaking = false
        }
    }

    val resumeSpeaking: () -> Unit = {
        lastSpokenText?.let { text ->
            if (!isSpeaking && lastSpokenPosition < text.length) {
                tts?.speak(text.substring(lastSpokenPosition), TextToSpeech.QUEUE_FLUSH, null, "UTTERANCE_ID")
                isSpeaking = true
            }
        }
    }

    return TextToSpeechState(
        tts = tts,
        isSpeaking = isSpeaking,
        lastSpokenText = lastSpokenText,
        lastSpokenPosition = lastSpokenPosition,
        selectedLanguage = selectedLanguage,
        startSpeaking = startSpeaking,
        pauseSpeaking = pauseSpeaking,
        resumeSpeaking = resumeSpeaking,
        stopSpeaking = stopSpeaking
    )
}

private fun configureTts(
    tts: TextToSpeech?,
    selectedLanguage: Locale,
    onSpeakingStateChanged: (Boolean) -> Unit,
    onPositionChanged: (Int) -> Unit // <--- add this
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




