package com.crd.gpstracker.activity;

import android.os.Bundle;
import android.webkit.WebView;

import com.crd.gpstracker.R;
import com.crd.gpstracker.activity.base.Activity;

public class Info extends Activity {
	private WebView  mWebView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info);
		
		mWebView = (WebView) findViewById(R.id.webview);
	} 
	
	@Override 
	public void onStart() {
		super.onStart();
		
		mWebView.loadUrl("file:///android_asset/about.html");
	}
}
