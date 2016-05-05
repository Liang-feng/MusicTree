package com.example.easymusicplayer1.db;

import java.util.ArrayList;

import com.example.easymusicplayer1.utility.MyApplication;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

/**
 * 读取本地音乐的信息
 * @author feng
 *
 */
public class ReadMusicInfoFromLocalDb {
	

	Cursor cursor;

	ContentResolver contentResolver;

	Context context;

	ArrayList<String> musicTitleList;

	ArrayList<String> musicUrlList;

	ArrayList<Integer> musicDurationList;

	ArrayList<String> musicImageList;

	ArrayList<String> musicIdList;
	
	ArrayList<String> musicSingerName;
	

	public ReadMusicInfoFromLocalDb() {
		context = MyApplication.getContext();
		contentResolver = context.getContentResolver();
		// cursor用完之后需要关闭么？如果关闭的话，每次从数据库查询东西是很麻烦的!!!
		cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
	}

	// 删除多媒体数据库中音乐名称的信息即可，因为重新打开app时，调用了initMusicTitle()函数，会初始化musicTitleList，而musicTitleList会被adapter用到，显示在ListView上，
	// musicTitleList中的元素要想不显示在ListView上，根据音乐id删除数据库中音乐的信息
	public boolean deleteMusicFromMediaStore(String[] musicId) {
		// 音乐名称从哪里来就应该在哪里删除，找到数据库表源!!!
		contentResolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media._ID + " = ?",
				musicId);
		return true;
	}
	
	

	//获取歌曲id，并不是1,2，3,4之类的id
	public ArrayList<String> getMusicId() 
	{
		musicIdList = new ArrayList<String>();
		if (cursor.moveToFirst()) 
		{
			do
			{
				String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
				musicIdList.add(id);
			} while (cursor.moveToNext());
		}
		return musicIdList;
	}
	

	public ArrayList<String> getMusicTitle()
{
		String musicName = null;
		musicTitleList = new ArrayList<String>();
		if (cursor.moveToFirst()) 
		{
			do 
			{
				musicName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
				musicTitleList.add(musicName);

			} while (cursor.moveToNext());
		}

		return musicTitleList;
	}

	public ArrayList<String> getMusicUrl() {
		musicUrlList = new ArrayList<String>();
		if (cursor.moveToFirst()) {
			do {
				String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
				musicUrlList.add(url);
				Log.e("ReadMusicInformationLocalDB", url);
				///System.out.println(url);
				//Toast.makeText(MyApplication.getContext() , url, Toast.LENGTH_SHORT).show();
			} while (cursor.moveToNext());
		}
		return musicUrlList;
	}

	public ArrayList<Integer> getMusicDuration() {
		musicDurationList = new ArrayList<Integer>();
		if (cursor.moveToFirst()) {
			do {
				Integer integer = Integer
						.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
				musicDurationList.add(integer);
			} while (cursor.moveToNext());
		}
		return musicDurationList;
	}
	
	public ArrayList<String> getMusicSingerName()
	{
		musicSingerName = new ArrayList<String>();
		if(cursor.moveToFirst())
		{
			do
			{
				String SingerName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
				musicSingerName.add(SingerName);
			}while(cursor.moveToNext());
		}
		
		return musicSingerName;
		
	}
	
	/*
	 * public ArrayList<String> getMusicImage() { musicImageList = new
	 * ArrayList<String>(); }
	 */
}

