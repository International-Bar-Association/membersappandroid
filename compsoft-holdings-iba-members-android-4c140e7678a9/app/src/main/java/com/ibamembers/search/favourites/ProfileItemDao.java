package com.ibamembers.search.favourites;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

public class ProfileItemDao extends BaseDaoImpl<ProfileItem, Long> {

    static final String COLUMN_USER_ID = "userId";
    static final String COLUMN_FIRST_NAME = "firstName";
    static final String COLUMN_LAST_NAME = "lastName";
    static final String COLUMN_FIRM_NAME = "firmName";
    static final String COLUMN_JOB_POSITION = "jobPosition";
    static final String COLUMN_PROFILE_PICTURE_URL = "profilePictureUrl";
    static final String COLUMN_ADDRESS = "address";
    static final String COLUMN_EMAIL = "email";
    static final String COLUMN_PHONE = "phone";
    static final String COLUMN_BIO = "bio";
    static final String COLUMN_COMMITTEES = "committees";
    static final String COLUMN_AREA_OF_PRACTICES = "areaOfPractices";
    static final String COLUMN_IMAGE_DATA = "imageData";

    public ProfileItemDao(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, ProfileItem.class);
    }

    public void removeProfileFromFavourites(long profileId) throws SQLException {
        ProfileItem profileItem = queryForId(profileId);
        if (profileItem != null) {
            delete(profileItem);
        }
    }

    public void addProfileToFavourites(ProfileItem profileItem) throws SQLException {
        createOrUpdate(profileItem);
    }
}

