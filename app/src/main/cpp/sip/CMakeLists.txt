# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

set(CMAKE_C_FLAGS "-DHAVE_CONFIG_H -DENABLE_TRACE -D__ANDROID_API__=17")

if(${ANDROID_ABI} STREQUAL "armeabi-v7a")
    include_directories(${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi)
elseif(${ANDROID_ABI} STREQUAL "arm64-v8a")
    include_directories(${ANDROID_SYSROOT}/usr/include/aarch64-linux-android)
else()
    include_directories(${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi)
endif()

include_directories(${CMAKE_CURRENT_SOURCE_DIR}
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/include
                    ${CMAKE_CURRENT_SOURCE_DIR}/exosip
                    ${CMAKE_CURRENT_SOURCE_DIR}/exosip/include
                    ${CMAKE_CURRENT_SOURCE_DIR}/cares)



set(OSIP2_SRC   ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osip2/fsm_misc.c
                ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osip2/ict.c
                ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osip2/ict_fsm.c
                ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osip2/ist.c
                ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osip2/ist_fsm.c
                ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osip2/nict.c
                ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osip2/nict_fsm.c
                ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osip2/nist.c
                ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osip2/nist_fsm.c
                ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osip2/osip.c
                ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osip2/osip_dialog.c
                ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osip2/osip_event.c
                ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osip2/osip_time.c
                ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osip2/osip_transaction.c
                ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osip2/port_condv.c
                ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osip2/port_fifo.c
                ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osip2/port_sema.c
                ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osip2/port_thread.c)

set(OSIPPARSER2_SRC ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_proxy_authorization.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_cseq.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_record_route.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_route.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_to.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_from.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_uri.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_authorization.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_header.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_www_authenticate.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_via.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_body.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_md5c.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_message.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_list.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_call_id.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_message_parse.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_contact.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_message_to_str.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_content_length.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_parser_cfg.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_content_type.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_proxy_authenticate.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_mime_version.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_port.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_call_info.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_content_disposition.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_accept_encoding.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_content_encoding.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_authentication_info.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_proxy_authentication_info.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_accept_language.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_accept.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_alert_info.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_error_info.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/osip_allow.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/sdp_accessor.c
                    ${CMAKE_CURRENT_SOURCE_DIR}/osip/src/osipparser2/sdp_message.c)

set(CARES_SRC   ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares__close_sockets.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares__get_hostent.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares__read_line.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares__timeval.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_android.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_cancel.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_data.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_destroy.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_expand_name.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_expand_string.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_fds.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_free_hostent.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_free_string.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_getenv.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_gethostbyaddr.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_gethostbyname.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_getnameinfo.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_getsock.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_init.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_library_init.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_llist.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_mkquery.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_create_query.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_nowarn.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_options.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_parse_a_reply.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_parse_aaaa_reply.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_parse_mx_reply.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_parse_naptr_reply.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_parse_ns_reply.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_parse_ptr_reply.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_parse_soa_reply.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_parse_srv_reply.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_parse_txt_reply.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_platform.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_process.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_query.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_search.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_send.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_strcasecmp.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_strdup.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_strerror.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_timeout.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_version.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/ares_writev.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/bitncmp.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/inet_net_pton.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/inet_ntop.c
                ${CMAKE_CURRENT_SOURCE_DIR}/cares/windows_port.c)

set(EXOSIP_SRC  ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/eXosip.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/eXconf.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/eXregister_api.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/eXcall_api.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/eXmessage_api.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/eXtransport.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/jrequest.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/jresponse.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/jcallback.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/jdialog.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/udp.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/jcall.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/jreg.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/eXutils.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/jevents.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/misc.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/jpipe.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/jpipe.h
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/jauth.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/eXtransport.h
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/eXosip2.h
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/eXtl_udp.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/eXtl_tcp.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/eXtl_dtls.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/eXtl_tls.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/milenage.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/rijndael.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/milenage.h
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/rijndael.h
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/eXsubscription_api.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/eXoptions_api.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/eXinsubscription_api.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/eXpublish_api.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/jnotify.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/jsubscribe.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/inet_ntop.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/inet_ntop.h
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/jpublish.c
                ${CMAKE_CURRENT_SOURCE_DIR}/exosip/src/sdp_offans.c)

add_library( # Sets the name of the library.
             sip

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # osip2 source files
             ${OSIP2_SRC}

             # osipparse source files
             ${OSIPPARSER2_SRC}

             # c-ares source files
             ${CARES_SRC}

             # exosip source file
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
                       sip

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )

