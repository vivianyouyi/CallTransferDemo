package com.dpvr.droidplaycontroller;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * sp
 */
public class PreferenceUtil {
	private static final String FILE_NAME = "config";

	public static void putString(Context context, String key, String value){
		SharedPreferences prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public static String getString(Context context, String key, String defValue) {
		SharedPreferences prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		return prefs.getString(key, defValue);
	}	
	
	public static void putInt(Context context, String key, int value){
		SharedPreferences prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	
	public static int getInt(Context context, String key, int defValue) {
		SharedPreferences prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		return prefs.getInt(key, defValue);
	}	
}
