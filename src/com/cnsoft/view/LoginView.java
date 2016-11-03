package com.cnsoft.view;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.cnsoft.algorithms.Algorithms;
import com.cnsoft.entities.Point;
import com.cnsoft.utils.Logger;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.VelocityTracker;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

public class LoginView extends SurfaceView implements Callback, OnTouchListener {
	private float curX, curY; // 相对于背景控件的坐标
	private Paint p = new Paint();
	private Path path = new Path();
	private SurfaceHolder holder;
	private DecimalFormat df = new DecimalFormat(); // 格式控制
	private ArrayList<Point> mPoint; // 一个笔画
	private Algorithms algorithms; // 算法实例
	private VelocityTracker vTracker; // 获取速率

	public LoginView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
		initData();
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		algorithms = Algorithms.getInstance(); // 得到算法类实例
		df.setMaximumFractionDigits(2); // 设置保留小数点位数
		holder = this.getHolder();
	}

	/**
	 * 初始化视图
	 */
	private void initView() {
		getHolder().addCallback(this);
		p.setColor(Color.RED);
		p.setTextSize(10);
		p.setAntiAlias(true);
		p.setStrokeWidth(5);
		p.setStyle(Style.STROKE);
		setOnTouchListener(this);
	}

	/**
	 * 初始化画布
	 */
	public void draw() {
		Canvas canvas = holder.lockCanvas();
		canvas.drawColor(Color.WHITE);
		canvas.drawPath(path, p);
		holder.unlockCanvasAndPost(canvas);
	}

	public void clear() {
		path.reset();
		draw();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		draw();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		curX = event.getX();
		curY = event.getY();

		// 跟踪速率
		if (vTracker == null) {
			vTracker = VelocityTracker.obtain();
		}
		vTracker.addMovement(event);
		vTracker.computeCurrentVelocity(1); // 设置单位px/ms

		// 封装点信息
		Point point = new Point();
		point.x = curX;
		point.y = v.getHeight() - curY;
		Float vx = vTracker.getXVelocity(); // 获取速度
		Float vy = vTracker.getYVelocity();
		point.Vx = vx;
		point.Vy = vy;
		point.curTime = System.currentTimeMillis();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:	// 按下

			mPoint = new ArrayList<Point>(); // 清空原始笔画缓存
			mPoint.add(point); // 该点加入这一笔画

			path.moveTo(curX, curY); // 画线
			draw();
			break;

		case MotionEvent.ACTION_MOVE:	// 移动
			float disPoint = (float) Math.sqrt((point.x - mPoint.get(mPoint.size() - 1).x)
					* (point.x - mPoint.get(mPoint.size() - 1).x)
					+ (point.y - mPoint.get(mPoint.size() - 1).y) * (point.y - mPoint.get(mPoint.size() - 1).y));
			// 大于某个阈值则算有效点
			if (disPoint > algorithms.RECORD_DIS) {
				mPoint.add(point); // 该点加入这一笔画
			}

			path.lineTo(curX, curY);
			draw();
			break;

		case MotionEvent.ACTION_UP:		//抬笔
			
			float disPointUP = (float) Math.sqrt((point.x - mPoint.get(mPoint.size() - 1).x)
					* (point.x - mPoint.get(mPoint.size() - 1).x)
					+ (point.y - mPoint.get(mPoint.size() - 1).y) * (point.y - mPoint.get(mPoint.size() - 1).y));

			// 大于某个阈值则算有效点
			if (disPointUP > algorithms.RECORD_DIS) {
				mPoint.add(point);// 该点加入这一笔画
			}
			algorithms.mPointList.add(mPoint);
			break;
		}

		return true;
	}

}
