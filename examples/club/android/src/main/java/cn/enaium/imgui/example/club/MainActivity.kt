package cn.enaium.imgui.example.club

import org.libsdl.app.SDLActivity

/**
 * Launcher activity hosting the imgui_club example.
 *
 * SDLActivity loads the app's shared library and calls its exported `SDL_main`
 * symbol on a dedicated SDL thread. libmain.so (built from the example KMP
 * module's androidNativeMain) already links SDL3 statically, so only "main"
 * needs to be loaded.
 */
class MainActivity : SDLActivity() {

    override fun getLibraries(): Array<String> = arrayOf("main")
}
