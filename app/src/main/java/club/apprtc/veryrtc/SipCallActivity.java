package club.apprtc.veryrtc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

/**
 * Activity for peer connection call setup, call waiting
 * and call view.
 */
public class SipCallActivity extends Activity implements OnClientListener,
        OnCallEvents {
    private static final String TAG = "SipCallActivity";

    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;

    public static final String EXTRA_OUTGOING = "club.apprtc.veryrtc.OUTGOING";
    public static final String EXTRA_USERNAME = "club.apprtc.veryrtc.USERNAME";
    public static final String EXTRA_VIDEO_CALL = "org.appspot.apprtc.VIDEO_CALL";
    public static final String EXTRA_DISPLAY_HUD = "org.appspot.apprtc.DISPLAY_HUD";
    public static final String EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED =
            "org.appsopt.apprtc.VIDEO_CAPTUREQUALITYSLIDER";

    private SurfaceViewRenderer pipRenderer;
    private SurfaceViewRenderer fullscreenRenderer;
    private Toast logToast;
    private boolean activityRunning;
    private boolean callControlFragmentVisible = true;
    private long callStartedTimeMs = 0;
    private boolean isError;
    private boolean micEnabled = true;
    private boolean speakerEnabled = true;
    private boolean screencaptureEnabled = false;
    private boolean videoCall = true;
    private boolean outgoing = true;
    private static Intent mediaProjectionPermissionResultData;
    private static int mediaProjectionPermissionResultCode;

    // Controls
    private SipCallFragment callFragment;
    private SipHudFragment hudFragment;

    private static String remote_user;

    private enum CallState {
        CALL_IDLE,
        CALL_OUTGOING_INIT,
        CALL_OUTGOING_RINGING,
        CALL_OUTPUT_EARLY_RINGING,
        CALL_INCOMING,
        CALL_CONNECTED
    }
    CallState callState = CallState.CALL_IDLE;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler(this));

        // Set window styles for fullscreen-window size. Needs to be done before
        // adding content.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());
        setContentView(R.layout.activity_sip_call);

        // Add to Listener
        MRTClient.getInstance().setOnClientListener(this);

        // Create UI controls.
        pipRenderer = (SurfaceViewRenderer) findViewById(R.id.pip_video_view);
        fullscreenRenderer = (SurfaceViewRenderer) findViewById(R.id.fullscreen_video_view);
        callFragment = new SipCallFragment();
        hudFragment = new SipHudFragment();

        // Show/hide call control fragment on view click.
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleCallControlFragmentVisibility();
            }
        };

        fullscreenRenderer.setOnClickListener(listener);

        final Intent intent = getIntent();

        // Get Intent parameters.
        videoCall = intent.getBooleanExtra(EXTRA_VIDEO_CALL, true);
        outgoing = intent.getBooleanExtra(EXTRA_OUTGOING, true);

        String username = intent.getStringExtra(EXTRA_USERNAME);
        Log.d(TAG, "Callee: " + username);
        if (username == null || username.length() == 0) {
            Log.e(TAG, "Incorrect callee ID in intent!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        remote_user = username;

        // Send intent arguments to fragments.
        callFragment.setArguments(intent.getExtras());
        hudFragment.setArguments(intent.getExtras());
        // Activate call and HUD fragments and start the call.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.call_fragment_container, callFragment);
        ft.add(R.id.hud_fragment_container, hudFragment);
        ft.commit();

        startCall();
    }

    @TargetApi(17)
    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager =
                (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics;
    }

    @TargetApi(19)
    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        return flags;
    }

    @TargetApi(21)
    private void startScreenCapture() {
        MediaProjectionManager mediaProjectionManager =
                (MediaProjectionManager) getApplication().getSystemService(
                        Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE)
            return;
        mediaProjectionPermissionResultCode = resultCode;
        mediaProjectionPermissionResultData = data;
        startCall();
    }

    // Activity interfaces
    @Override
    public void onStop() {
        super.onStop();
        activityRunning = false;

    }

    @Override
    public void onStart() {
        super.onStart();
        activityRunning = true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        MRTClient.getInstance().doVideoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MRTClient.getInstance().doVideoResume();
    }

    @Override
    protected void onDestroy() {
        Thread.setDefaultUncaughtExceptionHandler(null);
        disconnect();
        if (logToast != null) {
            logToast.cancel();
        }
        activityRunning = false;
        super.onDestroy();
    }

    // CallFragment.OnCallEvents interface implementation.

    @Override
    public void onCallAnswer() {
        MRTClient.getInstance().doAnswerCall(videoCall);
    }

    @Override
    public void onCallHangUp() {
        disconnect();
    }

    @Override
    public void onCameraSwitch() {
        MRTClient.getInstance().doCameraSwitch();
    }

    @Override
    public void onVideoScalingSwitch(RendererCommon.ScalingType scalingType) {
        fullscreenRenderer.setScalingType(scalingType);
    }

    @Override
    public void onCaptureFormatChange(int width, int height, int framerate) {
        MRTClient.getInstance().doCaptureFormatChange(width, height, framerate);
    }

    @Override
    public boolean onToggleMic() {

        micEnabled = !micEnabled;
        MRTClient.getInstance().doToggleMic(micEnabled);
        return micEnabled;
    }

    @Override
    public boolean onToggleSpeaker() {
        speakerEnabled = !speakerEnabled;
        MRTClient.getInstance().doToggleSpeaker(speakerEnabled);
        return speakerEnabled;
    }

    // Helper functions.
    private void toggleCallControlFragmentVisibility() {
        if (!callFragment.isAdded()) {
            return;
        }

        // Show/hide call control fragment
        callControlFragmentVisible = !callControlFragmentVisible;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (callControlFragmentVisible) {
            ft.show(callFragment);
            ft.show(hudFragment);
        } else {
            ft.hide(callFragment);
            ft.hide(hudFragment);
        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    private void startCall() {
        callStartedTimeMs = System.currentTimeMillis();
        if (videoCall) {
            MRTClient.getInstance().setRenderView(pipRenderer, fullscreenRenderer);
        }

        if (outgoing) {
            boolean res = MRTClient.getInstance().doStartCall(remote_user, videoCall);
            if (!res) {
                logAndToast("call failure");
                disconnect();
            }
        }
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    private void disconnect() {
        activityRunning = false;
        MRTClient.getInstance().doHangup();
        MRTClient.getInstance().removeClientListener(this);

        finish();
    }

    private void disconnectWithErrorMessage(final String errorMessage) {
        if (!activityRunning) {
            Log.e(TAG, "Critical error: " + errorMessage);
            disconnect();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(getText(R.string.channel_error_title))
                    .setMessage(errorMessage)
                    .setCancelable(false)
                    .setNeutralButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    disconnect();
                                }
                            })
                    .create()
                    .show();
        }
    }

    // Log |msg| and Toast about it.
    private void logAndToast(String msg) {
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        logToast.show();
    }

    private void reportError(final String description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isError) {
                    isError = true;
                    disconnectWithErrorMessage(description);
                }
            }
        });
    }

    // -----Implementation of SipClientListener ---------------
    @Override
    public void onLoginSuccessed(final boolean isLogin) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isLogin) {
                    disconnect();
                    callState = CallState.CALL_IDLE;
                }
            }
        });
    }

    @Override
    public void onLoginFailure(final MRTCReason reason) {
        logAndToast(reason.toString());

        disconnect();
        callState = CallState.CALL_IDLE;
    }

    @Override
    public void onCallRinging() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void onCallIncoming(final String fromUser, final boolean videoCall) {
        callStartedTimeMs = System.currentTimeMillis();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                callState = CallState.CALL_INCOMING;
            }
        });
    }

    @Override
    public void onCallConnected(boolean videoCall) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                callState = CallState.CALL_CONNECTED;
            }
        });
    }

    @Override
    public void onCallEnded(final MRTCReason reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAndToast(reason.toString());
                callState = CallState.CALL_IDLE;
                disconnect();
            }
        });
    }

    @Override
    public void onCallStatsReady(final String encoderStat,
                                 final String bweStat,
                                 final String connectionStat,
                                 final String videoSendStat,
                                 final String videoRecvStat) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isError) {
                    hudFragment.updateEncoderStatistics(encoderStat, bweStat,
                            connectionStat, videoSendStat, videoRecvStat);
                }
            }
        });
    }

    @Override
    public void onClientError(String s) {
        reportError(s);
    }
}
