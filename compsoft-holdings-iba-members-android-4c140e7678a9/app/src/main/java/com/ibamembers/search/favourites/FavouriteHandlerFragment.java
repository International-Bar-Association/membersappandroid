package com.ibamembers.search.favourites;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ibamembers.R;
import com.ibamembers.app.App;
import com.ibamembers.app.BaseFragment;
import com.ibamembers.app.DataManager;
import com.ibamembers.search.SearchProfileFragment;

import java.sql.SQLException;

import butterknife.ButterKnife;

public class FavouriteHandlerFragment extends BaseFragment {

    private static final String TAG_FAVOURITES = "TAG_FAVOURITES";
    private static final String TAG_FAVOURITES_PROFILE = "TAG_FAVOURITES_PROFILE";
    public static final String TAG_FAVOURITES_JOB = "TAG_FAVOURITES_JOB";
    private FavouritesFragment favouritesFragment;
    private SearchProfileFragment searchProfileFragment;
    private boolean isLandscape;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.favourites_multipane_fragment, container, false);
        ButterKnife.bind(this, view);

        setRetainInstance(true);

        FrameLayout contentFrame = (FrameLayout) view.findViewById(R.id.favourites_content_fragment);
        isLandscape = contentFrame != null;

        loadFragments();
        return view;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            favouritesFragment = (FavouritesFragment) getFragmentManager().getFragment(savedInstanceState, TAG_FAVOURITES);

            if (favouritesFragment != null) {
                favouritesFragment.setShouldShowSelected(isLandscape);
            }

            Fragment fragment = getFragmentManager().findFragmentById(R.id.favourites_content_fragment);

            if (fragment != null) {
                if (fragment instanceof SearchProfileFragment) {
                    searchProfileFragment = (SearchProfileFragment) fragment;
                }
            }
        }

        loadFragments();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (favouritesFragment != null) {
            getFragmentManager().putFragment(outState, TAG_FAVOURITES, favouritesFragment);
        }
    }

    private void loadFragments() {
        if (favouritesFragment == null) {
            favouritesFragment = new FavouritesFragment();
            favouritesFragment.setShouldShowSelected(isLandscape);
            loadFragmentIntoContainer(favouritesFragment, R.id.favourites_main_fragment, TAG_FAVOURITES);
        }

        if (isLandscape) {
            ProfileSnippet selectedSnippet = favouritesFragment.getSelectedProfileSnippet();

            if (selectedSnippet != null) {
                profileSnippetClicked(selectedSnippet);
            }
        }
    }

    public void clearContentFragment() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.remove(searchProfileFragment);
        fragmentTransaction.commit();
        searchProfileFragment = null;
    }

    private void loadFragmentIntoContainer(Fragment fragment, @IdRes int containerId, @Nullable String tag) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(containerId, fragment, tag);
        fragmentTransaction.commit();
    }

    public void shouldRefreshFavourites(float userId, boolean wasRemoved) {
        if (favouritesFragment != null) {
            try {
                favouritesFragment.shouldRefreshFavourites(userId, wasRemoved);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void searchProfileFinishedLoading(MenuItem favouriteMenuItem) {
        if (searchProfileFragment != null && favouriteMenuItem != null) {
            favouriteMenuItem.setVisible(true);
            searchProfileFragment.setUpFavouriteMenuItem(favouriteMenuItem);
        }
    }

    public boolean isProfilePresent() {
        return searchProfileFragment != null;
    }

    public boolean profileSnippetClicked(ProfileSnippet profileSnippet) {
        if (isLandscape) {
            App app = getApp();
            if (app != null) {
                if (shouldLoadSnippet(profileSnippet)) {
                    DataManager dataManager = app.getDataManager();
                    searchProfileFragment = new SearchProfileFragment();
                    searchProfileFragment.setArguments(SearchProfileFragment.getSearchProfileFragmentArgs(profileSnippet.getId(), dataManager.getFullName(profileSnippet.getFirstName(), profileSnippet.getLastName()), false, TAG_FAVOURITES_JOB));
                    loadFragmentIntoContainer(searchProfileFragment, R.id.favourites_content_fragment, TAG_FAVOURITES_PROFILE);
                }
                return true;
            }
        }
        return false;
    }

    private boolean shouldLoadSnippet(ProfileSnippet profileSnippet) {
        return searchProfileFragment == null || searchProfileFragment.getProfileId() != profileSnippet.getId();
    }
}
