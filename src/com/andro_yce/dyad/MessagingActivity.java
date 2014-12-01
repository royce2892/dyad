package com.andro_yce.dyad;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;

import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;

import java.util.Arrays;
import java.util.List;
import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;

public class MessagingActivity extends ActionBarActivity implements
		EmojiconGridFragment.OnEmojiconClickedListener,
		EmojiconsFragment.OnEmojiconBackspaceClickedListener {

	private String recipientId;
	private com.rockerhieu.emojicon.EmojiconEditText messageBodyField;
	private String messageBody;
	private MessageService.MessageServiceInterface messageService;
	private MessageAdapter messageAdapter;
	private ListView messagesList;
	private String currentUserId;
	private ServiceConnection serviceConnection = new MyServiceConnection();
	private MessageClientListener messageClientListener = new MyMessageClientListener();
	private SharedPreferences sharedPreferences;
	private Activity mActivity;
	private Button mSmiley;
	private Boolean isSmileyShown = false;
	Resources resources;
	DisplayMetrics metrics;
	RelativeLayout rel;
	RelativeLayout.LayoutParams params;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_launcher);
		setContentView(R.layout.activity_messaging);
		mActivity = this;
		resources = getApplicationContext().getResources();
		metrics = resources.getDisplayMetrics();
		rel = (RelativeLayout) findViewById(R.id.relSendMessage);
		params = (RelativeLayout.LayoutParams) rel.getLayoutParams();
		bindService(new Intent(this, MessageService.class), serviceConnection,
				BIND_AUTO_CREATE);

		Intent intent = getIntent();
		recipientId = intent.getStringExtra("RECIPIENT_ID");
		initSharedPreferences();
		addToSharedPreferences();
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("objectId", recipientId);
		query.findInBackground(new FindCallback<ParseUser>() {
			public void done(List<ParseUser> user, com.parse.ParseException e) {
				if (e == null) {
					if (user.size() == 1)
						addLogo(user);
				}
			}
		});
		currentUserId = ParseUser.getCurrentUser().getObjectId();

		messagesList = (ListView) findViewById(R.id.listMessages);
		messageAdapter = new MessageAdapter(this);
		messagesList.setAdapter(messageAdapter);
		populateMessageHistory();
		messageBodyField = (EmojiconEditText) findViewById(R.id.messageBodyField);
		messageBodyField.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if(!isSmileyShown)
				{
					showSmileyView();
					isSmileyShown = true;
				}
				else
				{
					hideSmileyView();
					isSmileyShown = false;
				}
			}
		});

		mSmiley = (Button) findViewById(R.id.use_system_default);
		mSmiley.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				hideSmileyView();
			}
		});
		findViewById(R.id.sendButton).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						sendMessage();
					}
				});
	}

	private void initSharedPreferences() {
		// TODO Auto-generated method stub

		sharedPreferences = getApplicationContext().getSharedPreferences(
				"DyadPrefs", Context.MODE_MULTI_PROCESS);

	}

	protected void addLogo(List<ParseUser> user) {
		// TODO Auto-generated method stub
		ParseFile file = user.get(0).getParseFile("profileImage");
		String your_first_name = user.get(0).getString("firstName");
		file.getDataInBackground(new GetDataCallback() {
			@Override
			public void done(byte[] data, ParseException e) {
				// TODO Auto-generated method stub
				if (e == null) {
					String temp = Base64.encodeToString(data, Base64.DEFAULT);
					addToSharedPreferences(temp);
				}
			}
		});
		if (your_first_name != null) {
			getSupportActionBar().setTitle("  " + your_first_name);
		}

	}

	/*
	 * private void customizeAactionBar(ActionBar mActionBar) { // TODO
	 * Auto-generated method stub mActionBar.setDisplayShowHomeEnabled(false);
	 * mActionBar.setDisplayShowTitleEnabled(false); LayoutInflater mInflater =
	 * LayoutInflater.from(this);
	 * 
	 * View mCustomView = mInflater.inflate(R.layout.messaging_action_bar,
	 * null); mActionBar.setCustomView(mCustomView);
	 * mActionBar.setDisplayShowCustomEnabled(true);
	 * mActionBar.setDisplayHomeAsUpEnabled(true);
	 * 
	 * }
	 */

	private void setEmojiconFragment(Boolean b) {
		// TODO Auto-generated method stub
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.emojicons, EmojiconsFragment.newInstance(b))
				.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.main, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		int id = item.getItemId();
		switch (id) {
		case R.id.action_settings:
			Intent settings = new Intent(getApplicationContext(),
					SettingsActivity.class);
			startActivity(settings);
			break;
		case R.id.action_call:
			Intent call = new Intent(getApplicationContext(),
					CallActivity.class);
			startActivity(call);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
	}

	// get previous messages from parse & display
	private void populateMessageHistory() {
		String[] userIds = { currentUserId, recipientId };
		ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
		query.whereContainedIn("senderId", Arrays.asList(userIds));
		query.whereContainedIn("recipientId", Arrays.asList(userIds));
		query.orderByAscending("createdAt");
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> messageList,
					com.parse.ParseException e) {
				if (e == null) {
					for (int i = 0; i < messageList.size(); i++) {
						WritableMessage message = new WritableMessage(
								messageList.get(i).get("recipientId")
										.toString(), messageList.get(i)
										.get("messageText").toString());
						if (messageList.get(i).get("senderId").toString()
								.equals(currentUserId)) {
							messageAdapter.addMessage(message,
									MessageAdapter.DIRECTION_OUTGOING);
						} else {
							messageAdapter.addMessage(message,
									MessageAdapter.DIRECTION_INCOMING);
						}
					}
				}
			}
		});
	}

	private void sendMessage() {
		messageBody = messageBodyField.getText().toString();
		if (messageBody.isEmpty()) {
			Toast.makeText(this, "Please enter a message", Toast.LENGTH_LONG)
					.show();
			return;
		}

		messageService.sendMessage(recipientId, messageBody);
		messageBodyField.setText("");
	}

	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

	}

	@Override
	public void onDestroy() {
		/*
		 * messageService.removeMessageClientListener(messageClientListener);
		 * unbindService(serviceConnection);
		 */
		super.onDestroy();
	}

	private class MyServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder iBinder) {
			messageService = (MessageService.MessageServiceInterface) iBinder;
			messageService.addMessageClientListener(messageClientListener);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			messageService = null;
		}
	}

	private class MyMessageClientListener implements MessageClientListener {
		@Override
		public void onMessageFailed(MessageClient client, Message message,
				MessageFailureInfo failureInfo) {
			Toast.makeText(MessagingActivity.this, "Message failed to send.",
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void onIncomingMessage(MessageClient client, Message message) {
			if (message.getSenderId().equals(recipientId)) {
				WritableMessage writableMessage = new WritableMessage(message
						.getRecipientIds().get(0), message.getTextBody());
				messageAdapter.addMessage(writableMessage,
						MessageAdapter.DIRECTION_INCOMING);

			}
		}

		@Override
		public void onMessageSent(MessageClient client, Message message,
				String recipientId) {

			final WritableMessage writableMessage = new WritableMessage(message
					.getRecipientIds().get(0), message.getTextBody());

			// only add message to parse database if it doesn't already exist
			// there
			ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
			query.whereEqualTo("sinchId", message.getMessageId());
			query.findInBackground(new FindCallback<ParseObject>() {
				@Override
				public void done(List<ParseObject> messageList,
						com.parse.ParseException e) {
					if (e == null) {
						if (messageList.size() == 0) {
							ParseObject parseMessage = new ParseObject(
									"ParseMessage");
							parseMessage.put("senderId", currentUserId);
							parseMessage.put("recipientId", writableMessage
									.getRecipientIds().get(0));
							parseMessage.put("messageText",
									writableMessage.getTextBody());
							parseMessage.put("sinchId",
									writableMessage.getMessageId());
							parseMessage.saveInBackground();

							messageAdapter.addMessage(writableMessage,
									MessageAdapter.DIRECTION_OUTGOING);
						}
					}
				}
			});
		}

		@Override
		public void onMessageDelivered(MessageClient client,
				MessageDeliveryInfo deliveryInfo) {
		}

		@Override
		public void onShouldSendPushData(MessageClient client, Message message,
				List<PushPair> pushPairs) {
		}
	}

	@Override
	public void onEmojiconClicked(Emojicon emojicon) {
		EmojiconsFragment.input(messageBodyField, emojicon);
	}

	@Override
	public void onEmojiconBackspaceClicked(View v) {
		EmojiconsFragment.backspace(messageBodyField);
	}

	private void showSmileyView() {
		hideKeyboardIfPresent();
		int px = (int) (280 * (metrics.densityDpi / 160f));
		params.height = px;
		rel.setLayoutParams(params);
		setEmojiconFragment(true);
	}

	private void hideKeyboardIfPresent() {
		// TODO Auto-generated method stub
		if (mActivity == null)
			return;

		InputMethodManager manager = (InputMethodManager) mActivity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		View currentFocus = mActivity.getCurrentFocus();
		if (manager == null || currentFocus == null)
			return;

		manager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
	}

	private void hideSmileyView() {
		int px = (int) (60 * (metrics.densityDpi / 160f));
		params.height = px;
		rel.setLayoutParams(params);
		setEmojiconFragment(false);
	}

	public void addToSharedPreferences() {
		Editor editor = sharedPreferences.edit();
		if (!sharedPreferences.contains("your_object_id")) {
			editor.putString("your_object_id", recipientId);
			editor.putString("my_object_id", ParseUser.getCurrentUser()
					.getObjectId());
		}
		editor.commit();
	}

	public void addToSharedPreferences(String s) {
		Editor editor = sharedPreferences.edit();
		editor.putString("your_image_bitmap", s);

		editor.commit();
		Log.i("id", sharedPreferences.getString("your_image_bitmap", "0"));
	}

}
