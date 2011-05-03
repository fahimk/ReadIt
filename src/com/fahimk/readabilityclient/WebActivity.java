package com.fahimk.readabilityclient;

import static com.fahimk.readabilityclient.JavascriptModifyFunctions.addButtonListeners;
import static com.fahimk.readabilityclient.JavascriptModifyFunctions.setupDefaultTheme;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class WebActivity extends Activity {
	private WebView webView;
	int key=0;
	private ProgressDialog Dialog;
	
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

		Dialog = new ProgressDialog(WebActivity.this);
		Dialog.setMessage("Loading Page..");
		Dialog.show();
		
		Log.e("html", id);
		webView.loadDataWithBaseURL("", id, "text/html", "UTF-8", "");  

		addButtonListeners(findViewById(R.id.mainFrame), webView);
		setupCustomPanel();
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




	protected void onSaveInstanceState(Bundle outState) {
		webView.saveState(outState);
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
			setupDefaultTheme(webView);
			view.loadUrl("javascript:(function() { " +  
					//"$(\"a[class='article-back-link']\").attr(\"href\", \"backButton#\");" +
					//"var images = document.getElementsByTagName('img'); var l = images.length; for (var i = 0; i < l; i++) {images[0].parentNode.removeChild(images[0])}" +
					"var readBar = document.getElementById('read-bar'); readBar.parentNode.removeChild(readBar);"+

					"var footNote = document.getElementById('article-marketing'); footNote.parentNode.removeChild(footNote);"+
					//"var hLink=document.getElementsByTagName(\"a\"); for (i=0;i<hLink.length;i++){ if(!hLink[i].href){ hLink[i].href = '#'; }}" +
					//"$('a:not([href*=\"#\"])').contents().unwrap();"+
			"})()");  
			Dialog.dismiss();
			super.onPageFinished(view, url);
		} 

	}
}


