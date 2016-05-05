package com.example.easymusicplayer1.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.example.easymusicplayer1.R;
import com.example.easymusicplayer1.model.MusicTop;
import com.example.easymusicplayer1.net.FindMusicFromInternet;
import com.example.easymusicplayer1.net.RequestMusicLyric;
import com.example.easymusicplayer1.service.MusicForegroundService1;
import com.example.easymusicplayer1.service.MusicForegroundService1.MusicBinder;
import com.example.easymusicplayer1.utility.MusicSeekBarThread;
import com.example.easymusicplayer1.utility.MyApplication;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ShareActionProvider;
import android.widget.TextView;

/**
 * 播放音乐的界面，可现实歌词，以及可以分享音乐的下载地址，以及音乐与歌词同步
 * @author feng
 *
 */
public class PlayMusicActivity extends Activity implements OnGestureListener
{
    public static int isOpen = 0;
    
	static TextView musicTitle;

	// ImageView musicImageView;

	static ImageView play_pause;       // “播放”图片

	ImageView playlist;                       // 显示播放列表的“图片”
	
	static SeekBar musicProgressBar;          //音乐播放进度条,seekBar 为课拖动进度条!
	
	static TextView startTime;                //音乐进度条前面的当前播放时间
	
	static TextView endTime;                  //音乐总时长
	
	static TextView musicLyric;               //音乐歌词
	
	static TextView musicSingleLyric;                //音乐单行歌词
	
	static SimpleDateFormat sDateFormat;
	
	int progress;
	
	static int time = 0; 
	
	static Boolean isPlay = true;
	
	public  static final int MUSIC_PROGRESS = 1;
	
	static MusicSeekBarThread musicSeekBarThread;                   //音乐进度条的子线程
	
	static Thread thread = null;                                           //在这里创建，方便kill掉

	static Boolean play = false;       // 用来记录当前的图片是“播放”图片，还是暂停“图片”
	
	static Boolean playOnce = false;      //不赋初值是会报错的，在下面if判断句中

	static MediaPlayer mediaPlayer = null;     
	
	Intent intent; // 用于获取从MyMusicFragment中的intent

	static String musicName;

	static String musicUrl;
	
	private static String activity;                //存储是哪个activity打开的PlayMusicActivity
	
	static int musicDuration;

	private GestureDetector gestureDetector;

	static int musicPosition; // 音乐在ListView的item上的位置，从MainActivity传递过来，方便用于进行顺序播放!!!

	private static ArrayList<String> musicTitleList; // 存储音乐名称

	private static ArrayList<String> musicUrlList; // 存储音乐文件的路径

	private static ArrayList<Integer> musicDurationList; // 存储音乐的播放时长
	
	private static ArrayList<String> musicLyricList;            //存储一首歌的歌词
	
	private static ArrayList<String> musicLyricTimeList;        //存储一首歌的歌词的时间轴
	
	private static ArrayList<String> musicSingerList;           //存储歌手
	
	private static String playOrder; // 每次从MyMusicFragment选歌时 ， 获取播放顺序
												// , 要不要考虑改一下，改成静态函数，或者new
												// 一个对象来调用函数返回？？
	ActionBar actionBar;
	
	ShareActionProvider mShareActionProvider;
	
	static ProgressDialog progressDialog;

	private static MusicForegroundService1.MusicBinder musicBinder;      //可以通过musicBinder实例调用service中的函数，对service进行操作

	Intent serviceIntent = null; // 用于绑定服务

	ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {

			musicBinder = (MusicBinder) service; // 获取控制service的对象
		}
	};
	
	
	static Handler handler = new Handler() {
		
		@SuppressWarnings("deprecation")
		@Override
		public void handleMessage(Message msg) {
		super.handleMessage(msg);
		
	    int currentPosition = msg.arg1;               //从子线程传来当前seekBar的位置
		String currentTime1 = (String) msg.obj;       //从子线程传当前的音乐播放位置
		
	    switch(msg.what)
		{
			case MUSIC_PROGRESS:
		    		musicProgressBar.setProgress(currentPosition);     //改变当前进度条的位置
		    		startTime.setText(currentTime1);                   //改变starttiem的数据
		    		Bundle bundle = msg.getData();
		    		String singleLyric = (String) bundle.get("single_lyric");
		    		if(singleLyric != null)          //第一次执行MusicSeekBarThrea线程时是空的，这个值，所以做此判断!
		    		{
		        		musicSingleLyric.setText(singleLyric);
		    		}
		    		
		    		//在2015年12月9日19:41修改
		    		//时刻同步歌曲歌词，防止在3G网络下，播放完一首时，在还没来得及获取歌词就按了seekBar，结果歌词后来同步不上了，所以加这句
		    		int index = synchronizationLyric(currentPosition);         
					if(index != 1)          //当歌词存在时，获取到了歌词，才进行同步歌词
					{
					    musicSeekBarThread.setLyricIndex(index);                      //同步歌词
					}
		    		break;
		}
	}
	
	    @Override
		public void dispatchMessage(Message msg) {
			super.dispatchMessage(msg);

		}
	};
	
	

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play_music);
		
		initView();                //绑定,获取控件,以及控件的监听事件

		
		Boolean isConnect = isConnectInternet();                         //判断当前是否有网络存在!!!
		if(isConnect)                                                    //如果联网了就显示progressBar
		{
    		setProgressDialog();
		}
		else
		{
			setSingleLyricText();                 //设置获取歌词失败的提示
			cancelProgressDialog();               //关闭掉progressDialog
			startMusic();                         //开始播放音乐
		}
    
		initActionBar();
        
		// 获取手势实例
		gestureDetector = new GestureDetector(PlayMusicActivity.this);

		readDataFromActivity(); // 从 “启动PlayMusicActivity”的活动 获取传递来的数据


		initMediaPlayer();            // 准备好播放
		
		setMusicDateInUI();          //设置歌词，seekBar，歌曲时长

		if(!activity.equals("MusicTopFragment"))         //如果是从音乐之家播放的音乐，那么就不用显示前台服务了!!!
		{
			Log.e("PlayMusicActivity", "绑定服务");
	    	// 与MusicForegroundService1绑定 ，为了可以在歌曲按顺序播放，或者是循环播放，或者是随机播放时，可以改变前台服务的歌曲名称
    		serviceIntent = new Intent(PlayMusicActivity.this, MusicForegroundService1.class);
    		bindService(serviceIntent, serviceConnection, 0);                
		}
	}
	
	
	/**
	 * 用来判断当前的网络是否存在!
	 * @return
	 */
	private Boolean isConnectInternet() {

		ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		
		if(networkInfo != null && networkInfo.isAvailable())
		{
			return true;
		}
		
		return false;
		
	}



	private void setProgressDialog() {

		progressDialog = new ProgressDialog(PlayMusicActivity.this , 1);           //后面的参数是风格，1比较好看
		progressDialog.setMessage("数据加载中");
		progressDialog.setCancelable(false);                       //设置dialog不能通过按back键返回，防止bug出现，即在歌词还没有加载出来之前就退出本activity的话，在按另一首歌曲时，歌词不匹配！！！
		progressDialog.show();
	
	}



	private void setMusicDateInUI() {
		musicTitle.setText(musicName);
		
		endTime.setText(sDateFormat.format(new Date(mediaPlayer.getDuration()))); //设置歌曲的总播放时间
		musicProgressBar.setMax(mediaPlayer.getDuration());                 //设置seekBar的总时长        
		
	}


	

	/**
	 * ActionBar的相关设置
	 */
    private void initActionBar() {
    	actionBar = getActionBar();
    	actionBar.setTitle("播放音乐");
		actionBar.setDisplayShowHomeEnabled(false);        //设置ActionBar左侧的图标不可见
		actionBar.setDisplayShowTitleEnabled(false);       //设置ActionBar左侧的标题不可见
		actionBar.setDisplayHomeAsUpEnabled(true);         //设置ActionBar左侧的返回上一级的图标显示出来!!!
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
        this.getMenuInflater().inflate(R.menu.play_music_activity , menu);
        
        MenuItem menuItem = menu.findItem(R.id.share_action);
        mShareActionProvider = (ShareActionProvider) menuItem.getActionProvider();
        
        FindMusicFromInternet.setShareActionProvider(mShareActionProvider);    //给FindMusicFromInternet类传递实例是为了，当数据获取完成后可以直接设置shareIntent,因为在这里设置shareIntent的话，会直接就调用了，数据还没来得及获取完!
        
        return true;
    }
    

    public static Intent getDefaultIntent(String data) {
    	
    	String shareData = data;
    	Intent intent = new Intent(Intent.ACTION_SEND);
       
        intent.putExtra(Intent.EXTRA_TEXT ,"【我正在听 : " + musicTitleList.get(musicPosition) + "  " + 
                        musicSingerList.get(musicPosition) + "\n" + "请到 " + shareData + " 下载】"
                        + "\n" + "       ————来自MusicTree");

    	intent.setType("text/plain");                        //指定传输的数据为普通文本数据
    	
    	return intent;
	}
    


	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId())
    	{
    	case android.R.id.home:               //给ActionBar左侧返回图标设置事件
    		finish();                         //结束activity
			//如果是从MyMusicActivity打开的PlayMusicActivity则退出时，返回到MyMusicActivity。
			//如果是从MusicTopActivity打开的PlayMusicActivity则退出时，返回到MusicTopActivity。
    		if(activity.equals("MyMusicFragment"))
            {
 		    	Intent intent = new Intent(PlayMusicActivity.this, MyMusicActivity.class);
 	     		startActivity(intent);
             }
            else if(activity.equals("MusicTopFragment"))
            {
         	   Intent intent = new Intent(PlayMusicActivity.this, MusicTopActivity.class);
 	     		startActivity(intent);
            }
    		break;
    		default:
    			break;
    	}
    	return super.onOptionsItemSelected(item);
    }



	/**
     * 绑定,获取控件,以及控件的监听事件
     */
	 private void initView() {
	    	 musicTitle = (TextView) findViewById(R.id.music_title);
	    	 musicLyric = (TextView) findViewById(R.id.music_lyric);
	    	 musicSingleLyric = (TextView) findViewById(R.id.music_single_lyric);
			play_pause = (ImageView) findViewById(R.id.music_play);
			playlist = (ImageView) findViewById(R.id.music_playlist);
			startTime = (TextView) findViewById(R.id.start_time);            
			endTime = (TextView) findViewById(R.id.end_time);
			musicProgressBar = (SeekBar) findViewById(R.id.music_seek_bar);
			sDateFormat = new SimpleDateFormat("mm:ss");
			
			setPlayAndPauseOnClickListener();               //设置播放和暂停按钮的监听事件!!!
			setSeekBarOnDragListener();                     //设置音乐的播放进度条被拖动时或者点击时的监听事件!!!
	
	}

    /**
     * 设置音乐的播放进度条被拖动时的监听事件!!!
     */
    private void setSeekBarOnDragListener() {
		
    	musicProgressBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser)
				{
					startTime.setText(sDateFormat.format(new Date(seekBar.getProgress())));    //当暂停拖动的时候，把时间设置到当前位置！
				}
		
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int currentProgress = seekBar.getProgress();
				mediaPlayer.seekTo(currentProgress);                //当停止拖动进度条后，播放当前进度的音乐
				musicSeekBarThread.setTime(currentProgress);        //给子线程更新时间，使得的starttime可以正确显示
                
				musicSingleLyric.setText("");                                  //使单句歌词为空白，在显示出正确的歌词之前
				int index1 = synchronizationLyric(currentProgress);            //同步歌词
				if(index1 != 1)          //当歌词存在时，获取到了歌词，才进行同步歌词
				{
				    musicSeekBarThread.setLyricIndex(index1);                      //同步歌词
				}
			}

	   });
    	
	}

	/**
	 * 用于移动seekBar后也能同步歌词 ,  可以重新建立一个类，传入currentProgress ， musicSeekBarThread，
	 * 以及musicLyricTime即可 参数即可
	 */
	public static int synchronizationLyric(int currentProgress) {
		
		int index = 0; 
		
		if (musicLyricTimeList != null) {
			// 用来索引当前的歌词是在哪个位置
			for (String timeStr : musicLyricTimeList) {
				int time = (int) musicSeekBarThread.changeTimeFormat(timeStr); // 解析时间格式，变为ms
				if ((currentProgress - time) < 0) // 获取离当前进度最近的歌词 ，
													// 反正最终在MusicSeekBarThread线程都会同步的，只是时间的精确度问题而已
				{
					// index--;
					break;
				}
				index++;
			}
		}
		return index;
		
		
	}

	/**
     * 设置播放和暂停按钮的监听事件，并且设置让前台服务的播放和暂停按钮保持同步!!!
     */
	private void setPlayAndPauseOnClickListener() {
		play_pause.setOnClickListener(new OnClickListener() { // 设置点击图片时候的监听事件
			//检查过了，貌似也没有bug，具体还要测试一下才知道!!!
			@Override
			public void onClick(View v) {
				if (play == true) {
					play_pause.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp); // 点击“播放图片”把图片置换为“暂停”图片
					play = false; // 标记当前的图片为“暂停”图片
					
					if (!mediaPlayer.isPlaying()) {
						mediaPlayer.start();
					}

					if(!activity.equals("MusicTopFragment"))          //如果不是从音乐之家启动到本activity就执行前台服务按钮的同步操作
					{
						musicBinder.changeMusicPlayOrPausePic(play); // 前台播放图片 与
					}
					
					if(playOnce)     //用于在播放方式为“只播放一次”的情况下，当播放一首歌结束，再次播放时可以控制startTime以及seekBar更新
					{
						startMusicSeekBarThread();         //启动子线程，来控制startTime以及seekBar的更新
						musicSeekBarThread.setMusicLyricList(musicLyricList);           //由于是单曲循环，数据不变
						musicSeekBarThread.setMusicLyricTimeList(musicLyricTimeList);
						playOnce = false;                  //标记为false，意思是当下次按下播放按钮的时候，不要再进入到这里面启动线程了
					}
				} else if (play == false) {
					play_pause.setImageResource(R.drawable.ic_play_circle_outline_white_48dp); // 点击“暂停图片”把图片置换为“播放”图片
					play = true; // 标记当前的图片为“播放”图片

					//slowDecreaseVolume();             //在点击播放按钮时，缓慢降低音乐声音!!!

					if (mediaPlayer.isPlaying()) // 如果歌曲正在播放，则暂停播放
					{
						mediaPlayer.pause();
					}

					if(!activity.equals("MusicTopFragment"))          //如果不是从音乐之家启动到本activity就执行前台服务按钮的同步操作
					{
		     			musicBinder.changeMusicPlayOrPausePic(play); // 前台播放图片 与PlayMusicActivity中的播放图片同步
					}
				}
			}
		});		
	}



	public static  int playLastMusic()
	 {
		if (musicPosition - 1 <= 0) // 如果目前在播放第一首歌，点击上一首的话，那么就播放最后一首
		{
			musicPosition = musicTitleList.size();
		}

		mediaPlayer.reset();                   // 重置mediaPlayer，因为下面运行出错，没有这行的话，出错了我才想到要重新设置mediaPlayer!!!
		musicUrl = musicUrlList.get(musicPosition - 1);
		musicName = musicTitleList.get(musicPosition - 1);

		musicPosition -= 1;

		initMediaPlayer(); // 准备播放下一首音乐
		
		musicDuration = mediaPlayer.getDuration();      //musicDurationList.get(musicPosition - 1);  //由于为空，所以换了种形式!!!

		
		musicTitle.setText(musicName);
		musicProgressBar.setMax(mediaPlayer.getDuration());   //设置音乐进度条的最大长度
		endTime.setText(sDateFormat.format(new Date(mediaPlayer.getDuration())));  //设置音乐总时长
		
		// 设置好播放的图片，如果原来为播放图片 ，那么如果不设置下面的话，就会出现图片混乱，播放和暂停功能无法正确实现
		play = false;
		play_pause.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
		
		musicLyric.setText("");
		musicSingleLyric.setText("");
		musicSingleLyric.setText("获取歌词中...");

		//获取下一首的歌词，下面这个函数会进行一系列的操作
		FindMusicFromInternet findMusicFromInternet = new FindMusicFromInternet(musicTitleList.get(musicPosition)
				+ musicSingerList.get(musicPosition));   //直接调用这个，从网络获取歌词id就会显示歌词了!
		findMusicFromInternet.getMusicIdFromInternet();
		//mediaPlayer.start(); // 开始播放音乐!!!

		return musicPosition;         //返回musicPosition给前台服务，用来改变当前的音乐名称
	}
	
	
	public static int playNextMusic() // 播放下一首歌
	{
		if (musicPosition == musicTitleList.size() - 1) // 如果目前在播放第一首歌，点击上一首的话，那么就播放最后一首
		{
			musicPosition = -1;
		}

		mediaPlayer.reset(); // 重置mediaPlayer，因为下面运行出错，没有这行的话，出错了我才想到要重新设置mediaPlayer!!!
		musicUrl = musicUrlList.get(musicPosition + 1);
		musicName = musicTitleList.get(musicPosition + 1);

		musicPosition += 1;

		initMediaPlayer(); // 准备播放下一首音乐
		
		musicDuration = mediaPlayer.getDuration();        //musicDurationList.get(musicPosition + 1); 为空，所以换了一下
		
		musicTitle.setText(musicName);                         //设置音乐标题
		musicProgressBar.setMax(mediaPlayer.getDuration());   //设置音乐进度条的最大长度
		endTime.setText(sDateFormat.format(new Date(mediaPlayer.getDuration())));  //设置音乐总时长
		
		

		// 设置好播放的图片，如果原来为播放图片 ，那么如果不设置下面的话，就会出现图片混乱，播放和暂停功能无法正确实现
		play = false;
		play_pause.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
		
		
		musicLyric.setText("");
		musicSingleLyric.setText("");
		musicSingleLyric.setText("获取歌词中...");

		
		//获取下一首的歌词，下面这个函数会进行一系列的操作
		FindMusicFromInternet findMusicFromInternet = new FindMusicFromInternet(musicTitleList.get(musicPosition)
						+ musicSingerList.get(musicPosition));   //直接调用这个，从网络获取歌词id就会显示歌词了!
		findMusicFromInternet.getMusicIdFromInternet();
		//mediaPlayer.start(); // 开始播放音乐!!!

		return musicPosition;         //返回musicPosition给前台服务，用来改变当前的音乐名称
	}

	public static void playOrPauseMusic() // 控制暂停或者播放音乐
	{
		Log.e("PlayMusicActivity", "奇怪!!");

		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			play_pause.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);
			play = true;
		} else if (!mediaPlayer.isPlaying()) {
			Log.e("PlayMusicActivity", "奇怪!!");
			mediaPlayer.start();
			play_pause.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
			play = false;
		}
	}

	// no bug
	public void readDataFromActivity() // 从启动PlayMusicActivity的Activity中读取数据
	{
		// 获取从MainActivity传来的数据
		intent = getIntent();                       // 一定要在函数里面初始化!!!外面初始化就不一样了，详情见博客!!!
		activity = intent.getStringExtra("fragment");
		
		if (activity.equals("MyMusicFragment"))
		{
			// 从 “启动PlayMusicActivity”的活动 获取传递来的数据
			Bundle bundle = intent.getExtras();
			musicName = bundle.getString("music_title");
			musicUrl = intent.getStringExtra("music_url");
			musicDuration = intent.getIntExtra("music_duration", 0);
			musicPosition = intent.getIntExtra("music_title_position", 0);
			musicTitleList = intent.getStringArrayListExtra("music_title_list");
			musicUrlList = intent.getStringArrayListExtra("music_url_list");
			musicSingerList = intent.getStringArrayListExtra("music_singer");
			musicDurationList = intent.getIntegerArrayListExtra("music_duration_list");
			playOrder = intent.getStringExtra("music_play_order");         //获取播放顺序!!!
		}
		else if (activity.equals("MusicTopFragment"))           //从“音乐馆传来的数据”
		{
			musicName = intent.getStringExtra("music_name");
			musicUrl = intent.getStringExtra("music_url");
			musicTitleList = intent.getStringArrayListExtra("music_title_list");
			musicUrlList = intent.getStringArrayListExtra("music_url_list");
			musicSingerList = intent.getStringArrayListExtra("music_singer");
			playOrder = "ONCE_PLAY";             //如果是在线播放，那么只播放一次，避免流量损失!
		}
		
	}

	
	/**
	 *  1.准备好播放音乐 , 以及播放音乐，还有监听音乐是否播放完成，进而选择播放方式
	 *  2.当播放完一首歌后，播放下一首歌，也会重新运行这个函数，那么子线程也会重新启动!!!
	 */
	// no bug 
	public static void initMediaPlayer() {
		
		try {
		   if(mediaPlayer == null)
			{
				mediaPlayer = new MediaPlayer();
			}
			mediaPlayer.setDataSource(musicUrl);    // 指定音频文件的路径
			mediaPlayer.prepare();                  // 让MediaPlayer进入到准备状态
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	/*	if(mediaPlayer != null)
    		Log.e("MainActivity" , mediaPlayer.toString());*/
		
		//mediaPlayer.start(); // 开始播放音乐            不要一开始就播放音乐，当歌词加载完才开始播放音乐!或者加载不出歌词也开始播放
		
		startMusicSeekBarThread();
	
		
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() { // 监听歌曲是否播放结束
			//已经检查过一次，貌似没有bug了，具体的还要测试一下才知道!!!
			@Override
			public void onCompletion(MediaPlayer mp) {

				//在这前面执行的原因是：由于endTime的TextView会先变化，进度条后变化，显得不美观，所以先进行进度条置0
				//不论是什么播放方式，循环也好，只播放一次也好，播放完就作如下设置!!!
				musicProgressBar.setProgress(0);                          //进度条重新开始
				startTime.setText("00:00");                           //重新设置音乐开始的时间

				try {
					Thread.sleep(1000);         // 播放完歌曲后暂停一下,可能会导致界面卡顿,导致动作延迟
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Log.e("PlayMusicActivity", playOrder);
				if (playOrder.equals("SINGLE_CIRCLE"))   // 如果设置了单曲循环 
				{
					musicSingleLyric.setText("");                 //设置文本为空,当重新播放时!
					startMusicSeekBarThread();
					musicSeekBarThread.setMusicLyricList(musicLyricList);           //由于是单曲循环，数据不变
					musicSeekBarThread.setMusicLyricTimeList(musicLyricTimeList);
					mediaPlayer.start(); // 就直接继续播放了
				} 
				else if (playOrder.equals("ORDER_PLAY")) // 如果从一首不能播放的跳到，一个不能播放的音乐文件，相当于，播放完毕，所以前台服务，与PlayMusicActivity会同步变化歌曲名称!!!
				{
					if (musicPosition == musicTitleList.size() - 1) // 如果播放的是最后一首，则重新从第一首歌播放!!!
					{
						musicPosition = -1;
					}
					
					musicSingleLyric.setText("歌词加载中...");  
					Log.e("PlayMusicActivity", playOrder + "里面");

					setMusicRelativeHandle();             //设置播放下一首音乐的相关设置
				} 
				else if (playOrder.equals("RANDOM_PLAY")) {
					musicPosition = (int) (Math.random() * (musicTitleList.size() - 2));

		            setMusicRelativeHandle();             //设置播放下一首音乐的相关设置

				} 
				else if (playOrder.equals("ONCE_PLAY")) {
					// 只播放一次，播放完后不进行任何操作!!!
					play = true;
					play_pause.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);
					if(!activity.equals("MusicTopFragment"))          //如果不是从音乐之家启动到本activity就执行前台服务按钮的同步操作
					{
			    		musicBinder.changeMusicPlayOrPausePic(play);//相对应的改变前台服务的播放或者暂停按钮
					}
					playOnce = true;   //用来标记在播放方式为“只播放一次”的情况下,当播放完后，当再次点击按钮播放时，利于重新设置seekBar
					//重新做如下设置
					musicSingleLyric.setText("");                 //设置文本为空,当重新播放时!
				}

			}

			/**
			 * 由于顺序播放与随机播放的代码相似，所以建立了一个函数来提高代码的重用率!!!
			 */
			private void setMusicRelativeHandle() {
				
				mediaPlayer.reset(); // 重置mediaPlayer，因为下面运行出错，没有这行的话，出错了我才想到要重新设置mediaPlayer!!!
				//mediaPlayer.release();         //释放资源
				musicUrl = musicUrlList.get(musicPosition + 1);
				musicName = musicTitleList.get(musicPosition + 1);
				musicDuration = musicDurationList.get(musicPosition + 1);

				musicPosition = musicPosition + 1;

				initMediaPlayer(); // 准备播放下一首音乐

				musicTitle.setText(musicName);
				musicProgressBar.setMax(mediaPlayer.getDuration());   //设置音乐进度条的最大长度
				endTime.setText(sDateFormat.format(new Date(mediaPlayer.getDuration())));  //设置音乐总时长
				
				musicLyric.setText("");                   //先清除原有的文本,即“歌词加载中....”
				//musicSingleLyric.setText("");             //先清除原有的单句文本, //这个在12月9日晚上19:26改了
				
				//获取下一首的歌词，下面这个函数会进行一系列的操作
				FindMusicFromInternet findMusicFromInternet = new FindMusicFromInternet(musicTitleList.get(musicPosition)
						+ musicSingerList.get(musicPosition));   //直接调用这个，从网络获取歌词id就会显示歌词了!
				findMusicFromInternet.getMusicIdFromInternet();
				
				if(!mediaPlayer.isPlaying())           //这个在12月9日晚上19:26添加了，为了防止网络慢。歌曲没开始播放
				{
					mediaPlayer.start(); // 开始播放音乐!!!
				}
				// 传递参数，是为了更新musicPosition的位置，musicTitleList貌似多余了，MyMusicFragment调用的话就不多余
				musicBinder.changeMusicTitle(musicTitleList, musicPosition); // 按顺序播放到下一首歌曲就修改前台服务的音乐名称!!!
			}
		});
	}
	
	/**
	 * 当在DataDipoe类中，解析完歌词后，就调用这哥函数来显示歌词
	 * @param musicLyric             DataDipose传递来的一首歌的歌词
	 * @param musicLyricTime         DataDipose传递来的一首歌的歌词的时间轴
	 */
	public static void setMusicLric(ArrayList<String> musicLyricList1 , ArrayList<String> musicLyricTimeList1)
	{
		musicLyricList = new ArrayList<String>();
		musicLyricTimeList = new ArrayList<String>();
		
		musicLyricList = musicLyricList1;
		musicLyricTimeList = musicLyricTimeList1;
		
		musicLyric.setText("");                   //先清除原有的文本,即“歌词加载中....”
		musicSingleLyric.setText("");             //先清除原有的单句文本,
		
		for(String lyric : musicLyricList)
		{
	    	musicLyric.setText(musicLyric.getText() + lyric + "\n"); //因为musicLyricList中的元素（歌词）有些是连着的，输出的时候会挤在一起，没有换行，所以我加了一个换行
		}
		
		mediaPlayer.start();                        //当加载完歌词后， 开始播放音乐
		//当加载完歌词后，传递歌词给musicSeekBarThread线程，用于与音乐同步
		musicSeekBarThread.setMusicLyricList(musicLyricList);           
		musicSeekBarThread.setMusicLyricTimeList(musicLyricTimeList);

	}
	
	/**
	 * 当FindMusicFromInternet类中，从网上获取歌曲id失败，则调用此方法!!!
	 */
	public static void setMusicLricIsNone()
	{
		musicLyricList = new ArrayList<String>();
		musicLyricTimeList = new ArrayList<String>();
		
		musicLyric.setText("");                       //先清除原有的文本,即“歌词加载中....”
		musicLyric.setText("查询歌词失败");
		
		mediaPlayer.start();                          //当加载不出歌词也 开始播放音乐

	}

	
	/**
	 * 启动音乐进度条的子线程，用于改变进度条进度，以及改变当前播放时间!!!
	 */
	private static void startMusicSeekBarThread() {
		
		Context mContext = MyApplication.getContext();
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		Boolean networkIsAvaliable = false;
		if(networkInfo != null && networkInfo.isAvailable())
		{
			networkIsAvaliable = true;
		}
		
		if(thread != null)
		{
	      	if(thread.isAlive())//如果先前已经开启过子线程，那么为了使seekBar只受到一个线程的控制，所以要kill掉以前开启过的线程!!!
       		{    //通过查网上资料得知，强行中断子线程有危险，所以我设置isPlay为false，让子线程提前终止!突发奇想!!!
      			musicSeekBarThread.setIsPlay(false);  
	     	}
		}               //与第一次执行相比                                               //true     //不变                    //不变                 //0（不变 //传递一首歌的时间轴
		musicSeekBarThread = new MusicSeekBarThread(isPlay , mediaPlayer ,handler ,time );  //最后一个参数:传递一首歌歌词
	    thread = new Thread(musicSeekBarThread);                           //开启子线程
	    musicSeekBarThread.setNetworkIsAvailable(networkIsAvaliable);      //网路是否可用，传给子线程，用来是否进行歌词同步，没网络则不进行同步!!!
		thread.start();
	}
	
	/**
	 * 在FindMusicFrmoInternet类中获取歌词失败就调用这个函数
	 */
/*	public static void setGetLyricResult()
	{
		musicLyric.setText("无法获取歌词");
	}*/
	
	/**
	 * 在DataDipose解析完歌词后就调用此函数
	 */
	public static void cancelProgressDialog()
	{
        if(progressDialog != null)             //防止不联网时，progressDialog出错
        {
    		progressDialog.cancel();
        }
	}
	
	
	public static void setSingleLyricText()
	{
		if(musicSingleLyric != null)     //防止不联网时，musicSingleLyric出错
		{
     		musicSingleLyric.setText("获取歌词失败");
		}
	}

    /**
     * 用于当歌词获取失败的时候，还能继续播放音乐
     */
	public static void startMusic()
	{
		if(mediaPlayer != null && !mediaPlayer.isPlaying())
		{
			mediaPlayer.start();
		}
	}

	// 当 当前activity被销毁的时候   no bug
	@Override
	protected void onDestroy() {
		/*    中断线程，在退出activity时，不然程序会报错!!!这个没有结束完线程，如果结束完就不会出现
		*  java.lang.IllegalStateException异常了，需要配合子线程中的isPlay使用(在if判断中加入isPlay)，
		*  来保证退出PlayMusicActivity时不出错!!!可能是执行完了super.onDestroy()，就正好运行到if判断句，
		*  执行了mediaPlayer。我的猜想是正确的，主线程没有给子线程足够的时间退出就执行了super.onDestroy()!!!
		*/
		if(thread != null)      
		{
			if(thread.isAlive())
			{
				musicSeekBarThread.setIsPlay(false);         //人为中断线程!!!good job!
			}
		}
		

		// 当活动结束的时候，停止播放音乐，并且释放有关资源
		super.onDestroy();
	
		
		if (mediaPlayer != null) {
			mediaPlayer.stop();           // 停止播放
			//当Mediaplayer对象不再被使用时，最好调用release（）方法对其进行释放，使其处于结束状态，此时它不能被使用
			mediaPlayer.release();//释放与mediaPlayer相关的资源 ， 释放后一定要设置为null// Set the MediaPlayer to null to avoid IlLegalStateException 
		   mediaPlayer = null;
		}
		
		if(serviceConnection != null && serviceIntent != null)
		{
	    	unbindService(serviceConnection);   //解绑service
       		stopService(serviceIntent);         //停止服务
		}
	}

	@Override
	public void finish() // 当退出activity的时候就调用，或者活动应该关闭的时候也会调用!!!
	{
		super.finish();
		// 设置退出时的动画
		overridePendingTransition(R.animator.from_left_in, R.animator.toward_right_out);
	}
	
	public static void slowDecreaseVolume()           //在按下暂停按钮或者退出音乐界面时,慢慢降低声音!!!
	{
		new Thread(new Runnable() {            //开启子线程，用来在退出PlayMusicActivity时，慢慢降低音乐声音
			 
			@Override
			public void run() {
				
				for(float i = 0.1f; i<=1.0; i += 0.1)
				{
	    			mediaPlayer.setVolume(1-i , 1-i);        //设置音量，左声道和右声道
	    			try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		
		try {                    //给声音足够的时间慢慢降低!!! 运行这么多时间才执行下面的onBack()
			Thread.sleep(150);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		// 当由左往右滑动的时候
		if (e1.getX() - e2.getX() < -120) {
			
			slowDecreaseVolume();      //慢慢降低声音 ， 在滑动退出的时候         //在点击播放按钮的话，没这个效果，需要查一下当前的音量是什么，有时间再做吧!!!!
			
			this.onBackPressed(); // 相当于按下了back键
	
			//如果是从MyMusicActivity打开的PlayMusicActivity则退出时，返回到MyMusicActivity。
			//如果是从MusicTopActivity打开的PlayMusicActivity则退出时，返回到MusicTopActivity。
			if(activity.equals("MyMusicFragment"))
           {
		    	Intent intent = new Intent(PlayMusicActivity.this, MyMusicActivity.class);
	     		startActivity(intent);
	     		MyMusicActivity.isOpen = 1;
            }
           else if(activity.equals("MusicTopFragment"))
           {
        	   Intent intent = new Intent(PlayMusicActivity.this, MusicTopActivity.class);
	     		startActivity(intent);
	     		MusicTopActivity.isOpen = 1;
           }
		}
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return this.gestureDetector.onTouchEvent(event);
	}

}