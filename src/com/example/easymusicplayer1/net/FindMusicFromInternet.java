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
 * ���ڱ��ظ��id�β�룬�������õ�����api�����ݸ������ƻ��߸���������������������Ȼ��ѵ�һ�׸�����singerId�洢����������
 * @author feng
 *
 */
public class FindMusicFromInternet {
	
	String musicNameOrSingerName;
	
	DataDipose musicDataDipose;
	
	static ShareActionProvider shareActionProvider;
	
	/**
	 * ��ͨ�������ɹ���ȡ��������Ϣ����ȡ������id��������ȡ��� , ��onSucces�����һ��Thread.sleep(50000)��������Ϳ���֪���Ƿ�Ϊ���߳���!
	 */
	AsyncHttpResponseHandler handler = new AsyncHttpResponseHandler() {
		
		@Override
		public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {

			//��û����ʱ�������PlayMusicActivity��onCreateִ�еĻ���
			Toast.makeText(MyApplication.getContext() , "�������ϻ�ȡ���idʧ�ܣ������»�ȡ", Toast.LENGTH_LONG).show();
			//PlayMusicActivity.setGetLyricResult();        //��PlayMusicActivity����ʾ��ȡ���ʧ��!
			PlayMusicActivity.cancelProgressDialog();  //ʹprogressD���� , 
			PlayMusicActivity.setSingleLyricText();    //��ʾ��ȡ���ʧ�ܵ���Ϣ
			PlayMusicActivity.startMusic();            //��ȡ���ʧ��Ҳ�ܼ�������
		}

		@Override
		public void onSuccess(int arg0, @SuppressWarnings("deprecation") Header[] arg1, byte[] arg2) {
			
			try {
				String response = new String(arg2 , "utf-8");
				
				musicDataDipose = new DataDipose();
				int id = musicDataDipose.diposeMusicId(response);                      //�������ݡ���ȡ�������ϻ�ȡ�ĸ�����id
				
				String url = musicDataDipose.diposeMusicUrl(response);                 //�������ݣ���ȡ�������ص�ַ 
				if(url != null)               //�����ȡ����������Ϊ�գ������ť������
				{
					if(shareActionProvider != null)   //��ֹ����4G������죬shareActionProvider��û��ֵ����ִ��������,��ҪҲ�а�
					{
				    	shareActionProvider.setShareIntent(PlayMusicActivity.getDefaultIntent(url));        //����shareIntent�����������ťʱ��ʱ��
					}
				}
				else
				{
					Toast.makeText(MyApplication.getContext() , "��ȡ����������ʧ��", Toast.LENGTH_SHORT).show();
				}
				
				
				if(id != 0)
				{
					new RequestMusicLyric(String.valueOf(id)).getMusicLyricFromInternet();   //�������ϸ��ݸ��id��ȡ���ָ��
				}
				else 
				{
					Toast.makeText(MyApplication.getContext() , "��ȡ���ʧ��", Toast.LENGTH_SHORT).show();
					PlayMusicActivity.cancelProgressDialog();  //ʹprogressD����
					PlayMusicActivity.setSingleLyricText();    //��ʾ��ȡ���ʧ�ܵ���Ϣ
					PlayMusicActivity.startMusic();            //��ȡ���ʧ��Ҳ�ܼ�������
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
	 * ���ݸ������ƻ��߸������֣�����һ��Ҳ�У�����ȡ��������Ϣ,����������ȡ����id�����������ٴ���������ȡ���
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
