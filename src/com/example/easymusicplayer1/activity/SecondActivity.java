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
 * ��ʵ����ס�Ľ��棬�����޽���
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
			setContentView(R.layout.second_activity);                        //û�����벼�֣��ֲ��ã�listViewΪ��!!!
		
			initActionBar();
		}
		
		/**
		 *   �Ե�ǰ���actionBar�����������!
		 */
		private void initActionBar() {
			
			actionBar = this.getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setTitle("������һ��");
			
		}
		
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {

			if(item.getItemId() == android.R.id.home)     //��������actionBar��ߵ�home�����˳�activity
			
			
			{
				finish();
				if(timeIsDone)        //���ʱ�䵽�ˣ��ŷ���ԭ����activity
				{
					Intent intent = new Intent(SecondActivity.this , ChooseTimeActivity.class);
					startActivity(intent);
				}

			}
			return super.onOptionsItemSelected(item);
		}
		
		/**
		 * ����ʱ���Ƿ���
		 * @param timeIsDone1
		 */
		public static void setTimeIsDone(Boolean timeIsDone1)
		{
			timeIsDone = timeIsDone1;
		}
}
