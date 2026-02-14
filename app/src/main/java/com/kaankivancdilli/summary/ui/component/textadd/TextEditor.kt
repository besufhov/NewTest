package com.kaankivancdilli.summary.ui.component.textadd

import android.graphics.Rect
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.ui.component.reusable.buttons.IconTextButton
import com.kaankivancdilli.summary.utils.keyboard.rememberKeyboardHeightDp

@Composable
fun TextEditor(
    text: String,
    onTextChange: (String) -> Unit,
    onClear: () -> Unit,
    onFileUpload: (Uri) -> Unit,
    isSubscribed: Boolean
) {

    val keyboardHeightDp = rememberKeyboardHeightDp()

    val screenHeightDp = with(LocalDensity.current) {
        LocalView.current.rootView.height.toDp()
    }

    val editorHeight by animateDpAsState(
        targetValue = if (keyboardHeightDp > 0.dp)
            screenHeightDp - keyboardHeightDp - 175.dp
        else
            screenHeightDp - 300.dp,
        animationSpec = tween(
            durationMillis = 125,
            easing = FastOutSlowInEasing
        ),
        label = "editorHeight"
    )

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val view = LocalView.current

    val rect = remember { Rect() }
    view.getWindowVisibleDisplayFrame(rect)


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> uri?.let { onFileUpload(it) } }
    )
    val copyLabel = stringResource(R.string.copy)
    val clearLabel = stringResource(R.string.clear)
    val uploadFileLabel = stringResource(R.string.upload_file)
    val copiedLabel = stringResource(R.string.copied)
    val enterTextOrPaste = stringResource(R.string.enter_text_or_paste)

    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = Modifier

    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .height(editorHeight)
                .shadow(
                    elevation = 1.dp,
                    shape = RoundedCornerShape(8.dp),
                    clip = false
                )
                .border(0.2.dp, Color.LightGray, shape = RoundedCornerShape(8.dp))
                .background(Color.White, shape = RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxSize()
                    .onFocusChanged {
                        isFocused = it.isFocused
                    },
                textStyle = TextStyle(fontSize = 18.sp, color = Color.Black, textAlign = TextAlign.Start),
                maxLines = Int.MAX_VALUE,
                cursorBrush = SolidColor(Color.Black),
            )

            if (text.isEmpty() && !isFocused) {
                Text(
                    text = enterTextOrPaste,
                    style = TextStyle(fontSize = 18.sp, color = Color.Gray, textAlign = TextAlign.Start),
                )
            }
        }
    }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp)
        ) {

            if (text.isNotEmpty()) {
                IconTextButton(
                    icon = Icons.Default.Close,
                    label = clearLabel,
                    onClick = onClear,
                    modifier = Modifier.weight(1f)
                )
            }

            IconTextButton(
                icon = Icons.Default.ContentCopy,
                label = copyLabel,
                onClick = {
                    clipboardManager.setText(AnnotatedString(text))
                    Toast.makeText(context, copiedLabel, Toast.LENGTH_SHORT).show()
                },
                enabled = text.isNotBlank(),
                modifier = Modifier.weight(1f)
            )

            IconTextButton(
                icon = Icons.Default.Upload,
                label = "PDF, DOCX, TXT",
                onClick = { launcher.launch("*/*") },
                modifier = (
                        if (text.isNotEmpty()) Modifier.weight(1.3f) else Modifier.weight(1f)
                        ),

                horizontalArrangement = Arrangement.Center
            )
        }
    }