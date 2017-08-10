package com.hotbitmapgg.bilibili.module.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.hotbitmapgg.bilibili.base.RxBaseActivity;
import com.hotbitmapgg.bilibili.utils.ClipboardUtil;
import com.hotbitmapgg.bilibili.utils.ConstantUtil;
import com.hotbitmapgg.bilibili.utils.ToastUtil;
import com.hotbitmapgg.bilibili.widget.CircleProgressView;
import com.hotbitmapgg.ohmybilibili.R;

import butterknife.BindView;

/**
 * Created by hcc on 16/8/7 14:12
 * 100332338@qq.com
 * <p/>
 * 浏览器界面
 */
public class BrowserActivity extends RxBaseActivity {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.circle_progress)
    CircleProgressView progressBar;
    @BindView(R.id.webView)
    WebView mWebView;

    private final Handler mHandler = new Handler();
    private String url, mTitle;
    private WebViewClientBase webViewClient = new WebViewClientBase();


    @Override
    public int getLayoutId() {
        return R.layout.activity_web;
    }


    @Override
    public void initViews(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent != null) {
            url = intent.getStringExtra(ConstantUtil.EXTRA_URL);
            mTitle = intent.getStringExtra(ConstantUtil.EXTRA_TITLE);
        }
        setupWebView();
    }


    @Override
    public void initToolBar() {
        mToolbar.setTitle(TextUtils.isEmpty(mTitle) ? "详情" : mTitle);
        setSupportActionBar(mToolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_browser, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_share:
                share();
                break;
            case R.id.menu_open:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
                break;
            case R.id.menu_copy:
                ClipboardUtil.setText(BrowserActivity.this, url);
                ToastUtil.ShortToast("已复制");
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void share() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
        intent.putExtra(Intent.EXTRA_TEXT, "来自「哔哩哔哩」的分享:" + url);
        startActivity(Intent.createChooser(intent, mTitle));
    }


    @Override
    public void onBackPressed() {
        // canGoBack() 是否可以后退
        if (mWebView.canGoBack() && mWebView.copyBackForwardList().getSize() > 0
                && !mWebView.getUrl().equals(mWebView.copyBackForwardList()
                .getItemAtIndex(0).getOriginalUrl())) {
            mWebView.goBack();
        } else {
            finish();
        }
    }


    public static void launch(Activity activity, String url, String title) {
        Intent intent = new Intent(activity, BrowserActivity.class);
        intent.putExtra(ConstantUtil.EXTRA_URL, url);
        intent.putExtra(ConstantUtil.EXTRA_TITLE, title);
        activity.startActivity(intent);
    }


    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        progressBar.spin();
        //声明WebSettings子类
        final WebSettings webSettings = mWebView.getSettings();
        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);// 设置与Js交互的权限
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);// 设置允许JS弹窗

        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);//不使用缓存，只从网络获取数据.
        webSettings.setDomStorageEnabled(true);// 开启 DOM storage API 功能
        webSettings.setGeolocationEnabled(true);
        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true);//将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true);// 缩放至屏幕的大小

        mWebView.getSettings().setBlockNetworkImage(true);
        mWebView.setWebViewClient(webViewClient);
        mWebView.requestFocus(View.FOCUS_DOWN);
        mWebView.getSettings().setDefaultTextEncodingName("UTF-8");//设置编码格式
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b2 = new AlertDialog
                        .Builder(BrowserActivity.this)
                        .setTitle(R.string.app_name)
                        .setMessage(message)
                        .setPositiveButton("确定", (dialog, which) -> result.confirm());
                b2.setCancelable(false);
                b2.create();
                b2.show();
                return true;
            }
        });
        mWebView.loadUrl(url);
    }


    /**
     * 处理各种通知 & 请求事件
     */
    public class WebViewClientBase extends WebViewClient {

        /**
         * 开始载入页面调用的，我们可以设定一个loading的页面，告诉用户程序在等待网络响应。
         * @param view
         * @param url
         * @param favicon
         */
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            //TODO 设定加载开始的操作
        }

        /**
         * 在页面加载结束时调用。我们可以关闭loading 条，切换程序动作。
         * @param view
         * @param url
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            progressBar.stopSpinning();
            mWebView.getSettings().setBlockNetworkImage(false);
        }

        /**
         * 复写shouldOverrideUrlLoading()方法，使得打开网页时不调用系统浏览器， 而是在本WebView中显示
         * @param view
         * @param url
         * @return
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            String errorHtml = "<html><body><h2>找不到网页</h2></body></html>";
            view.loadDataWithBaseURL(null, errorHtml, "text/html", "UTF-8", null);
        }

    }


    @Override
    protected void onPause() {
        mWebView.reload();
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        mWebView.destroy();
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
