/*
 * Per-OS/arch JNI artifact: darwin-x86_64.
 * Ships libimgui_jni.dylib as a classpath resource at
 * /cn/enaium/imgui/native/darwin-x86_64/, which NativeLoader
 * (in :imgui-kmp's jvmMain) extracts and System.load()s at runtime.
 */
import org.gradle.internal.os.OperatingSystem

plugins {
    `java-library`
    alias(libs.plugins.maven.publish)
}

group = rootProject.group
version = rootProject.version

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

val jniOs = "darwin"
val jniArch = "x86_64"
val classifier = "$jniOs-$jniArch"
val libFile = "libimgui_jni.dylib"
val resourceDir = "cn/enaium/imgui/native/$classifier"

val canBuildHere = OperatingSystem.current().isMacOsX

val nativeOutputDir = layout.buildDirectory.dir("jni-native/$classifier")
val cmakeBuildDir = layout.buildDirectory.dir("cmake-jni/$classifier")

val configureJniLibrary = tasks.register<Exec>("configureJniLibrary") {
    group = "build"
    description = "cmake-configures libimgui_jni for $classifier."
    onlyIf { canBuildHere }
    val outDir = nativeOutputDir.get().asFile
    val buildDir = cmakeBuildDir.get().asFile
    doFirst {
        outDir.mkdirs()
        buildDir.mkdirs()
    }
    workingDir = buildDir
    val javaHome = System.getProperty("java.home") ?: System.getenv("JAVA_HOME") ?: ""
    val jniInclude = if (javaHome.isNotEmpty()) "$javaHome/include" else ""
    commandLine(
        "cmake",
        rootProject.file("jni").absolutePath,
        "-DCMAKE_BUILD_TYPE=Release",
        "-DJNI_INCLUDE_DIR=$jniInclude",
        "-DJNI_INCLUDE_DIR_PLATFORM=$jniInclude/darwin",
        "-DCMAKE_OSX_ARCHITECTURES=x86_64",
        "-DCMAKE_SYSTEM_PROCESSOR=x86_64",
        "-DCMAKE_LIBRARY_OUTPUT_DIRECTORY=${outDir.absolutePath}",
    )
}

val buildJniLibrary = tasks.register<Exec>("buildJniLibrary") {
    group = "build"
    description = "Builds libimgui_jni.dylib for $classifier."
    onlyIf { canBuildHere }
    dependsOn(configureJniLibrary)
    workingDir = cmakeBuildDir.get().asFile
    commandLine("cmake", "--build", ".", "--config", "Release")
    inputs.files(rootProject.file("jni/CMakeLists.txt"), rootProject.file("jni/jni_bridge.cpp"))
    inputs.dir(rootProject.file("jni/c_api"))
    inputs.dir(rootProject.file("includes/imgui"))
    inputs.dir(rootProject.file("includes/implot"))
    outputs.file(nativeOutputDir.map { it.file(libFile) })
}

tasks.named<Copy>("processResources") {
    dependsOn(buildJniLibrary)
    // Copy the built shared library into the classpath resources. Sources
    // are resolved at execution time (the directory is created by the cmake
    // configure step); MSVC builds place the artifact in a Release/ folder
    // under the cmake build dir, so both locations are covered.
    from(nativeOutputDir) {
        include(libFile)
        into(resourceDir)
    }
    from(cmakeBuildDir.map { it.asFile.resolve("Release") }) {
        include(libFile)
        into(resourceDir)
    }
    doLast {
        val out = layout.buildDirectory.file("resources/main/$resourceDir/$libFile").get().asFile
        if (!out.isFile) {
            val candidates = mutableListOf<String>()
            nativeOutputDir.get().asFile.listFiles()?.forEach { candidates.add(it.name) }
            cmakeBuildDir.get().asFile.resolve("Release").listFiles()?.forEach { candidates.add(it.name) }
            throw GradleException(
                "Build of $libFile produced no artifact. Found: $candidates",
            )
        }
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    coordinates(
        groupId = rootProject.group.toString(),
        artifactId = "imgui-kmp-jni-jvm-$classifier",
        version = rootProject.version.toString(),
    )
    pom {
        name.set("imgui-kmp-jni-jvm-$classifier")
        description.set(
            "Prebuilt JNI shared library for imgui-kmp on $jniOs/$jniArch. " +
                "Loaded automatically by NativeLoader; not intended to be depended on directly.",
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
