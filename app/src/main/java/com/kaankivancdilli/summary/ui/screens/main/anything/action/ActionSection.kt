package com.kaankivancdilli.summary.ui.screens.main.anything.action

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.ui.screens.main.anything.layout.AnythingResponseCard
import com.kaankivancdilli.summary.ui.screens.main.anything.fields.getLocalizedActionFields
import com.kaankivancdilli.summary.utils.state.tts.TextToSpeechState
import java.util.Locale

@Composable
fun ActionSection(
    action: String,
    fields: List<String>,
    inputValues: MutableMap<String, MutableMap<String, String>>,
    responseTexts: MutableMap<String, String>,
    onInputChange: (String, String, String) -> Unit,
    onSend: () -> Unit,
    ttsState: TextToSpeechState,
    selectedLanguage: Locale,
    onLanguageSelected: (Locale) -> Unit,
    processingAction: String?, // Pass the loading state
    countdownTimers: Map<String, Int> // For send cooldown
) {
 //   val focusRequester = remember { FocusRequester() }

    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxWidth()) {

        val actionInputs = inputValues[action] ?: mutableMapOf()
        val localizedLabels = getLocalizedActionFields()

        fields.forEachIndexed { index, field ->
            val fieldValue = actionInputs[field] ?: ""
            val textFieldIsFocused = remember { mutableStateOf(false) }

            // 👇 Get localized label if available
            val label = localizedLabels[action]?.find { it.first == field }?.second ?: field

            // 👇 Determine if field should be enabled (only 1st field or others if first has value)
            val firstFieldKey = fields.firstOrNull()
            val firstFieldValue = firstFieldKey?.let { actionInputs[it].orEmpty() } ?: ""

            Spacer(modifier = Modifier.height(8.dp))

            val fieldHasText = fieldValue.isNotEmpty() // Track text presence

            TextField(
                value = fieldValue,
                onValueChange = { newValue -> onInputChange(action, field, newValue) },
                shape = RoundedCornerShape(12.dp),
                label = {
                    Text(
                        label,
                        fontSize = if (textFieldIsFocused.value || fieldHasText) 14.sp else 18.sp,
                        fontWeight = FontWeight.Normal
                    )
                },
                enabled = index == 0 || firstFieldValue.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .shadow(1.dp, RoundedCornerShape(12.dp))
                    .border(0.3.dp, Color.LightGray, RoundedCornerShape(12.dp))
                    .onFocusChanged { focusState ->
                        // Safe focus handling
                        runCatching {
                            textFieldIsFocused.value = focusState.isFocused
                        }
                    },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedLabelColor = Color.LightGray,
                    unfocusedLabelColor = Color.Gray
                ),
                textStyle = TextStyle(fontSize = 18.sp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SEND BUTTON
        val countdown = countdownTimers[action]
        val buttonEnabled = processingAction != action && countdown == null

        Button(
            onClick = {
                focusManager.clearFocus(force = true) // ⬅️ This removes both focus & cursor
                onSend()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp), // ⬅️ Increase height
            enabled = buttonEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (buttonEnabled) Color.Black else Color(0xFFB3B3B3)
            ),
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp) // ⬅️ More padding
        ) {
            when {
                processingAction == action -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp), // Slightly bigger spinner
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                }
                countdown != null -> {
                    Text(
                        stringResource(id = R.string.send) + " ($countdown)",
                        color = Color.White,
                        fontSize = 20.sp, // ⬅️ Bigger font
                        fontWeight = FontWeight.Medium
                    )
                }
                else -> {
                    Text(
                        stringResource(id = R.string.send),
                        color = Color.White,
                        fontSize = 20.sp, // ⬅️ Bigger font
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(8.dp))

        // RESPONSE CARD
        responseTexts[action]?.let { response ->
            key(response) {
                AnythingResponseCard(
                    response = response,
                    ttsState = ttsState,
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = onLanguageSelected
                )
            }
        }
    }
}
