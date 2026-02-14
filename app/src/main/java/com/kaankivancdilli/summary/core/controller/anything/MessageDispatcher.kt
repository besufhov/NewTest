package com.kaankivancdilli.summary.core.controller.anything

import android.content.Context
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.network.rest.RestApiManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MessageDispatcher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val restApiManager: RestApiManager
) {
    fun sendToServer(content: String) {
        restApiManager.sendMessage(context.getString(R.string.add_title) + content)
    }

    fun sendToWebSocket(type: String?, content: String) {
        restApiManager.sendMessage(type + content)
    }
}