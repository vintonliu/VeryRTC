package club.apprtc.veryrtc;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

public interface SipClientListener {
    void onRegistered(final boolean registered);
    void onRegisterFailure(final int reason);
    void onCallRinging(final SessionDescription remote_sdp);
    void onCallIncoming(final String from, final SessionDescription remote_sdp);
    void onCallConnected(final SessionDescription remote_sdp);
    void onCallEnded();
    void onCallFailure(final int reason);
    void onRemoteIceCandidate(final IceCandidate candidate);
}
