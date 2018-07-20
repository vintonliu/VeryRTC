package club.apprtc.veryrtc;

import android.os.Handler;
import android.os.HandlerThread;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

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

    public boolean doRegister(String proxy, String display, String username, String userpwd) {
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
        handler.post(new Runnable() {
            @Override
            public void run() {
                sipclient.pub_doUnRegister();
            }
        });
        return true;
    }

    public boolean doStartCall(String callee, final SessionDescription local_sdp) {
        if (callee.isEmpty() || local_sdp == null) {
            return false;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                sipclient.pub_doStartCall(callee, local_sdp.toString());
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
                sipclient.pub_doAnswer(local_sdp.toString());
            }
        });
        return true;
    }

    public boolean doHangup() {
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

        return true;
    }

    @Override
    public void onRegistered(boolean registered) {
        synchronized (listeners) {
            for (SipClientListener listener : listeners) {
                listener.onRegistered(registered);
            }
        }
    }

    @Override
    public void onRegisterFailure(int reason) {
        synchronized (listeners) {
            for (SipClientListener listener : listeners) {
                listener.onRegisterFailure(reason);
            }
        }
    }

    @Override
    public void onCallIncoming(String from, String remote_sdp) {
        synchronized (listeners) {
            for (SipClientListener listener: listeners) {
                if (remote_sdp.isEmpty()) {
                    listener.onCallIncoming(from, null);
                } else {
                    listener.onCallIncoming(from, new SessionDescription(remote_sdp));
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
    }

    @Override
    public void onCallConnected(String remote_sdp) {
        synchronized (listeners) {
            for (SipClientListener listener: listeners) {
                if (remote_sdp.isEmpty()) {
                    listener.onCallConnected(null);
                } else {
                    listener.onCallConnected(new SessionDescription(remote_sdp));
                }
            }
        }
    }

    @Override
    public void onCallEnded() {
        synchronized (listeners) {
            for (SipClientListener listener: listeners) {
                listener.onCallEnded();
            }
        }
    }

    @Override
    public void onCallFailure(int reason) {
        synchronized (listeners) {
            for (SipClientListener listener: listeners) {
                listener.onCallFailure(reason);
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
}
