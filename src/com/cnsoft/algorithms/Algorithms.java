package com.cnsoft.algorithms;

import java.util.ArrayList;

import com.cnsoft.entities.Point;
import com.cnsoft.utils.Logger;

import android.content.Context;

/*
 * 	�㷨������
 */
public class Algorithms {
	private static Algorithms algorithm = null; 
	
	public Preprocess mPreprocess; // Ԥ������
	public SplineInterpolation spline; // ����ƽ����
	public StaticRecognize featureVct; // ��̬�����б���
	public DynamicRecognize vCmp; // ��̬�����б���
	
	//���ɼ��ıʻ���Ϣ
	public ArrayList<ArrayList<Point>> mPointList; 
	private static int index = 0;	//�±�
	
	// Ԥ�����ֵ
	public final int BYLEN = 60; // ��־����ٽ�ֵ
	public final float BYK = (float) (15*Math.PI/180); // ���б���ٽ�ֵ
	public final float RECORD_DIS = 2;//���ڸ���ֵ��Ϊ��Ч������
	
	public int[] randomDynamic; //��̬����λ������
	
	private Algorithms() {
		mPreprocess = new Preprocess();
		spline = new SplineInterpolation();
		featureVct = new StaticRecognize();
		vCmp = new DynamicRecognize();
		mPointList = new ArrayList<ArrayList<Point>>();
	}

	public static Algorithms getInstance() {
		if (algorithm == null) {
			algorithm = new Algorithms();
		}
		return algorithm;
	}
	
	/*
	 * �ж��Ƿ�Ϊ������
	 */
	public boolean isEmpty(){
		return mPointList.size()==0;
	}
	
	/**
	 * Ԥ����+�ʻ����
	 * 
	 * @param isRegist
	 *            �Ƿ�Ϊע��
	 * 
	 */
	public boolean preprocess(boolean isRegist) {
		// ��ȡ��̬����
		if(isRegist){
			for (ArrayList<Point> mPoint : mPointList) {
				mPoint = mPreprocess.smoothing(mPoint); // Ԥ�������
				spline.cubic_getval(mPoint);
				featureVct.extractStroke(isRegist, mPoint,true,BYLEN, BYK); 
			}	
			//��̬������ȡ
			for(ArrayList<Point> mPoint : mPointList){
				vCmp.charVList.addAll(mPoint);
			}
			vCmp.charVList = mPreprocess.preprocess(vCmp.charVList);
			vCmp.addChars2Regist();
		}else{
			for (ArrayList<Point> mPoint : mPointList) {
				mPoint = mPreprocess.smoothing(mPoint); 
				spline.cubic_getval(mPoint);
				boolean isSimple = false;
				for(int i : randomDynamic){
					if(index==i){
						isSimple = true;
						break;
					}
				}
				featureVct.extractStroke(isRegist, mPoint,isSimple,BYLEN, BYK); 
			}
			for(ArrayList<Point> mPoint : mPointList){
				vCmp.charVList.addAll(mPoint);
			}
			vCmp.charVList = mPreprocess.preprocess(vCmp.charVList);
			vCmp.addChars2Login();
		}

		index++;
		mPointList.clear(); // ��ջ���ʻ�
		return true;
	}

	/**
	 * ע���û� ǰ��:�û�����Ч
	 * @param userName �û���
	 * @return ��Ч��
	 */
	public boolean registUser(String userName) {
		preprocess(true);
		boolean isOk =  featureVct.saveUserInfo(userName) && vCmp.addUser(userName); // �����û���Ϣ��ӵ�����Ϣ��
		return isOk;
	}

	/**
	 * �����ǰ�������ע����Ϣ
	 */
	public void clearRegistData() {
		vCmp.clearRegistInput();
		featureVct.clearRegistMap();
		mPointList.clear();
		index = 0;
	}

	/**
	 * �����ǰ������ĵ�¼��Ϣ
	 */
	public void clearLoginData() {
		vCmp.clearLoginInput();
		featureVct.clearLoginMap();
		mPointList.clear();
		index = 0;
	}

	/**
	 * �����������
	 */
	public void clearAllData() {
		vCmp.clearAll();
		featureVct.cleanAll();
		mPointList.clear();
	}

	/**
	 * ��ʶ��˳��֪ͨ��̬����ʶ����
	 * @param randomDynamic	��̬��λ������
	 * @param randomArr		��̬��������ע���ֵ�����
	 */
	public void publishOrder(int[] randomDynamic,int[] randomArr) {
		vCmp.randomInt = randomArr;
		vCmp.randomDynamic = randomDynamic;
	}
	
	/**
	 * �ۺ��б���: ͨ����̬����(Vx,Vy)��(x,y)ʱ������ų�����ͨ���ı��޹ؾ�̬��������ʶ��
	 * @return	ʶ������û���
	 */
	public String login(){
		preprocess(false);
		ArrayList<String> includeUser = new ArrayList<String>();
		//��̬����ʶ��
		String result = vCmp.recognizebyDynamic(includeUser);
		if(result.equals("����ע���û�")){
			return "����ע���û�";
		}
		
		//��̬����ʶ��
		String multiStr = featureVct.recognizeUserByMulti(includeUser);
		return "ʶ���û�: "+multiStr;
	}
}
