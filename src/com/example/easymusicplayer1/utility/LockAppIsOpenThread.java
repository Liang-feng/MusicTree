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
 * 是否到达用户指定时间的线程。
 * @author feng
 *
 */
public class LockAppIsOpenThread extends Thread {
	
	ComponentName topActivity;
	
	String currentAppPackageName;                        //存储当前在栈顶运行的程序
	
	ActivityManager mActivityManager;               //活动管理器
	
	ArrayList<Integer> lockAppList;
	
	ArrayList<App> appList;
	
	Context context;
	
	private Boolean timeIsEnd = false;
	
	boolean isActivityAlive = true;                       //用来标记ChooseActivity是否还存在,若不存在，结束线程
	
	public LockAppIsOpenThread(ArrayList<App> appList1) {
	     this.appList = new ArrayList<App>();                   //这个不要可以么？

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
			
			if (timeIsEnd)               //用来判断设置的时间是否到达,配合ChooseTimeActivity来使用!!! 
			{

				topActivity = mActivityManager.getRunningTasks(1).get(0).topActivity;
				currentAppPackageName = topActivity.getPackageName();

				Log.e("LockAppIsOpenThread", "currentAppPackageName = " + currentAppPackageName);

				//加入lockAppList是否为空的原因是：有可能lockAppList是空的，当不设置软件锁的时候!
				if (lockAppList != null && !lockAppList.isEmpty()) // 由于线程先执行，lockAppList还没有传值进去，所以先执行
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
	 * 从ChooseTimeActivity中的子线程传来的参数!!!
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
