package com.fahimk.readabilityclient;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ArticlesSQLiteOpenHelper extends SQLiteOpenHelper {

	public static final int VERSION = 1;
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

	public ArticlesSQLiteOpenHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
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
