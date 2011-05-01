package com.fahimk.readabilityclient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static com.fahimk.readabilityclient.HelperMethods.*;

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
		final TextView username = (TextView) findViewById(R.id.textview_username);
		final TextView password = (TextView) findViewById(R.id.textview_password);
		Button loginButton = (Button) findViewById(R.id.button_login);
		loginButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					loginAndAuthorize(username.getText().toString(), password
							.getText().toString());
				} catch (Exception e) {
					Log.e("error", e.toString());
				}
			}
		});
	}

	public void loginAndAuthorize(String username, String password)
	throws Exception {
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


}