package com.ibamembers.conference.event

import android.content.Context
import android.graphics.PointF
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatTextView
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.davemorrissey.labs.subscaleview.ImageSource
import com.ibamembers.R
import com.ibamembers.conference.event.job.ConferenceBuildingResponse
import com.ibamembers.conference.event.job.FloorResponse
import kotlinx.android.synthetic.main.conference_event_fragment.*
import java.util.*

class FloorManager {

//
//    private var conferenceBuildingList: List<ConferenceBuildingResponse> = ArrayList()
//    var conferenceBuildingMap: HashMap<Int, ConferenceBuildingResponse>? = HashMap()
//        private set
//
//    private fun buildFloors(context: Context, buildingId: Int) {
//        if (context.resources.getBoolean(R.bool.conference_show_floor_key)) {
//
//            conferenceBuildingMap!![buildingId]?.floors?.let { floors ->
//                Collections.sort(floors, FloorComparator())
//
//                floor_layout_wrapper.removeAllViews()
//                for (i in floors.indices.reversed()) {
//                    val floorLayout = AppCompatTextView(context!!)
//                    val params = LinearLayout.LayoutParams(
//                            context.resources.getDimensionPixelSize(R.dimen.conference_floor_picker_width),
//                            context.resources.getDimensionPixelSize(R.dimen.conference_floor_picker_height),
//                            1.0f)
//
//                    floorLayout.setTextColor(ContextCompat.getColor(context!!, android.R.color.black))
//                    floorLayout.layoutParams = params
//                    floorLayout.gravity = Gravity.CENTER
//                    floorLayout.text = floors[i]!!.name
//                    floorLayout.setBackgroundResource(R.color.floor_key_background_unpressed)
//                    floorLayout.setOnClickListener {
//                        resetAllFloorLayouts()
//                        setFloorClickedBackground(floorLayout)
//                        onFloorClicked(currentBuildingNumber, i, null)
//                    }
//
//                    floor_layout_wrapper.addView(floorLayout)
//                }
//            }
//
//            val lastIndex = conferenceBuildingList[0].floors?.size?:0
//            if (lastIndex != 0) setFloorClickedBackground(floor_layout_wrapper.getChildAt(lastIndex - 1) as AppCompatTextView)
//
//        } else {
//            floor_layout_wrapper.visibility = View.GONE
//        }
//    }
//
//    private fun resetAllFloorLayouts() {
//        for (i in 0 until floor_layout_wrapper.childCount) {
//            val nextChild = floor_layout_wrapper.getChildAt(i) as AppCompatTextView
//            nextChild.setBackgroundColor(ContextCompat.getColor(app!!, R.color.floor_key_background_unpressed))
//            nextChild.setTextColor(ContextCompat.getColor(app!!, R.color.floor_key_font_unpressed))
//        }
//    }
//
//    /**
//     * Set floor key (Optional)
//     */
//    private fun setFloorKeyClicked(newBuildingId: Int, floor: Int, point: PointF?, tapState: ConferenceEventActivity.ScheduleTapState){
//        if (this.currentBuildingNumber != newBuildingId) buildFloors(newBuildingId)
//
//        val floorCount = conferenceBuildingMap!![newBuildingId]?.floors?.size?:0
//        if (floorCount > 0) {
//            //We need to get the inverted index as the index for the layout starts at the top
//            var index = 0
//            for (i in floorCount - 1 downTo 0) {
//                if (floor == index) {
//                    index = i
//                    break
//                }
//                index++
//            }
//
//            //TODO
//            clearAreas()
//            //TODO FLOOR: reset floor layout
//            //resetAllFloorLayouts()
//            setFloorClickedBackground(floor_layout_wrapper.getChildAt(index) as AppCompatTextView)
//            setupFloorMapsWithFloorsOnly(newBuildingId, floor, point, tapState)
//        }
//    }
//
//    private fun setFloorClickedBackground(floorTextView: AppCompatTextView) {
//        floorTextView.setBackgroundColor(ContextCompat.getColor(context!!, R.color.floor_key_background_pressed))
//        floorTextView.setTextColor(ContextCompat.getColor(context!!, R.color.floor_key_font_pressed))
//    }
//
//    /**
//     * Use this method to animate between top or bottom floors from current floor. If already on floor then just add pin
//     */
//    private fun setupFloorMapsWithFloorsOnly(newBuildingId: Int, nextFloor: Int, point: PointF?, tapState: ConferenceEventActivity.ScheduleTapState) {
//        val drawable: String
//        val buildingList = Arrays.asList(*resources.getStringArray(R.array.building_name_list))
//
//        if ((currentFloor != nextFloor || this.currentBuildingNumber != newBuildingId) && conferenceBuildingMap != null && newBuildingId != -1 && conferenceBuildingMap!![newBuildingId]?.buildingName != "Offsite") {
//
//            mapFragment!!.view!!.visibility = View.INVISIBLE
//
//            if (conferenceBuildingMap!![newBuildingId]?.buildingName == "La Nuvola") {
//                when (nextFloor) {
//                    0    -> drawable = "coex_floor_1F.jpg"
//                    1    -> drawable = "coex_floor_2F.jpg"
//                    2    -> drawable = "la_nuvola_forum.jpg"
//                    else -> drawable = "la_nuvola_auditorium.jpg"
//                }
//
//                building_selector.text = buildingList[0]
//            } else { //if (conferenceBuildingMap.get(currentBuildingNumber).getBuildingName().equals("Palazzo Congressi")) {
//                when (nextFloor) {
//                    1    -> drawable = "palazzo_congressi_first.jpg"
//                    else -> drawable = "palazzo_congressi_ground.jpg"
//                }
//                building_selector.text = buildingList[1]
//            }
//
//            /**
//             * 3 layers is used to animate the scale up/down animation. Bottom Layer is the scalable view, middle and top layers are ImageViews to perform the animations.
//             * The 1st layer needs to reset its view when a new image is set after performing an animation and this causes the animation to flicker. This is why we use 2 extra layers to hide this flicker
//             * while performing the animation.
//             *
//             */
//
//            //Only perform this step if the
//            //if (!(nextFloor == 0 && currentFloor == -1) || !(newBuildingId == 0 && this.currentBuildingNumber == -1)) {
//
//            //dont animate if we are selecting maps
//            if (newBuildingId != conferenceBuildingList[conferenceBuildingList.size - 1].buildingId) {
//
//                var animationDirection: ImageAnimator.AnimationDirection? = null
//                val nextDrawable = getFloorImageStringForFloorId(nextFloor)
//                if (nextDrawable != null) {
//                    when {
//                        nextFloor < currentFloor -> animationDirection = ImageAnimator.AnimationDirection.Down
//                        nextFloor > currentFloor -> animationDirection = ImageAnimator.AnimationDirection.Up
//                        currentBuildingNumber != newBuildingId -> animationDirection = ImageAnimator.AnimationDirection.None
//                    }
//
//                    ImageAnimator().animateImage(
//                            getFloorImageString(nextFloor),
//                            animationDirection,
//                            point,
//                            tapState,
//                            floor_plan_image_top,
//                            floor_plan_image_dummy,
//                            floor_plan_image_top_animator,
//                            floor_plan_image_bottom_animator
//                    ) { point, tapState ->
//                        setPinOnImageLoaded(point, tapState)
//                        currentFloor = nextFloor
//                        currentBuildingNumber = newBuildingId
//                    }
//                } else {
//                    Toast.makeText(context, "Floor undefined", Toast.LENGTH_SHORT).show()
//                }
//
//
//            } else {
//                //Initial image loading
//                floor_plan_image_bottom_animator.visibility = View.GONE
//                floor_plan_image_top_animator.visibility = View.GONE
//
//                setPinOnImageLoaded(point, tapState)
//                floor_plan_image_top.setImage(ImageSource.asset(drawable))
//
//                currentFloor = nextFloor
//                currentBuildingNumber = newBuildingId
//            }
//
//        } else {
//            pinMap(point, tapState, true, false)
//        }
//    }
//
//    inner class FloorComparator : Comparator<FloorResponse> {
//        override fun compare(left: FloorResponse, right: FloorResponse): Int {
//            return right.floorIndex?.let { left.floorIndex!!.compareTo(it) }!!
//        }
//    }

}