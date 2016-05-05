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
 * ���ݸ���id�����Ҹ�ʡ�
 * @author feng
 *
 */
public class RequestMusicLyric {
	
	DataDipose dateDipose;

	String musicId;

	
	public RequestMusicLyric(String musicId) {     //�ӵ��÷���ȡ����id����ѯ���!!!
          this.musicId = musicId;
	}
	
	/*
	 * �ɹ���ȡ��ʺ󣬽��н���������������
	 */
    AsyncHttpResponseHandler handler1 = new AsyncHttpResponseHandler() {
		
		@Override
		public void onSuccess(int startCode , Header[] headers , byte[] responseBody) {
			try {
				Log.e("ReqeustMusicLric", "�ɹ���ȡ���");
				
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
			Toast.makeText(MyApplication.getContext() , "�������ϻ�ȡ���ʧ��", Toast.LENGTH_SHORT).show();
			PlayMusicActivity.cancelProgressDialog();  //ʹprogressD����
			PlayMusicActivity.setSingleLyricText();    //��ʾ��ȡ���ʧ�ܵ���Ϣ
			PlayMusicActivity.startMusic();            //��ȡ���ʧ��Ҳ�ܼ�������
		}
	};
	
	
	/**
	 * ���õ�����API��������ȡ���ָ�ʣ�����ʱ�����Լ�������ʱ�������ͨ�ı���
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