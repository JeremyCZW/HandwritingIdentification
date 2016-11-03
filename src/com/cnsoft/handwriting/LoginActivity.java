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
	private TextView tv_loginHint, tv_loginProcess;// ������ʾ,����
	private Algorithms algorithms;

	private static int curIndex; // ��ǰ���±�
	private String dynamicStr =""; // ��̬��¼����
	private String staticStr = ""; // ��̬��¼����
	private String loginStr = ""; 
	private int[] randomArr; // �������
	private int[] randomDynamic;//Ϊ��̬����������
	private int[] randomStatic = new int[InputSamples.LOGIN_STATIC_LEN];//Ϊ��̬����������
	private final int strLen = InputSamples.lOGIN_LEN;// ��½��������

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
		
		//�����ȡ��̬�����������1,4,2,6   (4,8)
		randomDynamic = InputSamples.getRandomInt(InputSamples.LOGIN_DYNAMIC_LEN,InputSamples.lOGIN_LEN);
		//�����ȡ��̬�����������0,3,5,7
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
		
		// ��õ�½��̬��������        (4,20);
		randomArr = InputSamples.getRandomInt(InputSamples.LOGIN_DYNAMIC_LEN, InputSamples.REGIST_LEN);
		// ��ö�̬ʶ��׶ε�½���� dynamicStr
		for (int i = 0; i < InputSamples.LOGIN_DYNAMIC_LEN; i++) {
			dynamicStr += InputSamples.REGIST_STR.substring(randomArr[i], randomArr[i] + 1);
		}
		// ��õ�½��̬��������        (4,35);	
		int[] staticArr = InputSamples.getRandomInt(InputSamples.LOGIN_STATIC_LEN,
				InputSamples.LOGIN_STATIC_STR.length());
		// ��þ�̬ʶ��׶ε�½���� staticStr
		for (int i = 0; i < InputSamples.LOGIN_STATIC_LEN; i++) {
			staticStr += InputSamples.LOGIN_STATIC_STR.substring(staticArr[i], staticArr[i] + 1);
		}
		
		//��ϳɵ�¼�ַ���
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
			tv_loginHint.setText("��ǰ��: " + loginStr.substring(curIndex, curIndex + 1));
			tv_loginProcess.setText("����: " + (++curIndex) + "/" + strLen);
			return true;
		}
		return false;
	}

	/**
	 * ������ʾ��Ϣ
	 */
	private void refresh() {
		curIndex = 0;
		nextHint();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_loginClear: // ��һ����

			if(algorithms.isEmpty()){
				break;
			}
			
			// �ж��Ƿ��Ѿ�ȫ�������꣬���Խ����ж���
			if (!nextHint()) {
				ToastUtils.showToast(this, "����ɵ�¼");
				break;
			}
			
			algorithms.preprocess(false);
			view.clear();
			
			break;
		case R.id.btn_loginOk: // ����
			
			if(algorithms.isEmpty()){
				break;
			}
			
			// �ж��ּ�ȡ���Ƿ����
			if (curIndex + 1 <= InputSamples.lOGIN_LEN) {
				ToastUtils.showToast(this, "��������Ϣ");
				break;
			}
			
			ToastUtils.showToast(this, "���ڵ���...");
			
			// ���߳����ڵ�¼
			new Thread(){
				public void run() {
					String userName = algorithms.login(); // ��ȡʶ����:�û���,İ���˻���ע���û�
					algorithms.clearLoginData();
					// ��ת�������
					Intent resultIntent = new Intent(LoginActivity.this, ResultActivity.class);
					resultIntent.putExtra("userName", userName);
					startActivity(resultIntent);
					LoginActivity.this.finish();
				};
			}.start();
			
			view.clear(); // ��ջ���
			break;
		case R.id.iv_loginBack: // ����������
			
			Intent backIntent = new Intent(this, MainActivity.class);
			startActivity(backIntent);
			this.finish();
			
			break;
		case R.id.btn_totalClear: // �������������Ϣ
			
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
		// 2.����һ���Ի������������
		builder.setIcon(R.drawable.reset)
				.setTitle("��ѡ��")
				.setMessage("��ȷ��Ҫ��д������ô")
				.setPositiveButton("��",new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						view.clear();
						refresh();
						algorithms.clearLoginData();
						ToastUtils.showToast(LoginActivity.this, "����ɹ�!");
					}
				});
		builder.setNegativeButton("��", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		builder.show();	
}
}
