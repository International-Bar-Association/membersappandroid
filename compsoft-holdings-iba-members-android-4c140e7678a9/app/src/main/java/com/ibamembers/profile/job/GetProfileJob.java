package com.ibamembers.profile.job;

import android.support.annotation.NonNull;

import com.birbit.android.jobqueue.RetryConstraint;
import com.ibamembers.app.JobConfig;
import com.ibamembers.app.RestClient;
import com.ibamembers.app.service.BaseJob;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class GetProfileJob extends BaseJob {

    private float userId;
    private String jobTag;

    public GetProfileJob(int userId) {
        super(JobConfig.PRIORITY_NORMAL);
        this.userId = userId;
    }

    public GetProfileJob(int userId, String jobTag) {
        super(JobConfig.PRIORITY_NORMAL, jobTag);
        this.jobTag = jobTag;
        this.userId = userId;
    }

    @Override
    public void onRun() throws Throwable {
        if (app != null) {
            RestClient restClient = new RestClient(app);

            try {
                Call<ProfileModel> responseCall = restClient.getApiService().getProfile((int) userId);
                Response<ProfileModel> response = responseCall.execute();

                if (response.isSuccessful() && response.body() != null) {

                    /*
                String imageUrl = profile.getProfilePictureUrl();
                if (!TextUtils.isEmpty(imageUrl)) {
                    if (!imageUrl.equals("N/A")) {
                        DataManager dataManager = app.getDataManager();
                        Bitmap imageBitmap = dataManager.getBitmapFromURL(imageUrl);
                        if (imageBitmap != null) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            imageBitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
                            profile.setImageData(stream.toByteArray());
                        }
                    }
                }*/

                    Success success = new Success(response.body(), jobTag);
                    app.getEventBus().post(success);

                } else {
                    app.getEventBus().post(new GetProfileJobError(response.message(), response.code()));
                }
            } catch (IOException e) {
                rerunJobTillLimit();
            }
        }

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        app.getEventBus().post(new GetProfileJobError(throwable.getMessage(), 0));
        return RetryConstraint.CANCEL;
    }

    public static class Success {

        private ProfileModel profileModel;
        private String jobTag;

        public Success(ProfileModel profileModel, String jobTag) {
            this.profileModel = profileModel;
            this.jobTag = jobTag;
        }

        public ProfileModel getProfileModel() {
            return profileModel;
        }

        public String getJobTag() {
            return jobTag;
        }
    }

    public static class GetProfileJobError {

        private String errorMessage;
        private int status;

        public GetProfileJobError(String errorMessage, int status) {
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