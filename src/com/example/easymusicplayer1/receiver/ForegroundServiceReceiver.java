package com.example.easymusicplayer1.receiver;

import java.util.ArrayList;

import com.example.easymusicplayer1.R;
import com.example.easymusicplayer1.activity.PlayMusicActivity;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * 这个还没有被使用，没有从MusicForegroundService移植过来呢!
 * @author feng
 *
 */
public class ForegroundServiceReceiver extends BroadcastReceiver {
	
    final static String CLICK_LAST = "com.example.eastmusicplayer1.music_last";
	
	final static String CLICK_NEXT = "com.example.eastmusicplayer1.music_next";
	
	final static String CLICK_PLAY_OR_PAUSE = "com.example.eastmusicplayer1.music_play_or_pause";
	
	final static String HEADSET_PLUG_EXTRACT = Intent.ACTION_HEADSET_PLUG;
	
	int musicTitlePosition;

	RemoteViews views;

	ArrayList<String> musicTitleList;

	Boolean play = false;          //用来标记当前的图片是否是播放图片，还是暂停图片
	
	Service serviceContext;

	Notification notification;
	
	public ForegroundServiceReceiver(RemoteViews views , ArrayList<String> musicTitleList , Service serviceContext ,
			Notification notification) {
 
		this.views = views;
		this.musicTitleList = musicTitleList;
		this.serviceContext = serviceContext;
		this.notification = notification;
		
	}


	@Override
	public void onReceive(Context context, Intent intent) {

		if(intent.getAction().equals(CLICK_LAST))
		{
			//在这里已经把PlayMusicActivity与本服务的musicTitlePosition同步了，这样前台服务的音乐名称就不会错乱!!!
			musicTitlePosition = PlayMusicActivity.playLastMusic();//播放上一首歌   , 并且返回歌曲的位置，便于前台和PlayMusicActivity的界面上的歌曲名称相同 
		    views.setTextViewText(R.id.title_text_view , musicTitleList.get(musicTitlePosition));   //修改音乐名称 , 如果删除了歌曲，会不会出错?????
			views.setImageViewResource(R.id.play_and_pause , R.drawable.ic_pause_black_48dp);   //让播放和暂停能正确实现
			
			play = false;
		}
		else if(intent.getAction().equals(CLICK_NEXT))
		{
			musicTitlePosition = PlayMusicActivity.playNextMusic();   //播放下一首歌曲，并且返回歌曲的位置，便于前台和PlayMusicActivity的界面上的歌曲名称相同
	    		
			views.setTextViewText(R.id.title_text_view , musicTitleList.get(musicTitlePosition));   //修改音乐名称
			views.setImageViewResource(R.id.play_and_pause , R.drawable.ic_pause_black_48dp);   //让播放和暂停能正确实现
			
		    play = false;
		}
		else if(intent.getAction().equals(CLICK_PLAY_OR_PAUSE) )
			//	|| intent.getAction().equals(HEADSET_PLUG_EXTRACT))//如果耳塞拔出或者按了前台服务的播放，暂停按钮        //no bug		{ 
			PlayMusicActivity.playOrPauseMusic();         //暂停或者播放歌曲
            
			if(play == true)
			{
                //PlayMusicActivity.slowDecreaseVolume();         //再点击暂停按钮的时候，缓慢降低声音!!!
                
	    		views.setImageViewResource(R.id.play_and_pause , R.drawable.ic_pause_black_48dp);
	    		play  = false;
	    		
			}
			else if(play == false)
			{
				views.setImageViewResource(R.id.play_and_pause , R.drawable.ic_play_arrow_black_48dp);
			    play = true;
			}
		
		serviceContext.startForeground(1 , notification);     //这里启动的前台服务的id要与下面onCreate()函数启动前台服务的id一致，不然会出现一闪一闪的,即启动了两个不同的服务
		
	}

}
