package edu.temple.basic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WikiViewerActivity extends AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CookieManager manager = CookieManager.getInstance();
        manager.setAcceptCookie(true);


        setContentView(R.layout.activity_wiki_viewer);

        WebView myWebView = findViewById(R.id.wikiView);
        myWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                return false;
            }
        });
        manager.setAcceptThirdPartyCookies(myWebView, true);
        Intent intent = getIntent();
        String url = intent.getStringExtra(MainActivity.WIKI_URL_EXTRA);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.loadUrl(url);
    }
}
