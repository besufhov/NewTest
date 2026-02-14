package com.kaankivancdilli.summary.ui.screens.main.anything.request

import com.kaankivancdilli.summary.ui.viewmodel.main.anything.AnythingViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun sendRequest(
    action: String,
    inputValues: MutableMap<String, MutableMap<String, String>>,
    actionFields: Map<String, List<String>>,
    summary: String,
    viewModel: AnythingViewModel,
    setLoading: (String, Boolean) -> Unit,
    retryCount: Int = 0,
    maxRetries: Int = 5
) {
    val fields = actionFields[action] ?: return
    val firstField = fields.firstOrNull() ?: return
    val actionInputs = inputValues[action] ?: return
    val mainSubject = actionInputs[firstField]?.takeIf { it.isNotBlank() } ?: return

    val additionalDetails = fields.drop(1)
        .mapNotNull { field -> actionInputs[field]?.takeIf { it.isNotBlank() }?.let { value -> "$field $value" } }
        .joinToString(" - ")

    val formattedRequest = if (additionalDetails.isNotEmpty()) {
        "$action: $summary - $mainSubject - $additionalDetails"
    } else {
        "$action: $summary $mainSubject"
    }

    println("Sending request: $formattedRequest (attempt ${retryCount + 1})")

    setLoading(action, true)
    viewModel.sendMessage(formattedRequest, action, actionInputs)

    CoroutineScope(Dispatchers.Main).launch {
        delay(5500L)

        if (viewModel.selectedActionType.value == action) {
            if (retryCount < maxRetries) {
                println("Retrying request for action: $action (attempt ${retryCount + 2})")
                sendRequest(
                    action,
                    inputValues,
                    actionFields,
                    summary,
                    viewModel,
                    setLoading,
                    retryCount + 1,
                    maxRetries
                )
            } else {
                viewModel.setSelectedAction(null)
                setLoading(action, false)
                println("Max retries reached for action: $action. Giving up.")
            }
        }
    }
}