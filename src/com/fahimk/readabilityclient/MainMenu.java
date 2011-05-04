package com.fahimk.readabilityclient;

import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.ARCHIVE;
import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.ARTICLE_AUTHOR;
import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.ARTICLE_CONTENT;
import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.ARTICLE_CONTENT_SIZE;
import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.ARTICLE_DOMAIN;
import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.ARTICLE_HREF;
import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.ARTICLE_ID;
import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.ARTICLE_SHORT_URL;
import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.ARTICLE_TABLE;
import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.ARTICLE_TITLE;
import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.ARTICLE_URL;
import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.BOOKMARK_ID;
import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.DATE_ADDED;
import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.DATE_FAVORITED;
import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.DATE_UPDATED;
import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.FAVORITE;
import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.MY_ID;
import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.READ_PERCENT;
import static com.fahimk.readabilityclient.HelperMethods.API_SECRET;
import static com.fahimk.readabilityclient.HelperMethods.PREF_NAME;
import static com.fahimk.readabilityclient.HelperMethods.getStream;
import static com.fahimk.readabilityclient.HelperMethods.requestApiUrl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

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
				editor.putString("previous_update", "0");
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


	private class SyncArticles extends AsyncTask<Void, Void, Void> {
		private ProgressDialog Dialog = new ProgressDialog(MainMenu.this);
		String oauthToken;
		String oauthTokenSecret;
		String oauthVerifier;
		String previousUpdate;
		SharedPreferences preferences;
		protected void onPreExecute() {
			preferences = getBaseContext().getSharedPreferences(PREF_NAME, 0);
			oauthToken = preferences.getString("oauth_token", null); 
			oauthTokenSecret = preferences.getString("oauth_token_secret", null);
			oauthVerifier = preferences.getString("oauth_verifier", null);
			previousUpdate = preferences.getString("previous_update", "0");
			
			Dialog.setMessage("Downloading source..");
			Dialog.show();
		}

		protected Void doInBackground(Void... params) {
			String extraParams = "";
				extraParams = String.format(
						"&oauth_token=%s&oauth_token_secret=%s&oauth_verifier=%s", 
						oauthToken, oauthTokenSecret, oauthVerifier);
			
			String bookmarksUrl = requestApiUrl("bookmarks", API_SECRET + oauthTokenSecret, extraParams);		
			InputStream bookmarksSource = getStream(bookmarksUrl);


			Gson bookmarkGson = new Gson();
			Reader bookmarkReader = new InputStreamReader(bookmarksSource);
			SearchBookmarks response = bookmarkGson.fromJson(bookmarkReader, SearchBookmarks.class);

			List<Bookmark> bookmarks = response.bookmarks;
			String latestUpdate = "0";
			for(Bookmark bm : bookmarks) {
				if(bm.date_updated.compareTo(previousUpdate) < 0) {
					continue;
				}
				Log.e("looking at bookmark", bm.article.title);
				ContentValues values = new ContentValues();
				if(bm.date_updated.compareTo(latestUpdate) > 0) {
					latestUpdate = bm.date_updated;
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
					if(bm.date_updated.compareTo(previousUpdate) > 0) {
						database.update(ARTICLE_TABLE, values, whereIDSame, null);
						Log.e("updated", bm.article.title);
					}
				}
				else {
					String params2 = String.format("&oauth_nonce=%s&oauth_timestamp=%s" + 
							"&oauth_consumer_key=%s&oauth_signature=%s&oauth_signature_method=PLAINTEXT" +
							"&xoauth_lang_pref=en-us", 
							HelperMethods.getNonce(),HelperMethods.getTimestamp(),HelperMethods.API_KEY,HelperMethods.API_SECRET + oauthTokenSecret);
					String articlesUrl = "https://readability.com" + bm.article_href + "/?" + extraParams + params2;
					InputStream articlesSource = getStream(articlesUrl);
					Gson articleGson = new Gson();
					Reader articleReader = new InputStreamReader(articlesSource);
					SearchArticle articleResponse = articleGson.fromJson(articleReader, SearchArticle.class);
					values.put(ARTICLE_AUTHOR, articleResponse.author);
					try {
						String c = getContent("https://readability.com/mobile/articles/"+articleResponse.id);
						//c = c.replaceAll("\"/", "\"file:///android_asset/");
						//c = c.replaceAll("\"/", "\"https://readability.com/");
						c = c.replaceAll("/media/css/mobile.css", "file:///android_asset/mobile.css");
						c = c.replaceAll("/media/js/jquery.min.js", "file:///android_asset/jquery.min.js");
						//c = c.replaceAll("<a href=\"#\" class=\"article-back-link\">", "<a href=\"##\" class=\"article-back-link\">");
						//Log.e("html", c);
						values.put(ARTICLE_CONTENT, c);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//values.put(ARTICLE_CONTENT, articleResponse.content);
					values.put(ARTICLE_CONTENT_SIZE, articleResponse.content_size); 
					values.put(ARTICLE_SHORT_URL, articleResponse.short_url);

					database.insert(ARTICLE_TABLE, null, values);
					Log.e("inserted", bm.article.title );
				}
				articleCursor.close();
			}
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString("previous_update", latestUpdate);
			editor.commit();
			return null;
		}

		protected String getContent(String s) throws Exception {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(s);
			HttpResponse response = client.execute(request);

			String html = "";
			InputStream in = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder str = new StringBuilder();
			String line = null;
			while((line = reader.readLine()) != null)
			{
				str.append(line);
			}
			in.close();
			html = str.toString();
			return html;
		}

		protected void onPostExecute(Void unused) {
			Dialog.dismiss();
		}

	}
}
