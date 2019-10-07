package com.ibamembers.search;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ibamembers.R;
import com.ibamembers.app.App;
import com.ibamembers.app.DataManager;
import com.ibamembers.app.DialogManager;
import com.ibamembers.profile.UserProfileFragment;
import com.ibamembers.profile.db.ProfileMessageDao;
import com.ibamembers.profile.job.GetProfileJob;
import com.ibamembers.profile.job.ProfileModel;
import com.ibamembers.search.favourites.ProfileItem;
import com.ibamembers.search.favourites.ProfileItemDao;
import com.ibamembers.search.favourites.ProfileSnippet;
import com.ibamembers.search.favourites.ProfileSnippetDao;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import butterknife.BindView;

public class SearchProfileFragment extends UserProfileFragment implements UserProfileFragment.UserProfileFragmentListener {

    public static final String KEY_SEARCH_PROFILE_ID = "KEY_SEARCH_PROFILE_ID";
    public static final String KEY_USER_NAME = "KEY_USER_NAME";
    public static final String KEY_FROM_MESSAGE_THREAD = "KEY_FROM_MESSAGE_THREAD";
    public static final String KEY_JOB_TAG = "KEY_JOB_TAG";

    public static Bundle getSearchProfileFragmentArgs(int profileId, String userName, boolean isFromMessageThread, String jobTag) {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_SEARCH_PROFILE_ID, profileId);
        bundle.putString(KEY_USER_NAME, userName);
        bundle.putBoolean(KEY_FROM_MESSAGE_THREAD, isFromMessageThread);
        bundle.putString(KEY_JOB_TAG, jobTag);
        return bundle;
    }

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.profile_change_picture_icon)
    protected ImageView profilePictureIcon;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.profile_loading_view)
    protected RelativeLayout profileLoadingView;

    private ProfileModel profileModel;
    private int profileId;
    private String userName;
    private SearchProfileFragmentListener searchProfileFragmentListener;
    private MenuItem favouriteMenuItem;
    private boolean profileFavourited;
    private String formattedProfileAddress;
    private String jobTag;
    private boolean localProfileContentLoaded;
    private boolean isFromMessageThread;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = super.onCreateView(inflater, container, savedInstanceState);

        localProfileContentLoaded = false;

        App app = getApp();
        Activity activity = getActivity();
        if (activity != null && app != null) {
            profilePictureIcon.setVisibility(View.INVISIBLE);
            profileVisibility.setVisibility(View.INVISIBLE);

            Bundle args = getArguments();
            profileId = args.getInt(KEY_SEARCH_PROFILE_ID, -1);
            jobTag = args.getString(KEY_JOB_TAG);
            userName = args.getString(KEY_USER_NAME);
            isFromMessageThread = args.getBoolean(KEY_FROM_MESSAGE_THREAD);

            String toolbarTitle;
            if (shouldUseNameForToolbar(userName)) {
                toolbarTitle = userName;
            } else {
                toolbarTitle = activity.getResources().getString(R.string.toolbar_title_user_profile);
            }

            searchProfileFragmentListener.changeToolbarTitle(toolbarTitle, jobTag);

            try {
                ProfileSnippetDao profileSnippetDao = app.getDatabaseHelper().getProfileSnippetDao();
                profileFavourited = profileSnippetDao.isProfileFavourited(profileId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (profileId != -1) {
            try {
                startLoadingProfile(profileId, jobTag);
            } catch (SQLException | IOException e) {
                e.printStackTrace();
                errorLoadingProfile();
            }
        }

        return view;
    }

    private boolean shouldUseNameForToolbar(String name) {
        if (TextUtils.isEmpty(name) || name.length() > 20) {
            return false;
        }
        return true;
    }

//    @SuppressWarnings("unused")
//    @OnClick(R.id.profile_message)
//    public void messageClicked() {
//        if (!isFromMessageThread) {
//            startActivity(ProfileMessageActivity.getProfileMessageActivity(getActivity(), profileId, userName, false, false));
//        } else {
//            //this fragment was loaded from the message thread so we finish the activity if message is clicked
//            Activity activity = getActivity();
//            if (activity != null) {
//                activity.finish();
//            }
//        }
//    }

    public boolean isProfileIdTheSame(int idIn) {
        return profileModel.getUserId() == idIn;
    }

    /**
     * Load profile from ProfileItemDao and call api to update profile
     * @param profileId
     * @param jobTag
     * @throws SQLException
     * @throws IOException
     */
    private void startLoadingProfile(int profileId, String jobTag) throws SQLException, IOException {
        App app = getApp();
        if (app != null) {
            ProfileItemDao profileItemDao = app.getDatabaseHelper().getProfileItemDao();
            ProfileItem profileItem = profileItemDao.queryForId((long)profileId);

            profileLoadingView.setVisibility(View.VISIBLE);

            if (profileItem != null) {
                localProfileContentLoaded = true;
                fillViewWithProfileItem(profileItem, app);
            }

            app.getJobManager(App.JobQueueName.Network).addJobInBackground(new GetProfileJob(profileId, jobTag));
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(GetProfileJob.Success success) {
        if (success.getJobTag().equals(jobTag)) {
            App app = getApp();
            if (app != null) {
                this.profileModel = success.getProfileModel();
                profileLoadingView.setVisibility(View.INVISIBLE);
                searchProfileFragmentListener.searchProfileFinishedLoading(jobTag);

                //If profile is attending conference, then we save an instance to the profileMessagesDao
                if (success.getProfileModel().getCurrentlyAttendingConference() != null && success.getProfileModel().getCurrentlyAttendingConference() > 0) {
                    try {
                        ProfileMessageDao profileMessageDao = getApp().getDatabaseHelper().getProfileMessageDao();
                        ProfileModel profile = success.getProfileModel();
                        profileMessageDao.saveResponseAsProfileMessage(profile.getUserId(),
                                app.getDataManager().getFullName(profile.getFirstName(), profile.getLastName()),
                                profile.getProfilePictureUrl(),
                                "");

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    fillView(app);
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(GetProfileJob.GetProfileJobError error) {
        App app = getApp();
        Activity activity = getActivity();
        if (app != null && activity != null && !localProfileContentLoaded) {
            if (error.getStatus() == activity.getResources().getInteger(R.integer.session_token_invalid_code)) {
                app.getConnectionManager().sessionExpired(activity, app);
            } else {
                errorLoadingProfile();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_profile_menu, menu);
        favouriteMenuItem = menu.findItem(R.id.action_favourite);
        setUpFavouriteMenuItem(favouriteMenuItem);
        favouriteMenuItem.setVisible(false);
    }

    public void showFavouriteMenuIcon() {
        if (favouriteMenuItem != null) {
            favouriteMenuItem.setVisible(true);
        }
    }

    public float getProfileId() {
        return profileId;
    }

    public void setUpFavouriteMenuItem(MenuItem favouriteMenuItem) {
        Activity activity = getActivity();
        if (activity != null) {
            this.favouriteMenuItem = favouriteMenuItem;
            if (profileFavourited) {
                favouriteMenuItem.setIcon(ContextCompat.getDrawable(activity, R.drawable.favourited));
            } else {
                favouriteMenuItem.setIcon(ContextCompat.getDrawable(activity, R.drawable.fav));
            }

            favouriteMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    App app = getApp();
                    if (app != null && SearchProfileFragment.this.profileModel != null) {
                        try {
                            setProfileFavourited(!profileFavourited);
                            searchProfileFragmentListener.favouriteStatusChanged(profileId, !profileFavourited);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    return true;
                }
            });
        }
    }

    private void fillViewWithProfileItem(ProfileItem profileItem, App app) throws SQLException {
        if (profileItem != null) {
            Activity activity = getActivity();
            if (activity != null) {
                byte[] profilePictureData = profileItem.getImageData();
                String fullName = app.getDataManager().getFullName(profileItem.getFirstName(), profileItem.getLastName());
                String firmName = profileItem.getFirmName();
                String jobRole = profileItem.getJobPosition();
                String formattedProfileAddress = profileItem.getAddress();
                String email = profileItem.getEmail();
                String phoneNumber = profileItem.getPhone();
                String bio = profileItem.getBio();
                String imagePath = profileItem.getPictureProfileUrl();

                List<String> committees = getCommittees(profileItem.getCommittees());
                List<String> areaOfPractices = getAreaOfPractices(profileItem.getAreaOfPractices());

                Resources resources = app.getResources();

                if (profilePictureData != null) {
                    try {
                        loadImage(imagePath, profilePicture);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    setNoPicture();
                }

                if (!TextUtils.isEmpty(fullName)) {
                    this.userNameText.setText(fullName);
                } else {
                    this.userNameText.setText(resources.getString(R.string.profile_no_name));
                }

                if (!TextUtils.isEmpty(firmName)) {
                    this.firmName.setText(firmName);
                } else {
                    this.firmName.setText("");
                }

                if (!TextUtils.isEmpty(jobRole)) {
                    this.jobRole.setText(jobRole);
                } else {
                    this.jobRole.setText("");
                }

                if (!TextUtils.isEmpty(formattedProfileAddress)) {
                    this.address.setText(formattedProfileAddress);
                } else {
                    this.address.setText("");
                }

                if (!TextUtils.isEmpty(email)) {
                    this.email.setText(email);
                } else {
                    this.email.setText(resources.getString(R.string.profile_no_content));
                }

                if (!TextUtils.isEmpty(phoneNumber)) {
                    this.phoneNumber.setText(phoneNumber);
                } else {
                    this.phoneNumber.setText(resources.getString(R.string.profile_no_content));
                }

                if (!TextUtils.isEmpty(phoneNumber)) {
                    this.phoneNumber.setText(phoneNumber);
                } else {
                    this.phoneNumber.setText(resources.getString(R.string.profile_no_content));
                }

                if (!TextUtils.isEmpty(bio)) {
                    this.bio.setText(bio);
                } else {
                    this.bio.setText(resources.getString(R.string.profile_no_content));
                }

                if (committees != null) {
                    if (committees.size() == 0) {
                        this.committees.setText(resources.getString(R.string.profile_no_committees));
                    } else {
                        this.committees.setText(formatListIntoLineBrokenString(committees));
                    }
                }

                if (areaOfPractices != null) {
                    if (areaOfPractices.size() == 0) {
                        this.areasOfPractices.setText(resources.getString(R.string.profile_no_areas_of_practices));
                    } else {
                        this.areasOfPractices.setText(formatListIntoLineBrokenString(areaOfPractices));
                    }
                }
            }
        }
    }

    @Override
    protected void fillView(App app) throws SQLException, IOException {
        if (profileModel != null) {
            Activity activity = getActivity();
            if (activity != null) {
                String profilePictureUrl = profileModel.getProfilePictureUrl();
                String fullName = app.getDataManager().getFullName(profileModel.getFirstName(), profileModel.getLastName());
                String firmName = profileModel.getFirmName();
                String jobRole = profileModel.getJobPosition();
                String[] addressLines = profileModel.getAddress().getAddressLines();
                String city = profileModel.getAddress().getCity();
                String country = profileModel.getAddress().getCountry();
                String zip = profileModel.getAddress().getPcZip();
                String email = profileModel.getEmail();
                String phoneNumber = profileModel.getPhone();
                String bio = profileModel.getBiography();

                DataManager dataManager = app.getDataManager();
                List<String> committees = getCommittees(dataManager.formatFloatArray(profileModel.getCommitteeIds()));
                List<String> areaOfPractices = getAreaOfPractices(dataManager.formatFloatArray(profileModel.getAreasOfPracticeIds()));
                formattedProfileAddress = dataManager.formatAddressLines(addressLines, city, zip, country);

                Resources resources = app.getResources();

                if (!TextUtils.isEmpty(profilePictureUrl)) {
                    loadImage(profilePictureUrl, profilePicture);
                } else {
                    setNoPicture();
                }

                if (!TextUtils.isEmpty(fullName)) {
                    this.userNameText.setText(fullName);
                } else {
                    this.userNameText.setText(resources.getString(R.string.profile_no_name));
                }

                if (!TextUtils.isEmpty(firmName)) {
                    this.firmName.setText(firmName);
                } else {
                    this.firmName.setText("");
                }

                if (!TextUtils.isEmpty(jobRole)) {
                    this.jobRole.setText(jobRole);
                } else {
                    this.jobRole.setText("");
                }

                if (!TextUtils.isEmpty(formattedProfileAddress)) {
                    this.address.setText(formattedProfileAddress);
                } else {
                    this.address.setText("");
                }

                if (!TextUtils.isEmpty(email)) {
                    this.email.setText(email);
                } else {
                    this.email.setText(resources.getString(R.string.profile_no_content));
                }

                if (!TextUtils.isEmpty(phoneNumber)) {
                    this.phoneNumber.setText(phoneNumber);
                } else {
                    this.phoneNumber.setText(resources.getString(R.string.profile_no_content));
                }

                if (!TextUtils.isEmpty(bio)) {
                    this.bio.setText(bio);
                } else {
                    this.bio.setText(resources.getString(R.string.profile_no_content));
                }

                if (committees != null) {
                    if (committees.size() == 0) {
                        this.committees.setText(resources.getString(R.string.profile_no_committees));
                    } else {
                        this.committees.setText(formatListIntoLineBrokenString(committees));
                    }
                }

                if (areaOfPractices != null) {
                    if (areaOfPractices.size() == 0) {
                        this.areasOfPractices.setText(resources.getString(R.string.profile_no_areas_of_practices));
                    } else {
                        this.areasOfPractices.setText(formatListIntoLineBrokenString(areaOfPractices));
                    }
                }
            }
        }
    }

    private void setProfileFavourited(boolean isFavourited) throws SQLException {
        App app = getApp();
        Activity activity = getActivity();
        if (app != null && activity != null) {

            profileFavourited = isFavourited;
            ProfileSnippetDao profileSnippetDao = app.getDatabaseHelper().getProfileSnippetDao();
            ProfileItemDao profileItemDao = app.getDatabaseHelper().getProfileItemDao();
            DataManager dataManager = app.getDataManager();

            if (isFavourited) {
                ProfileSnippet profileSnippet = new ProfileSnippet(profileModel.getUserId());
                profileSnippet.setFirstName(profileModel.getFirstName());
                profileSnippet.setLastName(profileModel.getLastName());
                profileSnippet.setFirmName(profileModel.getFirmName());
                profileSnippet.setJobPosition(profileModel.getJobPosition());
                profileSnippet.setAddress(formattedProfileAddress);
                profileSnippet.setImageData(profileModel.getImageData());
                profileSnippet.setProfilePicture(profileModel.getProfilePictureUrl());
                profileSnippet.setCurrentlyAttendingConference(profileModel.getCurrentlyAttendingConference());
                profileSnippetDao.addProfileToFavourites(profileSnippet);

                ProfileItem profileItem = new ProfileItem(profileModel.getUserId());
                profileItem.setFirstName(profileModel.getFirstName());
                profileItem.setLastName(profileModel.getLastName());
                profileItem.setFirmName(profileModel.getFirmName());
                profileItem.setJobPosition(profileModel.getJobPosition());
                profileItem.setAddress(formattedProfileAddress);
                profileItem.setImageData(profileModel.getImageData());
                profileItem.setPictureProfileUrl(profileModel.getProfilePictureUrl());
                profileItem.setEmail(profileModel.getEmail());
                profileItem.setPhone(profileModel.getPhone());
                profileItem.setBio(profileModel.getBiography());

                float[] committees = profileModel.getCommitteeIds();
                profileItem.setCommittees(dataManager.formatFloatArray(committees));

                float[] areaOfPractices = profileModel.getAreasOfPracticeIds();
                profileItem.setAreaOfPractices(dataManager.formatFloatArray(areaOfPractices));

                profileItemDao.addProfileToFavourites(profileItem);

                favouriteMenuItem.setIcon(ContextCompat.getDrawable(activity, R.drawable.favourited));
            } else {
                profileSnippetDao.removeProfileFromFavourites((long) profileModel.getUserId());
                profileItemDao.removeProfileFromFavourites((long) profileModel.getUserId());

                favouriteMenuItem.setIcon(ContextCompat.getDrawable(activity, R.drawable.fav));
            }
        }
    }

    private void errorLoadingProfile() {
        Activity activity = getActivity();
        App app = getApp();
        if (app != null && activity != null) {
            DialogManager dialogManager = app.getDialogManager();
            dialogManager.showNoInternetConnectionDialog(activity, DialogManager.UserInteraction.SAVING, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // retry
                    try {
                        startLoadingProfile(profileId, jobTag);
                    } catch (SQLException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // leave
                    searchProfileFragmentListener.leaveProfileFragment();
                }
            });
        }
    }

    public void favouriteHasChanged(float userId) {
        if (userId == profileId) {
            profileFavourited = false;
        }
    }

    @Override
    public void refreshProfileData() throws SQLException {
        // do nothing
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
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            searchProfileFragmentListener = (SearchProfileFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement SearchProfileFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        searchProfileFragmentListener = null;
    }

    public interface SearchProfileFragmentListener {
        void leaveProfileFragment();

        void favouriteStatusChanged(float userId, boolean wasRemoved);

        void searchProfileFinishedLoading(String tag);

        void changeToolbarTitle(String title, String jobTag);
    }
}
