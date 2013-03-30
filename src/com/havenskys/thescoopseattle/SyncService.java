package com.havenskys.thescoopseattle;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

import com.havenskys.newsbite.IRemoteService;
import com.havenskys.newsbite.IRemoteServiceCallback;
import com.havenskys.newsbite.ISecondary;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.format.DateUtils;
import android.util.Log;

public class SyncService extends Service implements Runnable {
	
	final RemoteCallbackList<IRemoteServiceCallback> mCallbacks = new RemoteCallbackList<IRemoteServiceCallback>();
	int mValue = 0; //used for Callback sample
	private static int MINMEMORY = 585;
	
	private static String TAG = "Service";
	
	//private Handler mHandler;
	private NotificationManager mNM;
	private ConnectivityManager mCM;
	private PowerManager mPM;
	private ActivityManager mAM;
	private Custom mLog;
	private Bundle mBundle;
	private AlarmManager mAlM;
	private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;
    
    
    private final class ServiceHandler extends Handler {

    	private int mRequestCount = 0;
    	private Custom mLog;
    	//private String TAG = "ServiceHandler";
        public ServiceHandler(Looper looper) { super(looper); }

        public void run(){
        	//mLog = new Custom(,TAG + " run()");
        	mLog.i(TAG,"ServiceHandler() run() +++++++++++++++++++++++++++++");
        }
        public void handleMessage(Message msg) {
        	mRequestCount++;
        	//TAG = TAG + " ServiceHandler";
        	mLog = new Custom(mContext,TAG + " handleMessage()");
        	mLog.i(TAG,"ServiceHandler() handleMessage() number("+mRequestCount+") +++++++++++++++++++++++++++++");
        	
        	
        	int what = msg.what;
        	int arg1 = msg.arg1;
        	mLog.e(TAG,"ServiceHandler() handleMessage() number("+mRequestCount+") what("+what+") arg1("+arg1+") memory free("+Runtime.getRuntime().freeMemory()+") max("+Runtime.getRuntime().maxMemory()+") available("+Runtime.getRuntime().totalMemory()+")");
        	//if( what == 7 ){
        		//mLog.w(TAG,"ServiceHandler() handleMessage() what("+what+") START UP, ignore.");
        		//return;
        	//}
        	//*/
        	final int N = mCallbacks.beginBroadcast();
            for (int i=0; i<N; i++) {
                try {
                    mCallbacks.getBroadcastItem(i).valueChanged(mValue);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing
                    // the dead object for us.
                }
            }
            mCallbacks.finishBroadcast();
            //*/
            //sendMessageDelayed(obtainMessage(7),5*1000);

        	
            int serviceId = msg.arg1;
            Intent intent = (Intent) msg.obj;
            String action = "";
            String who = "unknown";
            if( intent != null ){
            	action = intent.getAction();
            	if( intent.hasExtra("com.havenskys.thescoopseattle.who") ){
    	            Bundle extras = intent.getExtras();
    	            who = extras.containsKey("com.havenskys.thescoopseattle.who") ? extras.getString("com.havenskys.thescoopseattle.who") : "";
    	            mLog.i(TAG,"handleMessage() Received Extra Who " + who);
                }else{
                	mLog.i(TAG,"handleMessage() Did not receive Extra information " + who);
                }
            }
            if( who == "unknown" ){
            	who = "" + what;
            }
            
            
            
    		//mLog.i(TAG,"handleMessage() get access to Shared Preferences");
    		mSharedPreferences = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);

    		//mLog.i(TAG,"handleMessage() get Preferences Editor");
    		mPreferencesEditor = mSharedPreferences.edit();
    		mPreferencesEditor.putLong("syncstart", System.currentTimeMillis());
    		mPreferencesEditor.commit();
    		mLog.setSharedPreferences(mSharedPreferences, mPreferencesEditor);
            
    		//mLog.i(TAG,"onCreate() get AlarmManager");
    		mAlM = (AlarmManager) getSystemService(ALARM_SERVICE);
    		
    		//String servicehistory = mSharedPreferences.getString("servicehistory", "");
    		//mPreferencesEditor.putString("servicehistory", servicehistory + "\n" + DateUtils.formatDateTime(mContext, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME ) ).commit();
    		mLog.serviceState(TAG + " handleMessage()", "Service started by " + who);
    		
    		int syncInterval = mSharedPreferences.contains("sync") ? mSharedPreferences.getInt("sync",29) : 29;
    		mLog.i(TAG, "onCreate() setting a repeating reminder to wake up in 1 minute and " + syncInterval + " minutes.");
    		//mLog.w(TAG, "handleMessage() going to sleep (low resource usage and easily killable by system) for minutes("+syncInterval+")");
    		//long starttime = System.currentTimeMillis() + (syncInterval * 60 * 1000);
    		
    		
    		//Intent service = new Intent();
    		//service.setClass(mContext, com.havenskys.thescoopseattle.SyncService.class);
    		//service.putExtra("com.havenskys.thescoopseattle.who", TAG + " onCreate() Repeating Service Alarm minutes("+syncInterval+") at " + DateUtils.formatDateTime(mContext, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME ));
    		//PendingIntent serviceP = PendingIntent.getService(mContext, 1000, service, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_CANCEL_CURRENT);
    		//mAlM.setRepeating(mAlM.RTC_WAKEUP, starttime, mAlM.INTERVAL_FIFTEEN_MINUTES, serviceP);
    		
    		
    		Intent resetservice = new Intent();
            //com.havenskys.thescoopseattle.SERVICE_RESET
    		resetservice.setAction("com.havenskys.thescoopseattle.SERVICE_SCHEDULE1");
    		PendingIntent service3 = PendingIntent.getBroadcast(mContext, 0, resetservice, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_CANCEL_CURRENT);
    		//mAlM.set(AlarmManager.RTC_WAKEUP,( System.currentTimeMillis() + (syncInterval * 60 * 1000) ), service3);
    		Date d = new Date();
    		long lastrun = mSharedPreferences.getLong("lastrun", System.currentTimeMillis());
			long lastrunAgo = (System.currentTimeMillis() - lastrun)/1000/60/60;
			if( lastrunAgo > 24 ){
				// Haven't run the application for 25 hours or more, let's not schedule an evening run.
			}else{
				if( d.getHours() < 20 && d.getHours() > 8 ){
					if( lastrunAgo <= 1 ){
						mAlM.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (60 * 60 * 1000), AlarmManager.INTERVAL_HOUR, service3);
					}else{
						if( lastrunAgo > 8 ){ lastrunAgo = 9; }
						mAlM.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (lastrunAgo * 60 * 60 * 1000), AlarmManager.INTERVAL_HOUR, service3);
					}
				}else{
					long startup = System.currentTimeMillis() + (2 * 60 * 60 * 1000);
					if ( d.getHours() < 9 ){
						int diff = 9 - d.getHours();
						if ( diff > 5 ){
							startup = System.currentTimeMillis() + (5 * 60 * 60 * 1000);
						}else if ( diff > 3 ){
							startup = System.currentTimeMillis() + (3 * 60 * 60 * 1000);
						}else{
							startup = System.currentTimeMillis() + (60 * 60 * 1000);
						}
					}
					mAlM.setRepeating(AlarmManager.RTC_WAKEUP, startup, AlarmManager.INTERVAL_HALF_DAY, service3);
				}
			}

			
			
			//Intent startservice = new Intent();
			//startservice.setClass(mContext, com.havenskys.thescoopseattle.StartService.class);
			//startservice.putExtra("com.havenskys.thescoopseattle.who", TAG + " handleMessage() StartService Activity Alarm");
			//PendingIntent service2 = PendingIntent.getActivity(mContext, 0, startservice, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_CANCEL_CURRENT);
			//service2.setClass(mContext, com.havenskys.thescoopseattle.StartService.class);
			//mAlM.set(AlarmManager.RTC_WAKEUP,( System.currentTimeMillis() + (5 * 60 * 1000) ), service2);
			//mAlM.set(AlarmManager.RTC_WAKEUP,( System.currentTimeMillis() + (5 * 60 * 1000) ), serviceP);

    		/*/
    		mLog.i(TAG, "handleMessage() verify memory available to work with. free memory("+Runtime.getRuntime().freeMemory()+")");
    		if( Runtime.getRuntime().freeMemory() < (MINMEMORY * 1024) ){
    			mPreferencesEditor.putLong("lowmemory", System.currentTimeMillis()).commit();
    			mLog.e(TAG, "handleMessage() too little memory to work with freememory("+(int)(Runtime.getRuntime().freeMemory()/1024)+" K) MAKE SURE UI INFORMS USER please, will recheck in 1 minute.");
    			mPreferencesEditor.putLong("lowmemory", System.currentTimeMillis()).commit();
    			//mPreferencesEditor.putLong("lowmemory", System.currentTimeMillis()).commit();
    			//mLog.e(TAG, "handleMessage() too little memory second line, first show but commit preferences doesn't.");
    			//mHandler.postDelayed(this, 60 * 1000);
    			//stopSelf();
    			//Intent service = new Intent();
    			//service.setClass(this, SyncService.class);
    			//stopService(service);
    			
    			Intent resetservice = new Intent();
    	        //com.havenskys.thescoopseattle.SERVICE_RESET
    		  	resetservice.setAction("com.havenskys.thescoopseattle.IntentReceiver.SERVICE_RESET_1");
    			PendingIntent service3 = PendingIntent.getBroadcast(mContext, 0, resetservice, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_CANCEL_CURRENT);
    			//mAlM.set(AlarmManager.RTC_WAKEUP,( System.currentTimeMillis() + (60 * 1000) ), service3);
    			mAlM.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (60 * 1000), AlarmManager.INTERVAL_FIFTEEN_MINUTES, service3);
    			
    			//mServiceHandler.sendEmptyMessageDelayed(-2, 60 * 1000);
    			//mServiceHandler.postDelayed(this, 60 * 1000);
    			//SystemClock.sleep(60000);
    			
    			mLog.serviceState(TAG + " handleMessage()", "Low memory retrying in 1 minute.");
    			stopSelf();
    			return;
    		}//*/
    		//mPreferencesEditor.putLong("lowmemory", 0).commit();

    		//mLog.i(TAG,"onCreate() get NotificationManager");
    		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    		mLog.setNotificationManager(mNM);


            mLog.w(TAG, "handleMessage() running startUp()");
    		startUp();
            //getlatest();
    			
            
            
    		//mLog.w(TAG, "handleMessage() preparing to wait for next interval");
    		//mPreferencesEditor = mSharedPreferences.edit();
    		
        }
        
    }
    
    
    
    private Context mContext;
    
	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this.getApplicationContext();
		
		mLog = new Custom(this, TAG + " onCreate() 25");
		mLog.i(TAG,"onCreate() ++++++++++++++++++++++++++++++++++");
		
		//mLog.i(TAG,"onCreate() get access to Shared Preferences");
		mSharedPreferences = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);

		//mLog.i(TAG,"onCreate() get Preferences Editor");
		mPreferencesEditor = mSharedPreferences.edit();
		mLog.setSharedPreferences(mSharedPreferences, mPreferencesEditor);
        
		//mLog.i(TAG,"onCreate() get AlarmManager");
		mAlM = (AlarmManager) getSystemService(ALARM_SERVICE);
		
		int syncInterval = mSharedPreferences.contains("sync") ? mSharedPreferences.getInt("sync",29) : 29;
		mLog.i(TAG, "onCreate() setting a repeating reminder to wake up in 1 minute and " + syncInterval + " minutes.");
		//mLog.w(TAG, "handleMessage() going to sleep (low resource usage and easily killable by system) for minutes("+syncInterval+")");
		//long starttime = System.currentTimeMillis() + (syncInterval * 60 * 1000);
		
		//PendingIntent service = PendingIntent.getActivity(mContext, 0, intentJump2, Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_MULTIPLE_TASK );
		
		//Intent service = new Intent();
		//service.setClass(mContext, com.havenskys.thescoopseattle.SyncService.class);
		//service.putExtra("com.havenskys.thescoopseattle.who", TAG + " onCreate() Repeating Service Alarm minutes("+syncInterval+") at " + DateUtils.formatDateTime(mContext, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME ));
		//PendingIntent serviceP = PendingIntent.getService(mContext, 1000, service, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_CANCEL_CURRENT);
		//mAlM.setRepeating(mAlM.RTC_WAKEUP, starttime, mAlM.INTERVAL_FIFTEEN_MINUTES, serviceP);
		
		
		Intent resetservice = new Intent();
        //com.havenskys.thescoopseattle.SERVICE_RESET
		resetservice.setAction("com.havenskys.thescoopseattle.SERVICE_SCHEDULE0");
		PendingIntent service3 = PendingIntent.getBroadcast(mContext, 0, resetservice, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_CANCEL_CURRENT);
		//mAlM.set(AlarmManager.RTC_WAKEUP,( System.currentTimeMillis() + (syncInterval * 60 * 1000) ), service3);
		Date d = new Date(); 
		long lastrun = mSharedPreferences.getLong("lastrun", System.currentTimeMillis());
		long lastrunAgo = (System.currentTimeMillis() - lastrun)/1000/60/60;
		if( lastrunAgo > 24 ){
			// Hasn't been run in the last 25 hours, don't schedule automatic downloads.
		}else{
			if( d.getHours() < 20 && d.getHours() > 8 ){
				if( lastrunAgo <= 1 ){
					mAlM.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + (30 * 60 * 1000), AlarmManager.INTERVAL_FIFTEEN_MINUTES, service3);
				}else{
					if( lastrunAgo > 3 ){ lastrunAgo = 3; }
					mAlM.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + (lastrunAgo * 60 * 60 * 1000), AlarmManager.INTERVAL_HOUR, service3);
				}
			}else{
				long startup = System.currentTimeMillis() + (2 * 60 * 60 * 1000);
				if ( d.getHours() < 9 ){
					int diff = 9 - d.getHours();
					if ( diff > 4 ){
						startup = System.currentTimeMillis() + (4 * 60 * 60 * 1000);
					}else if ( diff > 2 ){
						startup = System.currentTimeMillis() + (2 * 60 * 60 * 1000);
					}else{
						startup = System.currentTimeMillis() + (60 * 60 * 1000);
					}
				}
				mAlM.setRepeating(AlarmManager.RTC, startup, AlarmManager.INTERVAL_HOUR, service3);
				//mAlM.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + (2 * 60 * 60 * 1000), AlarmManager.INTERVAL_HOUR, service3);
			}
		}

		
		
		//mLog.i(TAG,"onCreate() get AlarmManager");
		//mAlM = (AlarmManager) getSystemService(ALARM_SERVICE);
		//mAlM.
		
		//mLog.i(TAG,"onCreate() get ConnectivityManager");
		//mCM = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
		//mLog.i(TAG,"run() getting networks");
		//NetworkInfo[] ni = mCM.getAllNetworkInfo();
		//for( int i = 0; i < ni.length; i++){
			//mLog.i(TAG, "getlatest() type("+ni[i].getTypeName()+") available(" + ni[i].isAvailable() + ") connected("+ni[i].isConnected()+") connected("+ni[i].isConnectedOrConnecting()+")");
		//}
		
		
		//mLog.i(TAG,"onCreate() get PowerManager");
		//mPM = (PowerManager) this.getSystemService(POWER_SERVICE);
		//mPM.userActivity(System.currentTimeMillis(), true);
		
		
		

		/*/		
		mLog.i(TAG,"onCreate() get ActivityManager");
		mAM = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
		MemoryInfo outInfo = null;
		mAM.getMemoryInfo(outInfo);
		List<RunningAppProcessInfo> rap = mAM.getRunningAppProcesses();
		RunningAppProcessInfo rapi = null;
		for(int i = 0; i < rap.size(); i++ ){
			rapi = rap.get(i);
		}//*/

		/*
		mLog.i(TAG,"onCreate() get access to Shared Preferences");
		mSharedPreferences = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);

		mLog.i(TAG,"onCreate() get Preferences Editor");
		mPreferencesEditor = mSharedPreferences.edit();
		
		int startcount = mSharedPreferences.contains("startcount") ? mSharedPreferences.getInt("startcount",-2) : -2;
		long laststart = mSharedPreferences.contains("laststart") ? mSharedPreferences.getLong("laststart",-2) : -2;
		
		int ago = (int) ((System.currentTimeMillis() - laststart)/60000);
		if( laststart > 0 && ago > 30 ){// minutes
			startcount = 0;
		}

		
		startcount++;
		mPreferencesEditor.putInt("startcount", startcount).commit();
		mPreferencesEditor.putLong("laststart", System.currentTimeMillis()).commit();
		//*/
		
		//mHandler = new Handler();
		
		Log.i(TAG,"onCreate() HandlerThread");
        //HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_MORE_FAVORABLE);
        Log.i(TAG,"onCreate() HandlerThread.start()");
        thread.start();

        Log.i(TAG,"onCreate() HandlerThread.getLooper()");
        mServiceLooper = thread.getLooper();
        
       Log.i(TAG,"onCreate() ServiceHandler");
        mServiceHandler = new ServiceHandler(mServiceLooper);
        mServiceHandler.sendEmptyMessage(7);
        
		
		//int syncInterval = mSharedPreferences.contains("sync") ? mSharedPreferences.getInt("sync",30) : 30;
		
		/*
		if( startcount > 10 ){
			mLog.i(TAG, "onCreate() 58 startup("+startcount+") occured");
			mHandler.postDelayed(this, syncInterval * 60 * 1000);
			return;
		}
		
		long lastfeedactive = mSharedPreferences.contains("lastfeedactive") ? mSharedPreferences.getLong("lastfeedactive",-1) : -1;
		
		if( lastfeedactive > 0 && (System.currentTimeMillis() - lastfeedactive) > 30000 ){
			mLog.i(TAG, "onCreate() 68 lastfeedactive over 30 seconds ago");
			startUp();
		}else
			
			//*/
		
		//if( laststart > 0 && ago > syncInterval ){
	
		
		//}
        
        //Thread thr = new Thread(null, this, Custom.APP + "_service_thread");
        //thr.start();
        
        
    
        //Intent serviceH = new Intent();
		//serviceH.setClass(mContext, com.havenskys.thescoopseattle.SyncService.class);
		//serviceH.putExtra("com.havenskys.thescoopseattle.who", TAG + " onCreate() Repeating Service Alarm minutes("+syncInterval+") at " + DateUtils.formatDateTime(mContext, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME ));
		//Message msg = mServiceHandler.obtainMessage();
		//msg.what = -4;
        //msg.arg1 = startId;
        //msg.obj = serviceH;
        //mServiceHandler.sendMessage(msg);
        //mServiceHandler.sendEmptyMessageDelayed(-4, 1 * 1000);
        
        //msg = mServiceHandler.obtainMessage();
		//msg.what = -3;
        //msg.arg1 = startId;
        //msg.obj = serviceH;
        //mServiceHandler.sendMessageDelayed(msg, 100 * 1000 );
        //mServiceHandler.sendEmptyMessageDelayed(-3, 100 * 1000);
		
	}

	/*
	@Override
	public IBinder onBind(Intent arg0) {
		mLog.i(TAG,"onBind() ++++++++++++++++++++++++++++++++++");
		return null;
	}//*/

	public void run() {
		
		mLog.i(TAG,"run() ++++++++++++++++++++++++++++++++++");
		
		
		//int syncInterval = mSharedPreferences.contains("sync") ? mSharedPreferences.getInt("sync",29) : 29;
		//long starttime = System.currentTimeMillis() + (syncInterval * 60 * 1000);
		
		//Intent service = new Intent();
		//service.setClass(mContext, com.havenskys.thescoopseattle.SyncService.class);
		//service.putExtra("com.havenskys.thescoopseattle.who", TAG + " run() Repeating Service Alarm");
		//PendingIntent serviceP = PendingIntent.getService(mContext, 1000, service, PendingIntent.FLAG_CANCEL_CURRENT);
		//mAlM.setRepeating(mAlM.RTC_WAKEUP, starttime, mAlM.INTERVAL_FIFTEEN_MINUTES, serviceP);
		
		


		//mLog.i(TAG,"run() get access to Shared Preferences");
		mSharedPreferences = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);

		//mLog.i(TAG,"run() get Preferences Editor");
		mPreferencesEditor = mSharedPreferences.edit();
		
		mLog.setSharedPreferences(mSharedPreferences, mPreferencesEditor);
		
		getlatest();
		
		//int postcycle = mSharedPreferences.contains("postcycle") ? mSharedPreferences.getInt("postcycle",0) : 0;
		//postcycle++;
		//mLog.i(TAG, "run() service watcher 135  postcycle("+postcycle+")");
		
		//mPreferencesEditor.putInt("postcycle", postcycle).commit();
		//mHandler.postDelayed(this, 60 * 1000);
		/*
		for(int cycle = 0; cycle < 10; cycle++){
			
			mLog.i(TAG, "run() service watcher 140 cycle("+cycle+") postcycle("+-1+")");
			
			int lastfeedid = mSharedPreferences.contains("lastfeedid") ? mSharedPreferences.getInt("lastfeedid",-2) : -2;
			long lastsyncrestart = mSharedPreferences.contains("syncrestart") ? mSharedPreferences.getLong("syncrestart",-1) : -1;
			int syncrestartcount = mSharedPreferences.contains("syncrestartcount") ? mSharedPreferences.getInt("syncrestartcount",-1) : -1;
			long lastfeedactive = mSharedPreferences.contains("lastfeedactive") ? mSharedPreferences.getLong("lastfeedactive",-1) : -1;			
			int ago = (int) ((System.currentTimeMillis() - lastfeedactive)/1000);
			if( lastfeedid > -1 ){
				mLog.i(TAG, "run() 124 service watcher activefeed("+lastfeedid+") started("+ago+" seconds)");
			}
			
			
			int minactivity = 10;
			
			if( lastfeedactive <= 0 || lastfeedid == -1 ){
				//mPreferencesEditor.putInt("syncrestartcount", 0).commit();
				mLog.i(TAG, "run() 129 feed service is healthy");
				getlatest();
				mNM.cancel(-3000);
				break;
			}else if( ago > minactivity && lastfeedid > -1 ){
				mLog.w(TAG, "run() 135");
				//syncrestartcount++;
				//mPreferencesEditor.putInt("syncrestartcount", syncrestartcount).commit();
				mLog.w(TAG, "run() 154");
				if( ago > minactivity * 10 ){
					mLog.w(TAG, "run() 156");
					//mPreferencesEditor.putInt("syncrestartcount", 1).commit();
					//syncrestartcount = 1;
				}
				mLog.w(TAG, "run() 159");
				//if( syncrestartcount < mLog.dataFeed.length ){
					String longname = mLog.dataFeed[lastfeedid][0];
					mLog.i(TAG, "run() feed service is taking too long on "+longname+"(" + lastfeedid + ") starting");
					mPreferencesEditor.putLong("syncrestart", System.currentTimeMillis()).commit();
					getlatest();
					break;
					
				}else{
					mLog.w(TAG, "run() 151");
					long lastbugreport = mSharedPreferences.contains("lastbugreport") ? mSharedPreferences.getLong("lastbugreport",-1) : -1;
					String bugreport = mSharedPreferences.contains("bugreport") ? mSharedPreferences.getString("bugreport", "") : "";
					String bugtext = "Synchronization is being restarted too often (70) restarted("+syncrestartcount+") in "+((int) (System.currentTimeMillis() - lastsyncrestart)/1000 )+" seconds.";
					
					if( lastbugreport < (System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000) ) ){ // daily for now, weekly later, then monthly.
						mLog.w(TAG, "run() 157 BugReport: " + bugtext);
						mPreferencesEditor.putString("bugreport", bugreport + "\n" + bugtext ).commit();
					}else{
						mLog.w(TAG, "run() 160");
						
						Intent jump = new Intent(Intent.ACTION_SEND);
						jump.putExtra(Intent.EXTRA_TEXT, "This is an automatically constructed bug report.\n\n"+bugreport+"\n\n\n\n");
						jump.putExtra(Intent.EXTRA_EMAIL, new String[] {"\""+ Custom.APP + " Support\" <havenskys@gmail.com>"} ); 
						jump.putExtra(Intent.EXTRA_SUBJECT, "Support Request: " + Custom.APP);
						jump.setType("message/rfc822"); 
						//startActivity(Intent.createChooser(jump, "Email"));
						
						Notification notif = new Notification(R.drawable.docdot, "Doc Chomps Software, automatic support request, please send.", System.currentTimeMillis()); // This text scrolls across the top.
				        //PendingIntent pi2 = PendingIntent.getActivity(this, 0, intentJump2, Intent.FLAG_ACTIVITY_NEW_TASK );
				        PendingIntent pi2 = PendingIntent.getActivity(this, 0, jump, Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_MULTIPLE_TASK );
						notif.setLatestEventInfo(this, "Doc Chomps Software", "Please send this automated support request.", pi2); // This Text appears after the slide is open
						
				        mNM.notify(-3000, notif);
						
						mPreferencesEditor.putLong("lastbugreport", System.currentTimeMillis());
						mPreferencesEditor.putString("bugreport", "" );
						mPreferencesEditor.putInt("syncrestartcount", 0);
						mPreferencesEditor.commit();
						break;
					}
					
				}
				
				
			}
		//
			mLog.w(TAG, "run() 124 sleep");
			SystemClock.sleep(5000 * cycle);
		}
		//*/
		
		//mLog.w(TAG, "run() 191 loop end");
		
		//mLog.i(TAG,"run() get Preferences Editor");
		//mPreferencesEditor = mSharedPreferences.edit();
		//mPreferencesEditor.putInt("postcycle", 0).commit();
		
		//int syncInterval = mSharedPreferences.contains("sync") ? mSharedPreferences.getInt("sync",30) : 30;
		//mLog.w(TAG,"run() setting a reminder to wake up in " + syncInterval + " minutes.");
		
		//mHandler.postDelayed(this, syncInterval * 60 * 1000);
		
		mLog.w(TAG, "run() 217 end");
		//stopSelf();
	}

	@Override
	public void onDestroy() {
		mLog.i(TAG,"onDestroy() ++++++++++++++++++++++++++++++++++");
		if( mNM != null ){
			mNM.cancel(Custom.NOTIFY_ID);
		}
		
		mCallbacks.kill();
		super.onDestroy();
	}

	public void onResume(Intent intent, int startId){
		mLog.i(TAG, "onResume() +++++++++++++++++++++++++++++++");
		//Message msg = mServiceHandler.obtainMessage();
		//msg.what = -5;
        //msg.arg1 = startId;
        //msg.obj = intent;
        //mServiceHandler.sendMessage(msg);
		//mServiceHandler.sendEmptyMessage(-5);
        
		//startUp();
		
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		mLog.i(TAG,"onStart() ++++++++++++++++++++++++++++++++++");
        
		
		//startUp();
		
		Message msg = mServiceHandler.obtainMessage();
		msg.what = -6;
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
        //mServiceHandler.sendEmptyMessage(-6);
		//startUp();
		
		
		//Log.i(TAG,"onStart() HandlerThread");
        //HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        //HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_MORE_FAVORABLE);
        //Log.i(TAG,"onStart() HandlerThread.start()");
        //thread.start();

        //Log.i(TAG,"onStart() HandlerThread.getLooper()");
        //mServiceLooper = thread.getLooper();
        
        //Log.i(TAG,"onStart() ServiceHandler");
        //mServiceHandler = new ServiceHandler(mServiceLooper);
        //mServiceHandler.sendEmptyMessage(-6);
		
	}

	
	
	private void startUp() {
		
		mLog.i(TAG, "startUp() ++++++++++++++++++++");
		mPreferencesEditor.putLong("unexpected", 0);
		mPreferencesEditor.putLong("lowmemory", 0);
		mPreferencesEditor.putLong("cpublock", 0);
		mPreferencesEditor.commit();
		
		Thread thr = new Thread(null, this, Custom.APP + "_service_thread");
		
		mLog.i(TAG, "startUp() 203");
		
		//thr.setPriority(Thread.MIN_PRIORITY);
		//thr.setDaemon(true);
		thr.setUncaughtExceptionHandler(new UncaughtExceptionHandler(){

			public void uncaughtException(Thread thread, Throwable ex) {
				mLog.e(TAG,"startUp() 156 uncaughtException() restaring in 10 seconds [caught] ");
				
				//Intent startservice = new Intent();
				//startservice.setClass(mContext, com.havenskys.thescoopseattle.StartService.class);
				//startservice.putExtra("com.havenskys.thescoopseattle.who", TAG + " handleMessage() StartService Activity Alarm");
				//PendingIntent service2 = PendingIntent.getActivity(mContext, 0, startservice, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_CANCEL_CURRENT);
				//service2.setClass(mContext, com.havenskys.thescoopseattle.StartService.class);
				//mAlM.set(AlarmManager.RTC_WAKEUP,( System.currentTimeMillis() + (10 * 1000) ), service2);

				//Intent service = new Intent();
				//service.setClass(mContext, com.havenskys.thescoopseattle.SyncService.class);
				//service.putExtra("com.havenskys.thescoopseattle.who", TAG + " startUp() Recovery Repeating Service Alarm");
				//PendingIntent serviceP = PendingIntent.getService(mContext, 1000, service, PendingIntent.FLAG_CANCEL_CURRENT);
				//mAlM.set(AlarmManager.RTC_WAKEUP,( System.currentTimeMillis() + (10 * 1000) ), serviceP);
				//mAlM.setRepeating(AlarmManager.RTC_WAKEUP, ( System.currentTimeMillis() + (10 * 1000) ), AlarmManager.INTERVAL_FIFTEEN_MINUTES, serviceP);
				
				mPreferencesEditor.putLong("unexpected", System.currentTimeMillis());
				mPreferencesEditor.commit();
				Intent resetservice = new Intent();
		        //com.havenskys.thescoopseattle.SERVICE_RESET
				resetservice.setAction("com.havenskys.thescoopseattle.SERVICE_RECOVER0");
				//PendingIntent service3 = PendingIntent.getActivity(mContext, 0, resetservice, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_CANCEL_CURRENT);
				PendingIntent service4 = PendingIntent.getBroadcast(mContext, 77, resetservice, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_CANCEL_CURRENT);
				//mAlM.set(AlarmManager.RTC_WAKEUP,( System.currentTimeMillis() + (60 * 1000) ), service3);
				Date d = new Date(); 
				if( d.getHours() < 20 && d.getHours() > 8 ){
					mAlM.set(AlarmManager.RTC_WAKEUP, ( System.currentTimeMillis() + (10 * 1000) ), service4);
				}else{
					mAlM.set(AlarmManager.RTC_WAKEUP, ( System.currentTimeMillis() + (5 * 60 * 1000) ), service4);
				}
			}
			
		});
		
		mLog.i(TAG, "startUp() 215");
		
		thr.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(){

			public void uncaughtException(Thread thread, Throwable ex) {
				ex.printStackTrace();
				mLog.e(TAG,"startUp() 164 defaultUncaughtException() restarting in 10 seconds [caught] ");
				mPreferencesEditor.putLong("unexpected", System.currentTimeMillis());
				mPreferencesEditor.commit();
				Intent resetservice = new Intent();
		        //com.havenskys.thescoopseattle.SERVICE_RESET
				resetservice.setAction("com.havenskys.thescoopseattle.SERVICE_RECOVER1");
				//PendingIntent service3 = PendingIntent.getActivity(mContext, 0, resetservice, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_CANCEL_CURRENT);
				PendingIntent service4 = PendingIntent.getBroadcast(mContext, 78, resetservice, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_CANCEL_CURRENT);
				//mAlM.set(AlarmManager.RTC_WAKEUP,( System.currentTimeMillis() + (60 * 1000) ), service3);
				Date d = new Date(); 
				if( d.getHours() < 20 && d.getHours() > 8 ){
					mAlM.set(AlarmManager.RTC_WAKEUP, ( System.currentTimeMillis() + (10 * 1000) ), service4);
				}else{
					mAlM.set(AlarmManager.RTC_WAKEUP, ( System.currentTimeMillis() + (5 * 60 * 1000) ), service4);
				}
				
			}
			
		});
		
		mLog.i(TAG, "startUp() 224");
        thr.start();
        mLog.i(TAG, "startUp() 226");
	}



	private SharedPreferences mSharedPreferences;
	private Editor mPreferencesEditor;
	
	private void getlatest() {
		
		//if( mLog == null ){
			//mLog = new Custom(this, TAG + " getlatest() 69");
		//}
		mLog = new Custom(mContext,TAG + " getlatest()");
		mLog.i(TAG,"getlatest() ++++++++++++++++++++++++++++++++++");
		
		//SqliteWrapper.update(mContext, mResolver, DataProvider.CONTENT_URI, setValues, "_id = " + id, null);

		//mLog.i(TAG,"getlatest() get NotificationManager");
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		//mLog.i(TAG,"getlatest() get access to Shared Preferences");
		mSharedPreferences = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);

		//mLog.i(TAG,"getlatest() get Preferences Editor");
		mPreferencesEditor = mSharedPreferences.edit();
		
		//mLog.i(TAG,"getlatest() set NotificationManager");
		mLog.setNotificationManager(mNM);

		// Created this next line to overt a crash (no error) of the service tha happens when preferences are acquired using the below getFeeds() using these references.
		
		//mLog.i(TAG,"getlatest() set Shared Preferences");
		mLog.setSharedPreferences(mSharedPreferences,mPreferencesEditor);
		
		//mLog.i(TAG,"getlatest() cancel current Notification");
		//mNM.cancel(Custom.NOTIFY_ID);
		
		
        //String mUuid = UUID.randomUUID().toString();
        //content://settings/system/notification_sound
        //for (Account account1 : accountsWithNewMail.keySet()) { if (account1.isVibrate()) vibrate = true; ringtone = account1.getRingtone(); }

        //BASEURL = "http://www.seashepherd.org/news-and-media/sea-shepherd-news/feed/rss.html";
        //BASEURL = "http://www.whitehouse.gov/blog/";
        // This will block until load is low or time limit exceeded

		//mLog.i(TAG,"getlatest() get syncLoad");
		int syncLoad = mSharedPreferences.contains("syncload") ? mSharedPreferences.getInt("syncload",4) : 4;
	
		//mLog.i(TAG,"getlatest() get sync");
		int syncInterval = mSharedPreferences.contains("sync") ? mSharedPreferences.getInt("sync",30) : 30;
		
		//mLog.i(TAG,"getlatest() prepare service watcher");
		//mHandler.postDelayed(this, 10 * 1000);
		
		//mLog.i(TAG,"getlatest() getFeeds()");
		int getFeedReply = mLog.getFeeds(TAG + " getlatest() 85");
		
		//mLog.i(TAG,"getlatest() getFeeds() Reply("+getFeedReply+")");
		if( getFeedReply == 0 ){ //FAIL
			
			int failcnt = mSharedPreferences.contains("syncfail") ? mSharedPreferences.getInt("syncfail",0) : 0;
			failcnt++;
			
			//mLog.serviceState(TAG + " getlatest()", "recieved failure("+failcnt+") from getFeeds()");
			
			//int wait = failcnt * 2;
			//if( wait > syncInterval ){ wait = syncInterval; }
			mLog.i(TAG,"getFeeds() reported failed count("+failcnt+"), wait network state.");
			//mLog.setServiceNotification(TAG + " getlatest() 107", Custom.NOTIFY_ID, android.R.drawable.presence_offline, Custom.APP + " (Press here to Stop)", "Retry in "+wait+" minutes.", Custom.APP + " Download Failure, I will retry in "+wait+" minutes.");
			//mHandler.postDelayed( this, 1000 * 60 * wait );
			mPreferencesEditor.putInt("syncfail", failcnt);
			mPreferencesEditor.commit();
			
		}else if(getFeedReply == 1){ // GOOD
			//mNM.cancel(Custom.NOTIFY_ID);
			//if( syncInterval > 0 ){ 
				//mLog.i(TAG,"getlatest() 85 setting postDelay");
				//mHandler.postDelayed( this, 1000 * 60 * syncInterval );
			//}
			//mLog.serviceState(TAG + " getlatest()", "receieved successful run of getFeeds()");
			mPreferencesEditor.putInt("syncfail", 0);
			mPreferencesEditor.putLong("syncend", System.currentTimeMillis());
			mPreferencesEditor.commit();
			
			//int total = mSharedPreferences.contains("total") ? mSharedPreferences.getInt("total",0) : 0;
			//if( total == 0 ){
				
			//}
		}else if(getFeedReply == 2){ // Captured Issue, Memory failure
			//mLog.serviceState(TAG + " getlatest()", "received Memory Failure from getFeeds()");
			mLog.e(TAG, "getlatest() 146 Memory Issue getFeedReply("+getFeedReply+")");
			int failcnt = mSharedPreferences.contains("syncfail") ? mSharedPreferences.getInt("syncfail",0) : 0;
			failcnt++;
			mPreferencesEditor.putInt("syncfail", failcnt);
			mPreferencesEditor.commit();
			//if( failcnt < 3 ){
				//mHandler.postDelayed( this, 1000 * 5 );
			//}else{
				//mLog.e(TAG, "getlatest() 155 Memory Issue but won't retry because failure("+failcnt+") count is over 3 getFeedReply("+getFeedReply+")");	
			//}
			
		}else{ // Unhandled reply
			//mLog.serviceState(TAG + " getlatest()", "Unhandled reply from getFeeds()");
			mLog.e(TAG, "getlatest() 148 getFeedReply("+getFeedReply+")");
		}
  	  	

		mLog.i(TAG,"getlatest() 88 done");
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		mLog.i(TAG,"onConfigurationChanged() +++++++++++++++++++++++++++");
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onLowMemory() {
		mPreferencesEditor.putLong("lowmemory", System.currentTimeMillis());
		mPreferencesEditor.commit();
		mLog.e(TAG,"onLowMemory() threads("+Thread.activeCount()+") memory free("+Runtime.getRuntime().freeMemory()+") max("+Runtime.getRuntime().maxMemory()+") available("+Runtime.getRuntime().totalMemory()+")");
		//mPreferencesEditor.putLong("lowmemory", System.currentTimeMillis()).commit();
		
		Intent resetservice = new Intent();
        //com.havenskys.thescoopseattle.SERVICE_RESET
		resetservice.setAction("com.havenskys.thescoopseattle.SERVICE_RECOVER2");
		//PendingIntent service3 = PendingIntent.getActivity(mContext, 0, resetservice, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_CANCEL_CURRENT);
		PendingIntent service4 = PendingIntent.getBroadcast(mContext, 76, resetservice, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_CANCEL_CURRENT);
		//mAlM.set(AlarmManager.RTC_WAKEUP,( System.currentTimeMillis() + (60 * 1000) ), service3);
		Date d = new Date(); 
		if( d.getHours() < 20 && d.getHours() > 8 ){
			mAlM.set(AlarmManager.RTC_WAKEUP, ( System.currentTimeMillis() + (30 * 1000) ), service4);
			mLog.serviceState(TAG + " handleMessage()", "Low memory, retrying in 30 seconds.");
		}else{
			mAlM.set(AlarmManager.RTC_WAKEUP, ( System.currentTimeMillis() + (5 * 60 * 1000) ), service4);
			mLog.serviceState(TAG + " handleMessage()", "Low memory, retrying in 300 seconds.");
		}
		
		//mServiceHandler.sendEmptyMessageDelayed(-2, 60 * 1000);
		//mServiceHandler.postDelayed(this, 60 * 1000);
		//SystemClock.sleep(60000);
		//mPreferencesEditor.putLong("lowmemory", System.currentTimeMillis()).commit();
		
		super.onLowMemory();
		//stopSelf();
		
		//mLog.serviceState(TAG + " onLowMemory()","exiting by system request free memory("+Runtime.getRuntime().freeMemory()+")");
		
		
		//finalize();
		//exit();
	}

	@Override
	public void onRebind(Intent intent) {
		mLog.i(TAG,"onRebind() ++++++++++++++++++++++++++++++");
		super.onRebind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		mLog.i(TAG,"onUnbind() ++++++++++++++++++++++++++++");
		return super.onUnbind(intent);
	}
	

	@Override
    public IBinder onBind(Intent intent) {
        // Select the interface to return.  If your service only implements
        // a single interface, you can just return it here without checking
        // the Intent.
        if (IRemoteService.class.getName().equals(intent.getAction())) {
            return mBinder;
        }
        if (ISecondary.class.getName().equals(intent.getAction())) {
            return mSecondaryBinder;
        }
        return null;
    }

    /**
     * The IRemoteInterface is defined through IDL
     */
    private final IRemoteService.Stub mBinder = new IRemoteService.Stub() {
        public void registerCallback(IRemoteServiceCallback cb) {
            if (cb != null) mCallbacks.register(cb);
        }
        public void unregisterCallback(IRemoteServiceCallback cb) {
            if (cb != null) mCallbacks.unregister(cb);
        }
    };

    /**
     * A secondary interface to the service.
     */
    private final ISecondary.Stub mSecondaryBinder = new ISecondary.Stub() {
        public int getPid() {
            return Process.myPid();
        }
        public void basicTypes(int anInt, long aLong, boolean aBoolean,
                float aFloat, double aDouble, String aString) {
        }
    };

	


	
}
