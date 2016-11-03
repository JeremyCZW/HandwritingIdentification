package com.cnsoft.handwriting;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class InstroActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_instruction);
	}
	public void clickBack(View v){
		InstroActivity.this.finish();
	}
}
