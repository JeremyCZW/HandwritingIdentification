package com.cnsoft.algorithms;

import java.util.ArrayList;

import com.cnsoft.entities.Point;
import com.cnsoft.utils.Logger;

public class Preprocess {
	private final int K = 16;
	private final int SIGMA = 1;

	/**
	 * 位置归一化
	 * 
	 * @param p
	 */
	private void posNormalize(ArrayList<Point> p) {
		float sumX = 0, sumY = 0, avgX, avgY;
		int len = p.size();
		for (Point mPoint : p) {
			sumX += mPoint.x;
			sumY += mPoint.y;
		}

		avgX = sumX / (float) len;
		avgY = sumY / (float) len;

		for (Point mPoint : p) {
			mPoint.x -= avgX;
			mPoint.y -= avgY;
		}
	}

	/**
	 * 大小归一化
	 * 
	 * @param p
	 */
	private void sizeNormalize(ArrayList<Point> p) {
		float squareP = 0;
		for (Point mPoint : p) {
			squareP += (mPoint.x * mPoint.x + mPoint.y * mPoint.y);
		}
		squareP = (float) Math.sqrt(squareP);

		for (Point mPoint : p) {
			mPoint.x = K * mPoint.x / squareP;
			mPoint.y = K * mPoint.y / squareP;
		}
	}

	/**
	 * 高斯滤波,平滑化
	 * 
	 * @param p
	 */
	public ArrayList<Point> smoothing(ArrayList<Point> p) {
		int len = p.size();
		if (len < 6) {
			return p;
		}
		ArrayList<Point> p2 = new ArrayList<Point>();
		float sum = 0;
		for (int j = -2 * SIGMA; j <= 2 * SIGMA; j++) {
			sum += Math.pow(Math.E, (-j * j / (2 * SIGMA * SIGMA)));
		}
		p2.add(p.get(0));
		p2.add(p.get(1));
		for (int i = 2; i < len - 2; i++) {
			Point tmp = new Point();
			for (int j = -2 * SIGMA; j <= 2 * SIGMA; j++) {
				float divide = (float) (Math.pow(Math.E, (-j * j / (2 * SIGMA * SIGMA)))) / sum;
				tmp.x += p.get(j + i).x * divide;
				tmp.y += p.get(j + i).y * divide;
				tmp.Vx += p.get(j + i).Vx * divide;
				tmp.Vy += p.get(j + i).Vy * divide;
			}
			tmp.curTime = p.get(i).curTime;
			p2.add(tmp);
		}
		p2.add(p.get(len - 2));
		p2.add(p.get(len - 1));
		return p2;
	}

	/**
	 * 预处理
	 * 
	 * @param mPoint
	 *            点序
	 */
	public ArrayList<Point> preprocess(ArrayList<Point> mPoint) {
		posNormalize(mPoint);
		sizeNormalize(mPoint);
//		mPoint = smoothing(mPoint);
		dynamicPreprocess(mPoint);
		return mPoint;
	}

	
	/**
	 * 动态特征预处理
	 * 
	 * @param mPoint
	 *            一个笔画
	 */
	private void dynamicPreprocess(ArrayList<Point> mPoint) {
		int n = mPoint.size(), i;
		if (n < 2) {
			return;
		}
		// 计算加速度
		mPoint.get(0).aX = (float) ((mPoint.get(1).Vx - mPoint.get(0).Vx)
				/ (mPoint.get(1).curTime - mPoint.get(0).curTime));
		mPoint.get(0).aY = (float) ((mPoint.get(1).Vy - mPoint.get(0).Vy)
				/ (mPoint.get(1).curTime - mPoint.get(0).curTime));
		for (i = 1; i < n - 1; i++) {
			mPoint.get(i).aX = (float) ((mPoint.get(i + 1).Vx - mPoint.get(i - 1).Vx)
					/ (mPoint.get(i + 1).curTime - mPoint.get(i - 1).curTime));
			mPoint.get(i).aY = (float) ((mPoint.get(i + 1).Vy - mPoint.get(i - 1).Vy)
					/ (mPoint.get(i + 1).curTime - mPoint.get(i - 1).curTime));
		}
		mPoint.get(n - 1).aX = (float) ((mPoint.get(n - 1).Vx - mPoint.get(n - 2).Vx)
				/ (mPoint.get(n - 1).curTime - mPoint.get(n - 2).curTime));
		mPoint.get(n - 1).aY = (float) ((mPoint.get(n - 1).Vy - mPoint.get(n - 2).Vy)
				/ (mPoint.get(n - 1).curTime - mPoint.get(n - 2).curTime));
	}
}
