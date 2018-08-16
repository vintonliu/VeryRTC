package club.apprtc.veryrtc;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity implements OnClientListener {
    private final static String TAG = "LoginActivity";
    private static final int REMOVE_FAVORITE_INDEX = 0;
    private static final int AUDIO_CALL_INDEX = 1;
    private static final int VIDEO_CALL_INDEX = 2;
    private static final int CONNECTION_REQUEST = 1;

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 101;
    private static final int MY_PERMISSIONS_REQUEST = 102;
    private static final int MY_PERMISSIONS_STOREAGE_WRITE = 1003;

    private EditText edtUserName, edtPassword, edtCallee;
    private LinearLayout llLoginPanel, llLogonPanel;
    private TextView tvUsername, tvLoginTip;

    private ListView roomListView;
    private SharedPreferences sharedPref;
    private String keyprefRoomServerUrl;

    private String keyprefRoomList;
    private ArrayList<String> roomList;
    private ArrayAdapter<String> adapter;
    private String keyprefSipUsername;
    private String keyprefSipUserpwd;

    private boolean isRegistered = false;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mContext = this;

        // Get setting keys.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        keyprefRoomServerUrl = getString(R.string.pref_room_server_url_key);
        keyprefRoomList = getString(R.string.pref_room_list_key);
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

        MRTClient.getInstance().doInitialize(getApplicationContext());
        MRTClient.getInstance().setOnClientListener(this);
        Log.i(TAG, "MRTCSDK Version: " + MRTClient.getInstance().getVersion());

        askForPermisssions();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putString(keyprefRoomServerUrl, getString(R.string.pref_proxy_server_url_default));
//        editor.commit();
    }

    private AdapterView.OnItemClickListener roomListClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            String callee = ((TextView)view).getText().toString();
            connectToCall(true, callee, true);
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
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_STOREAGE_WRITE);
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
        } else if (item.getItemId() == AUDIO_CALL_INDEX) {
            AdapterView.AdapterContextMenuInfo info =
                    (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            String callee = roomList.get(info.position);
            connectToCall(true, callee, false);
            return true;
        } else if (item.getItemId() == VIDEO_CALL_INDEX) {
            AdapterView.AdapterContextMenuInfo info =
                    (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            String callee = roomList.get(info.position);
            connectToCall(true, callee, true);
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

        MRTClient.getInstance().doLogin(proxy, username, username, password);
    }

    public void doLogout() {
        if (isRegistered) {
            MRTClient.getInstance().doLogout();
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
        connectToCall(true, callee, true);
    }

    private void connectToCall(boolean outgoing, String remote, final boolean videoCall) {
        if (!isRegistered ||
            remote.isEmpty() ||
            !arePermissionGranted()) {
            return;
        }

        // Start AppRTCMobile activity.
        Log.d(TAG, "Connecting to remote: " + remote + " isVideo: " + videoCall);

        // Check statistics display option.
        boolean displayHud = sharedPref.getBoolean(getString(R.string.pref_displayhud_key), false);

        // Check capture quality slider flag.
        boolean captureQualitySlider = sharedPref.getBoolean(getString(R.string.pref_capturequalityslider_key), false);

        Intent intent = new Intent(this, SipCallActivity.class);

        intent.putExtra(SipCallActivity.EXTRA_OUTGOING, outgoing);
        intent.putExtra(SipCallActivity.EXTRA_USERNAME, remote);
        intent.putExtra(SipCallActivity.EXTRA_VIDEO_CALL, videoCall);
        intent.putExtra(SipCallActivity.EXTRA_DISPLAY_HUD, displayHud);
        intent.putExtra(SipCallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, captureQualitySlider);

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
    public void onLoginSuccessed(final boolean isLogin) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvLoginTip.setVisibility(View.GONE);

                if (isLogin) {
                    tvUsername.setText(edtUserName.getText().toString());
                    llLoginPanel.setVisibility(View.GONE);
                    llLogonPanel.setVisibility(View.VISIBLE);
                } else {
                    llLogonPanel.setVisibility(View.GONE);
                    llLoginPanel.setVisibility(View.VISIBLE);
                }
                isRegistered = isLogin;
            }
        });
    }

    @Override
    public void onLoginFailure(final MRTCReason reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                tvLoginTip.setVisibility(View.GONE);
                tvLoginTip.setText(reason.toString());
                llLogonPanel.setVisibility(View.GONE);
                llLoginPanel.setVisibility(View.VISIBLE);

                isRegistered = false;
                Toast.makeText(mContext, "Login failed with " + reason.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onCallRinging() {

    }

    @Override
    public void onCallIncoming(final String fromUser, final boolean videoCall) {
        Log.i(TAG, "onCallIncoming fromUser " + fromUser + " videoCall " + videoCall);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectToCall(false, fromUser, videoCall);
            }
        });
    }

    @Override
    public void onCallConnected(boolean videoCall) {

    }

    @Override
    public void onCallEnded(final MRTCReason reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, reason.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onCallStatsReady(String s, String s1, String s2, String s3, String s4) {

    }

    @Override
    public void onClientError(String s) {

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
        MRTClient.getInstance().doDispose();
    }
}
