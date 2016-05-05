package com.example.easymusicplayer1.activity;

import java.io.IOException;
import java.util.ArrayList;

import com.example.easymusicplayer1.R;
import com.example.easymusicplayer1.db.ReadMusicInfo;
import com.example.easymusicplayer1.db.ReadMusicInfoFromLocalDb;
import com.example.easymusicplayer1.model.Music;
import com.example.easymusicplayer1.utility.AppAdapter;
import com.example.easymusicplayer1.utility.LockAppIsOpenThread;
import com.example.easymusicplayer1.utility.MyApplication;
import com.example.easymusicplayer1.utility.TimeAdapter;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

@SuppressLint("UseValueOf")
public class ChooseTimeActivity extends Activity {
	
	public static int isOpen = 0;

	public static final int DELETE_TIME = 0;
	
	Context context = this;

	ListView timeListView;

	TimeAdapter timeAdapter;
	
	ArrayAdapter<String> adapter;

	ArrayList<String> timeList; // 用于adapter

	TimePickerDialog timePickerDialog;

	private static ArrayList<Integer> hourOfSetList; // 用于存放当前已经设置好的小时

	private static ArrayList<Integer> minuteOfSetList; // 用于存放当前已经设置好的分钟
	
	ArrayList<String> ringUrlList;               //用于存储铃声的播放地址

	String timeString; // 时间字符串，用于输出在ListView的Item项上

	int i = 1; // 用于标记让onSetTime里面的某几个函数只执行一次
	
	int index;               //用于索引铃声的位置

	int itemPosition = 0; // 用来记录ListView中被点击的item项的位置
	
	int itemTimeDone;          //用来记录哪个item项上的时间到了

	private static TimePicker timePicker; // 用于获取当前时间的对象

	Boolean ifSet = false;

	Boolean longClick = true; // 用来标记在设置完时间后是否执行adapter.notifyDataSetChanged();

	private final static int TIME_DONE = 1;

	private final static int CANCEL_SET_BELL = 2;

	private Boolean cancel = false; // 用来标记 在打开了时间选择器后是否退出了界面？？
	
	boolean isActivityAlive = true;           //用来当不在ChooseTimeActivity的时候，结束线程继续判断时间!!!

	MediaPlayer mediaPlayer;

	private static LockAppIsOpenThread lockAppIsOpenThread;

	private Handler handler = new Handler() { // 异步消息处理机制

		@SuppressLint("HandlerLeak")
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TIME_DONE:
				Intent intent = new Intent(MyApplication.getContext() , ChooseTimeActivity.class);
				startActivity(intent);
				
				Toast.makeText(getApplicationContext(), "时间到了", Toast.LENGTH_SHORT).show();

				initMediaPlayer(); // 时间到后播放音乐

				lockAppIsOpenThread.setTimeIsEnd(false);           // 告诉LockAppIsOpenThread线程，不要阻挡软件的使用了!
                SecondActivity.setTimeIsDone(true);                //时间到了，设置为true，使得可以从本activity退回到以前的activity
				
                setDialog();
				
				//changeSwitchState();                               //改变switch的状态
				//时间到后，启动activity
				
				break;
			case CANCEL_SET_BELL:
				break;
			}
		}

		/**
		 * 改变switch的状态
		 */
	/*	private void changeSwitchState() {
			//时间到后，设置swtich状态为关闭
			Switch timeSwicth = adapter.getItemSwitch(itemTimeDone);       //获取对应item项上的switch
			timeSwicth.setChecked(false);
			adapter.notifyDataSetChanged();            //通知ListView上的item项有改变，使得switch的状态改变可以显示出来
		}*/

		/**
		 * 当时间到后，播放音乐
		 */
		private void initMediaPlayer() {
			try {
				Log.e("ChooseTimeActivity", ringUrlList.get(index));
				mediaPlayer = new MediaPlayer();
				mediaPlayer.setDataSource(ringUrlList.get(index));
				mediaPlayer.prepare();
				mediaPlayer.start();

			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		/**
		 * 设置当时间到的时候的dialog界面
		 */
		private void setDialog() {

			View view = LayoutInflater.from(ChooseTimeActivity.this).inflate(R.layout.time_done_dialog, null);

			AlertDialog.Builder dialog = new AlertDialog.Builder(context);
			dialog.setCustomTitle(view);
			dialog.setCancelable(true);

			dialog.setPositiveButton("关闭闹铃", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					dialog.cancel();
					if (mediaPlayer.isPlaying()) {
						mediaPlayer.pause();
					}
				}
			});

			dialog.show();

		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_time_list_view);

		readDataFromActivity();
		initActionBar();

		initView();

		// 到底是为每一个闹钟都建立一个子线程监听呢，还是每个闹钟都只使用这个子线程好??????????????????
		// 还是为每一个闹钟设置一个线程监听好一点!!!
		TimeThread timeThread = new TimeThread();
		Thread thread = new Thread(timeThread);
		thread.start(); // 启动线程

		lockAppIsOpenThread = AppAdapter.getLockAppThread();
		Thread lockAppThread = new Thread(lockAppIsOpenThread);
		lockAppThread.start();

	}

	private void readDataFromActivity() {
		
		Intent intent = this.getIntent();
		this.index = intent.getIntExtra("ring_position", 0);
		
	}

	private void initView() {
		
		Toast.makeText(MyApplication.getContext(), "请设置上锁时间", Toast.LENGTH_LONG).show();

		ringUrlList = new ArrayList<String>();
		ringUrlList = new ReadMusicInfoFromLocalDb().getMusicUrl();        //从数据库获取音乐播放地址 ,  这个写法不好，还需要改进
		
		// listView的相关操作
		timeList = new ArrayList<String>(); // timeList可以先不Add东西，这样就不会建立item项了!!!
		timeListView = (ListView) findViewById(R.id.time_list_view);
		adapter = new ArrayAdapter<String>(ChooseTimeActivity.this, android.R.layout.simple_list_item_1  , timeList); // 出事化adater
		timeListView.setAdapter(adapter);

		timeListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				itemPosition = position;
				// 避免在判断时间是否到达的子线程中继续判断在ListViewitem项上没有的数据!
				try
				{
					if (hourOfSetList.get(position) != null && minuteOfSetList.get(position) != null)
					{
						hourOfSetList.remove(position);
						minuteOfSetList.remove(position);
					}
				} 
				catch (IndexOutOfBoundsException e)
				{
					e.printStackTrace();
				}
				longClick = false;            // 先不执行adapter.notifyDataSetChanged();
				setBell();                    // 创建一个定时闹钟
			}
		});
		
	
		timeListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				
				menu.add(0, 0, 0, "删除");
				}
		});

		hourOfSetList = new ArrayList<Integer>();
		minuteOfSetList = new ArrayList<Integer>();

	}
	

	private void initActionBar() {

		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle("设置时间");
	}

	private class TimeThread implements Runnable {

		/**
		 * 用来监听事件是否到达用户所设置的时间
		 */
		@Override
		public void run() {

			while (isActivityAlive ) {

				timePicker = new TimePicker(ChooseTimeActivity.this);
				// 如果当前时间已经 用户设置的时间，就跳出循环 , 判断的时候要遍历所有的ListView上的item项
				for (int index = 0; index < hourOfSetList.size(); index++) {

					if (timePicker.getCurrentHour().intValue() == hourOfSetList.get(index).intValue()
							&& timePicker.getCurrentMinute().intValue() == minuteOfSetList.get(index).intValue()) {
						Log.e("MainActivity", "闹钟" + new Integer(index).toString() + "  时间到了!!!");
						// 时间到了的listView上的item项无效了!!!
						hourOfSetList.remove(index);
						minuteOfSetList.remove(index);
						
						itemTimeDone = index;                //用来记录是哪个item项上的时间到了!!
						// 当时间到后发送message，在主线程更新ui
						sendMessageToHandler();

						// 为了避免时间到后的一分钟内不断出现progressDialog！
						try {
							Thread.sleep(60000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}

				try { // 放在外面，原因是当有几个相同闹钟时，岂不是只能判断一个闹钟，如果放在for循环里面的话！！！
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();

				}

			}

		}

		/**
		 * 时间到了就从子线程中发送message到handler处理ui
		 */
		private void sendMessageToHandler() {
			Message message = new Message();
			message.what = TIME_DONE;
			handler.sendMessage(message);
		}
	}

	/**
	 * 初始化时间列表的数据
	 */
	private void initDate1() {

		this.timeList.add(timeString);
	}

	TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {

		@Override // 比onCancleListener函数调用的早!!! 这个函数会被调用两次!!!
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

			if (i % 2 == 0) // 让内部函数只执行一次 , 即按下确定键才执行下面的，如果取消设置就不执行下面的!!!
			{
				// 输出时间
				if (minute < 10 && minute >= 0) {
					// timeString = String.format("%d:%d%d", hourOfDay , 0 ,
					// minute);
					timeString = hourOfDay + ":" + "0" + minute; // 设置标准的时间形式
				} else if (minute >= 10 && minute <= 60) // 加了else if
															// 而不是else，防止取消时间选择器时候，minute没有值，而执行了下面
				{
					// timeString = String.format("%d:%d", hourOfDay , minute);
					timeString = hourOfDay + ":" + minute; // 设置标准的时间形式
				}

				if (longClick) // 如果是通过菜单项来设置闹钟，则执行下面
				{
					initDate1(); // 只能执行一次，不然ListView的item项多出一项来!!!
				} 
				else // 如果是通过点击item项，更改闹钟则执行下面!!!
				{
					//当闹钟响后，timeList中的元素并没删除，而是hourOfSetList和minuteOfSetList中的删除而已，所以不用加异常捕捉
					timeList.set(itemPosition, timeString); // 点到哪个item项就替换哪个item项
					longClick = true; // 让通过菜单项设置闹钟后回归正常执行步骤
				}

				adapter.notifyDataSetChanged();

				hourOfSetList.add(hourOfDay); // 用于在子线程中判断是否到达当前时间
				minuteOfSetList.add(minute);

				remindUserTimeInfo(hourOfDay, minute); // 提醒用户闹钟几时响起

				//if(lockAppIsOpenThread != null)                    //
				{
	    			lockAppIsOpenThread.setTimeIsEnd(true);          // 开始监听被锁软件是否打开
	    			SecondActivity.setTimeIsDone(false);             //防止在SecondActivity返回上一级activity，进而退出了上锁功能
				}
			}
			i++;

		}


	};
	
	
	/**
	 * 这个函数的下面else if的两部分，可以修改一下，代码重用! 提醒用户还有多少时间到自己设置的时间!!!
	 * 
	 * @param hourOfDay
	 *            onTimeSet传下来的
	 * @param minute
	 *            onTimeSet传下来的
	 */
	private static void remindUserTimeInfo(int hourOfDay, int minute) {
		// 设置提醒，提醒用户什么时候闹钟将会响起!!!
		timePicker = new TimePicker(MyApplication.getContext());
		// 告诉用户：设置的时间会在一小时内响起！当设置的是在1小时以内的时间的时候!!!
		if (hourOfDay == timePicker.getCurrentHour().intValue()
				&& minute > timePicker.getCurrentMinute().intValue()) {
			Toast.makeText(MyApplication.getContext(),
					"闹钟将会在" + new Integer(minute - timePicker.getCurrentMinute()).toString() + "分钟后响起",
					Toast.LENGTH_SHORT).show();
		}
		// 当设置的时间大于当前的时间，即在今天内的时间
		else if (hourOfDay > timePicker.getCurrentHour().intValue()) {

			int m = 0;
			if (minute != timePicker.getCurrentMinute().intValue()) {
				if (minute > timePicker.getCurrentMinute().intValue()) {
					m = minute - timePicker.getCurrentMinute();
				} else if (minute < timePicker.getCurrentMinute().intValue()) {
					m = 60 - timePicker.getCurrentMinute() + minute;
					hourOfDay--;
				}
				Toast.makeText(MyApplication.getContext(),
						"闹钟将会在" + new Integer(hourOfDay - timePicker.getCurrentHour()).toString() + "小时"
								+ new Integer(m).toString() + "分钟后响起",
						Toast.LENGTH_SHORT).show();
			} else if (minute == timePicker.getCurrentMinute().intValue()) {
				Toast.makeText(MyApplication.getContext(),
						"闹钟将会在" + new Integer(hourOfDay - timePicker.getCurrentHour()).toString() + "小时后响起",
						Toast.LENGTH_SHORT).show();
			}

		}
		// 当设置的小时小于当前的时间的时候
		else if (hourOfDay < timePicker.getCurrentHour().intValue()) {
			int hour;
			int m = 0;
			hour = 23 - timePicker.getCurrentHour().intValue();
			hour += hourOfDay + 1;
			if (minute != timePicker.getCurrentMinute().intValue()) {
				if (minute > timePicker.getCurrentMinute().intValue()) {
					m = minute - timePicker.getCurrentMinute();
				} else if (minute < timePicker.getCurrentMinute().intValue()) {
					m = 60 - timePicker.getCurrentMinute() + minute;
					hour--;
				}
				Toast.makeText(MyApplication.getContext(), "闹钟将会在" + hour + "小时" + m + "分钟后响起", Toast.LENGTH_SHORT)
						.show();
			} else if (minute == timePicker.getCurrentMinute().intValue()) {
				Toast.makeText(MyApplication.getContext(), "闹钟将会在" + hour + "小时后响起", Toast.LENGTH_SHORT).show();
			}

		} else if (hourOfDay == timePicker.getCurrentHour().intValue()
				&& minute == timePicker.getCurrentMinute().intValue()) {
			Toast.makeText(MyApplication.getContext(), "闹钟将会在" + 24 + "小时后响起", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 设置闹铃
	 */
	public void setBell() // 因为考虑到有可能会被音乐app中的其它类引用到，所以为public
	{
		timePicker = new TimePicker(ChooseTimeActivity.this); // 每次获取新的当前时间，都要进行重新new
		// 一个对象 ，
		// 用来在TimePicker设置当前时间

		// 创建一个新的时间选择器对话框 , 已经相关参数设置
		timePickerDialog = new TimePickerDialog(ChooseTimeActivity.this, listener, timePicker.getCurrentHour(),
				timePicker.getCurrentMinute(), true);
		timePickerDialog.show(); // 显示时间选择器

		timePickerDialog.setOnCancelListener(new OnCancelListener() { // 设置取消时间选择器的监听事件
																		// ,
																		// 比onTimeSet()函数调用的迟!!!

			@Override
			public void onCancel(DialogInterface dialog) {

				i--; // 如果取消了时间选择器，则i--，这样就避免了，onTimeSet()函数里面的initDate1()和notifyDataChange()函数的胡乱调用!!!
				lockAppIsOpenThread.setTimeIsEnd(false); // 避免取消时间后还继续锁软件
			}
		});

	}
/*
	/**
	 * 用于TimeAdapter操作switch时调用
	 */
	public static void addElementToHourOfList(int hour)
	{
		hourOfSetList.add(hour);
	}
	
	/**
	 * 用于TimeAdapter操作switch时调用
	 */
	public static void addElementToMinuteOfList(int minute)
	{
		minuteOfSetList.add(minute);
	}
	
	public static void removeElementFromHourOfList(int hour)
	{
		hourOfSetList.remove(hour);
	}
	
	public static void removeElementFromMinuteOfList(int minute)
	{
		minuteOfSetList.remove(minute);
	}
	
	/**
	 * 
	 * @param index      第index个item项
	 * @param bool       是否为开启闹钟
	 */
	public static void setRelativeOperation(int index , Boolean bool)        
	{
		if(bool)
		{
			Log.e("ChooseTimeActivity", "index = " + index);
			remindUserTimeInfo(hourOfSetList.get(index) , minuteOfSetList.get(index)); // 提醒用户闹钟几时响起
		}
		
		lockAppIsOpenThread.setTimeIsEnd(bool);          // 开始监听被锁软件是否打开
		SecondActivity.setTimeIsDone(!bool);             //防止在SecondActivity返回上一级activity，进而退出了上锁功能
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.choose_time_activity_menu, menu);
		return true;
	}
	
	/**
	 * 当一个上下文菜单中的item项被点击时，会被调用
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		AdapterView.AdapterContextMenuInfo contextMenuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int index = (int) contextMenuInfo.id;
		
		if(item.getItemId() == DELETE_TIME)                //如果点击了删除
		{
			String[] timeArray = timeList.get(index).split(":");
			int hour = Integer.valueOf(timeArray[0]).intValue();
			int minute = Integer.valueOf(timeArray[1]).intValue();
			timeList.remove(index);
			
			hourOfSetList.remove(Integer.valueOf(hour));
			minuteOfSetList.remove(Integer.valueOf(minute));
			adapter.notifyDataSetChanged();
			if(timeList.isEmpty())                              //如果在删除了时间之后，
			{
				lockAppIsOpenThread.setTimeIsEnd(false);           
			}
			Log.e("ChooseTimeActivity", "已删除");
			Toast.makeText(MyApplication.getContext(), "删除成功", Toast.LENGTH_SHORT).show();
		}
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.add_a_bell) {

			setBell(); // 创建一个定时闹钟

			return true;
		} else if (id == android.R.id.home) {
			if (mediaPlayer != null) {
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.pause();
					mediaPlayer.release();
				}
			}
			isActivityAlive = false;                 //当此activity退出的时候，让子线程结束!
			lockAppIsOpenThread.setIsActivityAlive(false);
			finish();
			Intent intent = new Intent(ChooseTimeActivity.this, SetARingActivity.class);
			startActivity(intent);
			this.isOpen = 0;
			SetARingActivity.isOpen = 1;
		}

		return super.onOptionsItemSelected(item);
	}

}