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
	
	int resourceId;                                 //���ListView���Զ��岼�ֵ�id
	
	ArrayList<Integer> lockAppList;                 //��ű������������ListView��item���ϵ�λ��

	Boolean once = true;                                   //�������߳�ֻ����һ��
	
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

	
	//ִֻ��һ��
	public AppAdapter(Context context, int resource, List<App> objects) {
		super(context, resource, objects);
        this.resourceId = resource;
        this.context = context;
        lockAppList = new ArrayList<Integer>();             //ʵ����!!!
        appList = new ArrayList<App>();
        this.appList = (ArrayList<App>) objects;
        
		lockAppIsOpenThread = new LockAppIsOpenThread(appList);
	}
	
	/**
	 * convertView��֮ǰ���غõĲ��ֽ��л��� , ������ListView����Ч��
	 */
	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		app = getItem(position);  //�൱�ڻ�ȡ���ݽ�����ArrayList<App>�еĵ�position��Appʵ��            //��ȡ��ǰʵ���������ݼ����л�ȡ��ǰ��������ʵ������ Get the data item associated with the specified position in the data set.
     	view = LayoutInflater.from(context).inflate(resourceId, null);   //���벼��


	/*	if(convertView == null)
		{
         	view = LayoutInflater.from(context).inflate(resourceId, null);   //���벼��
         	
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
		lockSwitch.setChecked(false);                     //����״̬Ϊ�ر�
		
		for(int index=0; index<lockAppList.size(); index++)           //Ϊ�˷�ֹ�ղ�״̬�ı��button������ȥ�ֻ�������״̬������!!!�������˻ָ�����
		{
     		if(lockAppList.get(index).intValue() == position)
     		{
     			lockSwitch.setChecked(true);
     		}
		}
	     /*����Ǵ���ģ���Ϊ�����ListView��Ч�ʵ�ʱ����ı���lockSwitch��id����ô�ٵڶ��λ�ȡview.findViewById(R.id.switch1);��ʱ��ͻᱨ�����Ը�ΪsetTag()
	      *lockSwitch.setId(position);                
		*/
		lockSwitch.setTag(position);
		//����ÿһ��item���new��view����������ͻ�����ͬ��������ȥ��ʱ�������lockSwitch��״̬��û�ı䣬
		//���Բ������setOnCheckedChangeListener()������move��lockAppList�е�Ԫ�أ������������ᣡ
		lockSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				
				if(isChecked)         //���toggleButton��ѡ��
				{
					lockAppList.add((Integer) buttonView.getTag());
					lockAppIsOpenThread.setLockAppList(lockAppList);
					Log.e("AppAdapter", "�������switch��id =  " + String.valueOf(buttonView.getTag()));
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