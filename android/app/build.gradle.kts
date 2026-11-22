plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "hu.millnotes"
    compileSdk = 34

    defaultConfig {
        applicationId = "hu.millnotes"
        minSdk = 24
        targetSdk = 34
        versionCode = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()
        versionName = "2.1"
    }

    // Saját aláíró kulcs csak akkor, ha a GitHub-titokból megkapta (a repóba nem kerül kulcs).
    // Nélküle az alap debug-kulccsal írja alá, így is telepíthető.
    val ks = file("../millnotes.jks")
    val ksPass = System.getenv("MILLNOTES_KEY_PASS")
    signingConfigs {
        if (ks.exists() && ksPass != null) create("mill") {
            storeFile = ks
            storePassword = ksPass
            keyAlias = "millnotes"
            keyPassword = ksPass
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("mill") ?: signingConfigs.getByName("debug")
        }
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
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("androidx.webkit:webkit:1.11.0")
}
