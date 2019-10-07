package com.ibamembers.profile.message;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.ibamembers.R;
import com.ibamembers.app.MainBaseActivity;

public class ProfileMessageActivity extends MainBaseActivity {

    public static final String KEY_SEARCH_PROFILE_ID = "KEY_SEARCH_PROFILE_ID";
    public static final String KEY_USER_NAME = "KEY_USER_NAME";
    public static final String KEY_SHOW_PROFILE_BUTTON = "KEY_SHOW_PROFILE_BUTTON";
    public static final String KEY_IS_CONFERENCE = "KEY_IS_CONFERENCE";
    public static final String KEY_MESSAGE_STRING = "KEY_MESSAGE_STRING";

    public static Intent getProfileMessageActivity(Context context, int profileId, String userName, boolean showViewProfileButton, boolean isConference) {
        Intent intent = new Intent(context, ProfileMessageActivity.class);
        intent.putExtra(KEY_SEARCH_PROFILE_ID, profileId);
        intent.putExtra(KEY_USER_NAME, userName);
        intent.putExtra(KEY_SHOW_PROFILE_BUTTON, showViewProfileButton);
        intent.putExtra(KEY_IS_CONFERENCE, isConference);
        return intent;
    }

    private boolean isConference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            isConference = intent.getBooleanExtra(KEY_IS_CONFERENCE, false);
            int profileId = intent.getIntExtra(KEY_SEARCH_PROFILE_ID, 0);
            String username = intent.getStringExtra(KEY_USER_NAME);
            boolean showViewProfileButton = intent.getBooleanExtra(KEY_SHOW_PROFILE_BUTTON, false);
            getOrAddOnlyFragment(ProfileMessageFragment.class, ProfileMessageFragment.getProfileMessageFragment(profileId, username, showViewProfileButton, isConference));
        }

        setToolbar();
    }

    private void setToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (isConference) {
            baseToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.conference_theme_primary));
            setConferenceStatusBarColor();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}