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
	private float curX, curY; // ����ڱ����ؼ�������
	private Paint p = new Paint();
	private Path path = new Path();
	private SurfaceHolder holder;
	private DecimalFormat df = new DecimalFormat(); // ��ʽ����
	private ArrayList<Point> mPoint; // һ���ʻ�
	private Algorithms algorithms; // �㷨ʵ��
	private VelocityTracker vTracker; // ��ȡ����

	public LoginView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
		initData();
	}

	/**
	 * ��ʼ������
	 */
	private void initData() {
		algorithms = Algorithms.getInstance(); // �õ��㷨��ʵ��
		df.setMaximumFractionDigits(2); // ���ñ���С����λ��
		holder = this.getHolder();
	}

	/**
	 * ��ʼ����ͼ
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
	 * ��ʼ������
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

		// ��������
		if (vTracker == null) {
			vTracker = VelocityTracker.obtain();
		}
		vTracker.addMovement(event);
		vTracker.computeCurrentVelocity(1); // ���õ�λpx/ms

		// ��װ����Ϣ
		Point point = new Point();
		point.x = curX;
		point.y = v.getHeight() - curY;
		Float vx = vTracker.getXVelocity(); // ��ȡ�ٶ�
		Float vy = vTracker.getYVelocity();
		point.Vx = vx;
		point.Vy = vy;
		point.curTime = System.currentTimeMillis();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:	// ����

			mPoint = new ArrayList<Point>(); // ���ԭʼ�ʻ�����
			mPoint.add(point); // �õ������һ�ʻ�

			path.moveTo(curX, curY); // ����
			draw();
			break;

		case MotionEvent.ACTION_MOVE:	// �ƶ�
			float disPoint = (float) Math.sqrt((point.x - mPoint.get(mPoint.size() - 1).x)
					* (point.x - mPoint.get(mPoint.size() - 1).x)
					+ (point.y - mPoint.get(mPoint.size() - 1).y) * (point.y - mPoint.get(mPoint.size() - 1).y));
			// ����ĳ����ֵ������Ч��
			if (disPoint > algorithms.RECORD_DIS) {
				mPoint.add(point); // �õ������һ�ʻ�
			}

			path.lineTo(curX, curY);
			draw();
			break;

		case MotionEvent.ACTION_UP:		//̧��
			
			float disPointUP = (float) Math.sqrt((point.x - mPoint.get(mPoint.size() - 1).x)
					* (point.x - mPoint.get(mPoint.size() - 1).x)
					+ (point.y - mPoint.get(mPoint.size() - 1).y) * (point.y - mPoint.get(mPoint.size() - 1).y));

			// ����ĳ����ֵ������Ч��
			if (disPointUP > algorithms.RECORD_DIS) {
				mPoint.add(point);// �õ������һ�ʻ�
			}
			algorithms.mPointList.add(mPoint);
			break;
		}

		return true;
	}

}
