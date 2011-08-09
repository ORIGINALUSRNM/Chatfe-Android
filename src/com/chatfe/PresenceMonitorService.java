package com.chatfe;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class PresenceMonitorService extends Service {

	@Override
	public void onCreate() {
		super.onCreate();
		IntentFilter ifShutdown = new IntentFilter();
		ifShutdown.addAction(Intent.ACTION_SHUTDOWN);
		registerReceiver(shutdownReceiver, ifShutdown);
		
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		return Service.START_NOT_STICKY;
		
	}
	
	
	private BroadcastReceiver shutdownReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			try{
				
				URL url = new URL("http://api.chatfe.com?ses=6467853199&rt=shutdown");
				HttpURLConnection httpCon = (HttpURLConnection)url.openConnection();
				
			}catch(Exception e){
				
				
			}
			Log.e("in monitor", "shutting down");
		}
    	
    	
    };
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
