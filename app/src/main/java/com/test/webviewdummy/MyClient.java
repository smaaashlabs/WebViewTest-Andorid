package com.test.webviewdummy;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/***
 * @author himanshu-singh www.github.com/hi-manshu
 */
public class MyClient extends WebViewClient {
    private static MyWebListeners myWebListener;

    public interface MyWebListeners {
        void onPageFinished(WebView webView, String str);

        void onPageStarted(WebView webView, String str);

        void shouldOverrideUrl(WebView webView, String str);
    }

    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (myWebListener != null) {
            myWebListener.shouldOverrideUrl(view, url);
        }
        return true;
    }

    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (myWebListener != null) {
            myWebListener.onPageFinished(view, url);
        }
    }

    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (myWebListener != null) {
            myWebListener.onPageStarted(view, url);
        }
    }

    public static void setOnWebBrowserListener(MyWebListeners listener) {
        myWebListener = listener;
    }
}
