package com.kaankivancdilli.summary.ui.component.photomain.image

import android.graphics.Bitmap
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.*
import com.kaankivancdilli.summary.ui.component.reusable.dismiss.VerticalSwipeToDismiss
import kotlinx.coroutines.launch

@Composable
fun ImagePreviewRow(
    capturedImages: List<Triple<String, Bitmap, String>>,
    globalCalculatedCameraHeight: Dp,
    selectedImageId: String,
    onDismiss: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val initialPageIndex = capturedImages.indexOfFirst { it.first == selectedImageId }

    if (capturedImages.isNotEmpty()) {
        val pagerState = rememberPagerState(
            initialPage = initialPageIndex.coerceAtLeast(0),
            initialPageOffsetFraction = 0F,
            pageCount = { capturedImages.size },
        )
        Box(Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(globalCalculatedCameraHeight)
            ) { page ->

                val (imageId, imageBitmap, _) = capturedImages[page]

                VerticalSwipeToDismiss(
                    onSwipeAttempt = { onDismiss(imageId) },
                    animateBack = true
                ) {
                    Image(
                        bitmap = imageBitmap.asImageBitmap(),
                        contentDescription = "Captured Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            if (capturedImages.size > 1) {

                if (pagerState.currentPage > 0) {
                    IconButton(
                        onClick = {
                            if (pagerState.currentPage > 0) {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous", tint = Color.White)
                    }
                }

                if (pagerState.currentPage < capturedImages.size - 1) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val next = (pagerState.currentPage + 1) % capturedImages.size
                                pagerState.animateScrollToPage(next)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", tint = Color.White)
                    }
                }
            }

            IconButton(
                onClick = { onDismiss(capturedImages[pagerState.currentPage].first) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White)
            }
        }
    }
}