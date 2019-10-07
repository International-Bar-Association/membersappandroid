package com.ibamembers.login;

import com.google.gson.annotations.SerializedName;
import com.ibamembers.profile.job.ProfileModel;
import com.ibamembers.app.api.ResponseError;

public class LoginResponse {

    @SerializedName("SessionToken")
    private String sessionToken;

    @SerializedName("Profile")
    private ProfileModel profileModel;

    @SerializedName("ResponseError")
    private ResponseError responseError;

    public String getSessionToken() {
        return sessionToken;
    }

    public ProfileModel getProfileModel() {
        return profileModel;
    }

    public ResponseError getResponseError() {
        return responseError;
    }
}
