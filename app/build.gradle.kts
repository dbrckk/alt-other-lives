plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.alt.otherlives"
    compileSdk = 35

    val releaseVersionCode = System.getenv("ALT_VERSION_CODE")
        ?.toIntOrNull()
        ?.takeIf { it > 0 }
        ?: 1
    val releaseVersionName = System.getenv("ALT_VERSION_NAME")
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: "0.1.0"

    defaultConfig {
        applicationId = "com.alt.otherlives"
        minSdk = 26
        targetSdk = 35
        versionCode = releaseVersionCode
        versionName = releaseVersionName
    }

    val releaseKeystorePath = System.getenv("ALT_KEYSTORE_PATH")
    val releaseKeystorePassword = System.getenv("ALT_KEYSTORE_PASSWORD")
    val releaseKeyAlias = System.getenv("ALT_KEY_ALIAS")
    val releaseKeyPassword = System.getenv("ALT_KEY_PASSWORD")
    val hasReleaseSigning =
        !releaseKeystorePath.isNullOrBlank() &&
            !releaseKeystorePassword.isNullOrBlank() &&
            !releaseKeyAlias.isNullOrBlank() &&
            !releaseKeyPassword.isNullOrBlank()

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(requireNotNull(releaseKeystorePath))
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        getByName("release") {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.media3:media3-transformer:1.6.1")
    implementation("androidx.media3:media3-effect:1.6.1")
    implementation("androidx.media3:media3-common:1.6.1")
    implementation("io.coil-kt.coil3:coil-compose:3.0.4")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
