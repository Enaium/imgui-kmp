/*
 * Per-OS/arch JNI artifact: windows-x86_64.
 * Ships imgui_jni.dll as a classpath resource at
 * /cn/enaium/imgui/native/windows-x86_64/, which NativeLoader
 * (in :imgui-kmp's jvmMain) extracts and System.load()s at runtime.
 *
 * CMake writes the shared library directly into build/resources/main so the
 * produced JAR always carries it, regardless of generator or output-dir
 * quirks. No copy step is involved (a Copy task snapshots its sources at
 * configuration time, when the artifact does not exist yet on fresh
 * checkouts).
 */
import org.gradle.internal.os.OperatingSystem
import java.io.File

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

val cmakeExecutable = resolveCmakeExecutable()

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

val jniOs = "windows"
val jniArch = "x86_64"
val classifier = "$jniOs-$jniArch"
val libFile = "imgui_jni.dll"
val resourceDir = "cn/enaium/imgui/native/$classifier"

val canBuildHere = OperatingSystem.current().isWindows

// The shared library lands directly in the resources output, so the jar
// task picks it up with no further copying.
val resourceOutputDir = layout.buildDirectory.dir("resources/main/$resourceDir")
val cmakeBuildDir = layout.buildDirectory.dir("cmake-jni/$classifier")

val configureJniLibrary = tasks.register<Exec>("configureJniLibrary") {
    group = "build"
    description = "cmake-configures imgui_jni.dll for $classifier."
    onlyIf { canBuildHere }
    val outDir = resourceOutputDir.get().asFile
    val buildDir = cmakeBuildDir.get().asFile
    doFirst {
        outDir.mkdirs()
        buildDir.mkdirs()
    }
    workingDir = buildDir
    val javaHome = System.getProperty("java.home") ?: System.getenv("JAVA_HOME") ?: ""
    val jniInclude = if (javaHome.isNotEmpty()) "$javaHome/include" else ""
    commandLine(
        cmakeExecutable,
        rootProject.file("jni").absolutePath,
        "-G",
        "MinGW Makefiles",
        "-DCMAKE_BUILD_TYPE=Release",
        "-DJNI_INCLUDE_DIR=$jniInclude",
        "-DJNI_INCLUDE_DIR_PLATFORM=$jniInclude/win32",
        "-DCMAKE_LIBRARY_OUTPUT_DIRECTORY=${outDir.absolutePath}",
        
    )
}

val buildJniLibrary = tasks.register<Exec>("buildJniLibrary") {
    group = "build"
    description = "Builds imgui_jni.dll for $classifier."
    onlyIf { canBuildHere }
    dependsOn(configureJniLibrary)
    workingDir = cmakeBuildDir.get().asFile
    commandLine(cmakeExecutable, "--build", ".", "--config", "Release")
    inputs.files(rootProject.file("jni/CMakeLists.txt"), rootProject.file("jni/jni_bridge.cpp"))
    inputs.dir(rootProject.file("jni/c_api"))
    inputs.dir(rootProject.file("includes/imgui"))
    inputs.dir(rootProject.file("includes/implot"))
    inputs.dir(rootProject.file("includes/implot3d"))
    inputs.dir(rootProject.file("includes/imgui-node-editor"))
    inputs.dir(rootProject.file("includes/ImGuiFileDialog"))
    inputs.dir(rootProject.file("includes/ImGuiColorTextEdit"))
    inputs.dir(rootProject.file("includes/imgui_club"))
    outputs.file(resourceOutputDir.map { it.file(libFile) })
}

// cmake writes the library into the resources output; make sure it is
// built before anything packages it.
tasks.named("jar") {
    dependsOn(buildJniLibrary)
}

// The Android host tests (JVM) load the library from java.library.path; it
// lives in the same resources output the JNI jar ships.
val hostNativeDir = resourceOutputDir

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
