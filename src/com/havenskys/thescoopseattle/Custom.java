package com.havenskys.thescoopseattle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.Thread.State;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.Date;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.client.methods.HttpGet;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Process;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Custom {

	// CUSTOM
	public static String APP = "TheScoopSeattle";
	
	public static boolean PUBLISH = true;// Logs print if false
	public static boolean DEVWORKING = false;
	public static boolean QUITEFEED = true;
	public static int DEVFEED = 9; // if DEVWORKING is true this will be the only feed download
	public static int LIMITDOWNLOAD = 25;
	
	//public static int PARSESIZE_LIMIT = 50; //Kilobytes
	public static int DOWNLOAD_LIMIT = 20; //Kilobytes
	
	//public static String WHO = "SSCS";
	//public static String EMAIL = "info@seashepherd.org";
	
	public static int LITTLEICON = R.drawable.thescoopseattleicon;
	public static int TOPICON = R.drawable.thescoopseattleicon;
	public static int BACKGROUND = R.drawable.thescoopseattlebackground;
	
	private Context mContext;
    private ContentResolver mResolver;
    public String[][] dataFeed;
	public Custom(Context ctx, String who){
		TAG = who.replaceFirst(".* for ", "").replaceFirst(" .*", "");
		
		long freememory = Runtime.getRuntime().freeMemory();
		
		//w(TAG,"Custom() ++++++++++++++++++++++++++++++++++ freememory("+(int)(freememory/1024)+" K) for " + who);
		
		
		mContext = ctx;
		mResolver = ctx.getContentResolver();
		
		dataFeed = new String[10][9];
		//0: long name, 1: icon, 2: top image, 3: url, 4: entry split, 5: parse columns, 6: parse tags, 7: fix dates, 8: get article link column
		
		//dataFeed[0] = new String("Real Change News;;"+R.drawable.realchangeicon+";;"+R.drawable.realchangetop+";;http://www.realchangenews.org/index.php/site/rss_2.0/;;item;;title,link,date,content,guid;;title,link,dc:date,description,guid;;2;;false").split(";;");
		// no date time
		//dataFeed[1] = new String("Real Change Blog;;"+R.drawable.realchangeicon+";;"+R.drawable.realchangetop+";;http://www.realchangenews.org/index.php/site/rss_blog_2.0/;;item;;title,link,date,content,guid;;title,link,dc:date,description,guid;;2;;false").split(";;");
		// no date time
		dataFeed[0] = new String("The Stranger;;"+R.drawable.thestrangericon+";;"+R.drawable.thestrangertop+";;http://lineout.thestranger.com/seattle/Rss.xml;;item;;title,link,date,content,guid,author;;title,link,pubDate,description,guid,author;;2;;false").split(";;");
		dataFeed[1] = new String("Seattle Weekly;;"+R.drawable.seattleweeklyicon+";;"+R.drawable.seattleweeklytop+";;http://blogs.seattleweekly.com/dailyweekly/rss.xml;;item;;title,link,date,content,guid;;title,link,pubDate,description,guid;;2;;false").split(";;");
		dataFeed[2] = new String("Mayor Nickels;;"+R.drawable.mayornickelstop+";;"+R.drawable.mayornickelstop+";;http://nickelsnotebook.seattle.gov/category/nickels-notebook/feed;;item;;title,link,date,summary,content,guid;;title,link,pubDate,description,content:encoded,guid;;2;;false").split(";;");
		dataFeed[3] = new String("Seattle Times;;"+R.drawable.seattletimesicon+";;"+R.drawable.seattletimestop+";;http://seattletimes.nwsource.com/rss/seattletimes.xml;;item;;title,link,date,content,guid;;title,link,pubDate,description,guid;;2;;false").split(";;");
		dataFeed[4] = new String("Seattle PI;;"+R.drawable.seattlepiicon+";;"+R.drawable.seattlepitop+";;http://www.seattlepi.com/rss/local_2.rss;;item;;title,link,date,content,guid,author;;title,link,pubDate,description,guid,author;;2;;false").split(";;");
		//dataFeed[7] = new String("Seattle Land Use Notices;;"+R.drawable.seattlelanduse+";;"+R.drawable.seattlelanduse+";;http://web1.seattle.gov/dpd/luib/RSSAllAreas.aspx;;item;;title,link,date,content,guid;;title,link,pubDate,description,link;;2;;false").split(";;");
		// no date time
		dataFeed[5] = new String("The Scoop - Seattle Animal Shelter News and Events;;"+R.drawable.seattlethescoopanimal+";;"+R.drawable.seattlethescoopanimal+";;http://thescoop.seattle.gov/feed/;;item;;title,link,date,summary,content,guid;;title,link,pubDate,description,content:encoded,guid;;2;;false").split(";;");
		dataFeed[6] = new String("Seattle.gov City-Wide;;"+R.drawable.doc+";;"+R.drawable.doc+";;http://www.trumba.com/calendars/seattlegov-city-wide.rss;;item;;title,link,date,content,guid;;title,link,pubDate,description,guid;;2;;false").split(";;");
		dataFeed[7] = new String("Seattle.gov Weekend Events;;"+R.drawable.doc+";;"+R.drawable.doc+";;http://www.trumba.com/calendars/seattle-events-weekend-events.rss;;item;;title,link,date,content,guid;;title,link,pubDate,description,guid;;2;;false").split(";;");
		dataFeed[8] = new String("Parks Recreation;;"+R.drawable.seattleparksandrecicon+";;"+R.drawable.seattleparksandrec+";;http://www.trumba.com/calendars/parks-recreation.rss;;item;;title,link,date,content,guid;;title,link,pubDate,description,guid;;2;;false").split(";;");
		dataFeed[9] = new String("Hearing Examiner;;"+R.drawable.hearingexaminericon+";;"+R.drawable.hearingexaminer+";;http://www.trumba.com/calendars/hearing-examiner.rss;;item;;title,link,date,content,guid;;title,link,pubDate,description,guid;;2;;false").split(";;");
		
		/*/
		
		
		dataFeed[13] = new String("Seattle.gov Animal Shelter;;"+R.drawable.doc+";;"+R.drawable.doc+";;http://www.trumba.com/calendars/animalshelter.rss;;item;;title,link,date,content,guid;;title,link,pubDate,description,guid;;2;;false").split(";;");
		dataFeed[14] = new String("SPD Blotter;;"+R.drawable.doc+";;"+R.drawable.doc+";;http://feeds.feedburner.com/spdblotter?format=xml;;item;;title,link,date,content,guid;;title,link,pubDate,description,guid;;2;;false").split(";;");
		dataFeed[15] = new String("Seattle Land Use Notices;;"+R.drawable.doc+";;"+R.drawable.doc+";;http://web1.seattle.gov/dpd/luib/RSSAllAreas.aspx;;item;;title,link,date,content,guid;;title,link,pubDate,description,guid;;2;;false").split(";;");
		dataFeed[16] = new String("City of Seattle Information Security;;"+R.drawable.doc+";;"+R.drawable.doc+";;http://www.seattle.gov/informationsecurity/infosec.xml;;item;;title,link,date,content,guid;;title,link,pubDate,description,guid;;2;;false").split(";;");
		dataFeed[17] = new String("Seattle City Light - Lightreading;;"+R.drawable.doc+";;"+R.drawable.doc+";;http://www.seattle.gov/light/rss/lightReading.asp;;item;;title,link,date,content,guid;;title,link,pubDate,description,guid;;2;;false").split(";;");
		dataFeed[18] = new String("Arts and Culture Affairs;;"+R.drawable.doc+";;"+R.drawable.doc+";;http://www.trumba.com/calendars/arts-and-cultural-affairs.rss;;item;;title,link,date,content,guid;;title,link,pubDate,description,guid;;2;;false").split(";;");
		dataFeed[19] = new String("Funding Opportunities;;"+R.drawable.doc+";;"+R.drawable.doc+";;http://feeds.feedburner.com/FundingOpportunities?format=xml;;item;;title,link,date,content,guid;;title,link,pubDate,description,guid;;2;;false").split(";;");
		dataFeed[20] = new String("Calls for Artists;;"+R.drawable.doc+";;"+R.drawable.doc+";;http://feeds.feedburner.com/CallsForArtists?format=xml;;item;;title,link,date,content,guid;;title,link,pubDate,description,guid;;2;;false").split(";;");
		dataFeed[21] = new String("Job Opportunities;;"+R.drawable.doc+";;"+R.drawable.doc+";;http://feeds.feedburner.com/seattle/JobOpportunities?format=xml;;item;;title,link,date,content,guid;;title,link,pubDate,description,guid;;2;;false").split(";;");
		dataFeed[22] = new String("Training Opportunities;;"+R.drawable.doc+";;"+R.drawable.doc+";;http://feeds.feedburner.com/Seattle/TrainingOpportunities;;item;;title,link,date,content,guid;;title,link,pubDate,description,guid;;2;;false").split(";;");
		//dataFeed[6] = new String("name;;icon;;image;;url;;itemtest;;title,link,date,content,guid;;title,link,pubDate,description,link;;2;;false").split(";;");
		//dataFeed[6] = new String("name;;icon;;image;;url;;itemtest;;title,link,date,content,guid;;title,link,pubDate,description,link;;2;;false").split(";;");
		//dataFeed[6] = new String("name;;icon;;image;;url;;itemtest;;title,link,date,content,guid;;title,link,pubDate,description,link;;2;;false").split(";;");
		//dataFeed[6] = new String("name;;icon;;image;;url;;itemtest;;title,link,date,content,guid;;title,link,pubDate,description,link;;2;;false").split(";;");
		//dataFeed[6] = new String("name;;icon;;image;;url;;itemtest;;title,link,date,content,guid;;title,link,pubDate,description,link;;2;;false").split(";;");
		//dataFeed[6] = new String("name;;icon;;image;;url;;itemtest;;title,link,date,content,guid;;title,link,pubDate,description,link;;2;;false").split(";;");
		//dataFeed[6] = new String("name;;icon;;image;;url;;itemtest;;title,link,date,content,guid;;title,link,pubDate,description,link;;2;;false").split(";;");
		
		dataFeed[23] = new String("Sea Shepherd Conservation Society;;"+R.drawable.sscs+";;"+R.drawable.contactseashepherd2+";;http://www.seashepherd.org/news-and-media/sea-shepherd-news/feed/rss.html;;item;;title,link,date,content,guid;;title,link,pubDate,description,link;;2;;false").split(";;");
		dataFeed[24] = new String("The White House Blog;;"+R.drawable.usseal+";;"+R.drawable.whitehousegroup+";;http://www.whitehouse.gov/feed/blog/whitehouse/;;entry;;title,link,date,author,summary,content,guid;;title,link href,updated,name,summary,content,id;;;;false").split(";;");
		//dataFeed[2] = new String("Associated Press;;"+R.drawable.apicon+";;"+R.drawable.apuplink+";;http://hosted.ap.org/lineups/TOPHEADS-rss_2.0.xml?SITE=ALMON&SECTION=HOME;;item;;title,link,date,author,summary,guid;;title,link,pubDate,author,description,guid;;2;;true").split(";;");
		dataFeed[25] = new String("TED; Ideas worth spread: Talks;;"+R.drawable.ted+";;"+R.drawable.tedtop+";;http://feeds.feedburner.com/tedtalks_video;;itemtest;;title,link,date,author,summary,content,guid;;title,link,pubDate,itunes:author,itunes:summary,description,guid;;2;;false").split(";;");
		
		
		// TED Talks;;http://feeds.feedburner.com/tedtalks_video
		dataFeed[26] = new String("TED; Ideas worth spreading: Blog;;"+R.drawable.ted+";;"+R.drawable.tedtop+";;http://feeds.feedburner.com/tedblog;;entry;;title,link,date,author,summary,content,guid;;title,link href,updated,name,summary,content,id;;;;false").split(";;");
		// TED Blog;;http://feeds.feedburner.com/tedblog
		//dataFeed[27] = new String("NPR News;;"+R.drawable.npr+";;"+R.drawable.nprtop+";;http://www.npr.org/rss/rss.php?id=1001;;item;;title,link,date,summary,content,guid;;title,link,pubDate,description,content:encoded,guid;;2;;false").split(";;");
		// NPR News;;http://www.npr.org/rss/rss.php?id=1001
		//dataFeed[28] = new String("NPR All Things Considered;;"+R.drawable.npr+";;"+R.drawable.nprtop+";;http://www.npr.org/rss/rss.php?id=2;;item;;title,link,date,summary,content,guid;;title,link,pubDate,description,content:encoded,guid;;2;;false").split(";;");
		// NPR All Things Considered;;http://www.npr.org/rss/rss.php?id=2
		//dataFeed[29] = new String("The Economist;;"+R.drawable.theeconomist+";;"+R.drawable.theeconomisttop+";;http://www.economist.com/rss/daily_news_and_views_rss.xml;;item;;title,link,date,content,guid;;title,link,pubDate,description,guid;;2;;false").split(";;");
		// The Economist;;http://www.economist.com/rss/daily_news_and_views_rss.xml
		//dataFeed[7] = new String("The Economist;;"+R.drawable.theeconomist+";;"+R.drawable.theeconomist+";;http://www.economist.com/rss/daily_news_and_views_rss.xml;;item;;title,link,date,content;;title,link,pubDate,description;;2;;false").split(";;");
		// The Economist;;http://www.economist.com/rss/full_print_edition_rss.xml
		//dataFeed[7] = new String("The Economist;;"+R.drawable.theeconomist+";;"+R.drawable.theeconomist+";;http://www.economist.com/rss/daily_news_and_views_rss.xml;;item;;title,link,date,content;;title,link,pubDate,description;;2;;false").split(";;");
		// The Economist;;http://www.economist.com/rss/the_world_this_week_rss.xml
		//dataFeed[8] = new String("BBC News;;"+R.drawable.bbc+";;"+R.drawable.bbctop+";;http://newsrss.bbc.co.uk/rss/newsonline_world_edition/front_page/rss.xml;;item;;title,link,date,content,guid;;title,link,pubDate,description,guid;;2;;false").split(";;"); // category, media:thumbnail
		// BBC News;;http://newsrss.bbc.co.uk/rss/newsonline_world_edition/front_page/rss.xml
		//dataFeed[9] = new String("The Onion;;"+R.drawable.theonion+";;"+R.drawable.theonion+";;http://feeds.theonion.com/theonion/daily;;item;;title,link,date,content;;title,link,pubDate,description;;2;;false").split(";;"); 
		// The Onion;;http://feeds.theonion.com/theonion/daily
		//dataFeed[10] = new String("FARK;;"+R.drawable.fark+";;"+R.drawable.fark+";;http://www.fark.com/fark.rss;;item;;title,link,date,content,guid;;title,link,pubDate,description,guid;;2;;false").split(";;"); // media:title, media:thumbnail
		// FARK;;http://www.fark.com/fark.rss
		// CFCA
		// http://www.whitehouse.gov/feed/press/
		//dataFeed[9] = new String("The White House Press Office;;"+R.drawable.usseal+";;"+R.drawable.whitehousegroup+";;http://www.whitehouse.gov/feed/press/;;entry;;title,link,date,summary,content,guid;;title,link href,updated,summary,content,id;;;;false").split(";;");
		//http://www.whitehouse.gov/feed/gallery/
		//dataFeed[10] = new String("The White House Photo Gallery;;"+R.drawable.usseal+";;"+R.drawable.whitehousegroup+";;http://www.whitehouse.gov/feed/gallery/;;entry;;title,link,date,summary,guid;;title,link href,updated,summary,id;;;;false").split(";;");
		
		dataFeed[27] = new String("Doc Chomps Software;;"+R.drawable.docdot+";;"+R.drawable.doctop+";;http://www.skysfamily.com/skysfamily.com/Doc_Chomps_Software/rss.xml;;item;;title,link,date,content,guid;;title,link,pubDate,description,guid;;2;;false").split(";;");

		//http://blog.makezine.com/index.xml
		//dataFeed[12] = new String("MAKE Magazine;;"+R.drawable.make+";;"+R.drawable.maketop+";;http://blog.makezine.com/index.xml;;item;;title,link,date,content,guid,author;;title,link,pubDate,description,guid,author;;2;;false").split(";;");
		//dataFeed[12] = new String("Tinkering School;;"+R.drawable.tinkeringschool+";;"+R.drawable.tinkeringschooltop+";;http://www.tinkeringschool.com/blog/feed/;;item;;title,link,date,content,guid;;title,link,pubDate,description,guid;;2;;false").split(";;");
		
		//dataFeed[13] = new String("Scientific American;;"+R.drawable.scientificamerican+";;"+R.drawable.scientificamericantop+";;http://rss.sciam.com/ScientificAmerican-Global;;item;;title,link,date,content,guid;;title,link,pubDate,description,link;;2;;false").split(";;");
		
		//http://twitter.com/statuses/user_timeline/14927297.rss
		//dataFeed[14] = new String("Ad Busters;;"+R.drawable.adbusters+";;"+R.drawable.adbusterstop+";;https://www.adbusters.org/recent/feed;;item;;title,link,date,content,guid;;title,link,pubDate,description,guid;;2;;false").split(";;");

		
		//*/
		
	}
	
	
	public static String MAINURI = "com.havenskys.thescoopseattle";
	
	
	public static String TAG = "Custom";
	public static final String DATABASE_NAME = "articles.db";
    public static final String DATABASE_TABLE_NAME = "articles";
    public String SQL = "";
	public static int NOTIFY_ID = -2001;
	public static int NOTIFY_ID_ARTICLE = -2002;
	public int RESTARTMIN = 30;
	
    
	// CUSTOM
	//title, link, id, updated, summary, content
    public static final String ID           = "_id";
    public static final String TYPE         = "type";
    public static final String TITLE        = "title";
    public static final String LINK      	= "link";
    public static final String DATE         = "date";
    public static final String SUMMARY      = "summary";
    public static final String CONTENT      = "content";
    public static final String CONTENTURL   = "contenturl";
    //public static final String DESCRIPTION  = "description";
    public static final String AUTHOR       = "author";
    public static final String GUID       = "guid";
    //public static final String SUBTITLE     = "subtitle";
    //public static final String CATEGORY     = "category";
    //public static final String MEDIA        = "media";
    //public static final String MEDIATYPE    = "mediatype";
    //public static final String MEDIADURATION = "mediaduration";
    //public static final String MEDIASIZE    = "mediasize";
    public static final String LAST_UPDATED = "lastupdated";
    public static final String CREATED      = "created";
    public static final String STATUS       = "status";
    public static final String READ         = "read";
    public static final String SEEN         = "seen";
    public static final String DEFAULT_SORT_ORDER = CREATED + " DESC";
    
	// CUSTOM
	public String getContentSQL() {
		
		SQL = "CREATE TABLE " + DATABASE_TABLE_NAME + "(" +
        ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        TYPE + " NUMBER," +
        TITLE + " TEXT," +
        LINK + " TEXT UNIQUE," +
        DATE + " TEXT," +
        SUMMARY + " TEXT," +
        CONTENT + " TEXT," +
        CONTENTURL + " TEXT," +
        //DESCRIPTION + " TEXT," +
        AUTHOR + " TEXT," +
        GUID + " TEXT UNIQUE," +
        //SUBTITLE + " TEXT," +
        //CATEGORY + " TEXT," +
        //MEDIA + " TEXT," +
        //MEDIATYPE + " TEXT," +
        //MEDIADURATION + " TEXT," +
        //MEDIASIZE + " TEXT," +
        STATUS + " INTEGER DEFAULT 1," +
        CREATED + " INTEGER DEFAULT 0," +
        READ + " INTEGER DEFAULT 0," +
        SEEN + " INTEGER DEFAULT 0," +
        LAST_UPDATED + " INTEGER DEFAULT 0);";
		
		return SQL;

	}
	
	public void loadlist(listView view, boolean landscape) {
		i(TAG,"loadlist() ++++++++++++++++++++++++++++++++++");
		
		String upgrades = mSharedPreferences.getString("upgrades", "");
		String limit = " limit 50";
		/*/
		if( !upgrades.contains("Customize to Full Version.") ){
			limit = " limit 20";
		}//*/
		
		Cursor lCursor = null;
		
		// CUSTOM
		//String[] columns = new String[] {"_id", "title", "datetime(date,'localtime') as date", "summary"};
		
		
		String[] columns = new String[] {"_id", "type", "title", "datetime(date,'localtime') as date", "author", "summary", "seen", "read" };
		String[] from = new String[]{"type", "title", "date", "author", "summary", "seen", "read", "_id" };
		int[] to = new int[]{R.id.listrow_type, R.id.listrow_title, R.id.listrow_date, R.id.listrow_author, R.id.listrow_summary, R.id.listrow_seen, R.id.listrow_read, R.id.listrow_rowid };
        
		lCursor = SqliteWrapper.query(view, view.getContentResolver(), DataProvider.CONTENT_URI, 
        		columns,
        		"status > 0 and ( (read == 0 or read > "+(System.currentTimeMillis() - (60 * 60 * 1000) )+") and ( seen == 0 or seen > "+(System.currentTimeMillis() - (5 * 60 * 1000) )+" ) ) or (read > 0 AND read < "+Custom.LAST_UPDATED+")", // Future configurable time to expire seen and unread
        		null, 
        		"datetime(date) desc"+limit);// + startrow + "," + numrows
		
		view.startManagingCursor(lCursor);
		int count = lCursor.getCount();
        SimpleCursorAdapter entries = new SimpleCursorAdapter(view, R.layout.listrow, lCursor, from, to);
        //RelativeLayout tv = (RelativeLayout) view.findViewById(R.layout.listrow);
        //view.getListView().addHeaderView(tv);
        //RelativeLayout ll = new RelativeLayout(view); 
        //ll.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, 100));
        
        
        
        if( view.getListAdapter() == null ){
	        
	        LinearLayout lFooter = new LinearLayout(view); 
	        lFooter.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.FILL_PARENT, 65));
	        
	        //ll.setTag( new String("header") );
	        //ll.setBackgroundColor(Color.RED); 
	        //view.getListView().addHeaderView(ll);
	        //view.getListView().addHeaderView(ll, null, false);
	        //lFooter.setTag( new String("footer") );
	        //ll.setBackgroundColor(Color.BLACK);
	        //RelativeLayout ll = (RelativeLayout) view.findViewById(R.layout.listfooter);
	        view.getListView().addFooterView(lFooter, null, false);
	        
	        LinearLayout lHeader = new LinearLayout(view);
	        //lHeader.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.FILL_PARENT, 100));
	        lHeader.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.FILL_PARENT, ListView.LayoutParams.WRAP_CONTENT));
	        
	        //lHeader.setTag( new String("header") );
	        TextView tv = new TextView(mContext);
	        tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT) );
	        tv.setText("\n\n");
	        //tv.setTextSize(18);
			//tv.setShadowLayer( (float) 2.0, (float) 0.5, (float) 0.5, (int) Color.BLACK);
			//tv.setTextColor(Color.WHITE);
			lHeader.setPadding(5, 10, 5, 10);
	        lHeader.addView(tv);
			view.getListView().addHeaderView(lHeader, null, true);
		}
        
        
        view.setListAdapter(entries);
        //view.getListView().setFilterText("title");
        //view.getListView().setTextFilterEnabled(true);
		
	}
	
	
	public void loadlist(listSave view, boolean landscape) {
		i(TAG,"loadlist Save() ++++++++++++++++++++++++++++++++++");
		
		String upgrades = mSharedPreferences.getString("upgrades", "");
		String limit = "";
		/*/
		if( !upgrades.contains("Customize to Full Version.") ){
			limit = " limit 20";
		}//*/
		Cursor lCursor = null;
		
		// CUSTOM
		//String[] columns = new String[] {"_id", "title", "datetime(date,'localtime') as date", "summary"};
		
		
		String[] columns = new String[] {"_id", "type", "title", "datetime(date,'localtime') as date", "author", "summary", "seen", "read" };
		String[] from = new String[]{"type", "title", "date", "author", "summary", "seen", "read", "_id" };
		int[] to = new int[]{R.id.listrow_type, R.id.listrow_title, R.id.listrow_date, R.id.listrow_author, R.id.listrow_summary, R.id.listrow_seen, R.id.listrow_read, R.id.listrow_rowid };
        
		lCursor = SqliteWrapper.query(view, view.getContentResolver(), DataProvider.CONTENT_URI, 
        		columns,
        		"status > 0 and read > 0", // Future configurable time to expire seen and unread
        		null, 
        		"datetime(date) desc" + limit);
		
		view.startManagingCursor(lCursor);
		
        SimpleCursorAdapter entries = new SimpleCursorAdapter(view, R.layout.listrow, lCursor, from, to);
        
        if( view.getListAdapter() == null ){
	        LinearLayout lFooter = new LinearLayout(view); 
	        lFooter.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.FILL_PARENT, 65));
	        view.getListView().addFooterView(lFooter, null, false);
	        
	        LinearLayout lHeader = new LinearLayout(view);
	        lHeader.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.FILL_PARENT, ListView.LayoutParams.WRAP_CONTENT));
	        TextView tv = new TextView(mContext);
	        tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT) );
	        tv.setText("\n\n");
			lHeader.setPadding(5, 10, 5, 10);
	        lHeader.addView(tv);
			view.getListView().addHeaderView(lHeader, null, true);
		}
        
        view.setListAdapter(entries);
        //view.getListView().setTextFilterEnabled(true);
				
	}

	public void loadlist(listOut view, boolean landscape) {
		i(TAG,"loadlist Out() ++++++++++++++++++++++++++++++++++");
		
		String upgrades = mSharedPreferences.getString("upgrades", "");
		String limit = "";
		/*/
		if( !upgrades.contains("Customize to Full Version.") ){
			limit = " limit 40";
		}//*/
		
		Cursor lCursor = null;
		
		// CUSTOM
		//String[] columns = new String[] {"_id", "title", "datetime(date,'localtime') as date", "summary"};
		
		
		String[] columns = new String[] {"_id", "type", "title", "datetime(date,'localtime') as date", "author", "summary", "seen", "read" };
		String[] from = new String[]{"type", "title", "date", "author", "summary", "seen", "read", "_id" };
		int[] to = new int[]{R.id.listrow_type, R.id.listrow_title, R.id.listrow_date, R.id.listrow_author, R.id.listrow_summary, R.id.listrow_seen, R.id.listrow_read, R.id.listrow_rowid };
        
		lCursor = SqliteWrapper.query(view, view.getContentResolver(), DataProvider.CONTENT_URI, 
        		columns,
        		"status > 0 and read == 0 and ( seen == 0 or seen > "+(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000) )+" )", // Future configurable time to expire seen and unread
        		null, 
        		"datetime(date) desc" + limit);// limit 21,20
		
		view.startManagingCursor(lCursor);
		int count = lCursor.getCount();
        SimpleCursorAdapter entries = new SimpleCursorAdapter(view, R.layout.listrow, lCursor, from, to);
        
        if( view.getListAdapter() == null ){
	        LinearLayout lFooter = new LinearLayout(view); 
	        lFooter.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.FILL_PARENT, 65));
	        view.getListView().addFooterView(lFooter, null, false);
	        
	        LinearLayout lHeader = new LinearLayout(view);
	        lHeader.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.FILL_PARENT, ListView.LayoutParams.WRAP_CONTENT));
	        TextView tv = new TextView(mContext);
	        tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT) );
	        tv.setText("\n\n");
			lHeader.setPadding(5, 10, 5, 10);
	        lHeader.addView(tv);
			view.getListView().addHeaderView(lHeader, null, true);
		}
		
        view.setListAdapter(entries);
        //view.getListView().setTextFilterEnabled(true);
			
	}

	
	
	
	
	private String memoryBox;
	public boolean parseEntries(String who, int feedid, String httpPage) {
		i(TAG,"parseEntries() ++++++++++++++++++++++++++++++++++");
		
		String longname = dataFeed[feedid][0];
		int icon = Integer.parseInt(dataFeed[feedid][1]);
		int topimage = Integer.parseInt(dataFeed[feedid][2]);
		String baseurl = dataFeed[feedid][3];
		String entryTag = dataFeed[feedid][4];
		String[] parseColumns = dataFeed[feedid][5].split(",");
		String[] parseTags = dataFeed[feedid][6].split(",");
		String[] parseFixDates = dataFeed[feedid][7].split(",");
		boolean getArticle = Boolean.parseBoolean(dataFeed[feedid][8]);

		int successLimit = Custom.LIMITDOWNLOAD * 2;
		
		String upgrades = mSharedPreferences.getString("upgrades", "");
		/*/
		if( !upgrades.contains("Customize to Full Version.") ){
			successLimit = 2;
		}//*/
		
		i(TAG,"parseEntries() 261");
		int successCount = 0;
		int newCount = 0;
		int syncLoad = mSharedPreferences.contains("syncload") ? mSharedPreferences.getInt("syncload",4) : 4;

		ContentValues parsedValues = null;
		String[] parseTagParts = new String[2];
		int parseI = 0;
		int parseLen = parseTags.length;
		//.replaceAll("></", ">\n</")
		i(TAG,"parseEntries() page size("+httpPage.length()+") 271");
		
		//{
		String waitname = "wait_parseEntries_415";
		long waitstart = waitStart(waitname, 1000);
		//waitUpdate(waitname, waitstart);
			
		String[] input = null;
		//String httpPage = new String(httpPage);
		
		try {
			long freememory = Runtime.getRuntime().freeMemory();
			i(TAG,"parseEntries() 279 try Start free memory("+Runtime.getRuntime().freeMemory()+")");
			//if( httpPage.matches("<"+entryTag+">") ){
				//i(TAG,"parseEntries() 278");
			input = httpPage.replaceAll("<"+entryTag+">", "\n<"+entryTag+">\n").replaceAll("</"+entryTag+">", "\n</"+entryTag+">\n").replaceAll("/>", "/>\n").replaceAll("\r", "\n").split("\n");
			//}
			//if( httpPage.matches("</"+entryTag+">") ){
				//i(TAG,"parseEntries() 280");
				//httpPage = httpPage.replaceAll("</"+entryTag+">", "\n</"+entryTag+">\n");
			//}
			//if( httpPage.matches("/>") ){
				//i(TAG,"parseEntries() 282");
				//httpPage = httpPage.replaceAll("/>", "/>\n");
			//}
			//if( httpPage.matches("\r") ){
				//i(TAG,"parseEntries() 284");
				//httpPage = httpPage.replaceAll("\r", "\n");
			//}
		} catch( OutOfMemoryError e){
			e(TAG,"OutOfMemoryError while parsing httpPage, exit. (gracefully if possible)");
			SystemClock.sleep(2000);
			return false;
		}
			
		waitUpdate(waitname, waitstart);
		//}
		
		//w(TAG,"parseEntries() 292 Split page free memory("+Runtime.getRuntime().freeMemory()+")");
		//String[] input = httpPage.split("\n");
		httpPage = "";
		
		long feedactive = System.currentTimeMillis();
		mPreferencesEditor.putLong("lastfeedactive", feedactive);
		mPreferencesEditor.commit();
		
		i(TAG,"parseEntries() 296 content loop start free memory("+Runtime.getRuntime().freeMemory()+")");
		for( int i = 0; i < input.length; i++){
			
			if( successCount >= successLimit ){
				e(TAG,"parseEntries() 315 reached success count limit("+successCount+")");
				return true;
			}
			
			//if( successCount >= LIMITDOWNLOAD ){
				//i(TAG,"getlatest() 314 newCount reached limit " + LIMITDOWNLOAD);
				//return true;
			//}
			
			if( input[i].length() == 0 ){ continue; }
			if( !QUITEFEED ){
				i(TAG,"Line("+i+") " + input[i] );
			}
			if( input[i].contains("<"+entryTag+">") ){
				if( (System.currentTimeMillis() - feedactive) > 5 * 1000 ){
					feedactive = System.currentTimeMillis();
					mPreferencesEditor.putLong("lastfeedactive", feedactive);
					mPreferencesEditor.commit();
				}
				
				parsedValues = new ContentValues();
				
				
				/*/String/*/ waitname = "wait_parseEntries_486_" + feedid;
				/*/long/*/ waitstart = waitStart(waitname, 1000);
				//waitUpdate(waitname, waitstart);
				for(i++; i < input.length; i++){
					if( input[i].length() == 0 ){ continue; }
					if( !QUITEFEED ){
						i(TAG,"Item Line("+i+") " + input[i] );
					}
					if( input[i].contains("<"+entryTag+">") ){ /*Go Back in the itteration, we've somehow missed the </item> tag */ i--;	break; }
					if( input[i].contains("</"+entryTag+">") ){ 	break; }
					
					for(parseI = 0; parseI < parseLen; parseI++){
						parseTagParts = parseTags[parseI].split(" ", 2);
						if( input[i].contains("<"+parseTagParts[0]+">") || input[i].contains("<"+parseTagParts[0]+" ") ){
							if( parseTagParts.length == 2 ){
								parsedValues.put(parseColumns[parseI], getTagValue(who + " parseEntries() 153 ", input[i], parseTagParts[0], parseTagParts[1]) );
							}else{
								if( !input[i].contains("</"+parseTagParts[0]+">") ){
								//if( !input[i].contains("</"+parseTagParts[0]+">") && !input[i].contains("/>") ){
									for(int c = i; c < input.length; c++){ 
										input[i] += input[c];
										if( input[c].contains("</"+parseTagParts[0]+">") || input[c].contains("<"+parseTagParts[0]+" />") ){ break; }
									}
								}
								parsedValues.put(parseColumns[parseI], getTagContent(who + " parseEntries() 161 ", input[i], parseTagParts[0]) );
							}
						}
					}
				}
				
				waitUpdate(waitname, waitstart);
				
				
				
				//if( !parsedValues.containsKey(Custom.GUID) || parsedValues.getAsString(Custom.GUID).length() == 0 ){
					//w(TAG,"No GUID found for article, using LINK instead.");
					//parsedValues.put(Custom.GUID, parsedValues.getAsString(Custom.LINK) );
				//}
				
				int parseVerifyCount = 0;
				String logline = "";
				for(parseI = 0; parseI < parseLen; parseI++){
					String s = parsedValues.getAsString(parseColumns[parseI]);
					if( s != null ){
						if( s.length() > 200 ){
							s = s.replaceAll("\n.*", "<<< SHORTENED from " + s.length() + ">>>");
						}
						logline += parseI + "(" + parseColumns[parseI] + ":"+ s +") ";
						if( s.length() > 0 ){ }
						parseVerifyCount ++;
					}else{
						e(TAG,"Parse Failure? "+parseI+"("+parseColumns[parseI]+")");
					}
				}
				i(TAG,"parseEntries() 346 Parsed expected("+parseLen+") verified("+parseVerifyCount+") " + logline);
					
				
				// CUSTOM
				if( parseVerifyCount == parseLen ){
					successCount++;
					Cursor c = null;
					
					int status = 1;
					
					
					long rowid = -1;
					long readtime = -1;
					String lastdate = "";
					int lastfeedid = -1;
					String lastguid = "";
					
					i(TAG,"parseEntries() 367 required fields verified");
					
					try {
						//i(TAG,"parseEntries() 372 quering sql server for existence");
						c = SqliteWrapper.query(mContext, mResolver, DataProvider.CONTENT_URI, new String[] {"_id","date","read","type","guid"} , Custom.GUID + " = \""+parsedValues.getAsString(Custom.GUID)+"\"", null, null);
						//i(TAG,"parseEntries() 371");
						if( c != null ){ if( c.moveToFirst() ){ rowid = c.getLong(0); lastdate = c.getString(1); readtime = c.getLong(2); lastfeedid = c.getInt(3); lastguid = c.getString(4); } c.close(); }
					} catch (SQLiteException e){
						w(TAG,"SQLiteException " + e.getLocalizedMessage());
					}
					
					
					if( (System.currentTimeMillis() - feedactive) > 5 * 1000 ){
						feedactive = System.currentTimeMillis();
						mPreferencesEditor.putLong("lastfeedactive", feedactive);
						mPreferencesEditor.commit();
					}
					
					if( rowid < 0 ){
						
						i(TAG,"parseEntries() 379 record confirmed as new id("+rowid+"), confirming new Title("+parsedValues.getAsString(Custom.TITLE)+")");
						
						//parsedValues.put(Custom.TITLE, parsedValues.getAsString(Custom.TITLE).replaceAll("\"", "'") );
						String lastsummary = "";
						
						try {
							c = SqliteWrapper.query(mContext, mResolver, DataProvider.CONTENT_URI, new String[] {"_id","date","read","summary","type","guid"} , Custom.TITLE + " = \""+parsedValues.getAsString(Custom.TITLE)+"\"", null, null);
							if( c != null ){ if( c.moveToFirst() ){ rowid = c.getLong(0); lastdate = c.getString(1); readtime = c.getLong(2); lastsummary = c.getString(3); lastfeedid = c.getInt(4); lastguid = c.getString(5);} c.close(); }
						} catch (SQLiteException e){
							w(TAG,"SQLiteException " + e.getLocalizedMessage());
						}
						
						if( rowid > 0 ){
							i(TAG,"parseEntries() 624");
							
							if( lastsummary.length() > 0 ){
								
								if( !parsedValues.containsKey("summary") && parsedValues.containsKey("content") ){
									String summary = parsedValues.getAsString("content").replaceAll("<.*?>", "");
									int endlimit = summary.length();
									if( endlimit > 100 ){
										summary = summary.substring(0, 100);
									}else if( endlimit > 0 ){
										// summary is less than 100 but more than nothing
									}
									parsedValues.put(Custom.SUMMARY, summary);
								}
								
								// found a title match after not finding a GUID match, verify content/summary
								if( parsedValues.getAsString(Custom.SUMMARY).contains(lastsummary) ){
									e(TAG,"Duplicate Match found with title("+parsedValues.getAsString(Custom.TITLE)+") lastfeedid("+lastfeedid+") lastguid("+lastguid+") feedid("+feedid+") guid("+parsedValues.getAsString(Custom.GUID)+") and summary, setting status to 0 and allowing record to be updated/created");
									status = 0;
								}else{
									i(TAG,"Duplicate Not Match found with title("+parsedValues.getAsString(Custom.TITLE)+") lastfeedid("+lastfeedid+") feedid("+feedid+")");
								}
								
							}else{
								i(TAG,"parseEntries() 648");
							}
							
							rowid = -1;
							readtime = -1;
							lastdate = "";
							lastsummary = "";
							lastfeedid = -1;
						}
					}
					
					i(TAG,"parseEntries() 420");
					
					for( int f = 0; f < parseFixDates.length; f++ ){
						i(TAG,"parseEntries() 662");
						if( parseFixDates[f] == null ){ continue; }
						i(TAG,"parseEntries() 664");
						if( parseFixDates[f].length() == 0 ){ continue; }
						//
						i(TAG,"parseEntries() 667 parseFixDates("+parseFixDates[f]+")");
						
						int fixdatenum = 0;
						try {
							fixdatenum = Integer.parseInt(parseFixDates[f]);
						} catch(NumberFormatException e){
							e(TAG,"fixDate() 994 Number FormatException original("+parseFixDates[f]+") policy(moving on)");
						}
						String date = parsedValues.getAsString(parseColumns[fixdatenum]);
						parsedValues.put(parseColumns[fixdatenum], fixDate(date,TAG + " parseEntries() for " + who) );
						//i(TAG,"Fixing Date("+parseColumns[fixdatenum]+") PreviousValue("+date+") NewValue("+parsedValues.getAsString(parseColumns[fixdatenum])+")");
					}
					
					i(TAG,"parseEntries() 669");
					boolean update = false;
					//long rowid = getId(DataProvider.CONTENT_URI.toString(), Custom.LINK + " = \""+parsedValues.getAsString(Custom.LINK)+"\"");
					if( rowid > -1 ){
						
						//update if required
						
						// This will block until load is low or time limit exceeded
				        //loadLimit(TAG + " getlatest() 194", syncLoad+2 , 10 * 1000, 30 * 1000);
						if( !lastdate.contains(parsedValues.getAsString("date")) ){
							i(TAG,"Update blogitem("+rowid+") title(" + parsedValues.getAsString(Custom.TITLE) + ") guid(" + parsedValues.getAsString(Custom.GUID) + ") link(" + parsedValues.getAsString(Custom.LINK) + ") lastdate("+lastdate+") date("+parsedValues.getAsString("date")+")");
							update = true;
						}else{
							i(TAG,"Unchanged blogitem("+rowid+") lastdate("+lastdate+") newdate("+parsedValues.getAsString("date")+") title(" + parsedValues.getAsString(Custom.TITLE) + ") guid(" + parsedValues.getAsString(Custom.GUID) + ") link(" + parsedValues.getAsString(Custom.LINK) + ")");
						}
						
					}
					i(TAG,"parseEntries() 686");
					if( rowid < 0 || update ){
						//i(TAG,"parseEntries() 688");
						/*
						if( feedid == 5 || feedid == 6 ){
							i(TAG,"NPR Fix Content");
							parsedValues.put(Custom.CONTENT, parsedValues.getAsString(Custom.CONTENT).replaceFirst("<p><a href.*?email.>.*", ""));
						}//*/
						/*
						if( feedid == 13 ){ 
							i(TAG,"Scientific American Fix Content");
							parsedValues.put(Custom.CONTENT, parsedValues.getAsString(Custom.CONTENT).replaceFirst("\\[More\\]<.*", "</a>"));
						}//*/
						/*
						if( feedid == 14 ){
							//i(TAG,"Ad Busters Fix Content and Title");
							//parsedValues.put(Custom.CONTENT, parsedValues.getAsString(Custom.CONTENT).replaceFirst("adbusters: ", ""));
						}//*/
						i(TAG,"parseEntries() 703");
						if( !parsedValues.containsKey("summary") && parsedValues.containsKey("content") ){
							String summary = parsedValues.getAsString("content").replaceAll("<.*?>", "");
							int endlimit = summary.length();
							if( endlimit > 100 ){
								summary = summary.substring(0, 100);
							}else if( endlimit > 0 ){
								// summary is less than 100 but more than nothing
							}
							parsedValues.put(Custom.SUMMARY, summary);
						}
						
						//parsedValues.put(Custom.TITLE, parsedValues.getAsString(Custom.TITLE).replaceAll("\"", "'") );
						//.replaceAll("\"", "'")
						
						
						
						// This will block until load is low or time limit exceeded
				        //loadLimit(TAG + " getlatest() 194", syncLoad , 10 * 1000, 30 * 1000);
						
						// 0   1   2   3      4       5
						//day, D2 mon YEA4 HH:MM:SS +0000
						//YYYY-MM-DD HH:MM:SS
						// Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec
						//updated = fixDate(updated);
						
						
						
						/*
						if( getArticle && parsedValues.containsKey("link") ){
							
							String link = parsedValues.getAsString("link");
							
							String content = "";
							String contenturl = "";
							HTTPClient sp = new HTTPClient(mContext);
							i(TAG,"getBasePage() 590 " + baseurl);
							String httpStatus = sp.safeHttpGet(TAG + " getlatest() 210", new HttpGet(link) );
							if( httpStatus.contains("200") ){
								content = sp.getHttpPage();
								contenturl = sp.getUrl();
								parsedValues.put(Custom.CONTENT, content);
								parsedValues.put(Custom.CONTENTURL, contenturl);
								//articlecontenttype = sp.getContentType();
								//articlecontentencoding = sp.getContentEncoding();
								Log.i(TAG,"Download of article successful articleurl("+contenturl+")");
							}else{
								Log.e(TAG,"Download of article failed");
							}
							
						}//*/
						
						parsedValues.put(Custom.TYPE, feedid);
						parsedValues.put(Custom.LAST_UPDATED, System.currentTimeMillis());
						
						
						if( rowid < 0 ){
							
							i(TAG,"New title(" + parsedValues.getAsString(Custom.TITLE) + ") guid(" + parsedValues.getAsString(Custom.GUID) + ") link(" + parsedValues.getAsString(Custom.LINK) + ")");
							
							parsedValues.put(Custom.STATUS, status);
							parsedValues.put(Custom.CREATED, System.currentTimeMillis());
							
							try {
								SqliteWrapper.insert(mContext, mResolver, DataProvider.CONTENT_URI, parsedValues);
							} catch (SQLiteException e){
								e(TAG,"SQLiteException " + e.getLocalizedMessage());
							}
							
						}else if(update){
							
							parsedValues.put(Custom.SEEN, 0);
							
							i(TAG,"Update blogitem("+rowid+") title(" + parsedValues.getAsString(Custom.TITLE) + ") guid(" + parsedValues.getAsString(Custom.GUID) + ") link(" + parsedValues.getAsString(Custom.LINK) + ") lastdate("+lastdate+") date("+parsedValues.getAsString("date")+")");
							
							try {
								SqliteWrapper.update(mContext, mResolver, DataProvider.CONTENT_URI, parsedValues, "_id = " + rowid, null);
							} catch (SQLiteException e){
								e(TAG,"SQLiteException " + e.getLocalizedMessage());
							}
							if( readtime > 0 ){
								//loadLimit(TAG + " getlatest() 523 name("+longname+") url("+baseurl+") for " + who, syncLoad, 5 * 1000, 10 * 1000);
								
								//disabled 2009-07-20
								setEntryNotification(TAG + " getlatest()", Custom.NOTIFY_ID_ARTICLE, rowid, icon, longname, "UPDATED " + parsedValues.getAsString(Custom.TITLE).replaceAll("<.*?>", " ").trim(), "UPDATED " + longname + ": " + parsedValues.getAsString(Custom.TITLE).replaceAll("<.*?>", " ").trim());
								SystemClock.sleep(1000);
							}
						}

						rowid = getId(DataProvider.CONTENT_URI.toString(), Custom.GUID + " = \""+parsedValues.getAsString(Custom.GUID)+"\"");
						if( rowid > 0 ){
							//i(TAG,"Confirmed Insert/Update blogitem("+rowid+") guid(" + parsedValues.getAsString(Custom.GUID) + ") link("+parsedValues.getAsString(Custom.LINK)+")");
							
							if( successCount == 1 ){
								// CUSTOM
								//loadLimit(TAG + " getlatest() 535 name("+longname+") url("+baseurl+") for " + who, syncLoad, 5 * 1000, 10 * 1000);
								//if( FREEVERSION ){
									//setEntryNotification(TAG + " getlatest()", Custom.NOTIFY_ID_ARTICLE, rowid, icon, Custom.APP + " (Customize $3)", parsedValues.getAsString(Custom.TITLE).replaceAll("<.*?>", " ").trim(), longname + ": " + parsedValues.getAsString(Custom.TITLE).replaceAll("<.*?>", " ").trim());
								//}else{
								
								//disabled 2009-07-20
								setEntryNotification(TAG + " getlatest()", Custom.NOTIFY_ID_ARTICLE, rowid, icon, longname, parsedValues.getAsString(Custom.TITLE).replaceAll("<.*?>", " ").trim(), longname + ": " + parsedValues.getAsString(Custom.TITLE).replaceAll("<.*?>", " ").trim());
								//}
								//SystemClock.sleep(500);
							}
						}else{
							e(TAG,"Unable to confirm Insert/Update guid(" + parsedValues.getAsString(Custom.GUID) + ") link("+parsedValues.getAsString(Custom.LINK)+")");
						}
						newCount++;
						if( successCount >= LIMITDOWNLOAD ){
							w(TAG,"getlatest() 314 newCount reached limit " + LIMITDOWNLOAD);
							SystemClock.sleep(500);
							return true;
						}
						
					}
				}
			}
		}
		
		if( (System.currentTimeMillis() - feedactive) > 5 * 1000 ){
			feedactive = System.currentTimeMillis();
			mPreferencesEditor.putLong("lastfeedactive", feedactive);
			mPreferencesEditor.commit();
		}
		
		return true;
	}

	

	private long waitStart(String waitname, int defaultWait) {
		
		long waittime = mSharedPreferences.getLong(waitname, defaultWait);
		w(TAG,"waiting("+waitname+") waittime("+waittime+")");
		SystemClock.sleep(waittime);
		long waitstart = System.currentTimeMillis();
		
		return waitstart;
	}

	private void waitUpdate(String waitname, long waitstart) {
		
		long waittime = mSharedPreferences.getLong(waitname, 1000);
		long waitactual = System.currentTimeMillis() - waitstart;
		
		if( waitactual > waittime * 0.8 ){
			long diff = waitactual - waittime;
			if( diff < 10000 ){
				long updatewait = 100;
				if( diff > 2000 ){ updatewait = 2000; }
				else if( diff > 1000 ){ updatewait = 1000; }
				else if( diff > 500 ){ updatewait = 500; }
				e(TAG,"(positive) increasing waittime of " +waitname+ " by "+updatewait+"ms, waittime("+waittime+") actual("+waitactual+")");
				waittime += updatewait;
			}
		}else if( waittime > waitactual * 1.2 ){
			
			long diff = waittime - waitactual;
			if( diff > 100 ){
				long updatewait = 100;
				if( diff > 2000 ){ updatewait = 2000; }
				else if( diff > 1000 ){ updatewait = 1000; }
				else if( diff > 500 ){ updatewait = 500; }
				e(TAG,"(positive) decreasing waittime of " +waitname+ " by "+updatewait+"ms, waittime("+waittime+") actual("+waitactual+")");
				waittime -= updatewait;
			}
		}
		
		if( waittime >= 100 ){
			mPreferencesEditor.putLong(waitname, waittime);
		}else{
			mPreferencesEditor.putLong(waitname, 101);
		}
		mPreferencesEditor.commit();
	}

	private String getTagContent(String who, String lines, String tag) {
		String value = "";
		if( lines.contains("<"+tag+" ") ){
			long time = SystemClock.currentThreadTimeMillis();
			value = lines.replaceFirst(".*<"+tag+" ", "").replaceFirst(">", "SPLIT-"+time+">").replaceFirst(".*SPLIT-"+time+">", "").replaceFirst("</"+tag+">.*", "");
		}else{
			value = lines.replaceFirst(".*<"+tag+">", "").replaceFirst("</"+tag+">.*", "");
		}
		if( value.contains("CDATA[") ){
			value = value.replaceFirst("\\]\\].*", "").replaceFirst(".*CDATA\\[", "");
		}
		//.replaceAll("\"", "&#34;")
		value = value.replaceAll("&gt;", ">").replaceAll("&lt;", "<").replaceAll("&#39;", "'").replaceAll("&amp;", "&").replaceAll("&apos;", "'").replaceAll("&#34;", "'").replaceAll("&quot;", "'").replaceAll("\"", "'");
		return value.trim();
	}
	
	private String getTagValue(String who, String line, String tag, String key) {
		String value = "";
		value = line.replaceFirst(".*<"+tag+" .*"+key+"=\"", "").replaceFirst("\".*", "").trim();
		return value;
	}


	
    private NotificationManager mNM;
    public void setNotificationManager(NotificationManager notifmgr){
    	mNM = notifmgr;
    }
    
    private SharedPreferences mSharedPreferences;
	private Editor mPreferencesEditor;
	public void setSharedPreferences(SharedPreferences sharedPreferences, Editor preferencesEditor) {
		mSharedPreferences = sharedPreferences;
		mPreferencesEditor = preferencesEditor;
		
		
		String upgrades = mSharedPreferences.getString("upgrades", "");
		/*/
		if( upgrades.contains("Customize to Full Version.") ){
			//boolean prevfv = FREEVERSION;
			//FREEVERSION = true;
			//e(TAG,"setSharedPreferences() FreeVersion("+FREEVERSION+") Upgraded to Full Version.");
		}//*/
		
		if( upgrades.contains("Customize for a farker.") ){
			
			String[][] newDataFeed = new String[dataFeed.length+1][9];
			for(int i = 0; i < dataFeed.length; i++){
				newDataFeed[i] = dataFeed[i];
			}
			
			//newDataFeed[dataFeed.length] = new String("FARK;;"+R.drawable.farkicon+";;"+R.drawable.farklogo2+";;http://www.fark.com/fark.rss;;item;;title,link,date,content,guid;;title,link,pubDate,description,guid;;2;;false").split(";;"); // media:title, media:thumbnail
			
			dataFeed = newDataFeed;
		}
	}
	
	
	private String[] mLogLines = null;
	private int mLogLooper, mLogLen;
	public void i(String who, String data){
		if( PUBLISH ){ return; }
		//mLogLines = data.split("\n");
		//mLogLen = mLogLines.length;
		//for (mLogLooper = 0; mLogLooper < mLogLen; mLogLooper++){ Log.i(APP +" "+ who, mLogLines[mLogLooper] + " " + SystemClock.currentThreadTimeMillis()); }
		String[] logLines = data.split("\n");
		int logLen = logLines.length;
		for (int logLooper = 0; logLooper < logLen; logLooper++){
			if( logLines[logLooper].length() > 200 ){
				Log.i(APP +" "+ who, logLines[logLooper].substring(0, 199)+ " " + SystemClock.currentThreadTimeMillis());
				Log.i(APP +" "+ who, logLines[logLooper].substring(199, logLines[logLooper].length()-1 )+ " " + SystemClock.currentThreadTimeMillis());
			}else{
				Log.i(APP +" "+ who, logLines[logLooper]+ " " + SystemClock.currentThreadTimeMillis());
			}
		}
	}
	
	public void w(String who, String data){
		if( PUBLISH ){ return; }
		String[] logLines = data.split("\n");
		int logLen = logLines.length;
		for (int logLooper = 0; logLooper < logLen; logLooper++){ Log.w(APP +" "+ who, logLines[logLooper]+ " " + SystemClock.currentThreadTimeMillis()); }
	}
	
	public void e(String who, String data){
		//mLogLines = data.split("\n");
		//mLogLen = mLogLines.length;
		//for (mLogLooper = 0; mLogLooper < mLogLen; mLogLooper++){ Log.e(APP +" "+ who, mLogLines[mLogLooper]+ " " + SystemClock.currentThreadTimeMillis()); }
		String[] logLines = data.split("\n");
		int logLen = logLines.length;
		for (int logLooper = 0; logLooper < logLen; logLooper++){ Log.e(APP +" "+ who, logLines[logLooper]+ " " + SystemClock.currentThreadTimeMillis()); }
	}

	
	private String fixDate(String origUpdated, String who) {
		i(TAG,"fixDate() 980 original("+origUpdated+") for " + who);
		String updated = origUpdated + " = = = = = =";
		// Thrusday, July 23 - 2:53pm
		// 0          1    2 3  4
		// Mon, 29 Jun 2009 12:37:29 -0700
		// 0    1   2   3   4         5
		// 24 Jul 2009 17:00:00 GMT
		// 0   1   2    3       4
		String[] dateparts = updated.split(" ");
		if( !dateparts[0].contains(",") && dateparts[3].contains(":") ){
			updated = "xxx, " + updated;
			dateparts = updated.split(" ");
		}
		if( dateparts.length > 4 ){
			
			Date d = new Date();
			
			i(TAG,"fixDate() 988");
			//String[] dayword = new String("xxx Sunday Monday Tuesday Wednesday Thrusday Friday Saturday xxx").split(" ");
			String[] monthlong = new String("xxx January February March April May June July August September October November December xxx").split(" ");
			String[] month = new String("xxx Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec xxx").split(" ");
			
			int mon = 0;
			for(;mon < month.length; mon++){
				if( month[mon].equalsIgnoreCase(dateparts[2]) ){ break; } 
			}
			if( mon == 13 ){
				i(TAG,"fixDate() 997");
				for(mon = 0;mon < monthlong.length; mon++){
					if( monthlong[mon].equalsIgnoreCase(dateparts[1]) ){ break; } 
				}
				i(TAG,"fixDate() 1001");
				if( mon == 13 ){
					i(TAG,"fixDate() 1002");
					e(TAG,"Unable to determine month in fixDate("+updated+") using current("+d.getMonth()+")");
					mon = d.getMonth();
					//return updated;
				}
			}
			i(TAG,"fixDate() 1008 mon("+mon+")");
			
			int year = d.getYear() + 1900;
			int day = d.getDay();
			if( dateparts[3] == "-" ){
				dateparts[5] = "-0700";
				i(TAG,"fixDate() 1014 year("+(d.getYear()+1900)+") dateparts[2]("+dateparts[2]+")");
				try {
					year = d.getYear() + 1900;
					day = Integer.parseInt(dateparts[2]);
				} catch(NumberFormatException e){
					e(TAG,"fixDate() 994 Number FormatException original("+updated+") using current("+year+"-"+day+")");
				}
			}else{
				String yearS = dateparts[3];
				String dayS = dateparts[1];
				i(TAG,"fixDate() 1022 dateparts[3]year("+dateparts[3]+",yearS) dateparts[1]day("+dateparts[1]+",dayS)");
				try {
					i(TAG,"fixDate() 1031");
					year = Integer.parseInt(yearS);
					i(TAG,"fixDate() 1033");
					day = Integer.parseInt(dayS);
					i(TAG,"fixDate() 1035");
				} catch(NumberFormatException e){
					i(TAG,"fixDate() 1037");
					e(TAG,"fixDate() 555 Number FormatException original("+updated+") using current("+year+"-"+day+")");
				}
				i(TAG,"fixDate() 1040");
			}
			//i(TAG,"fixDate() 1037 year("+year+") mon("+mon+") day("+day+") dateparts[5]("+dateparts[5]+")");
			i(TAG,"fixDate() 1043");

			if( dateparts[5].contains("EST") ){ dateparts[5] = "-0400"; }
			else if( dateparts[5].contains("CST") ){ dateparts[5] = "-0500"; }
			else if( dateparts[5].contains("MST") ){ dateparts[5] = "-0600"; }
			else if( dateparts[5].contains("PST") ){ dateparts[5] = "-0700"; }
			else if( dateparts[5].contains("GMT") || dateparts[5].contains("0000") ){
				i(TAG,"fixDate() 1031 GMT");
				// All good in the GMT world
			}
			i(TAG,"fixDate() 1053");
			if( dateparts[5].contains("+") || dateparts[5].contains("-") ){
				i(TAG,"fixDate() 1033 date modification");
				// we've got a modification here.
				String[] time = dateparts[4].split(":");
				char sign = dateparts[5].charAt(0);
				int hourMod = 0;
				int minMod = 0;
				
				//String hourS = dateparts[5].substring(1, 2);
				//String minS = dateparts[5].substring(3, 4);
				int hour = 0;
				int minute = 0;
				int second = 0;
				
				i(TAG,"fixDate() 1045 charAt1("+dateparts[5].charAt(1)+")");
				try {
					if( dateparts[5].charAt(1) == '0' ){
						hourMod = Integer.parseInt(""+dateparts[5].charAt(2));
					}else{
						hourMod = (Integer.parseInt(""+dateparts[5].charAt(1)) * 10) + Integer.parseInt(""+dateparts[5].charAt(2));
					}
					if( dateparts[5].charAt(3) == '0' ){
						minMod = 0;
					}else{
						minMod = (Integer.parseInt(""+dateparts[5].charAt(3)) * 10) + Integer.parseInt(""+dateparts[5].charAt(4));
					}
					//hourMod = Integer.parseInt(hourS);
					//minMod = Integer.parseInt(minS);
					hour = Integer.parseInt(time[0]);
					
					if( time[1].regionMatches(true, 0, "pm", 0, time[1].length() ) ){
						hour += 12;
					}
					i(TAG,"fixDate() 1064");
					time[1] = time[1].replaceAll("pm", "").replaceAll("am", "");
					minute = Integer.parseInt(time[1]);
					if( time.length == 2 ){
						second = 0;
					}else{
						second = Integer.parseInt(time[2]);
					}
					i(TAG,"fixDate() 1072");
				} catch(NumberFormatException e){
					e(TAG,"fixDate() 565 Number FormatException original("+updated+") time("+time[0]+":"+time[1]+":"+time[2]+") policy(moving on)");
				}
				
				int[] mondays = new int[] {0,31,28,31,30,31,30,31,31,30,31,30,31};
				if( sign == '-' ){
					hour += hourMod;
					minute += minMod;
					if( minute >= 60 ){
						minute -= 60;
						hour++;
					}
					if( hour >= 24 ){
						hour -= 24;
						day++;
						if( day > mondays[mon] ){
							day -= mondays[mon];
							mon++;
							if( mon > 12 ){
								year++;
								mon -= 12;
							}
						}
					}
				}else if(sign == '+'){
					hour -= hourMod;
					minute -= minMod;
					if( minute < 0 ){
						minute += 60;
						hour--;
					}
					if( hour < 0 ){
						hour += 24;
						day--;
						if( day < 1 ){
							mon--;
							if( mon < 1 ){
								mon += 12;
								year--;
							}
							day += mondays[mon];
						}
					}
				}else{
					e(TAG,"fixDate() 585 Sign(+|-) unfamiliar original("+updated+") policy(moving on)");
				}
				
				dateparts[4] = (hour > 9) ? ""+hour : "0"+hour;
				dateparts[4] += (minute > 9) ? ":"+minute : ":0"+minute;
				dateparts[4] += (second > 9) ? ":"+second : ":0"+second;
				w(TAG,"fixDate() 560 date("+updated+") time("+time[0]+":"+time[1]+":"+time[2]+") sign("+sign+") hourMod("+hourMod+") minMod("+minMod+") new("+dateparts[4]+")");
				
			}else{
				
			}
			
			if( dateparts[4] == "=" || dateparts[4].length() < 2 ){
				dateparts[4] = "00:00:00";
				//dateparts[4] = (d.getHours() > 9) ? ""+d.getHours() : "0"+d.getHours();
				//dateparts[4] += (d.getMinutes() > 9) ? ":"+d.getMinutes() : ":0"+d.getMinutes();
				//dateparts[4] += (d.getSeconds() > 9) ? ":"+d.getSeconds() : ":0"+d.getSeconds();
			}
			updated = year + "";
			updated += (mon > 9) ? "-"+mon : "-0" + mon;
			updated += (day > 9) ? "-"+day : "-0" + day;
			updated += "T" + dateparts[4];
			
			//if( mon < 10 ){
				//updated = year + "-0" + mon + "-" + day + "T" + dateparts[4];
			//}else{
				//updated = year + "-" + mon + "-" + day + "T" + dateparts[4];
			//}
			i(TAG,"fixDate() 804 Updated date("+origUpdated+") to SQLite Format("+updated+") for " + who);
		}
		
		return updated;
	}

	private long mLastVib = 0;
	public void setEntryNotification(String who, int notificationID, long rowid, int icon, String title, String details, String topscroll){
		i(TAG,"setEntryNotification() for " + who);
		
		
		String waitname = "wait_setEntryNotification_1055";
		long waitstart = waitStart(waitname, 2000);
		//waitUpdate(waitname, waitstart);
		
		Notification notif = new Notification(icon, topscroll, System.currentTimeMillis()); // This text scrolls across the top.
		Intent intentJump2 = new Intent(mContext, com.havenskys.thescoopseattle.LoadArticle.class);
		intentJump2.putExtra("id", rowid);
		intentJump2.putExtra("tab", 1);
        //PendingIntent pi2 = PendingIntent.getActivity(this, 0, intentJump2, Intent.FLAG_ACTIVITY_NEW_TASK );
        PendingIntent pi2 = PendingIntent.getActivity(mContext, 0, intentJump2, Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_MULTIPLE_TASK );
        
        //if( syncvib != 3 ){ // NOT OFF
        	//notif.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE;
        //}else{
        	//notif.defaults = Notification.DEFAULT_LIGHTS;
        //}
        
		notif.setLatestEventInfo(mContext, title, details, pi2); // This Text appears after the slide is open
		
		//Calendar c = new Calendar();
		
		
		int syncvib = mSharedPreferences.contains("syncvib") ? mSharedPreferences.getInt("syncvib",1) : 1;
		
		if( (System.currentTimeMillis() - mLastVib) > (25 * 60 * 1000) ){
			mLastVib = System.currentTimeMillis();
			Date d = new Date(); 
			i(TAG,"setEntryNotification() 432 getHours("+d.getHours()+") for " + who);
			if( d.getHours() < 20 && d.getHours() > 8 ){
				switch (syncvib) {
				case 1: // ++_+
					notif.vibrate = new long[] { 100, 200, 100, 200, 500, 200 };
					break;
				case 3: // None _
					break;
				case 2: // ++
					notif.vibrate = new long[] { 100, 200, 100, 200 };
					break;
				case 4: // +_++
					notif.vibrate = new long[] { 100, 200, 500, 200, 100, 200 };
					break;
				}
			}
		}
        mNM.notify(notificationID, notif);
        
        waitUpdate(waitname, waitstart);
        
	}
	
	public void setServiceNotification(String who, int notificationID, int icon, String title, String details, String topscroll){
		//NotificationManager notifMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification notif = new Notification(icon, topscroll, System.currentTimeMillis());
		Intent intentJump = new Intent(mContext, com.havenskys.thescoopseattle.Stop.class);
		intentJump.putExtra("stoprequest", true);
        PendingIntent pi = PendingIntent.getActivity(mContext, 0, intentJump, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NO_HISTORY);
        notif.setLatestEventInfo(mContext, title,details, pi);
        //notif.defaults = Notification.DEFAULT_LIGHTS;
        //notif.vibrate = new long[] { 100, 100, 100, 200, 100, 300 };
        mNM.notify(notificationID, notif);
	}
	
	
	// This will block until load is low or time limit exceeded
    // loadLimit(TAG + " getlatest() 107", 1 , 5 * 1000, 3 * 60 * 1000);
	public void loadLimit(String who, int loadMin, int waitms, int waittimemax) {
		i(TAG,"loadLimit() loadMin("+loadMin+") waitms("+waitms+") waittimemax("+waittimemax+") for " + who);
		
		double load = 0;
		double lastload = 0;
        int waitloopmax = waittimemax / waitms;
        int sleepcounter = 0;
        //Thread sleeperThread = new Thread(){
        	//public void run(){ SystemClock.sleep(1000); }
        //};
        
        int spin = 0;
        for(int lc = 1; lc <= waitloopmax; lc++){
        	load = getload(TAG + " loadLimit() 822 for " + who);
        	
        	if( load > (loadMin + 0.99) ){
        		mPreferencesEditor.putLong("cpublock", System.currentTimeMillis());
        		mPreferencesEditor.commit();
        		w(TAG,"loadLimit() load("+load+") > loadMin("+loadMin+".99) for " + who);
        		//if( lc == 2 ){ // second loop, notify user app has paused.
        			//setServiceNotification(TAG + " loadLimit() 340", android.R.drawable.ic_media_pause, Custom.APP + " (Press to Stop)", "Waiting for device CPU load to decrease.", Custom.APP + " synchronizing service is paused, waiting for CPU load to decrease.");
        		//}
        		for(sleepcounter = 0; sleepcounter < (waitms/1000); sleepcounter++){ SystemClock.sleep(1000); }
        	}else{
        		mPreferencesEditor.putLong("cpublock", 0);
        		mPreferencesEditor.commit();
        		i(TAG,"loadLimit() load("+load+") <= loadMin("+loadMin+".99) for " + who);
        		break;
        		//lc = waitloopmax;// setting to end so the following code is run (protects against raising load)
        		//spin++;
        		//if( spin > 5 ){
        			//e(TAG,"Spin Doctor Limit Reached moving on.");
        			//break; 
        			//}
        	}
        	if( lc == waitloopmax ){
        		if( load > lastload ){
        			w(TAG,"loadLimit() load("+load+") > lastload("+lastload+") for " + who);
        			// Load is going up, let's hold off till this isn't true.
        			// First available chance though, I'm on it.
        			lc--;
        			SystemClock.sleep( (long)(waitms/2) );
        		}else{
        			w(TAG,"Waited for maximum limit("+waittimemax+"ms), running anyway. for " + who);
        		}
        	}
        	lastload = load;
        }
        mPreferencesEditor.putLong("cpublock", 0);
        mPreferencesEditor.commit();
        i(TAG,"loadLimit() DONE");
        //setServiceNotification(TAG + " loadLimit() 350", android.R.drawable.stat_notify_sync, Custom.APP + " (Press to Stop)", "Synchronizing updates.", Custom.APP + " synchronizing updates.");
	}
	
	
	private java.lang.Process mLoadProcess;
	private InputStream mLoadStream;
	private byte[] mLoadBytes;
	private String[] mLoadParts;
	private long mLoadStart;
	private int mLoadReadSize;
	private double mLoadDouble;
	public double getload(String who){
		i(TAG,"getload() for " + who);
		
		if( mSharedPreferences == null || mPreferencesEditor == null ){
			e(TAG,"getload() started without SharedPreferences being available. Shouldn't happen.");
			return 0;
		}
		
		i(TAG,"getload() getting Editor");
		mPreferencesEditor = mSharedPreferences.edit();
		
		Thread loadT = new Thread(){
			public void run(){
				
				mPreferencesEditor = mSharedPreferences.edit();
				
				long mLoadStart = System.currentTimeMillis();
				double mLoadDouble = 1.1; // if something goes wrong, best to error on the side of shy
				
				try {
					//i(TAG,"getload() cat /proc/loadavg");
					
					java.lang.Process mLoadProcess = Runtime.getRuntime().exec("cat /proc/loadavg");
					//i(TAG,"getload() waitFor[it]");
					mLoadProcess.waitFor();
					//i(TAG,"getload() get Input Stream");
					InputStream mLoadStream = mLoadProcess.getInputStream();
					//i(TAG,"getload() create byte array[50]");
					byte[] mLoadBytes = new byte[50];
					//i(TAG,"getload() read bytes");
					int mLoadReadSize = mLoadStream.read(mLoadBytes, 0, 49);
					//i(TAG,"getload() split load parts(" + new String(mLoadBytes).replaceFirst("\n.*", "").trim() + ")");
					String[] mLoadParts = new String(mLoadBytes).trim().replaceFirst("\n.*", "").replaceAll("\\s+", " ").split(" ");
					//i(TAG,"getload() convert load to Double from string part [0](" + mLoadParts[0] + ")");
					
					if( mLoadParts[0].contains(".") && mLoadParts[0].length() > 0 ){
						//i(TAG,"getload() as we expected");
						mLoadDouble = new Double(mLoadParts[0].toString());
					}else if(mLoadParts[0].length() > 0){
						e(TAG,"getload() no decimal ");
						int newint = Integer.parseInt(mLoadParts[0]);
						mLoadDouble = (double) newint;
					}else{
						e(TAG,"getload() empty");
						mLoadDouble = 4;
					}
					//w(TAG,"Load size("+mLoadReadSize+") load("+mLoadDouble+") ms("+(System.currentTimeMillis() - mLoadStart)+") loadavg("+new String(mLoadBytes).trim()+")");
					//mPreferencesEditor.putFloat("loadFound", (float) mLoadDouble).commit();
					//i(TAG,"getload() setting preference");
					mPreferencesEditor.putInt("loadFound", (int) (mLoadDouble * 1000) );
					mPreferencesEditor.commit();
					
					/*
					w(TAG,"Getting MEMINFO");
					Process top = Runtime.getRuntime().exec("cat /proc/meminfo");
					top.waitFor();
					InputStream topstream = top.getInputStream();
					mLoadBytes = new byte[1024];
					mLoadReadSize = topstream.read(mLoadBytes, 0, 1023);
					w(TAG,"MEMINFO " + new String(mLoadBytes).trim() );
					//*/
					
					/*
					w(TAG,"Getting PROC");
					Process top = Runtime.getRuntime().exec("ls /proc");
					top.waitFor();
					InputStream topstream = top.getInputStream();
					mLoadBytes = new byte[1024];
					mLoadReadSize = topstream.read(mLoadBytes, 0, 1023);
					String[] proclist = new String(mLoadBytes).trim().split("\n");
					for(int i = 0; i < proclist.length; i++){
						w(TAG,"PROC: " +  proclist[i].trim() );
						if( proclist[i].trim().contains("self") ){
							for(i++; i < proclist.length; i++){
								Process file = Runtime.getRuntime().exec("ls /proc/"+proclist[i].trim());
								file.waitFor();
								InputStream filestream = file.getInputStream();
								mLoadBytes = new byte[1024];
								mLoadReadSize = filestream.read(mLoadBytes, 0, 1023);
								String[] filelist = new String(mLoadBytes).trim().split("\n");
								for(int c = 0; c < filelist.length; i++){
									w(TAG,"FILE /proc/"+proclist[i].trim() + ": " +  filelist[c].trim() ); 
								}
							}
							
							break;
						}
					}
					//*/
					
					mLoadBytes = null;
		
				} catch (NumberFormatException e) {
					e(TAG,"Load NumberFormatException");
					e.printStackTrace();	
				} catch (InterruptedException e) {
					e(TAG,"Load InterruptedException");
					e.printStackTrace();
				} catch (IOException e) {
					e(TAG,"Load IOException");
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
			}
		};
		
		//loadT.setDaemon(true);
		//*/
		loadT.setUncaughtExceptionHandler(new UncaughtExceptionHandler(){

			public void uncaughtException(Thread thread, Throwable ex) {
				e(TAG,"getload() 966 uncaughtException() [caught] "  + ex.getMessage());
			}
			
		});
		
		//*
		loadT.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(){

			public void uncaughtException(Thread thread, Throwable ex) {
				e(TAG,"getload() 973 defaultUncaughtException() [caught] " + ex.getMessage());				
			}
		});//*/
		
		loadT.start();
		double loadFound = 0;
		
		i(TAG,"Waiting for reply");
		
		int maxwaitsec = 5;
		long waitstart = System.currentTimeMillis();
		int looplimit = 0;
		for(;;){
			if( (System.currentTimeMillis() - waitstart) > maxwaitsec * 1000 ){
				e(TAG,"getload() 1029 reached waitlimit("+maxwaitsec+") for " + who);
				break;
			}
			if( loadT.getState().name() == "BLOCKED" ){
				e(TAG,"getload() BLOCKED");
				loadT.interrupt();
				break;
			}else{
				i(TAG,"getload() state("+loadT.getState().name()+")");
			}
			try {
				loadT.sleep(250);
			} catch (InterruptedException e) {
				e(TAG,"getload() exception " + e.getLocalizedMessage());
				e.printStackTrace();
			}
			w(TAG,"getload() 1043");
			if( loadT.isAlive() ){
				i(TAG,"getload() 1006 thread is alive");
			}else{
				i(TAG,"getload() 1008 thread is nolonger active");
				loadFound = (double) mSharedPreferences.getInt("loadFound", 1000 * 1000);
				//loadFound = mSharedPreferences.contains("loadFound") ? (double) mSharedPreferences.getInt("loadFound", 1000 * 1000) : 1000 * 1000;
				loadFound = (loadFound/1000);
				if( loadFound != 1000 ){ break; }
			}
			
			SystemClock.sleep(250);
		}
		
		
		i(TAG,"getload() 574 found("+loadFound+") DONE");
		
		return loadFound;
	}
	
	
	public long getId(String path, String where){
		Object[][] data = getAndroidData(path,"_id",where,null);
		if( data == null ){
			return 0;
		}else{
			return new Long(data[0][0].toString());
		}
	}
	
	public Object[][] getAndroidData(String path, String columns, String where, String orderby){
		Object[][] reply = null;

		//mLog.w(TAG,"getAndroidData("+path+") columns("+columns+") where("+where+")");
		
		if( orderby == null ){
			orderby = "created desc";
		}
		
        Cursor dataCursor = SqliteWrapper.query(mContext, mResolver, Uri.parse(path) 
        		,columns.split(",")
        		,where
        		,null
        		,orderby //"date desc"
        		);
        
        
        if( dataCursor != null ){
        	if( dataCursor.moveToFirst() ){
        		int len = dataCursor.getCount();
        		int clen = dataCursor.getColumnCount();
        		reply = new Object[len][clen];
        		for(int r = 0; r < len ;r++){
        			dataCursor.moveToPosition(r);
	        		for(int c = 0; c < clen ;c++){
	        			reply[r][c] = dataCursor.getString(c);
	        		}
        		}
        	}else{
        		//mLog.w(TAG,"getAndroidData empty");
        	}

            dataCursor.close();
        }else{
        	//mLog.w(TAG,"getAndroidData null");
        }
		return reply;
	}


	//private Handler mServiceHandler;
	
	public int getFeeds(String who) {
		
		//mServiceHandler = serviceHandler;
		i(TAG,"getFeeds() 561 +++++++++++++++++++++++++++++++ for " + who);
		
		//i(TAG,"getFeeds() get Acceptable Load");
		int syncLoad = mSharedPreferences.contains("syncload") ? mSharedPreferences.getInt("syncload",4) : 4;
		
		//i(TAG,"getFeeds() get Sync Interval");
		int syncInterval = mSharedPreferences.contains("sync") ? mSharedPreferences.getInt("sync",30) : 30;
		
		long lastgetfeeds = mSharedPreferences.getLong("last_getfeeds", -1);
	
		if( lastgetfeeds > System.currentTimeMillis() - 60000 ){
			AlarmManager mAlM = (AlarmManager) mContext.getSystemService(mContext.ALARM_SERVICE);
			Intent resetservice = new Intent();
	        //com.havenskys.thescoopseattle.SERVICE_RESET
			resetservice.setAction("com.havenskys.thescoopseattle.SERVICE_START1");
			//PendingIntent service3 = PendingIntent.getActivity(mContext, 0, resetservice, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_CANCEL_CURRENT);
			PendingIntent service4 = PendingIntent.getBroadcast(mContext, 81, resetservice, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_CANCEL_CURRENT);
			//mAlM.set(AlarmManager.RTC_WAKEUP,( System.currentTimeMillis() + (60 * 1000) ), service3);
			mAlM.set(AlarmManager.RTC, ( System.currentTimeMillis() + (300 * 1000) ), service4);
			e(TAG,"getFeeds() 1264 already ran "+((System.currentTimeMillis() - lastgetfeeds)/1000)+" seconds ago for " + who);
			return -1;
		}
		mPreferencesEditor.putLong("last_getfeeds", System.currentTimeMillis());
		mPreferencesEditor.commit();
		e(TAG, "getFeeds() Started.");
		
		boolean success = false;
		for( int t = 0; t < 10; t++ ){
			
			i(TAG, "getFeeds() Network Details.");
			
			ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Service.CONNECTIVITY_SERVICE);
			NetworkInfo[] ni = cm.getAllNetworkInfo();
			for( int i = 0; i < ni.length; i++){
				i(TAG, "getFeeds() type("+ni[i].getTypeName()+") available(" + ni[i].isAvailable() + ") connected("+ni[i].isConnected()+") connected("+ni[i].isConnectedOrConnecting()+")");
				if( ni[i].isConnected() ){
					success = true;
					break;
				}
				if( ni[i].isConnectedOrConnecting() ){
					// Maybe we should sleep here instead and try again.
					success = true;
					break;
				}
				if( ni[i].isAvailable() ){
					success = true;
					break;
				}
			}
			if( success ){
				mPreferencesEditor.putLong("diagnostic_activenetwork", System.currentTimeMillis());
				mPreferencesEditor.commit();
				break;
			}
			
			SystemClock.sleep(1000);
		}
		if( !success ){
			serviceState(TAG+" getFeeds()","No connectivity.");
			e(TAG, "getFeeds() no connectivity available after waiting 10 seconds and rechecking each second.");
			return -1;
		}
		
		String feedsdue = "";
		int feedsdueNum = 0;
		for( int feedid = 0; feedid < dataFeed.length; feedid++ ){
			String longname = dataFeed[feedid][0];
        	String baseurl = dataFeed[feedid][3];
        	long lastsync = mSharedPreferences.getLong("synclast_"+feedid,0);
        	long lastsyncactive = mSharedPreferences.contains("synclastactive_"+feedid) ? mSharedPreferences.getLong("synclastactive_"+feedid,0) : -1;
        	long preflastsync = lastsync > lastsyncactive ? lastsync : lastsyncactive;
    		long sinceLast = (System.currentTimeMillis() - preflastsync)/1000/60;
    		if( sinceLast > syncInterval || lastsync == 0 ){
    			feedsdue += feedid+",";
    			feedsdueNum++;
    		}
		}
		if( feedsdueNum == 0 ){
			// Started and all good
			serviceState(TAG,"No work due.");
			//e(TAG,"getFeeds() (positive error) Verified no work to do.");
		}else{
			feedsdue = feedsdue.replaceFirst(",$", "");
			serviceState(TAG,"Work due on list("+feedsdue+")");//count("+feedsdueNum+")
		}
        
		String processFailList = "";
		String downloadFailList = "";
		String successList = "";
        int successCount = 0;
        
        
        
        int lastfeedid = mSharedPreferences.contains("lastfeedid") ? mSharedPreferences.getInt("lastfeedid",-1) : -1;
        if( lastfeedid == (dataFeed.length-1) ){
        	i(TAG," getFeeds() died on last feedid("+lastfeedid+") triggers reset for " + who);
        	lastfeedid = -1;
        } else if(lastfeedid > -1 ){
        	i(TAG,"getFeeds() start after last died feedid("+lastfeedid+")");
        }
        int stopcycle = -1;
        
        int tryruncount = 0;
        
        int syncrequest = mSharedPreferences.contains("syncrequest") ? mSharedPreferences.getInt("syncrequest", -1) : -1;
        if( syncrequest > -1 ){
        	lastfeedid = -1;
        }
        
        for( int feedid = 0; feedid < dataFeed.length; feedid++ ){

        	if( dataFeed[feedid] == null ){
        		serviceState(TAG + "getFeeds()", "feedid("+feedid+") null shouldn't happen.");
        		//e(TAG,"getFeeds() dataFeed feedid("+feedid+") null, continue loop");
        		continue;
        	}
        	if( feedid <= lastfeedid && !DEVWORKING ){
        		i(TAG,"getFeeds() dataFeed feedid("+feedid+") <= lastfeedid("+lastfeedid+"), give someone else a chance, continue loop");
        		continue;
        	}
        	if( DEVWORKING && feedid != DEVFEED ){
        		w(TAG,"getFeeds() DevWorking("+DEVFEED+") != feedid("+feedid+") getting to the gold, continue loop");
        		continue;
        	}
        	if( stopcycle > -1 && stopcycle < feedid ){// <, changed to ==
        		i(TAG,"getFeeds() stopcycle("+stopcycle+") < feedid("+feedid+"), breaking loop");
        		break;
        	}
        	if( syncrequest > -1 && feedid != syncrequest ){
        		i(TAG,"getFeeds() syncrequest("+syncrequest+") not feedid("+feedid+"), moving on to find requested, continue loop");
        		continue;
        	}

        	mPreferencesEditor.putLong("lastfeedactive", System.currentTimeMillis());
			mPreferencesEditor.commit();
        	
        	
        	String longname = dataFeed[feedid][0];
        	String baseurl = dataFeed[feedid][3];
        	

    		long lastsync = mSharedPreferences.contains("synclast_"+feedid) ? mSharedPreferences.getLong("synclast_"+feedid,(System.currentTimeMillis() - (1+syncInterval) * 60 * 1000)) : (System.currentTimeMillis() - (1+syncInterval) * 60 * 1000);
    		long sinceLast = (System.currentTimeMillis() - lastsync)/1000/60;
    		
        	//i(TAG,"getFeeds() get Last Sync Time name("+longname+") url("+baseurl+") ");
        	long lastsyncactive = mSharedPreferences.contains("synclastactive_"+feedid) ? mSharedPreferences.getLong("synclastactive_"+feedid,0) : -1;
        	mPreferencesEditor.putLong("synclastactive_"+feedid, System.currentTimeMillis());
        	mPreferencesEditor.commit(); // okay to update one already taken
        	if( lastsyncactive > 0 && lastsyncactive > (System.currentTimeMillis() - 30 * 1000) ){
        		w(TAG,"getFeeds() passing on name("+longname+") url("+baseurl+") syncInterval("+syncInterval+") lastRun("+sinceLast+" Minutes Ago), appears to already be running with another process.");
        		continue;
        	}
        	
        	
    		if( !DEVWORKING && sinceLast < syncInterval ){
    			w(TAG,"getFeeds() 712 Passing on name("+longname+") url("+baseurl+") syncInterval("+syncInterval+") lastRun("+sinceLast+" Minutes Ago) for " + who);
    			//successCount++; // spoof the success, it was recent
    			continue;
    		}
    		
    		
    		i(TAG,"getFeeds() 1114 name("+longname+") url("+baseurl+") lastRun("+sinceLast+" Minutes Ago) for " + who);
    		
    		i(TAG,"getFeeds() check Load Limit");
    		loadLimit(TAG + " getFeeds() 1102 name("+longname+") url("+baseurl+") for " + who, syncLoad, 5 * 1000, 30 * 1000);
            long lastfeedactive = mSharedPreferences.getLong("lastfeedactive", 0);
            //if( lastfeedactive > 0 && lastfeedactive > (System.currentTimeMillis() - 10 * 1000) ){
            	//w(TAG,"getFeeds() lastfeed still considered active only 10 seconds ago.  Too Quick, break loop.");
            	//break;
            //}
        	mPreferencesEditor.putInt("lastfeedid", feedid);
        	mPreferencesEditor.putLong("lastfeedactive", System.currentTimeMillis());
        	mPreferencesEditor.commit();
        	
        	
        	String waitname = "wait_getFeeds_1583_" + feedid;
			long waitstart = waitStart(waitname, 1000);
			//waitUpdate(waitname, waitstart);
        	tryruncount ++;
        	//i(TAG,"getFeeds() download url("+baseurl+")");
			//String httpPage = getBasePage(baseurl, feedid, TAG + " getFeeds() 1068 for " + who);//.replaceAll("&gt;", ">").replaceAll("&lt;", "<");
			//i(TAG,"getFeeds() download url("+baseurl+") size("+httpPage.length()+") COMPLETE for " + who);
			
        	//w(TAG,"getFeeds() get ConnectivityManager");
        	//ConnectivityManager cnnm = (ConnectivityManager) mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);
        	//w(TAG,"getFeeds() get NetworkInfo");
        	//NetworkInfo ninfo = cnnm.getActiveNetworkInfo();
        	//w(TAG,"getFeeds() got NetworkInfo state("+ninfo.getState().ordinal()+") name("+ninfo.getState().name()+")");
        	//android.os.Process.getElapsedCpuTime()
        	
        	
        	
			String httpPage = "";
			String gourl = baseurl;
			Socket socket = null;
			SSLSocket sslsocket = null;
			BufferedReader br = null;
			BufferedWriter bw = null;
			int loopcnt = 0;
			
			waitUpdate(waitname, waitstart);
			
			try {
				while(gourl.length() > 0 ){
					mPreferencesEditor.putLong("lastfeedactive", System.currentTimeMillis());
					mPreferencesEditor.commit();
					loopcnt ++;
					if( loopcnt > 4 ){
						e(TAG,"getFeeds() Looped 4 times, really?! this many forwards?");
						break;
					}
					boolean secure = gourl.contains("https:") ? true : false;
					String hostname = gourl.replaceFirst(".*://", "").replaceFirst("/.*", "");
					int port = secure ? 443 : 80;
					if( hostname.contains(":") ){
						String[] p = hostname.split(":");
						hostname = p[0];
						port = Integer.parseInt(p[1]);
					}
					String docpath = gourl.replaceFirst(".*://", "").replaceFirst(".*?/", "/");
					w(TAG,"getFeeds() hostname("+hostname+") path("+docpath+") gourl("+gourl+")");
					gourl = "";
					
					if( !secure ){
						sslsocket = null;
						
						w(TAG,"getFeeds() Connecting to hostname("+hostname+") port("+port+")");
						//String
						//waitname = "wait_getFeeds_1600_" + feedid;
						//long
						//waitstart = waitStart(waitname, 500);
						//waitUpdate(waitname, waitstart);
						socket = new Socket(hostname,port);
						
						//socket = new SecureSocket();
						//SecureSocket s = null;
						
						if( socket.isConnected() ){
							i(TAG,"getFeeds() Connecting to hostname("+hostname+") CONNECTED");
						}else{
							int loopcnt2 = 0;
							while( !socket.isConnected() ){
								e(TAG,"getFeeds() Not connected to hostname("+hostname+")");
								loopcnt2++;
								if( loopcnt2 > 10 ){
									e(TAG,"getFeeds() Not connected to hostname("+hostname+") TIMEOUT REACHED");
									break;
								}
								SystemClock.sleep(300);
							}
						}
						
						w(TAG,"getFeeds() Creating Writable to hostname("+hostname+") port("+port+")");
						bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
						w(TAG,"getFeeds() Creating Readable to hostname("+hostname+") port("+port+")");
						br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						//waitUpdate(waitname, waitstart);
						
						//bw.write("GET http://" + hostname + "/" + docpath + " HTTP/1.0\r\n");
						bw.write("GET " + docpath + " HTTP/1.0\r\n");
						bw.write("Host: " + hostname + "\r\n");
						bw.write("User-Agent: Android\r\n");
					}else{
						//socket = null;
						w(TAG,"getFeeds() Connecting Securely to hostname("+hostname+") port("+port+")");
						//String
						//waitname = "wait_getFeeds_1631_" + feedid;
						//long
						//waitstart = waitStart(waitname, 500);
						//waitUpdate(waitname, waitstart);
						SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
						sslsocket = (SSLSocket) factory.createSocket(hostname,443);
						SSLSession session = sslsocket.getSession();
						X509Certificate cert;
						try { cert = (X509Certificate) session.getPeerCertificates()[0]; }
						catch(SSLPeerUnverifiedException e){
							e(TAG,"getFeeds() Connecting to hostname("+hostname+") port(443) failed CERTIFICATE UNVERIFIED");
							break;
						}
						
						if( sslsocket.isConnected() ){
							i(TAG,"getFeeds() Connecting to hostname("+hostname+") CONNECTED");
						}else{
							int loopcnt2 = 0;
							while( !sslsocket.isConnected() ){
								e(TAG,"getFeeds() Not connected to hostname("+hostname+")");
								loopcnt2++;
								if( loopcnt2 > 20 ){
									e(TAG,"getFeeds() Not connected to hostname("+hostname+") TIMEOUT REACHED");
									break;
								}
								SystemClock.sleep(300);
							}
						}
												
						w(TAG,"getFeeds() Creating Writable to hostname("+hostname+") port("+port+")");
						bw = new BufferedWriter(new OutputStreamWriter(sslsocket.getOutputStream()));
						w(TAG,"getFeeds() Creating Readable to hostname("+hostname+") port("+port+")");
						br = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));
						
						//waitUpdate(waitname, waitstart);
						
						//bw.write("GET https://" + hostname + "/" + docpath + " HTTP/1.0\r\n");
						bw.write("GET " + docpath + " HTTP/1.0\r\n");
						bw.write("Host: " + hostname + "\r\n");
						bw.write("User-Agent: Android\r\n");
					}
					
					
					mPreferencesEditor.putLong("lastfeedactive", System.currentTimeMillis());
					mPreferencesEditor.commit();
					w(TAG,"getFeeds() Requesting document hostname("+hostname+") port("+port+")");
					//bw.write("GET " + docpath + " HTTP/1.0\r\n");
					//bw.write("Host: " + hostname + "\r\n");
					//bw.write("User-Agent: Android\r\n");
					//bw.write("Range: bytes=0-"+(1024 * DOWNLOAD_LIMIT)+"\r\n");
					//bw.write("TE: deflate\r\n");
					bw.write("\r\n");
					bw.flush();
					//http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html#sec5
					String status = "";
					String line = "";
					try {
						if( !secure ){
							if( br.ready() ){
								w(TAG,"getFeeds() Ready to be read");
							}else{
								int loopcnt2 = 0;
								while( !br.ready() ){
									e(TAG,"getFeeds() NOT Ready to be read");
									loopcnt2++;
									if( loopcnt2 > 20 ){
										e(TAG,"getFeeds() NOT Ready to be read TIMEOUT REACHED WAITING");
										line = br.readLine();
										e(TAG,"getFeeds() NOT Ready to be read TIMEOUT REACHED WAITING line("+line+")");
										break;
									}
									SystemClock.sleep(300);
								}
							}
						}else{
							// br.ready() doesn't work from the sslsocket source
						}
						
						//String
						//waitname = "wait_getFeeds_1703_" + feedid;
						//long
						//waitstart = waitStart(waitname, 1000);
						//waitUpdate(waitname, waitstart);
						
						int linecnt = 0;
						for(line = br.readLine(); line != null; line = br.readLine()){
							if( line.length() == 0 ){
								w(TAG,"getFeeds() End of header Reached");
								break;
							}
							linecnt++;
							i(TAG,"getFeeds() feed("+longname+") received("+line+")");
							if( line.regionMatches(true, 0, "Location:", 0, 9) ){
								gourl = line.replaceFirst(".*?:", "").trim();
								w(TAG,"getFeeds() feed("+longname+") FOUND FORWARD URL("+gourl+") ");
							}
						}
						w(TAG,"getFeeds() 1892");
						//waitUpdate(waitname, waitstart);
						
						if( gourl.length() > 0 ){ w(TAG,"getFeeds() 1895 continue"); continue; }
						if( line == null ){
							w(TAG,"getFeeds() 1897 end of read");
							w(TAG,"getFeeds() End of read");
						}
						if( linecnt > 0 ){
							w(TAG,"getFeeds() 1901");
							mPreferencesEditor.putLong("lastfeedactive", System.currentTimeMillis());
							mPreferencesEditor.putLong("lowmemory", 0);
							mPreferencesEditor.commit();
						}
						if( line != null ){
							w(TAG,"getFeeds() 1907");
							//*/String/*/ waitname = "wait_getFeeds_1733_" + feedid;
							//*/long/*/ waitstart = waitStart(waitname, 500);x
							//waitUpdate(waitname, waitstart);
							
							int zerocnt = 0;
							int growth = 0;
							for(line = br.readLine(); line != null; line = br.readLine()){
								if( line.length() == 0 ){
									zerocnt++;
									if( zerocnt%10 == 0 ){
										w(TAG,"getFeeds() 1917");
										SystemClock.sleep(50);
									}
									if( zerocnt > 500 ){
										e(TAG,"getFeeds() host("+hostname+") 50 empty lines received, moving on.");
										break;
									}
									continue;
								}
								zerocnt = 0;
								linecnt++;
								//i(TAG,"getFeeds() host("+hostname+") line("+line+")");
								growth += line.length();
								httpPage += line;
								if( httpPage.length() > 1024 * DOWNLOAD_LIMIT ){
									w(TAG,"getFeeds() downloaded "+DOWNLOAD_LIMIT+"K from the site, moving on.");
									break;
								}
								if( linecnt%100 == 0 || growth > 1024 ){
									growth = 0;
									mPreferencesEditor.putLong("lastfeedactive", System.currentTimeMillis());
									mPreferencesEditor.commit();
									w(TAG,"getFeeds() 1935 size("+httpPage.length()+")");
									SystemClock.sleep(50);
									//waitstart += 50;
								}
							}
							w(TAG,"getFeeds() 1940");
							//int intersleep = linecnt / 10;
							//waitstart += intersleep * 50;
							//waitUpdate(waitname, waitstart);
						}
						w(TAG,"getFeeds() feed("+longname+") Downloaded("+httpPage.length()+" bytes)");
						
						
						/*/
						
						if( br.ready() ){
							
						}
						while(br.ready()){
							line = br.readLine();
							if( line == null ){
								w(TAG,"getFeeds() End of read Reached");
								break;
							} else if( line.length() == 0 ){
								w(TAG,"getFeeds() End of header Reached");
								break;
							}
							i(TAG,"getFeeds() feed("+longname+") received("+line+")");
							if( line.regionMatches(true, 0, "Location:", 0, 9) ){
								gourl = line.replaceFirst(".*?:", "").trim();
								w(TAG,"getFeeds() feed("+longname+") FOUND FORWARD URL("+gourl+") ");
							}
						}
						
						
						
						mPreferencesEditor.putLong("lastfeedactive", System.currentTimeMillis()).commit();
						while(br.ready()){
							line = br.readLine();
							if( line == null ){
								w(TAG,"getFeeds() End of read Reached");
								break;
							} else if( line.length() == 0 ){
								w(TAG,"getFeeds() End of header Reached");
								break;
							}
						}
						//*/
					}catch (IOException e1) {
						String msg = null;
						msg = e1.getLocalizedMessage() != null ? e1.getLocalizedMessage() : e1.getMessage();
						if( msg == null ){
							msg = e1.getCause().getLocalizedMessage();
							if( msg == null ){ msg = ""; }
						}
						e(TAG,"getFeeds() IOException while reading from web server " + msg);
						e1.printStackTrace();
					}
					
					if( !secure ){
						socket.close();
					}else{
						sslsocket.close();
					}
				}
			} catch (UnknownHostException e1) {
				e(TAG,"getFeeds() unknownHostException");
				e1.printStackTrace();
			} catch (IOException e1) {
				e(TAG,"getFeeds() IOException");
				e1.printStackTrace();
			}
			
			
			mPreferencesEditor.putLong("lastfeedactive", System.currentTimeMillis());
			mPreferencesEditor.commit();
			
			if( httpPage.length() > 0 ){
				
				i(TAG,"getFeeds() 1124");
				mPreferencesEditor.putLong("lastfeedactive", System.currentTimeMillis());
				mPreferencesEditor.commit();
				//while( httpPage.length() > 0 ){
					
					//int len = ( httpPage.length() > (PARSESIZE_LIMIT * 1024) ) ? (PARSESIZE_LIMIT * 1024) : httpPage.length();
					//i(TAG,"getFeeds() getting " + len + " characters");
					//String subhttpPage = httpPage.substring(0, len - 1 );
					
					//i(TAG,"getFeeds() updating httpPage");
					//if( len < httpPage.length() ){
						//httpPage = httpPage.substring( (int)(len/2) , httpPage.length()-1);
					//}else{
						//httpPage = "";
					//}
					//i(TAG,"getFeeds() parseEntries subhttpPage size("+subhttpPage.length()+")");
				
					//i(TAG,"getFeeds() parseEntries httpPage size("+httpPage.length()+"), need to break this up into smaller chunks code exists here for that.");
					if( parseEntries(TAG + " getFeeds() feed("+longname+") for " + who, feedid, httpPage) ){
						i(TAG,"getFeeds() 1126 success in parseEntries()");
						successList += feedid+",";
						e(TAG, "getFeeds() (positive error) Successful processing of feed("+longname+")");
						successCount++;
						mPreferencesEditor.putLong("syncsuccess", System.currentTimeMillis());
						mPreferencesEditor.putLong("synclast_"+feedid, System.currentTimeMillis());
						mPreferencesEditor.putLong("synclastactive_"+feedid, 0);
						mPreferencesEditor.commit();
						
						//String
						waitname = "wait_getFeeds_1857_" + feedid;
						//long
						waitstart = waitStart(waitname, 500);
						//waitUpdate(waitname, waitstart);
						
						Cursor c = null;
						int totalCount = 0;
						c = SqliteWrapper.query(mContext, mResolver, DataProvider.CONTENT_URI, new String[] {"count(*)"} , "status > 0", null, null);
						// and (read == 0 or read > "+(System.currentTimeMillis() - (5 * 60 * 1000) )+") and ( seen == 0 or seen > "+(System.currentTimeMillis() - (5 * 60 * 1000) )+" )"
						if( c != null ){ if( c.moveToFirst() ){ totalCount = c.getInt(0); } c.close(); }
										
						//i(TAG, "getFeeds() 995 name("+longname+") RecordCount("+totalCount+")");
						mPreferencesEditor.putInt("total", totalCount);
						mPreferencesEditor.commit();
						c = null;
						int myCount = 0;
						c = SqliteWrapper.query(mContext, mResolver, DataProvider.CONTENT_URI, new String[] {"count(*)"} , "status > 0 and " + Custom.TYPE + " = " + feedid, null, null);
						// and (read == 0 or read > "+(System.currentTimeMillis() - (5 * 60 * 1000) )+") and ( seen == 0 or seen > "+(System.currentTimeMillis() - (5 * 60 * 1000) )+" )"
						if( c != null ){ if( c.moveToFirst() ){ myCount = c.getInt(0); } c.close(); }
						
						i(TAG, "getFeeds() 1004 name("+longname+") FeedRecordCount("+myCount+") TotalRecordCount("+totalCount+")");
						mPreferencesEditor.putLong("total_"+feedid, myCount);
						mPreferencesEditor.putInt("lastfeedid", -1);
				        mPreferencesEditor.putLong("lastfeedactive", 0);
				        mPreferencesEditor.commit();
						//SystemClock.sleep(1880);
				        
				        waitUpdate(waitname, waitstart);
						
					}else{
						mPreferencesEditor.putLong("synclastactive_"+feedid, 0);
						mPreferencesEditor.commit();
						processFailList += feedid + ",";
						//serviceState(TAG + " getFeeds()", "Failed to parse entries from feed("+longname+") after downloading("+httpPage.length()+" bytes)");
						e(TAG,"getFeeds() 740 failed parseEntries() which is horrible and happens on Memory issues, exiting");
						SystemClock.sleep(1880);
						return 2;
					}
				//}
					
			}else{
				mPreferencesEditor.putLong("synclastactive_"+feedid, 0);
				mPreferencesEditor.commit();
				downloadFailList += feedid + ",";
				//serviceState(TAG + " getFeeds()", "Fail download of feed("+longname+")");
				e(TAG,"getFeeds() Download "+longname+" feed("+baseurl+") failed for " + who);
				SystemClock.sleep(1880);
			}
			
			if( feedid == (dataFeed.length-1) ){ // Last Feed Finished
				if( lastfeedid > -1 ){ // If this feed jobs started from a middle zone start over
					stopcycle = lastfeedid;
					lastfeedid = -1;
					feedid = -1; // Will become 0 from the for loop
				}
			}
			
        }
        
        mPreferencesEditor.putInt("lastfeedid", -1);
        mPreferencesEditor.putLong("lastfeedactive", 0);
        mPreferencesEditor.commit();
        
        downloadFailList = downloadFailList.replaceFirst(",$", "");
        processFailList = processFailList.replaceFirst(",$", "");
        
        
        //if( feedsdueNum == 0 && successCount == 0 ){
			// Started and all good
			//serviceState(TAG,"Verified no work to do.");
			//e(TAG,"getFeeds() (positive error) Verified no work to do.");
		//}
		if( successCount > 0 && tryruncount > 0 ){
			
			feedsdue = feedsdue.replaceFirst(",$", "");
			successList = successList.replaceFirst(",$", "");
			
			String msg = "";
			if( downloadFailList.length() > 0 ){ msg += " downloadFailure("+downloadFailList+")"; }
			if( processFailList.length() > 0 ){ msg += " processFailure("+processFailList+")"; }
			serviceState(TAG + " getFeeds()","Finished with success("+successCount+"/"+tryruncount+"/"+feedsdueNum+") list("+successList+")"+msg);
			
			int days = 1;
			String upgrades = mSharedPreferences.getString("upgrades", "");
			/*/
			if( upgrades.contains("Customize to Full Version.") ){
				days = 7;
			}//*/
			//if( !FREEVERSION ){ days = 7; }
			
			ContentValues cv = new ContentValues();
			cv.put(SUMMARY, "");
			cv.put(CONTENT, "");
			cv.put(CONTENTURL, "");
			cv.put(AUTHOR, "");
			cv.put(STATUS, -1);
			cv.put(READ, 0);
			cv.put(SEEN, 0);
			cv.put(LAST_UPDATED, System.currentTimeMillis());
			
			int deletecount = SqliteWrapper.update(mContext, mResolver, DataProvider.CONTENT_URI, cv, "read == 0 AND lastupdated < " + (System.currentTimeMillis() - (1000 * 60 * 60 * 24 * days)), null);
			//int deletecount = SqliteWrapper.delete(mContext, mResolver, DataProvider.CONTENT_URI, "read == 0 AND lastupdated < " + (System.currentTimeMillis() - (1000 * 60 * 60 * 24 * days)), null);
			if( deletecount > 0 ){
				e(TAG,"Data retention policy deleted("+deletecount+") records older than days("+days+")");
			}
			//serviceState(TAG + " getFeeds()", "Finished processing feeds with success "+successCount+"/"+tryruncount+"");
			return 1;
		}else if( tryruncount == 0 ){
			serviceState(TAG + " getFeeds()", "Finished and verified no work.");
			return 1;
		}else if( successCount == 0 && tryruncount > 0){
			String msg = "";
			if( downloadFailList.length() > 0 ){ msg += " downloadFailure("+downloadFailList+")"; }
			if( processFailList.length() > 0 ){ msg += " processFailure("+processFailList+")"; }
			//if( successList.length() > 0 ){ msg += " successList("+successList+")"; }
			//serviceState(TAG,"Work complete with success("+successCount+"/"+tryruncount+"/"+feedsdueNum+")"+msg);
			serviceState(TAG + " getFeeds()", "Finished with failure("+successCount+"/"+tryruncount+"/"+feedsdueNum+")"+msg);
			//SystemClock.sleep(1880);
			return -1;
		}else{
			serviceState(TAG + " getFeeds()", "Unhandled failure processing "+successCount+"/"+tryruncount+"");
			return -1;
		}
	}
	
	
	private String getBasePage(final String baseurl, final int feedid, final String who) {
				
		i(TAG,"getBasePage() 590 url("+baseurl+")");
		
	
		String httpPage = "";
		
		mPreferencesEditor.remove("httppage");
		//mPreferencesEditor.putString("httppage", "");
		mPreferencesEditor.putLong("httppagetime", 0);
		mPreferencesEditor.commit();
		
		//Thread tr = new Thread(){
			//public void run(){
		
				
				int maxtrycount = 4;
				for(int trycount = 0; trycount < maxtrycount; trycount++){
					
					//mPreferencesEditor = mSharedPreferences.edit();
					mPreferencesEditor.putLong("httppagetime", System.currentTimeMillis());
					mPreferencesEditor.commit();
					
					HTTPClient sp = new HTTPClient(mContext, TAG + " getBasePage() 1222 Thread for " + who);
					sp.setSharedPreferences(mSharedPreferences);
					String httpStatus = sp.safeHttpGet(TAG + " getBasePage() 1224 Thread", new HttpGet(baseurl) );
					httpPage = sp.getHttpPage();
					
					//mPreferencesEditor = mSharedPreferences.edit();
					
					if( httpStatus.contains("200") && httpPage.length() > 0 ){
						i(TAG,"getBasePage() 1215 Download Successful url("+baseurl+") size("+httpPage.length()+") for " + who);
						mPreferencesEditor.putLong("httppagetime", 0);
						mPreferencesEditor.commit();
						
						i(TAG,"getBasePage() 1230 verifying freememory for " + who);
						
						long freememory = Runtime.getRuntime().freeMemory();
						if(httpPage.length() > freememory * .40){
							w(TAG,"page size is larger than 40% of freememory("+freememory+") cutting the page in half.");
							httpPage = httpPage.substring(0, (httpPage.length()/2) );
						}
						
						i(TAG,"getBasePage() 1230 putting the page in shared preferences for " + who);
						//mPreferencesEditor.putString("httppage", httpPage).commit();
						break;
						
					}else{
						//w(TAG,"getBasePage() 1219 Thread Download Failed.");
						if( trycount < maxtrycount ){
							w(TAG,"getBasePage() 1318 Thread Download Failed url("+baseurl+"), retrying("+(trycount+1)+"/"+maxtrycount+").");
						}else{
							e(TAG,"getBasePage() 1320 Thread Download Failed url("+baseurl+"), failed after "+maxtrycount+" attempts.");
							break;
						}
						SystemClock.sleep(1880);
						/*
						httpStatus = sp.safeHttpGet(TAG + " getBasePage() 1235 Thread", new HttpGet(baseurl) );
						httpPage = sp.getHttpPage();
						if( httpStatus.contains("200") && httpPage.length() > 0 ){
							i(TAG,"getBasePage() 1224 Thread Download Successful on retry page size("+httpPage.length()+")");
							mPreferencesEditor.putLong("httppagetime", 0);
							mPreferencesEditor.putString("httppage", httpPage).commit();
						}else{
							if( httpStatus == null ){ httpStatus = "NULL"; }
							e(TAG,"getBasePage() 1229 Thread Download Failed on retry. httpStatus("+httpStatus+") for " + who );
							mPreferencesEditor.putLong("httppagetime", -1);
							mPreferencesEditor.putString("httppage", "");
							mPreferencesEditor.commit();
						}//*/
					}
				}
				
				i(TAG,"getBasePage() 1250 Thread end for " + who);
			//}
		//};

		
		/*/
		tr.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			public void uncaughtException(Thread thread, Throwable arg1) {
				e(TAG,"getBasePage() 1229 defaultUncaughtException() [caught] "  + arg1.getMessage());
			}
			
		});/*
		tr.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			public void uncaughtException(Thread arg0, Throwable arg1) {
				e(TAG,"getBasePage() 1236 uncaughtException() [caught] "  + arg1.getMessage());
			}
			
		});
		//tr.setDaemon(true);
		tr.start();
		//*
				
		i(TAG,"getBasePage() 1228 Waiting for reply");
		//String httpPage = mSharedPreferences.getString("httppage", "");
		
		int maxwaitsec = 5;
		long waitstart = System.currentTimeMillis();
		int looplimit = 0;
		for(;;){

			if( tr.getState().name() == "BLOCKED" ){
				e(TAG,"getBasePage() BLOCKED");
				tr.interrupt();
				break;
			}else{
				i(TAG,"getBasePage() state("+tr.getState().name()+")");
			}
			try {
				tr.sleep(250);
			} catch (InterruptedException e) {
				e(TAG,"getBasePage() exception " + e.getLocalizedMessage());
				e.printStackTrace();
			}
			
			if( (System.currentTimeMillis() - waitstart) > maxwaitsec * 1000 ){
				
				
				
				if( tr.isAlive() ){
					w(TAG,"getBasePage() 1266 thread is alive but taking a long time for " + who);
				}else{
					w(TAG,"getBasePage() 1266 thread was dead for " + who);
					tr.start();
				}
				mPreferencesEditor.putLong("lastfeedactive", System.currentTimeMillis()).commit();
	        	//mPreferencesEditor.commit();
				w(TAG,"getBasePage() 1289");
				waitstart = System.currentTimeMillis();
				looplimit++;
				if( looplimit > 5 ){
					e(TAG,"getBasePage() 1274 looplimit("+looplimit+") for " + who);
					break;
				}
				w(TAG,"getBasePage() 1312 sleep 2 seconds");
				SystemClock.sleep(2000);
			}
			if( tr.isAlive() ){
				//i(TAG,"getBasePage() 1250 thread is alive for " + who);
			}else{
				i(TAG,"getBasePage() 1250 thread is nolonger active for " + who);
				httpPage = mSharedPreferences.getString("httppage", "");
				if( httpPage.length() > 0 ){ break; }else{
					tr.start();
					waitstart = System.currentTimeMillis();
					looplimit++;
					if( looplimit > 5 ){
						w(TAG,"getBasePage() 1288 looplimit("+looplimit+") for " + who);
						break;
					}
				}
			}
			SystemClock.sleep(250);
		}//*/
		i(TAG,"getBasePage() 1236 page size("+httpPage.length()+")");
		
		return httpPage;		
	}

	
	public void serviceState(String who, String what){
		
		
		if( mSharedPreferences == null ){
			e(TAG,"serviceState() started without shared preferences for " + who);
			return;
		}

		//android.os.Process.
		Runtime.getRuntime().gc();
		String Oservicehistory = mSharedPreferences.getString("servicehistory", "");//DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | 
		String servicehistory = Process.myPid() + ": " + DateUtils.formatDateTime(mContext, System.currentTimeMillis(),  DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME );
		servicehistory += " " + what;
		if( Oservicehistory.length() > 1024 * 5 ){
			//Oservicehistory.regionMatches(false, 0, "\n", 1023, (Oservicehistory.length()-1023-1));
			//int next = Oservicehistory.indexOf("\n", (1024 * 5) - 1);
			//if( next > 1 ){ Oservicehistory = Oservicehistory.substring(0,next-1); }
			int start = Oservicehistory.length() - (1024 * 5);
			if(start < 0 ){start = 0;}
			int next = Oservicehistory.indexOf("\n", start);
			if( next > 1 ){
				Oservicehistory = Oservicehistory.substring(next,Oservicehistory.length()-1);
			}
		}
		e(TAG,"(positive) ServiceState: " + servicehistory);
		//servicehistory += "\n" + Oservicehistory;
		mPreferencesEditor.putString("servicehistory", Oservicehistory + "\n" + servicehistory );
		mPreferencesEditor.commit();
		
	}

	private long mLastRestart = 0;
	private boolean mConsoleUnknownReset = false;
	private boolean mConsoleServiceReset = false;
	private boolean mConsoleTouchReset = true;
	private long mConsoleTouchLast = 0;
	public void refreshConsoleTouch(ImageView consoleTouch, Handler handler) {
		
		int syncInterval = mSharedPreferences.getInt("sync", 29);
		long syncstart = mSharedPreferences.getLong("syncstart", -1);
		long syncend = mSharedPreferences.getLong("syncend", -1);
		long lastactive = mSharedPreferences.getLong("lastfeedactive", -1);
		long lowmemory = mSharedPreferences.getLong("lowmemory", -1);
		long cpublock = mSharedPreferences.getLong("cpublock", -1);
		long unexpected = mSharedPreferences.getLong("unexpected", -1);
		long lastrun = mSharedPreferences.getLong("lastrun", -1);
		//mPreferencesEditor.putLong("lowmemory", System.currentTimeMillis()).commit();
		
		//
		
		
		e(TAG, "refreshConsoleTouch() pid("+Process.myPid()+") canthread("+Process.supportsProcesses()+") tid("+Process.myTid()+") mConsoleUnknownReset("+mConsoleUnknownReset+") mConsoleTouchReset("+mConsoleTouchReset+") mConsoleServiceReset("+mConsoleServiceReset+") syncInterval("+syncInterval+") syncstart("+syncstart+") syncend("+syncend+") lastactive("+lastactive+") lowmemory("+lowmemory+") cpublock("+cpublock+") +++++++++++++++++++++++++");
		
		
		boolean startservice = false;
		
		if( mConsoleServiceReset ){
			consoleTouch.setImageDrawable(mContext.getResources().getDrawable(R.drawable.listfooter_active));
			handler.postDelayed((Runnable) mContext, 1880);
			mConsoleServiceReset = false;
			mConsoleTouchReset = false;
			return;
		}
		
		if( mConsoleUnknownReset ){
			consoleTouch.setImageDrawable(mContext.getResources().getDrawable(R.drawable.listfooter_cpublock));
			handler.postDelayed((Runnable) mContext, 1880);
			mConsoleUnknownReset = false;
			mConsoleTouchReset = false;
			return;
		}
		
		if( mConsoleTouchReset ){
			mConsoleTouchReset = false;
			consoleTouch.setImageDrawable(mContext.getResources().getDrawable(R.drawable.listfooter));
			handler.postDelayed((Runnable) mContext, 1880);
			return;
		}
		mConsoleTouchReset = true;
		if( mConsoleTouchLast > System.currentTimeMillis() - 10 * 1000){
			e(TAG,"refreshConsoleTouch() last ran within 10 seconds, sounds like a duplicate.  leaving.");
			consoleTouch.setImageDrawable(mContext.getResources().getDrawable(R.drawable.listfooter_unhealthy));
			return;
		}
		mConsoleTouchLast = System.currentTimeMillis();
		//if( Process.getElapsedCpuTime() > 600 * 1000 ){
			//consoleTouch.setImageDrawable(mContext.getResources().getDrawable(R.drawable.listfooter_active));
			//return;
		//}
		
		//mConsoleTouch.post(new Runnable(){
			//public void run(){
		
			//}
		//});
		//mConsoleTouch.getHandler().sendEmptyMessage(1);
			
		//SystemClock.sleep(1000);

		
		/*
		if( syncstart > 0 && syncstart < (System.currentTimeMillis() - 60 * 1000) && lowmemory > 0 && lowmemory < (System.currentTimeMillis() - 2 * 60 * 1000 ) && cpublock > 0 && cpublock < (System.currentTimeMillis() - 2 * 60 * 1000 ) ){
			startservice = true;
			consoleTouch.setImageDrawable(mContext.getResources().getDrawable(R.drawable.listfooter_unhealthy));
			
		} else//*/ 
		
		if( cpublock > 0 ){
			//cpublock > (System.currentTimeMillis() - 2 * 60 * 1000);
			int since = (int) ((System.currentTimeMillis() - cpublock)/1000);
			consoleTouch.setImageDrawable(mContext.getResources().getDrawable(R.drawable.listfooter_cpublock));
			
			if( since >= 120 ){
				startservice = true;
				e(TAG, "refreshConsoleTouch() cpublock since("+since+" seconds) requesting restart");
			}else{
				e(TAG, "refreshConsoleTouch() cpublock since("+since+" seconds) recent");
			}
			
		} else if( lowmemory > 0 ){
			int since = (int) ((System.currentTimeMillis() - lowmemory)/1000);
			
			consoleTouch.setImageDrawable(mContext.getResources().getDrawable(R.drawable.listfooter_memoryblock));
			if( since >= 120 ){
				startservice = true;
				e(TAG, "refreshConsoleTouch() lowmemory since("+since+" seconds) requesting restart");
			}else{
				e(TAG, "refreshConsoleTouch() lowmemory since("+since+" seconds) recent");
			}
		} else if( syncstart > (System.currentTimeMillis() - 120 * 1000) || lastactive > (System.currentTimeMillis() - 120 * 1000) ){
			consoleTouch.setImageDrawable(mContext.getResources().getDrawable(R.drawable.listfooter_active));
			
		} else if( syncend > (System.currentTimeMillis() - syncInterval * 60 * 1000) ){
			boolean allverified = false;
			for( int feedid = 0; feedid < dataFeed.length; feedid++ ){
				String longname = dataFeed[feedid][0];
	        	String baseurl = dataFeed[feedid][3];
	        	long lastsync = mSharedPreferences.getLong("synclast_"+feedid,0);
	        	long lastsyncactive = mSharedPreferences.contains("synclastactive_"+feedid) ? mSharedPreferences.getLong("synclastactive_"+feedid,0) : -1;
	        	long preflastsync = lastsync > lastsyncactive ? lastsync : lastsyncactive;
	    		long sinceLast = (System.currentTimeMillis() - preflastsync)/1000/60;
	    		if( sinceLast > (syncInterval+2) || lastsync == 0 ){
	    			if( lastsync == 0 ){
	    				i(TAG,"refreshConsoleTouch() healthy service although feed("+longname+") has been requested to be checked and has not yet started. starting service");
	    			}else{
	    				if( sinceLast < syncInterval * 10 ){
		    				i(TAG,"refreshConsoleTouch() healthy service except feed("+longname+") hasn't been successfully checked in ("+sinceLast+" minutes) starting service");
	    				}else{
	    					i(TAG,"refreshConsoleTouch() healthy service except feed("+longname+") hasn't been successfully checked in ("+sinceLast+" minutes) over 10 times the syncinterval. moving on");
	    					continue;
	    				}
	    			}
	    			allverified = false;
    				startservice = true;
    				break;
	    		}else{
	    			allverified = true;
	    		}
			}
			if( allverified ){
				consoleTouch.setImageDrawable(mContext.getResources().getDrawable(R.drawable.listfooter_super));
			}else{
				startservice = true;
				consoleTouch.setImageDrawable(mContext.getResources().getDrawable(R.drawable.listfooter_healthy));
			}
			
		} else if( syncstart == 0 ){
			consoleTouch.setImageDrawable(mContext.getResources().getDrawable(R.drawable.listfooter_active));
			startservice = true;
			
		} else if( syncend > 0 && syncend < (System.currentTimeMillis() - (syncInterval+2) * 60 * 1000) ){ // wait a couple minutes before determining it's unhealthy
			//mConsoleUnknownReset = true;
			//mConsoleServiceReset = true;
			consoleTouch.setImageDrawable(mContext.getResources().getDrawable(R.drawable.listfooter_unhealthy));
			startservice = true;
			
		/*/
		} else {
			mConsoleUnknownReset = true;
			w(TAG, "Uncaught condition setting to active syncstart("+syncstart+") syncInterval("+syncInterval+") syncend("+syncend+") lowmemory("+lowmemory+") cpublock("+cpublock+")");
			consoleTouch.setImageDrawable(mContext.getResources().getDrawable(R.drawable.listfooter_active));
			startservice = true;
			//*/
		}else{
			e(TAG, "Uncaught condition setting to active syncstart("+syncstart+") syncInterval("+syncInterval+") syncend("+syncend+") lowmemory("+lowmemory+") cpublock("+cpublock+")");
			if( syncstart > 0 && syncstart > (System.currentTimeMillis() - syncInterval * 60 * 1000) ){
				consoleTouch.setImageDrawable(mContext.getResources().getDrawable(R.drawable.listfooter_active));
			}else{
				consoleTouch.setImageDrawable(mContext.getResources().getDrawable(R.drawable.listfooter_unhealthy));
			}
		}
		if( startservice ){
			w(TAG, "refreshConsoleTouch() Restarting service");
			if( mLastRestart < (System.currentTimeMillis() - 60 * 1000) ){
				mConsoleServiceReset = true;
				//i(TAG,"run() get AlarmManager");
				AlarmManager alm = (AlarmManager) mContext.getSystemService(mContext.ALARM_SERVICE);
				Intent resetservice = new Intent();
		        //com.havenskys.thescoopseattle.IntentReceiver.SERVICE_RESET
				resetservice.setAction("com.havenskys.thescoopseattle.SERVICE_RESET");
				PendingIntent service3 = PendingIntent.getBroadcast(mContext, 0, resetservice, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_CANCEL_CURRENT);
				//alm.set(AlarmManager.RTC_WAKEUP,( System.currentTimeMillis() + (2 * 1000) ), service3);
				//alm.setRepeating(AlarmManager.RTC_WAKEUP, (System.currentTimeMillis() + 3 * 1000), AlarmManager.INTERVAL_FIFTEEN_MINUTES, service3);
				alm.set(AlarmManager.RTC_WAKEUP, (System.currentTimeMillis() + 3 * 1000), service3);
				
				//Intent service = new Intent();
				//service.setClass(mContext, SyncService.class);
			    //mContext.stopService(service);
			    //service.putExtra("com.havenskys.thescoopseattle.who", TAG + " run()");
			    //mContext.startService(service);
				
				//Intent service = new Intent();
				//service.setClass(mContext, com.havenskys.thescoopseattle.SyncService.class);
				//service.putExtra("com.havenskys.thescoopseattle.who", TAG + " refreshConsoleTouch() Repeating Service Alarm minutes("+syncInterval+") at " + DateUtils.formatDateTime(mContext, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME ));
				//PendingIntent serviceP = PendingIntent.getService(mContext, 1000, service, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_CANCEL_CURRENT);
				//alm.setRepeating(AlarmManager.RTC_WAKEUP, (System.currentTimeMillis() + 1000), AlarmManager.INTERVAL_FIFTEEN_MINUTES, serviceP);
			    
			    mLastRestart = System.currentTimeMillis();
			    mPreferencesEditor.putLong("servicerestart", System.currentTimeMillis());
			    mPreferencesEditor.commit();
			//} else if( startservice ){
				//mHandler.postDelayed(this, 1880);
				//return;
			}else{
				w(TAG,"refreshConsoleTouch() Already Restarted "+( (System.currentTimeMillis() - mLastRestart)/1000 )+" seconds ago.");
			}
		}
		
		//mHandler.postDelayed(this, 1000 * 10);
		//mHandler.sendEmptyMessageDelayed(3, 1000 * 5);
		handler.postDelayed((Runnable) mContext, 1000 * 10);
	}

	
	private boolean mClearScreen = false;
	private String mLastStatement, mSay;
	private boolean mConsoleStartup = false;
	private long mHighload = 0;
	private long mNoRecords = 0;
	private boolean mConsoleReset = true;
	private ImageView mConsoleAction;
	//private boolean mConsolePause = false;
	private long mConsoleInPause = 0;
	private long mConsoleInPlay = 0;
	private long mLastConsoleTouch = 0;
	private long mLastConsoleRefresh = 0;
	public void refreshConsole(ImageView action, TextView text, ImageView console, ImageView title2,
			ImageView cover, Handler handler, Thread thread) {
		
		if( System.currentTimeMillis() - mLastConsoleRefresh < 990 ){
			e(TAG,"refreshConsole() quicker than a second, should be imposible, must be a duplicate process.");
			e(TAG,"refreshConsole() 2304 postDelay (quicker than permitted) 1880");
			handler.postDelayed( (Runnable) mContext, 1880);
			return;
		}
		mLastConsoleRefresh = System.currentTimeMillis();
		
		e(TAG,"refreshConsole() reset("+mConsoleReset+")");
		cover.setKeepScreenOn(true);
		console.requestFocusFromTouch();
		
		if( action.getVisibility() == ImageView.GONE ){
			action.setVisibility(ImageView.VISIBLE);
			text.setVisibility(TextView.VISIBLE);
			title2.setVisibility(ImageView.VISIBLE);
			console.setVisibility(ImageView.VISIBLE);
			cover.setVisibility(ImageView.VISIBLE);
			mLastStatement = "";
			mClearScreen = false;
			mConsoleReset = true;
			text.setText("Tri-la-Beep\n[computer ready]");
			e(TAG,"refreshConsole() 2324 postDelay Making all pieces visible 1880");
			handler.postDelayed( (Runnable) mContext, 1880);
			return;
		}
		
		if( mConsoleReset ){
			mConsoleReset = false;
			if( mConsoleInPause > 0 ){
				text.setText("[computer paused]");
			}else{
				//text.setText(""+Process.getElapsedCpuTime() );
				text.setText("");
			}
			e(TAG,"refreshConsole() 2337 postDelay clearing the console, between thoughts. 1000");
			handler.postDelayed( (Runnable) mContext, 1000);
			return;
		}
		
		if( mLastStatement == null ){
			
			mLastStatement = "";
			
			//if( mConsoleAction == null ){
				
				
				
				
				/*/
				console.setOnTouchListener(new OnTouchListener(){

					public boolean onTouch(View arg0, MotionEvent arg1) {
						if( arg1.getAction() == MotionEvent.ACTION_DOWN ){
							if( (System.currentTimeMillis() - mLastConsoleTouch) < 300 ){ return true; }
							mLastConsoleTouch = System.currentTimeMillis();
							if( mConsoleInPlay > 0 ){
								mConsoleInPlay = 0;
								mConsoleAction.setImageResource(android.R.drawable.ic_media_pause);
							}
							if( mConsoleInPause == 0 ){
								mConsoleInPause = System.currentTimeMillis();
								mConsoleAction.setImageResource(android.R.drawable.ic_media_play);
							}else{
								mConsoleInPlay = System.currentTimeMillis();
							}
						}else
						if( arg1.getAction() == MotionEvent.ACTION_UP ){
							mLastConsoleTouch = 0;	
						}
						return false;
					}
					
				});//*/
			//}
			//mConsoleStartup = true;
			//mSay = "Ready";
		}
		
		int speakspeed = mSharedPreferences.getInt("speakspeed", 1880 * 3);
		mConsoleAction = action;
		
		//if( mConsoleInPause == 0){ mConsoleInPause = System.currentTimeMillis(); }
		if( mConsoleInPause > (System.currentTimeMillis() - 60000) ){
			long diff = (System.currentTimeMillis() - mConsoleInPause);
			if( diff > 1000 && diff < 10000){ 
				speakspeed += 100;
				Toast.makeText(mContext, "Decreasing speed by 100 ms", Toast.LENGTH_SHORT).show();
			}else if(diff < 1000){
				speakspeed -= 100;
				if( speakspeed >= 1680 ){
					Toast.makeText(mContext, "Increasing speed by 100 ms", Toast.LENGTH_SHORT).show();
				}else{
					speakspeed = 1680; // FASTEST SPEED
				}
			}
			mPreferencesEditor.putInt("speakspeed", speakspeed);
			//mSay = mLastStatement;
			
			//*/
			console.setOnTouchListener(new OnTouchListener(){

				public boolean onTouch(View arg0, MotionEvent arg1) {
					if( arg1.getAction() == MotionEvent.ACTION_DOWN ){
						//if( (System.currentTimeMillis() - mLastConsoleTouch) < 300 ){ return true; }
						//mLastConsoleTouch = System.currentTimeMillis();
						mConsoleInPause = 0;
						mConsoleAction.setImageResource(android.R.drawable.ic_media_pause);
					//}else
				//		if( arg1.getAction() == MotionEvent.ACTION_UP ){
						//	mLastConsoleTouch = 0;	
						}
					return true;
				}
			});//*/
			
			mConsoleReset = true;
			if( mLastStatement.length() == 0 ){
				mLastStatement = text.getText().toString();
				if( mLastStatement.length() == 0 ){
					mLastStatement = "Pause";//word of the day?
				}
			}
			
			text.setText(mLastStatement);
			e(TAG,"refreshConsole() 2427 postDelay In Pause " + speakspeed);
			handler.postDelayed( (Runnable) mContext, speakspeed);
			return;
		}else if(mConsoleInPause == 0){
			
			action.setImageResource(android.R.drawable.ic_media_pause);
			
			console.setOnTouchListener(new OnTouchListener(){

				public boolean onTouch(View arg0, MotionEvent arg1) {
					if( arg1.getAction() == MotionEvent.ACTION_DOWN ){
						//if( (System.currentTimeMillis() - mLastConsoleTouch) < 300 ){ return true; }
						//mLastConsoleTouch = System.currentTimeMillis();
						mConsoleInPause = System.currentTimeMillis();
						mConsoleAction.setImageResource(android.R.drawable.ic_media_play);
					//}else
						//if( arg1.getAction() == MotionEvent.ACTION_UP ){
							//mLastConsoleTouch = 0;	
						}
					return true;
				}
			});//*/
		}
		
		
		
		Date d = new Date();
		int hour = d.getHours();
		
		
		mConsoleReset = true;
		
		
		int total = mSharedPreferences.getInt("total", 0);
		int syncInterval = mSharedPreferences.getInt("sync", 29);
		long syncstart = mSharedPreferences.getLong("syncstart", -1);
		long syncend = mSharedPreferences.getLong("syncend", -1);
		long lastactive = mSharedPreferences.getLong("lastfeedactive", -1);
		long lowmemory = mSharedPreferences.getLong("lowmemory", -1);
		long cpublock = mSharedPreferences.getLong("cpublock", -1);
		long unexpected = mSharedPreferences.getLong("unexpected", -1);
		int runcount = mSharedPreferences.getInt("runcount", 0);
		long httppagetime = mSharedPreferences.getLong("httppagetime", -1);
		
		
		int sisStart, sisEnd, sisLast, sisMemory, sisCPU, sisUNX;
		if( syncstart > 0 ){ sisStart = (int) ((System.currentTimeMillis() - syncstart)/1000); }else{sisStart = 0;}
		if( syncend > 0 ){ sisEnd = (int) ((System.currentTimeMillis() - syncend)/1000); }else{sisEnd = 0;}
		if( lastactive > 0 ){ sisLast = (int) ((System.currentTimeMillis() - syncstart)/1000); }else{sisLast = 0;}
		if( lowmemory > 0 ){ sisMemory = (int) ((System.currentTimeMillis() - syncstart)/1000); }else{sisMemory = 0;}
		if( cpublock > 0 ){ sisCPU = (int) ((System.currentTimeMillis() - syncstart)/1000); }else{sisCPU = 0;}
		if( unexpected > 0 ){ sisUNX = (int) ((System.currentTimeMillis() - unexpected)/1000); }else{sisUNX = 0;}

		String LOADING_RECORDS = "Loading records\nfor the first time.";
		String THANKYOU = "Thank you\nfor checking us out.";
		String UPGRADE0 = "If you like different, order a personalized upgrade...";
		String UPGRADE1 = "invent your own easter egg\nwithout writing code\nit's only 10 bucks...";
		String UPGRADE2 = "and available in every future publication.";
		//String UPGRADE2 = "your preference of function and design available in every future publication of this software.";
		
		mSay = "";
		//if( total == 0 ){
			Cursor db = null;
			db = SqliteWrapper.query(mContext, mResolver, DataProvider.CONTENT_URI, new String[] {"count(*)"} , "status > 0", null, null);
			// and (read == 0 or read > "+(System.currentTimeMillis() - (5 * 60 * 1000) )+") and ( seen == 0 or seen > "+(System.currentTimeMillis() - (5 * 60 * 1000) )+" )"
			if( db != null ){ if( db.moveToFirst() ){ total = db.getInt(0); } db.close(); }
							
			//i(TAG, "getFeeds() 995 name("+longname+") RecordCount("+totalCount+")");
			if( total > 0 ){
				mPreferencesEditor.putInt("total", total);
				mPreferencesEditor.commit();
			}
		//}
	
		if( total == 0 || mConsoleStartup ){
			
			if( mNoRecords == 0 ){
				mNoRecords = System.currentTimeMillis();
			}
			int secondsinstate = (int) ( (System.currentTimeMillis() - mNoRecords)/1000 );
			
			if( secondsinstate > 10 && !mConsoleStartup ){

			}else 
			if( mLastStatement == LOADING_RECORDS){
				mSay = THANKYOU;
			} else if( mLastStatement == THANKYOU ) {
				mSay = UPGRADE0;
			} else if( mLastStatement == UPGRADE0 ) {
				mSay = UPGRADE1;
			} else if( mLastStatement == UPGRADE1 ) {
				mSay = UPGRADE2;
			} else if( mLastStatement == UPGRADE2 ) {
				mConsoleStartup = false;
				//mClearScreen = true;
				if( total > 0 ){
					mSay = "Okay folks,\n" + total + " records ready";
					if(httppagetime > 0 && (System.currentTimeMillis() - httppagetime) < 30000 ){
						
					}
					else if( sisLast > 0 ){
						if( sisLast > 30 ){
							//mSay += " and more coming in slowly\nas recent as " + sisLast + " seconds ago.";
							// Let other checks look at this.
						}else if( sisLast > 20 ){
							mSay += " and more coming in slowly\nas recent as " + sisLast + " seconds ago";
						}else{
							mSay += " and more coming in\nas recent as " + sisLast + " seconds ago";
						}
					}
					mSay += ".\nEnjoy.";
				}
			
			}else{
				mSay = LOADING_RECORDS;
				mConsoleStartup = true;
			}
			if( mSay.length() > 0 ){
				mLastStatement = mSay;
				text.setText(mSay);
				e(TAG,"refreshConsole() 2547 postDelay total("+total+") mConsoleStartup("+mConsoleStartup+") " + speakspeed);
				handler.postDelayed( (Runnable) mContext, speakspeed);
				return;
			}
		}else{
			mNoRecords = 0;
		}
		
		

		// Low Memory 30 & 300
		// Unexpected Failure 10 & 300
		
		if( (unexpected > 15 && 20 > hour && hour > 8) || (unexpected > 305 && ( hour > 20 || hour < 8 ) ) ){
			if( unexpected > 340 ){
				
			}else
			if( unexpected > 330 ){
				mSay = "Retry must have failed somehow, may I suggest a reboot?";
				mSay += "\nIt was scheduled " + unexpected + " seconds ago";
				mSay += "\nfrom an Unexpected Failure.";
			}else
			if( unexpected > 30 ){
					
			}else
			if( unexpected > 20 ){
				mSay = "Retry must have failed somehow, may I suggest a reboot?";
				mSay += "\nIt was scheduled " + unexpected + " seconds ago";
				mSay += "\nfrom an Unexpected Failure.";
			} else{
				mSay = "Retry scheduled " + unexpected + " seconds ago.";
			}
		}else
		if( unexpected > 0 ){
			mSay = "Your system just had an unexpected failure occur.";
			 
			if( hour < 20 && hour > 8 ){
				if( sisUNX > 10){
					mSay += "\nRetry past "+(sisUNX - 10)+" seconds.";
				}else{
					mSay += "\nRetry in "+(10 - sisUNX)+" seconds.";
				}
			}else{
				if( sisUNX > 300 ){
					mSay += "\nRetry past "+(sisUNX-300)+" seconds.";
				}else{
					mSay += "\nRetry in "+(300-sisUNX)+" seconds.";
				}
			}
		}else
		if( sisCPU > 35 ){
			double load = getload(TAG + " Console");
			int syncLoad = mSharedPreferences.contains("syncload") ? mSharedPreferences.getInt("syncload",4) : 4;
			if( load > (syncLoad * 1.99) ){
				mSay = "System busy and verified\nfor " + sisCPU + " seconds.\nStanding by till it decreases.";
			}else{
				//mSay = "System error, not really busy\nfor " + sisCPU + " seconds.\n";
			}
		}else
		if( sisCPU > 20 ){
			if( sisCPU > 30 ){
				mSay = "System busy\nfor " + sisCPU + " seconds.\nStanding by\npast " + (sisCPU - 30) + " seconds.";
			}else{
				mSay = "System busy\nfor " + sisCPU + " seconds.\nStanding by\nfor " + (30 - sisCPU) + " seconds.";
			}
		}else
		if( sisCPU > 10 ){
			mSay = "System busy\nfor " + sisCPU + " seconds.\nStanding by.";
		}else 
		if( sisCPU > 0 ){
			mSay = "System busy\nfor " + sisCPU + " seconds\ncausing a slowdown\nin this information lane.";
		}else
		if( sisMemory > 10){
			if( hour < 20 && hour > 8 ){// 9am - 7:59pm
				mSay = "System exhausted\nfor " +sisMemory+" seconds\nretry after 30 seconds.";
			}else{
				mSay = "System exhausted\nfor " +sisMemory+" seconds\nretry after 60 seconds."; // service coded for 5 minutes, touch panel 1 minute
			}
			
		}else
		if( sisMemory > 0){
			mSay = "System reports low memory condition\n for " +sisMemory+" seconds";
		}else
		if( sisLast > 25 ){
			
		}else
		if( sisLast > 30 ){
			mSay = "Working Very Slowly\n";
			mSay += "as recent as " +(sisLast)+ " seconds ago.\n";
		}else 
		if( sisLast > 20 ){
			mSay = "Working Slowly\n";
			mSay += "as recent as " +(sisLast)+ " seconds ago.";
		}else 
		if( sisLast > 0 ){
			mSay = "Healthy active operation\n";
			mSay += "as recent as " +(sisLast)+ " seconds ago.";
		}else{
			if( total == 0 ){
				mSay = "Still waiting\nfor the first records.";
			}
			if( !mClearScreen ){
				if( runcount > 99 && runcount/100 < 10 && runcount/100 > 0 ){
					mSay = "Would you be interested\nin a modification for $10?\n\nPress Menu then Customize to order.";
					mClearScreen = true;
				}if( runcount > 999 && runcount/100 < 20 && runcount/100 > 9 ){
					mSay = "Maybe by the time you see this the United Earth Oceans will have already set sail, if not we're looking for support.";
					mClearScreen = true;
				}else{
					if( runcount == 10 || runcount == 30 || runcount == 31 || runcount == 32 || runcount == 33 ){
						mSay = "In pursuit of peace,\nfreedom, and happiness.";
						mClearScreen = true;
					}
				}
			}
		}
		
		if( mSay.length() == 0 ){
			//boolean allverified = false;
			int successcnt = 0;
			int failcnt = 0;
			String faillist = "";
			for( int feedid = 0; feedid < dataFeed.length; feedid++ ){
				
	        	long lastsync = mSharedPreferences.getLong("synclast_"+feedid,0);
	        	long lastsyncactive = mSharedPreferences.contains("synclastactive_"+feedid) ? mSharedPreferences.getLong("synclastactive_"+feedid,0) : -1;
	        	long preflastsync = lastsync > lastsyncactive ? lastsync : lastsyncactive;
	    		long sinceLast = (System.currentTimeMillis() - preflastsync)/1000/60;
	    		if( sinceLast > (syncInterval+2) || lastsync == 0 ){
	    			String longname = dataFeed[feedid][0];
		        	//String baseurl = dataFeed[feedid][3];
	    			
	    			
	    			if( lastsync == 0 ){
	    				
	    				faillist += longname + " is " + (sinceLast - syncInterval) + " minutes past due.";
	    				//i(TAG,"refreshConsoleTouch() healthy service although feed("+longname+") has been requested to be checked and has not yet started. starting service");
	    			}else{
	    				if( sinceLast < syncInterval * 10 ){
	    					failcnt ++;
	    					faillist += longname + " is " + (sinceLast - syncInterval) + " minutes past due.";
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
			
			if( failcnt == 0 && successcnt > 0 ){
				
			}else if(failcnt > 0 && successcnt > 0){
				if( successcnt > failcnt ){
					mSay = "Some sources\nhaven't been updated\nwhile the majority has.\n";
					if( mLastStatement == mSay ){ mSay = ""; }
				}
				
				if( mSay.length() == 0 ){
					if( failcnt < 4 ){
						mSay = "Failures:\n\n" + faillist;
					}else{
						mSay = "Too many sources are unavailable to list."; // reason for all the failures
					}
				}
			}else if(failcnt > 0 && successcnt == 0){
				mSay += "Every source failed.";
			}
			if( mLastStatement == mSay ){mSay = "";}
		}
		
		if( mSay.length() > 0 ){
			mLastStatement = mSay;
			text.setText(mSay);
			e(TAG,"refreshConsole() 2725 postDelay Said ("+mSay+") " + speakspeed);
			handler.postDelayed( (Runnable) mContext, speakspeed);
			return;
		}

		
		
		if( mClearScreen ){

			action.setVisibility(ImageView.GONE);
			if( mConsoleInPause > 0 ){
				action.setVisibility(ImageView.VISIBLE);
				e(TAG,"refreshConsole() 2737 postDelay discovered In Pause 1000");
				handler.postDelayed( (Runnable) mContext, 1000 );
				return;
			}
			//cover.setVisibility(ImageView.GONE);
			text.setVisibility(TextView.GONE);
			title2.setVisibility(ImageView.GONE);
			console.setVisibility(ImageView.GONE);
			cover.setVisibility(ImageView.GONE);
			cover.setKeepScreenOn(false);
			thread.stop();
			
		}else{
			mClearScreen = true;
			//mConsoleReset = false;
			
			int unseen = 0;
			db = null;
			db = SqliteWrapper.query(mContext, mResolver, DataProvider.CONTENT_URI, new String[] {"count(*)"} , "status > 0 AND seen == 0 AND read == 0", null, null);
			// and (read == 0 or read > "+(System.currentTimeMillis() - (5 * 60 * 1000) )+") and ( seen == 0 or seen > "+(System.currentTimeMillis() - (5 * 60 * 1000) )+" )"
			if( db != null ){ if( db.moveToFirst() ){ unseen = db.getInt(0); } db.close(); }
			
			if( 20 > hour && hour > 8 ){
				if( unseen > 0 ){
					mSay = "All systems operational.";
				}else{
					mSay = "All systems operational.";
				}
			}else{
				mSay = "Night-time mode.\nNo Vibration, minimal background automation.";
				
			}
			if( unseen > 0 ){
				mSay += "\n\nYou have " + unseen + " unseen records.";
			}
			mLastStatement = mSay;
			text.setText(mSay);
			e(TAG,"refreshConsole() 2774 postDelay Last presentation before closure. " + speakspeed);
			handler.postDelayed( (Runnable) mContext, speakspeed);
			return;
		}
		
	}

	public void threadRestart(Thread consoleCursor) {
		
		e(TAG,"threadRestart state("+consoleCursor.getState().name()+") threads("+Thread.activeCount()+") threadId("+consoleCursor.getId()+")");
		
		if( consoleCursor.getState() == State.TERMINATED){
			consoleCursor.interrupt();
			if( consoleCursor.isInterrupted() ){
				e(TAG,"threadRestart state("+consoleCursor.isInterrupted()+")");				
			}
			consoleCursor.start();
		}
		
		if( consoleCursor.isAlive() ){
			i(TAG, "onResume() verified mThread is alive");
			e(TAG,"threadRestart() 2784 refreshConsole() postDelay verified thread is alive 300");
			//mHandler.postDelayed((Runnable) this, 300);
		}else{
			
			e(TAG, "threadRestart() 2787 is starting mThread");
			try {
				consoleCursor.start();
			} catch (IllegalThreadStateException e){
				//w(TAG,"onResume() mThread already started.");
				e(TAG,"threadRestart() 2792 refreshConsole() postDelay thread already started (found through IllegalThreadStateException when starting) 300");
				//e.printStackTrace();
				//mHandler.postDelayed((Runnable) this, 300);
				//consoleCursor.run();
			}
		}
		
	}
	

	
}


