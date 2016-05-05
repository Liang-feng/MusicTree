package com.example.easymusicplayer1.activity;

import java.io.File;
import java.util.ArrayList;

import com.example.easymusicplayer1.R;
import com.example.easymusicplayer1.db.ReadMusicInfoFromLocalDb;
import com.example.easymusicplayer1.model.Music;
import com.example.easymusicplayer1.net.FindMusicFromInternet;
import com.example.easymusicplayer1.net.RequestMusicLyric;
import com.example.easymusicplayer1.service.MusicForegroundService1;
import com.example.easymusicplayer1.service.MusicForegroundService1.MusicBinder;
import com.example.easymusicplayer1.utility.MusicAdapter;
import com.example.easymusicplayer1.utility.MyApplication;
import com.example.easymusicplayer1.utility.ScanMusicFile;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TableLayout.LayoutParams;

/**
 * 我的音乐界面
 * @author feng
 *
 */
public class MyMusicActivity extends Activity {
	
	public static int isOpen = 0;                       //0为不是当前打开的，用于解决按back键后退出，下次点击app图标还能回到原位。

	String  playOrder = "ORDER_PLAY";                            //用于存储播放顺序，默认是按循序播放

	private ListView musicTitleListView;                         //下来刷新的实例

	private MusicAdapter adapter;                                //自定义adapter

	private ArrayList<String> musicTitleList;                    //歌曲名称

	private ArrayList<String> musicUrlList;                      //歌曲播放地址

	private ArrayList<Integer> musicDurationList;                //歌曲播放时长

	private ArrayList<String> musicIdList;                       //歌曲id，不是1,2,3之类的id

	private ArrayList<String> musicSingerName;                   //歌曲作者
	
	ArrayList<Music> musicList;                                  //用来存储音乐类的信息，用于ListView

	private Music music;                                         // 用于删除音乐文件，以及作为listview的类item

	private ReadMusicInfoFromLocalDb readMusicInfoFromLocalDb;      // 用于获取musicTitleList
	
	private Context context;              // 上下文，用于引用数据，比如图片等!!!

	private Intent serviceIntent = null;  // 用于启动service的intent

	private Intent intent = null ;        //用于启动PlayMusicActivity

	private int musicTitlePosition;                 //用于标记歌曲在ListView的item项上的位置，用于在前台服务中定位输出歌曲名

	private MusicForegroundService1.MusicBinder musicBinder;

	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {       // activity与service绑定成功时就会调用
			musicBinder = (MusicBinder) service;          // 与service沟通的桥梁
			//第一次进入PlayMusicActivity的界面时候，设置前台服务的音乐名称
			musicBinder.changeMusicTitle(musicTitleList , musicTitlePosition); // 通过调用MusicBinder的changeMusicTitle函数来对service进行操作!!!
			Log.e("MainActivity" , "绑定服务");

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_list_view);
		
		ScanMusicFile scanMusicFile = new ScanMusicFile();
		scanMusicFile.scanMusic(MyMusicActivity.this);
		
		initLayout();        //初始化Tab项“我的音乐”的界面  , 把手机中的音乐列出来!!! 
		initActionBar();     //ActionBar的相关设置	

		new Thread() {          // 增加子线程，其实不加也没事，看起来界面不卡顿,主要是为了练习一下线程!!!  从Music类中获取musicUrl，musicDuration，musicId
			@Override
			public void run() {
				initMusicData();         //初始化音乐有关数据!!!
			}
		}.start();


	}


	/**
	 * ActionBar的相关设置
	 */
	private void initActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);        //设置ActionBar左侧的图标不可见
		actionBar.setDisplayShowTitleEnabled(true);       //设置ActionBar左侧的标题不可见
		actionBar.setDisplayHomeAsUpEnabled(true);         //设置ActionBar左侧的返回上一级的图标显示出来!!!
		actionBar.setTitle("我的音乐");
	}

	//no bug
	private void initMusicTitleList() {        //初始化，获取music的title列表

		readMusicInfoFromLocalDb = new ReadMusicInfoFromLocalDb();        // 不要忘记初始化
		musicTitleList = new ArrayList<String>();
		musicTitleList = new ArrayList<String>(readMusicInfoFromLocalDb.getMusicTitle());
		Log.e("MyMusicActivity", musicTitleList.toString());
	}
	
	


	//no bug
	@Override
	public boolean onContextItemSelected(MenuItem item) {        //用于长按ListView的item项时出现菜单，删除
	
		music = new Music();
		
		// 返回有关ListView的item的有关信息
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

		//int index = info.position;
		int index = (int) info.id;                           // 获取当前ListView中点击item项的id

		File file = new File(musicUrlList.get(index));       //由于添加了下拉刷新，所以要删除的是index-1，而不是index了!!! 没有下拉刷新则不用修改

		switch (item.getItemId()) {
		case 0: // 点击菜单项中的"删除"

			if (file.exists() && file.isFile()) {
				// 删除音乐文件!!! 以及删除数据库中的音乐信息，为了不让其在app重新打开时再一次显示在ListView上!!!
				if (file.delete() && music.deleteMusicFromMediaStore(new String[] { musicIdList.get(index) })) {
					Toast.makeText(MyMusicActivity.this, "已删除歌曲 : " + musicTitleList.get(index), Toast.LENGTH_SHORT).show();
					// 进行下面的操作后，这样删除歌曲后，ListView中的item项上移，就不会出现路径错误了!!
					musicTitleList.remove(index);                 // 删除ListView的第index项item
					// 这个要放在Toast的后面，不然输出不了,会报错!!!
					musicUrlList.remove(index);                   // 删除musicUrlList中有关歌曲的路径信息
					musicIdList.remove(index);                    // 删除musicIdList中有关歌曲的id
					musicDurationList.remove(index);              // 删除musicDurationList中有关歌曲的时长
					musicList.remove(index);                      // 删除index位置上的音乐信息

					ScanMusicFile scanMusicFile = new ScanMusicFile();            //删除完数据后，扫描一下文件，更新多媒体数据库
					scanMusicFile.scanMusic(MyMusicActivity.this);
					
					adapter.notifyDataSetChanged(); // 通知adapter有数据改变 ,  重新通知adapter读取数据吧!!!
					
				} else {
					Toast.makeText(this, "删除歌曲失败", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(MyMusicActivity.this , "文件不存在", Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}

		return super.onContextItemSelected(item);

	}


	//no bug
	public void initLayout()
	{

        musicList = new ArrayList<Music>();
        musicList = initMusicList(musicList);
        adapter = new MusicAdapter(MyMusicActivity.this , R.layout.my_music_activity , musicList);
        
        // 下面是对listView进行操作
		musicTitleListView = (ListView) findViewById(R.id.msuic_list_view);
		//adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, musicTitleList);
		musicTitleListView.setAdapter(adapter);



		musicTitleListView.setOnItemClickListener(new OnItemClickListener() {    //no bug 设置点击ListView上的歌曲 
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				musicTitlePosition = position;             //因为加入了下拉刷新，所以要把position 改为 position - 1，没有下拉刷新则不用

				Log.e("MyMusicFragment" , musicSingerName.get(musicTitlePosition) + "  " + musicTitleList.get(musicTitlePosition));
				FindMusicFromInternet fMusicFromInternet = new FindMusicFromInternet(musicSingerName.get(musicTitlePosition) + " " + musicTitleList.get(musicTitlePosition));
				fMusicFromInternet.getMusicIdFromInternet();       //从网络上通过歌手名，或者歌名来搜索一首歌曲，然后截取搜索到的第一首歌，解析获得其id

				transferDate(position);        //已ListView上的item项的位置为参数，把数据包装，准备传递到PlayMusicActivity , 因为加入了下拉刷新，所以要把position 改为 position - 1 , 没有下拉刷新则不用改

				startActivity(intent);         // 启动playMusicActivity，即播放音乐的界面

				PlayMusicActivity.isOpen = 1;
				beginService();               //启动，绑定服务，以及传递数据到servic， 是先运行服务，然后才绑定服务，调用serviceConnection中的onReceive函数

			}
		});


		musicTitleListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() { // 长按Item显示出菜单
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {				
				menu.add(0, 0 , 0, "删除");         //创建菜单项“删除”
			}
		});




		/**
		 * //监听当用户下拉刷新时，只要加了监听事件，那么只要不调用结束刷新musicTitleListView.onRefreshComplete();  那么刷新的图标就会一直转动。
		 *        在AsyncTask中是为了再三秒后，关闭刷新!!!即模拟网络通信!!!
		 */
		/*				musicTitleListView.setOnRefreshListener(new OnRefreshListener<ListView>() {       //监听当用户下拉刷新时
				@Override
					public void onRefresh(PullToRefreshBase<ListView> refreshView) {

				    new AsyncTask<Void, Void, Void>()
                        {

							@Override
							protected Void doInBackground(Void... params) {
								new ScanMusicFile().scanMusic(MyMusicActivity.this);          //扫描文件   ,把新增加的文件的信息  放进多媒体数据库中

								try {
									Thread.sleep(3000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

								return null;
							}


							@Override
							protected void onPostExecute(Void result) {
								super.onPostExecute(result);
								updateMusicDate();                                    //用于在下拉刷新后，更新数据！！！

								adapter.notifyDataSetChanged();                     //通知adapter，ListView上的数据发生改变!!!
								musicTitleListView.onRefreshComplete();             //结束刷新动作!!!

							}

                        }.execute();

					}
				});*/


	}


	private ArrayList<Music> initMusicList(ArrayList<Music> musicList) {
		
		initMusicTitleList();        // 初始化musicTitleList
		initMusicSingerName();       //初始化musicSingerList
		initMusicDuration();         //初始化musicDurationList
		
		Music music;
		for(int index=0; index<musicTitleList.size(); index++)
		{
			music = new Music();
			music.setTitle(musicTitleList.get(index));
			music.setSingerName(musicSingerName.get(index));
			music.setDuration(musicDurationList.get(index));
    		musicList.add(music);       //由于Music类有点特殊，所以这样来!!!
		}
		return musicList;
	}


	public void initMusicId() {          //获取music的id
		setMusicIdList(new ArrayList<String>());
		setMusicIdList(new ArrayList<String>(readMusicInfoFromLocalDb.getMusicId()));
	}


	public void initMusicDuration() {         //获取music的播放时间
		musicDurationList = new ArrayList<Integer>(readMusicInfoFromLocalDb.getMusicDuration());
	}

	public void initMusicUrl() {              //获取music文件的路径
		musicUrlList = new ArrayList<String>();
		musicUrlList = new ArrayList<String>(readMusicInfoFromLocalDb.getMusicUrl());
	}

	//从多媒体数据库中获取歌手名称
	private void initMusicSingerName() {
		// TODO Auto-generated method stub
		musicSingerName = new ArrayList<String>();
		musicSingerName = readMusicInfoFromLocalDb.getMusicSingerName();
	}

	public ArrayList<String> getMusicIdList() {        //返回music的id列表 ， 用于什么？？？？
		return musicIdList;
	}

	public void setMusicIdList(ArrayList<String> musicIdList) {        //设置music的id的List , 用于什么？？？
		this.musicIdList = musicIdList;
	}



	private void transferDate(int position) {       //no bug 准备把数据传递给PlayMusicActivity

		intent = new Intent(MyMusicActivity.this , PlayMusicActivity.class); 
		Bundle bundle = new Bundle();
		bundle.putString("music_title", musicTitleList.get(position));
		intent.putExtra("fragment" , "MyMusicFragment");                            //标明是从MyFragment启动的PlayMusicActivity
		intent.putExtras(bundle);
		intent.putExtra("music_url", musicUrlList.get(position));
		intent.putExtra("music_duration", musicDurationList.get(position).intValue());
		intent.putExtra("music_title_position", position); // 传递当前音乐名称在ListView的item上位置，用来方便执行顺序播放！！！
		intent.putStringArrayListExtra("music_title_list", musicTitleList);// 传递musicTitleList，用于顺序，随机播放
		intent.putStringArrayListExtra("music_url_list", musicUrlList);           //传递音乐播放地址
		intent.putStringArrayListExtra("music_singer", musicSingerName);          //传递音乐作者过去
		intent.putIntegerArrayListExtra("music_duration_list", musicDurationList);       //传递音乐播放时长
		intent.putExtra("music_play_order" , playOrder);                             //传递音乐的播放顺序
		Log.e("MyMusicActivity", playOrder);
	}


	private void beginService() {           //启动，绑定服务，以及传递数据到service

		context = MyApplication.getContext();
		serviceIntent = new Intent(context , MusicForegroundService1.class);

		//serviceIntent.putExtra("music_title" , musicTitleList);                  //把歌曲名称列表传递到service，用于前台service中歌曲名称的变化
		//serviceIntent.putExtra("music_title_position" , musicTitlePosition);     //歌曲名在ListView的item项上的位置，用于在前台服务中定位输出歌曲名

		context.startService(serviceIntent);          //启动服务
		context.bindService(serviceIntent, serviceConnection , 0);          //绑定服务
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (serviceIntent != null) {
			// 用于程序退出时，退出service
			context.stopService(serviceIntent);        // 无法确定serviceIntent为不为空，所以要加上判断语句
			context.unbindService(serviceConnection);

		}


		Intent intent = new Intent(MyMusicActivity.this , FlatteningStartActivity.class);
		startActivity(intent);
		FlatteningStartActivity.isOpen = 1;
	}

	/**
	 *  初始化音乐有关数据！以及用于在刷新音乐列表后，更新其它数据！！！
	 */
	public void initMusicData()
	{
		initMusicUrl(); // 获取音乐路径
		initMusicDuration(); // 获取音乐时长
		initMusicId(); // 从数据库中获取音乐在数据库中的Id
		initMusicSingerName();       //从数据库中读取音乐作者
	}



	public void updateMusicDate()
	{
		////重新从数据库读取数据
		//  music = new Music();                              //怪不得，更新不了列表，原来是music没new，导致还是旧的数据!!!
		initMusicTitleList();                               //不过在initMusicTitleList()开头有new了一个music对象!!!
		initMusicData();
		Log.e("MainActivity", "musicTitleList = " + musicTitleList.toString());

	}


	//创建action Bar（操作栏）上的菜单
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_music_activity_menu , menu);       //在R.menu.main中设置了随机播放，顺序播放等的菜单项！！！
		return true;
	}

	// 点击菜单上的item后就会调用此函数!!!    no bug
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.


		int id = item.getItemId();


		if (id == R.id.action_settings)          //单曲循环
		{
			playOrder  = "SINGLE_CIRCLE";
		}
		else if(id == R.id.play_order_item)        //按循序播放
		{
			playOrder = "ORDER_PLAY";
		}
		else if(id == R.id.play_random_item)          //随机播放
		{
			playOrder = "RANDOM_PLAY";
		}
		else if(id == R.id.play_once_item)           //只播放一次
		{
			playOrder = "ONCE_PLAY";
		}
		else if(id == android.R.id.home)              //点击了actionBar左侧方向键
		{
			finish();
		}
		return super.onOptionsItemSelected(item);
	}


}