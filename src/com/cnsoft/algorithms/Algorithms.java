package com.cnsoft.algorithms;

import java.util.ArrayList;

import com.cnsoft.entities.Point;
import com.cnsoft.utils.Logger;

import android.content.Context;

/*
 * 	算法控制类
 */
public class Algorithms {
	private static Algorithms algorithm = null; 
	
	public Preprocess mPreprocess; // 预处理类
	public SplineInterpolation spline; // 样条平滑类
	public StaticRecognize featureVct; // 静态特征判别类
	public DynamicRecognize vCmp; // 动态特征判别类
	
	//所采集的笔划信息
	public ArrayList<ArrayList<Point>> mPointList; 
	private static int index = 0;	//下标
	
	// 预设的阈值
	public final int BYLEN = 60; // 拆分距离临界值
	public final float BYK = (float) (15*Math.PI/180); // 拆分斜率临界值
	public final float RECORD_DIS = 2;//大于该阈值设为有效采样点
	
	public int[] randomDynamic; //动态特征位置索引
	
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
	 * 判断是否为空输入
	 */
	public boolean isEmpty(){
		return mPointList.size()==0;
	}
	
	/**
	 * 预处理+笔画拆分
	 * 
	 * @param isRegist
	 *            是否为注册
	 * 
	 */
	public boolean preprocess(boolean isRegist) {
		// 提取静态特征
		if(isRegist){
			for (ArrayList<Point> mPoint : mPointList) {
				mPoint = mPreprocess.smoothing(mPoint); // 预处理操作
				spline.cubic_getval(mPoint);
				featureVct.extractStroke(isRegist, mPoint,true,BYLEN, BYK); 
			}	
			//动态特征提取
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
		mPointList.clear(); // 清空缓存笔划
		return true;
	}

	/**
	 * 注册用户 前提:用户名有效
	 * @param userName 用户名
	 * @return 有效性
	 */
	public boolean registUser(String userName) {
		preprocess(true);
		boolean isOk =  featureVct.saveUserInfo(userName) && vCmp.addUser(userName); // 将该用户信息添加到总信息中
		return isOk;
	}

	/**
	 * 清除当前已输入的注册信息
	 */
	public void clearRegistData() {
		vCmp.clearRegistInput();
		featureVct.clearRegistMap();
		mPointList.clear();
		index = 0;
	}

	/**
	 * 清除当前已输入的登录信息
	 */
	public void clearLoginData() {
		vCmp.clearLoginInput();
		featureVct.clearLoginMap();
		mPointList.clear();
		index = 0;
	}

	/**
	 * 清空所有数据
	 */
	public void clearAllData() {
		vCmp.clearAll();
		featureVct.cleanAll();
		mPointList.clear();
	}

	/**
	 * 将识别顺序通知动态特征识别类
	 * @param randomDynamic	动态字位置索引
	 * @param randomArr		动态字样对于注册字的索引
	 */
	public void publishOrder(int[] randomDynamic,int[] randomArr) {
		vCmp.randomInt = randomArr;
		vCmp.randomDynamic = randomDynamic;
	}
	
	/**
	 * 综合判别函数: 通过动态特征(Vx,Vy)、(x,y)时序进行排除，再通过文本无关静态特征进行识别
	 * @return	识别出的用户名
	 */
	public String login(){
		preprocess(false);
		ArrayList<String> includeUser = new ArrayList<String>();
		//动态特征识别法
		String result = vCmp.recognizebyDynamic(includeUser);
		if(result.equals("暂无注册用户")){
			return "暂无注册用户";
		}
		
		//静态特征识别法
		String multiStr = featureVct.recognizeUserByMulti(includeUser);
		return "识别用户: "+multiStr;
	}
}
