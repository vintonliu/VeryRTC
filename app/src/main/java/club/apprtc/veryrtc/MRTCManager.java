package club.apprtc.veryrtc;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.CameraEnumerator;
import org.webrtc.FileVideoCapturer;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFileRenderer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

class MRTCManager implements SipClientAPI.SipClientListener,
                                   PeerConnectionClient.PeerConnectionEvents {

    private final static String TAG = "MRTCManager";

    // Fix for devices running old Android versions not finding the libraries.
    // https://bugs.chromium.org/p/webrtc/issues/detail?id=6751
    static {
        try {
            System.loadLibrary("c++_shared");
            System.loadLibrary("boringssl.cr");
            System.loadLibrary("protobuf_lite.cr");
        } catch (UnsatisfiedLinkError e) {
            Logging.w(TAG, "Failed to load native dependencies: ", e);
        }
    }

    // Peer connection statistics callback period in ms.
    private static final int STAT_CALLBACK_PERIOD = 1000;

    private static class ProxyRenderer implements VideoRenderer.Callbacks {
        private VideoRenderer.Callbacks target;

        @Override
        synchronized public void renderFrame(VideoRenderer.I420Frame frame) {
            if (target == null) {
                Logging.d(TAG, "Dropping frame in proxy because target is null.");
                VideoRenderer.renderFrameDone(frame);
                return;
            }

            target.renderFrame(frame);
        }

        synchronized public void setTarget(VideoRenderer.Callbacks target) {
            this.target = target;
        }
    }

    private static class ProxyVideoSink implements VideoSink {
        private VideoSink target;

        @Override
        synchronized public void onFrame(VideoFrame frame) {
            if (target == null) {
                Logging.d(TAG, "Dropping frame in proxy because target is null.");
                return;
            }

            target.onFrame(frame);
        }

        synchronized public void setTarget(VideoSink target) {
            this.target = target;
        }
    }

    private final ProxyRenderer remoteProxyRenderer = new ProxyRenderer();
    private final ProxyVideoSink localProxyVideoSink = new ProxyVideoSink();
    private PeerConnectionClient peerConnectionClient = null;
    private AppRTCAudioManager audioManager = null;
    private SurfaceViewRenderer pipRenderer;
    private SurfaceViewRenderer fullscreenRenderer;
    private VideoFileRenderer videoFileRenderer;
    private final List<VideoRenderer.Callbacks> remoteRenderers = new ArrayList<VideoRenderer.Callbacks>();
    private PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;
    private boolean iceConnected = false;
    private boolean isError;
    private long callStartedTimeMs = 0;
    private boolean micEnabled = true;
    private boolean speakerMuteEnabled = false;
    private boolean screencaptureEnabled = false;
    // True if local view is in the fullscreen renderer.
    private boolean isSwappedFeeds;
    private CpuMonitor cpuMonitor;

    private SipClientAPI sipClientAPI;
    private Context mContext;
    private final Vector<OnClientListener> listeners;
    private MSettings mSettings;
    private boolean isInitialized;
    private final Handler workHandler;
    private final Handler mainHandler;

    /**
     *
     * @param context Application Context
     */
    public MRTCManager(Context context) {
        mContext = context;
        listeners = new Vector<>();

        sipClientAPI = new SipClientAPI(this);
        mSettings = new MSettings(context);

        remoteRenderers.add(remoteProxyRenderer);

        final HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        workHandler = new Handler(handlerThread.getLooper());

        mainHandler = new Handler();

        isInitialized = true;
    }

    public void close() {
        isInitialized = false;

        disconnect(MRTCReason.MRTC_REASON_USER_HANGUP);

        if (sipClientAPI != null) {
            sipClientAPI.doDispose();
            sipClientAPI = null;
        }

        synchronized (listeners) {
            listeners.clear();
        }

        if (mSettings != null) {
            mSettings = null;
        }

        workHandler.getLooper().quit();
    }

    public boolean doInitialize() {
        String uname = "MRTC/" + Build.MODEL + "/" + Build.BRAND;
        sipClientAPI.doSetUserAgent(uname, BuildConfig.VERSION_NAME);
        return true;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setOnClientListener(OnClientListener listener) {
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                // not find, then add it
                listeners.add(listener);
            }
        }
    }

    public void removeOnClientListener(OnClientListener listener) {
        synchronized (listeners) {
            if (listeners.contains(listener)) {
                listeners.remove(listener);
            }
        }
    }

    public boolean doSetParameter(String key, Object objValue) {
        mSettings.update(key, objValue);
        return true;
    }

    /**
     * set video render view, must invoke before @doStartCall and @doAnswerCall
     * to establish video call
     * @param localRender
     * @param remoteRender
     * @return
     */
    public boolean setRenderView(SurfaceViewRenderer localRender,
                                 SurfaceViewRenderer remoteRender) {
        if (localRender == null || remoteRender == null) {
            return false;
        }

        pipRenderer = localRender;
        fullscreenRenderer = remoteRender;

        pipRenderer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSwappedFeeds(!isSwappedFeeds);
            }
        });

        return true;
    }

    /**
     * Register to sip proxy server
     * @param proxy server ip address
     * @param nickName sip display name
     * @param userName sip username
     * @param userPwd sip user password
     * @return true for trying send REGISTER
     */
    public boolean doLogin(final String proxy, final String nickName,
                           final String userName, final String userPwd) {
        return sipClientAPI.doRegister(proxy, nickName, userName, userPwd);
    }

    /**
     * UnRegister from sip proxy server
     * @return true for trying send UNREGISTER
     */
    public boolean doLogout() {
        return sipClientAPI.doUnRegister();
    }

    public boolean doStartCall(final String toUser, final boolean videoCall) {
        if (!sipClientAPI.isRegistered()) {
            Log.w(TAG, "Couldn't start call when un-registered.");
            return false;
        }

        if (videoCall && (pipRenderer == null || fullscreenRenderer == null)) {
            Log.e(TAG, "Couldn't start video call since render view not set.");
            return false;
        }

        if (sipClientAPI.isInCall()) {
            Log.e(TAG, "Couldn't repeat start call.");
            return false;
        }

        if (peerConnectionClient != null) {
            peerConnectionClient.close();
            peerConnectionClient = null;
        }

        if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }

        mSettings.update(mSettings.keyprefVideoCall, videoCall);

        callStartedTimeMs = System.currentTimeMillis();
        isError = false;

        createAudioManager();
        createPeerConnectionFactory();
        createPeerConnection();

        if (peerConnectionClient == null) {
            Logging.e(TAG, "Creating PeerConnection failure.");
            disconnect(MRTCReason.MRTC_REASON_USER_HANGUP);
            return false;
        }

        logAndToast("Creating OFFER...");

        // Create offer. Offer SDP will be sent to answering client in
        // PeerConnectionEvents.onLocalDescription event.
        peerConnectionClient.createOffer();

        // Create call session and wait offer sdp
        sipClientAPI.doOutgoingInit(toUser);

        return true;
    }

    public boolean doAnswerCall(boolean videoCall) {
        if (videoCall && (pipRenderer == null || fullscreenRenderer == null)) {
            Logging.e(TAG, "Video render view is null.");
            return false;
        }

        if (!sipClientAPI.isInCall()) {
            return false;
        }

        if (!sipClientAPI.isVideoCall()) {
            videoCall = false;
        }
        mSettings.update(mSettings.keyprefVideoCall, videoCall);
        isError = false;

        createAudioManager();
        createPeerConnectionFactory();
        createPeerConnection();

        if (peerConnectionClient == null) {
            Logging.w(TAG, "Creating PeerConnection failure.");
            disconnect(MRTCReason.MRTC_REASON_USER_HANGUP);
            return false;
        }

        Logging.d(TAG, "Setting OFFER...");
        peerConnectionClient.setRemoteDescription(sipClientAPI.offerSDP());
        logAndToast("Creating ANSWER...");

        // Create answer. Answer SDP will be sent to offering client in
        // PeerConnectionEvents.onLocalDescription event.
        peerConnectionClient.createAnswer();

        return true;
    }

    public boolean doHangup() {
        if (sipClientAPI != null && sipClientAPI.isInCall()) {
            sipClientAPI.doHangup(MRTCReason.MRTC_REASON_USER_HANGUP);
        }

        return true;
    }

    public boolean doVideoPause() {
        // Don't stop the video when using screencapture to allow user to show other apps to the remote
        // end.
        if (peerConnectionClient != null && !screencaptureEnabled) {
            peerConnectionClient.stopVideoSource();
        }

        if (cpuMonitor != null) {
            cpuMonitor.pause();
        }

        return true;
    }

    public boolean doVideoResume() {
        if (peerConnectionClient != null && !screencaptureEnabled) {
            peerConnectionClient.startVideoSource();
        }

        if (cpuMonitor != null) {
            cpuMonitor.resume();
        }

        return true;
    }

    public boolean doCameraSwitch() {
        if (peerConnectionClient != null) {
            peerConnectionClient.switchCamera();
            return false;
        }

        return true;
    }

    public boolean doVideoScalingSwitch(RendererCommon.ScalingType scalingType) {
        fullscreenRenderer.setScalingType(scalingType);

        return true;
    }

    public boolean doCaptureFormatChange(int width, int height, int framerate) {
        if (peerConnectionClient != null) {
            peerConnectionClient.changeCaptureFormat(width, height, framerate);

            return true;
        }
        return false;
    }

    public boolean doToggleMic(boolean enable) {
        if (peerConnectionClient != null) {
            micEnabled = enable;
            peerConnectionClient.setAudioEnabled(micEnabled);
        }

        return micEnabled;
    }

    public boolean doToggleSpeaker(boolean enable) {
        if (audioManager != null) {
            if (enable) {
                audioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
            } else {
                audioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
            }

            return audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.SPEAKER_PHONE;
        }
        return false;
    }

    public boolean doSetSpeakerMute(boolean enable) {
        if (peerConnectionClient != null) {
            speakerMuteEnabled = enable;
            peerConnectionClient.setSpeakerMute(speakerMuteEnabled);
        }

        return speakerMuteEnabled;
    }

    public boolean doSetSipTransport(int transport, int port) {
        if (sipClientAPI == null) {
            return false;
        }

        return sipClientAPI.doSetSipTransport(transport, port);
    }

    private void createAudioManager() {
        // Create an audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager = AppRTCAudioManager.create(mContext);

        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        audioManager.start(new AppRTCAudioManager.AudioManagerEvents() {
            // This method will be called each time the number of available audio
            // devices has changed.
            @Override
            public void onAudioDeviceChanged(AppRTCAudioManager.AudioDevice selectedAudioDevice,
                                             Set<AppRTCAudioManager.AudioDevice> availableAudioDevices) {
                Logging.d(TAG, "onAudioManagerDevicesChanged: " + selectedAudioDevice + ", "
                        + "selected: " + availableAudioDevices);
                // TODO(henrika): add callback handler.
            }
        });

        // Audio call audio device default to earpiece,
        // Video call audio device default speaker
        if (mSettings.getMpcParameters().videoCallEnable) {
            audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
        } else {
            audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
        }
    }

    private void createPeerConnectionFactory() {
        MSettings.MPeerConnectionParameters mpcParameters = mSettings.getMpcParameters();

        // Create peer connection client.
        peerConnectionClient = new PeerConnectionClient();

        PeerConnectionClient.DataChannelParameters dataChannelParameters = null;
        if (mpcParameters.dataChannelEnable) {
            dataChannelParameters = new PeerConnectionClient.DataChannelParameters(mpcParameters.msgOrderEnable,
                    mpcParameters.maxRetransmitTimeMs,
                    mpcParameters.maxRetransmitAttempts,
                    mpcParameters.subProtocol,
                    mpcParameters.negotiatedEnable,
                    mpcParameters.dataId);
        }

        peerConnectionParameters = new PeerConnectionClient.PeerConnectionParameters(
                mpcParameters.videoCallEnable,
                mpcParameters.loopback,
                mpcParameters.tracing,
                Logging.Severity.LS_INFO,
                mpcParameters.videoWidth,
                mpcParameters.videoHeight,
                mpcParameters.videoFps,
                mpcParameters.videoMaxBitrate,
                mpcParameters.videoCodec,
                mpcParameters.videoHwCodecEnable,
                mpcParameters.videoFlexFecEnable,
                mpcParameters.audioStartBitrate,
                mpcParameters.audioCodec,
                mpcParameters.noAudioProcessingEnable,
                mpcParameters.aecdumpEnable,
                mpcParameters.useOpenSLES,
                mpcParameters.disableBuiltInAec,
                mpcParameters.disableBuiltInAgc,
                mpcParameters.disableBuiltInNs,
                mpcParameters.levelCtrlEnable,
                mpcParameters.disableWebrtcAgcAndHpf,
                dataChannelParameters);

        // Create CPU monitor
        cpuMonitor = new CpuMonitor(mContext);

        if (mpcParameters.loopback) {
            PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
            options.networkIgnoreMask = 0;
            peerConnectionClient.setPeerConnectionFactoryOptions(options);
        }

        peerConnectionClient.createPeerConnectionFactory(
                mContext, peerConnectionParameters, this);
    }

    private void createPeerConnection() {
        VideoCapturer videoCapturer = null;
        if (mSettings.getMpcParameters().videoCallEnable) {
            // Create video renderers.
            pipRenderer.init(peerConnectionClient.getRenderContext(), null);
            pipRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

            fullscreenRenderer.init(peerConnectionClient.getRenderContext(), null);
            fullscreenRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);

            pipRenderer.setZOrderMediaOverlay(true);
            pipRenderer.setEnableHardwareScaler(true);
            fullscreenRenderer.setEnableHardwareScaler(true);

            // Start with local feed in fullscreen and swap it to the pip when the call is connected
            setSwappedFeeds(true);

            videoCapturer = createVideoCapturer();
        }

        peerConnectionClient.createPeerConnection(localProxyVideoSink,
                                                remoteRenderers, videoCapturer,
                                                mSettings.getMpcParameters().iceServers);
    }

    private boolean arePermissionGranted() {
        return (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
    }

    private void setSwappedFeeds(boolean isSwappedFeeds) {
        Logging.d(TAG, "setSwappedFeeds: " + isSwappedFeeds);
        this.isSwappedFeeds = isSwappedFeeds;
        localProxyVideoSink.setTarget(isSwappedFeeds ? fullscreenRenderer : pipRenderer);
        remoteProxyRenderer.setTarget(isSwappedFeeds ? pipRenderer : fullscreenRenderer);
        fullscreenRenderer.setMirror(isSwappedFeeds);
        pipRenderer.setMirror(!isSwappedFeeds);
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(mContext) && mSettings.getMpcParameters().useCamera2;
    }

    private boolean captureToTexture() {
        return mSettings.getMpcParameters().videoCaptureTextureEnable;
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        for (String deviceName : deviceNames) {
            Logging.d(TAG, deviceName);
            final List<CameraEnumerationAndroid.CaptureFormat> formatList =
                    enumerator.getSupportedFormats(deviceName);
            for (CameraEnumerationAndroid.CaptureFormat format:
                 formatList) {
                Logging.d(TAG, format.toString());
            }
        }

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer = null;
        if (mSettings.getMpcParameters().videoFileAsCameraEnable &&
            !mSettings.getMpcParameters().videoFileAsCamera.isEmpty()) {
            try {
                videoCapturer = new FileVideoCapturer(mSettings.getMpcParameters().videoFileAsCamera);
            } catch (IOException e) {
                e.printStackTrace();
                reportError("Failed to open video file for emulated camera.");
                return null;
            }
        } else if (screencaptureEnabled) {

        } else if (useCamera2()) {
            if (!captureToTexture()) {
                reportError(mContext.getString(R.string.camera2_texture_only_error));
                return null;
            }

            Logging.d(TAG, "Creating capturer using camera2 API");
            videoCapturer = createCameraCapturer(new Camera2Enumerator(mContext));
        } else {
            Logging.d(TAG, "Creating capturer using camera1 API");
            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
        }

        if (videoCapturer == null) {
            reportError("Failed to open camera");
            return null;
        }
        return videoCapturer;
    }

    // Should be called from UI thread
    private void callConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        Log.i(TAG, "Call connected: delay=" + delta + "ms");
        if (peerConnectionClient == null || isError) {
            Log.w(TAG, "Call is connected in closed or error state");
            return;
        }
        // Enable statistics callback.
        peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);
        if (mSettings.getMpcParameters().videoCallEnable) {
            setSwappedFeeds(false /* isSwappedFeeds */);
        }
    }

    private void reportError(final String description) {
        workHandler.post(new Runnable() {
            @Override
            public void run() {
                MRTCReason reason = MRTCReason.MRTC_REASON_UNKNOWN;
                if (description.toUpperCase().contains("SDP")) {
                    reason = MRTCReason.MRTC_REASON_SDP_FAILURE;
                } else if (description.toUpperCase().contains("ICE")) {
                    reason = MRTCReason.MRTC_REASON_ICE_FAILURE;
                }

                if (!isError) {
                    isError = true;
                    disconnect(reason);
                }

                synchronized (listeners) {
                    for (OnClientListener listener : listeners) {
                        listener.onClientError(description);
                    }
                }
            }
        });

        Logging.e(TAG, description);
    }

    private void disconnect(MRTCReason reason) {
        remoteProxyRenderer.setTarget(null);
        localProxyVideoSink.setTarget(null);

        if (sipClientAPI != null && sipClientAPI.isInCall()) {
            sipClientAPI.doHangup(reason);
        }
        if (pipRenderer != null) {
            pipRenderer.release();
            pipRenderer = null;
        }
        if (videoFileRenderer != null) {
            videoFileRenderer.release();
            videoFileRenderer = null;
        }
        if (fullscreenRenderer != null) {
            fullscreenRenderer.release();
            fullscreenRenderer = null;
        }
        if (peerConnectionClient != null) {
            peerConnectionClient.close();
            peerConnectionClient = null;
        }

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (audioManager != null) {
                    audioManager.stop();
                    audioManager = null;
                }
            }
        });
    }

    // Log |msg| and Toast about it.
    private void logAndToast(String msg) {
        Log.d(TAG, msg);
    }

    private Map<String, String> getReportMap(StatsReport report) {
        Map<String, String> reportMap = new HashMap<String, String>();
        for (StatsReport.Value value : report.values) {
            reportMap.put(value.name, value.value);
        }
        return reportMap;
    }

    private void updateEncoderStatistics(final StatsReport[] reports) {
        if (!mSettings.getMpcParameters().displayHudEnable) {
            return;
        }

        StringBuilder encoderStat = new StringBuilder(128);
        StringBuilder bweStat = new StringBuilder();
        StringBuilder connectionStat = new StringBuilder();
        StringBuilder videoSendStat = new StringBuilder();
        StringBuilder videoRecvStat = new StringBuilder();
        String fps = null;
        String targetBitrate = null;
        String actualBitrate = null;

        for (StatsReport report : reports) {
            if (report.type.equals("ssrc") && report.id.contains("ssrc") && report.id.contains("send")) {
                // Send video statistics.
                Map<String, String> reportMap = getReportMap(report);
                String trackId = reportMap.get("googTrackId");
                if (trackId != null && trackId.contains(PeerConnectionClient.VIDEO_TRACK_ID)) {
                    fps = reportMap.get("googFrameRateSent");
                    videoSendStat.append(report.id).append("\n");
                    for (StatsReport.Value value : report.values) {
                        String name = value.name.replace("goog", "");
                        videoSendStat.append(name).append("=").append(value.value).append("\n");
                    }
                }
            } else if (report.type.equals("ssrc") && report.id.contains("ssrc")
                    && report.id.contains("recv")) {
                // Receive video statistics.
                Map<String, String> reportMap = getReportMap(report);
                // Check if this stat is for video track.
                String frameWidth = reportMap.get("googFrameWidthReceived");
                if (frameWidth != null) {
                    videoRecvStat.append(report.id).append("\n");
                    for (StatsReport.Value value : report.values) {
                        String name = value.name.replace("goog", "");
                        videoRecvStat.append(name).append("=").append(value.value).append("\n");
                    }
                }
            } else if (report.id.equals("bweforvideo")) {
                // BWE statistics.
                Map<String, String> reportMap = getReportMap(report);
                targetBitrate = reportMap.get("googTargetEncBitrate");
                actualBitrate = reportMap.get("googActualEncBitrate");

                bweStat.append(report.id).append("\n");
                for (StatsReport.Value value : report.values) {
                    String name = value.name.replace("goog", "").replace("Available", "");
                    bweStat.append(name).append("=").append(value.value).append("\n");
                }
            } else if (report.type.equals("googCandidatePair")) {
                // Connection statistics.
                Map<String, String> reportMap = getReportMap(report);
                String activeConnection = reportMap.get("googActiveConnection");
                if (activeConnection != null && activeConnection.equals("true")) {
                    connectionStat.append(report.id).append("\n");
                    for (StatsReport.Value value : report.values) {
                        String name = value.name.replace("goog", "");
                        connectionStat.append(name).append("=").append(value.value).append("\n");
                    }
                }
            }
        }

        if (mSettings.getMpcParameters().videoCallEnable) {
            if (fps != null) {
                encoderStat.append("Fps:  ").append(fps).append("\n");
            }
            if (targetBitrate != null) {
                encoderStat.append("Target BR: ").append(targetBitrate).append("\n");
            }
            if (actualBitrate != null) {
                encoderStat.append("Actual BR: ").append(actualBitrate).append("\n");
            }
        }

        if (cpuMonitor != null) {
            encoderStat.append("CPU%: ")
                    .append(cpuMonitor.getCpuUsageCurrent())
                    .append("/")
                    .append(cpuMonitor.getCpuUsageAverage())
                    .append(". Freq: ")
                    .append(cpuMonitor.getFrequencyScaleAverage());
        }

        synchronized (listeners) {
            for (OnClientListener listener : listeners) {
                listener.onCallStatsReady(encoderStat.toString(),
                                        bweStat.toString(),
                                        connectionStat.toString(),
                                        videoSendStat.toString(),
                                        videoRecvStat.toString());
            }
        }
    }

    @Override
    public void onLocalDescription(final SessionDescription sdp) {
        workHandler.post(new Runnable() {
            @Override
            public void run() {
                boolean res;
                if (sipClientAPI.isInitiator()) {
                    res = sipClientAPI.doStartCall(sdp);
                } else {
                    res = sipClientAPI.doAnswer(sdp);
                }

                if (res) {
                    if (peerConnectionParameters.videoMaxBitrate > 0) {
                        peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
                    }
                } else {
                    disconnect(MRTCReason.MRTC_REASON_USER_HANGUP);
                }
            }
        });
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
        workHandler.post(new Runnable() {
            @Override
            public void run() {
                sipClientAPI.doSendCandidate(candidate);
            }
        });
    }

    @Override
    public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
        Logging.d(TAG, "onIceCandidatesRemoved candidates = " + candidates.toString());
    }

    @Override
    public void onIceConnected() {
        Logging.d(TAG, "onIceConnected" );
        workHandler.post(new Runnable() {
            @Override
            public void run() {
                iceConnected = true;
                callConnected();
            }
        });
    }

    @Override
    public void onIceDisconnected() {
        Logging.d(TAG, "onIceDisconnected" );
        workHandler.post(new Runnable() {
            @Override
            public void run() {
                iceConnected = false;
                if (!isError) {
                    isError = true;
                    disconnect(MRTCReason.MRTC_REASON_ICE_FAILURE);
                }
            }
        });
    }

    @Override
    public void onPeerConnectionClosed() {
        Log.d(TAG, "onPeerConnectionClosed" );
    }

    @Override
    public void onPeerConnectionStatsReady(final StatsReport[] reports) {
        workHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!isError && iceConnected) {
                    updateEncoderStatistics(reports);
                }
            }
        });
    }

    @Override
    public void onPeerConnectionError(final String description) {
        Logging.d(TAG, "onPeerConnectionError description " + description);
        reportError(description);
    }

    @Override
    public void onRegistered(final boolean registered) {
        Logging.d(TAG, "onRegistered registered " + registered);
        workHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!registered) {
                    disconnect(MRTCReason.MRTC_REASON_USER_HANGUP);
                }

                synchronized (listeners) {
                    for (OnClientListener listener : listeners) {
                        listener.onLoginSuccessed(registered);
                    }
                }
            }
        });
    }

    @Override
    public void onRegisterFailure(final MRTCReason reason) {
        Logging.d(TAG, "onRegisterFailure reason: " + reason.toString());
        workHandler.post(new Runnable() {
            @Override
            public void run() {
                disconnect(MRTCReason.MRTC_REASON_USER_HANGUP);
                synchronized (listeners) {
                    for (OnClientListener listener : listeners) {
                        listener.onLoginFailure(reason);
                    }
                }
            }
        });
    }

    @Override
    public void onCallRinging(final SessionDescription remote_sdp) {
        Logging.d(TAG, "onCallRinging");

        workHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (listeners) {
                    for (OnClientListener listener : listeners) {
                        listener.onCallRinging();
                    }
                }
            }
        });
    }

    @Override
    public void onCallIncoming(final String fromUser, final SessionDescription remote_sdp) {
        Logging.d(TAG, "onCallIncoming");
        callStartedTimeMs = System.currentTimeMillis();

        workHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (listeners) {
                    for (OnClientListener listener : listeners) {
                        listener.onCallIncoming(fromUser, sipClientAPI.isVideoCall());
                    }
                }
            }
        });
    }

    @Override
    public void onCallConnected(final SessionDescription remoteSdp) {
        Logging.d(TAG, "onCallConnected");

        workHandler.post(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
                    return;
                }

                if (!sipClientAPI.isVideoCall()) {
                    peerConnectionClient.stopVideoSource();
                    peerConnectionClient.setVideoEnabled(false);
                }

                if (sipClientAPI.isInitiator() && remoteSdp != null) {
                    peerConnectionClient.setRemoteDescription(remoteSdp);
                }

                synchronized (listeners) {
                    for (OnClientListener listener : listeners) {
                        listener.onCallConnected(sipClientAPI.isVideoCall());
                    }
                }
            }
        });
    }

    @Override
    public void onCallEnded(final MRTCReason reason) {
        Logging.d(TAG, "onCallEnded(" + reason.toString() + ")");
        workHandler.post(new Runnable() {
            @Override
            public void run() {
                disconnect(MRTCReason.MRTC_REASON_NONE);

                if (!isError) {
                    synchronized (listeners) {
                        for (OnClientListener listener : listeners) {
                            listener.onCallEnded(reason);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onRemoteIceCandidate(final IceCandidate candidate) {
        Logging.d(TAG, "onRemoteIceCandidate");
        workHandler.post(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    return;
                }

                peerConnectionClient.addRemoteIceCandidate(candidate);
            }
        });
    }
}
