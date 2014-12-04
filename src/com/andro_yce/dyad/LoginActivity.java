package com.andro_yce.dyad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

import com.parse.LogInCallback;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// Handles login or signup as designed

public class LoginActivity extends Activity {

	private Button signUpButton;
	private EditText usernameField, phoneField, codeField;
	private TextView codeEnter;
	private String username = new String();
	private String password = new String();
	private String phone = new String();
	private CheckBox hasCode;
	public SharedPreferences sharedpreferences;
	ParseUser user = new ParseUser();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initSharedPrefs();
		// check if user has signed up , if yes jump to login else show login
		// screen
		if (sharedpreferences.contains("signup"))
			loginUser();
		else
			initUi();
	}

	// get username and password from prefs and skip to splash screen
	private void loginUser() {
		if (sharedpreferences.getBoolean("signup", false)) {
			username = sharedpreferences.getString("parse_user", "0");
			password = sharedpreferences.getString("parse_pass", "0");
			skipToSplashActivity();
		}
	}

	// initialize signup screen for new user
	private void initUi() {
		setContentView(R.layout.activity_login);

		signUpButton = (Button) findViewById(R.id.signupButton);
		usernameField = (EditText) findViewById(R.id.loginUsername);
		phoneField = (EditText) findViewById(R.id.loginPhone);
		codeField = (EditText) findViewById(R.id.loginCodeEnter);
		codeEnter = (TextView) findViewById(R.id.loginCodeEnterText);
		codeField.setVisibility(View.GONE);
		codeEnter.setVisibility(View.GONE);
		hasCode = (CheckBox) findViewById(R.id.loginCode);
		// manage if user has code to finish coupling
		hasCode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub

				if (buttonView.isChecked()) {
					codeField.setVisibility(View.VISIBLE);
					codeEnter.setVisibility(View.VISIBLE);
				} else {
					codeField.setVisibility(View.GONE);
					codeEnter.setVisibility(View.GONE);
				}

			}
		});

		signUpButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				username = usernameField.getText().toString();
				password = Math.random() * (10000000 - 1000000) + "";
				phone = phoneField.getText().toString();

				user.setUsername(phone);
				user.setPassword(password);
				user.put("myNumber", phone);
				user.put("firstName", username);
				if (!hasCode.isChecked()) {
					user.put("isPaired", false);
					user.put("secretCode",
							(int) (Math.random() * (9999 - 1000)) + "");
					signup();
				} else {
					user.put("isPaired", true);
					user.put("secretCode", codeField.getText().toString());
					signup();
				}
			}

		});
	}

	private void initSharedPrefs() {
		sharedpreferences = getApplicationContext().getSharedPreferences(
				"DyadPrefs", Context.MODE_MULTI_PROCESS);
	}

	// Login in background
	private void skipToSplashActivity() {
		// TODO Auto-generated method stub
		ParseUser.logInInBackground(username, password, new LogInCallback() {
			public void done(ParseUser user, com.parse.ParseException e) {
				if (user != null) {
					Intent splashIntent = new Intent(LoginActivity.this,
							SplashActivity.class);
					startActivity(splashIntent);
				} else {
					Toast.makeText(getApplicationContext(),
							"wrong user/pass combo", Toast.LENGTH_SHORT).show();
				}
			}
		});

	}

	// Sign up in background
	private void signup() {
		addToSharedPreferences(true);

		user.signUpInBackground(new SignUpCallback() {

			public void done(com.parse.ParseException e) {
				if (e == null) {

					Intent splashIntent = new Intent(LoginActivity.this,
							SplashActivity.class);
					startActivity(splashIntent);

				} else {
					addToSharedPreferences(false);
					Toast.makeText(getApplicationContext(),
							"There was an error signing up.", Toast.LENGTH_LONG)
							.show();
				}
			}

		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	// Add username ,password, number and self object id to database
	public void addToSharedPreferences(Boolean isSignedUp) {
		Editor editor = sharedpreferences.edit();
		editor.putBoolean("signup", isSignedUp);
		if (isSignedUp) {
			editor.putString("parse_user", phone);
			editor.putString("parse_pass", password);
			editor.putString("my_number", phone);
			/*
			 * editor.putString("my_object_id", ParseUser.getCurrentUser()
			 * .getObjectId());
			 */
		}
		editor.commit();
	}

}
