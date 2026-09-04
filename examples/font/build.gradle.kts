plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
        mainRun {
            mainClass = "cn.enaium.imgui.example.font.Main_jvmKt"
        }
    }

    macosArm64 {
        binaries.executable()
    }
    macosX64 {
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
