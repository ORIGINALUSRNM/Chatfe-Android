package com.chatfe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();
	private String screenDpi;
	//final float scale = getResources().getDisplayMetrics().density;
	@Override 
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chatfe_topic);
		//call Update Service
		
		Intent i = new Intent(this, UpdateService.class);
		i.putExtra("url", R.string.updateUrl);
		startService(i);
		stopService(i);
		//implement broadcast receiver to update UI.
	}
}
