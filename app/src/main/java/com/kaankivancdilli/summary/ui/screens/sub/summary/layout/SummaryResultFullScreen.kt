package com.kaankivancdilli.summary.ui.screens.sub.summary.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kaankivancdilli.summary.ui.component.device.isTablet
import com.kaankivancdilli.summary.ui.screens.sub.summary.type.ActionType
import com.kaankivancdilli.summary.ui.component.audio.control.AudioControls
import com.kaankivancdilli.summary.core.detection.autoDetectLanguage
import com.kaankivancdilli.summary.ui.component.reusable.buttons.export.ExportButtons
import com.kaankivancdilli.summary.ui.component.reusable.previewtext.EditablePreviewText
import com.kaankivancdilli.summary.ui.state.tts.TextToSpeechState
import java.util.Locale

@Composable
fun SummaryResultFullScreen(
    resultText: String,
    fileName: String,
    ttsState: TextToSpeechState,
    selectedLanguage: Locale,
    onDismiss: () -> Unit,
    onSaveEdit: (ActionType, String) -> Unit,
    actionType: ActionType
) {
    var internalLang by remember { mutableStateOf(selectedLanguage) }

    LaunchedEffect(resultText) {
        val detected = autoDetectLanguage(resultText)
        internalLang = detected
        ttsState.tts?.language = detected
    }

    val cleaned = resultText.replace(Regex("[#*\"]"), "").trim()
    val lines = cleaned.split("\n")
    val title = lines.firstOrNull().orEmpty()
    val body = lines.drop(1).joinToString("\n")

    var titleText by remember { mutableStateOf(title) }
    var bodyText by remember { mutableStateOf(body) }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(cleaned) {
        titleText = title
        bodyText = body
    }

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .verticalScroll(scrollState)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }

                    Row {
                        if (isEditing) {
                            IconButton(onClick = {
                                onSaveEdit(actionType, "$titleText\n$bodyText")
                                isEditing = false
                            }) {
                                Icon(Icons.Default.Save, contentDescription = "Save")
                            }
                        }
                        IconButton(onClick = { isEditing = !isEditing }) {
                            Icon(
                                if (isEditing) Icons.Default.Visibility else Icons.Default.Edit,
                                contentDescription = if (isEditing) "Preview" else "Edit"
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            if (isTablet()) PaddingValues(32.dp)
                            else PaddingValues(vertical = 8.dp, horizontal = 10.dp)
                        )
                        .shadow(
                            elevation = 1.dp,
                            shape = RoundedCornerShape(8.dp),
                            clip = false
                        )
                        .border(0.2.dp, Color.LightGray, shape = RoundedCornerShape(8.dp))
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    if (isEditing) {
                        Column {
                            EditablePreviewText(
                                titleText = titleText,
                                bodyText = bodyText,
                                isEditing = isEditing,
                                onTitleChange = { titleText = it },
                                onBodyChange = { bodyText = it }
                            )
                        }
                    } else {
                        Column {
                            EditablePreviewText(
                                titleText = titleText,
                                bodyText = bodyText,
                                isEditing = isEditing,
                                onTitleChange = { titleText = it },
                                onBodyChange = { bodyText = it }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                AudioControls(
                    isSpeaking = ttsState.isSpeaking,
                    lastSpokenText = ttsState.lastSpokenText,
                    lastSpokenPosition = ttsState.lastSpokenPosition,
                    summaryText = cleaned,
                    startSpeaking = ttsState.startSpeaking,
                    pauseSpeaking = ttsState.pauseSpeaking,
                    resumeSpeaking = ttsState.resumeSpeaking,
                    stopSpeaking = ttsState.stopSpeaking,
                    selectedLanguage = internalLang
                )

                Spacer(Modifier.height(16.dp))

                ExportButtons(
                    fileName = fileName,
                    text = cleaned,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}