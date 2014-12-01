package com.andro_yce.dyad;

import java.util.List;

import com.parse.FindCallback;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends ActionBarActivity {

	private ProgressDialog progressDialog;
	private BroadcastReceiver receiver;
	SharedPreferences sharedPreferences;
	TextView code, tv1;
	private static ParseUser currentUser;
	String yourId;
	ImageView splash;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		sharedPreferences = getApplicationContext().getSharedPreferences(
				"DyadPrefs", Context.MODE_MULTI_PROCESS);
		getSupportActionBar().hide();
		setContentView(R.layout.activity_splash);
		code = (TextView) findViewById(R.id.code);
		tv1 = (TextView) findViewById(R.id.tv1);
		splash = (ImageView) findViewById(R.id.splash_image);
		currentUser = ParseUser.getCurrentUser();
		Intent serviceIntent = new Intent(this, MessageService.class);
		// check if user is paired , else show the code screen or skip to messaging activity
		if (currentUser != null) {
			startService(serviceIntent);
			if (!currentUser.getBoolean("isPaired")) {
				checkIfPairingHappened();
				code.setText(currentUser.getString("secretCode"));
				code.setVisibility(View.VISIBLE);
				tv1.setVisibility(View.VISIBLE);
				splash.setVisibility(View.GONE);
			} else {
				splash.setVisibility(View.VISIBLE);
				showSpinner();
			}
		}
	}
	// check parse pairing 
	private void checkIfPairingHappened() {
		// TODO Auto-generated method stub
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("secretCode", currentUser.getString("secretCode"));
		query.findInBackground(new FindCallback<ParseUser>() {
			public void done(List<ParseUser> user, com.parse.ParseException e) {
				if (e == null) {
					if (user.size() == 2) {
						currentUser.put("isPaired", true);
						currentUser.saveInBackground();
						showSpinner();
					} else {

					}
				}
			}
		});
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (!sharedPreferences.getBoolean("signup", false))
			super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		stopService(new Intent(this, MessageService.class));
		super.onDestroy();
	}

	private void showSpinner() {

		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("Loading");
		progressDialog.setMessage("Please wait...");
		progressDialog.show();

		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				progressDialog.dismiss();
			}
		};
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
				new IntentFilter("com.andro_yce.dyad.SplashActivity"));
		openConnection();

	}

	protected void openConnection() {
		// TODO Auto-generated method stub
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("secretCode", currentUser.getString("secretCode"));
		query.findInBackground(new FindCallback<ParseUser>() {
			public void done(List<ParseUser> user, com.parse.ParseException e) {

				if (e == null) {
					Intent intent = new Intent(getApplicationContext(),
							MessagingActivity.class);
					if (user.size() == 2) {
						if (user.get(0).getObjectId()
								.contentEquals(currentUser.getObjectId()))
							yourId = user.get(1).getObjectId();
						else
							yourId = user.get(0).getObjectId();
						intent.putExtra("RECIPIENT_ID", yourId);
						startActivity(intent);
					}
				}
			}
		});
	}
}
