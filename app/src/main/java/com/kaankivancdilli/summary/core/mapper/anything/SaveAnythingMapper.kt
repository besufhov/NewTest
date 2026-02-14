package com.kaankivancdilli.summary.core.mapper.anything

import com.kaankivancdilli.summary.data.model.local.anything.SaveAnything

object SaveAnythingMapper {

    fun create(
        summary: String,
        type: String,
        isUserMessage: Boolean,
        fields: Map<String, String>
    ): SaveAnything {
        return SaveAnything(
            type = type,
            summarize = summary,
            paraphrase = if (type == "paraphrase") summary else "",
            rephrase = if (type == "rephrase") summary else "",
            expand = if (type == "expand") summary else "",
            bulletpoint = if (type == "bulletpoint") summary else "",
            name = fields["name"] ?: "",
            season = fields["season"] ?: "",
            episode = fields["episode"] ?: "",
            author = fields["author"] ?: "",
            chapter = fields["chapter"] ?: "",
            director = fields["director"] ?: "",
            year = fields["year"] ?: "",
            source = fields["source"] ?: "",
            birthday = fields["birthday"] ?: "",
            isUserMessage = isUserMessage
        )
    }
}