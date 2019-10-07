package com.ibamembers.profile.job;

import android.support.annotation.NonNull;

import com.birbit.android.jobqueue.RetryConstraint;
import com.ibamembers.app.JobConfig;
import com.ibamembers.app.RestClient;
import com.ibamembers.app.SettingDao;
import com.ibamembers.app.service.BaseJob;
import com.ibamembers.search.database.AreaOfPractice;
import com.ibamembers.search.database.AreaOfPracticeDao;
import com.ibamembers.search.database.Attendee;
import com.ibamembers.search.database.AttendeeDao;
import com.ibamembers.search.database.Committee;
import com.ibamembers.search.database.CommitteeDao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class RefreshJob extends BaseJob {

    public RefreshJob() throws IOException {
        super(JobConfig.PRIORITY_NORMAL);
    }

    @Override
    public void onRun() throws Throwable {
        if (app != null) {
            RestClient restClient = new RestClient(app);

            try {
                Call<RefreshResponse> responseCall = restClient.getApiService().refresh();
                Response<RefreshResponse> response = responseCall.execute();

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        saveNewData(response.body());
                        app.getEventBus().post(new Success());
                    }
                } else {
                    app.getEventBus().post(new Error(response.message(), response.code()));
                }
            } catch (IOException e) {
                rerunJobTillLimit();
            }
        }
    }

    private boolean hasRefreshedBefore() {
        if (app != null) {
            try {
                AreaOfPracticeDao areaOfPracticeDao = app.getDatabaseHelper().getAreaOfPracticeDao();
                return !areaOfPracticeDao.queryForAll().isEmpty();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private void saveNewData(RefreshResponse refreshResponse) throws SQLException {
        if (app != null) {
            AreaOfPracticeDao areaOfPracticeDao = app.getDatabaseHelper().getAreaOfPracticeDao();
            AreaOfPractice areaOfPractice;

            for (Map.Entry<String, String> entry : refreshResponse.getAreasOfPractice().entrySet()) {
                int id;
                try {
                    id = Integer.parseInt(entry.getKey());
                } catch(NumberFormatException e) {
                    continue;
                }

                areaOfPractice = new AreaOfPractice(id, entry.getValue());
                areaOfPracticeDao.createOrUpdate(areaOfPractice);
            }

            CommitteeDao committeeDao = app.getDatabaseHelper().getCommitteeDao();
            Committee committee;

            for (Map.Entry<String, String> entry : refreshResponse.getCommittees().entrySet()) {
                int id;
                try {
                    id = Integer.parseInt(entry.getKey());
                } catch(NumberFormatException e) {
                    continue;
                }

                committee = new Committee(id, entry.getValue());
                committeeDao.createOrUpdate(committee);
            }

            SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
            RefreshResponse.Conference conference = refreshResponse.getConference();
            if (conference != null) {
                settingDao.setConferenceId(conference.getConferenceId());
                settingDao.setConferenceIsShow(conference.isShowDetails());
                settingDao.setConferenceUrl(conference.getUrl());
                settingDao.setConferenceStartDate(conference.getStart());
                settingDao.setConferenceFinishDate(conference.getFinish());


                app.getDatabaseHelper().clearAttendeesDao();
                if (conference.getAttendees() != null) {

                    //TODO temp
                    AttendeeDao attendeeDao = app.getDatabaseHelper().getAttendeeeDao();
                    Attendee attendee;
                    for (RefreshResponse.AttendeeModel attendeeModel : conference.getAttendees()) {
                        attendee = new Attendee(attendeeModel.getId(), attendeeModel.getFirstName(), attendeeModel.getLastName(), attendeeModel.getFirmName(), attendeeModel.getProfilePicture());
                        attendeeDao.create(attendee);
                    }
                }
            } else {
                settingDao.setConferenceId(-1);
                settingDao.setConferenceIsShow(false);
                settingDao.setConferenceUrl(null);
                settingDao.setConferenceStartDate(null);
                settingDao.setConferenceFinishDate(null);
            }
        }
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        app.getEventBus().post(new Error(throwable.getMessage(), 0));
        return RetryConstraint.CANCEL;
    }

    public static class Success {
    }

    public static class Error {

        private String errorMessage;
        private int status;

        public Error(String errorMessage, int status) {
            this.errorMessage = errorMessage;
            this.status = status;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public int getStatus() {
            return status;
        }
    }
}
