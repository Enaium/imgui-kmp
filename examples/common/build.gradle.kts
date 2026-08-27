plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    macosArm64()
    linuxX64()
    linuxArm64()
    mingwX64()

    androidNativeArm64()
    androidNativeArm32()
    androidNativeX64()
    androidNativeX86()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.sdl.kmp)
                implementation(project(":imgui-kmp"))
            }
        }
    }
}
