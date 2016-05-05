package com.example.easymusicplayer1.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.widget.Toast;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 创建数据库，用来存储根据歌曲名和歌手来从网络获取的音乐的信息。
 * @author feng
 *
 */
public class MusicSQLiteOpenHelper extends SQLiteOpenHelper {

	Context myContext;       //   可能用于Toast的信息提示输出!!!
	
	public static final String CREATE_MUSIC = "create table Music ("
			+ "id integer primary key autoincrement , "
			+ "albumid integer , "
			+ "downUrl text , "
			+ "seconds integer , "
			+ "singerid integer , "
			+ "singername text , "
		    + "songid integer , "
			+ "songname text , "
		    + "url text "
		    + "lyric text) ";
	
	public MusicSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		myContext = context; 
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL(CREATE_MUSIC);      //创建音乐表
		//Toast.makeText(myContext, "成功创建音乐数据库", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  //数据库更新时候会调用!!!
		// TODO Auto-generated method stub
        switch(oldVersion)
        {
        case 1:
        	db.execSQL("alter table Music add column lyric text");       //升级数据库，添加一个字段
        case 2:       
        }
	}

}
