import com.android.build.api.dsl.AaptOptions
import com.android.build.api.dsl.AndroidResources

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
   // id("androidx.room")
  //  kotlin("kapt")

}

android {
    namespace = "com.kaankivancdilli.summary"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kaankivancdilli.summary"
        minSdk = 26
        targetSdk = 35
        versionCode = 28
        versionName = "1.2.5"

        testInstrumentationRunner = "androidx.kaankivancdilli.summary.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
          //  shrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

  //  androidResources {
  //      noCompress += "traineddata"
  //  }


    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            )
        }
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14" // Ensure correct version
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.room.ktx)
    implementation(libs.material)
    implementation(libs.androidx.camera.view)
    implementation(libs.play.services.mlkit.text.recognition.common)
    implementation(libs.vision.common)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.play.services.mlkit.text.recognition)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.tools.core)
    implementation(libs.core)
    implementation(libs.androidx.storage)
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.review.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.okhttp)
    implementation(libs.hilt.android)
    implementation(libs.androidx.material)
    implementation(libs.ui)
    ksp(libs.hilt.compiler)
    implementation(libs.gson)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler.v271)
    // Compose Tooling (for preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation("androidx.concurrent:concurrent-futures:1.3.0")
    // Navigation with Hilt for Compose
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle.v142)
    implementation(libs.androidx.camera.view.v142)
    implementation(libs.text.recognition)
    implementation(libs.text.recognition.japanese)
    implementation(libs.text.recognition.chinese)
    implementation(libs.text.recognition.korean)
    implementation(libs.text.recognition.devanagari)
    implementation("com.google.guava:guava:33.0.0-jre")
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.coil.compose) // Latest version
    implementation (libs.core.v1480)
    implementation (libs.androidx.datastore.preferences)
    implementation (libs.billing.ktx)
    implementation("org.apache.poi:poi-ooxml:5.2.3")
  //  implementation("org.apache.poi:poi-ooxml:3.17")
   // implementation ("org.docx4j:docx4j-android:8.3.5")

    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
  //  implementation("org.apache.pdfbox:pdfbox:2.0.27")
  //  implementation("org.apache.tika:tika-core:2.9.0")
  //  implementation("org.apache.tika:tika-parsers-standard-package:2.9.0")
 //   implementation("org.apache.tika:tika-parsers:2.9.0")
  //  implementation("com.github.librepdf:openpdf:1.3.30")
    implementation(libs.language.id)
    implementation(libs.play.services.ads)

    implementation ("cz.adaptech.tesseract4android:tesseract4android:4.9.0")


}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}