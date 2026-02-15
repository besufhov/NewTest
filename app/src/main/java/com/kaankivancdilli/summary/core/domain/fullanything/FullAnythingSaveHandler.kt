package com.kaankivancdilli.summary.core.domain.fullanything

import com.kaankivancdilli.summary.data.model.local.SaveAnything
import com.kaankivancdilli.summary.data.repository.main.anything.AnythingScreenRepository
import javax.inject.Inject

class FullAnythingSaveHandler @Inject constructor(
    private val repository: AnythingScreenRepository
) {

    suspend fun saveAnything(
        id: Int?,
        summarize: String,
        rephrase: String,
        paraphrase: String,
        expand: String,
        bulletpoint: String,
        isUserMessage: Boolean
    ) {

        if (id == null || id == 0) {

            val newText = SaveAnything(
                summarize = summarize,
                paraphrase = paraphrase,
                rephrase = rephrase,
                expand = expand,
                bulletpoint = bulletpoint,
                isUserMessage = isUserMessage,
                type = "",
                name = "",
                season = "",
                episode = "",
                author = "",
                chapter = "",
                director = "",
                year = "",
                birthday = "",
                source = ""
            )

            repository.saveAnything(newText)

        } else {

            val existingText = repository.getAnythingTextById(id)

            if (existingText != null) {

                val updatedText = existingText.copy(
                    summarize = if (summarize.isNotEmpty()) summarize else existingText.summarize,
                    paraphrase = if (paraphrase.isNotEmpty()) paraphrase else existingText.paraphrase,
                    rephrase = if (rephrase.isNotEmpty()) rephrase else existingText.rephrase,
                    expand = if (expand.isNotEmpty()) expand else existingText.expand,
                    bulletpoint = if (bulletpoint.isNotEmpty()) bulletpoint else existingText.bulletpoint,
                )

                repository.saveAnything(updatedText)
            }
        }
    }
}