plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("app.cash.sqldelight")
}

android {
    namespace = "com.mensinator.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mensinator.app"
        minSdk = 30
        targetSdk = 34
        versionCode = 12
        versionName = "1.8.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }
}

dependencies {

    // Versions
    val sqlDelightVersion = "2.0.2"
    val koinVersion = "4.0.0-RC1"

    // AndroidX Core Libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)

    // AndroidX Compose Libraries
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.window)

    // AndroidX Work Libraries
    implementation(libs.androidx.work.runtime.ktx)

    //SQLDelight
    implementation("app.cash.sqldelight:android-driver:$sqlDelightVersion")
    implementation("app.cash.sqldelight:coroutines-extensions-jvm:$sqlDelightVersion")
    implementation("app.cash.sqldelight:primitive-adapters:$sqlDelightVersion")

    // Koin for Android
    implementation(platform("io.insert-koin:koin-bom:$koinVersion"))
    implementation("io.insert-koin:koin-core")
    implementation (libs.koin.android)
    implementation("io.insert-koin:koin-androidx-compose")

    // Testing
    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

sqldelight {
    databases {
        create("MensinatorDB") {
            packageName.set("com.mensinator.app.database")
        }
    }
}