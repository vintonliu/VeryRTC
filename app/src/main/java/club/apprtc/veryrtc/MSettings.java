package club.apprtc.veryrtc;

import android.content.Context;
import android.util.Log;

import org.webrtc.Camera2Enumerator;
import org.webrtc.Logging;
import org.webrtc.PeerConnection;
import org.webrtc.voiceengine.WebRtcAudioUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

class MSettings {
    class MPeerConnectionParameters {
        public boolean loopback;

        /**
         * Video
         */
        public boolean videoCallEnable;
        public boolean screenCaptureEnable;
        public boolean useCamera2;
        public int     videoWidth;
        public int     videoHeight;
        public int     videoFps;
        public boolean videoCQSliderEnable; /* video capture quality slider enable */
        public int     videoMaxBitrate;
        public String  videoCodec;
        public boolean videoHwCodecEnable;
        public boolean videoCaptureTextureEnable;
        public boolean videoFlexFecEnable;
        public boolean videoFileAsCameraEnable = false;
        public String  videoFileAsCamera = "";

        /**
         * Audio
         */
        public int     audioStartBitrate;
        public String  audioCodec;
        public boolean noAudioProcessingEnable;
        public boolean aecdumpEnable;
        public boolean useOpenSLES;
        public boolean disableBuiltInAec;
        public boolean disableBuiltInAgc;
        public boolean disableBuiltInNs;
        public boolean levelCtrlEnable;
        public boolean disableWebrtcAgcAndHpf;
        public String  speakerphone;

        /**
         * Data channel
         */
        public boolean dataChannelEnable;
        public boolean msgOrderEnable;
        public int     maxRetransmitTimeMs;
        public int     maxRetransmitAttempts;
        public String  subProtocol;
        public int     dataId;
        public boolean negotiatedEnable;

        /**
         * Miscellaneous
         */
        public boolean tracing;
        public boolean displayHudEnable;

        /**
         * ICE
         */
        public List<PeerConnection.IceServer> iceServers;
    }

    private final static String TAG = "MSettings";

    public String keyprefVideoCall;
    public String keyprefScreencapture;
    public String keyprefCamera2;
    public String keyprefResolution;
    public String keyprefFps;
    public String keyprefCaptureQualitySlider;
    public String keyprefMaxVideoBitrateType;
    public String keyprefMaxVideoBitrateValue;
    public String keyPrefVideoCodec;
    public String keyprefHwCodec;
    public String keyprefCaptureToTexture;
    public String keyprefFlexfec;

    public String keyprefStartAudioBitrateType;
    public String keyprefStartAudioBitrateValue;
    public String keyPrefAudioCodec;
    public String keyprefNoAudioProcessing;
    public String keyprefAecDump;
    public String keyprefOpenSLES;
    public String keyprefDisableBuiltInAEC;
    public String keyprefDisableBuiltInAGC;
    public String keyprefDisableBuiltInNS;
    public String keyprefEnableLevelControl;
    public String keyprefDisableWebRtcAGCAndHPF;
    public String keyprefSpeakerphone;

    public String keyPrefRoomServerUrl;
    public String keyPrefDisplayHud;
    public String keyPrefTracing;

    public String keyprefEnableDataChannel;
    public String keyprefOrdered;
    public String keyprefMaxRetransmitTimeMs;
    public String keyprefMaxRetransmits;
    public String keyprefDataProtocol;
    public String keyprefNegotiated;
    public String keyprefDataId;

    private SharedPreferencesHelper sharedPreferencesHelper;
    private MPeerConnectionParameters mpcParameters;
    private Context mContext;

    public MSettings(Context context) {
        mContext = context;

        sharedPreferencesHelper = new SharedPreferencesHelper(context, "VeryRTCSettings");
        mpcParameters = new MPeerConnectionParameters();

        keyprefVideoCall = context.getString(R.string.pref_videocall_key);
        keyprefScreencapture = context.getString(R.string.pref_screencapture_key);
        keyprefCamera2 = context.getString(R.string.pref_camera2_key);
        keyprefResolution = context.getString(R.string.pref_resolution_key);
        keyprefFps = context.getString(R.string.pref_fps_key);
        keyprefCaptureQualitySlider = context.getString(R.string.pref_capturequalityslider_key);
        keyprefMaxVideoBitrateType = context.getString(R.string.pref_maxvideobitrate_key);
        keyprefMaxVideoBitrateValue = context.getString(R.string.pref_maxvideobitratevalue_key);
        keyPrefVideoCodec = context.getString(R.string.pref_videocodec_key);
        keyprefHwCodec = context.getString(R.string.pref_hwcodec_key);
        keyprefCaptureToTexture = context.getString(R.string.pref_capturetotexture_key);
        keyprefFlexfec = context.getString(R.string.pref_flexfec_key);

        keyprefStartAudioBitrateType = context.getString(R.string.pref_startaudiobitrate_key);
        keyprefStartAudioBitrateValue = context.getString(R.string.pref_startaudiobitratevalue_key);
        keyPrefAudioCodec = context.getString(R.string.pref_audiocodec_key);
        keyprefNoAudioProcessing = context.getString(R.string.pref_noaudioprocessing_key);
        keyprefAecDump = context.getString(R.string.pref_aecdump_key);
        keyprefOpenSLES = context.getString(R.string.pref_opensles_key);
        keyprefDisableBuiltInAEC = context.getString(R.string.pref_disable_built_in_aec_key);
        keyprefDisableBuiltInAGC = context.getString(R.string.pref_disable_built_in_agc_key);
        keyprefDisableBuiltInNS = context.getString(R.string.pref_disable_built_in_ns_key);
        keyprefEnableLevelControl = context.getString(R.string.pref_enable_level_control_key);
        keyprefDisableWebRtcAGCAndHPF = context.getString(R.string.pref_disable_webrtc_agc_and_hpf_key);
        keyprefSpeakerphone = context.getString(R.string.pref_speakerphone_key);

        keyprefEnableDataChannel = context.getString(R.string.pref_enable_datachannel_key);
        keyprefOrdered = context.getString(R.string.pref_ordered_key);
        keyprefMaxRetransmitTimeMs = context.getString(R.string.pref_max_retransmit_time_ms_key);
        keyprefMaxRetransmits = context.getString(R.string.pref_max_retransmits_key);
        keyprefDataProtocol = context.getString(R.string.pref_data_protocol_key);
        keyprefNegotiated = context.getString(R.string.pref_negotiated_key);
        keyprefDataId = context.getString(R.string.pref_data_id_key);

        keyPrefRoomServerUrl = context.getString(R.string.pref_room_server_url_key);
        keyPrefDisplayHud = context.getString(R.string.pref_displayhud_key);
        keyPrefTracing = context.getString(R.string.pref_tracing_key);

        doInitialize();

        setIceServers();
    }

    private void doInitialize() {
        /* WebRTC video settings */
        sharedPreferencesHelper.put(keyprefVideoCall,
                sharedPreferencesHelper.get(keyprefVideoCall,
                        getResBoolean(R.string.pref_videocall_default)));

        sharedPreferencesHelper.put(keyprefScreencapture,
                sharedPreferencesHelper.get(keyprefScreencapture,
                        getResBoolean(R.string.pref_screencapture_default)));

        sharedPreferencesHelper.put(keyprefCamera2,
                sharedPreferencesHelper.get(keyprefCamera2,
                        getResBoolean(R.string.pref_camera2_default)));

        sharedPreferencesHelper.put(keyprefResolution,
                sharedPreferencesHelper.get(keyprefResolution,
                        getResString(R.string.pref_resolution_default)));

        sharedPreferencesHelper.put(keyprefFps,
                sharedPreferencesHelper.getString(keyprefFps,
                        getResString(R.string.pref_fps_default)));

        sharedPreferencesHelper.put(keyprefCaptureQualitySlider,
                sharedPreferencesHelper.get(keyprefCaptureQualitySlider,
                        getResBoolean(R.string.pref_capturequalityslider_default)));

        sharedPreferencesHelper.put(keyprefMaxVideoBitrateType,
                sharedPreferencesHelper.get(keyprefMaxVideoBitrateType,
                        getResString(R.string.pref_maxvideobitrate_default)));

        sharedPreferencesHelper.put(keyprefMaxVideoBitrateValue,
                sharedPreferencesHelper.get(keyprefMaxVideoBitrateValue,
                        getResInt(R.string.pref_maxvideobitratevalue_default)));

        sharedPreferencesHelper.put(keyPrefVideoCodec,
                sharedPreferencesHelper.get(keyPrefVideoCodec,
                        getResString(R.string.pref_videocodec_default)));

        sharedPreferencesHelper.put(keyprefHwCodec,
                sharedPreferencesHelper.get(keyprefHwCodec,
                        getResBoolean(R.string.pref_hwcodec_default)));

        sharedPreferencesHelper.put(keyprefCaptureToTexture,
                sharedPreferencesHelper.get(keyprefCaptureToTexture,
                        getResBoolean(R.string.pref_capturetotexture_default)));

        sharedPreferencesHelper.put(keyprefFlexfec,
                sharedPreferencesHelper.get(keyprefFlexfec,
                        getResBoolean(R.string.pref_flexfec_default)));

        /* WebRTC audio settings */
        sharedPreferencesHelper.put(keyprefStartAudioBitrateType,
                sharedPreferencesHelper.get(keyprefStartAudioBitrateType,
                        getResString(R.string.pref_startaudiobitrate_default)));

        sharedPreferencesHelper.put(keyprefStartAudioBitrateValue,
                sharedPreferencesHelper.get(keyprefStartAudioBitrateValue,
                        getResInt(R.string.pref_startaudiobitratevalue_default)));

        sharedPreferencesHelper.put(keyPrefAudioCodec,
                sharedPreferencesHelper.get(keyPrefAudioCodec,
                        getResString(R.string.pref_audiocodec_default)));

        sharedPreferencesHelper.put(keyprefNoAudioProcessing,
                sharedPreferencesHelper.get(keyprefNoAudioProcessing,
                        getResBoolean(R.string.pref_noaudioprocessing_default)));

        sharedPreferencesHelper.put(keyprefAecDump,
                sharedPreferencesHelper.get(keyprefAecDump,
                        getResBoolean(R.string.pref_aecdump_default)));

        sharedPreferencesHelper.put(keyprefOpenSLES,
                sharedPreferencesHelper.get(keyprefOpenSLES,
                        getResBoolean(R.string.pref_opensles_default)));

        sharedPreferencesHelper.put(keyprefDisableBuiltInAEC,
                sharedPreferencesHelper.get(keyprefDisableBuiltInAEC,
                        getResBoolean(R.string.pref_disable_built_in_aec_default)));

        sharedPreferencesHelper.put(keyprefDisableBuiltInAGC,
                sharedPreferencesHelper.get(keyprefDisableBuiltInAGC,
                        getResBoolean(R.string.pref_disable_built_in_agc_default)));

        sharedPreferencesHelper.put(keyprefDisableBuiltInNS,
                sharedPreferencesHelper.get(keyprefDisableBuiltInNS,
                        getResBoolean(R.string.pref_disable_built_in_ns_default)));

        sharedPreferencesHelper.put(keyprefEnableLevelControl,
                sharedPreferencesHelper.get(keyprefEnableLevelControl,
                        getResBoolean(R.string.pref_enable_level_control_default)));

        sharedPreferencesHelper.put(keyprefDisableWebRtcAGCAndHPF,
                sharedPreferencesHelper.get(keyprefDisableWebRtcAGCAndHPF,
                        getResBoolean(R.string.pref_disable_webrtc_agc_default)));

        sharedPreferencesHelper.put(keyprefSpeakerphone,
                sharedPreferencesHelper.get(keyprefSpeakerphone,
                        getResString(R.string.pref_speakerphone_default)));

        /* WebRTC data channel settings */
        sharedPreferencesHelper.put(keyprefEnableDataChannel,
                sharedPreferencesHelper.get(keyprefEnableDataChannel,
                        getResBoolean(R.string.pref_enable_datachannel_default)));

        sharedPreferencesHelper.put(keyprefOrdered,
                sharedPreferencesHelper.get(keyprefOrdered,
                        getResBoolean(R.string.pref_ordered_default)));

        sharedPreferencesHelper.put(keyprefMaxRetransmitTimeMs,
                sharedPreferencesHelper.get(keyprefMaxRetransmitTimeMs,
                        getResInt(R.string.pref_max_retransmit_time_ms_default)));

        sharedPreferencesHelper.put(keyprefMaxRetransmits,
                sharedPreferencesHelper.get(keyprefMaxRetransmits,
                        getResInt(R.string.pref_max_retransmits_default)));

        sharedPreferencesHelper.put(keyprefDataProtocol,
                sharedPreferencesHelper.get(keyprefDataProtocol,
                        getResString(R.string.pref_data_protocol_default)));

        sharedPreferencesHelper.put(keyprefNegotiated,
                sharedPreferencesHelper.get(keyprefNegotiated,
                        getResBoolean(R.string.pref_negotiated_default)));

        sharedPreferencesHelper.put(keyprefDataId,
                sharedPreferencesHelper.get(keyprefDataId,
                        getResInt(R.string.pref_data_id_default)));

        /* Miscellaneous settings */
        sharedPreferencesHelper.put(keyPrefDisplayHud,
                sharedPreferencesHelper.get(keyPrefDisplayHud,
                        getResBoolean(R.string.pref_displayhud_default)));

        sharedPreferencesHelper.put(keyPrefTracing,
                sharedPreferencesHelper.get(keyPrefTracing,
                        getResBoolean(R.string.pref_tracing_default)));

        if (!Camera2Enumerator.isSupported(mContext)) {
            sharedPreferencesHelper.put(keyprefCamera2, false);
            Logging.w(TAG, "Camera2 not supported on this device.");
        }

        // Disable forcing WebRTC based AEC so it won't affect our value.
        // Otherwise, if it was enabled, isAcousticEchoCancelerSupported would always return false.
        WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(false);
        if (!WebRtcAudioUtils.isAcousticEchoCancelerSupported()) {
            sharedPreferencesHelper.put(keyprefDisableBuiltInAEC, false);
        }

        WebRtcAudioUtils.setWebRtcBasedAutomaticGainControl(false);
        if (!WebRtcAudioUtils.isAutomaticGainControlSupported()) {
            sharedPreferencesHelper.put(keyprefDisableBuiltInAGC, false);
        }

        WebRtcAudioUtils.setWebRtcBasedNoiseSuppressor(false);
        if (!WebRtcAudioUtils.isNoiseSuppressorSupported()) {
            sharedPreferencesHelper.put(keyprefDisableBuiltInNS, false);
        }

        commit();
    }

    private void commit() {
        mpcParameters.loopback = false;

        mpcParameters.videoCallEnable = sharedPreferencesHelper.getBoolean(keyprefVideoCall,
                                            getResBoolean(R.string.pref_videocall_default));

        mpcParameters.screenCaptureEnable = sharedPreferencesHelper.getBoolean(keyprefScreencapture,
                                            getResBoolean(R.string.pref_screencapture_default));

        mpcParameters.useCamera2 = sharedPreferencesHelper.getBoolean(keyprefCamera2,
                        getResBoolean(R.string.pref_camera2_default));

        String fps = sharedPreferencesHelper.getString(keyprefFps, getResString(R.string.pref_fps_default));
        String[] fpsValues = fps.split("[ x]+");
        int cameraFps = 0;
        if (fpsValues.length == 2) {
            try {
                cameraFps = Integer.parseInt(fpsValues[0]);
            } catch (NumberFormatException e) {
                cameraFps = 0;
                Log.e(TAG, "Wrong camera fps setting: " + fps);
            }
        }
        mpcParameters.videoFps = cameraFps;

        mpcParameters.videoCQSliderEnable = sharedPreferencesHelper.getBoolean(keyprefCaptureQualitySlider,
                        getResBoolean(R.string.pref_capturequalityslider_default));

        mpcParameters.videoMaxBitrate = sharedPreferencesHelper.getInt(keyprefMaxVideoBitrateValue,
                        getResInt(R.string.pref_maxvideobitratevalue_default));

        mpcParameters.videoCodec =  sharedPreferencesHelper.getString(keyPrefVideoCodec,
                        getResString(R.string.pref_videocodec_default));

        mpcParameters.videoHwCodecEnable = sharedPreferencesHelper.getBoolean(keyprefHwCodec,
                        getResBoolean(R.string.pref_hwcodec_default));

        mpcParameters.videoCaptureTextureEnable = sharedPreferencesHelper.getBoolean(keyprefCaptureToTexture,
                        getResBoolean(R.string.pref_capturetotexture_default));

        mpcParameters.videoFlexFecEnable = sharedPreferencesHelper.getBoolean(keyprefFlexfec,
                        getResBoolean(R.string.pref_flexfec_default));

        String resolution = sharedPreferencesHelper.getString(keyprefResolution,
                                getResString(R.string.pref_resolution_default));
        String[] dimensions = resolution.split("[ x]+");
        if (dimensions.length == 2) {
            try {
                mpcParameters.videoWidth = Integer.parseInt(dimensions[0]);
                mpcParameters.videoHeight = Integer.parseInt(dimensions[1]);
            } catch (NumberFormatException e) {
                Logging.e(TAG, "Wrong video resolution setting: " + resolution);
            }
        }

        /* WebRTC audio settings */
        mpcParameters.audioStartBitrate = sharedPreferencesHelper.getInt(keyprefStartAudioBitrateValue,
                        getResInt(R.string.pref_startaudiobitratevalue_default));

        mpcParameters.audioCodec = sharedPreferencesHelper.getString(keyPrefAudioCodec,
                        getResString(R.string.pref_audiocodec_default));

        mpcParameters.noAudioProcessingEnable = sharedPreferencesHelper.getBoolean(keyprefNoAudioProcessing,
                        getResBoolean(R.string.pref_noaudioprocessing_default));

        mpcParameters.aecdumpEnable = sharedPreferencesHelper.getBoolean(keyprefAecDump,
                        getResBoolean(R.string.pref_aecdump_default));

        mpcParameters.useOpenSLES = sharedPreferencesHelper.getBoolean(keyprefOpenSLES,
                        getResBoolean(R.string.pref_opensles_default));

        mpcParameters.disableBuiltInAec = sharedPreferencesHelper.getBoolean(keyprefDisableBuiltInAEC,
                        getResBoolean(R.string.pref_disable_built_in_aec_default));

        mpcParameters.disableBuiltInAgc = sharedPreferencesHelper.getBoolean(keyprefDisableBuiltInAGC,
                        getResBoolean(R.string.pref_disable_built_in_agc_default));

        mpcParameters.disableBuiltInNs = sharedPreferencesHelper.getBoolean(keyprefDisableBuiltInNS,
                        getResBoolean(R.string.pref_disable_built_in_ns_default));

        mpcParameters.levelCtrlEnable = sharedPreferencesHelper.getBoolean(keyprefEnableLevelControl,
                        getResBoolean(R.string.pref_enable_level_control_default));

        mpcParameters.disableWebrtcAgcAndHpf = sharedPreferencesHelper.getBoolean(keyprefDisableWebRtcAGCAndHPF,
                        getResBoolean(R.string.pref_disable_webrtc_agc_default));

        mpcParameters.speakerphone = sharedPreferencesHelper.getString(keyprefSpeakerphone,
                        getResString(R.string.pref_speakerphone_default));

        /* WebRTC data channel settings */
        mpcParameters.dataChannelEnable = sharedPreferencesHelper.getBoolean(keyprefEnableDataChannel,
                        getResBoolean(R.string.pref_enable_datachannel_default));

        mpcParameters.msgOrderEnable = sharedPreferencesHelper.getBoolean(keyprefOrdered,
                        getResBoolean(R.string.pref_ordered_default));

        mpcParameters.maxRetransmitTimeMs = sharedPreferencesHelper.getInt(keyprefMaxRetransmitTimeMs,
                        getResInt(R.string.pref_max_retransmit_time_ms_default));

        mpcParameters.maxRetransmitAttempts = sharedPreferencesHelper.getInt(keyprefMaxRetransmits,
                        getResInt(R.string.pref_max_retransmits_default));

        mpcParameters.subProtocol = sharedPreferencesHelper.getString(keyprefDataProtocol,
                        getResString(R.string.pref_data_protocol_default));

        mpcParameters.negotiatedEnable = sharedPreferencesHelper.getBoolean(keyprefNegotiated,
                        getResBoolean(R.string.pref_negotiated_default));

        mpcParameters.dataId = sharedPreferencesHelper.getInt(keyprefDataId,
                        getResInt(R.string.pref_data_id_default));

        /* Miscellaneous settings */
        mpcParameters.displayHudEnable = sharedPreferencesHelper.getBoolean(keyPrefDisplayHud,
                        getResBoolean(R.string.pref_displayhud_default));

        mpcParameters.tracing = sharedPreferencesHelper.getBoolean(keyPrefTracing,
                        getResBoolean(R.string.pref_tracing_default));

    }

    public boolean update(String key, Object objectValue) {
        if (key.equals(keyprefResolution)) {
            String value = (String)objectValue;
            String[] values = mContext.getResources().getStringArray(R.array.videoResolutionsValues);
            List<String> list = Arrays.asList(values);
            if (!list.contains(value)) {
                return false;
            }
        }

        if (key.equals(keyprefFps)) {
            String value = (String)objectValue;
            String[] values = mContext.getResources().getStringArray(R.array.cameraFps);
            List<String> list = Arrays.asList(values);
            if (!list.contains(value)) {
                return false;
            }
            if (value.equals("Default")) {
                objectValue = 15;
            }
        }

        if (key.equals(keyPrefVideoCodec)) {
            String value = (String)objectValue;
            String[] values = mContext.getResources().getStringArray(R.array.videoCodecs);
            List<String> list = Arrays.asList(values);
            if (!list.contains(value)) {
                return false;
            }
        }

        if (key.equals(keyPrefAudioCodec)) {
            String value = (String)objectValue;
            String[] values = mContext.getResources().getStringArray(R.array.audioCodecs);
            List<String> list = Arrays.asList(values);
            if (!list.contains(value)) {
                return false;
            }
        }

        if (key.equals(keyprefSpeakerphone)) {
            String value = (String)objectValue;
            String[] values = mContext.getResources().getStringArray(R.array.speakerphoneValues);
            List<String> list = Arrays.asList(values);
            if (!list.contains(value)) {
                return false;
            }
        }

        sharedPreferencesHelper.put(key, objectValue);
        commit();

        return true;
    }

    public MPeerConnectionParameters getMpcParameters() {
        return mpcParameters;
    }

    private void setIceServers() {
        LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<PeerConnection.IceServer>();
        PeerConnection.IceServer turnServer =
                PeerConnection.IceServer.builder("turn:120.132.120.136:3478?transport=udp")
                        .setUsername("apprtc1").setPassword("apprtc1")
                        .createIceServer();
//        PeerConnection.IceServer turnServer1 =
//                PeerConnection.IceServer.builder("turn:120.132.120.136:3478?transport=tcp")
//                        .setUsername("apprtc1").setPassword("apprtc1")
//                        .createIceServer();
        PeerConnection.IceServer stunServer =
                PeerConnection.IceServer.builder("stun:120.132.120.136:3478")
                        .createIceServer();

        iceServers.add(turnServer);
//        iceServers.add(turnServer1);
        iceServers.add(stunServer);

        mpcParameters.iceServers = iceServers;
    }

    private boolean getResBoolean(int attributeId) {
        return Boolean.valueOf(mContext.getString(attributeId));
    }

    private int getResInt(int attributeId) {
        return Integer.parseInt(mContext.getString(attributeId));
    }

    private String getResString(int attributeId) {
        return mContext.getString(attributeId);
    }
}
