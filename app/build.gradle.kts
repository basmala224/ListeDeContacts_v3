import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// La clé Google Maps est lue dans local.properties (ignoré par Git) ou dans
// la variable d'environnement MAPS_API_KEY. Elle n'est jamais écrite dans le dépôt.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val mapsApiKey: String = (localProps.getProperty("MAPS_API_KEY")
    ?: System.getenv("MAPS_API_KEY") ?: "").trim()
    .takeUnless { it == "VOTRE_CLE_API_GOOGLE_MAPS" } ?: "" // valeur d'exemple = pas de clé

android {
    namespace = "com.example.contactsv3"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.contactsv3"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "3.0"

        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
        buildConfigField("boolean", "MAPS_KEY_CONFIGURED", (mapsApiKey.isNotEmpty()).toString())

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.fragment:fragment-ktx:1.8.2")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.4")
    implementation("com.google.android.gms:play-services-maps:19.0.0")

    testImplementation("junit:junit:4.13.2")
}
