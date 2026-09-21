plugins { id("com.android.application") }
android {
    namespace = "com.drops.pos"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.drops.pos.clean"
        minSdk = 23
        targetSdk = 35
        versionCode = 19
        versionName = "4.0.2"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
