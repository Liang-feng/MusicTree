package com.example.easymusicplayer1.activity;

import java.io.File;
import java.util.ArrayList;

import com.example.easymusicplayer1.db.ReadMusicInfoFromLocalDb;
import com.example.easymusicplayer1.model.Music;
import com.example.easymusicplayer1.net.FindMusicFromInternet;
import com.example.easymusicplayer1.net.RequestMusicLyric;
import com.example.easymusicplayer1.service.MusicForegroundService1;
import com.example.easymusicplayer1.service.MusicForegroundService1.MusicBinder;
import com.example.easymusicplayer1.utility.MyApplication;
import com.example.easymusicplayer1.utility.ScanMusicFile;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

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

public class MyMusicFragment extends Fragment {
	
	ReadMusicInfoFromLocalDb readMusicInfoFromLocalDb;
	
	private LinearLayout linearLayout = null;

	private PullToRefreshListView musicTitleListView;             //下来刷新的实例

	private ArrayAdapter<String> adapter;

	private ArrayList<String> musicTitleList;                    //歌曲名称

	private ArrayList<String> musicUrlList;                      //歌曲播放地址

	private ArrayList<Integer> musicDurationList;                //歌曲播放时长
 
	private ArrayList<String> musicIdList;                       //歌曲id，不是1,2,3之类的id
	
	private ArrayList<String> musicSingerName;                   //歌曲作者
	
	private Music music;                  // 用于获取musicTitleList

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
			musicBinder = (MusicBinder) service; // 与service沟通的桥梁
            //第一次进入PlayMusicActivity的界面时候，设置前台服务的音乐名称
			musicBinder.changeMusicTitle(musicTitleList , musicTitlePosition); // 通过调用MusicBinder的changeMusicTitle函数来对service进行操作!!!
			Log.e("MainActivity" , "绑定服务");

		}
	};

	/**
	 * 可以利用此函数的LayoutInflater引入xml布局文件
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		linearLayout = initLayout();        //初始化Tab项“我的音乐”的界面  , 把手机中的音乐列出来!!! 
		
		
		new Thread() {                      // 增加子线程，其实不加也没事，看起来界面不卡顿,主要是为了练习一下线程!!!  从Music类中获取musicUrl，musicDuration，musicId
			@Override
			public void run() {
				initMusicData();         //初始化音乐有关数据!!!
			}
		}.start();

			
		musicTitleListView.setOnItemClickListener(new OnItemClickListener() {    //no bug 设置点击ListView上的歌曲 
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				musicTitlePosition = position - 1;                 //因为加入了下拉刷新，所以要把position 改为 position - 1
				
				Log.e("MyMusicFragment" , musicSingerName.get(musicTitlePosition) + "  " + musicTitleList.get(musicTitlePosition));
                FindMusicFromInternet fMusicFromInternet = new FindMusicFromInternet(musicSingerName.get(musicTitlePosition) + " " + musicTitleList.get(musicTitlePosition));
				fMusicFromInternet.getMusicIdFromInternet();       //从网络上通过歌手名，或者歌名来搜索一首歌曲，然后截取搜索到的第一首歌，解析获得其id
				
				transferDate(position - 1);        //已ListView上的item项的位置为参数，把数据包装，准备传递到PlayMusicActivity , 因为加入了下拉刷新，所以要把position 改为 position - 1

				startActivity(intent);         // 启动playMusicActivity，即播放音乐的界面

				beginService();               //启动，绑定服务，以及传递数据到servic， 是先运行服务，然后才绑定服务，调用serviceConnection中的onReceive函数

			}
		});

		
		musicTitleListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() { // 长按Item显示出菜单
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {				
				menu.add(0, 0 , 0, "删除");         //创建菜单项“删除”
			}
		});

		return linearLayout;
	}

	//no bug
	private void initMusicTitleList() {        //初始化，获取music的title列表

		readMusicInfoFromLocalDb = new ReadMusicInfoFromLocalDb();
		music = new Music(); // 不要忘记初始化
		musicTitleList = new ArrayList<String>();
		musicTitleList = new ArrayList<String>(readMusicInfoFromLocalDb.getMusicTitle());
		Log.e("MainActivity", musicTitleList.toString());
	}
	
	
    //no bug
	@Override
	public boolean onContextItemSelected(MenuItem item) {        //用于长按ListView的item项时出现菜单，删除
		// 返回有关ListView的item的有关信息
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		
		//int index = info.position;
		int index = (int) info.id;      // 获取当前ListView中点击item项的id

		File file = new File(musicUrlList.get(index-1));       //由于添加了下拉刷新，所以要删除的是index-1，而不是index了!!!
 
		switch (item.getItemId()) {
		case 0: // 点击菜单项中的"删除"

			if (file.exists() && file.isFile()) {
				// 删除音乐文件!!! 以及删除数据库中的音乐信息，为了不让其在app重新打开时再一次显示在ListView上!!!
				if (file.delete() && music.deleteMusicFromMediaStore(new String[] { musicIdList.get(index-1) })) {
					Toast.makeText(getActivity(), "已删除歌曲 : " + musicTitleList.get(index-1), Toast.LENGTH_SHORT).show();
					// 进行下面的操作后，这样删除歌曲后，ListView中的item项上移，就不会出现路径错误了!!
					musicTitleList.remove(index-1); // 删除ListView的第index项item
													// 这个要放在Toast的后面，不然输出不了,会报错!!!
					musicUrlList.remove(index-1); // 删除musicUrlList中有关歌曲的路径信息
					musicIdList.remove(index-1); // 删除musicIdList中有关歌曲的id
					musicDurationList.remove(index-1); // 删除musicDurationList中有关歌曲的时长

					adapter.notifyDataSetChanged(); // 通知adapter有数据改变 ,  重新通知adapter读取数据吧!!!

				} else {
					Toast.makeText(getActivity(), "删除歌曲失败", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(getActivity(), "文件不存在", Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}

		return super.onContextItemSelected(item);

	}
	
	 
	//no bug
	public LinearLayout initLayout()
	{
	           	// 使用java也能配置布局
				LinearLayout linearLayout = new LinearLayout(getActivity());
				// android:layout_width = "match_parent" android:layout_height ="match_parent"
				LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				linearLayout.setLayoutParams(layoutParams);

				// 下面是对listView进行操作
				initMusicTitleList(); // 初始化musicTitleList
				adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, musicTitleList);
				musicTitleListView = new PullToRefreshListView(getActivity());            //创建下拉刷新的listView实例
				musicTitleListView.setAdapter(adapter);
				
				
				/**
				 * //监听当用户下拉刷新时，只要加了监听事件，那么只要不调用结束刷新musicTitleListView.onRefreshComplete();  那么刷新的图标就会一直转动。
				 *        在AsyncTask中是为了再三秒后，关闭刷新!!!即模拟网络通信!!!
				 */
				musicTitleListView.setOnRefreshListener(new OnRefreshListener<ListView>() {       //监听当用户下拉刷新时
				@Override
					public void onRefresh(PullToRefreshBase<ListView> refreshView) {

				    new AsyncTask<Void, Void, Void>()
                        {

							@Override
							protected Void doInBackground(Void... params) {
								new ScanMusicFile().scanMusic(getActivity());          //扫描文件   ,把新增加的文件的信息  放进多媒体数据库中
								
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
				});
				
				
				
				// 设置listView的width和height
				LayoutParams listViweLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				musicTitleListView.setLayoutParams(listViweLayoutParams);      
				// 设置视图
				linearLayout.addView(musicTitleListView); // 差点忘了加，小心，怪不得没东西出来!!!
				
				return linearLayout;
	}
	

	public void initMusicId() {          //获取music的id
		setMusicIdList(new ArrayList<String>());
		setMusicIdList(new ArrayList<String>(readMusicInfoFromLocalDb.getMusicId()));
	}

	
	public void initMusicDuration() {         //获取music的播放时间
		musicDurationList = new ArrayList<Integer>();
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

		intent = new Intent(getActivity(), PlayMusicActivity.class); 
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
	}
	
	
	private void beginService() {           //启动，绑定服务，以及传递数据到service
		
		context = MyApplication.getContext();
		serviceIntent = new Intent(context , MusicForegroundService1.class);

		//serviceIntent.putExtra("music_title" , musicTitleList);                //把歌曲名称列表传递到service，用于前台service中歌曲名称的变化
		//serviceIntent.putExtra("music_title_position" , musicTitlePosition);//歌曲名在ListView的item项上的位置，用于在前台服务中定位输出歌曲名

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
	

}