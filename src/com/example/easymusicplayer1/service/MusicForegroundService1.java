package com.example.easymusicplayer1.service;

import java.util.ArrayList;

import com.example.easymusicplayer1.R;
import com.example.easymusicplayer1.activity.PlayMusicActivity;
import com.example.easymusicplayer1.receiver.ForegroundServiceReceiver;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * 前台服务，有一个监听拔耳塞，插耳塞的监听事件，留待完善!!!
 * @author feng
 *
 */
public class MusicForegroundService1 extends Service {

	Notification notification;
	
	MusicBinder musicBinder;
	
	final static String CLICK_LAST = "com.example.eastmusicplayer1.music_last";
	
	final static String CLICK_NEXT = "com.example.eastmusicplayer1.music_next";
	
	final static String CLICK_PLAY_OR_PAUSE = "com.example.eastmusicplayer1.music_play_or_pause";
	
	final static String HEADSET_PLUG_EXTRACT = Intent.ACTION_HEADSET_PLUG;          //耳机是否插入或者拔出

	RemoteViews views;
	
	Boolean play = true;          //用来标记当前的图片是否是播放图片，还是暂停图片,第二个else if加上耳机的判断的话true就为暂停，没有耳机的判断则false为暂停图片 
	
	Service serviceContext;
	
	ArrayList<String> musicTitleList;
	
	int musicTitlePosition;
	
	
	/*广播为什么会无缘无故被调用呢，搞的onReceive执行了，进而调用了第二个else if语句里面的PlayMusicActivity.playOrPauseMusic();  
	*  偏偏只调用了第二个else if，我没有点击啊!
	*/
	BroadcastReceiver receiver = new BroadcastReceiver() {
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
			//如果下面多加了|| intent.getAction().equals(HEADSET_PLUG_EXTRACT)，那么不按播放按钮键，这里也会被执行，接受了耳机的广播，晕!!!
			else if(intent.getAction().equals(CLICK_PLAY_OR_PAUSE) 
					|| intent.getAction().equals(HEADSET_PLUG_EXTRACT))    //如果耳塞拔出或者按了前台服务的播放，暂停按钮        //no bug
			{ 
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
				
		
			}
			
			serviceContext.startForeground(1 , notification);     //这里启动的前台服务的id要与下面onCreate()函数启动前台服务的id一致，不然会出现一闪一闪的,即启动了两个不同的服务
		}
	};
	
	
	
	public class MusicBinder extends Binder
	{       
		
		//MyMusicFragment , PlayMusicActivity 与  service的交互 , 用于使得刚开始前台service的音乐名称是用户所点击的歌曲!!!
		public void changeMusicTitle(ArrayList<String> musicTitleList1 , int musicTitlePosition1)
		{
			musicTitleList = musicTitleList1;              //接受从MyMusicFragment传递来的数据
			musicTitlePosition = musicTitlePosition1;
			//Toast.makeText(getApplicationContext(),  "数据传递成功了么？" + musicTitleList , Toast.LENGTH_SHORT).show();
			
			views.setTextViewText(R.id.title_text_view , musicTitleList.get(musicTitlePosition));
            serviceContext.startForeground(1 , notification);
		}
		
		
		public void changeMusicPlayOrPausePic(Boolean play1)   //供PlayMusicActivity调用。 PlayMusicActivity中的播放图片 与 前台播放的图片 同步
		{
			if(play1 == true)
			{
		    	views.setImageViewResource(R.id.play_and_pause , R.drawable.ic_play_arrow_black_48dp);
			}
			else if(play1 == false)
			{
				views.setImageViewResource(R.id.play_and_pause , R.drawable.ic_pause_black_48dp);
			}
			
            serviceContext.startForeground(1 , notification);
            play = play1;           //统一标记当前的图片是播放还是暂停

		}
		
		
	}
	
	//no bug
	@Override
	public IBinder onBind(Intent intent) {		//似乎可以通过传递参数过来，减少static变量的使用，在启动活动的时候!!!上网查一下资料先，看onBind的参数intent??????????/
		
		musicBinder = new MusicBinder();
		return musicBinder;
	}
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Intent intent = new Intent(this , PlayMusicActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this , 0 , intent , PendingIntent.FLAG_UPDATE_CURRENT);
		
		Intent lastIntent = new Intent(CLICK_LAST);
		Intent nextIntent = new Intent(CLICK_NEXT);
		Intent playOrPauseIntent = new Intent(CLICK_PLAY_OR_PAUSE);
		PendingIntent lastPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, lastIntent, PendingIntent.FLAG_UPDATE_CURRENT);    //flag 参数:如果PendingIntent已经存在，则保留，但是改变其extra的数据
		PendingIntent nextPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent playOrPausePendingIntet = PendingIntent.getBroadcast(getApplicationContext(), 0, playOrPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	
		
		
		views = new RemoteViews(getPackageName(), R.layout.music_foreground_layout) ;
		
		views.setOnClickPendingIntent(R.id.last, lastPendingIntent);
		views.setOnClickPendingIntent(R.id.next, nextPendingIntent);
		views.setOnClickPendingIntent(R.id.play_and_pause 	, playOrPausePendingIntet);
		
		
		notification = new Notification.Builder(this)
				.setSmallIcon(R.drawable.ic_music_note_black_48dp)
				//.setContent(views)
				.setContentIntent(pendingIntent)
				.build();
		
		notification.bigContentView = views;
		
		serviceContext = this;          //用来在MusicBinder
		this.startForeground(1 , notification);              //这里启动的前台服务的id要与上面onReceive函数上面启动前台服务的id一致，不然会出现一闪一闪的
		
		
		//ForegroundServiceReceiver receiver = new ForegroundServiceReceiver(views, musicTitleList, serviceContext, notification);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CLICK_LAST);                    //点击了前台服务的上一首
		intentFilter.addAction(CLICK_NEXT);                    //点击了前台服务的下一首
		intentFilter.addAction(CLICK_PLAY_OR_PAUSE);           //点击了前台服务的播放或者暂停
		intentFilter.addAction(HEADSET_PLUG_EXTRACT);          //耳塞拔出
		this.registerReceiver(receiver, intentFilter);
		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(receiver);  //Unregister a previously registered BroadcastReceiver.All filters that have been registered for this BroadcastReceiver will be removed.

	}

}
