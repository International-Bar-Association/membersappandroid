package com.ibamembers.conference.event

import android.content.Context
import android.graphics.PointF
import android.os.Parcelable

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.BufferedReader
import java.io.File

import java.io.IOException
import java.io.InputStream

class JsonReader(private val context: Context) {

    companion object {
        private val FORM_INPUT_NAME = "floors.json"
    }

    fun getFloorListFromJSON(index: Int):Floor? {
        val json = context.assets.open(FORM_INPUT_NAME).bufferedReader().use { it.readText() }
        val floorList = Gson().fromJson<FloorList>(json, FloorList::class.java)
        return floorList.floors.find { it.floorId == index}
    }

    fun getBuildingIdForArea(areaId: Int): Int {
        val json = context.assets.open(FORM_INPUT_NAME).bufferedReader().use { it.readText() }
        val floorList = Gson().fromJson<FloorList>(json, FloorList::class.java)
        for (floor in ArrayList(floorList.floors)) {
            for (area in ArrayList(floor.areas)) {
                if (area.areaId == areaId) {
                    return floor.floorId
                }
            }
        }

        return floorList.floors[0].floorId
    }
}


data class FloorList(@SerializedName("floors") val floors: List<Floor> = ArrayList())

data class Floor(@SerializedName("floorId") val floorId: Int = 0,
                 @SerializedName("areas") val areas: List<Area> = ArrayList())

data class Area(@SerializedName("areaId") val areaId: Int = 0,
                @SerializedName("areaPoints") val areaPoints: List<PointF> = ArrayList())
