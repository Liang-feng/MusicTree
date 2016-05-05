package com.example.easymusicplayer1.utility;

import java.util.ArrayList;
import java.util.List;

import com.example.easymusicplayer1.R;
import com.example.easymusicplayer1.model.App;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class AppAdapter extends ArrayAdapter<App> implements OnClickListener{
	
	int resourceId;                                 //存放ListView上自定义布局的id
	
	ArrayList<Integer> lockAppList;                 //存放被锁的软件的在ListView的item项上的位置

	Boolean once = true;                                   //设置子线程只开启一次
	
	ArrayList<App> appList;
	
	static LockAppIsOpenThread lockAppIsOpenThread = null;
	
	Thread thread;
	
	Context context ;
	
	App app;
	
	View view = null;
	
	ImageView appIcon;
	
	TextView appName;
	
	Switch lockSwitch;
	
	ToggleButton toggleButton;
	
	class ViewHolder
	{
		ImageView appIcon1;
		
		TextView appName1;
		
		Switch lockSwitch1;
		
		ToggleButton toggleButton;
	}
	
    ViewHolder viewHolder;

	
	//只执行一次
	public AppAdapter(Context context, int resource, List<App> objects) {
		super(context, resource, objects);
        this.resourceId = resource;
        this.context = context;
        lockAppList = new ArrayList<Integer>();             //实例化!!!
        appList = new ArrayList<App>();
        this.appList = (ArrayList<App>) objects;
        
		lockAppIsOpenThread = new LockAppIsOpenThread(appList);
	}
	
	/**
	 * convertView将之前加载好的布局进行缓存 , 提升了ListView运行效率
	 */
	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		app = getItem(position);  //相当于获取传递进来的ArrayList<App>中的第position个App实例            //获取当前实例，从数据集合中获取当前项的数据项（实例）！ Get the data item associated with the specified position in the data set.
     	view = LayoutInflater.from(context).inflate(resourceId, null);   //引入布局


	/*	if(convertView == null)
		{
         	view = LayoutInflater.from(context).inflate(resourceId, null);   //引入布局
         	
			viewHolder = new ViewHolder();
            viewHolder.appIcon1 = (ImageView) view.findViewById(R.id.app_icon);
            viewHolder.appName1 = (TextView) view.findViewById(R.id.app_name);
           // viewHolder.lockSwitch1 = (Switch) view.findViewById(R.id.switch1);
            //viewHolder.toggleButton = (ToggleButton)view.findViewById(R.id.toggleButton1);
            view.setTag(viewHolder);
		}
		else
		{
			view = convertView;                           
			viewHolder = (ViewHolder) view.getTag();
		}
		
		appIcon = viewHolder.appIcon1;
		appName = viewHolder.appName1;*/
     	
		//lockSwitch = viewHolder.lockSwitch1;
		//toggleButton = viewHolder.toggleButton;
     	
     	this.appIcon = (ImageView) view.findViewById(R.id.app_icon);
        this.appName = (TextView) view.findViewById(R.id.app_name);
		//toggleButton = (ToggleButton)view.findViewById(R.id.toggleButton1);
        lockSwitch = (Switch) view.findViewById(R.id.switch1);
		
		appIcon.setImageDrawable(app.getIcon());
		appName.setText(app.getName());		
		lockSwitch.setChecked(false);                     //设置状态为关闭
		
		for(int index=0; index<lockAppList.size(); index++)           //为了防止刚才状态改变的button，滑下去又滑上来后状态不见了!!!所以做此恢复处理！
		{
     		if(lockAppList.get(index).intValue() == position)
     		{
     			lockSwitch.setChecked(true);
     		}
		}
	     /*这个是错误的，因为当提高ListView的效率的时候，你改变了lockSwitch的id，那么再第二次获取view.findViewById(R.id.switch1);的时候就会报错，所以改为setTag()
	      *lockSwitch.setId(position);                
		*/
		lockSwitch.setTag(position);
		//由于每一个item项都新new了view，所以这个和缓存大大不同，当滑上去的时候，上面的lockSwitch的状态并没改变，
		//所以不会调用setOnCheckedChangeListener()函数，move掉lockAppList中的元素，而缓存机制则会！
		lockSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				
				if(isChecked)         //如果toggleButton被选中
				{
					lockAppList.add((Integer) buttonView.getTag());
					lockAppIsOpenThread.setLockAppList(lockAppList);
					Log.e("AppAdapter", "被点击的switch的id =  " + String.valueOf(buttonView.getTag()));
				}
				else 
				{
					lockAppList.remove((Integer) buttonView.getTag());
					lockAppIsOpenThread.removeLockAppListElement((Integer) buttonView.getTag());
				}
				
			}
		});
		
		return view;
	}
	
	@Override
	public void onClick(View v) {

		
		
	}
	
	
	public static LockAppIsOpenThread getLockAppThread()
	{
		return lockAppIsOpenThread;
	}
	

}