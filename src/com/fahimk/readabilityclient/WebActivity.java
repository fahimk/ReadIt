package com.fahimk.readabilityclient;

import static com.fahimk.readabilityclient.JavascriptModifyFunctions.*;
import static com.fahimk.readabilityclient.HelperMethods.*;
import static com.fahimk.readabilityclient.HTMLOutline.*;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebActivity extends Activity {
	private WebView webView;
	int key=0;
	double scrollPosition = 0.0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.web);
		getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

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
					MyActivity.setTitle(R.string.app_name);
			}
		});

		Bundle data = getIntent().getExtras();
		String content = "";
		String url = "";
		
		boolean isLocal = false;
		if(data != null) {
			content = data.getString("article_content");
			url = data.getString("article_url");
			isLocal = data.getBoolean("local");
			scrollPosition = data.getDouble("scroll_position");
		}
		Log.e("abc", String.format("%s,%s,%s", content, url, isLocal));
		if(!isLocal && url != null) {
			try {
				String s = "https://www.readability.com/mobile/articles/"+url;
				content = parseHTML(s);
			} catch (Exception e) {
				Log.e("exception loading url", e.getMessage());
			}
		}
		content = header+content+footer;
		//Log.e("content", content);

		webView.getSettings().setCacheMode(WebSettings.LOAD_NORMAL);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		webView.setWebViewClient(new CustomWebView());

		webView.loadDataWithBaseURL("", content, "text/html", "UTF-8", "");


	}

	private void setupCustomPanel() {
		final EditPanel popup = (EditPanel) findViewById(R.id.popup_window);
		popup.setVisibility(View.GONE);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if(event.getAction() == KeyEvent.ACTION_DOWN)
		{
			switch(keyCode)
			{
			case KeyEvent.KEYCODE_MENU:
				final EditPanel popup = (EditPanel) findViewById(R.id.popup_window);
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
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
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
			//			view.loadUrl("javascript:(function() { " +  
			//					"$(\".article-back-link\").attr(\"href\", \"##\")" +  
			//			"})()");
			//
//			view.loadUrl("javascript:(function() { " +  
//					//"$(\"a[class='article-back-link']\").attr(\"href\", \"backButton#\");" +
//					//"var images = document.getElementsByTagName('img'); var l = images.length; for (var i = 0; i < l; i++) {images[0].parentNode.removeChild(images[0])}" +
//					"var readBar = document.getElementById('read-bar'); readBar.parentNode.removeChild(readBar);"+
//
//					"var footNote = document.getElementById('article-marketing'); footNote.parentNode.removeChild(footNote);"+
//					//"var hLink=document.getElementsByTagName(\"a\"); for (i=0;i<hLink.length;i++){ if(!hLink[i].href){ hLink[i].href = '#'; }}" +
//					//"$('a:not([href*=\"#\"])').contents().unwrap();"+
//			"})()");  
			
			//need better algorithm for this based on number of words
			//view.scrollTo(0, (int) (scrollPosition * view.getContentHeight() + view.getHeight()));
			addButtonListeners(findViewById(R.id.mainFrame), webView);
			setupCustomPanel();
			setupDefaultTheme(webView);
		} 

	}
}


