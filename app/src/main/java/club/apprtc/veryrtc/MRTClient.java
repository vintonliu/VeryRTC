package club.apprtc.veryrtc;

import android.content.Context;

import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

public class MRTClient {
    /* @deprecated  */
    private final static String keyprefVideoCall = "videocall_key";
    /* Disable/enable screen capture for video call */
    public final static String keyprefScreencapture = "screencapture_key";
    /* Disable/enable camera2 if supported */
    public final static String keyprefCamera2 = "camera2_key";
    /* Video capture resolution, valid value: 3840 x 2160, 1920 x 1080, 1280 x 720, 640 x 480, 320 x 240 */
    public final static String keyprefResolution = "resolution_key";
    /* Video capture framerate, valid value: 15, 30 */
    public final static String keyprefFps = "fps_key";
    /* @deprecated */
    private final static String keyprefCaptureQualitySlider = "capturequalityslider_key";
    /* @deprecated */
    private final static String keyprefMaxVideoBitrateType = "maxvideobitrate_key";
    /* Video encoder max bitrate */
    public final static String keyprefMaxVideoBitrateValue = "maxvideobitratevalue_key";
    /* Video encoder codec, valid value: VP8, VP9, H264 Baseline, H264 High */
    public final static String keyPrefVideoCodec = "videocodec_key";
    /* Disable/enable device hardware encoder and decoder for video call */
    public final static String keyprefHwCodec = "hwcodec_key";
    /* Disable/enable capture to texture */
    public final static String keyprefCaptureToTexture = "capturetotexture_key";
    /* Disable/enable Codec-agnostic Flexible FEC for video call */
    public final static String keyprefFlexfec = "flexfec_key";
    /* @deprecated */
    private final static String keyprefStartAudioBitrateType = "startaudiobitrate_key";
    /* Audio start bitrate */
    public final static String keyprefStartAudioBitrateValue = "startaudiobitratevalue_key";
    /* Audio codec, valid value: OPUS, ISAC */
    public final static String keyPrefAudioCodec = "audiocodec_key";
    /* Disable/enable audio processing */
    public final static String keyprefNoAudioProcessing = "audioprocessing_key";
    /* Disable/enable AEC dump test */
    private final static String keyprefAecDump = "aecdump_key";
    /* Disable/enable OpenSl ES for audio playback */
    public final static String keyprefOpenSLES = "opensles_key";
    /* Disable/enable built-in(hardware) AEC */
    public final static String keyprefDisableBuiltInAEC = "disable_built_in_aec_key";
    /* Disable/enable built-in(hardware) AGC */
    public final static String keyprefDisableBuiltInAGC = "disable_built_in_agc_key";
    /* Disable/enable built-in(hardware) NS */
    public final static String keyprefDisableBuiltInNS = "disable_built_in_ns_key";
    /* Disable/enable audio level control */
    public final static String keyprefEnableLevelControl = "enable_level_control_key";
    /* Disable/enable WebRTC AGC and HPF */
    public final static String keyprefDisableWebRtcAGCAndHPF = "disable_webrtc_agc_and_hpf_key";
    /* Set speakerphone work mode, valid value: auto, true, false */
    public final static String keyprefSpeakerphone = "speakerphone_key";

    /* Disable/enable report call statistics period */
    public final static String keyPrefDisplayHud = "displayhud_key";
    /* Disable/enable tracing */
    public final static String keyPrefTracing = "tracing_key";

    /* Disable/enable data channel */
    public final static String keyprefEnableDataChannel = "data_settings_key";
    /* Disable/enable order messages */
    public final static String keyprefOrdered = "ordered_key";
    /* Set data channel max retransmit time in ms */
    public final static String keyprefMaxRetransmitTimeMs = "max_retransmit_time_ms_key";
    /* Set max attempts data channel to retransmit */
    public final static String keyprefMaxRetransmits = "max_retransmits_key";
    /* Set data channel's protocol */
    public final static String keyprefDataProtocol = "Subprotocol_key";
    /* Disable/enable data channel negotiated */
    public final static String keyprefNegotiated = "negotiated_key";
    /* Set data channel Id */
    public final static String keyprefDataId = "data_id_key";

    private MRTCManager mrtcManager;
    private static final MRTClient ourInstance = new MRTClient();

    /**
     * Singleton
     * @return singleton
     */
    public static MRTClient getInstance() {
        return ourInstance;
    }

    /**
     *
     */
    private MRTClient() {

    }

    public String getVersion() {
        String version = "MRTCSDK-" + BuildConfig.BUILD_TYPE + "-V" + BuildConfig.VERSION_NAME;
        return version;
    }

    /**
     * Initialize SDK, must invoke first before other invoke methods
     * @param context Application context
     * @return
     */
    public synchronized boolean doInitialize(Context context) {
        if (mrtcManager != null) {
            return true;
        }

        mrtcManager = new MRTCManager(context);
        return mrtcManager.doInitialize();
    }

    /**
     * Release all sdk resource, must invoke when application quit
     */
    public synchronized void doDispose() {
        if (mrtcManager != null) {
            mrtcManager.close();
        }
    }

    /**
     * Register listener
     * @param listener
     */
    public synchronized boolean setOnClientListener(OnClientListener listener) {
        if (!mrtcManager.isInitialized()) {
            return false;
        }

        mrtcManager.setOnClientListener(listener);
        return true;
    }

    /**
     * UnRegister listener
     * @param listener
     */
    public synchronized boolean removeClientListener(OnClientListener listener) {
        if (!mrtcManager.isInitialized()) {
            return false;
        }

        mrtcManager.removeOnClientListener(listener);
        return true;
    }

    /**
     * Configure the SDK by specific key-value
     * @param key key
     * @param objValue value
     * @return true if success, else false
     */
    public synchronized boolean doSetParameter(String key, Object objValue) {
        if (!mrtcManager.isInitialized()) {
            return false;
        }

        return mrtcManager.doSetParameter(key, objValue);
    }

    /**
     * set video render view, must invoke before @doStartCall and @doAnswerCall
     * to establish video call
     * @param localRender
     * @param remoteRender
     * @return operate success or not
     */
    public synchronized boolean setRenderView(SurfaceViewRenderer localRender,
                                 SurfaceViewRenderer remoteRender) {
        if (!mrtcManager.isInitialized()) {
            return false;
        }
        return mrtcManager.setRenderView(localRender, remoteRender);
    }

    /**
     * Login to proxy server
     * @param proxy sip proxy server address
     * @param nickName sip display name
     * @param userName sip user name
     * @param userPwd sip user password
     * @return operate success or not
     */
    public synchronized boolean doLogin(final String proxy, final String nickName,
                            final String userName, final String userPwd) {
        if (proxy.isEmpty() || nickName.isEmpty() || userName.isEmpty() || userPwd.isEmpty()) {
            return false;
        }

        if (!mrtcManager.isInitialized()) {
            return false;
        }

        return mrtcManager.doLogin(proxy, nickName, userName, userPwd);
    }

    /**
     * Logout from proxy server
     * @return operate success or not
     */
    public synchronized boolean doLogout() {
        if (!mrtcManager.isInitialized()) {
            return false;
        }

        return mrtcManager.doLogout();
    }

    /**
     * Start an audio/video call with @toUser
     * @param toUser peer username
     * @return operate success or not
     */
    public synchronized boolean doStartCall(final String toUser, final boolean videoCall) {
        if (toUser.isEmpty()) {
            return false;
        }
        if (!mrtcManager.isInitialized()) {
            return false;
        }

        return mrtcManager.doStartCall(toUser, videoCall);
    }

    /**
     * Try answer the call, when call connected would  fired @onCallConnected,
     * else fired @onCallEnded if failed
     * @param videoCall true to accept as video call if incoming video call
     * @return operate success or not
     */
    public synchronized boolean doAnswerCall(boolean videoCall) {
        if (!mrtcManager.isInitialized()) {
            return false;
        }

        return mrtcManager.doAnswerCall(videoCall);
    }

    /**
     * Try terminate the call, when call terminated would fired @onCallEnded
     * @return operate success or not
     */
    public synchronized boolean doHangup() {
        if (!mrtcManager.isInitialized()) {
            return false;
        }

        return mrtcManager.doHangup();
    }

    /**
     * Pause video capture
     * @return stop success or not
     */
    public synchronized boolean doVideoPause() {
        if (!mrtcManager.isInitialized()) {
            return false;
        }

        return mrtcManager.doVideoPause();
    }

    /**
     * Start video capture
     * @return start success or not
     */
    public synchronized boolean doVideoResume() {
        if (!mrtcManager.isInitialized()) {
            return false;
        }

        return mrtcManager.doVideoResume();
    }

    /**
     * Toggle Camera between facing front and back
     * @return Toggle success or not
     */
    public synchronized boolean doCameraSwitch() {
        if (!mrtcManager.isInitialized()) {
            return false;
        }

        return mrtcManager.doCameraSwitch();
    }

    /**
     * @deprecated  Change full screen render types of video scaling
     * @param scalingType see detail @RendererCommon.ScalingType
     * @return change success or not
     */
    private synchronized boolean doVideoScalingSwitch(RendererCommon.ScalingType scalingType) {
        if (!mrtcManager.isInitialized()) {
            return false;
        }

        return mrtcManager.doVideoScalingSwitch(scalingType);
    }

    /**
     * Change video encoder output image format
     * @param width encoder output image width
     * @param height encoder output image height
     * @param framerate encoder output image framerate
     * @return change success or not
     */
    public synchronized boolean doCaptureFormatChange(int width, int height, int framerate) {
        if (!mrtcManager.isInitialized()) {
            return false;
        }

        return mrtcManager.doCaptureFormatChange(width, height, framerate);
    }

    /**
     * Toggle mic work or not, peer would not hear any voice if disabled
     * @param enable Muted local audio if disabled
     * @return current mic state
     */
    public synchronized boolean doToggleMic(boolean enable) {
        if (!mrtcManager.isInitialized()) {
            return false;
        }

        return mrtcManager.doToggleMic(enable);
    }

    /**
     * Toggle between Speakerphone and Earpiece
     * @param enable Audio playback to Speakerphone if enabled
     * @return true if Speakerphone used, else false
     */
    public synchronized boolean doToggleSpeaker(boolean enable) {
        if (!mrtcManager.isInitialized()) {
            return false;
        }

        return mrtcManager.doToggleSpeaker(enable);
    }

    /**
     * Toggle speaker mute
     * @param enable user couldn't hear from peer if enabled
     * @return current speaker mute status
     */
    public synchronized boolean doToggleSpeakerMute(boolean enable) {
        if (!mrtcManager.isInitialized()) {
            return false;
        }

        return mrtcManager.doSetSpeakerMute(enable);
    }
}
