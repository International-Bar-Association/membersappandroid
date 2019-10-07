package com.ibamembers.search.favourites;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

public class ProfileSnippetDao extends BaseDaoImpl<ProfileSnippet, Long> {

    static final String COLUMN_USER_ID = "userId";
    static final String COLUMN_FIRST_NAME = "firstName";
    static final String COLUMN_LAST_NAME = "lastName";
    static final String COLUMN_FIRM_NAME = "firmName";
    static final String COLUMN_JOB_POSITION = "jobPosition";
    static final String COLUMN_PROFILE_PICTURE = "profilePicture";
    static final String COLUMN_CURRENTLY_ATTENDING_CONFERENCE = "currentlyAttendingConference";
    static final String COLUMN_ADDRESS = "address";
    static final String COLUMN_IMAGE_DATA = "imageData";

    public ProfileSnippetDao(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, ProfileSnippet.class);
    }

    public boolean isProfileFavourited(long profileId) throws SQLException {
        ProfileSnippet profileSnippet = queryForId(profileId);
        return profileSnippet != null;
    }

    public void removeProfileFromFavourites(long profileId) throws SQLException {
        ProfileSnippet profileSnippet = queryForId(profileId);
        if (profileSnippet != null) {
            delete(profileSnippet);
        }
    }

    public void addProfileToFavourites(ProfileSnippet profileSnippet) throws SQLException {
        createOrUpdate(profileSnippet);
    }
}
