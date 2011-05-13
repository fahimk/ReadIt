package com.fahimk.readabilityclient;

import static com.fahimk.readabilityclient.HelperMethods.API_SECRET;
import static com.fahimk.readabilityclient.HelperMethods.OAUTH_AUTHORIZE;
import static com.fahimk.readabilityclient.HelperMethods.displayAlert;
import static com.fahimk.readabilityclient.HelperMethods.requestApiUrl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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

	public static ProgressDialog createProgressDialog(Context context, String title, String message) {
		ProgressDialog pDialog = new ProgressDialog(context);
		pDialog.setTitle(title);
		pDialog.setMessage(message);
		return pDialog;
	}
	
}
