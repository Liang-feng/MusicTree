package com.example.easymusicplayer1.utility;

import java.util.List;

import com.example.easymusicplayer1.R;
import com.example.easymusicplayer1.model.Music;
import com.example.easymusicplayer1.model.Ring;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;

public class RingAdapter extends ArrayAdapter<Ring> {

	int resourceId;
	
	Context mContext;
	
	
	
	public RingAdapter(Context context, int resource, List<Ring> objects) {
		super(context, resource, objects);
        this.resourceId = resource;
	    this.mContext = context;
	}
	
	
	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		Ring ring = getItem(position);
		
		View view = LayoutInflater.from(mContext).inflate(resourceId, null);
		
		TextView musicName = (TextView) view.findViewById(R.id.music_name);
		
		musicName.setText(ring.getName());
		
		return view;
	}

}
