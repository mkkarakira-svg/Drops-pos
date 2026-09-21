plugins { id("com.android.application") }
android {
    namespace = "com.drops.pos"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.drops.pos.v1"
        minSdk = 23
        targetSdk = 35
        versionCode = 6
        versionName = "1.3.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
