package com.fahimk.readabilityclient;

import static com.fahimk.readabilityclient.HelperMethods.PREF_NAME;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class HelperMethods {
	public final static String PREF_NAME = "ReadabilityClient";

	public final static String API_KEY = "fahimkarim";
	public final static String API_SECRET = "zZdk2JFek6Ymf8uhETFBYpdTNS7CAJM5%26";
	public final static String URL_API = "https://www.readability.com/api/rest/v1/";
	public final static String URL_CALLBACK = "com.fahimk://readability";


	public final static String OAUTH_AUTHORIZE = "oauth/authorize/";
	public final static String OAUTH_REQUEST = "oauth/request_token/";
	public final static String OAUTH_ACCESS = "oauth/access_token/";


	public final static int MSG_END = 1;
	public final static int MSG_FAIL = 2;
	public final static int MSG_START_SETUPVIEWS = 3;
	public final static int MSG_START_WEBVIEWINTENT = 4;
	public final static int MSG_START_SYNCARTICLES = 5;
	public final static int MSG_BAD_URL = 6;
	
	public final static int MSG_WV_INIT = 7;
	public final static int MSG_WV_ADDFAV = 8;
	public final static int MSG_WV_ADDARC = 9;


	public final static String zeroUpdate = "2011-01-01 00:00:00";
	
	public static SQLiteDatabase setupDB(Context c) {
		ArticlesSQLiteOpenHelper helper = new ArticlesSQLiteOpenHelper(c);
		return helper.getWritableDatabase();
		//database.delete(ARTICLE_TABLE, null, null);
	}
	
	public static String requestApiUrl(String page, String apiSecret, String extras) {
		String url = String.format(URL_API + "%s?&oauth_nonce=%s&oauth_timestamp=%s" + 
				"&oauth_consumer_key=%s&oauth_signature=%s&oauth_signature_method=PLAINTEXT" +
				"&xoauth_lang_pref=en-us%s", 
				page,getNonce(),getTimestamp(),API_KEY,apiSecret,extras);

		return url;
	}
	
	public static String getNonce() {
		return Long.toString(new Random().nextLong());
	}

	public static String getTimestamp() {
		Calendar cal = Calendar.getInstance();
		return Long.toString(cal.getTimeInMillis() / 1000);
	}
	
	public static void darkenImage(ImageView v) {
		v.setColorFilter(0x55000000, PorterDuff.Mode.SRC_ATOP);
	}

	public static void lightenImage(ImageView v) {
		v.setColorFilter(0x99FFFFFF, PorterDuff.Mode.SRC_ATOP);
	}
	
	public static boolean checkAuthorization(Context context) {
		SharedPreferences tokenInfo = context.getSharedPreferences(PREF_NAME, 0);
		String token = tokenInfo.getString("oauth_token", null);
		String tokenSecret = tokenInfo.getString("oauth_token_secret", null);
		String verifier = tokenInfo.getString("oauth_verifier", null);
		return (token != null && tokenSecret != null && verifier != null);
	}

	public static void handleTouches(ImageView button) {
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

	public static InputStream getStream(String url) {
		DefaultHttpClient client = new DefaultHttpClient(); 
		HttpGet getRequest = new HttpGet(url);
		Log.e("heres what i got", "hello");
		try {
			HttpResponse getResponse = client.execute(getRequest);
			final int statusCode = getResponse.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) { 
				Log.w("HelperMethods.retrieveStream", 
						"Error " + statusCode + " for URL " + url); 
				return null;
			}

			return getResponse.getEntity().getContent();

		} 
		catch (IOException e) {
			getRequest.abort();
			Log.w("HelperMethods.retrieveStream", "Error for URL " + url, e);
		}

		return null;

	}

	public static String postData(String url, List<NameValuePair> nameValuePairs) throws Exception {
		HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(url);
	    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        HttpResponse response = httpclient.execute(httppost);
		InputStream in = response.getEntity().getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder str = new StringBuilder();
		String line = null;
		while((line = reader.readLine()) != null)
		{
			str.append(line);
		}
		in.close();
		Log.e("post str", str + " abc");
		return str.toString();
	}
	
	public static String parseHTML(String Url) throws Exception {
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(Url);

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
		html = html.replaceAll("/media/css/mobile.css", "file:///android_asset/mobile.css");
		html = html.replaceAll("/media/js/jquery.min.js", "file:///android_asset/jquery.min.js");
		//c = c.replaceAll("<a href=\"#\" class=\"article-back-link\">", "<a href=\"##\" class=\"article-back-link\">");
		//Log.e("html", c);
		return html;
	}

	public static void displayAlert(Context context, String title, String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			} }); 
		alertDialog.show();
	}
	
	public static void displayInfo(Context context, String title, String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setIcon(android.R.drawable.ic_dialog_info);
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			} }); 
		alertDialog.show();
	}

	public static ProgressDialog createProgressDialog(Context context, String title, String message) {
		ProgressDialog pDialog = new ProgressDialog(context);
		pDialog.setTitle(title);
		pDialog.setMessage(message);
		return pDialog;
	}
	
}
