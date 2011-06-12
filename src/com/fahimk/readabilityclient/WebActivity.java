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
import static com.fahimk.readabilityclient.HelperMethods.MSG_START_SYNCARTICLES;
import static com.fahimk.readabilityclient.HelperMethods.MSG_WV_ADDARC;
import static com.fahimk.readabilityclient.HelperMethods.MSG_WV_ADDFAV;
import static com.fahimk.readabilityclient.HelperMethods.MSG_WV_INIT;
import static com.fahimk.readabilityclient.HelperMethods.PREF_NAME;
import static com.fahimk.readabilityclient.HelperMethods.checkAuthorization;
import static com.fahimk.readabilityclient.HelperMethods.displayAlert;
import static com.fahimk.readabilityclient.HelperMethods.getStream;
import static com.fahimk.readabilityclient.HelperMethods.parseHTML;
import static com.fahimk.readabilityclient.HelperMethods.requestApiUrl;
import static com.fahimk.readabilityclient.HelperMethods.setupDB;
import static com.fahimk.readabilityclient.HelperMethods.zeroUpdate;
import static com.fahimk.readabilityclient.JavascriptModifyFunctions.addButtonListeners;
import static com.fahimk.readabilityclient.JavascriptModifyFunctions.setupDefaultTheme;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.fahimk.jsonobjects.Bookmark;
import com.fahimk.jsonobjects.SearchBookmarks;
import com.google.gson.Gson;

public class WebActivity extends Activity {
	private WebView webView;
	int key=0;
	boolean articleSaved = false;
	boolean authorized = false;
	String favorite = "0";
	String archive = "0";
	String bookmarkID = "";
	String fullUrl = "";
	String url = "";
	String title = "";
	private SQLiteDatabase database;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		database = setupDB(this);

		SharedPreferences sharedPreferences  = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		boolean fullScreen = sharedPreferences.getBoolean("fullScreen", false);
		if(fullScreen) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			getWindow().requestFeature(Window.FEATURE_PROGRESS);
		}
		setContentView(R.layout.web);
		if(!fullScreen) {
			getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
		}
		setupCustomPanel();
		webView = (WebView)this.findViewById(R.id.webView);

		final Activity MyActivity = this;
		webView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress)   
			{
				//Make the bar disappear after URL is loaded, and changes string to Loading...
				MyActivity.setTitle("Loading...");
				MyActivity.setProgress(progress * 100); //Make the bar disappear after URL is loaded

				// Return the app name after finish loading
				if(progress == 100)
					MyActivity.setTitle("ReadIt");
			}
		});

		Bundle data = getIntent().getExtras();
		String content = "";
		authorized = checkAuthorization(this);
		if(data != null) {
			content = data.getString("article_content");
			url = data.getString("article_url");
			articleSaved = data.getBoolean("saved");
			bookmarkID = data.getString("bookmark_id");
			favorite = data.getString("favorite");
			archive = data.getString("archive");
			fullUrl = data.getString("full_url");
			title = data.getString("article_title");
		}
		Log.e("url", url + "hi");
		if(url != null && (content == null || content.length() < 3)) {
			try {
				String s = "https://www.readability.com/mobile/articles/"+url;
				getHTMLThread(s);
			} catch (Exception e) {
				Log.e("exception loading url", e.getMessage());
			}
		}
		else {
			Log.e("content", content);
			initializeWV(content);
		}
		//Log.e("content", content);

	}

	public void initializeWV(String content) {
		webView.getSettings().setCacheMode(WebSettings.LOAD_NORMAL);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		webView.setWebViewClient(new CustomWebView());
		webView.loadDataWithBaseURL("", content, "text/html", "UTF-8", "");
	}



	private void getHTMLThread(final String url) throws Exception {
		ProgressDialog pDialog = HelperMethods.createProgressDialog(WebActivity.this, "Loading", "retrieving content...");
		pDialog.show();
		final Handler myHandler = new WebHandler(pDialog);
		final Message msg = new Message();
		new Thread() {
			public void run() {
				try {
					Looper.prepare();
					String html = parseHTML(url);
					msg.obj = html;
					msg.what = MSG_END;
					msg.arg1 = MSG_WV_INIT;
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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		if(articleSaved) {
			inflater.inflate(R.menu.menu_authorized, menu);
		}
		else {
			inflater.inflate(R.menu.menu_guest, menu);
		}
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if(articleSaved) {
			MenuItem fav = (MenuItem) menu.getItem(0);
			MenuItem arc = (MenuItem) menu.getItem(1);
			if(favorite.equals("0")){
				fav.setIcon(android.R.drawable.btn_star_big_off);
			}
			else {
				fav.setIcon(android.R.drawable.btn_star_big_on);
			}
			if(archive.equals("0")) {
				arc.setIcon(android.R.drawable.checkbox_off_background);
			}
			else {
				arc.setIcon(android.R.drawable.checkbox_on_background);
			}
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_archive:
			toggleArchive();
			return true;
		case R.id.menu_favorite:
			toggleFavorite();
			return true;
		case R.id.menu_styles:
			showPanel();
			return true;
		case R.id.menu_readlater:
			if(checkAuthorization(this)) {
				addBookmark(fullUrl);
			}
			else {
				displayAlert(this, "Save bookmark", "To save bookmarks for later reading, you need an account on readability.com. To open an account, go back to the main menu, and click the authorize button.");
			}
			return true;
		case R.id.menu_share:
			showShareDialog();
			return true;
		default:
			return false;
		}
	}


	private void showShareDialog() {
		Dialog shareDialog = new Dialog(this);
		shareDialog.setContentView(R.layout.dialog_share);
		shareDialog.setTitle("Share this article.");
		shareDialog.setCanceledOnTouchOutside(true);
		shareDialog.show();

		ImageButton twitterButton = (ImageButton) shareDialog.findViewById(R.id.button_share_twitter);
		ImageButton faceButton = (ImageButton) shareDialog.findViewById(R.id.button_share_facebook);
		ImageButton emailButton = (ImageButton) shareDialog.findViewById(R.id.button_share_email);

		try {
			title = URLEncoder.encode(title,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} 
		twitterButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://twitter.com/intent/tweet?text=" + title + "&url=http%3A%2F%2Frdd.me%2F" + url + "&via=readability"));
				startActivity(browserIntent);
			}
		});

		faceButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://www.facebook.com/sharer/sharer.php?u=http%3A%2F%2Fwww.readability.com%2Farticles%2F" + url));
				startActivity(browserIntent);
			}
		});

		emailButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://www.readability.com/articles/" + url + "/email"));
				startActivity(browserIntent);
			}
		});
	}

	private void toggleFavorite() {
		favorite = (favorite.equals("0")) ? "1" : "0";
		WebSyncArticles task = new WebSyncArticles();
		task.execute();
		ProgressDialog pDialog = HelperMethods.createProgressDialog(WebActivity.this, "Loading", "adding to favorites...");
		final Handler myHandler = new WebHandler(pDialog);
		final Message msg = new Message();
		new Thread() {
			public void run() {
				try {
					Looper.prepare();
					SharedPreferences preferences = getBaseContext().getSharedPreferences(PREF_NAME, 0);
					String oauthToken = preferences.getString("oauth_token", null); 
					String oauthTokenSecret = preferences.getString("oauth_token_secret", null);
					String oauthVerifier = preferences.getString("oauth_verifier", null);
					String extraParams = String.format(
							"&oauth_token=%s&oauth_token_secret=%s&oauth_verifier=%s", 
							oauthToken, oauthTokenSecret, oauthVerifier);
					String bookmarksUrl = requestApiUrl("bookmarks/" + bookmarkID +"/", API_SECRET + oauthTokenSecret, extraParams);
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add(new BasicNameValuePair("favorite", favorite));
					nameValuePairs.add(new BasicNameValuePair("archive", archive));
					Log.e("favorite", bookmarksUrl + "hello");
					String message = HelperMethods.postData(bookmarksUrl, nameValuePairs);
					msg.obj = message;
					msg.what = MSG_END;
					msg.arg1 = MSG_WV_ADDFAV;
					msg.arg2 = Integer.parseInt(favorite);
					myHandler.sendMessage(msg);
				}
				catch (Exception e) {
					e.printStackTrace();
					msg.what = MSG_FAIL;
					myHandler.sendMessage(msg);
				}

			}
		}.start();

	}

	private void toggleArchive() {
		archive = (archive.equals("0")) ? "1" : "0";
		WebSyncArticles task = new WebSyncArticles();
		task.execute();
		ProgressDialog pDialog = HelperMethods.createProgressDialog(WebActivity.this, "Loading", "adding to favorites...");
		final Handler myHandler = new WebHandler(pDialog);
		final Message msg = new Message();
		new Thread() {
			public void run() {
				try {
					Looper.prepare();
					SharedPreferences preferences = getBaseContext().getSharedPreferences(PREF_NAME, 0);
					String oauthToken = preferences.getString("oauth_token", null); 
					String oauthTokenSecret = preferences.getString("oauth_token_secret", null);
					String oauthVerifier = preferences.getString("oauth_verifier", null);
					String extraParams = String.format(
							"&oauth_token=%s&oauth_token_secret=%s&oauth_verifier=%s", 
							oauthToken, oauthTokenSecret, oauthVerifier);
					String bookmarksUrl = requestApiUrl("bookmarks/" + bookmarkID +"/", API_SECRET + oauthTokenSecret, extraParams);
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add(new BasicNameValuePair("archive", archive));
					nameValuePairs.add(new BasicNameValuePair("favorite", favorite));
					String message = HelperMethods.postData(bookmarksUrl, nameValuePairs);
					msg.obj = message;
					msg.what = MSG_END;
					msg.arg1 = MSG_WV_ADDARC;
					msg.arg2 = Integer.parseInt(archive);
					myHandler.sendMessage(msg);
				}
				catch (Exception e) {
					e.printStackTrace();
					msg.what = MSG_FAIL;
					myHandler.sendMessage(msg);
				}

			}
		}.start();
	}

	private void setupCustomPanel() {
		final EditPanel popup = (EditPanel) findViewById(R.id.popup_window);
		popup.setVisibility(View.GONE);

		ImageView hidePanel = (ImageView) findViewById(R.id.button_hidepanel);
		hidePanel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				hidePanel();
			}
		});
	}

	public void hidePanel() {
		final EditPanel popup = (EditPanel) findViewById(R.id.popup_window);
		popup.setVisibility(View.GONE);
		webView.setClickable(true);
		popup.setClickable(false);
	}

	public void showPanel() {
		final EditPanel popup = (EditPanel) findViewById(R.id.popup_window);
		popup.setVisibility(View.VISIBLE);
		webView.setClickable(false);
		popup.setClickable(true);
	}

	protected void addBookmark(final String url) {
		ProgressDialog pDialog = HelperMethods.createProgressDialog(WebActivity.this, "Loading", "connecting to readability server...");
		pDialog.show();
		final Handler myHandler = new WebHandler(pDialog);
		final Message msg = new Message();
		new Thread() {
			public void run() {
				try {
					Looper.prepare();
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

	private class CustomWebView extends WebViewClient {

		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.e("url", url);
			if (!url.startsWith("https://www.readability.com")) {  

				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);

				return true;  
			} else {
				view.loadUrl(url);
				return true;
			}
		}


		@Override
		public void onPageFinished(WebView view, String url){
			Toast message2 = Toast.makeText(WebActivity.this, "Press the menu button to customize text and modify the bookmark.", Toast.LENGTH_LONG);
			message2.show();
			view.loadUrl("javascript:(function() { " +  
					"var readBar = document.getElementById('read-bar'); readBar.parentNode.removeChild(readBar);"+
					"var footNote = document.getElementById('article-marketing'); footNote.parentNode.removeChild(footNote);"+
					"var bottom = document.getElementById('article-nav'); bottom.parentNode.removeChild(bottom);" +
			"})()");  

			//need better algorithm for this based on number of words
			//view.scrollTo(0, (int) (scrollPosition * view.getContentHeight() + view.getHeight()));
			addButtonListeners(findViewById(R.id.mainFrame), webView);
			setupDefaultTheme(webView);
		} 

	}

	public class WebHandler extends Handler {

		ProgressDialog pDialog;

		public WebHandler(ProgressDialog pd) {
			super();
			pDialog = pd;
		}
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MSG_END:
				if(pDialog.isShowing())
					pDialog.dismiss();
				switch(msg.arg1) {
				case MSG_WV_ADDFAV:
					String action = (msg.arg2 == 0) ? "Removed from" : "Added to";
					Toast message = Toast.makeText(WebActivity.this, action + " favorites.", Toast.LENGTH_LONG);
					message.show();
					break;
				case MSG_WV_ADDARC:
					String action3 = (msg.arg2 == 0) ? "Removed from" : "Added to"; 
					Toast message3 = Toast.makeText(WebActivity.this, action3 + " archive.", Toast.LENGTH_LONG);
					message3.show();
					break;
				case MSG_WV_INIT:
					initializeWV((String) msg.obj);
					break;
				case MSG_START_SYNCARTICLES:
					new WebSyncArticles().execute();
					Toast bookmarkConfirmMessage = Toast.makeText(WebActivity.this, "Bookmark added.", Toast.LENGTH_LONG);
					bookmarkConfirmMessage.show();
					break;
				case MSG_BAD_URL:
					Toast bookmarkCancelMessage = Toast.makeText(WebActivity.this, "Error posting bookmark.", Toast.LENGTH_LONG);
					bookmarkCancelMessage.show();
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

	public class WebSyncArticles extends AsyncTask<Void, Integer, Boolean> {
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
						String html = parseHTML("https://readability.com/mobile/articles/"+bm.article.id);
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
			}
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString("previous_update", latestUpdate);
			editor.commit();
			return true;
		}

	}
}


