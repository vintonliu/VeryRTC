//
// Created by dell on 0018,2018/7/18.
//

#ifndef VERYRTC_JNI_HELPER_H
#define VERYRTC_JNI_HELPER_H

#include <jni.h>
#include <android/log.h>
#include <string>


#ifndef NDEBUG
#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "SipJni", __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, "SipJni", __VA_ARGS__))
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, "SipJni", __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, "SipJni", __VA_ARGS__))
#else
#define LOGI(...)
#define LOGE(...)
#define LOGD(...)
#define LOGW(...)
#endif


namespace mrtc {
jint InitGlobalJniVariables(JavaVM *jvm);
JNIEnv* GetEnv();
bool IsNull(JNIEnv* jni, jobject obj);
jobject NewGlobalRef(JNIEnv* jni, jobject o);
void DeleteGlobalRef(JNIEnv* jni, jobject o);
jclass GetObjectClass(JNIEnv* jni, jobject object);
jfieldID GetFieldID(JNIEnv* jni, jclass c, const char* name, const char* signature);
jmethodID GetMethodID(JNIEnv* jni, jclass c, const std::string& name, const char* signature);
jobject GetObjectField(JNIEnv* jni, jobject object, jfieldID id);
jint GetIntField(JNIEnv* jni, jobject object, jfieldID id);
bool GetBooleanField(JNIEnv* jni, jobject object, jfieldID id);
jlong GetLongField(JNIEnv* jni, jobject object, jfieldID id);
jclass GetObjectClass(JNIEnv* jni, jobject object);
jstring JavaStringFromStdString(JNIEnv* jni, const std::string& native);
std::string JavaToStdString(JNIEnv* jni, const jstring& j_string);
JNIEnv* AttachCurrentThreadIfNeeded();
void ThreadDestructor(JNIEnv *jni);

class ScopedJni {
public:
    ScopedJni() {
        jni_ = AttachCurrentThreadIfNeeded();
    }

    ~ScopedJni() {
        ThreadDestructor(jni_);
    }

    JNIEnv *GetEnv() { return jni_; }

private:
    JNIEnv *jni_;
};
} /* namespace mrtc */
#endif //VERYRTC_JNI_HELPER_H
