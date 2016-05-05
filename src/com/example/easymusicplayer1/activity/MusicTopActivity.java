package com.example.easymusicplayer1.activity;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.Header;

import com.example.easymusicplayer1.R;
import com.example.easymusicplayer1.db.MusicSQLiteOpenHelper;
import com.example.easymusicplayer1.db.ReadMusicInfo;
import com.example.easymusicplayer1.model.MusicTop;
import com.example.easymusicplayer1.net.DownloadMusicTask;
import com.example.easymusicplayer1.net.FindMusicFromInternet;
import com.example.easymusicplayer1.net.RequestHttpUrlConnection;
import com.example.easymusicplayer1.service.MusicForegroundService1;
import com.example.easymusicplayer1.service.MusicForegroundService1.MusicBinder;
import com.example.easymusicplayer1.utility.DataDipose;
import com.example.easymusicplayer1.utility.MusicTopAdapter;
import com.example.easymusicplayer1.utility.MyApplication;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class MusicTopActivity extends Activity {
	
	public static int isOpen = 0;                       //0为不是当前打开的

	ListView lv_musicTop;

	ListView lv_menu;

	ArrayAdapter<String> menuAdapter;

	ArrayAdapter<String> adapter;

	ArrayList<String> musicNameTopList; // 用于存储音乐标题

	ArrayList<String> musicUrlList; // 用于存储音乐的在线播放地址!!!

	ArrayList<String> musicDownUrlList; // 用于存储音乐的下载地址!!!

	ArrayList<Integer> musicAlbumidList; // 用于存储音乐的专辑id

	ArrayList<Integer> musicSecondsList; // 用于存储音乐的总播放时间

	ArrayList<Integer> musicSingerIdList; // 用于存储音乐的歌手id

	ArrayList<String> musicSingerNameList; // 用于存储音乐歌手名称

	ArrayList<Integer> musicSongIdList; // 用于存储音乐的id

	ArrayList<String> musicSongNameList; // 用于存储音乐的名称

	MusicSQLiteOpenHelper musicDb; // 用于操作数据库

	ReadMusicInfo readMusicInfo;

	RequestHttpUrlConnection requestHttpUrlConnection;

	String response;

	int musicTitlePosition; // 当前被点击的item项的位置!!!

	AlertDialog.Builder dialog; // 用于在“音乐馆”点击音乐时出现，与菜单栏相似的dialog！！！

	ProgressDialog progressDialog;

	// 主线程
	AsyncHttpResponseHandler resHandler = new AsyncHttpResponseHandler() {

		public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable e) {

			Toast.makeText(MusicTopActivity.this, "当前没有可用网络", Toast.LENGTH_LONG).show();

			Log.e("StartActivity", "失败获得数据");
			progressDialog.cancel();

			// 鍋氫竴浜涘紓甯稿鐞�
			e.printStackTrace();
		}

		// 从网络上请求数据成功就调用这个!!!
		public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
			try {
				Toast.makeText(MusicTopActivity.this, "加载成功", Toast.LENGTH_SHORT).show();
				String data = new String(responseBody, "utf-8");
				long b = System.currentTimeMillis();
				// long a=(Long) txt.getTag();
				System.out.println("response is :" + new String(responseBody, "utf-8"));
				// System.out.println("used time is :"+(b-a));

				DataDipose.dealString(data); // 解析返回来的数据，并且存储到数据库里面!!!

				initView(); // 初始化界面

				progressDialog.cancel();

				Log.e("MusicTopActivity", "成功获得数据");
				// 鍦ㄦ瀵硅繑鍥炲唴瀹瑰仛澶勭悊
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

		}
	};

	private MusicForegroundService1.MusicBinder musicBinder;

	ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			musicBinder = (MusicBinder) service; // 与service沟通的桥梁
			// 第一次进入PlayMusicActivity的界面时候，设置前台服务的音乐名称
			musicBinder.changeMusicTitle(musicNameTopList, musicTitlePosition); // 通过调用MusicBinder的changeMusicTitle函数来对service进行操作!!!
			Log.e("MainActivity", "绑定服务");
		}
	};

	private static final Handler handler = new Handler() {

		@Override
		public void dispatchMessage(Message msg) {
			// TODO Auto-generated method stub
			super.dispatchMessage(msg);
		}

		@Override
		public void handleMessage(Message msg) {
			String response = (String) msg.obj;
			Log.e("MusicTopActivity", "response :   " + response);
			super.handleMessage(msg);
		}
	};

	public static Handler getHandler() {
		return handler;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_top_list);

		initActionBar();

		createProgressDialog(); // 创建一个progressDialog，用户体验
		requestLyricDataFromInternet(); // 从网上获取数据

	}

	/**
	 * 创建一个progressDialog，用户体验
	 */
	private void createProgressDialog() {

		progressDialog = new ProgressDialog(MusicTopActivity.this, 1);
		progressDialog.setMessage("排行榜数据加载中...");
		progressDialog.setCancelable(true);                  //还是设置取消，不然退出本activity会对于扫描本地音乐文件有影响             //不能设置取消，会影响体验
		progressDialog.show();
	}

	/**
	 * 启动子线程来从网络获取音乐数据,注意经过 我自己测试得，到底是先启动MainActivity还是先执行onFailure()函数或者
	 * 是onSuccess()函数，是由线程内的sleep决定的，相互之间没有影响!!!
	 */
	private void requestLyricDataFromInternet() {

		new Thread(new Runnable() {

			@Override
			public void run() {

				requestDataFromInternet(); // 提前开始进行网络请求，防止反应不过来，arrayList为空,而报错!!!

				if (distinguishNowNetWork() == 13) {
					try {
						Thread.sleep(1000); // 如果是4G网络，减短从网络上获取数据的时间!!
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} else {
					// 注意主线程不要设置sleep，不然控件都显示不出来!!!
					try {
						Thread.sleep(5000); // 如果是3G网络，有足够的时间从网络上获取数据!!
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}

			}
		}).start();
	}

	/**
	 * 调用第三方API函数，从网络上获取音乐排行榜数据，以及获取音乐歌词!!!
	 */
	public void requestDataFromInternet() {
		/**
		 * 开启子线程 , 调用第三方API函数，获取音乐排行榜
		 */
		new com.example.easymusicplayer1.showapi.ShowApiRequest("http://route.showapi.com/213-4", "11961",
				"1a1ee362464b4cd6beb9f69c43787f86").setResponseHandler(resHandler)
						// .addTextPara("keyword", "昨夜小楼又东风")
						.addTextPara("topid", "5").post();

	}

	/**
	 * 返回当前网络是多少G网络，如果是4G网络则快点进入到MainActivity中去!!!
	 * 
	 * @return
	 */
	public int distinguishNowNetWork() {
		ConnectivityManager cManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		@SuppressWarnings("deprecation")
		NetworkInfo networkInfo = cManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		return networkInfo.getSubtype(); // 返回当前网络的类型所代表的整数!!!

	}

	/**
	 * 界面，获取控件
	 */
	private void initView() {

		readMusicInfo = new ReadMusicInfo();
		musicNameTopList = new ArrayList<String>();
		musicUrlList = new ArrayList<String>();
		musicDownUrlList = new ArrayList<String>();
		musicAlbumidList = new ArrayList<Integer>();
		musicSecondsList = new ArrayList<Integer>();
		musicSingerIdList = new ArrayList<Integer>();
		musicSingerNameList = new ArrayList<String>();
		musicSongIdList = new ArrayList<Integer>();
		musicSongNameList = new ArrayList<String>();
		ArrayList<MusicTop> musicTopList = new ArrayList<MusicTop>();
        
		//Log.e("MusicTopActivity", "测试请求的数据先返回，还是这里先执行!!!");

		initMusicSingerNameList();            // 获取歌手姓名
		initMusicNameTopListDate();           // 初始化，音乐数据名称，ListView将要显示的数据
		musicTopList = initMusicTopList(musicTopList);                  //ListView将要显示的数据
		 
		lv_musicTop = (ListView) findViewById(R.id.msuic_top_list_view); // 新建listView
		MusicTopAdapter adapter = new MusicTopAdapter(MusicTopActivity.this , R.layout.music_top_activity , musicTopList);
		lv_musicTop.setAdapter(adapter); // 为listView设置adapter

		/**
		 * 监听如果点击了，音乐馆中的音乐，则先检测当前是否为wifi条件下，弹出提示框，
		 */
		lv_musicTop.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				musicTitlePosition = position;
				initMusicUrl(); // 从数据库中获取对应当前item项音乐的在线播放地址！！！
				initMusicDownUrl(); // 从数据库中获取对应当前item项音乐的下载地址！！

				initDialog(); // 当点击“音乐馆”的歌曲时，弹出类似于菜单的自定义dialog!!!
			}

			/**
			 * 对dialog进行相关设置，缺陷：不能通过代码来取消，要用户自己进行取消，或许我可以设置个RadioButton，让用户自己选，
			 * 然后。 有时间再说
			 */
			private void initDialog() { // 初始化dialog，对其属性进行相关设置!!!

				dialog = new AlertDialog.Builder(MusicTopActivity.this);
				dialog.setCancelable(true);                                  
				//initMenuListView(); // 初始化listVie
				//dialog.setView(lv_menu); // 为dialog设置一个listView
			    dialog.setPositiveButton("在线试听" , new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						PlayMusicActivity.isOpen = 1;
						FindMusicFromInternet fMusicFromInternet = new FindMusicFromInternet(musicSingerNameList.get(musicTitlePosition) + " " + musicNameTopList.get(musicTitlePosition));
					    fMusicFromInternet.getMusicIdFromInternet();       //从网络上通过歌手名，或者歌名来搜索一首歌曲，然后截取搜索到的第一首歌，解析获得其id
						onlineListener(); // 在线试听
						dialog.cancel();
					}
				});
			    
			    dialog.setNegativeButton("下载歌曲", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						onlineListenerAndDownload(); // 下载歌曲
						dialog.cancel();
					}
				});
				dialog.show();

			}

			/**
			 * Dialog上的ListView自定义布局
			 */
	/*		private void initMenuListView() {

				String[] menuItemStr = { "在线试听", "下载歌曲" };
				lv_menu = new ListView(MusicTopActivity.this);
				lv_menu.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				menuAdapter = new ArrayAdapter<String>(MusicTopActivity.this, android.R.layout.simple_list_item_1,
						menuItemStr);
				lv_menu.setAdapter(menuAdapter);

				lv_menu.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						switch (position) {
						case 0:
							onlineListener(); // 在线试听
							break;
						case 1:
							onlineListenerAndDownload(); // 下载歌曲
							break;
						default:
							break;
						}
					}
				});

			}*/

		});

	}

	/**
	 * 从数据库获取歌手姓名
	 */
	private void initMusicSingerNameList() {
		
		this.musicSingerNameList = readMusicInfo.getMusicSingerName();
		Log.e("MusicTopActivty", musicSingerNameList.toString());
		
	}

	/**
	 * 初始化listView的数据
	 * @param musicTopList
	 */
	private ArrayList<MusicTop> initMusicTopList(ArrayList<MusicTop> musicTopList) {

		MusicTop music;

		for(int index=0; index<musicNameTopList.size(); index++)
		{
			music = new MusicTop();
			music.setSongName(musicNameTopList.get(index));
			if(musicSingerNameList.get(index) == null)               //为了防止有些歌曲没有歌手的，没有输出，不好看，所以...
			{
    			music.setSingerName("未知");
			}
			else
			{
    			music.setSingerName(musicSingerNameList.get(index));
			}
			musicTopList.add(music);

		}
		
		return musicTopList;
	}

	/**
	 * 对actionBar做相关设置
	 */
	private void initActionBar() {

		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true); // 设置ActionBar左侧的返回上一级的图标显示出来!!!
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setTitle("音乐之家");
		// actionBar.show();
	}

	/**
	 * 从数据库中读取数据
	 */
	private void initMusicNameTopListDate() {

		musicNameTopList = readMusicInfo.getMusicName(); // 从数据库中读取音乐名称
		Log.e("MusicTopActivty", musicNameTopList.toString());

		if (musicNameTopList.isEmpty()) {
			musicNameTopList.add("从网络请求数据来不及响应");
		}
	}

	private void initMusicUrl() {
		musicUrlList = readMusicInfo.getMusicUrl();
	}

	private void initMusicDownUrl() {
		musicDownUrlList = readMusicInfo.getMusicDownUrl();
	}

	/**
	 *    暂时不用启动前台服务了，bug多!!!
	 */
	private void beginService() { // 启动，绑定服务，以及传递数据到service

		Context context = MyApplication.getContext();
		Intent serviceIntent = new Intent(context, MusicForegroundService1.class);

		// serviceIntent.putExtra("music_title" , musicTitleList);
		// //把歌曲名称列表传递到service，用于前台service中歌曲名称的变化
		// serviceIntent.putExtra("music_title_position" ,
		// musicTitlePosition);//歌曲名在ListView的item项上的位置，用于在前台服务中定位输出歌曲名

		context.startService(serviceIntent); // 启动服务
		context.bindService(serviceIntent, serviceConnection, 0); // 绑定服务
	}

	/**
	 * 点击在线播放后，打开PlayMusicActivity，传递有关歌曲名，以及播放地址过去，并绑定前台服务!!!
	 */
	private void onlineListener() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(MusicTopActivity.this, PlayMusicActivity.class);
		intent.putExtra("fragment", "MusicTopFragment"); // 用于给PlayMusicActivity判断是哪个fragment启动了它
		intent.putExtra("music_name", musicNameTopList.get(musicTitlePosition));
		intent.putExtra("music_url", musicUrlList.get(musicTitlePosition));
		intent.putStringArrayListExtra("music_title_list", musicNameTopList);
		intent.putStringArrayListExtra("music_url_list", musicUrlList);
		intent.putStringArrayListExtra("music_singer", musicSingerNameList);
		startActivity(intent);
		//beginService();

	}

	/**
	 * 当点击dialog上的“下载歌曲”的时候, 从网络上下载歌曲
	 */
	private void onlineListenerAndDownload() {

		// 下载歌曲,传递 下载地址，context，音乐名称 过去
		new DownloadMusicTask(musicDownUrlList.get(musicTitlePosition), MusicTopActivity.this, musicNameTopList.get(musicTitlePosition))
				.execute();

		/*
		 * { Intent intent = new Intent(getActivity() , SecondActivity.class);
		 * intent.putExtra("url" , musicDownUrlList.get(position1));
		 * startActivity(intent); }
		 */
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home) // 点击了actionBar左侧方向键
		{
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() { // 摧毁活动时，删除数据库，避免数据过多!!!

		MusicSQLiteOpenHelper db = new MusicSQLiteOpenHelper(MusicTopActivity.this, "MusicStore.db", null, 2);
		SQLiteDatabase sqliteDatabase = db.getWritableDatabase();
		sqliteDatabase.delete("Music", null, null); // 在每次退出本activity之前都要把之前的表内数据全部清除，避免音乐馆数据重复
		// this.deleteDatabase("/data/data/com.example.easymusicplayer1/databases/MusicStore.db");
		super.onDestroy();

		Intent intent = new Intent(MusicTopActivity.this, FlatteningStartActivity.class);
		startActivity(intent);
		FlatteningStartActivity.isOpen = 1;
	}

}