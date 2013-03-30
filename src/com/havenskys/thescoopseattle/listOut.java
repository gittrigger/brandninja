package com.havenskys.thescoopseattle;

import java.lang.Thread.State;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class listOut extends ListActivity implements Runnable {

	private static String TAG = "ListOut";
	private static int LAYOUT = R.layout.listout;
	private static int TOUCHPAD = R.id.listout_footer;
	private static int CONSOLETEXT = R.id.listout_text;
	

	//private Bundle mIntentExtras;
	private SharedPreferences mSharedPreferences;
	private Editor mPreferencesEditor;
	private Custom mLog;
	private ListView mListView;
	private Drawable mItemClickDrawable;
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		mLog.e(TAG, "onListItemClick() position("+position+") id("+id+")");
		if( position == 0 ){
			Intent goFish = new Intent(mContext, Customize.class);
			goFish.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(goFish);
		}else{
			mPreferencesEditor.putInt("position_"+TAG, position);
			mPreferencesEditor.putLong("id", id); mPreferencesEditor.commit();
			setTitle("" + id);
		}
		//mListView.setSelector(mItemClickDrawable);
	}

	
	private long mLastFocus = -1;
	private RelativeLayout mFocusView;
	//private TextView mTitle, mSummary, mLastFocusView;
	private long mLastFocusStart = 0;
	private ContentResolver mResolver;
	private Context mContext;
	private int mHeaderSeenCount = 0;
	private boolean mLandscape = false;
	
	
	private Handler mUpdateSeenHandler = new Handler() {
	    public void handleMessage(Message msg) {
	    	Bundle b = msg.getData();
			final long id = b.containsKey("id") ? b.getLong("id",-1) : -1;
			
			Cursor c = null;
			long seen = -1;
			c = SqliteWrapper.query(mContext, mResolver, DataProvider.CONTENT_URI, new String[] {"seen"} , "_id = " + id, null, null);
			// and (read == 0 or read > "+(System.currentTimeMillis() - (5 * 60 * 1000) )+") and ( seen == 0 or seen > "+(System.currentTimeMillis() - (5 * 60 * 1000) )+" )"
			if( c != null ){ if( c.moveToFirst() ){ seen = c.getLong(0); } c.close(); }
			
			mLog.w(TAG, "mUpdateSeenHandler id("+id+") seen("+seen+") selected("+getListView().getSelectedItemId()+")");
			
			if( id > -1 && seen == 0 ){
				
				Thread t = new Thread(){
					public void run(){
				
						SystemClock.sleep(1880);
						
						long cur = getListView().getSelectedItemId();
						mLog.w(TAG, "mUpdateSeenHandler id("+id+") selected("+getListView().getSelectedItemId()+") current("+cur+") after 1.88 seconds");
						
						if( id == cur ){
							Cursor c = null;
							long seen = -1;
							c = SqliteWrapper.query(mContext, mResolver, DataProvider.CONTENT_URI, new String[] {"seen"} , "_id = " + id, null, null);
							// and (read == 0 or read > "+(System.currentTimeMillis() - (5 * 60 * 1000) )+") and ( seen == 0 or seen > "+(System.currentTimeMillis() - (5 * 60 * 1000) )+" )"
							if( c != null ){ if( c.moveToFirst() ){ seen = c.getLong(0); } c.close(); }
							if( seen == 0 ){
								ContentValues setValues = new ContentValues();
								setValues.put(Custom.SEEN, System.currentTimeMillis() );
								SqliteWrapper.update(mContext, mResolver, DataProvider.CONTENT_URI, setValues, "_id = " + id, null);
								
								//for(int i = 0; i < 8; i++){ SystemClock.sleep(1000); }
								SystemClock.sleep(30 * 1000 - 1880);
								if( id == getListView().getSelectedItemId() ){
									mLog.w(TAG, "mUpdateSeenHandler id("+id+") selected("+getListView().getSelectedItemId()+")");
									setValues.put(Custom.SEEN, 0 );
									SqliteWrapper.update(mContext, mResolver, DataProvider.CONTENT_URI, setValues, "_id = " + id, null);
								}
							}else{
								mLog.w(TAG, "mUpdateSeenHandler seen("+seen+") id("+id+") selected("+getListView().getSelectedItemId()+")");
							}
						}
					}
				};
				t.start();
				
			}
			
			/*
			String where = "";
			long id1 = b.containsKey("id1") ? b.getLong("id1",-1) : -1;
			long id2 = b.containsKey("id2") ? b.getLong("id2",-1) : -1;
			if( id > -1 ){ where += "_id = " + id; }
			if( id1 > -1 ){ if(where.length() > 0){where += " or ";} where += "_id = " + id1; }
			if( id2 > -1 ){ if(where.length() > 0){where += " or ";} where += "_id = " + id2; }
			if( where.length() > 0 ){
				mLog.i(TAG, "mUpdateSeenHandler() 56 id("+id+") where("+where+")");
				SqliteWrapper.update(mContext, mResolver, DataProvider.CONTENT_URI, setValues, where, null);
			}else{
				mLog.i(TAG, "mUpdateSeenHandler() 56 id("+id+") but the where was empty");
			}//*/
			
			
			
			
	    }
	};
	
	private Handler mUpdateViewHandler = new Handler() {
	    public void handleMessage(Message msg) {
	    	Bundle b = msg.getData();
	    	long id = b.containsKey("id") ? b.getLong("id",-1) : -1;
	    	//SystemClock.sleep(33);
	    	mLog.i(TAG, "mUpdateViewHandler() 56 id("+id+")");
	    	
	    	
			ContentValues setValues = new ContentValues();
			setValues.put(Custom.SEEN, System.currentTimeMillis() );
			SqliteWrapper.update(mContext, mResolver, DataProvider.CONTENT_URI, setValues, "_id = " + id, null);
	    }
	};
	
	/*
	private Handler mUpdateLastSelection = new Handler() {
	    public void handleMessage(Message msg) {
	    	long id = mListView.getSelectedItemPosition();
	    	mFocusView = (RelativeLayout) mListView.getFocusedChild();
	    	if( mFocusView != null ){
				mTitle = (TextView) mFocusView.getChildAt(4);
				mTitle.setTextColor(Color.argb(255, 250, 250, 250));
	    	}
	    }
	};//*/
	
	/*
	private Handler mUpdateSelection = new Handler() {
	    public void handleMessage(Message msg) {
	    	Bundle b = new Bundle();
	    	b.putLong("id", mLastFocus);
	    	Message m = new Message();
			m.setData(b);
			mUpdateLastSelection.sendMessage(m);
	    	long id = mListView.getSelectedItemPosition();
	    	mLastFocus = id;
	    	mFocusView = (RelativeLayout) mListView.getFocusedChild();
	    	if( mFocusView != null ){
				mTitle = (TextView) mFocusView.getChildAt(4);
				mTitle.setTextColor(Color.argb(255, 250, 150, 25));
	    	}
	    }
	};//*/
	
	//private Message mMsgSwap, mMsgSwap2;
	//private Bundle mBundleSwap, mBundleSwap2;
	
	private String mYesterday, mToday;
	private TextView seenView, readView, dateView, longname, rowidView, seenViewR, mConsoleText;
	private ImageView stateImage, imageIcon, mConsoleTouch;
	private String longnameText = "";
	private TextView listTitle;
	private String listTitleText, listDate;
	private RelativeLayout childView, childViewR;
	private LinearLayout edgeChildView;
	private long mFocusId, rowid;
	private int mFocusPosition = -1;
	private int mScrollState = 0;
	private long[] mVisibleList;
	private long mVisibleTimer;
	private Drawable mMenuSave, mMenuView;
	private long mActionTouchEventTime = 0;
	private int mWidth, mHeight;
	private int mRowStart, mRowCount;
	private int mMidButtonWidth, mLeftButtonEnd, mRightButtonStart;
	private Drawable[] mIconList;
	private Drawable mBackground;
	private Handler mBackgroundChange;
	private long mLastHeaderUpdate;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  setContentView(LAYOUT);
	  
	  
	  mLog = new Custom(this, TAG + " onCreate() 115");
	  mLog.i(TAG,"onCreate() ++++++++++++++++++++");
	  
	  mResolver = this.getContentResolver();
	  mContext = this;
	  
	  
	  mVisibleList = new long[] {-1, -1, -1};
	  mSharedPreferences = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
	  mPreferencesEditor = mSharedPreferences.edit();
	  mLog.setSharedPreferences(mSharedPreferences, mPreferencesEditor);

	  mIconList = new Drawable[mLog.dataFeed.length];
	  //mListView = getListView();
	  mListView = (ListView) findViewById(android.R.id.list);
	  
	  
	  
	  /*/
	  LinearLayout ll = new LinearLayout(this);
	  ll.setOrientation(LinearLayout.VERTICAL);
	  ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 100));
	  ll.setBackgroundColor(Color.RED);
	  //*
	  TextView textv = (TextView) findViewById(R.id.listview_text);
	  TextView tv = new TextView(this);
	  tv.setLayoutParams(textv.getLayoutParams());
	  tv.setVisibility(View.VISIBLE);
	  tv.setText("Header Line");
	  mListView.addHeaderView(tv);
	  //*/
	  
	  //getListView().addHeaderView(ll);
	  //Button b = new Button(this);
	  //b.setText("button");
	  //mListView.addHeaderView(b);
	  


	  //mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
      
      int total = mSharedPreferences.contains("total") ? mSharedPreferences.getInt("total",0) : 0;
    
      mConsoleText = (TextView) findViewById(CONSOLETEXT);
      mConsoleTouch = (ImageView) findViewById(TOUCHPAD);
      mConsoleTouch.setFocusable(false);
      mConsoleTouch.setFocusableInTouchMode(false);
      
      
      mBackground = this.getResources().getDrawable(Custom.BACKGROUND);
      mBackgroundChange = new Handler(){
    	  public void handleMessage(Message msg) { 
	  			if( getListView().getBackground() == null && mScrollState == 0 ){
	  				mLog.w(TAG, "mBackgroundChange Thread, setting background mScrollState("+mScrollState+")");
	  				//getListView().setBackgroundResource(R.drawable.starbright);
	  				
	  				long lastactive =  mSharedPreferences.getLong("lastfeedactive", 1);
	  				if( lastactive > 0 ){
	  					
	  				}else{
	  					getListView().setBackgroundDrawable(mBackground);
	  				}
	  				
	  				
	  				//mBackground = null;
	  			//}else if(b == null && mScrollState > 0){
	  				//mLog.w(TAG, "mBackgroundChange Thread, checking again in a while mScrollState("+mScrollState+")");
	  				//mBackgroundChange.sendEmptyMessageDelayed(16, 1880);
	  			}else{
	  				mLog.w(TAG, "mBackgroundChange Thread, doing nothing mScrollState("+mScrollState+")");
	  				//getListView().setBackgroundDrawable(null);
	  			}
        	}
        };
      
      
      mHandler = new Handler();
      
      mThread = new Thread(null, this, "touchconsole_runnable");
      mThread.setPriority(Thread.MAX_PRIORITY);
      mThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler(){

		public void uncaughtException(Thread thread, Throwable ex) {
			// TODO Auto-generated method stub
			mLog.e(TAG, "mThread uncaughtException() " + ex.getMessage() );
			//mThread.start();
		}
    	  
      });
	  mThread.start();
	  
      
      
	  //LinearLayout spare = (LinearLayout) findViewById(R.id.listview_spare);
      //LinearLayout spareD = spare;
      //TextView nt = (TextView) spareD.getChildAt(0);
	  //nt.setText("You currently have " + total + " records in your database.");
	  //spareD.setVisibility(View.VISIBLE);
	  //getListView().addHeaderView((View) nt);
	  
	  //mItemClickDrawable = getResources().getDrawable(R.drawable.listgray);
	  
	  

	
	  
	  //mMsgSwap = new Message();
	  //mBundleSwap = new Bundle();
	  
	  //mMsgSwap2 = new Message();
	  //mBundleSwap2 = new Bundle();
	  
	  //mIntentExtras = getIntent().getExtras();
	  //long id = mIntentExtras != null ? mIntentExtras.getLong("id") : 0;

	  //mPreferencesEditor.putLong("id", id); mPreferencesEditor.commit();
      

	
	  mConsoleText.setText("");
	  //final int totalF = total;
	  //mConsoleText.postDelayed(new Runnable(){ public void run() { mConsoleText.setText(totalF + " Records"); } }, 10 * 1000);
	  
	  //if(total > 0 ){
		  //mConsoleText.postDelayed(new Runnable(){ public void run() { mConsoleText.setText("Touch Here"); } }, 5 * 1000);
		  //mConsoleText.postDelayed(new Runnable(){ public void run() { mConsoleText.setText(""); } }, 10 * 1000);
	  //}
	  
	  
	  /*
	  mSharedPreferences.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener(){

		public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key ) {
			if( key == "total" ){
				final int total = sharedPreferences.getInt("total",0);
				//mConsoleText.postDelayed(new Runnable(){ public void run() { mConsoleText.setText(total + " Records"); } }, 5 * 1000);
				mLog.w(TAG,"Updating console text with updated record count("+total+")");
				TextView consoleText = (TextView) findViewById(CONSOLETEXT);
				consoleText.post(new Runnable(){ public void run() { mConsoleText.setText(total + " Records"); } });
			}
		}
		  
	  });
	  //*/
	  
	  
	  mConsoleTouch.setOnTouchListener(new OnTouchListener(){

		public boolean onTouch(View v, MotionEvent event) {

			if(event.getAction() == MotionEvent.ACTION_UP){
				mActionTouchEventTime = 0;
				return true;
			}
			if(event.getAction() == MotionEvent.ACTION_CANCEL){
				mActionTouchEventTime = 0;
				return true;
			}
			
			if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE){
				
				if( (System.currentTimeMillis() - mActionTouchEventTime) < (1880*2) ){ // Future configurable, scroll rate
					return true;
				}
				mActionTouchEventTime = System.currentTimeMillis();
				
				
				
				int pos = getListView().getSelectedItemPosition();
				int firstVis = getListView().getFirstVisiblePosition();
				int top = getListView().getFirstVisiblePosition();
				int cnt = getListView().getCount();
				String direction = "way";
				int origpos = pos;
				
				/*/
				if( pos == -1 ){
					getListView().requestFocusFromTouch();
					pos = getListView().getSelectedItemPosition();
					if( pos > -1 ){
						mLog.w(TAG,"Console Touch acquired position after requesting touch");
					}
				}//*/
				
				//mLog.w(TAG,"Console Touch");
				
				if( pos == -1 && mFocusPosition > -1 ){
					//mLog.w(TAG,"Console Touch changing pos("+pos+") to known mFocusPosition("+mFocusPosition+")");
					pos = mFocusPosition;
				}
				
				if( event.getX() > mRightButtonStart ){
					direction = "down";// POS UP
					if(pos == -1){
						//mLog.w(TAG,"Console Touch changing pos("+pos+") to firstVis("+firstVis+")");
						pos = firstVis;// yes, plus 2 in total
					}else{
						//mLog.w(TAG,"Console Touch changing pos("+pos+") to upper("+(pos + 1)+")");
						pos += 1;
					}
					if( pos >= cnt ){
						//mLog.w(TAG,"Console Touch changing pos("+pos+") to cnt("+(cnt-1)+")");
						pos = cnt-1;
					}
				}else if(event.getX() < mLeftButtonEnd){
					direction = "up";// POS DOWN
					if(pos == -1){
						//mLog.w(TAG,"Console Touch changing pos("+pos+") to firstVis("+firstVis+")");
						pos = firstVis;// yes, odd, nullified
					}else{
						//mLog.w(TAG,"Console Touch changing pos("+pos+") to lower("+(pos - 1)+")");
						pos -= 1;
					}
					if( pos < 0 ){
						//mLog.w(TAG,"Console Touch droping focus");
						getListView().clearFocus();
						//getListView().requestFocus(View.FOCUS_UP);
						setTitle("");
						return true;
					}
				}else if(event.getX() > mLeftButtonEnd && event.getX() < mRightButtonStart ){
					direction = "action";
					getListView().setSelection(pos);
					mPreferencesEditor.putInt("position_"+TAG, pos);
					mPreferencesEditor.putLong("id", mFocusId);
					mPreferencesEditor.commit();
					setTitle("" + mFocusId);
					return true;
				}
				
				getListView().requestFocusFromTouch();
				getListView().setSelected(true);
				
				int fromTop = -1;
				int fromTopHeight = -1;
				if( pos > -1 ){
					try {
						View cv = getListView().getChildAt(pos);
						fromTop = cv.getTop();
						fromTopHeight = cv.getHeight();
						if( (fromTopHeight + fromTop) < (fromTopHeight * .6) ){
							mLog.w(TAG, "ConsoleTouch top entry is hidden");
							if( direction == "up" ){
								if( pos > 0 ){
									pos --;
								}
							}else{
								if( pos < cnt-1 ){
									pos ++;
								}
							}
						}
					} catch (NullPointerException e){
						mLog.e(TAG, "NullPointerException fromTop("+fromTop+") fromTopHeight("+fromTopHeight+") " + e.getLocalizedMessage() );
					}
				}
				
				//mLog.w(TAG, "ConsoleTouch original("+origpos+") pos("+pos+") Y("+fromTop+") H("+fromTopHeight+") vis("+(fromTopHeight + fromTop)+") limit("+(fromTopHeight * .6)+") top("+top+") direction("+direction+") firstVis("+firstVis+") cnt("+cnt+")");
				
				//getListView().getChildAt(pos).requestFocusFromTouch();
				
				
				//getListView().setSelection(pos);
				if( fromTop < -1 ){
					getListView().setSelection(pos);
				} else {
					getListView().setSelectionFromTop(pos, 50);
				}
				
				rowid = getListView().getItemIdAtPosition(pos);
				if( rowid > -1 ){
					Bundle b = new Bundle();
			    	b.putLong("id", rowid);
			    	Message m = new Message();
					m.setData(b);
					mUpdateSeenHandler.sendMessageDelayed(m,100);
				}
				
				
				//mLog.w(TAG, "onTouch() pos("+pos+") top("+top+") cnt("+cnt+") X("+event.getX()+") Y("+event.getY()+") ");

			}
			return true;
			
		}
		  
	  });
	  //*
	  
	  mMenuSave = getResources().getDrawable(android.R.drawable.ic_menu_save);
	  //mMenuView = getResources().getDrawable(R.drawable.checkbox_on_background);
	  mMenuView = getResources().getDrawable(R.drawable.btn_check);
	  
	  
	  	mListView.setOnFocusChangeListener(new OnFocusChangeListener(){

			public void onFocusChange(View child, boolean hasFocus) {
				//if( true ){return;}
				//mLog.i(TAG, "onFocusChange() tag("+ mListView.getItemIdAtPosition(child.getId()) +") getId("+child.getId()+") hasFocus("+hasFocus+") ");
				
				//mFocusView = (RelativeLayout) child;
				//mTitle = (TextView) mFocusView.getChildAt(4);
				if( hasFocus ){
					if( getListView().getCount() > 0 ){
						updateHeaderView(TAG + " onFocusChange() 525", getListView().getChildAt(0) );
					}
					//mLastFocusView.setTextColor(Color.argb(255, 250, 150, 25));
					//View c = child;
					//child.requestFocusFromTouch();
					//mListView.setSelected(true);
					//mListView.requestFocusFromTouch();
					//RelativeLayout r = (RelativeLayout) child.get;
					//mTitle = (TextView) r.getChildAt(4);
					//mTitle.setTextColor(Color.argb(255, 250, 150, 25));
					//mLastFocusView = mTitle;
					//if( mTitle != null ){
						//mTitle.setTextColor(Color.argb(255, 250, 150, 25));
					//}
					
					//RelativeLayout r = (RelativeLayout) getListView().findFocus();
					//mTitle = (TextView) r.getChildAt(4);
					//mTitle.setTextColor(Color.argb(255, 250, 150, 25));
					//mLastFocusView = mTitle;
					
					
					//if( getSelectedItemId() == mLastFocus ){
						//mTitle.setTextColor(Color.argb(255, 250, 150, 25));
					//}
				}else{
					mActionEventTime = 0;
					
					//if( mTitle != null ){
						//mTitle = null;
						//mLastFocus = mLastFocus;
						//mTitle.setTextColor(Color.argb(255, 250, 250, 250));
					//}
					
						
				}
				
				
				//child.setTag("set");
				
				/*
				child.setOnFocusChangeListener(new OnFocusChangeListener(){
					public void onFocusChange(View v, boolean hasFocus) {
						if( !hasFocus ){
							RelativeLayout rl = (RelativeLayout) v;
							TextView tView = (TextView) rl.getChildAt(4);
							tView.setTextColor(Color.argb(255, 250, 250, 250));
						}
					}
				});/*/
				
				//tView.setTextColor(Color.argb(255, 250, 150, 25));
					
				//mBundleSwap.putLong("id", mLastFocus);
				//mMsgSwap.setData(mBundleSwap);
				//mUpdateSelection.sendMessage(mMsgSwap);
				
				/*
				if( !hasFocus && mLastFocusView != null ){
					if( !mLastFocusView.hasFocus() ){
						mTitle = (TextView) mLastFocusView.getChildAt(4);
						mTitle.setTextColor(Color.argb(200, 250, 250, 250));
					}
					//mSummary = (TextView) mLastFocusView.getChildAt(5);
					//mSummary.setTextColor(Color.argb(255, 250, 250, 250));
					
					//mSummary = (TextView) mLastFocusView.getChildAt(1);
					//mSummary.setTextColor(Color.argb(255, 250, 250, 250));
					//mSummary = (TextView) mLastFocusView.getChildAt(2);
					//mSummary.setTextColor(Color.argb(255, 250, 250, 250));
					//mSummary = (TextView) mLastFocusView.getChildAt(3);
					//mSummary.setTextColor(Color.argb(255, 250, 250, 250));
				}	
				if( hasFocus && mLastFocusView != null ){
					if( mLastFocusView.hasFocus() ){
						mTitle = (TextView) mLastFocusView.getChildAt(4);
						mTitle.setTextColor(Color.argb(200, 250, 150, 25));
					}
				}//*/
			}
	  		
	  	});
	  	
	  	
	  	mListView.setOnScrollListener(new OnScrollListener(){

			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if( true ){return;}
				mLog.i(TAG, "onScrollStateChanged() scroll("+mScrollState+") topid("+getListView().getItemIdAtPosition(firstVisibleItem)+") firstVisibleItem("+firstVisibleItem+") visibleItemCount("+visibleItemCount+") totalItemCount("+totalItemCount+")");

				if( mScrollState > 0 ){
					//getListView().setSelected(false);
					//mFocusPosition = -1;
					//mFocusId = -1;
				}
				/*
				long diff = (System.currentTimeMillis() - mVisibleTimer);
				if( diff >= 1880 && diff < 10000 ){
					mLog.w(TAG,"onScrollStateChanged() seen top 0("+mVisibleList[0]+") 1("+mVisibleList[1]+") 2("+mVisibleList[2]+")");
					Bundle b = new Bundle();
			    	b.putLong("id", mVisibleList[0]);
			    	b.putLong("id1", mVisibleList[1]);
			    	b.putLong("id2", mVisibleList[2]);
			    	Message m = new Message();
					m.setData(b);
					mUpdateSeenHandler.sendMessageDelayed(m,100);
				}
				
				if( mScrollState > 0 ){
					//rowidView = (TextView) childView.getChildAt(9);
					//rowid = Long.parseLong(rowidView.getText().toString());
					//mLog.w(TAG,"onChildViewAdded() seen rowid("+rowid+")");
					if( visibleItemCount > 3 ){
						mVisibleList[2] = getListView().getItemIdAtPosition(firstVisibleItem);
						mVisibleList[1] = getListView().getItemIdAtPosition(firstVisibleItem-1);
						mVisibleList[0] = getListView().getItemIdAtPosition(firstVisibleItem-2);
					}
				}else{
					mVisibleList[2] = -1;
				}
				//*/
				
				
				/*
				if( mScrollState > 0 ){
					if(mVisibleList[0] > -1 || mVisibleList[1] > -1 || mVisibleList[2] > -1){
						long diff = (System.currentTimeMillis() - mVisibleTimer);
						if( diff >= 1880 && diff < 10000 ){
							mLog.i(TAG, "onScrollStateChanged() setting seen("+mVisibleList[0]+","+mVisibleList[1]+") ago("+(diff/100)+" deciseconds) ");
							//if(mVisibleList[0] > -1)
							{
								Bundle b = new Bundle();
						    	b.putLong("id", mVisibleList[0]);
						    	b.putLong("id1", mVisibleList[1]);
						    	b.putLong("id2", mVisibleList[2]);
						    	Message m = new Message();
								m.setData(b);
								mUpdateSeenHandler.sendMessageDelayed(m,300);
							}
						}
					}
				}//*/
				
				
				//if( mTitle != null ){
					//mTitle.setTextColor(Color.argb(255, 250, 250, 250));
					//mLastFocusView = null;
				//}
			}

			public void onScrollStateChanged(AbsListView view, int scrollState) {
				//if( true ){return;}
				//mLog.i(TAG, "onScrollStateChanged() state("+scrollState+") ");
				mScrollState = scrollState;
				
				
				if( scrollState > 0 && getListView().getBackground() != null ){
					getListView().setBackgroundDrawable(null);
					getListView().setBackgroundColor(Color.BLACK);
					mLog.i(TAG, "onScrollStateChanged() state("+scrollState+") clearing background");
				}else if( scrollState == 0 ){
					if( getListView().getBackground() == null ){
						mBackgroundChange.sendEmptyMessageDelayed(17, 1880 );
					}
					
					//mLog.i(TAG, "onScrollStateChanged() state("+scrollState+") setting background");
				}
			}
	  		
	  	});
	  
	  	mListView.setOnItemSelectedListener(new OnItemSelectedListener(){

			public void onItemSelected(AdapterView<?> arg0, View child, int position, long itemid) {
				//if( true ){return;}
				//mLog.i(TAG, "onItemSelected() position("+position+") itemid("+itemid+") ");
				if( child != null ){ child.requestFocusFromTouch(); }
				mFocusPosition = position;
				mFocusId = itemid;
				if( position == 0 ){
					updateHeaderView(TAG + " onItemSelected() 702", child);
				}
				
				//if( mTitle != null ){
					//mTitle.setTextColor(Color.argb(255, 250, 250, 250));
				//}
				
				//mFocusView = (RelativeLayout) child;
				//mTitle = (TextView) mFocusView.getChildAt(4);
				//mTitle.setTextColor(Color.argb(255, 250, 150, 25));
				
				//mBundleSwap.putLong("id", mLastFocus);
				//mMsgSwap.setData(mBundleSwap);
				//mUpdateSelection.sendMessage(mMsgSwap);
				
				
				
				//mListView.setSelection(position);
			
				/*
				if( mLastFocusView != null ){
					if( !mLastFocusView.hasFocus() ){
						mTitle = (TextView) mLastFocusView.getChildAt(4);
						mTitle.setTextColor(Color.argb(200, 250, 250, 250));
					}
					//mSummary = (TextView) mLastFocusView.getChildAt(5);
					//mSummary.setTextColor(Color.argb(255, 250, 250, 250));
					
					//mSummary = (TextView) mLastFocusView.getChildAt(1);
					//mSummary.setTextColor(Color.argb(255, 250, 250, 250));
					//mSummary = (TextView) mLastFocusView.getChildAt(2);
					//mSummary.setTextColor(Color.argb(255, 250, 250, 250));
					//mSummary = (TextView) mLastFocusView.getChildAt(3);
					//mSummary.setTextColor(Color.argb(255, 250, 250, 250));
				}
				
				{
					if( mListView.getSelectedItemPosition() == position ){
						mFocusView = (RelativeLayout) child;
						mTitle = (TextView) mFocusView.getChildAt(4);
						mTitle.setTextColor(Color.argb(255, 250, 150, 25));
					}
					//ImageView image = (ImageView) childView.getChildAt(0);
					
					//mSummary = (TextView) mFocusView.getChildAt(5);
					
					//mSummary.setTextColor(Color.argb(255, 255, 255, 255));
					
					//mTitle = (TextView) mFocusView.getChildAt(1);
					//mTitle.setTextColor(Color.argb(255, 255, 255, 255));
					//mTitle = (TextView) mFocusView.getChildAt(2);
					//mTitle.setTextColor(Color.argb(255, 255, 255, 255));
					//mTitle = (TextView) mFocusView.getChildAt(3);
					//mTitle.setTextColor(Color.argb(255, 255, 255, 255));
				}
				
				//*/
				
				if( mLastFocus != itemid ){
					//int cnt = getListView().getCount();
					//if( position == (cnt -1) ){
						//mRowStart = 1;
						//mRowCount = mRowCount + 10;
						//mLog.loadlist(listView.this,mRowStart,mRowCount);
						//child.setPadding(0, 0, 0, 80);
					//}
				}
				
				mLastFocus = itemid;
				mLastFocusStart = System.currentTimeMillis();
				//mLastFocusView = mTitle;
				
				//if( mListView.getSelectedItemPosition() == position ){
					
				//}
				
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				//mLog.i(TAG, "onItemSelected() nothing selected ");
				mLastFocus = -1;
				mLastFocusStart = System.currentTimeMillis();
				
				//getListView().setSelected(false);
				mFocusPosition = -1;
				mFocusId = -1;
			}
	  		
	  	});
	  	
	  	
		mListView.setOnHierarchyChangeListener(new OnHierarchyChangeListener(){
	
			public void onChildViewAdded(View parent, View child) {
				
				//Bundle b = new Bundle();
		    	//b.putLong("id", child.getId() );
		    	//Message m = new Message();
				//m.setData(b);
				//mUpdateViewHandler.sendMessage(m);
				
				
				int pos = getListView().getPositionForView(child);
				int inx = getListView().indexOfChild(child);
				//mLog.e(TAG,"onChildViewAdded position("+pos+") index("+inx+")");
				//*/
				//String tag = child.getTag().toString();
				//if( tag == null ){
					//mLog.e(TAG,"tag is empty");
				//}else{
					//mLog.e(TAG, "tag contents("+tag+")");
					if( inx == 0 && pos < 2 ){
						updateHeaderView(TAG + " onChildViewAdded() 811",child);
					}
					
					if( pos == getListView().getCount() - 1 ){
						//mLog.e(TAG, "onChildViewAdded Located Footer");
						return;
					}
				//}//*/
				
					
				//*
				try {
					childView = (RelativeLayout) child;
				} catch (ClassCastException e){
					mLog.w(TAG, "Failed to create childView, probably a header or footer.");
					//e.printStackTrace();
					return;
				}
				longname = (TextView) childView.getChildAt(1);
				
				//*
				//mLog.w(TAG, "onChildViewAdded() 73");
				if( longname.length() < 3 ){
					
					//mLog.w(TAG, "onChildViewAdded() isShown(" + child.isShown() + ") bottom("+child.getBottom()+") ");
					mVisibleTimer = System.currentTimeMillis();
					
					int feedid = 0;
					int iconref = 0;
					//mLog.w(TAG, "onChildViewAdded() 77");
					
					try {
						feedid = Integer.parseInt(longname.getText().toString());
						if( feedid >= mLog.dataFeed.length ){
							iconref = R.drawable.globeicon;
						}
						if( iconref == 0 ){
							iconref = Integer.parseInt(mLog.dataFeed[feedid][1]);
						}
					} catch (NumberFormatException e){
						mLog.e(TAG, "onChildViewAdded() Parsing the feedid and iconref from longname.getText() failed " + e.getMessage());
						return;
					}
					
					
					
					longnameText = "";
					longnameText = mLog.dataFeed[feedid][0];
					
					
					listTitle = (TextView) childView.getChildAt(4);
					listTitleText = listTitle.getText().toString();
					//listTitleText = listTitleText.replaceAll("&#34;", "\"");
					listTitle.setText( listTitleText );
					
					//mLog.w(TAG, "onChildViewAdded() 82 feedid("+feedid+") iconref("+iconref+") top("+child.getTop()+") bottom("+child.getBottom()+") Y("+child.getScrollY()+") height("+child.getHeight()+") scroll("+mScrollState+") longname("+longname.getText().toString()+") title("+title.getText().toString()+") setting focusListener");
					
					//longname.setText(longnameText);
					
					
					imageIcon = (ImageView) childView.getChildAt(0);
					//final int iconref2 = iconref;
					//final String longname2 = longnameText;
					
					//Thread t = new Thread(){
						//public void run(){
							//ImageView imageIcon = (ImageView) childView.getChildAt(0);
							//imageIcon.setImageResource(iconref);
							if( mIconList[feedid] == null ){
								if( iconref > 0 ){
									mIconList[feedid] = getResources().getDrawable(iconref);
								}
							}
							if( mIconList[feedid] != null ){
								imageIcon.setImageDrawable(mIconList[feedid]);
							}
							//TextView longname = (TextView) childView.getChildAt(1);
							longname.setText(longnameText);
						//}
					//};
					//t.start();
					
					
					
					
					/*
					childView.setOnFocusChangeListener(new OnFocusChangeListener(){
						public void onFocusChange(View v, boolean hasFocus) {
							RelativeLayout rl = (RelativeLayout) v;
							TextView tView = (TextView) rl.getChildAt(4);
							if( !hasFocus ){
								tView.setTextColor(Color.argb(255, 250, 250, 250));
							//}else{
								//tView.setTextColor(Color.argb(255, 250, 150, 25));
							}
						}
					});//*/
					
					//mLog.w(TAG, "onChildViewAdded() 82 feedid("+feedid+") iconref("+iconref+") DONE");
					
					//image.setVisibility(View.VISIBLE);
					//longname.setTextColor(Color.argb(255, 250, 200, 150));
					
					dateView = (TextView) childView.getChildAt(2);
					
					//mLog.w(TAG,"onChildViewAdded() today("+mToday+") yesterday("+mYesterday+")");
					
					//if( Date.getTag().toString() != "set" ){
					listDate = dateView.getText().toString();
					listDate = listDate.replaceFirst(mToday, "Today").replaceFirst(mYesterday, "Yesterday").replaceFirst(":..$", "");
					dateView.setText(listDate);
						//Date.setTag("set");
					//}
					
						
						
						
						
					//mVisibleList[0] = mVisibleList[1];
					//mVisibleList[1] = mVisibleList[2];
					
					//mVisibleTimer = System.currentTimeMillis();
					
					seenView = (TextView) childView.getChildAt(6);
					readView = (TextView) childView.getChildAt(7);
					stateImage = (ImageView) childView.getChildAt(8);
					if( readView.length() > 1 ){
						stateImage.setImageDrawable(mMenuSave);
					}else if( seenView.length() > 1 ){
						stateImage.setImageDrawable(mMenuView);
						mVisibleList[2] = -1;
					}else{
						//stateImage.setImageResource(android.R.drawable.);
						stateImage.setImageResource(R.drawable.docdot);
					}
					//seenView.setText(""+System.currentTimeMillis());
					
					//if( mTitle == null ){
					
					//if( mFocusId == mLastFocus ){
						//mTitle = (TextView) childView.getChildAt(4);
						//mTitle.setTextColor(Color.argb(255, 250, 150, 25));
					//}else{
					//*
						
					
						
						//if( rowid == mFocusId){//rowid == mLastFocus || 
							//mTitle = (TextView) childView.getChildAt(4);
							//mTitle.setTextColor(Color.argb(255, 250, 150, 25));
						//}
						//*/
					//}
					//}
					
						
				}//*/
				 else{
					//longname.setTextColor(Color.argb(255, 250, 250, 250));
				}
				
			}
	
			public void onChildViewRemoved(View parent, View child) {
				//mLog.w(TAG, "onChildViewRemoved() +++++++++++++++++++++++++++++++");
				//childView = (RelativeLayout) child;
				//seenView = (TextView) childView.getChildAt(6);
				
				
				
			}
			
		});
		
		
		

		//mHandler.post(this);
		
		//*/
	}

	protected void updateHeaderView(String who, View child) {
		
		if( mLastHeaderUpdate > (System.currentTimeMillis() - 1880) ){
			return; // Only 1 per instant
		}
		
		edgeChildView = null;
		try {
			edgeChildView = (LinearLayout) child;
		} catch (ClassCastException e){
			mLog.e(TAG, "Failed to create childView as LinearLayout, probably an item.");
			edgeChildView = null;
			return;
		}
		
		if( edgeChildView == null ){
			return;
		}
		mLastHeaderUpdate = System.currentTimeMillis();
		mLog.e(TAG, "onChildViewAdded Located Header");
		//edgeChildView.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.FILL_PARENT, ListView.LayoutParams.WRAP_CONTENT));
        if( mLandscape ){
        	edgeChildView.setOrientation(LinearLayout.HORIZONTAL);
        }else{
        	edgeChildView.setOrientation(LinearLayout.VERTICAL);
        }
        edgeChildView.setGravity(Gravity.CENTER);
		
		edgeChildView.removeAllViews();
		
		//longname = (TextView) edgeChildView.getChildAt(1);
		mHeaderSeenCount++;
		//longname.setText("Seen by ListView " + mHeaderSeenCount +" Times");
		//if( mHeaderSeenCount > 5 ){ ll.removeAllViews(); }
		/*
		ImageView upgradebutton = null;
		
		upgradebutton = new ImageView(mContext);
		upgradebutton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 80));
		upgradebutton.setScaleType(ImageView.ScaleType.FIT_CENTER);
		upgradebutton.setImageResource(R.drawable.upgrade);
		upgradebutton.setFocusable(true);
		upgradebutton.setClickable(true);
		upgradebutton.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				Intent goFish = new Intent(mContext, Customize.class);
				goFish.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(goFish);
			}
        	
        });//*/
		
		String upgrades = mSharedPreferences.getString("upgrades", "");
		
		//if( upgrades.contains("Customize to Full Version.") ){ upgradebutton.setVisibility(View.GONE); }
	
        TextView tv = new TextView(mContext);
        tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT) );
        int totalcount = mSharedPreferences.getInt("total", 0);
        long syncsuccess = mSharedPreferences.getLong("syncsuccess", 0);
        
        String text = "";
        
        if( TAG == "ListView" ){
        
	        text = "Records in Database: "+totalcount+"";
	        
			/*if( upgrades.contains("Customize to Full Version.") ){
				
			}else{
				
				//if( getListView().getCount() > 18 ){
					//text += "\n(Showing some of the most recent)";
					  //text += "\nUpgrade to full version to see more.";
				//}
				//tv.setText("\n\nUnread will be deleted after 24 hours.  Customize to The Full Version for 7 days.  Press Menu then Customize.\n");
			}//*/
			
			if( syncsuccess > 0 ){
				text += "\n" + "As recent as ";
				int diff = (int) ((System.currentTimeMillis() - syncsuccess)/1000);
				if( diff > 60 ){
					if( diff > 3600 ){
						text += (diff/3600) +" hours ago.";
					}else{
						text += (diff/60) +" minutes ago.";
					}
				}else{
					text += diff + " seconds ago.";
				}
			}
			tv.setText(text);
			
        } else if( TAG == "ListSave" ){
        	
        	int count = 0;
        	Cursor countCursor = SqliteWrapper.query(mContext, mContext.getContentResolver(), DataProvider.CONTENT_URI, 
        			new String[] {"count(*)"},
        			"status > 0 and read > 0", // Future configurable time to expire seen and unread
        			null, 
        			null);
        	if( countCursor != null ){if( countCursor.moveToFirst() ){count = countCursor.getInt(0);} countCursor.close();}
		
        	mPreferencesEditor.putInt("savedcount", count);
        	mPreferencesEditor.commit();
        	
        	text = "Records in Database: "+totalcount+"";
        	/*/
	        if( upgrades.contains("Customize to Full Version.") ){
				
			}else{
				if( count > 10 ){
					text += "\n(Showing some of the most recent, Customize to see more.)";
				}
			}//*/
			if( count > 1 ){
				text += "\n" + count + " saved records.";
			}
			tv.setText(text);
        } else if( TAG == "ListOut" ){
        	int count = 0;
        	Cursor countCursor = SqliteWrapper.query(mContext, mContext.getContentResolver(), DataProvider.CONTENT_URI, 
        			new String[] {"count(*)"},
        			"status > 0 and read == 0", // Future configurable time to expire seen and unread
        			null, 
        			null);
        	if( countCursor != null ){if( countCursor.moveToFirst() ){count = countCursor.getInt(0);} countCursor.close();}
		
        	mPreferencesEditor.putInt("outcount", count);
        	mPreferencesEditor.commit();
        	
        	text = "Records in Database: "+totalcount+"";
        	/*
        	if( upgrades.contains("Customize to Full Version.") ){
				
			}else{
				if( count > 38 ){
					text += "(Showing only some of the most recent)";
				}
				text += "\nThese will be deleted after 24 hours.";
				if( count > 30 ){
					text += "\nUpgrade to full version to see more and to retain for 7 days.";
				}
			}//*/
			if( count > 1 ){
				text += "\n" + count + " outbound records.";
			}
			tv.setText(text);
        }
		tv.setTextSize(18);
		tv.setShadowLayer( (float) 2.0, (float) 0.5, (float) 0.5, (int) Color.BLACK);
		tv.setTextColor(Color.WHITE);
		
		//if( mLandscape ){
		//	edgeChildView.addView(upgradebutton);
			//edgeChildView.addView(tv);
		//}else{
			edgeChildView.addView(tv);
			//edgeChildView.addView(upgradebutton);
		//}
		return;
	}

	private void loadRecord(long id) {
		mLog.i(TAG,"loadRecord() ++++++++++++++++++++");
		
		//*
		
		int lastposition = mSharedPreferences.getInt("position_"+TAG,-1);
		if( id > -1 || lastposition > -1 ){
			
			getListView().requestFocusFromTouch();
			
			int cnt = getListView().getCount();
			int position = 0;
			if( id > -1 ){
				for( position = (cnt-1); position > 0; position--){
					if( getListView().getItemIdAtPosition(position) == id ){
						break;
					}
				}
			}
			if( position == 0 && lastposition > 0 ){
				if( lastposition >= (cnt-1) ){
					lastposition = cnt-1;
				}
				position = lastposition;
				id = getListView().getItemIdAtPosition(position);
			}
			getListView().setSelectionFromTop(position, 50);
			getListView().setSelected(true);
			mLastFocus = id;
			mLastFocusStart = System.currentTimeMillis();
			//getListView().requestFocus();
			
		}//*/
	}


	
	
	

	@Override
	public void onContentChanged() {
		
		super.onContentChanged();
		
		
		//mLog = new Custom(mContext,TAG + " onContentChanged() 930");
		//mLog.w(TAG, "onContentChanged() +++++++++++++++++++++++++++++++");
		
		//if(true){return;} 
		/*
		mListView = getListView();

		int cnt = mListView.getCount();
		mLog.w(TAG,"onContentChanged() 102 cnt("+cnt+")");
		int position = 0;
		for( position = cnt; position > 0; position--){
			mLog.w(TAG,"onContentChanged() 105 position("+position+")");
			
			//LinearLayout im = (LinearLayout) mListView.getItemAtPosition(position);
			//mLog.w(TAG,"onContentChanged() children(" + im.getChildCount() +")" );
			
			//getListView().getItemIdAtPosition(position);
			//if( getListView().getItemIdAtPosition(position) == id ){
				//break;
			//}
		}
		//*/
		
		

	}
	
	private long mActionEventTime = 0;
	//private long mLastActionEventTime = 0;
	private int TRACKBALL_ACTION_SPACE = 150;//ms
	private int mActionLastDirection = -1;
	
	@Override
	public boolean dispatchTrackballEvent(MotionEvent ev) {
		
		// MOVEMENT, UP or DOWN in to mode
		if( ev.getAction() == MotionEvent.ACTION_MOVE && ev.getY() != 0 ){
			

			if( (System.currentTimeMillis() - mActionEventTime) < TRACKBALL_ACTION_SPACE){// && direction == mActionLastDirection){
				//ev.setAction(MotionEvent.ACTION_CANCEL);
				//super.dispatchTrackballEvent(ev);
				return true;
				//Log.w(TAG,"Canceling Trackball motion("+(ev.getEventTime() - mActionEventTime)+") < " + TRACKBALL_ACTION_SPACE);
				//ev.setAction(ev.ACTION_CANCEL);
				//return super.dispatchTrackballEvent(ev);
			}

			int direction = -1;
			if( ev.getY() > 0 ){
				direction = 1; //pos -
			}else if( ev.getY() < 1 ){
				direction = 2; //pos -
			}

			if( (System.currentTimeMillis() - mActionEventTime) > TRACKBALL_ACTION_SPACE * 5 ){
				mActionLastDirection = direction;
				//mActionEventTime = System.currentTimeMillis() + (int) (TRACKBALL_ACTION_SPACE/3);
				mActionEventTime = System.currentTimeMillis();
				return true;
			}

			int pos = getListView().getSelectedItemPosition();
			
			//mUpdateSeenHandler.sendEmptyMessage(0);
			
			//mLastActionEventTime = mActionEventTime;
						
			
			// At the top of the list and wanting to move up.
			if( pos == 0 && direction == 2){
				//ev.setAction(MotionEvent.ACTION_MOVE);
				//listView.this.setTitle("");
				getListView().clearFocus();
				//getListView().requestFocus(View.FOCUS_UP);
				setTitle("");
				return super.dispatchTrackballEvent(ev);
			}
			if( pos == 0 && direction == 1){
				
			}
			
			int top = getListView().getFirstVisiblePosition();
			int cnt = getListView().getCount();
			int pag = getListView().getChildCount();
			long rowid = -1;
			
			//mLog.w(TAG, "Trackball UP/DOWN direction("+direction+") pos("+pos+") top("+top+") cnt("+cnt+") pag("+pag+") rowid("+rowid+") X("+ev.getX()+") Y("+ev.getY()+") top("+getListView().getFirstVisiblePosition()+")");
			
			if( pos == AdapterView.INVALID_POSITION ){
				
				 //RelativeLayout v = (RelativeLayout) getListView().getChildAt(top);
				//int[] location = new int[] {-1,-1};
				//v.getBottom();
				//if(v.getBottom() > 50 ){
				//if( top == 0){
				//getListView().setSelected(true);
				getListView().requestFocusFromTouch();
				getListView().setSelection(top);
				rowid = getListView().getItemIdAtPosition(top);
				
				if( rowid < -1 ){
					return false;
				}
				
				
				Bundle b = new Bundle();
		    	b.putLong("id", rowid);
		    	Message m = new Message();
				m.setData(b);
				mUpdateSeenHandler.sendMessageDelayed(m,100);
				
				//}else if(top+1 < cnt ){
					//getListView().setSelection(top+1);
					
				//}
				
				
				//}else{
					//getListView().setSelection(top+1);
				//}
				return false;
			}
			//else if(top < pos ){
				//getListView().setSelection(top);
				//return true;
			//}
			
			if( pos < 0 ){ pos = 0; }
			if( pos >= cnt ){
				pos = cnt-1;
			}
			
			//if( true ){
				//return super.dispatchTrackballEvent(ev);
			//}
			
			
			
			
			/*
			// UP
			if( ev.getY() < 0 && (ev.getY() * -1) > 1.3  ){ // UP -0.16_7, -0.3_, -0.5, -0.6_, ???
				//.3 page turn
				//.5 glide page turn
				return super.dispatchTrackballEvent(ev);
			}
			// DOWN
			if( ev.getY() > 0 && ev.getY() > 1.3  ){ // UP -0.16_7, -0.3_, -0.5, -0.6_, ???
				//.3 page turn
				//.5 glide page turn
				return super.dispatchTrackballEvent(ev);
			}//*/

			
			
			// DOWN
			if( ev.getY() > 1  ){ // UP -0.16_7, -0.3_, -0.5, -0.6_, ???
				pos += pag - 1;
				if(pos > cnt ){pos = cnt-1;}
				getListView().setSelectionFromTop(pos, 50);
				//mLog.w(TAG, "Trackball PAGE DOWN pos("+pos+") X("+ev.getX()+") Y("+ev.getY()+") top("+getListView().getFirstVisiblePosition()+")");
			}
			// UP
			else if( ev.getY() < -1  ){ // UP -0.16_7, -0.3_, -0.5, -0.6_, ???
				pos -= pag - 1;
				if(pos < 0){pos = 0;}
				if(pos > 1){
					getListView().setSelectionFromTop(pos, 50);
				}else{
					getListView().setSelectionFromTop(pos, 0);
				}
			}
			
			
			// DOWN
			if( ev.getY() > .1  ){ // UP -0.16_7, -0.3_, -0.5, -0.6_, ???
				//.3 page turn
				//.5 glide page turn
				//int pos = getListView().getSelectedItemPosition();
				//if( pos > 0 ){
				pos += 1;
				getListView().setSelectionFromTop(pos, 50);
				//}else{
					//getListView().setSelectionFromTop(pos, 0);
				//}
				//if( pos < getListView().getChildCount() ){
					//getListView().setSelection(pos+1);
				//}
				
				//return super.dispatchTrackballEvent(ev);
			}
			// UP
			else if( (ev.getY() * -1) > .1  ){ // UP -0.16_7, -0.3_, -0.5, -0.6_, ???
				//.3 page turn
				//.5 glide page turn
				//int pos = getListView().getSelectedItemPosition();
				pos -= 1;
				if(pos > -1){
					//if( pos > 1 ){
						getListView().setSelectionFromTop(pos, 50);
					//}
					//getListView().setSelection(pos-1);
				}else{
					ev.setAction(MotionEvent.ACTION_MOVE);
					super.dispatchTrackballEvent(ev);
					return false;
				}
			}
			
			rowid = getListView().getItemIdAtPosition(pos);
			Bundle b = new Bundle();
	    	b.putLong("id", rowid);
	    	Message m = new Message();
			m.setData(b);
			mUpdateSeenHandler.sendMessageDelayed(m,100);
			
			//ev.setAction(MotionEvent.ACTION_CANCEL);
			return true;
			//return super.dispatchTrackballEvent(ev);
			
			
			
			// time limited
			
	
		
			
			
			
		} else if(ev.getAction() == ev.ACTION_MOVE && ev.getX() != 0 && ev.getY() != 0){
			// Diaganal Movement
			//mLog.w(TAG, "Trackball DIAG X("+ev.getX()+") Y("+ev.getY()+") top("+getListView().getFirstVisiblePosition()+")");
			ev.setAction(ev.ACTION_CANCEL);
		} else if(ev.getAction() == ev.ACTION_MOVE && ev.getX() != 0){
			// LEFT RIGHT Movement
			//mLog.w(TAG, "Trackball RIGHT/LEFT X("+ev.getX()+") Y("+ev.getY()+") top("+getListView().getFirstVisiblePosition()+")");
			ev.setAction(ev.ACTION_CANCEL);
		}
		
		
		return super.dispatchTrackballEvent(ev);
	}

	@Override
	public void setSelection(int position) {
		// TODO Auto-generated method stub
		super.setSelection(position);
		//mLog.i(TAG,"setSelection() ++++++++++++++++++++");
	}

	
	@Override
	protected void onResume() {
		mLog.w(TAG,"onResume() ++++++++++++++++++++++++++++++++");
		
		super.onResume();
		
		//mConsoleTouchReset = true;
		
		//mHandler.post(this);
		//mHandler.postDelayed(this, 10 * 1000);
		//mHandler.post(this);
		if( mThread.isAlive() ){
			mLog.i(TAG, "onResume() verified mThread is alive");
			//mHandler.postDelayed(this, 30);
		}else if(mThread.getState() == State.RUNNABLE){
			mLog.w(TAG, "onResume() is starting mThread");
			try {
				mThread.start();
			} catch (IllegalThreadStateException e){
				mLog.w(TAG,"onResume() mThread already started.");
				//mHandler.postDelayed(this, 100);
			}
		}
		
		setGlobals();
		
		//mLandscape = (mWidth > mHeight) ? true : false;
		//mLog.loadlist(this, mLandscape);
		
		
		long id = mSharedPreferences.contains("id") ? mSharedPreferences.getLong("id",-1) : -1;
		loadRecord(id);
		
	}
	
	@Override
	protected void onStart() {
		mLog.i(TAG,"onStart() ++++++++++++++++++++");
		super.onStart();
		
		//mConsoleTouchReset = true;
		
		if( mThread.isAlive() ){
			mLog.i(TAG, "onStart() verified mThread is alive");
			//mHandler.postDelayed(this, 30);
		}else{
			mLog.w(TAG, "onStart() is starting mThread");
			try {
				mThread.start();
			} catch (IllegalThreadStateException e){
				mLog.w(TAG,"onStart() mThread already started.");
				//mHandler.postDelayed(this, 100);
			}
		}

		//mHandler.postDelayed(this, 100);
		//mHandler.post(this);
		//mHandler.postDelayed(this, 10 * 1000);
		//Thread thr = new Thread(null, this, "touchconsole_runnable");
	    //thr.start();
		
		
		setGlobals();
		
		boolean landscape = (mWidth > mHeight) ? true : false;
		mLog.loadlist(this, landscape);
		
		long id = mSharedPreferences.contains("id") ? mSharedPreferences.getLong("id",-1) : -1;
		loadRecord(id);
		
		
	}
	
	public void setGlobals(){
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

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		
		if( key == "lowmemory" || key == "cpublock" || key == "syncstart" || key == "syncend" || key == "lastfeedactive"){
			//mHandler.post(this);
			//mHandler.postDelayed(this, 10 * 1000);
			//mHandler.sendEmptyMessage(7);
			//mHandler.sendEmptyMessageDelayed(5, 10 * 1000);
		}
		
		/*
		if( key == "lowmemory" ){
			mConsoleTouch.setImageDrawable(getResources().getDrawable(R.drawable.listfooter));
			SystemClock.sleep(100);
			long t = sharedPreferences.getLong(key, 0);
			if(t > 0){
				mConsoleTouch.setImageDrawable(getResources().getDrawable(R.drawable.listfooter_memoryblock));
				mHandler.postDelayed(this, 1000 * 10);
				//mHandler.postDelayed(this, 1000 * 2 * 60);
			}else{
				mConsoleTouch.setImageDrawable(getResources().getDrawable(R.drawable.listfooter_active));
			}
		} else if( key == "cpublock" ){
			mConsoleTouch.setImageDrawable(getResources().getDrawable(R.drawable.listfooter));
			SystemClock.sleep(100);
			long t = sharedPreferences.getLong(key, 0);
			if( t > 0 ){
				mConsoleTouch.setImageDrawable(getResources().getDrawable(R.drawable.listfooter_cpublock));
				mHandler.postDelayed(this, 1000 * 10);
				//mHandler.postDelayed(this, 1000 * 60);
			}else{
				mConsoleTouch.setImageDrawable(getResources().getDrawable(R.drawable.listfooter_active));
			}
		} else if( key == "syncstart" ){
			mConsoleTouch.setImageDrawable(getResources().getDrawable(R.drawable.listfooter));
			SystemClock.sleep(100);
			//long t = sharedPreferences.getLong(key, 0);
			int syncInterval = mSharedPreferences.getInt("syncinterval", 29);
			mConsoleTouch.setImageDrawable(getResources().getDrawable(R.drawable.listfooter_active));
			mHandler.postDelayed(this, 1000 * 10);
			//mHandler.postDelayed(this, 1000 * 60);
			//mHandler.postDelayed(this, 1000 * 60 * syncInterval);
		} else if( key == "syncend" ){
			mConsoleTouch.setImageDrawable(getResources().getDrawable(R.drawable.listfooter));
			SystemClock.sleep(100);
			//long t = sharedPreferences.getLong(key, 0);
			int syncInterval = mSharedPreferences.getInt("syncinterval", 29);
			mConsoleTouch.setImageDrawable(getResources().getDrawable(R.drawable.listfooter_healthy));
			mHandler.postDelayed(this, 1000 * 10);
			//mHandler.postDelayed(this, 1000 * 60);
			//mHandler.postDelayed(this, 1000 * 60 * syncInterval);
		} else if( key == "lastfeedactive" ){
			mConsoleTouch.setImageDrawable(getResources().getDrawable(R.drawable.listfooter));
			SystemClock.sleep(100);
			//long t = sharedPreferences.getLong(key, 0);
			int syncInterval = mSharedPreferences.getInt("syncinterval", 29);
			mConsoleTouch.setImageDrawable(getResources().getDrawable(R.drawable.listfooter_active));
			mHandler.postDelayed(this, 1000 * 10);
			//mHandler.postDelayed(this, 1000 * 60);
			//mHandler.postDelayed(this, 1000 * 60 * syncInterval);
		}//*/
	}

	
	private Handler mHandler;
	private Thread mThread;
	
	public void run() {
		
		mLog.refreshConsoleTouch(mConsoleTouch, mHandler);

	}

}
