package com.kaankivancdilli.summary.data.repository.sub.summary

import android.graphics.Bitmap
import android.util.Log
import com.kaankivancdilli.summary.data.local.dao.image.ImageDao
import com.kaankivancdilli.summary.data.local.dao.text.TextDao
import com.kaankivancdilli.summary.data.model.local.image.ImageEntity
import com.kaankivancdilli.summary.data.model.local.text.SaveTexts
import com.kaankivancdilli.summary.utils.bitmap.bitmapToByteArray
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class SummaryScreenRepository @Inject constructor(
    private val textDao: TextDao,
    private val imageDao: ImageDao
) {

    suspend fun saveTextWithImages(
        text: SaveTexts,
        images: List<Triple<String,Bitmap,String>>
    ): Long {

        val textId = textDao.insertMessage(text)

        imageDao.deleteByTextId(textId.toInt())

        images.forEach { (name, bmp, recognized) ->
            imageDao.insert(
                ImageEntity(
                textId       = textId.toInt(),
                name         = name,
                recognizedText = recognized,
                imageData    = bitmapToByteArray(bmp)
            )
            )
        }
        return textId
    }

    fun getTextWithImages(textId: Int): Flow<Pair<SaveTexts?, List<ImageEntity>>> =
        textDao.getSavedTextByIdFlow(textId)
            .combine(imageDao.getImagesForText(textId)) { txt, imgs ->
                txt to imgs
            }

    suspend fun saveText(message: SaveTexts) {
        val existingText = textDao.getLatestEntryForOcr(message.ocrText)

        if (existingText != null) {

            if (message.summarize.isNotBlank()) {
                textDao.updateSummary(message.ocrText, message.summarize)
            }

            if (message.paraphrase.isNotBlank()) {
                textDao.updateParaphrase(message.ocrText, message.paraphrase)
            }

            if (message.rephrase.isNotBlank()) {
                textDao.updateRephrase(message.ocrText, message.rephrase)
            }

            if (message.expand.isNotBlank()) {
                textDao.updateExpand(message.ocrText, message.expand)
            }

            if (message.bulletpoint.isNotBlank()) {
                textDao.updateBulletpoint(message.ocrText, message.bulletpoint)
            }

        } else {

            textDao.insertMessage(message)
        }
    }

    suspend fun getSavedTextById(textId: Int): SaveTexts? {
        val result = textDao.getSavedTextById(textId)
        Log.d("SummaryScreenRepository", "Fetched text: $result")
        return result
    }

    fun getSavedTextByOcrText(ocrText: String): SaveTexts? {
        return textDao.getSavedTextByOcrText(ocrText)
    }

    fun getTextHistory(): Flow<List<SaveTexts>> = textDao.getAllMessages()

    suspend fun deleteText(message: SaveTexts) {
        textDao.deleteMessage(message)
    }
}
