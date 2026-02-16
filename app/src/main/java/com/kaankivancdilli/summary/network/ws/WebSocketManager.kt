package com.kaankivancdilli.summary.network.ws

import com.kaankivancdilli.summary.data.model.network.receive.ChatGptReceive
import com.kaankivancdilli.summary.data.model.network.response.ChatGptResponse
import com.kaankivancdilli.summary.data.model.network.response.ResponseContent
import com.kaankivancdilli.summary.ui.state.network.ResultState
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import javax.inject.Inject

class WebSocketManager @Inject constructor(
    private val client: OkHttpClient,
    private val gson: Gson
) {

    private var webSocket: WebSocket? = null

    private val _messages = MutableStateFlow<ResultState<String>>(ResultState.Idle)

    val texts: StateFlow<ResultState<String>> = _messages.asStateFlow()

    private val openAiUrl = "wss://api.openai.com/v1/realtime?model=gpt-4o-mini-realtime-preview-2024-12-17"
    private val openAiToken = "Test"

    fun connect() {
        val request = Request.Builder()
            .url(openAiUrl)
            .addHeader("Authorization", "Bearer $openAiToken")
            .addHeader("OpenAI-Beta", "realtime=v1")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _messages.tryEmit(ResultState.Loading)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val chatResponse = gson.fromJson(text, ChatGptReceive::class.java)
                    val messageText = chatResponse.response?.output?.firstOrNull()?.content?.firstOrNull()?.text

                    if (!messageText.isNullOrEmpty()) {
                        _messages.tryEmit(ResultState.Success(messageText))
                        disconnect() // ✅ Disconnect after success
                    } else {
                        _messages.tryEmit(ResultState.Error("No text found in response"))
                    }
                } catch (e: Exception) {
                    _messages.tryEmit(ResultState.Error("Parsing error: ${e.localizedMessage}"))
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _messages.tryEmit(ResultState.Error("Connection error: ${t.message}"))
                disconnect()
            }
        })
    }

    fun sendMessage(userMessage: String) {
        if (webSocket == null) {
            connect()
        }

        val event = ChatGptResponse(
            type = "response.create",
            response = ResponseContent(
                modalities = listOf("text"),
                instructions = "$userMessage"
            )
        )

        val jsonMessage = gson.toJson(event)
        webSocket?.send(jsonMessage)
    }

    fun disconnect() {
        webSocket?.close(1000, null)
        webSocket = null
    }
}