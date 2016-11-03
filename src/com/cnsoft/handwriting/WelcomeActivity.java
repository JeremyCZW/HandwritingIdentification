package com.cnsoft.handwriting;

import java.util.ArrayList;
import java.util.HashMap;

import com.cnsoft.algorithms.Algorithms;
import com.cnsoft.entities.Point;
import com.cnsoft.entities.Stroke;
import com.cnsoft.utils.MyDatabaseHelper;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

/**
 * 欢迎界面,数据导入
 * 
 * @author JeremyChen
 *
 */
public class WelcomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == 0x123) {
					Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
					startActivity(intent);
					WelcomeActivity.this.finish();
				}
			}
		};
		
		/**
		 * 异步加载用户数据
		 */
		new Thread() {
			public void run() {
				loadUserInfo();
			};
		}.start();

		/**
		 * 等待线程
		 */
		new Thread() {
			@Override
			public void run() {
				super.run();
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				handler.sendEmptyMessage(0x123);
			}
		}.start();
	}

	/**
	 * 从数据库中加载用户数据
	 */
	private void loadUserInfo() {

		MyDatabaseHelper dbHelper = new MyDatabaseHelper(this);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Algorithms algorithms = Algorithms.getInstance();
		// 查询所有静态特征数据库
		Cursor staticCursor = db.rawQuery("select * from " + MyDatabaseHelper.t_static, null);
		HashMap<String, HashMap<Integer, ArrayList<Stroke>>> staticList = new HashMap<String, HashMap<Integer, ArrayList<Stroke>>>();
		while (staticCursor.moveToNext()) {
			HashMap<Integer, ArrayList<Stroke>> oneUser = new HashMap<Integer, ArrayList<Stroke>>();
			String username = staticCursor.getString(0);
			String[] eightKind = new String[8];
			for (int i = 0; i < 8; i++) {
				ArrayList<Stroke> oneKind = new ArrayList<Stroke>();
				eightKind[i] = staticCursor.getString(i + 1);
				// 如果这个方向没向量
				if (TextUtils.isEmpty(eightKind[i])) {
					oneUser.put(i + 1, oneKind);
					continue;
				}
				String[] vctArray = eightKind[i].split(";");
				int vctNum = vctArray.length;
				for (int j = 0; j < vctNum; j++) {
					String[] val = vctArray[j].split(",");
					double[] featherVct = new double[8];
					for (int k = 0; k < 8; k++) {
						featherVct[k] = Double.parseDouble(val[k]);
					}
					Stroke s = new Stroke();
					s.fetureVct = featherVct;
					oneKind.add(s);
				}
				oneUser.put(i + 1, oneKind);
			}
			staticList.put(username, oneUser);
		}
		algorithms.featureVct.setUserList(staticList);

		// 查询所有动态特征数据库
		HashMap<String, HashMap<Integer, ArrayList<Point>>> dynamicList = new HashMap<String, HashMap<Integer, ArrayList<Point>>>();
		Cursor dynamicCursor = db.rawQuery("select * from " + MyDatabaseHelper.t_dynamic, null);
		while (dynamicCursor.moveToNext()) {
			HashMap<Integer, ArrayList<Point>> oneUser = new HashMap<Integer, ArrayList<Point>>();
			String username = dynamicCursor.getString(0);
			String dynamicFeature = dynamicCursor.getString(1);
			String[] vctArray = dynamicFeature.split(";");
			for (String vctStr : vctArray) {
				String[] typeStr = vctStr.split(":");
				Integer numb = Integer.parseInt(typeStr[0]);
				ArrayList<Point> charList = new ArrayList<Point>();
				String[] charVctList = typeStr[1].split("/");
				for (String onePoint : charVctList) {
					Point p = new Point();
					String[] rawV = onePoint.split(",");
					p.Vx = Float.parseFloat(rawV[0]);
					p.Vy = Float.parseFloat(rawV[1]);
					p.x = Float.parseFloat(rawV[2]);
					p.y = Float.parseFloat(rawV[3]);
					charList.add(p);
				}
				oneUser.put(numb, charList);
			}
			dynamicList.put(username, oneUser);
		}
		algorithms.vCmp.setRegistIfo(dynamicList);

		dbHelper.close();
	}

}
