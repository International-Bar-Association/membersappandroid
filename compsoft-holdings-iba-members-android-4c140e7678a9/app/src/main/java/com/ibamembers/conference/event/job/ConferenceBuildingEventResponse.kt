package com.ibamembers.conference.event.job

import com.google.gson.annotations.SerializedName

import java.util.ArrayList

class ConferenceBuildingEventResponse {
    @SerializedName("Buildings")
    var buildingList: List<ConferenceBuildingResponse>? = ArrayList()
        get() = if (field != null) {
            field
        } else ArrayList()

    @SerializedName("Events")
    var eventList: List<ConferenceEventResponse>? = ArrayList()
        get() = if (field != null) {
            field
        } else ArrayList()

    /**
     * Onsite buildings should have at least 1 floor and the last element should be an offsite build
     */
    fun validateFloorsInBuildingList(): Boolean {
        var valid = true
        for (building in ArrayList(buildingList)) {
            if ((building.floors == null || building.floors.isEmpty()) && building.buildingName != "Offsite") {
                valid = false
                break
            }
        }

        if (buildingList?.last()?.buildingName != "Offsite") valid = false

        return valid
    }
}

class ConferenceBuildingResponse @JvmOverloads constructor(
        @SerializedName("BuildingId") val buildingId: Int = 0,
        @SerializedName("BuildingName") val buildingName: String? = null,
        @SerializedName("Floors") val floors: List<FloorResponse>? = null)

class FloorResponse @JvmOverloads constructor(
    @SerializedName("Name") val name: String? = null,
    @SerializedName("FloorIndex") val floorIndex: Int? = null
)