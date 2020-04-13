package com.example.mcflix;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;


/*
 * Implements a simple activity that allows the user to watch a video/movie that he has selected
 */
public class VideoActivity extends Activity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_main);

        Content content = (Content)getIntent().getSerializableExtra("content"); //Get the content that is going to be played

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());

        //The following webView settings are enabled otherwise we can't open different urls in the same application session
        webView.clearCache(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl( "javascript:window.location.reload( true )" );

        webView.loadUrl(content.getUrls().get(0)); // For now we only get the MP4 format which is the most compatible with all android versions

    }
}
