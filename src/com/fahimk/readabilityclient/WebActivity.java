package com.fahimk.readabilityclient;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

public class WebActivity extends Activity {
	private WebView webView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web);
		if (savedInstanceState != null)
			((WebView)findViewById(R.id.webView)).restoreState(savedInstanceState);
		Bundle data = getIntent().getExtras();
		String content = "hello";
		if(data != null) {
			content = data.getString("content");
		}

		webView = (WebView)this.findViewById(R.id.webView);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		//myWebView.setWebViewClient(new CustomWebView());
		webView.loadDataWithBaseURL("...", content, "text/html", "UTF-8", "");  
		//myWebView.loadUrl(""https://www.readability.com/articles/" + content);
		//myWebView.loadUrl("javascript:(%28function%28%29%7Bwindow.baseUrl%3D%27https%3A//www.readability.com%27%3Bwindow.readabilityToken%3D%27LZPUBv9XN3GWbSMsxFSXnQFjsH7d6LS2BQaH26ZF%27%3Bvar%20s%3Ddocument.createElement%28%27script%27%29%3Bs.setAttribute%28%27type%27%2C%27text/javascript%27%29%3Bs.setAttribute%28%27charset%27%2C%27UTF-8%27%29%3Bs.setAttribute%28%27src%27%2CbaseUrl%2B%27/bookmarklet/save.js%27%29%3Bdocument.documentElement.appendChild%28s%29%3B%7D%29%28%29)");
		
        webView.setOnTouchListener(new View.OnTouchListener() {
            
            public boolean onTouch(View v, MotionEvent event) {
                WebView.HitTestResult hr = ((WebView)v).getHitTestResult();
                if (hr != null && "##".equals(hr.getExtra()))
                	WebActivity.this.finish();
                Log.e("test", "getExtra = "+ hr.getExtra() + "\t\t Type=" + hr.getType());
                webView.requestFocus(View.FOCUS_DOWN);
                return false;
                
            }
        });
	}
	
	protected void onSaveInstanceState(Bundle outState) {
		webView.saveState(outState);
	}
	//	private class CustomWebView extends WebViewClient {
	//
	//		@Override
	//		public boolean shouldOverrideUrlLoading(WebView view, String url) {
	//			view.loadUrl(url);
	//			return true;
	//		}
	//
	//	}
}


