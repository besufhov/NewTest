package com.kaankivancdilli.summary.ui.utils.documents.extraction.pdf

import java.io.InputStream

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper

import com.tom_roush.pdfbox.io.MemoryUsageSetting

fun extractTextFromPdf(inputStream: InputStream, maxChars: Int = 16000): String {
    val textBuilder = StringBuilder()

    try {
        PDDocument.load(inputStream, null, MemoryUsageSetting.setupTempFileOnly()).use { document ->
            val totalPages = document.numberOfPages
            val stripper = PDFTextStripper()

            for (page in 1..totalPages) {
                if (textBuilder.length >= maxChars) break

                stripper.startPage = page
                stripper.endPage = page

                val pageText = try {
                    stripper.getText(document)
                } catch (oom: OutOfMemoryError) {
                    // Skip this page if it's too large
                    ""
                }

                val remaining = maxChars - textBuilder.length
                if (pageText.length <= remaining) {
                    textBuilder.append(pageText)
                } else {
                    textBuilder.append(pageText.substring(0, remaining))
                    break
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return textBuilder.toString()
}



fun extractTextFromPdfVanilla(inputStream: InputStream): String {
    return try {
        PDDocument.load(inputStream).use { document ->
            val stripper = PDFTextStripper()
            stripper.getText(document)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}
























