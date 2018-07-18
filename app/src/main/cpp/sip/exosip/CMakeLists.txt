# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

# cmake_minimum_required(VERSION 3.4.1)

# if(${ANDROID_ABI} STREQUAL "armeabi-v7a")
#     include_directories(${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi)
# elseif(${ANDROID_ABI} STREQUAL "arm64-v8a")
#     include_directories(${ANDROID_SYSROOT}/usr/include/aarch64-linux-android)
# else()
#     include_directories(${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi)
# endif()

set(EXOSIP_CUR_DIR ${CMAKE_SOURCE_DIR}/src/main/cpp/sip/exosip)

# include_directories(${EXOSIP_CUR_DIR}
#                     ${EXOSIP_CUR_DIR}/include
#                     ${EXOSIP_CUR_DIR}/../osip/include)

set(EXOSIP_INC ${EXOSIP_CUR_DIR}
               ${EXOSIP_CUR_DIR}/include)

set(EXOSIP_SRC  ${EXOSIP_CUR_DIR}/src/eXosip.c
                ${EXOSIP_CUR_DIR}/src/eXconf.c
                ${EXOSIP_CUR_DIR}/src/eXregister_api.c
                ${EXOSIP_CUR_DIR}/src/eXcall_api.c
                ${EXOSIP_CUR_DIR}/src/eXmessage_api.c
                ${EXOSIP_CUR_DIR}/src/eXtransport.c
                ${EXOSIP_CUR_DIR}/src/jrequest.c
                ${EXOSIP_CUR_DIR}/src/jresponse.c
                ${EXOSIP_CUR_DIR}/src/jcallback.c
                ${EXOSIP_CUR_DIR}/src/jdialog.c
                ${EXOSIP_CUR_DIR}/src/udp.c
                ${EXOSIP_CUR_DIR}/src/jcall.c
                ${EXOSIP_CUR_DIR}/src/jreg.c
                ${EXOSIP_CUR_DIR}/src/eXutils.c
                ${EXOSIP_CUR_DIR}/src/jevents.c
                ${EXOSIP_CUR_DIR}/src/misc.c
                ${EXOSIP_CUR_DIR}/src/jpipe.c
                ${EXOSIP_CUR_DIR}/src/jpipe.h
                ${EXOSIP_CUR_DIR}/src/jauth.c
                ${EXOSIP_CUR_DIR}/src/eXtransport.h
                ${EXOSIP_CUR_DIR}/src/eXosip2.h
                ${EXOSIP_CUR_DIR}/src/eXtl_udp.c
                ${EXOSIP_CUR_DIR}/src/eXtl_tcp.c
                ${EXOSIP_CUR_DIR}/src/eXtl_dtls.c
                ${EXOSIP_CUR_DIR}/src/eXtl_tls.c
                ${EXOSIP_CUR_DIR}/src/milenage.c
                ${EXOSIP_CUR_DIR}/src/rijndael.c
                ${EXOSIP_CUR_DIR}/src/milenage.h
                ${EXOSIP_CUR_DIR}/src/rijndael.h
                ${EXOSIP_CUR_DIR}/src/eXsubscription_api.c
                ${EXOSIP_CUR_DIR}/src/eXoptions_api.c
                ${EXOSIP_CUR_DIR}/src/eXinsubscription_api.c
                ${EXOSIP_CUR_DIR}/src/eXpublish_api.c
                ${EXOSIP_CUR_DIR}/src/jnotify.c
                ${EXOSIP_CUR_DIR}/src/jsubscribe.c
                ${EXOSIP_CUR_DIR}/src/inet_ntop.c
                ${EXOSIP_CUR_DIR}/src/inet_ntop.h
                ${EXOSIP_CUR_DIR}/src/jpublish.c
                ${EXOSIP_CUR_DIR}/src/sdp_offans.c)


# add_library ( # Sets the name of the library.
#               exosip
#
#               # Sets the library as a static library.
#               STATIC
#
#               # Provides a relative path to your source file(s).
#               ${EXOSIP_SRC})


# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

# find_library( # Sets the name of the path variable.
#               log-lib
#
#               # Specifies the name of the NDK library that
#               # you want CMake to locate.
#               log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

# target_link_libraries( # Specifies the target library.
#                        exosip
#
#                        # Links the target library to the log library
#                        # included in the NDK.
#                        ${log-lib})
