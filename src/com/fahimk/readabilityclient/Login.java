package com.fahimk.readabilityclient;

import static com.fahimk.readabilityclient.HelperMethods.API_SECRET;
import static com.fahimk.readabilityclient.HelperMethods.OAUTH_ACCESS;
import static com.fahimk.readabilityclient.HelperMethods.OAUTH_AUTHORIZE;
import static com.fahimk.readabilityclient.HelperMethods.OAUTH_REQUEST;
import static com.fahimk.readabilityclient.HelperMethods.PREF_NAME;
import static com.fahimk.readabilityclient.HelperMethods.URL_CALLBACK;
import static com.fahimk.readabilityclient.HelperMethods.requestApiUrl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class Login extends Activity {

	String oauthToken;
	String oauthTokenSecret;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		setupViews();
	}

	public void onResume() {
		super.onResume();
				SharedPreferences tokenInfo = getBaseContext().getSharedPreferences(PREF_NAME, 0);
				String token = tokenInfo.getString("oauth_token", null);
				String tokenSecret = tokenInfo.getString("oauth_token_secret", null);
				String verifier = tokenInfo.getString("oauth_verifier", null);
				if(token != null && tokenSecret != null && verifier != null) {
					Intent intent = new Intent(getBaseContext(), MainMenu.class);
					startActivity(intent);
					this.finish();
				}
		Log.e("onResume", (token == null) + " ");
	}

	public void setupViews() {
		Button loginButton = (Button) findViewById(R.id.button_login);
		Button readButton = (Button) findViewById(R.id.searchButton);

		loginButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					loginAndAuthorize();
				} catch (Exception e) {
					Log.e("error", e.toString());
				}
			}
		});

		readButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ReadUrlTask readTask = new ReadUrlTask();
				readTask.execute("http://google.com");
			}
		});
	}

	public void loginAndAuthorize() throws Exception {
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
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(requestApiUrl(OAUTH_AUTHORIZE, API_SECRET, "&" + tokenString)));
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_NO_HISTORY
				| Intent.FLAG_FROM_BACKGROUND);
		startActivity(intent);
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		Uri uri = intent.getData();
		if (uri != null && uri.toString().startsWith(URL_CALLBACK)) {
			try {
				HttpClient httpclient = new DefaultHttpClient();
				String oauthVerifier = uri.getQueryParameter("oauth_verifier");
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

				SharedPreferences tokenInfo = getBaseContext().getSharedPreferences(PREF_NAME, 0);
				SharedPreferences.Editor editor = tokenInfo.edit();
				editor.putString("oauth_token", oauthToken);
				editor.putString("oauth_token_secret", oauthTokenSecret);
				editor.putString("oauth_verifier", oauthVerifier);
				editor.commit();
			} catch (Exception e) {
				Log.e("exception, onNewIntent", e.getMessage() + "null");
			}
		} else {
			Log.e("error", "empty");
		}
	}
	
	public class ReadUrlTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... urls) {
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
				Log.e("string", line.substring(begin+articlesObject.length(), end));
				
			}
			catch(Exception e) {
				Log.e("error", e.getLocalizedMessage());
			}
			return true;
		}
		
	}


}