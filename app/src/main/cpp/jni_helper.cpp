//
// Created by dell on 0018,2018/7/18.
//

#include <pthread.h>
#include <string>
#include <vector>
#include "jni_helper.h"


namespace mrtc {
static JavaVM* g_jvm = nullptr;

JavaVM *GetJVM() {
    return g_jvm;
}

// Return a |JNIEnv*| usable on this thread or NULL if this thread is detached.
JNIEnv* GetEnv() {
    void* env = nullptr;
    jint status = g_jvm->GetEnv(&env, JNI_VERSION_1_6);
    if (!(((env != nullptr) && (status == JNI_OK)) ||
        ((env == nullptr) && (status == JNI_EDETACHED)))) {
        LOGE("Unexpected GetEnv return: %d", status);
        return nullptr;
    }

    return reinterpret_cast<JNIEnv*>(env);
}

jint InitGlobalJniVariables(JavaVM *jvm) {
    g_jvm = jvm;

    if (g_jvm == nullptr) {
        LOGE("InitGlobalJniVariables handed NULL?");
        return  -1;
    }

    return JNI_VERSION_1_6;
}

bool IsNull(JNIEnv* jni, jobject obj) {
    return jni->IsSameObject(obj, nullptr);
}

jobject NewGlobalRef(JNIEnv* jni, jobject o) {
    jobject ret = jni->NewGlobalRef(o);

    return ret;
}

void DeleteGlobalRef(JNIEnv* jni, jobject o) {
    jni->DeleteGlobalRef(o);
}

jmethodID GetMethodID(JNIEnv* jni, jclass c, const std::string& name, const char* signature) {
    jmethodID m = jni->GetMethodID(c, name.c_str(), signature);

    return m;
}

jfieldID GetFieldID(JNIEnv* jni, jclass c, const char* name, const char* signature) {
    jfieldID f = jni->GetFieldID(c, name, signature);

    return f;
}

jobject GetObjectField(JNIEnv* jni, jobject object, jfieldID id) {
    jobject o = jni->GetObjectField(object, id);

    return o;
}

jclass GetObjectClass(JNIEnv* jni, jobject object) {
    jclass c = jni->GetObjectClass(object);
    if (!c)
    {
        return nullptr;
    }
    return c;
}

jint GetIntField(JNIEnv* jni, jobject object, jfieldID id) {
    jint i = jni->GetIntField(object, id);

    return i;
}

bool GetBooleanField(JNIEnv* jni, jobject object, jfieldID id) {
    jboolean b = jni->GetBooleanField(object, id);

    return b;
}

jlong GetLongField(JNIEnv* jni, jobject object, jfieldID id) {
    jlong l = jni->GetLongField(object, id);

    return l;
}

// Given a UTF-8 encoded |native| string return a new (UTF-16) jstring.
jstring JavaStringFromStdString(JNIEnv* jni, const std::string& native) {
    jstring jstr = jni->NewStringUTF(native.c_str());

    return jstr;
}

// Given a jstring, reinterprets it to a new native string.
std::string JavaToStdString(JNIEnv* jni, const jstring& j_string) {
    // Invoke String.getBytes(String charsetName) method to convert |j_string|
    // to a byte array.
    const jclass string_class = GetObjectClass(jni, j_string);
    const jmethodID get_bytes =
            GetMethodID(jni, string_class, "getBytes", "(Ljava/lang/String;)[B");
    const jstring charset_name = jni->NewStringUTF("ISO-8859-1");

    const jbyteArray j_byte_array =
            (jbyteArray)jni->CallObjectMethod(j_string, get_bytes, charset_name);

    const size_t len = jni->GetArrayLength(j_byte_array);

    std::vector<char> buf(len);
    jni->GetByteArrayRegion(j_byte_array, 0, len,
                            reinterpret_cast<jbyte*>(&buf[0]));


    return std::string(buf.begin(), buf.end());
}

JNIEnv* AttachCurrentThreadIfNeeded() {
    JNIEnv *jni = GetEnv();
    if (jni) {
        return jni;
    }

    bool isAttached = false;
    JNIEnv* env = nullptr;
    int status = g_jvm->AttachCurrentThread(&env, NULL);
    if(status < 0) {
        LOGE("callback_handler: failed to attach "
                     "current thread");
        return nullptr;
    }
    jni = reinterpret_cast<JNIEnv*>(env);
    isAttached = true;

    return jni;
}

void ThreadDestructor(JNIEnv *jni) {
    if (jni) {
        g_jvm->DetachCurrentThread();
    }
}
}