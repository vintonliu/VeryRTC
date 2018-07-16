# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

# cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_C_FLAGS "-Wno-incompatible-pointer-types-discards-qualifiers")
ADD_DEFINITIONS(-DHAVE_EXOSIP_DSCP -DHAVE_EXOSIP_GET_VERSION)

if(${ANDROID_ABI} STREQUAL "armeabi-v7a")
    include_directories(${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi)
elseif(${ANDROID_ABI} STREQUAL "arm64-v8a")
    include_directories(${ANDROID_SYSROOT}/usr/include/aarch64-linux-android)
else()
    include_directories(${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi)
endif()

set(COREAPI_CUR_DIR ${CMAKE_SOURCE_DIR}/src/main/cpp/sip/coreapi)

include_directories(${COREAPI_CUR_DIR}
                    ${COREAPI_CUR_DIR}/../exosip/include
                    ${COREAPI_CUR_DIR}/../osip/include)

set(COREAPI_SRC  ${COREAPI_CUR_DIR}/address.c
                 ${COREAPI_CUR_DIR}/authentication.c
                 ${COREAPI_CUR_DIR}/callbacks.c
                 ${COREAPI_CUR_DIR}/linphonecall.c
                 ${COREAPI_CUR_DIR}/linphonecore.c
                 ${COREAPI_CUR_DIR}/logging.c
                 ${COREAPI_CUR_DIR}/misc.c
                 ${COREAPI_CUR_DIR}/mscommon.c
                 ${COREAPI_CUR_DIR}/offeranswer.c
                 ${COREAPI_CUR_DIR}/payloadtype.c
                 ${COREAPI_CUR_DIR}/port.c
                 ${COREAPI_CUR_DIR}/proxy.c
                 ${COREAPI_CUR_DIR}/sal_api.c
                 ${COREAPI_CUR_DIR}/sal_eXosip2.c
                 ${COREAPI_CUR_DIR}/sal_eXosip2_presence.c
                 ${COREAPI_CUR_DIR}/sal_eXosip2_sdp.c)

add_library ( # Sets the name of the library.
              coreapi

              # Sets the library as a static library.
              STATIC

              # Provides a relative path to your source file(s).
              ${COREAPI_SRC})


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
                       coreapi

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib})
