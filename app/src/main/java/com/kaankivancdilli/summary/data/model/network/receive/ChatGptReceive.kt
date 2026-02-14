package com.kaankivancdilli.summary.data.model.network.receive

data class ChatGptReceive(
    val type: String,
    val event_id: String?,
    val session: SessionContent?,
    val item: ItemContent?,
    val response: ResponseData?
)

data class ResponseData(
    val objectType: String,
    val id: String,
    val status: String,
    val output: List<ItemContent>
)

data class SessionContent(
    val id: String,
    val model: String,
    val expires_at: Long,
    val modalities: List<String>,
    val instructions: String,
    val voice: String,
    val turn_detection: TurnDetection,
    val input_audio_format: String,
    val output_audio_format: String,
    val input_audio_transcription: String?,
    val tool_choice: String,
    val temperature: Double,
    val max_response_output_tokens: String,
    val client_secret: String?,
    val tools: List<String>
)

data class TurnDetection(
    val type: String,
    val threshold: Double,
    val prefix_padding_ms: Long,
    val silence_duration_ms: Long,
    val create_response: Boolean,
    val interrupt_response: Boolean
)

data class ItemContent(
    val id: String,
    val objectType: String,
    val type: String,
    val status: String,
    val role: String,
    val content: List<Content>
)

// New data class to capture the "content" field inside the "item"
data class Content(
    val type: String,
    val text: String
)
