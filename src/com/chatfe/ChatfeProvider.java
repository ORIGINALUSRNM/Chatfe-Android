package com.chatfe;

import java.util.HashMap;

import com.chatfe.ChatfeProviderMetaData;
import com.chatfe.ChatfeProviderMetaData.AuthenticateTableMetaData;
import com.chatfe.ChatfeProviderMetaData.TopicTableMetaData;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.database.sqlite.*;


public class ChatfeProvider extends ContentProvider{

	private static HashMap<String, String> sTopicProjectionMap;
	static
	{
		sTopicProjectionMap = new HashMap<String, String>();
		sTopicProjectionMap.put(TopicTableMetaData._ID, TopicTableMetaData._ID);
		
		//topic, remote URL, locl URI, mime type
		sTopicProjectionMap.put(TopicTableMetaData.TOPIC, TopicTableMetaData.TOPIC);
		sTopicProjectionMap.put(TopicTableMetaData.REMOTE_IMG_URL, TopicTableMetaData.REMOTE_IMG_URL);
		sTopicProjectionMap.put(TopicTableMetaData.LOCAL_IMG_URI, TopicTableMetaData.LOCAL_IMG_URI);
		sTopicProjectionMap.put(TopicTableMetaData.IMG_MIME, TopicTableMetaData.IMG_MIME);
		
		//created and modified date
		sTopicProjectionMap.put(TopicTableMetaData.CREATED_DATE, TopicTableMetaData.CREATED_DATE);
		sTopicProjectionMap.put(TopicTableMetaData.MODIFIED_DATE, TopicTableMetaData.MODIFIED_DATE);
		
	}
	
	private static HashMap<String, String> sAuthenticateProjectionMap;
	static
	{
		sAuthenticateProjectionMap = new HashMap<String, String>();
		sAuthenticateProjectionMap.put(AuthenticateTableMetaData._ID, AuthenticateTableMetaData._ID);
		// phone#, density and encryption key.
		sAuthenticateProjectionMap.put(AuthenticateTableMetaData.PHONE, AuthenticateTableMetaData.PHONE);
		sAuthenticateProjectionMap.put(AuthenticateTableMetaData.SCREEN_DENSITY, AuthenticateTableMetaData.SCREEN_DENSITY);
		sAuthenticateProjectionMap.put(AuthenticateTableMetaData.ENCRYPT_KEY, AuthenticateTableMetaData.ENCRYPT_KEY);
		
		//created and modified date 
		sAuthenticateProjectionMap.put(AuthenticateTableMetaData.ENCRYPT_KEY, AuthenticateTableMetaData.ENCRYPT_KEY);
		sAuthenticateProjectionMap.put(AuthenticateTableMetaData.CREATED_DATE, AuthenticateTableMetaData.CREATED_DATE);
		sAuthenticateProjectionMap.put(AuthenticateTableMetaData.MODIFIED_DATE, AuthenticateTableMetaData.MODIFIED_DATE);
		
		
		
	}
	
	private static final UriMatcher sUriMatcher;
	private static final int INCOMING_TOPIC_COLLECTION_URI_INDICATOR = 1;
	private static final int INCOMING_SINGLE_TOPIC_URI_INDICATOR = 2;
	private static final int INCOMING_AUTHENTICATE_COLLECTION_URI_INDICATOR = 3;
	private static final int INCOMING_SINGLE_AUTHENTICATE_URI_INDICATOR = 4;
	
	static{
		
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(ChatfeProviderMetaData.AUTHORITY, "topics", INCOMING_TOPIC_COLLECTION_URI_INDICATOR);
		sUriMatcher.addURI(ChatfeProviderMetaData.AUTHORITY, "topics/#", INCOMING_SINGLE_TOPIC_URI_INDICATOR);
		sUriMatcher.addURI(ChatfeProviderMetaData.AUTHORITY, "authenticate", INCOMING_AUTHENTICATE_COLLECTION_URI_INDICATOR);
		sUriMatcher.addURI(ChatfeProviderMetaData.AUTHORITY, "authenticate/#", INCOMING_SINGLE_AUTHENTICATE_URI_INDICATOR);
		
		
	}
	
	private DatabaseHelper mOpenHelper;
	
	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		public static final String TAG = DatabaseHelper.class.getSimpleName();
		DatabaseHelper(Context context){
			super(context, ChatfeProviderMetaData.DATABASE_NAME, null, 
					ChatfeProviderMetaData.DATABASE_VERSION);
			
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			
			db.execSQL("CREATE TABLE" + TopicTableMetaData.TABLE_NAME + " ("
					+ ChatfeProviderMetaData.TopicTableMetaData._ID
					+ " INTEGER PRIMARY KEY,"
					+ TopicTableMetaData.TOPIC + " TEXT,"
					+ TopicTableMetaData.REMOTE_IMG_URL + " TEXT,"
					+ TopicTableMetaData.LOCAL_IMG_URI + " TEXT,"
					+ TopicTableMetaData.IMG_MIME + " TEXT,"
					+ TopicTableMetaData.CREATED_DATE + " INTEGER,"
					+ TopicTableMetaData.MODIFIED_DATE + " INTEGER"
					+ ");");
			
			db.execSQL("CREATE TABLE" + AuthenticateTableMetaData.TABLE_NAME + " ("
					+ ChatfeProviderMetaData.AuthenticateTableMetaData._ID 
					+ " INTEGER PRIMARY KEY,"
					+ AuthenticateTableMetaData.PHONE + " TEXT,"
					+ AuthenticateTableMetaData.SCREEN_DENSITY + " TEXT,"
					+ AuthenticateTableMetaData.ENCRYPT_KEY + " TEXT,"
					+ AuthenticateTableMetaData.CREATED_DATE + " INTEGER,"
					+ AuthenticateTableMetaData.MODIFIED_DATE + " INTEGER"
					+ ");");
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + TopicTableMetaData.TABLE_NAME);
			onCreate(db);
			
		}
		
	}
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		String rowId;
		switch(sUriMatcher.match(uri)){
		case INCOMING_TOPIC_COLLECTION_URI_INDICATOR:
			count = db.delete(TopicTableMetaData.TABLE_NAME, where, whereArgs);
			break;
		case INCOMING_SINGLE_TOPIC_URI_INDICATOR:
			rowId = uri.getPathSegments().get(1);
			count = db.delete(TopicTableMetaData.TABLE_NAME
							  , TopicTableMetaData._ID + "=" +rowId
							  + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "")
							  , whereArgs);
			break;
		case INCOMING_AUTHENTICATE_COLLECTION_URI_INDICATOR:
			count = db.delete(AuthenticateTableMetaData.TABLE_NAME, where, whereArgs);
			break;
		case INCOMING_SINGLE_AUTHENTICATE_URI_INDICATOR:
			rowId = uri.getPathSegments().get(1);
			count = db.delete(AuthenticateTableMetaData.TABLE_NAME
							  , AuthenticateTableMetaData._ID + "=" + rowId
							  + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "")
							  , whereArgs);
		default:
			throw new IllegalArgumentException("unknown URI " + uri);
		
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch(sUriMatcher.match(uri)){
		case INCOMING_TOPIC_COLLECTION_URI_INDICATOR:
			return TopicTableMetaData.CONTENT_TYPE;
		case INCOMING_SINGLE_TOPIC_URI_INDICATOR:
			return TopicTableMetaData.CONTENT_ITEM_TYPE;
		case INCOMING_AUTHENTICATE_COLLECTION_URI_INDICATOR:
			return AuthenticateTableMetaData.CONTENT_TYPE;
		case INCOMING_SINGLE_AUTHENTICATE_URI_INDICATOR:
			return AuthenticateTableMetaData.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		//validate uri
		if(sUriMatcher.match(uri) != INCOMING_TOPIC_COLLECTION_URI_INDICATOR || 
		   sUriMatcher.match(uri) != INCOMING_AUTHENTICATE_COLLECTION_URI_INDICATOR	){
			throw new IllegalArgumentException("Unknown URI " + uri);
			
		}
		if(sUriMatcher.match(uri) == INCOMING_TOPIC_COLLECTION_URI_INDICATOR){
		
				Long now = Long.valueOf(System.currentTimeMillis());
				if(values.containsKey(TopicTableMetaData.CREATED_DATE) == false){
					values.put(TopicTableMetaData.CREATED_DATE, now);
					
				}
				if(values.containsKey(TopicTableMetaData.MODIFIED_DATE) == false){
					values.put(TopicTableMetaData.MODIFIED_DATE, now);
					
				}
				if(values.containsKey(TopicTableMetaData.TOPIC) == false){
					throw new SQLException("Failed to insert row because must provide topic. " + uri);
					
				}
				if(values.containsKey(TopicTableMetaData.REMOTE_IMG_URL) == false){
					throw new SQLException("Failed to insert row because must provide remote img url. " + uri);
					
				}
				if(values.containsKey(TopicTableMetaData.LOCAL_IMG_URI) == false){
					throw new SQLException("Failed to insert row because must provide local img uri. " + uri);
					
				}
				
				SQLiteDatabase db = mOpenHelper.getWritableDatabase();
				long rowId = db.insert(TopicTableMetaData.TABLE_NAME, TopicTableMetaData.TOPIC, values);
				if(rowId > 0){
					Uri insertedTopicUri = ContentUris.withAppendedId(TopicTableMetaData.CONTENT_URI, rowId);
					getContext().getContentResolver().notifyChange(insertedTopicUri, null);
					return insertedTopicUri;
					
				}
				
				throw new SQLException("Failed to insert row into " + uri);
		}
		else if(sUriMatcher.match(uri) == INCOMING_AUTHENTICATE_COLLECTION_URI_INDICATOR){
			
			Long now = Long.valueOf(System.currentTimeMillis());
			if(values.containsKey(AuthenticateTableMetaData.CREATED_DATE) == false){
				values.put(AuthenticateTableMetaData.CREATED_DATE, now);
				
			}
			if(values.containsKey(AuthenticateTableMetaData.MODIFIED_DATE) == false){
				values.put(AuthenticateTableMetaData.MODIFIED_DATE, now);
				
			}
			if(values.containsKey(AuthenticateTableMetaData.PHONE) == false){
				throw new SQLException("Failed to insert row because must provide phone number. " + uri);
				
			}
			if(values.containsKey(AuthenticateTableMetaData.SCREEN_DENSITY) == false){
				throw new SQLException("Failed to insert row because must provide density. " + uri);
				
			}
			if(values.containsKey(AuthenticateTableMetaData.ENCRYPT_KEY) == false){
				throw new SQLException("Failed to insert row because must provide encryption key. " + uri);
				
			}
			
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			long rowId = db.insert(AuthenticateTableMetaData.TABLE_NAME, AuthenticateTableMetaData.PHONE, values);
			if(rowId > 0){
				Uri insertedTopicUri = ContentUris.withAppendedId(AuthenticateTableMetaData.CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(insertedTopicUri, null);
				return insertedTopicUri;
				
			}
			
			throw new SQLException("Failed to insert row into " + uri);
			
			
		}
		else
		{
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		switch(sUriMatcher.match(uri))
		{
			case INCOMING_TOPIC_COLLECTION_URI_INDICATOR:
				qb.setTables(TopicTableMetaData.TABLE_NAME);
				qb.setProjectionMap(sTopicProjectionMap);
				break;
			case INCOMING_SINGLE_TOPIC_URI_INDICATOR:
				qb.setTables(TopicTableMetaData.TABLE_NAME);
				qb.setProjectionMap(sTopicProjectionMap);
				qb.appendWhere(TopicTableMetaData._ID + "="
						+ uri.getPathSegments().get(1));
				break;
			case INCOMING_AUTHENTICATE_COLLECTION_URI_INDICATOR:
				qb.setTables(AuthenticateTableMetaData.TABLE_NAME);
				qb.setProjectionMap(sAuthenticateProjectionMap);
				break;
			case INCOMING_SINGLE_AUTHENTICATE_URI_INDICATOR:
				qb.setTables(AuthenticateTableMetaData.TABLE_NAME);
				qb.setProjectionMap(sAuthenticateProjectionMap);
				qb.appendWhere(AuthenticateTableMetaData._ID + "="
						+ uri.getPathSegments().get(1));
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		
		
		}
		String orderBy;
		if(sUriMatcher.match(uri) == INCOMING_TOPIC_COLLECTION_URI_INDICATOR ||
		   sUriMatcher.match(uri) == INCOMING_TOPIC_COLLECTION_URI_INDICATOR)
		{
			
			if(TextUtils.isEmpty(sortOrder)){
				orderBy = TopicTableMetaData.DEFAULT_SORT_ORDER;
				
			}else{
				orderBy = sortOrder;
				
			}
			
		}
		else if(sUriMatcher.match(uri) == INCOMING_AUTHENTICATE_COLLECTION_URI_INDICATOR ||
				sUriMatcher.match(uri) == INCOMING_SINGLE_AUTHENTICATE_URI_INDICATOR){
			if(TextUtils.isEmpty(sortOrder)){
				orderBy = AuthenticateTableMetaData.DEFAULT_SORT_ORDER;
				
			}else{
				orderBy = sortOrder;
				
			}
			
		}
		else {throw new IllegalArgumentException("Unknown URI " + uri);}
				
		
		// Get the database and run the query
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
		int i = c.getCount();
		
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		String rowId;
		switch(sUriMatcher.match(uri)){
		case INCOMING_TOPIC_COLLECTION_URI_INDICATOR:
			count = db.update(TopicTableMetaData.TABLE_NAME, values, where, whereArgs);
			break;
		case INCOMING_SINGLE_TOPIC_URI_INDICATOR:
			rowId = uri.getPathSegments().get(1);
			count = db.update(TopicTableMetaData.TABLE_NAME, 
							  values, 
							  TopicTableMetaData._ID + "=" + rowId
							  + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "")
							  , whereArgs);
			break;
		case INCOMING_AUTHENTICATE_COLLECTION_URI_INDICATOR:
			count = db.update(AuthenticateTableMetaData.TABLE_NAME, values, where, whereArgs);
			break;
		case INCOMING_SINGLE_AUTHENTICATE_URI_INDICATOR:
			rowId = uri.getPathSegments().get(1);
			count = db.update(AuthenticateTableMetaData.TABLE_NAME, 
							  values, 
							  TopicTableMetaData._ID + "=" + rowId
							  + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "")
							  , whereArgs);
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}
