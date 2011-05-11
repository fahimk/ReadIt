package com.fahimk.readabilityclient;

import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.*;
import static com.fahimk.readabilityclient.HelperMethods.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fahimk.jsonobjects.Bookmark;
import com.fahimk.jsonobjects.SearchBookmarks;
import com.google.gson.Gson;

public class MainMenu extends Activity {

	private SQLiteDatabase database;
	private String zeroUpdate = "2011-01-01 00:00:00";
	String oauthToken;
	String oauthTokenSecret;

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
		boolean authorized = checkAuthorization();
		Log.e("authorization", "is " + authorized);
		Button authorizeButton = (Button) findViewById(R.id.button_authorize);
		Button deleteButton = (Button) findViewById(R.id.button_delete);
		Button readNowButton = (Button) findViewById(R.id.button_readnow);

		final TextView urlRead = (TextView) findViewById(R.id.edittext_url);

		//the main icon buttons
		final ImageView bookmarksButton = (ImageView) findViewById(R.id.button_bookmarks);
		final ImageView syncButton = (ImageView) findViewById(R.id.button_sync);
		final ImageView addButton = (ImageView) findViewById(R.id.button_add);
		final ImageView exitButton = (ImageView) findViewById(R.id.button_exit);
		final ImageView helpButton = (ImageView) findViewById(R.id.button_help);
		final ImageView settingsButton = (ImageView) findViewById(R.id.button_settings);
		final TextView settingsText = (TextView) findViewById(R.id.text_settings);
		
		if(authorized) {
			settingsButton.setImageResource(R.drawable.icon_settings);
			settingsText.setText("Settings");
			
			removeImageEffects(bookmarksButton);
			removeImageEffects(syncButton);
			removeImageEffects(addButton);
			
			handleTouches(bookmarksButton);
			handleTouches(syncButton);
			handleTouches(addButton);
			
			bookmarksButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					startActivity(new Intent(getBaseContext(), ReadingList.class));
				}
			});

			syncButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					new SyncArticles().execute();
				}
			});

		}
		//not authorized yet
		else {
			settingsButton.setImageResource(R.drawable.icon_auth);
			settingsText.setText("Authorize");
			
			lightenImage(bookmarksButton);
			lightenImage(syncButton);
			lightenImage(addButton);
			
			settingsButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					try {
						authorize();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		
		handleTouches(exitButton);
		handleTouches(helpButton);
		handleTouches(settingsButton);
		
		
		readNowButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(urlRead.getWindowToken(), 0);
				GetArticleTask fetchContent = new GetArticleTask();
				String url = urlRead.getText().toString();
				if(url != "") {
					fetchContent.execute(url);
				}

			}
		});
		
		deleteButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				database.delete(ARTICLE_TABLE, null, null);
				SharedPreferences preferences = getBaseContext().getSharedPreferences(PREF_NAME, 0);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString("previous_update", zeroUpdate);
				editor.commit();
			}
		});
		
		exitButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				database.close();
				MainMenu.this.finish();
			}
		});

	}

	public void launchWebBrowser(String visitUrl) {
		if(visitUrl != "") {
			Intent i = new Intent(getBaseContext(), WebActivity.class);
			i.putExtra("article_url", visitUrl);
			i.putExtra("local", false);
			startActivity(i);
		}
	}
	
	public void removeImageEffects(ImageView v) {
		v.setColorFilter(null);
	}
	
	public void darkenImage(ImageView v) {
		v.setColorFilter(0x55000000, PorterDuff.Mode.SRC_ATOP);
	}
	
	public void lightenImage(ImageView v) {
		v.setColorFilter(0x99FFFFFF, PorterDuff.Mode.SRC_ATOP);
	}
	
	public void handleTouches(ImageView button) {
		button.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent m) {
				if(m.getAction() == MotionEvent.ACTION_DOWN) {
					darkenImage((ImageView) v);
				}
				else if (m.getAction() == MotionEvent.ACTION_UP){
					((ImageView) v).setColorFilter(null);
				}
				return false;
			}
		});

	}

	public boolean checkAuthorization() {
		SharedPreferences tokenInfo = getBaseContext().getSharedPreferences(PREF_NAME, 0);
		String token = tokenInfo.getString("oauth_token", null);
		String tokenSecret = tokenInfo.getString("oauth_token_secret", null);
		String verifier = tokenInfo.getString("oauth_verifier", null);
		return (token != null && tokenSecret != null && verifier != null);
	}

	public void authorize() throws Exception {
		String requestURL = requestApiUrl(OAUTH_REQUEST, API_SECRET, "&oauth_callback=" + URL_CALLBACK);
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet req = new HttpGet(new URI(requestURL));
		HttpResponse resRequest = httpclient.execute(req);
		BufferedReader buffReader = new BufferedReader(new InputStreamReader(resRequest.getEntity().getContent()));
		String tokenString = buffReader.readLine();

		if (tokenString == null)
			return;
		Uri parseTokens = Uri.parse("?"+tokenString);
		oauthToken = parseTokens.getQueryParameter("oauth_token");
		oauthTokenSecret = parseTokens.getQueryParameter("oauth_token_secret");
		SharedPreferences tokenInfo = getBaseContext().getSharedPreferences(PREF_NAME, 0);
		SharedPreferences.Editor editor = tokenInfo.edit();
		editor.putString("oauth_token", oauthToken);
		editor.putString("oauth_token_secret", oauthTokenSecret);
		editor.commit();
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(requestApiUrl(OAUTH_AUTHORIZE, API_SECRET, "&" + tokenString)));
//		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
//				| Intent.FLAG_ACTIVITY_NO_HISTORY
//				| Intent.FLAG_FROM_BACKGROUND);
		startActivity(intent);
	}
	

	@Override
	public void onResume() {
		super.onResume();
		Uri uri = getIntent().getData();
		if (uri != null && uri.toString().startsWith(URL_CALLBACK)) {
			try {
				SharedPreferences tokenInfo = getBaseContext().getSharedPreferences(PREF_NAME, 0);
				oauthToken = tokenInfo.getString("oauth_token", null);
				oauthTokenSecret = tokenInfo.getString("oauth_token_secret", null);
				
				HttpClient httpclient = new DefaultHttpClient();
				String oauthVerifier = uri.getQueryParameter("oauth_verifier");
				Log.e("oauthToken", oauthTokenSecret + " hi");
				String url = requestApiUrl(OAUTH_ACCESS, API_SECRET+oauthTokenSecret, 
						String.format(
								"&oauth_token=%s&oauth_token_secret=%s&oauth_verifier=%s", 
								oauthToken, oauthTokenSecret, oauthVerifier));
				HttpGet httpget = new HttpGet(url);
				HttpResponse response = httpclient.execute(httpget);

				BufferedReader a = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String tokenString = a.readLine();
				
				if (tokenString == null)
					return;
				Uri parseTokens = Uri.parse("?"+tokenString);
				oauthToken = parseTokens.getQueryParameter("oauth_token");
				oauthTokenSecret = parseTokens.getQueryParameter("oauth_token_secret");

				SharedPreferences.Editor editor = tokenInfo.edit();
				editor.putString("oauth_token", oauthToken);
				editor.putString("oauth_token_secret", oauthTokenSecret);
				editor.putString("oauth_verifier", oauthVerifier);
				editor.commit();
				setupViews();
			} catch (Exception e) {
				Log.e("exception, onNewIntent", "hi");
				e.printStackTrace();
			}
		} else {
			Log.e("error", "empty");
		}
	}

	public class GetArticleTask extends AsyncTask<String, Void, String> {
		ProgressDialog progress;
		String articleId;

		@Override
		protected void onPostExecute(String url) {
			if(progress.isShowing()) {
				progress.dismiss();
			}
			if(url == "") {
				displayAlert(MainMenu.this, "Error", "Could not parse the url, please check and try again.");
			}
		
			launchWebBrowser(url);
		}

		protected void onPreExecute() {
			progress = new ProgressDialog(MainMenu.this);
			progress.setMessage("Parsing using readability.com...");
			progress.show();
		}

		@Override
		protected String doInBackground(String... urls) {
			String url = urls[0];

			try {
				DefaultHttpClient mHttpClient = new DefaultHttpClient();
				BasicHttpContext mHttpContext = new BasicHttpContext();
				CookieStore mCookieStore      = new BasicCookieStore();        
				mHttpContext.setAttribute(ClientContext.COOKIE_STORE, mCookieStore);

				HttpGet httpget = new HttpGet("https://www.readability.com/shorten");

				HttpResponse response = mHttpClient.execute(httpget, mHttpContext);

				HttpPost httpost = new HttpPost("https://www.readability.com/~/");

				List <NameValuePair> nvps = new ArrayList <NameValuePair>();
				nvps.add(new BasicNameValuePair("url", url));

				httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
				httpost.setHeader("Keep-Alive", "115");
				httpost.setHeader("Connection", "keep-alive");
				httpost.setHeader("Referer", "https://www.readability.com/shorten");
				httpost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0.1) Gecko/20100101 Firefox/4.0.1");
				httpost.setHeader("X-Requested-With", "XMLHttpRequest");
				List<Cookie> cl = mCookieStore.getCookies();
				StringBuffer com = new StringBuffer();
				for(Cookie c: cl) {
					com.append(c.getName());
					com.append("=");
					com.append(c.getValue());
					com.append(";");
				}
				httpost.setHeader("Cookie", com.toString());
				response = mHttpClient.execute(httpost, mHttpContext);

				InputStream i = response.getEntity().getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(i));
				String line = reader.readLine();
				String articlesObject = "/articles/";
				int begin = line.indexOf("/articles/");
				int end = line.indexOf("\"", begin);
				return line.substring(begin+articlesObject.length(), end);
				
			}
			catch(Exception e) {
				return "";
			}
		}

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
				else {
					values.put(ARTICLE_CONTENT, "");
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
