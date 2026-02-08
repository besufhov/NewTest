package com.kaankivancdilli.summary.ui.screens.sub

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kaankivancdilli.summary.ui.viewmodel.TextHistoryViewModel

@Composable
fun FullTextScreen(
    messageId: Int,
    navController: NavController,
    viewModel: TextHistoryViewModel = hiltViewModel()
) {
    val sendTexts by viewModel.saveTexts.collectAsState()
    val message = sendTexts.find { it.id == messageId }
    val scrollState = rememberScrollState()

    var text by remember { mutableStateOf("Loading...") } // ✅ Start with loading state

    // ✅ Update text when sendTexts updates
    LaunchedEffect(sendTexts) {
        text = message?.summarize ?: "Message not found"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Full Text") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                textStyle = TextStyle(fontSize = 18.sp),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent
                )
            )
        }
    }
}
