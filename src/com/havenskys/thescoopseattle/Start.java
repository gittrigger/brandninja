package com.havenskys.thescoopseattle;

import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TabActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

public class Start extends TabActivity {
	
	private static String TAG = "Start";
	
	private TabHost mTabHost;
	private Bundle mIntentExtras;
	private NotificationManager mNM;
	//private Handler mHandler;
	
	private SharedPreferences mSharedPreferences;
	private Editor mPreferencesEditor;
	private Custom mLog;
	private ImageView mCover, mTitle, mConsole, mAction, mClose, mCustomize;
	private LinearLayout mActionBar;
	private TextView mText;
	private Context mContext;
	private ContentResolver mResolver;
	private boolean mLandscape = false;
	private Handler mHandler;
	private Thread mThread;
	
	private Handler mClearScreen = new Handler() {
	    public void handleMessage(Message msg) {
	    	//Bundle b = msg.getData();
	    	//String text = b.containsKey("text") ? b.getString("text") : "";
	    	mLog.e(TAG, "(positive error) mClearScreen Handle running");
	    	mText.setText("");
	    	if( mConsole.getVisibility() == View.VISIBLE ){
	    		mPreferencesEditor.putLong("computerclose", System.currentTimeMillis() );
	    		mPreferencesEditor.putLong("computer", 0);
	    		mPreferencesEditor.putLong("computerpause", 0);
	    		mPreferencesEditor.putLong("computerplay", 0);
				mPreferencesEditor.commit();
	    		mCustomize.setVisibility(View.GONE);
	    		mActionBar.setVisibility(View.GONE);
	    		mText.setVisibility(View.GONE);
		    	mConsole.setVisibility(View.GONE);
		    	mTitle.setVisibility(View.GONE);
		    	mCover.setVisibility(View.GONE);
	    	}
	    }
	};
	
	private long mLastUpdateConsole = 0;
	private Handler mUpdateConsole = new Handler() {
	    public void handleMessage(Message msg) {
	    	Bundle b = msg.getData();
	    	String text = b.containsKey("text") ? b.getString("text") : "";
	    	mLog.e(TAG, "(positive error) mUpdateConsole Handle received text("+text+")");
	    	long computer = mSharedPreferences.getLong("computer", 0);
	    	long close = mSharedPreferences.getLong("computerclose", 0);
	    	
	    	if( computer == 0 && close > 0 ){
	    		mText.setText("{computer close}");
	    		return;
	    	}
	    	long lastdiff = (System.currentTimeMillis() - mLastUpdateConsole); 
	    	if( lastdiff < (1500) ){
	    		mLog.e(TAG, "mUpdateConsole Handle last spoke("+lastdiff+" < 1500) delaying("+(1500-lastdiff)+")");
	    		SystemClock.sleep( (int)(1500-lastdiff) );	
	    	}
	    	mText.setText(text);
	    	
	    	
	    	
	    	if( close < (System.currentTimeMillis() - 30 * 1000) && mConsole.getVisibility() == View.GONE ){
	    		mPreferencesEditor.putLong("computerpause",0);
	    		mPreferencesEditor.putLong("computerclose",0);
	    		mPreferencesEditor.putLong("computerplay", System.currentTimeMillis());
	    		mPreferencesEditor.commit();
	    		
		    	mCover.setVisibility(View.VISIBLE);
		    	mTitle.setVisibility(View.VISIBLE);
		    	mConsole.setVisibility(View.VISIBLE);
		    	mText.setVisibility(View.VISIBLE);
		    	
		    	mActionBar.setVisibility(View.VISIBLE);
		    	if( System.currentTimeMillis()%3 == 0 ){
		    		mCustomize.setVisibility(View.VISIBLE);
		    	}
		    	//mCover.requestFocusFromTouch();
		    	//mAction.requestFocusFromTouch();
	    	}
	    	if( !mCustomize.hasFocus() && !mClose.hasFocus() && !mAction.hasFocus() ){
	    		mAction.requestFocus();
	    	}
	    }
	};
	
	private String mLastText = "";
	private Handler mConsoleActionFocus = new Handler() {
	    public void handleMessage(Message msg) {
	    	mAction.requestFocus();
	    }
	};
	private Handler mConsoleHandler = new Handler() {
	    public void handleMessage(Message msg) {
	    	mLog.e(TAG, "mConsoleHandler running Thread mConsoleCursor whatmsg("+msg.what+")");
	    	
	    	//mConsoleCursor.run();
	    	//if( !mConsoleCursor.isAlive() )
	    		//mConsoleCursor.start();
	    	
	    	long computertime = mSharedPreferences.getLong("computer", -1);
	    	mPreferencesEditor.putLong("computer", System.currentTimeMillis());
			mPreferencesEditor.commit();
			if( computertime > 0 ){
				long computerseconds = (System.currentTimeMillis() - computertime)/1000;
	    		if( computerseconds > 2 ){
	    			mLog.e(TAG, "mConsoleHandler running mConsoleCursor through another thread start.");
	    			Thread jumpThread = new Thread(){ public void run(){ mConsoleCursor.run(); } };
	    	    	jumpThread.start();
	    		} else {
	    			mLog.e(TAG, "mConsoleHandler refused to start computer it was reporting "+computerseconds+" seconds ago.");
	    			//mClearScreen.sendEmptyMessage(11);
	    		}
			}else{
				mLog.e(TAG, "mConsoleHandler running mConsoleCursor through another thread start.");
    			Thread jumpThread = new Thread(){ public void run(){ mConsoleCursor.run(); } };
    	    	jumpThread.start();
			}
    	
	    }
	};
	
	private Thread mConsoleCursor = new Thread(){
		private Message textMessage, pauseMessage;
		private Bundle textBundle, pauseBundle;
		private void sendMessage(String text, int sleepms){
			if( text == "" && mLastText == "" ){
				return;
			}
			long close = mSharedPreferences.getLong("computerclose", 0);
			if( close > System.currentTimeMillis() - 30000 ){
				mLog.e(TAG, "mConsoleCursor sendMessage() exiting because close was requested ("+(System.currentTimeMillis() - close)+").");
				SystemClock.sleep(300);
				mConsoleCursor.interrupt();
				return;
			}
			mPreferencesEditor.putLong("computer", System.currentTimeMillis());
			mPreferencesEditor.commit();
			mLastText = text;
			textBundle = new Bundle();
	    	textBundle.putString("text", text);
	    	textMessage = new Message();
			textMessage.setData(textBundle);
			//mUpdateConsole.sendMessage(m);
			mUpdateConsole.sendMessageDelayed(textMessage, 100);
			SystemClock.sleep(sleepms);
			long pause = mSharedPreferences.getLong("computerpause", 0);
			
			
				
			//Message pauseMessage;
			//Bundle pauseBundle;
			while( pause > 0 ){
				mConsoleActionFocus.sendEmptyMessageDelayed(10, 100);
				
				textBundle = new Bundle();
		    	textBundle.putString("text", text);
		    	textMessage = new Message();
				textMessage.setData(textBundle);
				mUpdateConsole.sendMessageDelayed(textMessage, 100);
				SystemClock.sleep(sleepms);
				
				close = mSharedPreferences.getLong("computerclose", 0);
				if( close > 0 ){finish();}
				
				pause = mSharedPreferences.getLong("computerpause", 0);
				if( pause > 0 ){
					pauseMessage = new Message();
					pauseBundle = new Bundle();
					pauseBundle.putString("text", "[computer paused]");
					pauseMessage.setData(pauseBundle);
					mUpdateConsole.sendMessageDelayed(pauseMessage,100);
					SystemClock.sleep(1880);
					pause = mSharedPreferences.getLong("computerpause", 0);
					
					close = mSharedPreferences.getLong("computerclose", 0);
					if( close > 0 ){finish();}
				}
				
				
			}
		}
		private int getTotal(){
			int total = 0;
			int prevtotal = mSharedPreferences.getInt("total", 0);
			
			Cursor db = null;
			db = SqliteWrapper.query(mContext, mResolver, DataProvider.CONTENT_URI, new String[] {"count(*)"} , "status > 0", null, null);
			// and (read == 0 or read > "+(System.currentTimeMillis() - (5 * 60 * 1000) )+") and ( seen == 0 or seen > "+(System.currentTimeMillis() - (5 * 60 * 1000) )+" )"
			if( db != null ){ if( db.moveToFirst() ){ total = db.getInt(0); } db.close(); }
							
			//i(TAG, "getFeeds() 995 name("+longname+") RecordCount("+totalCount+")");
			if( total > prevtotal ){
				mPreferencesEditor.putInt("total", total);
				mPreferencesEditor.commit();
			}
			return total;
		}
		public void run(){
			boolean resetConsole = false;
			boolean consoleStartup = false;
			
			for(;;){
				
				long computer = mSharedPreferences.getLong("computer", 0);
		    	long close = mSharedPreferences.getLong("computerclose", 0);
		    	
		    	if( computer == 0 && close > 0 ){
		    		//mText.setText("{computer close}");
		    		return;
		    	}
		    	
				mPreferencesEditor.putLong("computer", System.currentTimeMillis());
				mPreferencesEditor.commit();
				SystemClock.sleep(30);
				//if( mCover.getVisibility() == ImageView.GONE ){
					//mLog.e(TAG, "Sleeping thread for 5 seconds. pid:" + android.os.Process.myTid() );
					//SystemClock.sleep(2000);
					//continue;
				//}

				
				if( resetConsole ){
					sendMessage("",1000);
					//SystemClock.sleep(1000);
					resetConsole = false;
				}
				
				resetConsole = true;
				
				{
					//sendMessage("Test " + android.os.Process.getElapsedCpuTime());
					/*/
					Bundle b = new Bundle();
			    	b.putString("text", "Test " + android.os.Process.getElapsedCpuTime() );
			    	Message m = new Message();
					m.setData(b);
					//mUpdateConsole.sendMessage(m);
					mUpdateConsole.sendMessageDelayed(m, 100);
					//*/
					//resetConsole = true;
					//SystemClock.sleep(1880);
				}
				
				int sisStart, sisEnd, sisLast, sisMemory, sisCPU, sisUNX, sisHPT, sisRST, sisLRN;
				
				Date d = new Date();
				int hour = d.getHours();
				
				int speakspeed = mSharedPreferences.getInt("speakspeed", 1880 * 3);
				
				//int total = mSharedPreferences.getInt("total", 0);
				
				long lastrun = mSharedPreferences.getLong("lastrun", -1);
				
				int runcount = mSharedPreferences.getInt("computerruncount", 0);
				mPreferencesEditor.putInt("computerruncount", ++runcount);
				mPreferencesEditor.commit();
				
				//mPreferencesEditor.putInt("computerruncount", runcount+1);
				//mPreferencesEditor.commit();
				
				//SystemClock.sleep(300);
				{	
					//String LOADING_RECORDS = "Loading records\nfor the first time.";
					String THANKYOU = "Thank you\nfor checking us out.";
					String UPGRADE0 = "If you like different, order a customization...";
					String UPGRADE1 = "";
					if( System.currentTimeMillis()%100 > 1 ){
						UPGRADE1 = "create\nsoftware\nwithout writing code\nonly 10 bucks...";
					}else{
						UPGRADE1 = "invent\nwithout writing code\nit's only 10 bucks...";
					}
					String UPGRADE2 = "and available in every future publication.";
					//String UPGRADE2 = "your preference of function and design available in every future publication of this software.";
				
			
					int total = getTotal();
			
					if( (total == 0 || consoleStartup) || runcount == 1 ){
						consoleStartup = true;
						
						//SystemClock.sleep(speakspeed);
						//if( System.currentTimeMillis()%100 > 1 ){ sendMessage(THANKYOU, speakspeed); }
						if( runcount == 1 ){
							
							if( System.currentTimeMillis()%100 > 1 ){ sendMessage(THANKYOU, speakspeed); }
							
							if( System.currentTimeMillis()%3 == 0 ){
								//SystemClock.sleep(speakspeed);
								sendMessage(UPGRADE0, speakspeed);
								//SystemClock.sleep(speakspeed);
								sendMessage(UPGRADE1, speakspeed);
								//SystemClock.sleep(speakspeed);
								sendMessage(UPGRADE2, speakspeed);
								//SystemClock.sleep(speakspeed);
							}
						}
						
						long syncstart = mSharedPreferences.getLong("syncstart", -1);
						if( syncstart > 0 ){ sisStart = (int) ((System.currentTimeMillis() - syncstart)/1000); }else{sisStart = -1;}
						if( sisStart > -1 ){
							sendMessage("Download service started " + sisStart + " seconds ago.",speakspeed);
						}
						
						total = getTotal();
						
						if( total == 0 ){
							if( runcount == 1 ){
								sendMessage("Loading records\nfor the first time.",speakspeed);
								SystemClock.sleep(8000);
								continue;
							} else if( runcount == 6 ){ sendMessage("Loading Records\nStill no records, odd.",speakspeed); SystemClock.sleep(1000); continue;
							} else if( runcount == 5 ){ sendMessage("Loading Records\nStill no records?!",speakspeed); SystemClock.sleep(8000); continue;
							} else if( runcount == 4 ){ sendMessage("Loading Records\nSometimes this takes a while.",speakspeed); SystemClock.sleep(8000); continue; 
							} else if( runcount == 3 ){ sendMessage("Loading Records\nStill working.",speakspeed); SystemClock.sleep(8000); continue;
							} else if( runcount == 2 ){ sendMessage("Loading Records\n",speakspeed); SystemClock.sleep(8000); continue;
							//} else if( runcount > 10 ){ sendMessage("Loading Records\nIt's taking a while.",speakspeed);
							//} else if( runcount > 9 ){ sendMessage("Loading Records\n",speakspeed);
							
							} else if( runcount > 10 && runcount%10 == 1 ){ sendMessage("Still no records?\nMaybe contact havenskys@gmail.com?",speakspeed); continue;
							} else {
								long lastnetwork = mSharedPreferences.getLong("diagnostic_activenetwork",-1);
								boolean connectivity = false;
								
								if( lastnetwork > System.currentTimeMillis() - 60 * 1000 ){
									//connectivity = true;
								}else{
									ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Service.CONNECTIVITY_SERVICE);
									NetworkInfo[] ni = cm.getAllNetworkInfo();
									for( int i = 0; i < ni.length; i++){
										if( ni[i].isConnected() ){
											connectivity = true; break; }
										if( ni[i].isConnectedOrConnecting() ){
											// Maybe we should sleep here instead and try again.
											connectivity = true; break; }
										if( ni[i].isAvailable() ){
											connectivity = true; break; }
									}
									if( connectivity ){
										mPreferencesEditor.putLong("diagnostic_activenetwork", System.currentTimeMillis());
										mPreferencesEditor.commit();
									}
	
									if( connectivity ){
										sendMessage("Loading Records\nYou have an active network.",speakspeed);
									}else{
										
										long sisLastNetwork = 0;
										if( lastnetwork > -1 ){ sisLastNetwork = (int) ((System.currentTimeMillis() - lastnetwork)/1000); }else{sisLastNetwork = -1;}
										
										if( sisLastNetwork == -1 ){
											sendMessage("Loading Records\nNo active network ever detected.\n",speakspeed);
										}else if( sisLastNetwork > 72 * 60 ){
											long days = (sisLastNetwork/(24*60));
											sendMessage("Loading Records\nNo active network for\n"+days+" days.",speakspeed);
										}else if( sisLastNetwork > 5 * 60 ){
											long hours = (sisLastNetwork/60);
											sendMessage("Loading Records\nNo active network for\n"+hours+" hours.",speakspeed);
										}else if( sisLastNetwork > 1 ){
											sendMessage("Loading Records\nNo active network for\n"+sisLastNetwork+" minutes.",speakspeed);
										}else if( sisLastNetwork == 0 ){
											sendMessage("Loading Records\nNetwork connectivity established now.",speakspeed);
										}
										continue;
									}
								}
							}
						}
						

						//consoleStartup = false;
						
						total = getTotal();
						
						if( total > 0 ){
							consoleStartup = false;
							if( System.currentTimeMillis()%2 == 0 ){
								sendMessage("Okay folks,\n" + total + " records ready and more coming in.", speakspeed);
							}else{
								sendMessage("" + total + " records ready and more coming.", speakspeed);
							}
							//SystemClock.sleep(speakspeed);
							if( System.currentTimeMillis()%3 == 0 ){
								sendMessage(".\nEnjoy.", speakspeed);
							}
							//SystemClock.sleep(speakspeed);
							mClearScreen.sendEmptyMessageDelayed(6, 100);
							break;
						}
					}
				}
				/*/
				if(httppagetime > 0 && (System.currentTimeMillis() - httppagetime) < 30000 ){
					say += "\ndownloading";
				}
				else if( sisLast > 0 ){
					if( sisLast > 30 ){
						//mSay += " and more coming in slowly\nas recent as " + sisLast + " seconds ago.";
						// Let other checks look at this.
					}else if( sisLast > 20 ){
						say += " and more coming in slowly\nas recent as " + sisLast + " seconds ago";
					}else{
						say += " and more coming in\nas recent as " + sisLast + " seconds ago";
					}
				}//*/
				
				
				
				
				
				
				
				
				
				
				sendMessage("",1000);
				speakspeed = mSharedPreferences.getInt("speakspeed", 1880 * 3);
				

				int syncInterval = mSharedPreferences.getInt("sync", 31);
				
				{
					int successcnt = 0;
					int failcnt = 0;
					String faillist = "";
					long oldest = 0;
					for( int feedid = 0; feedid < mLog.dataFeed.length; feedid++ ){
						
			        	long lastsync = mSharedPreferences.getLong("synclast_"+feedid,0);
			        	//oldest = (lastsync < oldest || oldest == 0) ? lastsync : oldest;
			        	if( lastsync > 0 && (lastsync < oldest || oldest == 0) ){ oldest = lastsync; } // more efficient
			        	long lastsyncactive = mSharedPreferences.contains("synclastactive_"+feedid) ? mSharedPreferences.getLong("synclastactive_"+feedid,0) : -1;
			        	long preflastsync = lastsync > lastsyncactive ? lastsync : lastsyncactive;
			    		long sinceLast = (System.currentTimeMillis() - preflastsync)/1000/60;
			    		if( sinceLast > (syncInterval+2) || lastsync == 0 ){
			    			String longname = mLog.dataFeed[feedid][0];
				        	//String baseurl = dataFeed[feedid][3];
			    			if( lastsync == 0 ){
			    				faillist += longname + " is new.\n";
			    				//i(TAG,"refreshConsoleTouch() healthy service although feed("+longname+") has been requested to be checked and has not yet started. starting service");
			    			}else{
			    				if( sinceLast < syncInterval * 10 ){
			    					failcnt ++;
			    					faillist += longname + " is " + (sinceLast - syncInterval) + " minutes late.\n";
				    				//i(TAG,"refreshConsoleTouch() healthy service except feed("+longname+") hasn't been successfully checked in ("+sinceLast+" minutes) starting service");
			    				}else{
			    					//i(TAG,"refreshConsoleTouch() healthy service except feed("+longname+") hasn't been successfully checked in ("+sinceLast+" minutes) over 10 times the syncinterval. moving on");
			    					//continue;
			    				}
			    			}
			    			
			    		}else{
			    			successcnt ++;
			    			//allverified = true;
			    		}
			    		
					}
					
					long oldestsince = (System.currentTimeMillis() - oldest)/60/1000;
					if( oldestsince > lastrun ){
						sendMessage("Data has been updated.\n", speakspeed);
					}else if( failcnt == 0 && successcnt > 0 ){
						if( oldestsince == 0 ){
							sendMessage("Data is fresh,\nwithin a minute.", speakspeed);
						}else{
							if( oldestsince > 172 ){
								sendMessage("Data freshness within\n" + oldestsince/60 + " hours.", speakspeed);
							}else{
								if( oldestsince == 1 ){
									sendMessage("Data freshness\nwithin a minute.", speakspeed);
								}else{
									sendMessage("Data freshness within\n" + oldestsince + " minutes.", speakspeed);
								}
							}
						}
						//mClearScreen.sendEmptyMessageDelayed(5, 100);
						//break;
					}else if(failcnt > 0 && successcnt > 0){
						if( successcnt > failcnt ){
							sendMessage("Some sources\nhaven't been updated.\n[listing late sources]", speakspeed);
						}else{
							sendMessage("Most sources\nhaven't been updated.\n[listing late sources]", speakspeed);
						}
					}else if(failcnt > 0 && successcnt == 0){
						sendMessage("Every source\nis late.", speakspeed);
						sendMessage("By about\n" + oldestsince + " minutes.", speakspeed);
					}
					
					if( failcnt > 0 ){
						//sendMessage("[listing late sources]", speakspeed);
						sendMessage("Late Sources\n\n" + faillist, speakspeed * 2);
					//}else if(failcnt > 10){
						//sendMessage("Too many sources are late to list.", speakspeed); // reason for all the failures
					}
				}
				
				
				
				
				
				sendMessage("",1000);
				speakspeed = mSharedPreferences.getInt("speakspeed", 1880 * 3);

				
				{
					lastrun = mSharedPreferences.getLong("lastrun", -1);
					if( lastrun > 0 ){ sisLRN = (int) ((System.currentTimeMillis() - lastrun)/1000); }else{sisLRN = 0;}
				
					long unexpected = mSharedPreferences.getLong("unexpected", -1);
					if( unexpected > 0 ){ sisUNX = (int) ((System.currentTimeMillis() - unexpected)/1000); }else{sisUNX = 0;}
					
					long syncstart = mSharedPreferences.getLong("syncstart", -1);
					if( syncstart > 0 ){ sisStart = (int) ((System.currentTimeMillis() - syncstart)/1000); }else{sisStart = 0;}
					
					if( unexpected > syncstart ){
					//           unexpected occurred more than 15 seconds ago and Daytime
					//                                         305                Nighttime
					if( (sisUNX > 15 && 20 > hour && hour > 8) || (sisUNX > 305 && ( hour > 20 || hour < 8 ) ) ){
						if( sisUNX > 640 ){
							
						}else
						if( sisUNX > 330 ){
							sendMessage("Retry must have failed somehow, may I suggest a reboot?", speakspeed);
							sendMessage("It was scheduled " + unexpected + " seconds ago", speakspeed);
							sendMessage("from an Unexpected Failure.", speakspeed);
						//}else
						//if( unexpected > 30 ){
								
						}else
						if( sisUNX > 20 ){
							sendMessage("Retry must have failed somehow, may I suggest a reboot?", speakspeed);
							sendMessage("It was scheduled " + unexpected + " seconds ago", speakspeed);
							sendMessage("from an Unexpected Failure.", speakspeed);
						} else{
							sendMessage("Retry scheduled " + unexpected + " seconds ago.", speakspeed);
						}
					}else
					if( sisUNX > 0 ){
						sendMessage("Your system just had an unexpected failure.", speakspeed);
						sendMessage("[detail follows]", speakspeed);
						
						if( hour < 20 && hour > 8 ){ // Daytime
							if( sisUNX > 10){
								sendMessage("Retry past "+(sisUNX - 10)+" seconds.", speakspeed);
							}else{
								sendMessage("Retry in "+(10 - sisUNX)+" seconds.", speakspeed);
							}
						}else{
							if( sisUNX > 300 ){
								sendMessage("Retry past "+(sisUNX-300)+" seconds.", speakspeed);
							}else{
								sendMessage("Retry in "+(300-sisUNX)+" seconds.", speakspeed);
							}
						}
					}
					}
				}
				
				
				
				
				
				
				
				
				

				for(;;){
					
					sendMessage("",1000);
					speakspeed = mSharedPreferences.getInt("speakspeed", 1880 * 3);
					
					
					// CPU
					
					long syncstart = mSharedPreferences.getLong("syncstart", -1);
					if( syncstart > 0 ){ sisStart = (int) ((System.currentTimeMillis() - syncstart)/1000); }else{sisStart = 0;}
					
					long lastactive = mSharedPreferences.getLong("lastfeedactive", -1);
					if( lastactive > 0 ){ sisLast = (int) ((System.currentTimeMillis() - lastactive)/1000); }else{sisLast = -1;}
					
					long servicerestart = mSharedPreferences.getLong("servicerestart", -1);
					if( servicerestart > 0 ){ sisRST = (int) ((System.currentTimeMillis() - servicerestart)/1000); }else{sisRST = -1;}
					
					long cpublock = mSharedPreferences.getLong("cpublock", -1);
					if( cpublock > 0 ){ sisCPU = (int) ((System.currentTimeMillis() - cpublock)/1000); }else{sisCPU = -1;}
				
					
					if(  sisCPU > -1 && sisCPU < sisLast && sisCPU < sisStart && sisCPU < sisRST){
						if( sisCPU > 45 ){
							double load = mLog.getload(TAG + " Console");
							int syncLoad = mSharedPreferences.contains("syncload") ? mSharedPreferences.getInt("syncload",4) : 4;
							if( load > (syncLoad * 1.99) ){
								sendMessage("Processor Overloaded\n"+(int)(load*100)+"% Utilization\nStanding by\ntill it decreases.", speakspeed);
								continue;
							}else{
								//mSay = "System error, not really busy\nfor " + sisCPU + " seconds.\n";
							}
						}else
						if( sisCPU > 20 ){
							double load = mLog.getload(TAG + " Console");
							if( sisCPU > 30  ){
								sendMessage("Processor Overloaded\nfor " + sisCPU + " seconds.\n"+(int)(load*100)+"% Utilization\nStood by\nbeyond planned by\n" + (sisCPU - 30) + " seconds.\nPlease restart.", speakspeed);
								continue;
							}else{
								sendMessage("Processor Overloaded\nfor " + sisCPU + " seconds.\n"+(int)(load*100)+"% Utilization\nStanding by for\n" + (30 - sisCPU) + " seconds.", speakspeed);
								continue;
							}
						}else
						if( sisCPU > 10 ){
							double load = mLog.getload(TAG + " Console");
							sendMessage("Processor Overloaded\nfor " + sisCPU + " seconds.\n"+(int)(load*100)+"% Utilization\nStanding by.", speakspeed);
							continue;
						}else 
						if( sisCPU > 0 ){
							double load = mLog.getload(TAG + " Console");
							sendMessage("Processor Overloaded\nfor " + sisCPU + " seconds.\n"+(int)(load*100)+"% Utilization\nCausing a slowdown\nin this information lane.", speakspeed);
							continue;
						}
					}
					
					
					// Memory
					

					long lowmemory = mSharedPreferences.getLong("lowmemory", -1);
					if( lowmemory > 0 ){ sisMemory = (int) ((System.currentTimeMillis() - lowmemory)/1000); }else{sisMemory = -1;}

					
					if( sisMemory > -1 && sisMemory < sisLast && sisMemory < sisStart && sisMemory < sisRST ){
						if( sisMemory > 10 ){
							if( hour < 20 && hour > 8 ){// 9am - 7:59pm
								if ( sisMemory > 300 ) {
									sendMessage("System reported low memory\nFailed to retry "+((sisMemory-30)/60)+" minutes ago.", speakspeed);
								} else if( sisMemory > 180 ){
									sendMessage("System reports low memory\nretry "+((sisMemory-30)/60)+" minutes late.", speakspeed);
									continue;
								} else if ( sisMemory > 30 ) {
									sendMessage("System reports low memory\nretry "+(sisMemory-30)+" seconds late.", speakspeed);
									continue;
								} else{
									sendMessage("System reports low memory\nretry in "+(30-sisMemory)+" seconds.", speakspeed);
									continue;
								}
							}else{
								if ( sisMemory > 180 ) {
									sendMessage("System reports low memory\nretry "+(sisMemory/60)+" minutes late.", speakspeed);
								} else if( sisMemory > 60 ) {
									sendMessage("System reports low memory\nretry "+(sisMemory-60)+" seconds late.", speakspeed); // service coded for 5 minutes, touch panel 1 minute
									continue;
								} else {
									sendMessage("System reports low memory\nretry in " +(60-sisMemory)+" seconds.", speakspeed); // service coded for 5 minutes, touch panel 1 minute
									continue;
								}
							}
							
						}else
						if( sisMemory > 0 ){
							sendMessage("System reports\nlow memory\n" +sisMemory+" seconds ago.", speakspeed);
							continue;
						}else
						if( sisMemory == 0 ){
							sendMessage("System reports\nlow memory now.", speakspeed);
							continue;
						}
					}
					
					
					// Service Restart Requested
					
					long httppagetime = mSharedPreferences.getLong("httppagetime", -1);
					if( httppagetime > 0 ){ sisHPT = (int) ((System.currentTimeMillis() - httppagetime)/1000); }else{sisHPT = -1;}

					syncstart = mSharedPreferences.getLong("syncstart", -1);
					if( syncstart > 0 ){ sisStart = (int) ((System.currentTimeMillis() - syncstart)/1000); }else{sisStart = -1;}
					
					if( sisStart > -1 && sisRST > -1 ){
						if( (sisRST < sisMemory || sisRST < sisCPU) && sisStart < sisRST && sisRST < sisLast ){
							sendMessage("Service restart was requested\n" +sisRST+" seconds ago.", speakspeed);
							continue;
						}
					}
					
					if( sisStart > -1 ){
						if( sisStart < 600 && sisStart < sisLast ){
							if( sisStart == 0 ){
								sendMessage("Service starting.", speakspeed);
							}else{
								sendMessage("Service started\n" +sisStart+" seconds ago.", speakspeed);
							}
							//continue;
						}
					}
					
					
					// Last Activity
					
					if( sisHPT > -1 && sisHPT < sisLast ){
						if( sisHPT == 0 ){
							sendMessage("Downloading now.", speakspeed);
						}else{
							sendMessage("Downloading\nfor " +(sisHPT)+ " seconds now.", speakspeed);
						}
						continue;
					}else if( sisLast > 20 && sisLast < sisStart ){
						sendMessage("Working\n" +(sisLast)+ " seconds now.\n\nSeems Very Slow", speakspeed);
					}else 
					if( sisLast > 10 && sisLast < sisStart ){
						sendMessage("Working\n" +(sisLast)+ " seconds now.\n\nSeems Slow", speakspeed);
						continue;
					}else 
					if( sisLast > 0 && sisLast < sisStart ){
						sendMessage("Working\n" +(sisLast)+ " seconds now.", speakspeed);
						continue;
					}
					
					{
						
						if( System.currentTimeMillis()%10000 == 0 ){
							sendMessage("You're cool\nbecause you know.", speakspeed);
						}else if( System.currentTimeMillis()%1000 < 4 ){
							sendMessage("Beautiful Days", speakspeed);
						}else if( System.currentTimeMillis()%1000 < 50 ){
							sendMessage("Sweeeet", speakspeed);
						}else if( System.currentTimeMillis()%1000 < 50 ){
							sendMessage("Ocean Friends", speakspeed);
						}
						
						if( runcount > 99 && runcount/100 < 10 && runcount/100 > 0 ){
							sendMessage("Would you be interested\nin a modification for $10?\n\nPress Menu then Customize to order.", speakspeed);
						}if( runcount > 999 && runcount/100 < 20 && runcount/100 > 9 ){
							sendMessage("Maybe by the time you see this the United Earth Oceans will have already set sail, if not we're looking for support.", speakspeed);
						}else{
							if( runcount == 10 || runcount == 30 || runcount == 31 || runcount == 32 || runcount == 33 ){
								sendMessage("In pursuit of peace, freedom, and happiness.", speakspeed);
							}
						}
					}

					
					long syncend = mSharedPreferences.getLong("syncend", -1);
					if( syncend > 0 ){ sisEnd = (int) ((System.currentTimeMillis() - syncend)/1000); }else{sisEnd = -1;}
					
					if( sisEnd > -1 && sisEnd < sisLast && sisEnd < sisStart ){
						if( sisEnd == 0 ){
							sendMessage("Complete",speakspeed);
						}else{
							sendMessage("Update Complete\n" + sisEnd + " seconds ago.",speakspeed);
						}
					}
					
					break;
				}
				
				
				
				int total = getTotal();
				
				if( total == 0 ){
					sendMessage("Still waiting\nfor the first records.", speakspeed);
					continue;
				}
				
				
				
				
				
				
				
				sendMessage("",1000);
				speakspeed = mSharedPreferences.getInt("speakspeed", 1880 * 3);
				
				{
					
					if( 20 > hour && hour > 8 ){
						//if( unseen > 0 ){
							//sendMessage("All systems operational.", speakspeed);
						//}else{
							//sendMessage("All systems operational.", speakspeed);
						//}
					}else{
						sendMessage("Nighttime Mode\nsilent\npower saver", speakspeed);
					}
					int unseen = 0;
					Cursor db = null;
					
					int lasthours = 1;
					for(; lasthours < 25; lasthours++ ){
						db = SqliteWrapper.query(mContext, mResolver, DataProvider.CONTENT_URI, new String[] {"count(*)"} , "status > 0 AND seen == 0 AND read == 0 AND "+mLog.LAST_UPDATED+" > "+(System.currentTimeMillis() - (lasthours * 60 * 60 * 1000) )+" ", null, null);
						// and (read == 0 or read > "+(System.currentTimeMillis() - (5 * 60 * 1000) )+") and ( seen == 0 or seen > "+(System.currentTimeMillis() - (5 * 60 * 1000) )+" )"
						if( db != null ){ if( db.moveToFirst() ){ unseen = db.getInt(0); } db.close(); }
						if( unseen > 0 ){ break; }
					}
					
					
					if( lasthours == 1  && unseen > 0 ){
						sendMessage(unseen + " unseen\nfrom the last hour.", speakspeed);
					}else if(lasthours > 1 && unseen > 0 ){
						sendMessage(unseen + " unseen\nfrom the last\n"+lasthours+" hours.", speakspeed);
					}else{
						db = null;
						db = SqliteWrapper.query(mContext, mResolver, DataProvider.CONTENT_URI, new String[] {"count(*)"} , "status > 0 AND seen == 0 AND read == 0", null, null);
						// and (read == 0 or read > "+(System.currentTimeMillis() - (5 * 60 * 1000) )+") and ( seen == 0 or seen > "+(System.currentTimeMillis() - (5 * 60 * 1000) )+" )"
						if( db != null ){ if( db.moveToFirst() ){ unseen = db.getInt(0); } db.close(); }
						sendMessage(unseen + " unseen records.", speakspeed);
					}
				}
				
				{
					//if( android.os.Process.getElapsedCpuTime() > 30 * 1000 ){
						mClearScreen.sendEmptyMessageDelayed(5, 100);
						break;
						//stop();
					//}
				}
				
				//SystemClock.sleep(1880);
				//suspend();
				//finalize();
				//int pid = android.os.Process.myTid();
				//android.os.Process.killProcess(pid);
			}
			mLog.e(TAG, "Exited for;; loop");
			mClearScreen.sendEmptyMessageDelayed(5, 100);
			mPreferencesEditor.putLong("computer", 0);
			mPreferencesEditor.commit();
			//Thread.yield();
		}
	};
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mLog = new Custom(this, TAG + " onCreate() 44 ");
        setContentView(R.layout.start);
        
        mLog.w(TAG,"onCreate("+android.os.Process.supportsProcesses()+") ++++++++++++++++++++++++++++++++");

        mIntentExtras = getIntent().getExtras();
        long id = mIntentExtras != null ? mIntentExtras.getLong("id") : -1;
        mConsoleCursor.setDaemon(true);
        mConsoleCursor.setPriority(Thread.MAX_PRIORITY);
        
        mLog.w(TAG,"onCreate() 77");
        mCover = (ImageView) findViewById(R.id.start_cover);
        mTitle = (ImageView) findViewById(R.id.start_title);
        mConsole = (ImageView) findViewById(R.id.start_console);
        mText = (TextView) findViewById(R.id.start_text);
        mActionBar = (LinearLayout) findViewById(R.id.start_actionbar);
        mAction = (ImageView) findViewById(R.id.start_action);
        mClose = (ImageView) findViewById(R.id.start_close);
        mCustomize = (ImageView) findViewById(R.id.start_customize);
        mCover.setOnTouchListener(new OnTouchListener(){ public boolean onTouch(View v, MotionEvent event) { mAction.requestFocus(); return true; } });
        //mCover.requestFocusFromTouch();
        mCover.setKeepScreenOn(true);
        

        
        mContext = this;
        mResolver = mContext.getContentResolver();
	        
        mTabHost = getTabHost();
        //mTabHost.setForeground(drawable);
        
        mLog.w(TAG,"onCreate() 72");
		
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mLog.setNotificationManager(mNM);
        
        mSharedPreferences = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
  	  	mPreferencesEditor = mSharedPreferences.edit();
  	  	mLog.setSharedPreferences(mSharedPreferences, mPreferencesEditor);
  	  	
		//mClose.clearFocus();
		//mAction.requestFocus();
  	  	
		mPreferencesEditor.putLong("computerplay", 0);
		mPreferencesEditor.putLong("computerpause", 0);
		mPreferencesEditor.putLong("computerclose", 0 );
		mPreferencesEditor.commit();
        
        mLog.w(TAG,"onCreate() 81");
        int runcount = mSharedPreferences.getInt("runcount", 0);
        mPreferencesEditor.putInt("runcount", runcount + 1);
        mPreferencesEditor.putLong("lastrun", System.currentTimeMillis() );
        mPreferencesEditor.commit();
        
        mLog.w(TAG,"onCreate() 84");
        {
        	Intent listView = new Intent(this, listView.class);
	        listView.putExtra("id", id);
	        
	        mLog.w(TAG,"onCreate() 86");
	        TabSpec t1 = mTabHost.newTabSpec("listview");
	        
	        mLog.w(TAG,"onCreate() 88");
	        t1.setIndicator(null, getResources().getDrawable(Custom.TOPICON));//Custom.TOPICON
	        mLog.w(TAG,"onCreate() 90");
	        t1.setContent(listView);
	        mLog.w(TAG,"onCreate() 92");
	        mTabHost.addTab(t1);
			mTabHost.setCurrentTab(1);
			
        }
        //mTabHost.setCurrentTab(tab);

        mLog.w(TAG,"onCreate() 97");
        
    	{	
		int topimage = R.drawable.microscope;
		//int topimage = R.drawable.microscope;
		//int topimage = android.R.drawable.ic_menu_compass;
		
        Intent browseView = new Intent(this, browseView.class);
        	//browseView.putExtra("id", id);
			//listView.setClass(this, browseView.class);
        TabSpec t2 = mTabHost.newTabSpec("browse");
        	t2.setIndicator(null, getResources().getDrawable(topimage));
        	t2.setContent(browseView);
        mTabHost.addTab(t2);
    	}
        
        {
	        Intent listView2 = new Intent(this, listSave.class);
	        //listView2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        
        	//listView.putExtra("id", id);
        	//listView.setClass(this, listSave.class);
	        TabSpec t = mTabHost.newTabSpec("listsave");
	        
	        //t.setIndicator(null, getResources().getDrawable(android.R.drawable.ic_menu_save));
	        //t.setIndicator(null, getResources().getDrawable(R.drawable.briefcase));
	        t.setIndicator(null, getResources().getDrawable(R.drawable.disk));
	        	t.setContent(listView2);
	        mTabHost.addTab(t);
        }
        
        {
	        Intent listView2 = new Intent(this, listOut.class);
	        //listView2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        
        	//listView.putExtra("id", id);
        	//listView.setClass(this, listOut.class);
	        TabSpec t = mTabHost.newTabSpec("listout");
	        	t.setIndicator(null, getResources().getDrawable(R.drawable.outhouse));
	        	//t.setIndicator(null, getResources().getDrawable(R.drawable.tab_out));
	        	t.setContent(listView2);
	        mTabHost.addTab(t);
        }
        
        mLog.w(TAG,"onCreate() 124");
        
        
        
        
        //Thread getlatestThread = new Thread(){ public void run(){ getlatest(); } };
        //getlatestThread.start();
        
        
        mTabHost.setOnTabChangedListener(new OnTabChangeListener(){

			public void onTabChanged(String tabname) {
				int tab = mSharedPreferences.contains("tab") ? mSharedPreferences.getInt("tab",0) : 0;
				int cur = mTabHost.getCurrentTab();
				mLog.w(TAG, "onTabChanced() arg0("+tabname+") currenttab("+cur+") lasttab("+tab+")");
				mPreferencesEditor.putInt("lasttab", tab);
				mPreferencesEditor.putInt("tab", cur );
				mPreferencesEditor.commit();
				
				if( cur == 0 ){
					
				}
				
			}
        	
        });
    	
        mLog.w(TAG,"onCreate() 149");
        
        Thread getloadRunner = new Thread(){
			public void run(){
				for(;;){
					double load = mLog.getload(TAG + " Custom(getloadRunner) 55");
					if( load < 4 ){
						mLog.i(TAG,"Current System Load " + load);
					} else if( load < 6 ){
						mLog.w(TAG,"Current System Load " + load);
					} else {
						mLog.e(TAG,"Current System Load " + load);
					}
					SystemClock.sleep( 5 * 60 * 1000 );
				}
				
				
			}
		};
		//getloadRunner.start();
		
		
        
		
        
        //mHandler = new Handler();
        
        
        boolean stoprequest = mIntentExtras != null ? mIntentExtras.getBoolean("stoprequest") : false;
		int tab = mSharedPreferences.contains("tab") ? mSharedPreferences.getInt("tab",0) : 0;
		
		mLog.w(TAG,"onCreate() 173 set record id in preferences('id')");
		
        if( id > -1 ){
			mPreferencesEditor.putLong("id", id);
			mPreferencesEditor.commit();
        }
    	
        //mPreferencesEditor.putLong("id", 0).commit();
		if( stoprequest ){
			mLog.w(TAG,"onCreate() 1052 stop request");
			mPreferencesEditor.putBoolean("stoprequest", false);
			mPreferencesEditor.commit();
		}else{
			//int total = mSharedPreferences.contains("total") ? mSharedPreferences.getInt("total",-1) : -1;
			//if( total == -1 ){
				//Toast.makeText(this, "Loading Service for the First Time", 5000).show();
				//serviceRestart("onCreate() 61");
			//}
			int total = mSharedPreferences.getInt("total",-1);
			if( total < 1 || Custom.DEVWORKING){
				if( Custom.DEVWORKING ){
					if( Custom.DEVFEED > -1 ){
						String longname = mLog.dataFeed[Custom.DEVFEED][0];
						
						mPreferencesEditor.remove("synclast_"+Custom.DEVFEED);
						
						//int deletecount = SqliteWrapper.delete(mContext, mResolver, DataProvider.CONTENT_URI, "type = " + Custom.DEVFEED, null);
						int deletecount = SqliteWrapper.delete(mContext, mResolver, DataProvider.CONTENT_URI, "status < 100", null);
						//int deletecount = SqliteWrapper.delete(mContext, mResolver, DataProvider.CONTENT_URI, "read == 0 AND lastupdated < " + (System.currentTimeMillis() - (1000 * 60 * 60 * 24 * days)), null);
						if( deletecount > 0 ){
							Toast.makeText(this, "Requesting " + longname + ", deleted " + deletecount + " records", 1880).show();
						}else{
							Toast.makeText(this, "Requesting " + longname, 1880).show();
						}

					}else{
						Toast.makeText(this, "Requesting Updates", 1880).show();
						
						for( int feedid = 0; feedid < mLog.dataFeed.length; feedid++ ){
							mPreferencesEditor.remove("synclast_"+feedid);
						}
					}
					mPreferencesEditor.commit();
				}
				mLog.i(TAG,"onCreate() get AlarmManager");
				mAlM = (AlarmManager) getSystemService(ALARM_SERVICE);
				Intent resetservice = new Intent();
		        //com.havenskys.thescoopseattle.SERVICE_RESET
				resetservice.setAction("com.havenskys.thescoopseattle.SERVICE_START0");
				PendingIntent service3 = PendingIntent.getBroadcast(this, 0, resetservice, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_CANCEL_CURRENT);
				mAlM.set(AlarmManager.RTC_WAKEUP,( System.currentTimeMillis() + (1880) ), service3);
				Toast.makeText(this, "Starting Up Service", Toast.LENGTH_LONG).show();
			}else{
				//mAlM.set(AlarmManager.RTC_WAKEUP,( System.currentTimeMillis() + (30 * 1880) ), service3);
			}
			
		}
        
        mLog.w(TAG,"onCreate() 189");
        
        if( id > 0 ){
			mNM.cancel(Custom.NOTIFY_ID_ARTICLE);
			mNM.cancel((int) id);
			mTabHost.setCurrentTab(1);
		}else{
			mTabHost.setCurrentTab(tab);
		}
        
        mTabHost.setOnTouchListener(new OnTouchListener(){
			public boolean onTouch(View v, MotionEvent event) {
				mLog.e(TAG, "onCreate() 887 mTabHost onTouch("+event.getAction()+") up("+MotionEvent.ACTION_UP+") down("+MotionEvent.ACTION_DOWN+") move("+MotionEvent.ACTION_MOVE+") cancel("+MotionEvent.ACTION_CANCEL+")");
				//if( event.getAction() == MotionEvent.ACTION_DOWN ){
					//mTabHost.performClick();
				//}
				return false;
			}
        });
        
        //mHandler = new Handler();
        
        //mThread = new Thread(null, this, "computerconsole_runnable");
        //mThread.setPriority(Thread.MAX_PRIORITY);
        //mThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler(){

  		//public void uncaughtException(Thread thread, Throwable ex) {
  			//mLog.e(TAG, "mThread uncaughtException() " + ex.getMessage() );
  			//mThread.start();
  		//} 
        //});
        //mLog.e(TAG,"onCreate() 263 starting Console Thread.");
        //mThread.start();

        
        
        
        mAction.setOnClickListener(new OnClickListener(){
        	//private ImageView image;
        	private long pause, play;
			public void onClick(View v) {
				//image = (ImageView) v;
				pause = mSharedPreferences.getLong("computerpause", 0);
				play = mSharedPreferences.getLong("computerplay", 0);
				
				mLog.e(TAG, "onClickListener() mAction pause("+pause+")");
				if( pause > 0 ){
					mAction.setImageResource(R.drawable.console_pause);
					
					mPreferencesEditor.putLong("computerplay", System.currentTimeMillis());
					mPreferencesEditor.putLong("computerpause", 0);
					mPreferencesEditor.commit();
					
					Message pauseMessage = new Message();
					Bundle pauseBundle = new Bundle();
					pauseBundle.putString("text", "{computer resume}");
					pauseMessage.setData(pauseBundle);
					mUpdateConsole.sendMessage(pauseMessage);
				}else{
					mAction.setImageResource(R.drawable.console_play);
					
					mPreferencesEditor.putLong("computerplay", 0);
					mPreferencesEditor.putLong("computerpause", System.currentTimeMillis());
					mPreferencesEditor.commit();
					
					Message pauseMessage = new Message();
					Bundle pauseBundle = new Bundle();
					pauseBundle.putString("text", "{computer pause}");
					pauseMessage.setData(pauseBundle);
					mUpdateConsole.sendMessage(pauseMessage);
				}
				//mAction.requestFocus();
			}        	
        });
        
        mAction.setOnTouchListener(new OnTouchListener(){
        	//private ImageView image;
        	private long pause;
        	private long play;
        	public boolean onTouch(View v, MotionEvent event) {
        		
        		if( event.getAction() == MotionEvent.ACTION_DOWN ){
					//image = (ImageView) v;
					pause = mSharedPreferences.getLong("computerpause", 0);
					play = mSharedPreferences.getLong("computerplay", 0);
					
					mLog.e(TAG, "onTouchListener() mAction pause("+pause+")");
					if( pause > 0 ){
						mAction.setImageResource(R.drawable.console_pause);
						
						mPreferencesEditor.putLong("computerplay", System.currentTimeMillis());
						mPreferencesEditor.putLong("computerpause", 0);
						mPreferencesEditor.commit();
						
						Message pauseMessage = new Message();
						Bundle pauseBundle = new Bundle();
						pauseBundle.putString("text", "{computer resume}");
						pauseMessage.setData(pauseBundle);
						mUpdateConsole.sendMessageDelayed(pauseMessage,100);
					}else{
						mAction.setImageResource(R.drawable.console_play);
						
						mPreferencesEditor.putLong("computerplay", 0);
						mPreferencesEditor.putLong("computerpause", System.currentTimeMillis());
						mPreferencesEditor.commit();
						
						Message pauseMessage = new Message();
						Bundle pauseBundle = new Bundle();
						pauseBundle.putString("text", "{computer pause}");
						pauseMessage.setData(pauseBundle);
						mUpdateConsole.sendMessageDelayed(pauseMessage,100);
					}
					//return true;
					//mAction.requestFocus();
        		}
			
				return true;
			}        	
        });
        
        mAction.setOnFocusChangeListener(new OnFocusChangeListener(){
        	//private ImageView image;
        	private long pause;
        	private long play;
        	private Drawable playunselected = mContext.getResources().getDrawable(R.drawable.console_play);
			private Drawable playselected = mContext.getResources().getDrawable(R.drawable.console_play_selected);
			private Drawable pauseunselected = mContext.getResources().getDrawable(R.drawable.console_pause);
			private Drawable pauseselected = mContext.getResources().getDrawable(R.drawable.console_pause_selected);
			public void onFocusChange(View v, boolean hasfocus) {
				// = (ImageView) v;
				pause = mSharedPreferences.getLong("computerpause", 0);
				play = mSharedPreferences.getLong("computerplay", 0);
				
				if( pause > 0 ){
					if( hasfocus ){
						//mAction.setImageResource(R.drawable.console_play_selected);
						mAction.setImageDrawable(playselected);
						mAction.requestFocus();
					}else{
						//mAction.setImageResource(R.drawable.console_play);
						mAction.setImageDrawable(playunselected);
					}
				}else{
					if( hasfocus ){
						mAction.setImageResource(R.drawable.console_pause_selected);
						mAction.setImageDrawable(pauseselected);
						mAction.requestFocus();
					}else{
						mAction.setImageResource(R.drawable.console_pause);
						mAction.setImageDrawable(pauseunselected);
					}
				}
				
			}
        });
        
        mClose.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				mLog.e(TAG, "onClickListener() mClose");
				mPreferencesEditor.putLong("computerclose", System.currentTimeMillis() + 5000 ); // Stay off for 5 seconds.
				mPreferencesEditor.commit();
				Message pauseMessage = new Message();
				Bundle pauseBundle = new Bundle();
				pauseBundle.putString("text", "{computer end program}");
				pauseMessage.setData(pauseBundle);
				mUpdateConsole.sendMessageDelayed(pauseMessage,100);
				mClearScreen.sendEmptyMessageDelayed(9, 10);
			}
        });
        
        mClose.setOnTouchListener(new OnTouchListener(){
			public boolean onTouch(View v, MotionEvent event) {
				if( event.getAction() == MotionEvent.ACTION_DOWN ){
					mLog.e(TAG, "onTouchListener() mClose");
					mPreferencesEditor.putLong("computerclose", System.currentTimeMillis() + 5000 ); // Stay off for 5 seconds.
					mPreferencesEditor.commit();
					Message pauseMessage = new Message();
					Bundle pauseBundle = new Bundle();
					pauseBundle.putString("text", "{computer end program}");
					pauseMessage.setData(pauseBundle);
					mUpdateConsole.sendMessageDelayed(pauseMessage,100);
					mClearScreen.sendEmptyMessageDelayed(10, 10);
					return true;
				}
				return false;
			}
        });
        
        mClose.setOnFocusChangeListener(new OnFocusChangeListener(){
        	//private ImageView image;
        	private Drawable selected = mContext.getResources().getDrawable(R.drawable.console_close_selected);
        	private Drawable unselected = mContext.getResources().getDrawable(R.drawable.console_close);
			public void onFocusChange(View v, boolean hasfocus) {
				//image = (ImageView) v;
				if( hasfocus ){
					mClose.setImageDrawable(selected);
				}else{
					mClose.setImageDrawable(unselected);
				}
			}
        });
        
        
        mCustomize.setOnFocusChangeListener(new OnFocusChangeListener(){
        	//private ImageView image;
        	private Drawable selected = mContext.getResources().getDrawable(R.drawable.customize_selected);
        	private Drawable unselected = mContext.getResources().getDrawable(R.drawable.customize);
			public void onFocusChange(View v, boolean hasfocus) {
				//ImageView image = (ImageView) v;
				if( hasfocus ){
					mCustomize.setImageDrawable(selected);
				}else{
					mCustomize.setImageDrawable(unselected);
				}
			}
        });
        
        mCustomize.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				Intent goFish = new Intent(mContext, Customize.class);
				startActivity(goFish);				
			}
        });
        mCustomize.setOnTouchListener(new OnTouchListener(){
			public boolean onTouch(View arg0, MotionEvent arg1) {
				Intent goFish = new Intent(mContext, Customize.class);
				startActivity(goFish);
				return true;
			}
        });
        
        
        Message pauseMessage = new Message();
		Bundle pauseBundle = new Bundle();
		pauseBundle.putString("text", "{computer}\n=tri-la-beep=");
		pauseMessage.setData(pauseBundle);
		mUpdateConsole.sendMessageDelayed(pauseMessage,100);
		
    }

    private AlarmManager mAlM;
    
	@Override
	protected void onStart() {
		mLog.w(TAG,"onStart() ++++++++++++++++++++++++++++++++");
		super.onStart();
		
		//mLog.threadRestart(mConsoleCursor);
		long close = mSharedPreferences.getLong("computerclose", 0);
		if(close > System.currentTimeMillis() - 5000 ){
			
		}else{
			
			mPreferencesEditor.putLong("computerclose", 0 );
			mPreferencesEditor.commit();
			
			if( !mConsoleHandler.sendEmptyMessage(8) ){
				if( !mConsoleHandler.sendEmptyMessage(8) ){
					mLog.e(TAG, "onStart() failed twice to start computer.");
				}
			}else{
				mCover.setVisibility(View.VISIBLE);
			}
			//mCover.setVisibility(View.VISIBLE);
	    	//mTitle.setVisibility(View.VISIBLE);
			//mConsoleHandler.sendEmptyMessage(7);
		}
		
		//mConsoleCursor.start();
		/*/
		if( mThread.isAlive() ){
			mLog.i(TAG, "onStart() verified mThread is alive");
			mLog.e(TAG,"onStart() 278 refreshConsole() postDelay 300");
			mHandler.postDelayed((Runnable) this, 300);
		}else{
			mLog.w(TAG, "onStart() is starting mThread");
			try {
				mThread.start();
			} catch (IllegalThreadStateException e){
				mLog.w(TAG,"onStart() mThread already started.");
				mLog.e(TAG,"onStart() 286 refreshConsole() postDelay 300");
				mHandler.postDelayed((Runnable) this, 300);
			}
		}//*/
		
		//Intent service = new Intent();
		//service.setClass(this, SyncService.class);
		//service.putExtra("com.havenskys.thescoopseattle.who", TAG + " onStart() 214");
	    //startService(service);
	    
		/*/
		long syncstart = mSharedPreferences.getLong("syncstart", -1);
		long lastactive = mSharedPreferences.getLong("lastfeedactive", -1);
		long lowmemory = mSharedPreferences.getLong("lowmemory", -1);
		//mPreferencesEditor.putLong("lowmemory", System.currentTimeMillis()).commit();
		
		if( syncstart < (System.currentTimeMillis() - 60 * 1000) && lowmemory < (System.currentTimeMillis() - 2 * 60 * 1000 ) ){
		}
		//*/

	}

	
	@Override
	protected void onResume() {

		super.onResume();
		//mLog.threadRestart(mConsoleCursor);
		//mConsoleHandler.sendEmptyMessageDelayed(8, 100);
		/*/
		long close = mSharedPreferences.getLong("computerclose", 0);
		if(close > System.currentTimeMillis() - 5000 ){
			
		}else{
			//mCover.setVisibility(View.VISIBLE);
	    	//mTitle.setVisibility(View.VISIBLE);
			//mConsoleHandler.sendEmptyMessage(8);
			if( !mConsoleHandler.sendEmptyMessage(9) ){
				if( !mConsoleHandler.sendEmptyMessage(9) ){
					mLog.e(TAG, "onResume() failed twice to start computer.");
				}
			}else{
				mCover.setVisibility(View.VISIBLE);
			}
		}//*/

	}


	@Override
	protected void onChildTitleChanged(Activity childActivity, CharSequence title) {
		
		mLog.w(TAG,"onChildTitleChanged() tabCount("+mTabHost.getTabWidget().getChildCount()+") childActivity("+childActivity.getClass().getName()+") title("+title+") ++++++++++++++++++++++++++++++++");
		
		String name = childActivity.getClass().getName();
				
		//if( mTabHost.getTabWidget().getChildCount() == 1 ){
        	//mNM.cancel(Custom.NOTIFY_ID_ARTICLE);
		//}
		
		if( name.contains("browseView") ){
			if( title.length() == 0 ){
				//mTabHost.requestFocusFromTouch();
				int lasttab = mSharedPreferences.contains("lasttab") ? mSharedPreferences.getInt("lasttab",0) : 0;
				if( lasttab == 1 ){ lasttab = 0; }
				mLog.w(TAG,"onChildTitleChanged() lasttab("+lasttab+")");
				mTabHost.setCurrentTab(lasttab);
				return;
			}
			return;
		}
		
		
		if( name.contains("listView") || name.contains("listSave") || name.contains("listOut") ){
		
			if( title.length() == 0 ){
				mTabHost.requestFocusFromTouch();
				int tab = mSharedPreferences.contains("tab") ? mSharedPreferences.getInt("tab",0) : 0;
				mTabHost.setCurrentTab(tab);
				mPreferencesEditor.remove("id");
				mPreferencesEditor.remove("position_ListView");
				mPreferencesEditor.remove("position_ListSave");
				mPreferencesEditor.remove("position_ListOut");
				mPreferencesEditor.commit();
				return;
			}
			
			long id = Long.parseLong(title.toString());
			
			if( id > 0 ){
				mPreferencesEditor.putLong("id", id); mPreferencesEditor.commit();
	        	//mNM.cancel(Custom.NOTIFY_ID_ARTICLE);
		        //mTabHost.getChildAt(1).setTag(id);
		        
		        //mTabHost.getChildAt(2).setTag(id);
		        
	        	/*
		        int topimage = android.R.drawable.ic_menu_compass;
		        int feedid = 0;
		        Cursor dataCursor = SqliteWrapper.query(this, this.getContentResolver(), DataProvider.CONTENT_URI 
		        		,new String[]{"type"}
		        		,"_id = " + id
		        		,null
		        		,null //orderby //"date desc"
		        		);
		        
		        if( dataCursor != null ){
		        	if( dataCursor.moveToFirst() ){
		        		feedid = dataCursor.getInt(0);
		    			topimage = Integer.parseInt(mLog.dataFeed[feedid][2]);
		        	}
		        }
		        
		        if( mTabHost.getChildCount() > 1 ){
			        TabWidget tw = mTabHost.getTabWidget();
			        tw.removeViewAt(1);
		        }
		        
		        Intent browseView = new Intent(this, browseView.class);
	        	//browseView.putExtra("id", id);
		        TabSpec t2 = mTabHost.newTabSpec("browse");
		        t2.setIndicator(null, getResources().getDrawable(topimage));
		        t2.setContent(browseView);
		        mTabHost.addTab(t2);
		        
		        //mTabHost.getChildAt(1).
		        //*/
		        
		        //mTabHost.setCurrentTab(1);
		        Intent browseView = new Intent(this, browseView.class);
		        browseView.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(browseView);
		        
			}else{
				if( id == -100 ){
					//Intent jump = new Intent(this,listView.class);
					//mTabHost.set
					//childActivity.setIntent(jump);
					//childActivity.startActivity(jump);
				}
			}
		}
		
		super.onChildTitleChanged(childActivity, title);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		mLog.w(TAG, "onContextItemSelected() ");
		return super.onContextItemSelected(item);
	}

	@Override
	public void onContextMenuClosed(Menu menu) {
		// TODO Auto-generated method stub
		mLog.w(TAG, "onContextMenuClosed() ");
		super.onContextMenuClosed(menu);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		mLog.w(TAG, "onCreateContextMenu() ");
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		mLog.w(TAG, "onCreateOptionsMenu() ");
		
		//menu.add(Menu.NONE, 101, 0, "View Source Webpage").setIcon(android.R.drawable.ic_menu_view);
		
		//menu.add(Menu.NONE, 202, 1, "Email " + mLog.WHO).setIcon(Custom.LITTLEICON);
		
		menu.add(Menu.NONE, 1, 1, "Doc Chomps Software")
		.setIcon(R.drawable.docdot);
		
		menu.add(Menu.NONE, 202, 4, "Customize")
			.setIcon(R.drawable.upgradeicon);
		
		menu.add(Menu.NONE, 2, 3, "Help")
			.setIcon(android.R.drawable.ic_menu_help);
		
		menu.add(Menu.NONE, 203, 0, "Computer")
		.setIcon(R.drawable.upgradeicon);
		
		menu.add(Menu.NONE, 201, 5, "Email Support")
			.setIcon(android.R.drawable.ic_menu_send);

		
		/*/
		
		{
			int groupNum = 20;
			SubMenu sync = menu.addSubMenu(Menu.NONE, groupNum, 20, "Synchronization"); //getItem().
			sync.setIcon(R.drawable.ic_menu_refresh);
			sync.add(groupNum, 0, 0, "Now");
			sync.add(groupNum, 5, 2, "5 Minutes");
			sync.add(groupNum, 30, 3, "30 Minutes");
			sync.add(groupNum, 60, 3, "Hourly");
			sync.add(groupNum, 60 * 2, 4, "2 Hours");
			sync.add(groupNum, 60 * 3, 5, "3 Hours");
			sync.add(groupNum, 60 * 4, 6, "4 Hours");
			sync.add(groupNum, 60 * 6, 7, "6 Hours");
			sync.add(groupNum, 60 * 8, 8, "8 Hours");
			sync.add(groupNum, 60 * 12, 9, "12 Hours");
			sync.add(groupNum, 60 * 24, 10, "Daily");
			sync.add(groupNum, 60 * 24 * 2, 11, "2 Days");
			sync.add(groupNum, 60 * 24 * 4, 12, "4 Days");
			sync.add(groupNum, 60 * 24 * 7, 13, "Weekly");
			int syncInterval = mSharedPreferences.contains("sync") ? mSharedPreferences.getInt("sync",30) : 30;
			sync.setGroupCheckable(groupNum, true, true);
			sync.setGroupEnabled(groupNum, true);
			
			MenuItem activeitem = null;
			activeitem = sync.findItem(syncInterval);
			if( activeitem == null ){
				if( syncInterval > 0 ){
					sync.add(groupNum, syncInterval, 1, syncInterval + " Minutes");
				}else{
					syncInterval = 30; // Must exist.
				}
				activeitem = sync.findItem(syncInterval);
			}
			activeitem.setChecked(true);
		}
		
		//sync.findItem(11).setChecked(true);
		
		//Intent intent = new Intent(null, getIntent().getData());
	    //intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		//sync.addIntentOptions(2, 12, 0, this.getComponentName(), null, intent, Menu.FLAG_PERFORM_NO_CLOSE | Menu.FLAG_APPEND_TO_GROUP, null);

		{
			int groupNum = 21;
			//int load = (int) mLog.getload();
			SubMenu submenu = menu.addSubMenu(Menu.NONE, groupNum, 20, "Acceptable Load"); //getItem().
			submenu.setIcon(android.R.drawable.ic_menu_preferences);
			//syncload.add(groupNum, load, 0, "Your Load is at " + load);
			submenu.add(groupNum, 1, 2, "1 (Healthy)");
			submenu.add(groupNum, 2, 3, "2 (Alright)");
			submenu.add(groupNum, 3, 4, "3 (Okay)");
			submenu.add(groupNum, 4, 5, "4 (Acceptable)");
			submenu.add(groupNum, 5, 6, "5 (Busy)");
			submenu.add(groupNum, 6, 7, "6 (Extremely Busy) ");
			int syncLoad = mSharedPreferences.contains("syncload") ? mSharedPreferences.getInt("syncload",4) : 4;
			submenu.setGroupCheckable(groupNum, true, true);
			submenu.setGroupEnabled(groupNum, true);
			
			MenuItem activeitem = null;
			activeitem = submenu.findItem(syncLoad);
			if( activeitem == null ){
				if( syncLoad > 0 ){
					submenu.add(2, syncLoad, 1, "Load " + syncLoad);
				}else{
					syncLoad = 4; // Must exist.
				}
				activeitem = submenu.findItem(syncLoad);
			}
			activeitem.setChecked(true);
		}

		{
			int groupNum = 22;
			SubMenu submenu = menu.addSubMenu(Menu.NONE, groupNum, 20, "Vibrate"); //getItem().
			submenu.setIcon(R.drawable.ic_vibrate);
			submenu.add(groupNum, 3, 2, "NO _");
			submenu.add(groupNum, 2, 3, "YES ++");
			submenu.add(groupNum, 1, 3, "YES ++_+");
			submenu.add(groupNum, 4, 4, "YES +_++");
			int syncvib = mSharedPreferences.contains("syncvib") ? mSharedPreferences.getInt("syncvib",1) : 1;
			submenu.setGroupCheckable(groupNum, true, true);
			submenu.setGroupEnabled(groupNum, true);
			
			MenuItem activeitem = null;
			activeitem = submenu.findItem(syncvib);
			if( activeitem == null ){
				syncvib = 1; // Must exist.
				activeitem = submenu.findItem(syncvib);
			}
			activeitem.setChecked(true);
		}//*/

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		mLog.w(TAG, "onOptionsItemSelected() groupid(" + item.getGroupId() + ") itemid("+item.getItemId()+") title("+item.getTitle()+")");
		if( item.getGroupId() == 20 ){ // Synchronize Timeline
			int itemvalue = item.getItemId(); // minutes
			if( itemvalue == 0 ){
				
				mLog.w(TAG, "onOptionsItemSelected() 332 requested full manual sync");
				
				Toast.makeText(this, "Requesting Updates", 1880).show();
				
				for( int feedid = 0; feedid < mLog.dataFeed.length; feedid++ ){
					mPreferencesEditor.remove("synclast_"+feedid);
				}
				mPreferencesEditor.commit();
				
				
				
				this.serviceRestart(TAG + " onOptionsItemSelected() 335 ");
				
			}else{
				Toast.makeText(this, "Updating Synchronization Interval", 1880).show();
				
				mLog.w(TAG, "Updating Synchronization Interval(" + itemvalue + ")");
				mPreferencesEditor.putInt("sync", itemvalue); mPreferencesEditor.commit();
				if( ! item.isChecked() ){ item.setChecked(true); serviceRestart("onOptionsItemSelected() sync[interval]");}
			}
			return true;
		}
		if( item.getGroupId() == 21 ){ // Acceptable Load
			int itemvalue = item.getItemId(); // minutes
			Toast.makeText(this, "Updating Acceptable Load", 1880).show();
			mLog.w(TAG, "Updating Acceptable Load(" + itemvalue + ")");
			mPreferencesEditor.putInt("syncload", itemvalue); mPreferencesEditor.commit();
			if( ! item.isChecked() ){ item.setChecked(true); serviceRestart("onOptionsItemSelected() syncload");}
			return true;
		}
		if( item.getGroupId() == 22 ){ // Vibrate
			int itemvalue = item.getItemId(); // minutes
			Toast.makeText(this, "Updating Vibration Notification", 1880).show();
			mLog.w(TAG, "Updating Vibrate(" + itemvalue + ")");
			mPreferencesEditor.putInt("syncvib", itemvalue); mPreferencesEditor.commit();
			if( ! item.isChecked() ){ item.setChecked(true); serviceRestart("onOptionsItemSelected() syncvib");}
			return true;
		}
		
		switch(item.getItemId()){
		case 1: //About
			{
				Intent jump = new Intent(this, About.class);
				startActivity(jump);
			}
			break;
		case 2: //Help
			{
				Intent jump = new Intent(this, Help.class);
				startActivity(jump);
			}
			break;
		case 101:
			//{
				//Intent jump = new Intent(Intent.ACTION_VIEW, Uri.parse(Custom.BASEURL));
				//startActivity(jump);
			//}
			break;
		case 201:
			{
				Intent jump = new Intent(Intent.ACTION_SEND);
				jump.putExtra(Intent.EXTRA_TEXT, "This is a request for special help.  Contained in this request are the only details to help diagnose and understand an issue.\n\n\n\n");
				jump.putExtra(Intent.EXTRA_EMAIL, new String[] {"\""+ Custom.APP + " Support\" <havenskys@gmail.com>"} ); 
				jump.putExtra(Intent.EXTRA_SUBJECT, "Support Request: " + Custom.APP);
				jump.setType("message/rfc822"); 
				startActivity(Intent.createChooser(jump, "Email"));
			}
			break;
		case 202:
			{
				Intent goFish = new Intent(this, Customize.class);
				startActivity(goFish);
				//Intent jump = new Intent(Intent.ACTION_SEND);
				//jump.putExtra(Intent.EXTRA_TEXT, "");
				//jump.putExtra(Intent.EXTRA_EMAIL, new String[] {"\""+Custom.WHO + "\" <"+Custom.EMAIL+">"});
				//jump.putExtra(Intent.EXTRA_SUBJECT, "Hello");
				//jump.setType("message/rfc822"); 
				//startActivity(Intent.createChooser(jump, "Email"));
			}
			break;
		case 203:
			//if( mCover.getVisibility() == ImageView.GONE ){
				//mLog.threadRestart(mConsoleCursor);
				//mCover.setVisibility(View.VISIBLE);
		    	//mTitle.setVisibility(View.VISIBLE);
			
			mPreferencesEditor.putLong("computerclose", 0 );
			mPreferencesEditor.commit();
			
				if( !mConsoleHandler.sendEmptyMessage(7) ){
					if( !mConsoleHandler.sendEmptyMessage(7) ){
						mLog.e(TAG, "onOptionsItemSelected() failed twice to start computer.");
					}
				}else{
					mCover.setVisibility(View.VISIBLE);
				}
			
			//}
			/*/
			if( mCover.getVisibility() == ImageView.GONE ){
				mCover.setVisibility(ImageView.VISIBLE);// mCover used here, mAction used in Custom.
				if( mThread.isAlive() ){
					mLog.i(TAG, "Computer verified mThread is alive");
					mLog.e(TAG,"onStart() 661 refreshConsole() postDelay 300");
					mHandler.postDelayed((Runnable) this, 300);
				}else{
					mLog.w(TAG, "Computer is starting mThread");
					try {
						mThread.start();
					} catch (IllegalThreadStateException e){
						mLog.w(TAG,"Computer mThread already started.");
						mLog.e(TAG,"onStart() 669 refreshConsole() postDelay 300");
						mHandler.postDelayed((Runnable) this, 300);
					}
				}
				
			}//*/
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	
	private void serviceRestart(final String who) {
		
		final Context ctx = this;
		//mPreferencesEditor.putBoolean("servicerestart", true); mPreferencesEditor.commit();
		
		mLog.w(TAG, "serviceRestart() from " + who);
		Thread s = new Thread(){
			public void run(){
				SystemClock.sleep(1880);
				
			    Intent service = new Intent();
				service.setClass(ctx, SyncService.class);
			    stopService(service);
			    service.putExtra("com.havenskys.thescoopseattle.who", TAG + " serviceRestart() from " + who);
			    startService(service);
			}
		};
		s.setPriority(Thread.NORM_PRIORITY);
		s.start();
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		// TODO Auto-generated method stub
		mLog.w(TAG, "onOptionsMenuClosed()");
		super.onOptionsMenuClosed(menu);
	}


//	public void run() {
		
		//mLog.refreshConsole(mAction, mText, mConsole, mTitle, mCover, mHandler, mThread);

	//}


	@Override
	protected void onDestroy() {
		mLog.e(TAG, "onDestroy()");
		super.onDestroy();
	}


	@Override
	protected void onPause() {
		mLog.e(TAG, "onPause()");
		super.onPause();
	}


	@Override
	protected void onStop() {
		mLog.e(TAG, "onStop()");
		super.onStop();
	}


	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		mLog.e(TAG, "onMenuOpened() featureId("+featureId+")");
		
		return super.onMenuOpened(featureId, menu);
	}


	@Override
	public void openContextMenu(View view) {
		mLog.e(TAG, "openContentMenu()");
		
		
		super.openContextMenu(view);
	}


	@Override
	public void openOptionsMenu() {
		
		mLog.e(TAG, "openOptionsMenu");
		super.openOptionsMenu();
		
		
		
		if( !mConsoleHandler.sendEmptyMessage(7) ){
			if( !mConsoleHandler.sendEmptyMessage(7) ){
				mLog.e(TAG, "onMenuOpened() failed twice to start computer.");
			}
		}else{
			mCover.setVisibility(View.VISIBLE);
		}
		
		
	}


	@Override
	public boolean onCreatePanelMenu(int featureId, Menu menu) {
		mLog.e(TAG, "onCreatePanelMenu()");
		return super.onCreatePanelMenu(featureId, menu);
	}




}










