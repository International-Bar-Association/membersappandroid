package com.ibamembers.search.job;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.birbit.android.jobqueue.RetryConstraint;
import com.ibamembers.app.SettingDao;
import com.ibamembers.app.App;
import com.ibamembers.app.JobConfig;
import com.ibamembers.app.RestClient;
import com.ibamembers.app.service.BaseJob;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class SearchJob extends BaseJob {

    public static String SEARCH_JOB_TAG = "SEARCH_JOB_TAG";
    private int skip;
    private int take;

    public SearchJob(int skip, int take) {
        super(JobConfig.PRIORITY_LOW, SEARCH_JOB_TAG);
        this.skip = skip;
        this.take = take;
    }

    @Override
    public void onRun() throws Throwable {
        if (app != null) {
            RestClient restClient = new RestClient(app);

            SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
            String firstName = settingDao.getSearchFirstName();
            String lastName = settingDao.getSearchLastName();
            String firmName = settingDao.getSearchFirmName();
            String city = settingDao.getSearchCity();
            String country = settingDao.getSearchCountry();
            boolean isConference = settingDao.getSearchConference();
            int committeeId = (int) settingDao.getSearchCommitteeId();
            int areaOfPracticeId = (int) settingDao.getSearchAreaOfPracticeId();
            int countryId = app.getDataManager().getCountryIdFromName(app, country);

            Map<String, String> searchMap = new HashMap<>();

            if (!TextUtils.isEmpty(firstName)) {
                searchMap.put("FirstName", firstName);
            }

            if (!TextUtils.isEmpty(lastName)) {
                searchMap.put("LastName", lastName);
            }

            if (!TextUtils.isEmpty(firmName)) {
                searchMap.put("FirmName", firmName);
            }

            if (!TextUtils.isEmpty(city)) {
                searchMap.put("City", city);
            }

            if (countryId != -1) {
                searchMap.put("Country", String.valueOf(countryId));
            }

            if (areaOfPracticeId != -1) {
                searchMap.put("AreaOfPractice", String.valueOf(areaOfPracticeId));
            }

            if (committeeId != -1) {
                searchMap.put("Committee", String.valueOf(committeeId));
            }

            if (isConference) {
                searchMap.put("OnlyConferenceAttendees", String.valueOf(isConference));
            }

            try {
                Call<ProfileSnippetModel[]> responseCall = restClient.getApiService().search(searchMap, skip, take);
                Response<ProfileSnippetModel[]> response = responseCall.execute();

                if (response.isSuccessful() && response.body() != null) {
                    app.getJobManager(App.JobQueueName.Loading).addJob(new ConvertProfileSnippetsJob(response.body(), skip));
                } else {
                    app.getEventBus().post(new SearchJobError("", response.code()));
                }
            } catch (IOException e) {
                rerunJobTillLimit();
            }
        }
    }
    
    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        app.getEventBus().post(new SearchJobError(throwable.getMessage(), 0));
        return RetryConstraint.CANCEL;
    }

    public static class SearchJobError {

        private String errorMessage;
        private int status;

        public SearchJobError(String errorMessage, int status) {
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
