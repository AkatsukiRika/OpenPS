# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("openps")

set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/lib/opencv-mobile-4.10.0-android/sdk/native/jni)
find_package(OpenCV REQUIRED)

set(TFLite_ROOT ${CMAKE_SOURCE_DIR}/lib/tensorflow-lite-2.16.1)
include_directories(${TFLite_ROOT}/headers)

set(ONNXRuntime_ROOT ${CMAKE_SOURCE_DIR}/lib/onnxruntime-android-1.20.0)
include_directories(${ONNXRuntime_ROOT}/headers)

set(MNN_ROOT ${CMAKE_SOURCE_DIR}/lib/mnn-2.9.0-android)
include_directories(${MNN_ROOT}/include)

if (${CMAKE_ANDROID_ARCH_ABI} STREQUAL "armeabi-v7a")
    set(TFLite_LIB_DIR ${TFLite_ROOT}/jni/armeabi-v7a)
    set(ONNXRuntime_LIB_DIR ${ONNXRuntime_ROOT}/jni/armeabi-v7a)
    set(MNN_LIB_DIR ${MNN_ROOT}/jni/armeabi-v7a)
elseif (${CMAKE_ANDROID_ARCH_ABI} STREQUAL "arm64-v8a")
    set(TFLite_LIB_DIR ${TFLite_ROOT}/jni/arm64-v8a)
    set(ONNXRuntime_LIB_DIR ${ONNXRuntime_ROOT}/jni/arm64-v8a)
    set(MNN_LIB_DIR ${MNN_ROOT}/jni/arm64-v8a)
else()
    message(FATAL_ERROR "Unsupported ABI: ${ANDROID_ABI}")
endif()

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
#
# In this top level CMakeLists.txt, ${CMAKE_PROJECT_NAME} is used to define
# the target library name; in the sub-module's CMakeLists.txt, ${PROJECT_NAME}
# is preferred for the same purpose.
#
# In order to load a library into your app from Java/Kotlin, you must call
# System.loadLibrary() and pass the name of the library defined here;
# for GameActivity/NativeActivity derived applications, the same library name must be
# used in the AndroidManifest.xml file.
add_library(${CMAKE_PROJECT_NAME} SHARED
        # List C/C++ source files with relative paths to this CMakeLists.txt.
        NativeLib.cpp
        model/SkinModelProcessor.cpp
        model/InpaintModelProcessor.cpp
        cv/CvLoader.cpp
        cv/InpaintLoader.cpp
        cv/CvUtils.cpp)

add_library(tflite SHARED IMPORTED)
set_target_properties(tflite PROPERTIES IMPORTED_LOCATION ${TFLite_LIB_DIR}/libtensorflowlite_jni.so)

add_library(onnxruntime SHARED IMPORTED)
set_target_properties(onnxruntime PROPERTIES IMPORTED_LOCATION ${ONNXRuntime_LIB_DIR}/libonnxruntime.so)

add_library(c++_shared SHARED IMPORTED)
set_target_properties(c++_shared PROPERTIES IMPORTED_LOCATION ${MNN_LIB_DIR}/libc++_shared.so)

add_library(MNN SHARED IMPORTED)
set_target_properties(MNN PROPERTIES IMPORTED_LOCATION ${MNN_LIB_DIR}/libMNN.so)

# Specifies libraries CMake should link to your target library. You
# can link libraries from various origins, such as libraries defined in this
# build script, prebuilt third-party libraries, or Android system libraries.
target_link_libraries(${CMAKE_PROJECT_NAME}
        # List libraries link to the target library
        android
        log
        jnigraphics
        c++_shared
        MNN
        tflite
        onnxruntime
        ${OpenCV_LIBS})