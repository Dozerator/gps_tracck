plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

// Читает значение из gradle.properties с дефолтом, чтобы свежий чекаут собирался
// "из коробки" против эмулятора, даже если разработчик не настраивал прод-сервер.
fun gradleProp(name: String, default: String): String =
    (project.findProperty(name) as String?)?.takeIf { it.isNotBlank() } ?: default

android {
    namespace = "com.example.operator"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.operator"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Адрес backend-сервера и параметры TLS — см. gradle.properties.
        buildConfigField("String", "SERVER_IP", "\"${gradleProp("SERVER_IP", "10.0.2.2")}\"")
        buildConfigField("String", "SERVER_URL", "\"${gradleProp("SERVER_URL", "http://10.0.2.2:8002")}\"")
        buildConfigField("String", "WS_URL", "\"${gradleProp("WS_URL", "ws://10.0.2.2:8002/ws/operator")}\"")
        buildConfigField("String", "CERT_PIN", "\"${gradleProp("CERT_PIN", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-service:2.8.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.1")
    implementation("androidx.activity:activity-ktx:1.9.0")

    // Сеть
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Геолокация
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Карта без Google Maps
    implementation("org.osmdroid:osmdroid-android:6.1.20")

    // Корутины
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Офлайн-очередь: локальное хранилище и фоновая синхронизация
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
