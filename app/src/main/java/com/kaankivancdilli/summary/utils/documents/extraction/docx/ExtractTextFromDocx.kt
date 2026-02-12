package com.kaankivancdilli.summary.utils.documents.extraction.docx

import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.InputStream

// DOCX text extraction function

fun extractTextFromDocx(inputStream: InputStream, maxChars: Int = 16000): String {
    val text = StringBuilder()

    try {
        XWPFDocument(inputStream).use { doc ->
            for (para in doc.paragraphs) {
                if (text.length >= maxChars) break

                val paraText = try {
                    para.text
                } catch (oom: OutOfMemoryError) {
                    // Skip this paragraph if too big
                    continue
                }

                val remaining = maxChars - text.length
                if (paraText.length <= remaining) {
                    text.append(paraText).append("\n")
                } else {
                    text.append(paraText.substring(0, remaining))
                    break
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return text.toString()
}



fun extractTextFromDocxVanilla(inputStream: InputStream): String {
    val doc = XWPFDocument(inputStream)
    val text = StringBuilder()
    for (para in doc.paragraphs) {
        text.append(para.text).append("\n")
    }
    return text.toString()
}
