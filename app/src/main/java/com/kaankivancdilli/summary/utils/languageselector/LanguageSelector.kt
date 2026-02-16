package com.kaankivancdilli.summary.utils.languageselector

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun LanguageSelector(
    tts: TextToSpeech?,
    selectedLanguage: Locale,
    onLanguageSelected: (Locale) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val languages = listOf(
        "🇺🇸 English" to Locale.US,
        "🇪🇸 Spanish" to Locale("es", "ES"),
        "🇫🇷 French" to Locale.FRANCE,
        "🇩🇪 German" to Locale.GERMANY,
        "🇹🇷 Turkish" to Locale("tr", "TR")
    )

    val selectedLang = languages.find { it.second == selectedLanguage }?.first ?: "🇺🇸 English"

    val buttonShape = MaterialTheme.shapes.large
    val buttonBorder = BorderStroke(1.dp, Color.LightGray)
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFFF5F5F5),
        contentColor = Color.Black
    )

    Box {
        Button(
            onClick = { expanded = true },
            shape = buttonShape,
            border = buttonBorder,
            colors = buttonColors
        ) {
            Text(selectedLang)
            Icon(
                imageVector = Icons.Outlined.ArrowDropDown,
                contentDescription = "Dropdown",
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFFF5F5F5))
        ) {
            languages.forEach { (name, locale) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        tts?.language = locale
                        onLanguageSelected(locale)
                        expanded = false
                    }
                )
            }
        }
    }
}