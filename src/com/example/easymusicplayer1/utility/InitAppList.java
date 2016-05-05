package com.example.easymusicplayer1.utility;

import java.util.ArrayList;
import java.util.List;

import com.example.easymusicplayer1.model.App;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public class InitAppList {
	
	private ArrayList<App> appList;

	PackageManager packageManager;        //包管理器，可用于获取app的包名，app的名称，icon等
	
	Context context;
	
	List<PackageInfo> packageInfoList;
	
	
	public InitAppList() {

		context = MyApplication.getContext();
		appList = new ArrayList<App>();
	}
	
	
	/**
	 * 初始化appList的数据 , 获取手机上的packageer管理器，可以取得手机app的icon，包名，名称
	 */
	public void initAppListData() {
		
		
		packageManager = context.getPackageManager();       //Return PackageManager instance to find global package information
		packageInfoList = packageManager.getInstalledPackages(0);   //Return a List of all packages that are installed on the device.
		
		for(PackageInfo packageInfo : packageInfoList)
		{
			
			Drawable appIcon = packageInfo.applicationInfo.loadIcon(packageManager);    //获取app的Icon
		    String appName = (String) packageInfo.applicationInfo.loadLabel(packageManager);        //获取app的名称
		    String packageName = packageInfo.packageName;                                   //获取app的包名，用于判断当前执行的app符不符合条件
		    App app = new App(packageName , appName , appIcon);
		    
		    appList.add(app);

		}
	}
	
	
	public ArrayList<App> getAppList() {

		return appList;
	}
	
	
	

}
