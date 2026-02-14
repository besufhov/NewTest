package com.kaankivancdilli.summary.ui

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.google.android.gms.ads.MobileAds
import androidx.core.view.WindowInsetsControllerCompat
import com.kaankivancdilli.summary.ui.navigation.controller.NavigationController
import com.kaankivancdilli.summary.ui.viewmodel.sub.sharedimage.SharedImageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val sharedImageViewModel: SharedImageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.WHITE
        } else {
            window.statusBarColor = Color.TRANSPARENT // fallback for old devices
        }

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        setContent {
            NavigationController(sharedImageViewModel)
        }
    }
}