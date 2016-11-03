package com.cnsoft.constants;

import java.util.Random;

public class InputSamples {
	/*
	 * 注册20个字,登录8个字
	 */
	public static final String REGIST_STR = "冷军语匠别网渺望延道阻野状快定店然场花跹";
	public static final String LOGIN_STATIC_STR ="校手床窗外活说知运货雷走幼优队皮路托布驼"+
"站笑肖申像克孤白却式拾独识李姓老是我飞莞"+
"胖点慢风起的话如果不句容要死灵芝华仕亡易"+
"梦补见考过或许好愿你总村头员会脑雨海灭世"+
"厄得久光问看比赛开始年候上学院长沙拉钩夫"+
"社那再就变成谈哭血泣色彩妆前后果条船务正"+
"起泣鬼宫模队华伍特颈为子疯啥的么那天买神"+
"卖里衣菜色面有服项法师台兄圈州灯泡水地方"+
"硕忙到哥点左娥皇女长时吧间英美家国动音讯"+
"右去想会再系做讲机电猫朵子龙座没饭统后啊"+
"们课妹多崽四百事万虚会务吃着来点业堂下落"+
"握爪科齿牙霸占在嗯那些小好的用孩学纸尾片"+
"厄耶和给伊支行华挺剧有空调噢瓜戏多尔透为"+
"遥油今级控别生归蛮船到桥头自杂志伟岸本齐"+
"史然直气弄干利泡叶问号集它尸行题茶饿也净"+
"燕又麦当能前压过量几提天根来讲固供血力劳"+
"设个换和测方感轱觉丽案王蛤君杰出发置烧月"+
"烤午骆戏工中作份安阳程浩南历逸肉麻瓜鞋以"+
"皮德滚间鲁车班身客本户粗克士程总享共伤受"+
"抢如鸡短带坏角裤学公寓提交让骤衣雨步生腿"+
"逸伟玩差甘距岸夫蔗楼果糖汁扔在嗯对刃钢化"+
"解甲鱼比决情总魏味共动趣笛卡明显文尔绝鸣次写计厅区列冈仁勺允跃笑和距处须笔钢盂罗"
			+ "奉症收牡朴玩礼灯杰红孔饮廷卫防邦江壮怀宇庄过地城准开送扛因形席狂"
			+ "冬艾宾性行将尤识活冠匣郊院却建剑矣旬你周";
	public static final int REGIST_LEN = REGIST_STR.length(); // 注册字样长度
	public static final int LOGIN_DYNAMIC_LEN = 4; // 登录动态字样长度
	public static final int LOGIN_STATIC_LEN = 4; // 登录静态字样长度
	public static final int lOGIN_LEN = LOGIN_DYNAMIC_LEN + LOGIN_STATIC_LEN; // 注册字样长度

	/**
	 * 取不重复的随机数
	 * 
	 * @param len
	 *            连续取len次随机数
	 * @param range
	 *            选取随机数范围[0,y)
	 * @return
	 */
	public static int[] getRandomInt(int len, int range) {
		int i = 0;
		Random random = new Random();
		boolean[] bool = new boolean[range];
		int[] rand = new int[len];
		int randInt = 0;
		for (int j = 0; j < len; j++) {
			/** 得到len个不同的随机数 */
			do {
				randInt = random.nextInt(range);
			} while (bool[randInt]);
			bool[randInt] = true;
			rand[i++] = randInt;
		}
		return rand;
	}
}
