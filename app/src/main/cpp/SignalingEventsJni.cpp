//
// Created by dell on 0018,2018/7/18.
//

#include "SignalingEventsJni.h"
#include "jni_helper.h"

namespace mrtc {

SignalingEventsJni::SignalingEventsJni(JNIEnv *jni, jobject j_observer) {
    LOGI("%s ctor", __FUNCTION__);
    j_observer_global_ = NewGlobalRef(jni, j_observer);
    if (j_observer_global_) {
        j_observer_class_ = (jclass) NewGlobalRef(jni, GetObjectClass(jni, j_observer_global_));
    }
    LOGI("%s Leave", __FUNCTION__);
}

SignalingEventsJni::~SignalingEventsJni() {
    LOGI("%s ~dtor", __FUNCTION__);

    ScopedJni jni;

    if (jni.GetEnv()) {
        if (j_observer_global_) {
            DeleteGlobalRef(jni.GetEnv(), j_observer_global_);
            j_observer_global_ = nullptr;
        }
        if (j_observer_class_) {
            DeleteGlobalRef(jni.GetEnv(), j_observer_class_);
            j_observer_class_ = nullptr;
        }
    }

    LOGI("%s Leave", __FUNCTION__);
}

void SignalingEventsJni::onRegistered(bool registered) {
    LOGI("%s registered = %s", __FUNCTION__, registered ? "true" : "false");

    ScopedJni jni;
    jmethodID m = GetMethodID(jni.GetEnv(), j_observer_class_,
                              "onRegistered", "(Z)V");
    jni.GetEnv()->CallVoidMethod(j_observer_global_, m, registered);
}

void SignalingEventsJni::onRegisterFailure(RegisterReason reason) {
    LOGI("%s reason = %d", __FUNCTION__, reason);
    ScopedJni jni;
    jmethodID m = GetMethodID(jni.GetEnv(), j_observer_class_,
                              "onRegisterFailure", "()V");
    jni.GetEnv()->CallVoidMethod(j_observer_global_, m);
}

void SignalingEventsJni::onCallProcess() {
    ScopedJni jni;
    jmethodID m = GetMethodID(jni.GetEnv(), j_observer_class_,
                              "onCallProcess", "()V");
    jni.GetEnv()->CallVoidMethod(j_observer_global_, m);
}

void SignalingEventsJni::onCallRinging() {
    ScopedJni jni;
    jmethodID m = GetMethodID(jni.GetEnv(), j_observer_class_,
                              "onCallRinging", "()V");
    jni.GetEnv()->CallVoidMethod(j_observer_global_, m);
}

void SignalingEventsJni::onCallConnected(const SignalingParameters &params) {
    ScopedJni jni;
    jmethodID m = GetMethodID(jni.GetEnv(), j_observer_class_,
                              "onCallConnected",
                              "(Ljava/lang/String;)V");
    jstring from = JavaStringFromStdString(jni.GetEnv(), params.from);
    jstring sdp = JavaStringFromStdString(jni.GetEnv(), params.rsdp);
    jni.GetEnv()->CallVoidMethod(j_observer_global_, m, from, sdp);
}

void SignalingEventsJni::onCallIncoming(const SignalingParameters &params) {
    ScopedJni jni;
    jmethodID m = GetMethodID(jni.GetEnv(), j_observer_class_,
                              "onCallIncoming",
                              "(Ljava/lang/String;Ljava/lang/String;)V");
    jstring sdp = JavaStringFromStdString(jni.GetEnv(), params.rsdp);
    jni.GetEnv()->CallVoidMethod(j_observer_global_, m, sdp);
}

void SignalingEventsJni::onCallEnded() {
    ScopedJni jni;
    jmethodID m = GetMethodID(jni.GetEnv(), j_observer_class_,
                              "onCallEnded", "()V");
    jni.GetEnv()->CallVoidMethod(j_observer_global_, m);
}

void SignalingEventsJni::onCallFailure(CallReason reason) {
    ScopedJni jni;
    jmethodID m = GetMethodID(jni.GetEnv(), j_observer_class_,
                              "onCallFailure", "(I)V");
    jni.GetEnv()->CallVoidMethod(j_observer_global_, m, (int)reason);
}

void SignalingEventsJni::onRemoteDescription(const std::string &sdp) {
//    ScopedJni jni;
//    jmethodID m = GetMethodID(jni.GetEnv(), j_observer_class_,
//                              "onCallEnded", "()V");
//    jni.GetEnv()->CallVoidMethod(j_observer_global_, m);
}

void SignalingEventsJni::onRemoteIceCandidate(const std::string &candidate) {
    ScopedJni jni;
    jmethodID m = GetMethodID(jni.GetEnv(), j_observer_class_,
                              "onRemoteIceCandidate", "(Ljava/lang/String;)V");
    jstring jcandidate = JavaStringFromStdString(jni.GetEnv(), candidate);
    jni.GetEnv()->CallVoidMethod(j_observer_global_, m, jcandidate);
}

void SignalingEventsJni::onRemoteIceCandidatesRemoved(const std::string &candidate) {
//    ScopedJni jni;
//    jmethodID m = GetMethodID(jni.GetEnv(), j_observer_class_,
//                              "onCallEnded", "()V");
//    jni.GetEnv()->CallVoidMethod(j_observer_global_, m);
}

} /* namespace mrtc */