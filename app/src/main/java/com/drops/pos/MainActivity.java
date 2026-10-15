package com.drops.pos;

import android.app.Activity;
import android.webkit.JavascriptInterface;
import android.util.Base64;
import java.io.OutputStream;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient.FileChooserParams;
import android.widget.Toast;
import android.os.Bundle;
import android.os.Build;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.graphics.Color;

public class MainActivity extends Activity {
    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;
    private static final int FILE_REQUEST = 2026;
    private static final int SAVE_BACKUP_REQUEST = 2027;
    private byte[] pendingBackup;
    private String pendingBackupName;
    private static final String APP_URL = "https://mkkarakira-svg.github.io/Drops-pos/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.setNavigationBarColor(Color.rgb(245, 248, 252));
        if (Build.VERSION.SDK_INT >= 30) {
            window.setNavigationBarContrastEnforced(false);
        }

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(245, 248, 252));
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setOnApplyWindowInsetsListener((v, insets) -> {
            int bottom;
            if (Build.VERSION.SDK_INT >= 30) {
                bottom = insets.getInsets(WindowInsets.Type.navigationBars()).bottom;
            } else {
                bottom = insets.getSystemWindowInsetBottom();
            }
            // Keep the page full height; the web bottom navigation gets its own safe-area padding.
            v.setPadding(0, 0, 0, 0);
            return insets;
        });

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.clearCache(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(false);

        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface public void saveBackup(String filename, String base64) {
                try {
                    byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                    if (bytes.length == 0 || bytes.length > 20 * 1024 * 1024) throw new Exception("Invalid backup size");
                    runOnUiThread(() -> {
                        pendingBackup = bytes;
                        pendingBackupName = filename.matches("DropsPOS-backup-[0-9-]+\\.json") ? filename : "DropsPOS-backup.json";
                        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("application/json");
                        intent.putExtra(Intent.EXTRA_TITLE, pendingBackupName);
                        startActivityForResult(intent, SAVE_BACKUP_REQUEST);
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Backup error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }
        }, "DropsAndroid");
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                try {
                    Intent intent = params.createIntent();
                    startActivityForResult(intent, FILE_REQUEST);
                    return true;
                } catch (Exception e) {
                    fileCallback = null;
                    return false;
                }
            }
        });
        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            if (url.startsWith("blob:")) {
                Toast.makeText(this, "Use the backup save option inside Drops POS", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.addRequestHeader("User-Agent", userAgent == null ? "" : userAgent);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "DropsPOS-backup.json");
                ((DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(request);
                Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        setContentView(webView);

        if (savedInstanceState == null) {
            webView.loadUrl(APP_URL + "?appv=4.4.5");
        } else {
            webView.restoreState(savedInstanceState);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SAVE_BACKUP_REQUEST) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null && pendingBackup != null) {
                try (OutputStream stream = getContentResolver().openOutputStream(data.getData())) {
                    if (stream == null) throw new Exception("Cannot open destination");
                    stream.write(pendingBackup);
                    stream.flush();
                    Toast.makeText(this, "Backup saved successfully", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Backup save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            pendingBackup = null;
            pendingBackupName = null;
            return;
        }
        if (requestCode == FILE_REQUEST && fileCallback != null) {
            fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            fileCallback = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
