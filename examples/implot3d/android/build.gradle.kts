plugins {
    alias(libs.plugins.android.application)
}

// The KMP example module builds libmain.so (with an exported SDL_main) for
// every androidNative ABI; copy those into jniLibs and depend on the link
// tasks so the APK is assembled after them. We also copy `libc++_shared.so`
// for each ABI because the stock Android emulator system image doesn't ship
// the shared C++ runtime, and libmain.so exports a `libc++_shared.so`
// dependency via Kotlin/Native's toolchain.
// Each Android build type packages the matching Kotlin/Native binary: the debug
// APK gets the unoptimized `mainDebugShared` library, the release APK the
// optimized `mainReleaseShared` one. Kotlin/Native debug code is an order of
// magnitude slower on frame-heavy code, so device/emulator frame-rate runs want
// `assembleRelease` (signed with the debug keystore, see `buildTypes`).
val androidAbis = mapOf(
    "androidNativeArm64" to "arm64-v8a",
    "androidNativeArm32" to "armeabi-v7a",
    "androidNativeX64" to "x86_64",
    "androidNativeX86" to "x86",
)
val cxxSharedTriple = mapOf(
    "androidNativeArm64" to "aarch64-linux-android",
    "androidNativeArm32" to "arm-linux-androideabi",
    "androidNativeX64" to "x86_64-linux-android",
    "androidNativeX86" to "i686-linux-android",
)

abstract class PrepareJniLibsTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val abis: MapProperty<String, String>

    @get:Input
    abstract val cxxSharedTriples: MapProperty<String, String>

    @get:Input
    abstract val buildType: Property<String>

    @TaskAction
    fun run() {
        val bin = project.layout.projectDirectory.dir("../build/bin").asFile
        outputDir.get().asFile.deleteRecursively()

        // Resolve Kotlin/Native's Android toolchain libc++_shared.so first, then
        // fall back to a freshly installed NDK copy.
        fun knanCxxShared(target: String): File? {
            val konanData = System.getenv("KONAN_DATA_DIR")
                ?: System.getProperty("user.home")?.let { File(it, ".konan").absolutePath }
            val toolchain = File(konanData, "dependencies").listFiles()
                ?.firstOrNull { it.isDirectory && it.name.matches(Regex("target-toolchain-.*-android_ndk")) }
                ?: return null
            val triple = cxxSharedTriples.get()[target] ?: return null
            return File(toolchain, "sysroot/usr/lib/$triple/libc++_shared.so").takeIf { it.exists() }
        }

        fun ndkCxxShared(target: String): File? {
            val sdkDir = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT") ?: return null
            val prebuiltDir = File(sdkDir, "ndk").listFiles()
                ?.filter { it.isDirectory && it.name.matches(Regex("\\d+(\\.\\d+)+")) }
                ?.maxByOrNull { it.name }
                ?.let { File(it, "toolchains/llvm/prebuilt") }
                ?: return null
            val host = when {
                System.getProperty("os.name").lowercase().contains("mac") -> "darwin-" + System.getProperty("os.arch")
                System.getProperty("os.name").lowercase().contains("linux") -> "linux-" + System.getProperty("os.arch")
                else -> null
            } ?: return null
            val triple = cxxSharedTriples.get()[target] ?: return null
            return File(prebuiltDir, "$host/sysroot/usr/lib/$triple/libc++_shared.so").takeIf { it.exists() }
        }

        abis.get().forEach { (target, abi) ->
            val src = File(bin, "$target/main${buildType.get().replaceFirstChar { it.uppercase() }}Shared/libmain.so")
            if (!src.exists()) {
                throw GradleException("Expected $src — did the ${buildType.get()} Kotlin/Native link task fail in :examples:implot3d?")
            }
            val dstDir = File(outputDir.get().asFile, abi)
            dstDir.mkdirs()
            src.copyTo(File(dstDir, "libmain.so"), overwrite = true)
            val cxxShared = knanCxxShared(target) ?: ndkCxxShared(target)
            if (cxxShared != null) {
                cxxShared.copyTo(File(dstDir, "libc++_shared.so"), overwrite = true)
            } else {
                logger.warn("No libc++_shared.so found for $abi; the APK may fail to load libmain.so at runtime.")
            }
        }
    }
}

android {
    namespace = "cn.enaium.imgui.example.implot3d"
    compileSdk = 36
    defaultConfig {
        applicationId = "cn.enaium.imgui.example.implot3d"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            // Local device/emulator runs: sign the release APK with the debug
            // keystore so it installs without a release key.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// One jniLibs preparation task per build type, wired to that build type's
// Kotlin/Native link tasks and registered with AGP's Variant API.
androidComponents {
    onVariants { variant ->
        val variantBuildType = requireNotNull(variant.buildType) { "application variant has no build type" }
        val capitalizedBuildType = variantBuildType.replaceFirstChar { it.uppercase() }
        val prepare = tasks.register<PrepareJniLibsTask>("prepareJniLibs$capitalizedBuildType") {
            outputDir.set(layout.buildDirectory.dir("generated/jniLibs/$variantBuildType"))
            abis.set(androidAbis)
            cxxSharedTriples.set(cxxSharedTriple)
            buildType.set(variantBuildType)
        }
        androidAbis.keys.forEach { target ->
            val linkTask = project(":examples:implot3d").tasks.named(
                "linkMain${capitalizedBuildType}Shared${target.replaceFirstChar { it.uppercase() }}",
            )
            prepare.configure {
                dependsOn(linkTask)
                // Re-run packaging whenever the linked libmain.so changes; otherwise the
                // task is silently UP-TO-DATE after its first run and a rebuilt libmain.so
                // never reaches the APK.
                inputs.files(linkTask.flatMap { (it as org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink).outputFile })
            }
        }
        variant.sources.jniLibs?.addGeneratedSourceDirectory(prepare) { it.outputDir }
    }
}

dependencies {
    implementation(libs.sdl.kmp)
}