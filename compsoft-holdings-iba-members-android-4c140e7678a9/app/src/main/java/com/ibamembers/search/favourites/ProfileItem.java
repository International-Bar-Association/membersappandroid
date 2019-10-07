package com.ibamembers.search.favourites;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class ProfileItem {

    @DatabaseField(id = true, index = true, columnName = ProfileItemDao.COLUMN_USER_ID)
    private int id;

    @DatabaseField(columnName = ProfileItemDao.COLUMN_FIRST_NAME)
    private String firstName;

    @DatabaseField(columnName = ProfileItemDao.COLUMN_LAST_NAME)
    private String lastName;

    @DatabaseField(columnName = ProfileItemDao.COLUMN_FIRM_NAME)
    private String firmName;

    @DatabaseField(columnName = ProfileItemDao.COLUMN_JOB_POSITION)
    private String jobPosition;

    @DatabaseField(columnName = ProfileItemDao.COLUMN_ADDRESS)
    private String address;

    @DatabaseField(columnName = ProfileItemDao.COLUMN_EMAIL)
    private String email;

    @DatabaseField(columnName = ProfileItemDao.COLUMN_PHONE)
    private String phone;

    @DatabaseField(columnName = ProfileItemDao.COLUMN_BIO)
    private String bio;

    @DatabaseField(columnName = ProfileItemDao.COLUMN_COMMITTEES)
    private String committees;

    @DatabaseField(columnName = ProfileItemDao.COLUMN_AREA_OF_PRACTICES)
    private String areaOfPractices;

    @DatabaseField(columnName = ProfileItemDao.COLUMN_PROFILE_PICTURE_URL)
    private String pictureProfileUrl;

    @DatabaseField(dataType = DataType.BYTE_ARRAY, columnName = ProfileItemDao.COLUMN_IMAGE_DATA)
    byte[] imageData;

    @SuppressWarnings("unused")
    public ProfileItem() {}

    public ProfileItem(int id) {
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCommittees() {
        return committees;
    }

    public void setCommittees(String committees) {
        this.committees = committees;
    }

    public String getAreaOfPractices() {
        return areaOfPractices;
    }

    public void setAreaOfPractices(String areaOfPractices) {
        this.areaOfPractices = areaOfPractices;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public String getPictureProfileUrl() {
        return pictureProfileUrl;
    }

    public void setPictureProfileUrl(String pictureProfileUrl) {
        this.pictureProfileUrl = pictureProfileUrl;
    }
}
