package com.chatfe;

import android.net.Uri;
import android.provider.BaseColumns;


public class ChatfeProviderMetaData {
	
	public static final String AUTHORITY = "com.chatfe.ChatfeProvider";
	
	public static final String DATABASE_NAME = "chatfe.db";
	public static final int DATABASE_VERSION = 1;
	public static final String TOPICS_TABLE_NAME = "topics";
	public static final String AUTHENTICATE_TABLE_NAME = "authenticate";
	
	private ChatfeProviderMetaData(){}
	
	//inner class describing TopicTable
	
	public static final class TopicTableMetaData implements BaseColumns{
		
		private TopicTableMetaData(){}
		public  static final String TABLE_NAME = "topics";
		
		// uri and mime type definitions
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/topics");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.chatfe.topic";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.chatfe.topic";
		public static final String DEFAULT_SORT_ORDER = "modified ASC";
		
		//additional columns
		public static final String TOPIC = "topic";
		public static final String REMOTE_IMG_URL = "remote_img_url";
		public static final String LOCAL_IMG_URI = "local_img_uri";
		public static final String IMG_MIME = "img_mime";
		public static final String CREATED_DATE = "created";
		public static final String MODIFIED_DATE = "modified";
		
	}
	
	public static final class AuthenticateTableMetaData implements BaseColumns{
		
		private AuthenticateTableMetaData(){}
		public static final String TABLE_NAME = "authenticate";
		public static final String DEFAULT_SORT_ORDER = "modified ASC";
		
		// uri and mime type
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/authenticate");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.chatfe.authenticate";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.item/vnd.chatfe.authenticate";
		
		// additional columns
		public static final String PHONE = "phone";
		public static final String  SCREEN_DENSITY = "screen_density";
		public static final String ENCRYPT_KEY = "encrypt_key";
		public static final String CREATED_DATE = "created";
		public static final String MODIFIED_DATE = "modified";
		
		
		
		
	}
	


}
