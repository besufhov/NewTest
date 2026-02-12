package com.kaankivancdilli.summary.data.model.network.response

data class ChatGptResponse(
    val type: String,
    val response: ResponseContent,
)

data class ResponseContent(
    val modalities: List<String>,
    val instructions: String,
)

data class OutputItem(
    val id: String,
    val objectType: String,
    val type: String,
    val status: String,
    val role: String,
    val content: List<ContentItem>?
)

data class ContentItem(
    val type: String,
    val text: String?
)
