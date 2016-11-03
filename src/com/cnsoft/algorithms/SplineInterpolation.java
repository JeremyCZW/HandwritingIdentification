package com.cnsoft.algorithms;

import java.util.ArrayList;

import com.cnsoft.entities.Point;
import com.cnsoft.utils.Logger;

/**
 * 	��������-Ԥ����
 */
public class SplineInterpolation {

	private final float MINVAL = 0.08f; // �ͷ�ֵ

	/**
	 * ������ԽǾ���
	 * 
	 * @param x
	 *            �����δ֪������
	 * @param n
	 *            δ֪������
	 * @param D
	 *            ����
	 * @param A
	 *            ���ԽǾ��������һ��ϵ��
	 * @param B
	 *            ���ԽǾ�������м�ϵ��
	 * @param C
	 *            ���ԽǾ�������ϲ�ϵ�� �����ϵ�� [B0, C0, ... [A1, B1, C1, ... [0, A2, B2,
	 *            C2, ... [0, ... An-1, Bn-1]
	 */
	public void TDMA(float[] X, final int n, float[] A, float[] B, float[] C, float[] D) {
		int i;
		float tmp;
		// �����Ǿ���
		C[0] = C[0] / B[0];
		D[0] = D[0] / B[0];

		for (i = 1; i < n; i++) {
			tmp = (B[i] - A[i] * C[i - 1]);
			C[i] = C[i] / tmp;
			D[i] = (D[i] - A[i] * D[i - 1]) / tmp;
		}

		// ֱ�����X�����һ��ֵ
		X[n - 1] = D[n - 1];

		// ��������� ���X
		for (i = n - 2; i >= 0; i--) {
			X[i] = D[i] - C[i] * X[i + 1];
		}
	}

	/**
	 * ��Ȼ�߽��������������
	 * 
	 * @param pointList
	 *            һ�����ֵĵ���
	 * @param step
	 *            ָ������ֵ������������Խ���б���жϣ����� б����Ϣ��ӵ�ԭ���ĵ㼯��
	 */
	public void cubic_getval(ArrayList<Point> pointList) {
		int n = pointList.size(); // �����ĸ���(n+1)
		if (n < 4) {
			return;
		}
		// ����ϵ��,�������߹�n��
		float[] ai = new float[n - 1];
		float[] bi = new float[n - 1];
		float[] ci = new float[n - 1];
		float[] di = new float[n - 1];

		float[] h = new float[n - 1]; // ��������ļ��

		/*
		 * M�����ϵ��[B0, C0, ...[A1, B1, C1, ...[0, A2, B2, C2, ...[0, ... An-1,
		 * Bn-1]
		 */
		float[] A = new float[n - 2];
		float[] B = new float[n - 2];
		float[] C = new float[n - 2];
		float[] D = new float[n - 2]; // �Ⱥ��ұߵĳ�������
		float[] E = new float[n - 2]; // M����

		float[] M = new float[n]; // �����˵��M����

		int i, sum = 0;

		// ����x�Ĳ���
		for (i = 0; i < n - 1; i++) {
			h[i] = pointList.get(i + 1).x - pointList.get(i).x;
			sum += h[i];
			// �ͷ�ֵ
			if (h[i] == 0) {
				h[i] = MINVAL;
			}
		}
		// ָ��ϵ��
		for (i = 0; i < n - 3; i++) {
			A[i] = h[i];
			B[i] = 2 * (h[i] + h[i + 1]);
			C[i] = h[i + 1];
		}

		// ָ������D
		for (i = 0; i < n - 3; i++) {
			D[i] = 6 * ((pointList.get(i + 2).y - pointList.get(i + 1).y) / h[i + 1]
					- (pointList.get(i + 1).y - pointList.get(i).y) / h[i]);
		}

		// ������ԽǾ��󣬽����ֵ��E
		TDMA(E, n - 3, A, B, C, D);

		M[0] = 0; // ��Ȼ�߽���׶�MΪ0
		M[n - 1] = 0; // ��Ȼ�߽��ĩ��MΪ0
		for (i = 1; i < n - 1; i++) {
			M[i] = E[i - 1]; // ������Mֵ
		}

		// ���������������ߵ�ϵ��
		for (i = 0; i < n - 1; i++) {
			ai[i] = pointList.get(i).y;
			bi[i] = (pointList.get(i + 1).y - pointList.get(i).y) / h[i] - (2 * h[i] * M[i] + h[i] * M[i + 1]) / 6;
			pointList.get(i).slopeByLine = bi[i];
			ci[i] = M[i] / 2;
			di[i] = (M[i + 1] - M[i]) / (6 * h[i]);
		}
	}
}
