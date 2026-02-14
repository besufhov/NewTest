package com.kaankivancdilli.summary.utils.documents.extraction.txt

import java.io.InputStream


fun extractTextFromTxt(inputStream: InputStream, maxChars: Int = 16000): String {
    val builder = StringBuilder()

    try {
        inputStream.bufferedReader().use { reader ->
            val buffer = CharArray(1024)
            var totalRead = 0

            while (true) {
                val charsRead = try {
                    reader.read(buffer)
                } catch (oom: OutOfMemoryError) {
                    // Stop reading if memory runs out
                    break
                }

                if (charsRead == -1) break

                val remaining = maxChars - totalRead
                if (charsRead <= remaining) {
                    builder.append(buffer, 0, charsRead)
                    totalRead += charsRead
                } else {
                    builder.append(buffer, 0, remaining)
                    break
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return builder.toString()
}