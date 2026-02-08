package com.kaankivancdilli.summary.ui.screens.main

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.ui.component.textadd.TextEditor
import com.kaankivancdilli.summary.ui.utils.upload.handleFileUpload
import com.kaankivancdilli.summary.ui.viewmodel.textadd.TextAddViewModel
import com.kaankivancdilli.summary.utils.admob.AdMobBanner
import com.kaankivancdilli.summary.utils.reusable.CustomTopBar
import com.kaankivancdilli.summary.utils.state.ResultState

@Composable
fun isTablet(): Boolean {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    return screenWidthDp >= 600 // 600dp+ is typically considered tablet
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextAddScreen(navController: NavController, viewModel: TextAddViewModel = hiltViewModel()) {

    val isSubscribed by viewModel.isSubscribed.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    val textFieldValue by viewModel.textState.collectAsState()
    val summaryState by viewModel.summaryState.collectAsState()

    val continueLabel = stringResource(R.string.continue_)
    val extractingFileLabel = stringResource(R.string.extracting_file)
    val errorSummarizing = stringResource(R.string.error_summarizing)

    val lifecycleOwner = LocalLifecycleOwner.current

    val textEditorLabel = stringResource(R.string.text_editor)

    LaunchedEffect(Unit) {
        val actuallySubscribed = viewModel.subscriptionChecker.isUserSubscribed()
        if (actuallySubscribed != isSubscribed) {
            viewModel.setSubscriptionStatus(actuallySubscribed)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                // Save when app goes to background
                viewModel.saveText(viewModel.textState.value)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val navigateToSummary: (String) -> Unit = { text ->
        if (text.isNotBlank()) {
            navController.navigate("summaryScreen/${Uri.encode(text)}")
        }
    }

    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .zIndex(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(extractingFileLabel, color = Color.White)
                }
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.White, // ✅ Prevent background glitches
            topBar = {
                CustomTopBar(title = textEditorLabel)
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // ✅ Handles system bars properly
                    .padding(top = 6.dp, start = 16.dp, end = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {

             //   if (!isSubscribed) {
             //       AdMobBanner()
            //        Spacer(modifier = Modifier.height(6.dp))
           //     } else {
           //         Spacer(modifier = Modifier.height(4.dp))
           //     }


                    TextEditor(
                        text = textFieldValue,
                        onTextChange = viewModel::updateTextState,
                        onClear = { viewModel.clearText() },
                        onFileUpload = { uri ->
                            isLoading = true
                            handleFileUpload(
                                context = context,
                                uri = uri,
                                viewModel = viewModel
                            ) {
                                isLoading = false
                            }
                        },
                        isSubscribed = isSubscribed
                    )
                    Button(
                        onClick = {
                            if (textFieldValue.isNotBlank()) {
                                navController.navigate("summaryScreen/${Uri.encode(textFieldValue)}")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            // .padding(if (isTablet()) 8.dp else 8.dp),
                            .padding(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = MaterialTheme.shapes.large,
                        contentPadding = PaddingValues(
                            horizontal = 8.dp,
                            vertical = if (isTablet()) 20.dp else 8.dp // <- Button thicker on tablet
                        )
                    ) {
                        Text(
                            continueLabel,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = if (isTablet()) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }






    LaunchedEffect(summaryState) {
        if (summaryState is ResultState.Success) {
            isLoading = false
            val summary = (summaryState as ResultState.Success).data
            navController.navigate("summaryScreen/${Uri.encode(summary)}")
            viewModel.resetSummary()
        } else if (summaryState is ResultState.Error) {
            isLoading = false
            Toast.makeText(context, errorSummarizing, Toast.LENGTH_SHORT).show()
            viewModel.resetSummary()
        }
    }

}






@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(viewModel: TextAddViewModel) {

    val textEditorLabel = stringResource(R.string.text_editor)

    Box(
        modifier = Modifier
            .fillMaxWidth()
        // .padding(4.dp) // Optional padding for better appearance
    ) {
        // CenterAlignedTopAppBar with rounded bottom corners
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = textEditorLabel,
                    style = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color.Black
            ),
            modifier = Modifier
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                    clip = false // important to keep the shadow visible
                )
                .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)) // Clip the bottom corners
             //   .border(1.dp, Color.LightGray, shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)) // Border only at the bottom with rounded corners
        )
    }
}















