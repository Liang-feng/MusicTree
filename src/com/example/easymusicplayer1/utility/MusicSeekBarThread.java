package com.example.easymusicplayer1.utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

/**
 * 用来使歌词，seekBar（歌曲进度）,歌曲播放   三者同步 , 时间可以支持小时的功能没完善
 * @author feng
 *
 */
public class MusicSeekBarThread implements Runnable {

	Boolean isPlay;                           //标记当前音乐是否还在播放
	
	boolean networkIsAvaliable = true;               //标记当前网络是否可用
	
	MediaPlayer mediaPlayer; 
	
	SimpleDateFormat sDateFormat;             //用于解析毫秒
	
	Handler handler;
	
	int time;                                 //用于记录音乐的当前播放时间
	
	int duration;                             //用于存储歌曲时间的播放总长度!!!
	
	public static final int MUSIC_PROGRESS = 1;         //用于handler内部函数sendMessage
	
	String cureentTime1 = "";                         //如果不赋初值，那么在下面if判断句会出错!!!不可以赋值为null
	
	ArrayList<String> musicLyricTimeList;                  //用于存储一首歌中每句歌词时间轴
	
	ArrayList<String> musicLyricList;                      //用于存储一首歌的歌词
	
	TextView musicLyric;                                   //用于显示全部歌词
	
	TextView musicSingleLyric;                             //用于显示单句歌词
	 
	int index = 0;                                         //索引歌词 和 歌词时间轴的位置
	 //初始化为null，方便PlayMusicActivity中的handler判断singleLyric是否为空
	String singleLyric = null;                     //用于存储单句歌词，传递给PlayMusicActivity中的handler
	
	Boolean bool = false;                       //用于标记是否进入了synchronizationLyricAndMusic函数的if条件句里面
	
	@SuppressLint("SimpleDateFormat")
	public MusicSeekBarThread(Boolean isPlay , MediaPlayer mediaPlayer , Handler handler , int time
			) {
		musicLyricTimeList = new ArrayList<String>();
		musicLyricList = new ArrayList<String>();
		this.isPlay = isPlay;
		this.mediaPlayer = mediaPlayer;
		this.handler = handler;
		this.time = time;
		sDateFormat = new SimpleDateFormat("mm:ss");
		duration = mediaPlayer.getDuration();
        
	}
	
	/**
	 * 函数前头加上synchronized是为了，让此方法在同一时间只能被一个线程调用，只有执行完后，另一个线程才能执行此方法
	 * 相当于为此函数加锁.
	 */
	
	
	@Override
	public synchronized void run() {
		
		while (isPlay) {
			

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
                Log.e("MusicSeekBarThread", e.toString());
                System.out.println("MusicSeekBarThread 异常 : " + e.toString());
				e.printStackTrace();
			}
			

			/* 设置判断语句,为了防止用户一开始就把seekBar拖到底，而造成最后的time（当前播放时间）与duration不符合
			*  。以及在暂停播放后seekBar左边的秒数还在动,当音乐暂停后，以免点击后seekBar还在进行移动!!!
			*  如果不设置判读语句，那么暂停后，seekBar还会移动一格，即还会调用sendMessage()
			*  60行以及69到73行，如果放在外面，则在播放下一首歌曲的时候，seekBar会出现指针错乱!!
			*  还有一个bug，那就是如果是在前台服务点击下一首，还是会进入到里面，seekBar进度条与startTime有点不协调，
			*  如果以后需要改进，那么在前台服务定义一个boolean变量，另外再在下面的if判断语句中加入多这个boolean判断即可!!!
			*/

			/*在退出PlayMusicActivity的时候出现异常java.lang.IllegalStateException异常，
			 *该异常表示，当前对客户端的响应已经结束，不能在响应已经结束（或说消亡）后再向客户端（实际上是缓冲区）输出任何内容。
			 * 报错是mediaPlayer.isPlaying()出错，在退出PlayMusicActivity时，执行了onDestroy()函数，
			 * 当前客户端响应已经结束，所以不能再使用mediaPlayer向客户端（实际上是缓冲区）输出任何内容，报错！！！
			 */
			if(isPlay && time != duration && mediaPlayer.isPlaying() )  
			{
				
				int currentPosition = mediaPlayer.getCurrentPosition();
				
				if(networkIsAvaliable)
				{
	    			synchronizationLyricAndMusic(currentPosition);           //使歌词与音乐同步!!!
				}
	    		time += 1000;
	    		
				cureentTime1 = sDateFormat.format(new Date(time));

				//用来处理主线程的ui
				Message msg = new Message();
				msg.what = MUSIC_PROGRESS;
				msg.arg1 = currentPosition;
				if(bool && networkIsAvaliable)          //使用bool防止每次都传递重复的值过去!!!以及使用networkIsAvaliable来判断当前的网络是否存在，没网络则不同步歌词，防止报错,当然也可以加异常
				{
		    		Bundle bundle = new Bundle();
		    		bundle.putString("single_lyric", singleLyric);        
		    		msg.setData(bundle);           //用于返回当前的单句歌词
		    		bool = false;
				}
				msg.obj = cureentTime1;
				handler.sendMessage(msg);           //传递到PlayMusicActivity的handler上
			}

	        //如果音乐播放结束，或者时间到了，则跳出循环。 这样写的好处是，当音乐暂停时，子线程并不会退出去，当再次播放时，又可以继续正常工作
			if (cureentTime1.equals(sDateFormat.format(new Date(duration)))) {
				isPlay = false;
			}
		}
	}
	
	
	/**
	 * 使歌词与音乐同步!!!
	 */
	private void synchronizationLyricAndMusic(int currentPosition) {

		//由于启动本线程  在  本线程活动歌词数据之前，所以做此判断!即没活动数据，那么musicLyrictimeList就为空，所以做此判断!!
		if (musicLyricTimeList.size() != 0 && index < musicLyricTimeList.size())        
		{
			String LyricTime = musicLyricTimeList.get(index);
			long millisecond = changeTimeFormat(LyricTime);
			
			Log.e("MusicSeekBarThread", "currentPosition = " + String.valueOf(currentPosition) + 
					"     millisecond = " + String.valueOf(millisecond));

			if (currentPosition >= millisecond) // 如果当前音乐播放的进度，大于或等于歌词时间轴的进度，就移动歌词
			{
				//String lyric = (String) musicLyric.getText();
				singleLyric = musicLyricList.get(index);            //获取当前时刻的单句歌词
				Log.e("MusicSeekBarThread", singleLyric);
				index++;
				bool = true;
			}

		}
	}
	


	/**
	 * 改变时间的格式，从分转化为毫秒，用于与seekBar的进度做比较，进而同步歌词
	 * @return       返回转换后的毫秒数
	 */
	@SuppressLint("UseValueOf")
	public long changeTimeFormat(String time)
	{
		long millisecond = 0;
		String[] time1 = time.split(":");                                       //解析00:05.26格式的数据   , 如果是01:00:05.26呢
		//加上try ， catch的原因是，由于“古巨基-重复犯错”这首歌的musicLyricTime的第一个元素不是时间形式，带有offset，会抛出异常，
		//程序中断，防止程序中断，所以我在这里接住异常;
		//if(time1.length <= 2)          //如果歌曲是1小时以下
		{
			try 
			{
				millisecond = new Integer(time1[0]).longValue() * 60 * 1000; // 分转化为毫秒

				String[] time2 = time1[1].split("\\.");
				millisecond += new Integer(time2[0]).longValue() * 1000; // 秒转化为毫秒
				// 后叠加
				millisecond += new Integer(time2[1]).longValue(); // 毫秒叠加
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		/*else if(time1.length == 3)         //歌曲超过一小时
		{
			try 
			{
				millisecond = new Integer(time1[0]).longValue() * 60 * 60 * 1000; // 小时转化为毫秒

				millisecond += new Integer(time1[1]).longValue() * 60 * 1000;     // 分转化为毫秒
				
				String[] time2 = time1[2].split("\\.");
				millisecond += new Integer(time2[0]).longValue() * 1000;          // 秒转化为毫秒
				// 后叠加
				millisecond += new Integer(time2[1]).longValue();                 // 毫秒叠加
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}*/
		return millisecond;
	}
	
	
	public void setTime(int time)
	{
		this.time = time;                      //如果拖动seekBar，则startTime要改变，在这里改变
	}
	
	public void setIsPlay(Boolean bool)
	{
		isPlay = bool;
	}
	
	public void setMusicLyricTimeList(ArrayList<String> musicLyricTimeList)
	{
		this.musicLyricTimeList = musicLyricTimeList;
	}
	
	public void setMusicLyricList(ArrayList<String> musicLyricList)
	{
		this.musicLyricList = musicLyricList;
	}
	
	/**
	 * 当拖动seekBar的时候，设置歌词的索引，使得歌词可以同步!
	 */
	public void setLyricIndex(int index1)
	{
		this.index = index1;
	}
	
	public void setNetworkIsAvailable(Boolean bool)
	{
		this.networkIsAvaliable = bool;
	}

}
