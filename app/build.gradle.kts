import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
}

// Load local.properties for API key configuration
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

android {
    namespace = "com.kairos.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kairos.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
            arg("room.generateKotlin", "true")
        }

        // Production builds never embed provider secrets. The current direct-provider
        // integrations remain available for local debug builds only while a backend
        // gateway is introduced. Empty release values make the features fail closed.
        buildConfigField("String", "AI_API_KEY", "\"\"")
        buildConfigField("String", "OPENROUTER_API_KEY", "\"\"")
        buildConfigField("String", "THERAPIST_API_KEY", "\"\"")
        buildConfigField("String", "TTS_API_KEY", "\"\"")
    }

    sourceSets {
        getByName("androidTest") {
            assets.srcDirs("$projectDir/schemas")
        }
    }

    signingConfigs {
        create("release") {
            // Security: This logic prevents silently falling back to a debug key.
            // It ensures the build fails if the release keystore is missing during a release build.
            val isReleaseTask = project.gradle.startParameter.taskNames.any {
                it.contains("assembleRelease", ignoreCase = true) || it.contains("bundleRelease", ignoreCase = true)
            }

            val keystoreFile = file("Kairos-release.jks")
            val rootKeystoreFile = file("../keystore/Kairos-release.jks")
            val resolvedStoreFile = when {
                keystoreFile.exists() -> keystoreFile
                rootKeystoreFile.exists() -> rootKeystoreFile
                else -> null
            }

            if (isReleaseTask && resolvedStoreFile == null) {
                throw org.gradle.api.GradleException("Release keystore file not found. Could not find 'Kairos-release.jks' in 'app/' or 'keystore/'. Place the keystore file in the correct location or configure the path in 'app/build.gradle.kts'.")
            }

            storeFile = resolvedStoreFile
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")

            // Fail-secure: Prevent release builds with missing keystore
            val isReleaseTask = project.gradle.startParameter.taskNames.any {
                it.contains("release", ignoreCase = true) && (it.contains("assemble") || it.contains("bundle"))
            }
            if (isReleaseTask && storeFile == null) {
                throw GradleException(
                    "Release keystore not found. " +
                            "Ensure 'prody-release.jks' is present in 'app/' or 'keystore/' directories, " +
                            "and KEYSTORE_PASSWORD, KEY_ALIAS, and KEY_PASSWORD env vars are set."
                )
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            // Local-only credentials for development. Never promoted to release.
            buildConfigField(
                "String",
                "AI_API_KEY",
                "\"${localProperties.getProperty("AI_API_KEY", "").trim()}\""
            )
            buildConfigField(
                "String",
                "OPENROUTER_API_KEY",
                "\"${localProperties.getProperty("OPENROUTER_API_KEY", "").trim()}\""
            )
            buildConfigField(
                "String",
                "THERAPIST_API_KEY",
                "\"${localProperties.getProperty("THERAPIST_API_KEY", "").trim()}\""
            )
            buildConfigField(
                "String",
                "TTS_API_KEY",
                "\"${localProperties.getProperty("TTS_API_KEY", "").trim()}\""
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
        // buildConfig is enabled via gradle.properties: android.defaults.buildfeatures.buildconfig=true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/versions/**"
        }
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // Material Icons - Core and Extended for full icon coverage
    implementation(libs.androidx.material.icons.core)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.material)
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

// Security - Encryption & Biometrics
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.biometric)
    implementation(libs.sqlcipher.android)
    implementation(libs.sqlite.ktx)

    // Network Security
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Splash Screen
    implementation(libs.androidx.splash.screen)

    // Accompanist
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.permissions)

    // Coil
    implementation(libs.coil.compose)

    // Gemini AI
    implementation(libs.generativeai)

    // Firebase Auth
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Glance (App Widgets)
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)

    // Core Library Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.3")

    // Unit Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.arch.core.testing)

    // Android UI Testing
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.mockk.android)

    // Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Networking
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)

    // Gson
    implementation(libs.gson)

    // MCP Diagnostic Server (Debug only)
    // debugImplementation(libs.mcp.kotlin.sdk)
    // debugImplementation(libs.ktor.server.core)
    // debugImplementation(libs.ktor.server.cio)
    // debugImplementation(libs.ktor.server.sse)
}
