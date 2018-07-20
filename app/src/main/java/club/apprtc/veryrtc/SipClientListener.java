package club.apprtc.veryrtc;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

public interface SipClientListener {
    void onRegistered(boolean registered);
    void onRegisterFailure(int reason);
    void onCallRinging(final SessionDescription remote_sdp);
    void onCallIncoming(String from, final SessionDescription remote_sdp);
    void onCallConnected(final SessionDescription remote_sdp);
    void onCallEnded();
    void onCallFailure(int reason);
    void onRemoteIceCandidate(final IceCandidate candidate);
}
