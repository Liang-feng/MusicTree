package com.example.easymusicplayer1.utility;

import java.text.SimpleDateFormat;
import java.util.List;

import com.example.easymusicplayer1.R;
import com.example.easymusicplayer1.model.Music;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MusicAdapter extends ArrayAdapter<Music> {
	
	int resourceId;
	
	View view;
	
	TextView musicName;
	
	TextView musicSinger;
	
	TextView musicDuration;
	
	ViewHolder viewHolder;             //一个内部类，用来提高ListView的滑动效率
	
	SimpleDateFormat simgpleDateFormat;

	public MusicAdapter(Context context, int resource, List<Music> objects) {
		super(context, resource, objects);
		resourceId = resource;
	}
	
	class ViewHolder
	{
		TextView musicName;
		TextView musicSinger;
		TextView musicDuration;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Music music = this.getItem(position);
		
		if(convertView == null)
		{
			simgpleDateFormat = new SimpleDateFormat("mm:ss");
    		view = LayoutInflater.from(getContext()).inflate(R.layout.my_music_activity , null);
    	    viewHolder = new ViewHolder();
    	    viewHolder.musicName = (TextView) view.findViewById(R.id.music_name);
    	    viewHolder.musicSinger = (TextView) view.findViewById(R.id.music_singer);
    	    viewHolder.musicDuration = (TextView)view.findViewById(R.id.music_duration);
    	    view.setTag(viewHolder);
		}
		else
		{
			view = convertView;
			viewHolder = (ViewHolder) view.getTag();
		}

		musicName = viewHolder.musicName;
		musicSinger = viewHolder.musicSinger;
		musicDuration = viewHolder.musicDuration;
	    
	    musicName.setText(music.getTitle());                       //显示歌曲名
	    musicSinger.setText(music.getSingerName());                   //显示歌曲作者
	    musicDuration.setText(simgpleDateFormat.format(music.getDuration()));                                                 //显示时长
	    return view;
	}

}
