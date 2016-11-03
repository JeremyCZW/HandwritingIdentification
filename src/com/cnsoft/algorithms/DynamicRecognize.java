package com.cnsoft.algorithms;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import com.cnsoft.constants.InputSamples;
import com.cnsoft.entities.Point;
import com.cnsoft.utils.Logger;

public class DynamicRecognize {
	// �ж�Ϊİ���˵���ֵ
	private final float MINV = 500f;
	private final float MINP = 100000f;

	private final int TOPN = 3;
	private DecimalFormat df = new DecimalFormat(); // ��ʽ����
	private int curIndex = 0; // ��¼��ǰ�ַ�����
	public int[] randomInt; // ��̬����������ע���ֵ�����
	public int[] randomDynamic;// ��̬��λ������
	public ArrayList<Point> charVList; // һ���ֵ����ʵļ���(����)
	public HashMap<Integer, ArrayList<Point>> registCharsVList;// һ�������������ʵļ���
	public HashMap<Integer, ArrayList<Point>> loginCharsVList;// һ�������������ʵļ���
	public HashMap<String, HashMap<Integer, ArrayList<Point>>> userList; // ����ע�������ʼ���(�����ݿ�Խ�)

	public DynamicRecognize() {
		super();
		df.setMaximumFractionDigits(1); // ���ñ���С����λ��
		charVList = new ArrayList<Point>();
		registCharsVList = new HashMap<Integer, ArrayList<Point>>();
		loginCharsVList = new HashMap<Integer, ArrayList<Point>>();
		userList = new HashMap<String, HashMap<Integer, ArrayList<Point>>>();
	}

	/**
	 * �����¼��Ϣ
	 */
	public void clearLoginInput() {
		curIndex = 0;
		charVList.clear();
		loginCharsVList.clear();
	}

	/**
	 * ���ע����Ϣ
	 */
	public void clearRegistInput() {
		curIndex = 0;
		charVList.clear();
		registCharsVList.clear();
	}

	/**
	 * ���������Ϣ
	 */
	public void clearAll() {
		curIndex = 0;
		charVList.clear();
		loginCharsVList.clear();
		registCharsVList.clear();
		userList.clear();
	}

	/**
	 * ��һ���ּ���ע���ּ�����(����:д��һ���ֵ�ʱ��)
	 */
	public void addChars2Regist() {
		if (charVList != null && charVList.size() > 0) { // ����ֲ���,����ֵ��ʱ�򣬽�������ּ�����
			registCharsVList.put(++curIndex, charVList);
			charVList = new ArrayList<Point>(); // Ϊ��д��һ��������׼��
		}
	}

	/**
	 * ��һ���ּ�������ֵ伯����(����:д��һ���ֵ�ʱ��)
	 */
	public void addChars2Login() {
		if (charVList != null && charVList.size() > 0) {
			loginCharsVList.put(++curIndex, charVList);
			charVList = new ArrayList<Point>();
		}
	}

	/**
	 * ����ǰ�û���ӵ��û�������
	 * 
	 * @param userName
	 *            �û���
	 * @return �Ƿ�����ɹ�
	 */
	public boolean addUser(String userName) {
		// ������Ч�Լ��
		if (registCharsVList != null && registCharsVList.size() > 0) {
			userList.put(userName, registCharsVList);
			// ��ʼ����ʱ����,Ϊ��һ�û�ע���ṩ׼��
			registCharsVList = new HashMap<Integer, ArrayList<Point>>();
			curIndex = 0;
			return true;
		}
		curIndex = 0;
		return false;
	}

	public HashMap<String, HashMap<Integer, ArrayList<Point>>> getRegistInfo() {
		return userList;
	}

	public void setRegistIfo(HashMap<String, HashMap<Integer, ArrayList<Point>>> userList) {
		this.userList = userList;
	}

	/**
	 * ��̬������ʱ�����������ۺ��б�
	 * 
	 * @return 
	 */
	public String recognizebyDynamic(ArrayList<String> includeUser) {
		String result = "�����ж���Ϣ(����Խ��Խ����) :\n";
		float tmpVx, tmpVy, tmpX, tmpY, curSumByV = 0, curSumByPos = 0;
		// ��Ϣ��Ч�Լ��
		int userNum = userList.size(); // �û�����
		if (userNum == 0) {
			return "����ע���û�";
		}

		// top-N������������
		int curTopN = TOPN > userNum ? userNum : TOPN;
		TopN[] topn = new TopN[curTopN];
		for (int i = 0; i < curTopN; i++) {
			topn[i] = new TopN();
		}

		for (String userName : userList.keySet()) {
			HashMap<Integer, ArrayList<Point>> registMap = userList.get(userName);
			for (int i = 0; i < InputSamples.LOGIN_DYNAMIC_LEN; i++) {
				ArrayList<Point> registCharList = registMap.get(randomInt[i] + 1);
				ArrayList<Point> loginCharList = loginCharsVList.get(randomDynamic[i] + 1);

				// ����������Ϣ
				ArrayList<Float> registVx = new ArrayList<Float>();
				ArrayList<Float> registVy = new ArrayList<Float>();
				ArrayList<Float> registX = new ArrayList<Float>();
				ArrayList<Float> registY = new ArrayList<Float>();

				ArrayList<Float> loginVx = new ArrayList<Float>();
				ArrayList<Float> loginVy = new ArrayList<Float>();
				ArrayList<Float> loginX = new ArrayList<Float>();
				ArrayList<Float> loginY = new ArrayList<Float>();

				for (Point P1 : registCharList) {
					registX.add(P1.x);
					registY.add(P1.y);
					registVx.add(P1.Vx);
					registVy.add(P1.Vy);
				}
				for (Point P2 : loginCharList) {
					loginX.add(P2.x);
					loginY.add(P2.y);
					loginVx.add(P2.Vx);
					loginVy.add(P2.Vy);
				}

				// ����DTW����
				tmpX = DTW.dtw(registX, loginX);
				tmpY = DTW.dtw(registY, loginY);
				tmpVx = DTW.dtw(registVx, loginVx);
				tmpVy = DTW.dtw(registVy, loginVy);
				curSumByPos += (tmpX * tmpX + tmpY * tmpY);
				curSumByV += (tmpVx * tmpVx + tmpVy * tmpVy);
			}

			curSumByPos /= InputSamples.LOGIN_DYNAMIC_LEN;
			curSumByV /= InputSamples.LOGIN_DYNAMIC_LEN;
			result+="�û���: "+userName+" ����: "+df.format(curSumByV)+"\n";
			// �������ݵ�top-n
			for (int i = 0; i < curTopN; i++) {
				// �����������ƶ�����
				if (curSumByPos < topn[i].levelByPos) {
					// ������һλ
					for (int j = curTopN - 2; j >= i; j--) {
						topn[j + 1].levelByPos = topn[j].levelByPos;
						topn[j + 1].userNameByPos = topn[j].userNameByPos;
					}
					topn[i].levelByPos = curSumByPos;
					topn[i].userNameByPos = userName;
					break;
				}
			}
			for (int i = 0; i < curTopN; i++) {
				// ��̬�������ƶ�����
				if (curSumByV < topn[i].levelByV) {
					for (int j = curTopN - 2; j >= i; j--) {
						topn[j + 1].levelByV = topn[j].levelByV;
						topn[j + 1].userNameByV = topn[j].userNameByV;
					}
					topn[i].levelByV = curSumByV;
					topn[i].userNameByV = userName;
					break;
				}
			}
			curSumByPos = curSumByV = 0;
		}

		// �жϾ���������û��Ƿ�С�ڸ�����ֵ
		if (topn[0].userNameByPos.equals(topn[0].userNameByV) && topn[0].levelByV < MINV && topn[0].levelByPos < MINP) {
			includeUser.add(topn[0].userNameByPos);
		}
		return result;
	}

	/**
	 * top-N struct
	 * 
	 * @author JeremyChen
	 * 
	 */
	class TopN {

		public TopN() {
			super();
			userNameByV = userNameByPos = "";
			levelByV = levelByPos = Double.MAX_VALUE;
		}

		String userNameByV;
		double levelByV;
		String userNameByPos;
		double levelByPos;
	}
}
