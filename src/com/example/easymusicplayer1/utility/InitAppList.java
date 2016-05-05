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

	PackageManager packageManager;        //���������������ڻ�ȡapp�İ�����app�����ƣ�icon��
	
	Context context;
	
	List<PackageInfo> packageInfoList;
	
	
	public InitAppList() {

		context = MyApplication.getContext();
		appList = new ArrayList<App>();
	}
	
	
	/**
	 * ��ʼ��appList������ , ��ȡ�ֻ��ϵ�packageer������������ȡ���ֻ�app��icon������������
	 */
	public void initAppListData() {
		
		
		packageManager = context.getPackageManager();       //Return PackageManager instance to find global package information
		packageInfoList = packageManager.getInstalledPackages(0);   //Return a List of all packages that are installed on the device.
		
		for(PackageInfo packageInfo : packageInfoList)
		{
			
			Drawable appIcon = packageInfo.applicationInfo.loadIcon(packageManager);    //��ȡapp��Icon
		    String appName = (String) packageInfo.applicationInfo.loadLabel(packageManager);        //��ȡapp������
		    String packageName = packageInfo.packageName;                                   //��ȡapp�İ����������жϵ�ǰִ�е�app������������
		    App app = new App(packageName , appName , appIcon);
		    
		    appList.add(app);

		}
	}
	
	
	public ArrayList<App> getAppList() {

		return appList;
	}
	
	
	

}
