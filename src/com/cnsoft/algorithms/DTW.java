package com.cnsoft.algorithms;

import java.util.ArrayList;

/**
 * 动态时间规整算法(DTW)
 * @author JeremyChen
 *
 */
public class DTW {
	public static float dtw(ArrayList<Float> p1,ArrayList<Float> p2){
		int len1 = p1.size();
		int len2 = p2.size();
		float[][] dis = new float[len1][len2];
		int i,j;
		for(i=0;i<len1;i++){
			for(j=0;j<len2;j++){
				dis[i][j] = (p1.get(i) - p2.get(j))*(p1.get(i) - p2.get(j));
			}
		}
		for(i=1;i<len1;i++){
			dis[i][1] = dis[i][1]+dis[i-1][1];
		}
		for(j=1;j<len2;j++){
			dis[1][j] = dis[1][j]+dis[1][j-1];
		}
		for(i=1;i<len1;i++){
			for(j=1;j<len2;j++){
				float dij = dis[i][j]+Math.min(dis[i-1][j],dis[i][j-1]);
				dis[i][j] =Math.min(2*dis[i][j]+dis[i-1][j-1], dij);
			}
		}
		return dis[len1-1][len2-1];
	}
}
