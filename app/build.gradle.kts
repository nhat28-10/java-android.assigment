plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.groupassignment"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.groupassignment"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // ViewModel and LiveData
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.runtime)

    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // SQLite
    implementation(libs.sqlite)

    // RecyclerView
    implementation(libs.recyclerview)

    // CardView
    implementation(libs.cardview)

    // SwipeRefreshLayout
    implementation(libs.swiperefreshlayout)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}