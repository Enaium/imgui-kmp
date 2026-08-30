pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "imgui-kmp"

include(":imgui-kmp")
include(":examples:common")
include(":examples:android-sdl")
include(":examples:sdl_renderer")
include(":examples:sdl_gpu")
include(":examples:sdl_renderer:android")
include(":examples:sdl_gpu:android")
include(":examples:node_editor")
include(":examples:implot3d")
include(":examples:implot3d:android")
include(":examples:filedialog")
include(":examples:filedialog:android")
include(":examples:colortextedit")
include(":examples:colortextedit:android")
include(":examples:club")
include(":examples:node_editor:android")
include(":examples:club:android")

// Per-OS/arch JNI artifacts that bundle the prebuilt libimgui_jni shared
// library as a classpath resource. NativeLoader extracts the matching one at
// runtime.
listOf(
    "linux-x86_64",
    "linux-aarch64",
    "darwin-x86_64",
    "darwin-aarch64",
    "windows-x86_64",
).forEach { classifier ->
    val name = ":jni-jvm-$classifier"
    include(name)
    project(name).projectDir = file("jni/jvm/$classifier")
}
