package com.ibamembers.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.ibamembers.app.MainBaseActivity;
import com.ibamembers.profile.UserProfileFragment;

public class SearchProfileActivity extends MainBaseActivity implements SearchProfileFragment.SearchProfileFragmentListener, UserProfileFragment.UserProfileFragmentListener {

    private static final String TAG_SEARCH_ACTIVITY_JOB = "TAG_SEARCH_ACTIVITY_JOB";
    public static final String KEY_SEARCH_PROFILE_ID = "KEY_SEARCH_PROFILE_ID";
    public static final String KEY_USER_NAME = "KEY_USER_NAME";
    public static final String KEY_FROM_MESSAGE_THREAD = "KEY_FROM_MESSAGE_THREAD";

    public static Intent getSearchProfileActivityIntent(Context context, int profileId, String userName, boolean isFromMessageThread) {
        Intent intent = new Intent(context, SearchProfileActivity.class);
        intent.putExtra(KEY_SEARCH_PROFILE_ID, profileId);
        intent.putExtra(KEY_USER_NAME, userName);
        intent.putExtra(KEY_FROM_MESSAGE_THREAD, isFromMessageThread);
        return intent;
    }

    private SearchProfileFragment searchProfileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        searchProfileFragment = getOrAddOnlyFragment(SearchProfileFragment.class,
                SearchProfileFragment.getSearchProfileFragmentArgs(intent.getIntExtra(KEY_SEARCH_PROFILE_ID, -1),
                        intent.getStringExtra(KEY_USER_NAME),
						intent.getBooleanExtra(KEY_FROM_MESSAGE_THREAD, false),
                        TAG_SEARCH_ACTIVITY_JOB));
        searchProfileFragment.setHasOptionsMenu(true);
    }

    @Override
    public void leaveProfileFragment() {
        finish();
    }

    @Override
    public void favouriteStatusChanged(float userId, boolean wasRemoved) {
        setResult(RESULT_OK);
    }

    @Override
    public void searchProfileFinishedLoading(String jobTag) {
        if (searchProfileFragment != null) {
            searchProfileFragment.showFavouriteMenuIcon();
        }
    }

    @Override
    public void changeToolbarTitle(String title, String jobTag) {
        this.setTitle(title);
    }

    @Override
    public void editBio() {
        // do nothing
    }

    @Override
    public void updateConferenceButton() {
        //do nothing
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
