package com.havenskys.thescoopseattle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Customize extends Activity {

	private static final String TAG = "Customize";
	private SharedPreferences mSharedPreferences;
	private Editor mPreferencesEditor;
	private Custom mLog;
	private ImageView mUpgrade;
	private TextView mCodeLabel;
	private EditText mCode;
	private Context mContext;
	
	private Handler mUpdateCode = new Handler() {
		public void handleMessage(Message msg) {
	    	Bundle b = msg.getData();
	    	String text = b.containsKey("text") ? b.getString("text") : "";
	    	if( mCode.length() == 0 && !mCode.hasFocus()){ mCode.setText(text); mCode.setTextColor(Color.GRAY); mCode.setTag("enter"); }
    	}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().requestFeature(Window.FEATURE_LEFT_ICON);
		this.setContentView(R.layout.upgrade);
		//this.getWindow().setFeatureDrawable(Window.FEATURE_LEFT_ICON, getResources().getDrawable(R.drawable.docdot));
		//this.setTitle("Customize");
		mContext = this;
		
		mLog = new Custom(this, TAG + " onCreate() 22");
		mSharedPreferences = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
		mPreferencesEditor = mSharedPreferences.edit();
		mLog.setSharedPreferences(mSharedPreferences, mPreferencesEditor);
		
		mUpgrade = (ImageView) findViewById(R.id.upgrade_image);
		mCodeLabel = (TextView) findViewById(R.id.upgrade_text);
		mCode = (EditText) findViewById(R.id.upgrade_code);
		
		
		
		//double load = mLog.getload(TAG + " onCreate() 25");
		String upgrades = mSharedPreferences.getString("upgrades", "");
		
		String body = "";
		body += "<html>\n<body text=#f0f0f0 bgcolor=#303030 link=#0066cc vlink=#0066cc>\n";
		
		if( !upgrades.contains("Customize to Full Version.") ){
			
		}else{
			//
			
		}
		
		
		
		body += "\n";
		body += "\n";
		body += "\n";
		body += "</body></html>\n";
		
		//view.loadData(body, "text/html", "UTF-8");
		//view.loadData(body, "text/html", "US-ASCII");
		
		//WebView view = (WebView) findViewById(R.id.upgrade_view);
		//view.getSettings().setJavaScriptEnabled(true);
		//view.getSettings().setSupportZoom(true);
		//view.loadData("<html><body><a href=>Purchase an Customize</a></body></html>", "text/html", "UTF-8");
		
		//if( upgrades.trim().length() > 0 ){
		String newtext = "Press the CUSTOMIZE button to go to the online order form.";
		mCode.postDelayed(new Runnable(){

			public void run() {
				Bundle b = new Bundle();
				Message m = new Message();
				b.putString("text", "Enter your activation code.");
				m.setData(b);
				mUpdateCode.sendMessageDelayed(m, 300);
			}
			
		}, 500);
		if( upgrades.length() > 0 ){
			newtext += "\n\nCurrent Upgrades:" + upgrades ;
		}else{
			newtext += "\n\nNo upgrades added yet, cheer up, you'll get one soon. Ask a friend what makes them special.";
		}
		mCodeLabel.setTextSize(18);
		mCodeLabel.setShadowLayer((float)2, (float)1.5, (float)1.5, Color.BLACK);
		mCodeLabel.setText(newtext);
		//}else{
			//mCodeLabel.setText("Press the purple UPGRADE button to order your personalized.\nEnter your activation code below and it will be recognized automatically if it is correct.");
		//}
		
		mCode.setOnFocusChangeListener(new OnFocusChangeListener(){
			public void onFocusChange(View v, boolean hasFocus) {
				if( hasFocus ){
					if( v.getTag() == "enter" ){
						v.setTag("");
						mCode.setText("");
						mCode.setTextColor(Color.BLACK);
					}
				}else{
					
				}
			}
		});
		
		mCode.setOnKeyListener(new OnKeyListener(){

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if( event.getAction() == KeyEvent.ACTION_UP ){
					String text = mCode.getText().toString().trim();
					if( text.compareToIgnoreCase("In pursuit of peace, freedom, and happiness.") == 0 ){
						String upgrades = mSharedPreferences.getString("upgrades", "");
						if( upgrades.contains("Customize to Full Version.") ){
							//mPreferencesEditor.putBoolean("Full Version", false);
							mPreferencesEditor.putString("upgrades", upgrades.replaceAll("\nUpgrade to Full Version.","") );
							mPreferencesEditor.commit();
							Toast.makeText(mContext, "Customize \"Full Version\" removed.", 1880 * 6).show();
						}else{
							//mPreferencesEditor.putBoolean("Full Version", true);
							mPreferencesEditor.putString("upgrades", upgrades + "\nUpgrade to Full Version.");
							mPreferencesEditor.commit();
							Toast.makeText(mContext, "Upgrading to Full Version", Toast.LENGTH_LONG).show();
						}
						upgrades = mSharedPreferences.getString("upgrades", "");
						if(upgrades.trim().length() > 0 ){
							mCodeLabel.setText("Current Upgrades:" + upgrades);
							mCode.setText("");
						}else{
							mCodeLabel.setText("You have disabled all your upgrades. If this isn't correct click the activation code area below to reinstate it.");
						}
					} else if( text.compareToIgnoreCase("ryry") == 0 ){
						String upgrades = mSharedPreferences.getString("upgrades", "");
						if( upgrades.contains("RyRy awesome.") ){
							mPreferencesEditor.putString("upgrades", upgrades.replaceAll("\nRyRy awesome.","") );
							mPreferencesEditor.commit();
							Toast.makeText(mContext, "Customization RyRy removed.", Toast.LENGTH_LONG).show();
						}else{
							mPreferencesEditor.putString("upgrades", upgrades + "\nRyRy awesome.");
							mPreferencesEditor.commit();
							Toast.makeText(mContext, "Customizer by RyRy.", Toast.LENGTH_LONG).show();
						}
						upgrades = mSharedPreferences.getString("upgrades", "");
						if(upgrades.trim().length() > 0 ){
							mCodeLabel.setText("Current Upgrades:" + upgrades);
							mCode.setText("");
						}else{
							mCodeLabel.setText("You have disabled all your upgrades. If this isn't correct click the activation code area below to reinstate it.");
						}
					}
					
					
				}
				return false;
			}
			
		});
		
		mUpgrade.setOnFocusChangeListener(new OnFocusChangeListener(){
			public void onFocusChange(View v, boolean hasFocus) {
				if( hasFocus ){
					mUpgrade.setImageResource(R.drawable.customize_selected);
				}else{
					mUpgrade.setImageResource(R.drawable.customize);
				}
			}
		});
		
		mUpgrade.setOnTouchListener(new OnTouchListener(){

			public boolean onTouch(View v, MotionEvent event) {
				if( event.getAction() == MotionEvent.ACTION_DOWN ){
					mUpgrade.setImageResource(R.drawable.customize_selected);
				}else if( event.getAction() == MotionEvent.ACTION_UP ){
					mUpgrade.setImageResource(R.drawable.customize);
				}
				return false;
			}
			
		});
		
		mUpgrade.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				
				Toast.makeText(mContext, "Opening The Scoop Customize Webpage. Billing is handled by Google.", 10 * 1000 ).show();
				Uri upgradeUri = Uri.parse("http://www.skysfamily.com/TheScoop/Customize.html");
				Intent jump = new Intent(Intent.ACTION_VIEW, upgradeUri);
				startActivity(jump);
			}
			
		});
		
	}
	
	
}
