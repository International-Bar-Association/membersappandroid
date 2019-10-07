package com.ibamembers.conference.event.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable
public class ConferenceEvent {

    @DatabaseField(id = true, index = true, columnName = ConferenceEventDao.COLUMN_EVENT_ITEM_ID)
    private int id;

    @DatabaseField(columnName = ConferenceEventDao.COLUMN_CONFERENCE_ID)
    private int conferenceId;

    @DatabaseField(columnName = ConferenceEventDao.COLUMN_START_TIME)
    private Date startTime;

    @DatabaseField(columnName = ConferenceEventDao.COLUMN_END_TIME)
    private Date endTime;

    @DatabaseField(columnName = ConferenceEventDao.COLUMN_ROOM_ID)
    private int roomId;

    @DatabaseField(columnName = ConferenceEventDao.COLUMN_TITLE)
    private String title;

    @DatabaseField(columnName = ConferenceEventDao.COLUMN_SUB_TITLE)
    private String subTitle;

    @DatabaseField(columnName = ConferenceEventDao.COLUMN_ROOM_NAME)
    private String roomName;

    @DatabaseField(columnName = ConferenceEventDao.COLUMN_ROOM_CENTRE_X)
    private int roomCentreX;

    @DatabaseField(columnName = ConferenceEventDao.COLUMN_ROOM_CENTRE_Y)
    private int roomCentreY;

    @DatabaseField(columnName = ConferenceEventDao.COLUMN_FLOOR)
    private int floor;

    @DatabaseField(columnName = ConferenceEventDao.COLUMN_BUILDING_ID)
    private int buildingId;

    public ConferenceEvent() {
    }

    public ConferenceEvent(int eventItemId, int conferenceId, Date startTime, Date endTime, int roomId, String title, String subTitle, String roomName, int roomCentreX, int roomCentreY, int floor, int buildingId) {
        this.id = eventItemId;
        this.conferenceId = conferenceId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.roomId = roomId;
        this.title = title;
        this.subTitle = subTitle;
        this.roomName = roomName;
        this.roomCentreX = roomCentreX;
        this.roomCentreY = roomCentreY;
        this.floor = floor;
        this.buildingId = buildingId;
    }

    public int getEventItemId() {
        return id;
    }

    public int getConferenceId() {
        return conferenceId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public String getRoomName() {
        return roomName;
    }

    public int getRoomCentreX() {
        return roomCentreX;
    }

    public int getRoomCentreY() {
        return roomCentreY;
    }

    public int getFloor() {
        return floor;
    }

    public int getBuildingId() {
        return buildingId;
    }
}
