package com.cnsoft.algorithms;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import com.cnsoft.entities.Point;
import com.cnsoft.entities.Stroke;
import com.cnsoft.utils.Logger;


/**
 * ��̬�����б���
 * 
 * @author JeremyChen
 *
 */
public class StaticRecognize {
	
	private final int DIMEN = 8; // ��������ά��
	private final int BYFRONT = 10;	//ͳ�Ƹ��ʷֲ�����
	private final double BYMIN = 50; // ȷ��Ϊ���û�������ٽ�ֵ
	private final float P_BYSTEP = 10f; // ���ݸ���
	private DecimalFormat df = new DecimalFormat(); // ��ʽ����
	// 8���ʻ�����,�����ע��
	public HashMap<Integer, ArrayList<Stroke>> strokesRegistMap;
	public ArrayList<Stroke> simpleLoginList;	 // �ı���������б�
	public ArrayList<Stroke> multiLoginList; 	 // �ı����������б�
	// ���û�����
	public HashMap<String, HashMap<Integer, ArrayList<Stroke>>> userList;
	// �����жϼ�¼��
	public HashMap<String, Integer[]> resultMatrix;
	// ��������,ѡ��ǰTOPN��
	private final int TOPN = 3;
	public double[] topNDisMatrix;
	public String[] topNNameMatrix;

	public StaticRecognize() {
		super();
		df.setMaximumFractionDigits(2); // ���ñ���С����λ��
		newRegistStrokeMap();
		newLoginStrokeMap();
		userList = new HashMap<String, HashMap<Integer, ArrayList<Stroke>>>(); 
	}

	/**
	 * ���û�����,ע����Ϣ ��ʼ���������ӳ��
	 */
	public void newRegistStrokeMap() {
		strokesRegistMap = new HashMap<Integer, ArrayList<Stroke>>();
		for (int i = 1; i <= DIMEN; i++) {
			strokesRegistMap.put(i, new ArrayList<Stroke>());
		}
	}

	/**
	 * ����,���û�����һ��������Ϣ,�輰ʱ���� ��ʼ������֤���ӳ��
	 */
	public void newLoginStrokeMap() {
		simpleLoginList = new ArrayList<Stroke>();
		multiLoginList = new ArrayList<Stroke>();
	}

	/**
	 * ������õ������ӳ��
	 */
	public void clearLoginMap() {
		simpleLoginList.clear();
		multiLoginList.clear();
	}

	/**
	 * ������õ������ӳ��
	 */
	public void clearRegistMap() {
		for (int i = 1; i <= DIMEN; i++) {
			strokesRegistMap.get(i).clear();
		}
	}

	/**
	 * ���浱ǰ�û�����,���ע��
	 * @return
	 */
	public boolean saveUserInfo(String registUserName) {
			userList.put(registUserName, strokesRegistMap);
			newRegistStrokeMap(); // �½���һ���û�ʵ��
			return true;
	}

	/**
	 * ����û�����Ч��
	 * @param registUserName
	 * @return
	 */
	public boolean isUserNameValid(String registUserName){
		if (userList.containsKey(registUserName)) {
			return false;
		}
		return true;
	}
	
	public HashMap<String, HashMap<Integer, ArrayList<Stroke>>> getRegistInfo() {
		return userList;
	}

	public void setUserList(HashMap<String, HashMap<Integer, ArrayList<Stroke>>> userList) {
		this.userList = userList;
	}

	public void cleanAll() {
		clearLoginMap();
		clearRegistMap();
		userList.clear();
	}

	/**
	 * �ʻ����
	 * 
	 * @param isRegist
	 *            �ж��Ƿ�Ϊע��
	 * @param mPoint
	 *            ����ĵ���
	 * @param isSimpleLogin
	 *            �Ƿ�Ϊ�ı���ص�¼
	 * @param byLen
	 *            ��ֵĳ�����ֵ(������ľ��볬����ֵ�ĵ㿼�ǲ��)
	 * @param byK
	 *            ��ֵ�б����ֵ(������б�����������߲����ֵ�ĵ㿼�ǲ��) ��ֽ����������������8���ʻ�������
	 */
	public void extractStroke(boolean isRegist, ArrayList<Point> mPoint, boolean isSimpleLogin, float byLen,
			float byK) {

		// ��Ч�Լ��
		if (mPoint == null || mPoint.size() < 3) {
			return;
		}

		int n = mPoint.size(); // ����������
		Point frontP = mPoint.get(0); // ��¼�ʶ����
		// ���Ҳ�ֵ�
		for (int i = 1; i < n - 1; i++) {
			Point lastP = mPoint.get(i);
			// ����õ����һ��ֵ����С��ĳ����ֵ,�������
			if (Math.sqrt((lastP.x - frontP.x) * (lastP.x - frontP.x)
					+ (lastP.y - frontP.y) * (lastP.y - frontP.y)) < byLen) {
				continue;
			}
			// б����Ч���ж�
			float subX = (mPoint.get(i + 1).x - lastP.x);
			if (Math.abs(subX) < 2) {
				continue;
			}
			// �жϸõ��Ƿ�����б�ʲ������
			float subK = (float) Math
					.abs(Math.atan(lastP.slopeByLine) - Math.atan((mPoint.get(i + 1).y - lastP.y) / subX));

			if (subK > byK) {
				Stroke stroke = new Stroke(); // �½��ʶλ�����Ԫ
				stroke.fromPnt = frontP;
				stroke.toPnt = lastP;
				getFeatureVct(stroke, isRegist, isSimpleLogin); // ��ȡ��������
				frontP = lastP;
			}
		}
		// ���һ��Ĵ���
		Stroke stroke = new Stroke(); // �½��ʶλ�����Ԫ
		stroke.fromPnt = frontP;
		stroke.toPnt = mPoint.get(n - 1);
		getFeatureVct(stroke, isRegist, isSimpleLogin); // ��ȡ��������
	}

	/**
	 * ���ݻ��ֽ����ȡ��������
	 * 
	 * @param mStroke
	 *            ���ֽ��
	 * @param isRegist
	 * 			  �Ƿ�Ϊע��
	 * @param isSimpleLogin
	 *            �Ƿ�Ϊ�ı���ص�¼
	 * @return �������������µ�ԭ���ֽ����,�������û���Ϣ
	 */
	public Stroke getFeatureVct(Stroke mStroke, boolean isRegist, boolean isSimpleLogin) {
		// ���ݵ������ע��ѡ��

		Point fromPoint = mStroke.fromPnt; // ��ʼ��
		Point toPoint = mStroke.toPnt; // �յ�

		float dx = Math.abs(toPoint.x - fromPoint.x);
		float dy = Math.abs(toPoint.y - fromPoint.y);
		float dis = (float) Math.sqrt(dx * dx + dy * dy);
		float s = (float) Math.sqrt(dx * dx + dy * dy);
		float a1 = Math.abs(dx - dy) / s;
		float a2 = (float) (Math.sqrt(2) * Math.min(dx, dy) / s);
		int a1Index = 0, a2Index = 0; // a1,a2����
		float subX = fromPoint.x - toPoint.x;
		float subY = fromPoint.y - toPoint.y;
		float absX_Y = Math.abs(toPoint.y - fromPoint.y) - Math.abs(toPoint.x - fromPoint.x);
		// ���ൽָ��8���ʻ������ᣬ��..
		if (subX <= 0 && absX_Y <= 0) {
			a1Index = 6;
		} else if (subX > 0 && absX_Y <= 0) {
			a1Index = 2;
		} else if (subY <= 0 && absX_Y > 0) {
			a1Index = 4;
		} else if (subY > 0 && absX_Y > 0) {
			a1Index = 0;
		}

		if (subX <= 0 && subY <= 0) {
			a2Index = 5;
		} else if (subX <= 0 && subY > 0) {
			a2Index = 7;
		} else if (subX > 0 && subY > 0) {
			a2Index = 1;
		} else if (subX > 0 && subY <= 0) {
			a2Index = 3;
		}
		mStroke.fetureVct = new double[DIMEN];
		mStroke.fetureVct[a1Index] = s * a1 / (a1 + a2); // ��������,������һ��stroke��
		mStroke.fetureVct[a2Index] = s * a2 / (a1 + a2);

		// �ж��Ƿ�Ϊע��
		if (isRegist) {
			strokesRegistMap.get(a1Index + 1).add(mStroke);
			strokesRegistMap.get(a2Index + 1).add(mStroke);
		} else {
			// �����±�
			mStroke.index1 = a1Index;
			mStroke.index2 = a2Index;
			if (isSimpleLogin) {
				simpleLoginList.add(mStroke);
			} else {
				multiLoginList.add(mStroke);
			}
		}

		return mStroke;
	}

	/**
	 * ����ŷʽ����
	 * 
	 * @param regist
	 * @param login
	 * @return distance
	 */
	public double calculateDistance(double[] regist, double[] login) {
		int i;
		double sum = 0;
		for (i = 0; i < DIMEN; i++) {
			sum += (regist[i] - login[i]) * (regist[i] - login[i]);
		}
		return sum;
	}

	
	/**
	 * �ı��޹�ʶ��
	 * 
	 * @param includedUser	�������û�
	 *             
	 * @return �û���
	 */
	public String recognizeUserByMulti(ArrayList<String> includedUser) {
		ArrayList<Stroke> loginList = multiLoginList;
		int vctLen = loginList.size(); // ������������
		
		if (includedUser.size() == 0) {
			return "İ����";
		}
		
		int curTopN = TOPN > (includedUser.size()) ?(includedUser.size()) : TOPN; // ����ӦtopN
		HashMap<String, ResultMatrix> topnResult = new HashMap<String, StaticRecognize.ResultMatrix>(); // �������
		// ��С����洢����
		HashMap<String, Double[]> distMap = new HashMap<String, Double[]>();
		for (String userName : userList.keySet()) {
				distMap.put(userName, new Double[vctLen]);
		}

		double minDist;
		TopN[] topn = null;

		// ����ÿ��������������
		for (int k = 0; k < vctLen; k++) {
			Stroke mStroke = loginList.get(k);

			// ��������,��������������(0-7)
			int index1 = mStroke.index1;
			int index2 = mStroke.index2;
			double[] loginVct = mStroke.fetureVct;

			// top-N��������
			topn = new TopN[curTopN];
			for (int i = 0; i < curTopN; i++) {
				topn[i] = new TopN();
				topn[i].level = Double.MAX_VALUE;
			}

			// ��ע���û���ͬ��������Ƚ�,��min-dist
			String userName = includedUser.get(0);
				// ���Ϊ�ų�������,������
				ArrayList<Stroke> registStkList1 = userList.get(userName).get(index1 + 1);
				ArrayList<Stroke> registStkList2 = userList.get(userName).get(index2 + 1);
				minDist = Double.MAX_VALUE; // ��ʼ����ʱ��Сֵ,��ʾ�뵱ǰ�û�����С����
				// ����������������
				for (Stroke stroke1 : registStkList1) {
					double[] registVct = stroke1.fetureVct;
					double dist = calculateDistance(loginVct, registVct);
					minDist = (dist < minDist ? dist : minDist);
				}

				for (Stroke stroke2 : registStkList2) {
					double[] registVct = stroke2.fetureVct;
					double dist = calculateDistance(loginVct, registVct);
					minDist = (dist < minDist ? dist : minDist);
				}
				distMap.get(userName)[k] = minDist; // �洢�����������

				// �������ݵ�top-n
				for (int i = 0; i < curTopN; i++) {
					if (minDist < topn[i].level) {
						// ������һλ
						for (int j = curTopN - 2; j >= i; j--) {
							topn[j + 1].level = topn[j].level;
							topn[j + 1].userName = topn[j].userName;
						}
						topn[i].level = minDist;
						topn[i].userName = userName;
						break;
					}
				}


			// �����������û���ͳ��topN���� ����С��ĳ����ֵ�ж�topK�Ƿ���Ϊ���ƻ���������
			for (int i = 0; i < curTopN; i++) {
				// ��������û���¼������һ����¼
				if (!topnResult.containsKey(topn[i].userName)) {
					ResultMatrix result = new ResultMatrix();
					result.numSimi = 0;
					result.count = 0;
					topnResult.put(topn[i].userName, result);
				}

				if (topn[i].level < BYMIN) {
					topnResult.get(topn[i].userName).numSimi += (1 - i * 0.3);
					topnResult.get(topn[i].userName).count++;
				}

			}
		}

		float tmpMax = Float.MIN_VALUE;
		String tmpUser = null;
		for (String userName : topnResult.keySet()) {
			// ����ÿ���û��ľ���ֲ�����
			double[] P = new double[BYFRONT];
			if(!userList.containsKey(userName)){
				continue;
			}
			Double[] minDistArr = distMap.get(userName);
			for (double dist : minDistArr) {
				for (int x = 0; x < BYFRONT; x++) {
					if (dist < P_BYSTEP * (x + 1)) {
						P[x]++;
					}
				}
			}
			float tmp = 0;
			for (int x = 0; x < BYFRONT; x++) {
				P[x] /= vctLen;
				tmp += P[x];
			}
			// ���ʷֲ���ֵ,�����������ִ�����ֵ�����ߵľ�ֵ
			float mean = tmp / BYFRONT;
			float simiMean = topnResult.get(userName).numSimi / topnResult.get(userName).count;
			float pMean = (tmp + simiMean) / 2;

			if(simiMean>tmpMax&&mean >= 0.20 && simiMean >= 0.7 && P[3]>=0.20&&includedUser.contains(userName)){
				tmpMax = simiMean;
				tmpUser = userName;
			}
		}
		
		if(tmpUser!=null){
			return tmpUser;
		}else{
			return "İ����";
		}
	}

	/**
	 * �ı����ʶ��
	 * 
	 * @param includedUser
	 *            ���������� 
	 * @return �û���
	 */
	public String recognizeUserBySimple(ArrayList<String> includedUser) {
		ArrayList<Stroke> loginList = simpleLoginList;
		int registNum = userList.size(); // ע������
		
		if (includedUser.size() == 0) {
			return "İ����";
		}
		
		int vctLen = loginList.size(); // ������������
		HashMap<String, ResultMatrix> topnResult = new HashMap<String, StaticRecognize.ResultMatrix>(); // �������

		// ��С����洢����
		HashMap<String, Double[]> distMap = new HashMap<String, Double[]>();
		for (String userName : userList.keySet()) {
				distMap.put(userName, new Double[vctLen]);
		}

		double minDist;
		TopN[] topn = null;
		int i, j, k;
		int curTopN = TOPN > registNum ? registNum : TOPN; // ����ӦtopN
		// ����ÿ��������������
		for (k = 0; k < vctLen; k++) {
			Stroke mStroke = loginList.get(k);

			// ��������,��������������(0-7)
			int index1 = mStroke.index1;
			int index2 = mStroke.index2;
			double[] loginVct = mStroke.fetureVct;

			// top-N��������
			topn = new TopN[curTopN];
			for (i = 0; i < curTopN; i++) {
				topn[i] = new TopN();
				topn[i].level = Double.MAX_VALUE;
			}

			// ��ÿ��ע���û���ͬ��������Ƚ�,��min-dist
			for (String userName : userList.keySet()) {
				ArrayList<Stroke> registStkList1 = userList.get(userName).get(index1 + 1);
				ArrayList<Stroke> registStkList2 = userList.get(userName).get(index2 + 1);
				minDist = Double.MAX_VALUE; // ��ʼ����ʱ��Сֵ,��ʾ�뵱ǰ�û�����С����
				// ����������������
				for (Stroke stroke1 : registStkList1) {
					double[] registVct = stroke1.fetureVct;
					double dist = calculateDistance(loginVct, registVct);
					minDist = (dist < minDist ? dist : minDist);
				}

				for (Stroke stroke2 : registStkList2) {
					double[] registVct = stroke2.fetureVct;
					double dist = calculateDistance(loginVct, registVct);
					minDist = (dist < minDist ? dist : minDist);
				}
				distMap.get(userName)[k] = minDist; // �洢�����������

				// �������ݵ�top-n
				for (i = 0; i < curTopN; i++) {
					if (minDist < topn[i].level) {
						// ������һλ
						for (j = curTopN - 2; j >= i; j--) {
							topn[j + 1].level = topn[j].level;
							topn[j + 1].userName = topn[j].userName;
						}
						topn[i].level = minDist;
						topn[i].userName = userName;
						break;
					}
				}

			}

			// �����������û���ͳ��topN���� ����С��ĳ����ֵ�ж�topK�Ƿ���Ϊ���ƻ���������
			for (i = 0; i < curTopN; i++) {
				// ��������û���¼������һ����¼
				if (!topnResult.containsKey(topn[i].userName)) {
					ResultMatrix result = new ResultMatrix();
					result.numSimi = 0;
					result.count = 0;
					topnResult.put(topn[i].userName, result);
				}

				if (topn[i].level < BYMIN) {
					topnResult.get(topn[i].userName).numSimi += (1 - i * 0.3);
					topnResult.get(topn[i].userName).count++;
				}

			}
		}

		String tmpStr = "---�ı����   p[<40]   mean    simiMean ---: \n";
		float tmpMax = Float.MIN_VALUE;
		String tmpUser = null;
	
		for (String userName : topnResult.keySet()) {
			if(!userList.containsKey(userName)){
				continue;
			}
			// ����ÿ���û��ľ���ֲ�����
			double[] P = new double[BYFRONT];
			Double[] minDistArr = distMap.get(userName);
			for (double dist : minDistArr) {
				for (int x = 0; x < BYFRONT; x++) {
					if (dist < P_BYSTEP * (x + 1)) {
						P[x]++;
					}
				}
			}
			float tmp = 0;
			for (int x = 0; x < BYFRONT; x++) {
				P[x] /= vctLen;
				tmp += P[x];
			}
			// ���ʷֲ���ֵ,�����������ִ�����ֵ�����ߵľ�ֵ
			float mean = tmp / BYFRONT;
			float simiMean = topnResult.get(userName).numSimi / topnResult.get(userName).count;
			float pMean = (tmp + simiMean) / 2;

			tmpStr += userName + "    " + df.format(P[3])+"    "+df.format(mean)+"    "+df.format(simiMean)
			+  "\n";
			if(simiMean>tmpMax&&mean >=0.46 && simiMean >= 0.75 && P[3]>=0.46&&includedUser.contains(userName)){
				tmpMax = simiMean;
				tmpUser = userName;
			}
		}

		if (tmpUser != null) {
			 tmpStr+="ʶ���û�: "+tmpUser;
			 return tmpStr;
		} else {
			 tmpStr+="ʶ���û�: "+"İ����";
			 return tmpStr;
		}
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
			userName = "";
		}

		String userName;
		double level; // ����
	}

	/**
	 * top-n result num matrix
	 * 
	 * @author JeremyChen
	 *
	 */
	class ResultMatrix {

		public ResultMatrix() {
			super();
		}

		float numSimi;
		float count;
	}
}
