package com.ibamembers.app

import com.ibamembers.conference.event.job.ConferenceEventResponse

class KotlinUtils{

    companion object {
        fun compareEvents(conferenceEventList: List<ConferenceEventResponse> ): List<ConferenceEventResponse> {
            return conferenceEventList.sortedWith( compareBy ({ it.getStartTime()} , {it.title}))
        }
    }

}
