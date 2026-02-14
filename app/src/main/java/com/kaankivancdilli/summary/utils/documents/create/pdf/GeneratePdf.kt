package com.kaankivancdilli.summary.utils.documents.create.pdf

import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import java.io.OutputStream

fun generatePdf(text: String, outputStream: OutputStream) {
    val document = PdfDocument()
    val pageWidth = 595
    val pageHeight = 842
    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas

    val textPaint = TextPaint().apply {
        color = android.graphics.Color.BLACK
        textSize = 14f
    }

    val margin = 40
    val usableWidth = pageWidth - margin * 2

    val staticLayout = StaticLayout.Builder
        .obtain(text, 0, text.length, textPaint, usableWidth)
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .setLineSpacing(0f, 1f)
        .setIncludePad(false)
        .build()

    canvas.translate(margin.toFloat(), margin.toFloat())
    staticLayout.draw(canvas)

    document.finishPage(page)
    document.writeTo(outputStream)
    document.close()
}