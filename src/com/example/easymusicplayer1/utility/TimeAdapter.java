package com.example.easymusicplayer1.utility;

import java.util.ArrayList;
import java.util.List;

import com.example.easymusicplayer1.R;
import com.example.easymusicplayer1.activity.ChooseTimeActivity;

import android.content.Context;
import android.service.chooser.ChooserTargetService;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

public class TimeAdapter extends ArrayAdapter<String> {
	
	int resourcedId;
	
	Context mContext;
	
	TextView timeTv;
	
	Switch timeSwitch;
	
	ArrayList<Switch> switchList;
	
	ArrayList<String> timeList;
	
	int index;

	public TimeAdapter(Context context, int resource, List<String> objects) {
		super(context, resource, objects);
        this.resourcedId = resource;
	    mContext = context;
	    switchList = new ArrayList<Switch>();
	    timeList = (ArrayList<String>) objects;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		String time = this.getItem(position);
		
		View view = LayoutInflater.from(mContext).inflate(R.layout.choose_time_activity , null);
		
		timeTv = (TextView) view.findViewById(R.id.time_text_view);
		timeSwitch = (Switch) view.findViewById(R.id.time_switch);
		timeTv.setText(time);
		timeSwitch.setTag(position);
		timeSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


				String []timeArray = timeList.get(index).split(":");
				int hour = Integer.valueOf(timeArray[0]);
				int minute = Integer.valueOf(timeArray[1]);
				
				if(isChecked)
				{
					ChooseTimeActivity.addElementToHourOfList(hour);
					ChooseTimeActivity.addElementToMinuteOfList(minute);
					ChooseTimeActivity.setRelativeOperation(index , true);    //index为第item项个true为开启闹钟
				}
				else
				{
					ChooseTimeActivity.addElementToHourOfList(hour);
					ChooseTimeActivity.addElementToMinuteOfList(minute);
					ChooseTimeActivity.setRelativeOperation(index , false);    //false为关闭闹钟
				}
			}
		});
		
		switchList.add(timeSwitch);          //用于在ChooseTimeActivity操作其状态!!!
		Log.e("TimeAdpater", String.valueOf(switchList.size()));
		
		return view;
	}
	
	/**
	 * @return 返回item项上的switch列表
	 */
	public Switch getItemSwitch(int position)
	{
		index = position;
		return switchList.get(position);
	}

}
