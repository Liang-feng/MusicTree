package com.example.easymusicplayer1.activity;

import java.util.ArrayList;
import java.util.List;

import com.example.easymusicplayer1.R;
import com.example.easymusicplayer1.model.App;
import com.example.easymusicplayer1.utility.AppAdapter;
import com.example.easymusicplayer1.utility.GetAppListThread;
import com.example.easymusicplayer1.utility.MyApplication;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

/**
 * 用来显示系统的全部应用程序，先前觉得ListView显示的慢，搞了一下java的序列化，没搞成，以后多加注意
 * @author feng
 *
 */
public class TimingLockAppActivity extends Activity {
	
	public static int isOpen = 0;                       //0为不是当前打开的
	
	private static final int OK = 1;
	
	ListView appListView;
	
	ArrayList<App> appList;        //存储app的信息，//存储app实例，app实例带有包名，名称，icon，用于在AppAdapter中显示ImageView和TextView
	
	AppAdapter adapter;            //自定义adapter
		
	PackageManager packageManager;        //包管理器，可用于获取app的包名，app的名称，icon等
	
	List<PackageInfo> packageInfoList;
	
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			if(msg.what == OK) 
			{
	    		setDialog();           //设置一个dialog
			}

		}
		
		/**
		 * 设置一个dialog，用来进行用户提示
		 */
		private void setDialog()
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(TimingLockAppActivity.this , 1);
			dialog.setTitle("注意");
			dialog.setMessage("请选择要上锁的app以及在手机管家为本app设置白名单");
			dialog.setPositiveButton("确定", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {

					dialog.cancel();
				}
			});
	
			dialog.show();
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_list_view);
	
		initView();                           //用于获取布局控件的实例，以及设置相关监听事件
		initActionBar();                      //ActionBar的相关设置
	}
	
	

	
	/**
	 * 想要获取app数据，以防在启动本activity时候,阻塞
	 */
	@SuppressWarnings("unchecked")
	private void readDataFromFlatteningActivity() {
		//Intent intent = this.getIntent();
		//appList = (ArrayList<App>) intent.getExtras().getSerializable("app_list");
		//GetAppListThread getAppListThread = (GetAppListThread) intent.getSerializableExtra("get_app_list_thread");

		appList = new ArrayList<App>();
		appList = GetAppListThread.getAppList();
		Log.e("TimingLockAppActivity", appList.toString());
	}


	/**
	 * 用于获取布局控件的实例，以及设置相关监听事件,以及相关实例的初始化
	 */
	private void initView() {
		
		//readDataFromFlatteningActivity();
		
		//显示progressDialog，避免由于数据多，listView界面出现的慢，体验不好!!!
		final ProgressDialog progressDialog = new ProgressDialog(TimingLockAppActivity.this , 1);   //就0,1,2这三个风格,2比较好看
		progressDialog.setMessage("数据加载中");
		progressDialog.setCancelable(false);                         //禁止取消，否者影响体验!!!
		progressDialog.show();
		
		new Thread(new Runnable(){

			@Override
			public void run() {
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				progressDialog.cancel();
				//发送msg，操作主线程ui，显示提示信息
				Message msg = new Message();
				msg.what = OK;
				handler.sendMessage(msg);

			}}).start();

		
		initAppListData();                                            //初始化appList的数据
		appListView = (ListView) findViewById(R.id.app_list_view);
		adapter = new AppAdapter(TimingLockAppActivity.this , R.layout.timing_lock_app_activity , appList);
		appListView.setAdapter(adapter);
		
	}
	
	
	/**
	 * ActionBar的相关设置
	 */
    private void initActionBar() {
    	ActionBar actionBar = getActionBar();
    	actionBar.setTitle("选择APP");                          //设置actionBar左侧的文字
		actionBar.setDisplayShowHomeEnabled(false);        //设置ActionBar左侧的图标不可见
		actionBar.setDisplayShowTitleEnabled(true);       //设置ActionBar左侧的标题不可见
		actionBar.setDisplayHomeAsUpEnabled(true);         //设置ActionBar左侧的返回上一级的图标显示出来!!!
	}


	/**
	 * 初始化appList的数据 , 获取手机上的packageer管理器，可以取得手机app的icon，包名，名称
	 */
	private void initAppListData() {
		
		appList = new ArrayList<App>();

		packageManager = this.getPackageManager();       //Return PackageManager instance to find global package information
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
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Intent intent = new Intent(TimingLockAppActivity.this , FlatteningStartActivity.class);
		startActivity(intent);
		FlatteningStartActivity.isOpen = 1;
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		//MenuInflater    This class is used to instantiate menu XML files into Menu objects.
        //这个类是用来实例化菜单进入菜单对象的XML文件。
        //getMenuInfater()      Returns a MenuInflater with this context.
		getMenuInflater().inflate(R.menu.lock_activity_menu , menu);		
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home)           //点击了actionBar左侧方向键
		{
			finish();
			FlatteningStartActivity.isOpen = 1;
		}
		else if(item.getItemId() == R.id.choose_a_ring)
		{
			Intent intent = new Intent(TimingLockAppActivity.this , SetARingActivity.class);
			startActivity(intent);
			SetARingActivity.isOpen = 1;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
    //Return the handle to a system-level service by name. The class of the returned object varies（不同的） by the requested name. Currently available names are: 
	//通过名字返回系统级服务的句柄。不同的请求名字返回的对象的类不同。目前可用的名称是:
	//A android.app.ActivityManager for interacting with the global activity state of the system. 
    //返回一个ActivityManager实例，
	//Interact with the overall activities running in the system.
    //这个实例与在系统中的所有正在运行的activity相互作用
//	mActivityManager = (ActivityManager) this.getSystemService("activity");
	//Return a list of the tasks that are currently running, with the most recent being first and older ones after in order.
	//getRunningTasks()返回一系列的当前正在运行的任务，最近运行的排在第一，以前运行的排在后边，以此类推
	//The activity component at the top of the history stack of the task. This is what the user is currently doing.
    //topActivity    这个类组件在历史任务堆栈的顶端，这是用户当前正在做的!!!
//	topActivity = mActivityManager.getRunningTasks(1).get(0).topActivity;   
	//Return the package name of this component.
    //返回这个组件的包名
//	packgeName = topActivity.getPackageName();        //Return the package name of this component.
	//Return the class name of this component.
    //返回这个组件的类名
//	String className = topActivity.getClassName();

}