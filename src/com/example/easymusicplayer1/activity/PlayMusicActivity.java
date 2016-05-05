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
 * �������ֵĽ��棬����ʵ��ʣ��Լ����Է������ֵ����ص�ַ���Լ���������ͬ��
 * @author feng
 *
 */
public class PlayMusicActivity extends Activity implements OnGestureListener
{
    public static int isOpen = 0;
    
	static TextView musicTitle;

	// ImageView musicImageView;

	static ImageView play_pause;       // �����š�ͼƬ

	ImageView playlist;                       // ��ʾ�����б�ġ�ͼƬ��
	
	static SeekBar musicProgressBar;          //���ֲ��Ž�����,seekBar Ϊ���϶�������!
	
	static TextView startTime;                //���ֽ�����ǰ��ĵ�ǰ����ʱ��
	
	static TextView endTime;                  //������ʱ��
	
	static TextView musicLyric;               //���ָ��
	
	static TextView musicSingleLyric;                //���ֵ��и��
	
	static SimpleDateFormat sDateFormat;
	
	int progress;
	
	static int time = 0; 
	
	static Boolean isPlay = true;
	
	public  static final int MUSIC_PROGRESS = 1;
	
	static MusicSeekBarThread musicSeekBarThread;                   //���ֽ����������߳�
	
	static Thread thread = null;                                           //�����ﴴ��������kill��

	static Boolean play = false;       // ������¼��ǰ��ͼƬ�ǡ����š�ͼƬ��������ͣ��ͼƬ��
	
	static Boolean playOnce = false;      //������ֵ�ǻᱨ��ģ�������if�жϾ���

	static MediaPlayer mediaPlayer = null;     
	
	Intent intent; // ���ڻ�ȡ��MyMusicFragment�е�intent

	static String musicName;

	static String musicUrl;
	
	private static String activity;                //�洢���ĸ�activity�򿪵�PlayMusicActivity
	
	static int musicDuration;

	private GestureDetector gestureDetector;

	static int musicPosition; // ������ListView��item�ϵ�λ�ã���MainActivity���ݹ������������ڽ���˳�򲥷�!!!

	private static ArrayList<String> musicTitleList; // �洢��������

	private static ArrayList<String> musicUrlList; // �洢�����ļ���·��

	private static ArrayList<Integer> musicDurationList; // �洢���ֵĲ���ʱ��
	
	private static ArrayList<String> musicLyricList;            //�洢һ�׸�ĸ��
	
	private static ArrayList<String> musicLyricTimeList;        //�洢һ�׸�ĸ�ʵ�ʱ����
	
	private static ArrayList<String> musicSingerList;           //�洢����
	
	private static String playOrder; // ÿ�δ�MyMusicFragmentѡ��ʱ �� ��ȡ����˳��
												// , Ҫ��Ҫ���Ǹ�һ�£��ĳɾ�̬����������new
												// һ�����������ú������أ���
	ActionBar actionBar;
	
	ShareActionProvider mShareActionProvider;
	
	static ProgressDialog progressDialog;

	private static MusicForegroundService1.MusicBinder musicBinder;      //����ͨ��musicBinderʵ������service�еĺ�������service���в���

	Intent serviceIntent = null; // ���ڰ󶨷���

	ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {

			musicBinder = (MusicBinder) service; // ��ȡ����service�Ķ���
		}
	};
	
	
	static Handler handler = new Handler() {
		
		@SuppressWarnings("deprecation")
		@Override
		public void handleMessage(Message msg) {
		super.handleMessage(msg);
		
	    int currentPosition = msg.arg1;               //�����̴߳�����ǰseekBar��λ��
		String currentTime1 = (String) msg.obj;       //�����̴߳���ǰ�����ֲ���λ��
		
	    switch(msg.what)
		{
			case MUSIC_PROGRESS:
		    		musicProgressBar.setProgress(currentPosition);     //�ı䵱ǰ��������λ��
		    		startTime.setText(currentTime1);                   //�ı�starttiem������
		    		Bundle bundle = msg.getData();
		    		String singleLyric = (String) bundle.get("single_lyric");
		    		if(singleLyric != null)          //��һ��ִ��MusicSeekBarThrea�߳�ʱ�ǿյģ����ֵ�����������ж�!
		    		{
		        		musicSingleLyric.setText(singleLyric);
		    		}
		    		
		    		//��2015��12��9��19:41�޸�
		    		//ʱ��ͬ��������ʣ���ֹ��3G�����£�������һ��ʱ���ڻ�û���ü���ȡ��ʾͰ���seekBar�������ʺ���ͬ�������ˣ����Լ����
		    		int index = synchronizationLyric(currentPosition);         
					if(index != 1)          //����ʴ���ʱ����ȡ���˸�ʣ��Ž���ͬ�����
					{
					    musicSeekBarThread.setLyricIndex(index);                      //ͬ�����
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
		
		initView();                //��,��ȡ�ؼ�,�Լ��ؼ��ļ����¼�

		
		Boolean isConnect = isConnectInternet();                         //�жϵ�ǰ�Ƿ����������!!!
		if(isConnect)                                                    //��������˾���ʾprogressBar
		{
    		setProgressDialog();
		}
		else
		{
			setSingleLyricText();                 //���û�ȡ���ʧ�ܵ���ʾ
			cancelProgressDialog();               //�رյ�progressDialog
			startMusic();                         //��ʼ��������
		}
    
		initActionBar();
        
		// ��ȡ����ʵ��
		gestureDetector = new GestureDetector(PlayMusicActivity.this);

		readDataFromActivity(); // �� ������PlayMusicActivity���Ļ ��ȡ������������


		initMediaPlayer();            // ׼���ò���
		
		setMusicDateInUI();          //���ø�ʣ�seekBar������ʱ��

		if(!activity.equals("MusicTopFragment"))         //����Ǵ�����֮�Ҳ��ŵ����֣���ô�Ͳ�����ʾǰ̨������!!!
		{
			Log.e("PlayMusicActivity", "�󶨷���");
	    	// ��MusicForegroundService1�� ��Ϊ�˿����ڸ�����˳�򲥷ţ�������ѭ�����ţ��������������ʱ�����Ըı�ǰ̨����ĸ�������
    		serviceIntent = new Intent(PlayMusicActivity.this, MusicForegroundService1.class);
    		bindService(serviceIntent, serviceConnection, 0);                
		}
	}
	
	
	/**
	 * �����жϵ�ǰ�������Ƿ����!
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

		progressDialog = new ProgressDialog(PlayMusicActivity.this , 1);           //����Ĳ����Ƿ��1�ȽϺÿ�
		progressDialog.setMessage("���ݼ�����");
		progressDialog.setCancelable(false);                       //����dialog����ͨ����back�����أ���ֹbug���֣����ڸ�ʻ�û�м��س���֮ǰ���˳���activity�Ļ����ڰ���һ�׸���ʱ����ʲ�ƥ�䣡����
		progressDialog.show();
	
	}



	private void setMusicDateInUI() {
		musicTitle.setText(musicName);
		
		endTime.setText(sDateFormat.format(new Date(mediaPlayer.getDuration()))); //���ø������ܲ���ʱ��
		musicProgressBar.setMax(mediaPlayer.getDuration());                 //����seekBar����ʱ��        
		
	}


	

	/**
	 * ActionBar���������
	 */
    private void initActionBar() {
    	actionBar = getActionBar();
    	actionBar.setTitle("��������");
		actionBar.setDisplayShowHomeEnabled(false);        //����ActionBar����ͼ�겻�ɼ�
		actionBar.setDisplayShowTitleEnabled(false);       //����ActionBar���ı��ⲻ�ɼ�
		actionBar.setDisplayHomeAsUpEnabled(true);         //����ActionBar���ķ�����һ����ͼ����ʾ����!!!
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
        this.getMenuInflater().inflate(R.menu.play_music_activity , menu);
        
        MenuItem menuItem = menu.findItem(R.id.share_action);
        mShareActionProvider = (ShareActionProvider) menuItem.getActionProvider();
        
        FindMusicFromInternet.setShareActionProvider(mShareActionProvider);    //��FindMusicFromInternet�ഫ��ʵ����Ϊ�ˣ������ݻ�ȡ��ɺ����ֱ������shareIntent,��Ϊ����������shareIntent�Ļ�����ֱ�Ӿ͵����ˣ����ݻ�û���ü���ȡ��!
        
        return true;
    }
    

    public static Intent getDefaultIntent(String data) {
    	
    	String shareData = data;
    	Intent intent = new Intent(Intent.ACTION_SEND);
       
        intent.putExtra(Intent.EXTRA_TEXT ,"���������� : " + musicTitleList.get(musicPosition) + "  " + 
                        musicSingerList.get(musicPosition) + "\n" + "�뵽 " + shareData + " ���ء�"
                        + "\n" + "       ������������MusicTree");

    	intent.setType("text/plain");                        //ָ�����������Ϊ��ͨ�ı�����
    	
    	return intent;
	}
    


	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId())
    	{
    	case android.R.id.home:               //��ActionBar��෵��ͼ�������¼�
    		finish();                         //����activity
			//����Ǵ�MyMusicActivity�򿪵�PlayMusicActivity���˳�ʱ�����ص�MyMusicActivity��
			//����Ǵ�MusicTopActivity�򿪵�PlayMusicActivity���˳�ʱ�����ص�MusicTopActivity��
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
     * ��,��ȡ�ؼ�,�Լ��ؼ��ļ����¼�
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
			
			setPlayAndPauseOnClickListener();               //���ò��ź���ͣ��ť�ļ����¼�!!!
			setSeekBarOnDragListener();                     //�������ֵĲ��Ž��������϶�ʱ���ߵ��ʱ�ļ����¼�!!!
	
	}

    /**
     * �������ֵĲ��Ž��������϶�ʱ�ļ����¼�!!!
     */
    private void setSeekBarOnDragListener() {
		
    	musicProgressBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser)
				{
					startTime.setText(sDateFormat.format(new Date(seekBar.getProgress())));    //����ͣ�϶���ʱ�򣬰�ʱ�����õ���ǰλ�ã�
				}
		
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int currentProgress = seekBar.getProgress();
				mediaPlayer.seekTo(currentProgress);                //��ֹͣ�϶��������󣬲��ŵ�ǰ���ȵ�����
				musicSeekBarThread.setTime(currentProgress);        //�����̸߳���ʱ�䣬ʹ�õ�starttime������ȷ��ʾ
                
				musicSingleLyric.setText("");                                  //ʹ������Ϊ�հף�����ʾ����ȷ�ĸ��֮ǰ
				int index1 = synchronizationLyric(currentProgress);            //ͬ�����
				if(index1 != 1)          //����ʴ���ʱ����ȡ���˸�ʣ��Ž���ͬ�����
				{
				    musicSeekBarThread.setLyricIndex(index1);                      //ͬ�����
				}
			}

	   });
    	
	}

	/**
	 * �����ƶ�seekBar��Ҳ��ͬ����� ,  �������½���һ���࣬����currentProgress �� musicSeekBarThread��
	 * �Լ�musicLyricTime���� ��������
	 */
	public static int synchronizationLyric(int currentProgress) {
		
		int index = 0; 
		
		if (musicLyricTimeList != null) {
			// ����������ǰ�ĸ�������ĸ�λ��
			for (String timeStr : musicLyricTimeList) {
				int time = (int) musicSeekBarThread.changeTimeFormat(timeStr); // ����ʱ���ʽ����Ϊms
				if ((currentProgress - time) < 0) // ��ȡ�뵱ǰ��������ĸ�� ��
													// ����������MusicSeekBarThread�̶߳���ͬ���ģ�ֻ��ʱ��ľ�ȷ���������
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
     * ���ò��ź���ͣ��ť�ļ����¼�������������ǰ̨����Ĳ��ź���ͣ��ť����ͬ��!!!
     */
	private void setPlayAndPauseOnClickListener() {
		play_pause.setOnClickListener(new OnClickListener() { // ���õ��ͼƬʱ��ļ����¼�
			//�����ˣ�ò��Ҳû��bug�����廹Ҫ����һ�²�֪��!!!
			@Override
			public void onClick(View v) {
				if (play == true) {
					play_pause.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp); // ���������ͼƬ����ͼƬ�û�Ϊ����ͣ��ͼƬ
					play = false; // ��ǵ�ǰ��ͼƬΪ����ͣ��ͼƬ
					
					if (!mediaPlayer.isPlaying()) {
						mediaPlayer.start();
					}

					if(!activity.equals("MusicTopFragment"))          //������Ǵ�����֮����������activity��ִ��ǰ̨����ť��ͬ������
					{
						musicBinder.changeMusicPlayOrPausePic(play); // ǰ̨����ͼƬ ��
					}
					
					if(playOnce)     //�����ڲ��ŷ�ʽΪ��ֻ����һ�Ρ�������£�������һ�׸�������ٴβ���ʱ���Կ���startTime�Լ�seekBar����
					{
						startMusicSeekBarThread();         //�������̣߳�������startTime�Լ�seekBar�ĸ���
						musicSeekBarThread.setMusicLyricList(musicLyricList);           //�����ǵ���ѭ�������ݲ���
						musicSeekBarThread.setMusicLyricTimeList(musicLyricTimeList);
						playOnce = false;                  //���Ϊfalse����˼�ǵ��´ΰ��²��Ű�ť��ʱ�򣬲�Ҫ�ٽ��뵽�����������߳���
					}
				} else if (play == false) {
					play_pause.setImageResource(R.drawable.ic_play_circle_outline_white_48dp); // �������ͣͼƬ����ͼƬ�û�Ϊ�����š�ͼƬ
					play = true; // ��ǵ�ǰ��ͼƬΪ�����š�ͼƬ

					//slowDecreaseVolume();             //�ڵ�����Ű�ťʱ������������������!!!

					if (mediaPlayer.isPlaying()) // ����������ڲ��ţ�����ͣ����
					{
						mediaPlayer.pause();
					}

					if(!activity.equals("MusicTopFragment"))          //������Ǵ�����֮����������activity��ִ��ǰ̨����ť��ͬ������
					{
		     			musicBinder.changeMusicPlayOrPausePic(play); // ǰ̨����ͼƬ ��PlayMusicActivity�еĲ���ͼƬͬ��
					}
				}
			}
		});		
	}



	public static  int playLastMusic()
	 {
		if (musicPosition - 1 <= 0) // ���Ŀǰ�ڲ��ŵ�һ�׸裬�����һ�׵Ļ�����ô�Ͳ������һ��
		{
			musicPosition = musicTitleList.size();
		}

		mediaPlayer.reset();                   // ����mediaPlayer����Ϊ�������г���û�����еĻ����������Ҳ��뵽Ҫ��������mediaPlayer!!!
		musicUrl = musicUrlList.get(musicPosition - 1);
		musicName = musicTitleList.get(musicPosition - 1);

		musicPosition -= 1;

		initMediaPlayer(); // ׼��������һ������
		
		musicDuration = mediaPlayer.getDuration();      //musicDurationList.get(musicPosition - 1);  //����Ϊ�գ����Ի�������ʽ!!!

		
		musicTitle.setText(musicName);
		musicProgressBar.setMax(mediaPlayer.getDuration());   //�������ֽ���������󳤶�
		endTime.setText(sDateFormat.format(new Date(mediaPlayer.getDuration())));  //����������ʱ��
		
		// ���úò��ŵ�ͼƬ�����ԭ��Ϊ����ͼƬ ����ô�������������Ļ����ͻ����ͼƬ���ң����ź���ͣ�����޷���ȷʵ��
		play = false;
		play_pause.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
		
		musicLyric.setText("");
		musicSingleLyric.setText("");
		musicSingleLyric.setText("��ȡ�����...");

		//��ȡ��һ�׵ĸ�ʣ�����������������һϵ�еĲ���
		FindMusicFromInternet findMusicFromInternet = new FindMusicFromInternet(musicTitleList.get(musicPosition)
				+ musicSingerList.get(musicPosition));   //ֱ�ӵ���������������ȡ���id�ͻ���ʾ�����!
		findMusicFromInternet.getMusicIdFromInternet();
		//mediaPlayer.start(); // ��ʼ��������!!!

		return musicPosition;         //����musicPosition��ǰ̨���������ı䵱ǰ����������
	}
	
	
	public static int playNextMusic() // ������һ�׸�
	{
		if (musicPosition == musicTitleList.size() - 1) // ���Ŀǰ�ڲ��ŵ�һ�׸裬�����һ�׵Ļ�����ô�Ͳ������һ��
		{
			musicPosition = -1;
		}

		mediaPlayer.reset(); // ����mediaPlayer����Ϊ�������г���û�����еĻ����������Ҳ��뵽Ҫ��������mediaPlayer!!!
		musicUrl = musicUrlList.get(musicPosition + 1);
		musicName = musicTitleList.get(musicPosition + 1);

		musicPosition += 1;

		initMediaPlayer(); // ׼��������һ������
		
		musicDuration = mediaPlayer.getDuration();        //musicDurationList.get(musicPosition + 1); Ϊ�գ����Ի���һ��
		
		musicTitle.setText(musicName);                         //�������ֱ���
		musicProgressBar.setMax(mediaPlayer.getDuration());   //�������ֽ���������󳤶�
		endTime.setText(sDateFormat.format(new Date(mediaPlayer.getDuration())));  //����������ʱ��
		
		

		// ���úò��ŵ�ͼƬ�����ԭ��Ϊ����ͼƬ ����ô�������������Ļ����ͻ����ͼƬ���ң����ź���ͣ�����޷���ȷʵ��
		play = false;
		play_pause.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
		
		
		musicLyric.setText("");
		musicSingleLyric.setText("");
		musicSingleLyric.setText("��ȡ�����...");

		
		//��ȡ��һ�׵ĸ�ʣ�����������������һϵ�еĲ���
		FindMusicFromInternet findMusicFromInternet = new FindMusicFromInternet(musicTitleList.get(musicPosition)
						+ musicSingerList.get(musicPosition));   //ֱ�ӵ���������������ȡ���id�ͻ���ʾ�����!
		findMusicFromInternet.getMusicIdFromInternet();
		//mediaPlayer.start(); // ��ʼ��������!!!

		return musicPosition;         //����musicPosition��ǰ̨���������ı䵱ǰ����������
	}

	public static void playOrPauseMusic() // ������ͣ���߲�������
	{
		Log.e("PlayMusicActivity", "���!!");

		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			play_pause.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);
			play = true;
		} else if (!mediaPlayer.isPlaying()) {
			Log.e("PlayMusicActivity", "���!!");
			mediaPlayer.start();
			play_pause.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
			play = false;
		}
	}

	// no bug
	public void readDataFromActivity() // ������PlayMusicActivity��Activity�ж�ȡ����
	{
		// ��ȡ��MainActivity����������
		intent = getIntent();                       // һ��Ҫ�ں��������ʼ��!!!�����ʼ���Ͳ�һ���ˣ����������!!!
		activity = intent.getStringExtra("fragment");
		
		if (activity.equals("MyMusicFragment"))
		{
			// �� ������PlayMusicActivity���Ļ ��ȡ������������
			Bundle bundle = intent.getExtras();
			musicName = bundle.getString("music_title");
			musicUrl = intent.getStringExtra("music_url");
			musicDuration = intent.getIntExtra("music_duration", 0);
			musicPosition = intent.getIntExtra("music_title_position", 0);
			musicTitleList = intent.getStringArrayListExtra("music_title_list");
			musicUrlList = intent.getStringArrayListExtra("music_url_list");
			musicSingerList = intent.getStringArrayListExtra("music_singer");
			musicDurationList = intent.getIntegerArrayListExtra("music_duration_list");
			playOrder = intent.getStringExtra("music_play_order");         //��ȡ����˳��!!!
		}
		else if (activity.equals("MusicTopFragment"))           //�ӡ����ֹݴ��������ݡ�
		{
			musicName = intent.getStringExtra("music_name");
			musicUrl = intent.getStringExtra("music_url");
			musicTitleList = intent.getStringArrayListExtra("music_title_list");
			musicUrlList = intent.getStringArrayListExtra("music_url_list");
			musicSingerList = intent.getStringArrayListExtra("music_singer");
			playOrder = "ONCE_PLAY";             //��������߲��ţ���ôֻ����һ�Σ�����������ʧ!
		}
		
	}

	
	/**
	 *  1.׼���ò������� , �Լ��������֣����м��������Ƿ񲥷���ɣ�����ѡ�񲥷ŷ�ʽ
	 *  2.��������һ�׸�󣬲�����һ�׸裬Ҳ���������������������ô���߳�Ҳ����������!!!
	 */
	// no bug 
	public static void initMediaPlayer() {
		
		try {
		   if(mediaPlayer == null)
			{
				mediaPlayer = new MediaPlayer();
			}
			mediaPlayer.setDataSource(musicUrl);    // ָ����Ƶ�ļ���·��
			mediaPlayer.prepare();                  // ��MediaPlayer���뵽׼��״̬
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	/*	if(mediaPlayer != null)
    		Log.e("MainActivity" , mediaPlayer.toString());*/
		
		//mediaPlayer.start(); // ��ʼ��������            ��Ҫһ��ʼ�Ͳ������֣�����ʼ�����ſ�ʼ��������!���߼��ز������Ҳ��ʼ����
		
		startMusicSeekBarThread();
	
		
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() { // ���������Ƿ񲥷Ž���
			//�Ѿ�����һ�Σ�ò��û��bug�ˣ�����Ļ�Ҫ����һ�²�֪��!!!
			@Override
			public void onCompletion(MediaPlayer mp) {

				//����ǰ��ִ�е�ԭ���ǣ�����endTime��TextView���ȱ仯����������仯���Եò����ۣ������Ƚ��н�������0
				//������ʲô���ŷ�ʽ��ѭ��Ҳ�ã�ֻ����һ��Ҳ�ã������������������!!!
				musicProgressBar.setProgress(0);                          //���������¿�ʼ
				startTime.setText("00:00");                           //�����������ֿ�ʼ��ʱ��

				try {
					Thread.sleep(1000);         // �������������ͣһ��,���ܻᵼ�½��濨��,���¶����ӳ�
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Log.e("PlayMusicActivity", playOrder);
				if (playOrder.equals("SINGLE_CIRCLE"))   // ��������˵���ѭ�� 
				{
					musicSingleLyric.setText("");                 //�����ı�Ϊ��,�����²���ʱ!
					startMusicSeekBarThread();
					musicSeekBarThread.setMusicLyricList(musicLyricList);           //�����ǵ���ѭ�������ݲ���
					musicSeekBarThread.setMusicLyricTimeList(musicLyricTimeList);
					mediaPlayer.start(); // ��ֱ�Ӽ���������
				} 
				else if (playOrder.equals("ORDER_PLAY")) // �����һ�ײ��ܲ��ŵ�������һ�����ܲ��ŵ������ļ����൱�ڣ�������ϣ�����ǰ̨������PlayMusicActivity��ͬ���仯��������!!!
				{
					if (musicPosition == musicTitleList.size() - 1) // ������ŵ������һ�ף������´ӵ�һ�׸貥��!!!
					{
						musicPosition = -1;
					}
					
					musicSingleLyric.setText("��ʼ�����...");  
					Log.e("PlayMusicActivity", playOrder + "����");

					setMusicRelativeHandle();             //���ò�����һ�����ֵ��������
				} 
				else if (playOrder.equals("RANDOM_PLAY")) {
					musicPosition = (int) (Math.random() * (musicTitleList.size() - 2));

		            setMusicRelativeHandle();             //���ò�����һ�����ֵ��������

				} 
				else if (playOrder.equals("ONCE_PLAY")) {
					// ֻ����һ�Σ�������󲻽����κβ���!!!
					play = true;
					play_pause.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);
					if(!activity.equals("MusicTopFragment"))          //������Ǵ�����֮����������activity��ִ��ǰ̨����ť��ͬ������
					{
			    		musicBinder.changeMusicPlayOrPausePic(play);//���Ӧ�ĸı�ǰ̨����Ĳ��Ż�����ͣ��ť
					}
					playOnce = true;   //��������ڲ��ŷ�ʽΪ��ֻ����һ�Ρ��������,��������󣬵��ٴε����ť����ʱ��������������seekBar
					//��������������
					musicSingleLyric.setText("");                 //�����ı�Ϊ��,�����²���ʱ!
				}

			}

			/**
			 * ����˳�򲥷���������ŵĴ������ƣ����Խ�����һ����������ߴ����������!!!
			 */
			private void setMusicRelativeHandle() {
				
				mediaPlayer.reset(); // ����mediaPlayer����Ϊ�������г���û�����еĻ����������Ҳ��뵽Ҫ��������mediaPlayer!!!
				//mediaPlayer.release();         //�ͷ���Դ
				musicUrl = musicUrlList.get(musicPosition + 1);
				musicName = musicTitleList.get(musicPosition + 1);
				musicDuration = musicDurationList.get(musicPosition + 1);

				musicPosition = musicPosition + 1;

				initMediaPlayer(); // ׼��������һ������

				musicTitle.setText(musicName);
				musicProgressBar.setMax(mediaPlayer.getDuration());   //�������ֽ���������󳤶�
				endTime.setText(sDateFormat.format(new Date(mediaPlayer.getDuration())));  //����������ʱ��
				
				musicLyric.setText("");                   //�����ԭ�е��ı�,������ʼ�����....��
				//musicSingleLyric.setText("");             //�����ԭ�еĵ����ı�, //�����12��9������19:26����
				
				//��ȡ��һ�׵ĸ�ʣ�����������������һϵ�еĲ���
				FindMusicFromInternet findMusicFromInternet = new FindMusicFromInternet(musicTitleList.get(musicPosition)
						+ musicSingerList.get(musicPosition));   //ֱ�ӵ���������������ȡ���id�ͻ���ʾ�����!
				findMusicFromInternet.getMusicIdFromInternet();
				
				if(!mediaPlayer.isPlaying())           //�����12��9������19:26����ˣ�Ϊ�˷�ֹ������������û��ʼ����
				{
					mediaPlayer.start(); // ��ʼ��������!!!
				}
				// ���ݲ�������Ϊ�˸���musicPosition��λ�ã�musicTitleListò�ƶ����ˣ�MyMusicFragment���õĻ��Ͳ�����
				musicBinder.changeMusicTitle(musicTitleList, musicPosition); // ��˳�򲥷ŵ���һ�׸������޸�ǰ̨�������������!!!
			}
		});
	}
	
	/**
	 * ����DataDipoe���У��������ʺ󣬾͵�����纯������ʾ���
	 * @param musicLyric             DataDipose��������һ�׸�ĸ��
	 * @param musicLyricTime         DataDipose��������һ�׸�ĸ�ʵ�ʱ����
	 */
	public static void setMusicLric(ArrayList<String> musicLyricList1 , ArrayList<String> musicLyricTimeList1)
	{
		musicLyricList = new ArrayList<String>();
		musicLyricTimeList = new ArrayList<String>();
		
		musicLyricList = musicLyricList1;
		musicLyricTimeList = musicLyricTimeList1;
		
		musicLyric.setText("");                   //�����ԭ�е��ı�,������ʼ�����....��
		musicSingleLyric.setText("");             //�����ԭ�еĵ����ı�,
		
		for(String lyric : musicLyricList)
		{
	    	musicLyric.setText(musicLyric.getText() + lyric + "\n"); //��ΪmusicLyricList�е�Ԫ�أ���ʣ���Щ�����ŵģ������ʱ��ἷ��һ��û�л��У������Ҽ���һ������
		}
		
		mediaPlayer.start();                        //���������ʺ� ��ʼ��������
		//���������ʺ󣬴��ݸ�ʸ�musicSeekBarThread�̣߳�����������ͬ��
		musicSeekBarThread.setMusicLyricList(musicLyricList);           
		musicSeekBarThread.setMusicLyricTimeList(musicLyricTimeList);

	}
	
	/**
	 * ��FindMusicFromInternet���У������ϻ�ȡ����idʧ�ܣ�����ô˷���!!!
	 */
	public static void setMusicLricIsNone()
	{
		musicLyricList = new ArrayList<String>();
		musicLyricTimeList = new ArrayList<String>();
		
		musicLyric.setText("");                       //�����ԭ�е��ı�,������ʼ�����....��
		musicLyric.setText("��ѯ���ʧ��");
		
		mediaPlayer.start();                          //�����ز������Ҳ ��ʼ��������

	}

	
	/**
	 * �������ֽ����������̣߳����ڸı���������ȣ��Լ��ı䵱ǰ����ʱ��!!!
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
	      	if(thread.isAlive())//�����ǰ�Ѿ����������̣߳���ôΪ��ʹseekBarֻ�ܵ�һ���̵߳Ŀ��ƣ�����Ҫkill����ǰ���������߳�!!!
       		{    //ͨ�����������ϵ�֪��ǿ���ж����߳���Σ�գ�����������isPlayΪfalse�������߳���ǰ��ֹ!ͻ������!!!
      			musicSeekBarThread.setIsPlay(false);  
	     	}
		}               //���һ��ִ�����                                               //true     //����                    //����                 //0������ //����һ�׸��ʱ����
		musicSeekBarThread = new MusicSeekBarThread(isPlay , mediaPlayer ,handler ,time );  //���һ������:����һ�׸���
	    thread = new Thread(musicSeekBarThread);                           //�������߳�
	    musicSeekBarThread.setNetworkIsAvailable(networkIsAvaliable);      //��·�Ƿ���ã��������̣߳������Ƿ���и��ͬ����û�����򲻽���ͬ��!!!
		thread.start();
	}
	
	/**
	 * ��FindMusicFrmoInternet���л�ȡ���ʧ�ܾ͵����������
	 */
/*	public static void setGetLyricResult()
	{
		musicLyric.setText("�޷���ȡ���");
	}*/
	
	/**
	 * ��DataDipose�������ʺ�͵��ô˺���
	 */
	public static void cancelProgressDialog()
	{
        if(progressDialog != null)             //��ֹ������ʱ��progressDialog����
        {
    		progressDialog.cancel();
        }
	}
	
	
	public static void setSingleLyricText()
	{
		if(musicSingleLyric != null)     //��ֹ������ʱ��musicSingleLyric����
		{
     		musicSingleLyric.setText("��ȡ���ʧ��");
		}
	}

    /**
     * ���ڵ���ʻ�ȡʧ�ܵ�ʱ�򣬻��ܼ�����������
     */
	public static void startMusic()
	{
		if(mediaPlayer != null && !mediaPlayer.isPlaying())
		{
			mediaPlayer.start();
		}
	}

	// �� ��ǰactivity�����ٵ�ʱ��   no bug
	@Override
	protected void onDestroy() {
		/*    �ж��̣߳����˳�activityʱ����Ȼ����ᱨ��!!!���û�н������̣߳����������Ͳ������
		*  java.lang.IllegalStateException�쳣�ˣ���Ҫ������߳��е�isPlayʹ��(��if�ж��м���isPlay)��
		*  ����֤�˳�PlayMusicActivityʱ������!!!������ִ������super.onDestroy()�����������е�if�жϾ䣬
		*  ִ����mediaPlayer���ҵĲ�������ȷ�ģ����߳�û�и����߳��㹻��ʱ���˳���ִ����super.onDestroy()!!!
		*/
		if(thread != null)      
		{
			if(thread.isAlive())
			{
				musicSeekBarThread.setIsPlay(false);         //��Ϊ�ж��߳�!!!good job!
			}
		}
		

		// ���������ʱ��ֹͣ�������֣������ͷ��й���Դ
		super.onDestroy();
	
		
		if (mediaPlayer != null) {
			mediaPlayer.stop();           // ֹͣ����
			//��Mediaplayer�����ٱ�ʹ��ʱ����õ���release����������������ͷţ�ʹ�䴦�ڽ���״̬����ʱ�����ܱ�ʹ��
			mediaPlayer.release();//�ͷ���mediaPlayer��ص���Դ �� �ͷź�һ��Ҫ����Ϊnull// Set the MediaPlayer to null to avoid IlLegalStateException 
		   mediaPlayer = null;
		}
		
		if(serviceConnection != null && serviceIntent != null)
		{
	    	unbindService(serviceConnection);   //���service
       		stopService(serviceIntent);         //ֹͣ����
		}
	}

	@Override
	public void finish() // ���˳�activity��ʱ��͵��ã����߻Ӧ�ùرյ�ʱ��Ҳ�����!!!
	{
		super.finish();
		// �����˳�ʱ�Ķ���
		overridePendingTransition(R.animator.from_left_in, R.animator.toward_right_out);
	}
	
	public static void slowDecreaseVolume()           //�ڰ�����ͣ��ť�����˳����ֽ���ʱ,������������!!!
	{
		new Thread(new Runnable() {            //�������̣߳��������˳�PlayMusicActivityʱ������������������
			 
			@Override
			public void run() {
				
				for(float i = 0.1f; i<=1.0; i += 0.1)
				{
	    			mediaPlayer.setVolume(1-i , 1-i);        //������������������������
	    			try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		
		try {                    //�������㹻��ʱ����������!!! ������ô��ʱ���ִ�������onBack()
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
		// ���������һ�����ʱ��
		if (e1.getX() - e2.getX() < -120) {
			
			slowDecreaseVolume();      //������������ �� �ڻ����˳���ʱ��         //�ڵ�����Ű�ť�Ļ���û���Ч������Ҫ��һ�µ�ǰ��������ʲô����ʱ��������!!!!
			
			this.onBackPressed(); // �൱�ڰ�����back��
	
			//����Ǵ�MyMusicActivity�򿪵�PlayMusicActivity���˳�ʱ�����ص�MyMusicActivity��
			//����Ǵ�MusicTopActivity�򿪵�PlayMusicActivity���˳�ʱ�����ص�MusicTopActivity��
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