import java.util.Properties
plugins {
    id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.plugin.compose")
}
val lp = Properties().apply { rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) } }
android {
    namespace = "com.daegubus.alert"; compileSdk = 34
    defaultConfig {
        applicationId = "com.daegubus.alert"; minSdk = 26; targetSdk = 34; versionCode = 1; versionName = "1.0"
        buildConfigField("String", "API_KEY", "\"${lp.getProperty("DATA_GO_KR_KEY", "")}\"")
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    val bom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(bom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
