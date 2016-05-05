package com.example.easymusicplayer1.utility;

import java.util.ArrayList;

import com.example.easymusicplayer1.activity.PlayMusicActivity;
import com.example.easymusicplayer1.db.SaveMusicInfo;
import com.example.easymusicplayer1.model.MusicTop;
import android.annotation.SuppressLint;
import android.util.Log;
import android.widget.Toast;

/**
 * ���������緵�ص����ݣ����Ұ����ݴ洢�����ݿ���!!!
 * 
 * @author feng
 *
 */
public class DataDipose {

	private static MusicTop music;

	ArrayList<String> musicLyricTime;

	ArrayList<String> musicLyric;

	/**
	 * ���½������̣����ܽ��ĵ�!!!! �������������ݷ�����music������!!!�����ϻ�ȡ�������а����ݵ���Ϣ�������������ݿ����棡
	 * 
	 * @param response
	 */
	@SuppressLint("UseValueOf")
	public static void dealString(String response) {

		try
		{
			// �Ⱥ�"[" , "]"�ָ��ַ��� ,�����ַ���������"[" , "]"
			String[] allMusic = response.split("\\[");
			// Toast.makeText(MyApplication.getContext() ,
			// String.valueOf(allMusic.length) , Toast.LENGTH_LONG).show();
			String[] allMusic1 = allMusic[1].split("\\]"); // allMusic1[0]������

			String[] allMusic2 = allMusic1[0].split("\\{");

			int musicArrayLength = allMusic2.length - 1; // ��������ĳ���
			for (int i = 1; i < musicArrayLength; i++) // i��1��ʼ����ΪallMusic2�����һ��Ԫ����""
			{
				allMusic2[i] = allMusic2[i].substring(0, allMusic2[i].length() - 2); // ɾ����}
				// ,
			}
			// ���һ��Ԫ��������ͬ��û��","��
			allMusic2[musicArrayLength] = allMusic2[musicArrayLength].substring(0,
					allMusic2[musicArrayLength].length() - 1);

			int i = 0;
			for (String musicInfo : allMusic2) {
				if (i == 0) {
				} // ��Ϊ��һ��Ԫ��Ϊ""���Բ����ַ�������
				else {
					// �����ݽ������������Լ��洢�����ݿ�����!!!
					String[] musicMsg = musicInfo.split(","); // ���ַ���һ","���� ,
					// musicMsg�ַ�������ʽ��"albumid":
					// 1182135
					// "seconds": 214
					music = new MusicTop();

					for (String music1 : musicMsg) {

						String[] array = music1.split(":");

						if (array[0].equals("\"albumid\"")) {
							music.setAlbumId(Integer.valueOf(array[1]));
						} else if (array[0].equals("\"downUrl\"")) {
							array[1] = array[1].substring(1, array[1].length());
							array[2] = array[2].substring(0, array[2].length() - 1);
							music.setDownUrl(array[1].concat(":" + array[2]));
						} else if (array[0].equals("\"seconds\"")) {
							music.setSeconds(new Integer(array[1])); // ���Ե�ʱ�����������!!!
						} else if (array[0].equals("\"singerid\"")) {
							music.setSingerId(Integer.valueOf(array[1]));
						} else if (array[0].equals("\"singername\"")) {
							music.setSingerName(array[1] = array[1].substring(1, array[1].length() - 1));
						} else if (array[0].equals("\"songid\"")) {
							music.setSongId(Integer.valueOf(array[1]));
						} else if (array[0].equals("\"songname\"")) {
							music.setSongName(array[1] = array[1].substring(1, array[1].length() - 1));
						} else if (array[0].equals("\"url\"")) {
							array[1] = array[1].substring(1, array[1].length());
							array[2] = array[2].substring(0, array[2].length() - 1);
							music.setUrl(array[1].concat(":" + array[2]));
						}

					}

					SaveMusicInfo.saveMusicInfo(music); // �����ݴ洢�����ݿ���
					/*
					 * txt.setText(txt.getText() +
					 * String.valueOf(music.getAlbumId()) + "  " +
					 * music.getDownUrl() + "  " +music.getSeconds() + "   " +
					 * String.valueOf(music.getSingerId()) + "  " +
					 * music.getSingerName() + "    "
					 * +String.valueOf(music.getSongId()) + "  " +
					 * music.getSongName() + "   " + music.getUrl() + "   " +
					 * "\n\n");
					 */

				}
				i++;
			}
		}catch(ArrayIndexOutOfBoundsException e)            //��ֹ���ݻ�ȡʧ��(�����Խ��)ʱ��appֹͣ����
		{
			Toast.makeText(MyApplication.getContext(), "�ӹٷ���ȡ����ʧ�ܣ����緱æ�����Ժ�����", Toast.LENGTH_SHORT).show();
			Log.e("DataDipose", e.toString());
			e.printStackTrace();
		}
		

	}

	/**
	 * ����һ��������࣡ԭ����һ���ģ������ַ����������������ݸ���id�������ϻ�ȡ�ĸ�ʣ���
	 * 
	 * @param response
	 */
	@SuppressLint({ "UseValueOf", "NewApi" })
	public void dealMusicLyric(String response) {

		musicLyricTime = new ArrayList<String>();
		musicLyric = new ArrayList<String>();
		
		try{
		// ����3����ԣ����޹���Ϣ��������ֻʣ�´���ʱ����ĸ��
		String[] allMusic = response.split("\\{");
		String[] allMusic1 = allMusic[2].split("\\}");
		Log.e("DataDipose", "sllMusic1 = " + allMusic1[0]);
		//���ڵ�����-when���׸��  " �����,�˳�"���ִ���"," ����������","���н��������Ե������׸��allMusic2[1]����������Ҫ��
		//�������ĸ���������һλ!!!�������������õ������鳤��ֻ��3�����˸�����5��������һ�¸ı�
		String[] allMusic2 = allMusic1[0].split(",");
		for(int index=1; index<allMusic2.length-2; index++)           //Ϊ����Ӧ��Щ�������ĸ���
		{
			allMusic2[0] += allMusic2[index];
		}
		


		// ���ڴ������ϻ�ȡ�����������루html�����������������滻
		allMusic2[0] = allMusic2[0].replaceAll("&#58;", ":"); // ������&#58; �滻Ϊ:
		allMusic2[0] = allMusic2[0].replaceAll("&#32;", " "); // ������&#32; �滻Ϊ�ո�
		allMusic2[0] = allMusic2[0].replaceAll("&#40;", "("); // ������&#40; �滻Ϊ(
		allMusic2[0] = allMusic2[0].replaceAll("&#41;", ")"); // ������&#41; �滻Ϊ)
		allMusic2[0] = allMusic2[0].replaceAll("&#46;", "."); // ������&#46; �滻Ϊ.
		allMusic2[0] = allMusic2[0].replaceAll("&#10;", "\n"); // ������&#10; �滻Ϊ����
		allMusic2[0] = allMusic2[0].replaceAll("&#13;", "\n"); // ������&#13; �滻Ϊ����
		allMusic2[0] = allMusic2[0].replaceAll("&#45;", "-"); // ������&#45; �滻Ϊ-
		allMusic2[0] = allMusic2[0].replaceAll("&#39;", "'"); // ������&#39; �滻Ϊ'
		// ��������Щ�����û�������ַ����滻,�Լ��Ѹ���е������ַ����滻���������滻ar��ԭ���ǣ���ֹ���Ҳ���滻
		allMusic2[0] = allMusic2[0].replaceAll("\"lyric\":", "");    // ��"lyric"
																     // �滻Ϊ���
		allMusic2[0] = allMusic2[0].replaceAll("\"", "");            // ��" �滻Ϊ��
		allMusic2[0] = allMusic2[0].replaceAll("ti:", "����:");      // ��ti: �滻Ϊ����
		allMusic2[0] = allMusic2[0].replaceAll("ar:", "����:");      // ������ar: �滻Ϊ����
		allMusic2[0] = allMusic2[0].replaceAll("al:", "����:");      // ������ar: �滻Ϊ����

		Log.e("DataDipose", "allMusic2 = " + allMusic2[1]);

		// allMusic3�õ�ÿ�и�ʵ���Ϣ                          
		String[] allMusic3 = allMusic2[0].split("\\\n"); // ���бȽ����⣬������\\ʹ������׼ȷ��������!!����API��ҳ���صĽ�������˽�����!!
		for (int i = 0; i < allMusic3.length; i++) {
			Log.e("DataDipose", "allMusic3 = " + allMusic3[i]);
		}

	
		int i = 0;

		for (String s : allMusic3) {
			Log.e("DataDipose", "s   = " + s);
			String[] strArray = s.split("\\[");
			 Log.e("DataDipose", "strArray[0] = " + strArray[1]);

			String[] strArray1 = null;
			// Ϊ�˷�ֹ��Щ�������һ�о�Ϊ���ı��ģ�strArray[1]�Ѿ�Խ����!
			if (strArray.length != 1) {
				strArray1 = strArray[1].split("\\]");
				Log.e("DataDipose", strArray1[0]);

				if (i >= 5) // ǰ5�ж���
				{
					if (strArray1 != null && strArray1.length == 2) // ��Ϊ��Щ�ǿյģ���û�и�ʣ����������ж�
					{
						musicLyric.add(strArray1[1]);
						Log.e("DataDipose", strArray1[1]);
					} else {
						musicLyric.add("\n"); // ��ʱ���ᣬ�����Ϊ�գ������������
						Log.e("DataDipose", "  ");
					}
					musicLyricTime.add(strArray1[0]); // ǰ5��Ԫ�ز���ʱ�䣬�����������addԪ�ؽ�ȥ
				}
			}
			i++;
		}
		
		
		}catch(ArrayIndexOutOfBoundsException e)            //��ֹ���ݻ�ȡʧ��(�����Խ��)ʱ��appֹͣ����
		{
			musicLyric.add("���޸��");
			Toast.makeText(MyApplication.getContext(), "�ӹٷ���ȡ����ʧ�ܣ����緱æ�����Ժ�����", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

		PlayMusicActivity.setMusicLric(musicLyric, musicLyricTime);
		PlayMusicActivity.cancelProgressDialog();                         // ȡ��progressDialog�����ȡ�ø��
		PlayMusicActivity.startMusic();                                   // ��ȡ���ʧ��Ҳ�ܼ�������
		Log.e("DataDipose", musicLyric.toString());
		Log.e("DataDipose", musicLyricTime.toString()); // ǰ5��Ԫ�ز���ʱ�䣬
		Log.e("DataDipose", String.valueOf("musicLyric Length : " + musicLyric.size()) + "  " + "musicLyricTimeList :"
				+ String.valueOf(musicLyricTime.size()));

	}

	public ArrayList<String> getLyric() {
		return musicLyric;
	}

	public ArrayList<String> getLyricTime() {
		return musicLyricTime;
	}

	/**
	 * ���������������������ĸ�����id��ʵ���Ͼ��ǻ�ȡ������id������ �����������ĸ���id��û���򷵻�null
	 */
	public int diposeMusicId(String response) {

		try
		{
			Log.e("DataDipose", response);
			// �Ⱥ�"[" , "]"�ָ��ַ��� ,�����ַ���������"[" , "]"
			String[] allMusic = response.split("\\[");
			String[] allMusic1 = allMusic[1].split("\\]"); // allMusic1[0]������

			String[] allMusic2 = allMusic1[0].split("\\{");

			Log.e("DataDipose", allMusic2[1]);

			// ����Ľ����У�����ֻ��Ҫ��һ���������Ľ�������������Ĳ��ܣ�����Ľ�����allMusic2[2]�������ˣ�
			int musicArrayLength = allMusic2.length - 1; // ��������ĳ���
			for (int i = 1; i <= musicArrayLength; i++) // i��1��ʼ����ΪallMusic2�����һ��Ԫ����""
			{
				allMusic2[i] = allMusic2[i].substring(0, allMusic2[i].length() - 2); // ɾ����}
				// ,
			}

		//	Log.e("DataDipose", allMusic2[1]);
			// Log.e("DataDipose", allMusic2[1]);
			// Log.e("DataDipose", allMusic2[2]);
			// ���һ��Ԫ��������ͬ��û��","��
			allMusic2[musicArrayLength] = allMusic2[musicArrayLength].substring(0,
					allMusic2[musicArrayLength].length() - 1);

			// �����ݽ������������Լ��洢�����ݿ�����!!!

			String[] musicMsg = allMusic2[1].split(","); // ���ַ���һ","���� ,
			// musicMsg�ַ�������ʽ��"albumid":
			// 1182135
			// "seconds": 214

			for (String music1 : musicMsg) {

				String[] array = music1.split(":");

				if (array[0].equals("\"songid\"")) {

					//Toast.makeText(MyApplication.getContext(), array[0], Toast.LENGTH_SHORT).show();          //��ʾsongid����ַ���

					return new Integer(array[1]).intValue(); // ���ز�ѯ���ĸ���id��������ѯ����

				}

			}

		}         //�쳣ִ���껹�����ִ������ĳ���
		catch(ArrayIndexOutOfBoundsException e)            //��ֹ���ݻ�ȡʧ��(�����Խ��)ʱ��appֹͣ����
		{
			Toast.makeText(MyApplication.getContext(), "�ӹٷ���ȡ����ʧ�ܣ����緱æ�����Ժ�����", Toast.LENGTH_SHORT).show();
			Log.e("DataDipose", e.toString());
			e.printStackTrace();
		}
		// SaveMusicInfo.saveMusicInfo(music); //�����ݴ洢�����ݿ���
		return 0;

	}

	/**
	 * ���������������������ĸ�����id��ʵ���Ͼ��ǻ�ȡ������id������ �����������ĸ���id��û���򷵻�null
	 */
	public String diposeMusicUrl(String response) {

		try
		{
			Log.e("DataDipose", response);
			// �Ⱥ�"[" , "]"�ָ��ַ��� ,�����ַ���������"[" , "]"
			String[] allMusic = response.split("\\[");
			String[] allMusic1 = allMusic[1].split("\\]"); // allMusic1[0]������

			String[] allMusic2 = allMusic1[0].split("\\{");

		//	Log.e("DataDipose", allMusic2[1]);

			// ����Ľ����У�����ֻ��Ҫ��һ���������Ľ�������������Ĳ��ܣ�����Ľ�����allMusic2[2]�������ˣ�
			int musicArrayLength = allMusic2.length - 1; // ��������ĳ���
			for (int i = 1; i <= musicArrayLength; i++) // i��1��ʼ����ΪallMusic2�����һ��Ԫ����""
			{
				allMusic2[i] = allMusic2[i].substring(0, allMusic2[i].length() - 2); // ɾ����}
			    // ,
			}

	//		Log.e("DataDipose", allMusic2[1]);
			// Log.e("DataDipose", allMusic2[1]);
			// Log.e("DataDipose", allMusic2[2]);
			// ���һ��Ԫ��������ͬ��û��","��
			allMusic2[musicArrayLength] = allMusic2[musicArrayLength].substring(0,
					allMusic2[musicArrayLength].length() - 1);

			// �����ݽ������������Լ��洢�����ݿ�����!!!

			String[] musicMsg = allMusic2[1].split(","); // ���ַ���һ","���� ,
			// musicMsg�ַ�������ʽ��"albumid":
			// 1182135
			// "seconds": 214

			for (String music1 : musicMsg) {

				String[] array = music1.split(":");

				if (array[0].equals("\"downUrl\"")) {

					// �����ص�ַ��˫����ȥ��
					array[1] = array[1].replaceAll("\"", "");
					array[2] = array[2].replaceAll("\"", "");
					return array[1] + ":" + array[2]; // ���ز�ѯ���ĸ���id��������ѯ����

				}

			}
		}catch(ArrayIndexOutOfBoundsException e)
		{		
			Toast.makeText(MyApplication.getContext(), "�ӹٷ���ȡ����ʧ�ܣ����緱æ�����Ժ�����", Toast.LENGTH_SHORT).show();
			Log.e("DataDipose", e.toString());
			e.printStackTrace();
		}

		// SaveMusicInfo.saveMusicInfo(music); //�����ݴ洢�����ݿ���
		return null;

	}

}
