package com.kaankivancdilli.summary.ui.component.photomain.text.preview

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import com.kaankivancdilli.summary.ui.component.photomain.text.TextItem

@Composable
fun PreviewTextView(
    recognizedTexts: List<String>,
    onToggleExpand: () -> Unit,
    onDeleteImage: (Int) -> Unit,
    onDone: (String) -> Unit,
    animatedHeight: Dp,
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .height(animatedHeight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onDeleteImage(-1) },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                ,
                colors = ButtonDefaults.buttonColors(Color.White),
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(0.05.dp, Color.LightGray),
                elevation = ButtonDefaults.buttonElevation(1.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Clear,
                    contentDescription = "Clear",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Black
                )
            }

            Button(
                onClick = {
                    val allText = recognizedTexts.joinToString("\n").trim()
                    if (allText.isNotBlank()) {
                        onDone(allText)
                    } else {
                        Log.e("Navigation", "Empty text, not navigating")
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                ,
                colors = ButtonDefaults.buttonColors(Color.White),
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(0.05.dp, Color.LightGray),
                elevation = ButtonDefaults.buttonElevation(1.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "Done",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Black
                )
            }

            Button(
                onClick = {
                    if (recognizedTexts.isNotEmpty()) {
                        val allText = recognizedTexts.joinToString("\n")
                        clipboardManager.setText(AnnotatedString(allText))
                        Toast.makeText(context, "All text copied!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp),
                colors = ButtonDefaults.buttonColors(Color.White),
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(0.05.dp, Color.LightGray),
                elevation = ButtonDefaults.buttonElevation(1.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CopyAll,
                    contentDescription = "Copy All",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Black
                )
            }

            Button(
                onClick = onToggleExpand,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp),
                colors = ButtonDefaults.buttonColors(Color.White),
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(0.05.dp, Color.LightGray),
                elevation = ButtonDefaults.buttonElevation(1.dp)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Outlined.ExpandMore else Icons.Outlined.ExpandLess,
                    contentDescription = if (isExpanded) "Minimize" else "Expand",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Black
                )
            }
        }

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(recognizedTexts) { index, text ->
                TextItem(
                    text = text,
                    index = index,
                    onDelete = { onDeleteImage(index) }
                )
            }
        }
    }
}
