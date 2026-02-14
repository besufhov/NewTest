package com.kaankivancdilli.summary.ui.screens.main.anything.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kaankivancdilli.summary.ui.component.audio.control.AudioControls
import com.kaankivancdilli.summary.core.detection.autoDetectLanguage
import com.kaankivancdilli.summary.ui.state.tts.TextToSpeechState
import com.kaankivancdilli.summary.ui.component.reusable.previewtext.EditablePreviewText
import com.kaankivancdilli.summary.ui.component.reusable.buttons.export.ExportButtons
import java.util.Locale

@Composable
fun AnythingResponseCard(
    response: String,
    ttsState: TextToSpeechState,
) {

    var internalSelectedLanguage by remember { mutableStateOf(Locale.getDefault()) }

    LaunchedEffect(response) {
        val detectedLocale = autoDetectLanguage(response)
        internalSelectedLanguage = detectedLocale
        ttsState.tts?.language = detectedLocale
    }

    val fileName = "response_${System.currentTimeMillis()}"

    val cleanedText = response
        .replace(Regex("[#*\"`]"), "")
        .trim()

    val lines = cleanedText.split("\n")
    val title = lines.firstOrNull() ?: ""
    val body = lines.drop(1).joinToString("\n")

    var titleText by remember { mutableStateOf(title) }
    var bodyText by remember { mutableStateOf(body) }

        Column(
            modifier = Modifier
                .padding(vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 1.dp,
                        shape = RoundedCornerShape(8.dp),
                        clip = false
                    )
                    .border(0.2.dp, Color.LightGray, shape = RoundedCornerShape(8.dp))
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                EditablePreviewText(
                    titleText = titleText,
                    bodyText = bodyText,
                    isEditing = false,
                    onTitleChange = { titleText = it },
                    onBodyChange = { bodyText = it }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AudioControls(
                isSpeaking = ttsState.isSpeaking,
                lastSpokenText = ttsState.lastSpokenText,
                lastSpokenPosition = ttsState.lastSpokenPosition,
                summaryText = cleanedText,
                selectedLanguage = internalSelectedLanguage,
                startSpeaking = ttsState.startSpeaking,
                pauseSpeaking = ttsState.pauseSpeaking,
                resumeSpeaking = ttsState.resumeSpeaking,
                stopSpeaking = ttsState.stopSpeaking
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExportButtons(
                fileName = fileName,
                text = cleanedText,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }