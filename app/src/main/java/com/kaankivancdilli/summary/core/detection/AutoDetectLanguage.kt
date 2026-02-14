package com.kaankivancdilli.summary.core.detection

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.mlkit.nl.languageid.LanguageIdentification
import kotlinx.coroutines.tasks.await
import java.util.Locale

val supportedLocales = mapOf(
    "en" to Locale.US,
    "es" to Locale("es", "ES"),
    "fr" to Locale.FRANCE,
    "de" to Locale.GERMANY,
    "tr" to Locale("tr", "TR")
)

suspend fun autoDetectLanguage(response: String): Locale {
    val languageIdentifier = LanguageIdentification.getClient()

    val cleanedResponse = response.trim().replace(Regex("[^\\p{L}\\p{N}\\s]"), "")
    if (cleanedResponse.length < 20) {
        return Locale.getDefault()
    }

    return try {
        val languageCode = languageIdentifier.identifyLanguage(cleanedResponse).await()
        Log.d("AutoDetect", "Language code: $languageCode")
        val locale = supportedLocales[languageCode]
        if (languageCode == "und" || locale == null) {
            Locale.getDefault()
        } else {
            locale
        }
    } catch (e: Exception) {
        Log.e("AutoDetect", "Language detection failed", e)
        Locale.getDefault()
    }
}