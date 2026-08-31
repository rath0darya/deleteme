plugins { id("com.android.application") }

android {
    namespace = "com.rath0darya.deleteme"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.rath0darya.deleteme"
        minSdk = 23
        targetSdk = 37
        versionCode = 3
        versionName = "2.1"
    }
}

dependencies {
    implementation("androidx.core:core:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("com.google.android.material:material:1.13.0")
}
