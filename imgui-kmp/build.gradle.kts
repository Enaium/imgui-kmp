import java.io.File
import java.util.Properties
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.maven.publish)
}

group = rootProject.group
version = rootProject.version

val jniDir = rootProject.projectDir.resolve("jni")

val hostOs = OperatingSystem.current()
val hostArch = System.getProperty("os.arch").lowercase()

// Whether the current host can cross-compile the C++ library for the given
// Kotlin/Native target. Apple targets build from macOS via Xcode; linuxX64 and
// mingwX64 build on Linux hosts (mingwX64 via the MinGW cross-compiler, which
// matches Kotlin/Native's own MinGW linker).
fun hasMingwCrossToolchain(): Boolean {
    val name = "x86_64-w64-mingw32-gcc"
    return System.getenv("PATH")?.split(File.pathSeparator).orEmpty().any { dir ->
        val f = File(dir, name)
        f.isFile && f.canExecute()
    }
}

// linuxArm64 builds on a native aarch64 host with the default compiler; an
// x86_64 host needs the aarch64-linux-gnu cross-compiler (matches
// Kotlin/Native's own linker targets for linuxArm64).
fun hasAarch64CrossToolchain(): Boolean {
    val name = "aarch64-linux-gnu-gcc"
    return System.getenv("PATH")?.split(File.pathSeparator).orEmpty().any { dir ->
        val f = File(dir, name)
        f.isFile && f.canExecute()
    }
}

fun isAarch64Host(): Boolean = hostArch == "aarch64" || hostArch == "arm64"

fun canBuildNativeTarget(targetName: String): Boolean {
    return when {
        hostOs.isMacOsX && targetName.startsWith("macos") -> true
        hostOs.isMacOsX && targetName.startsWith("ios") -> true
        hostOs.isMacOsX && targetName.startsWith("tvos") -> true
        hostOs.isMacOsX && targetName.startsWith("watchos") -> true
        hostOs.isLinux && targetName == "linuxX64" -> true
        hostOs.isLinux && targetName == "linuxArm64" -> isAarch64Host() || hasAarch64CrossToolchain()
        hostOs.isLinux && targetName == "mingwX64" && hasMingwCrossToolchain() -> true
        // mingwX64 also cross-compiles from macOS with the Homebrew MinGW-w64
        // toolchain (`brew install mingw-w64`), same as the Linux branch below.
        hostOs.isMacOsX && targetName == "mingwX64" && hasMingwCrossToolchain() -> true
        // androidNative targets cross-compile the C++ library with the Android
        // NDK toolchain (MSVC hosts are excluded: the default CMake generator
        // cannot drive the NDK toolchain).
        targetName.startsWith("androidNative") && canBuildAndroidJni() && androidNdkToolchain?.isFile == true -> true
        else -> false
    }
}

// Resolves cmake from PATH plus the common install locations (IDEs often
// start Gradle without the Homebrew paths on PATH).
fun resolveCmakeExecutable(): String {
    val exeName = if (OperatingSystem.current().isWindows) "cmake.exe" else "cmake"

    System.getenv("PATH")?.split(File.pathSeparator).orEmpty().forEach { dir ->
        val candidate = File(dir, exeName)
        if (candidate.isFile && candidate.canExecute()) return candidate.absolutePath
    }

    val extraPaths = listOf(
        "/opt/homebrew/bin",
        "/usr/local/bin",
        "/usr/bin",
        "/opt/local/bin",
    )
    extraPaths.forEach { dir ->
        val candidate = File(dir, exeName)
        if (candidate.isFile && candidate.canExecute()) return candidate.absolutePath
    }

    return exeName
}

val cmakeExecutable: String by lazy { resolveCmakeExecutable() }

// ==================== Android NDK resolution ====================
// Resolved before the `kotlin {}` block so cinterop's `canBuild` checks (and
// the -staticLibrary embedding for androidNative targets) see the toolchain.
val androidApiLevel = 21

val pinnedAndroidNdkVersion = "27.0.12077973"

// Windows hosts default CMake to the MSVC generator which is incompatible
// with the NDK toolchain; the Android JNI libs are built on macOS/Linux.
fun canBuildAndroidJni(): Boolean = !OperatingSystem.current().isWindows

fun resolveAndroidSdkDir(): java.io.File? {
    listOf("ANDROID_HOME", "ANDROID_SDK_ROOT").forEach { key ->
        System.getenv(key)?.takeIf { it.isNotBlank() }?.let {
            val f = file(it)
            if (f.isDirectory) return f
        }
    }
    val localProps = rootProject.file("local.properties")
    if (localProps.isFile) {
        val props = Properties().apply { localProps.inputStream().use { load(it) } }
        props.getProperty("sdk.dir")?.takeIf { it.isNotBlank() }?.let {
            val f = file(it)
            if (f.isDirectory) return f
        }
    }
    return null
}

fun resolveAndroidNdkDir(): java.io.File? {
    listOf("ANDROID_NDK_HOME", "ANDROID_NDK_ROOT", "NDK_HOME").forEach { key ->
        System.getenv(key)?.takeIf { it.isNotBlank() }?.let {
            val f = file(it)
            if (f.isDirectory) return f
        }
    }
    val sdk = resolveAndroidSdkDir() ?: return null
    val ndkParent = sdk.resolve("ndk")
    if (!ndkParent.isDirectory) return null
    val pinned = ndkParent.resolve(pinnedAndroidNdkVersion)
    if (pinned.isDirectory) return pinned
    return ndkParent.listFiles()?.filter { it.isDirectory }?.maxByOrNull { it.name }
}

val resolvedAndroidNdk = resolveAndroidNdkDir()
val androidNdkToolchain = resolvedAndroidNdk?.resolve("build/cmake/android.toolchain.cmake")

kotlin {
    // ==================== JVM ====================
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    // ==================== wasm (JS) ====================
    wasmJs {
        // Enables the JS plugin so consumers can bundle the wasm module.
        nodejs()
    }

    // ==================== Android ====================
    android {
        namespace = "cn.enaium.imgui"
        compileSdk = 37
        minSdk = 21

        withHostTest {}

        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                }
            }
        }
    }

    // ==================== Native ====================
    macosArm64()
    macosX64()

    linuxX64()

    linuxArm64()

    mingwX64()

    // Android native targets (the AAR's JVM API is separate; these are used
    // by Kotlin/Native Android apps, e.g. via sdl-kmp's libmain.so).
    androidNativeArm64()
    androidNativeArm32()
    androidNativeX64()
    androidNativeX86()

    iosArm64()
    iosX64()
    iosSimulatorArm64()

    tvosArm64()
    tvosSimulatorArm64()

    watchosArm64()
    watchosSimulatorArm64()
    watchosDeviceArm64()

    // ==================== cinterop for all native targets ====================
    targets.withType<KotlinNativeTarget> {
        val targetName = this.name
        val canBuild = canBuildNativeTarget(targetName)

        compilations.getByName("main") {
            cinterops {
                create("imgui") {
                    defFile(project.file("src/nativeInterop/cinterop/imgui.def"))
                    includeDirs(
                        project.file("src/nativeInterop/cinterop"),
                        rootProject.file("jni/c_api"),
                        rootProject.file("includes/imgui"),
                        rootProject.file("includes/implot"),
                        rootProject.file("includes/imgui-node-editor"),
                        rootProject.file("includes/ImGuiFileDialog"),
                        rootProject.file("includes/imgui_club/imgui_memory_editor"),
                        rootProject.file("includes/imgui_club"),
                    )
                    if (canBuild) {
                        // Embed the per-target static library into the produced
                        // cinterop klib. Targets that can't be built on this host
                        // still get bindings (for klib publishing); the static
                        // library is built and embedded when building on the
                        // matching host.
                        val outputDir = layout.buildDirectory.dir("native/$targetName").get().asFile
                        extraOpts(
                            "-libraryPath", outputDir.absolutePath,
                            "-staticLibrary", "libimgui.a",
                        )
                    }
                }
            }
        }
    }

    // ==================== Source sets ====================
    // Ensure the default hierarchy template (nativeMain, appleMain, macosMain,
    // iosMain, tvosMain, ...) is materialized before we create custom source
    // sets, so that native targets keep their shared intermediate sets.
    applyDefaultHierarchyTemplate()
    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }

        // SDL backends (cn.enaium.imgui.backends.sdl). sdl-kmp publishes for
        // every target imgui-kmp declares except the watchOS family, so the
        // backends live in these intermediate source sets instead of commonMain.
        // Two variants are needed because the native one must sit on top of
        // nativeMain (to see the expect/actual declarations) while the JVM one
        // sits on top of commonMain. Both share the src/sdlMain/kotlin sources.
        // The flat bridge (expect object Jni + shared adapters, ImGuiJni.kt) is
        // shared by the JVM and wasm targets; each supplies an `actual object
        // Jni` backed by its own FFI (JNI, or the Emscripten module on wasm).
        val jniMain by creating {
            dependsOn(getByName("commonMain"))
        }

        val sdlMain by creating {
            dependsOn(getByName("commonMain"))
            dependencies {
                implementation(libs.sdl.kmp)
            }
        }
        val sdlNativeMain by creating {
            dependsOn(getByName("nativeMain"))
            kotlin.srcDir("src/sdlMain/kotlin")
            dependencies {
                implementation(libs.sdl.kmp)
            }
        }

        // All targets sdl-kmp supports: desktop, mobile Apple and Android.
        getByName("jvmMain").dependsOn(sdlMain)
        getByName("jvmMain").dependsOn(jniMain)
        getByName("wasmJsMain").dependsOn(jniMain)

        // SDL backends on top of the wasm actuals (sdlMain already carries the
        // src/sdlMain/kotlin sources and the sdl-kmp dependency).
        getByName("wasmJsMain").dependsOn(sdlMain)
        getByName("wasmJsMain") {
            dependencies {
                implementation(libs.kotlinx.browser)
            }
        }
        getByName("androidMain").dependsOn(sdlMain)
        // androidMain reuses the JVM actuals (src/jvmMain) through the srcDir
        // below, so it also needs the shared expect/shared-adapter layer.
        getByName("androidMain").dependsOn(jniMain)
        listOf(
            "macosArm64",
            "macosX64",
            "linuxX64",
            "linuxArm64",
            "mingwX64",
            "androidNativeArm64",
            "androidNativeArm32",
            "androidNativeX64",
            "androidNativeX86",
            "iosArm64",
            "iosX64",
            "iosSimulatorArm64",
            "tvosArm64",
            "tvosSimulatorArm64",
        ).forEach { targetName ->
            getByName("${targetName}Main").dependsOn(sdlNativeMain)
        }

        getByName("commonTest") {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        getByName("jvmMain") {
            dependencies {
                // Bundle all five JNI artifacts so consumers get the right
                // native binary out of the box; NativeLoader picks one at
                // runtime by os.name/os.arch.
                runtimeOnly(project(":jni-jvm-linux-x86_64"))
                runtimeOnly(project(":jni-jvm-linux-aarch64"))
                runtimeOnly(project(":jni-jvm-darwin-x86_64"))
                runtimeOnly(project(":jni-jvm-darwin-aarch64"))
                runtimeOnly(project(":jni-jvm-windows-x86_64"))
            }
        }

        getByName("jvmTest") {
            dependencies {
                implementation(libs.junit.jupiter)
                runtimeOnly(libs.junit.platform.launcher)
            }
        }

        getByName("androidMain") {
            // Share the same JNI source code with JVM. On Android the
            // NativeLoader falls back to System.loadLibrary since the .so is
            // bundled inside the AAR's jniLibs.
            kotlin.srcDir("src/jvmMain/kotlin")
        }
    }
}

// ==================== Desktop JVM ====================
// The JNI library is provided by the :jni-jvm-{linux,darwin}-{x86_64,aarch64}
// subprojects. Their JARs ship libimgui_jni as a classpath resource that
// NativeLoader extracts at runtime - so :jvmTest needs no java.library.path
// tweak.
//
// Android host-test targets (Android unit tests running on the JVM) call
// System.loadLibrary directly from ImGui.jvm.kt, so they still need a flat
// directory containing libimgui_jni.{so,dylib}. We reuse the host's
// :jni-jvm-* subproject build output for that.
val hostJniProjectName = run {
    val arch = System.getProperty("os.arch").lowercase()
    val os = OperatingSystem.current()
    val archClassifier = when (arch) {
        "amd64", "x86_64", "x64" -> "x86_64"
        "aarch64", "arm64" -> "aarch64"
        else -> null
    }
    val osClassifier = when {
        os.isLinux -> "linux"
        os.isMacOsX -> "darwin"
        os.isWindows -> "windows"
        else -> null
    }
    if (osClassifier != null && archClassifier != null) {
        ":jni-jvm-$osClassifier-$archClassifier"
    } else {
        null
    }
}

if (hostJniProjectName != null) {
    val hostJniProject = project(hostJniProjectName)
    val hostClassifier = hostJniProjectName.removePrefix(":jni-jvm-")
    val hostNativeDir = hostJniProject.layout.buildDirectory.dir(
        "resources/main/cn/enaium/imgui/native/$hostClassifier",
    )

    tasks.withType<Test>().configureEach {
        if (name.contains("AndroidHostTest", ignoreCase = true)) {
            dependsOn("$hostJniProjectName:buildJniLibrary")
            systemProperty("java.library.path", hostNativeDir.get().asFile.absolutePath)
        }
    }
}

// ==================== Native: build static C++ library for each target ====================
fun registerNativeBuildTasks(targetName: String, cmakeFlags: List<String> = emptyList()) {
    val outputDir = layout.buildDirectory.dir("native/$targetName").get().asFile
    val cmakeBuildDir = layout.buildDirectory.dir("cmake-$targetName").get().asFile

    val configureTask = tasks.register<Exec>("configureNative_$targetName") {
        onlyIf { canBuildNativeTarget(targetName) }
        doFirst {
            cmakeBuildDir.mkdirs()
            outputDir.mkdirs()
        }
        workingDir = cmakeBuildDir
        commandLine(
            listOf(
                cmakeExecutable, jniDir.absolutePath,
                "-DCMAKE_BUILD_TYPE=Release",
                "-DBUILD_JNI=OFF",
                "-DCMAKE_ARCHIVE_OUTPUT_DIRECTORY=${outputDir.absolutePath}",
            ) + cmakeFlags,
        )
    }

    val buildTask = tasks.register<Exec>("buildNative_$targetName") {
        onlyIf { canBuildNativeTarget(targetName) }
        dependsOn(configureTask)
        workingDir = cmakeBuildDir
        commandLine(cmakeExecutable, "--build", ".", "--config", "Release")
    }

    tasks.matching {
        it.name.startsWith("cinteropImgui") &&
            it.name.endsWith(targetName.replaceFirstChar { c -> c.uppercase() })
    }.configureEach {
        dependsOn(buildTask)
        // The cinterop task's up-to-date check only watches headers and the
        // .def file; register the static library as an input so a rebuild of
        // libimgui.a re-embeds it (otherwise stale archives leak into the
        // published klib on incremental builds). Only when the library is
        // actually built on this host (canBuild=false targets ship bindings
        // only and never produce libimgui.a).
        if (canBuildNativeTarget(targetName)) {
            inputs.file(outputDir.resolve("libimgui.a"))
        }
    }
}

if (hostOs.isMacOsX) {
    registerNativeBuildTasks(
        "macosArm64",
        listOf(
            "-DCMAKE_OSX_ARCHITECTURES=arm64",
            "-DCMAKE_SYSTEM_PROCESSOR=arm64",
        ),
    )
    registerNativeBuildTasks(
        "macosX64",
        listOf(
            "-DCMAKE_OSX_ARCHITECTURES=x86_64",
            "-DCMAKE_SYSTEM_PROCESSOR=x86_64",
        ),
    )
    registerNativeBuildTasks(
        "iosArm64",
        listOf(
            "-DCMAKE_SYSTEM_NAME=iOS",
            "-DCMAKE_SYSTEM_PROCESSOR=arm64",
            "-DCMAKE_OSX_ARCHITECTURES=arm64",
            "-DCMAKE_OSX_SYSROOT=iphoneos",
            "-DCMAKE_OSX_DEPLOYMENT_TARGET=13.0",
        ),
    )
    registerNativeBuildTasks(
        "iosX64",
        listOf(
            "-DCMAKE_SYSTEM_NAME=iOS",
            "-DCMAKE_SYSTEM_PROCESSOR=x86_64",
            "-DCMAKE_OSX_ARCHITECTURES=x86_64",
            "-DCMAKE_OSX_SYSROOT=iphonesimulator",
            "-DCMAKE_OSX_DEPLOYMENT_TARGET=13.0",
        ),
    )
    registerNativeBuildTasks(
        "iosSimulatorArm64",
        listOf(
            "-DCMAKE_SYSTEM_NAME=iOS",
            "-DCMAKE_SYSTEM_PROCESSOR=arm64",
            "-DCMAKE_OSX_ARCHITECTURES=arm64",
            "-DCMAKE_OSX_SYSROOT=iphonesimulator",
            "-DCMAKE_OSX_DEPLOYMENT_TARGET=13.0",
        ),
    )
    registerNativeBuildTasks(
        "tvosArm64",
        listOf(
            "-DCMAKE_SYSTEM_NAME=tvOS",
            "-DCMAKE_SYSTEM_PROCESSOR=arm64",
            "-DCMAKE_OSX_ARCHITECTURES=arm64",
            "-DCMAKE_OSX_SYSROOT=appletvos",
            "-DCMAKE_OSX_DEPLOYMENT_TARGET=13.0",
        ),
    )
    registerNativeBuildTasks(
        "tvosSimulatorArm64",
        listOf(
            "-DCMAKE_SYSTEM_NAME=tvOS",
            "-DCMAKE_SYSTEM_PROCESSOR=arm64",
            "-DCMAKE_OSX_ARCHITECTURES=arm64",
            "-DCMAKE_OSX_SYSROOT=appletvsimulator",
            "-DCMAKE_OSX_DEPLOYMENT_TARGET=13.0",
        ),
    )
    registerNativeBuildTasks(
        "watchosArm64",
        listOf(
            "-DCMAKE_SYSTEM_NAME=watchOS",
            "-DCMAKE_SYSTEM_PROCESSOR=arm64",
            "-DCMAKE_OSX_ARCHITECTURES=arm64",
            "-DCMAKE_OSX_SYSROOT=watchos",
            "-DCMAKE_OSX_DEPLOYMENT_TARGET=5.0",
        ),
    )
    registerNativeBuildTasks(
        "watchosSimulatorArm64",
        listOf(
            "-DCMAKE_SYSTEM_NAME=watchOS",
            "-DCMAKE_SYSTEM_PROCESSOR=arm64",
            "-DCMAKE_OSX_ARCHITECTURES=arm64",
            "-DCMAKE_OSX_SYSROOT=watchsimulator",
            "-DCMAKE_OSX_DEPLOYMENT_TARGET=5.0",
        ),
    )
    registerNativeBuildTasks(
        "watchosDeviceArm64",
        listOf(
            "-DCMAKE_SYSTEM_NAME=watchOS",
            "-DCMAKE_SYSTEM_PROCESSOR=arm64",
            "-DCMAKE_OSX_ARCHITECTURES=arm64",
            "-DCMAKE_OSX_SYSROOT=watchos",
            "-DCMAKE_OSX_DEPLOYMENT_TARGET=5.0",
        ),
    )
    // mingwX64 cross-compiles with the Homebrew MinGW-w64 toolchain
    // (`brew install mingw-w64`); the produced libimgui.a is embedded into the
    // mingwX64 cinterop klib, exactly like the Linux branch does it.
    registerNativeBuildTasks(
        "mingwX64",
        listOf(
            "-DCMAKE_SYSTEM_NAME=Windows",
            "-DCMAKE_SYSTEM_PROCESSOR=x86_64",
            "-DCMAKE_C_COMPILER=x86_64-w64-mingw32-gcc",
            "-DCMAKE_CXX_COMPILER=x86_64-w64-mingw32-g++",
            "-DCMAKE_RC_COMPILER=x86_64-w64-mingw32-windres",
        ),
    )
} else if (hostOs.isLinux) {
    registerNativeBuildTasks("linuxX64")
    // linuxArm64: builds only when the toolchain is present (native aarch64
    // host, or x86_64 host with the aarch64-linux-gnu cross-compiler installed
    // via `sudo apt-get install gcc-aarch64-linux-gnu g++-aarch64-linux-gnu`);
    // otherwise the target ships bindings-only klibs, so the C++ static
    // library is never attempted on runners that lack the cross-compiler
    // (e.g. the Android CI job).
    registerNativeBuildTasks(
        "linuxArm64",
        if (isAarch64Host()) emptyList() else listOf(
            "-DCMAKE_SYSTEM_NAME=Linux",
            "-DCMAKE_SYSTEM_PROCESSOR=aarch64",
            "-DCMAKE_C_COMPILER=aarch64-linux-gnu-gcc",
            "-DCMAKE_CXX_COMPILER=aarch64-linux-gnu-g++",
        ),
    )
    // mingwX64 is cross-compiled with MinGW-w64; install it with
    // `sudo apt-get install gcc-mingw-w64-x86-64 g++-mingw-w64-x86-64`.
    // CMAKE_SYSTEM_NAME=Windows makes CMake treat this as a Windows
    // cross-build (WIN32 branch, .a archive in MinGW format).
    registerNativeBuildTasks(
        "mingwX64",
        listOf(
            "-DCMAKE_SYSTEM_NAME=Windows",
            "-DCMAKE_SYSTEM_PROCESSOR=x86_64",
            "-DCMAKE_C_COMPILER=x86_64-w64-mingw32-gcc",
            "-DCMAKE_CXX_COMPILER=x86_64-w64-mingw32-g++",
            "-DCMAKE_RC_COMPILER=x86_64-w64-mingw32-windres",
        ),
    )
}

// ==================== Android: build JNI shared library per ABI ====================
val androidJniAbis = listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")

val androidJniLibsDir = layout.buildDirectory.dir("jniLibs")

val buildAndroidJniLibs by tasks.registering {
    group = "build"
    description = "Builds the JNI shared library for all Android ABIs."
}

androidJniAbis.forEach { abi ->
    val outputDir = layout.buildDirectory.dir("jniLibs/$abi")
    val cmakeBuildDir = layout.buildDirectory.dir("cmake-android-$abi").get().asFile

    val configureTask = tasks.register<Exec>("configureAndroidJni_$abi") {
        onlyIf { canBuildAndroidJni() && androidNdkToolchain?.isFile == true }
        doFirst {
            cmakeBuildDir.mkdirs()
            outputDir.get().asFile.mkdirs()
        }
        workingDir = cmakeBuildDir
        commandLine(
            cmakeExecutable, jniDir.absolutePath,
            "-DCMAKE_BUILD_TYPE=Release",
            "-DCMAKE_TOOLCHAIN_FILE=${androidNdkToolchain?.absolutePath ?: ""}",
            "-DANDROID_ABI=$abi",
            "-DANDROID_PLATFORM=android-$androidApiLevel",
            "-DCMAKE_LIBRARY_OUTPUT_DIRECTORY=${outputDir.get().asFile.absolutePath}",
        )
    }

    val buildTask = tasks.register<Exec>("buildAndroidJni_$abi") {
        onlyIf { canBuildAndroidJni() && androidNdkToolchain?.isFile == true }
        dependsOn(configureTask)
        workingDir = cmakeBuildDir
        commandLine(cmakeExecutable, "--build", ".", "--config", "Release")
    }

    buildAndroidJniLibs.configure { dependsOn(buildTask) }
}

// ==================== Android native (Kotlin/Native) static libraries ====================
// Cross-compiled with the NDK toolchain for the androidNative targets; the
// resulting libimgui.a is embedded into each target's cinterop klib.
androidNdkToolchain?.takeIf { it.isFile && canBuildAndroidJni() }?.let { toolchain ->
    listOf(
        "androidNativeArm64" to "arm64-v8a",
        "androidNativeArm32" to "armeabi-v7a",
        "androidNativeX64" to "x86_64",
        "androidNativeX86" to "x86",
    ).forEach { (targetName, abi) ->
        registerNativeBuildTasks(
            targetName,
            listOf(
                "-DCMAKE_TOOLCHAIN_FILE=${toolchain.absolutePath}",
                "-DANDROID_ABI=$abi",
                "-DANDROID_PLATFORM=android-$androidApiLevel",
                // `libc++_shared.so` (shipped with the platform since API 24),
                // which avoids embedding NDK's static libc++ into the K/N-linked
                // `libmain.so` and the resulting dead-code elimination that
                // strips `~basic_string` before dlopen can resolve it.
                "-DANDROID_STL=c++_shared",
            ),
        )
    }
}

androidComponents {
    onVariants { variant ->
        variant.sources.jniLibs?.addStaticSourceDirectory(
            androidJniLibsDir.get().asFile.absolutePath,
        )
    }
}

tasks.matching { it.name.startsWith("merge") && it.name.contains("JniLibFolders") }
    .configureEach { dependsOn(buildAndroidJniLibs) }

// ==================== wasm: build the imgui module for the wasmJs target ====================
// The C API (+ extensions) is cross-compiled with Emscripten and linked into a
// standalone ES module (imgui_wasm.js + imgui_wasm.wasm) that the wasmJs
// actuals drive through a JS glue layer (see wasm/imgui_kmp_glue.js). The
// emsdk is located via EMSDK, the `wasm.emsdk` Gradle property, or ~/emsdk -
// the same convention as sdl-kmp.
fun emsdkPath(): String {
    System.getenv("EMSDK")?.takeIf { it.isNotBlank() }?.let { return it }
    providers.gradleProperty("wasm.emsdk").orNull?.takeIf { it.isNotBlank() }?.let { return it }
    val home = System.getProperty("user.home")
    listOf(File(home, "emsdk"), File(home, "sdk/emsdk"))
        .forEach { if (it.isDirectory()) return it.absolutePath }
    return ""
}

val wasmEmsdk = emsdkPath()
val wasmEmxx = File(wasmEmsdk, "upstream/emscripten/em++")
val wasmToolchain = File(wasmEmsdk, "upstream/emscripten/cmake/Modules/Platform/Emscripten.cmake")
val wasmSourceDir = projectDir.resolve("wasm")
val wasmOutput = layout.buildDirectory.dir("wasm").get().asFile
val wasmNativeOutput = layout.buildDirectory.dir("native/wasm").get().asFile
val wasmCmakeDir = layout.buildDirectory.dir("cmake-wasm").get().asFile

// The emsdk ships its own python/node; emcc refuses the system python 3.9 that
// macOS resolves by default.
fun wasmEnv(): Map<String, String> {
    val pythonDir = File(wasmEmsdk, "python").listFiles()?.firstOrNull { it.isDirectory }
    val nodeDir = File(wasmEmsdk, "node").listFiles()?.firstOrNull { it.isDirectory }?.resolve("bin")
    val path = listOfNotNull(
        File(wasmEmsdk, "upstream/emscripten").absolutePath,
        pythonDir?.resolve("bin")?.absolutePath,
        nodeDir?.absolutePath,
        System.getenv("PATH"),
    ).joinToString(File.pathSeparator)
    return mapOf("EMSDK" to wasmEmsdk, "EM_CONFIG" to File(wasmEmsdk, ".emscripten").absolutePath, "PATH" to path)
}

val configureWasmImgui = tasks.register<Exec>("configureWasmImgui") {
    group = "wasm"
    description = "Configures the Emscripten build of the imgui C API."
    onlyIf { wasmEmsdk.isNotEmpty() }
    dependsOn(rootProject.tasks.named("applySubmodulePatches"))
    doFirst {
        wasmCmakeDir.mkdirs()
        wasmNativeOutput.mkdirs()
    }
    workingDir = wasmCmakeDir
    environment(wasmEnv())
    val makeProgram = if (OperatingSystem.current().isWindows) "nmake" else "/usr/bin/make"
    commandLine(
        cmakeExecutable, jniDir.absolutePath,
        "-DCMAKE_BUILD_TYPE=Release",
        "-DCMAKE_TOOLCHAIN_FILE=${wasmToolchain.absolutePath}",
        "-DCMAKE_MAKE_PROGRAM=$makeProgram",
        "-DBUILD_JNI=OFF",
        "-DCMAKE_ARCHIVE_OUTPUT_DIRECTORY=${wasmNativeOutput.absolutePath}",
    )
}

val buildWasmImgui = tasks.register<Exec>("buildWasmImgui") {
    group = "wasm"
    description = "Builds libimgui.a for the wasm target with Emscripten."
    onlyIf { wasmEmsdk.isNotEmpty() }
    dependsOn(configureWasmImgui)
    workingDir = wasmCmakeDir
    environment(wasmEnv())
    commandLine(cmakeExecutable, "--build", ".", "--config", "Release")
    inputs.files(
        fileTree(jniDir) {
            include("**/*.cc", "**/*.h", "CMakeLists.txt")
            // jni/jvm holds the per-OS JNI artifact projects; their build
            // outputs must not be treated as sources of this task.
            exclude("jvm/**")
        },
    )
    outputs.file(File(wasmNativeOutput, "libimgui.a"))
}

val generateWasmShim = tasks.register<Exec>("generateWasmShim") {
    group = "wasm"
    description = "Generates the wasm shim + export list from the imgui C API headers."
    workingDir = rootDir
    commandLine(
        "/usr/bin/env", "python3",
        wasmSourceDir.resolve("gen_wasm_shim.py").absolutePath,
        rootDir.absolutePath,
        wasmOutput.absolutePath,
    )
    inputs.file(jniDir.resolve("c_api/imgui_c.h"))
    inputs.file(wasmSourceDir.resolve("gen_wasm_shim.py"))
    outputs.file(File(wasmOutput, "imgui_kmp_shim.cpp"))
    outputs.file(File(wasmOutput, "imgui_kmp_exports.js"))
}

val linkWasmImgui = tasks.register<Exec>("linkWasmImgui") {
    group = "wasm"
    description = "Links libimgui.a + the generated shim into imgui_wasm.js/.wasm."
    onlyIf { wasmEmsdk.isNotEmpty() }
    dependsOn(buildWasmImgui, generateWasmShim)
    workingDir = wasmSourceDir
    environment(wasmEnv())
    doFirst {
        wasmOutput.mkdirs()
        val exports = Regex("\"([A-Za-z_]\\w*)\"")
            .findAll(File(wasmOutput, "imgui_kmp_exports.js").readText())
            .map { it.groupValues[1] }
            .distinct()
            .joinToString(",") { "_$it" }
        commandLine(
            wasmEmxx.absolutePath,
            File(wasmOutput, "imgui_kmp_shim.cpp").absolutePath,
            File(wasmNativeOutput, "libimgui.a").absolutePath,
            "-I${jniDir.resolve("c_api").absolutePath}",
            "-I${rootProject.file("includes/imgui").absolutePath}",
            "-o", File(wasmOutput, "imgui_wasm.js").absolutePath,
            "-sMODULARIZE=1", "-sEXPORT_NAME=ImguiModule", "-sEXPORT_ES6=1",
            "-sEXPORTED_FUNCTIONS=$exports,_malloc,_free",
            "-sEXPORTED_RUNTIME_METHODS=stringToUTF8,lengthBytesUTF8,UTF8ToString,HEAPU8,HEAPU16,HEAP32,HEAPF32,HEAPF64,HEAP8",
            "-sALLOW_MEMORY_GROWTH=1", "-sINITIAL_MEMORY=67108864",
            "-sENVIRONMENT=web,worker,node", "--no-entry",
            "-sDISABLE_EXCEPTION_CATCHING=1",
        )
    }
    inputs.file(File(wasmOutput, "imgui_kmp_shim.cpp"))
    inputs.file(File(wasmNativeOutput, "libimgui.a"))
    outputs.file(File(wasmOutput, "imgui_wasm.js"))
    outputs.file(File(wasmOutput, "imgui_wasm.wasm"))
}

// The glue + export list are the runtime assets a host page imports.
val assembleWasmAssets = tasks.register<Copy>("assembleWasmAssets") {
    group = "wasm"
    description = "Copies the wasm module + glue into build/wasm/assets."
    dependsOn(linkWasmImgui)
    from(wasmOutput) { include("imgui_wasm.js", "imgui_wasm.wasm", "imgui_kmp_exports.js") }
    from(wasmSourceDir) { include("imgui_kmp_glue.js") }
    into(layout.buildDirectory.dir("wasm/assets"))
}

// ==================== npm package ====================
// The Emscripten module cannot ride inside the klib, so it ships as an npm
// package next to it (same layout as sdl-kmp's @enaium/sdl-kmp).
val wasmNpmDir = layout.buildDirectory.dir("wasm/npm").get().asFile
// The project version is already a valid semver (1.0.0-fixed.1.93.0).
val npmPackageVersion = version.toString()

tasks.register("assembleWasmNpm") {
    group = "build"
    description = "Stages the @enaium/imgui-kmp npm package in build/wasm/npm."
    dependsOn(assembleWasmAssets)
    inputs.dir(layout.buildDirectory.dir("wasm/assets"))
    inputs.property("npmPackageVersion", npmPackageVersion)
    outputs.dir(wasmNpmDir)
    doLast {
        wasmNpmDir.deleteRecursively()
        wasmNpmDir.mkdirs()
        copy {
            from(layout.buildDirectory.dir("wasm/assets"))
            into(wasmNpmDir)
        }
        wasmNpmDir.resolve("package.json").writeText(
            """
            {
              "name": "@enaium/imgui-kmp",
              "version": "$npmPackageVersion",
              "description": "Emscripten-compiled Dear ImGui + ImPlot module and JS glue for the imgui-kmp wasmJs target",
              "license": "MIT",
              "type": "module",
              "exports": {
                ".": "./imgui_kmp_glue.js",
                "./imgui_wasm.js": "./imgui_wasm.js",
                "./package.json": "./package.json"
              },
              "files": ["imgui_kmp_glue.js", "imgui_kmp_exports.js", "imgui_wasm.js", "imgui_wasm.wasm"],
              "homepage": "https://github.com/Enaium/imgui-kmp",
              "repository": {
                "type": "git",
                "url": "git+https://github.com/Enaium/imgui-kmp.git"
              }
            }
            """.trimIndent() + "\n",
        )
    }
}

tasks.register<Exec>("publishWasmNpm") {
    group = "publishing"
    description = "Publishes @enaium/imgui-kmp to npm (token: npm.publish.key)."
    dependsOn("assembleWasmNpm")
    workingDir = wasmNpmDir
    // The token lives in ~/.gradle/gradle.properties (next to the Maven
    // Central entries); CI writes it there from the NPM_PUBLISH_KEY secret.
    // -Pnpm.publish.key=<token> overrides it for one-off runs.
    val npmKey = providers.gradleProperty("npm.publish.key")
    doFirst {
        val key = npmKey.orNull?.takeIf { it.isNotBlank() }
            ?: throw GradleException(
                "npm.publish.key is not set: add it to ~/.gradle/gradle.properties " +
                    "or pass -Pnpm.publish.key=<token>",
            )
        // Expanded by npm from the environment, so the token never lands in
        // package.json or the tarball.
        environment("NODE_AUTH_TOKEN", key)
        wasmNpmDir.resolve(".npmrc").writeText(
            "//registry.npmjs.org/:_authToken=\${NODE_AUTH_TOKEN}\n",
        )
    }
    // The -fixed.<imgui> version is a semver prerelease, so npm needs an
    // explicit tag; `latest` keeps `npm i @enaium/imgui-kmp` working.
    commandLine("npm", "publish", "--access", "public", "--tag", "latest")
}

// ==================== Publishing ====================
mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    coordinates(
        groupId = group.toString(),
        artifactId = "imgui-kmp",
        // null -> the plugin falls back to project.version (gradle.properties
        // or -Pversion on the command line)
        version = null,
    )

    pom {
        name.set("imgui-kmp")
        description.set(
            "Kotlin Multiplatform bindings for Dear ImGui and ImPlot.",
        )
        url.set("https://github.com/Enaium/imgui-kmp")
        inceptionYear.set("2026")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("Enaium")
            }
        }

        scm {
            url.set("https://github.com/Enaium/imgui-kmp")
            connection.set("scm:git:git@github.com:Enaium/imgui-kmp.git")
            developerConnection.set("scm:git:git@github.com:Enaium/imgui-kmp.git")
        }

        issueManagement {
            system.set("GitHub")
            url.set("https://github.com/Enaium/imgui-kmp/issues")
        }
    }
}


