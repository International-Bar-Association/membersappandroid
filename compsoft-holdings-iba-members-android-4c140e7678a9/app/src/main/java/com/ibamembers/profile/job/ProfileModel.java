package com.ibamembers.profile.job;

import com.google.gson.annotations.SerializedName;
import com.ibamembers.app.api.Address;

public class ProfileModel {

    @SuppressWarnings("unused")
    @SerializedName("Id")
    private int userId;

    @SuppressWarnings("unused")
    @SerializedName("FirstName")
    private String firstName;

    @SuppressWarnings("unused")
    @SerializedName("LastName")
    private String lastName;

    @SuppressWarnings("unused")
    @SerializedName("Public")
    private boolean _public;

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
    private Integer CurrentlyAttendingConference;

    @SuppressWarnings("unused")
    @SerializedName("Email")
    private String email;

    @SuppressWarnings("unused")
    @SerializedName("Biography")
    private String biography;

    @SuppressWarnings("unused")
    @SerializedName("Phone")
    private String phone;

    @SuppressWarnings("unused")
    @SerializedName("AreasOfPractice")
    private float[] areasOfPractice;

    @SuppressWarnings("unused")
    @SerializedName("Committees")
    private float[] committee;

    @SuppressWarnings("unused")
    @SerializedName("Address")
    private Address address;

    @SuppressWarnings("unused")
    @SerializedName("Access")
    private Access access;

    private byte[] imageData;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getUserId() {
        return userId;
    }

    public boolean is_public() {
        return _public;
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
        return CurrentlyAttendingConference;
    }

    public String getEmail() {
        return email;
    }

    public String getBiography() {
        return biography;
    }

    public String getPhone() {
        return phone;
    }

    public float[] getAreasOfPracticeIds() {
        return areasOfPractice;
    }

    public float[] getCommitteeIds() {
        return committee;
    }

    public Address getAddress() {
        return address;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public Access getAccess() {
        return access;
    }

    public class Access {

        @SuppressWarnings("unused")
        @SerializedName("Class")
        private float profileClass;

        @SuppressWarnings("unused")
        @SerializedName("CanSearchDirectory")
        private boolean canSearchDirectory;

        public boolean getCanSearchDirectory() {
            return canSearchDirectory;
        }

        public float getProfileClass() {
            return profileClass;
        }
    }
}
