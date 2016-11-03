package com.cnsoft.constants;

import java.util.Random;

public class InputSamples {
	/*
	 * ע��20����,��¼8����
	 */
	public static final String REGIST_STR = "����ｳ���������ӵ���Ұ״�춨��Ȼ������";
	public static final String LOGIN_STATIC_STR ="У�ִ������˵֪�˻��������Ŷ�Ƥ·�в���"+
"վЦФ����˹°�ȴʽʰ��ʶ���������ҷ�ݸ"+
"�ֵ�������Ļ����������Ҫ����֥��������"+
"�β������������Ը���ܴ�ͷԱ�����꺣����"+
"��þù��ʿ�������ʼ�����ѧԺ��ɳ������"+
"�����پͱ��̸��Ѫ��ɫ��ױǰ�����������"+
"������ģ�ӻ����ؾ�Ϊ�ӷ�ɶ��ô��������"+
"�����²�ɫ���з��ʦ̨��Ȧ�ݵ���ˮ�ط�"+
"˶æ���������Ů��ʱ�ɼ�Ӣ���ҹ�����Ѷ"+
"��ȥ�����ϵ��������è��������û��ͳ��"+
"�ǿ��ö����İ�����������������ҵ������"+
"��צ�Ƴ�����ռ������ЩС�õ��ú�ѧֽβƬ"+
"��Ү�͸���֧�л�ͦ���пյ��޹�Ϸ���͸Ϊ"+
"ң�ͽ񼶿ر�������������ͷ����־ΰ������"+
"ʷȻֱ��Ū������Ҷ�ʺż���ʬ������Ҳ��"+
"��������ǰѹ����������������̹�Ѫ����"+
"������Ͳⷽ���������������ܳ���������"+
"������Ϸ�������ݰ����̺������������Ь��"+
"Ƥ�¹���³������ͱ����ֿ�ʿ����������"+
"���缦�̴����ǿ�ѧ��Ԣ�ύ�������경����"+
"��ΰ���ʾశ����¥����֭�����Ŷ��иֻ�"+
"�����Ⱦ�����κζ����Ȥ�ѿ������Ķ�������д�������и�������ԾЦ�;ദ��ʸ�����"
			+ "��֢��ĵ������ƽܺ����͢�����׳����ׯ���س�׼���Ϳ�����ϯ��"
			+ "���������н���ʶ���ϻ��Ժȴ������Ѯ����";
	public static final int REGIST_LEN = REGIST_STR.length(); // ע����������
	public static final int LOGIN_DYNAMIC_LEN = 4; // ��¼��̬��������
	public static final int LOGIN_STATIC_LEN = 4; // ��¼��̬��������
	public static final int lOGIN_LEN = LOGIN_DYNAMIC_LEN + LOGIN_STATIC_LEN; // ע����������

	/**
	 * ȡ���ظ��������
	 * 
	 * @param len
	 *            ����ȡlen�������
	 * @param range
	 *            ѡȡ�������Χ[0,y)
	 * @return
	 */
	public static int[] getRandomInt(int len, int range) {
		int i = 0;
		Random random = new Random();
		boolean[] bool = new boolean[range];
		int[] rand = new int[len];
		int randInt = 0;
		for (int j = 0; j < len; j++) {
			/** �õ�len����ͬ������� */
			do {
				randInt = random.nextInt(range);
			} while (bool[randInt]);
			bool[randInt] = true;
			rand[i++] = randInt;
		}
		return rand;
	}
}
