package com.chatfe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.chatfe.ChatfeProvider;
import com.chatfe.ChatfeProviderMetaData.TopicTableMetaData;
import com.chatfe.ChatfeProviderMetaData.AuthenticateTableMetaData;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.content.ContentResolver;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class UpdateService extends Service {

	private static final String TAG = UpdateService.class.getSimpleName();
	public static final String TRANSACTION_DONE = "com.chatfe.TRASACTION_DONE";
	private static final String apiPre = "http://api.chatfe.com?ses=";//http://api.chatfe.com?ses=
	private static final String topicAPI = "rt=topic";//"rt=topic";
	private static final String startAPI = "rt=start";//"rt=start";
	private static final String stopAPI = "rt=stop";//"rt=stop";
	String topicText;
	String topicImgUri;
	File tempDir;
	String localImgName; 
	File localImgFile; 
	
	//private HashMap<String, String> userInfo;
	
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		String tmpLocation = Environment.getExternalStorageDirectory().getPath() + "/topicImg/";
		tempDir = new File(tmpLocation);
		if(!tempDir.exists()){
			tempDir.mkdir();
			
		}
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		//return super.onStartCommand(intent, flags, startId);
		new Thread(new ChatfeUpdater()).start();
		Log.e(TAG, "Updateservice done. ");
		return Service.START_NOT_STICKY;
		
	}
	
	private class ChatfeUpdater implements Runnable{


		public ChatfeUpdater(){
			
		}
		
		@Override
		public void run() {
			String phoneNumber;
			String screenDpi;
			Display display;
			int width;
			int height;
			
			
			String [] projection = new String[]{
					AuthenticateTableMetaData.PHONE,
					AuthenticateTableMetaData.SCREEN_DENSITY,
					AuthenticateTableMetaData.SCREEN_WIDTH,
					AuthenticateTableMetaData.SCREEN_HEIGHT,
					AuthenticateTableMetaData.ENCRYPT_KEY
			};
		
			Uri authUri = ContentUris.withAppendedId(AuthenticateTableMetaData.CONTENT_URI, 1);
			Cursor cur = getContentResolver().query(authUri, projection, null, null, null);
			
			if(!cur.moveToFirst()){//data not there yet.
				
				
				phoneNumber = getPhoneNumber();
				phoneNumber = encryptPhone(phoneNumber); 
				screenDpi = Float.toString(getResources().getDisplayMetrics().density);
				display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
				width = display.getWidth();
				height = display.getHeight();
				
				ContentValues values = new ContentValues();
				values.put(AuthenticateTableMetaData.PHONE, phoneNumber);
				values.put(AuthenticateTableMetaData.SCREEN_DENSITY, screenDpi);
				values.put(AuthenticateTableMetaData.SCREEN_WIDTH, width);
				values.put(AuthenticateTableMetaData.SCREEN_HEIGHT, height);
				
				ContentResolver cr = getBaseContext().getContentResolver();
				Uri uri = cr.insert(ChatfeProviderMetaData.AuthenticateTableMetaData.CONTENT_URI, values);
			
			}
			else
			{	cur.moveToFirst();
				int phoneCol = cur.getColumnIndex(AuthenticateTableMetaData.PHONE);
				int dpiCol = cur.getColumnIndex(AuthenticateTableMetaData.SCREEN_DENSITY);
				int widthCol = cur.getColumnIndex(AuthenticateTableMetaData.SCREEN_WIDTH);
				int heightCol = cur.getColumnIndex(AuthenticateTableMetaData.SCREEN_HEIGHT);
				
				phoneNumber = cur.getString(phoneCol);
				screenDpi = cur.getString(dpiCol);
				width = cur.getInt(widthCol);
				height = cur.getInt(heightCol);
				
				
			}
			//create Url connection calling api: http://api.chatfe.com?aid=[encrypted(key+phone)]&rt=topic
			
			
			String updateUrl = apiPre + phoneNumber + "&" + topicAPI; 
			
			//String updateUrl = apiPre;
			//TopicData topicData = new TopicData();
			
			try{
				
				//set default in case xml is corrupted. 
				//TopicsXMLHandler.topicText = "Open Topic";
				//TopicsXMLHandler.topicImgUrl = "@drawable/splash";
				
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();
				
				
				URL sourceUrl = new URL(updateUrl); 
				
				TopicsXMLHandler myHandler = new TopicsXMLHandler();
				xr.setContentHandler(myHandler);
				
			
				xr.parse(new InputSource(sourceUrl.openStream()));
				
				if(myHandler.topicText == null){
					
					/*String [] textProjection = new String[]{
							TopicTableMetaData.TOPIC
					};
					
					Uri textUri = ContentUris.withAppendedId(TopicTableMetaData.CONTENT_URI, 1);
					topicText = oneItemQuery(textUri, textProjection);*/
					//topicText = "Open discussion";
					Log.e(TAG, "nothing found buddy!");
					return;
					
				}
				else{
					topicText = myHandler.topicText;
					
				}
				if(myHandler.topicImgUrl == null){
					/*String [] imgProjection = new String[]{
							TopicTableMetaData.REMOTE_IMG_URL
					};
					
					Uri imgUri = ContentUris.withAppendedId(TopicTableMetaData.CONTENT_URI, 1);
					topicImgUri = oneItemQuery(imgUri, imgProjection);*/
					return;
					
				}else{
					
					topicImgUri = myHandler.topicImgUrl;
				}
				
			}catch(Exception e){
				Log.e(TAG, e.toString());
				//System.out.println("XML Parsing Excpetion = " + e);
				return;
			}
			
			TopicDbUpdate();
			saveNewImage();   		
			
		}	
		
		public void TopicDbUpdate(){
			
			String topicString = topicText;
			String imgUrl = topicImgUri;
			
			
			
			localImgName = imgUrl.substring(imgUrl.lastIndexOf("/")+1);
			localImgFile = new File(tempDir.getPath() +"/" +localImgName);
			
			
			//update db with new values;
			Uri topicUri = ContentUris.withAppendedId(ChatfeProviderMetaData.TopicTableMetaData.CONTENT_URI, 1);
			ContentValues values = new ContentValues();
			values.put(TopicTableMetaData.TOPIC, topicString);
			values.put(TopicTableMetaData.REMOTE_IMG_URL, imgUrl);
			values.put(TopicTableMetaData.LOCAL_IMG_URI, localImgFile.toString());
			ContentResolver cr = getBaseContext().getContentResolver();
			
			int rowsUpdated;
			
			if((rowsUpdated = cr.update(topicUri, values, null, null)) == 0)
			{
				cr.insert(ChatfeProviderMetaData.TopicTableMetaData.CONTENT_URI, values);
				
			}
			
		}
		
		public void saveNewImage(){
			String imageUrl = null;
			String [] projection = new String[]{
					TopicTableMetaData.REMOTE_IMG_URL };
			Uri topic = TopicTableMetaData.CONTENT_URI;
			Cursor cur = getContentResolver().query(topic, projection, null, null, null);
			
			if(cur.moveToFirst())
			{
				
				int imgCol = cur.getColumnIndex(TopicTableMetaData.REMOTE_IMG_URL);
				imageUrl = cur.getString(imgCol);
			}
			
			// new url request. 
			try{	
				URL url = new URL(imageUrl);
				HttpURLConnection httpCon = (HttpURLConnection)url.openConnection();
				if(httpCon.getResponseCode() != 200)
					{Log.i(TAG, "not 200");
					throw new Exception("Failed to connect");}
				
				InputStream is = httpCon.getInputStream();
				
				
				FileOutputStream fos = new FileOutputStream(localImgFile);
				writeStream(is, fos);
				fos.flush(); 
				fos.close();
				is.close();
				notifyFinished();
				
				
			}catch(Exception e){
				Log.e(TAG, "New Image download failed!");
				
			}
			
			
		}
		
		private void writeStream(InputStream is, OutputStream fos) throws Exception{
			byte buffer[] = new byte[80000];
			int read = is.read(buffer);
			while(read != -1){
				fos.write(buffer, 0, read);
				read = is.read(buffer);
			}
		}
		
		private void notifyFinished(){
			
			Intent i = new Intent(TRANSACTION_DONE);
			UpdateService.this.sendBroadcast(i);
			UpdateService.this.stopSelf();
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
	public String encryptPhone(String s) {
	    return s;
		//try {
	        // Create MD5 Hash
	    	//code source: http://www.androidsnippets.com/create-a-md5-hash-and-dump-as-a-hex-string;
	       /* MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
	        digest.update(s.getBytes());
	        byte messageDigest[] = digest.digest(); */
	        
	        // Create Hex String
	      /*  StringBuffer hexString = new StringBuffer();
	        for (int i=0; i<messageDigest.length; i++)
	            hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
	        return hexString.toString();
	        
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    return "";*/
	}
	
	public String oneItemQuery(Uri dbUri, String [] projection){
		String qResult;
		
		Cursor cur = getContentResolver().query(dbUri, projection, null, null, null);
		
		if(!cur.moveToFirst()){//data not there yet.
			Log.e(TAG, "no data found in oneItemQuery");
			qResult = "Open Topic";
		}
		else
		{	cur.moveToFirst();
			int firstCol = cur.getColumnIndex(projection[0]);
			
			qResult = cur.getString(firstCol);
		}
		
		
		
		return qResult;
	}
	
	

}
