package com.fahimk.readabilityclient;

import static com.fahimk.readabilityclient.HelperMethods.*;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ArticlesSQLiteOpenHelper extends SQLiteOpenHelper {

	public static final int VERSION = 5;
	public static final String DB_NAME = "article_db.sqlite";
	public static String MY_ID = "my_id";
	public static String ARTICLE_TABLE = "article";
	public static String ARTICLE_ID = "article_id";
	public static String ARTICLE_AUTHOR = "author";
	public static String ARTICLE_CONTENT = "content";
	public static String ARTICLE_CONTENT_SIZE = "content_size";
	public static String ARTICLE_DOMAIN = "domain";
	public static String ARTICLE_SHORT_URL = "short_url";
	public static String ARTICLE_TITLE = "title";
	public static String ARTICLE_URL = "url";
	public static String READ_PERCENT = "read_percent";
	public static String DATE_UPDATED = "date_updated";
	public static String FAVORITE = "favorite";
	public static String BOOKMARK_ID = "bookmark_id";
	public static String DATE_ADDED = "date_added";
	public static String ARTICLE_HREF = "article_href";
	public static String DATE_FAVORITED = "date_favorited";
	public static String ARCHIVE = "archive";
	Context c;
	
	public ArticlesSQLiteOpenHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
		c = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db);
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.delete(ARTICLE_TABLE, null, null);
		SharedPreferences preferences = c.getSharedPreferences(PREF_NAME, 0);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("previous_update", zeroUpdate);
		editor.commit();
	}

	protected void createTables(SQLiteDatabase db) {
		db.execSQL(
				"create table " + ARTICLE_TABLE + " (" +
				MY_ID + " integer primary key autoincrement not null, " +
				ARTICLE_AUTHOR + " text, " +
				ARTICLE_CONTENT + " text, " +
				ARTICLE_CONTENT_SIZE + " integer, " + 
				ARTICLE_DOMAIN + " text, " +
				ARTICLE_SHORT_URL + " text, " +
				ARTICLE_TITLE + " text, " +
				ARTICLE_URL + " text, " +
				READ_PERCENT + " real, " +
				DATE_UPDATED + " text, " +
				FAVORITE + " integer, " +
				ARTICLE_ID + " integer, " +
				BOOKMARK_ID + " integer, " +
				DATE_ADDED + " text, " +
				ARTICLE_HREF + " text, " +
				DATE_FAVORITED + " text, " +
				ARCHIVE + " integer" +
				");"
		);
	}

}
