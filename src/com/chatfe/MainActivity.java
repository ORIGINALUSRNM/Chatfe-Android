package com.chatfe;
//test comment;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.chatfe.ChatfeProviderMetaData.AuthenticateTableMetaData;
import com.chatfe.ChatfeProviderMetaData.TopicTableMetaData;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();
	private Intent talkI;
	private static final String[] DAILY_TOPIC = new String[] {"Wearing dirty underwear"};
	private ListView listView;
	private int ht;
	private int wt;
	LinearLayout  linear;
	TextView text;
	ImageView image;
	Button btn;
	
	SimpleCursorAdapter mAdapter;
	/* Notes on images: ldpi(.75x) (240px x 250px)
	 					mdp (x) (320px x 333px)
	 					hdpi(1.5x) (480px x 500px)
	 					xhdpi(2.0x)
	 */
	@Override 
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		
		//create ui with java and update with current info.
		 LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
             ViewGroup.LayoutParams.FILL_PARENT,
             ViewGroup.LayoutParams.FILL_PARENT,
             0.0F);
		 

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0F);
       
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0F);
		
		linear = new LinearLayout(this);
		linear.setOrientation(LinearLayout.VERTICAL);
		linear.setLayoutParams(containerParams);
		
		text = new TextView(this);
		text.setText(getTopicText());
		text.setId(R.string.textId);
		text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
		text.setLayoutParams(textParams);
		
		image = new ImageView(this);
		image.setImageBitmap(getTopicImg());
		image.setId(R.string.imgId);
		image.setLayoutParams(imgParams);
		
		btn = new Button(this);
		btn.setText("Talk");
		btn.setId(R.string.talkbtnId);
		btn.setHeight(10);
		btn.setLayoutParams(textParams);
		
		String talkStatus;
		String btnString;
		talkStatus = getTalkStatus();
		if(talkStatus == "no"){
			btnString = "Talk";
		}else{
			btnString = "Waiting for Match!";
		}
		
		linear.addView(image);
		linear.addView(text);
		linear.addView(btn);
		
		setContentView(linear);
		//setContentView(R.layout.chatfe_topic);
		
		//updateUIWithCurAdapt();
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(UpdateService.TRANSACTION_DONE);
		registerReceiver(topicReceiver, intentFilter);
		
		IntentFilter intentFilterTalk = new IntentFilter();
		intentFilterTalk.addAction(TalkUpdaterService.TALKUPDATE_DONE);
		registerReceiver(talkUpdateReceiver, intentFilterTalk);
		
		
		btn.setOnClickListener(talkListener);
		//Button talkBtn = (Button)findViewById(R.id.talkBtn);
		//talkBtn.setOnClickListener(talkListener);
		
		talkI = new Intent(this, TalkUpdaterService.class);
		
		//call Update Service
		
		Intent mI = new Intent(this, PresenceMonitorService.class);
		startService(mI);
		
		Intent i = new Intent(this, UpdateService.class);
		i.putExtra("url", R.string.updateUrl);
		startService(i);
		
		
		
		
        
		//implement broadcast receiver to update UI.
	}
	
	public void onResume(){
		
		super.onPause();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(UpdateService.TRANSACTION_DONE);
		registerReceiver(topicReceiver, intentFilter);
		
		IntentFilter intentFilterTalk = new IntentFilter();
		intentFilterTalk.addAction(TalkUpdaterService.TALKUPDATE_DONE);
		registerReceiver(talkUpdateReceiver, intentFilterTalk);
		
	}
	
	public void onPause(){
		super.onDestroy();
		unregisterReceiver(topicReceiver);
		unregisterReceiver(talkUpdateReceiver);
		
	}
	public void onDestory(){
		super.onDestroy();
		unregisterReceiver(topicReceiver);
		unregisterReceiver(talkUpdateReceiver);
	}
	
	private void updateUI(){
		
		String topic = null;
		String imgLocation = null;
		
		String [] projection = new String[]{
				TopicTableMetaData.TOPIC,  
				TopicTableMetaData.LOCAL_IMG_URI};
		Uri uri = TopicTableMetaData.CONTENT_URI;
		Cursor cur = getContentResolver().query(uri, projection, null, null, null);
		
		if(cur.moveToFirst()){
			
			int topicCol = cur.getColumnIndex(TopicTableMetaData.TOPIC);
			int imgCol = cur.getColumnIndex(TopicTableMetaData.LOCAL_IMG_URI);
			topic = cur.getString(topicCol);
			imgLocation = cur.getString(imgCol);
		}
		Log.e(TAG, imgLocation);
		File topImg = new File(imgLocation);
		if(!topImg.exists())
		{
			Log.e(TAG, "failed to find image");
			return;
		}
		Bitmap bm = BitmapFactory.decodeFile(imgLocation);
		image.setImageBitmap(bm);
		//ImageView iv = (ImageView)findViewById(R.id.chatfe_img);
		//iv.setImageBitmap(bm);
		
		text.setText(topic);
		//TextView topicText = (TextView)findViewById(R.string.textId);
		//topicText.setText(topic);
		
		
		
		//File imageFile = new File();
		
	}
	private BroadcastReceiver topicReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				
				updateUI();
			}
		};
		
	private BroadcastReceiver talkUpdateReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				
				Log.i(TAG, "talkupdate received");
				
				
				//Button btn = (Button)findViewById(R.id.talkBtn);
				if(TalkUpdaterService.talkStatus == 0){
					
					btn.setText("Talk");
				}
				else if(TalkUpdaterService.talkStatus == 1){
					btn.setText("Waiting for Match!");
				}
				
			}
		};
		
	public String getTalkStatus(){
		String talkStatus;
		String [] projection = new String[]{
				AuthenticateTableMetaData.AVAILABLE
		};
	
		Uri authUri = ContentUris.withAppendedId(AuthenticateTableMetaData.CONTENT_URI, 1);
		Cursor cur = getContentResolver().query(authUri, projection, null, null, null);
		
		if(!cur.moveToFirst()){//data not there yet.
			talkStatus = "no";
		
		}	
		else{
			int statusCol = cur.getColumnIndex(AuthenticateTableMetaData.AVAILABLE);
			talkStatus = cur.getString(statusCol);
		}
		return talkStatus;
	}	
	private OnClickListener talkListener = new OnClickListener(){
    	
    	public void onClick(View v) {
    	   
    		startService(talkI);
    	   
    	 }
    	
    };
    
    public String getTopicText(){
    	String topic;
    	
    	String [] projection = new String[]{
				TopicTableMetaData.TOPIC};
		Uri uri = TopicTableMetaData.CONTENT_URI;
		Cursor cur = getContentResolver().query(uri, projection, null, null, null);
		if(!cur.moveToFirst()){
			
			topic = "Welcome to Chatfe!";
		}
		else{
			int topicCol = cur.getColumnIndex(TopicTableMetaData.TOPIC);
			topic = cur.getString(topicCol);
			
		}
		return topic;
    	
    	
    }
    
    public Bitmap getTopicImg(){
    	String localBmPath;
    	File tempDir;
    	Bitmap bd;
    	
    	
    	String [] projection = new String[]{
				TopicTableMetaData.LOCAL_IMG_URI};
		Uri uri = TopicTableMetaData.CONTENT_URI;
		Cursor cur = getContentResolver().query(uri, projection, null, null, null);
		
		if(!cur.moveToFirst()){
			bd = BitmapFactory.decodeResource(this.getResources(),R.drawable.chat_img);
			//BitmapDrawable  bd1 = (BitmapDrawable)this.getResources().getDrawable(R.drawable.chat_img);
			
		}
		else{
			
			int imgCol = cur.getColumnIndex(TopicTableMetaData.LOCAL_IMG_URI);
			localBmPath = cur.getString(imgCol);
			bd = BitmapFactory.decodeFile(localBmPath);
		}
		
    	return bd;
    }
    
    public void updateUIWithCurAdapt(){
    	
    	String [] projection = new String[]{
				TopicTableMetaData.TOPIC};
		Uri uri = TopicTableMetaData.CONTENT_URI;
		Cursor cur = getContentResolver().query(uri, projection, null, null, null);
    	
		if(!cur.moveToFirst()){
			String defaultTopic = this.getString(R.string.topic);
			
			
			BitmapDrawable  bd = (BitmapDrawable)this.getResources().getDrawable(R.drawable.chat_img);
			String defaultImgPath = this.getString(R.drawable.chat_img);
			
			ContentValues values = new ContentValues();
			values.put(TopicTableMetaData.TOPIC, defaultTopic);
			values.put(TopicTableMetaData.LOCAL_IMG_URI, defaultImgPath);
			
			ContentResolver cr = getBaseContext().getContentResolver();
			Uri resultUri = cr.insert(uri, values);
	
		}
		
    	Cursor cur2 = getContentResolver().query(uri, projection, null, null, null);
    	
		SimpleCursorAdapter curAdapt = new SimpleCursorAdapter(this,
    						R.layout.list_item, 
    						cur2,
    						new String[]{TopicTableMetaData.TOPIC},
    						new int[] {	android.R.id.list});
    	
    }
    
    
}
