cmake_minimum_required(VERSION 3.16)

project(imgui C CXX)
set(CMAKE_CXX_STANDARD 17)

# Builds the JNI shared library by default. Native (cinterop) targets build
# only the static library by passing -DBUILD_JNI=OFF.
option(BUILD_JNI "Build the JNI shared library" ON)

set(IMGUI_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../includes/imgui)
set(IMPLOT_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../includes/implot)
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
    ${C_API_DIR}/imgui_c.cc
    ${C_API_DIR}/implot_c.cc
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
