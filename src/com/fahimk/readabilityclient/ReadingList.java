package com.fahimk.readabilityclient;

import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.*;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;

import com.fahimk.jsonobjects.Article;

public class ReadingList extends TabActivity {

	String oauthToken;
	String oauthTokenSecret;
	String oauthVerifier;
	private SQLiteDatabase database;
	
	private static final String READ_TAB_TAG = "Reading List";
	private static final String FAV_TAB_TAG = "Favorites";
	private static final String ARC_TAB_TAG = "Archives";

	private TabHost tabHost;
	
	private ArrayList<Article> favArticlesInfo;
	private ListView favList;
	
	private ArrayList<Article> readArticlesInfo;
	private ListView readList;
	

	private ArrayList<Article> arcArticlesInfo;
	private ListView arcList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reading_list);
		setupDB();
		setupLists();
		setupTabs();
	}
	
	public void onResume() {
		super.onResume();
		setupLists();
	}
	
	public void setupDB() {
		ArticlesSQLiteOpenHelper helper = new ArticlesSQLiteOpenHelper(this);
		database = helper.getWritableDatabase();
	}
	
	public void onDestroy() {
		super.onDestroy();
		database.close();
	}

	public void setupTabs() {
		tabHost.getTabWidget().setDividerDrawable(R.drawable.tab_divider);

		addTab(readList, READ_TAB_TAG, tabHost);
		addTab(favList, FAV_TAB_TAG, tabHost);
		addTab(arcList, ARC_TAB_TAG, tabHost);
		
		tabHost.setCurrentTab(2);
		tabHost.setCurrentTab(1);
		tabHost.setCurrentTab(0);
	}
	
		
	private void addTab(final View view, final String tag, TabHost th) {
		View tabview = createTabLabel(th.getContext(), tag);
	        TabSpec setContent = th.newTabSpec(tag).setIndicator(tabview).setContent(new TabContentFactory() {
			public View createTabContent(String tag) {return view;}
		});
		th.addTab(setContent);
	}

	private static View createTabLabel(final Context context, final String text) {
		View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		return view;
	}
	
	public void setupLists() {
		tabHost = getTabHost();
		readList = (ListView) findViewById(R.id.list_read);
		favList = (ListView) findViewById(R.id.list_fav);
		arcList = (ListView) findViewById(R.id.list_arch);

		readArticlesInfo = new ArrayList<Article>();
		favArticlesInfo = new ArrayList<Article>();
		arcArticlesInfo = new ArrayList<Article>();
		
		
		readList.setAdapter(getAdapterQuery(ARCHIVE + "=0", readArticlesInfo));
		favList.setAdapter(getAdapterQuery(FAVORITE + "=1", favArticlesInfo));
		arcList.setAdapter(getAdapterQuery(ARCHIVE + "=1", arcArticlesInfo));
		
		readList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Intent i = new Intent(getBaseContext(), WebActivity.class);
				i.putExtra("saved", true);
				i.putExtra("article_content", readArticlesInfo.get(position).content);
				i.putExtra("archive", "0");
				i.putExtra("article_url", readArticlesInfo.get(position).id);
				i.putExtra("full_url", readArticlesInfo.get(position).url);
				i.putExtra("favorite", readArticlesInfo.get(position).favorite);
				i.putExtra("bookmark_id", readArticlesInfo.get(position).bookmark_id);
				i.putExtra("article_title", readArticlesInfo.get(position).title);
				i.putExtra("read_percent", readArticlesInfo.get(position).read_percent);
				startActivity(i);
			}
		});
		
		favList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Intent i = new Intent(getBaseContext(), WebActivity.class);
				i.putExtra("saved", true);
				i.putExtra("favorite", "1");
				i.putExtra("archive", favArticlesInfo.get(position).archive);
				i.putExtra("article_url", favArticlesInfo.get(position).id);
				i.putExtra("article_content", favArticlesInfo.get(position).content);
				i.putExtra("full_url", favArticlesInfo.get(position).url);
				i.putExtra("bookmark_id", favArticlesInfo.get(position).bookmark_id);
				i.putExtra("article_title", readArticlesInfo.get(position).title);
				i.putExtra("read_percent", readArticlesInfo.get(position).read_percent);
				startActivity(i);
			}
		});
		
		arcList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Intent i = new Intent(getBaseContext(), WebActivity.class);
				i.putExtra("saved", true);
				i.putExtra("archive", "1");
				i.putExtra("article_url", arcArticlesInfo.get(position).id);
				i.putExtra("full_url", arcArticlesInfo.get(position).url);
				i.putExtra("bookmark_id", arcArticlesInfo.get(position).bookmark_id);
				i.putExtra("article_title", readArticlesInfo.get(position).title);
				i.putExtra("read_percent", readArticlesInfo.get(position).read_percent);
				startActivity(i);
			}
		});

	}

	public ReadingListAdapter getAdapterQuery(String filter, ArrayList<Article> articleInfo) {
		Log.e("getAdapterQuery", "running query");
		//String url, String domain, String id, String title, String content
		String[] getStrColumns = new String[] {ARTICLE_URL, ARTICLE_DOMAIN, ARTICLE_ID, ARTICLE_TITLE, ARTICLE_CONTENT, BOOKMARK_ID, FAVORITE, ARCHIVE, READ_PERCENT};
		Cursor ac = database.query(
				ARTICLE_TABLE,
				getStrColumns,
				filter, null, null, null, DATE_ADDED + " DESC");
		ac.moveToFirst();
		if(!ac.isAfterLast()) {
			do {
				Article tempArticle = new Article(ac.getString(0),ac.getString(1),ac.getString(2),ac.getString(3),ac.getString(4), ac.getString(5), ac.getString(6), ac.getString(7), ac.getString(8));
				articleInfo.add(tempArticle);
			} while (ac.moveToNext());
		}
		ac.close();
		return new ReadingListAdapter(getBaseContext(), articleInfo);
	}
}
