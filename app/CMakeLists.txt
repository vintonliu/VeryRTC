# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

set (CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})
set(NATIVE_DIR ${PROJECT_SOURCE_DIR}/src/main/cpp)

set(CMAKE_C_FLAGS "-O2 -Wno-incompatible-pointer-types-discards-qualifiers")

# if (${CMAKE_BUILD_TYPE} STREQUAL "Release")
# set(CMAKE_C_FLAGS "-O2")
# set(DCMAKE_CXX_FLAGS "-O2")
# endif()

ADD_DEFINITIONS(-DANDROID
                -DHAVE_CONFIG_H
                -DENABLE_TRACE
                -D__ANDROID_API__=17
                -DHAVE_EXOSIP_DSCP
                -DHAVE_EXOSIP_GET_VERSION
                -DDONOTWAIT_ENDOFTRANSACTION)


INCLUDE(src/main/cpp/sip/osip/CMakeLists.txt)
INCLUDE(src/main/cpp/sip/c-ares/CMakeLists.txt)
INCLUDE(src/main/cpp/sip/exosip/CMakeLists.txt)
INCLUDE(src/main/cpp/sip/coreapi/CMakeLists.txt)

include_directories(${NATIVE_DIR}
                    ${NATIVE_DIR}/sip
                    ${OSIP2_INC}
                    ${EXOSIP_INC}
                    ${COREAPI_INC}
                    ${CARES_INC})

add_library( # Sets the name of the library.
             Sip

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # osip2 source files
             ${OSIP2_SRC}

             # osipparse source files
             ${OSIPPARSER2_SRC}

             # c-ares source files
             ${CARES_SRC}

             # exosip source files
             ${EXOSIP_SRC}

             # linphone coreapi files
             ${COREAPI_SRC}

             ${NATIVE_DIR}/SipClient.cpp
             ${NATIVE_DIR}/SipChannelClientJni.cpp
             ${NATIVE_DIR}/SignalingEventsJni.cpp
             ${NATIVE_DIR}/jni_helper.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       Sip

                       # Links the internal static librarys
                       # osip2
                       # cares
                       # exosip
                       # coreapi

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib})

# ADD_SUBDIRECTORY(src/main/cpp/sip)
