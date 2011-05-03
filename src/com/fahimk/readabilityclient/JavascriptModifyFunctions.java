package com.fahimk.readabilityclient;

import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

public class JavascriptModifyFunctions {
	static boolean showImages = true;
	static boolean showLinks = true;
	
	final static int TYPE_COL = 0;
	final static int TYPE_SIZE = 1;
	final static int TYPE_STYLE = 2;

	static int[] currentTypesSelected = {2,2,0};
	static String[] mobileStyles = {"Newspaper","Novel", "eBook","Inverse","Athelas"};
	static String[] mobileSizes = {"x-small","small", "medium","large","x-large"};
	static String[] mobileCols = {"x-narrow","narrow","medium","wide","x-wide"};
    	
    
	static String[] editNames = {"col", "size", "style"};

	static Button toggleImages;
	static Button toggleLinks;

	static Button smallerWidth;
	static Button largerWidth;

	static Button previousTheme;
	static Button nextTheme;
	static TextView currentThemeText;

	static Button smallerText;
	static Button largerText;






	public static void addButtonListeners(View frameLayout, final WebView webView) {
		toggleImages = (Button) frameLayout.findViewById(R.id.button_edit_toggleimages);
		toggleLinks = (Button) frameLayout.findViewById(R.id.button_edit_togglelinks);

		previousTheme = (Button) frameLayout.findViewById(R.id.button_edit_prevtheme);
		nextTheme = (Button) frameLayout.findViewById(R.id.button_edit_nexttheme);
		currentThemeText = (TextView) frameLayout.findViewById(R.id.text_edit_currenttheme);

		smallerText = (Button) frameLayout.findViewById(R.id.button_edit_prevsize);
		largerText = (Button) frameLayout.findViewById(R.id.button_edit_nextsize);

		smallerWidth = (Button) frameLayout.findViewById(R.id.button_edit_prevwidth);
		largerWidth = (Button) frameLayout.findViewById(R.id.button_edit_nextwidth);

		toggleImages.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showImages = !showImages;
				displayImages(webView);
			}
		});

		toggleLinks.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showLinks = !showLinks;
				displayLinks(webView);
			}
		});

		previousTheme.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(currentTypesSelected[TYPE_STYLE] > 0) {
					setTheme(webView, TYPE_STYLE, --currentTypesSelected[TYPE_STYLE]);
					currentThemeText.setText(mobileStyles[currentTypesSelected[TYPE_STYLE]]);
				}
				toggleButtons(TYPE_STYLE);
			}
		});

		nextTheme.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(currentTypesSelected[TYPE_STYLE] < mobileStyles.length - 1) {
					setTheme(webView, TYPE_STYLE, ++currentTypesSelected[TYPE_STYLE]);
					currentThemeText.setText(mobileStyles[currentTypesSelected[TYPE_STYLE]]);
				}
				toggleButtons(TYPE_STYLE);
			}
		});
		


		smallerWidth.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(currentTypesSelected[TYPE_COL] > 0) {
					setTheme(webView, TYPE_COL, --currentTypesSelected[TYPE_COL]);
				}
				toggleButtons(TYPE_COL);
			}
		});
		
		largerWidth.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(currentTypesSelected[TYPE_COL] < mobileCols.length - 1) {
					setTheme(webView, TYPE_COL, ++currentTypesSelected[TYPE_COL]);
				}
				toggleButtons(TYPE_COL);
			}
		});

		smallerText.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(currentTypesSelected[TYPE_SIZE] > 0) {
					setTheme(webView, TYPE_SIZE, --currentTypesSelected[TYPE_SIZE]);
					Log.e("test", "test");
				}				
				toggleButtons(TYPE_SIZE);
			}
		});
		
		largerText.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(currentTypesSelected[TYPE_SIZE] < mobileSizes.length - 1) {
					setTheme(webView, TYPE_SIZE, ++currentTypesSelected[TYPE_SIZE]);
				}
				toggleButtons(TYPE_SIZE);
			}
		});

	}

	protected static void displayLinks(WebView webView) {
		if(showLinks) {			
			webView.loadUrl("javascript:(function() { " +
					"$('abc').replaceWith(function() { return '<a href=\"'+$(this).attr('href')+'\" >'+this.innerHTML+'</a>'; });"+
			"})()");		}
		else {
			webView.loadUrl("javascript:(function() { " +
					"$('a').replaceWith(function() { return '<abc href=\"'+$(this).attr('href')+'\" >'+this.innerHTML+'</abc>'; });"+
			"})()");

		}
	}

	public static void displayImages(WebView webView) {
		if(showImages) {
			webView.loadUrl("javascript:(function() { $('img').show(); })()");
		}
		else {
			webView.loadUrl("javascript:(function() { $('img').hide();})()");
		}

	}

	public static void toggleButtons(int type) {
		switch (type) {
		case TYPE_STYLE: 
			previousTheme.setEnabled(currentTypesSelected[TYPE_STYLE] > 0);
			nextTheme.setEnabled(currentTypesSelected[TYPE_STYLE] < mobileStyles.length-1);
			break;

		case TYPE_SIZE: 
			smallerText.setEnabled(currentTypesSelected[TYPE_SIZE] > 0);
			largerText.setEnabled(currentTypesSelected[TYPE_SIZE] < mobileSizes.length-1);
			break;
			
		case TYPE_COL: 
			smallerWidth.setEnabled(currentTypesSelected[TYPE_COL] > 0);
			largerWidth.setEnabled(currentTypesSelected[TYPE_COL] < mobileSizes.length-1);
			break;

		}
	}

	public static void setupDefaultTheme(WebView webView) {
		Log.e("defaults", String.format("%d,%d,%d",currentTypesSelected[0],currentTypesSelected[1],currentTypesSelected[2]));
		setTheme(webView, TYPE_STYLE, currentTypesSelected[TYPE_STYLE]);
		setTheme(webView, TYPE_SIZE, currentTypesSelected[TYPE_SIZE]);
		setTheme(webView, TYPE_COL, currentTypesSelected[TYPE_COL]);
		currentThemeText.setText(mobileStyles[currentTypesSelected[TYPE_STYLE]]);
		toggleButtons(TYPE_STYLE);
		toggleButtons(TYPE_SIZE);
		toggleButtons(TYPE_COL);
		displayImages(webView);	
		displayLinks(webView);
	}

	public static void setTheme(WebView webView, int type, int value) {
		String[] useArray;
		switch(type) {
		case TYPE_COL:
			useArray = mobileCols;
			break;
		case TYPE_SIZE:
			useArray = mobileSizes;
			break;
		case TYPE_STYLE:
			useArray = mobileStyles;
			break;
		default:
			useArray = mobileStyles;
			break;
		}

		String js = "javascript:(function() { " +
		"var originalClass = $('article').attr('class');" +
		"$('article').attr('class', originalClass.replace(/mobile-"+editNames[type]+"-[a-zA-Z0-9-_]*/, 'mobile-"+editNames[type]+"-"+useArray[value].toLowerCase()+"'))" +
		"})()";
		Log.e("js", js);
		Log.e("defaults", String.format("%d,%d,%d",currentTypesSelected[0],currentTypesSelected[1],currentTypesSelected[2]));

		webView.loadUrl(js);
	}

}

