package com.kaankivancdilli.summary.ui.screens.main.photomain

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaankivancdilli.summary.ui.viewmodel.main.photomain.OcrViewModel
import com.kaankivancdilli.summary.core.domain.handler.photomain.PageBoundedOcrHandler
import com.kaankivancdilli.summary.ui.component.photomain.text.preview.PreviewTextView
import androidx.compose.ui.platform.*
import com.kaankivancdilli.summary.ui.component.photomain.image.ImagePreviewRow
import java.util.UUID
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kaankivancdilli.summary.data.repository.main.photomain.OcrRepository
import com.kaankivancdilli.summary.ui.component.photomain.detection.PageDetectionOverlay
import com.kaankivancdilli.summary.ui.state.texttoimage.TextImageState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import coil.compose.rememberAsyncImagePainter
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.core.controller.photomain.OcrCameraController
import com.kaankivancdilli.summary.core.processor.OcrProcessor
import com.kaankivancdilli.summary.ui.viewmodel.sub.sharedimage.SharedImageViewModel

@Composable
fun PhotoMainScreen(
    navController: NavController,
    pageBoundedOcrHandler: PageBoundedOcrHandler,
    textImageState: TextImageState,
    sharedImageViewModel: SharedImageViewModel,
    ocrViewModel: OcrViewModel
) {

    var showPreviewRow by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current
    val capturedImages by textImageState.capturedImages.collectAsState()
    val ocrRepository = remember { OcrRepository(context) }
    val cameraController = remember { OcrCameraController() }

    val ocrProcessor = remember {
        OcrProcessor(ocrRepository, pageBoundedOcrHandler)
    }

    val viewModelStoreOwner = remember(navController) {
        navController.getViewModelStoreOwner(navController.graph.id)
    }

    val ocrViewModel: OcrViewModel = viewModel(
        viewModelStoreOwner,
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OcrViewModel(ocrRepository, pageBoundedOcrHandler, textImageState, ocrProcessor, cameraController) as T
            }
        }
    )
    val capturedImage by ocrViewModel.capturedImage.collectAsState(initial = null)
    val recognizedTexts by textImageState.recognizedTexts.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) println("✅ Camera permission granted")
            else println("❌ Camera permission denied")
        }
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri>? ->
        uris?.forEach { uri ->
            ocrViewModel.processImage(context, uri)
        }
    }

    val imagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                imagePickerLauncher.launch("image/*")
            } else {
                println("❌ Storage permission denied")
            }
        }
    )

    val deleteImageTextLabel = stringResource(R.string.delete_image)
    val deleteImageTextAskLabel = stringResource(R.string.delete_ask)
    val yesLabel = stringResource(R.string.yes)
    val noLabel = stringResource(R.string.no)

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(capturedImage) {
        capturedImage?.let { image ->
            val latestRecognizedText = recognizedTexts.lastOrNull() ?: ""
            val updatedImages = capturedImages + Triple(
                UUID.randomUUID().toString(),
                image,
                latestRecognizedText
            )
            textImageState.updateCapturedImages(updatedImages)
        }
    }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Box(modifier = Modifier.fillMaxSize()) {
        var cameraHeight by remember { mutableStateOf(0.dp) }
        var previewView: PreviewView? by remember { mutableStateOf(null) }


        AndroidView(
            factory = {
                ocrViewModel.initializeCamera(context, lifecycleOwner, null).apply {
                    previewView = this
                    viewTreeObserver.addOnGlobalLayoutListener {
                        if (height > 0) {
                            post {
                                val cameraAspectRatio = 3f / 4f
                                val calculatedCameraHeight = (width / cameraAspectRatio).toInt()

                                if (cameraHeight == 0.dp) {
                                    cameraHeight = with(density) { calculatedCameraHeight.toDp() }
                                    println("Camera Height: $cameraHeight")
                                }
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cameraHeight)
                .align(Alignment.TopCenter),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier.offset(y = -50.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(6.dp, Color.White, CircleShape)
                        .clickable { ocrViewModel.triggerFocusAndResume() }
                )
            }

            IconButton(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        imagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    } else {
                        imagePickerLauncher.launch("image/*")
                    }
                },
                modifier = Modifier
                    .size(50.dp)
                    .offset(x = 75.dp, y = -50.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoLibrary,
                    contentDescription = "Pick Image",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        if (cameraHeight > 0.dp) {
            var isExpanded by remember { mutableStateOf(false) }
            val baseHeight = screenHeight - cameraHeight - if (Build.VERSION.SDK_INT < 35) 56.dp else 0.dp
            val expandedHeight = screenHeight - baseHeight + 56.dp
            val animatedHeight by animateDpAsState(
                targetValue = if (isExpanded) expandedHeight else baseHeight,
                animationSpec = tween(durationMillis = 300),
                label = "HeightAnimation"
            )

            PageDetectionOverlay(pageBoundedOcrHandler)
            var showDeleteConfirmation by remember { mutableStateOf(false) }
            var deleteIndex by remember { mutableStateOf(-1) }
            if (showPreviewRow && capturedImages.isNotEmpty()) {
                ImagePreviewRow(
                    capturedImages = capturedImages,
                    globalCalculatedCameraHeight = cameraHeight,
                    onDismiss = { imageId ->
                        deleteIndex = capturedImages.indexOfFirst { it.first == imageId }
                        showDeleteConfirmation = true
                    },
                    selectedImageId = 0.toString()
                )
            }

            if (showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmation = false },
                    containerColor = Color.LightGray,
                    title = { Text(text = deleteImageTextLabel, color = Color.Black) },
                    text = { Text(text = deleteImageTextAskLabel, color = Color.Black) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val updatedImages = if (deleteIndex == -1) {
                                    emptyList()
                                } else {
                                    capturedImages.toMutableList().apply { removeAt(deleteIndex) }
                                }
                                textImageState.updateCapturedImages(updatedImages)
                                if (updatedImages.isEmpty()) showPreviewRow = false

                                showDeleteConfirmation = false
                                deleteIndex = -1
                            },
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = Color.Black,
                                contentColor = Color.White
                            )
                        ) {
                            Text(yesLabel)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDeleteConfirmation = false
                                deleteIndex = -1
                            },
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = Color.Black,
                                contentColor = Color.White
                            )
                        ) {
                            Text(noLabel)
                        }
                    }
                )
            }

            PreviewTextView(
                recognizedTexts = recognizedTexts,
                onToggleExpand = { isExpanded = !isExpanded },
                onDeleteImage = { index ->
                    if (capturedImages.isNotEmpty()) {
                        deleteIndex = index
                        showDeleteConfirmation = true
                    }
                },
                onDone = {
                    if (recognizedTexts.isNotEmpty()) {
                        val merged = recognizedTexts.joinToString(separator = "\n\n")
                        Log.d("PhotoMainScreen", "Captured images for shared: ${capturedImages.size}")
                        if (capturedImages.isNotEmpty()) {
                            sharedImageViewModel.setImageData(capturedImages)
                            navController.navigate("summaryScreen/${Uri.encode(merged)}")
                        } else {
                            Log.e("Navigation", "No images captured, not navigating")
                        }
                        textImageState.updateCapturedImages(emptyList())
                    } else {
                        Log.e("Navigation", "Empty text, not navigating")
                    }
                },
                navController = navController,
                animatedHeight = animatedHeight,
                isExpanded = isExpanded,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        if (capturedImages.isNotEmpty()) {
            val latestImage = capturedImages.last().second

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cameraHeight)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(3.dp, Color.Black, RoundedCornerShape(4.dp))
                        .clickable { showPreviewRow = !showPreviewRow }
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(latestImage),
                        contentDescription = "Latest Captured Image",
                        modifier = Modifier.size(60.dp, 90.dp),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .background(Color.Black, CircleShape)
                            .size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${capturedImages.size}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}