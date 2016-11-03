package com.cnsoft.entities;

/**
 * @author JeremyChen
 * 记录点的信息
 */
public class Point {
	public float x;	//横坐标
	public float y;	//纵坐标
	
	public float slopeByLine;	//该点斜率,即样条曲线导数

	public float Vx; 	// 速度正交分解
	public float Vy;
	public float aX;	//x加速度
	public float aY;	//x加速度
	public long curTime;	//时间戳
}
