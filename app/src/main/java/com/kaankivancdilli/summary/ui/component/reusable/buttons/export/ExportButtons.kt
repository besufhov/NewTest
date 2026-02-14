package com.kaankivancdilli.summary.ui.component.reusable.buttons.export

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.utils.documents.share.text.sharePlainText
import com.kaankivancdilli.summary.utils.documents.saveastype.docx.saveTextAsDocxToDownloads
import com.kaankivancdilli.summary.utils.documents.saveastype.pdf.saveTextAsPdfToDownloads
import com.kaankivancdilli.summary.utils.documents.saveastype.txt.saveTextAsTxtToDownloads
import com.kaankivancdilli.summary.utils.documents.share.docx.shareDocxFromDownloads
import com.kaankivancdilli.summary.utils.documents.share.pdf.sharePdfFromDownloads
import com.kaankivancdilli.summary.utils.documents.share.txt.shareTxtFromDownloads


@Composable
fun ExportButtons(
    fileName: String,
    text: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var isPdfSaved by remember { mutableStateOf(false) }
    var isDocxSaved by remember { mutableStateOf(false) }

    val copiedLabel = stringResource(R.string.copied)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top
    ) {

        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .shadow(
                    elevation = 1.dp,
                    shape = MaterialTheme.shapes.large,
                    clip = false
                )
                .border(
                    width = 0.05.dp,
                    color = Color.LightGray,
                    shape = MaterialTheme.shapes.large
                )
               ,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = {
                    saveTextAsPdfToDownloads(context, fileName, text)
                    isPdfSaved = true
                }) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "Save as PDF",
                        modifier = Modifier.size(30.dp),
                        tint = Color.Black
                    )

                }

                if (isPdfSaved) {
                    IconButton(onClick = {
                        sharePdfFromDownloads(context, "$fileName.pdf")
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share PDF")
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .shadow(
                    elevation = 1.dp,
                    shape = MaterialTheme.shapes.large,
                    clip = false
                )
                .border(
                    width = 0.05.dp,
                    color = Color.LightGray,
                    shape = MaterialTheme.shapes.large
                )
            ,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = {
                        saveTextAsDocxToDownloads(context, fileName, text)
                        isDocxSaved = true
                }) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Save as DOCX",
                        modifier = Modifier.size(30.dp),
                        tint = Color.Black
                    )
                }

                if (isDocxSaved) {
                    IconButton(onClick = {
                        shareDocxFromDownloads(context, "$fileName.docx")
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share DOCX")
                    }
                }
            }
        }

        var isTextFileSaved by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .shadow(
                    elevation = 1.dp,
                    shape = MaterialTheme.shapes.large,
                    clip = false
                )
                .border(
                    width = 0.05.dp,
                    color = Color.LightGray,
                    shape = MaterialTheme.shapes.large
                )
            ,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = {
                    saveTextAsTxtToDownloads(context, fileName, text)
                    isTextFileSaved = true
                }) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save Text File",
                        modifier = Modifier.size(30.dp),
                        tint = Color.Black
                    )
                }

                if (isTextFileSaved) {
                    IconButton(onClick = {
                        shareTxtFromDownloads(context, "$fileName.txt")
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Text File")
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .shadow(
                    elevation = 1.dp,
                    shape = MaterialTheme.shapes.large,
                    clip = false
                )
                .border(
                    width = 0.05.dp,
                    color = Color.LightGray,
                    shape = MaterialTheme.shapes.large
                )
            ,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = MaterialTheme.shapes.large,
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = {
                    clipboardManager.setText(AnnotatedString(text))
                    Toast.makeText(context, copiedLabel, Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy to clipboard",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Black
                    )
                }

                IconButton(onClick = {
                    sharePlainText(context, text)
                }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Text",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Black
                    )
                }
            }
        }
    }
}