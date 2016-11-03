package com.cnsoft.algorithms;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import com.cnsoft.entities.Point;
import com.cnsoft.entities.Stroke;
import com.cnsoft.utils.Logger;


/**
 * 静态特征判别类
 * 
 * @author JeremyChen
 *
 */
public class StaticRecognize {
	
	private final int DIMEN = 8; // 特征向量维度
	private final int BYFRONT = 10;	//统计概率分布常量
	private final double BYMIN = 50; // 确认为该用户的最大临界值
	private final float P_BYSTEP = 10f; // 阶梯概率
	private DecimalFormat df = new DecimalFormat(); // 格式控制
	// 8个笔画大类,登入和注册
	public HashMap<Integer, ArrayList<Stroke>> strokesRegistMap;
	public ArrayList<Stroke> simpleLoginList;	 // 文本相关特征列表
	public ArrayList<Stroke> multiLoginList; 	 // 文本无相特征列表
	// 多用户数据
	public HashMap<String, HashMap<Integer, ArrayList<Stroke>>> userList;
	// 距离判断记录表
	public HashMap<String, Integer[]> resultMatrix;
	// 辅助数组,选出前TOPN项
	private final int TOPN = 3;
	public double[] topNDisMatrix;
	public String[] topNNameMatrix;

	public StaticRecognize() {
		super();
		df.setMaximumFractionDigits(2); // 设置保留小数点位数
		newRegistStrokeMap();
		newLoginStrokeMap();
		userList = new HashMap<String, HashMap<Integer, ArrayList<Stroke>>>(); 
	}

	/**
	 * 多用户多例,注册信息 初始化样本类别映射
	 */
	public void newRegistStrokeMap() {
		strokesRegistMap = new HashMap<Integer, ArrayList<Stroke>>();
		for (int i = 1; i <= DIMEN; i++) {
			strokesRegistMap.put(i, new ArrayList<Stroke>());
		}
	}

	/**
	 * 单例,多用户重用一个登入信息,需及时重置 初始化待验证类别映射
	 */
	public void newLoginStrokeMap() {
		simpleLoginList = new ArrayList<Stroke>();
		multiLoginList = new ArrayList<Stroke>();
	}

	/**
	 * 清空重置登入类别映射
	 */
	public void clearLoginMap() {
		simpleLoginList.clear();
		multiLoginList.clear();
	}

	/**
	 * 清空重置登入类别映射
	 */
	public void clearRegistMap() {
		for (int i = 1; i <= DIMEN; i++) {
			strokesRegistMap.get(i).clear();
		}
	}

	/**
	 * 保存当前用户资料,完成注册
	 * @return
	 */
	public boolean saveUserInfo(String registUserName) {
			userList.put(registUserName, strokesRegistMap);
			newRegistStrokeMap(); // 新建下一个用户实例
			return true;
	}

	/**
	 * 检查用户名有效性
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
	 * 笔画拆分
	 * 
	 * @param isRegist
	 *            判断是否为注册
	 * @param mPoint
	 *            传入的点序
	 * @param isSimpleLogin
	 *            是否为文本相关登录
	 * @param byLen
	 *            拆分的长度阈值(对两点的距离超过该值的点考虑拆分)
	 * @param byK
	 *            拆分的斜率阈值(对两点斜率与样条曲线差超过该值的点考虑拆分) 拆分结果的特征向量加入8个笔画大类中
	 */
	public void extractStroke(boolean isRegist, ArrayList<Point> mPoint, boolean isSimpleLogin, float byLen,
			float byK) {

		// 有效性检查
		if (mPoint == null || mPoint.size() < 3) {
			return;
		}

		int n = mPoint.size(); // 传入点序个数
		Point frontP = mPoint.get(0); // 记录笔段起点
		// 查找拆分点
		for (int i = 1; i < n - 1; i++) {
			Point lastP = mPoint.get(i);
			// 如果该点和上一拆分点距离小于某个阈值,不做拆分
			if (Math.sqrt((lastP.x - frontP.x) * (lastP.x - frontP.x)
					+ (lastP.y - frontP.y) * (lastP.y - frontP.y)) < byLen) {
				continue;
			}
			// 斜率有效性判断
			float subX = (mPoint.get(i + 1).x - lastP.x);
			if (Math.abs(subX) < 2) {
				continue;
			}
			// 判断该点是否满足斜率拆分条件
			float subK = (float) Math
					.abs(Math.atan(lastP.slopeByLine) - Math.atan((mPoint.get(i + 1).y - lastP.y) / subX));

			if (subK > byK) {
				Stroke stroke = new Stroke(); // 新建笔段基本单元
				stroke.fromPnt = frontP;
				stroke.toPnt = lastP;
				getFeatureVct(stroke, isRegist, isSimpleLogin); // 获取特征向量
				frontP = lastP;
			}
		}
		// 最后一点的处理
		Stroke stroke = new Stroke(); // 新建笔段基本单元
		stroke.fromPnt = frontP;
		stroke.toPnt = mPoint.get(n - 1);
		getFeatureVct(stroke, isRegist, isSimpleLogin); // 获取特征向量
	}

	/**
	 * 根据划分结果提取特征向量
	 * 
	 * @param mStroke
	 *            划分结果
	 * @param isRegist
	 * 			  是否为注册
	 * @param isSimpleLogin
	 *            是否为文本相关登录
	 * @return 将特征向量更新到原划分结果中,并保存用户信息
	 */
	public Stroke getFeatureVct(Stroke mStroke, boolean isRegist, boolean isSimpleLogin) {
		// 根据登入或者注册选择

		Point fromPoint = mStroke.fromPnt; // 起始点
		Point toPoint = mStroke.toPnt; // 终点

		float dx = Math.abs(toPoint.x - fromPoint.x);
		float dy = Math.abs(toPoint.y - fromPoint.y);
		float dis = (float) Math.sqrt(dx * dx + dy * dy);
		float s = (float) Math.sqrt(dx * dx + dy * dy);
		float a1 = Math.abs(dx - dy) / s;
		float a2 = (float) (Math.sqrt(2) * Math.min(dx, dy) / s);
		int a1Index = 0, a2Index = 0; // a1,a2索引
		float subX = fromPoint.x - toPoint.x;
		float subY = fromPoint.y - toPoint.y;
		float absX_Y = Math.abs(toPoint.y - fromPoint.y) - Math.abs(toPoint.x - fromPoint.x);
		// 归类到指定8个笔画类别，如横，竖..
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
		mStroke.fetureVct[a1Index] = s * a1 / (a1 + a2); // 特征向量,保存于一个stroke中
		mStroke.fetureVct[a2Index] = s * a2 / (a1 + a2);

		// 判断是否为注册
		if (isRegist) {
			strokesRegistMap.get(a1Index + 1).add(mStroke);
			strokesRegistMap.get(a2Index + 1).add(mStroke);
		} else {
			// 保存下标
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
	 * 计算欧式距离
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
	 * 文本无关识别
	 * 
	 * @param includedUser	保留的用户
	 *             
	 * @return 用户名
	 */
	public String recognizeUserByMulti(ArrayList<String> includedUser) {
		ArrayList<Stroke> loginList = multiLoginList;
		int vctLen = loginList.size(); // 特征向量个数
		
		if (includedUser.size() == 0) {
			return "陌生人";
		}
		
		int curTopN = TOPN > (includedUser.size()) ?(includedUser.size()) : TOPN; // 自适应topN
		HashMap<String, ResultMatrix> topnResult = new HashMap<String, StaticRecognize.ResultMatrix>(); // 结果矩阵
		// 最小距离存储矩阵
		HashMap<String, Double[]> distMap = new HashMap<String, Double[]>();
		for (String userName : userList.keySet()) {
				distMap.put(userName, new Double[vctLen]);
		}

		double minDist;
		TopN[] topn = null;

		// 遍历每个登入特征向量
		for (int k = 0; k < vctLen; k++) {
			Stroke mStroke = loginList.get(k);

			// 特征向量,及其所属特征组(0-7)
			int index1 = mStroke.index1;
			int index2 = mStroke.index2;
			double[] loginVct = mStroke.fetureVct;

			// top-N辅助数组
			topn = new TopN[curTopN];
			for (int i = 0; i < curTopN; i++) {
				topn[i] = new TopN();
				topn[i].level = Double.MAX_VALUE;
			}

			// 与注册用户相同的特征组比较,找min-dist
			String userName = includedUser.get(0);
				// 如果为排除名单里,则跳过
				ArrayList<Stroke> registStkList1 = userList.get(userName).get(index1 + 1);
				ArrayList<Stroke> registStkList2 = userList.get(userName).get(index2 + 1);
				minDist = Double.MAX_VALUE; // 初始化临时最小值,表示与当前用户的最小距离
				// 遍历两个特征集合
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
				distMap.get(userName)[k] = minDist; // 存储到距离矩阵中

				// 更新数据到top-n
				for (int i = 0; i < curTopN; i++) {
					if (minDist < topn[i].level) {
						// 往后移一位
						for (int j = curTopN - 2; j >= i; j--) {
							topn[j + 1].level = topn[j].level;
							topn[j + 1].userName = topn[j].userName;
						}
						topn[i].level = minDist;
						topn[i].userName = userName;
						break;
					}
				}


			// 遍历完所有用户后，统计topN数据 根据小于某个阈值判断topK是否标记为相似或不相似向量
			for (int i = 0; i < curTopN; i++) {
				// 若不存该用户记录则新增一条记录
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
			// 计算每个用户的距离分布概率
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
			// 概率分布均值,相似向量出现次数均值，二者的均值
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
			return "陌生人";
		}
	}

	/**
	 * 文本相关识别
	 * 
	 * @param includedUser
	 *            保留的名单 
	 * @return 用户名
	 */
	public String recognizeUserBySimple(ArrayList<String> includedUser) {
		ArrayList<Stroke> loginList = simpleLoginList;
		int registNum = userList.size(); // 注册人数
		
		if (includedUser.size() == 0) {
			return "陌生人";
		}
		
		int vctLen = loginList.size(); // 特征向量个数
		HashMap<String, ResultMatrix> topnResult = new HashMap<String, StaticRecognize.ResultMatrix>(); // 结果矩阵

		// 最小距离存储矩阵
		HashMap<String, Double[]> distMap = new HashMap<String, Double[]>();
		for (String userName : userList.keySet()) {
				distMap.put(userName, new Double[vctLen]);
		}

		double minDist;
		TopN[] topn = null;
		int i, j, k;
		int curTopN = TOPN > registNum ? registNum : TOPN; // 自适应topN
		// 遍历每个登入特征向量
		for (k = 0; k < vctLen; k++) {
			Stroke mStroke = loginList.get(k);

			// 特征向量,及其所属特征组(0-7)
			int index1 = mStroke.index1;
			int index2 = mStroke.index2;
			double[] loginVct = mStroke.fetureVct;

			// top-N辅助数组
			topn = new TopN[curTopN];
			for (i = 0; i < curTopN; i++) {
				topn[i] = new TopN();
				topn[i].level = Double.MAX_VALUE;
			}

			// 与每个注册用户相同的特征组比较,找min-dist
			for (String userName : userList.keySet()) {
				ArrayList<Stroke> registStkList1 = userList.get(userName).get(index1 + 1);
				ArrayList<Stroke> registStkList2 = userList.get(userName).get(index2 + 1);
				minDist = Double.MAX_VALUE; // 初始化临时最小值,表示与当前用户的最小距离
				// 遍历两个特征集合
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
				distMap.get(userName)[k] = minDist; // 存储到距离矩阵中

				// 更新数据到top-n
				for (i = 0; i < curTopN; i++) {
					if (minDist < topn[i].level) {
						// 往后移一位
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

			// 遍历完所有用户后，统计topN数据 根据小于某个阈值判断topK是否标记为相似或不相似向量
			for (i = 0; i < curTopN; i++) {
				// 若不存该用户记录则新增一条记录
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

		String tmpStr = "---文本相关   p[<40]   mean    simiMean ---: \n";
		float tmpMax = Float.MIN_VALUE;
		String tmpUser = null;
	
		for (String userName : topnResult.keySet()) {
			if(!userList.containsKey(userName)){
				continue;
			}
			// 计算每个用户的距离分布概率
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
			// 概率分布均值,相似向量出现次数均值，二者的均值
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
			 tmpStr+="识别用户: "+tmpUser;
			 return tmpStr;
		} else {
			 tmpStr+="识别用户: "+"陌生人";
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
		double level; // 距离
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
