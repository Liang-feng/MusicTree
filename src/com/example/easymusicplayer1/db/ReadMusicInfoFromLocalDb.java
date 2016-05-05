package com.example.easymusicplayer1.db;

import java.util.ArrayList;

import com.example.easymusicplayer1.utility.MyApplication;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

/**
 * ��ȡ�������ֵ���Ϣ
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
		// cursor����֮����Ҫ�ر�ô������رյĻ���ÿ�δ����ݿ��ѯ�����Ǻ��鷳��!!!
		cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
	}

	// ɾ����ý�����ݿ����������Ƶ���Ϣ���ɣ���Ϊ���´�appʱ��������initMusicTitle()���������ʼ��musicTitleList����musicTitleList�ᱻadapter�õ�����ʾ��ListView�ϣ�
	// musicTitleList�е�Ԫ��Ҫ�벻��ʾ��ListView�ϣ���������idɾ�����ݿ������ֵ���Ϣ
	public boolean deleteMusicFromMediaStore(String[] musicId) {
		// �������ƴ���������Ӧ��������ɾ�����ҵ����ݿ��Դ!!!
		contentResolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media._ID + " = ?",
				musicId);
		return true;
	}
	
	

	//��ȡ����id��������1,2��3,4֮���id
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
