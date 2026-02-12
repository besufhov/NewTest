package com.kaankivancdilli.summary.ui.navigation.controller

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.ui.component.photomain.ocr.handler.PageBoundedOcrHandler
import com.kaankivancdilli.summary.ui.navigation.layout.NavigationBarItems
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
import com.kaankivancdilli.summary.utils.state.texttoimage.TextImageState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class NoRippleInteractionSource : MutableInteractionSource {
    override suspend fun emit(interaction: Interaction) {}
    override val interactions: Flow<Interaction> = emptyFlow()
    override fun tryEmit(interaction: Interaction) = true
}


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun NavigationController(sharedImageViewModel: SharedImageViewModel) {
    val navController = rememberNavController()

    val context = LocalContext.current
    // Create dependencies ONCE at the root level
    val pageBoundedOcrHandler = remember { PageBoundedOcrHandler(context) }
    val textImageState = remember { TextImageState() }


    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->

        val modifier = if (Build.VERSION.SDK_INT >= 35) {
            Modifier.fillMaxSize() // 🚫 don’t apply innerPadding on Android 15+
        } else {
            Modifier.fillMaxSize().padding(innerPadding) // ✅ needed on 8–14
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


            // ✅ FullTextScreen navigation
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


            // ✅ SummaryScreen navigation (restored)
            // ✅ Route for passing OCR text
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

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    // Calculate bottom padding from system navigation bars
    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // List of tabs
    val tabs = listOf(
        NavigationBarItems.PhotoMain,
        NavigationBarItems.TextAdd,
        NavigationBarItems.Anything,
        NavigationBarItems.History,
        NavigationBarItems.Settings
    )

    // Stack Divider above BottomNavigation
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Divider(color = Color.LightGray, thickness = 0.5.dp)
        BottomNavigation(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = bottomPadding)
                .shadow(elevation = 2.dp, shape = RectangleShape, clip = false)
                .zIndex(1f),
            backgroundColor = MaterialTheme.colors.background,
            contentColor = MaterialTheme.colors.onBackground
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            tabs.forEach { tab ->
                val title = when (tab) {
                    is NavigationBarItems.PhotoMain -> stringResource(R.string.camera)
                    is NavigationBarItems.TextAdd -> stringResource(R.string.add)
                    is NavigationBarItems.History -> stringResource(R.string.history)
                    is NavigationBarItems.Anything -> stringResource(R.string.anything)
                    is NavigationBarItems.Settings -> stringResource(R.string.settings)
                }

                BottomNavigationItem(
                    icon = { Icon(imageVector = tab.icon, contentDescription = title) },
                    selected = currentRoute == tab.route,
                    onClick = {
                        if (currentRoute != tab.route) {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    interactionSource = remember { NoRippleInteractionSource() },
                    selectedContentColor = Color.Black,
                    unselectedContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.25f)
                )
            }
        }
    }
}

