package com.kaankivancdilli.summary.ui.screens.main.anything.values

fun updateInputValues(
    inputValues: MutableMap<String, MutableMap<String, String>>,
    action: String,
    field: String,
    value: String
): MutableMap<String, MutableMap<String, String>> {
    return inputValues.toMutableMap().apply {
        val updatedFields = getOrDefault(action, mutableMapOf()).toMutableMap()
        updatedFields[field] = value
        put(action, updatedFields)
    }
}