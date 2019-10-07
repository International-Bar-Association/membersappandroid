package com.ibamembers.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.ibamembers.R;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

public class DialogManager {

    public enum EditProfile {
        EDIT_BIO,
        EDIT_PROFILE_PICTURE;
    }

    public enum UserInteraction {
        PROFILE_PICTURE,
        BIOGRAPHY,
        SAVING,
        LOADING_PROFILE,
        SEARCHING;

        public static String messageFor(@NonNull Context context, @NonNull UserInteraction userInteraction) {
            Resources resources = context.getResources();

            switch (userInteraction) {
                case PROFILE_PICTURE:
                    return resources.getString(R.string.dialog_manager_no_internet_connection_dialog_message_profile_picture);
                case BIOGRAPHY:
                    return resources.getString(R.string.dialog_manager_no_internet_connection_dialog_message_bio);
                case SAVING:
                    return resources.getString(R.string.dialog_manager_no_internet_connection_dialog_message_saving);
                case LOADING_PROFILE:
                    return resources.getString(R.string.dialog_manager_no_internet_connection_dialog_message_loading);
                case SEARCHING:
                    return resources.getString(R.string.dialog_manager_no_internet_connection_dialog_message_search);
            }

            return null;
        }
    }

    public void showNoInternetConnectionDialog(@NonNull Context context, @NonNull UserInteraction userInteraction) {
        showNoInternetConnectionDialog(context, userInteraction, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
            }
        });
    }

    public void showNoInternetConnectionDialog(@NonNull Context context, @NonNull UserInteraction userInteraction, @NonNull DialogInterface.OnClickListener positiveButtonClickListener, @NonNull DialogInterface.OnClickListener negativeButtonClickListener) {
        Resources resources = context.getResources();
        String title = resources.getString(R.string.dialog_manager_no_internet_connection_dialog_title);
        String message = UserInteraction.messageFor(context, userInteraction);
        String positive;
        String negative;
        boolean isCancellable = true;

        if (userInteraction == UserInteraction.SAVING) {
            positive = resources.getString(R.string.dialog_manager_no_internet_connection_dialog_positive_button_saving);
            negative = resources.getString(R.string.dialog_manager_no_internet_connection_dialog_negative_button_saving);
            isCancellable = false;
        }
        else if (userInteraction == UserInteraction.LOADING_PROFILE) {
            positive = resources.getString(R.string.dialog_manager_no_internet_connection_dialog_positive_button_loading);
            negative = resources.getString(R.string.dialog_manager_no_internet_connection_dialog_negative_button_loading);
            isCancellable = false;
        }
        else {
            positive = resources.getString(R.string.dialog_manager_no_internet_connection_dialog_positive_button_message);
            negative = null;
        }

        createAndShowDialog(context, title, message, positive, negative, positiveButtonClickListener, negativeButtonClickListener, isCancellable);
    }

    private void createAndShowDialog(@NonNull Context context, @Nullable String title, @NonNull String message, @Nullable String positiveButtonTitle, @Nullable String negativeButtonTitle, @NonNull DialogInterface.OnClickListener positiveButtonClickListener, @NonNull DialogInterface.OnClickListener negativeButtonClickListener, boolean isCancellable) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context)
                .setMessage(message);

        if (!TextUtils.isEmpty(title)) {
            alertDialogBuilder.setTitle(title);
        }

        if (!TextUtils.isEmpty(positiveButtonTitle)) {
            alertDialogBuilder.setPositiveButton(positiveButtonTitle, positiveButtonClickListener);
        }

        if (!TextUtils.isEmpty(negativeButtonTitle)) {
            alertDialogBuilder.setNegativeButton(negativeButtonTitle, negativeButtonClickListener);
        }

        if (!isCancellable) {
            alertDialogBuilder.setCancelable(false);
        }

        alertDialogBuilder.show();
    }

    public boolean shouldShowNoBioDialog(App app) throws SQLException {
        SettingDao settingDao = app.getDatabaseHelper().getSettingDao();

        if (settingDao.canPromptBio()) {

            Date lastPromptDate = settingDao.getLastBioPromptDate();

            if (lastPromptDate == null) {
                return true;
            } else if (isDateOlderThan5Days(lastPromptDate)) {
                return true;
            }
        }

        return false;
    }

    public boolean shouldShowNoProfilePictureDialog(App app) throws SQLException {
        SettingDao settingDao = app.getDatabaseHelper().getSettingDao();

        if (settingDao.canPromptProfilePicture()) {

            Date lastPromptDate = settingDao.getLastProfilePicturePromptDate();

            if (lastPromptDate == null) {
                return true;
            } else if (isDateOlderThan5Days(lastPromptDate)) {
                return true;
            }
        }

        return false;
    }

    private boolean isDateOlderThan5Days(Date date) {
        Calendar lastUpdated = Calendar.getInstance();
        lastUpdated.setTime(date);
        Calendar now = Calendar.getInstance();
        long differenceInMillis = now.getTimeInMillis() - lastUpdated.getTimeInMillis();
        long differenceInDays = (differenceInMillis) / 1000L / 60L / 60L / 24L;

        return differenceInDays > 5;
    }

    public void showNoBioDialog(final App app, Context context) throws SQLException {
        final SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
        settingDao.setLastBioPromptDate(new Date());
        Resources resources = app.getResources();

        new AlertDialog.Builder(context)
                .setMessage(resources.getString(R.string.missing_info_prompt_bio_message))
                .setPositiveButton(resources.getString(R.string.missing_info_prompt_bio_positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        app.getEventBus().post(EditProfile.EDIT_BIO);
                    }
                })
                .setNeutralButton(resources.getString(R.string.missing_info_prompt_bio_neutral), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setNegativeButton(resources.getString(R.string.missing_info_prompt_bio_negative), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            settingDao.setPromptProfilePicture(false);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .show();
    }

    public void showNoProfilePictureDialog(final App app, Context context) throws SQLException {
        final SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
        settingDao.setLastProfilePicturePromptDate(new Date());
        Resources resources = app.getResources();

        new AlertDialog.Builder(context)
                .setMessage(resources.getString(R.string.missing_info_prompt_profile_message))
                .setPositiveButton(resources.getString(R.string.missing_info_prompt_profile_positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        app.getEventBus().post(EditProfile.EDIT_PROFILE_PICTURE);
                    }
                })
                .setNeutralButton(resources.getString(R.string.missing_info_prompt_profile_neutral), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setNegativeButton(resources.getString(R.string.missing_info_prompt_profile_negative), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            settingDao.setPromptBio(false);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .show();
    }
}
