package com.example.easymusicplayer1.net;

import java.io.UnsupportedEncodingException;

import org.apache.http.Header;

import com.example.easymusicplayer1.activity.PlayMusicActivity;
import com.example.easymusicplayer1.utility.DataDipose;
import com.example.easymusicplayer1.utility.MyApplication;
import com.example.easymusicplayer1.utility.ParseDataWithJSONObject;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.util.Log;
import android.widget.Toast;

/**
 * 根据歌曲id，查找歌词。
 * @author feng
 *
 */
public class RequestMusicLyric {
	
	DataDipose dateDipose;

	String musicId;

	
	public RequestMusicLyric(String musicId) {     //从调用方获取歌曲id来查询歌词!!!
          this.musicId = musicId;
	}
	
	/*
	 * 成功获取歌词后，进行解析返回来的数据
	 */
    AsyncHttpResponseHandler handler1 = new AsyncHttpResponseHandler() {
		
		@Override
		public void onSuccess(int startCode , Header[] headers , byte[] responseBody) {
			try {
				Log.e("ReqeustMusicLric", "成功获取歌词");
				
				String response = new String(responseBody , "utf-8");
				
				//Log.e("RequestMusicLyric", response);
				System.out.println(response);
				dateDipose = new DataDipose();
				dateDipose.dealMusicLyric(response);
				
				//ParseDataWithJSONObject.parseData(response);
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		@Override
		public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
			Toast.makeText(MyApplication.getContext() , "从网络上获取歌词失败", Toast.LENGTH_SHORT).show();
			PlayMusicActivity.cancelProgressDialog();  //使progressD不见
			PlayMusicActivity.setSingleLyricText();    //显示获取歌词失败的信息
			PlayMusicActivity.startMusic();            //获取歌词失败也能继续播放
		}
	};
	
	
	/**
	 * 调用第三方API函数，获取音乐歌词（带有时间轴以及不带有时间轴的普通文本）
	 */
	public void getMusicLyricFromInternet()
	{
	
		new com.example.easymusicplayer1.showapi.ShowApiRequest("http://route.showapi.com/213-2" , "11961" , 
				"1a1ee362464b4cd6beb9f69c43787f86")
		.setResponseHandler(handler1)
		.addTextPara("musicid" , musicId)
		.post();
	}

}