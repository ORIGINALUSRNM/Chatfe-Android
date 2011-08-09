package com.chatfe;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

import com.chatfe.ChatfeProviderMetaData.AuthenticateTableMetaData;
import com.chatfe.ChatfeProviderMetaData.TopicTableMetaData;



import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class TalkUpdaterService extends Service {

	public static final String TALKUPDATE_DONE = "com.chatfe.TALKUPDATE_DONE";
	private static final String TAG = TalkUpdaterService.class.getSimpleName();
	public static int talkStatus = 0;
	private static final String apiPre = "http://api.chatfe.com?ses=";
	private static final String startAPI = "rt=start";
	private static final String stopAPI = "rt=stop";
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		//return super.onStartCommand(intent, flags, startId);
		new Thread(new TalkUpdater()).start();
		return Service.START_NOT_STICKY;
		
	}
	
	private class TalkUpdater implements Runnable{


		public TalkUpdater(){
			
		}
		
		@Override
		public void run() {
			String phoneNumber = null;
			String statusUrl = null;
			
			String [] projection = new String[]{
					ChatfeProviderMetaData.AuthenticateTableMetaData.PHONE};
			Uri uri = ChatfeProviderMetaData.AuthenticateTableMetaData.CONTENT_URI;
			Cursor cur = getContentResolver().query(uri, projection, null, null, null);
			if(cur.moveToFirst()){
				int phoneCol = cur.getColumnIndex(ChatfeProviderMetaData.AuthenticateTableMetaData.PHONE);
				phoneNumber = cur.getString(phoneCol);
				
			}
			else{
				
				Log.e(TAG, "faile to get number");
			}
			
			
			if(TalkUpdaterService.talkStatus == 0){
				
				statusUrl = apiPre + phoneNumber + "&" + startAPI;
				Log.i(TAG, "talkStatus = " + TalkUpdaterService.talkStatus);
			}
			else if(TalkUpdaterService.talkStatus == 1){
				
				statusUrl = apiPre + phoneNumber + "&" + stopAPI;
				Log.i(TAG, "talkStatus = " + TalkUpdaterService.talkStatus);
			}
			
			try{
				Log.e(TAG, statusUrl);
				URL url = new URL(statusUrl);
				HttpURLConnection httpCon = (HttpURLConnection)url.openConnection();
				if(httpCon.getResponseCode() != 200)
					{Log.i(TAG, Integer.toString(httpCon.getResponseCode()));
					throw new Exception("Failed to connect");}
				else{
					if(TalkUpdaterService.talkStatus == 0){TalkUpdaterService.talkStatus = 1;}
					else TalkUpdaterService.talkStatus = 0;
					
				}
			}catch(Exception e){
				Log.e(TAG, e.getMessage());
				notifyFinished();
			}
			
			updateTalkStatus(TalkUpdaterService.talkStatus);
			notifyFinished();
				
		}
			
			
		}
	
	private void notifyFinished(){
		
		
		Intent i = new Intent(TALKUPDATE_DONE);
		TalkUpdaterService.this.sendBroadcast(i);
		TalkUpdaterService.this.stopSelf();
	
	}

	private void updateTalkStatus(int talkStatus){
		
		String talkStatusText;
		if(talkStatus == 0){
			talkStatusText = "no";
		}else{
			talkStatusText = "yes";
		}
		Uri authUri = ContentUris.withAppendedId(ChatfeProviderMetaData.AuthenticateTableMetaData.CONTENT_URI, 1);
		ContentValues values = new ContentValues();
		values.put(AuthenticateTableMetaData.AVAILABLE, talkStatusText);
		ContentResolver cr = getBaseContext().getContentResolver();
		
		int rowsUpdated;
		
		if((rowsUpdated = cr.update(authUri, values, null, null)) == 0)
		{
			cr.insert(ChatfeProviderMetaData.AuthenticateTableMetaData.CONTENT_URI, values);
			
		}
	}
	
}
