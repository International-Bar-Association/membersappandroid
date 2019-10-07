package com.ibamembers.conference.event

import com.ibamembers.app.App
import com.ibamembers.conference.event.job.ConferenceBuildingEventResponse
import com.ibamembers.conference.event.job.ConferenceBuildingResponse
import com.ibamembers.conference.event.job.ConferenceEventResponse
import com.ibamembers.conference.event.job.FloorResponse
import org.joda.time.DateTime
import java.util.*

class MockData{

    companion object {

        fun getConferenceEventResponse(app: App): ConferenceBuildingEventResponse {

            val favouriteList = app.listOfFavouriteSchedule

            return ConferenceBuildingEventResponse().apply {
                val tomorrow = DateTime().plusDays(1)

                eventList = ArrayList<ConferenceEventResponse>().apply {
                    add(ConferenceEventResponse(0, 1, DateTime().toDate(), DateTime().plusHours(1).toDate(), 0,
                            "Rule of Law Symposium: what is business for the rule of law)", "Conference Room (North)", "Room name1", 300, 300, 0, 4)
                            .apply { startTimeString = "2019-09-23T06:00:00.000+00:00"
                                     endTimeString = "2019-09-23T07:15:00.000+00:00"})
                    add(ConferenceEventResponse(1, 1,  DateTime().plusHours(1).toDate(), DateTime().plusHours(2).toDate(), 0,
                            "Rule of Law Symposium: what is business for the rule of law", "Conference Room (North)", "Room name2", 400, 400, 1, 5)
                            .apply { startTimeString = "2019-09-24T06:00:00.000+00:00"
                                     endTimeString = "2019-09-24T07:15:00.000+00:00"})
                    add(ConferenceEventResponse(2, 1, DateTime().plusDays(1).toDate(), DateTime().plusDays(1).plusHours(1).toDate(), 1,
                            "LPD Showcase: initial coin offerings (ICO)", "Title2", "Conference Room (South)", 500, 500, 1, 6)
                            .apply { startTimeString = "2019-09-25T06:00:00.000+00:00"
                                     endTimeString = "2019-09-25T07:15:00.000+00:00"})
                    add(ConferenceEventResponse(3, 1, DateTime().plusDays(1).plusHours(1).toDate(), DateTime().plusDays(1).plusHours(2).toDate(), 1,
                            "LPD Showcase: initial coin offerings (ICO)", "Title2", "Conference Room (South)", 600, 600, 2, 6)
                            .apply { startTimeString = "2019-09-26T06:00:00.000+00:00"
                                     endTimeString = "2019-09-26T07:15:00.000+00:00"})

                    add(ConferenceEventResponse(4, 1,tomorrow.plusDays(2).plusHours(1).toDate(), tomorrow.plusDays(2).plusHours(2).toDate(), 2,
                            "IBA Showcase: cybersecurity - launch of IBA guidelines", "Title2", "Conference Room (West)", 100, 100, 1, 6)
                            .apply { startTimeString = "2019-09-27T06:00:00.000+00:00"
                                     endTimeString = "2019-09-27T07:15:00.000+00:00"})
                    add(ConferenceEventResponse(5, 1, tomorrow.plusHours(2).toDate(), tomorrow.plusHours(3).toDate(), 2,
                            "IBA Showcase: cybersecurity - launch of IBA guidelines", "Title2", "Conference Room (East)", 100, 100, 1, 6))

                    add(ConferenceEventResponse(6, 1, tomorrow.plusHours(2).toDate(), tomorrow.plusHours(3).toDate(), 2,
                            "Offsite 1", "region 1", "Room name3", 100, 100, 0, 7)
                            .apply {
                                latitude = 37.54200f
                                longitude = 126.991892f
                                buildingId = 4
                            })

                    add(ConferenceEventResponse(7, 1, tomorrow.plusHours(2).toDate(), tomorrow.plusHours(3).toDate(), 2,
                            "Offsite 2", "region 2", "Room name3", 100, 100, 0, 7)
                            .apply {
                                latitude = 37.541391f
                                longitude = 127.012170f
                                buildingId = 4
                            })
                    }

                eventList!!.map { it.apply { isFavourite = favouriteList.contains(it.eventItemId) }}

//                buildingList = ArrayList<ConferenceBuildingResponse>().apply {
//                    add(ConferenceBuildingResponse(0, "FloorResponse 1F",
//                            ArrayList<FloorResponse>().apply {
//                                add(FloorResponse("Hall A", 0))
//                                add(FloorResponse("Hall B", 1))
//                                add(FloorResponse("Lobby", 2))}))
//                    add(ConferenceBuildingResponse(1, "FloorResponse 2F",
//                            ArrayList<FloorResponse>().apply {
//                                add(FloorResponse("Hall A", 0))}))
//                    add(ConferenceBuildingResponse(2, "FloorResponse 3F",
//                            ArrayList<FloorResponse>().apply {
//                                add(FloorResponse("Hall A", 0))}))
//                    add(ConferenceBuildingResponse(3, "FloorResponse 4F",
//                            ArrayList<FloorResponse>().apply {
//                                add(FloorResponse("Hall A", 0))}))
//                    add(ConferenceBuildingResponse(4, "Offsite",
//                            ArrayList<FloorResponse>().apply {
//                                add(FloorResponse("Hall A", 0))}))
//                }
                buildingList = ArrayList<ConferenceBuildingResponse>().apply {
                    add(ConferenceBuildingResponse(4, "1F ",
                            ArrayList<FloorResponse>().apply {
                                add(FloorResponse("1F_exhibition_hall_A", 0))}))
                    add(ConferenceBuildingResponse(5, "2F",
                            ArrayList<FloorResponse>().apply {
                                add(FloorResponse("1F_exhibition_hall_B", 1))}))
                    add(ConferenceBuildingResponse(6, "3F",
                            ArrayList<FloorResponse>().apply {
                                add(FloorResponse("3F_conference_room_south", 2))
                                add(FloorResponse("3F_conference_room_E", 3))}))
                    add(ConferenceBuildingResponse(7, "Offsite",
                            ArrayList()))
                    add(ConferenceBuildingResponse(8, "4F",
                            ArrayList<FloorResponse>().apply {
                                add(FloorResponse("coex_zone_4F_room_south", 1))}))
                }
            }
        }

    }

}