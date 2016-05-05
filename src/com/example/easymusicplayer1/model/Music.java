package com.example.easymusicplayer1.model;

import java.util.ArrayList;

import com.example.easymusicplayer1.utility.MyApplication;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

public class Music {
	
	

	Cursor cursor;

	ContentResolver contentResolver;

	Context context;
/*
	ArrayList<String> musicTitleList;

	ArrayList<String> musicUrlList;

	ArrayList<Integer> musicDurationList;

	ArrayList<String> musicImageList;

	ArrayList<String> musicIdList;
	
	ArrayList<String> musicSingerName;*/
	
	private String title;
	
	private String url;
	
	private int duration;
	
	private String image;
	
	private int id;
	
	private String singerName;
	

	public Music() {
		context = MyApplication.getContext();
		contentResolver = context.getContentResolver();
		// cursor����֮����Ҫ�ر�ô������رյĻ���ÿ�δ����ݿ��ѯ�����Ǻ��鷳��!!!
	/*	cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);*/
	}

	// ɾ����ý�����ݿ����������Ƶ���Ϣ���ɣ���Ϊ���´�appʱ��������initMusicTitle()���������ʼ��musicTitleList����musicTitleList�ᱻadapter�õ�����ʾ��ListView�ϣ�
	// musicTitleList�е�Ԫ��Ҫ�벻��ʾ��ListView�ϣ���������idɾ�����ݿ������ֵ���Ϣ
	public boolean deleteMusicFromMediaStore(String[] musicId) {
		// �������ƴ���������Ӧ��������ɾ�����ҵ����ݿ��Դ!!!
		contentResolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media._ID + " = ?",
				musicId);
		return true;
	}
	
	public void setTitle(String title1)
	{
		this.title = title1;
	}
	
	public void setUrl(String url1)
	{
		this.url = url1;
	}
	
	public void setDuration(int duration1)
	{
		this.duration = duration1;
	}
	
	public void setImage(String image1)
	{
		this.image = image1;
	}
	
	public void setId(int id1)
	{
		this.id = id1;
	}
	
	public void setSingerName(String singerName1)
	{
		this.singerName = singerName1;
	}
	
	
	public String getTitle()
	{
		return title;
	}
	
	public String getUrl()
	{
		return url;
	}
	
	public int getDuration()
	{
		return duration;
	}
	
	public String getImage()
	{
		return image;
	}
	
	public int getId()
	{
		return id;
	}
	
	public String getSingerName()
	{
		return singerName;
	}
	
	
	


}
