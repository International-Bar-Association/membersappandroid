package com.ibamembers.profile;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.ibamembers.R;
import com.ibamembers.app.App;
import com.ibamembers.app.ConnectionManager;
import com.ibamembers.app.DialogManager;
import com.ibamembers.app.EventBusFragment;
import com.ibamembers.app.SettingDao;
import com.ibamembers.profile.job.SetBiographyJob;
import com.ibamembers.profile.job.ToggleProfileIsPublicJob;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.sql.SQLException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditBioFragment extends EventBusFragment {

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.bio_make_profile_public_switch)
    protected Switch publicSwitch;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.bio_edit)
    protected EditText bioEdit;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.bio_character_count)
    protected TextView characterCount;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.edit_bio_progress_bar_background)
    protected View progressBarBackground;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.edit_bio_progress_bar)
    protected ProgressBar progressBar;

    private EditBioListener editBioListener;
    private boolean isUpdatingData;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_bio_fragment, container, false);
        ButterKnife.bind(this, view);

        try {
            fillViews();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        bioEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                App app = getApp();
                if (app != null) {
                    int charLimit = app.getResources().getInteger(R.integer.edit_bio_character_limit);
                    int stringLength = s.length();
                    if (stringLength != 0 && stringLength - charLimit < 2) {
                        if (bioEdit.length() > charLimit) {
                            String txt = s.toString();
                            bioEdit.setTextKeepState(txt.substring(0, txt.length() - 1));
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                App app = getApp();
                if (app != null) {
                    int charLimit = app.getResources().getInteger(R.integer.edit_bio_character_limit);
                    String bioEditText = bioEdit.getText().toString();

                    // handles user pasting text and exceeding character limit
                    if (bioEditText.length() > charLimit) {
                        String shortenedString = bioEditText.substring(0, charLimit);
                        bioEdit.setText(shortenedString);
                        bioEdit.setSelection(bioEdit.length());
                    }

                    refreshCharacterCount();

                }
            }
        });

        return view;
    }

    private void fillViews() throws SQLException {
        App app = getApp();
        if (app != null) {
            SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
            boolean isPublic = settingDao.isPublic();
            String bio = settingDao.getCachedBiography();

            publicSwitch.setChecked(isPublic);

            if (!TextUtils.isEmpty(bio)) {
                bioEdit.setText(bio);
                bioEdit.setSelection(bio.length());
            }

            refreshCharacterCount();
        }
    }

    private void refreshCharacterCount() {
        Activity activity = getActivity();
        App app = getApp();
        if (app != null && activity != null) {
            Resources resources = app.getResources();

            int charLimit = resources.getInteger(R.integer.edit_bio_character_limit);
            int charCount = bioEdit.getText().length();
            characterCount.setText(resources.getString(R.string.bio_character_count, charCount));

            if (charCount == charLimit) {
                characterCount.setTextColor(ContextCompat.getColor(activity, R.color.editBioCharacterCountLimitReached));
            } else {
                characterCount.setTextColor(ContextCompat.getColor(activity, R.color.editBioCharacterCount));
            }
        }
    }

    // called by whatever controls the Fragment. Usually called when user tried to exit via back press.
    public void saveProfileData() {
        setIsLoading(true);
        App app = getApp();
        Activity activity = getActivity();
        if (app != null && activity != null) {
            ConnectionManager connectionManager = app.getConnectionManager();

            if (!connectionManager.canConnectToInternet(activity)) {
                cannotSaveProfileInformation(app, activity);
            } else {
                try {
                    updateProfilePrivacy();
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateProfilePrivacy() throws SQLException, IOException {
        App app = getApp();
        if (app != null) {
            SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
            boolean isPublic = settingDao.isPublic();
            if (publicSwitch.isChecked() == isPublic) {
                // no need to call API, progress to updating profile biography
                updateProfileBio();
            }
            else {
                app.getJobManager(App.JobQueueName.Network).addJobInBackground(new ToggleProfileIsPublicJob());
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ToggleProfileIsPublicJob.Success success) {
        try {
            updateProfileBio();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ToggleProfileIsPublicJob.Error error) {
        App app = getApp();
        Activity activity = getActivity();
        if (app != null && activity != null) {
            if (error.getStatus() == activity.getResources().getInteger(R.integer.session_token_invalid_code)) {
                app.getConnectionManager().sessionExpired(activity, app);
            } else {
                cannotSaveProfileInformation(app, activity);
            }
        }
    }

    private void updateProfileBio() throws SQLException, IOException {
        App app = getApp();
        if (app != null) {
            SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
            String cachedBio = settingDao.getCachedBiography();
            String bioText = bioEdit.getText().toString();

            if (bioText.equals(cachedBio)) {
                // no need to call API, user is finished editing
                setIsLoading(false);
                editBioListener.userFinishedEditing();
            }
            else {
                app.getJobManager(App.JobQueueName.Network).addJobInBackground(new SetBiographyJob(bioText));
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SetBiographyJob.Success success) {
        setIsLoading(false);
        editBioListener.userFinishedEditing();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SetBiographyJob.Error error) {
        App app = getApp();
        Activity activity = getActivity();
        if (app != null && activity != null) {
            if (error.getStatus() == activity.getResources().getInteger(R.integer.session_token_invalid_code)) {
                app.getConnectionManager().sessionExpired(activity, app);
            } else {
                cannotSaveProfileInformation(app, activity);
            }
        }
    }

    private void cannotSaveProfileInformation(App app, Activity activity) {
        DialogManager dialogManager = app.getDialogManager();
        dialogManager.showNoInternetConnectionDialog(activity, DialogManager.UserInteraction.SAVING, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveProfileData();
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setIsLoading(false);
                editBioListener.userFinishedEditing();
            }
        });
    }

    private void setIsLoading(boolean isLoading) {
        isUpdatingData = isLoading;
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            //progressBarBackground.setVisibility(View.VISIBLE);
        }
        else {
            progressBar.setVisibility(View.INVISIBLE);
            //progressBarBackground.setVisibility(View.INVISIBLE);
        }
    }

    public boolean getIsUpdatingData() {
        return isUpdatingData;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            editBioListener = (EditBioListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement EditBioListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        editBioListener = null;
    }

    public interface EditBioListener {
        void userFinishedEditing();
    }
}
