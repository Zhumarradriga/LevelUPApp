plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)


}

android {
    namespace = "com.example.levelupapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.levelupapp"
        minSdk = 26
        //noinspection OldTargetApi
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    // Основные библиотеки AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

// Зависимости Jetpack Compose
    implementation(platform(libs.androidx.compose.bom)) // Обеспечивает согласованность версий библиотек Compose
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

// Навигация
    implementation(libs.androidx.navigation.compose)

// Хранение данных
    implementation(libs.androidx.datastore.preferences)

// Сеть
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)

// Загрузка изображений
    implementation(libs.coil.compose)

// Работа с датами
    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.runtime)

// Тестирование
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

// Опционально: Material CalendarView
    implementation(libs.material.calendarview)

    implementation (libs.androidx.appcompat)
}