# imgui-kmp

[![Maven Central](https://img.shields.io/maven-central/v/cn.enaium.imgui/imgui-kmp?label=Maven%20Central)](https://central.sonatype.com/artifact/cn.enaium.imgui/imgui-kmp)
[![License](https://img.shields.io/github/license/Enaium/imgui-kmp)](https://github.com/Enaium/imgui-kmp/blob/main/LICENSE)
[![GitHub Actions](https://img.shields.io/github/actions/workflow/status/Enaium/imgui-kmp/test.yml?label=test)](https://github.com/Enaium/imgui-kmp/actions/workflows/test.yml)
[![GitHub Repo stars](https://img.shields.io/github/stars/Enaium/imgui-kmp?style=social)](https://github.com/Enaium/imgui-kmp)

Kotlin Multiplatform bindings for [Dear ImGui](https://github.com/ocornut/imgui) and the ecosystem around it: [ImPlot](https://github.com/epezent/implot), [ImPlot3D](https://github.com/brenocq/implot3d), [imgui-node-editor](https://github.com/thedmd/imgui-node-editor), [ImGuiFileDialog](https://github.com/aiekick/ImGuiFileDialog), [ImGuiColorTextEdit](https://github.com/goossens/ImGuiColorTextEdit) (TextEditor + extras + event hooks), [imgui_markdown](https://github.com/enkisoftware/imgui_markdown), the [imgui_club](https://github.com/ocornut/imgui_club) extensions (memory editor, multi-context compositor, threaded rendering) and SDL3 platform/renderer backends. The bindings wrap the C++ code from the `includes/` submodules through a C API, a JNI bridge (JVM/Android) and Kotlin/Native cinterop (all native targets).

## Supported Platforms

| Platform       | Targets                                                     | Mechanism                                  |
| -------------- | ----------------------------------------------------------- | ------------------------------------------ |
| **JVM**        | Linux x86_64/aarch64, macOS arm64/x86_64, Windows x86_64     | JNI (per-OS/arch JAR resource, auto-extracted by `NativeLoader`) |
| **Android**    | arm64-v8a, armeabi-v7a, x86, x86_64 (JVM API)                | JNI (`.so` bundled in the AAR's `jniLibs`) |
| **Android native** | arm64-v8a, armeabi-v7a, x86_64, x86 (Kotlin/Native API)  | cinterop (static library, NDK cross-compiled) |
| **iOS**        | arm64, x64, simulatorArm64                                   | Kotlin/Native cinterop (static library)    |
| **macOS**      | arm64, x86_64                                                | Kotlin/Native cinterop (static library)    |
| **Linux**      | x86_64, aarch64                                                       | Kotlin/Native cinterop (static library)    |
| **Windows**    | mingwX64                                                     | Kotlin/Native cinterop (static library)    |
| **tvOS**       | arm64, simulatorArm64                                        | Kotlin/Native cinterop (static library)    |
| **watchOS**    | arm64, simulatorArm64, deviceArm64                           | Kotlin/Native cinterop (static library)    |

Android has **two independent APIs**: the JVM API (an AAR with per-ABI JNI `.so` files, loaded via `System.loadLibrary`) and the Kotlin/Native API (`androidNative*` targets, linked as a static library into your `libmain.so`). Use the JVM API from an Android app written in Kotlin/JVM; use the native API when your Android app is Kotlin/Native (e.g. via [sdl-kmp](https://github.com/Enaium/sdl-kmp)'s `libmain.so`), since a JVM `.so` and a native `libmain.so` cannot be loaded side by side.

## Gradle Dependency

**Kotlin Multiplatform / Android / native:**

```kotlin
implementation("cn.enaium.imgui:imgui-kmp:1.0.6")
```

**JVM:** the right native binary is resolved automatically — the `imgui-kmp-jvm` artifact pulls in the matching `:jni-jvm-*` sibling on the classpath:

- `imgui-kmp-jni-jvm-linux-x86_64`
- `imgui-kmp-jni-jvm-linux-aarch64`
- `imgui-kmp-jni-jvm-darwin-x86_64`
- `imgui-kmp-jni-jvm-darwin-aarch64`
- `imgui-kmp-jni-jvm-windows-x86_64`

`NativeLoader` detects `os.name`/`os.arch` at runtime, extracts the matching binary from the classpath to a temp directory, and `System.load`s it. No `java.library.path` setup is required for downstream JVM consumers. On Android the `.so` is loaded from the AAR's `jniLibs` via `System.loadLibrary`.

## Quick Start

```kotlin
import cn.enaium.imgui.ImGui
import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.extensions.implot.ImPlot

fun main() {
    // 1. Create the imgui context and set up the IO
    val context = ImGui.createContext()
    val io = ImGui.getIO()
    io.displaySize = ImVec2(1280f, 800f)   // set from your window every frame
    io.deltaTime = 1f / 60f

    // 2. ImPlot needs an explicit context bound to the imgui context
    ImPlot.setImGuiContext(context)
    val plotContext = ImPlot.createContext()

    // 3. One frame: build the UI, then render the draw data with your backend
    ImGui.newFrame()
    if (ImGui.begin("Hello")) {
        ImGui.text("Hello, imgui-kmp!")
        if (ImPlot.beginPlot("Sine")) {
            ImPlot.setupFinish()
            val xs = FloatArray(100) { it.toFloat() }
            val ys = FloatArray(100) { kotlin.math.sin(it / 10.0).toFloat() }
            ImPlot.plotLine("sin", xs, ys)
            ImPlot.endPlot()
        }
    }
    ImGui.end()
    ImGui.render()

    // 4. ImGui.getDrawData() -> draw the meshes with your renderer
    val drawData = ImGui.getDrawData()

    ImPlot.destroyContext(plotContext)
    ImGui.destroyContext(context)
}
```

### Renderer backends

The draw data is plain vertex/index buffers (20 bytes per vertex: pos, uv, color), so it can be fed into any renderer. The `examples` directory holds six modules written in Kotlin on top of [sdl-kmp](https://github.com/Enaium/sdl-kmp):

- `examples/common` — the shared demo UI (`DemoUi`), the SDL platform backend (`ImGuiSdlBackend`) and the reusable `SdlRendererApp` frame-loop bootstrap used by the renderer-based examples.
- `examples/android-sdl` — the vendored `org.libsdl.app` Android glue (`SDLActivity`, ...) shared by every example Android app.
- `examples/sdl_renderer` — **SDL renderer** (`ImGuiSdlRendererBackend`), mirrors `imgui_impl_sdlrenderer3.cpp` using `SDL_RenderGeometry`.
- `examples/sdl_gpu` — **SDL GPU** (`ImGuiSdlGpuBackend`), mirrors `imgui_impl_sdlgpu3.cpp` using the SDL3 GPU API with the precompiled SPIR-V/MSL shaders shipped with Dear ImGui.
- `examples/node_editor` — a blueprints-style node graph on the [imgui-node-editor](https://github.com/thedmd/imgui-node-editor) bindings: draggable typed pins, link creation, deletion and context menus.
- `examples/club` — the [imgui_club](https://github.com/ocornut/imgui_club) bindings: the `MemoryEditor` hex editor and the `MultiContextCompositor` stacking a second ImGui context over the main UI in one window, with cross-context drag & drop (drag a swatch from the main window onto the overlay — a different ImGui context — and the compositor delivers the payload there).
- `examples/implot3d` — [ImPlot3D](https://github.com/brenocq/implot3d) 3D plots in a ShowDemoWindow-style layout: line/scatter/surface/mesh demos, box scale & rotation, markers & text, NaN handling.
- `examples/filedialog` — [ImGuiFileDialog](https://github.com/aiekick/ImGuiFileDialog): open/save/directory dialogs with filter collections, per-extension file styles and selection reporting.
- `examples/colortextedit` — [ImGuiColorTextEdit](https://github.com/goossens/ImGuiColorTextEdit): a syntax-highlighted text editor with language/palette switching, find/replace, markers and view toggles, plus the extras (TrieAutoComplete, notifications) and event hooks for language-server integration.
- `examples/markdown` — [imgui_markdown](https://github.com/enkisoftware/imgui_markdown) rendered directly into a window (headings, lists, code, clickable links with tooltips).

Run them headless or with a window:

```bash
# JVM (SDL renderer)
SDL_VIDEO_DRIVER=dummy ./gradlew :examples:sdl_renderer:jvmRun --args="--frames 120"

# JVM (SDL GPU, needs a real display)
./gradlew :examples:sdl_gpu:jvmRun --args="--frames 120"

# Native macOS (headless)
SDL_VIDEO_DRIVER=dummy IMGUI_KMP_FRAMES=120 ./examples/sdl_renderer/build/bin/macosArm64/debugExecutable/sdl_renderer.kexe

# JVM (node editor)
SDL_VIDEO_DRIVER=dummy ./gradlew :examples:node_editor:jvmRun --args="--frames 120"

# JVM (imgui_club: memory editor + multi-context compositor)
SDL_VIDEO_DRIVER=dummy ./gradlew :examples:club:jvmRun --args="--frames 120"

# JVM (implot3d / filedialog / colortextedit / markdown)
SDL_VIDEO_DRIVER=dummy ./gradlew :examples:implot3d:jvmRun --args="--frames 120"
SDL_VIDEO_DRIVER=dummy ./gradlew :examples:filedialog:jvmRun --args="--frames 120"
SDL_VIDEO_DRIVER=dummy ./gradlew :examples:colortextedit:jvmRun --args="--frames 120"
SDL_VIDEO_DRIVER=dummy ./gradlew :examples:markdown:jvmRun --args="--frames 120"
```

The examples consume `:imgui-kmp` as a project dependency, so they always build against the local source. (Standalone consumers use `cn.enaium.imgui:imgui-kmp:1.0.6` from Maven Central or Maven Local.)

## API Overview

The binding mirrors the C++ API closely. Widgets take mutable arrays for out-parameters (like `BooleanArray` for `checkbox`), matching the pointer semantics of Dear ImGui:

```kotlin
val value = FloatArray(1) { 0.5f }
if (ImGui.sliderFloat("value", value, 0f, 1f)) { /* value[0] changed */ }

val checked = BooleanArray(1) { true }
ImGui.checkbox("checked", checked)

val current = IntArray(1)
ImGui.combo("fruit", current, arrayOf("Apple", "Banana", "Cherry"))

val text = ImGui.inputText("name", "hello")  // returns the buffer content
```

Style colors and flags are exposed as constants:

```kotlin
ImGui.pushStyleColor(ImGuiCol.BUTTON, ImVec4(0.4f, 0.2f, 0.6f, 1f))
ImGui.popStyleColor()

ImGui.begin("window", flags = ImGuiWindowFlags.NO_SAVED_SETTINGS)
```

Plot items accept an `ImPlotSpec` for per-item styling:

```kotlin
ImPlot.plotScatter("points", xs, ys, ImPlotSpec(marker = ImPlotMarker.CIRCLE, markerSize = 4f))
ImPlot.plotBars("bars", values, barSize = 0.6)
ImPlot.plotHistogram("distribution", values)
ImPlot.pushColormap(ImPlotColormap.PLASMA)
```

The complete demo UI (menu bar, tabs, tables, popups, plots, the built-in `showDemoWindow` / `ImPlot.showDemoWindow`) lives in `examples/common/src/commonMain/kotlin/cn/enaium/imgui/example/common/DemoUi.kt`.

## Building

The Dear ImGui, ImPlot, ImPlot3D, imgui-node-editor, ImGuiFileDialog, ImGuiColorTextEdit, imgui_markdown and imgui_club sources are git submodules under `includes/`:

```bash
git submodule update --init --recursive
```

- `jni/` — CMake build of the static library (`libimgui.a`) and the JNI bridge, plus the per-OS/arch JVM JNI artifact projects.
- `jni/c_api/` — the C API consumed by both the JNI bridge and the cinterop bindings.
- `imgui-kmp/` — the multiplatform module: `cn.enaium.imgui` for ImGui, `cn.enaium.imgui.extensions.implot` for ImPlot, `...extensions.implot3d` for ImPlot3D, `...extensions.nodeeditor` for imgui-node-editor, `...extensions.filedialog` for ImGuiFileDialog, `...extensions.colortextedit` for ImGuiColorTextEdit (TextEditor + TrieAutoComplete + Notifications + event hooks), `...extensions.markdown` for imgui_markdown and `...extensions.memoryeditor` / `...extensions.mcc` / `...extensions.threadedrendering` for imgui_club, plus the SDL backends under `cn.enaium.imgui.backends.sdl`.
- `examples/` — the shared demo UI (`examples/common`), the shared SDL Android glue (`examples/android-sdl`) and the SDL renderer (`examples/sdl_renderer`), SDL GPU (`examples/sdl_gpu`), node editor (`examples/node_editor`), imgui_club (`examples/club`), ImPlot3D (`examples/implot3d`), file dialog (`examples/filedialog`), text editor (`examples/colortextedit`) and Markdown (`examples/markdown`) demos, shared across JVM, desktop native and Android native targets.

```bash
./gradlew :imgui-kmp:jvmTest          # JVM tests (uses the host JNI artifact)
./gradlew :imgui-kmp:macosArm64Test   # native tests
./gradlew :imgui-kmp:buildAndroidJniLibs  # all four Android ABI .so files
./gradlew :imgui-kmp:publishToMavenLocal # publish everything to Maven Local
```

## License

MIT, see [LICENSE](LICENSE). All bundled libraries (Dear ImGui, ImPlot, imgui-node-editor, ImGuiFileDialog, imgui_club) are MIT-licensed as well.
