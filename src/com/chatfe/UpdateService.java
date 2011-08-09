package com.chatfe;

import java.io.File;
import java.util.HashMap;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class UpdateService extends Service {

	private static final String TAG = UpdateService.class.getSimpleName();
	private HashMap<String, String> userInfo;
	
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		String tmpLocation = Environment.getExternalStorageDirectory().getPath() + "/phone/";
		File tempDir = new File(tmpLocation);
		if(!tempDir.exists()){
			tempDir.mkdir();
		}
	
		
		//userInfo = getAuthInfo();
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
		
		//new Thread(new CredConnect());
		
		
	}
	
	private class CredConnect implements Runnable{


		public CredConnect(){
			
		}
		
		@Override
		public void run() {
			Log.i(TAG, "in fucking new thread");
			
		}
		
		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	public HashMap<String,String> getAuthInfo(){
		
		HashMap<String, String> authInfo = new HashMap<String, String>();
		
		
		if(getPhoneNumber().matches("\\d{10}")){ 
			
			authInfo.put("phone", getPhoneNumber());
		}
		else
		{
			authInfo.put("phone", "");
			Log.e(TAG, "Invalid Phone number");
		}
		
		return authInfo;
	}
	
	public String getPhoneNumber(){
		TelephonyManager mTelephonyMgr;
		mTelephonyMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		return mTelephonyMgr.getLine1Number();
	}
	
	

}
