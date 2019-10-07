package com.ibamembers.search.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Attendee {

    @DatabaseField(generatedId = true, index = true, columnName = AttendeeDao.COLUMN_ID)
    private int id;

    @DatabaseField(columnName = AttendeeDao.COLUMN_USER_ID)
    private float userId;

    @DatabaseField(columnName = AttendeeDao.COLUMN_FIRST_NAME)
    private String firstName;

    @DatabaseField(columnName = AttendeeDao.COLUMN_LAST_NAME)
    private String lastName;

    @DatabaseField(columnName = AttendeeDao.COLUMN_FIRM_NAME)
    private String firmName;

    @DatabaseField(columnName = AttendeeDao.COLUMN_PROFILE_PICURE)
    private String profilePicture;

    @SuppressWarnings("unused")
    public Attendee() {}

    public Attendee(float id, String firstName, String lastName, String firmName, String profilePicture ) {
        this.userId = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.firmName = firmName;
        this.profilePicture = profilePicture;
    }

    public int getId() {
        return id;
    }

    public float getUserId() {
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

    public String getProfilePicture() {
        return profilePicture;
    }
}