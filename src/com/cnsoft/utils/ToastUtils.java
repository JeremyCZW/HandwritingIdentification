package com.cnsoft.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {

	private static Toast toast;
	public static int SHORT_DUR = Toast.LENGTH_SHORT;
	public static int LONG_DUR = Toast.LENGTH_LONG;
	/*
	 * 	toast  info
	 * */
	
	public static void showToast(Context context, CharSequence text, int time){
		if(toast==null){
			toast = Toast.makeText(context, text, time);
		}else{
			toast.setText(text);
			toast.setDuration(time);
		}
		toast.show();
	}
	public static void showToast(Context context, CharSequence text){
		if(toast==null){
			toast = Toast.makeText(context, text, SHORT_DUR);
		}else{
			toast.setText(text);
			toast.setDuration(SHORT_DUR);
		}
		toast.show();
	}
}
