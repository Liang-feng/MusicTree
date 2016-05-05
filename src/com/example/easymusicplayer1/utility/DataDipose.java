package com.example.easymusicplayer1.utility;

import java.util.ArrayList;

import com.example.easymusicplayer1.activity.PlayMusicActivity;
import com.example.easymusicplayer1.db.SaveMusicInfo;
import com.example.easymusicplayer1.model.MusicTop;
import android.annotation.SuppressLint;
import android.util.Log;
import android.widget.Toast;

/**
 * 解析从网络返回的数据，并且把数据存储到数据库中!!!
 * 
 * @author feng
 *
 */
public class DataDipose {

	private static MusicTop music;

	ArrayList<String> musicLyricTime;

	ArrayList<String> musicLyric;

	/**
	 * 如下解析过程，看总结文档!!!! 解析出来得数据放在了music对象中!!!从网上获取音乐排行榜数据的信息，并保存在数据库里面！
	 * 
	 * @param response
	 */
	@SuppressLint("UseValueOf")
	public static void dealString(String response) {

		try
		{
			// 先后按"[" , "]"分割字符串 ,所得字符串不包括"[" , "]"
			String[] allMusic = response.split("\\[");
			// Toast.makeText(MyApplication.getContext() ,
			// String.valueOf(allMusic.length) , Toast.LENGTH_LONG).show();
			String[] allMusic1 = allMusic[1].split("\\]"); // allMusic1[0]是所需

			String[] allMusic2 = allMusic1[0].split("\\{");

			int musicArrayLength = allMusic2.length - 1; // 音乐数组的长度
			for (int i = 1; i < musicArrayLength; i++) // i从1开始，因为allMusic2数组第一个元素是""
			{
				allMusic2[i] = allMusic2[i].substring(0, allMusic2[i].length() - 2); // 删除掉}
				// ,
			}
			// 最后一个元素有所不同，没有","了
			allMusic2[musicArrayLength] = allMusic2[musicArrayLength].substring(0,
					allMusic2[musicArrayLength].length() - 1);

			int i = 0;
			for (String musicInfo : allMusic2) {
				if (i == 0) {
				} // 因为第一个元素为""所以不做字符串解析
				else {
					// 把数据进行最后解析，以及存储进数据库里面!!!
					String[] musicMsg = musicInfo.split(","); // 对字符串一","解析 ,
					// musicMsg字符串的形式是"albumid":
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
							music.setSeconds(new Integer(array[1])); // 调试的时候发现这里错了!!!
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

					SaveMusicInfo.saveMusicInfo(music); // 把数据存储到数据库中
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
		}catch(ArrayIndexOutOfBoundsException e)            //防止数据获取失败(数组会越界)时，app停止运行
		{
			Toast.makeText(MyApplication.getContext(), "从官方获取数据失败，网络繁忙，请稍后再试", Toast.LENGTH_SHORT).show();
			Log.e("DataDipose", e.toString());
			e.printStackTrace();
		}
		

	}

	/**
	 * 如上一个函数差不多！原理是一样的，利用字符串解析，解析根据歌曲id从网络上获取的歌词！！
	 * 
	 * @param response
	 */
	@SuppressLint({ "UseValueOf", "NewApi" })
	public void dealMusicLyric(String response) {

		musicLyricTime = new ArrayList<String>();
		musicLyric = new ArrayList<String>();
		
		try{
		// 下面3句可以，把无关信息消除掉，只剩下带有时间轴的歌词
		String[] allMusic = response.split("\\{");
		String[] allMusic1 = allMusic[2].split("\\}");
		Log.e("DataDipose", "sllMusic1 = " + allMusic1[0]);
		//由于当我们-when这首歌的  " 王铮亮,潘辰"歌手处有"," ，而我是以","进行解析的所以导致这首歌的allMusic2[1]才是我所需要的
		//比正常的歌曲后退了一位!!!正常歌曲解析得到的数组长度只有3，而此歌曲有5，所以做一下改变
		String[] allMusic2 = allMusic1[0].split(",");
		for(int index=1; index<allMusic2.length-2; index++)           //为了适应有些不正常的歌曲
		{
			allMusic2[0] += allMusic2[index];
		}
		


		// 由于从网络上获取的数据有乱码（html），所以做出如下替换
		allMusic2[0] = allMusic2[0].replaceAll("&#58;", ":"); // 把乱码&#58; 替换为:
		allMusic2[0] = allMusic2[0].replaceAll("&#32;", " "); // 把乱码&#32; 替换为空格
		allMusic2[0] = allMusic2[0].replaceAll("&#40;", "("); // 把乱码&#40; 替换为(
		allMusic2[0] = allMusic2[0].replaceAll("&#41;", ")"); // 把乱码&#41; 替换为)
		allMusic2[0] = allMusic2[0].replaceAll("&#46;", "."); // 把乱码&#46; 替换为.
		allMusic2[0] = allMusic2[0].replaceAll("&#10;", "\n"); // 把乱码&#10; 替换为换行
		allMusic2[0] = allMusic2[0].replaceAll("&#13;", "\n"); // 把乱码&#13; 替换为换行
		allMusic2[0] = allMusic2[0].replaceAll("&#45;", "-"); // 把乱码&#45; 替换为-
		allMusic2[0] = allMusic2[0].replaceAll("&#39;", "'"); // 把乱码&#39; 替换为'
		// 如下再做些便于用户体验的字符串替换,以及把歌词中的无用字符串替换，不单单替换ar的原因是：防止歌词也被替换
		allMusic2[0] = allMusic2[0].replaceAll("\"lyric\":", "");    // 把"lyric"
																     // 替换为歌词
		allMusic2[0] = allMusic2[0].replaceAll("\"", "");            // 把" 替换为空
		allMusic2[0] = allMusic2[0].replaceAll("ti:", "主题:");      // 把ti: 替换为主题
		allMusic2[0] = allMusic2[0].replaceAll("ar:", "歌手:");      // 把乱码ar: 替换为歌手
		allMusic2[0] = allMusic2[0].replaceAll("al:", "歌名:");      // 把乱码ar: 替换为歌名

		Log.e("DataDipose", "allMusic2 = " + allMusic2[1]);

		// allMusic3得到每行歌词的信息                          
		String[] allMusic3 = allMusic2[0].split("\\\n"); // 换行比较特殊，所以用\\使换行能准确解析出来!!根据API网页返回的结果来做此解析的!!
		for (int i = 0; i < allMusic3.length; i++) {
			Log.e("DataDipose", "allMusic3 = " + allMusic3[i]);
		}

	
		int i = 0;

		for (String s : allMusic3) {
			Log.e("DataDipose", "s   = " + s);
			String[] strArray = s.split("\\[");
			 Log.e("DataDipose", "strArray[0] = " + strArray[1]);

			String[] strArray1 = null;
			// 为了防止有些歌曲间隔一行就为无文本的，strArray[1]已经越界了!
			if (strArray.length != 1) {
				strArray1 = strArray[1].split("\\]");
				Log.e("DataDipose", strArray1[0]);

				if (i >= 5) // 前5行都是
				{
					if (strArray1 != null && strArray1.length == 2) // 因为有些是空的，并没有歌词，所以做此判断
					{
						musicLyric.add(strArray1[1]);
						Log.e("DataDipose", strArray1[1]);
					} else {
						musicLyric.add("\n"); // 有时间轴，但歌词为空，所以做此添加
						Log.e("DataDipose", "  ");
					}
					musicLyricTime.add(strArray1[0]); // 前5个元素不是时间，所以在这里才add元素进去
				}
			}
			i++;
		}
		
		
		}catch(ArrayIndexOutOfBoundsException e)            //防止数据获取失败(数组会越界)时，app停止运行
		{
			musicLyric.add("暂无歌词");
			Toast.makeText(MyApplication.getContext(), "从官方获取数据失败，网络繁忙，请稍后再试", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

		PlayMusicActivity.setMusicLric(musicLyric, musicLyricTime);
		PlayMusicActivity.cancelProgressDialog();                         // 取消progressDialog，如果取得歌词
		PlayMusicActivity.startMusic();                                   // 获取歌词失败也能继续播放
		Log.e("DataDipose", musicLyric.toString());
		Log.e("DataDipose", musicLyricTime.toString()); // 前5个元素不是时间，
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
	 * 解析从网络上搜索出来的歌曲的id，实际上就是获取歌曲的id！！！ 返回搜索到的歌曲id，没有则返回null
	 */
	public int diposeMusicId(String response) {

		try
		{
			Log.e("DataDipose", response);
			// 先后按"[" , "]"分割字符串 ,所得字符串不包括"[" , "]"
			String[] allMusic = response.split("\\[");
			String[] allMusic1 = allMusic[1].split("\\]"); // allMusic1[0]是所需

			String[] allMusic2 = allMusic1[0].split("\\{");

			Log.e("DataDipose", allMusic2[1]);

			// 下面的解析中，由于只需要第一个搜索到的结果，所以其它的不管，下面的解析中allMusic2[2]有问题了！
			int musicArrayLength = allMusic2.length - 1; // 音乐数组的长度
			for (int i = 1; i <= musicArrayLength; i++) // i从1开始，因为allMusic2数组第一个元素是""
			{
				allMusic2[i] = allMusic2[i].substring(0, allMusic2[i].length() - 2); // 删除掉}
				// ,
			}

		//	Log.e("DataDipose", allMusic2[1]);
			// Log.e("DataDipose", allMusic2[1]);
			// Log.e("DataDipose", allMusic2[2]);
			// 最后一个元素有所不同，没有","了
			allMusic2[musicArrayLength] = allMusic2[musicArrayLength].substring(0,
					allMusic2[musicArrayLength].length() - 1);

			// 把数据进行最后解析，以及存储进数据库里面!!!

			String[] musicMsg = allMusic2[1].split(","); // 对字符串一","解析 ,
			// musicMsg字符串的形式是"albumid":
			// 1182135
			// "seconds": 214

			for (String music1 : musicMsg) {

				String[] array = music1.split(":");

				if (array[0].equals("\"songid\"")) {

					//Toast.makeText(MyApplication.getContext(), array[0], Toast.LENGTH_SHORT).show();          //显示songid这个字符串

					return new Integer(array[1]).intValue(); // 返回查询到的歌曲id，用来查询歌曲

				}

			}

		}         //异常执行完还会继续执行下面的程序
		catch(ArrayIndexOutOfBoundsException e)            //防止数据获取失败(数组会越界)时，app停止运行
		{
			Toast.makeText(MyApplication.getContext(), "从官方获取数据失败，网络繁忙，请稍后再试", Toast.LENGTH_SHORT).show();
			Log.e("DataDipose", e.toString());
			e.printStackTrace();
		}
		// SaveMusicInfo.saveMusicInfo(music); //把数据存储到数据库中
		return 0;

	}

	/**
	 * 解析从网络上搜索出来的歌曲的id，实际上就是获取歌曲的id！！！ 返回搜索到的歌曲id，没有则返回null
	 */
	public String diposeMusicUrl(String response) {

		try
		{
			Log.e("DataDipose", response);
			// 先后按"[" , "]"分割字符串 ,所得字符串不包括"[" , "]"
			String[] allMusic = response.split("\\[");
			String[] allMusic1 = allMusic[1].split("\\]"); // allMusic1[0]是所需

			String[] allMusic2 = allMusic1[0].split("\\{");

		//	Log.e("DataDipose", allMusic2[1]);

			// 下面的解析中，由于只需要第一个搜索到的结果，所以其它的不管，下面的解析中allMusic2[2]有问题了！
			int musicArrayLength = allMusic2.length - 1; // 音乐数组的长度
			for (int i = 1; i <= musicArrayLength; i++) // i从1开始，因为allMusic2数组第一个元素是""
			{
				allMusic2[i] = allMusic2[i].substring(0, allMusic2[i].length() - 2); // 删除掉}
			    // ,
			}

	//		Log.e("DataDipose", allMusic2[1]);
			// Log.e("DataDipose", allMusic2[1]);
			// Log.e("DataDipose", allMusic2[2]);
			// 最后一个元素有所不同，没有","了
			allMusic2[musicArrayLength] = allMusic2[musicArrayLength].substring(0,
					allMusic2[musicArrayLength].length() - 1);

			// 把数据进行最后解析，以及存储进数据库里面!!!

			String[] musicMsg = allMusic2[1].split(","); // 对字符串一","解析 ,
			// musicMsg字符串的形式是"albumid":
			// 1182135
			// "seconds": 214

			for (String music1 : musicMsg) {

				String[] array = music1.split(":");

				if (array[0].equals("\"downUrl\"")) {

					// 把下载地址的双引号去掉
					array[1] = array[1].replaceAll("\"", "");
					array[2] = array[2].replaceAll("\"", "");
					return array[1] + ":" + array[2]; // 返回查询到的歌曲id，用来查询歌曲

				}

			}
		}catch(ArrayIndexOutOfBoundsException e)
		{		
			Toast.makeText(MyApplication.getContext(), "从官方获取数据失败，网络繁忙，请稍后再试", Toast.LENGTH_SHORT).show();
			Log.e("DataDipose", e.toString());
			e.printStackTrace();
		}

		// SaveMusicInfo.saveMusicInfo(music); //把数据存储到数据库中
		return null;

	}

}
