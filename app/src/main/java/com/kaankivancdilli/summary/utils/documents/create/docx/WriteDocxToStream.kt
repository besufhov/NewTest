package com.kaankivancdilli.summary.utils.documents.create.docx

import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.OutputStream

fun writeDocxToStream(text: String, outputStream: OutputStream) {
    val doc = XWPFDocument()
    val para = doc.createParagraph()
    val run = para.createRun()
    run.setText(text)
    doc.write(outputStream)
    doc.close()
}