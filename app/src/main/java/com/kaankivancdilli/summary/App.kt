package com.kaankivancdilli.summary

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Required for pdfbox-android to load fonts/resources properly
        PDFBoxResourceLoader.init(applicationContext)
    }
}