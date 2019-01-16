package com.test.webviewdummy;

import android.Manifest;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MyClient.WebBrowserListener {
    private static final int MULTIPLE_PERMISSIONS = 111;
    private final int CHROME_CUSTOM_TAB_REQUEST_CODE = 100;
    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};
    //    String url = "https://s3.ap-south-1.amazonaws.com/sony-dance-poc-test/index.html";
    String url = "https://s3.ap-south-1.amazonaws.com/sony-dance-poc-test/index.html";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        String url = "https://superdancer.smaaash.com";
        //setWeb(url);
//        String url = "https://4e48785e.ngrok.io/camera.html";
//        launchCustomTabs(url);
        if (checkPermissions()) {
            loadUrl(url);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHROME_CUSTOM_TAB_REQUEST_CODE) {
            finish();
        }
    }

    public void launchCustomTabs(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.setData(Uri.parse(url));
        startActivityForResult(customTabsIntent.intent, CHROME_CUSTOM_TAB_REQUEST_CODE);
    }

    @Override
    public void onPageFinished(WebView webView, String str) {

    }

    @Override
    public void onPageStarted(WebView webView, String str) {

    }

    @Override
    public void shouldOverrideUrl(WebView webView, String url) {
        webView.loadUrl(url);

    }

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(MainActivity.this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadUrl(url);
                } else {

//permissions missing
                    checkPermissions();

                }
                return;
            }
        }

    }

    public void loadUrl(String url) {
        WebView webview_view = findViewById(R.id.webview_view);
        webview_view.setWebViewClient(new MyClient());
        MyClient.setOnWebBrowserListener(this);
        webview_view.getSettings().setJavaScriptEnabled(true);
        webview_view.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview_view.getSettings().setUseWideViewPort(true);
        webview_view.getSettings().setLoadWithOverviewMode(true);
        webview_view.getSettings().setDomStorageEnabled(true);
        webview_view.getSettings().setDatabaseEnabled(true);
        webview_view.getSettings().setAllowFileAccessFromFileURLs(true);
        webview_view.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webview_view.getSettings().setAllowFileAccess(true);
        webview_view.getSettings().setPluginState(WebSettings.PluginState.ON);
        webview_view.getSettings().setAllowContentAccess(true);
        webview_view.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimeType,
                                        long contentLength) {


                if (url.startsWith("data:")) {  //when url is base64 encoded data
                    String path = createAndSaveFileFromBase64Url(url);
                    return;
                }

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimeType);
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading");
                String filename = URLUtil.guessFileName(url, contentDisposition, mimeType);
                request.setTitle(filename);
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Downloading", Toast.LENGTH_LONG).show();

            }
        });
        webview_view.setWebChromeClient(new

                                                WebChromeClient() {

                                                    @Override
                                                    public void onPermissionRequest(final PermissionRequest request) {
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                            request.grant(request.getResources());
                                                        }
                                                    }

                                                });
        if (Build.VERSION.SDK_INT >= 17) {
            webview_view.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }
        webview_view.loadUrl(url);
    }

    public String createAndSaveFileFromBase64Url(String url) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String filetype = url.substring(url.indexOf("/") + 1, url.indexOf(";"));
        String filename = System.currentTimeMillis() + "." + filetype;
        File file = new File(path, filename);
        try {
            if(!path.exists())
                path.mkdirs();
            if(!file.exists())
                file.createNewFile();

            String base64EncodedString = url.substring(url.indexOf(",") + 1);
            byte[] decodedBytes = Base64.decode(base64EncodedString, Base64.DEFAULT);
            OutputStream os = new FileOutputStream(file);
            os.write(decodedBytes);
            os.close();

            //Tell the media scanner about the new file so that it is immediately available to the user.
            MediaScannerConnection.scanFile(this,
                    new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });
 Toast.makeText(getApplicationContext(), "Download Done", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Log.d("ExternalStorage", "Error writing " + file, e);
        }

        return file.toString();
    }
}
