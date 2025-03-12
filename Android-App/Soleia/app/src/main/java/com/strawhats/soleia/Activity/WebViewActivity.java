package com.strawhats.soleia.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.material.appbar.MaterialToolbar;
import com.strawhats.soleia.R;

public class WebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_web_view);


        WebView webView = findViewById(R.id.web_view);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webView.setWebViewClient(new MyWebViewClient()); // Use a custom WebViewClient (see below)
        String url = getIntent().getStringExtra("url");
        webView.loadUrl(url);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT); // Or LOAD_CACHE_ELSE_NETWORK for offline


        String title = getIntent().getStringExtra("title");
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(title);

        // Handle back navigation
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });

    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // This is crucial:  Load the URL within the WebView, not the external browser
            view.loadUrl(url);
            return true;  // Indicate that we've handled the URL
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            // Handle errors gracefully (e.g., display an error message)
            Log.e("WebView Error", error.toString());
            // You might want to load a local error page here:
            // view.loadUrl("file:///android_asset/error.html"); // Example
            super.onReceivedError(view, request, error);
        }
    }


}