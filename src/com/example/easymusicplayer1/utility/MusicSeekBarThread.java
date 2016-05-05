package com.example.easymusicplayer1.utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

/**
 * ����ʹ��ʣ�seekBar���������ȣ�,��������   ����ͬ�� , ʱ�����֧��Сʱ�Ĺ���û����
 * @author feng
 *
 */
public class MusicSeekBarThread implements Runnable {

	Boolean isPlay;                           //��ǵ�ǰ�����Ƿ��ڲ���
	
	boolean networkIsAvaliable = true;               //��ǵ�ǰ�����Ƿ����
	
	MediaPlayer mediaPlayer; 
	
	SimpleDateFormat sDateFormat;             //���ڽ�������
	
	Handler handler;
	
	int time;                                 //���ڼ�¼���ֵĵ�ǰ����ʱ��
	
	int duration;                             //���ڴ洢����ʱ��Ĳ����ܳ���!!!
	
	public static final int MUSIC_PROGRESS = 1;         //����handler�ڲ�����sendMessage
	
	String cureentTime1 = "";                         //���������ֵ����ô������if�жϾ�����!!!�����Ը�ֵΪnull
	
	ArrayList<String> musicLyricTimeList;                  //���ڴ洢һ�׸���ÿ����ʱ����
	
	ArrayList<String> musicLyricList;                      //���ڴ洢һ�׸�ĸ��
	
	TextView musicLyric;                                   //������ʾȫ�����
	
	TextView musicSingleLyric;                             //������ʾ������
	 
	int index = 0;                                         //������� �� ���ʱ�����λ��
	 //��ʼ��Ϊnull������PlayMusicActivity�е�handler�ж�singleLyric�Ƿ�Ϊ��
	String singleLyric = null;                     //���ڴ洢�����ʣ����ݸ�PlayMusicActivity�е�handler
	
	Boolean bool = false;                       //���ڱ���Ƿ������synchronizationLyricAndMusic������if����������
	
	@SuppressLint("SimpleDateFormat")
	public MusicSeekBarThread(Boolean isPlay , MediaPlayer mediaPlayer , Handler handler , int time
			) {
		musicLyricTimeList = new ArrayList<String>();
		musicLyricList = new ArrayList<String>();
		this.isPlay = isPlay;
		this.mediaPlayer = mediaPlayer;
		this.handler = handler;
		this.time = time;
		sDateFormat = new SimpleDateFormat("mm:ss");
		duration = mediaPlayer.getDuration();
        
	}
	
	/**
	 * ����ǰͷ����synchronized��Ϊ�ˣ��ô˷�����ͬһʱ��ֻ�ܱ�һ���̵߳��ã�ֻ��ִ�������һ���̲߳���ִ�д˷���
	 * �൱��Ϊ�˺�������.
	 */
	
	
	@Override
	public synchronized void run() {
		
		while (isPlay) {
			

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
                Log.e("MusicSeekBarThread", e.toString());
                System.out.println("MusicSeekBarThread �쳣 : " + e.toString());
				e.printStackTrace();
			}
			

			/* �����ж����,Ϊ�˷�ֹ�û�һ��ʼ�Ͱ�seekBar�ϵ��ף����������time����ǰ����ʱ�䣩��duration������
			*  ���Լ�����ͣ���ź�seekBar��ߵ��������ڶ�,��������ͣ����������seekBar���ڽ����ƶ�!!!
			*  ����������ж���䣬��ô��ͣ��seekBar�����ƶ�һ�񣬼��������sendMessage()
			*  60���Լ�69��73�У�����������棬���ڲ�����һ�׸�����ʱ��seekBar�����ָ�����!!
			*  ����һ��bug���Ǿ����������ǰ̨��������һ�ף����ǻ���뵽���棬seekBar��������startTime�е㲻Э����
			*  ����Ժ���Ҫ�Ľ�����ô��ǰ̨������һ��boolean�������������������if�ж�����м�������boolean�жϼ���!!!
			*/

			/*���˳�PlayMusicActivity��ʱ������쳣java.lang.IllegalStateException�쳣��
			 *���쳣��ʾ����ǰ�Կͻ��˵���Ӧ�Ѿ���������������Ӧ�Ѿ���������˵������������ͻ��ˣ�ʵ�����ǻ�����������κ����ݡ�
			 * ������mediaPlayer.isPlaying()�������˳�PlayMusicActivityʱ��ִ����onDestroy()������
			 * ��ǰ�ͻ�����Ӧ�Ѿ����������Բ�����ʹ��mediaPlayer��ͻ��ˣ�ʵ�����ǻ�����������κ����ݣ���������
			 */
			if(isPlay && time != duration && mediaPlayer.isPlaying() )  
			{
				
				int currentPosition = mediaPlayer.getCurrentPosition();
				
				if(networkIsAvaliable)
				{
	    			synchronizationLyricAndMusic(currentPosition);           //ʹ���������ͬ��!!!
				}
	    		time += 1000;
	    		
				cureentTime1 = sDateFormat.format(new Date(time));

				//�����������̵߳�ui
				Message msg = new Message();
				msg.what = MUSIC_PROGRESS;
				msg.arg1 = currentPosition;
				if(bool && networkIsAvaliable)          //ʹ��bool��ֹÿ�ζ������ظ���ֵ��ȥ!!!�Լ�ʹ��networkIsAvaliable���жϵ�ǰ�������Ƿ���ڣ�û������ͬ����ʣ���ֹ����,��ȻҲ���Լ��쳣
				{
		    		Bundle bundle = new Bundle();
		    		bundle.putString("single_lyric", singleLyric);        
		    		msg.setData(bundle);           //���ڷ��ص�ǰ�ĵ�����
		    		bool = false;
				}
				msg.obj = cureentTime1;
				handler.sendMessage(msg);           //���ݵ�PlayMusicActivity��handler��
			}

	        //������ֲ��Ž���������ʱ�䵽�ˣ�������ѭ���� ����д�ĺô��ǣ���������ͣʱ�����̲߳������˳�ȥ�����ٴβ���ʱ���ֿ��Լ�����������
			if (cureentTime1.equals(sDateFormat.format(new Date(duration)))) {
				isPlay = false;
			}
		}
	}
	
	
	/**
	 * ʹ���������ͬ��!!!
	 */
	private void synchronizationLyricAndMusic(int currentPosition) {

		//�����������߳�  ��  ���̻߳�������֮ǰ�����������ж�!��û����ݣ���ômusicLyrictimeList��Ϊ�գ����������ж�!!
		if (musicLyricTimeList.size() != 0 && index < musicLyricTimeList.size())        
		{
			String LyricTime = musicLyricTimeList.get(index);
			long millisecond = changeTimeFormat(LyricTime);
			
			Log.e("MusicSeekBarThread", "currentPosition = " + String.valueOf(currentPosition) + 
					"     millisecond = " + String.valueOf(millisecond));

			if (currentPosition >= millisecond) // �����ǰ���ֲ��ŵĽ��ȣ����ڻ���ڸ��ʱ����Ľ��ȣ����ƶ����
			{
				//String lyric = (String) musicLyric.getText();
				singleLyric = musicLyricList.get(index);            //��ȡ��ǰʱ�̵ĵ�����
				Log.e("MusicSeekBarThread", singleLyric);
				index++;
				bool = true;
			}

		}
	}
	


	/**
	 * �ı�ʱ��ĸ�ʽ���ӷ�ת��Ϊ���룬������seekBar�Ľ������Ƚϣ�����ͬ�����
	 * @return       ����ת����ĺ�����
	 */
	@SuppressLint("UseValueOf")
	public long changeTimeFormat(String time)
	{
		long millisecond = 0;
		String[] time1 = time.split(":");                                       //����00:05.26��ʽ������   , �����01:00:05.26��
		//����try �� catch��ԭ���ǣ����ڡ��ž޻�-�ظ��������׸��musicLyricTime�ĵ�һ��Ԫ�ز���ʱ����ʽ������offset�����׳��쳣��
		//�����жϣ���ֹ�����жϣ��������������ס�쳣;
		//if(time1.length <= 2)          //���������1Сʱ����
		{
			try 
			{
				millisecond = new Integer(time1[0]).longValue() * 60 * 1000; // ��ת��Ϊ����

				String[] time2 = time1[1].split("\\.");
				millisecond += new Integer(time2[0]).longValue() * 1000; // ��ת��Ϊ����
				// �����
				millisecond += new Integer(time2[1]).longValue(); // �������
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		/*else if(time1.length == 3)         //��������һСʱ
		{
			try 
			{
				millisecond = new Integer(time1[0]).longValue() * 60 * 60 * 1000; // Сʱת��Ϊ����

				millisecond += new Integer(time1[1]).longValue() * 60 * 1000;     // ��ת��Ϊ����
				
				String[] time2 = time1[2].split("\\.");
				millisecond += new Integer(time2[0]).longValue() * 1000;          // ��ת��Ϊ����
				// �����
				millisecond += new Integer(time2[1]).longValue();                 // �������
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}*/
		return millisecond;
	}
	
	
	public void setTime(int time)
	{
		this.time = time;                      //����϶�seekBar����startTimeҪ�ı䣬������ı�
	}
	
	public void setIsPlay(Boolean bool)
	{
		isPlay = bool;
	}
	
	public void setMusicLyricTimeList(ArrayList<String> musicLyricTimeList)
	{
		this.musicLyricTimeList = musicLyricTimeList;
	}
	
	public void setMusicLyricList(ArrayList<String> musicLyricList)
	{
		this.musicLyricList = musicLyricList;
	}
	
	/**
	 * ���϶�seekBar��ʱ�����ø�ʵ�������ʹ�ø�ʿ���ͬ��!
	 */
	public void setLyricIndex(int index1)
	{
		this.index = index1;
	}
	
	public void setNetworkIsAvailable(Boolean bool)
	{
		this.networkIsAvaliable = bool;
	}

}
