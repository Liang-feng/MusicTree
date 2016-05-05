package com.example.easymusicplayer1.activity;

import java.io.IOException;
import java.util.ArrayList;

import com.example.easymusicplayer1.R;
import com.example.easymusicplayer1.db.ReadMusicInfo;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * 现实被锁住的界面，阿狸大哭界面
 * @author feng
 *
 */
public class SecondActivity extends Activity
{
    	public static int isOpen = 0;
	
	    MediaPlayer mediaPlayer;
	    
	    ActionBar actionBar;
	    
		private static boolean timeIsDone=true;

	    
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.second_activity);                        //没有引入布局，怪不得，listView为空!!!
		
			initActionBar();
		}
		
		/**
		 *   对当前活动的actionBar进行相关设置!
		 */
		private void initActionBar() {
			
			actionBar = this.getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setTitle("返回上一级");
			
		}
		
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {

			if(item.getItemId() == android.R.id.home)     //如果点击了actionBar左边的home键就退出activity
			
			
			{
				finish();
				if(timeIsDone)        //如果时间到了，才返回原来的activity
				{
					Intent intent = new Intent(SecondActivity.this , ChooseTimeActivity.class);
					startActivity(intent);
				}

			}
			return super.onOptionsItemSelected(item);
		}
		
		/**
		 * 设置时间是否到了
		 * @param timeIsDone1
		 */
		public static void setTimeIsDone(Boolean timeIsDone1)
		{
			timeIsDone = timeIsDone1;
		}
}
