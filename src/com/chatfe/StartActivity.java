package com.chatfe;

import com.chatfe.StartActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

public class StartActivity extends Activity {
    /** Called when the activity is first created. */
	private static final String TAG = StartActivity.class.getSimpleName();
	private int screenDpi;
	private String sDpi;
    @Override 
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        screenDpi = getScreenDpi();
        sDpi = Integer.toString(screenDpi);
        Log.e(TAG, sDpi);
        setContentView(R.layout.main);
        final int welcomeScreenTime = 3000;
        Thread welcomeThread = new Thread(){
        	
	        int wait = 0;
	        @Override
	        public void run(){
	        	try{
	        		super.run();
	        		while(wait < welcomeScreenTime){
	        			sleep(100);
	        			wait += 100;
	        		}
	        	}catch(Exception e){
	        			Log.e(TAG, "Exception: " + e);
	        			
	        	}finally{
	        			
	        		Intent i = new Intent(StartActivity.this, MainActivity.class);
	        		i.putExtra("dpi", screenDpi);	
	        		startActivity(i);
	        		finish();
	        	}
	        		
	        }
        	
         };
         welcomeThread.start();
    }
    
    public int getScreenDpi(){
		
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.densityDpi;
	}
    
}