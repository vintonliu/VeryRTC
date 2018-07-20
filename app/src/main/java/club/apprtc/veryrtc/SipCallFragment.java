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

import android.app.Activity;
import android.app.Fragment;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.webrtc.RendererCommon.ScalingType;

/**
 * Fragment for call control.
 */
public class SipCallFragment extends Fragment {
  private View controlView;
  private TextView contactView;
  private ImageButton disconnectButton;
  private ImageButton cameraSwitchButton;
  private ImageButton videoScalingButton;
  private ImageButton toggleMuteButton;
  private ImageButton rejectButton;
  private ImageButton connectButton;
  private TextView captureFormatText;
  private SeekBar captureFormatSlider;
  private LinearLayout ll_incoming_container;
  private LinearLayout ll_call_container;
  private OnCallEvents callEvents;
  private ScalingType scalingType;
  private boolean videoCallEnabled = true;
  private boolean outgoing = true;

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    controlView = inflater.inflate(R.layout.sip_fragment_call, container, false);

    // Create UI controls.
    contactView = (TextView) controlView.findViewById(R.id.contact_name_call);
    disconnectButton = (ImageButton) controlView.findViewById(R.id.button_call_disconnect);
    cameraSwitchButton = (ImageButton) controlView.findViewById(R.id.button_call_switch_camera);
    videoScalingButton = (ImageButton) controlView.findViewById(R.id.button_call_scaling_mode);
    toggleMuteButton = (ImageButton) controlView.findViewById(R.id.button_call_toggle_mic);
    rejectButton = (ImageButton) controlView.findViewById(R.id.button_call_reject);
    connectButton = (ImageButton) controlView.findViewById(R.id.button_call_connect);
    captureFormatText = (TextView) controlView.findViewById(R.id.capture_format_text_call);
    captureFormatSlider = (SeekBar) controlView.findViewById(R.id.capture_format_slider_call);
    ll_incoming_container = (LinearLayout) controlView.findViewById(R.id.buttons_incoming_container);
    ll_call_container = (LinearLayout) controlView.findViewById(R.id.buttons_call_container);
    ll_incoming_container.setVisibility(View.GONE);

    // Add buttons click events.
    disconnectButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        callEvents.onCallHangUp();
      }
    });

    cameraSwitchButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        callEvents.onCameraSwitch();
      }
    });

    rejectButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        callEvents.onCallHangUp();
      }
    });

    connectButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ll_incoming_container.setVisibility(View.GONE);
        ll_call_container.setVisibility(View.VISIBLE);
        callEvents.onCallAnswer();
      }
    });

    videoScalingButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (scalingType == ScalingType.SCALE_ASPECT_FILL) {
          videoScalingButton.setBackgroundResource(R.drawable.ic_action_full_screen);
          scalingType = ScalingType.SCALE_ASPECT_FIT;
        } else {
          videoScalingButton.setBackgroundResource(R.drawable.ic_action_return_from_full_screen);
          scalingType = ScalingType.SCALE_ASPECT_FILL;
        }
        callEvents.onVideoScalingSwitch(scalingType);
      }
    });
    scalingType = ScalingType.SCALE_ASPECT_FILL;

    toggleMuteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        boolean enabled = callEvents.onToggleMic();
        toggleMuteButton.setAlpha(enabled ? 1.0f : 0.3f);
      }
    });

    return controlView;
  }

  @Override
  public void onStart() {
    super.onStart();

    boolean captureSliderEnabled = false;
    Bundle args = getArguments();
    if (args != null) {
      String contactName = args.getString(SipCallActivity.EXTRA_USERNAME);
      contactView.setText(contactName);
      videoCallEnabled = args.getBoolean(SipCallActivity.EXTRA_VIDEO_CALL, true);
      captureSliderEnabled = videoCallEnabled
          && args.getBoolean(SipCallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, false);
      outgoing = args.getBoolean(SipCallActivity.EXTRA_OUTGOING);
    }
    if (!videoCallEnabled) {
      cameraSwitchButton.setVisibility(View.INVISIBLE);
    }
    if (captureSliderEnabled) {
      captureFormatSlider.setOnSeekBarChangeListener(
          new CaptureQualityController(captureFormatText, callEvents));
    } else {
      captureFormatText.setVisibility(View.GONE);
      captureFormatSlider.setVisibility(View.GONE);
    }

    if (outgoing) {
      ll_incoming_container.setVisibility(View.GONE);
      ll_call_container.setVisibility(View.GONE);
    } else {
      ll_incoming_container.setVisibility(View.VISIBLE);
      ll_call_container.setVisibility(View.GONE);
    }

  }

  // TODO(sakal): Replace with onAttach(Context) once we only support API level 23+.
  @SuppressWarnings("deprecation")
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    callEvents = (OnCallEvents) activity;
  }
}
