package com.kaankivancdilli.summary

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

object SettingsDataStore {
    private val LANGUAGE_KEY = stringPreferencesKey("app_language")

    suspend fun saveLanguage(context: Context, language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    fun getLanguage(context: Context): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[LANGUAGE_KEY]
        }
    }
}


