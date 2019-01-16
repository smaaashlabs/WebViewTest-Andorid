package com.test.webviewdummy;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyClient extends WebViewClient {
    private static WebBrowserListener lWebBrowserListener;

    public interface WebBrowserListener {
        void onPageFinished(WebView webView, String str);

        void onPageStarted(WebView webView, String str);

        void shouldOverrideUrl(WebView webView, String str);
    }

    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (lWebBrowserListener != null) {
            lWebBrowserListener.shouldOverrideUrl(view, url);
        }
        return true;
    }

    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (lWebBrowserListener != null) {
            lWebBrowserListener.onPageFinished(view, url);
        }
    }

    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (lWebBrowserListener != null) {
            lWebBrowserListener.onPageStarted(view, url);
        }
    }

    public static void setOnWebBrowserListener(WebBrowserListener listener) {
        lWebBrowserListener = listener;
    }
}