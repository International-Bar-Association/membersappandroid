package com.ibamembers.content;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.ibamembers.app.BaseActivity;
import com.ibamembers.app.MainBaseActivity;

public class WebViewActivity extends MainBaseActivity {

    public static final String KEY_OFFLINE_URL = "KEY_OFFLINE_URL";
    public static final String KEY_WEB_URL = "KEY_WEB_URL";

    public static Intent getWebViewActivityIntent(Context context, String offlineUrl, String url) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(KEY_OFFLINE_URL, offlineUrl);
        intent.putExtra(KEY_WEB_URL, url);
        return intent;
    }

    private WebViewFragment fragment;

    @Override
    protected LayoutType getLayoutType() {
        return LayoutType.NO_SCROLLVIEW;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDisplayHomeAsUpEnabled(true);
        setTitle("");
        Intent intent = getIntent();
        if (intent != null) {
            String offlineUrl = intent.getStringExtra(KEY_OFFLINE_URL);
            String url = intent.getStringExtra(KEY_WEB_URL);
            fragment = getOrAddOnlyFragment(WebViewFragment.class, WebViewFragment.getWebViewFragmentArgs(offlineUrl, url));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (fragment != null)
            if (fragment.getWebView().canGoBack()) {
                fragment.getWebView().goBack();
                return;
            }
        super.onBackPressed();
    }
}
