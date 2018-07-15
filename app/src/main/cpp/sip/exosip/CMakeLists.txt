# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

# cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_C_FLAGS "-DHAVE_CONFIG_H")

if(${ANDROID_ABI} STREQUAL "armeabi-v7a")
    include_directories(${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi)
elseif(${ANDROID_ABI} STREQUAL "arm64-v8a")
    include_directories(${ANDROID_SYSROOT}/usr/include/aarch64-linux-android)
else()
    include_directories(${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi)
endif()


include_directories(${CMAKE_CURRENT_SOURCE_DIR}
                    ${CMAKE_CURRENT_SOURCE_DIR}/include
                    ${CMAKE_CURRENT_SOURCE_DIR}/../osip/include)

set(EXOSIP_SRC  src/eXosip.c
                src/eXconf.c
                src/eXregister_api.c
                src/eXcall_api.c
                src/eXmessage_api.c
                src/eXtransport.c
                src/jrequest.c
                src/jresponse.c
                src/jcallback.c
                src/jdialog.c
                src/udp.c
                src/jcall.c
                src/jreg.c
                src/eXutils.c
                src/jevents.c
                src/misc.c
                src/jpipe.c
                src/jpipe.h
                src/jauth.c
                src/eXtransport.h
                src/eXosip2.h
                src/eXtl_udp.c
                src/eXtl_tcp.c
                src/eXtl_dtls.c
                src/eXtl_tls.c
                src/milenage.c
                src/rijndael.c
                src/milenage.h
                src/rijndael.h
                src/eXsubscription_api.c
                src/eXoptions_api.c
                src/eXinsubscription_api.c
                src/eXpublish_api.c
                src/jnotify.c
                src/jsubscribe.c
                src/inet_ntop.c
                src/inet_ntop.h
                src/jpublish.c
                src/sdp_offans.c)


add_library ( # Sets the name of the library.
              libexosip

              # Sets the library as a static library.
              SHARED

              # Provides a relative path to your source file(s).
              ${EXOSIP_SRC})


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
                       libexosip

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib}
                       libosip
                       libcares)

ADD_SUBDIRECTORY(${CMAKE_CURRENT_SOURCE_DIR}/../osip)
ADD_SUBDIRECTORY(src/main/cpp/sip/cares)