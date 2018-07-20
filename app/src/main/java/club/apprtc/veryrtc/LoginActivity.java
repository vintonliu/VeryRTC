package club.apprtc.veryrtc;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity implements SipClientListener {
    private final static String TAG = "LoginActivity";
    private static final int REMOVE_FAVORITE_INDEX = 0;
    private static final int CONNECTION_REQUEST = 1;

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 101;
    private static final int MY_PERMISSIONS_REQUEST = 102;

    private EditText edtUserName, edtPassword, edtCallee;
    private LinearLayout llLoginPanel, llLogonPanel;
    private TextView tvUsername, tvLoginTip;

    private ListView roomListView;
    private SharedPreferences sharedPref;
    private String keyprefRoomServerUrl;
    private String keyprefVideoCallEnabled;
    private String keyprefScreencapture;
    private String keyprefCamera2;
    private String keyprefResolution;
    private String keyprefFps;
    private String keyprefCaptureQualitySlider;
    private String keyprefVideoBitrateType;
    private String keyprefVideoBitrateValue;
    private String keyprefVideoCodec;
    private String keyprefAudioBitrateType;
    private String keyprefAudioBitrateValue;
    private String keyprefAudioCodec;
    private String keyprefHwCodecAcceleration;
    private String keyprefCaptureToTexture;
    private String keyprefFlexfec;
    private String keyprefNoAudioProcessingPipeline;
    private String keyprefAecDump;
    private String keyprefOpenSLES;
    private String keyprefDisableBuiltInAec;
    private String keyprefDisableBuiltInAgc;
    private String keyprefDisableBuiltInNs;
    private String keyprefEnableLevelControl;
    private String keyprefDisableWebRtcAGCAndHPF;
    private String keyprefDisplayHud;
    private String keyprefTracing;
    private String keyprefRoom;
    private String keyprefRoomList;
    private ArrayList<String> roomList;
    private ArrayAdapter<String> adapter;
    private String keyprefEnableDataChannel;
    private String keyprefOrdered;
    private String keyprefMaxRetransmitTimeMs;
    private String keyprefMaxRetransmits;
    private String keyprefDataProtocol;
    private String keyprefNegotiated;
    private String keyprefDataId;
    private String keyprefSipUsername;
    private String keyprefSipUserpwd;

    private static SipClientAPI sipClientAPI;

    private boolean isRegistered = false;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = this;

        // Get setting keys.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        keyprefVideoCallEnabled = getString(R.string.pref_videocall_key);
        keyprefScreencapture = getString(R.string.pref_screencapture_key);
        keyprefCamera2 = getString(R.string.pref_camera2_key);
        keyprefResolution = getString(R.string.pref_resolution_key);
        keyprefFps = getString(R.string.pref_fps_key);
        keyprefCaptureQualitySlider = getString(R.string.pref_capturequalityslider_key);
        keyprefVideoBitrateType = getString(R.string.pref_maxvideobitrate_key);
        keyprefVideoBitrateValue = getString(R.string.pref_maxvideobitratevalue_key);
        keyprefVideoCodec = getString(R.string.pref_videocodec_key);
        keyprefHwCodecAcceleration = getString(R.string.pref_hwcodec_key);
        keyprefCaptureToTexture = getString(R.string.pref_capturetotexture_key);
        keyprefFlexfec = getString(R.string.pref_flexfec_key);
        keyprefAudioBitrateType = getString(R.string.pref_startaudiobitrate_key);
        keyprefAudioBitrateValue = getString(R.string.pref_startaudiobitratevalue_key);
        keyprefAudioCodec = getString(R.string.pref_audiocodec_key);
        keyprefNoAudioProcessingPipeline = getString(R.string.pref_noaudioprocessing_key);
        keyprefAecDump = getString(R.string.pref_aecdump_key);
        keyprefOpenSLES = getString(R.string.pref_opensles_key);
        keyprefDisableBuiltInAec = getString(R.string.pref_disable_built_in_aec_key);
        keyprefDisableBuiltInAgc = getString(R.string.pref_disable_built_in_agc_key);
        keyprefDisableBuiltInNs = getString(R.string.pref_disable_built_in_ns_key);
        keyprefEnableLevelControl = getString(R.string.pref_enable_level_control_key);
        keyprefDisableWebRtcAGCAndHPF = getString(R.string.pref_disable_webrtc_agc_and_hpf_key);
        keyprefDisplayHud = getString(R.string.pref_displayhud_key);
        keyprefTracing = getString(R.string.pref_tracing_key);
        keyprefRoomServerUrl = getString(R.string.pref_room_server_url_key);
        keyprefRoom = getString(R.string.pref_room_key);
        keyprefRoomList = getString(R.string.pref_room_list_key);
        keyprefEnableDataChannel = getString(R.string.pref_enable_datachannel_key);
        keyprefOrdered = getString(R.string.pref_ordered_key);
        keyprefMaxRetransmitTimeMs = getString(R.string.pref_max_retransmit_time_ms_key);
        keyprefMaxRetransmits = getString(R.string.pref_max_retransmits_key);
        keyprefDataProtocol = getString(R.string.pref_data_protocol_key);
        keyprefNegotiated = getString(R.string.pref_negotiated_key);
        keyprefDataId = getString(R.string.pref_data_id_key);
        keyprefSipUsername = getString(R.string.pref_sip_username);
        keyprefSipUserpwd = getString(R.string.pref_sip_userpwd);

        edtUserName = (EditText)findViewById(R.id.edtUserName);
        edtPassword = (EditText)findViewById(R.id.edtPassword);
        edtCallee = (EditText) findViewById(R.id.edtCallee);
        tvUsername = (TextView) findViewById(R.id.tvUsername);
        tvLoginTip = (TextView) findViewById(R.id.tvLoginTip);
        tvLoginTip.setVisibility(View.GONE);
        llLoginPanel = (LinearLayout)findViewById(R.id.llLoginPanel);
        llLogonPanel = (LinearLayout)findViewById(R.id.llLogonPannel);
        llLogonPanel.setVisibility(View.GONE);

        roomListView = (ListView) findViewById(R.id.room_listview);
        roomListView.setEmptyView(findViewById(android.R.id.empty));
        roomListView.setOnItemClickListener(roomListClickListener);
        registerForContextMenu(roomListView);

        sipClientAPI = new SipClientAPI(this);

        askForPermisssions();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(keyprefRoomServerUrl, getString(R.string.pref_proxy_server_url_default));
        editor.commit();
    }

    private AdapterView.OnItemClickListener roomListClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            String callee = ((TextView)view).getText().toString();
            connectToCall(true, callee, null);
        }
    };

    public void askForPermisssions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }

    private boolean arePermissionGranted() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        if (!isRegistered) {
//            getMenuInflater().inflate(R.menu.connect_menu, menu);
//        } else {
            getMenuInflater().inflate(R.menu.logon_menu, menu);
//        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items.
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            doLogout();
            return true;
        }else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.room_listview) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(roomList.get(info.position));
            String[] menuItems = getResources().getStringArray(R.array.roomListContextMenu);
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        } else {
            super.onCreateContextMenu(menu, v, menuInfo);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == REMOVE_FAVORITE_INDEX) {
            AdapterView.AdapterContextMenuInfo info =
                    (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            roomList.remove(info.position);
            adapter.notifyDataSetChanged();
            return true;
        }

        return super.onContextItemSelected(item);
    }

    public void doLogin(View view) {
        String username = edtUserName.getText().toString();
        String password = edtPassword.getText().toString();
        String proxy = sharedPref.getString(keyprefRoomServerUrl, getString(R.string.pref_proxy_server_url_default));
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.error_field_required, Toast.LENGTH_LONG);
            return;
        }

        tvLoginTip.setVisibility(View.VISIBLE);
        sipClientAPI.doRegister(proxy, username, username, password);
    }

    public void doLogout() {
        if (isRegistered) {
            sipClientAPI.doUnRegister();
        }
    }

    public void doStartCall(View view) {
        String callee = edtCallee.getText().toString();
        if (callee.isEmpty()) {
            Toast.makeText(mContext, "Callee invalid", Toast.LENGTH_LONG);
            return;
        }
        
        if (!roomList.contains(callee)) {
            adapter.add(callee);
            adapter.notifyDataSetChanged();
        }
        connectToCall(true, callee, null);
    }

    private void connectToCall(boolean outgoing, String remote, SessionDescription remote_sdp) {
        if (!isRegistered ||
            remote.isEmpty() ||
            !arePermissionGranted()) {
            return;
        }
        boolean useValuesFromIntent = false;

        // Video call enabled flag.
        boolean videoCallEnabled = sharedPrefGetBoolean(R.string.pref_videocall_key,
                SipCallActivity.EXTRA_VIDEO_CALL, R.string.pref_videocall_default, useValuesFromIntent);

        // Use screencapture option.
        boolean useScreencapture = sharedPrefGetBoolean(R.string.pref_screencapture_key,
                SipCallActivity.EXTRA_SCREENCAPTURE, R.string.pref_screencapture_default, useValuesFromIntent);

        // Use Camera2 option.
        boolean useCamera2 = sharedPrefGetBoolean(R.string.pref_camera2_key, SipCallActivity.EXTRA_CAMERA2,
                R.string.pref_camera2_default, useValuesFromIntent);

        // Get default codecs.
        String videoCodec = sharedPrefGetString(R.string.pref_videocodec_key,
                SipCallActivity.EXTRA_VIDEOCODEC, R.string.pref_videocodec_default, useValuesFromIntent);
        String audioCodec = sharedPrefGetString(R.string.pref_audiocodec_key,
                SipCallActivity.EXTRA_AUDIOCODEC, R.string.pref_audiocodec_default, useValuesFromIntent);

        // Check HW codec flag.
        boolean hwCodec = sharedPrefGetBoolean(R.string.pref_hwcodec_key,
                SipCallActivity.EXTRA_HWCODEC_ENABLED, R.string.pref_hwcodec_default, useValuesFromIntent);

        // Check Capture to texture.
        boolean captureToTexture = sharedPrefGetBoolean(R.string.pref_capturetotexture_key,
                SipCallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, R.string.pref_capturetotexture_default,
                useValuesFromIntent);

        // Check FlexFEC.
        boolean flexfecEnabled = sharedPrefGetBoolean(R.string.pref_flexfec_key,
                SipCallActivity.EXTRA_FLEXFEC_ENABLED, R.string.pref_flexfec_default, useValuesFromIntent);

        // Check Disable Audio Processing flag.
        boolean noAudioProcessing = sharedPrefGetBoolean(R.string.pref_noaudioprocessing_key,
                SipCallActivity.EXTRA_NOAUDIOPROCESSING_ENABLED, R.string.pref_noaudioprocessing_default,
                useValuesFromIntent);

        // Check Disable Audio Processing flag.
        boolean aecDump = sharedPrefGetBoolean(R.string.pref_aecdump_key,
                SipCallActivity.EXTRA_AECDUMP_ENABLED, R.string.pref_aecdump_default, useValuesFromIntent);

        // Check OpenSL ES enabled flag.
        boolean useOpenSLES = sharedPrefGetBoolean(R.string.pref_opensles_key,
                SipCallActivity.EXTRA_OPENSLES_ENABLED, R.string.pref_opensles_default, useValuesFromIntent);

        // Check Disable built-in AEC flag.
        boolean disableBuiltInAEC = sharedPrefGetBoolean(R.string.pref_disable_built_in_aec_key,
                SipCallActivity.EXTRA_DISABLE_BUILT_IN_AEC, R.string.pref_disable_built_in_aec_default,
                useValuesFromIntent);

        // Check Disable built-in AGC flag.
        boolean disableBuiltInAGC = sharedPrefGetBoolean(R.string.pref_disable_built_in_agc_key,
                SipCallActivity.EXTRA_DISABLE_BUILT_IN_AGC, R.string.pref_disable_built_in_agc_default,
                useValuesFromIntent);

        // Check Disable built-in NS flag.
        boolean disableBuiltInNS = sharedPrefGetBoolean(R.string.pref_disable_built_in_ns_key,
                SipCallActivity.EXTRA_DISABLE_BUILT_IN_NS, R.string.pref_disable_built_in_ns_default,
                useValuesFromIntent);

        // Check Enable level control.
        boolean enableLevelControl = sharedPrefGetBoolean(R.string.pref_enable_level_control_key,
                SipCallActivity.EXTRA_ENABLE_LEVEL_CONTROL, R.string.pref_enable_level_control_key,
                useValuesFromIntent);

        // Check Disable gain control
        boolean disableWebRtcAGCAndHPF = sharedPrefGetBoolean(
                R.string.pref_disable_webrtc_agc_and_hpf_key, SipCallActivity.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF,
                R.string.pref_disable_webrtc_agc_and_hpf_key, useValuesFromIntent);

        // Get video resolution from settings.
        int videoWidth = 0;
        int videoHeight = 0;
        if (useValuesFromIntent) {
            videoWidth = getIntent().getIntExtra(SipCallActivity.EXTRA_VIDEO_WIDTH, 0);
            videoHeight = getIntent().getIntExtra(SipCallActivity.EXTRA_VIDEO_HEIGHT, 0);
        }
        if (videoWidth == 0 && videoHeight == 0) {
            String resolution =
                    sharedPref.getString(keyprefResolution, getString(R.string.pref_resolution_default));
            String[] dimensions = resolution.split("[ x]+");
            if (dimensions.length == 2) {
                try {
                    videoWidth = Integer.parseInt(dimensions[0]);
                    videoHeight = Integer.parseInt(dimensions[1]);
                } catch (NumberFormatException e) {
                    videoWidth = 0;
                    videoHeight = 0;
                    Log.e(TAG, "Wrong video resolution setting: " + resolution);
                }
            }
        }

        // Get camera fps from settings.
        int cameraFps = 0;
        if (useValuesFromIntent) {
            cameraFps = getIntent().getIntExtra(SipCallActivity.EXTRA_VIDEO_FPS, 0);
        }
        if (cameraFps == 0) {
            String fps = sharedPref.getString(keyprefFps, getString(R.string.pref_fps_default));
            String[] fpsValues = fps.split("[ x]+");
            if (fpsValues.length == 2) {
                try {
                    cameraFps = Integer.parseInt(fpsValues[0]);
                } catch (NumberFormatException e) {
                    cameraFps = 0;
                    Log.e(TAG, "Wrong camera fps setting: " + fps);
                }
            }
        }

        // Check capture quality slider flag.
        boolean captureQualitySlider = sharedPrefGetBoolean(R.string.pref_capturequalityslider_key,
                SipCallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED,
                R.string.pref_capturequalityslider_default, useValuesFromIntent);

        // Get video and audio start bitrate.
        int videoStartBitrate = 0;
        if (useValuesFromIntent) {
            videoStartBitrate = getIntent().getIntExtra(SipCallActivity.EXTRA_VIDEO_BITRATE, 0);
        }
        if (videoStartBitrate == 0) {
            String bitrateTypeDefault = getString(R.string.pref_maxvideobitrate_default);
            String bitrateType = sharedPref.getString(keyprefVideoBitrateType, bitrateTypeDefault);
            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue = sharedPref.getString(
                        keyprefVideoBitrateValue, getString(R.string.pref_maxvideobitratevalue_default));
                videoStartBitrate = Integer.parseInt(bitrateValue);
            }
        }

        int audioStartBitrate = 0;
        if (useValuesFromIntent) {
            audioStartBitrate = getIntent().getIntExtra(SipCallActivity.EXTRA_AUDIO_BITRATE, 0);
        }
        if (audioStartBitrate == 0) {
            String bitrateTypeDefault = getString(R.string.pref_startaudiobitrate_default);
            String bitrateType = sharedPref.getString(keyprefAudioBitrateType, bitrateTypeDefault);
            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue = sharedPref.getString(
                        keyprefAudioBitrateValue, getString(R.string.pref_startaudiobitratevalue_default));
                audioStartBitrate = Integer.parseInt(bitrateValue);
            }
        }

        // Check statistics display option.
        boolean displayHud = sharedPrefGetBoolean(R.string.pref_displayhud_key,
                SipCallActivity.EXTRA_DISPLAY_HUD, R.string.pref_displayhud_default, useValuesFromIntent);

        boolean tracing = sharedPrefGetBoolean(R.string.pref_tracing_key, SipCallActivity.EXTRA_TRACING,
                R.string.pref_tracing_default, useValuesFromIntent);

        // Get datachannel options
        boolean dataChannelEnabled = sharedPrefGetBoolean(R.string.pref_enable_datachannel_key,
                SipCallActivity.EXTRA_DATA_CHANNEL_ENABLED, R.string.pref_enable_datachannel_default,
                useValuesFromIntent);
        boolean ordered = sharedPrefGetBoolean(R.string.pref_ordered_key, SipCallActivity.EXTRA_ORDERED,
                R.string.pref_ordered_default, useValuesFromIntent);
        boolean negotiated = sharedPrefGetBoolean(R.string.pref_negotiated_key,
                SipCallActivity.EXTRA_NEGOTIATED, R.string.pref_negotiated_default, useValuesFromIntent);
        int maxRetrMs = sharedPrefGetInteger(R.string.pref_max_retransmit_time_ms_key,
                SipCallActivity.EXTRA_MAX_RETRANSMITS_MS, R.string.pref_max_retransmit_time_ms_default,
                useValuesFromIntent);
        int maxRetr =
                sharedPrefGetInteger(R.string.pref_max_retransmits_key, SipCallActivity.EXTRA_MAX_RETRANSMITS,
                        R.string.pref_max_retransmits_default, useValuesFromIntent);
        int id = sharedPrefGetInteger(R.string.pref_data_id_key, SipCallActivity.EXTRA_ID,
                R.string.pref_data_id_default, useValuesFromIntent);
        String protocol = sharedPrefGetString(R.string.pref_data_protocol_key,
                SipCallActivity.EXTRA_PROTOCOL, R.string.pref_data_protocol_default, useValuesFromIntent);

        // Start AppRTCMobile activity.
        Log.d(TAG, "Connecting to remote " + remote);

        Intent intent = new Intent(this, SipCallActivity.class);

        intent.putExtra(SipCallActivity.EXTRA_OUTGOING, outgoing);
        intent.putExtra(SipCallActivity.EXTRA_USERNAME, remote);
        if (remote_sdp != null) {
            intent.putExtra(SipCallActivity.EXTRA_REMOTE_SDP, remote_sdp);
        }
        intent.putExtra(SipCallActivity.EXTRA_LOOPBACK, false);
        intent.putExtra(SipCallActivity.EXTRA_VIDEO_CALL, videoCallEnabled);
        intent.putExtra(SipCallActivity.EXTRA_SCREENCAPTURE, useScreencapture);
        intent.putExtra(SipCallActivity.EXTRA_CAMERA2, useCamera2);
        intent.putExtra(SipCallActivity.EXTRA_VIDEO_WIDTH, videoWidth);
        intent.putExtra(SipCallActivity.EXTRA_VIDEO_HEIGHT, videoHeight);
        intent.putExtra(SipCallActivity.EXTRA_VIDEO_FPS, cameraFps);
        intent.putExtra(SipCallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, captureQualitySlider);
        intent.putExtra(SipCallActivity.EXTRA_VIDEO_BITRATE, videoStartBitrate);
        intent.putExtra(SipCallActivity.EXTRA_VIDEOCODEC, videoCodec);
        intent.putExtra(SipCallActivity.EXTRA_HWCODEC_ENABLED, hwCodec);
        intent.putExtra(SipCallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, captureToTexture);
        intent.putExtra(SipCallActivity.EXTRA_FLEXFEC_ENABLED, flexfecEnabled);
        intent.putExtra(SipCallActivity.EXTRA_NOAUDIOPROCESSING_ENABLED, noAudioProcessing);
        intent.putExtra(SipCallActivity.EXTRA_AECDUMP_ENABLED, aecDump);
        intent.putExtra(SipCallActivity.EXTRA_OPENSLES_ENABLED, useOpenSLES);
        intent.putExtra(SipCallActivity.EXTRA_DISABLE_BUILT_IN_AEC, disableBuiltInAEC);
        intent.putExtra(SipCallActivity.EXTRA_DISABLE_BUILT_IN_AGC, disableBuiltInAGC);
        intent.putExtra(SipCallActivity.EXTRA_DISABLE_BUILT_IN_NS, disableBuiltInNS);
        intent.putExtra(SipCallActivity.EXTRA_ENABLE_LEVEL_CONTROL, enableLevelControl);
        intent.putExtra(SipCallActivity.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, disableWebRtcAGCAndHPF);
        intent.putExtra(SipCallActivity.EXTRA_AUDIO_BITRATE, audioStartBitrate);
        intent.putExtra(SipCallActivity.EXTRA_AUDIOCODEC, audioCodec);
        intent.putExtra(SipCallActivity.EXTRA_DISPLAY_HUD, displayHud);
        intent.putExtra(SipCallActivity.EXTRA_TRACING, tracing);
        intent.putExtra(SipCallActivity.EXTRA_CMDLINE, false);
        intent.putExtra(SipCallActivity.EXTRA_RUNTIME, 0);

        intent.putExtra(SipCallActivity.EXTRA_DATA_CHANNEL_ENABLED, dataChannelEnabled);

        if (dataChannelEnabled) {
            intent.putExtra(SipCallActivity.EXTRA_ORDERED, ordered);
            intent.putExtra(SipCallActivity.EXTRA_MAX_RETRANSMITS_MS, maxRetrMs);
            intent.putExtra(SipCallActivity.EXTRA_MAX_RETRANSMITS, maxRetr);
            intent.putExtra(SipCallActivity.EXTRA_PROTOCOL, protocol);
            intent.putExtra(SipCallActivity.EXTRA_NEGOTIATED, negotiated);
            intent.putExtra(SipCallActivity.EXTRA_ID, id);
        }

        startActivityForResult(intent, CONNECTION_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    private String sharedPrefGetString(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        String defaultValue = getString(defaultId);
        if (useFromIntent) {
            String value = getIntent().getStringExtra(intentName);
            if (value != null) {
                return value;
            }
            return defaultValue;
        } else {
            String attributeName = getString(attributeId);
            return sharedPref.getString(attributeName, defaultValue);
        }
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    private boolean sharedPrefGetBoolean(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        boolean defaultValue = Boolean.valueOf(getString(defaultId));
        if (useFromIntent) {
            return getIntent().getBooleanExtra(intentName, defaultValue);
        } else {
            String attributeName = getString(attributeId);
            return sharedPref.getBoolean(attributeName, defaultValue);
        }
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    private int sharedPrefGetInteger(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        String defaultString = getString(defaultId);
        int defaultValue = Integer.parseInt(defaultString);
        if (useFromIntent) {
            return getIntent().getIntExtra(intentName, defaultValue);
        } else {
            String attributeName = getString(attributeId);
            String value = sharedPref.getString(attributeName, defaultString);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Wrong setting for: " + attributeName + ":" + value);
                return defaultValue;
            }
        }
    }

    @Override
    public void onRegistered(boolean registered) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvLoginTip.setVisibility(View.GONE);

                if (registered) {
                    tvUsername.setText(edtUserName.getText().toString());
                    llLoginPanel.setVisibility(View.GONE);
                    llLogonPanel.setVisibility(View.VISIBLE);
                } else {
                    llLogonPanel.setVisibility(View.GONE);
                    llLoginPanel.setVisibility(View.VISIBLE);
                }
                isRegistered = registered;
            }
        });
    }

    @Override
    public void onRegisterFailure(int reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvLoginTip.setVisibility(View.GONE);
                llLogonPanel.setVisibility(View.GONE);
                llLoginPanel.setVisibility(View.VISIBLE);

                isRegistered = false;
                Toast.makeText(mContext, "Login failed", Toast.LENGTH_LONG);
            }
        });
    }

    @Override
    public void onCallRinging(final SessionDescription remote_sdp) {

    }

    @Override
    public void onCallIncoming(String from, final SessionDescription remote_sdp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (sipClientAPI == null) {
                    Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
                    return;
                }
                if (remote_sdp != null) {
                    connectToCall(false, from, remote_sdp);
                }
            }
        });
    }

    @Override
    public void onCallConnected(final SessionDescription remote_sdp) {

    }

    @Override
    public void onCallEnded() {

    }

    @Override
    public void onCallFailure(int reason) {

    }

    @Override
    public void onRemoteIceCandidate(final IceCandidate candidate) {

    }

    public static SipClientAPI getSipClientAPI() {
        return sipClientAPI;
    }

    @Override
    protected void onPause() {
        super.onPause();
        String username = edtUserName.getText().toString();
        String userpwd = edtPassword.getText().toString();
        String calleeListJson = new JSONArray(roomList).toString();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(keyprefSipUsername, username);
        editor.putString(keyprefSipUserpwd, userpwd);
        editor.putString(keyprefRoomList, calleeListJson);
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String username = sharedPref.getString(keyprefSipUsername, "");
        String userpwd = sharedPref.getString(keyprefSipUserpwd, "");
        edtUserName.setText(username);
        edtPassword.setText(userpwd);

        String calleeListJson = sharedPref.getString(keyprefRoomList, null);
        roomList = new ArrayList<String>();
        if (calleeListJson != null) {
            try {
                JSONArray jsonArray = new JSONArray(calleeListJson);
                for (int i = 0; i < jsonArray.length(); i++) {
                    roomList.add(jsonArray.get(i).toString());
                }
            } catch (JSONException e) {
                Log.e(TAG, "Failed to load room list: " + e.toString());
            }
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roomList);
        roomListView.setAdapter(adapter);
        if (adapter.getCount() > 0) {
            roomListView.requestFocus();
            roomListView.setItemChecked(0, true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sipClientAPI.doDispose();
        sipClientAPI = null;
    }
}
