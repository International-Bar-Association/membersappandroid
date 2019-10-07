package com.ibamembers.conference.event.job

import com.google.gson.annotations.SerializedName
import com.ibamembers.conference.event.ConferenceEventActivity

import org.joda.time.DateTime

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ConferenceEventResponse(@field:SerializedName("EventItemId")
                              val eventItemId: Int,
                              @field:SerializedName("ConferenceId")
                              val conferenceId: Int,
                              @field:SerializedName("StartTime")
                              private var startTime: Date?,
                              @field:SerializedName("EndTime")
                              private var endTime: Date?,
                              @field:SerializedName("RoomId")
                              val roomId: Int,
                              @field:SerializedName("Title")
                              val title: String,
                              @field:SerializedName("SubTitle")
                              val subTitle: String,
                              @field:SerializedName("RoomName")
                              var roomName: String?,
                              @field:SerializedName("RoomCentreX")
                              var roomCentreX: Int,
                              @field:SerializedName("RoomCentreY")
                              var roomCentreY: Int,
                              @field:SerializedName("Floor")
                              var floor: Int, @field:SerializedName("BuildingId")
                              var buildingId: Int) {

    @SerializedName("StartTimeString")
    var startTimeString: String? = null
    @SerializedName("EndTimeString")
    var endTimeString: String? = null
    @SerializedName("Attending")
    var isAttending: Boolean = false
    @SerializedName("Lat")
    var latitude: Float = 0.toFloat()
    @SerializedName("Long")
    var longitude: Float = 0.toFloat()

    companion object{
        const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
    }

    var isAnchored: Boolean = false
    var isFavourite: Boolean = false

    var tapState: ConferenceEventActivity.ScheduleTapState? = null
        get() {
            return if (field == null) {
                this.tapState = ConferenceEventActivity.ScheduleTapState.None
                field
            } else {
                if (isCurrent) {
                    ConferenceEventActivity.ScheduleTapState.Current
                } else field
            }
        }

    val isCurrent: Boolean
        get() {
            if (startTime != null && endTime != null) {
                val tz = Calendar.getInstance().timeZone
                val offsetFromCurrentTimezone = tz.getOffset(System.currentTimeMillis()).toLong()
                val hourDifference = (offsetFromCurrentTimezone * -1) / (1000 * 60 * 60)

                //remove added timezone from time
                val startDateTime = DateTime(startTime).plusHours(hourDifference.toInt())
                val endDateTime = DateTime(endTime).plusHours(hourDifference.toInt())
                val currentDate = Date()

                return startDateTime.isBefore(currentDate.time) && endDateTime.isAfter(currentDate.time)
            }
            return false
        }

    init {
        this.isAnchored = false
    }

    fun getStartTime(): Date? {
        //used for mocks
        if (startTimeString != null) {
            val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            return try {
                sdf.parse(startTimeString)
            } catch (e: ParseException) {
                e.printStackTrace()
                null
            }

        }

        return startTime
    }

    fun getEndTime(): Date? {
        //used for mocks
        if (endTimeString != null) {
            val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            return try {
                sdf.parse(endTimeString)
            } catch (e: ParseException) {
                e.printStackTrace()
                null
            }
        }

        return endTime
    }

    fun setStartTime(startTime: Date) {
        this.startTime = startTime
    }

    fun setEndTime(endTime: Date) {
        this.endTime = endTime
    }
}
