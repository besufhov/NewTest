package com.kaankivancdilli.summary.ui.component.audio.control

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun AudioControls(
    isSpeaking: Boolean,
    lastSpokenText: String?,
    lastSpokenPosition: Int,
    summaryText: String,
    selectedLanguage: Locale,
    startSpeaking: (String, Locale) -> Unit,
    pauseSpeaking: () -> Unit,
    resumeSpeaking: () -> Unit,
    stopSpeaking: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                if (isSpeaking) {
                    pauseSpeaking()
                } else {
                    if (lastSpokenText == summaryText && lastSpokenPosition > 0) {
                        resumeSpeaking()
                    } else {
                        startSpeaking(summaryText, selectedLanguage)
                    }
                }
            },
            modifier = Modifier
                .height(68.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .shadow(
                    elevation = 1.dp,
                    shape = MaterialTheme.shapes.large,
                    clip = false
                )
                .border(
                    width = 0.05.dp,
                    color = Color.LightGray,
                    shape = MaterialTheme.shapes.large
                )
               ,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = if (isSpeaking) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                contentDescription = if (isSpeaking) "Pause" else "Play",
                modifier = Modifier
                    .size(36.dp),
                tint = Color.Black
            )
        }

        Button(
            onClick = { stopSpeaking() },
            modifier = Modifier
                .height(68.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .shadow(
                    elevation = 1.dp,
                    shape = MaterialTheme.shapes.large,
                    clip = false
                )
                .border(
                    width = 0.05.dp,
                    color = Color.LightGray,
                    shape = MaterialTheme.shapes.large
                )
            ,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = MaterialTheme.shapes.large,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)

        ) {
            Icon(
                imageVector = Icons.Outlined.Stop,
                contentDescription = "Stop",
                modifier = Modifier
                    .size(36.dp),
                tint = Color.Black
            )
        }
    }
}