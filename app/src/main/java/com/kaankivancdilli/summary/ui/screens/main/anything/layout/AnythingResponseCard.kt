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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kaankivancdilli.summary.ui.utils.Audio.AudioControls
import com.kaankivancdilli.summary.ui.utils.detection.autoDetectLanguage
import com.kaankivancdilli.summary.utils.state.tts.TextToSpeechState
import com.kaankivancdilli.summary.ui.utils.reusable.previewtext.EditablePreviewText
import com.kaankivancdilli.summary.ui.utils.reusable.buttons.ExportButtons
import java.util.Locale



@Composable
fun AnythingResponseCard(
    response: String,
    ttsState: TextToSpeechState,
    selectedLanguage: Locale,
    onLanguageSelected: (Locale) -> Unit
) {

    var internalSelectedLanguage by remember { mutableStateOf(Locale.getDefault()) }

    LaunchedEffect(response) {
        val detectedLocale = autoDetectLanguage(response)
        internalSelectedLanguage = detectedLocale
        ttsState.tts?.language = detectedLocale
    }

    val context = LocalContext.current
    val fileName = "response_${System.currentTimeMillis()}" // or use something like UUID

    // Clean the entire text, including the title and body, for unwanted characters
    val cleanedText = response
        .replace(Regex("[#*\"`]"), "") // remove markdown characters
        // .replace(Regex("\\s+"), " ")   // normalize whitespace
        .trim()

    // Apply cleaning logic for title and body just for the preview/edit section
    val lines = cleanedText.split("\n") // Now using cleaned text for title and body
    val title = lines.firstOrNull() ?: ""
    val body = lines.drop(1).joinToString("\n")

    var titleText by remember { mutableStateOf(title) }
    var bodyText by remember { mutableStateOf(body) }
    var isEditing by remember { mutableStateOf(false) }


        Column(
            modifier = Modifier
                .padding(vertical = 8.dp) // More internal spacing
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 1.dp,
                        shape = RoundedCornerShape(8.dp),
                        clip = false // important to keep the shadow visible
                    )
                    .border(0.2.dp, Color.LightGray, shape = RoundedCornerShape(8.dp))
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .padding(12.dp) // Inner padding for readability
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
                selectedLanguage = internalSelectedLanguage, // <-- pass here
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

