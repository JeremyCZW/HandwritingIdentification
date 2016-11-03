package com.cnsoft.algorithms;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import com.cnsoft.constants.InputSamples;
import com.cnsoft.entities.Point;
import com.cnsoft.utils.Logger;

public class DynamicRecognize {
	// 判断为陌生人的阈值
	private final float MINV = 500f;
	private final float MINP = 100000f;

	private final int TOPN = 3;
	private DecimalFormat df = new DecimalFormat(); // 格式控制
	private int curIndex = 0; // 记录当前字符序数
	public int[] randomInt; // 动态区字样对于注册字的索引
	public int[] randomDynamic;// 动态的位置索引
	public ArrayList<Point> charVList; // 一个字的速率的集合(缓存)
	public HashMap<Integer, ArrayList<Point>> registCharsVList;// 一个人所有字速率的集合
	public HashMap<Integer, ArrayList<Point>> loginCharsVList;// 一个人所有字速率的集合
	public HashMap<String, HashMap<Integer, ArrayList<Point>>> userList; // 所有注册者速率集合(与数据库对接)

	public DynamicRecognize() {
		super();
		df.setMaximumFractionDigits(1); // 设置保留小数点位数
		charVList = new ArrayList<Point>();
		registCharsVList = new HashMap<Integer, ArrayList<Point>>();
		loginCharsVList = new HashMap<Integer, ArrayList<Point>>();
		userList = new HashMap<String, HashMap<Integer, ArrayList<Point>>>();
	}

	/**
	 * 清除登录信息
	 */
	public void clearLoginInput() {
		curIndex = 0;
		charVList.clear();
		loginCharsVList.clear();
	}

	/**
	 * 清除注册信息
	 */
	public void clearRegistInput() {
		curIndex = 0;
		charVList.clear();
		registCharsVList.clear();
	}

	/**
	 * 清除所有信息
	 */
	public void clearAll() {
		curIndex = 0;
		charVList.clear();
		loginCharsVList.clear();
		registCharsVList.clear();
		userList.clear();
	}

	/**
	 * 将一个字加入注册字集合中(条件:写下一个字的时候)
	 */
	public void addChars2Regist() {
		if (charVList != null && charVList.size() > 0) { // 这个字不空,且有值得时候，将其放入字集合中
			registCharsVList.put(++curIndex, charVList);
			charVList = new ArrayList<Point>(); // 为书写下一个字做好准备
		}
	}

	/**
	 * 将一个字加入登入字典集合中(条件:写下一个字的时候)
	 */
	public void addChars2Login() {
		if (charVList != null && charVList.size() > 0) {
			loginCharsVList.put(++curIndex, charVList);
			charVList = new ArrayList<Point>();
		}
	}

	/**
	 * 将当前用户添加到用户集合中
	 * 
	 * @param userName
	 *            用户名
	 * @return 是否操作成功
	 */
	public boolean addUser(String userName) {
		// 特征有效性检查
		if (registCharsVList != null && registCharsVList.size() > 0) {
			userList.put(userName, registCharsVList);
			// 初始化临时变量,为下一用户注册提供准备
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
	 * 动态特征和时序坐标特征综合判别
	 * 
	 * @return 
	 */
	public String recognizebyDynamic(ArrayList<String> includeUser) {
		String result = "辅助判断信息(距离越近越相似) :\n";
		float tmpVx, tmpVy, tmpX, tmpY, curSumByV = 0, curSumByPos = 0;
		// 信息有效性检查
		int userNum = userList.size(); // 用户个数
		if (userNum == 0) {
			return "暂无注册用户";
		}

		// top-N辅助排序数组
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

				// 解析特征信息
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

				// 计算DTW距离
				tmpX = DTW.dtw(registX, loginX);
				tmpY = DTW.dtw(registY, loginY);
				tmpVx = DTW.dtw(registVx, loginVx);
				tmpVy = DTW.dtw(registVy, loginVy);
				curSumByPos += (tmpX * tmpX + tmpY * tmpY);
				curSumByV += (tmpVx * tmpVx + tmpVy * tmpVy);
			}

			curSumByPos /= InputSamples.LOGIN_DYNAMIC_LEN;
			curSumByV /= InputSamples.LOGIN_DYNAMIC_LEN;
			result+="用户名: "+userName+" 距离: "+df.format(curSumByV)+"\n";
			// 更新数据到top-n
			for (int i = 0; i < curTopN; i++) {
				// 坐标特征相似度排序
				if (curSumByPos < topn[i].levelByPos) {
					// 往后移一位
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
				// 动态特征相似度排序
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

		// 判断距离最近的用户是否小于给定阈值
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
