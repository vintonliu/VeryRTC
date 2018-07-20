package club.apprtc.veryrtc;

import android.util.Log;

public class SipChannelClient {
    static {
        System.loadLibrary("Sip");
    }

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

    private native long nativeCreateObserver(SipNativeObserver observer);
    private native void freeNativeObserver(long nativeObserver);

    private native long doCreateClient(long nativeObserver);

    private native void freeNativeClient(long nativeClient);

    private native boolean doRegister(String proxy, String display, String username, String userpwd);

    private native boolean doUnRegister();

    private native boolean doStartCall(String callee, String local_sdp);

    private native boolean doAnswer(String local_sdp);

    private native boolean doHangup();

    private native boolean doSendCandidate(String candidate);

    private final static String TAG = "SipChannel";

    private final long nativeObserver;
    private final long nativeClient;
    public SipChannelClient(SipNativeObserver observer) {
        nativeObserver = nativeCreateObserver(observer);
        nativeClient = doCreateClient(nativeObserver);
        if (nativeClient == 0)
        {
            Log.e(TAG, "native Sip channel client create failure.");
        }
    }

    /*
    * Free native resources associated with this Sip Channel.
    * Note that this method cannot be safely called from an observer callback
    * */
    public void dispose() {
        freeNativeClient(nativeClient);
        freeNativeObserver(nativeObserver);
    }

    public boolean pub_doRegister(String proxy, String display, String username, String userpwd) {
        if (proxy.isEmpty() || username.isEmpty() || userpwd.isEmpty()) {
            return false;
        }

        return doRegister(proxy, display, username, userpwd);
    }

    public boolean pub_doUnRegister() {
        return doUnRegister();
    }

    public boolean pub_doStartCall(String callee, String local_sdp) {
        if (callee.isEmpty() || local_sdp.isEmpty()) {
            return false;
        }
        return doStartCall(callee, local_sdp);
    }

    public boolean pub_doAnswer(String local_sdp) {
        if (local_sdp.isEmpty()) {
            return false;
        }
        return doAnswer(local_sdp);
    }

    public boolean pub_doHangup() {
        return doHangup();
    }

    public boolean pub_doSendLocalCandidate(String candidate) {
        if (candidate.isEmpty()) {
            return false;
        }
        return doSendCandidate(candidate);
    }
}
