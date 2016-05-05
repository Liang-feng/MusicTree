package com.example.easymusicplayer1.utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class ParseDataWithJSONObject {
	
	public static final String TAG = "ParseDataWithJSONObject";
	
	public static void parseData(String jsonData)
	{
		Log.e(TAG , "jsonData = "+ jsonData);
		
		try {
			JSONObject jsonObject = new JSONObject(jsonData);
			JSONArray jsonArray = jsonObject.getJSONArray("showapi_res_body"); 
			System.out.println(jsonObject.toString());
			for(int index=0; index<jsonArray.length(); index++)
			{
				JSONObject jsonObject1 = jsonArray.getJSONObject(index);
				System.out.println(jsonObject.toString());
			}
			/*JSONArray jsonArray = new JSONArray(jsonData);
			Log.e(TAG, "length = " + String.valueOf(jsonArray.length()));
			for(int index=0; index<jsonArray.length(); index++)
			{
	     		JSONObject jsonObject = jsonArray.getJSONObject(index);
	     		String showapi_res_code = jsonObject.getString("showapi_res_code");
	     		System.out.println("showapi_res_code = " + showapi_res_code);
			}*/
		} catch (JSONException e) {
			Log.e(TAG, e.toString());
			e.printStackTrace();
		}
	}

}
