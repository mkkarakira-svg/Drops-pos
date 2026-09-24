package com.drops.pos;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.View;
import android.graphics.Color;
import android.view.Window;
import android.view.WindowManager;\nimport android.view.WindowInsets;

public class MainActivity extends Activity {
    private WebView webView;
    private static final String APP_URL = "https://mkkarakira-svg.github.io/Drops-pos/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the Android status bar (clock) while keeping the navigation area usable.
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.setStatusBarColor(Color.rgb(245, 248, 252));
        window.setNavigationBarColor(Color.rgb(245, 248, 252));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(245, 248, 252));
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);\n        // Android 15 may draw app content behind the system navigation bar.\n        // Add only the bottom system inset so the POS bottom buttons stay tappable.\n        webView.setOnApplyWindowInsetsListener((v, insets) -> {\n            int bottom = 0;\n            if (android.os.Build.VERSION.SDK_INT >= 30) {\n                bottom = insets.getInsets(WindowInsets.Type.navigationBars()).bottom;\n            } else {\n                bottom = insets.getSystemWindowInsetBottom();\n            }\n            v.setPadding(0, 0, 0, bottom);\n            return insets;\n        });

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(false);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        setContentView(webView);

        if (savedInstanceState == null) {
            webView.loadUrl(APP_URL);
        } else {
            webView.restoreState(savedInstanceState);
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
