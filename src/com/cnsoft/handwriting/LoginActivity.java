package com.cnsoft.handwriting;


import com.cnsoft.algorithms.Algorithms;
import com.cnsoft.constants.InputSamples;
import com.cnsoft.utils.Logger;
import com.cnsoft.utils.ToastUtils;
import com.cnsoft.view.LoginView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class LoginActivity extends Activity implements OnClickListener {
	private Button iv_loginBack;
	private LoginView view;
	private Button btn_loginClear, btn_loginOk, btn_totalClear,btn_loginClearOne;
	private TextView tv_loginHint, tv_loginProcess;// 字样提示,进度
	private Algorithms algorithms;

	private static int curIndex; // 当前字下标
	private String dynamicStr =""; // 动态登录字样
	private String staticStr = ""; // 静态登录字样
	private String loginStr = ""; 
	private int[] randomArr; // 随机索引
	private int[] randomDynamic;//为动态特征的索引
	private int[] randomStatic = new int[InputSamples.LOGIN_STATIC_LEN];//为静态特征的索引
	private final int strLen = InputSamples.lOGIN_LEN;// 登陆字样长度

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		initView();
		initData();
	}

	private void initData() {
		algorithms = Algorithms.getInstance();
		algorithms.clearLoginData();
		
		//随机获取动态区域的索引如1,4,2,6   (4,8)
		randomDynamic = InputSamples.getRandomInt(InputSamples.LOGIN_DYNAMIC_LEN,InputSamples.lOGIN_LEN);
		//随机获取静态区域的索引如0,3,5,7
		for(int i=0,k=0;i<InputSamples.lOGIN_LEN;i++){
			boolean isExist = false;
			for(int j=0;j<InputSamples.LOGIN_DYNAMIC_LEN;j++){
				if(i==randomDynamic[j]){
					isExist = true;
					break;
				}
			}
			if(isExist==false){
				randomStatic[k++] = i;
			}
		}
		
		// 获得登陆动态字样索引        (4,20);
		randomArr = InputSamples.getRandomInt(InputSamples.LOGIN_DYNAMIC_LEN, InputSamples.REGIST_LEN);
		// 获得动态识别阶段登陆字样 dynamicStr
		for (int i = 0; i < InputSamples.LOGIN_DYNAMIC_LEN; i++) {
			dynamicStr += InputSamples.REGIST_STR.substring(randomArr[i], randomArr[i] + 1);
		}
		// 获得登陆静态字样索引        (4,35);	
		int[] staticArr = InputSamples.getRandomInt(InputSamples.LOGIN_STATIC_LEN,
				InputSamples.LOGIN_STATIC_STR.length());
		// 获得静态识别阶段登陆字样 staticStr
		for (int i = 0; i < InputSamples.LOGIN_STATIC_LEN; i++) {
			staticStr += InputSamples.LOGIN_STATIC_STR.substring(staticArr[i], staticArr[i] + 1);
		}
		
		//组合成登录字符串
		for(int i=0;i<InputSamples.lOGIN_LEN;i++){
			for(int j=0;j<InputSamples.LOGIN_DYNAMIC_LEN;j++){
				if(i==randomDynamic[j]){
					loginStr+=dynamicStr.substring(j,j+1);
					break;
				}
			}
			
			for(int k=0;k<InputSamples.LOGIN_STATIC_LEN;k++){
				if(i==randomStatic[k]){
					loginStr+=staticStr.substring(k,k+1);
					break;
				}
			}
		}
		
		algorithms.publishOrder(randomDynamic,randomArr);
		algorithms.randomDynamic = randomDynamic;
		refresh();
	}

	private void initView() {
		iv_loginBack = (Button) findViewById(R.id.iv_loginBack);
		btn_loginClear = (Button) findViewById(R.id.btn_loginClear);
		btn_loginOk = (Button) findViewById(R.id.btn_loginOk);
		view = (LoginView) findViewById(R.id.v_loginDraw);
		btn_totalClear = (Button) findViewById(R.id.btn_totalClear);
		tv_loginHint = (TextView) findViewById(R.id.tv_loginHint);
		tv_loginProcess = (TextView) findViewById(R.id.tv_loginProcess);
		btn_loginClearOne = (Button) findViewById(R.id.btn_loginClearOne);
		
		btn_loginClearOne.setOnClickListener(this);
		btn_totalClear.setOnClickListener(this);
		iv_loginBack.setOnClickListener(this);
		btn_loginClear.setOnClickListener(this);
		btn_loginOk.setOnClickListener(this);
	}

	private boolean nextHint() {
		if (curIndex + 1 <= strLen) {
			tv_loginHint.setText("当前字: " + loginStr.substring(curIndex, curIndex + 1));
			tv_loginProcess.setText("进度: " + (++curIndex) + "/" + strLen);
			return true;
		}
		return false;
	}

	/**
	 * 重置提示信息
	 */
	private void refresh() {
		curIndex = 0;
		nextHint();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_loginClear: // 下一个字

			if(algorithms.isEmpty()){
				break;
			}
			
			// 判断是否已经全部输入完，可以进行判断了
			if (!nextHint()) {
				ToastUtils.showToast(this, "请完成登录");
				break;
			}
			
			algorithms.preprocess(false);
			view.clear();
			
			break;
		case R.id.btn_loginOk: // 登入
			
			if(algorithms.isEmpty()){
				break;
			}
			
			// 判断字迹取样是否完成
			if (curIndex + 1 <= InputSamples.lOGIN_LEN) {
				ToastUtils.showToast(this, "请完善信息");
				break;
			}
			
			ToastUtils.showToast(this, "正在登入...");
			
			// 新线程用于登录
			new Thread(){
				public void run() {
					String userName = algorithms.login(); // 获取识别结果:用户名,陌生人或无注册用户
					algorithms.clearLoginData();
					// 跳转结果界面
					Intent resultIntent = new Intent(LoginActivity.this, ResultActivity.class);
					resultIntent.putExtra("userName", userName);
					startActivity(resultIntent);
					LoginActivity.this.finish();
				};
			}.start();
			
			view.clear(); // 清空画布
			break;
		case R.id.iv_loginBack: // 返回主界面
			
			Intent backIntent = new Intent(this, MainActivity.class);
			startActivity(backIntent);
			this.finish();
			
			break;
		case R.id.btn_totalClear: // 清除本次输入信息
			
			popDialog();
			
			break;
		case R.id.btn_loginClearOne:
			
			algorithms.mPointList.clear();
			view.clear();
			
			break;
		default:
			break;
		}
	}
	
	/**
	 * AlertDialog
	 */
	public void popDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// 2.进行一个对话框参数的设置
		builder.setIcon(R.drawable.reset)
				.setTitle("请选择")
				.setMessage("您确认要重写所有字么")
				.setPositiveButton("是",new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						view.clear();
						refresh();
						algorithms.clearLoginData();
						ToastUtils.showToast(LoginActivity.this, "清除成功!");
					}
				});
		builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		builder.show();	
}
}
