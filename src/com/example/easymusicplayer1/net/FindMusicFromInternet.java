package com.example.easymusicplayer1.net;

import java.io.UnsupportedEncodingException;

import org.apache.http.Header;

import com.example.easymusicplayer1.activity.PlayMusicActivity;
import com.example.easymusicplayer1.utility.DataDipose;
import com.example.easymusicplayer1.utility.MyApplication;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.support.v4.view.ActionProvider;
import android.util.Log;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 由于本地歌词id参差不齐，所以利用第三方api，根据歌曲名称或者歌曲作者名来搜索歌曲，然后把第一首歌曲的singerId存储起来！！！
 * @author feng
 *
 */
public class FindMusicFromInternet {
	
	String musicNameOrSingerName;
	
	DataDipose musicDataDipose;
	
	static ShareActionProvider shareActionProvider;
	
	/**
	 * 当通过搜索成功获取歌曲的信息后，提取出歌曲id，进而获取歌词 , 在onSucces里面放一个Thread.sleep(50000)看阻塞否就可以知道是否为主线程了!
	 */
	AsyncHttpResponseHandler handler = new AsyncHttpResponseHandler() {
		
		@Override
		public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {

			//当没联网时，这里比PlayMusicActivity的onCreate执行的还快
			Toast.makeText(MyApplication.getContext() , "从网络上获取歌词id失败，请重新获取", Toast.LENGTH_LONG).show();
			//PlayMusicActivity.setGetLyricResult();        //在PlayMusicActivity中显示获取歌词失败!
			PlayMusicActivity.cancelProgressDialog();  //使progressD不见 , 
			PlayMusicActivity.setSingleLyricText();    //显示获取歌词失败的信息
			PlayMusicActivity.startMusic();            //获取歌词失败也能继续播放
		}

		@Override
		public void onSuccess(int arg0, @SuppressWarnings("deprecation") Header[] arg1, byte[] arg2) {
			
			try {
				String response = new String(arg2 , "utf-8");
				
				musicDataDipose = new DataDipose();
				int id = musicDataDipose.diposeMusicId(response);                      //解析数据。获取从网络上获取的歌曲的id
				
				String url = musicDataDipose.diposeMusicUrl(response);                 //解析数据，获取歌曲下载地址 
				if(url != null)               //如果获取被分享数据为空，则分享按钮不可用
				{
					if(shareActionProvider != null)   //防止由于4G网络过快，shareActionProvider还没赋值，就执行了这里,不要也行吧
					{
				    	shareActionProvider.setShareIntent(PlayMusicActivity.getDefaultIntent(url));        //设置shareIntent，即点击分享按钮时的时间
					}
				}
				else
				{
					Toast.makeText(MyApplication.getContext() , "获取被分享数据失败", Toast.LENGTH_SHORT).show();
				}
				
				
				if(id != 0)
				{
					new RequestMusicLyric(String.valueOf(id)).getMusicLyricFromInternet();   //从网络上根据歌词id获取音乐歌词
				}
				else 
				{
					Toast.makeText(MyApplication.getContext() , "获取歌词失败", Toast.LENGTH_SHORT).show();
					PlayMusicActivity.cancelProgressDialog();  //使progressD不见
					PlayMusicActivity.setSingleLyricText();    //显示获取歌词失败的信息
					PlayMusicActivity.startMusic();            //获取歌词失败也能继续播放
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	};
	

	
	
	public FindMusicFromInternet(String str) {

		musicNameOrSingerName = str;
	}
	
	public FindMusicFromInternet()
	{}
	
	/**
	 * 根据歌曲名称或者歌曲名字，或者一起也行，来获取歌曲的信息,进而用来获取歌曲id，进而用来再次搜索来获取歌词
	 */
	public void getMusicIdFromInternet()
	{
		new com.example.easymusicplayer1.showapi.ShowApiRequest("http://route.showapi.com/213-1", 
				"11961" , "1a1ee362464b4cd6beb9f69c43787f86")
		.setResponseHandler(handler)
		.addTextPara("keyword", musicNameOrSingerName)
		.post();
		
	}
	
	
	public static void setShareActionProvider(ShareActionProvider shareActionProvider1)
	{
		shareActionProvider = shareActionProvider1;
	}

}
