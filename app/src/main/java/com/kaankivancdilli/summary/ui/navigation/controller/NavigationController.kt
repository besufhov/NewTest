package com.kaankivancdilli.summary.ui.navigation.controller

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kaankivancdilli.summary.core.domain.handler.photomain.PageBoundedOcrHandler
import com.kaankivancdilli.summary.ui.navigation.layout.BottomNavigationBar
import com.kaankivancdilli.summary.ui.navigation.NavigationBarItems
import com.kaankivancdilli.summary.ui.viewmodel.sub.sharedimage.SharedImageViewModel
import com.kaankivancdilli.summary.ui.screens.main.anything.screen.AnythingScreen
import com.kaankivancdilli.summary.ui.screens.main.history.HistoryScreen
import com.kaankivancdilli.summary.ui.screens.main.textadd.TextAddScreen
import com.kaankivancdilli.summary.ui.screens.sub.summary.screen.SummaryScreen
import com.kaankivancdilli.summary.ui.screens.main.photomain.PhotoMainScreen
import com.kaankivancdilli.summary.ui.screens.main.settings.SettingsScreen
import com.kaankivancdilli.summary.ui.screens.sub.history.screen.FullAnythingScreen
import com.kaankivancdilli.summary.ui.screens.sub.fulltext.FullTextScreen
import com.kaankivancdilli.summary.ui.viewmodel.main.photomain.OcrViewModel
import com.kaankivancdilli.summary.ui.state.texttoimage.TextImageState

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun NavigationController(sharedImageViewModel: SharedImageViewModel) {
    val navController = rememberNavController()

    val context = LocalContext.current
    val pageBoundedOcrHandler = remember { PageBoundedOcrHandler(context) }
    val textImageState = remember { TextImageState() }


    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->

        val modifier = if (Build.VERSION.SDK_INT >= 35) {
            Modifier.fillMaxSize()
        } else {
            Modifier.fillMaxSize().padding(innerPadding)
        }

        NavHost(
            navController = navController,
            startDestination = NavigationBarItems.Anything.route,
            modifier = modifier

        ) {
            composable(NavigationBarItems.PhotoMain.route) { backStackEntry ->
                val ocrViewModel: OcrViewModel = hiltViewModel(backStackEntry)

                PhotoMainScreen(
                    navController = navController,
                    pageBoundedOcrHandler = pageBoundedOcrHandler,
                    textImageState = textImageState,
                    sharedImageViewModel = sharedImageViewModel,
                    ocrViewModel = ocrViewModel
                )
            }

            composable(NavigationBarItems.TextAdd.route) { TextAddScreen(navController) }
            composable(NavigationBarItems.History.route) { HistoryScreen(navController) }
            composable(NavigationBarItems.Anything.route) { AnythingScreen() }
            composable(NavigationBarItems.Settings.route) { SettingsScreen() }

            composable(
                route = "full_text/{messageId}",
                arguments = listOf(navArgument("messageId") { type = NavType.IntType })
            ) { backStackEntry ->
                val messageId = backStackEntry.arguments?.getInt("messageId") ?: -1
                FullTextScreen(messageId, navController)
            }

            composable(
                "full_anything/{messageId}/{section}",
                arguments = listOf(
                    navArgument("messageId") { type = NavType.IntType },
                    navArgument("section") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val messageId = backStackEntry.arguments?.getInt("messageId") ?: return@composable
                val section = backStackEntry.arguments?.getString("section") ?: ""
                FullAnythingScreen("", messageId, initialSection = section, navController)
            }

            composable(
                route = "summaryScreen/{text}",
                arguments = listOf(navArgument("text") { type = NavType.StringType })
            ) { backStackEntry ->
                val text = backStackEntry.arguments?.getString("text") ?: ""

                SummaryScreen(
                    text = text,
                    messageId = null,
                    section = "",
                    sharedImageViewModel = sharedImageViewModel, // ✅
                    onBack = {
                        navController.popBackStack() // ✅ Ensure screen is fully removed
                    }
                )
            }

            composable(
                route = "summaryScreen/{messageId}/{text}/{section}",
                arguments = listOf(
                    navArgument("messageId") { type = NavType.IntType },
                    navArgument("text") { type = NavType.StringType },
                    navArgument("section") { type = NavType.StringType }

                )
            ) { backStackEntry ->
                val messageId = backStackEntry.arguments?.getInt("messageId")
                val text = backStackEntry.arguments?.getString("text") ?: ""
                val section = backStackEntry.arguments?.getString("section") ?: ""

                SummaryScreen(
                    text = text,
                    messageId = messageId,
                    section = section,
                    sharedImageViewModel = sharedImageViewModel, // ✅
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}