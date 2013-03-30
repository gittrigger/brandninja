package com.havenskys.thescoopseattle;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.widget.TextView;

public class commentView extends Activity implements OnTouchListener, OnLongClickListener {

	private static String TAG = "Comment";

	private WebView mBrowser;
	private TextView mText;
	//private Bundle mIntentExtras;
	private long mCurrentID = 0;
	private SharedPreferences mSharedPreferences;
	private Editor mPreferencesEditor;
	private Custom mLog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLog = new Custom(this, TAG + " onCreate() 30");
		setContentView(R.layout.comment);
		
		mSharedPreferences = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
        mPreferencesEditor = mSharedPreferences.edit();
        long id = mSharedPreferences.contains("id") ? mSharedPreferences.getLong("id",0) : 0;
        //mPreferencesEditor.putLong("id", id); mPreferencesEditor.commit();
		
		//mIntentExtras = getIntent().getExtras();
		//long id = mIntentExtras != null ? mIntentExtras.getLong("id") : 0;
		
		mBrowser = (WebView) this.findViewById(R.id.comment_web);
		mText = (TextView) this.findViewById(R.id.comment_text);
		
		
		loadRecord(id);
		
	}
	
	
	
	private void loadRecord(long id) {
		
		if( mCurrentID == id ){return;}
		
		mCurrentID = id;
		Cursor lCursor = SqliteWrapper.query(this, getContentResolver(), DataProvider.CONTENT_URI, 
        		//new String[] { "_id", "address", "body", "strftime(\"%Y-%m-%d %H:%M:%S\", date, \"unixepoch\", \"localtime\") as date" },
        		//strftime("%Y-%m-%d %H:%M:%S"
				//new String[] {"_id", "title", "link", "datetime(date,'localtime') as date", "summary"  },
				new String[] {"_id", "title" },
				//new String[] { "_id", "address", "body", "date" },
        		"_id = " + id, 
        		null, 
        		null);
		
		if( lCursor != null ){
			startManagingCursor(lCursor);
			if ( lCursor.moveToFirst() ){
				String title = null;
				//String link = null;
				//String date = null;
				//String summary = null;
				
				if( lCursor.getColumnCount() == 5 ){/// <<<<<<<<<<<<<<<<<  LOOK HERE
					title = lCursor.getString(1) != null ? lCursor.getString(1) : "";
					//link = lCursor.getString(2) != null ? lCursor.getString(2) : "";
					//date = lCursor.getString(3) != null ? lCursor.getString(3) : "";
					//summary = lCursor.getString(4) != null ? lCursor.getString(4) : "";
					
					mLog.w(TAG,"Found rowid("+id+") title("+title+")");
					mText.setText(title);
					mBrowser.getSettings().setJavaScriptEnabled(true);
					mBrowser.getSettings().setSupportZoom(true);
					mBrowser.setInitialScale(80);
					mBrowser.setOnTouchListener(this);
					mBrowser.getSettings().setSaveFormData(true);
					mBrowser.canGoForward();
					mBrowser.getSettings().setSupportMultipleWindows(false);
					mBrowser.loadUrl("http://www.seashepherd.org/contact/general-public.html");
					
				}

			}
		}
	}



	private long mLastTouchDown = 0;
	private long mThisTouchDown = 0;
	public boolean onTouch(View v, MotionEvent event) {

		
		switch( event.getAction() ){
		case MotionEvent.ACTION_DOWN:
			mLog.i(TAG,"onTouch() DOWN x("+event.getX()+") y("+event.getY()+") scale("+mBrowser.getScale()+") touchtime("+event.getDownTime()+") touchhistory("+event.getHistorySize()+")");
			
			
			mThisTouchDown = System.currentTimeMillis();
			
			if( (mThisTouchDown - mLastTouchDown) < 500 ){
				if( mBrowser.getScale() < 1){ mBrowser.zoomIn(); }
			}
			
			mLastTouchDown = mThisTouchDown;
			
		case MotionEvent.ACTION_UP:
			mLog.i(TAG,"onTouch() UP x("+event.getX()+") y("+event.getY()+") scale("+mBrowser.getScale()+") touchtime("+event.getDownTime()+") touchhistory("+event.getHistorySize()+")");
			
			break;
		case MotionEvent.ACTION_MOVE:
			mLog.i(TAG,"onTouch() MOVE x("+event.getX()+") y("+event.getY()+") scale("+mBrowser.getScale()+") touchtime("+event.getDownTime()+") touchhistory("+event.getHistorySize()+")");
			
			break;
		case MotionEvent.ACTION_CANCEL:
			mLog.i(TAG,"onTouch() CANCEL x("+event.getX()+") y("+event.getY()+") scale("+mBrowser.getScale()+") touchtime("+event.getDownTime()+") touchhistory("+event.getHistorySize()+")");
			
			break;
		}
		
		return false;
	}


	public boolean onLongClick(View arg0) {
		mBrowser.zoomOut();
		mBrowser.zoomOut();
		mBrowser.zoomOut();
		return false;
	}
	
	@Override
	protected void onResume() {
		mLog.w(TAG,"onResume() ++++++++++++++++++++++++++++++++");
		
		super.onResume();
		long id = mSharedPreferences.contains("id") ? mSharedPreferences.getLong("id",0) : 0;
		loadRecord(id);
		
	}
	
}
