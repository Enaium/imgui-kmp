// com.android.library is already on the build classpath (AGP is loaded by
// :imgui-kmp's com.android.kotlin.multiplatform.library plugin), so requesting
// a version here fails the compatibility check. Version-less application
// resolves the classpath copy, which is the same AGP 9.3.1 the app modules use.
plugins {
    id("com.android.library")
}

// The org.libsdl.app package (SDLActivity, SDLSurface, ...) vendored from
// sdl-kmp's examples so every example Android app can share one copy instead
// of shipping its own. The app modules depend on this library and their
// MainActivity extends org.libsdl.app.SDLActivity.
android {
    namespace = "org.libsdl.app"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
}
