package com.fahimk.readabilityclient;

import com.fahimk.readabilityclient.JavascriptModifyFunctions.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class WebActivity extends Activity {
	private WebView webView;
	int key=0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.web);

		// Makes Progress bar Visible
		getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);


		if (savedInstanceState != null)
			((WebView)findViewById(R.id.webView)).restoreState(savedInstanceState);
		Bundle data = getIntent().getExtras();
		String id = "";
		if(data != null) {
			id = data.getString("article_id");
		}

		webView = (WebView)this.findViewById(R.id.webView);
		webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		webView.setWebViewClient(new CustomWebView());

		final Activity MyActivity = this;
		webView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress)   
			{
				//Make the bar disappear after URL is loaded, and changes string to Loading...
				MyActivity.setTitle("Loading...");
				MyActivity.setProgress(progress * 100); //Make the bar disappear after URL is loaded

				// Return the app name after finish loading
				if(progress == 100)
					MyActivity.setTitle(R.string.app_name);
			}
		});
		Log.e("html", id);
		webView.loadDataWithBaseURL("...", id, "text/html", "UTF-8", "");  

		JavascriptModifyFunctions a = new JavascriptModifyFunctions();
		a.addButtonListeners(findViewById(R.id.mainFrame), webView);
		setupCustomPanel();
	}
	
	private void setupCustomPanel() {
		final EditPanel popup = (EditPanel) findViewById(R.id.popup_window);
		popup.setVisibility(View.GONE);
		final Button btn=(Button)findViewById(R.id.show_popup_button);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if(key==0){
					key=1;
					popup.setVisibility(View.VISIBLE);
					webView.setClickable(false);
					popup.setClickable(true);
				}
				else if(key==1){
					key=0;
					popup.setVisibility(View.GONE);
					webView.setClickable(true);
					popup.setClickable(false);
				}
			}
		});
	}





	protected void onSaveInstanceState(Bundle outState) {
		webView.saveState(outState);
	}

	private class CustomWebView extends WebViewClient {

		public boolean shouldOverrideUrlLoading(WebView view, String url) {
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
			//			view.loadUrl("javascript:(function() { " +  
			//					"$(\".article-back-link\").attr(\"href\", \"##\")" +  
			//			"})()");
			view.loadUrl("javascript:(function() { " +  
					//"$(\"a[class='article-back-link']\").attr(\"href\", \"backButton#\");" +
					//"var images = document.getElementsByTagName('img'); var l = images.length; for (var i = 0; i < l; i++) {images[0].parentNode.removeChild(images[0])}" +
					"var readBar = document.getElementById('read-bar'); readBar.parentNode.removeChild(readBar);"+

					"var footNote = document.getElementById('article-marketing'); footNote.parentNode.removeChild(footNote);"+
					//"var hLink=document.getElementsByTagName(\"a\"); for (i=0;i<hLink.length;i++){ if(!hLink[i].href){ hLink[i].href = '#'; }}" +
					//"$('a:not([href*=\"#\"])').contents().unwrap();"+
			"})()");  
			super.onPageFinished(view, url);
		} 

	}
}


