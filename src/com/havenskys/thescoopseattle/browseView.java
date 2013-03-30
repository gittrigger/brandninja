package com.havenskys.thescoopseattle;

import java.lang.Thread.State;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class browseView extends Activity implements Runnable {

	private static String TAG = "Browse";
	
	private WebView mSummary, mContent;
	private TextView mType, mTitle, mDate, mAuthor, mConsoleText;
	private ImageView mImage, mConsoleTouch;
	private LinearLayout mImageCase;
	//private ImageView mBackground;
	private RelativeLayout mLinearLayout;
	private String mLink;
	//private Bundle mIntentExtras;
	private long mCurrentID = -1;
	private SharedPreferences mSharedPreferences;
	private Editor mPreferencesEditor;
	private Custom mLog;
	int mWidth, mHeight;
	private long mActionTouchEventTime;
	private String mYesterday, mToday;
	private int mMidButtonWidth, mRightButtonStart, mLeftButtonEnd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLog = new Custom(this, TAG + " onCreate() 49");
		
		setContentView(R.layout.browser);
		

		loadRelative();

		mSharedPreferences = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
        mPreferencesEditor = mSharedPreferences.edit();
        mLog.setSharedPreferences(mSharedPreferences, mPreferencesEditor);
        
        long id = mSharedPreferences.contains("id") ? mSharedPreferences.getLong("id",-1) : -1;		
		
		mResolver = this.getContentResolver();
		mContext = this;
		
		mConsoleText = (TextView) findViewById(R.id.browser_text);
	    mConsoleTouch = (ImageView) findViewById(R.id.browser_footer);
	    mConsoleTouch.setFocusable(false);
	    mConsoleTouch.setFocusableInTouchMode(false);
	    
	    mHandler = new Handler();
	      
	      mThread = new Thread(null, this, "touchconsole_runnable");
	      mThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler(){
	
			public void uncaughtException(Thread thread, Throwable ex) {
				// TODO Auto-generated method stub
				mLog.e(TAG, "mThread uncaughtException() " + ex.getMessage() );
				//mThread.start();
			}
	    	  
	      });
	      mThread.start();

		
        
        
        //if( id != mCurrentID ){
        	
	        //mPreferencesEditor.putLong("id", id); mPreferencesEditor.commit();
	
	
			Bundle ie = getIntent().getExtras();
			long iid = ie != null ? ie.getLong("id") : -1;
			if( iid > -1 ){
				id = iid;
			}
        
	        mContent = (WebView) this.findViewById(R.id.browser_viewer);
			mContent.setBackgroundColor(Color.argb(100, 0, 0, 0));
			mContent.getSettings().setJavaScriptEnabled(true);
			mContent.getSettings().setMinimumFontSize(10);
			mContent.getSettings().setTextSize(WebSettings.TextSize.NORMAL);
			//mContent.loadDataWithBaseURL(link, content, "text/html", "UTF-8", link);
			mContent.getSettings().setSupportZoom(true);
			//mContent.post(new Runnable(){public void run(){ mContent.setVisibility(View.GONE); } });
			
			mLinearLayout = (RelativeLayout) this.findViewById(R.id.browser);
			mImage = (ImageView) this.findViewById(R.id.browser_image);
			mImageCase = (LinearLayout) findViewById(R.id.browser_imagecase);
			mType = (TextView) this.findViewById(R.id.browser_type);
			mTitle = (TextView) this.findViewById(R.id.browser_title);
			mDate = (TextView) this.findViewById(R.id.browser_date);
			mAuthor = (TextView) this.findViewById(R.id.browser_author);
			
			//mImageCase.setFocusable(true); mImageCase.setFocusableInTouchMode(true);
			mImageCase.setVisibility(LinearLayout.VISIBLE);
			
			//mImageCase.requestFocus();
			//mImageCase.requestFocusFromTouch();
			
			
			
			mImageCase.setOnFocusChangeListener(new OnFocusChangeListener(){
				private Drawable unselected = mContext.getResources().getDrawable(R.drawable.blackpearl);
				private Drawable selected = mContext.getResources().getDrawable(R.drawable.blackpearl_selected);
				public void onFocusChange(View v, boolean hasFocus) {
					if( hasFocus ){
						mImageCase.setBackgroundDrawable(selected);
					}else{
						mImageCase.setBackgroundDrawable(unselected);
					}
				}
			});
			
			mImageCase.setOnClickListener(new OnClickListener(){
				public void onClick(View v) {
					mImageCase.setVisibility(LinearLayout.GONE);
					mContent.requestFocus();
				}
			});
			
			mImageCase.setOnTouchListener(new OnTouchListener(){
				public boolean onTouch(View v, MotionEvent event) {
					if( event.getAction() == MotionEvent.ACTION_DOWN ){
						mImageCase.setVisibility(LinearLayout.GONE);
						mContent.requestFocus();
						//return true;
					}
					return true;
				}
			});
			
			mContent.setOnFocusChangeListener(new OnFocusChangeListener(){
				public void onFocusChange(View v, boolean hasFocus) {
					if( hasFocus && mImageCase.getVisibility() == ImageView.VISIBLE ){
						mImageCase.requestFocusFromTouch();
					}
				}
			});
			
			
			
			//mCurrentID = id;
			//loadRecord(id);
			
        //}
			
			
			//mConsoleText.setText("Touch");
			//final int totalF = total;
			//mConsoleText.postDelayed(new Runnable(){ public void run() { mConsoleText.setText(totalF + " Records"); } }, 10 * 1000);
			  
			//mConsoleText.postDelayed(new Runnable(){ public void run() { mConsoleText.setText("Touch Here"); } }, 5 * 1000);
			mConsoleText.postDelayed(new Runnable(){ public void run() { mConsoleText.setText(""); } }, 10 * 1000);
			  
			  mConsoleTouch.setOnTouchListener(new OnTouchListener(){

				public boolean onTouch(View v, MotionEvent event) {

					if(event.getAction() == MotionEvent.ACTION_UP){
						mActionTouchEventTime = 0;
					}
					if(event.getAction() == MotionEvent.ACTION_CANCEL){
						mActionTouchEventTime = 0;
					}
					
					if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE){
						int timetrigger = 0;
						
						// Right
						if( event.getX() > mRightButtonStart ){
							timetrigger = 1000 * 7;
							
						//Left
						}else if(event.getX() < mLeftButtonEnd){
							timetrigger = 330;
							
						//Middle
						}else if(event.getX() > mLeftButtonEnd && event.getX() < mRightButtonStart ){
							timetrigger = 1000 * 2; // Although the screen will already be switch probably.
						}
						
						if( (System.currentTimeMillis() - mActionTouchEventTime) < timetrigger ){ // Future configurable, scroll rate
							return true;
						}
						
						mActionTouchEventTime = System.currentTimeMillis();
						
						int myp = mContent.getHeight();
						int myy = mContent.getScrollY();
						int myt = mContent.getContentHeight();
						
						if( event.getX() > mRightButtonStart ){
							// Right
							//int got = myy+myp-(myp/2);
							int got = myy+myp-75;
							if( got >= myt-myp ){
								mContent.scrollTo(0, myt-myp);
							}else{
								mContent.scrollTo(0, got);
							}
						}else if(event.getX() < mLeftButtonEnd){
							// Left
							int got = myy-myp+75;
							if( got < (myp/4) ){got = 0;}
							mContent.scrollTo(0, got);
						}else if(event.getX() > mLeftButtonEnd && event.getX() < mRightButtonStart ){
							// Middle
							clearView();
							setTitle("");
							finish();
						}

					}
					return true;
					
				}
				  
			  });
			  //*
			
			  
			  
	}
	
	private ContentResolver mResolver;
	private Context mContext;
	private Handler mUpdateReadHandler = new Handler() {
	    public void handleMessage(Message msg) {
	    	//ImageView iv = (ImageView) findViewById(R.id.browser_state);
			//iv.setImageResource(R.drawable.docdot);
	    	
	    	Bundle b = msg.getData();
	    	long id = b.containsKey("id") ? b.getLong("id",-1) : -1;
	    	
	    	long currentid = mSharedPreferences.contains("id") ? mSharedPreferences.getLong("id",-1) : -1;
	    	int tab = mSharedPreferences.contains("tab") ? mSharedPreferences.getInt("tab",-1) : -1;
	    	if( tab == 1 && id == currentid ){
		    	mLog.i(TAG, "mUpdateSeenHandler() 56 id("+id+")");
				ContentValues setValues = new ContentValues();
				setValues.put(Custom.READ, System.currentTimeMillis() );
				SqliteWrapper.update(mContext, mResolver, DataProvider.CONTENT_URI, setValues, "_id = " + id, null);
				ImageView iv = (ImageView) findViewById(R.id.browser_state);
				iv.setImageResource(android.R.drawable.ic_menu_save);
				iv.setVisibility(View.VISIBLE);
	    	}
	    }
	};


    private void loadRecord(long id) {
    	
    	mLog.w(TAG,"loadRecord() ++++++++++++++++++++++++++++++++");
    	
    	//if( id == mCurrentID ){ return; }
    	
    	clearView();

    	mCurrentID = id;
    	
    	if( id < 0 ){
    		mImage.setImageResource(R.drawable.globe);
    	}
    	
    	mImageCase.setVisibility(LinearLayout.VISIBLE);
    	//mImageCase.requestFocus();
    	
    	if( id < 0 ){
    		return;
    	}else{
    		mImageCase.requestFocusFromTouch();
    	}
    	
    	
    	
		
		
    	//mContent.loadData("<html><body bgcolor=#FFFFFF><font size=4><center>Loading</center></font></body></html>", "text/html", "UTF-8");
    	
    	//mCurrentID = id;
		Cursor lCursor = SqliteWrapper.query(this, getContentResolver(), DataProvider.CONTENT_URI, 
        		//new String[] { "_id", "address", "body", "strftime(\"%Y-%m-%d %H:%M:%S\", date, \"unixepoch\", \"localtime\") as date" },
        		//strftime("%Y-%m-%d %H:%M:%S"
        		new String[] {"_id", "title", "link", "datetime(date,'localtime')", "content", "author", "summary", "contenturl", "type", "read" },
				//new String[] { "_id", "address", "body", "date" },
        		"_id = " + id,
        		null, 
        		null);
		
		
		if( lCursor != null ){
			startManagingCursor(lCursor);
			if ( lCursor.moveToFirst() ){
				
				String title = null;
				String link = null;
				String date = null;
				String content = null;
				String author = null;
				String summary = null;
				String contenturl = null;
				int feedid = 0;
				long readtime = 0;
				
				if( lCursor.getColumnCount() == 10 ){/// <<<<<<<<<<<<<<<<<  LOOK HERE
					title = lCursor.getString(1) != null ? lCursor.getString(1) : "";
					link = lCursor.getString(2) != null ? lCursor.getString(2) : "";
					date = lCursor.getString(3) != null ? lCursor.getString(3) : "";
					content = lCursor.getString(4) != null ? lCursor.getString(4) : "";
					author = lCursor.getString(5) != null ? lCursor.getString(5) : "";
					summary = lCursor.getString(6) != null ? lCursor.getString(6) : "";
					contenturl = lCursor.getString(7) != null ? lCursor.getString(7) : "";
					feedid = lCursor.getInt(8);
					readtime = lCursor.getLong(9);
					// UPDATE THE REQUIRED COL COUNT ABOVE ^^^^^^^^^^^^^^^^ LOOK THERE
					
					if( readtime > 0 ){
						ImageView iv = (ImageView) findViewById(R.id.browser_state);
						iv.setImageResource(android.R.drawable.ic_menu_save);
						iv.setVisibility(View.VISIBLE);
					}
					
					Message msg = new Message();
					Bundle b = new Bundle();
					b.putLong("id", id);
					msg.setData(b);
					mUpdateReadHandler.sendMessageDelayed(msg,1880);
					
					
					String longname = mLog.dataFeed[feedid][0];
					int icon = Integer.parseInt(mLog.dataFeed[feedid][1]);
					int topimage = Integer.parseInt(mLog.dataFeed[feedid][2]);
					
					date = date.replaceFirst(mToday, "Today").replaceFirst(mYesterday, "Yesterday").replaceFirst(":..$", "");

					mLog.w(TAG,"Found rowid("+id+") title("+title+")");
					mType.setText(longname);
					mTitle.setText(title.replaceAll("&quot;", "\"").replaceAll("\\?s", "'s"));
					mDate.setText(date);
					mAuthor.setText(author);
					mImage.setImageResource(topimage);
					//mBackground.setImageResource(topimage);
					//mBackground.setImageDrawable(this.getResources().getDrawable(topimage));

					
					if( author.length() > 0 ){
						if( !author.contains("By") ){
							author = "By " + author;
						}
						author += "<br>\n";
					}
					
					
					if( feedid == 5 || feedid == 6 ){
						mLog.w(TAG,"NPR Fix Content: " + content);
						content = content.replaceAll("<p><a href.*?email.>.*", "");
					}
					

					mContent.setBackgroundColor(Color.argb(100, 0, 0, 0));
					mContent.setVisibility(View.VISIBLE);
					
					//mContent = (WebView) this.findViewById(R.id.browser_viewer);
					//mContent.getSettings().supportMultipleWindows();
					//mContent.getSettings().setJavaScriptEnabled(true);
					//mContent.loadDataWithBaseURL(link, content, "text/html", "UTF-8", link);
					//mContent.getSettings().setSupportZoom(true);
					
					if( contenturl.length() > 0 ){
						link = contenturl;
						//mContent.loadDataWithBaseURL(link, content, "text/html", "UTF-8", link);
					}
					if(content.length() > 0 ){
						// Good
					}else if( summary.length() > 0 ){
							content += summary + "<br>\n";
					} else {
						content += "\n<br>\n<br><center><b><a href=\""+ link + "\">View Complete Article</a></b></center>";
					}
					
					
					
					//mTitle.setVisibility(View.GONE);
					mDate.setVisibility(View.GONE);
					mType.setVisibility(View.GONE);
					String head = "<div style=\"font-size:14px;align:center;\">"+ longname;
					if( !Custom.PUBLISH ){
						head += "("+feedid+")";
					}
					head += "<br>\n" + author + date + "<br>\n</div>";
					if( feedid != 0 ){
						head += "<br>\n";
					}
					
					if( content.regionMatches(true, 0, title, 0, content.length()-1) || feedid == 0 ){
						//mTitle.setVisibility(View.GONE);
					}else{
						//mTitle.setVisibility(View.GONE);
						head += "\n<center><b>"+title+"</b></center>\n<br>\n";
					}
					
					// bgproperties=\"fixed\" background=\"file:///android_asset/grayback.png\"
					mContent.loadDataWithBaseURL(link, "<html><style>body,div,span {font-size: 20px;}; h1,h2,h3,h4 {font-size: 21px; text-align: middle;}; </style><body bgcolor=#000000 text=#e0e0e0 link=#0066cc vlink=#0066cc>"+head+"<div align=justify>" + content + "</div><br>\n<br>\n<br>\n<br>\n<br>\n<br>\n</body></html>", "text/html", "UTF-8", link);
					//mContent.setBackgroundResource(R.drawable.grayback);
					
					//mContent.setBackgroundColor(Color.argb(100, 0, 0, 0));
					//mContent.setVisibility(View.VISIBLE);
					//mContent.refreshDrawableState();
					
					//mContent.loadUrl("about:blank");
					
					RelativeLayout webviewl = (RelativeLayout) findViewById(R.id.browser_webview);
					webviewl.setVisibility(View.VISIBLE);
					

					//mContent.setBackgroundColor(Color.argb(100, 0, 0, 0));
					//mContent.setVisibility(View.VISIBLE);
					

					mLink = link;

					ContentValues cv = new ContentValues();
					cv.put("status", 2);
					SqliteWrapper.update(this, getContentResolver(), DataProvider.CONTENT_URI, cv, "_id = " + id, null);
					
					//mContent.loadData("<html><body bgcolor=#000000 fontsize=16 text=#e0e0e0 link=#0066cc vlink=#9966cc><br><center><b><a href=\""+ link + "\">View Complete Article</a></b></center></body></html>", "text/html", "UTF-8");
					
				}
			}
			//mBrowser.addJavascriptInterface(new AndroidBridge(), "android");
			
		}
	}


	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onCreatePanelMenu(int featureId, Menu menu) {
		// TODO Auto-generated method stub
		menu.add(0, 401, 0, "Go To Article")
			.setIcon(android.R.drawable.ic_menu_compass);
        menu.add(0, 402, 0, "Forward")
			.setIcon(R.drawable.ic_menu_forward_mail);
		return super.onCreatePanelMenu(featureId, menu);
	}


	@Override
	public View onCreatePanelView(int featureId) {
		// TODO Auto-generated method stub
		return super.onCreatePanelView(featureId);
	}

	


	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		mLog.w(TAG,"onMenuItemSelected()");
		return super.onMenuItemSelected(featureId, item);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mLog.w(TAG,"onOptionsItemSelected()");
		
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		final String link = mLink; 
		final String title = mTitle.getText().toString(); 

    	
		switch(item.getItemId()){
		case 401:
			mContent.reload();
			//Intent d = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
			//startActivity(d);
			break;
		case 402:
			{
				Intent jump = new Intent(Intent.ACTION_SEND);
				jump.putExtra(Intent.EXTRA_TEXT, "I found something of interest.\n\n" + title + "\n" +link + "\n\n\n"); 
				jump.putExtra(Intent.EXTRA_SUBJECT, "FW: " + mLog.APP + " (" + title + ")");
				jump.setType("message/rfc822"); 
				startActivity(Intent.createChooser(jump, "Email"));
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		mLog.w(TAG,"onConfigurationChanged() ++++++++++++++++++++++++++++++++");
		super.onConfigurationChanged(newConfig);
	}


	@Override
	protected void onRestart() {
		mLog.w(TAG,"onRestart() ++++++++++++++++++++++++++++++++");
		super.onRestart();
	}


	@Override
	protected void onResume() {
		mLog.w(TAG,"onResume() ++++++++++++++++++++++++++++++++");
		super.onResume();
		//long id = mSharedPreferences.contains("id") ? mSharedPreferences.getLong("id",0) : 0;
		
		loadRelative();
		
		//mHandler.post(this);
		//mHandler.postDelayed(this, 10 * 1000);
		//mHandler.post(this);
		if( mThread.isAlive() ){
			mLog.i(TAG, "onResume() verified mThread is alive");
		}else if(mThread.getState() == State.RUNNABLE){
		//}else{
			mLog.w(TAG, "onResume() is starting mThread state("+mThread.getState().name()+")");
			try {
				mThread.start();
			} catch (IllegalThreadStateException e){
				mLog.w(TAG,"onResume() mThread already started.");
			}
		//}else{
			//mLog.w(TAG, "onResume() state("+mThread.getState().name()+")");
		}
		
		long id = mSharedPreferences.contains("id") ? mSharedPreferences.getLong("id",-1) : -1;
		loadRecord(id);
	}


	private void loadRelative() {
		mLog.w(TAG,"loadRelative() ++++++++++++++++++++++++++++++++");
		Date d = new Date();
		mToday = (d.getYear() + 1900) + "-";
		mToday += ( d.getMonth() < 10 ) ? "0" + (d.getMonth()+1) : ""+ (d.getMonth()+1);
		mToday += ( d.getDate() < 10 ) ? "-0" + d.getDate() : "-" + d.getDate();
		
		d.setTime(System.currentTimeMillis() - (24 * 60 * 60 * 1000) );
		mYesterday = (d.getYear() + 1900) + "-";
		mYesterday += ( d.getMonth() < 9 ) ? "0" + (d.getMonth()+1) : ""+ (d.getMonth()+1);
		mYesterday += ( d.getDate() < 10 ) ? "-0" + d.getDate() : "-" + d.getDate();
		
		Display display = getWindowManager().getDefaultDisplay();
		//boolean isPortrait = display.getWidth() < display.getHeight();
		//mWidth = isPortrait ? display.getWidth() : display.getHeight();
		//mHeight = isPortrait ? display.getHeight() : display.getWidth();

		mWidth = display.getWidth();
		mHeight = display.getHeight();
		
		mMidButtonWidth = (int) (mWidth * .23);
		mLeftButtonEnd = (mWidth/2) - (mMidButtonWidth/2);
		mRightButtonStart = mLeftButtonEnd + mMidButtonWidth;
		mLog.w(TAG,"Button Boundaries left("+mLeftButtonEnd+") midsize("+mMidButtonWidth+") right("+mRightButtonStart+") ");

	}


	private void clearView() {
		mLog.w(TAG,"clearView() ++++++++++++++++++++++++++++++++");
		
		ImageView iv = (ImageView) findViewById(R.id.browser_state);
		//iv.setVisibility(View.GONE);
		iv.setImageResource(R.drawable.dot);
		
		mImage.setImageResource(R.drawable.globe);
		
		RelativeLayout webviewl = (RelativeLayout) findViewById(R.id.browser_webview);
		webviewl.setVisibility(View.INVISIBLE);
		mContent.loadData("<html><body bgcolor=#000000></body></html>", "text/html", "UTF-8");
		
		//iv.setImageResource(R.drawable.docdot);
		
		
		//mContent = (WebView) this.findViewById(R.id.browser_viewer);
		//WebView content = (WebView) this.findViewById(R.id.browser_viewer);
		//mContent.post(new Runnable(){ public void run(){ mContent.setVisibility(View.GONE); } });
		
        
		//content.setVisibility(WebView.GONE);
		//content.loadData("<html><body bgcolor=#000000 fontsize=16 text=#e0e0e0 link=#0066cc vlink=#9966cc>Loading...</body></html>", "text/html", "UTF-8");
		//content.setBackgroundColor(Color.argb(100, 0, 0, 0));
		//mBackground = (ImageView) this.findViewById(R.id.browser_background);
		
		mType.setText("");
    	mTitle.setText("");
		mDate.setText("");
		mAuthor.setText("");
		
		//mContent.setVisibility(WebView.GONE);
		//mContent.setVisibility(View.VISIBLE);
		//mContent.clearView();
	}


	
	@Override
	protected void onStart() {
		mLog.w(TAG,"onStart() ++++++++++++++++++++++++++++++++");
		super.onStart();
		
		//mHandler.post(this);
		//mHandler.postDelayed(this, 10 * 1000);
		//mHandler.post(this);
		if( mThread.isAlive() ){
			mLog.i(TAG, "onStart() verified mThread is alive");
		}else if(mThread.getState() == State.RUNNABLE){
		//}else{
			mLog.w(TAG, "onStart() is starting mThread state("+mThread.getState().name()+")");
			try {
				mThread.start();
			} catch (IllegalThreadStateException e){
				mLog.w(TAG,"onStart() mThread already started.");
			}
		//}else{
			//mLog.w(TAG, "onStart() state("+mThread.getState().name()+")");
		}
		
		//mCurrentID = mSharedPreferences.contains("id") ? mSharedPreferences.getLong("id",-1) : -1;
		//loadRecord(mCurrentID);
	}


	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
		//mMessageId = savedInstanceState.getLong("messageid");
		
		
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		mLog.w(TAG,"onSaveInstanceState() ++++++++++++++++++++++++++++++++");
		//if( mContent != null ){
			//mContent.loadData("<html><body bgcolor=#000000 fontsize=16 text=#e0e0e0 link=#0066cc vlink=#9966cc></body></html>", "text/html", "UTF-8");
		//}
	}


	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		mLog.w(TAG,"onWindowFocusChanged() focus("+hasFocus+") ++++++++++++++++++++++++++++++++");
		//mLog.w(TAG, "browserWindow focus changed to("+hasFocus+")");
		//if( !hasFocus ){
			//if( mContent != null ){
				//mContent.loadData("<html><body bgcolor=#000000 fontsize=16 text=#e0e0e0 link=#0066cc vlink=#9966cc></body></html>", "text/html", "UTF-8");
			//}
		//}
		super.onWindowFocusChanged(hasFocus);
	}

	private Handler mHandler;
	private Thread mThread;
	
	public void run() {
		mLog.i(TAG,"run() +++++++++++++++++++++++++++");
		mLog.refreshConsoleTouch(mConsoleTouch, mHandler);
	}    
}

