package com.kaankivancdilli.summary.ui.component.textadd

import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.ui.component.textadd.layout.IconTextButton
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.Dp

@Composable
fun TextEditor(
    text: String,
    onTextChange: (String) -> Unit,
    onClear: () -> Unit,
    onFileUpload: (Uri) -> Unit,
    isSubscribed: Boolean
) {

    val keyboardHeightDp = rememberKeyboardHeightDp()
    //   val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val screenHeightDp = with(LocalDensity.current) {
        LocalView.current.rootView.height.toDp()
    }

    // val adHeight = if (isSubscribed) 0.dp else 50.dp
    val keyboardHeight = if (isSubscribed) 180.dp else 230.dp

    val screenSize = if (isSubscribed) 0.65f else 0.45f

  //  val adHeight = if (isSubscribed) 0.dp else 220.dp



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
     //   .padding(vertical = 8.dp)

    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .height(editorHeight)
                .shadow(
                    elevation = 1.dp,
                    shape = RoundedCornerShape(8.dp),
                    clip = false // important to keep the shadow visible
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

            // Show placeholder only if the text is empty and the field is not focused
            if (text.isEmpty() && !isFocused) {
                Text(
                    text = enterTextOrPaste,
                    style = TextStyle(fontSize = 18.sp, color = Color.Gray, textAlign = TextAlign.Start),
                  //  modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
                )
            }
        }
    }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp) // invisible 2 dp
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

enum class Keyboard {
    Opened, Closed
}

@Composable
fun keyboardAsState(): State<Keyboard> {
    val keyboardState = remember { mutableStateOf(Keyboard.Closed) }
    val view = LocalView.current
    DisposableEffect(view) {
        val onGlobalListener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            keyboardState.value = if (keypadHeight > screenHeight * 0.15) {
                Keyboard.Opened
            } else {
                Keyboard.Closed
            }
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(onGlobalListener)

        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalListener)
        }
    }

    return keyboardState
}

@Composable
fun rememberKeyboardHeightDp(): Dp {
    val view = LocalView.current
    val density = LocalDensity.current
    val keyboardHeight = remember { mutableStateOf(0) }

    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val r = Rect()
            view.getWindowVisibleDisplayFrame(r)
            val screenHeight = view.rootView.height
            val heightDiff = screenHeight - r.height()

            // Only consider it keyboard if height diff is significant
            if (heightDiff > screenHeight * 0.15) {
                keyboardHeight.value = heightDiff
            } else {
                keyboardHeight.value = 0
            }
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    return with(density) { keyboardHeight.value.toDp() }
}

