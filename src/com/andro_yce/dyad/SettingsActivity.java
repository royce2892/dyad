package com.andro_yce.dyad;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class SettingsActivity extends ActionBarActivity implements
		OnClickListener {

	TextView mStatusText, mTextSize;
	ImageView mProfileImage;
	EditText mStatusEdit;
	Activity mActivity;
	SharedPreferences sharedPreferences;
	private String cameraFilePath = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// set title as settings
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setTitle("Settings");
		setContentView(R.layout.activity_settings);

		sharedPreferences = getApplicationContext().getSharedPreferences(
				"DyadPrefs", Context.MODE_MULTI_PROCESS);
		// call set Image function
		mProfileImage = (com.andro_yce.dyad.CircleImageView) findViewById(R.id.user_profile_image);
		if (sharedPreferences.contains("my_image_bitmap"))
			setProfileImage();

		// set status
		mStatusText = (TextView) findViewById(R.id.user_change_status);
		mTextSize = (TextView) findViewById(R.id.text_size);
		if (sharedPreferences.contains("my_status"))
			mStatusText.setText(sharedPreferences.getString("my_status",
					"Set Status"));

		mStatusEdit = (EditText) findViewById(R.id.user_change_status_edit);

		mStatusText.setClickable(true);
		mTextSize.setClickable(true);
		mProfileImage.setClickable(true);

		mStatusText.setOnClickListener(this);
		mProfileImage.setOnClickListener(this);
		mTextSize.setOnClickListener(this);

		mActivity = this;
	}

	// set profile image of user
	private void setProfileImage() {
		// TODO Auto-generated method stub
		try {
			byte[] encodeByte = Base64.decode(
					sharedPreferences.getString("my_image_bitmap", "0"),
					Base64.DEFAULT);
			Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
					encodeByte.length);
			mProfileImage.setImageBitmap(bitmap);
		} catch (Exception e) {
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {

		// Change status
		case R.id.user_change_status:
			mStatusText.setVisibility(View.GONE);
			mStatusEdit.setVisibility(View.VISIBLE);
			mStatusEdit.setOnEditorActionListener(new OnEditorActionListener() {
				public boolean onEditorAction(TextView v, int actionId,
						KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						handleStatus(mStatusEdit.getText().toString());
						mStatusText.setVisibility(View.VISIBLE);
						mStatusText.setText(mStatusEdit.getText().toString());
						mStatusEdit.setVisibility(View.GONE);
					}
					return false;
				}
			});

			break;

		// Change profile Image
		case R.id.user_profile_image:
			openGallery();
			break;

		// Change text size
		case R.id.text_size:
			String size[] = { "Large", "Medium", "Size" };
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder.setTitle("Text Size").setItems(size,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							int size = 14;
							if (which == 0)
								size = 22;
							else if (which == 1)
								size = 18;
							Editor editor = sharedPreferences.edit();
							editor.putInt("text_size", size);
							editor.commit();
						}
					});
			builder.create().show();
		}
	}

	// update status change on parse db
	protected void handleStatus(String status) {
		// TODO Auto-generated method stub
		Editor editor = sharedPreferences.edit();
		editor.putString("my_status", status);
		editor.commit();
		ParseUser currentUser = ParseUser.getCurrentUser();
		currentUser.put("status", status);
		currentUser.saveInBackground();
	}

	// code for uploading image clicked from camera
	/*
	 * protected void openCamera() { // TODO Auto-generated method stub File
	 * file = getOutputMediaFile(); final Uri filePath = Uri.fromFile(file); //
	 * final String fileDirPath = file.getParent();
	 * 
	 * 
	 * mFileObserver = new FileObserver(fileDirPath) {
	 * 
	 * @Override public void onEvent(int event, final String path) { // check if
	 * its not equal to .probe because thats created every // time camera is
	 * launched if (event == FileObserver.CREATE && !path.equals(".probe")) {
	 * runOnUiThread(new Runnable() {
	 * 
	 * @Override public void run() { cameraFilePath = fileDirPath + '/' + path;
	 * } }); } } };
	 * 
	 * 
	 * mFileObserver.startWatching();
	 * 
	 * Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	 * cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, filePath);
	 * startActivityForResult(cameraIntent, 0); }
	 */

	/*
	 * public static File getOutputMediaFile() { // Create the storage directory
	 * if it does not exist File mediaDir = getDyadFolder(); if (mediaDir ==
	 * null) return null;
	 * 
	 * // Create a media file name // String timeStamp = new
	 * SimpleDateFormat("yyyyMMdd_HHmmss") .format(new Date()); File mediaFile;
	 * mediaFile = new File(mediaDir.getPath() + File.separator + "IMG_" +
	 * timeStamp + ".jpg");
	 * 
	 * return mediaFile; }
	 */
	/*
	 * private static File getDyadFolder() { // TODO Auto-generated method stub
	 * final File sChatPhotoDirectory = new File( Environment
	 * .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
	 * "DyadPictures"); if (!sChatPhotoDirectory.exists()) {
	 * sChatPhotoDirectory.mkdirs(); } return sChatPhotoDirectory; }
	 */

	// Show options for picking images from gallery
	protected void openGallery() {
		// TODO Auto-generated method stub
		try {
			Intent getContentIntent = createGetContentIntent("image/*");
			Intent intent = Intent.createChooser(getContentIntent,
					"Choose a File");
			startActivityForResult(intent, 1);
		} catch (ActivityNotFoundException e) {
		}
	}

	private Intent createGetContentIntent(String string) {
		// TODO Auto-generated method stub
		final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		// The MIME data type filter
		intent.setType(string);
		// Only return URIs that can be opened with ContentResolver
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		return intent;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch (requestCode) {
		// from camera

		case 0:
			Bitmap myImageBitmap = null;
			try {
				myImageBitmap = BitmapFactory.decodeStream(getContentResolver()
						.openInputStream(Uri.parse(cameraFilePath)));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			mProfileImage.setImageBitmap(myImageBitmap);
			addToSp(myImageBitmap);
			break;
		// from gallery
		case 1:
			final Uri uri = data.getData();
			Bitmap myImageBitmap2 = null;
			try {
				myImageBitmap2 = BitmapFactory
						.decodeStream(getContentResolver().openInputStream(uri));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			mProfileImage.setImageBitmap(myImageBitmap2);
			addToSp(myImageBitmap2);
			break;

		default:
			break;
		}
		/*
		 * if (mFileObserver != null) { mFileObserver.stopWatching();
		 * mFileObserver = null; }
		 */
	}

	// add image to db and parse
	private void addToSp(Bitmap myImageBitmap) {
		// TODO Auto-generated method stub

		// convert bitmap to bytes and then string
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		myImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		byte[] b = baos.toByteArray();
		String temp = Base64.encodeToString(b, Base64.DEFAULT);

		// upload image on parse
		ParseUser me = ParseUser.getCurrentUser();
		ParseFile file = new ParseFile("my_image.png", b);
		file.saveInBackground();
		me.put("profileImage", file);
		me.saveInBackground();

		// add to db
		Editor editor = sharedPreferences.edit();
		editor.putString("my_image_bitmap", temp);
		editor.commit();
	}

	/*
	 * private String getRealPathFromURI(Uri contentURI) { Cursor cursor =
	 * getContentResolver().query(contentURI, null, null, null, null); if
	 * (cursor == null) { return contentURI.getPath(); } else {
	 * cursor.moveToFirst(); int idx = cursor
	 * .getColumnIndex(MediaStore.Images.ImageColumns.DATA); return
	 * cursor.getString(idx); } }
	 */

}
