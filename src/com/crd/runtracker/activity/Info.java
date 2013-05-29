package com.crd.runtracker.activity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.crd.runtracker.R;
import com.crd.runtracker.activity.base.Activity;
import com.markupartist.android.widget.ActionBar;

public class Info extends Activity {
	private WebView mWebView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info);

		mWebView = (WebView) findViewById(R.id.webview);
	}

	@Override
	public void onStart() {
		super.onStart();
		actionBar = (ActionBar) findViewById(R.id.action_bar);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.ic_menu_back;
			}

			@Override
			public void performAction(View view) {
				finish();
			}
		});

		mWebView.loadUrl("file:///android_asset/about.html");
	}
}
