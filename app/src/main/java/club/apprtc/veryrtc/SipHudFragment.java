/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package club.apprtc.veryrtc;

import android.app.Fragment;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Fragment for HUD statistics display.
 */
public class SipHudFragment extends Fragment {
  private View controlView;
  private TextView encoderStatView;
  private TextView hudViewBwe;
  private TextView hudViewConnection;
  private TextView hudViewVideoSend;
  private TextView hudViewVideoRecv;
  private ImageButton toggleDebugButton;
  private boolean videoCallEnabled;
  private boolean displayHud;
  private volatile boolean isRunning;

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    controlView = inflater.inflate(R.layout.sip_fragment_hud, container, false);

    // Create UI controls.
    encoderStatView = (TextView) controlView.findViewById(R.id.encoder_stat_call);
    hudViewBwe = (TextView) controlView.findViewById(R.id.hud_stat_bwe);
    hudViewConnection = (TextView) controlView.findViewById(R.id.hud_stat_connection);
    hudViewVideoSend = (TextView) controlView.findViewById(R.id.hud_stat_video_send);
    hudViewVideoRecv = (TextView) controlView.findViewById(R.id.hud_stat_video_recv);
    toggleDebugButton = (ImageButton) controlView.findViewById(R.id.button_toggle_debug);

    toggleDebugButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (displayHud) {
          int visibility =
              (hudViewBwe.getVisibility() == View.VISIBLE) ? View.INVISIBLE : View.VISIBLE;
          hudViewsSetProperties(visibility);
        }
      }
    });

    return controlView;
  }

  @Override
  public void onStart() {
    super.onStart();

    Bundle args = getArguments();
    if (args != null) {
      videoCallEnabled = args.getBoolean(SipCallActivity.EXTRA_VIDEO_CALL, true);
      displayHud = args.getBoolean(SipCallActivity.EXTRA_DISPLAY_HUD, false);
    }
    int visibility = displayHud ? View.VISIBLE : View.INVISIBLE;
    encoderStatView.setVisibility(visibility);
    toggleDebugButton.setVisibility(visibility);
    hudViewsSetProperties(View.INVISIBLE);
    isRunning = true;
  }

  @Override
  public void onStop() {
    isRunning = false;
    super.onStop();
  }

  private void hudViewsSetProperties(int visibility) {
    hudViewBwe.setVisibility(visibility);
    hudViewConnection.setVisibility(visibility);
    hudViewVideoSend.setVisibility(visibility);
    hudViewVideoRecv.setVisibility(visibility);
    hudViewBwe.setTextSize(TypedValue.COMPLEX_UNIT_PT, 5);
    hudViewConnection.setTextSize(TypedValue.COMPLEX_UNIT_PT, 5);
    hudViewVideoSend.setTextSize(TypedValue.COMPLEX_UNIT_PT, 5);
    hudViewVideoRecv.setTextSize(TypedValue.COMPLEX_UNIT_PT, 5);
  }

  public void updateEncoderStatistics(final String encoderStat,
                                      final String bweStat,
                                      final String connectionStat,
                                      final String videoSendStat,
                                      final String videoRecvStat) {
    if (!isRunning || !displayHud) {
      return;
    }

    hudViewBwe.setText(bweStat);
    hudViewConnection.setText(connectionStat);
    hudViewVideoSend.setText(videoSendStat);
    hudViewVideoRecv.setText(videoRecvStat);
    encoderStatView.setText(encoderStat);
  }
}
