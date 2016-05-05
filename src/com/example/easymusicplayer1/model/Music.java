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
		// cursor用完之后需要关闭么？如果关闭的话，每次从数据库查询东西是很麻烦的!!!
	/*	cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);*/
	}

	// 删除多媒体数据库中音乐名称的信息即可，因为重新打开app时，调用了initMusicTitle()函数，会初始化musicTitleList，而musicTitleList会被adapter用到，显示在ListView上，
	// musicTitleList中的元素要想不显示在ListView上，根据音乐id删除数据库中音乐的信息
	public boolean deleteMusicFromMediaStore(String[] musicId) {
		// 音乐名称从哪里来就应该在哪里删除，找到数据库表源!!!
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
