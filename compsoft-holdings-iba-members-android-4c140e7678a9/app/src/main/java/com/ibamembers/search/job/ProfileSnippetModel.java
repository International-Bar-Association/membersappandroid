package com.ibamembers.search.job;

import com.google.gson.annotations.SerializedName;
import com.ibamembers.app.api.Address;
import com.ibamembers.app.api.ResponseError;

public class ProfileSnippetModel {

    @SuppressWarnings("unused")
    @SerializedName("UserId")
    private int userId;

    @SuppressWarnings("unused")
    @SerializedName("FirstName")
    private String firstName;

    @SuppressWarnings("unused")
    @SerializedName("LastName")
    private String lastName;

    @SuppressWarnings("unused")
    @SerializedName("FirmName")
    private String firmName;

    @SuppressWarnings("unused")
    @SerializedName("JobPosition")
    private String jobPosition;

    @SuppressWarnings("unused")
    @SerializedName("ProfilePicture")
    private String profilePictureUrl;

    @SuppressWarnings("unused")
    @SerializedName("CurrentlyAttendingConference")
    private Integer currentlyAttendingConference;

    @SuppressWarnings("unused")
    @SerializedName("Address")
    private Address address;

    @SuppressWarnings("unused")
    @SerializedName("ResponseError")
    private ResponseError responseError;

    public int getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirmName() {
        return firmName;
    }

    public String getJobPosition() {
        return jobPosition;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public Integer getCurrentlyAttendingConference() {
        return currentlyAttendingConference;
    }

    public Address getAddress() {
        return address;
    }

    public ResponseError getResponseError() {
        return responseError;
    }
}
