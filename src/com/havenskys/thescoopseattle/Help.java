package com.havenskys.thescoopseattle;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;

public class Help extends Activity {
	
	private static final String TAG = "Help";
	private SharedPreferences mSharedPreferences;
	private Editor mPreferencesEditor;
	private Custom mLog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().requestFeature(Window.FEATURE_LEFT_ICON);
		this.setContentView(R.layout.help);
		this.getWindow().setFeatureDrawable(Window.FEATURE_LEFT_ICON, getResources().getDrawable(android.R.drawable.ic_menu_help));
		this.setTitle("Help");
		
		mLog = new Custom(this, TAG + " onCreate() 22");
		mSharedPreferences = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
		mPreferencesEditor = mSharedPreferences.edit();
		mLog.setSharedPreferences(mSharedPreferences, mPreferencesEditor);
		
		
		
		WebView view = (WebView) findViewById(R.id.help_view);
		view.getSettings().setJavaScriptEnabled(true);
		view.getSettings().setSupportZoom(true);
		
		double load = mLog.getload(TAG + " onCreate() 25");
		
		String body = "";
		body += "<html><body text=#f0f0f0 bgcolor=#303030 link=#0066cc vlink=#0066cc>";
		body += "<h2>Doc Chomps Software</h2>\n";
		body += "Support is available via email at <a href=\"mailto:havenskys@gmail.com?Subject='Support Request: "+Custom.APP+"'\">havenskys@gmail.com</a> or through the application menu system.";
		body += "<h3>Description</h3>\nThis software is intend to provide locally available data, integrated into the Android interface, while synchronizing of the data takes place in the background.  With this application you can read the information you find interesting as though you have already downloaded the content, because this application's service does it for you.\n";
		//body += "<h3>Menu: View Source Webpage</h3>\nThis will send you to the same page where the content is being gathered from. <a href=\"" + Custom.BASEURL + "\">" + Custom.BASEURL + "</a>\n";
		//body += "<h3>Menu: Email "+ Custom.WHO +"</h3>\nThis will prepare an email for you to "+Custom.WHO+" at their email address <a href=\"mailto:"+Custom.EMAIL+"\">"+Custom.EMAIL+"</a>.\n";
		body += "<h3>Menu: About DCS</h3>\nThis presents dialog related to Doc Chomps Software and the team who has written this software.\n";
		body += "<h3>Menu: Help</h3>\nThis information is intented to be an overview description of this application, it's function, configuration, and use.\n";
		body += "<h3>Menu: Email Support</h3>\nThis will prepare an email for you to Support at the email address havenskys@gmail.com.\n";
		body += "<h3>Menu: Synchronization Interval</h3>\nYes, synchronization isn't in the dictionary but it should be.  This multiple choice list of times when you want to check for new content.\n";
		body += "<h3>Menu: Acceptable Load</h3>\nConfused, it's okay, this is new to some people.  This number represents the number of processes waiting to be run, I like my systems under 0 but Android runs hot at around 3.  Around 5 is when you see your phone hang for a second.  If your phone is never lower than your setting it will run anyways as soon as the load slows down sometime after 30 seconds.  If your phone become busy for any reason, the background process will slow down for you, letting everyone cool off.  Having this set too low will slow down the entire download process so keep that in mind.  4 is the default, it's reasonable.  Your load as of opening this dialog is shown at the bottom of this document.\n";
		body += "<h3>Menu: Vibrate</h3>\nWe've provided a few options in vibration notification i.e. buzz(+), buzz(+), wait(_), buzz(+) or the always popular buzz(+) buzz(+) :). You will only be buzzed on the first new record.\n";
		body += "<h3>Article Menu: View Article Link</h3>\nThis will send you to the official web page the article is associated with.\n";
		body += "<h3>Article Menu: Forward</h3>\nThis will prepare an email for you to forward the article title and link.\n";
		body += "<h3>Bottom Touch Pad: Colors</h3>\n";
		body += "Red: Sources haven't been checked recently or completed successfully.<br>\n";
		body += "Orange: Your Android's load is above the acceptable load, your CPU is really busy and the interface is probably slow.<br>\n";
		body += "Yellow: Your Android is reporting a low memory (RAM) condition, possibly restarting your device will help clear up space.<br>\n";
		body += "Green: Download/Synchronization activity is occuring, if it changes from Red, Orange, or Yellow to Green a service restart was requested.<br>\n";
		body += "Blue: Everything is running in a healthy manner.<br>\n";
		body += "Purple: Everything is verified to have been downloaded as requested and everything is healthy.<br>\n";
		body += "<h2>System Info</h2>\n";
		body += "Current Load: " + load;
		body += "</body></html>";
		view.loadData(body, "text/html", "UTF-8");
	}

	
}
