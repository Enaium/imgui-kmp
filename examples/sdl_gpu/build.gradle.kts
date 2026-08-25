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
            mainClass = "cn.enaium.imgui.example.gpu.Main_jvmKt"
        }
    }

    macosArm64 {
        binaries.executable()
    }

    linuxX64 {
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
    // The imgui-kmp c_api wraps ImGui::Shortcut in `imgui_shortcut`. The
    // cinterop stub is emitted unconditionally, but the underlying c_api
    // definition lives in `libimgui.a` and K/N's per-symbol dead-code
    // elimination drops it from libmain.so when the example doesn't call
    // `ImGui.shortcut` directly. Worse, the Android cinterop klib ships
    // with an empty `default/targets/.../included/` (the macOS one does
    // embed libimgui.a), so the symbol isn't reachable through the klib
    // at all. Workaround: link libimgui.a directly and use
    // `-Wl,-u,imgui_shortcut` to force the linker to pull it in. libimgui.a
    // is compiled with `-DANDROID_STL=c++_shared` so it has no static
    // C++ runtime to embed; the matching `libc++_shared.so` is bundled
    // The K/N toolchain ships its own libc++_shared.so under
    // `$KONAN_DATA_DIR/.../sysroot/usr/lib/<triple>/`. Linking that exact file
    // (rather than passing `-lc++_shared` and hoping the sysroot resolves it)
    // makes K/N add it to libmain.so's DT_NEEDED, so the Android loader picks
    // it up at dlopen time. The matching shared lib is what K/N's own runtime
    // was built against, which keeps the basic_string ABI in lock-step with
    // the references inside libmain.so.
    val knanCxxShared = mapOf(
        "androidNativeArm64" to "/Users/enaium/.konan/dependencies/target-toolchain-2-osx-android_ndk/sysroot/usr/lib/aarch64-linux-android/libc++_shared.so",
        "androidNativeArm32" to "/Users/enaium/.konan/dependencies/target-toolchain-2-osx-android_ndk/sysroot/usr/lib/arm-linux-androideabi/libc++_shared.so",
        "androidNativeX64" to "/Users/enaium/.konan/dependencies/target-toolchain-2-osx-android_ndk/sysroot/usr/lib/x86_64-linux-android/libc++_shared.so",
        "androidNativeX86" to "/Users/enaium/.konan/dependencies/target-toolchain-2-osx-android_ndk/sysroot/usr/lib/i686-linux-android/libc++_shared.so",
    )
    androidNativeArm64 {
        binaries.sharedLib("main") {
            konanAndroidLibDir("arm64-v8a")?.let { linkerOpts("-L$it") }
            linkerOpts("-Wl,--allow-multiple-definition")
            linkerOpts("-Wl,-u,imgui_shortcut")
            linkerOpts(knanCxxShared.getValue("androidNativeArm64"))
            rootProject.file("imgui-kmp/build/native/androidNativeArm64/libimgui.a").takeIf { it.exists() }?.let {
                linkerOpts(it.absolutePath)
            }
        }
    }
    androidNativeArm32 {
        binaries.sharedLib("main") {
            konanAndroidLibDir("armeabi-v7a")?.let { linkerOpts("-L$it") }
            linkerOpts("-Wl,--allow-multiple-definition")
            linkerOpts("-Wl,-u,imgui_shortcut")
            linkerOpts(knanCxxShared.getValue("androidNativeArm32"))
            rootProject.file("imgui-kmp/build/native/androidNativeArm32/libimgui.a").takeIf { it.exists() }?.let {
                linkerOpts(it.absolutePath)
            }
        }
    }
    androidNativeX64 {
        binaries.sharedLib("main") {
            konanAndroidLibDir("x86_64")?.let { linkerOpts("-L$it") }
            linkerOpts("-Wl,--allow-multiple-definition")
            linkerOpts("-Wl,-u,imgui_shortcut")
            linkerOpts(knanCxxShared.getValue("androidNativeX64"))
            rootProject.file("imgui-kmp/build/native/androidNativeX64/libimgui.a").takeIf { it.exists() }?.let {
                linkerOpts(it.absolutePath)
            }
        }
    }
    androidNativeX86 {
        binaries.sharedLib("main") {
            konanAndroidLibDir("x86")?.let { linkerOpts("-L$it") }
            linkerOpts("-Wl,--allow-multiple-definition")
            linkerOpts("-Wl,-u,imgui_shortcut")
            linkerOpts(knanCxxShared.getValue("androidNativeX86"))
            rootProject.file("imgui-kmp/build/native/androidNativeX86/libimgui.a").takeIf { it.exists() }?.let {
                linkerOpts(it.absolutePath)
            }
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
        jvmArgs(
            "--enable-native-access=ALL-UNNAMED",
            "-XstartOnFirstThread",
        )
    }
}
