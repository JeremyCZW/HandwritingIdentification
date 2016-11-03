package com.cnsoft.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper {
	// 数据库名，表名
	public final static String DATABASENAME = "Handwriting.db3";
	public final static String t_static = "handwriting_static";
	public final static String t_dynamic = "handwriting_dynamic";
	// 建表
	final String CREATE_STATICTABLE_SQL = "create table handwriting_static "
			+ "(username varchar(10) primary key, onekind text, twokind text, threekind text, fourkind text, fivekind text, sixkind text, sevenkind text, eightkind text)";
	final String CREATE_DYNAMICTABLE_SQL = "create table handwriting_dynamic "
			+ "(username varchar(10) primary key,dynamicfeature text)";

	public MyDatabaseHelper(Context context) {
		super(context, DATABASENAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// 创建静态，动态数据库
		db.execSQL(CREATE_STATICTABLE_SQL);
		db.execSQL(CREATE_DYNAMICTABLE_SQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
