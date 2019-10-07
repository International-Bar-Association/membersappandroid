package com.ibamembers.profile.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class ProfileMessage {

    @DatabaseField(id = true, index = true, columnName = ProfileMessageDao.COLUMN_ID)
    private int id;

    @DatabaseField(columnName = ProfileMessageDao.COLUMN_PROFILE_NAME)
    private String profileName;

    @DatabaseField(columnName = ProfileMessageDao.COLUMN_IMAGE_URL)
    private String imageUrl;

    @DatabaseField(columnName = ProfileMessageDao.COLUMN_MESSAGE_STRING)
    private String messageString;

    @SuppressWarnings("unused")
    public ProfileMessage() {}

    public ProfileMessage(int id, String profileName, String imageUrl, String messageString) {
        this.id = id;
        this.profileName = profileName;
        this.imageUrl = imageUrl;
        this.messageString = messageString;
    }

    public int getId() {
        return id;
    }

    public String getProfileName() {
        return profileName;
    }

    public String getMessageString() {
        return messageString;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setMessageString(String messageString) {
        this.messageString = messageString;
    }
}
