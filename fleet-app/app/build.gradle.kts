plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.example.fleetapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fleetapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

   kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")

    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}