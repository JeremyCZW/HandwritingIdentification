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
	private Button iv_registBack; // ���ز˵�
	private RegistView view; // ����
	private Button btn_registClearCanvas, btn_regist, btn_registClearAll, btn_registClearOne; // ��ջ��������ע�ᣬ�������
	private EditText et_registName; // �û��������
	private TextView tv_registHint, tv_registProcess; // ��ǰ������ʾ,����

	private Algorithms algorithm; // �㷨��
	private static int curIndex; // ��¼ע����Ϣ�����±�
	private final int strLen = InputSamples.REGIST_LEN;// ע����������
	private MyDatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private String username;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_regist);
		initView(); // ��ʼ������
		initData(); // ��ʼ������
	}

	private void initData() {
		algorithm = Algorithms.getInstance(); // ʵ�����㷨��
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
			tv_registHint.setText("��ǰ��: " + InputSamples.REGIST_STR.substring(curIndex, 1 + curIndex));
			tv_registProcess.setText("����: " + ++curIndex + "/" + strLen);
			return true;
		}
		return false;
	}

	/**
	 * ������ʾ��Ϣ
	 */
	private void refresh() {
		curIndex = 0; // �����±�
		nextHint(); // ��ʼ��������ʾ
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_registClearCanvas: // ��һ����

			//���û���룬ֱ�ӷ���
			if(algorithm.isEmpty()){
				break;
			}
			
			if (!nextHint()) {
				ToastUtils.showToast(this, "�����ע��");
				break;
			}
			
			algorithm.preprocess(true);
			view.clear();

			break;
		case R.id.btn_regist: // ���ע��
			
			if(algorithm.isEmpty()){
				break;
			}
			
			if (curIndex + 1 <= strLen) {
				ToastUtils.showToast(this, "��������Ϣ");
				break;
			}

			username = et_registName.getText().toString(); // ��ȡ�û���
			if (username == null || TextUtils.isEmpty(username)) { // �û�����Ч�Լ��
				ToastUtils.showToast(this, "�������û���");
				break;
			}
			
			if(!algorithm.featureVct.isUserNameValid(username)){
				ToastUtils.showToast(this, "�û����ظ���");
				break;
			}
			
			// ע����û�
			if (algorithm.registUser(username) && insertUserInfo()) {

				ToastUtils.showToast(this, "ע��ɹ�!");
				algorithm.clearRegistData();
				// ��ת��������
				Intent intent = new Intent(this, MainActivity.class);
				startActivity(intent);
				this.finish();
			}
			break;
		case R.id.iv_registBack: // �������˵�
			Intent backIntent = new Intent(this, MainActivity.class);
			startActivity(backIntent);
			this.finish();
			break;
		case R.id.btn_registClearAll: // ��������,�����������������Ϣ
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
	 * ���û���Ϣ�������ݿ�
	 * @return
	 */
	private boolean insertUserInfo() {
		// ��ȡ�ڴ��е�����
		HashMap<String, HashMap<Integer, ArrayList<Stroke>>> staticData = algorithm.featureVct.getRegistInfo();
		HashMap<String, HashMap<Integer, ArrayList<Point>>> dynamicData = algorithm.vCmp.getRegistInfo();
		
		//�洢��̬������Ϣ
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

		//�洢��̬������Ϣ
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
	 * �ر�����
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
				.setTitle("��ѡ��")
				.setMessage("��ȷ��Ҫ��д������ô")
				.setPositiveButton("��",new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						view.clear();
						algorithm.clearRegistData();
						refresh();
						ToastUtils.showToast(RegistActivity.this, "����ɹ�!");
					}
				});
		builder.setNegativeButton("��", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.show();	
}
}
