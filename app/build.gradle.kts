plugins { id("com.android.application") }
android {
    namespace = "com.drops.pos"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.drops.pos.v1"
        minSdk = 23
        targetSdk = 35
        versionCode = 16
        versionName = "3.2.3"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
