package com.example.easymusicplayer1.utility;

import java.io.Serializable;
import java.util.ArrayList;

import com.example.easymusicplayer1.model.App;

import android.util.Log;

public class GetAppListThread extends Thread implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static ArrayList<App> appList;
	
	public GetAppListThread() {

		appList = new ArrayList<App>();
	}
	
	@Override
	public void run() {
		super.run();
		
        InitAppList initData = new InitAppList();
        initData.initAppListData();
        appList = new ArrayList<App>();
        appList = initData.getAppList();
	}
	
	public static ArrayList<App> getAppList()
	{
		Log.e("GetAppListThread", appList.toString());
		return appList;
	}

}
