package club.apprtc.veryrtc;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

class SipClientAPI implements SipChannelClient.SipNativeObserver {

    /**
     * Struct holding the signaling parameters of an Sip client.
     */
    class SignalingParameters {
        public final String proxy;
        public final String nickName;
        public final String userName;
        public final String userPwd;

        private boolean isRegistered;

        public SignalingParameters(String proxy, String nickName,
                                   String userName, String userPwd) {
            this.proxy = proxy;
            this.nickName = nickName;
            this.userName = userName;
            this.userPwd = userPwd;
            this.isRegistered = false;
        }

        public boolean isRegistered() {
            return isRegistered;
        }

        public void setRegistered(boolean registered) {
            isRegistered = registered;
        }
    }

    public enum CallState {
        CALL_IDLE,
        CALL_OUTGOING_INIT,
        CALL_OUTGOING_PROCESSING,
        CALL_OUTGOING_RINGING,
        CALL_OUTPUT_EARLY_RINGING,
        CALL_INCOMING,
        CALL_CONNECTED
    }

    /**
     *
     */
    private class SessionParameters {
        boolean isInitiator;
        boolean isLocalHangup;
        CallState state;
        String fromUser;
        String toUser;
        SessionDescription offerSdp;
        SessionDescription answerSdp;
        List<IceCandidate> localCandidates;
        List<IceCandidate> remoteCandidates;
        MRTCReason reason;

        SessionParameters() {
            state = CallState.CALL_IDLE;
            isInitiator = false;
            reason = MRTCReason.MRTC_REASON_NONE;
        }
    }

    public enum SipReason {
        // Keep in sync with MSip/SignalingEvent.h!
        SIP_REASON_NONE,
        SIP_REASON_NO_RESPONSE,
        SIP_REASON_BAD_CREDENTIALS,
        SIP_REASON_DECLINED,
        SIP_REASON_NOT_FOUND,
        SIP_REASON_NO_ANSWER,
        SIP_REASON_BUSY,
        SIP_REASON_TEMPORARILY_UNAVAILABLE,
        SIP_REASON_MEDIA_INCOMPATIBLE,
        SIP_REASON_CANCEL,
        SIP_REASON_REQUEST_TIMEOUT,
        SIP_REASON_SERVER_INTERNAL_ERROR,
        SIP_REASON_UNKNOWN,

        SIP_REASON_USER_HANGUP,
        SIP_REASON_PEER_HANGUP,
    }

    private final static String TAG = "SipClientAPI";

    // Names used for a IceCandidate JSON object.
    private final static String kCandidateSdpMidName = "sdpMid";
    private final static String kCandidateSdpMlineIndexName = "sdpMLineIndex";
    private final static String kCandidateSdpName = "candidate";

    private SipChannelClient sipclient;
    private final Vector<SipClientListener> listeners;
    private final Handler handler;
    private SignalingParameters signalingParameters;
    private SessionParameters sessionParameters;

    // Queued local ICE candidates are consumed only after received remote
    // ringing or connected message. Similarly local ICE candidates are sent to
    // remote peer after received ringing or connected message.
    private LinkedList<IceCandidate> queuedCallerCandidates = null;

    public SipClientAPI(SipClientListener listener) {
        listeners = new Vector<>();
        listeners.add(listener);

        final HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                InitClientInternal();
            }
        });
    }

    private void InitClientInternal() {
        sipclient = new SipChannelClient(this);
    }

    public void doDispose() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                sipclient.dispose();
                handler.getLooper().quit();
            }
        });

        listeners.clear();
    }

    public boolean isRegistered() {
        return signalingParameters.isRegistered();
    }

    public boolean isInitiator() {
        if (sessionParameters == null) {
            return false;
        }

        return sessionParameters.isInitiator;
    }

    public CallState getState() {
        return sessionParameters.state;
    }

    public boolean isInCall() {
        return (sessionParameters != null) && (sessionParameters.state != CallState.CALL_IDLE);
    }

    public boolean isVideoCall() {
        if (getState() == CallState.CALL_IDLE) {
            return false;
        }
        if (getState() == CallState.CALL_CONNECTED) {
            return (sessionParameters.offerSdp.containVideo() &&
                    sessionParameters.answerSdp.containVideo());
        } else {
            return sessionParameters.offerSdp.containVideo();
        }
    }

    public SessionDescription offerSDP() {
        return sessionParameters.offerSdp;
    }

    public SessionDescription answerSDP() {
        return sessionParameters.answerSdp;
    }

    public boolean doRegister(final String proxy, final String display,
                              final String username, final String userpwd) {
        if (proxy.isEmpty() || username.isEmpty() || userpwd.isEmpty()) {
            return false;
        }

        signalingParameters = new SignalingParameters(proxy, display, username, userpwd);

        handler.post(new Runnable() {
            @Override
            public void run() {
                sipclient.pub_doRegister(proxy, display, username, userpwd);
            }
        });

        return true;
    }

    public boolean doUnRegister() {
        signalingParameters.isRegistered = false;
        handler.post(new Runnable() {
            @Override
            public void run() {
                sipclient.pub_doUnRegister();
            }
        });
        return true;
    }

    public boolean doOutgoingInit(final String toUser) {
        if (!(signalingParameters.isRegistered) || toUser.isEmpty() ) {
            return false;
        }
        if (sessionParameters != null) {
            sessionParameters = null;
        }
        sessionParameters = new SessionParameters();
        sessionParameters.isInitiator = true;
        sessionParameters.fromUser = signalingParameters.userName;
        sessionParameters.toUser = toUser;
        sessionParameters.state = CallState.CALL_OUTGOING_INIT;

        return true;
    }

    public boolean doStartCall(final SessionDescription local_sdp) {
        if (!(signalingParameters.isRegistered) || local_sdp == null) {
            Log.e(TAG,"doStartCall cancel since un-register");
            return false;
        }
        queuedCallerCandidates = new LinkedList<IceCandidate>();

        sessionParameters.offerSdp = local_sdp;
        sessionParameters.state = CallState.CALL_OUTGOING_PROCESSING;

        handler.post(new Runnable() {
            @Override
            public void run() {
                sipclient.pub_doStartCall(sessionParameters.toUser,
                                        local_sdp.getDescription());
            }
        });

        return true;
    }

    public boolean doAnswer(final SessionDescription local_sdp) {
        if (local_sdp == null) {
            return false;
        }

        sessionParameters.answerSdp = local_sdp;

        handler.post(new Runnable() {
            @Override
            public void run() {
                sipclient.pub_doAnswer(local_sdp.getDescription());
            }
        });
        return true;
    }

    public boolean doHangup(MRTCReason reason) {
        if (sessionParameters == null) {
            return true;
        }

        sessionParameters.isLocalHangup = true;
        sessionParameters.reason = reason;
        handler.post(new Runnable() {
            @Override
            public void run() {
                sipclient.pub_doHangup();
            }
        });
        return true;
    }

    public boolean doSendCandidate(final IceCandidate candidate) {
        if (candidate == null) {
            return false;
        }

        if (sessionParameters == null) {
            return false;
        }

        if ((sessionParameters.isInitiator) && (queuedCallerCandidates != null)) {
            queuedCallerCandidates.add(candidate);
            Log.i(TAG, "doSendCandidate add: " + candidate.toString());
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    JSONObject json = new JSONObject();
                    jsonPut(json, kCandidateSdpMlineIndexName, candidate.sdpMLineIndex);
                    jsonPut(json, kCandidateSdpMidName, candidate.sdpMid);
                    jsonPut(json, kCandidateSdpName, candidate.sdp);

                    sipclient.pub_doSendLocalCandidate(json.toString());
                    Log.i(TAG, "doSendCandidate send: " + candidate.toString());
                }
            }, 100);
        }

        return true;
    }

    public boolean doSetUserAgent(final String uname, final String uver) {
        if (uname.isEmpty() || uver.isEmpty()) {
            return false;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                sipclient.pub_doSetUserAgent(uname, uver);
            }
        });

        return true;
    }

    public boolean doSetSipTransport(final int transport, final int port) {
        if (port < 0) {
            return false;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                sipclient.pub_doSetSipTransport(transport, port);
            }
        });

        return true;
    }

    @Override
    public void onRegistered(boolean registered) {
        signalingParameters.isRegistered = registered;
        synchronized (listeners) {
            for (SipClientListener listener : listeners) {
                listener.onRegistered(registered);
            }
        }
    }

    @Override
    public void onRegisterFailure(int reason) {
        signalingParameters.isRegistered = false;
        synchronized (listeners) {
            for (SipClientListener listener : listeners) {
                listener.onRegisterFailure(convertSipReason(SipReason.values()[reason]));
            }
        }
    }

    @Override
    public void onCallIncoming(String from, String remote_sdp) {
        if (sessionParameters != null) {
            sessionParameters = null;
        }
        sessionParameters = new SessionParameters();
        sessionParameters.isInitiator = false;
        sessionParameters.fromUser = from;
        sessionParameters.toUser = signalingParameters.userName;
        sessionParameters.state = CallState.CALL_INCOMING;

        synchronized (listeners) {
            for (SipClientListener listener: listeners) {
                if (remote_sdp.isEmpty()) {
                    listener.onCallIncoming(from, null);
                } else {
                    String type = "OFFER";
                    SessionDescription sdp = new SessionDescription(
                            SessionDescription.Type.fromCanonicalForm(type),
                            remote_sdp);

                    sessionParameters.offerSdp = sdp;
                    listener.onCallIncoming(from, sdp);
                }
            }
        }
    }

    @Override
    public void onCallProcess() {
        sessionParameters.state = CallState.CALL_OUTGOING_PROCESSING;
        synchronized (listeners) {
            for (SipClientListener listener: listeners) {

            }
        }
    }

    @Override
    public void onCallRinging() {
        sessionParameters.state = CallState.CALL_OUTGOING_RINGING;
        synchronized (listeners) {
            for (SipClientListener listener: listeners) {
                listener.onCallRinging(null);
            }
        }
    }

    @Override
    public void onCallConnected(String remote_sdp) {
        sessionParameters.state = CallState.CALL_CONNECTED;

        synchronized (listeners) {
            for (SipClientListener listener: listeners) {
                if (isInitiator() && remote_sdp.isEmpty()) {
                    // We need remote sdp as an Initiator
                    listener.onCallEnded(MRTCReason.MRTC_REASON_MEDIA_NOT_ACCEPT);
                } else {
                    if (isInitiator()) {
                        String type = "ANSWER";
                        SessionDescription sdp = new SessionDescription(
                                SessionDescription.Type.fromCanonicalForm(type),
                                remote_sdp);
                        sessionParameters.answerSdp = sdp;
                        listener.onCallConnected(sdp);
                    } else {
                        // As non-initiator, we have all sdp before
                        listener.onCallConnected(null);
                    }
                }
            }
        }

        // send all local cache candidates
        drainCandidates();
    }

    @Override
    public void onCallEnded(int reason) {
        queuedCallerCandidates = null;
        sessionParameters.state = CallState.CALL_IDLE;

        synchronized (listeners) {
            for (SipClientListener listener: listeners) {
                if (sessionParameters.reason == MRTCReason.MRTC_REASON_NONE) {
                    SipReason sipReason = SipReason.values()[reason];
                    if (sipReason == SipReason.SIP_REASON_NONE &&
                        !sessionParameters.isLocalHangup) {
                        listener.onCallEnded(MRTCReason.MRTC_REASON_PEER_HANGUP);
                    } else {
                        listener.onCallEnded(convertSipReason(sipReason));
                    }
                } else {
                    listener.onCallEnded(sessionParameters.reason);
                }
            }
        }
    }

    @Override
    public void onRemoteIceCandidate(String candidate) {
        synchronized (listeners) {
            try {
                JSONObject json = new JSONObject(candidate);
                for (SipClientListener listener: listeners) {
                    listener.onRemoteIceCandidate(new IceCandidate(
                            json.getString(kCandidateSdpMidName),
                            json.getInt(kCandidateSdpMlineIndexName),
                            json.getString(kCandidateSdpName)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void addObserver(SipClientListener listener){
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @SuppressWarnings("unused")
    public void removeObserver(SipClientListener listener){
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    // Put a |key|->|value| mapping in |json|.
    private static void jsonPut(JSONObject json, String key, Object value) {
        try {
            json.put(key, value);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void drainCandidates() {
        Log.i(TAG, "drainCandidates");
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (queuedCallerCandidates != null) {
                        for (IceCandidate candidate : queuedCallerCandidates) {
                            JSONObject json = new JSONObject();
                            jsonPut(json, kCandidateSdpMlineIndexName, candidate.sdpMLineIndex);
                            jsonPut(json, kCandidateSdpMidName, candidate.sdpMid);
                            jsonPut(json, kCandidateSdpName, candidate.sdp);

                            sipclient.pub_doSendLocalCandidate(json.toString());
                            Log.i(TAG, "drainCandidates Send candidates: " + json.toString());
                            Thread.sleep(100);
                        }
                        queuedCallerCandidates = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private MRTCReason convertSipReason(SipClientAPI.SipReason reason) {
        switch (reason) {
            case SIP_REASON_NONE:
                return MRTCReason.MRTC_REASON_NONE;
            case SIP_REASON_NO_RESPONSE:
                return MRTCReason.MRTC_REASON_NO_RESPONSE;
            case SIP_REASON_BAD_CREDENTIALS:
                return MRTCReason.MRTC_REASON_BAD_CREDENTIALS;
            case SIP_REASON_DECLINED:
                return MRTCReason.MRTC_REASON_PEER_DECLINED;
            case SIP_REASON_NOT_FOUND:
                return MRTCReason.MRTC_REASON_NOT_FOUND;
            case SIP_REASON_NO_ANSWER:
                return MRTCReason.MRTC_REASON_PEER_NO_ANSWER;
            case SIP_REASON_BUSY:
                return MRTCReason.MRTC_REASON_PEER_BUSY;
            case SIP_REASON_TEMPORARILY_UNAVAILABLE:
                return MRTCReason.MRTC_REASON_TEMPORARILY_UNAVAILABLE;
            case SIP_REASON_MEDIA_INCOMPATIBLE:
                return MRTCReason.MRTC_REASON_MEDIA_NOT_ACCEPT;
            case SIP_REASON_CANCEL:
                return MRTCReason.MRTC_REASON_USER_CANCEL;
            case SIP_REASON_REQUEST_TIMEOUT:
                return MRTCReason.MRTC_REASON_REQUEST_TIMEOUT;
            case SIP_REASON_SERVER_INTERNAL_ERROR:
                return MRTCReason.MRTC_REASON_SERVER_INTERNAL_ERROR;

            default:
                return MRTCReason.MRTC_REASON_UNKNOWN;
        }
    }

    interface SipClientListener {
        void onRegistered(final boolean registered);
        void onRegisterFailure(final MRTCReason reason);
        void onCallRinging(final SessionDescription remoteSdp);
        void onCallIncoming(final String fromUser, final SessionDescription remoteSdp);
        void onCallConnected(final SessionDescription remoteSdp);
        void onCallEnded(final MRTCReason reason);
        void onRemoteIceCandidate(final IceCandidate candidate);
    }
}
