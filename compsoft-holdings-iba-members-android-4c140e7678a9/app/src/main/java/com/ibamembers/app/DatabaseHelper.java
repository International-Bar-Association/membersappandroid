package com.ibamembers.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ibamembers.conference.event.db.ConferenceEvent;
import com.ibamembers.conference.event.db.ConferenceEventDao;
import com.ibamembers.content.db.ContentDownload;
import com.ibamembers.content.db.ContentDownloadDao;
import com.ibamembers.messages.db.NewMessage;
import com.ibamembers.messages.db.NewMessageBufferDao;
import com.ibamembers.profile.db.ProfileMessage;
import com.ibamembers.profile.db.ProfileMessageDao;
import com.ibamembers.search.database.AreaOfPractice;
import com.ibamembers.search.database.AreaOfPracticeDao;
import com.ibamembers.search.database.Attendee;
import com.ibamembers.search.database.AttendeeDao;
import com.ibamembers.search.database.Committee;
import com.ibamembers.search.database.CommitteeDao;
import com.ibamembers.search.favourites.ProfileItem;
import com.ibamembers.search.favourites.ProfileItemDao;
import com.ibamembers.search.favourites.ProfileSnippet;
import com.ibamembers.search.favourites.ProfileSnippetDao;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "app.db";
    private static final int DATABASE_VERSION = 5;
    private static final String LOG_TAG = "DBHelper";

    private SettingDao settingDao;
    private AreaOfPracticeDao areaOfPracticeDao;
    private CommitteeDao committeeDao;
    private ProfileSnippetDao profileSnippetDao;
    private ProfileItemDao profileItemDao;
    private ContentDownloadDao contentDownloadDao;
    private NewMessageBufferDao newMessageBufferDao;
    private ConferenceEventDao conferenceEventDao;
    private AttendeeDao attendeeDao;
    private ProfileMessageDao testEventDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Setting.class);
            TableUtils.createTable(connectionSource, AreaOfPractice.class);
            TableUtils.createTable(connectionSource, Committee.class);
            TableUtils.createTable(connectionSource, ProfileSnippet.class);
            TableUtils.createTable(connectionSource, ProfileItem.class);
            TableUtils.createTable(connectionSource, ContentDownload.class);
            TableUtils.createTable(connectionSource, NewMessage.class);
            TableUtils.createTable(connectionSource, ConferenceEvent.class);
            TableUtils.createTable(connectionSource, ProfileMessage.class);
            TableUtils.createTable(connectionSource, Attendee.class);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                ProfileItemDao profileItemDao = getProfileItemDao();
                profileItemDao.executeRaw("ALTER TABLE `ProfileItem` ADD COLUMN profilePictureUrl STRING;");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (oldVersion < 3) {
            try {
                ConferenceEventDao conferenceEventDao = getConferenceEventDao();
                conferenceEventDao.executeRaw("ALTER TABLE `ConferenceEvent` ADD COLUMN roomName STRING;");
                conferenceEventDao.executeRaw("ALTER TABLE `ConferenceEvent` ADD COLUMN roomCentreX STRING;");
                conferenceEventDao.executeRaw("ALTER TABLE `ConferenceEvent` ADD COLUMN roomCentreY STRING;");
                conferenceEventDao.executeRaw("ALTER TABLE `ConferenceEvent` ADD COLUMN floor STRING;");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (oldVersion < 4) {
            try {
                ProfileSnippetDao profileSnippetDao = getProfileSnippetDao();
                profileSnippetDao.executeRaw("ALTER TABLE `ProfileSnippet` DROP COLUMN profilePicture;");
                profileSnippetDao.executeRaw("ALTER TABLE `ProfileSnippet` ADD COLUMN profilePicture STRING;");
                profileSnippetDao.executeRaw("ALTER TABLE `ProfileSnippet` ADD COLUMN imageDate BYTE_ARRAY;");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (oldVersion < 5) {
            try {
                ProfileSnippetDao profileSnippetDao = getProfileSnippetDao();
                profileSnippetDao.executeRaw("ALTER TABLE `ProfileMessage` ADD COLUMN imageUrl STRING;");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized SettingDao getSettingDao() throws SQLException {
        if (settingDao == null) {
            settingDao = new SettingDao(getConnectionSource());
        }
        return settingDao;
    }

    public synchronized AreaOfPracticeDao getAreaOfPracticeDao() throws SQLException {
        if (areaOfPracticeDao == null) {
            areaOfPracticeDao = new AreaOfPracticeDao(getConnectionSource());
        }
        return areaOfPracticeDao;
    }

    public synchronized CommitteeDao getCommitteeDao() throws SQLException {
        if (committeeDao == null) {
            committeeDao = new CommitteeDao(getConnectionSource());
        }
        return committeeDao;
    }

    public synchronized AttendeeDao getAttendeeeDao() throws SQLException {
        if (attendeeDao == null) {
            attendeeDao = new AttendeeDao(getConnectionSource());
        }
        return attendeeDao;
    }

    public void clearAttendeesDao() {
        try {
            TableUtils.clearTable(getConnectionSource(), Attendee.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized ProfileSnippetDao getProfileSnippetDao() throws SQLException {
        if (profileSnippetDao == null) {
            profileSnippetDao = new ProfileSnippetDao(getConnectionSource());
        }
        return profileSnippetDao;
    }

    public synchronized ProfileItemDao getProfileItemDao() throws SQLException {
        if (profileItemDao == null) {
            profileItemDao= new ProfileItemDao(getConnectionSource());
        }
        return profileItemDao;
    }

    public synchronized ContentDownloadDao getContentDownloadDao() throws SQLException {
        if (contentDownloadDao == null) {
            contentDownloadDao = new ContentDownloadDao(getConnectionSource());
        }
        return contentDownloadDao;
    }

    public synchronized NewMessageBufferDao getNewMessageBufferDao() throws SQLException {
        if (newMessageBufferDao == null) {
            newMessageBufferDao = new NewMessageBufferDao(getConnectionSource());
        }
        return newMessageBufferDao;
    }

    public void clearNewMessageBufferDao() {
        try {
            TableUtils.clearTable(getConnectionSource(), NewMessage.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized ConferenceEventDao getConferenceEventDao() throws SQLException {
        if (conferenceEventDao == null) {
            conferenceEventDao = new ConferenceEventDao(getConnectionSource());
        }
        return conferenceEventDao;
    }

    public synchronized ProfileMessageDao getProfileMessageDao() throws SQLException {
        if (testEventDao == null) {
            testEventDao = new ProfileMessageDao(getConnectionSource());
        }
        return testEventDao;
    }
}
