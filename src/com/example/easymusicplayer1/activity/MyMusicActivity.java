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
 * �ҵ����ֽ���
 * @author feng
 *
 */
public class MyMusicActivity extends Activity {
	
	public static int isOpen = 0;                       //0Ϊ���ǵ�ǰ�򿪵ģ����ڽ����back�����˳����´ε��appͼ�껹�ܻص�ԭλ��

	String  playOrder = "ORDER_PLAY";                            //���ڴ洢����˳��Ĭ���ǰ�ѭ�򲥷�

	private ListView musicTitleListView;                         //����ˢ�µ�ʵ��

	private MusicAdapter adapter;                                //�Զ���adapter

	private ArrayList<String> musicTitleList;                    //��������

	private ArrayList<String> musicUrlList;                      //�������ŵ�ַ

	private ArrayList<Integer> musicDurationList;                //��������ʱ��

	private ArrayList<String> musicIdList;                       //����id������1,2,3֮���id

	private ArrayList<String> musicSingerName;                   //��������
	
	ArrayList<Music> musicList;                                  //�����洢���������Ϣ������ListView

	private Music music;                                         // ����ɾ�������ļ����Լ���Ϊlistview����item

	private ReadMusicInfoFromLocalDb readMusicInfoFromLocalDb;      // ���ڻ�ȡmusicTitleList
	
	private Context context;              // �����ģ������������ݣ�����ͼƬ��!!!

	private Intent serviceIntent = null;  // ��������service��intent

	private Intent intent = null ;        //��������PlayMusicActivity

	private int musicTitlePosition;                 //���ڱ�Ǹ�����ListView��item���ϵ�λ�ã�������ǰ̨�����ж�λ���������

	private MusicForegroundService1.MusicBinder musicBinder;

	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {       // activity��service�󶨳ɹ�ʱ�ͻ����
			musicBinder = (MusicBinder) service;          // ��service��ͨ������
			//��һ�ν���PlayMusicActivity�Ľ���ʱ������ǰ̨�������������
			musicBinder.changeMusicTitle(musicTitleList , musicTitlePosition); // ͨ������MusicBinder��changeMusicTitle��������service���в���!!!
			Log.e("MainActivity" , "�󶨷���");

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_list_view);
		
		ScanMusicFile scanMusicFile = new ScanMusicFile();
		scanMusicFile.scanMusic(MyMusicActivity.this);
		
		initLayout();        //��ʼ��Tab��ҵ����֡��Ľ���  , ���ֻ��е������г���!!! 
		initActionBar();     //ActionBar���������	

		new Thread() {          // �������̣߳���ʵ����Ҳû�£����������治����,��Ҫ��Ϊ����ϰһ���߳�!!!  ��Music���л�ȡmusicUrl��musicDuration��musicId
			@Override
			public void run() {
				initMusicData();         //��ʼ�������й�����!!!
			}
		}.start();


	}


	/**
	 * ActionBar���������
	 */
	private void initActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);        //����ActionBar����ͼ�겻�ɼ�
		actionBar.setDisplayShowTitleEnabled(true);       //����ActionBar���ı��ⲻ�ɼ�
		actionBar.setDisplayHomeAsUpEnabled(true);         //����ActionBar���ķ�����һ����ͼ����ʾ����!!!
		actionBar.setTitle("�ҵ�����");
	}

	//no bug
	private void initMusicTitleList() {        //��ʼ������ȡmusic��title�б�

		readMusicInfoFromLocalDb = new ReadMusicInfoFromLocalDb();        // ��Ҫ���ǳ�ʼ��
		musicTitleList = new ArrayList<String>();
		musicTitleList = new ArrayList<String>(readMusicInfoFromLocalDb.getMusicTitle());
		Log.e("MyMusicActivity", musicTitleList.toString());
	}
	
	


	//no bug
	@Override
	public boolean onContextItemSelected(MenuItem item) {        //���ڳ���ListView��item��ʱ���ֲ˵���ɾ��
	
		music = new Music();
		
		// �����й�ListView��item���й���Ϣ
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

		//int index = info.position;
		int index = (int) info.id;                           // ��ȡ��ǰListView�е��item���id

		File file = new File(musicUrlList.get(index));       //��������������ˢ�£�����Ҫɾ������index-1��������index��!!! û������ˢ�������޸�

		switch (item.getItemId()) {
		case 0: // ����˵����е�"ɾ��"

			if (file.exists() && file.isFile()) {
				// ɾ�������ļ�!!! �Լ�ɾ�����ݿ��е�������Ϣ��Ϊ�˲�������app���´�ʱ��һ����ʾ��ListView��!!!
				if (file.delete() && music.deleteMusicFromMediaStore(new String[] { musicIdList.get(index) })) {
					Toast.makeText(MyMusicActivity.this, "��ɾ������ : " + musicTitleList.get(index), Toast.LENGTH_SHORT).show();
					// ��������Ĳ���������ɾ��������ListView�е�item�����ƣ��Ͳ������·��������!!
					musicTitleList.remove(index);                 // ɾ��ListView�ĵ�index��item
					// ���Ҫ����Toast�ĺ��棬��Ȼ�������,�ᱨ��!!!
					musicUrlList.remove(index);                   // ɾ��musicUrlList���йظ�����·����Ϣ
					musicIdList.remove(index);                    // ɾ��musicIdList���йظ�����id
					musicDurationList.remove(index);              // ɾ��musicDurationList���йظ�����ʱ��
					musicList.remove(index);                      // ɾ��indexλ���ϵ�������Ϣ

					ScanMusicFile scanMusicFile = new ScanMusicFile();            //ɾ�������ݺ�ɨ��һ���ļ������¶�ý�����ݿ�
					scanMusicFile.scanMusic(MyMusicActivity.this);
					
					adapter.notifyDataSetChanged(); // ֪ͨadapter�����ݸı� ,  ����֪ͨadapter��ȡ���ݰ�!!!
					
				} else {
					Toast.makeText(this, "ɾ������ʧ��", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(MyMusicActivity.this , "�ļ�������", Toast.LENGTH_SHORT).show();
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
        
        // �����Ƕ�listView���в���
		musicTitleListView = (ListView) findViewById(R.id.msuic_list_view);
		//adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, musicTitleList);
		musicTitleListView.setAdapter(adapter);



		musicTitleListView.setOnItemClickListener(new OnItemClickListener() {    //no bug ���õ��ListView�ϵĸ��� 
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				musicTitlePosition = position;             //��Ϊ����������ˢ�£�����Ҫ��position ��Ϊ position - 1��û������ˢ������

				Log.e("MyMusicFragment" , musicSingerName.get(musicTitlePosition) + "  " + musicTitleList.get(musicTitlePosition));
				FindMusicFromInternet fMusicFromInternet = new FindMusicFromInternet(musicSingerName.get(musicTitlePosition) + " " + musicTitleList.get(musicTitlePosition));
				fMusicFromInternet.getMusicIdFromInternet();       //��������ͨ�������������߸���������һ�׸�����Ȼ���ȡ�������ĵ�һ�׸裬���������id

				transferDate(position);        //��ListView�ϵ�item���λ��Ϊ�����������ݰ�װ��׼�����ݵ�PlayMusicActivity , ��Ϊ����������ˢ�£�����Ҫ��position ��Ϊ position - 1 , û������ˢ�����ø�

				startActivity(intent);         // ����playMusicActivity�����������ֵĽ���

				PlayMusicActivity.isOpen = 1;
				beginService();               //�������󶨷����Լ��������ݵ�servic�� �������з���Ȼ��Ű󶨷��񣬵���serviceConnection�е�onReceive����

			}
		});


		musicTitleListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() { // ����Item��ʾ���˵�
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {				
				menu.add(0, 0 , 0, "ɾ��");         //�����˵��ɾ����
			}
		});




		/**
		 * //�������û�����ˢ��ʱ��ֻҪ���˼����¼�����ôֻҪ�����ý���ˢ��musicTitleListView.onRefreshComplete();  ��ôˢ�µ�ͼ��ͻ�һֱת����
		 *        ��AsyncTask����Ϊ��������󣬹ر�ˢ��!!!��ģ������ͨ��!!!
		 */
		/*				musicTitleListView.setOnRefreshListener(new OnRefreshListener<ListView>() {       //�������û�����ˢ��ʱ
				@Override
					public void onRefresh(PullToRefreshBase<ListView> refreshView) {

				    new AsyncTask<Void, Void, Void>()
                        {

							@Override
							protected Void doInBackground(Void... params) {
								new ScanMusicFile().scanMusic(MyMusicActivity.this);          //ɨ���ļ�   ,�������ӵ��ļ�����Ϣ  �Ž���ý�����ݿ���

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
								updateMusicDate();                                    //����������ˢ�º󣬸������ݣ�����

								adapter.notifyDataSetChanged();                     //֪ͨadapter��ListView�ϵ����ݷ����ı�!!!
								musicTitleListView.onRefreshComplete();             //����ˢ�¶���!!!

							}

                        }.execute();

					}
				});*/


	}


	private ArrayList<Music> initMusicList(ArrayList<Music> musicList) {
		
		initMusicTitleList();        // ��ʼ��musicTitleList
		initMusicSingerName();       //��ʼ��musicSingerList
		initMusicDuration();         //��ʼ��musicDurationList
		
		Music music;
		for(int index=0; index<musicTitleList.size(); index++)
		{
			music = new Music();
			music.setTitle(musicTitleList.get(index));
			music.setSingerName(musicSingerName.get(index));
			music.setDuration(musicDurationList.get(index));
    		musicList.add(music);       //����Music���е����⣬����������!!!
		}
		return musicList;
	}


	public void initMusicId() {          //��ȡmusic��id
		setMusicIdList(new ArrayList<String>());
		setMusicIdList(new ArrayList<String>(readMusicInfoFromLocalDb.getMusicId()));
	}


	public void initMusicDuration() {         //��ȡmusic�Ĳ���ʱ��
		musicDurationList = new ArrayList<Integer>(readMusicInfoFromLocalDb.getMusicDuration());
	}

	public void initMusicUrl() {              //��ȡmusic�ļ���·��
		musicUrlList = new ArrayList<String>();
		musicUrlList = new ArrayList<String>(readMusicInfoFromLocalDb.getMusicUrl());
	}

	//�Ӷ�ý�����ݿ��л�ȡ��������
	private void initMusicSingerName() {
		// TODO Auto-generated method stub
		musicSingerName = new ArrayList<String>();
		musicSingerName = readMusicInfoFromLocalDb.getMusicSingerName();
	}

	public ArrayList<String> getMusicIdList() {        //����music��id�б� �� ����ʲô��������
		return musicIdList;
	}

	public void setMusicIdList(ArrayList<String> musicIdList) {        //����music��id��List , ����ʲô������
		this.musicIdList = musicIdList;
	}



	private void transferDate(int position) {       //no bug ׼�������ݴ��ݸ�PlayMusicActivity

		intent = new Intent(MyMusicActivity.this , PlayMusicActivity.class); 
		Bundle bundle = new Bundle();
		bundle.putString("music_title", musicTitleList.get(position));
		intent.putExtra("fragment" , "MyMusicFragment");                            //�����Ǵ�MyFragment������PlayMusicActivity
		intent.putExtras(bundle);
		intent.putExtra("music_url", musicUrlList.get(position));
		intent.putExtra("music_duration", musicDurationList.get(position).intValue());
		intent.putExtra("music_title_position", position); // ���ݵ�ǰ����������ListView��item��λ�ã���������ִ��˳�򲥷ţ�����
		intent.putStringArrayListExtra("music_title_list", musicTitleList);// ����musicTitleList������˳���������
		intent.putStringArrayListExtra("music_url_list", musicUrlList);           //�������ֲ��ŵ�ַ
		intent.putStringArrayListExtra("music_singer", musicSingerName);          //�����������߹�ȥ
		intent.putIntegerArrayListExtra("music_duration_list", musicDurationList);       //�������ֲ���ʱ��
		intent.putExtra("music_play_order" , playOrder);                             //�������ֵĲ���˳��
		Log.e("MyMusicActivity", playOrder);
	}


	private void beginService() {           //�������󶨷����Լ��������ݵ�service

		context = MyApplication.getContext();
		serviceIntent = new Intent(context , MusicForegroundService1.class);

		//serviceIntent.putExtra("music_title" , musicTitleList);                  //�Ѹ��������б����ݵ�service������ǰ̨service�и������Ƶı仯
		//serviceIntent.putExtra("music_title_position" , musicTitlePosition);     //��������ListView��item���ϵ�λ�ã�������ǰ̨�����ж�λ���������

		context.startService(serviceIntent);          //��������
		context.bindService(serviceIntent, serviceConnection , 0);          //�󶨷���
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (serviceIntent != null) {
			// ���ڳ����˳�ʱ���˳�service
			context.stopService(serviceIntent);        // �޷�ȷ��serviceIntentΪ��Ϊ�գ�����Ҫ�����ж����
			context.unbindService(serviceConnection);

		}


		Intent intent = new Intent(MyMusicActivity.this , FlatteningStartActivity.class);
		startActivity(intent);
		FlatteningStartActivity.isOpen = 1;
	}

	/**
	 *  ��ʼ�������й����ݣ��Լ�������ˢ�������б��󣬸����������ݣ�����
	 */
	public void initMusicData()
	{
		initMusicUrl(); // ��ȡ����·��
		initMusicDuration(); // ��ȡ����ʱ��
		initMusicId(); // �����ݿ��л�ȡ���������ݿ��е�Id
		initMusicSingerName();       //�����ݿ��ж�ȡ��������
	}



	public void updateMusicDate()
	{
		////���´����ݿ��ȡ����
		//  music = new Music();                              //�ֲ��ã����²����б���ԭ����musicûnew�����»��Ǿɵ�����!!!
		initMusicTitleList();                               //������initMusicTitleList()��ͷ��new��һ��music����!!!
		initMusicData();
		Log.e("MainActivity", "musicTitleList = " + musicTitleList.toString());

	}


	//����action Bar�����������ϵĲ˵�
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_music_activity_menu , menu);       //��R.menu.main��������������ţ�˳�򲥷ŵȵĲ˵������
		return true;
	}

	// ����˵��ϵ�item��ͻ���ô˺���!!!    no bug
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.


		int id = item.getItemId();


		if (id == R.id.action_settings)          //����ѭ��
		{
			playOrder  = "SINGLE_CIRCLE";
		}
		else if(id == R.id.play_order_item)        //��ѭ�򲥷�
		{
			playOrder = "ORDER_PLAY";
		}
		else if(id == R.id.play_random_item)          //�������
		{
			playOrder = "RANDOM_PLAY";
		}
		else if(id == R.id.play_once_item)           //ֻ����һ��
		{
			playOrder = "ONCE_PLAY";
		}
		else if(id == android.R.id.home)              //�����actionBar��෽���
		{
			finish();
		}
		return super.onOptionsItemSelected(item);
	}


}