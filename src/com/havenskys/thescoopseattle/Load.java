package com.havenskys.thescoopseattle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

public class Load extends Activity implements Runnable {

	private Bundle mIntentExtras;
	private TextView mText;
	private Handler mHandler;
	private Context mContext;
	private Thread mThread;
	private String mAction;
	private long mId;
	
	public void onCreate(Bundle icicle){
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.load);
	
		mIntentExtras = getIntent().getExtras();
		
		mText = (TextView) findViewById(R.id.load_text);
		
		//this.getWindow().setf
		mContext = this;
		mHandler = new Handler();
		mThread = new Thread();
		mThread.setName("loading");
		
		mHandler.postDelayed(this, 100);
		
	}
	
	
	public void onStart(){
		super.onStart();
		Log.i(this.getClass().getName(),"onStart()");
		
		
		mAction = mIntentExtras != null ? mIntentExtras.getString("action") : "unstated";
		mId = mIntentExtras != null ? mIntentExtras.getLong("id") : 0;
		
		mText.setTextSize(32);
		mText.setText("Loading " + mAction  + " #"+ mId);

		
		//if( action == "Record" ){
		
        //finish();
		//}
		
	}

	public void run() {
		
		
		
		
		Intent browseView = new Intent(this, browseView.class);
        //browseView.setFlags( Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_FORWARD_RESULT );
        browseView.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_FORWARD_RESULT );
        browseView.putExtra("id", mId);
        startActivity(browseView);	
        
        finish();
	}
}
