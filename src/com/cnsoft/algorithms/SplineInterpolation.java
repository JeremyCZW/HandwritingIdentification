package com.cnsoft.algorithms;

import java.util.ArrayList;

import com.cnsoft.entities.Point;
import com.cnsoft.utils.Logger;

/**
 * 	样条函数-预处理
 */
public class SplineInterpolation {

	private final float MINVAL = 0.08f; // 惩罚值

	/**
	 * 求解三对角矩阵
	 * 
	 * @param x
	 *            待求的未知数向量
	 * @param n
	 *            未知数个数
	 * @param D
	 *            常数
	 * @param A
	 *            三对角矩阵的最下一侧系数
	 * @param B
	 *            三对角矩阵的最中间系数
	 * @param C
	 *            三对角矩阵的最上侧系数 矩阵的系数 [B0, C0, ... [A1, B1, C1, ... [0, A2, B2,
	 *            C2, ... [0, ... An-1, Bn-1]
	 */
	public void TDMA(float[] X, final int n, float[] A, float[] B, float[] C, float[] D) {
		int i;
		float tmp;
		// 上三角矩阵
		C[0] = C[0] / B[0];
		D[0] = D[0] / B[0];

		for (i = 1; i < n; i++) {
			tmp = (B[i] - A[i] * C[i - 1]);
			C[i] = C[i] / tmp;
			D[i] = (D[i] - A[i] * D[i - 1]) / tmp;
		}

		// 直接求出X的最后一个值
		X[n - 1] = D[n - 1];

		// 逆向迭代， 求出X
		for (i = n - 2; i >= 0; i--) {
			X[i] = D[i] - C[i] * X[i + 1];
		}
	}

	/**
	 * 自然边界的三次样条曲线
	 * 
	 * @param pointList
	 *            一个文字的点序
	 * @param step
	 *            指定的阈值，超过它则可以进行斜率判断，并将 斜率信息添加到原来的点集中
	 */
	public void cubic_getval(ArrayList<Point> pointList) {
		int n = pointList.size(); // 计算点的个数(n+1)
		if (n < 4) {
			return;
		}
		// 曲线系数,样条曲线共n条
		float[] ai = new float[n - 1];
		float[] bi = new float[n - 1];
		float[] ci = new float[n - 1];
		float[] di = new float[n - 1];

		float[] h = new float[n - 1]; // 相邻两点的间隔

		/*
		 * M矩阵的系数[B0, C0, ...[A1, B1, C1, ...[0, A2, B2, C2, ...[0, ... An-1,
		 * Bn-1]
		 */
		float[] A = new float[n - 2];
		float[] B = new float[n - 2];
		float[] C = new float[n - 2];
		float[] D = new float[n - 2]; // 等号右边的常数矩阵
		float[] E = new float[n - 2]; // M矩阵

		float[] M = new float[n]; // 包含端点的M矩阵

		int i, sum = 0;

		// 计算x的步长
		for (i = 0; i < n - 1; i++) {
			h[i] = pointList.get(i + 1).x - pointList.get(i).x;
			sum += h[i];
			// 惩罚值
			if (h[i] == 0) {
				h[i] = MINVAL;
			}
		}
		// 指定系数
		for (i = 0; i < n - 3; i++) {
			A[i] = h[i];
			B[i] = 2 * (h[i] + h[i + 1]);
			C[i] = h[i + 1];
		}

		// 指定常数D
		for (i = 0; i < n - 3; i++) {
			D[i] = 6 * ((pointList.get(i + 2).y - pointList.get(i + 1).y) / h[i + 1]
					- (pointList.get(i + 1).y - pointList.get(i).y) / h[i]);
		}

		// 求解三对角矩阵，结果赋值给E
		TDMA(E, n - 3, A, B, C, D);

		M[0] = 0; // 自然边界的首端M为0
		M[n - 1] = 0; // 自然边界的末端M为0
		for (i = 1; i < n - 1; i++) {
			M[i] = E[i - 1]; // 其它的M值
		}

		// 计算三次样条曲线的系数
		for (i = 0; i < n - 1; i++) {
			ai[i] = pointList.get(i).y;
			bi[i] = (pointList.get(i + 1).y - pointList.get(i).y) / h[i] - (2 * h[i] * M[i] + h[i] * M[i + 1]) / 6;
			pointList.get(i).slopeByLine = bi[i];
			ci[i] = M[i] / 2;
			di[i] = (M[i + 1] - M[i]) / (6 * h[i]);
		}
	}
}
