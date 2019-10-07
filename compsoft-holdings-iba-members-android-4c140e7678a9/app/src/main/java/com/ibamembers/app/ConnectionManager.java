package com.ibamembers.app;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.birbit.android.jobqueue.TagConstraint;
import com.ibamembers.R;
import com.ibamembers.login.LoginActivity;

import java.sql.SQLException;

import static com.ibamembers.content.job.GetContentLibraryJob.CONTENT_LIBRARY_JOB_TAG;
import static com.ibamembers.messages.job.GetNormalMessagesJob.MESSAGE_JOB_TAG;

public class ConnectionManager {

    public boolean canConnectToInternet(@NonNull Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void sessionExpired(@NonNull Context context, @NonNull App app) {
        Toast.makeText(context, context.getResources().getString(R.string.session_expired_message),
                Toast.LENGTH_LONG).show();
        try {
            logout(context, app);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void logout(@NonNull Context context, App app) throws SQLException {
        SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
        settingDao.setUserRegistered(false);
        settingDao.setLoginDate(null);

        //clear conference
        settingDao.setConferenceId(-1);
        settingDao.setConferenceIsShow(false);
        settingDao.setConferenceUrl(null);
        settingDao.setConferenceStartDate(null);
        settingDao.setConferenceFinishDate(null);

        // clear search filters
        settingDao.setSearchFirstName("");
        settingDao.setSearchLastName("");
        settingDao.setSearchFirmName("");
        settingDao.setSearchCity("");
        settingDao.setSearchCountry("");
        settingDao.setIdSearchCommitteeId(-1);
        settingDao.setSearchAreaOfPracticeId(-1);

        app.getJobManager(App.JobQueueName.Network).cancelJobsInBackground(null, TagConstraint.ANY, CONTENT_LIBRARY_JOB_TAG);
        app.getJobManager(App.JobQueueName.Network).cancelJobsInBackground(null, TagConstraint.ANY, MESSAGE_JOB_TAG);

        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

}
