package com.cnsoft.handwriting;

import com.cnsoft.algorithms.Algorithms;
import com.cnsoft.utils.MyDatabaseHelper;
import com.cnsoft.utils.ToastUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {
	// 登录，注册，清空用户按钮
	private Button btn_mainLogin, btn_mainRegist, btn_mainClear,btn_sm;
	private Algorithms algorithm;
	private MyDatabaseHelper dbHelper;
	private SQLiteDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView(); // 初始化布局
	}

	private void initView() {
		btn_mainLogin = (Button) findViewById(R.id.btn_mainLogin);
		btn_mainRegist = (Button) findViewById(R.id.btn_mainRegist);
		btn_mainClear = (Button) findViewById(R.id.btn_mainClear);
		btn_sm=(Button) findViewById(R.id.btn_sm);
		btn_mainLogin.setOnClickListener(this);
		btn_mainRegist.setOnClickListener(this);
		btn_mainClear.setOnClickListener(this);
		btn_sm.setOnClickListener(this);
		algorithm = Algorithms.getInstance();
		dbHelper = new MyDatabaseHelper(this);
		db = dbHelper.getReadableDatabase();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_mainLogin: // 登入,跳转登入界面
			Intent loginIntent = new Intent(this, LoginActivity.class);
			startActivity(loginIntent);
			break;
		case R.id.btn_mainRegist: // 注册,跳转注册界面
			Intent registIntent = new Intent(this, RegistActivity.class);
			startActivity(registIntent);
			break;
		case R.id.btn_mainClear: // 清除所有用户数据
			popDialog();
			break;
		case R.id.btn_sm:
			Intent intent=new Intent(this,InstroActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

	/**
	 * 清除所有用户数据
	 */
	private void clearAllData() {
		algorithm.clearAllData();
		db.execSQL("delete from handwriting_static");
		db.execSQL("delete from handwriting_dynamic");
	}
	
	/**
	 * AlertDialog
	 */
	public void popDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.reset)
				.setTitle("请选择")
				.setMessage("您确认要清除所有用户数据么")
				.setPositiveButton("是",new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						clearAllData();
						ToastUtils.showToast(MainActivity.this, "数据已清除!");
					}
				});
		builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.show();	
}
}
