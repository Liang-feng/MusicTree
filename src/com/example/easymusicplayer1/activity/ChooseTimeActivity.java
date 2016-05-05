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

	ArrayList<String> timeList; // ����adapter

	TimePickerDialog timePickerDialog;

	private static ArrayList<Integer> hourOfSetList; // ���ڴ�ŵ�ǰ�Ѿ����úõ�Сʱ

	private static ArrayList<Integer> minuteOfSetList; // ���ڴ�ŵ�ǰ�Ѿ����úõķ���
	
	ArrayList<String> ringUrlList;               //���ڴ洢�����Ĳ��ŵ�ַ

	String timeString; // ʱ���ַ��������������ListView��Item����

	int i = 1; // ���ڱ����onSetTime�����ĳ��������ִֻ��һ��
	
	int index;               //��������������λ��

	int itemPosition = 0; // ������¼ListView�б������item���λ��
	
	int itemTimeDone;          //������¼�ĸ�item���ϵ�ʱ�䵽��

	private static TimePicker timePicker; // ���ڻ�ȡ��ǰʱ��Ķ���

	Boolean ifSet = false;

	Boolean longClick = true; // ���������������ʱ����Ƿ�ִ��adapter.notifyDataSetChanged();

	private final static int TIME_DONE = 1;

	private final static int CANCEL_SET_BELL = 2;

	private Boolean cancel = false; // ������� �ڴ���ʱ��ѡ�������Ƿ��˳��˽��棿��
	
	boolean isActivityAlive = true;           //����������ChooseTimeActivity��ʱ�򣬽����̼߳����ж�ʱ��!!!

	MediaPlayer mediaPlayer;

	private static LockAppIsOpenThread lockAppIsOpenThread;

	private Handler handler = new Handler() { // �첽��Ϣ�������

		@SuppressLint("HandlerLeak")
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TIME_DONE:
				Intent intent = new Intent(MyApplication.getContext() , ChooseTimeActivity.class);
				startActivity(intent);
				
				Toast.makeText(getApplicationContext(), "ʱ�䵽��", Toast.LENGTH_SHORT).show();

				initMediaPlayer(); // ʱ�䵽�󲥷�����

				lockAppIsOpenThread.setTimeIsEnd(false);           // ����LockAppIsOpenThread�̣߳���Ҫ�赲�����ʹ����!
                SecondActivity.setTimeIsDone(true);                //ʱ�䵽�ˣ�����Ϊtrue��ʹ�ÿ��Դӱ�activity�˻ص���ǰ��activity
				
                setDialog();
				
				//changeSwitchState();                               //�ı�switch��״̬
				//ʱ�䵽������activity
				
				break;
			case CANCEL_SET_BELL:
				break;
			}
		}

		/**
		 * �ı�switch��״̬
		 */
	/*	private void changeSwitchState() {
			//ʱ�䵽������swtich״̬Ϊ�ر�
			Switch timeSwicth = adapter.getItemSwitch(itemTimeDone);       //��ȡ��Ӧitem���ϵ�switch
			timeSwicth.setChecked(false);
			adapter.notifyDataSetChanged();            //֪ͨListView�ϵ�item���иı䣬ʹ��switch��״̬�ı������ʾ����
		}*/

		/**
		 * ��ʱ�䵽�󣬲�������
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
		 * ���õ�ʱ�䵽��ʱ���dialog����
		 */
		private void setDialog() {

			View view = LayoutInflater.from(ChooseTimeActivity.this).inflate(R.layout.time_done_dialog, null);

			AlertDialog.Builder dialog = new AlertDialog.Builder(context);
			dialog.setCustomTitle(view);
			dialog.setCancelable(true);

			dialog.setPositiveButton("�ر�����", new OnClickListener() {

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

		// ������Ϊÿһ�����Ӷ�����һ�����̼߳����أ�����ÿ�����Ӷ�ֻʹ��������̺߳�??????????????????
		// ����Ϊÿһ����������һ���̼߳�����һ��!!!
		TimeThread timeThread = new TimeThread();
		Thread thread = new Thread(timeThread);
		thread.start(); // �����߳�

		lockAppIsOpenThread = AppAdapter.getLockAppThread();
		Thread lockAppThread = new Thread(lockAppIsOpenThread);
		lockAppThread.start();

	}

	private void readDataFromActivity() {
		
		Intent intent = this.getIntent();
		this.index = intent.getIntExtra("ring_position", 0);
		
	}

	private void initView() {
		
		Toast.makeText(MyApplication.getContext(), "����������ʱ��", Toast.LENGTH_LONG).show();

		ringUrlList = new ArrayList<String>();
		ringUrlList = new ReadMusicInfoFromLocalDb().getMusicUrl();        //�����ݿ��ȡ���ֲ��ŵ�ַ ,  ���д�����ã�����Ҫ�Ľ�
		
		// listView����ز���
		timeList = new ArrayList<String>(); // timeList�����Ȳ�Add�����������Ͳ��Ὠ��item����!!!
		timeListView = (ListView) findViewById(R.id.time_list_view);
		adapter = new ArrayAdapter<String>(ChooseTimeActivity.this, android.R.layout.simple_list_item_1  , timeList); // ���»�adater
		timeListView.setAdapter(adapter);

		timeListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				itemPosition = position;
				// �������ж�ʱ���Ƿ񵽴�����߳��м����ж���ListViewitem����û�е�����!
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
				longClick = false;            // �Ȳ�ִ��adapter.notifyDataSetChanged();
				setBell();                    // ����һ����ʱ����
			}
		});
		
	
		timeListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				
				menu.add(0, 0, 0, "ɾ��");
				}
		});

		hourOfSetList = new ArrayList<Integer>();
		minuteOfSetList = new ArrayList<Integer>();

	}
	

	private void initActionBar() {

		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle("����ʱ��");
	}

	private class TimeThread implements Runnable {

		/**
		 * ���������¼��Ƿ񵽴��û������õ�ʱ��
		 */
		@Override
		public void run() {

			while (isActivityAlive ) {

				timePicker = new TimePicker(ChooseTimeActivity.this);
				// �����ǰʱ���Ѿ� �û����õ�ʱ�䣬������ѭ�� , �жϵ�ʱ��Ҫ�������е�ListView�ϵ�item��
				for (int index = 0; index < hourOfSetList.size(); index++) {

					if (timePicker.getCurrentHour().intValue() == hourOfSetList.get(index).intValue()
							&& timePicker.getCurrentMinute().intValue() == minuteOfSetList.get(index).intValue()) {
						Log.e("MainActivity", "����" + new Integer(index).toString() + "  ʱ�䵽��!!!");
						// ʱ�䵽�˵�listView�ϵ�item����Ч��!!!
						hourOfSetList.remove(index);
						minuteOfSetList.remove(index);
						
						itemTimeDone = index;                //������¼���ĸ�item���ϵ�ʱ�䵽��!!
						// ��ʱ�䵽����message�������̸߳���ui
						sendMessageToHandler();

						// Ϊ�˱���ʱ�䵽���һ�����ڲ��ϳ���progressDialog��
						try {
							Thread.sleep(60000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}

				try { // �������棬ԭ���ǵ��м�����ͬ����ʱ������ֻ���ж�һ�����ӣ��������forѭ������Ļ�������
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();

				}

			}

		}

		/**
		 * ʱ�䵽�˾ʹ����߳��з���message��handler����ui
		 */
		private void sendMessageToHandler() {
			Message message = new Message();
			message.what = TIME_DONE;
			handler.sendMessage(message);
		}
	}

	/**
	 * ��ʼ��ʱ���б������
	 */
	private void initDate1() {

		this.timeList.add(timeString);
	}

	TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {

		@Override // ��onCancleListener�������õ���!!! ��������ᱻ��������!!!
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

			if (i % 2 == 0) // ���ڲ�����ִֻ��һ�� , ������ȷ������ִ������ģ����ȡ�����þͲ�ִ�������!!!
			{
				// ���ʱ��
				if (minute < 10 && minute >= 0) {
					// timeString = String.format("%d:%d%d", hourOfDay , 0 ,
					// minute);
					timeString = hourOfDay + ":" + "0" + minute; // ���ñ�׼��ʱ����ʽ
				} else if (minute >= 10 && minute <= 60) // ����else if
															// ������else����ֹȡ��ʱ��ѡ����ʱ��minuteû��ֵ����ִ��������
				{
					// timeString = String.format("%d:%d", hourOfDay , minute);
					timeString = hourOfDay + ":" + minute; // ���ñ�׼��ʱ����ʽ
				}

				if (longClick) // �����ͨ���˵������������ӣ���ִ������
				{
					initDate1(); // ֻ��ִ��һ�Σ���ȻListView��item����һ����!!!
				} 
				else // �����ͨ�����item�����������ִ������!!!
				{
					//���������timeList�е�Ԫ�ز�ûɾ��������hourOfSetList��minuteOfSetList�е�ɾ�����ѣ����Բ��ü��쳣��׽
					timeList.set(itemPosition, timeString); // �㵽�ĸ�item����滻�ĸ�item��
					longClick = true; // ��ͨ���˵����������Ӻ�ع�����ִ�в���
				}

				adapter.notifyDataSetChanged();

				hourOfSetList.add(hourOfDay); // ���������߳����ж��Ƿ񵽴ﵱǰʱ��
				minuteOfSetList.add(minute);

				remindUserTimeInfo(hourOfDay, minute); // �����û����Ӽ�ʱ����

				//if(lockAppIsOpenThread != null)                    //
				{
	    			lockAppIsOpenThread.setTimeIsEnd(true);          // ��ʼ������������Ƿ��
	    			SecondActivity.setTimeIsDone(false);             //��ֹ��SecondActivity������һ��activity�������˳�����������
				}
			}
			i++;

		}


	};
	
	
	/**
	 * �������������else if�������֣������޸�һ�£���������! �����û����ж���ʱ�䵽�Լ����õ�ʱ��!!!
	 * 
	 * @param hourOfDay
	 *            onTimeSet��������
	 * @param minute
	 *            onTimeSet��������
	 */
	private static void remindUserTimeInfo(int hourOfDay, int minute) {
		// �������ѣ������û�ʲôʱ�����ӽ�������!!!
		timePicker = new TimePicker(MyApplication.getContext());
		// �����û������õ�ʱ�����һСʱ�����𣡵����õ�����1Сʱ���ڵ�ʱ���ʱ��!!!
		if (hourOfDay == timePicker.getCurrentHour().intValue()
				&& minute > timePicker.getCurrentMinute().intValue()) {
			Toast.makeText(MyApplication.getContext(),
					"���ӽ�����" + new Integer(minute - timePicker.getCurrentMinute()).toString() + "���Ӻ�����",
					Toast.LENGTH_SHORT).show();
		}
		// �����õ�ʱ����ڵ�ǰ��ʱ�䣬���ڽ����ڵ�ʱ��
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
						"���ӽ�����" + new Integer(hourOfDay - timePicker.getCurrentHour()).toString() + "Сʱ"
								+ new Integer(m).toString() + "���Ӻ�����",
						Toast.LENGTH_SHORT).show();
			} else if (minute == timePicker.getCurrentMinute().intValue()) {
				Toast.makeText(MyApplication.getContext(),
						"���ӽ�����" + new Integer(hourOfDay - timePicker.getCurrentHour()).toString() + "Сʱ������",
						Toast.LENGTH_SHORT).show();
			}

		}
		// �����õ�СʱС�ڵ�ǰ��ʱ���ʱ��
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
				Toast.makeText(MyApplication.getContext(), "���ӽ�����" + hour + "Сʱ" + m + "���Ӻ�����", Toast.LENGTH_SHORT)
						.show();
			} else if (minute == timePicker.getCurrentMinute().intValue()) {
				Toast.makeText(MyApplication.getContext(), "���ӽ�����" + hour + "Сʱ������", Toast.LENGTH_SHORT).show();
			}

		} else if (hourOfDay == timePicker.getCurrentHour().intValue()
				&& minute == timePicker.getCurrentMinute().intValue()) {
			Toast.makeText(MyApplication.getContext(), "���ӽ�����" + 24 + "Сʱ������", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * ��������
	 */
	public void setBell() // ��Ϊ���ǵ��п��ܻᱻ����app�е����������õ�������Ϊpublic
	{
		timePicker = new TimePicker(ChooseTimeActivity.this); // ÿ�λ�ȡ�µĵ�ǰʱ�䣬��Ҫ��������new
		// һ������ ��
		// ������TimePicker���õ�ǰʱ��

		// ����һ���µ�ʱ��ѡ�����Ի��� , �Ѿ���ز�������
		timePickerDialog = new TimePickerDialog(ChooseTimeActivity.this, listener, timePicker.getCurrentHour(),
				timePicker.getCurrentMinute(), true);
		timePickerDialog.show(); // ��ʾʱ��ѡ����

		timePickerDialog.setOnCancelListener(new OnCancelListener() { // ����ȡ��ʱ��ѡ�����ļ����¼�
																		// ,
																		// ��onTimeSet()�������õĳ�!!!

			@Override
			public void onCancel(DialogInterface dialog) {

				i--; // ���ȡ����ʱ��ѡ��������i--�������ͱ����ˣ�onTimeSet()���������initDate1()��notifyDataChange()�����ĺ��ҵ���!!!
				lockAppIsOpenThread.setTimeIsEnd(false); // ����ȡ��ʱ��󻹼��������
			}
		});

	}
/*
	/**
	 * ����TimeAdapter����switchʱ����
	 */
	public static void addElementToHourOfList(int hour)
	{
		hourOfSetList.add(hour);
	}
	
	/**
	 * ����TimeAdapter����switchʱ����
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
	 * @param index      ��index��item��
	 * @param bool       �Ƿ�Ϊ��������
	 */
	public static void setRelativeOperation(int index , Boolean bool)        
	{
		if(bool)
		{
			Log.e("ChooseTimeActivity", "index = " + index);
			remindUserTimeInfo(hourOfSetList.get(index) , minuteOfSetList.get(index)); // �����û����Ӽ�ʱ����
		}
		
		lockAppIsOpenThread.setTimeIsEnd(bool);          // ��ʼ������������Ƿ��
		SecondActivity.setTimeIsDone(!bool);             //��ֹ��SecondActivity������һ��activity�������˳�����������
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.choose_time_activity_menu, menu);
		return true;
	}
	
	/**
	 * ��һ�������Ĳ˵��е�item����ʱ���ᱻ����
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		AdapterView.AdapterContextMenuInfo contextMenuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int index = (int) contextMenuInfo.id;
		
		if(item.getItemId() == DELETE_TIME)                //��������ɾ��
		{
			String[] timeArray = timeList.get(index).split(":");
			int hour = Integer.valueOf(timeArray[0]).intValue();
			int minute = Integer.valueOf(timeArray[1]).intValue();
			timeList.remove(index);
			
			hourOfSetList.remove(Integer.valueOf(hour));
			minuteOfSetList.remove(Integer.valueOf(minute));
			adapter.notifyDataSetChanged();
			if(timeList.isEmpty())                              //�����ɾ����ʱ��֮��
			{
				lockAppIsOpenThread.setTimeIsEnd(false);           
			}
			Log.e("ChooseTimeActivity", "��ɾ��");
			Toast.makeText(MyApplication.getContext(), "ɾ���ɹ�", Toast.LENGTH_SHORT).show();
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

			setBell(); // ����һ����ʱ����

			return true;
		} else if (id == android.R.id.home) {
			if (mediaPlayer != null) {
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.pause();
					mediaPlayer.release();
				}
			}
			isActivityAlive = false;                 //����activity�˳���ʱ�������߳̽���!
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