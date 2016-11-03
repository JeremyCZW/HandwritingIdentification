package com.cnsoft.handwriting;

import com.cnsoft.handwriting.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ResultActivity extends Activity{
	private TextView tv_result;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);
		initView();					//��ʼ���ؼ�
		initData();					//��ʼ������
	}

	private void initView() {
		tv_result = (TextView) findViewById(R.id.tv_result);

	}
	
	private void initData() {
		Intent resultIntent = getIntent();
		String resultStr = resultIntent.getStringExtra("userName");		//����жϽ��
		resultStr = resultStr==null?"İ����":resultStr;
		tv_result.setText(resultStr);
	}
	
}
