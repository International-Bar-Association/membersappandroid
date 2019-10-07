package com.ibamembers.content;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.MenuItem;

import com.ibamembers.app.BaseActivity;
import com.ibamembers.app.MainBaseActivity;

public class ContentDetailActivity extends MainBaseActivity implements ContentDetailFragment.ContentDetailFragmentListener {

    public static final String KEY_CONTENT_STRING = "KEY_CONTENT_STRING";
    public static final String KEY_IS_DOWNLOADS = "KEY_IS_DOWNLOADS";

    public static Intent getContentDetailActivityIntent(Context context, String contentString, boolean isMyDownloads) {
        Intent intent = new Intent(context, ContentDetailActivity.class);
        intent.putExtra(KEY_CONTENT_STRING, contentString);
        intent.putExtra(KEY_IS_DOWNLOADS, isMyDownloads);
        return intent;
    }

    private ContentDetailFragment contentDetailFragment;

    @Override
    protected LayoutType getLayoutType() {
        return LayoutType.IMAGE_TOOLBAR;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            finish();
        }
        setDisplayHomeAsUpEnabled(true);
        setTitle("");
        Intent intent = getIntent();
        if (intent != null) {
            String contentString = intent.getStringExtra(KEY_CONTENT_STRING);
            boolean isDownloads = intent.getBooleanExtra(KEY_IS_DOWNLOADS, false);
            contentDetailFragment = getOrAddOnlyFragment(ContentDetailFragment.class, ContentDetailFragment.getContentDetailFragmentArgs(contentString, isDownloads));
            contentDetailFragment.setHasOptionsMenu(true);
        }
    }

    @Override
    public void loadVideoPlayer(String videoUrl) {
        startActivity(VideoPlayerActivity.getVideoPlayerActivityIntent(this, videoUrl));
    }

    @Override
    public void loadWebView(String offlineUrl, String url) {
        startActivity(WebViewActivity.getWebViewActivityIntent(this, offlineUrl, url));
    }

    @Override
    public void invalidateMenus(boolean canDownload, boolean isDownloaded) {
        invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }
}
