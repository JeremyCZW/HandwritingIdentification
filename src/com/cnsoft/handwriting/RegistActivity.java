package com.cnsoft.handwriting;

import java.util.ArrayList;
import java.util.HashMap;

import com.cnsoft.algorithms.Algorithms;
import com.cnsoft.constants.InputSamples;
import com.cnsoft.entities.Point;
import com.cnsoft.entities.Stroke;
import com.cnsoft.utils.MyDatabaseHelper;
import com.cnsoft.utils.ToastUtils;
import com.cnsoft.view.RegistView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class RegistActivity extends Activity implements OnClickListener {
	private Button iv_registBack; // 返回菜单
	private RegistView view; // 画布
	private Button btn_registClearCanvas, btn_regist, btn_registClearAll, btn_registClearOne; // 清空画布，完成注册，清空数据
	private EditText et_registName; // 用户名输入框
	private TextView tv_registHint, tv_registProcess; // 当前字样提示,进度

	private Algorithms algorithm; // 算法类
	private static int curIndex; // 记录注册信息字样下标
	private final int strLen = InputSamples.REGIST_LEN;// 注册字样长度
	private MyDatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private String username;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_regist);
		initView(); // 初始化布局
		initData(); // 初始化数据
	}

	private void initData() {
		algorithm = Algorithms.getInstance(); // 实例化算法类
		dbHelper = new MyDatabaseHelper(this);
		db = dbHelper.getReadableDatabase();
		refresh();
	}

	private void initView() {
		view = (RegistView) findViewById(R.id.v_registDraw);
		btn_registClearCanvas = (Button) findViewById(R.id.btn_registClearCanvas);
		btn_regist = (Button) findViewById(R.id.btn_regist);
		btn_registClearAll = (Button) findViewById(R.id.btn_registClearAll);
		iv_registBack = (Button) findViewById(R.id.iv_registBack);
		et_registName = (EditText) findViewById(R.id.et_registName);
		tv_registHint = (TextView) findViewById(R.id.tv_registHint);
		tv_registProcess = (TextView) findViewById(R.id.tv_registProcess);
		btn_registClearOne = (Button) findViewById(R.id.btn_registClearOne);

		btn_registClearOne.setOnClickListener(this);
		btn_registClearCanvas.setOnClickListener(this);
		btn_regist.setOnClickListener(this);
		btn_registClearAll.setOnClickListener(this);
		iv_registBack.setOnClickListener(this);
	}

	private boolean nextHint() {
		if (curIndex + 1 <= strLen) {
			tv_registHint.setText("当前字: " + InputSamples.REGIST_STR.substring(curIndex, 1 + curIndex));
			tv_registProcess.setText("进度: " + ++curIndex + "/" + strLen);
			return true;
		}
		return false;
	}

	/**
	 * 重置提示信息
	 */
	private void refresh() {
		curIndex = 0; // 重置下标
		nextHint(); // 初始化字样提示
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_registClearCanvas: // 下一个字

			//如果没输入，直接返回
			if(algorithm.isEmpty()){
				break;
			}
			
			if (!nextHint()) {
				ToastUtils.showToast(this, "请完成注册");
				break;
			}
			
			algorithm.preprocess(true);
			view.clear();

			break;
		case R.id.btn_regist: // 完成注册
			
			if(algorithm.isEmpty()){
				break;
			}
			
			if (curIndex + 1 <= strLen) {
				ToastUtils.showToast(this, "请完善信息");
				break;
			}

			username = et_registName.getText().toString(); // 获取用户名
			if (username == null || TextUtils.isEmpty(username)) { // 用户名有效性检查
				ToastUtils.showToast(this, "请输入用户名");
				break;
			}
			
			if(!algorithm.featureVct.isUserNameValid(username)){
				ToastUtils.showToast(this, "用户名重复了");
				break;
			}
			
			// 注册该用户
			if (algorithm.registUser(username) && insertUserInfo()) {

				ToastUtils.showToast(this, "注册成功!");
				algorithm.clearRegistData();
				// 跳转至主界面
				Intent intent = new Intent(this, MainActivity.class);
				startActivity(intent);
				this.finish();
			}
			break;
		case R.id.iv_registBack: // 返回主菜单
			Intent backIntent = new Intent(this, MainActivity.class);
			startActivity(backIntent);
			this.finish();
			break;
		case R.id.btn_registClearAll: // 重新输入,清除本次输入所有信息
			popDialog();
			break;
		case R.id.btn_registClearOne:
			algorithm.mPointList.clear();
			view.clear();
			break;
		default:
			break;
		}
	}

	/**
	 * 新用户信息插入数据库
	 * @return
	 */
	private boolean insertUserInfo() {
		// 获取内存中的数据
		HashMap<String, HashMap<Integer, ArrayList<Stroke>>> staticData = algorithm.featureVct.getRegistInfo();
		HashMap<String, HashMap<Integer, ArrayList<Point>>> dynamicData = algorithm.vCmp.getRegistInfo();
		
		//存储静态特征信息
		HashMap<Integer, ArrayList<Stroke>> userStaticFeature = staticData.get(username);
		StringBuilder[] sb = new StringBuilder[8];
		for (int i = 0; i < 8; i++) {
			sb[i] = new StringBuilder("");
			ArrayList<Stroke> featureForKind = userStaticFeature.get(i + 1);
			if (featureForKind.size() != 0) {
				for (Stroke mStroke : featureForKind) {
					for (double a : mStroke.fetureVct) {
						sb[i].append(a + ",");
					}
					sb[i].append(";");
				}
			}
		}
		db.execSQL("insert into handwriting_static values ( '" + username + "' , '" + sb[0].toString() + "' , '"
				+ sb[1].toString() + "' , '" + sb[2].toString() + "' , '" + sb[3].toString() + "' , '"
				+ sb[4].toString() + "' , '" + sb[5].toString() + "' , '" + sb[6].toString() + "' , '"
				+ sb[7].toString() + "')");

		//存储动态特征信息
		HashMap<Integer, ArrayList<Point>> userDynamicFeather = dynamicData.get(username);
		StringBuilder sb1 = new StringBuilder("");
		for (Integer itg : userDynamicFeather.keySet()) {
			sb1.append(itg + ":");
			ArrayList<Point> charDyFeather = userDynamicFeather.get(itg);
			for (Point mPoint : charDyFeather) {
				sb1.append(mPoint.Vx + "," + mPoint.Vy + "," + mPoint.x+ "," +mPoint.y +"/");
			}
			sb1.append(";");
		}
		db.execSQL("insert into handwriting_dynamic values ( '" + username + "' , '" + sb1 + "' )");
		return true;
	}

	/**
	 * 关闭连接
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dbHelper != null) {
			dbHelper.close();
		}
	}
	
	/**
	 * AlertDialog
	 */
	public void popDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.reset)
				.setTitle("请选择")
				.setMessage("您确认要重写所有字么")
				.setPositiveButton("是",new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						view.clear();
						algorithm.clearRegistData();
						refresh();
						ToastUtils.showToast(RegistActivity.this, "清除成功!");
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
