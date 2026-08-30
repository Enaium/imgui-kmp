plugins {
    alias(libs.plugins.android.application)
}

// The KMP example module builds libmain.so (with an exported SDL_main) for
// every androidNative ABI; copy those into jniLibs and depend on the link
// tasks so the APK is assembled after them. We also copy `libc++_shared.so`
// for each ABI because the stock Android emulator system image doesn't ship
// the shared C++ runtime, and libmain.so exports a `libc++_shared.so`
// dependency via Kotlin/Native's toolchain.
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
            val src = File(bin, "$target/mainDebugShared/libmain.so")
            if (!src.exists()) {
                throw GradleException("Expected $src — did linkMainDebugShared$target fail in :examples:filedialog?")
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

val prepareJniLibs = tasks.register<PrepareJniLibsTask>("prepareJniLibs") {
    outputDir.set(layout.buildDirectory.dir("generated/jniLibs"))
    abis.set(androidAbis)
    cxxSharedTriples.set(cxxSharedTriple)
}
prepareJniLibs.configure {
    androidAbis.keys.forEach { target ->
        val linkTask = project(":examples:filedialog").tasks.named(
            "linkMainDebugShared${target.replaceFirstChar { it.uppercase() }}",
        )
        dependsOn(linkTask)
        // Re-run packaging whenever the linked libmain.so changes; otherwise the
        // task is silently UP-TO-DATE after its first run and a rebuilt libmain.so
        // never reaches the APK.
        inputs.files(linkTask.flatMap { (it as org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink).outputFile })
    }
}

android {
    namespace = "cn.enaium.imgui.example.filedialog"
    compileSdk = 36
    defaultConfig {
        applicationId = "cn.enaium.imgui.example.filedialog"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    // The org.libsdl.app SDLActivity sources live in the shared
    // :examples:android-sdl library (one copy for all example apps), so the
    // Java side matches the statically linked SDL3 in the sdl-kmp klib. (The
    // upstream sdl-kmp examples point at a ../../../SDL/ submodule;
    // imgui-kmp doesn't ship one.)
}

// Register the generated libmain.so directory with AGP's Variant API.
androidComponents {
    onVariants { variant ->
        variant.sources.jniLibs?.addGeneratedSourceDirectory(prepareJniLibs) { it.outputDir }
    }
}

dependencies {
    implementation(project(":examples:android-sdl"))
}