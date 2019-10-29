# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
include_directories(${CMAKE_SOURCE_DIR}/../../../../../include/)

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp
        StyleTransfer.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.


add_library(tflite SHARED IMPORTED)
set_target_properties(
  tflite
  PROPERTIES
    IMPORTED_LOCATION
    ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libtensorflowlite.so)

add_library(tflite_gpu SHARED IMPORTED)
set_target_properties(
        tflite_gpu
        PROPERTIES
        IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libtensorflowlite_gpu_gl.so
)


add_library(lib_opencv SHARED IMPORTED)
set_target_properties(
        lib_opencv
        PROPERTIES
        IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libopencv_java4.so)

target_link_libraries( # Specifies the target library.
        native-lib
        tflite
        lib_opencv

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})