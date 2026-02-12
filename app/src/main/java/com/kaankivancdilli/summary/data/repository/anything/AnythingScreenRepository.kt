package com.kaankivancdilli.summary.data.repository.anything

import android.util.Log
import com.kaankivancdilli.summary.data.local.dao.anything.AnythingDao
import com.kaankivancdilli.summary.data.model.local.anything.SaveAnything
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AnythingScreenRepository @Inject constructor(
    private val anythingDao: AnythingDao
) {
    suspend fun saveAnything(message: SaveAnything) {

        val existingText = anythingDao.getLatestEntryForSummary(message.summarize)

        if (existingText != null) {
            // If summarizing, update only the summarize field
            if (message.summarize.isNotBlank()) {
                anythingDao.updateSummary(message.summarize)
            }

            // If paraphrasing, update only the paraphrase field
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
            // If no entry exists, insert the new message
            anythingDao.insertAnything(message)
        }
    }

    suspend fun getAnythingTextById(textId: Int): SaveAnything? {
        val result = anythingDao.getAnythingTextById(textId)
        Log.d("AnythingScreenRepository", "Fetched text: $result")
        return result
    }

    fun getAllAnything(): Flow<List<SaveAnything>> {
        return anythingDao.getAllAnything() // Make sure this is calling the correct DAO function
    }


    suspend fun deleteAnything(message: SaveAnything) { // New delete function
        anythingDao.deleteAnything(message)
    }
}