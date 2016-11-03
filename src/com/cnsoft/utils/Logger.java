package com.cnsoft.utils;

import android.util.Log;

public class Logger {

	/**
	 * INFO LOG
	 * 
	 * @param TAG
	 * @param msg
	 *            level -info
	 */
	public static void showLog(String TAG, String msg) {
		Log.i(TAG, msg);
	}

	/**
	 * OTHER LOG
	 * 
	 * @param TAG
	 * @param msg
	 * @param level
	 *            2-verbose 3-debug 4-info 5-warning 6-error
	 */
	public static void showLog(String TAG, String msg, int level) {
		switch (level) {
		case Log.INFO:
			Log.i(TAG, msg);
			break;
		case Log.WARN:
			Log.w(TAG, msg);
			break;
		case Log.ERROR:
			Log.e(TAG, msg);
			break;
		case Log.VERBOSE:
			Log.v(TAG, msg);
			break;
		case Log.DEBUG:
			Log.d(TAG, msg);
			break;
		default:
			Log.i(TAG, msg);
			break;
		}
	}
}
