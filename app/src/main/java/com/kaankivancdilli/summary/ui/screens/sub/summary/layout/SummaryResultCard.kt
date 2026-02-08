package com.kaankivancdilli.summary.ui.screens.sub.summary.layout

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kaankivancdilli.summary.ui.screens.main.isTablet
import com.kaankivancdilli.summary.ui.screens.sub.summary.type.ActionType
import com.kaankivancdilli.summary.ui.utils.reusable.EditablePreviewText
import com.kaankivancdilli.summary.ui.utils.Audio.AudioControls
import com.kaankivancdilli.summary.ui.utils.detection.autoDetectLanguage
import com.kaankivancdilli.summary.ui.utils.reusable.ExportButtons
import com.kaankivancdilli.summary.ui.utils.detection.state.TextToSpeechState
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SummaryResultCard(
    action: ActionType,
    resultText: String,
    fileName: String,
    ttsState: TextToSpeechState,
    selectedLanguage: Locale,
    enableEditing: Boolean = false,
    onSaveEdit: (ActionType, String) -> Unit,
    modifier: Modifier = Modifier, // ✅ ADD THIS
    onExpand: (() -> Unit)? = null // ✅ ADD THIS
) {
    var internalLang by remember { mutableStateOf(selectedLanguage) }
    LaunchedEffect(resultText) {
        val detected = autoDetectLanguage(resultText)
        internalLang = detected
        ttsState.tts?.language = detected
    }
    var expanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val cleaned = resultText.replace(Regex("[#*\"`]"), "").trim()
    val lines = cleaned.split("\n")
    val title = lines.firstOrNull().orEmpty()
    val body = lines.drop(1).joinToString("\n")

    var titleText by remember { mutableStateOf(title) }
    var bodyText by remember { mutableStateOf(body) }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(cleaned) {
        titleText = title
        bodyText  = body
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            // tapping your card can still trigger onExpand if you need it
            .combinedClickable(
                onClick    = { onExpand?.invoke() },
                onLongClick= { expanded = !expanded },
                role       = Role.Button
            ),
        shape   = RoundedCornerShape(12.dp),
     //   border  = BorderStroke(1.dp, Color(0xFFB3B3B3)),
        colors  = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            val sizeModifier = if (expanded) {
                Modifier
                    .fillMaxWidth()
                    // no explicit max – it will wrap to the full text height
                    .wrapContentHeight()
            } else {
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 600.dp)
            }
            // enable scrolling *only* during editing, otherwise let wrapContentHeight do its job
            val boxMod = Modifier
                .fillMaxWidth()
                .then(
                    if (isEditing) {
                        Modifier
                            .heightIn(min = 200.dp, max = 600.dp)
                            .verticalScroll(scrollState)
                    } else if (expanded) {
                        Modifier.wrapContentHeight()
                    } else {
                        Modifier.heightIn(min = 200.dp, max = 600.dp)
                    }
                )
                .shadow(
                    elevation = 1.dp,
                    shape = RoundedCornerShape(8.dp),
                    clip = false // important to keep the shadow visible
                )
                .border(0.2.dp, Color.LightGray, shape = RoundedCornerShape(8.dp))
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .padding(12.dp) // Inner padding for readability

            Box(modifier = boxMod) {
                if (enableEditing) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (isEditing) {
                                IconButton(onClick = {
                                    onSaveEdit(action, "$titleText\n$bodyText")
                                    isEditing = false
                                }) {
                                    Icon(Icons.Default.Save, "Save", tint = Color.Gray)
                                }
                            }
                            IconButton(onClick = { isEditing = !isEditing }) {
                                Icon(
                                    if (isEditing) Icons.Default.Visibility else Icons.Default.Edit,
                                    if (isEditing) "Preview" else "Edit",
                                    tint = Color.Gray
                                )
                            }
                        }

                        EditablePreviewText(
                            titleText = titleText,
                            bodyText = bodyText,
                            isEditing = isEditing,
                            onTitleChange = { titleText = it },
                            onBodyChange = { bodyText = it }
                        )
                    }
                } else {
                    // ABSOLUTELY NO SCROLLING HERE
                    Text(
                        text = cleaned,
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Start
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

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

            Spacer(Modifier.height(12.dp))

            ExportButtons(
                fileName = fileName,
                text = cleaned,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


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
                    // Left: Close
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }

                    // Right: Save (if editing) + Edit/Preview toggle
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

                    // 👉 Box for text only
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
                                clip = false // important to keep the shadow visible
                            )
                            .border(0.2.dp, Color.LightGray, shape = RoundedCornerShape(8.dp))
                            .background(Color.White, shape = RoundedCornerShape(8.dp))
                            .padding(12.dp) // Inner padding for readability
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
                            // ABSOLUTELY NO SCROLLING HERE
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






