import org.gradle.api.GradleException

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.maven.publish) apply false
}

allprojects {
    group = "cn.enaium.imgui"
    version = "1.0.6"
}

// ==================== Submodule patches ====================
// The vendored submodules (imgui-node-editor, ImGuiFileDialog) carry fixes
// that must not live in the submodule history itself (the submodules stay
// pinned to upstream commits). The fixes live as patches under patches/ and
// are applied here, before any task that compiles the C++ sources, so both
// CI and local builds work from a clean checkout.
val applySubmodulePatches = tasks.register("applySubmodulePatches") {
    group = "build"
    description = "Applies patches/ to the vendored submodules (idempotent)."
    doLast {
        fun applyPatch(patchFile: java.io.File, targetDir: java.io.File) {
            if (!patchFile.isFile) {
                throw GradleException("Patch file not found: ${patchFile.absolutePath}")
            }
            fun gitApply(reverse: Boolean): Pair<Int, String> {
                val cmd = mutableListOf("git", "apply")
                if (reverse) cmd.add("--reverse")
                cmd.add("--check")
                cmd.add(patchFile.absolutePath)
                val proc = ProcessBuilder(cmd)
                    .directory(targetDir)
                    .redirectErrorStream(true)
                    .start()
                val out = proc.inputStream.bufferedReader().readText()
                return proc.waitFor() to out
            }
            // Already applied -> reverse check succeeds; leave it alone.
            val (reverseExit, _) = gitApply(reverse = true)
            if (reverseExit == 0) {
                logger.info("Patch ${patchFile.name} already applied to ${targetDir.name}; skipping.")
                return
            }
            val proc = ProcessBuilder("git", "apply", patchFile.absolutePath)
                .directory(targetDir)
                .redirectErrorStream(true)
                .start()
            val out = proc.inputStream.bufferedReader().readText()
            if (proc.waitFor() != 0) {
                throw GradleException(
                    "Failed to apply ${patchFile.name} to ${targetDir.absolutePath}: $out",
                )
            }
            logger.lifecycle("Applied ${patchFile.name} to ${targetDir.name}.")
        }

        applyPatch(
            rootProject.file("patches/imgui-node-editor.patch"),
            rootProject.file("includes/imgui-node-editor"),
        )
        applyPatch(
            rootProject.file("patches/ImGuiFileDialog.patch"),
            rootProject.file("includes/ImGuiFileDialog"),
        )
    }
}

// Any task that compiles the vendored C++ (native static libs, Android JNI,
// per-OS JNI dylibs) needs the patches applied first.
allprojects {
    tasks.configureEach {
        if (
            name.startsWith("buildNative_") ||
            name.startsWith("configureNative_") ||
            name.startsWith("buildAndroidJni_") ||
            name.startsWith("configureAndroidJni_") ||
            name.startsWith("buildJniLibrary") ||
            name.startsWith("configureJniLibrary")
        ) {
            dependsOn(rootProject.tasks.named("applySubmodulePatches"))
        }
    }
}
