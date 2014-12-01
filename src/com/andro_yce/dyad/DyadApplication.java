package com.andro_yce.dyad;

import com.parse.Parse;

import android.app.Application;

public class DyadApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, "adTcwCMZtbgoK5bKkuGhRW5qJC2UhhNm2y8Ssy3Q",
				"kWj7WlzzrAGS1jeJkFsG0ll2k4IsMPvgz8jB9Vjf");
	}

}
