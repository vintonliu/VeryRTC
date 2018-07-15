# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

# cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_C_FLAGS "-DHAVE_CONFIG_H -DENABLE_TRACE -D__ANDROID_API__=17")

if(${ANDROID_ABI} STREQUAL "armeabi-v7a")
    include_directories(${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi)
elseif(${ANDROID_ABI} STREQUAL "arm64-v8a")
    include_directories(${ANDROID_SYSROOT}/usr/include/aarch64-linux-android)
else()
    include_directories(${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi)
endif()


include_directories(${CMAKE_CURRENT_SOURCE_DIR}
                    ${CMAKE_CURRENT_SOURCE_DIR}/src
                    ${CMAKE_CURRENT_SOURCE_DIR}/include
                    ${CMAKE_CURRENT_SOURCE_DIR}/src/osip2
                    ${CMAKE_CURRENT_SOURCE_DIR}/src/osipparser2)

set(OSIP2_SRC   src/osip2/fsm_misc.c
                src/osip2/ict.c
                src/osip2/ict_fsm.c
                src/osip2/ist.c
                src/osip2/ist_fsm.c
                src/osip2/nict.c
                src/osip2/nict_fsm.c
                src/osip2/nist.c
                src/osip2/nist_fsm.c
                src/osip2/osip.c
                src/osip2/osip_dialog.c
                src/osip2/osip_event.c
                src/osip2/osip_time.c
                src/osip2/osip_transaction.c
                src/osip2/port_condv.c
                src/osip2/port_fifo.c
                src/osip2/port_sema.c
                src/osip2/port_thread.c)

set(OSIPPARSER2_SRC src/osipparser2/osip_proxy_authorization.c
                    src/osipparser2/osip_cseq.c
                    src/osipparser2/osip_record_route.c
                    src/osipparser2/osip_route.c
                    src/osipparser2/osip_to.c
                    src/osipparser2/osip_from.c
                    src/osipparser2/osip_uri.c
                    src/osipparser2/osip_authorization.c
                    src/osipparser2/osip_header.c
                    src/osipparser2/osip_www_authenticate.c
                    src/osipparser2/osip_via.c
                    src/osipparser2/osip_body.c
                    src/osipparser2/osip_md5c.c
                    src/osipparser2/osip_message.c
                    src/osipparser2/osip_list.c
                    src/osipparser2/osip_call_id.c
                    src/osipparser2/osip_message_parse.c
                    src/osipparser2/osip_contact.c
                    src/osipparser2/osip_message_to_str.c
                    src/osipparser2/osip_content_length.c
                    src/osipparser2/osip_parser_cfg.c
                    src/osipparser2/osip_content_type.c
                    src/osipparser2/osip_proxy_authenticate.c
                    src/osipparser2/osip_mime_version.c
                    src/osipparser2/osip_port.c
                    src/osipparser2/osip_call_info.c
                    src/osipparser2/osip_content_disposition.c
                    src/osipparser2/osip_accept_encoding.c
                    src/osipparser2/osip_content_encoding.c
                    src/osipparser2/osip_authentication_info.c
                    src/osipparser2/osip_proxy_authentication_info.c
                    src/osipparser2/osip_accept_language.c
                    src/osipparser2/osip_accept.c
                    src/osipparser2/osip_alert_info.c
                    src/osipparser2/osip_error_info.c
                    src/osipparser2/osip_allow.c
                    src/osipparser2/sdp_accessor.c
                    src/osipparser2/sdp_message.c)

add_library ( # Sets the name of the library.
              libosip2

              # Sets the library as a static library.
              SHARED

              # Provides a relative path to your source file(s).
              # osip2 source files
              ${OSIP2_SRC}

              # osipparse source files
              ${OSIPPARSER2_SRC})


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
                       libosip2

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib})