package com.kaankivancdilli.summary.ui.component.photomain.ocr.cloud

import android.graphics.Bitmap
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class CloudOcrClient {

    private val apiKey = "AIzaSyDKswgXx933-BPWYEFNK_m4nubWzawPkt4"

    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    suspend fun recognizeText(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        try {
            // Convert Bitmap to Base64 string
            val byteArray = bitmapToJpegByteArray(bitmap)
            val base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP)

            // Build the JSON payload for Google Vision API
            val jsonRequest = JSONObject().apply {
                put("requests", listOf(JSONObject().apply {
                    put("image", JSONObject().put("content", base64Image))
                    put("features", listOf(JSONObject().put("type", "TEXT_DETECTION")))
                }))
            }

            val body = jsonRequest.toString().toRequestBody(JSON)

            val url = "https://vision.googleapis.com/v1/images:annotate?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Cloud OCR failed with HTTP ${response.code}")
                }
                val responseBody = response.body?.string() ?: ""
                val jsonResponse = JSONObject(responseBody)
                val textAnnotations = jsonResponse
                    .getJSONArray("responses")
                    .getJSONObject(0)
                    .optJSONArray("textAnnotations")
                if (textAnnotations != null && textAnnotations.length() > 0) {
                    return@withContext textAnnotations.getJSONObject(0).getString("description")
                }
                return@withContext ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext ""
        }
    }

    private fun bitmapToJpegByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        return outputStream.toByteArray()
    }
}

