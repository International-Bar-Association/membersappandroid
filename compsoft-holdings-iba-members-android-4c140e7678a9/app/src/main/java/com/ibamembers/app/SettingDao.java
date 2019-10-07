package com.ibamembers.app;

import com.ibamembers.R;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.Date;

public class SettingDao extends BaseDaoImpl<Setting, Long> {

    static final String COLUMN_ID = "id";
    static final String COLUMN_VALUE_STRING = "valueString";
    static final String COLUMN_VALUE_DATE = "valueDate";
    static final String COLUMN_VALUE_FLOAT = "valueFloat";
    static final String COLUMN_VALUE_BOOLEAN = "valueBoolean";
    static final String COLUMN_VALUE_INT = "valueInt";
    static final String COLUMN_VALUE_LONG = "valueLong";
    static final String COLUMN_VALUE_BYTE_ARRAY = "byteArray";

    private static final long ID_LAST_REFRESHED_DATE = 0;
    private static final long ID_CACHED_ID = 1;
    private static final long ID_CACHED_FIRST_NAME = 2;
    private static final long ID_CACHED_LAST_NAME = 3;
    private static final long ID_CACHED_JOB_POSITION = 4;
    private static final long ID_CACHED_PROFILE_PICTURE = 5;
    private static final long ID_CACHED_EMAIL = 6;
    private static final long ID_CACHED_BIOGRAPHY = 7;
    private static final long ID_CACHED_PHONE = 8;
    private static final long ID_CACHED_AREA_OF_PRACTICE_IDS = 9;
    private static final long ID_CACHED_COMMITTEE_IDS = 10;
    private static final long ID_CACHED_CITY = 11;
    private static final long ID_CACHED_COUNTY = 12;
    private static final long ID_CACHED_ADDRESS_LINES = 13;
    private static final long ID_CACHED_STATE = 14;
    private static final long ID_CACHED_COUNTRY = 15;
    private static final long ID_CACHED_ZIP = 16;
    private static final long ID_USERNAME = 17;
    private static final long ID_BASIC_AUTH_HEADER = 18;
    private static final long ID_SESSION_TOKEN = 19;
    private static final long ID_PUBLIC = 20;
    private static final long ID_CACHED_FIRM_NAME = 21;
    private static final long ID_REMEMBER_ME = 22;
    private static final long ID_PASSWORD = 23;
    private static final long ID_SAVED_SEARCH_FIRST_NAME = 24;
    private static final long ID_SAVED_SEARCH_LAST_NAME = 25;
    private static final long ID_SAVED_SEARCH_FIRM_NAME = 26;
    private static final long ID_SAVED_SEARCH_CONFERENCE = 49;
    private static final long ID_SAVED_SEARCH_CITY = 27;
    private static final long ID_SAVED_SEARCH_COUNTRY = 28;
    private static final long ID_SAVED_SEARCH_COMMITTEE_ID = 29;
    private static final long ID_SAVED_SEARCH_AREA_OF_PRACTICE_ID = 30;
    private static final long ID_USER_REGISTERED = 31;
    private static final long ID_LOGIN_DATE = 32;
    private static final long ID_MISSING_PROMPT_CAN_BIO = 33;
    private static final long ID_MISSING_PROMPT_CAN_PROFILE_PICTURE = 34;
    private static final long ID_MISSING_PROMPT_LAST_BIO_PROMPT = 34;
    private static final long ID_MISSING_PROMPT_LAST_PROFILE_PICTURE_PROMPT = 35;
    private static final long ID_IMAGE_DATA = 36;
    private static final long ID_CACHED_CLASS = 37;
    private static final long ID_CONFERENCE_IS_SHOW = 40;
    private static final long ID_CONFERENCE_ID = 46;
    private static final long ID_CONFERENCE_URL = 41;
    private static final long ID_CONFERENCE_START = 42;
    private static final long ID_CONFERENCE_FINISH = 43;
    private static final long ID_CONFERENCE_NAME = 47;
    private static final long ID_CONFERENCE_VENUE = 48;
    private static final long ID_GCM_TOKEN = 44;
    private static final long ID_GCM_REGISTERED_WITH_SERVER = 45;

    protected SettingDao(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Setting.class);
    }

    public Date getLastRefreshed() throws SQLException {
        Setting setting = queryForId(ID_LAST_REFRESHED_DATE);
        if (setting != null) {
            return setting.getValueDate();
        }
        return null;
    }

    public void setLastRefreshed(Date date) throws SQLException {
        Setting setting = new Setting(ID_LAST_REFRESHED_DATE, date);
        createOrUpdate(setting);
    }

    public int getCachedId() throws SQLException {
        Setting setting = queryForId(ID_CACHED_ID);
        return setting == null ? -1 : setting.getValueInt();
    }

    public void setCachedId(int id) throws SQLException {
        Setting setting = new Setting(ID_CACHED_ID, id);
        createOrUpdate(setting);
    }

    public float getCachedClass() throws SQLException {
        Setting setting = queryForId(ID_CACHED_CLASS);
        return setting == null ? -1 : setting.getValueFloat();
    }

    public static boolean isClassAllowedToSearch(App app) {
        if (app != null) {
            try {
                SettingDao settingDao = app.getDatabaseHelper().getSettingDao();
                int profileClass = (int) settingDao. getCachedClass();
                int[] bits = app.getResources().getIntArray(R.array.user_class_search_allowed);
                for (Integer profileClassInt : bits) {
                    if (profileClassInt == profileClass) {
                        return true;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void setCachedClass(float profileClass) throws SQLException {
        Setting setting = new Setting(ID_CACHED_CLASS, profileClass);
        createOrUpdate(setting);
    }

    public String getCachedFirstName() throws SQLException {
        Setting setting = queryForId(ID_CACHED_FIRST_NAME);
        return setting == null ? null : setting.getValueString();
    }

    public void setCachedFirstName(String firstName) throws SQLException {
        Setting setting = new Setting(ID_CACHED_FIRST_NAME, firstName);
        createOrUpdate(setting);
    }

    public String getCachedLastName() throws SQLException {
        Setting setting = queryForId(ID_CACHED_LAST_NAME);
        return setting == null ? null : setting.getValueString();
    }

    public void setCachedLastName(String lastName) throws SQLException {
        Setting setting = new Setting(ID_CACHED_LAST_NAME, lastName);
        createOrUpdate(setting);
    }

    public String getCachedJobPosition() throws SQLException {
        Setting setting = queryForId(ID_CACHED_JOB_POSITION);
        return setting == null ? null : setting.getValueString();
    }

    public void setCachedJobPosition(String jobPosition) throws SQLException {
        Setting setting = new Setting(ID_CACHED_JOB_POSITION, jobPosition);
        createOrUpdate(setting);
    }

    public String getCachedProfilePictureUrl() throws SQLException {
        Setting setting = queryForId(ID_CACHED_PROFILE_PICTURE);
        return setting == null ? null : setting.getValueString();
    }

    public void setCachedProfilePictureUrl(String profilePictureUrl) throws SQLException {
        Setting setting = new Setting(ID_CACHED_PROFILE_PICTURE, profilePictureUrl);
        createOrUpdate(setting);
    }

    public String getCachedEmail() throws SQLException {
        Setting setting = queryForId(ID_CACHED_EMAIL);
        return setting == null ? null : setting.getValueString();
    }

    public void setCachedEmail(String email) throws SQLException {
        Setting setting = new Setting(ID_CACHED_EMAIL, email);
        createOrUpdate(setting);
    }



    public String getCachedBiography() throws SQLException {
        Setting setting = queryForId(ID_CACHED_BIOGRAPHY);
        return setting == null ? null : setting.getValueString();
    }

    public void setCachedBiography(String biography) throws SQLException {
        Setting setting = new Setting(ID_CACHED_BIOGRAPHY, biography);
        createOrUpdate(setting);
    }

    public String getCachedPhone() throws SQLException {
        Setting setting = queryForId(ID_CACHED_PHONE);
        return setting == null ? null : setting.getValueString();
    }

    public void setCachedPhone(String phone) throws SQLException {
        Setting setting = new Setting(ID_CACHED_PHONE, phone);
        createOrUpdate(setting);
    }

    public String getCachedAreaOfPracticeIds() throws SQLException {
        Setting setting = queryForId(ID_CACHED_AREA_OF_PRACTICE_IDS);
        return setting == null ? null : setting.getValueString();
    }

    public void setCachedAreaOfPracticeIds(String areaOfPracticeId) throws SQLException {
        Setting setting = new Setting(ID_CACHED_AREA_OF_PRACTICE_IDS, areaOfPracticeId);
        createOrUpdate(setting);
    }

    public String getCachedCommitteeIds() throws SQLException {
        Setting setting = queryForId(ID_CACHED_COMMITTEE_IDS);
        return setting == null ? null : setting.getValueString();
    }

    public void setCachedCommitteeIds(String cachedCommitteeId) throws SQLException {
        Setting setting = new Setting(ID_CACHED_COMMITTEE_IDS, cachedCommitteeId);
        createOrUpdate(setting);
    }

    public String getCachedCity() throws SQLException {
        Setting setting = queryForId(ID_CACHED_CITY);
        return setting == null ? null : setting.getValueString();
    }

    public void setCachedCity(String city) throws SQLException {
        Setting setting = new Setting(ID_CACHED_CITY, city);
        createOrUpdate(setting);
    }

    public String getCachedCounty() throws SQLException {
        Setting setting = queryForId(ID_CACHED_COUNTY);
        return setting == null ? null : setting.getValueString();
    }

    public void setCachedCounty(String county) throws SQLException {
        Setting setting = new Setting(ID_CACHED_COUNTY, county);
        createOrUpdate(setting);
    }

    public String getCachedAddressLines() throws SQLException {
        Setting setting = queryForId(ID_CACHED_ADDRESS_LINES);
        return setting == null ? null : setting.getValueString();
    }

    public void setCachedAddressLines(String addressLines) throws SQLException {
        Setting setting = new Setting(ID_CACHED_ADDRESS_LINES, addressLines);
        createOrUpdate(setting);
    }

    public String getCachedState() throws SQLException {
        Setting setting = queryForId(ID_CACHED_STATE);
        return setting == null ? null : setting.getValueString();
    }

    public void setCachedState(String state) throws SQLException {
        Setting setting = new Setting(ID_CACHED_STATE, state);
        createOrUpdate(setting);
    }

    public String getCachedCountry() throws SQLException {
        Setting setting = queryForId(ID_CACHED_COUNTRY);
        return setting == null ? null : setting.getValueString();
    }

    public void setCachedCountry(String country) throws SQLException {
        Setting setting = new Setting(ID_CACHED_COUNTRY, country);
        createOrUpdate(setting);
    }

    public String getCachedZip() throws SQLException {
        Setting setting = queryForId(ID_CACHED_ZIP);
        return setting == null ? null : setting.getValueString();
    }

    public void setCachedZip(String zip) throws SQLException {
        Setting setting = new Setting(ID_CACHED_ZIP, zip);
        createOrUpdate(setting);
    }

    public String getUsername() throws SQLException {
        Setting setting = queryForId(ID_USERNAME);
        return setting == null ? null : setting.getValueString();
    }

    public void setUsername(String username) throws SQLException {
        Setting setting = new Setting(ID_USERNAME, username);
        createOrUpdate(setting);
    }

    public String getPassword() throws SQLException {
        Setting setting = queryForId(ID_PASSWORD);
        return setting == null ? null : setting.getValueString();
    }

    public void setPassword(String password) throws SQLException {
        Setting setting = new Setting(ID_PASSWORD, password);
        createOrUpdate(setting);
    }

    public String getBasicAuthHeader() throws SQLException {
        Setting setting = queryForId(ID_BASIC_AUTH_HEADER);
        return setting == null ? null : setting.getValueString();
    }

    public void setBasicAuthHeader(String basicAuthHeader) throws SQLException {
        Setting setting = new Setting(ID_BASIC_AUTH_HEADER, basicAuthHeader);
        createOrUpdate(setting);
    }

    public String getSessionToken() throws SQLException {
        Setting setting = queryForId(ID_SESSION_TOKEN);
        return setting == null ? null : setting.getValueString();
    }

    public void setSessionToken(String sessionToken) throws SQLException {
        Setting setting = new Setting(ID_SESSION_TOKEN, sessionToken);
        createOrUpdate(setting);
    }

    public boolean isPublic() throws SQLException {
        Setting setting = queryForId(ID_PUBLIC);
        return setting != null && setting.isValueBoolean();
    }

    public void setPublic(boolean _public) throws SQLException {
        Setting setting = new Setting(ID_PUBLIC, _public);
        createOrUpdate(setting);
    }

    public String getCachedFirmName() throws SQLException {
        Setting setting = queryForId(ID_CACHED_FIRM_NAME);
        return setting == null ? null : setting.getValueString();
    }

    public void setCachedFirmName(String firmName) throws SQLException {
        Setting setting = new Setting(ID_CACHED_FIRM_NAME, firmName);
        createOrUpdate(setting);
    }

    public boolean isRememberMe() throws SQLException {
        Setting setting = queryForId(ID_REMEMBER_ME);
        return setting != null && setting.isValueBoolean();
    }

    public void setRememberMe(boolean rememberMe) throws SQLException {
        Setting setting = new Setting(ID_REMEMBER_ME, rememberMe);
        createOrUpdate(setting);
    }

    public String getSearchFirstName() throws SQLException {
        Setting setting = queryForId(ID_SAVED_SEARCH_FIRST_NAME);
        return setting == null ? null : setting.getValueString();
    }

    public void setSearchFirstName(String searchFirstName) throws SQLException {
        Setting setting = new Setting(ID_SAVED_SEARCH_FIRST_NAME, searchFirstName);
        createOrUpdate(setting);
    }

    public String getSearchLastName() throws SQLException {
        Setting setting = queryForId(ID_SAVED_SEARCH_LAST_NAME);
        return setting == null ? null : setting.getValueString();
    }

    public void setSearchLastName(String lastName) throws SQLException {
        Setting setting = new Setting(ID_SAVED_SEARCH_LAST_NAME, lastName);
        createOrUpdate(setting);
    }

    public String getSearchFirmName() throws SQLException {
        Setting setting = queryForId(ID_SAVED_SEARCH_FIRM_NAME);
        return setting == null ? null : setting.getValueString();
    }

    public void setSearchFirmName(String firmName) throws SQLException {
        Setting setting = new Setting(ID_SAVED_SEARCH_FIRM_NAME, firmName);
        createOrUpdate(setting);
    }

    public boolean getSearchConference() throws SQLException {
        Setting setting = queryForId(ID_SAVED_SEARCH_CONFERENCE);
        return setting == null ? false : setting.isValueBoolean();
    }

    public void setSearchConference(boolean isConference) throws SQLException {
        Setting setting = new Setting(ID_SAVED_SEARCH_CONFERENCE, isConference);
        createOrUpdate(setting);
    }

    public String getSearchCity() throws SQLException {
        Setting setting = queryForId(ID_SAVED_SEARCH_CITY);
        return setting == null ? null : setting.getValueString();
    }

    public void setSearchCity(String city) throws SQLException {
        Setting setting = new Setting(ID_SAVED_SEARCH_CITY, city);
        createOrUpdate(setting);
    }

    public String getSearchCountry() throws SQLException {
        Setting setting = queryForId(ID_SAVED_SEARCH_COUNTRY);
        return setting == null ? null : setting.getValueString();
    }

    public void setSearchCountry(String country) throws SQLException {
        Setting setting = new Setting(ID_SAVED_SEARCH_COUNTRY, country);
        createOrUpdate(setting);
    }

    public long getSearchCommitteeId() throws SQLException {
        Setting setting = queryForId(ID_SAVED_SEARCH_COMMITTEE_ID);
        return setting == null ? -1 : setting.getValueLong();
    }

    public void setIdSearchCommitteeId(long committeeId) throws SQLException {
        Setting setting = new Setting(ID_SAVED_SEARCH_COMMITTEE_ID, committeeId);
        createOrUpdate(setting);
    }

    public long getSearchAreaOfPracticeId() throws SQLException {
        Setting setting = queryForId(ID_SAVED_SEARCH_AREA_OF_PRACTICE_ID);
        return setting == null ? -1 : setting.getValueLong();
    }

    public void setSearchAreaOfPracticeId(long areaOfPracticeId) throws SQLException {
        Setting setting = new Setting(ID_SAVED_SEARCH_AREA_OF_PRACTICE_ID, areaOfPracticeId);
        createOrUpdate(setting);
    }

    public boolean isUserRegistered() throws SQLException {
        Setting setting = queryForId(ID_USER_REGISTERED);
        return setting != null && setting.isValueBoolean();
    }

    public void setUserRegistered(boolean userRegistered) throws SQLException {
        Setting setting = new Setting(ID_USER_REGISTERED, userRegistered);
        createOrUpdate(setting);
    }

    public Date getLoginDate() throws SQLException {
        Setting setting = queryForId(ID_LOGIN_DATE);
        if (setting != null) {
            return setting.getValueDate();
        }
        return null;
    }

    public void setLoginDate(Date date) throws SQLException {
        Setting setting = new Setting(ID_LOGIN_DATE, date);
        createOrUpdate(setting);
    }

    public boolean canPromptBio() throws SQLException {
        Setting setting = queryForId(ID_MISSING_PROMPT_CAN_BIO);
        return setting == null || setting.isValueBoolean();
    }

    public void setPromptBio(boolean canPromptBio) throws SQLException {
        Setting setting = new Setting(ID_MISSING_PROMPT_CAN_BIO, canPromptBio);
        createOrUpdate(setting);
    }

    public boolean canPromptProfilePicture() throws SQLException {
        Setting setting = queryForId(ID_MISSING_PROMPT_CAN_PROFILE_PICTURE);
        return setting == null || setting.isValueBoolean();
    }

    public void setPromptProfilePicture(boolean canPromptProfilePicture) throws SQLException {
        Setting setting = new Setting(ID_MISSING_PROMPT_CAN_PROFILE_PICTURE, canPromptProfilePicture);
        createOrUpdate(setting);
    }

    public Date getLastBioPromptDate() throws SQLException {
        Setting setting = queryForId(ID_MISSING_PROMPT_LAST_BIO_PROMPT);
        if (setting != null) {
            return setting.getValueDate();
        }
        return null;
    }

    public void setLastBioPromptDate(Date date) throws SQLException {
        Setting setting = new Setting(ID_MISSING_PROMPT_LAST_BIO_PROMPT, date);
        createOrUpdate(setting);
    }

    public Date getLastProfilePicturePromptDate() throws SQLException {
        Setting setting = queryForId(ID_MISSING_PROMPT_LAST_PROFILE_PICTURE_PROMPT);
        if (setting != null) {
            return setting.getValueDate();
        }
        return null;
    }

    public void setLastProfilePicturePromptDate(Date date) throws SQLException {
        Setting setting = new Setting(ID_MISSING_PROMPT_LAST_PROFILE_PICTURE_PROMPT, date);
        createOrUpdate(setting);
    }

    public byte[] getCachedImageData() throws SQLException {
        Setting setting = queryForId(ID_IMAGE_DATA);
        return setting == null ? null : setting.getValueByteArray();
    }

    public void setImageData(byte[] imageData) throws SQLException {
        Setting setting = new Setting(ID_IMAGE_DATA, imageData);
        createOrUpdate(setting);
    }

    public void clearData() throws SQLException {
        setCachedFirstName(null);
        setCachedLastName(null);
        setCachedFirmName(null);
        setCachedJobPosition(null);
        setCachedAddressLines(null);
        setCachedProfilePictureUrl(null);
        setCachedCountry(null);
        setCachedCounty(null);
        setCachedCity(null);
        setCachedCommitteeIds(null);
        setCachedAreaOfPracticeIds(null);
        setCachedBiography(null);
        setCachedEmail(null);
        setCachedPhone(null);
        setCachedId(-1);
        setCachedState(null);
        setCachedZip(null);
        setPublic(false);
    }

    /*
        CONFERENCE
     */
    public void setConferenceIsShow(boolean canShowConference) throws SQLException {
        Setting setting = new Setting(ID_CONFERENCE_IS_SHOW, canShowConference);
        createOrUpdate(setting);
    }

    public boolean getConferenceIsShow() throws SQLException {
        Setting setting = queryForId(ID_CONFERENCE_IS_SHOW);
        return setting == null || setting.isValueBoolean();
    }

    public void setConferenceId(int id) throws SQLException {
        Setting setting = new Setting(ID_CONFERENCE_ID, id);
        createOrUpdate(setting);
    }

    public int getConferenceId() throws SQLException {
        Setting setting = queryForId(ID_CONFERENCE_ID);
        return setting == null ? -1 : setting.getValueInt();
    }

    public void setConferenceUrl(String url) throws SQLException {
        Setting setting = new Setting(ID_CONFERENCE_URL, url);
        createOrUpdate(setting);
    }

    public String getConferenceUrl() throws SQLException {
        Setting setting = queryForId(ID_CONFERENCE_URL);
        return setting == null ? null : setting.getValueString();
    }

    public void setConferenceStartDate(String startDate) throws SQLException {
        Setting setting = new Setting(ID_CONFERENCE_START, startDate);
        createOrUpdate(setting);
    }

    public String getConferenceStartDate() throws SQLException {
        Setting setting = queryForId(ID_CONFERENCE_START);
        return setting == null ? null : setting.getValueString();
    }

    public void setConferenceFinishDate(String endData) throws SQLException {
        Setting setting = new Setting(ID_CONFERENCE_FINISH, endData);
        createOrUpdate(setting);
    }

    public String getConferenceFinishDate() throws SQLException {
        Setting setting = queryForId(ID_CONFERENCE_FINISH);
        return setting == null ? null : setting.getValueString();
    }

    public void setConferenceName(String name) throws SQLException {
        Setting setting = new Setting(ID_CONFERENCE_NAME, name);
        createOrUpdate(setting);
    }

    public String getConferenceName() throws SQLException {
        Setting setting = queryForId(ID_CONFERENCE_NAME);
        return setting == null ? null : setting.getValueString();
    }

    public void setConferenceVenue(String name) throws SQLException {
        Setting setting = new Setting(ID_CONFERENCE_VENUE, name);
        createOrUpdate(setting);
    }

    public String getConferenceVenue() throws SQLException {
        Setting setting = queryForId(ID_CONFERENCE_VENUE);
        return setting == null ? null : setting.getValueString();
    }

    public String getGCMRegistrationToken() throws SQLException {
        Setting setting = queryForId(ID_GCM_TOKEN);
        return setting == null ? null : setting.getValueString();
    }

    public void setGCMRegistrationToken(String firstName) throws SQLException {
        Setting setting = new Setting(ID_GCM_TOKEN, firstName);
        createOrUpdate(setting);
    }

    public void setIsTokenRegisteredWithServer(boolean isRegistered) throws SQLException {
        Setting setting = new Setting(ID_GCM_REGISTERED_WITH_SERVER, isRegistered);
        createOrUpdate(setting);
    }

    public boolean getIsTokenRegisteredWithServer() throws SQLException {
        Setting setting = queryForId(ID_GCM_REGISTERED_WITH_SERVER);
        return setting == null || setting.isValueBoolean();
    }
}
