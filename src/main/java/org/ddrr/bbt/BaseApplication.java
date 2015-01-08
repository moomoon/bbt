package org.ddrr.bbt;

import android.app.Application;

public class BaseApplication extends Application {

	private static BaseApplication mAppInstance;

	@Override
	public void onCreate() {
		mAppInstance = this;
		super.onCreate();
	}

	public static BaseApplication getAppInstance() {
		return mAppInstance;
	}
}
