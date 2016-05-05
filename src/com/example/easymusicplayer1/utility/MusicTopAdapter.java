package com.example.easymusicplayer1.utility;

import java.util.List;

import com.example.easymusicplayer1.R;
import com.example.easymusicplayer1.model.MusicTop;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MusicTopAdapter extends ArrayAdapter<MusicTop> {
	
	int resourceId;
	
	View view;
	
	TextView musicName;
	
	TextView musicSinger;
	
	ViewHolder viewHolder;             //һ���ڲ��࣬�������ListView�Ļ���Ч��

	public MusicTopAdapter(Context context, int resource, List<MusicTop> objects) {
		super(context, resource, objects);
		resourceId = resource;
	}
	
	class ViewHolder
	{
		TextView musicName;
		TextView musicSinger;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		MusicTop music = this.getItem(position);
		
		if(convertView == null)
		{
    		view = LayoutInflater.from(getContext()).inflate(R.layout.music_top_activity , null);
    	    viewHolder = new ViewHolder();
    	    viewHolder.musicName = (TextView) view.findViewById(R.id.music_name);
    	    viewHolder.musicSinger = (TextView) view.findViewById(R.id.music_singer);
    	    view.setTag(viewHolder);
		}
		else
		{
			view = convertView;
			viewHolder = (ViewHolder) view.getTag();
		}

		musicName = viewHolder.musicName;
		musicSinger = viewHolder.musicSinger;
	    
	    musicName.setText(music.getSongName());                       //���ø�����
	    musicSinger.setText(music.getSingerName());                   //���ø�������
		
		return view;
	}

}
