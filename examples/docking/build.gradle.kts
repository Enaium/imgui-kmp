plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

// Kotlin/Native's own Android toolchain sysroot (api 26) ships the NDK stub
// libraries (libEGL, libGLESv2, libOpenSLES, libaaudio, ...) that SDL3's
// android drivers reference at link time. Point -L at the per-ABI directory
// so the libmain.so link resolves them without needing an extra NDK install.
fun konanAndroidLibDir(abi: String): String? {
    val konanData = System.getenv("KONAN_DATA_DIR")
        ?: providers.gradleProperty("konan.data.dir").getOrElse("${System.getProperty("user.home")}/.konan")
    val toolchain = File(konanData, "dependencies").listFiles()
        ?.firstOrNull { it.isDirectory && it.name.matches(Regex("target-toolchain-.*-android_ndk")) }
        ?: return null
    val triple = when (abi) {
        "arm64-v8a" -> "aarch64-linux-android"
        "armeabi-v7a" -> "arm-linux-androideabi"
        "x86_64" -> "x86_64-linux-android"
        "x86" -> "i686-linux-android"
        else -> return null
    }
    return "$toolchain/sysroot/usr/lib/$triple/26"
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
        mainRun {
            mainClass = "cn.enaium.imgui.example.docking.Main_jvmKt"
        }
    }

    macosArm64 {
        binaries.executable()
    }

    linuxX64 {
        binaries.executable()
    }

    linuxArm64 {
        binaries.executable()
    }

    mingwX64 {
        binaries.executable()
    }

    // Android native targets build libmain.so with an exported SDL_main entry
    // point; SDLActivity (from the SDL3 AAR) loads and calls it. The SDL3
    // static library is linked in from the sdl-kmp klib; the Kotlin/Native
    // android sysroot provides the NDK system libraries it references. The
    // compiler-rt builtins embedded in the sdl-kmp klib overlap with K/N's
    // bundled libgcc on some ABIs (e.g. __sync_* on armv7); allow duplicates
    // so the first (K/N's) definition wins.
    androidNativeArm64 {
        binaries.sharedLib("main") {
            konanAndroidLibDir("arm64-v8a")?.let { linkerOpts("-L$it") }
            linkerOpts("-Wl,--allow-multiple-definition")
        }
    }
    androidNativeArm32 {
        binaries.sharedLib("main") {
            konanAndroidLibDir("armeabi-v7a")?.let { linkerOpts("-L$it") }
            linkerOpts("-Wl,--allow-multiple-definition")
        }
    }
    androidNativeX64 {
        binaries.sharedLib("main") {
            konanAndroidLibDir("x86_64")?.let { linkerOpts("-L$it") }
            linkerOpts("-Wl,--allow-multiple-definition")
        }
    }
    androidNativeX86 {
        binaries.sharedLib("main") {
            konanAndroidLibDir("x86")?.let { linkerOpts("-L$it") }
            linkerOpts("-Wl,--allow-multiple-definition")
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":examples:common"))
                implementation(libs.sdl.kmp)
                implementation(project(":imgui-kmp"))
            }
        }
    }
}

// SDL3 on macOS (via LWJGL) must run on the first thread, otherwise video
// driver init fails with "No available video device". Mirrors sdl-kmp's own
// examples. --enable-native-access silences the LWJGL JVM warnings.
tasks.withType(JavaExec::class.java).configureEach {
    if (org.gradle.internal.os.OperatingSystem.current().isMacOsX && name == "jvmRun") {
        jvmArgs("--enable-native-access=ALL-UNNAMED", "-XstartOnFirstThread")
    }
}
