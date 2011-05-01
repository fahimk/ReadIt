package com.fahimk.readabilityclient;

import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.*;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ReadingList extends Activity {

	String oauthToken;
	String oauthTokenSecret;
	String oauthVerifier;
	private SQLiteDatabase database;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reading_list);
//		SharedPreferences tokenInfo = getBaseContext().getSharedPreferences(PREF_NAME, 0);
//		oauthToken = tokenInfo.getString("oauth_token", null); 
//		oauthTokenSecret = tokenInfo.getString("oauth_token_secret", null);
//		oauthVerifier = tokenInfo.getString("oauth_verifier", null);
		setupDB();
		setupViews();
	}

	public void setupDB() {
		ArticlesSQLiteOpenHelper helper = new ArticlesSQLiteOpenHelper(this);
		database = helper.getWritableDatabase();
	}
	
	public void onDestroy() {
		super.onDestroy();
		database.close();
	}


	public void setupViews() {
		List<String> articleTitles = new ArrayList<String>();
		final List<String> articleContent = new ArrayList<String>();
		Cursor articlesCursor = database.query(
				ARTICLE_TABLE,
				new String[] {ARTICLE_TITLE, ARTICLE_CONTENT},
				null, null, null, null, DATE_ADDED);
		articlesCursor.moveToFirst();
		if(!articlesCursor.isAfterLast()) {
			do {
				articleTitles.add(articlesCursor.getString(0));
				articleContent.add(articlesCursor.getString(1));
			} while (articlesCursor.moveToNext());
		}
		articlesCursor.close();
		ListView a = (ListView) findViewById(R.id.list_bookmarks);
		a.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, articleTitles));
		a.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Intent i = new Intent(getBaseContext(), WebActivity.class);
				i.putExtra("content", articleContent.get(position));
				startActivity(i);
			}
		});
	}
}
