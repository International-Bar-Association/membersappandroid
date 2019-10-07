package com.ibamembers.search.database;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

public class AttendeeDao extends BaseDaoImpl<Attendee, Integer> {

    static final String COLUMN_ID = "id";
    static final String COLUMN_USER_ID = "userId";
    static final String COLUMN_FIRST_NAME = "firstName";
    static final String COLUMN_LAST_NAME = "lastName";
    static final String COLUMN_FIRM_NAME = "firmName";
    static final String COLUMN_PROFILE_PICURE = "profilePicture";

    public AttendeeDao(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Attendee.class);
    }
}
