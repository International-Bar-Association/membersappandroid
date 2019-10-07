package com.ibamembers.search.favourites;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import com.ibamembers.app.App;
import com.ibamembers.app.DataManager;
import com.ibamembers.app.MainBaseActivity;
import com.ibamembers.search.ProfileSnippetFragment;
import com.ibamembers.search.SearchProfileActivity;
import com.ibamembers.search.SearchProfileFragment;

public class FavouritesActivity extends MainBaseActivity implements ProfileSnippetFragment.ProfileSnippetFragmentListener, SearchProfileFragment.SearchProfileFragmentListener {

    private static final int USER_PROFILE_REQUEST_CODE = 100;

    private FavouriteHandlerFragment favouriteHandlerFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDisplayHomeAsUpEnabled(true);
        favouriteHandlerFragment = getOrAddOnlyFragment(FavouriteHandlerFragment.class);
    }

    @Override
    public void profileSnippetClicked(ProfileSnippet profileSnippet, String jobTag) {
        if (favouriteHandlerFragment != null) {
            if (profileSnippet != null) { // if null favourites fragment should clear content view
                boolean handled = false;
                if (jobTag.equals(FavouriteHandlerFragment.TAG_FAVOURITES_JOB)) {
                    handled = favouriteHandlerFragment.profileSnippetClicked(profileSnippet);
                }

                if (!handled) {
                    App app = (App) getApplication();
                    if (app != null) {
                        DataManager dataManager = app.getDataManager();
                        Intent intent = SearchProfileActivity.getSearchProfileActivityIntent(this, profileSnippet.getId(), dataManager.getFullName(profileSnippet.getFirstName(), profileSnippet.getLastName()), false);
                        startActivityForResult(intent, USER_PROFILE_REQUEST_CODE);
                    }
                }
            }
        }
    }

    @Override
    public void profileHeaderSnippetClicked() {
        //do nothing
    }

    @Override
    public void leaveProfileFragment() {

    }

    @Override
    public void favouriteStatusChanged(float userId, boolean wasRemoved) {

    }

    @Override
    public void searchProfileFinishedLoading(String tag) {

    }

    @Override
    public void changeToolbarTitle(String title, String jobTag) {

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
