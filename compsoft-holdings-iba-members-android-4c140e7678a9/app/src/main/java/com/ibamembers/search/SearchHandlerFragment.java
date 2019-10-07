package com.ibamembers.search;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.MenuRes;
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
import com.ibamembers.profile.message.ProfileMessageFragment;
import com.ibamembers.search.favourites.ProfileSnippet;

import butterknife.ButterKnife;

public class SearchHandlerFragment extends BaseFragment {

    private static final String KEY_IS_CONFERENCE = "KEY_IS_CONFERENCE";

    public static Bundle getSearchHandlerFragmentArguments(boolean isConference) {
        Bundle args = new Bundle();
        args.putBoolean(KEY_IS_CONFERENCE, isConference);
        return args;
    }

    private boolean isSearchFilter;
    private SearchFilterFragment searchFilterFragment;
    private SearchResultFragment searchResultFragment;
    private SearchProfileFragment searchProfileFragment;
    private ProfileMessageFragment conferenceChatFragment;
    private DataPickerFragment dataPickerFragment;

    private static final String TAG_SEARCH_RESULT = "TAG_SEARCH_RESULT";
    private static final String TAG_SEARCH_PROFILE = "TAG_SEARCH_PROFILE";
    public static final String TAG_SEARCH_JOB = "TAG_SEARCH_JOB";
    private boolean isLandscape;
    private MenuItem favouriteMenuItem;

    private boolean isConference;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_multipane_fragment, container, false);
        ButterKnife.bind(this, view);

        FrameLayout contentFrame = view.findViewById(R.id.search_content_fragment);
        isLandscape = contentFrame != null;
        isSearchFilter = true;

        Bundle args = getArguments();
        if (args != null) {
            isConference = args.getBoolean(KEY_IS_CONFERENCE, false);
        }

        return view;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            searchResultFragment = (SearchResultFragment) getFragmentManager().getFragment(savedInstanceState, TAG_SEARCH_RESULT);

            if (searchResultFragment != null) {
                isSearchFilter = false;
                searchResultFragment.setShouldShowSelected(isLandscape);
            }

            Fragment fragment = getFragmentManager().findFragmentById(R.id.search_content_fragment);

            if (fragment instanceof SearchProfileFragment) {
                searchProfileFragment = (SearchProfileFragment) fragment;
            } else if (fragment instanceof DataPickerFragment) {
                dataPickerFragment = (DataPickerFragment) fragment;
            }
        }

        loadFragments();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!isSearchFilter) {
            getFragmentManager().putFragment(outState, TAG_SEARCH_RESULT, searchResultFragment);
        }
    }

    private void loadFragments() {
        if (isSearchFilter) {
            searchFilterFragment = new SearchFilterFragment();
            searchFilterFragment.setIsConference(isConference);
            loadFragmentIntoContainer(searchFilterFragment, R.id.search_main_fragment, true, null);
        } else {
            if (searchResultFragment == null) {
                searchResultFragment = new SearchResultFragment();
                searchResultFragment.setShouldShowSelected(isLandscape);
                searchResultFragment.setIsConference(isConference);
                loadFragmentIntoContainer(searchResultFragment, R.id.search_main_fragment, false, null);
            }
        }

        if (isLandscape && searchResultFragment != null) {
            ProfileSnippet selectedSnippet = searchResultFragment.getSelectedProfileSnippet();
            if (selectedSnippet != null) {
                profileSnippetClicked(selectedSnippet);
            }
        }
    }

    public @MenuRes int getMenuResourceId() {
        if (isSearchFilter) {
            if (isLandscape) {
                return R.menu.search_filter_and_favourite_menu;
            } else {
                return R.menu.search_filter_menu;
            }
        } else {
            if (isLandscape) {
                return R.menu.search_result_and_favourites_menu;
            } else {
                return R.menu.search_result_menu;
            }
        }
    }

    public boolean isProfilePresent() {
        return searchProfileFragment != null;
    }

    public boolean isDataPickerPresent() {
        return dataPickerFragment != null;
    }

    public void switchToSearchResults(boolean newFragment) {
        if (isSearchFilter) {
            if (newFragment) {
                if (searchFilterFragment.hasFilterChanged()) {
                    searchResultFragment = new SearchResultFragment();
                } else {
                    searchFilterFragment.setFilterChanged(false);
                }
            }

            if (searchResultFragment == null) {
                searchResultFragment = new SearchResultFragment();
            }

            searchResultFragment.setShouldShowSelected(isLandscape);
            searchResultFragment.setHasOptionsMenu(true);
            searchResultFragment.setIsConference(isConference);
            loadFragmentIntoContainer(searchResultFragment, R.id.search_main_fragment, false, null);
            isSearchFilter = false;
        }
    }

    public void cancelSearchJob(){
        if (searchResultFragment != null) {
            searchResultFragment.cancelSearching();
        }
    }

    public void switchToSearchFilter(boolean newFragment) {
        if (!isSearchFilter) {
            if (newFragment) {
                searchFilterFragment = new SearchFilterFragment();
            }

            if (searchFilterFragment == null) {
                searchFilterFragment = new SearchFilterFragment();
            }

            searchFilterFragment.setHasOptionsMenu(true);
            searchFilterFragment.setIsConference(isConference);
            loadFragmentIntoContainer(searchFilterFragment, R.id.search_main_fragment, true, null);
            isSearchFilter = true;
        }
    }

    public void searchProfileFinishedLoading(MenuItem favouriteMenuItem) {
        if (searchProfileFragment != null && favouriteMenuItem != null) {
            this.favouriteMenuItem = favouriteMenuItem;
            favouriteMenuItem.setVisible(true);
            searchProfileFragment.setUpFavouriteMenuItem(favouriteMenuItem);
        }
    }

    private void loadFragmentIntoContainer(Fragment fragment, @IdRes int containerId, boolean isSearchFilter, @Nullable String tag) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        if (!isSearchFilter) {
            fragmentTransaction.replace(containerId, fragment, TAG_SEARCH_RESULT);
        } else {
            fragmentTransaction.replace(containerId, fragment, tag);
        }

        fragmentTransaction.commit();
    }

    public void setShouldRefreshBooleans(boolean shouldRefreshCommittee, boolean shouldRefreshAreaOfPractices, boolean shouldRefreshCountries, boolean shouldRefreshConference) {
        if (isSearchFilter) {
            if (searchFilterFragment != null) {
                searchFilterFragment.setShouldRefreshBooleans(shouldRefreshCommittee, shouldRefreshAreaOfPractices, shouldRefreshCountries, shouldRefreshConference);
            }
        }
    }

    public void favouritesHaveChanged(float userId) {
        if (searchProfileFragment != null) {
            searchProfileFragment.favouriteHasChanged(userId);
        }
    }

    public boolean canSearchFabBeDisplayed() {
        if (searchFilterFragment != null) {
            return searchFilterFragment.isSearchFabVisible();
        }
        return false;
    }

    public boolean isSearchFilter() {
        return isSearchFilter;
    }

    public boolean getDataPickerValues(DataPickerFragment.DataType dataType) {
        if (isLandscape) {
            dataPickerFragment = new DataPickerFragment();
            dataPickerFragment.setArguments(DataPickerFragment.getDataPickerFragmentArguments(dataType));
            loadFragmentIntoContainer(dataPickerFragment, R.id.search_content_fragment, false, null);
            searchProfileFragment = null;
            return true;
        }
        return false;
    }

    public void setSearchTerm(String searchTerm) {
        if (dataPickerFragment != null) {
            dataPickerFragment.setSearchTerm(searchTerm);
        }
    }

    public boolean profileSnippetClicked(ProfileSnippet profileSnippet) {
        if (isLandscape) {
            App app = getApp();
            if (app != null) {

                DataManager dataManager = app.getDataManager();

                if (!isConference) {
                    if (shouldLoadSnippet(profileSnippet)) {

                        if (favouriteMenuItem != null) {
                            favouriteMenuItem.setVisible(false);
                        }

                        searchProfileFragment = new SearchProfileFragment();
                        searchProfileFragment.setArguments(SearchProfileFragment.getSearchProfileFragmentArgs(profileSnippet.getId(), dataManager.getFullName(profileSnippet.getFirstName(), profileSnippet.getLastName()), false, TAG_SEARCH_JOB));
                        loadFragmentIntoContainer(searchProfileFragment, R.id.search_content_fragment, false, TAG_SEARCH_PROFILE);
                        dataPickerFragment = null;
                    }
                } else {
                    conferenceChatFragment = new ProfileMessageFragment();
                    conferenceChatFragment.setArguments(ProfileMessageFragment.getProfileMessageFragment(profileSnippet.getId(), dataManager.getFullName(profileSnippet.getFirstName(), profileSnippet.getLastName()), true, isConference));

                    loadFragmentIntoContainer(conferenceChatFragment, R.id.search_content_fragment, false, TAG_SEARCH_PROFILE );
                }
                return true;
            }
        }
        return false;
    }

    private boolean shouldLoadSnippet(ProfileSnippet profileSnippet) {
        if (searchProfileFragment == null) {
            return true;
        }

        if (searchProfileFragment.getProfileId() != profileSnippet.getId()) {
            return true;
        }

        return false;
    }

    public void clearContentFragment() {
        Fragment contentFragment = getFragmentManager().findFragmentById(R.id.search_content_fragment);

        if (contentFragment != null) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.remove(contentFragment);
            fragmentTransaction.commit();
            dataPickerFragment = null;
            searchProfileFragment = null;
        }
    }
}
