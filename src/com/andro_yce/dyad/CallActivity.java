package com.andro_yce.dyad;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;

import java.util.List;

public class CallActivity extends ActionBarActivity {

	private static final String APP_KEY = "8d7e15f8-d962-4d15-8a8e-c13086912723";
	private static final String APP_SECRET = "fPMYrulMGUuKxKQz3BOAGg==";
	private static final String ENVIRONMENT = "sandbox.sinch.com";
	private Call call;
	private TextView callState, callAction;
	private SinchClient sinchClient;
	private String callerId;
	private String recipientId;
	private SharedPreferences sharedPreferences;
	private CircleImageView face;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.call);

		sharedPreferences = getApplicationContext().getSharedPreferences(
				"DyadPrefs", Context.MODE_MULTI_PROCESS);
		callerId = sharedPreferences.getString("my_object_id", "a");
		recipientId = sharedPreferences.getString("your_object_id", "b");
		sinchClient = Sinch.getSinchClientBuilder().context(this)
				.userId(callerId).applicationKey(APP_KEY)
				.applicationSecret(APP_SECRET).environmentHost(ENVIRONMENT)
				.build();

		sinchClient.setSupportCalling(true);
		sinchClient.startListeningOnActiveConnection();
		if (!sinchClient.isStarted())
			sinchClient.start();

		sinchClient.getCallClient().addCallClientListener(
				new SinchCallClientListener());
		callState = (TextView) findViewById(R.id.callState);
		callAction = (TextView) findViewById(R.id.callAction);
		face = (CircleImageView) findViewById(R.id.face);
		// call = sinchClient.getCallClient().callUser(recipientId);
		// call.addCallListener(new SinchCallListener());
		try {
			byte[] encodeByte = Base64.decode(
					sharedPreferences.getString("your_image_bitmap", "0"),
					Base64.DEFAULT);
			Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
					encodeByte.length);
			face.setImageBitmap(bitmap);
		} catch (Exception e) {
		}
		callState.setText("CALL");
		callState.setClickable(true);
		callState.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (call == null) {
					call = sinchClient.getCallClient().callUser(recipientId);
					call.addCallListener(new SinchCallListener());
					callState.setText("Hang Up");
					callAction.setVisibility(View.VISIBLE);
				} else {
					call.hangup();
					callAction.setVisibility(View.GONE);
				}
			}
		});
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	private class SinchCallListener implements CallListener {
		@Override
		public void onCallEnded(Call endedCall) {
			call = null;
			callAction.setText("CALL ENDED");
			callState.setText("Call");
			setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
		}

		@Override
		public void onCallEstablished(Call establishedCall) {
			callAction.setText("CONNECTED");
			setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
		}

		@Override
		public void onCallProgressing(Call progressingCall) {
			callAction.setText("RINGING");
		}

		@Override
		public void onShouldSendPushNotification(Call call,
				List<PushPair> pushPairs) {
		}
	}

	private class SinchCallClientListener implements CallClientListener {
		@Override
		public void onIncomingCall(CallClient callClient, Call incomingCall) {
			call = incomingCall;
			call.answer();
			call.addCallListener(new SinchCallListener());
			callState.setText("Hang Up");
		}
	}
}
