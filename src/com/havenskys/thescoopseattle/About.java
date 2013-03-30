package com.havenskys.thescoopseattle;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;

public class About extends Activity {

	private final static String TAG = "About";
	private Custom mLog;
	private SharedPreferences mSharedPreferences;
	private Editor mPreferencesEditor;
	
	//private Process mLoadProcess;
	//private InputStream mLoadStream;
	private byte[] mLoadBytes;
	//private String[] mLoadParts;
	//private long mLoadStart;
	private int mLoadReadSize;
	//private double mLoadDouble;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().requestFeature(Window.FEATURE_LEFT_ICON);
		this.setContentView(R.layout.about);
		this.getWindow().setFeatureDrawable(Window.FEATURE_LEFT_ICON, getResources().getDrawable(R.drawable.docdot));
		this.setTitle("About");
		//this.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	
		mLog = new Custom(this, TAG + " onCreate() 34");
		mSharedPreferences = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
		mPreferencesEditor = mSharedPreferences.edit();
		mLog.setSharedPreferences(mSharedPreferences, mPreferencesEditor);
		
		
		
		
		
		WebView view = (WebView) findViewById(R.id.about_view);
		view.getSettings().setJavaScriptEnabled(true);
		view.getSettings().setSupportZoom(true);
		
		double load = mLog.getload(TAG + " onCreate() 48");
		
		String body = "";
		body += "<html><body background=\"file:///android_asset/grayback.png\" text=#f0f0f0 bgcolor=#303030 link=#0066cc vlink=#0066cc alink=#0066CC>";
		body += "<h2>Doc Chomps Software</h2>\n";
		body += "This software was written by Haven Skys in Seattle, WA and Northern Minnesota.  You may contact me for a custom solution at havenskys@gmail.com, mention 'Custom Solution Request' in your email subject.  There is more information about this application in 'Help'.";
		body += "<br>Doc Chomps is my American Pitbull/Boxer companion.  Super smart, tiger stripped, and more friendly than I am.";
		body += "<h2>System Info</h2>\n";
		body += "Current Load: " + load + "<br>\n<br>\n";
		
		String servicehistory = mSharedPreferences.getString("servicehistory", "");
		body += "<h2>Service History</h2>\n";
		body += servicehistory.replaceAll("\n", "<br>\n");
		
		try {
		//*
		{
			mLog.w(TAG,"Getting MEMINFO");
			Process top = Runtime.getRuntime().exec("cat /proc/meminfo");
			top.waitFor();
			InputStream topstream = top.getInputStream();
			mLoadBytes = new byte[1024 * 2];
			mLoadReadSize = topstream.read(mLoadBytes, 0, (1024*2)-1);
			mLog.w(TAG,"MEMINFO " + new String(mLoadBytes).trim() );
			body += "<h3>Memory Info</h3>\n";
			body += new String(mLoadBytes).trim().replaceAll("\n", "<br>\n");
		}
		//*/
		
		//*
		if( false ){
			mLog.w(TAG,"Getting /bin");
			Process top = Runtime.getRuntime().exec("cat /proc/stats");
			top.waitFor();
			InputStream topstream = top.getInputStream();
			mLoadBytes = new byte[1024 * 10];
			mLoadReadSize = topstream.read(mLoadBytes, 0, (1024*10)-1);
			//mLog.w(TAG,"MEMINFO " + new String(mLoadBytes).trim() );
			body += "<h3>which cat</h3>\n";
			body += new String(mLoadBytes).trim().replaceAll("\n", "<br>\n");
		}
		//*/
		
		//*
		if(false){
			mLog.w(TAG,"Getting PROC");
			Process top = Runtime.getRuntime().exec("ls -l /proc");
			top.waitFor();
			InputStream topstream = top.getInputStream();
			mLoadBytes = new byte[1024 * 10];
			mLoadReadSize = topstream.read(mLoadBytes, 0, (1024*10)-1);
			String[] proclist = new String(mLoadBytes).trim().split("\n");
			body += "<h3>Proc list</h3>\n<ul>";
			//body += new String(mLoadBytes).trim().replaceAll("\n", "<br>\n");
			for(int i = 0; i < proclist.length; i++){
				mLog.w(TAG,"PROC: " +  proclist[i].trim() );
				body += "<li>"+ proclist[i].trim();
				if( proclist[i].trim().contains("self") ){
					//for(i++; i < proclist.length; i++){
						Process file = Runtime.getRuntime().exec("ls /proc/"+proclist[i].trim());
						file.waitFor();
						InputStream filestream = file.getInputStream();
						mLoadBytes = new byte[1024];
						mLoadReadSize = filestream.read(mLoadBytes, 0, 1023);
						String[] filelist = new String(mLoadBytes).trim().split("\n");
						for(int c = 0; c < filelist.length; c++){
							//mLog.w(TAG,"FILE /proc/"+proclist[i].trim() + ": " +  filelist[c].trim() );
							body += "FILE /proc/"+proclist[i].trim() + ": " +  filelist[c].trim() + "<br>\n";
						}
					//}
					break;
				}
				body += "</li>\n";
			}
			body += "</ul>\n";
		}
		//*/
		
		} catch (InterruptedException e) {
			mLog.e(TAG,"Load InterruptedException");
			e.printStackTrace();
		} catch (IOException e) {
			mLog.e(TAG,"Load IOException");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		body += "</body></html>";
		view.loadData(body, "text/html", "UTF-8");
	}

	
}
