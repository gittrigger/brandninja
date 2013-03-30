package com.havenskys.thescoopseattle;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.Toast;

public class LoadArticle extends Activity {
	
	
	private static String TAG = "LoadArticle";
	
	private Custom mLog;

	private Bundle mIntentExtras;
	private SharedPreferences mSharedPreferences;
	private Editor mPreferencesEditor;
	private NotificationManager mNM;
	private final int NOTIFY_ID = 1;
	public static int NOTIFY_ID_ARTICLE = 2;
	//private Handler mHandler;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.start);
        mLog = new Custom(this, TAG + " onCreate() 31");
        
        mIntentExtras = getIntent().getExtras();
		long id = mIntentExtras != null ? mIntentExtras.getLong("id") : 0;
        
		
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mLog.setNotificationManager(mNM);
        mNM.cancel(NOTIFY_ID_ARTICLE);
        
        mSharedPreferences = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
  	  	mPreferencesEditor = mSharedPreferences.edit();
  	  	mLog.setSharedPreferences(mSharedPreferences, mPreferencesEditor);
  	  	mPreferencesEditor.putLong("id", id).commit();

        Intent jump = new Intent();
			jump.setClass(this, com.havenskys.thescoopseattle.Start.class);
			jump.putExtra("id",id);
	    	jump.addFlags(jump.FLAG_ACTIVITY_CLEAR_TOP | jump.FLAG_ACTIVITY_NEW_TASK);
	    	startActivity(jump);
	    	
	    Toast.makeText(this, "Loading Article", Toast.LENGTH_LONG).show();
    	
		finish();

    }



}










