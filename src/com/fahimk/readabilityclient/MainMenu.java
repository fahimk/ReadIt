package com.fahimk.readabilityclient;

import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.ARCHIVE;
import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.ARTICLE_CONTENT;
import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.ARTICLE_DOMAIN;
import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.ARTICLE_HREF;
import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.ARTICLE_ID;
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
import static com.fahimk.readabilityclient.HelperMethods.MSG_BAD_URL;
import static com.fahimk.readabilityclient.HelperMethods.MSG_END;
import static com.fahimk.readabilityclient.HelperMethods.MSG_FAIL;
import static com.fahimk.readabilityclient.HelperMethods.MSG_START_SETUPVIEWS;
import static com.fahimk.readabilityclient.HelperMethods.MSG_START_SYNCARTICLES;
import static com.fahimk.readabilityclient.HelperMethods.MSG_START_WEBVIEWINTENT;
import static com.fahimk.readabilityclient.HelperMethods.OAUTH_ACCESS;
import static com.fahimk.readabilityclient.HelperMethods.OAUTH_AUTHORIZE;
import static com.fahimk.readabilityclient.HelperMethods.OAUTH_REQUEST;
import static com.fahimk.readabilityclient.HelperMethods.PREF_NAME;
import static com.fahimk.readabilityclient.HelperMethods.URL_CALLBACK;
import static com.fahimk.readabilityclient.HelperMethods.checkAuthorization;
import static com.fahimk.readabilityclient.HelperMethods.displayAlert;
import static com.fahimk.readabilityclient.HelperMethods.displayInfo;
import static com.fahimk.readabilityclient.HelperMethods.getStream;
import static com.fahimk.readabilityclient.HelperMethods.handleTouches;
import static com.fahimk.readabilityclient.HelperMethods.lightenImage;
import static com.fahimk.readabilityclient.HelperMethods.parseHTML;
import static com.fahimk.readabilityclient.HelperMethods.requestApiUrl;
import static com.fahimk.readabilityclient.HelperMethods.setupDB;
import static com.fahimk.readabilityclient.HelperMethods.zeroUpdate;

import java.io.BufferedReader;
import java.io.IOException;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.fahimk.jsonobjects.Bookmark;
import com.fahimk.jsonobjects.SearchBookmarks;
import com.google.gson.Gson;

public class MainMenu extends Activity {

	private SQLiteDatabase database;
	boolean skip = false;
	boolean debug = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu);
		String webPageUrl = getIntent().getStringExtra(Intent.EXTRA_TEXT);
		if(webPageUrl != null) {
			webPageUrl = webPageUrl.split("\n")[0];
			GetArticleTask fetchContent = new GetArticleTask();
			fetchContent.execute(webPageUrl);
		}
		database = setupDB(this);
		setupViews();
		SharedPreferences sharedPreferences  = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		boolean syncNow = sharedPreferences.getBoolean("syncStartup", false);
		skip = sharedPreferences.getBoolean("skipMenu", false);
		if(syncNow) {
			SyncArticles task = new SyncArticles();
			task.execute(skip);
		}
		else if(skip) {
			startActivity(new Intent(getBaseContext(), ReadingList.class));
		}
	}

	public void onDestroy() {
		super.onDestroy();
		database.close();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu_clear, menu);
	    return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_delete:
	    	database.delete(ARTICLE_TABLE, null, null);
			SharedPreferences preferences = getBaseContext().getSharedPreferences(PREF_NAME, 0);
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString("previous_update", zeroUpdate);
			editor.commit();
	        return true;
	    }
	    return false;
	}
	
	public void setupViews() {
		boolean authorized = checkAuthorization(this);
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

		if(!debug) {
			deleteButton.setVisibility(View.GONE);
			authorizeButton.setVisibility(View.GONE);
		}
		if(authorized) {
			settingsButton.setImageResource(R.drawable.icon_settings);
			settingsText.setText("Settings");

			removeImageEffects(bookmarksButton);
			removeImageEffects(syncButton);
			removeImageEffects(addButton);

			handleTouches(bookmarksButton);
			handleTouches(syncButton);
			handleTouches(addButton);

			addButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					addBookmark();
				}
			});

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

			settingsButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					startActivity(new Intent(getBaseContext(), Preferences.class));
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
					authorize();
				}
			});
		}

		handleTouches(exitButton);
		handleTouches(helpButton);
		handleTouches(settingsButton);

		helpButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				displayInfo(MainMenu.this, "Help", "This is a client for readability.com. If you do not have an account, you can use the url bar at the top to format web articles or if you want to save articles you need a readability.com account which you can create by clicking on the authorize button.");
			}
		});

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

		EditText userInput = (EditText) findViewById(R.id.edittext_url);
		userInput.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_GO) {
					GetArticleTask fetchContent = new GetArticleTask();
					String url = urlRead.getText().toString();
					if(url != "") {
						fetchContent.execute(url);
					}
				}
				return true;
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


	protected void addBookmark() {
		ProgressDialog pDialog = HelperMethods.createProgressDialog(MainMenu.this, "Loading", "connecting to readability server...");
		pDialog.show();
		final Handler myHandler = new MessageHandler(pDialog);
		final Message msg = new Message();
		new Thread() {
			public void run() {
				try {
					Looper.prepare();
					final TextView urlRead = (TextView) findViewById(R.id.edittext_url);
					String url = urlRead.getText().toString();
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(urlRead.getWindowToken(), 0);
					if(url == "" || url.length() < 3) {
						msg.what = MSG_END;
						msg.arg1 = MSG_BAD_URL;
						myHandler.sendMessage(msg);
					}
					else {
						SharedPreferences preferences = getBaseContext().getSharedPreferences(PREF_NAME, 0);
						String oauthToken = preferences.getString("oauth_token", null); 
						String oauthTokenSecret = preferences.getString("oauth_token_secret", null);
						String oauthVerifier = preferences.getString("oauth_verifier", null);
						String extraParams = String.format(
								"&oauth_token=%s&oauth_token_secret=%s&oauth_verifier=%s", 
								oauthToken, oauthTokenSecret, oauthVerifier);
						String bookmarksUrl = requestApiUrl("bookmarks", API_SECRET + oauthTokenSecret, extraParams);
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
						nameValuePairs.add(new BasicNameValuePair("url", url));

						String message = HelperMethods.postData(bookmarksUrl, nameValuePairs);
						msg.obj = message;
						msg.arg1 = MSG_START_SYNCARTICLES;
						msg.what = MSG_END;
						myHandler.sendMessage(msg);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					msg.what = MSG_FAIL;
					myHandler.sendMessage(msg);
				}

			}
		}.start();
	}

	public void launchWebBrowser(String visitUrl) {
		if(visitUrl != "") {
			Intent i = new Intent(getBaseContext(), WebActivity.class);
			i.putExtra("article_url", visitUrl);
			i.putExtra("saved", false);
			startActivity(i);
		}
	}

	public void launchWebBrowser(String visitUrl, String fullUrl) {
		if(visitUrl != "") {
			Intent i = new Intent(getBaseContext(), WebActivity.class);
			i.putExtra("article_url", visitUrl);
			i.putExtra("full_url", fullUrl);
			i.putExtra("saved", false);
			startActivity(i);
		}
	}


	public void removeImageEffects(ImageView v) {
		v.setColorFilter(null);
	}

	public void authorize() {
		ProgressDialog pDialog = HelperMethods.createProgressDialog(MainMenu.this, "Loading", "retrieving authorization url...");
		pDialog.show();
		final Handler myHandler = new MessageHandler(pDialog);
		final Message msg = new Message();
		new Thread() {
			public void run() {
				try {
					Looper.prepare();
					String requestURL = requestApiUrl(OAUTH_REQUEST, API_SECRET, "&oauth_callback=" + URL_CALLBACK);
					HttpClient httpclient = new DefaultHttpClient();
					HttpGet req = new HttpGet(new URI(requestURL));
					HttpResponse resRequest = httpclient.execute(req);
					BufferedReader buffReader = new BufferedReader(new InputStreamReader(resRequest.getEntity().getContent()));
					String tokenString = buffReader.readLine();

					if (tokenString == null)
						return;
					Uri parseTokens = Uri.parse("?"+tokenString);
					final String oauthToken = parseTokens.getQueryParameter("oauth_token");
					final String oauthTokenSecret = parseTokens.getQueryParameter("oauth_token_secret");

					SharedPreferences tokenInfo = getBaseContext().getSharedPreferences(PREF_NAME, 0);
					SharedPreferences.Editor editor = tokenInfo.edit();
					editor.putString("oauth_token", oauthToken);
					editor.putString("oauth_token_secret", oauthTokenSecret);
					editor.commit();

					msg.obj = tokenString;
					msg.arg1 = MSG_START_WEBVIEWINTENT;
					msg.what = MSG_END;
					myHandler.sendMessage(msg);

				}
				catch(Exception e) {
					e.printStackTrace();
					msg.what = MSG_FAIL;
					myHandler.sendMessage(msg);

				}
			}

		}.start();
	}


	@Override
	public void onResume() {
		super.onResume();
		setupViews();
		final Uri uri = getIntent().getData();
		if (uri != null && uri.toString().startsWith(URL_CALLBACK)) {
			if(checkAuthorization(this)) {
				return;
			}
			ProgressDialog pDialog = HelperMethods.createProgressDialog(MainMenu.this, "Loading", "confirming authorization...");
			pDialog.show();
			final Handler myHandler = new MessageHandler(pDialog);
			final Message msg = new Message();
			new Thread() {
				String oauthToken = null;
				String oauthTokenSecret = null;

				public void run() {
					try {
						Looper.prepare();
						SharedPreferences tokenInfo = getBaseContext().getSharedPreferences(PREF_NAME, 0);
						oauthToken = tokenInfo.getString("oauth_token", null);
						oauthTokenSecret = tokenInfo.getString("oauth_token_secret", null);


						Log.e("oathtoken" , oauthToken + " hi");
						Log.e("oathtoken" , oauthTokenSecret + " hi");
						if (oauthToken == null || oauthTokenSecret == null)
							throw new NullPointerException();
						HttpClient httpclient = new DefaultHttpClient();
						String oauthVerifier = uri.getQueryParameter("oauth_verifier");
						Log.e("uri", uri.getQuery());
						String url = requestApiUrl(OAUTH_ACCESS, API_SECRET+oauthTokenSecret, 
								String.format(
										"&oauth_token=%s&oauth_token_secret=%s&oauth_verifier=%s", 
										oauthToken, oauthTokenSecret, oauthVerifier));
						Log.e("url", url);
						HttpGet httpget = new HttpGet(url);
						HttpResponse response = httpclient.execute(httpget);

						BufferedReader a = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
						String tokenString = a.readLine();

						Log.e("tokenString", tokenString + " hello");
						if (tokenString == null)
							throw new NullPointerException();
						Uri parseTokens = Uri.parse("?"+tokenString);
						oauthToken = parseTokens.getQueryParameter("oauth_token");
						oauthTokenSecret = parseTokens.getQueryParameter("oauth_token_secret");
						Log.e("here", "here3");

						SharedPreferences.Editor editor = tokenInfo.edit();
						editor.putString("oauth_token", oauthToken);
						editor.putString("oauth_token_secret", oauthTokenSecret);
						editor.putString("oauth_verifier", oauthVerifier);
						editor.commit();


						msg.what = MSG_END;
						msg.arg1 = MSG_START_SETUPVIEWS;
						Log.e("here", "here");
						myHandler.sendMessage(msg);
					} catch (Exception e) {
						Log.e("error", "here");
						e.printStackTrace();
						msg.what = MSG_FAIL;
						myHandler.sendMessage(msg);

					}
				}
			}.start();

		}
	}

	public class MessageHandler extends Handler {

		ProgressDialog pDialog;

		public MessageHandler(ProgressDialog pd) {
			super();
			pDialog = pd;
		}
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MSG_END:
				if(pDialog != null && pDialog.isShowing())
					pDialog.dismiss();
				switch(msg.arg1) {
				case MSG_START_SETUPVIEWS:
					setupViews();
					break;
				case MSG_START_WEBVIEWINTENT:
					String tokenString = (String) msg.obj;
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(requestApiUrl(OAUTH_AUTHORIZE, API_SECRET, "&" + tokenString)));
					startActivity(intent);
					break;
				case MSG_START_SYNCARTICLES:
					new SyncArticles().execute();
					Toast bookmarkConfirmMessage = Toast.makeText(MainMenu.this, "Bookmark added.", Toast.LENGTH_LONG);
					bookmarkConfirmMessage.show();
					break;
				case MSG_BAD_URL:
					Toast badUrlMessage = Toast.makeText(MainMenu.this, "Please enter the article url in the textbox above.", Toast.LENGTH_LONG);
					badUrlMessage.show();
					break;
				}
				break;
			case MSG_FAIL:
				if(pDialog.isShowing())
					pDialog.dismiss();
				displayAlert(pDialog.getContext(), "Error", "Could not connect to readability.com, please check your internet connection status and try again.");
				break;
			}
		}

	}
	public class GetArticleTask extends AsyncTask<String, Void, String> {
		ProgressDialog progress;
		String articleId;
		final String connectionError = "conn";
		final String urlError = "url";
		String fullUrl = "";
		@Override
		protected void onPostExecute(String url) {
			if(progress.isShowing()) {
				progress.dismiss();
			}
			if(url.equals(connectionError)) {
				displayAlert(MainMenu.this, "Error", "Could not connect to readability, please check connection and try again.");
				return;
			}
			else if(url.equals(urlError)) {
				displayAlert(MainMenu.this, "Error", "Could not parse the url, please check the url and try again.");
				return;
			}

			launchWebBrowser(url, fullUrl);
		}

		protected void onPreExecute() {
			progress = new ProgressDialog(MainMenu.this);
			progress.setMessage("Parsing using readability.com...");
			progress.show();
		}

		@Override
		protected String doInBackground(String... urls) {
			String url = urls[0];
			fullUrl = url;
			try {
				DefaultHttpClient mHttpClient = new DefaultHttpClient();
				BasicHttpContext mHttpContext = new BasicHttpContext();
				CookieStore mCookieStore      = new BasicCookieStore();        
				mHttpContext.setAttribute(ClientContext.COOKIE_STORE, mCookieStore);

				HttpGet httpget = new HttpGet("http://www.readability.com/shorten");

				HttpResponse response;
				response = mHttpClient.execute(httpget, mHttpContext);

				HttpPost httpost = new HttpPost("http://www.readability.com/~/");

				List <NameValuePair> nvps = new ArrayList <NameValuePair>();
				nvps.add(new BasicNameValuePair("url", url));

				httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
				httpost.setHeader("Keep-Alive", "115");
				httpost.setHeader("Connection", "keep-alive");
				httpost.setHeader("Referer", "http://www.readability.com/shorten");
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
			catch (IOException e) {
				e.printStackTrace();
				return connectionError;
			}
			catch (Exception e) {
				e.printStackTrace();
				return urlError;
			}
		}

	}

	public class SyncArticles extends AsyncTask<Boolean, Integer, Boolean> {
		ProgressDialog progressDialog;
		ProgressDialog tempDialog;
		String oauthToken;
		String oauthTokenSecret;
		String oauthVerifier;
		SharedPreferences preferences;
		String previousUpdate;
		protected void onPreExecute() {
			preferences = getSharedPreferences(PREF_NAME, 0);
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

		protected Boolean doInBackground(Boolean... params) {
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
			//Log.e("bookmarksSource", bookmarksSource.toString() + "abc");
			Gson bookmarkGson = new Gson();
			Reader bookmarkReader = new InputStreamReader(bookmarksSource);
			SearchBookmarks response = bookmarkGson.fromJson(bookmarkReader, SearchBookmarks.class);

			List<Bookmark> bookmarks = response.bookmarks;
			String latestUpdate = zeroUpdate;
			//Log.e("looking at bookmark", "hello");
			int count = 0;
			publishProgress(0, bookmarks.size());
			for(Bookmark bm : bookmarks) {
				//Log.e("looking at bookmark", bm.article.title);
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
				//Log.e("ids", String.format("bookmark=%s || article=%s", bm.id, bm.article.id));
				String whereIDSame = BOOKMARK_ID + "=" + bm.id;
				Cursor articleCursor = database.query(
						ARTICLE_TABLE,
						new String[] {MY_ID},
						whereIDSame, null, null, null, null);
				Log.e("count", articleCursor.getCount() + " " + bm.id);
				if(articleCursor.getCount() > 0) {
					database.update(ARTICLE_TABLE, values, whereIDSame, null);
					Log.e("updated", bm.article.title);
				}
				else if(!bm.archive) {
					try {
						String html = parseHTML("http://readability.com/mobile/articles/"+bm.article.id);
						values.put(ARTICLE_CONTENT, html);
						database.insert(ARTICLE_TABLE, null, values);
						Log.e("inserted", bm.article.title );
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else {
					values.put(ARTICLE_CONTENT, "");
					database.insert(ARTICLE_TABLE, null, values);
					Log.e("inserted", bm.article.title );
				}

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
			if(unused == false) {
				displayAlert(MainMenu.this, "Error", "Could not connect to readability.com, please check your internet connection status and try again.");
			}
			if(skip) {
				startActivity(new Intent(MainMenu.this, ReadingList.class));
			}
		}

	}

}
