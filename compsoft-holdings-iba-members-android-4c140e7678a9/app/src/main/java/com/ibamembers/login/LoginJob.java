package com.ibamembers.login;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.birbit.android.jobqueue.RetryConstraint;
import com.ibamembers.app.DataManager;
import com.ibamembers.app.JobConfig;
import com.ibamembers.app.RestClient;
import com.ibamembers.app.SettingDao;
import com.ibamembers.app.service.BaseJob;
import com.urbanairship.UAirship;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Response;

public class LoginJob extends BaseJob {

    private String username;
    private String password;

    public LoginJob(String username, String password){
        super(JobConfig.PRIORITY_NORMAL);
        this.username = username;
        this.password = password;
    }

    @Override
    public void onRun() throws Throwable {
        if (app != null) {
            String basicAuthHeader = RestClient.getAuthorizationHeader(username, password, null);
            saveBasicAuthHeader(basicAuthHeader);

            RestClient restClient = new RestClient(app,
                    RestClient.getXAuth("/" + RestClient.LOGIN_PATH,
                            RestClient.getDateHeader(app),
                            basicAuthHeader,
                            app.getServiceApiKey()));


            try {
                Call<LoginResponse> responseCall = restClient.getApiService(basicAuthHeader).login();
                Response<LoginResponse> response = responseCall.execute();

                if (response.isSuccessful()) {
                    LoginResponse responseBody = response.body();
                    if (responseBody != null) {
                        saveUserProfileData(responseBody);
                        app.getEventBus().post(new Success());
                    }
                } else {
                    app.getEventBus().post(new Error(response.message()));
                }
            } catch (IOException e) {
                if (!rerunJobTillLimit()) app.getEventBus().post(new Error(null));
            }
        }
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        //app.getEventBus().post(new Error(throwable.getMessage()));
        return RetryConstraint.RETRY;
    }


    private void saveBasicAuthHeader(String basicAuthHeader) throws SQLException {
        if (app != null) {
            SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
            settingDao.setBasicAuthHeader(basicAuthHeader);
        }
    }

    private void saveUserProfileData(LoginResponse loginResponse) throws SQLException {
        if (app != null) {
            String sessionToken = loginResponse.getSessionToken();
            String firstName = loginResponse.getProfileModel().getFirstName();
            String lastName = loginResponse.getProfileModel().getLastName();
            int userId = loginResponse.getProfileModel().getUserId();
            float profileClass = loginResponse.getProfileModel().getAccess().getProfileClass();
            boolean _public = loginResponse.getProfileModel().is_public();
            String firmName = loginResponse.getProfileModel().getFirmName();
            String jobPosition = loginResponse.getProfileModel().getJobPosition();
            String profilePictureUrl = loginResponse.getProfileModel().getProfilePictureUrl();
            String email = loginResponse.getProfileModel().getEmail();
            String biography = loginResponse.getProfileModel().getBiography();
            String phone = loginResponse.getProfileModel().getPhone();
            float[] areaOfPractice = loginResponse.getProfileModel().getAreasOfPracticeIds();
            float[] committees = loginResponse.getProfileModel().getCommitteeIds();
            String[] addressLines = loginResponse.getProfileModel().getAddress().getAddressLines();
            String city = loginResponse.getProfileModel().getAddress().getCity();
            String county = loginResponse.getProfileModel().getAddress().getCounty();
            String state = loginResponse.getProfileModel().getAddress().getState();
            String country = loginResponse.getProfileModel().getAddress().getCountry();
            String zip = loginResponse.getProfileModel().getAddress().getPcZip();

            SettingDao settingDao = app.getDatabaseHelper().getSettingDao();

            /*
            if (!TextUtils.isEmpty(profilePictureUrl)) {
                if (!profilePictureUrl.equals("N/A")) {
                    DataManager dataManager = app.getDataManager();
                    Bitmap imageBitmap = dataManager.getBitmapFromURL(profilePictureUrl);
                    if (imageBitmap != null) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        imageBitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
                        settingDao.setImageData(stream.toByteArray());
                    }
                }
            }*/

            // if username is different from last clear data
            String oldUsername = settingDao.getUsername();
            if (!TextUtils.isEmpty(oldUsername)) {
                if (!settingDao.getUsername().equals(username)) {
                    settingDao.clearData();
                }
            }

            if (!TextUtils.isEmpty(sessionToken)) {
                settingDao.setSessionToken(sessionToken);
            }

            if (!TextUtils.isEmpty(firstName)) {
                settingDao.setCachedFirstName(firstName);
            }

            if (!TextUtils.isEmpty(lastName)) {
                settingDao.setCachedLastName(lastName);
            }

            settingDao.setCachedId(userId);
            settingDao.setCachedClass(profileClass);
            settingDao.setPublic(_public);
            UAirship.shared().getNamedUser().setId(String.valueOf(userId));

            if (!TextUtils.isEmpty(firmName)) {
                settingDao.setCachedFirmName(firmName);
            }

            if (!TextUtils.isEmpty(jobPosition)) {
                settingDao.setCachedJobPosition(jobPosition);
            }

            if (!TextUtils.isEmpty(profilePictureUrl)) {
                settingDao.setCachedProfilePictureUrl(profilePictureUrl);
            }

            if (!TextUtils.isEmpty(email)) {
                settingDao.setCachedEmail(email);
            }

            if (!TextUtils.isEmpty(biography)) {
                settingDao.setCachedBiography(biography);
            }

            if (!TextUtils.isEmpty(phone)) {
                settingDao.setCachedPhone(phone);
            }

            DataManager dataManager = app.getDataManager();
            settingDao.setCachedAreaOfPracticeIds(dataManager.formatFloatArray(areaOfPractice));
            settingDao.setCachedCommitteeIds(dataManager.formatFloatArray(committees));
            settingDao.setCachedAddressLines(app.getDataManager().formatAddressLines(addressLines, city, zip, country));

            if (!TextUtils.isEmpty(city)) {
                settingDao.setCachedCity(city);
            }

            if (!TextUtils.isEmpty(county)) {
                settingDao.setCachedCounty(county);
            }

            if (!TextUtils.isEmpty(state)) {
                settingDao.setCachedState(state);
            }

            if (!TextUtils.isEmpty(country)) {
                settingDao.setCachedCountry(country);
            }

            if (!TextUtils.isEmpty(zip)) {
                settingDao.setCachedZip(zip);
            }

            if (!TextUtils.isEmpty(username)) {
                settingDao.setUsername(username);
            }

            if (!TextUtils.isEmpty(password)) {
                settingDao.setPassword(password);
            }

            settingDao.setUserRegistered(true);
            settingDao.setLoginDate(new Date());
        }
    }

    public static class Success {
    }

    public static class Error {

        private String errorMessage;

        public Error(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
