package com.fahimk.readabilityclient;

import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.*;

import java.util.ArrayList;
import java.util.List;

import android.app.TabActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;

public class ReadingList extends TabActivity {

	String oauthToken;
	String oauthTokenSecret;
	String oauthVerifier;
	private SQLiteDatabase database;
	
	private static final String READ_TAB_TAG = "Reading List";
	private static final String FAV_TAB_TAG = "Favorites";
	private static final String ARC_TAB_TAG = "Archives";

	private TabHost tabHost;
	
	private ArrayList<String> favArticleIds;
	private ListView favList;
	
	private ArrayList<String> readArticleIds;
	private ListView readList;
	
	private ArrayList<String> arcArticleIds;
	private ListView arcList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reading_list);
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
		tabHost = getTabHost();
		readList = (ListView) findViewById(R.id.list_read);
		favList = (ListView) findViewById(R.id.list_fav);
		arcList = (ListView) findViewById(R.id.list_arch);

		readArticleIds = new ArrayList<String>();
		favArticleIds = new ArrayList<String>();
		arcArticleIds = new ArrayList<String>();
		
		readList.setAdapter(getAdapterQuery(ARCHIVE + "=0", readArticleIds));
		favList.setAdapter(getAdapterQuery(FAVORITE + "=1", favArticleIds));
		arcList.setAdapter(getAdapterQuery(ARCHIVE + "=1", arcArticleIds));
		
		readList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Intent i = new Intent(getBaseContext(), WebActivity.class);
				Log.e("content", readArticleIds.get(position));
				i.putExtra("article_id", readArticleIds.get(position));
				startActivity(i);
			}
		});
		
		favList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Intent i = new Intent(getBaseContext(), WebActivity.class);
				i.putExtra("article_id", favArticleIds.get(position));
				startActivity(i);
			}
		});
		
		arcList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Intent i = new Intent(getBaseContext(), WebActivity.class);
				i.putExtra("article_id", arcArticleIds.get(position));
				startActivity(i);
			}
		});
		
		// add views to tab host
		tabHost.addTab(tabHost.newTabSpec(READ_TAB_TAG).setIndicator(READ_TAB_TAG).setContent(new TabContentFactory() {
			public View createTabContent(String arg0) {
				return readList;
			}
			
		}));
		tabHost.addTab(tabHost.newTabSpec(FAV_TAB_TAG).setIndicator(FAV_TAB_TAG).setContent(new TabContentFactory() {
			public View createTabContent(String arg0) {
				return favList;
			}
		}));
		tabHost.addTab(tabHost.newTabSpec(ARC_TAB_TAG).setIndicator(ARC_TAB_TAG).setContent(new TabContentFactory() {
			public View createTabContent(String arg0) {
				return arcList;
			}
		}));
		tabHost.setCurrentTab(2);
		tabHost.setCurrentTab(1);
		tabHost.setCurrentTab(0);
/*		List<String> articleTitles = new ArrayList<String>();
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
		});*/
	}

	public ArrayAdapter<String> getAdapterQuery(String filter, ArrayList<String> contentList) {
		Log.e("getAdapterQuery", "running query");
		List<String> articleTitles = new ArrayList<String>();
		Cursor articlesCursor = database.query(
				ARTICLE_TABLE,
				new String[] {ARTICLE_TITLE, ARTICLE_CONTENT},
				filter, null, null, null, DATE_ADDED + " DESC");
		articlesCursor.moveToFirst();
		if(!articlesCursor.isAfterLast()) {
			do {
				articleTitles.add(articlesCursor.getString(0));
				contentList.add(articlesCursor.getString(1));
			} while (articlesCursor.moveToNext());
		}
		articlesCursor.close();
		return new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, articleTitles);
	}
}
