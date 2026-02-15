package com.kaankivancdilli.summary.data.repository.main.anything

import android.util.Log
import com.kaankivancdilli.summary.data.local.dao.anything.AnythingDao
import com.kaankivancdilli.summary.data.model.local.SaveAnything
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AnythingScreenRepository @Inject constructor(
    private val anythingDao: AnythingDao
) {
    suspend fun saveAnything(message: SaveAnything) {

        val existingText = anythingDao.getLatestEntryForSummary(message.summarize)

        if (existingText != null) {

            if (message.summarize.isNotBlank()) {
                anythingDao.updateSummary(message.summarize)
            }

            if (message.paraphrase.isNotBlank()) {
                anythingDao.updateParaphrase(message.summarize, message.paraphrase)
            }

            if (message.rephrase.isNotBlank()) {
                anythingDao.updateRephrase(message.summarize, message.rephrase)
            }

            if (message.expand.isNotBlank()) {
                anythingDao.updateExpand(message.summarize, message.expand)
            }

            if (message.bulletpoint.isNotBlank()) {
                anythingDao.updateBulletpoint(message.summarize, message.bulletpoint)
            }

        } else {

            anythingDao.insertAnything(message)
        }
    }

    suspend fun getAnythingTextById(textId: Int): SaveAnything? {
        val result = anythingDao.getAnythingTextById(textId)
        Log.d("AnythingScreenRepository", "Fetched text: $result")
        return result
    }

    fun getAllAnything(): Flow<List<SaveAnything>> {
        return anythingDao.getAllAnything()
    }

    suspend fun deleteAnything(message: SaveAnything) {
        anythingDao.deleteAnything(message)
    }
}