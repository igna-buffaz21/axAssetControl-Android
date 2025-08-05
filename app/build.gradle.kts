plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
}

android {
    namespace = "aumax.estandar.axappestandar"
    compileSdk = 34  // Volvemos a 34 temporalmente

    defaultConfig {
        applicationId = "aumax.estandar.axappestandar"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.1"

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
    kotlinOptions {
        jvmTarget = "11"
    }

    // Habilitar Data Binding y View Binding
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Lector
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

    // Retrofit y Gson
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Material y Navigation
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

    // Room - EXACTAMENTE COMO EN EL PROYECTO FUNCIONAL
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.4.3")  // ✅ Mantener esta versión como en el proyecto funcional
    ksp("androidx.room:room-compiler:2.6.1")

    // Lifecycle ViewModels - Para Views tradicionales
    //implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
}