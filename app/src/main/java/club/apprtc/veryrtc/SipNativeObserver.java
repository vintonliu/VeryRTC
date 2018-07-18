package club.apprtc.veryrtc;

public interface SipNativeObserver {
    void onRegistered(boolean registered);
    void onRegisterFailure(int reason);
    void onCallIncoming(String from, String remote_sdp);
    void onCallProcess();
    void onCallRinging();
    void onCallConnected(String remote_sdp);
    void onCallEnded();
    void onCallFailure(int reason);
    void onRemoteIceCandidate(String candidate);
}
