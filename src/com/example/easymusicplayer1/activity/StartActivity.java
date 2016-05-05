package com.example.easymusicplayer1.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.Header;

import com.example.easymusicplayer1.R;
import com.example.easymusicplayer1.utility.DataDipose;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

/**
 * 开始界面，显示MusicTree图片，在启动图片过程中，做一些数据初始化操作，比如获取”音乐之家“的数据。
 * @author feng
 *
 */
public class StartActivity extends Activity {
	
	public static int isOpen = 1;
	
	MediaPlayer mediaPlayer;
	
	//主线程
	AsyncHttpResponseHandler resHandler = new AsyncHttpResponseHandler(){
		
		public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable e) {
			
			Toast.makeText(StartActivity.this, "当前没有可用网络", Toast.LENGTH_LONG).show();
			
			Log.e("StartActivity", "获得数据失败");
			//鍋氫竴浜涘紓甯稿鐞�
			e.printStackTrace();
		}
		
		 //从网络上请求数据成功就调用这个!!!
		public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {      
			try { 
				Toast.makeText(StartActivity.this, "加载成功", Toast.LENGTH_SHORT).show();
				String data = new String(responseBody,"utf-8");
				long b=System.currentTimeMillis();
		    	//long a=(Long) txt.getTag();
				System.out.println("response is :"+new String(responseBody,"utf-8"));
				//System.out.println("used time is :"+(b-a));
				
				DataDipose.dealString(data);      //解析返回来的数据，并且存储到数据库里面!!!
				
				Log.e("StartActivity", "成功获得数据");
				//鍦ㄦ瀵硅繑鍥炲唴瀹瑰仛澶勭悊
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
	}};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.start_activty);
		
		
		/**
		 * 启动子线程来从网络获取音乐数据,注意经过 我自己测试得，到底是先启动MainActivity还是先执行onFailure()函数或者
		 * 是onSuccess()函数，是由线程内的sleep决定的，相互之间没有影响!!!
		 */
	    new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				//requestDataFromInternet();            //提前开始进行网络请求，防止反应不过来，arrayList为空,而报错!!!
				
				if(distinguishNowNetWork() == 13)
				{
					try {
						Thread.sleep(1000);      //如果是4G网络，减短从网络上获取数据的时间!!
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
                else
                {
					// 注意主线程不要设置sleep，不然控件都显示不出来!!!
					try {
						Thread.sleep(5000); // 如果是3G网络，有足够的时间从网络上获取数据!!
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				
				Intent intent = new Intent(StartActivity.this , FlatteningStartActivity.class);
			    startActivity(intent);
			    finish();
			    
			    
			    FlatteningStartActivity.isOpen = 1;

			}
		}).start();
	}
	


	/**
	 * 返回当前网络是多少G网络，如果是4G网络则快点进入到MainActivity中去!!!
	 * @return
	 */
	public int distinguishNowNetWork()
	{
		ConnectivityManager cManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
	    @SuppressWarnings("deprecation")
		NetworkInfo networkInfo = cManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	    
	    return networkInfo.getSubtype();       //返回当前网络的类型所代表的整数!!!

	}
	
	
	/**
	 * 调用第三方API函数，从网络上获取音乐排行榜数据，以及获取音乐歌词!!!
	 */
	public void requestDataFromInternet()
	{
		/**
		 * 开启子线程 , 调用第三方API函数，获取音乐排行榜
		 */
		new com.example.easymusicplayer1.showapi.ShowApiRequest("http://route.showapi.com/213-4", "11961",
				"1a1ee362464b4cd6beb9f69c43787f86").setResponseHandler(resHandler)
						// .addTextPara("keyword", "昨夜小楼又东风")
						.addTextPara("topid", "5").post();

		
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
