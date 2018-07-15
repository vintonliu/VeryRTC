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


include_directories(${CMAKE_CURRENT_SOURCE_DIR})

set(CARES_SRC   ares__close_sockets.c
                ares__get_hostent.c
                ares__read_line.c
                ares__timeval.c
                ares_android.c
                ares_cancel.c
                ares_data.c
                ares_destroy.c
                ares_expand_name.c
                ares_expand_string.c
                ares_fds.c
                ares_free_hostent.c
                ares_free_string.c
                ares_getenv.c
                ares_gethostbyaddr.c
                ares_gethostbyname.c
                ares_getnameinfo.c
                ares_getsock.c
                ares_init.c
                ares_library_init.c
                ares_llist.c
                ares_mkquery.c
                ares_create_query.c
                ares_nowarn.c
                ares_options.c
                ares_parse_a_reply.c
                ares_parse_aaaa_reply.c
                ares_parse_mx_reply.c
                ares_parse_naptr_reply.c
                ares_parse_ns_reply.c
                ares_parse_ptr_reply.c
                ares_parse_soa_reply.c
                ares_parse_srv_reply.c
                ares_parse_txt_reply.c
                ares_platform.c
                ares_process.c
                ares_query.c
                ares_search.c
                ares_send.c
                ares_strcasecmp.c
                ares_strdup.c
                ares_strerror.c
                ares_timeout.c
                ares_version.c
                ares_writev.c
                bitncmp.c
                inet_net_pton.c
                inet_ntop.c
                windows_port.c)


add_library ( # Sets the name of the library.
              libcares

              # Sets the library as a static library.
              SHARED

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
                       libcares

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib})