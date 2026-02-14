package com.kaankivancdilli.summary.ui.navigation.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.ui.navigation.NavigationBarItems
import com.kaankivancdilli.summary.ui.component.interaction.NoRippleInteractionSource

@Composable
fun BottomNavigationBar(navController: NavHostController) {

    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val tabs = listOf(
        NavigationBarItems.PhotoMain,
        NavigationBarItems.TextAdd,
        NavigationBarItems.Anything,
        NavigationBarItems.History,
        NavigationBarItems.Settings
    )

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