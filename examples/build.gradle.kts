plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
        mainRun {
            mainClass = "cn.enaium.imgui.example.Main_jvmKt"
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

    sourceSets {
        commonMain {
            dependencies {
                // sdl-kmp is consumed from Maven Central; imgui-kmp from the
                // local Maven repository (run `./gradlew :imgui-kmp:publishToMavenLocal` first).
                implementation(libs.sdl.kmp)
                implementation(libs.imgui.kmp)
            }
        }
    }
}
