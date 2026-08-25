cmake_minimum_required(VERSION 3.16)

project(imgui C CXX)
set(CMAKE_CXX_STANDARD 17)

# imgui-node-editor's headers require the ImVec2/ImVec4 operators; defining
# this before any imgui include keeps every translation unit consistent.
add_compile_definitions(IMGUI_DEFINE_MATH_OPERATORS)

# On Windows, DLLs are runtime artifacts and ignore CMAKE_LIBRARY_OUTPUT_DIRECTORY
# (which only controls the import library). Route them to the same place so the
# JNI packaging steps can find the artifact.
if(WIN32 AND CMAKE_LIBRARY_OUTPUT_DIRECTORY)
    set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_LIBRARY_OUTPUT_DIRECTORY})
endif()

# Builds the JNI shared library by default. Native (cinterop) targets build
# only the static library by passing -DBUILD_JNI=OFF.
option(BUILD_JNI "Build the JNI shared library" ON)

set(IMGUI_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../includes/imgui)
set(IMPLOT_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../includes/implot)
set(NODE_EDITOR_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../includes/imgui-node-editor)
set(FILE_DIALOG_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../includes/ImGuiFileDialog)
set(MEMORY_EDITOR_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../includes/imgui_club/imgui_memory_editor)
set(CLUB_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../includes/imgui_club)
set(C_API_DIR ${CMAKE_CURRENT_SOURCE_DIR}/c_api)

# ---- imgui + implot static library ----
add_library(imgui STATIC
    ${IMGUI_DIR}/imgui.cpp
    ${IMGUI_DIR}/imgui_demo.cpp
    ${IMGUI_DIR}/imgui_draw.cpp
    ${IMGUI_DIR}/imgui_tables.cpp
    ${IMGUI_DIR}/imgui_widgets.cpp
    ${IMPLOT_DIR}/implot.cpp
    ${IMPLOT_DIR}/implot_items.cpp
    ${IMPLOT_DIR}/implot_demo.cpp
    ${NODE_EDITOR_DIR}/imgui_node_editor.cpp
    ${NODE_EDITOR_DIR}/imgui_node_editor_api.cpp
    ${NODE_EDITOR_DIR}/crude_json.cpp
    ${NODE_EDITOR_DIR}/imgui_canvas.cpp
    ${FILE_DIALOG_DIR}/ImGuiFileDialog.cpp
    ${CLUB_DIR}/imgui_multicontext_compositor/imgui_multicontext_compositor.cpp
    ${C_API_DIR}/imgui_c.cc
    ${C_API_DIR}/implot_c.cc
    ${C_API_DIR}/node_editor_c.cc
    ${C_API_DIR}/file_dialog_c.cc
    ${C_API_DIR}/memory_editor_c.cc
    ${C_API_DIR}/multi_context_compositor_c.cc
    ${C_API_DIR}/threaded_rendering_c.cc
)

target_include_directories(imgui PRIVATE
    ${NODE_EDITOR_DIR}
    ${FILE_DIALOG_DIR}
    ${MEMORY_EDITOR_DIR}
    ${CLUB_DIR}/imgui_multicontext_compositor
    ${CLUB_DIR}/imgui_threaded_rendering
)

target_include_directories(imgui PUBLIC
    ${C_API_DIR}
    ${IMGUI_DIR}
    ${IMPLOT_DIR}
)

if(MSVC)
    target_compile_options(imgui PRIVATE /W3 /EHsc /bigobj)
else()
    target_compile_options(imgui PRIVATE -Wall -Wno-unused-function -fPIC -fexceptions -O2)
endif()

set_target_properties(imgui PROPERTIES OUTPUT_NAME imgui)

# ---- JNI shared library ----
if(BUILD_JNI)
    add_library(imgui_jni SHARED ${CMAKE_CURRENT_SOURCE_DIR}/jni_bridge.cpp)
    set_target_properties(imgui_jni PROPERTIES OUTPUT_NAME imgui_jni)
    if(WIN32)
        # MinGW prefixes DLLs with "lib"; strip it so the artifact is
        # imgui_jni.dll everywhere (NativeLoader expects no prefix).
        set_target_properties(imgui_jni PROPERTIES PREFIX "")
        if(MINGW)
            # Statically link the MinGW runtime so the DLL loads without
            # libstdc++-6.dll/libgcc_s_seh-1.dll/libwinpthread-1.dll on PATH.
            target_link_options(imgui_jni PRIVATE -static -static-libgcc -static-libstdc++)
        endif()
    endif()

    if(ANDROID)
        find_library(log-lib log)
        if(log-lib)
            target_link_libraries(imgui_jni PRIVATE ${log-lib})
        endif()
    elseif(JNI_INCLUDE_DIR)
        target_include_directories(imgui_jni PRIVATE ${JNI_INCLUDE_DIR})
        if(JNI_INCLUDE_DIR_PLATFORM)
            target_include_directories(imgui_jni PRIVATE ${JNI_INCLUDE_DIR_PLATFORM})
        endif()
    else()
        find_package(JNI REQUIRED)
        target_include_directories(imgui_jni PRIVATE ${JNI_INCLUDE_DIRS})
    endif()

    target_link_libraries(imgui_jni PRIVATE imgui)
endif()
