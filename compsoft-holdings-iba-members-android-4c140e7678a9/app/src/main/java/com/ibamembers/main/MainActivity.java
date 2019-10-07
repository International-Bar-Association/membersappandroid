package com.ibamembers.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.MenuRes;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.gson.Gson;
import com.ibamembers.R;
import com.ibamembers.app.App;
import com.ibamembers.app.BaseFragment;
import com.ibamembers.app.ConnectionManager;
import com.ibamembers.app.DataManager;
import com.ibamembers.app.DialogManager;
import com.ibamembers.app.IntentManager;
import com.ibamembers.app.SettingDao;
import com.ibamembers.app.gcm.MyFcmMessagingService;
import com.ibamembers.app.gcm.RegistrationIntentService;
import com.ibamembers.conference.ConferenceMainActivity;
import com.ibamembers.conference.event.job.GetConferenceJob;
import com.ibamembers.content.ContentBaseFragment;
import com.ibamembers.content.ContentDetailActivity;
import com.ibamembers.content.ContentDetailFragment;
import com.ibamembers.content.ContentFragment;
import com.ibamembers.content.ContentHandlerFragment;
import com.ibamembers.content.VideoPlayerActivity;
import com.ibamembers.content.WebViewActivity;
import com.ibamembers.content.job.ContentModel;
import com.ibamembers.messages.MessagesDetailActivity;
import com.ibamembers.messages.MessagesDetailFragment;
import com.ibamembers.messages.MessagesFragment;
import com.ibamembers.messages.MessagesHandlerFragment;
import com.ibamembers.messages.job.GeneralMessageModel;
import com.ibamembers.profile.EditBioActivity;
import com.ibamembers.profile.UserProfileFragment;
import com.ibamembers.profile.UserProfileNoAccessFragment;
import com.ibamembers.profile.job.RefreshJob;
import com.ibamembers.profile.message.ProfileMessageActivity;
import com.ibamembers.search.DataPickerActivity;
import com.ibamembers.search.DataPickerFragment;
import com.ibamembers.search.ProfileSnippetFragment;
import com.ibamembers.search.SearchFilterFragment;
import com.ibamembers.search.SearchHandlerFragment;
import com.ibamembers.search.SearchProfileActivity;
import com.ibamembers.search.SearchProfileFragment;
import com.ibamembers.search.SearchResultFragment;
import com.ibamembers.search.favourites.FavouritesActivity;
import com.ibamembers.search.favourites.ProfileSnippet;
import com.soundcloud.android.crop.Crop;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements UserProfileFragment.UserProfileFragmentListener, SearchResultFragment.SearchResultFragmentListener,
        SearchFilterFragment.SearchFilterFragmentListener, ProfileSnippetFragment.ProfileSnippetFragmentListener, DataPickerFragment.DataPickerFragmentListener,
        SearchProfileFragment.SearchProfileFragmentListener, ContentFragment.ContentFragmentListener, ContentDetailFragment.ContentDetailFragmentListener,
        MessagesFragment.MessageFragmentListener{

    private final static int EDIT_BIOGRAPHY = 100;
    public static final int COMMITTEE_REQUEST_CODE = 101;
    public static final int AREA_OF_PRACTICE_REQUEST_CODE = 102;
    public static final int COUNTRY_REQUEST_CODE = 103;
    private static final int CONFERENCE_REQUEST_CODE = 104;

    private static final int USER_PROFILE_REQUEST_CODE = 500;
    private static final int MESSAGE_READ_REQUEST_CODE = 600;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.main_coordinator_layout)
    protected CoordinatorLayout coordinatorLayout;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.main_activity_tabs)
    protected TabLayout mainTabs;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.viewpager)
    protected ViewPager mainViewPager;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.search_fab)
    protected FloatingActionButton searchFab;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.search_filter_fab)
    protected FloatingActionButton searchFilterFab;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.conf_fab)
    protected FloatingActionButton conferenceFab;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.conference_badge)
    protected TextView conferenceBadge;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.main_progress_bar)
	protected ProgressBar progressBar;

    private final String KEY_IS_FILTER = "KEY_IS_FILTER";
    private final String KEY_PAGER_POSITION = "KEY_PAGER_POSITION";
    private final int USER_PROFILE_PAGER_POSITION = 0;
    private final int MESSAGES_PAGER_POSITION = 1;
    private final int SEARCH_PAGER_POSITION = 2;
    private final int CONTENT_PAGER_POSITION = 3;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (searchHandlerFragment != null) {
            outState.putBoolean(KEY_IS_FILTER, searchHandlerFragment.isSearchFilter());
            outState.putInt(KEY_PAGER_POSITION, pagerPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isFilter = savedInstanceState.getBoolean(KEY_IS_FILTER, true);
        pagerPosition = savedInstanceState.getInt(KEY_PAGER_POSITION, USER_PROFILE_PAGER_POSITION);
        orientationChanged = true;
    }

    private UserProfileFragment userProfileFragment;
    private UserProfileNoAccessFragment userProfileNoAccessFragment;
    private MessagesHandlerFragment messagesHandlerFragment;
    private SearchHandlerFragment searchHandlerFragment;
    private ContentHandlerFragment contentHandlerFragment;

    private boolean isFilter;
    private int pagerPosition;
    private boolean orientationChanged;
    private boolean profileSnippetWaitingToBeClicked;
    private ProfileSnippet storedProfileSnippet;
    private String storedJobTag;
    private boolean canSearch;
    private boolean isShowConference = true;

    public int newMessageId = -1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == EDIT_BIOGRAPHY) {
                profileShouldRefresh = true;
            } else if (requestCode == COMMITTEE_REQUEST_CODE) {
                shouldRefreshCommittee = true;
            } else if (requestCode == AREA_OF_PRACTICE_REQUEST_CODE) {
                shouldRefreshAreaOfPractices = true;
            } else if (requestCode == COUNTRY_REQUEST_CODE) {
                shouldRefreshCountries = true;
            } else if (requestCode == CONFERENCE_REQUEST_CODE) {
                shouldRefreshConference = true;
            } else if (requestCode == USER_PROFILE_REQUEST_CODE) {
                shouldRefreshFavourites = true;
            } else if (requestCode == Crop.REQUEST_CROP && userProfileFragment != null) {
                userProfileFragment.onActivityResult(requestCode, resultCode, data);
            } else if (requestCode == MESSAGE_READ_REQUEST_CODE) {
                handleMessageOnResult(data);
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private MainPagerAdapter mainPagerAdapter;
    private MenuItem favouriteMenuItem;
    private MenuItem searchMenuItem;
    private MenuItem deleteMenuItem;
    private boolean profileShouldRefresh;
    private boolean shouldRefreshCommittee;
    private boolean shouldRefreshAreaOfPractices;
    private boolean shouldRefreshCountries;
    private boolean shouldRefreshConference;
    private boolean shouldRefreshFavourites;

    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        setToolbarTitle(R.string.toolbar_title_profile);

        //TODO GOt an error not allowed to start serve: app is in background uid
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);

//        App app = (App) getApplication();
//        if (app != null) {
//            app.getDefaultTracker(); // initialise tracker
//        }

        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        searchFab.hide();
        searchFilterFab.hide();

        canSearch = SettingDao.isClassAllowedToSearch((App) getApplication());
        conferenceFab.hide();

        setupViewPager(mainViewPager);
        mainTabs.setupWithViewPager(mainViewPager);
        updateTabImages(mainPagerAdapter.getCount() - 1);

        mainViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                dismissKeyboardIfUp();
                updateTabImages(position);
                updateFab(position);
                pagerPosition = position;
                invalidateOptionsMenu();

                if (position == 2) {
                    searchProfileFinishedLoading(SearchHandlerFragment.TAG_SEARCH_JOB);
                }

                if (position == MESSAGES_PAGER_POSITION) {
                    if (messagesHandlerFragment != null) {
                        messagesHandlerFragment.loadMessages();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public void loadConferenceEventFromLink() {
        App app = (App) getApplication();
        if (app != null) {
            int eventId = app.getLoadedEventId();
            if (eventId != 0) {
				        startActivity(new Intent(this, ConferenceMainActivity.class));
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null) {
            String stringId = intent.getStringExtra(MyFcmMessagingService.NOTIFICATION_ID_KEY);
            String stringType = intent.getStringExtra(MyFcmMessagingService.NOTIFICATION_TYPE_KEY);

            if (!TextUtils.isEmpty(stringId) && !TextUtils.isEmpty(stringType)) {
                if (stringType.equals(MyFcmMessagingService.MessageType.Standard.name())) {
                    final int id = Integer.parseInt(stringId);
                    mainViewPager.setCurrentItem(1);
                    newMessageId = id;
                    if (messagesHandlerFragment != null) {
                        messagesHandlerFragment.loadNewMessage(id);
                    }
                    setIntent(new Intent());
                } else {
                    App app = (App) getApplication();
                    if (app != null) {
                        int messageId = app.getP2PMessageId();
                        if (messageId != 0) {
                            startActivity(new Intent(this, ConferenceMainActivity.class));
                        }
                    }
                }
            }
        }
    }

    public void setNotificationBadge(){
        App app = (App) getApplication();
        if (app != null) {
            SharedPreferences sharedPrefs = app.getSharedPreferences();
            int notificationCount = sharedPrefs.getInt(MyFcmMessagingService.KEY_NOTIFICATION_COUNT, 0);

            if (notificationCount > 0) {
                conferenceBadge.setVisibility(View.VISIBLE);
                conferenceBadge.setText(String.valueOf(notificationCount));
            } else {
                conferenceBadge.setVisibility(View.INVISIBLE);
            }
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.search_fab)
    public void searchFabClicked() {
        App app = (App) getApplication();

        if (app != null) {
            ConnectionManager connectionManager = app.getConnectionManager();
            if (connectionManager.canConnectToInternet(MainActivity.this)) {
                searchFab.hide();
                searchFilterFab.show();
                dismissKeyboardIfUp();
                searchHandlerFragment.clearContentFragment();
                if (searchHandlerFragment != null) {
                    searchHandlerFragment.switchToSearchResults(true);
                }
            } else {
                showCantConnectToInternetDialog(DialogManager.UserInteraction.SEARCHING);
            }
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.search_filter_fab)
    public void searchFilterFabClicked() {
        searchFilterFab.hide();
        if (searchHandlerFragment != null) {
            searchHandlerFragment.switchToSearchFilter(true);
        }

        if (searchHandlerFragment.isSearchFilter()) {
            if (searchHandlerFragment.canSearchFabBeDisplayed()) {
                searchFab.show();
            }
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.conf_fab)
    public void conferenceFabClicked() {
        startActivity(new Intent(this, ConferenceMainActivity.class));
    }

    private void setupConferenceFabAndWebView() {
        if (isShowConference) {
            conferenceFab.show();
        } else {
            conferenceFab.setVisibility(View.INVISIBLE);
        }
    }

    //TODO Not used
    private void updateConferenceFabBackgroundTint(boolean isNormal) {
        if (isNormal) {
            conferenceFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, R.color.conference_fab_background)));
            //conferenceFab.setElevation(1);
        } else {
            conferenceFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity.this, R.color.conference_fab_background_faint)));
            conferenceFab.setElevation(0);
        }
    }

    private void checkCanSearch() {
        App app = (App) getApplication();
        if (app != null) {
            try {
                SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
                int profileClass = (int) settingDao.getCachedClass();
                Resources res = getResources();
                int[] bits = res.getIntArray(R.array.user_class_search_allowed);
                for (Integer profileClassInt : bits) {
                    if (profileClassInt == profileClass) {
                        Log.i("MainMenu", "Class can search");
                        canSearch = true;
                        break;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (getMenuResource() != -1) {
            inflater.inflate(getMenuResource(), menu);
            favouriteMenuItem = menu.findItem(R.id.action_favourite);
            searchMenuItem = menu.findItem(R.id.action_search);
            deleteMenuItem = menu.findItem(R.id.action_delete);

            switch (pagerPosition) {
                case USER_PROFILE_PAGER_POSITION:
                    if (favouriteMenuItem != null) {
                        favouriteMenuItem.setVisible(false);
                    }
                    break;
                case MESSAGES_PAGER_POSITION:
                    if (messagesHandlerFragment != null) {
                        favouriteMenuItem.setVisible(false);
                    }
                    break;
                case SEARCH_PAGER_POSITION:
                    if (canSearch) {
                        if (searchHandlerFragment != null) {
                            if (favouriteMenuItem != null) {
                                favouriteMenuItem.setVisible(false);
                                if (searchHandlerFragment.isProfilePresent()) {
                                    searchHandlerFragment.searchProfileFinishedLoading(favouriteMenuItem);
                                }
                            }

                            if (searchMenuItem != null) {
                                searchMenuItem.setVisible(false);

                                if (searchHandlerFragment.isDataPickerPresent()) {
                                    searchMenuItem.setVisible(true);
                                }

                                searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
                                if (searchView != null) {
                                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                                        @Override
                                        public boolean onQueryTextSubmit(String query) {
                                            return false;
                                        }

                                        @Override
                                        public boolean onQueryTextChange(String newText) {
                                            if (searchHandlerFragment != null) {
                                                searchHandlerFragment.setSearchTerm(newText);
                                            }
                                            return true;
                                        }
                                    });
                                }
                            }
                        }
                        break;
                    }
                case CONTENT_PAGER_POSITION:
                    if (contentHandlerFragment != null) {
                        favouriteMenuItem.setVisible(false);
                    }
                    break;
            }
        }

        return true;
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DialogManager.EditProfile editProfile) {
        switch (editProfile) {
            case EDIT_BIO:
                if (userProfileFragment != null) {
                    userProfileFragment.editBioClicked();
                }
                break;
            case EDIT_PROFILE_PICTURE:
                if (userProfileFragment != null) {
                    userProfileFragment.changePictureClicked();
                }
                break;
        }
    }

    private
    @MenuRes
    int getMenuResource() {
        if (pagerPosition == SEARCH_PAGER_POSITION && canSearch) {
            if (searchHandlerFragment != null && canSearch) {
                return searchHandlerFragment.getMenuResourceId();
            } else {
                return -1;
            }
        } else {
            return R.menu.profile_menu;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        App app = (App) getApplication();
        switch (item.getItemId()) {
            case R.id.action_contact_iba:
                if (app != null) {
                    IntentManager intentManager = app.getIntentManager();
                    intentManager.contactIba(this);
                }
                return true;
            case R.id.action_logout:
                try {
                    if (app != null) {
                        app.getConnectionManager().logout(this, app);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return false;
        }
    }

    private void updateFragmentsIfNeeded() {
        if (orientationChanged) {
            if (searchHandlerFragment != null) {
                if (!isFilter) {
                    searchHandlerFragment.switchToSearchResults(false);
                }
            }

            //Set to a new thread because was getting error where FAB anchor was changed after CoordinatorLayout was change but before it was complete
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateFab(pagerPosition);
                    updateTabImages(pagerPosition);
                    orientationChanged = false;
                }
            }, 400);
        }
    }

    private void updateTabImages(int selectedTabPosition) {
        for (int i = 0; i < mainTabs.getTabCount(); i++) {
            TabLayout.Tab tab;
            tab = mainTabs.getTabAt(i);
            if (tab != null) {
                switch (i) {
                    case 0:
                        tab.setIcon(selectedTabPosition == 0 ? R.drawable.selected_tab_profile : R.drawable.tab_profile);
                        break;
                    case 1:
                        tab.setIcon(selectedTabPosition == 1 ? R.drawable.selected_tab_messages : R.drawable.tab_messages);
                        break;
                    case 2:
                        if (canSearch) {
                            tab.setIcon(selectedTabPosition == 2 ? R.drawable.selected_tab_search : R.drawable.tab_search);
                            break;
                        }
                    case 3:
                        tab.setIcon(selectedTabPosition == 3 ? R.drawable.selected_tab_content : R.drawable.tab_content);
                        break;
                }
            }
        }
    }

    private void anchorFabBottom(FloatingActionButton fab) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        params.setAnchorId(R.id.viewpager);
        params.anchorGravity = Gravity.BOTTOM | Gravity.END;
        params.setMargins(0,
                0,
                (int) getResources().getDimension(R.dimen.activity_horizontal_margin),
                (int) getResources().getDimension(R.dimen.activity_horizontal_margin));
        fab.setLayoutParams(params);
    }

    private void updateFab(int position) {
        switch (position) {
            case 0:
                searchFab.hide();
                searchFilterFab.hide();
                setToolbarTitle(R.string.toolbar_title_profile);
                break;
            case 1:
                searchFab.hide();
                searchFilterFab.hide();
                if (messagesHandlerFragment != null) {
                    if (messagesHandlerFragment.isLandscape()) {
                        //anchorFabBottom(conferenceFab, true);
                    }
                }

                setToolbarTitle(R.string.toolbar_title_messages);
                break;
            case 2:
                if (canSearch) {
                    if (searchHandlerFragment != null) {
                        searchFab.hide();
                        searchFilterFab.hide();

                        if (searchHandlerFragment.isSearchFilter()) {
                            if (searchHandlerFragment.canSearchFabBeDisplayed()) {
                                searchFab.show();
                            }
                        } else {
                            searchFilterFab.show();
                        }
                    }
                    setToolbarTitle(R.string.toolbar_title_search);
                    break;
                }
            case 3:
                searchFab.hide();
                searchFilterFab.hide();
                setToolbarTitle(R.string.toolbar_title_content);
                break;
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mainPagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setCurrentItem(mainPagerAdapter.getCount() - 1);
    }

    private void setToolbarTitle(@StringRes int title) {
        setTitle(this.getResources().getString(title));
    }

    @Override
    protected void onResume() {
        super.onResume();
        runRefreshDataIfNeeded();

        if (profileShouldRefresh) {
            if (userProfileFragment != null) {
                try {
                    userProfileFragment.refreshProfileData();
                    profileShouldRefresh = false;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        if (searchHandlerFragment != null) {
            searchHandlerFragment.setShouldRefreshBooleans(shouldRefreshCommittee, shouldRefreshAreaOfPractices, shouldRefreshCountries, shouldRefreshConference);
        }

		setNotificationBadge();
        onNewIntent(getIntent());
    }

    private boolean runRefreshDataIfNeeded() {
        App app = (App) getApplication();
        if (app != null) {
            try {
                DataManager dataManager = app.getDataManager();
                dataManager.refreshData(app);
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

	@SuppressWarnings("unused")
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(MyFcmMessagingService.NotificationEvent event) {
		App app = (App) getApplication();
		if (app != null) {
			setNotificationBadge();
		}
	}

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RefreshJob.Success refreshJobSuccess) {
        App app = (App) getApplication();
        if (app != null) {
            try {
                app.getDataManager().resetDataRefreshedDate(app);
                //We should only have a conference object (from server) during the date bounds of conference
                updateConferenceButton();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RefreshJob.Error refreshJobError) {
        App app = (App) getApplication();
        if (app != null) {
            if (refreshJobError.getStatus() == getResources().getInteger(R.integer.session_token_invalid_code)) {
                app.getConnectionManager().sessionExpired(this, app);
            } else {
                //showProgressIndication(false);
                //serverErrorReceived(refreshJobError.getErrorMessage());
                //TODO error handling
            }
        }
    }

    @Override
    public void editBio() {
        // TODO landscape
        Intent intent = new Intent(this, EditBioActivity.class);
        startActivityForResult(intent, EDIT_BIOGRAPHY);
    }

    @Override
    public void updateConferenceButton() {
        App app = (App) getApplication();
        if (app != null) {
            //If conference is null then hide fad
//            if (isConferenceOn()) {
//                updateConferenceFabBackgroundTint(false);
//                conferenceFab.show();
//                loadConferenceEventFromLink();
//            } else {
//                conferenceFab.hide();
//                anchorFabBottom(searchFab);
//            }

            conferenceFab.show();
            loadConferenceEventFromLink();
        }
    }

    private boolean isConferenceOn() {
        App app = (App) getApplication();
        if (app != null) {
            try {
                SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
                SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.default_api_date_format), Locale.getDefault());
                String conferenceStartDateString = settingDao.getConferenceStartDate();
                String conferenceEndDateString = settingDao.getConferenceFinishDate();
                int conferenceId = settingDao.getConferenceId();

                if (conferenceStartDateString != null && conferenceEndDateString != null) {
                    Log.i("MainActivity", "Start date: " + conferenceStartDateString.toString());

                    DateTime today = new DateTime().withTimeAtStartOfDay();
                    DateTime startDate = new DateTime(sdf.parse(conferenceStartDateString)).withTimeAtStartOfDay();
                    DateTime endDate = new DateTime(sdf.parse(conferenceEndDateString)).withTimeAtStartOfDay();

                    if ((today.isAfter(startDate) || today.equals(startDate)
                        && today.isBefore(endDate) || today.isEqual(endDate)) //We are outside the date bounds of the conference
                        && getResources().getInteger(R.integer.current_conference_id) == conferenceId)
                        return true;
                }

            } catch (ParseException | SQLException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetConferenceSuccess(GetConferenceJob.Success response) {
        App app = (App) getApplication();
        if (app != null) {
            try {
                SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
                settingDao.setConferenceName(response.getResponse().getName());
                settingDao.setConferenceVenue(response.getResponse().getVenue());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        //Only show if the conferenceId of current app corresponds to the fetched conferenceId
        isShowConference = true;
        setupConferenceFabAndWebView();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetConferenceFailed(GetConferenceJob.GetConferenceJobError response) {
        Log.e("MainActivity", "Failed to fetch conference details");
    }

    @Override
    public void profileSnippetClicked(ProfileSnippet profileSnippet, String jobTag) {
        if (searchHandlerFragment != null) {
            if (profileSnippet != null) { // if null favourites fragment should clear content view
                boolean handled = false;
                if (jobTag.equals(SearchHandlerFragment.TAG_SEARCH_JOB)) {
                    handled = searchHandlerFragment.profileSnippetClicked(profileSnippet);
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

            storedProfileSnippet = null;
            storedJobTag = null;
            profileSnippetWaitingToBeClicked = false;

            if (searchMenuItem != null) {
                searchMenuItem.setVisible(false);
            }

        } else {
            profileSnippetWaitingToBeClicked = true;
            storedProfileSnippet = profileSnippet;
            storedJobTag = jobTag;
        }
    }

    @Override
    public void contentClicked(ContentModel content, ContentBaseFragment.ContentTabsState tabState) {
        if (contentHandlerFragment != null) {
            if (content != null) {
                boolean handled;
                handled = contentHandlerFragment.handleContentClickedInLandscape(content);

                if (!handled) {
                    startActivity(ContentDetailActivity.getContentDetailActivityIntent(this, new Gson().toJson(content), tabState == ContentBaseFragment.ContentTabsState.MyDownloads));
                }
            }
        }
    }

    @Override
    public void profileHeaderSnippetClicked() {
        startActivity(new Intent(this, FavouritesActivity.class));
    }

    @Override
    public void finishedSearching() {
        searchFilterFab.hide();

        if (searchHandlerFragment.isSearchFilter()) {
            if (searchHandlerFragment.canSearchFabBeDisplayed()) {
                searchFab.show();
            }
        }

        if (searchHandlerFragment != null) {
            searchHandlerFragment.switchToSearchFilter(false);
        }
    }

    private void showCantConnectToInternetDialog(DialogManager.UserInteraction userInteraction) {
        App app = (App) getApplication();
        if (app != null) {
            DialogManager dialogManager = app.getDialogManager();
            dialogManager.showNoInternetConnectionDialog(this, userInteraction);
        }
    }

    public void dismissKeyboardIfUp() {
        View focusedView = getCurrentFocus();
        if (focusedView != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    @Override
    public void favouritesClicked() {
        startActivity(new Intent(this, FavouritesActivity.class));
    }

    @Override
    public void loadDataPickerView(DataPickerFragment.DataType dataType) {

        if (searchHandlerFragment != null) {
            if (!searchHandlerFragment.getDataPickerValues(dataType)) {
                switch (dataType) {
                    case COMMITTEES:
                        startActivityForResult(DataPickerActivity.getDataPickerActivityIntent(this, DataPickerFragment.DataType.COMMITTEES, false), COMMITTEE_REQUEST_CODE);
                        return;
                    case AREA_OF_PRACTICES:
                        startActivityForResult(DataPickerActivity.getDataPickerActivityIntent(this, DataPickerFragment.DataType.AREA_OF_PRACTICES, false), AREA_OF_PRACTICE_REQUEST_CODE);
                        return;
                    case COUNTRIES:
                        startActivityForResult(DataPickerActivity.getDataPickerActivityIntent(this, DataPickerFragment.DataType.COUNTRIES, false), COUNTRY_REQUEST_CODE);
                        return;
                    case CONFERENCE:
                        startActivityForResult(DataPickerActivity.getDataPickerActivityIntent(this, DataPickerFragment.DataType.CONFERENCE, false), CONFERENCE_REQUEST_CODE);
                        return;
                }
            } else if (searchMenuItem != null && favouriteMenuItem != null) {
                searchMenuItem.setVisible(true);
                resetSearchView();
                favouriteMenuItem.setVisible(false);
            }
        }
    }

    private void resetSearchView() {
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
            searchView.setIconified(true);
        }
    }

    @Override
    public void setSearchButtonVisibility(boolean isVisible) {
        if (pagerPosition == SEARCH_PAGER_POSITION) {
            if (isVisible) {
                searchFab.show();
            } else {
                searchFab.hide();
            }
        }
    }

    @Override
    public void newDataItemSelected() {
        if (searchHandlerFragment != null) {
            searchHandlerFragment.setShouldRefreshBooleans(true, true, true, true);
        }
    }

    @Override
    public void leaveProfileFragment() {
        // do nothing
    }

    @Override
    public void favouriteStatusChanged(float userId, boolean wasRemoved) {
        if (searchHandlerFragment != null && wasRemoved) {
            searchHandlerFragment.favouritesHaveChanged(userId);
        }
    }

    @Override
    public void searchProfileFinishedLoading(String jobTag) {
        if (jobTag.equals(SearchHandlerFragment.TAG_SEARCH_JOB) && pagerPosition == SEARCH_PAGER_POSITION) {
            if (searchHandlerFragment != null) {
                searchHandlerFragment.searchProfileFinishedLoading(favouriteMenuItem);
            }
        }
    }

    @Override
    public void changeToolbarTitle(String title, String jobTag) {
        if (jobTag.equals(SearchHandlerFragment.TAG_SEARCH_JOB) && pagerPosition == SEARCH_PAGER_POSITION) {
            this.setTitle(title);
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
        if (contentHandlerFragment != null) {
            contentHandlerFragment.setIsAndCanDownloadAndInvalidate(canDownload, isDownloaded);
        }
    }

    @Override
    public void messageClicked(GeneralMessageModel message) {
        if (messagesHandlerFragment != null) {
            if (message != null) {
                boolean handled;
                handled = messagesHandlerFragment.handleMessageClickedInLandscape(message);

                if (!handled) {
                    if (message.getMessageType() != MessagesDetailFragment.MessageType.Profile) {
                        startActivityForResult(MessagesDetailActivity.getMessagesDetailActivityIntent(this, new Gson().toJson(message)), MESSAGE_READ_REQUEST_CODE);
                    } else {
                        startActivityForResult(ProfileMessageActivity.getProfileMessageActivity(this, message.getAppUserMessageId(), message.getTitle(), true, false), MESSAGE_READ_REQUEST_CODE);
                    }
                }
            }
        }
    }

    @Override
    public void handleNewMessage(GeneralMessageModel message) {
        if (!isTablet() && !isLandscape()) {
            startActivityForResult(MessagesDetailActivity.getMessagesDetailActivityIntent(this, new Gson().toJson(message), true), MESSAGE_READ_REQUEST_CODE);
        }
    }

    @Override
    public void deleteMessageDetailInLandscape() {
        if (messagesHandlerFragment != null) {
            messagesHandlerFragment.clearMessageDetailFragment();
        }
    }

    @Override
    public int getNewMessageIdAndResetId() {
        int tempId = newMessageId;
        newMessageId = -1;
        return tempId;
    }

    private void setMessageAsRead() {
        if (messagesHandlerFragment != null) {
            messagesHandlerFragment.setMessageAsRead();
        }
    }

    //TODO mvoe this to the respective fragments
    private void handleMessageOnResult(Intent data) {
        if (data != null) {
            MessagesDetailFragment.MessageStatus messageStatus = (MessagesDetailFragment.MessageStatus) data.getSerializableExtra(MessagesDetailActivity.KEY_STATUS);
            boolean isNewMessage = data.getBooleanExtra(MessagesDetailActivity.KEY_STATUS_IS_NEW, false); //TODO
            switch (messageStatus) {
                case Read:
                    setMessageAsRead();
                    break;
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(BaseFragment.ShowSnackBar response) {
        showSnackBarWithMessage(response.getMessage());
    }

    public void showSnackBarWithMessage(String message) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private class MainPagerAdapter extends FragmentPagerAdapter {

        private static final int TAB_COUNT = 3;
        private static final int TAB_COUNT_SEARCH = 4;

        UserProfileFragment userProfileFragment;
        UserProfileNoAccessFragment userProfileNoAccessFragment;
        SearchHandlerFragment searchHandlerFragment;
        MessagesHandlerFragment messagesHandlerFragment;
        ContentHandlerFragment contentHandlerFragment;

        public MainPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            userProfileFragment = new UserProfileFragment();
            userProfileNoAccessFragment = new UserProfileNoAccessFragment();
            messagesHandlerFragment = new MessagesHandlerFragment();
            searchHandlerFragment = new SearchHandlerFragment();
            contentHandlerFragment = new ContentHandlerFragment();
        }

        @Override
        public Fragment getItem(int position) {
            if (canSearch) {
                switch (position) {
                    case 0:
                        return userProfileFragment;
                    case 1:
                        return messagesHandlerFragment;
                    case 2:
                        return searchHandlerFragment;
                    case 3:
                        return contentHandlerFragment;
                    default:
                        return null;
                }
            } else {
                switch (position) {
                    case 0:
                        return userProfileNoAccessFragment;
                    case 1:
                        return messagesHandlerFragment;
                    case 2:
                        return contentHandlerFragment;
                    default:
                        return null;
                }
            }
        }

        @Override
        public int getCount() {
            return canSearch ? TAB_COUNT_SEARCH : TAB_COUNT;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            // save the appropriate reference depending on position
            if (canSearch) {
                switch (position) {
                    case 0:
                        MainActivity.this.userProfileFragment = (UserProfileFragment) createdFragment;
                        break;
                    case 1:
                        MainActivity.this.messagesHandlerFragment = (MessagesHandlerFragment) createdFragment;
                        break;
                    case 2:
                        MainActivity.this.searchHandlerFragment = (SearchHandlerFragment) createdFragment;
                        checkIfProfileWaitingToBeClicked();
                        break;
                    case 3:
                        MainActivity.this.contentHandlerFragment = (ContentHandlerFragment) createdFragment;
                        break;
                }
            } else {
                switch (position) {
                    case 0:
                        MainActivity.this.userProfileNoAccessFragment = (UserProfileNoAccessFragment) createdFragment;
                        break;
                    case 1:
                        MainActivity.this.messagesHandlerFragment = (MessagesHandlerFragment) createdFragment;
                        break;
                    case 2:
                        MainActivity.this.contentHandlerFragment = (ContentHandlerFragment) createdFragment;
                        break;
                }
            }

            //TODO removed temporily
            updateFragmentsIfNeeded();
            return createdFragment;
        }
    }

    private void checkIfProfileWaitingToBeClicked() {
        if (profileSnippetWaitingToBeClicked) {
            profileSnippetClicked(storedProfileSnippet, storedJobTag);
        }
    }
    public boolean isTablet() {
        return (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public boolean isLandscape() {
        return (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        App app = (App) getApplication();
        if (app != null) {
            if (!app.getEventBus().isRegistered(this)) {
                app.getEventBus().register(this);
            }
        }

        //something weird here causing the search icon to show on the top left
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mainViewPager.getCurrentItem() != SEARCH_PAGER_POSITION) {
                    searchFilterFab.hide();
                }
            }
        }, 200);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App app = (App) getApplication();
        if (app != null) {
            app.getEventBus().unregister(this);
        }
    }
}