package com.ibamembers.search.favourites;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class ProfileSnippet {

    @DatabaseField(id = true, index = true, columnName = ProfileSnippetDao.COLUMN_USER_ID)
    private int id;

    @DatabaseField(columnName = ProfileSnippetDao.COLUMN_FIRST_NAME)
    private String firstName;

    @DatabaseField(columnName = ProfileSnippetDao.COLUMN_LAST_NAME)
    private String lastName;

    @DatabaseField(columnName = ProfileSnippetDao.COLUMN_FIRM_NAME)
    private String firmName;

    @DatabaseField(columnName = ProfileSnippetDao.COLUMN_JOB_POSITION)
    private String jobPosition;

    @DatabaseField(dataType = DataType.BYTE_ARRAY, columnName = ProfileSnippetDao.COLUMN_IMAGE_DATA)
    private byte[] imageData;

    @DatabaseField(columnName = ProfileSnippetDao.COLUMN_PROFILE_PICTURE)
    private String profilePicture;

    @DatabaseField(columnName = ProfileSnippetDao.COLUMN_CURRENTLY_ATTENDING_CONFERENCE)
    private Integer currentlyAttendingConference;

    @DatabaseField(columnName = ProfileSnippetDao.COLUMN_ADDRESS)
    private String address;

    private boolean isSelected;

    @SuppressWarnings("unused")
    public ProfileSnippet() {}

    public ProfileSnippet(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirmName() {
        return firmName;
    }

    public void setFirmName(String firmName) {
        this.firmName = firmName;
    }

    public String getJobPosition() {
        return jobPosition;
    }

    public void setJobPosition(String jobPosition) {
        this.jobPosition = jobPosition;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] profilePicture) {
        this.imageData = profilePicture;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Integer getCurrentlyAttendingConference() {
        return currentlyAttendingConference;
    }

    public void setCurrentlyAttendingConference(Integer currentlyAttendingConference) {
        this.currentlyAttendingConference = currentlyAttendingConference;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
