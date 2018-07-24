package club.apprtc.veryrtc;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.util.LinkedList;
import java.util.Vector;

public class SipClientAPI implements SipChannelClient.SipNativeObserver {
    private final static String TAG = "SipClientAPI";

    // Names used for a IceCandidate JSON object.
    private final static String kCandidateSdpMidName = "sdpMid";
    private final static String kCandidateSdpMlineIndexName = "sdpMLineIndex";
    private final static String kCandidateSdpName = "candidate";

    private SipChannelClient sipclient;
    private Vector<SipClientListener> listeners;
    private final Handler handler;
    private boolean isRegistered = false;

    // Queued local ICE candidates are consumed only after received remote
    // ringing or connected message. Similarly local ICE candidates are sent to
    // remote peer after received ringing or connected message.
    private LinkedList<IceCandidate> queuedCallerCandidates = null;
    private enum SipCallState {
        CALL_IDLE,
        CALL_OUTGOING_INIT,
        CALL_OUTGOING_RINGING,
        CALL_OUTGOING_EARLY_RINGING,
        CALL_INCOMING,
        CALL_CONNECTED
    }
    SipCallState callState = SipCallState.CALL_IDLE;


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
    }

    public boolean doRegister(final String proxy, final String display,
                              final String username, final String userpwd) {
        if (proxy.isEmpty() || username.isEmpty() || userpwd.isEmpty()) {
            return false;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                sipclient.pub_doRegister(proxy, display, username, userpwd);
            }
        });

        return true;
    }

    public boolean doUnRegister() {
        isRegistered = false;
        handler.post(new Runnable() {
            @Override
            public void run() {
                sipclient.pub_doUnRegister();
            }
        });
        return true;
    }

    public boolean doStartCall(final String callee, final SessionDescription local_sdp) {
        if (!isRegistered || callee.isEmpty() || local_sdp == null) {
            return false;
        }
        queuedCallerCandidates = new LinkedList<IceCandidate>();

        handler.post(new Runnable() {
            @Override
            public void run() {
                sipclient.pub_doStartCall(callee, local_sdp.getDescription());
                callState = SipCallState.CALL_OUTGOING_INIT;
            }
        });
        return true;
    }

    public boolean doAnswer(final SessionDescription local_sdp) {
        if (local_sdp == null) {
            return false;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                sipclient.pub_doAnswer(local_sdp.getDescription());
                callState = SipCallState.CALL_CONNECTED;
            }
        });
        return true;
    }

    public boolean doHangup() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                sipclient.pub_doHangup();
                callState = SipCallState.CALL_IDLE;
            }
        });
        return true;
    }

    public boolean doSendCandidate(final IceCandidate candidate) {
        if (candidate == null) {
            return false;
        }

        if (callState == SipCallState.CALL_OUTGOING_INIT) {
            if (queuedCallerCandidates != null) {
                queuedCallerCandidates.add(candidate);
            }
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    JSONObject json = new JSONObject();
                    jsonPut(json, kCandidateSdpMlineIndexName, candidate.sdpMLineIndex);
                    jsonPut(json, kCandidateSdpMidName, candidate.sdpMid);
                    jsonPut(json, kCandidateSdpName, candidate.sdp);

                    sipclient.pub_doSendLocalCandidate(json.toString());
                }
            });
        }

        return true;
    }

    @Override
    public void onRegistered(boolean registered) {
        isRegistered = registered;
        synchronized (listeners) {
            for (SipClientListener listener : listeners) {
                listener.onRegistered(registered);
            }
        }
    }

    @Override
    public void onRegisterFailure(int reason) {
        isRegistered = false;
        synchronized (listeners) {
            for (SipClientListener listener : listeners) {
                listener.onRegisterFailure(reason);
            }
        }
    }

    @Override
    public void onCallIncoming(String from, String remote_sdp) {
        synchronized (listeners) {
            callState = SipCallState.CALL_INCOMING;
            for (SipClientListener listener: listeners) {
                if (remote_sdp.isEmpty()) {
                    listener.onCallIncoming(from, null);
                } else {
                    String type = "OFFER";
                    SessionDescription sdp = new SessionDescription(
                            SessionDescription.Type.fromCanonicalForm(type),
                            remote_sdp);
                    listener.onCallIncoming(from, sdp);
                }
            }
        }
    }

    @Override
    public void onCallProcess() {
        synchronized (listeners) {
            for (SipClientListener listener: listeners) {

            }
        }
    }

    @Override
    public void onCallRinging() {
        synchronized (listeners) {
            for (SipClientListener listener: listeners) {
                listener.onCallRinging(null);
            }
        }

        callState = SipCallState.CALL_OUTGOING_RINGING;
        drainCandidates();
    }

    @Override
    public void onCallConnected(String remote_sdp) {
        synchronized (listeners) {
            for (SipClientListener listener: listeners) {
                if (remote_sdp.isEmpty()) {
                    listener.onCallConnected(null);
                } else {
                    if (callState == SipCallState.CALL_OUTGOING_RINGING ||
                        callState == SipCallState.CALL_OUTGOING_INIT) {
                        String type = "ANSWER";
                        SessionDescription sdp = new SessionDescription(
                                SessionDescription.Type.fromCanonicalForm(type),
                                remote_sdp);
                        listener.onCallConnected(sdp);
                    } else {
                        listener.onCallConnected(null);
                    }
                }
            }
        }

        if ((callState != SipCallState.CALL_IDLE) &&
            (callState != SipCallState.CALL_INCOMING)) {
            drainCandidates();
        }
        callState = SipCallState.CALL_CONNECTED;
    }

    @Override
    public void onCallEnded() {
        synchronized (listeners) {
            callState = SipCallState.CALL_IDLE;
            for (SipClientListener listener: listeners) {
                listener.onCallEnded();
            }
        }
        queuedCallerCandidates = null;
    }

    @Override
    public void onCallFailure(int reason) {
        synchronized (listeners) {
            callState = SipCallState.CALL_IDLE;
            for (SipClientListener listener: listeners) {
                listener.onCallFailure(reason);
            }
        }

        queuedCallerCandidates = null;
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
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (queuedCallerCandidates != null) {
                        Log.d(TAG, "Send local candidates to remote");
                        for (IceCandidate candidate : queuedCallerCandidates) {
                            JSONObject json = new JSONObject();
                            jsonPut(json, kCandidateSdpMlineIndexName, candidate.sdpMLineIndex);
                            jsonPut(json, kCandidateSdpMidName, candidate.sdpMid);
                            jsonPut(json, kCandidateSdpName, candidate.sdp);

                            sipclient.pub_doSendLocalCandidate(json.toString());

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
}
