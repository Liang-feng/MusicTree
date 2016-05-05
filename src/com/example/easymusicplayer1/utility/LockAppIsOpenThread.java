package com.example.easymusicplayer1.utility;

import java.util.ArrayList;

import com.example.easymusicplayer1.activity.SecondActivity;
import com.example.easymusicplayer1.model.App;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * �Ƿ񵽴��û�ָ��ʱ����̡߳�
 * @author feng
 *
 */
public class LockAppIsOpenThread extends Thread {
	
	ComponentName topActivity;
	
	String currentAppPackageName;                        //�洢��ǰ��ջ�����еĳ���
	
	ActivityManager mActivityManager;               //�������
	
	ArrayList<Integer> lockAppList;
	
	ArrayList<App> appList;
	
	Context context;
	
	private Boolean timeIsEnd = false;
	
	boolean isActivityAlive = true;                       //�������ChooseActivity�Ƿ񻹴���,�������ڣ������߳�
	
	public LockAppIsOpenThread(ArrayList<App> appList1) {
	     this.appList = new ArrayList<App>();                   //�����Ҫ����ô��

	     this.appList = appList1;
	     context = MyApplication.getContext();
	     
	 	mActivityManager = (ActivityManager) context.getSystemService("activity");
		Log.e("LockAppIsOpenThread", appList.toString());

	}
	
	
	@Override
	public void run() {
		super.run();
		
		
		while (isActivityAlive ) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block e.printStackTrace();
			}
			
			if (timeIsEnd)               //�����ж����õ�ʱ���Ƿ񵽴�,���ChooseTimeActivity��ʹ��!!! 
			{

				topActivity = mActivityManager.getRunningTasks(1).get(0).topActivity;
				currentAppPackageName = topActivity.getPackageName();

				Log.e("LockAppIsOpenThread", "currentAppPackageName = " + currentAppPackageName);

				//����lockAppList�Ƿ�Ϊ�յ�ԭ���ǣ��п���lockAppList�ǿյģ����������������ʱ��!
				if (lockAppList != null && !lockAppList.isEmpty()) // �����߳���ִ�У�lockAppList��û�д�ֵ��ȥ��������ִ��
				{
					for (Integer index : lockAppList) {
						App app = appList.get(index.intValue());
						if (app.getPackgeName().equals(currentAppPackageName)) {
							Intent intent = new Intent(context, SecondActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							context.startActivity(intent);
							break;
						}
					}
				}

			}
		}
	}
	
	
	public void setLockAppList(ArrayList<Integer> lockAppList1)
	{
		lockAppList = new ArrayList<Integer>();
		lockAppList = lockAppList1;
	}
	
	public void removeLockAppListElement(Object object)
	{
		lockAppList.remove(object);
	}
	
	/**
	 * ��ChooseTimeActivity�е����̴߳����Ĳ���!!!
	 * @param timeIsEnd1
	 */
	public void setTimeIsEnd(Boolean timeIsEnd1)
	{
		this.timeIsEnd = timeIsEnd1;
	}
	
	
	public void setIsActivityAlive(Boolean bool)
	{
		isActivityAlive = bool;
	}

}
