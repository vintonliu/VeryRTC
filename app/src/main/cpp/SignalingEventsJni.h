//
// Created by dell on 0018,2018/7/18.
//

#ifndef VERYRTC_SIGNALINGEVENTSJNI_H
#define VERYRTC_SIGNALINGEVENTSJNI_H

#include <iostream>
#include <string>
#include <jni.h>

#include "SignalingEvents.h"
namespace mrtc {

class SignalingEventsJni : public SignalingEvents {
public:
    SignalingEventsJni(JNIEnv *jni, jobject j_observer);
    virtual ~SignalingEventsJni();

    void onRegistered(bool registered) override;

    void onRegisterFailure(RegisterReason reason) override;

    void onCallProcess() override;

    void onCallRinging() override;

    void onCallConnected(const SignalingParameters &params) override;

    void onCallIncoming(const SignalingParameters &params) override;

    void onCallEnded() override;

    void onCallFailure(CallReason reason) override;

    void onRemoteDescription(const std::string &sdp) override;

    void onRemoteIceCandidate(const std::string &candidate) override;

    void onRemoteIceCandidatesRemoved(const std::string &candidate) override;

private:
    jobject j_observer_global_;
    jclass j_observer_class_;
};

}  /* namespace mrtc */
#endif //VERYRTC_SIGNALINGEVENTSJNI_H
