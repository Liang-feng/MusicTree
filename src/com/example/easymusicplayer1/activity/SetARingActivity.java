package com.example.easymusicplayer1.activity;
import java.io.IOException;
import java.util.ArrayList;

import com.example.easymusicplayer1.R;
import com.example.easymusicplayer1.db.ReadMusicInfo;
import com.example.easymusicplayer1.db.ReadMusicInfoFromLocalDb;
import com.example.easymusicplayer1.model.Music;
import com.example.easymusicplayer1.model.Ring;
import com.example.easymusicplayer1.utility.MyApplication;
import com.example.easymusicplayer1.utility.RingAdapter;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * ����ʱ�䵽�������
 * @author feng
 *
 */
public class SetARingActivity extends Activity {
	
	public static int isOpen = 0;
	
//	ArrayList<Ring> ringList ;
	ArrayList<String> ringList ;

	ArrayList<String> ringUrlList;
	
	int index = 0;               //��������������λ�ã����û��������������ô��Ĭ���ǵ�һ�׸�
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_a_ring_list_view);
		
		initView();
	
	}

	
	private void initView() {
		
		Toast.makeText(MyApplication.getContext() , "��ѡ����������ѡ��Ĭ��Ϊ��һ��", Toast.LENGTH_LONG).show();
		initActionBar();

		ringList = new ArrayList<String>();		
		
		initRingList();
		ListView listView = (ListView) findViewById(R.id.ring_list_view);
		//RingAdapter adapter = new RingAdapter(SetARingActivity.this , R.layout.set_a_ring_activity  , ringList);
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(SetARingActivity.this ,
	                           		android.R.layout.simple_list_item_1 , ringList);
		listView.setAdapter(adapter);		
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				index = position;
				//ѡ���˸�����Ϳ���ֱ��ѡʱ���ˣ�Ҳ���Բ�ѡ�����������Ͻǵ�menu������һ������!!!
				Intent intent = new Intent(SetARingActivity.this , ChooseTimeActivity.class);
				intent.putExtra("ring_position", index);
				startActivity(intent);
			}

		});
	}

	
	
	private void initActionBar() {
		
		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle("ѡ������");
		
	}

	private void initRingList() {
	
		ReadMusicInfoFromLocalDb  readMusicInfoFromLocalDb = new ReadMusicInfoFromLocalDb();
        ringList = readMusicInfoFromLocalDb.getMusicTitle();		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		this.getMenuInflater().inflate(R.menu.set_a_ring_activity_menu, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if(item.getItemId() == R.id.set_lock_time)
		{
			Intent intent = new Intent(SetARingActivity.this , ChooseTimeActivity.class);
			intent.putExtra("ring_position", index);
			startActivity(intent);
			ChooseTimeActivity.isOpen = 1;
			this.isOpen = 0;
		}
		else if(item.getItemId() == android.R.id.home)       //�����������������ô�ͻ�Ĭ���ǵ�һ�׸���
		{
			finish();
			Intent intent = new Intent(SetARingActivity.this , TimingLockAppActivity.class);
			startActivity(intent);
			TimingLockAppActivity.isOpen = 1;
			this.isOpen = 0;
		}
		return super.onOptionsItemSelected(item);
	}

}
