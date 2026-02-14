package com.kaankivancdilli.summary.ui.state.usage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.usagePrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "usage_prefs")


class FreeUsageTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val FREE_COUNT_KEY = intPreferencesKey("free_usage_count")
    private val TOTAL_SUMMARY_COUNT_KEY = intPreferencesKey("total_summary_count")

    val countFlow: Flow<Int> = context.usagePrefsDataStore.data
        .map { prefs -> prefs[FREE_COUNT_KEY] ?: 0 }

    val totalCountFlow: Flow<Int> = context.usagePrefsDataStore.data
        .map { prefs -> prefs[TOTAL_SUMMARY_COUNT_KEY] ?: 0 }

    suspend fun incrementAndGet(): Int {
        var newCount = 0
        context.usagePrefsDataStore.edit { prefs ->
            val current = prefs[FREE_COUNT_KEY] ?: 0
            newCount = current + 1
            prefs[FREE_COUNT_KEY] = newCount

            val total = prefs[TOTAL_SUMMARY_COUNT_KEY] ?: 0
            prefs[TOTAL_SUMMARY_COUNT_KEY] = total + 1
        }
        return newCount
    }

    suspend fun getCount(): Int {
        val prefs = context.usagePrefsDataStore.data.first()
        return prefs[FREE_COUNT_KEY] ?: 0
    }

    suspend fun getTotalCount(): Int {
        val prefs = context.usagePrefsDataStore.data.first()
        return prefs[TOTAL_SUMMARY_COUNT_KEY] ?: 0
    }

    suspend fun resetCount() {
        context.usagePrefsDataStore.edit { prefs -> prefs[FREE_COUNT_KEY] = 0 }
    }
}
