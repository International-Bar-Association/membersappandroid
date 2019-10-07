package com.ibamembers.conference.event.db;

import android.content.Context;

import com.ibamembers.conference.event.job.ConferenceEventResponse;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

public class ConferenceEventDao extends BaseDaoImpl<ConferenceEvent, Integer> {

    static final String COLUMN_EVENT_ITEM_ID = "id";
    static final String COLUMN_CONFERENCE_ID = "conferenceId";
    static final String COLUMN_START_TIME = "startTime";
    static final String COLUMN_END_TIME = "endTime";
    static final String COLUMN_ROOM_ID = "roomId";
    static final String COLUMN_TITLE = "title";
    static final String COLUMN_SUB_TITLE = "subTitle";
    static final String COLUMN_ROOM_NAME = "roomName";
    static final String COLUMN_ROOM_CENTRE_X = "roomCentreX";
    static final String COLUMN_ROOM_CENTRE_Y = "roomCentreY";
    static final String COLUMN_FLOOR = "floor";
    static final String COLUMN_BUILDING_ID = "building";

    public ConferenceEventDao(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, ConferenceEvent.class);
    }

    public void saveEventResponseAsEvent(ConferenceEventResponse eventResponse) throws SQLException {
        ConferenceEvent conferenceEvent = new ConferenceEvent(eventResponse.getEventItemId(),
                eventResponse.getConferenceId(),
                eventResponse.getStartTime(),
                eventResponse.getEndTime(),
                eventResponse.getRoomId(),
                eventResponse.getTitle(),
                eventResponse.getSubTitle(),
                eventResponse.getRoomName(),
                eventResponse.getRoomCentreX(),
                eventResponse.getRoomCentreY(),
                eventResponse.getFloor(),
                eventResponse.getBuildingId());

            createOrUpdate(conferenceEvent);
    }

    public static ConferenceEventResponse convertEventAsEventResponse(ConferenceEvent conferenceEvent) {
        return new ConferenceEventResponse(conferenceEvent.getEventItemId(),
                conferenceEvent.getConferenceId(),
                conferenceEvent.getStartTime(),
                conferenceEvent.getEndTime(),
                conferenceEvent.getRoomId(),
                conferenceEvent.getTitle(),
                conferenceEvent.getSubTitle(),
                conferenceEvent.getRoomName(),
                conferenceEvent.getRoomCentreX(),
                conferenceEvent.getRoomCentreY(),
                conferenceEvent.getFloor(),
                conferenceEvent.getFloor());
    }
}
