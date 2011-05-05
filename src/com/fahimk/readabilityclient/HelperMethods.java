package com.fahimk.readabilityclient;

import static com.fahimk.readabilityclient.ArticlesSQLiteOpenHelper.ARTICLE_CONTENT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

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


	public static String requestApiUrl(String page, String apiSecret, String extras) {
		String url = String.format(URL_API + "%s?&oauth_nonce=%s&oauth_timestamp=%s" + 
				"&oauth_consumer_key=%s&oauth_signature=%s&oauth_signature_method=PLAINTEXT" +
				"&xoauth_lang_pref=en-us%s", 
				page,getNonce(),getTimestamp(),API_KEY,apiSecret,extras);

		return url;
	}
	public static String getNonce() {
		Random random = new Random();
		return Long.toString(Math.abs(random.nextLong()), 60000);
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

}
