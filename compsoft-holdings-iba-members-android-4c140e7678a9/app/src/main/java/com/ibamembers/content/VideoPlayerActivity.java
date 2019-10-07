package com.ibamembers.content;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.ibamembers.app.BaseActivity;
import com.ibamembers.app.MainBaseActivity;

public class VideoPlayerActivity extends MainBaseActivity {

    public static final String KEY_VIDEO_URL = "KEY_VIDEO_URL";

    public static Intent getVideoPlayerActivityIntent(Context context, String url) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra(KEY_VIDEO_URL, url);
        return intent;
    }

    @Override
    protected LayoutType getLayoutType() {
        return LayoutType.SCROLLVIEW_NO_TOOLBAR;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        Intent intent = getIntent();
        if (intent != null) {
            String videoUrl = intent.getStringExtra(KEY_VIDEO_URL);
            getOrAddOnlyFragment(VideoPlayerFragment.class, VideoPlayerFragment.getVideoPlayerFragmentArgs(videoUrl));
        }
    }
}
