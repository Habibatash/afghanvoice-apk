package com.afghanvoice.admin;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Afghan Voice Admin Android shell. Authentication is handled by WordPress. */
public class MainActivity extends Activity {
    private static final String SITE_HOST = "afghanvoice.gt.tc";
    private static final String START_URL = "https://afghanvoice.gt.tc/wp-admin/admin.php?page=afghanvoice-admin-app";
    private static final int FILE_CHOOSER_REQUEST = 3401;

    private FrameLayout root;
    private WebView webView;
    private ProgressBar progress;
    private View errorPanel;
    private View splashPanel;
    private ValueCallback<Uri[]> filePathCallback;
    private String lastUrl = START_URL;
    private long lastBackPressAt = 0L;
    private static final long DOUBLE_BACK_WINDOW_MS = 2000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWindow();
        buildLayout();
        configureWebView();
        if (savedInstanceState != null) {
            WebView.HitTestResult ignored = webView.getHitTestResult();
            webView.restoreState(savedInstanceState);
            if (webView.getUrl() == null) webView.loadUrl(START_URL);
        } else {
            webView.loadUrl(START_URL);
        }
    }

    private void configureWindow() {
        Window window = getWindow();
        window.setStatusBarColor(Color.rgb(11, 41, 69));
        window.setNavigationBarColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    private void buildLayout() {
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(242, 245, 250));

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(242, 245, 250));
        FrameLayout.LayoutParams webParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        root.addView(webView, webParams);

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgress(0);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.rgb(22, 119, 184)));
        progress.setIndeterminate(false);
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(3), Gravity.TOP);
        root.addView(progress, progressParams);

        errorPanel = createErrorPanel();
        errorPanel.setVisibility(View.GONE);
        root.addView(errorPanel, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        splashPanel = createSplashPanel();
        root.addView(splashPanel, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);
    }

    private View createSplashPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(28), dp(28), dp(28), dp(28));
        panel.setBackgroundColor(Color.rgb(242, 246, 251));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.mipmap.ic_launcher);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dp(112), dp(112));
        logoParams.bottomMargin = dp(18);
        panel.addView(logo, logoParams);

        TextView title = new TextView(this);
        title.setText("افغان غږ نشراتي اداره");
        title.setTextColor(Color.rgb(18, 59, 97));
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD));
        panel.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView subtitle = new TextView(this);
        subtitle.setText("د خبرونو او خپرونو مدیریتي اپ");
        subtitle.setTextColor(Color.rgb(105, 122, 139));
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subtitleParams.topMargin = dp(5);
        panel.addView(subtitle, subtitleParams);

        ProgressBar spinner = new ProgressBar(this);
        spinner.setIndeterminate(true);
        spinner.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(Color.rgb(22, 119, 184)));
        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(dp(34), dp(34));
        spinnerParams.topMargin = dp(25);
        panel.addView(spinner, spinnerParams);
        return panel;
    }

    private void hideSplash() {
        if (splashPanel == null || splashPanel.getVisibility() != View.VISIBLE) return;
        View oldSplash = splashPanel;
        oldSplash.animate().alpha(0f).setDuration(220).withEndAction(() -> {
            if (root != null) root.removeView(oldSplash);
            if (splashPanel == oldSplash) splashPanel = null;
        }).start();
    }

    private View createErrorPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(28), dp(24), dp(28), dp(24));
        panel.setBackgroundColor(Color.rgb(242, 245, 250));

        TextView title = new TextView(this);
        title.setText("اړیکه نه ده ټینګه شوې");
        title.setTextColor(Color.rgb(18, 59, 97));
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        panel.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView body = new TextView(this);
        body.setText("د افغان غږ ادارې ته د ننوتلو لپاره انټرنېټ ته اړتیا ده. اړیکه وګورئ او بیا هڅه وکړئ.");
        body.setTextColor(Color.rgb(65, 76, 91));
        body.setTextSize(16);
        body.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bodyParams.topMargin = dp(12);
        panel.addView(body, bodyParams);

        Button retry = new Button(this);
        retry.setText("بیا هڅه وکړئ");
        retry.setTextColor(Color.WHITE);
        retry.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(18, 59, 97)));
        retry.setOnClickListener(v -> {
            errorPanel.setVisibility(View.GONE);
            webView.loadUrl(lastUrl == null ? START_URL : lastUrl);
        });
        LinearLayout.LayoutParams retryParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        retryParams.topMargin = dp(18);
        panel.addView(retry, retryParams);
        return panel;
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setGeolocationEnabled(false);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setUserAgentString(settings.getUserAgentString() + " AfghanVoiceAdminAndroid/1.0.6");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setSafeBrowsingEnabled(true);
            webView.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES);
        }

        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(webView, false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
                if ("https".equals(scheme) && isSiteHost(uri.getHost())) return false;
                if ("http".equals(scheme) && isSiteHost(uri.getHost())) {
                    String secure = uri.buildUpon().scheme("https").build().toString();
                    view.loadUrl(secure);
                    return true;
                }
                openExternal(uri);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                lastUrl = url;
                errorPanel.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                progress.setProgress(5);
                lastBackPressAt = 0L;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                lastUrl = url;
                progress.setVisibility(View.GONE);
                hideSplash();
                CookieManager.getInstance().flush();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request != null && request.isForMainFrame()) {
                    progress.setVisibility(View.GONE);
                    hideSplash();
                    errorPanel.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, android.webkit.WebResourceResponse response) {
                if (request != null && request.isForMainFrame() && response != null && response.getStatusCode() >= 400) {
                    progress.setVisibility(View.GONE);
                    hideSplash();
                    errorPanel.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this, "د پاڼې د پرانیستلو پر مهال د سرور ستونزه رامنځته شوه.", Toast.LENGTH_LONG).show();
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progress.setProgress(newProgress);
                if (newProgress >= 100) progress.setVisibility(View.GONE);
                else progress.setVisibility(View.VISIBLE);
            }

            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                                             FileChooserParams params) {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = callback;
                Intent intent = buildFilePickerIntent(params);
                try {
                    startActivityForResult(Intent.createChooser(intent, "انځور یا فایل وټاکئ"), FILE_CHOOSER_REQUEST);
                    return true;
                } catch (ActivityNotFoundException ex) {
                    filePathCallback = null;
                    callback.onReceiveValue(null);
                    Toast.makeText(MainActivity.this, "د فایل ټاکلو اپ ونه موندل شو.", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            Uri uri = Uri.parse(url);
            openExternal(uri);
        });
    }

    private Intent buildFilePickerIntent(WebChromeClient.FileChooserParams params) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        if (params != null) {
            String[] accepts = params.getAcceptTypes();
            List<String> types = new ArrayList<>();
            if (accepts != null) {
                for (String accept : accepts) {
                    if (accept != null && !accept.trim().isEmpty()) types.add(accept.trim());
                }
            }
            if (!types.isEmpty()) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, types.toArray(new String[0]));
                if (types.size() == 1) intent.setType(types.get(0));
            }
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, params.getMode() == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE);
        }
        return intent;
    }

    private boolean isSiteHost(String host) {
        return host != null && (SITE_HOST.equalsIgnoreCase(host) || ("www." + SITE_HOST).equalsIgnoreCase(host));
    }

    private void openExternal(Uri uri) {
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!("https".equals(scheme) || "http".equals(scheme) || "mailto".equals(scheme) || "tel".equals(scheme))) {
            Toast.makeText(this, "دا ډول لینک د اپ له لارې نه پرانیستل کېږي.", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } catch (ActivityNotFoundException ignored) {
            Toast.makeText(this, "د دې لینک د پرانیستلو اپ ونه موندل شو.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST && filePathCallback != null) {
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK && data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    results = new Uri[count];
                    for (int i = 0; i < count; i++) results[i] = data.getClipData().getItemAt(i).getUri();
                } else if (data.getData() != null) {
                    results = new Uri[]{data.getData()};
                }
            }
            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (webView != null) webView.saveState(outState);
    }

    @Override
    public void onBackPressed() {
        if (errorPanel != null && errorPanel.getVisibility() == View.VISIBLE) {
            errorPanel.setVisibility(View.GONE);
            lastBackPressAt = 0L;
            return;
        }
        if (webView == null) {
            handleDoubleBackToExit();
            return;
        }

        // First close the app drawer. If it is closed, return through safe
        // same-site WebView history so a website opened from the app returns
        // to the exact app page instead of leaving the app.
        webView.evaluateJavascript(
                "(function(){return window.AVAAHandleBack ? window.AVAAHandleBack() : 'continue-back';})()",
                value -> {
                    if ("\"menu-closed\"".equals(value)) return;
                    int targetIndex = findPreviousSafeHistoryIndex();
                    if (targetIndex >= 0) {
                        android.webkit.WebBackForwardList history = webView.copyBackForwardList();
                        int delta = targetIndex - history.getCurrentIndex();
                        lastBackPressAt = 0L;
                        if (delta != 0) webView.goBackOrForward(delta);
                        return;
                    }
                    handleDoubleBackToExit();
                });
    }

    private int findPreviousSafeHistoryIndex() {
        if (webView == null) return -1;
        android.webkit.WebBackForwardList history = webView.copyBackForwardList();
        int current = history.getCurrentIndex();
        for (int i = current - 1; i >= 0; i--) {
            android.webkit.WebHistoryItem item = history.getItemAtIndex(i);
            if (item != null && isSafeInAppHistoryUrl(item.getUrl())) return i;
        }
        return -1;
    }

    private boolean isSafeInAppHistoryUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) return false;
        try {
            Uri uri = Uri.parse(rawUrl);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            String path = uri.getPath() == null ? "" : uri.getPath().toLowerCase(Locale.ROOT);
            String action = uri.getQueryParameter("action");
            if (!"https".equals(scheme) || !isSiteHost(uri.getHost())) return false;
            if (path.endsWith("/wp-login.php") || path.endsWith("/wp-admin/admin-ajax.php") || path.endsWith("/wp-admin/admin-post.php")) return false;
            if ("logout".equalsIgnoreCase(action)) return false;
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void handleDoubleBackToExit() {
        long now = SystemClock.uptimeMillis();
        if (lastBackPressAt > 0 && now - lastBackPressAt <= DOUBLE_BACK_WINDOW_MS) {
            lastBackPressAt = 0L;
            Toast.makeText(this, "اپ بندېږي.", Toast.LENGTH_SHORT).show();
            moveTaskToBack(true);
        } else {
            lastBackPressAt = now;
            Toast.makeText(this, "د اپ د بندولو لپاره یو ځل بیا د شاته تګ تڼۍ کېکاږئ.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (filePathCallback != null) {
            filePathCallback.onReceiveValue(null);
            filePathCallback = null;
        }
        if (webView != null) {
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();
        }
        super.onDestroy();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
