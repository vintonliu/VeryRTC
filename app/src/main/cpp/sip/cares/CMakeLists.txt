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

set(CARES_CUR_DIR ${CMAKE_SOURCE_DIR}/src/main/cpp/sip/cares)

include_directories(${CARES_CUR_DIR})

set(CARES_SRC   ${CARES_CUR_DIR}/ares__close_sockets.c
                ${CARES_CUR_DIR}/ares__get_hostent.c
                ${CARES_CUR_DIR}/ares__read_line.c
                ${CARES_CUR_DIR}/ares__timeval.c
                ${CARES_CUR_DIR}/ares_android.c
                ${CARES_CUR_DIR}/ares_cancel.c
                ${CARES_CUR_DIR}/ares_data.c
                ${CARES_CUR_DIR}/ares_destroy.c
                ${CARES_CUR_DIR}/ares_expand_name.c
                ${CARES_CUR_DIR}/ares_expand_string.c
                ${CARES_CUR_DIR}/ares_fds.c
                ${CARES_CUR_DIR}/ares_free_hostent.c
                ${CARES_CUR_DIR}/ares_free_string.c
                ${CARES_CUR_DIR}/ares_getenv.c
                ${CARES_CUR_DIR}/ares_gethostbyaddr.c
                ${CARES_CUR_DIR}/ares_gethostbyname.c
                ${CARES_CUR_DIR}/ares_getnameinfo.c
                ${CARES_CUR_DIR}/ares_getsock.c
                ${CARES_CUR_DIR}/ares_init.c
                ${CARES_CUR_DIR}/ares_library_init.c
                ${CARES_CUR_DIR}/ares_llist.c
                ${CARES_CUR_DIR}/ares_mkquery.c
                ${CARES_CUR_DIR}/ares_create_query.c
                ${CARES_CUR_DIR}/ares_nowarn.c
                ${CARES_CUR_DIR}/ares_options.c
                ${CARES_CUR_DIR}/ares_parse_a_reply.c
                ${CARES_CUR_DIR}/ares_parse_aaaa_reply.c
                ${CARES_CUR_DIR}/ares_parse_mx_reply.c
                ${CARES_CUR_DIR}/ares_parse_naptr_reply.c
                ${CARES_CUR_DIR}/ares_parse_ns_reply.c
                ${CARES_CUR_DIR}/ares_parse_ptr_reply.c
                ${CARES_CUR_DIR}/ares_parse_soa_reply.c
                ${CARES_CUR_DIR}/ares_parse_srv_reply.c
                ${CARES_CUR_DIR}/ares_parse_txt_reply.c
                ${CARES_CUR_DIR}/ares_platform.c
                ${CARES_CUR_DIR}/ares_process.c
                ${CARES_CUR_DIR}/ares_query.c
                ${CARES_CUR_DIR}/ares_search.c
                ${CARES_CUR_DIR}/ares_send.c
                ${CARES_CUR_DIR}/ares_strcasecmp.c
                ${CARES_CUR_DIR}/ares_strdup.c
                ${CARES_CUR_DIR}/ares_strerror.c
                ${CARES_CUR_DIR}/ares_timeout.c
                ${CARES_CUR_DIR}/ares_version.c
                ${CARES_CUR_DIR}/ares_writev.c
                ${CARES_CUR_DIR}/bitncmp.c
                ${CARES_CUR_DIR}/inet_net_pton.c
                ${CARES_CUR_DIR}/inet_ntop.c
                ${CARES_CUR_DIR}/windows_port.c)


add_library ( # Sets the name of the library.
              cares

              # Sets the library as a static library.
              STATIC

              # Provides a relative path to your source file(s).
              ${CARES_SRC})


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
                       cares

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib})