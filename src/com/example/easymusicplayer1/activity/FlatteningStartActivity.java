package com.example.easymusicplayer1.activity;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.Header;

import com.example.easymusicplayer1.R;
import com.example.easymusicplayer1.model.App;
import com.example.easymusicplayer1.utility.DataDipose;
import com.example.easymusicplayer1.utility.GetAppListThread;
import com.example.easymusicplayer1.utility.InitAppList;
import com.example.easymusicplayer1.utility.ScanMusicFile;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

/**
 * 初始界面，显示“我的音乐”，“音乐之家”等。
 * @author feng
 *
 */
public class FlatteningStartActivity extends Activity implements OnClickListener{
	
	public static int isOpen = 0;                       //0为不是当前打开的
	/**“我的音乐”父布局*/
    LinearLayout myMusicLinearLayout;                   //“我的音乐”父布局
    /**“音乐馆”父布局*/
    LinearLayout musicHomeLinearLayout;                //“音乐馆”父布局
    /**“特殊功能”父布局*/
    LinearLayout specificFunctionLinearLayout;        //“特殊功能”父布局

    Context context;
    /**app列表,存储app信息*/
    ArrayList<App> appList;                     
    /**获取本地手机所有app的信息*/
    GetAppListThread getAppListThread;
    /**用来标记MusicTopActivity是否被打开*/
    private static int time = 0;                             //用来标记MusicTopActivity是否被打开

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flattening_start_activity);
        
		ScanMusicFile scanMusicFile = new ScanMusicFile();
		scanMusicFile.scanMusic(FlatteningStartActivity.this);           //防止在打开“我的音乐”的时候，出现不存在的音乐文件的列表,由于删除不正常

        context = this;

        initActionBar();
        initView();       //获得布局的控件实例，以及设置它们的监听事件
        
        getAppListThread = new GetAppListThread();
        Thread thread = new Thread(getAppListThread);
        thread.start();
  
    }
    
	
    /**
     * 获得布局的控件实例，以及设置它们的监听事件
     */
	private void initActionBar() {
		
		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setCustomView(R.layout.custom_action_bar);              //为actionBar设置一个自定义布局
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);       //显示自定义actionBar,如果自定义actionBAr被设置了的话！
	}



	/**
     * 获得布局的控件实例，以及设置它们的监听事件
     */
    public void initView()
    {
        this.musicHomeLinearLayout = (LinearLayout) findViewById(R.id.music_home_layout);
        this.myMusicLinearLayout = (LinearLayout) findViewById(R.id.my_music_layout);
        this.specificFunctionLinearLayout = (LinearLayout) findViewById(R.id.specific_function_layout);

        myMusicLinearLayout.setOnClickListener(this);
        musicHomeLinearLayout.setOnClickListener(this);
        specificFunctionLinearLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch(v.getId())
        {
            case R.id.my_music_layout:
                Intent intent = new Intent(FlatteningStartActivity.this , MyMusicActivity.class);
                context.startActivity(intent);
                MyMusicActivity.isOpen = 1;
                this.isOpen = 0;
                break;
            case R.id.music_home_layout:
            	Intent intent1 = new Intent(FlatteningStartActivity.this , MusicTopActivity.class);
            	intent1.putExtra("music_top_activity_open_time", time);
                time++;                             //标记MusicTopActivity的打开次数，用来决定是否从网上获取数据
                Log.e("FlatteningStartActivity", String.valueOf(time));
                context.startActivity(intent1);
                MusicTopActivity.isOpen = 1;
                this.isOpen = 0;
                break;
            case R.id.specific_function_layout:
            	Intent intent2 = new Intent(FlatteningStartActivity.this , TimingLockAppActivity.class);
                //由于在点击特殊功能时才加载数据的话，由于ListView的item项多，而且数据多，所以会有点慢才显示出TimingLockAppActivity，所以先加载数据
                //intent2.putExtra("get_app_list_thread", getAppListThread);
                context.startActivity(intent2);
                TimingLockAppActivity.isOpen = 1;
                this.isOpen = 0;
                break;
        }

    }

}
