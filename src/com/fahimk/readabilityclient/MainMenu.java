package com.fahimk.readabilityclient;

import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.*;
import static com.fahimk.readabilityclient.HelperMethods.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.fahimk.jsonobjects.Bookmark;
import com.fahimk.jsonobjects.SearchArticle;
import com.fahimk.jsonobjects.SearchBookmarks;
import com.google.gson.Gson;

public class MainMenu extends Activity {

	private SQLiteDatabase database;
	private String zeroUpdate = "2011-01-01 00:00:00";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu);
		setupDB();
		setupViews();
	}

	public void setupDB() {
		ArticlesSQLiteOpenHelper helper = new ArticlesSQLiteOpenHelper(this);
		database = helper.getWritableDatabase();
		//database.delete(ARTICLE_TABLE, null, null);
	}

	public void onDestroy() {
		super.onDestroy();
		database.close();
	}

	public void setupViews() {
		Button readButton = (Button) findViewById(R.id.button_read);
		Button syncButton = (Button) findViewById(R.id.button_sync);
		Button addButton = (Button) findViewById(R.id.button_add);
		Button deleteButton = (Button) findViewById(R.id.button_delete);
		Button exitButton = (Button) findViewById(R.id.button_exit);

		deleteButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				database.delete(ARTICLE_TABLE, null, null);
				SharedPreferences preferences = getBaseContext().getSharedPreferences(PREF_NAME, 0);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString("previous_update", zeroUpdate);
				editor.commit();
			}
		});

		readButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				startActivity(new Intent(getBaseContext(), ReadingList.class));
			}
		});

		syncButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new SyncArticles().execute();
			}
		});

		exitButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				database.close();
				MainMenu.this.finish();
			}
		});

	}


	private class SyncArticles extends AsyncTask<Void, Integer, Boolean> {
		ProgressDialog progressDialog;
		ProgressDialog tempDialog;
		String oauthToken;
		String oauthTokenSecret;
		String oauthVerifier;
		SharedPreferences preferences;
		String previousUpdate;

		protected void onPreExecute() {
			preferences = getBaseContext().getSharedPreferences(PREF_NAME, 0);
			oauthToken = preferences.getString("oauth_token", null); 
			oauthTokenSecret = preferences.getString("oauth_token_secret", null);
			oauthVerifier = preferences.getString("oauth_verifier", null);
			previousUpdate = preferences.getString("previous_update", zeroUpdate);

			tempDialog = new ProgressDialog(MainMenu.this);
			tempDialog.setMessage("Connecting to server...");
			tempDialog.show();

			progressDialog = new ProgressDialog(MainMenu.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMessage("Synchronizing Articles...");
			progressDialog.setProgress(0);
			progressDialog.setCancelable(false);
		}

		protected Boolean doInBackground(Void... params) {
			String extraParams = "";
			Log.e("previousUpdate", previousUpdate);
			extraParams = String.format(
					"&oauth_token=%s&oauth_token_secret=%s&oauth_verifier=%s&updated_since=%s", 
					oauthToken, oauthTokenSecret, oauthVerifier, previousUpdate.substring(0,10));

			//			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			//			Log.e("previousUpdate", previousUpdate);
			//			nameValuePairs.add(new BasicNameValuePair("updated_since", previousUpdate));

			String bookmarksUrl = requestApiUrl("bookmarks", API_SECRET + oauthTokenSecret, extraParams);
			Log.e("url", bookmarksUrl);
			InputStream bookmarksSource = getStream(bookmarksUrl);

			if(bookmarksSource == null) {
				Log.e("bookmarksSource", "url request was empty");
				return false;
			}
			Log.e("bookmarksSource", bookmarksSource.toString());
			Gson bookmarkGson = new Gson();
			Reader bookmarkReader = new InputStreamReader(bookmarksSource);
			SearchBookmarks response = bookmarkGson.fromJson(bookmarkReader, SearchBookmarks.class);

			List<Bookmark> bookmarks = response.bookmarks;
			String latestUpdate = "0";
			Log.e("looking at bookmark", "hello");
			int count = 0;
			publishProgress(0, bookmarks.size());
			for(Bookmark bm : bookmarks) {
				Log.e("looking at bookmark", bm.article.title);
				ContentValues values = new ContentValues();
				if(bm.date_updated.compareTo(latestUpdate) > 0) {
					latestUpdate = bm.date_updated.split(" ")[0];
				}
				values.put(DATE_UPDATED, bm.date_updated);
				values.put(READ_PERCENT, bm.read_percent);	
				values.put(FAVORITE, bm.favorite);
				values.put(BOOKMARK_ID, bm.id);
				values.put(DATE_ADDED, bm.date_added);
				values.put(DATE_FAVORITED, bm.date_favorited);
				values.put(ARCHIVE, bm.archive);
				values.put(ARTICLE_HREF, bm.article_href);
				values.put(ARTICLE_ID, bm.article.id);
				values.put(ARTICLE_URL, bm.article.url);
				values.put(ARTICLE_DOMAIN, bm.article.domain);
				values.put(ARTICLE_TITLE, bm.article.title);
				Log.e("ids", String.format("bookmark=%s || article=%s", bm.id, bm.article.id));
				String whereIDSame = BOOKMARK_ID + "=" + bm.id;
				Cursor articleCursor = database.query(
						ARTICLE_TABLE,
						new String[] {MY_ID},
						whereIDSame, null, null, null, null);
				if(articleCursor.getCount() > 0) {
					database.update(ARTICLE_TABLE, values, whereIDSame, null);
					Log.e("updated", bm.article.title);
				}
				else if(!bm.archive) {
					try {
						String html = parseHTML("https://readability.com/mobile/articles/"+bm.article.id);
						values.put(ARTICLE_CONTENT, html);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}


				database.insert(ARTICLE_TABLE, null, values);
				Log.e("inserted", bm.article.title );
				count++;
				publishProgress(count, bookmarks.size());
			}
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString("previous_update", latestUpdate);
			editor.commit();
			return true;
		}

		protected void onProgressUpdate(Integer... values) {
			if(tempDialog.isShowing()) {
				tempDialog.dismiss();
				progressDialog.setMax(values[1]);
				progressDialog.show();
			}
			progressDialog.setProgress(values[0]);
		}

		protected void onPostExecute(Boolean unused) {
			if(tempDialog.isShowing()) {
				tempDialog.dismiss();
			}
			if(progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
		}

	}
}
