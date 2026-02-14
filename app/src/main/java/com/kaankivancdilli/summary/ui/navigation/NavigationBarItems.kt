package com.kaankivancdilli.summary.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.outlined.Addchart
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector


sealed class NavigationBarItems(val route: String, val icon: ImageVector) {
    object PhotoMain : NavigationBarItems("photoMain", Icons.Outlined.CameraAlt)
    object Anything : NavigationBarItems("anything", Icons.Outlined.Addchart)
    object TextAdd : NavigationBarItems("textAdd", Icons.Default.AddCircle)
    object History : NavigationBarItems("history", Icons.Outlined.History)
    object Settings : NavigationBarItems("settings", Icons.Outlined.Settings)
}