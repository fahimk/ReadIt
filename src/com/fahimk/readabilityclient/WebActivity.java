package com.fahimk.readabilityclient;

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

public class WebActivity extends Activity {
	private WebView webView;
	private static String mobileURL = "https://www.readability.com/mobile/articles/";
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
		//		String content = "Error loading article.";
		//		try {
		//			content = getContent("https://readability.com/mobile/articles/"+id);
		//			content = content.replaceAll("\"/", "\"https://readability.com/");
		//			content = content.replaceAll("<a href=\"#\" class=\"article-back-link\">", "<a href=\"##\" class=\"article-back-link\">");
		//
		//		} catch (Exception e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

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
		//webView.loadDataWithBaseURL("...", content, "text/html", "UTF-8", "");  
		webView.loadUrl(mobileURL + id);
		//myWebView.loadUrl("javascript:(%28function%28%29%7Bwindow.baseUrl%3D%27https%3A//www.readability.com%27%3Bwindow.readabilityToken%3D%27LZPUBv9XN3GWbSMsxFSXnQFjsH7d6LS2BQaH26ZF%27%3Bvar%20s%3Ddocument.createElement%28%27script%27%29%3Bs.setAttribute%28%27type%27%2C%27text/javascript%27%29%3Bs.setAttribute%28%27charset%27%2C%27UTF-8%27%29%3Bs.setAttribute%28%27src%27%2CbaseUrl%2B%27/bookmarklet/save.js%27%29%3Bdocument.documentElement.appendChild%28s%29%3B%7D%29%28%29)");
		webView.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				WebView.HitTestResult hr = ((WebView)v).getHitTestResult();
				if (hr != null && (mobileURL + "backButton#").equals(hr.getExtra()))
					WebActivity.this.finish();
				Log.e("test", "getExtra = "+ hr.getExtra() + "\t\t Type=" + hr.getType());
				webView.requestFocus(View.FOCUS_DOWN);
				return false;

			}
		});
	}

	protected String getContent(String s) throws Exception {
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(s);
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
		return html;
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
					"$(\"a[class='article-back-link']\").attr(\"href\", \"backButton#\");" +
					"var images = document.getElementsByTagName('img'); var l = images.length; for (var i = 0; i < l; i++) {images[0].parentNode.removeChild(images[0])}" +
					"var readBar = document.getElementById('read-bar'); readBar.parentNode.removeChild(readBar);"+
					"var footNote = document.getElementById('article-marketing'); footNote.parentNode.removeChild(footNote);"+
					"var hLink=document.getElementsByTagName(\"a\"); for (i=0;i<hLink.length;i++){ if(!hLink[i].href){ hLink[i].href = '#'; }}" +
					"$('a:not([href*=\"#\"])').contents().unwrap();"+
			"})()");  
			super.onPageFinished(view, url);
		} 

	}
}


