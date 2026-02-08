package com.kaankivancdilli.summary.network

import com.google.android.datatransport.BuildConfig
import com.google.gson.Gson
import com.kaankivancdilli.summary.data.model.web.ChatGptRestData.ChatCompletionResponse
import com.kaankivancdilli.summary.utils.state.ResultState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject


class RestApiManager {

    private val gson = Gson()
    private val client = OkHttpClient()
    private val _messages = MutableStateFlow<ResultState<String>>(ResultState.Idle)
    val texts: StateFlow<ResultState<String>> = _messages.asStateFlow()

    private val apiUrl = "https://api.openai.com/v1/chat/completions"
    private val apiKey = "Test"

    fun sendMessage(userMessage: String) {
        _messages.value = ResultState.Loading

        val requestBody = mapOf(
            "model" to "gpt-4o-mini",
            "messages" to listOf(
                mapOf("role" to "user", "content" to userMessage)
            ),
            "temperature" to 0.7
        )

        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(
                gson.toJson(requestBody).toRequestBody("application/json".toMediaType())
            )
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _messages.tryEmit(ResultState.Error("Network error: ${e.localizedMessage}"))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        _messages.tryEmit(ResultState.Error("HTTP error: ${response.code}"))
                        return
                    }

                    val body = response.body?.string()
                    try {
                        val json = gson.fromJson(body, ChatCompletionResponse::class.java)
                        val reply = json.choices.firstOrNull()?.message?.content
                        if (!reply.isNullOrEmpty()) {
                            _messages.tryEmit(ResultState.Success(reply))
                        } else {
                            _messages.tryEmit(ResultState.Error("Empty response"))
                        }
                    } catch (e: Exception) {
                        _messages.tryEmit(ResultState.Error("Parsing error: ${e.localizedMessage}"))
                    }
                }
            }
        })
    }
}

