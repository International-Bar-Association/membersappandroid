package com.ibamembers.profile;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.ibamembers.R;
import com.ibamembers.app.App;
import com.ibamembers.app.ConnectionManager;
import com.ibamembers.app.DataManager;
import com.ibamembers.app.DialogManager;
import com.ibamembers.app.PhotoPickerFragment;
import com.ibamembers.app.SettingDao;
import com.ibamembers.login.LoginJob;
import com.ibamembers.profile.job.UploadProfilePictureJob;
import com.ibamembers.search.database.AreaOfPractice;
import com.ibamembers.search.database.AreaOfPracticeDao;
import com.ibamembers.search.database.Committee;
import com.ibamembers.search.database.CommitteeDao;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserProfileFragment extends PhotoPickerFragment {

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.scroll_view)
    protected NestedScrollView profileScrollView;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.profile_picture_progress_bar)
    protected ProgressBar profilePictureProgressBar;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.profile_picture)
    protected ImageView profilePicture;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.profile_user_name)
    protected TextView userNameText;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.profile_firm_name)
    protected TextView firmName;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.profile_job_role)
    protected TextView jobRole;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.profile_address)
    protected TextView address;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.profile_email)
    protected TextView email;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.profile_phone_number)
    protected TextView phoneNumber;

//    @SuppressWarnings("WeakerAccess")
//    @BindView(R.id.profile_message_layout)
//    protected LinearLayout messageLayout;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.profile_bio)
    protected TextView bio;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.profile_committees)
    protected TextView committees;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.profile_areas_of_practice)
    protected TextView areasOfPractices;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.profile_profile_visibility)
    protected TextView profileVisibility;

//	@SuppressWarnings("WeakerAccess")
//	@BindView(R.id.attending_members_layout)
//	protected RelativeLayout attendingMembersBadge;

    private UserProfileFragmentListener userProfileFragmentListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.profile_fragment, container, false);
        ButterKnife.bind(this, view);

        App app = getApp();
        if (app != null) {
            try {
                fillView(app);
                //refreshDataIfNeeded(app); //Data is already refreshed on app launch
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }

        return view;
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.profile_profile_visibility)
    public void editBioClicked() {
        App app = getApp();
        Activity activity = getActivity();
        if (app != null && activity != null) {
            ConnectionManager connectionManager = app.getConnectionManager();
            if (connectionManager.canConnectToInternet(activity)) {
                userProfileFragmentListener.editBio();
            } else {
                showCantConnectToInternetDialog(DialogManager.UserInteraction.BIOGRAPHY);
            }
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.profile_change_picture_icon)
    public void changePictureClicked() {
        App app = getApp();
        Activity activity = getActivity();
        if (app != null && activity != null) {
            ConnectionManager connectionManager = app.getConnectionManager();
            if (connectionManager.canConnectToInternet(activity)) {
                editPicture();
            } else {
                showCantConnectToInternetDialog(DialogManager.UserInteraction.PROFILE_PICTURE);
            }
        }
    }

    protected void fillView(App app) throws SQLException, IOException {
        Activity activity = getActivity();
        if (activity != null && app != null) {
            boolean promptDialogBeenShown = false;
            SettingDao settingDao = app.getDatabaseHelper().getSettingDao();

            byte[] imageData = settingDao.getCachedImageData();
            String imagePath = settingDao.getCachedProfilePictureUrl();
            String fullName = app.getDataManager().getFullName(settingDao);
            String firmName = settingDao.getCachedFirmName();
            String jobRole = settingDao.getCachedJobPosition();
            String address = settingDao.getCachedAddressLines();
            String email = settingDao.getCachedEmail();
            String phoneNumber = settingDao.getCachedPhone();
            String bio = settingDao.getCachedBiography();
            boolean _public = settingDao.isPublic();
            boolean isAttendingConference = settingDao.getConferenceIsShow();
            List<String> committees = getCommittees(settingDao.getCachedCommitteeIds());
            List<String> areaOfPractices = getAreaOfPractices(settingDao.getCachedAreaOfPracticeIds());

            Resources resources = app.getResources();
            DialogManager dialogManager = app.getDialogManager();

            if (imagePath != null) {
                loadImage(imagePath, profilePicture);
            } else {
                setNoPicture();
                if (dialogManager.shouldShowNoProfilePictureDialog(app)) {
                    promptDialogBeenShown = true;
                    dialogManager.showNoProfilePictureDialog(app, activity);
                }
            }

            //attendingMembersBadge.setVisibility(View.INVISIBLE);

            this.userNameText.setText(!TextUtils.isEmpty(fullName) ? fullName : resources.getString(R.string.profile_no_name));

            this.firmName.setText(!TextUtils.isEmpty(firmName) ? firmName : "");

            this.jobRole.setText(!TextUtils.isEmpty(jobRole) ? jobRole : "");

            this.address.setText(!TextUtils.isEmpty(address) ? address : "");

            this.email.setText(!TextUtils.isEmpty(email) ? email : resources.getString(R.string.profile_no_content));

            this.phoneNumber.setText(!TextUtils.isEmpty(phoneNumber) ? phoneNumber : resources.getString(R.string.profile_no_content));


            if (!TextUtils.isEmpty(bio)) {
                this.bio.setText(bio);
            } else {
                this.bio.setText(resources.getString(R.string.profile_no_content));

                if (!promptDialogBeenShown) {
                    if (dialogManager.shouldShowNoBioDialog(app)) {
                        dialogManager.showNoBioDialog(app, activity);
                    }
                }
            }

            if (_public) {
                profileVisibility.setText(resources.getString(R.string.profile_visibility_state_public));
                profileVisibility.setTextColor(ContextCompat.getColor(activity, R.color.profileIsPublic));
            } else {
                profileVisibility.setText(resources.getString(R.string.profile_visibility_state_private));
                profileVisibility.setTextColor(ContextCompat.getColor(activity, R.color.profileHeaderText));
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

    protected void setNoPicture() {
        Activity activity = getActivity();
        if (activity != null) {
            profilePictureProgressBar.setVisibility(View.INVISIBLE);
            profilePicture.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.profile_image_placeholder));
        }
    }

    protected void loadImage(byte[] imageData, ImageView imageView) {
        Activity activity = getActivity();
        if (activity != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            profilePictureProgressBar.setVisibility(View.INVISIBLE);
            imageView.setImageBitmap(bitmap);
        }
    }

    protected void loadImage(String imageUrl, ImageView imageView) throws IOException {
        Activity activity = getActivity();

        if (imageUrl.equals("N/A")) {
            setNoPicture();
            return;
        }

        if (activity != null) {
            profilePictureProgressBar.setVisibility(View.VISIBLE);


            RequestOptions options = new RequestOptions();
            options.centerCrop();
            options.diskCacheStrategy(DiskCacheStrategy.NONE);

            RequestListener requestListener = new RequestListener() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                    profilePictureProgressBar.setVisibility(View.INVISIBLE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                    profilePictureProgressBar.setVisibility(View.INVISIBLE);
                    return false;
                }
            };

            Glide.with(activity)
                    .load(imageUrl)
                    .apply(options)
                    .listener(requestListener)
                    .into(imageView);
        }
    }

    protected String formatListIntoLineBrokenString(List<String> lineBreakedStrings) {
        String finalResult = "";

        for (int i = 0; i < lineBreakedStrings.size(); i++) {
            finalResult += "\u2022";
            finalResult += lineBreakedStrings.get(i);

            if (i != lineBreakedStrings.size() - 1) {
                finalResult += "\n\n";
            }
        }

        return finalResult;
    }

    protected List<String> getCommittees(String formattedCommitteeString) throws SQLException {
        App app = getApp();
        if (app != null) {
            CommitteeDao committeeDao = app.getDatabaseHelper().getCommitteeDao();
            List<String> committees = new ArrayList<>();

            long[] committeeIds = parseArrayString(formattedCommitteeString);
            for (long committeeId : committeeIds) {
                Committee committee = committeeDao.queryForId(committeeId);
                if (committee != null) {
                    committees.add(committee.getName());
                }
            }

            return committees;
        }
        return null;
    }

    protected List<String> getAreaOfPractices(String formatedAreaOfPracticesString) throws SQLException {
        App app = getApp();
        if (app != null) {
            AreaOfPracticeDao areaOfPracticeDao = app.getDatabaseHelper().getAreaOfPracticeDao();
            List<String> areaOfPractices = new ArrayList<>();

            long[] areaOfPracticeIds = parseArrayString(formatedAreaOfPracticesString);
            for (long areaOfPracticeId : areaOfPracticeIds) {
                AreaOfPractice areaOfPractice = areaOfPracticeDao.queryForId(areaOfPracticeId);
                if (areaOfPractice != null) {
                    areaOfPractices.add(areaOfPractice.getName());
                }
            }

            return areaOfPractices;
        }
        return null;
    }

    // breaks up the committee or areaPoints of practice String into a List seperated by ","
    private long[] parseArrayString(String arrayString) {
        if (TextUtils.isEmpty(arrayString)) {
            return new long[0];
        }

        String[] r = arrayString.split("[,]");
        long[] result = new long[r.length];

        for (int i = 0; i < r.length; i++) {
            result[i] = Long.parseLong(r[i]);
        }

        return result;
    }

    private void editPicture() {
        startAddPhotoIntent();
    }

    @Override
    public void imagePicked(File file) {
        try {
            Glide.with(this).load(file).into(profilePicture);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), options);
            sendNewImageToServer(file);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void sendNewImageToServer(File file) throws SQLException, IOException {
        App app = getApp();
        if (app != null) {
            app.getJobManager(App.JobQueueName.Network).addJobInBackground(new UploadProfilePictureJob(file));
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(UploadProfilePictureJob.Success success) {
        try {
            refreshProfileData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(UploadProfilePictureJob.Error failure) {
        Log.e("userProfileFragment", failure.getStatus() + " : Failed to Upload image");
    }


    /**
     * Note: Changed from refreshing every 48hours to refresh when the user profile is loaded.
     * This is to allow updating of the conference button.
     */
    private void refreshDataIfNeeded(App app) throws IOException, SQLException {
        DataManager dataManager = app.getDataManager();
        dataManager.refreshData(app);
    }

//    @SuppressWarnings("unused")
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEventMainThread(RefreshJob.Success refreshJobSuccess) {
//        if (userProfileFragmentListener != null) {
//            userProfileFragmentListener.updateConferenceButton();
//        }
//    }

    /**
     * Uses Login call to refresh profile data.
     * When call completes logic will look at all information and if it is different update it.
     *
     * @throws SQLException
     */
    public void refreshProfileData() throws SQLException {
        App app = getApp();
        if (app != null) {
            SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
            app.getJobManager(App.JobQueueName.Network).addJobInBackground(new LoginJob(settingDao.getUsername(), settingDao.getPassword()));
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LoginJob.Success loginJobSuccess) {
        App app = getApp();
        if (app != null) {
            try {
                fillView(app);
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showCantConnectToInternetDialog(DialogManager.UserInteraction userInteraction) {
        Activity activity = getActivity();
        App app = getApp();
        if (app != null && activity != null) {
            DialogManager dialogManager = app.getDialogManager();
            dialogManager.showNoInternetConnectionDialog(activity, userInteraction);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            userProfileFragmentListener = (UserProfileFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement UserProfileFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        userProfileFragmentListener = null;
    }

    public interface UserProfileFragmentListener {
        void editBio();
        void updateConferenceButton();
    }
}
