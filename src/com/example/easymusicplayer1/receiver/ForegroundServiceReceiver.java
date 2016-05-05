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
 * �����û�б�ʹ�ã�û�д�MusicForegroundService��ֲ������!
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

	Boolean play = false;          //������ǵ�ǰ��ͼƬ�Ƿ��ǲ���ͼƬ��������ͣͼƬ
	
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
			//�������Ѿ���PlayMusicActivity�뱾�����musicTitlePositionͬ���ˣ�����ǰ̨������������ƾͲ������!!!
			musicTitlePosition = PlayMusicActivity.playLastMusic();//������һ�׸�   , ���ҷ��ظ�����λ�ã�����ǰ̨��PlayMusicActivity�Ľ����ϵĸ���������ͬ 
		    views.setTextViewText(R.id.title_text_view , musicTitleList.get(musicTitlePosition));   //�޸��������� , ���ɾ���˸������᲻�����?????
			views.setImageViewResource(R.id.play_and_pause , R.drawable.ic_pause_black_48dp);   //�ò��ź���ͣ����ȷʵ��
			
			play = false;
		}
		else if(intent.getAction().equals(CLICK_NEXT))
		{
			musicTitlePosition = PlayMusicActivity.playNextMusic();   //������һ�׸��������ҷ��ظ�����λ�ã�����ǰ̨��PlayMusicActivity�Ľ����ϵĸ���������ͬ
	    		
			views.setTextViewText(R.id.title_text_view , musicTitleList.get(musicTitlePosition));   //�޸���������
			views.setImageViewResource(R.id.play_and_pause , R.drawable.ic_pause_black_48dp);   //�ò��ź���ͣ����ȷʵ��
			
		    play = false;
		}
		else if(intent.getAction().equals(CLICK_PLAY_OR_PAUSE) )
			//	|| intent.getAction().equals(HEADSET_PLUG_EXTRACT))//��������γ����߰���ǰ̨����Ĳ��ţ���ͣ��ť        //no bug		{ 
			PlayMusicActivity.playOrPauseMusic();         //��ͣ���߲��Ÿ���
            
			if(play == true)
			{
                //PlayMusicActivity.slowDecreaseVolume();         //�ٵ����ͣ��ť��ʱ�򣬻�����������!!!
                
	    		views.setImageViewResource(R.id.play_and_pause , R.drawable.ic_pause_black_48dp);
	    		play  = false;
	    		
			}
			else if(play == false)
			{
				views.setImageViewResource(R.id.play_and_pause , R.drawable.ic_play_arrow_black_48dp);
			    play = true;
			}
		
		serviceContext.startForeground(1 , notification);     //����������ǰ̨�����idҪ������onCreate()��������ǰ̨�����idһ�£���Ȼ�����һ��һ����,��������������ͬ�ķ���
		
	}

}
