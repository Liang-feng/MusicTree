package com.example.easymusicplayer1.model;

import java.io.Serializable;

import android.graphics.drawable.Drawable;

/**
 * 事后加上Parcelable序列化
 * @author feng
 *
 */
public class App implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String packageName;
	
	private String name;
	
	private Drawable icon;
	
	public App(String packageName1 , String name1 , Drawable icon1) {

		this.packageName = packageName1;
		this.name = name1;
		this.icon = icon1;
	}
	
	public String getPackgeName()
	{
		return packageName;
	}
	
	public String getName()
	{
		return name;
	}

	public Drawable getIcon()
	{
		return icon;
	}
	

}
