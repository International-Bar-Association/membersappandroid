package com.ibamembers.messages;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;

import com.ibamembers.app.BaseActivity;
import com.ibamembers.app.MainBaseActivity;
import com.ibamembers.content.ContentDetailFragment;
import com.ibamembers.content.VideoPlayerActivity;
import com.ibamembers.content.WebViewActivity;

public class MessagesDetailActivity extends MainBaseActivity implements ContentDetailFragment.ContentDetailFragmentListener {

    public static final String KEY_MESSAGE_STRING = "KEY_MESSAGE_STRING";
    public static final String KEY_IS_NEW_MESSAGE = "KEY_IS_NEW_MESSAGE";
    public static final String KEY_STATUS = "KEY_STATUS";
    public static final String KEY_STATUS_IS_NEW = "KEY_STATUS_IS_NEW";

    public static Intent getMessagesDetailActivityIntent(Context context, String messageString) {
        return getMessagesDetailActivityIntent(context, messageString, false);
    }

    public static Intent getMessagesDetailActivityIntent(Context context, String messageString, boolean isNewMessage) {
        Intent intent = new Intent(context, MessagesDetailActivity.class);
        intent.putExtra(KEY_MESSAGE_STRING, messageString);
        intent.putExtra(KEY_STATUS_IS_NEW, isNewMessage);
        return intent;
    }

    private boolean isNewMessage;

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
            String messageString = intent.getStringExtra(KEY_MESSAGE_STRING);
            isNewMessage = intent.getBooleanExtra(KEY_IS_NEW_MESSAGE, false);
            MessagesDetailFragment fragment = getOrAddOnlyFragment(MessagesDetailFragment.class, MessagesDetailFragment.getMessagesDetailFragmentArgs(messageString));
            fragment.setHasOptionsMenu(true);
        }
    }

    @Override
    protected BaseActivity.LayoutType getLayoutType() {
        return BaseActivity.LayoutType.NO_SCROLLVIEW;
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(KEY_STATUS, MessagesDetailFragment.MessageStatus.Read);
        if (isNewMessage) {
            intent.putExtra(KEY_STATUS_IS_NEW, true);
        }
        setResult(RESULT_OK, intent);
        finish();
    }
}
