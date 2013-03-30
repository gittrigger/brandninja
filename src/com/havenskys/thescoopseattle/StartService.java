package com.havenskys.thescoopseattle;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class StartService extends Activity {
	
	private static String TAG = "StartService";
	
	private Custom mLog;

	private NotificationManager mNM;
	private final int NOTIFY_ID = 1;
	//private Handler mHandler;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.start);
        mLog = new Custom(this, TAG + " onCreate() 25");
        
		//Toast.makeText(this, mLog.APP +" synchronizing is disabled, restart this application to reactivate.", Toast.LENGTH_LONG).show();
		
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent service = new Intent();
		service.setClass(this, com.havenskys.thescoopseattle.SyncService.class);
		
    	startService(service);
		//mNM.cancel(NOTIFY_ID);
		finish();

    }



}











