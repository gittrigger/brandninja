package com.havenskys.thescoopseattle;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class DataProvider extends ContentProvider {
	
	private static String TAG = "DataProvider";

	private SQLiteDatabase mDb;
    private DatabaseHelper mDbHelper;
    private Custom mLog;
    private static final int DB_VERSION = 22;
    private static final int ALL_MESSAGES = 1;
    private static final int SPECIFIC_MESSAGE = 2;
    
    private static final UriMatcher URI_MATCHER;
    static{
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(Custom.MAINURI, "blogitem", ALL_MESSAGES);
        URI_MATCHER.addURI(Custom.MAINURI, "blogitem/#", SPECIFIC_MESSAGE);
    }

    public static final Uri CONTENT_URI = Uri.parse( "content://"+Custom.MAINURI+"/blogitem");
	
    //url text unique not null, urltext text, farkpicurl text, farkpictext text, commenturl text, commenttext text, description text
    
 

    // Database creation/version management helper.
    // Create it statically because we don't need to have customized instances.
    private static class DatabaseHelper extends SQLiteOpenHelper {

    	private static String TAG = "DataProviderDB";
    	private Custom mLog;
    	private Context mContext;
        //public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
			//super(context, name, factory, version);
			// TODO Auto-generated constructor stub
		//}
        DatabaseHelper(Context context) {
            super(context, Custom.DATABASE_NAME, null, DB_VERSION);
            mContext = context;
            mLog = new Custom(mContext, TAG + " DatabaseHelper() 52");
            mLog.i(TAG, "DatabaseHelper() 53");
        }

		@Override
        public void onCreate(SQLiteDatabase db){
			
			mLog.i(TAG,"DatabaseHelper onCreate() ++++++++++++++++++++++++");
        	try{
                db.execSQL( mLog.getContentSQL() );
            } catch (SQLException e) {
            	e.printStackTrace();
            }
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        	mLog.i(TAG,"DatabaseHelper onUpgrade() ++++++++++++++++++++++++");
            db.execSQL("DROP TABLE IF EXISTS " + Custom.DATABASE_TABLE_NAME);
            onCreate(db);
        }

    }
    
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		//mLog.w(TAG,"delete() uri("+uri+") lastsegment("+uri.getLastPathSegment()+")");
		//int rowCount = mDb.delete(Custom.DATABASE_TABLE_NAME, Custom.ID + " = " + uri.getLastPathSegment(), selectionArgs);

		int rowCount = mDb.delete(Custom.DATABASE_TABLE_NAME, selection, selectionArgs);
		
        // Notify any listeners and return the deleted row count.
        getContext().getContentResolver().notifyChange(uri, null);
        return rowCount;

	}

	@Override
	public String getType(Uri uri) {
		
        switch (URI_MATCHER.match(uri)){
        case ALL_MESSAGES:
        	mLog.w(TAG,"gettype() uri("+uri+") ALL MESSAGES");
            return "vnd.android.cursor.dir/blogitem"; // List of items.
        case SPECIFIC_MESSAGE:
        	mLog.w(TAG,"gettype() uri("+uri+") Specific Message");
            return "vnd.android.cursor.item/blogitem";     // Specific item.
        default:
            return null;
        }
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if( mDb == null ){
			return null;
		}
		long rowId = -1;
       rowId = mDb.insert(Custom.DATABASE_TABLE_NAME, "rawcontent", values);
       Uri newUri = Uri.withAppendedPath(CONTENT_URI, ""+rowId);
       //mLog.w(TAG,"insert()  newUri(" + newUri.toString() + ")");
       
       
       // Notify any listeners and return the URI of the new row.
       getContext().getContentResolver().notifyChange(CONTENT_URI, null);
       
       /*/
       if( rowId > 100 ){
    	   int del = (int) (rowId - 100);
    	   mDb.execSQL("update " + DATABASE_TABLE_NAME + " set "+ CONTENT +" = \"\" where _id < "+del+" ");
       }
       //*/
       
       return newUri;

	}

	@Override
	public boolean onCreate() {

		mLog = new Custom(this.getContext(), TAG + " onCreate() 130");
		mDbHelper = new DatabaseHelper(getContext());

		//final Context con = getContext();
        try{
        	mDb = mDbHelper.getWritableDatabase();
        	
            //mDb = mDbHelper.openDatabase(getContext(), DATABASE_NAME, null, DB_VERSION);
            //mLogger.info("RssContentProvider.onCreate(): Opened a database");
        } catch (Exception ex) {
        	mLog.e(TAG,"Failed to connected to Database, exception");
        	ex.printStackTrace();
              return false;
        }
        if(mDb == null){
        	mLog.e(TAG,"Failed to connected to Database, mDb null");
            return false;
        } else {
            return true;
        }

	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		
		if( mDb == null ){
			return null;
		}
		SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();

        // Set the table we're querying.
        qBuilder.setTables(Custom.DATABASE_TABLE_NAME);

        // If the query ends in a specific record number, we're
        // being asked for a specific record, so set the
        // WHERE clause in our query.
        if((URI_MATCHER.match(uri)) == SPECIFIC_MESSAGE){
            qBuilder.appendWhere("_id=" + uri.getLastPathSegment()); // + uri.getPathLeafId());
        }

        // Set sort order. If none specified, use default.
        if(TextUtils.isEmpty(sortOrder)){
            sortOrder = Custom.DEFAULT_SORT_ORDER;
        }

        // Make the query.
        Cursor c = qBuilder.query(mDb,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		
		//mLog.w(TAG,"update() uri("+uri+") lastsegment("+uri.getLastPathSegment()+") selection("+selection+")");
		
		// NOTE Argument checking code omitted. Check your parameters!
        int updateCount = mDb.update(Custom.DATABASE_TABLE_NAME, values, selection, selectionArgs);

        // Notify any listeners and return the updated row count.
        //getContext().getContentResolver().notifyUpdate(uri, null);
        getContext().getContentResolver().notifyChange(uri, null);
		return updateCount;
	}
	
	
	
}
