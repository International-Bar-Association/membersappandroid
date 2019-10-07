package com.ibamembers.profile.db;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.List;

/**
 * This is used to store all message threads per profile.
 */
public class ProfileMessageDao extends BaseDaoImpl<ProfileMessage, Integer> {

    static final String COLUMN_ID = "id";
    static final String COLUMN_PROFILE_NAME = "profileName";
    static final String COLUMN_IMAGE_URL = "imageUrl";
    static final String COLUMN_MESSAGE_STRING = "messageString";

    public ProfileMessageDao(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, ProfileMessage.class);
    }

    public void saveResponseAsProfileMessage(int profileId, String profileName, String imageUrl, String message) throws SQLException {
        ProfileMessage profileMessage = new ProfileMessage(profileId, profileName, imageUrl, message);
        createOrUpdate(profileMessage);
    }

    public List<ProfileMessage> queryForProfileId(int profileId) throws SQLException{
        QueryBuilder<ProfileMessage, Integer> qb = queryBuilder();
        qb.where().eq(COLUMN_ID, profileId);
        return query(qb.prepare());
    }
}
