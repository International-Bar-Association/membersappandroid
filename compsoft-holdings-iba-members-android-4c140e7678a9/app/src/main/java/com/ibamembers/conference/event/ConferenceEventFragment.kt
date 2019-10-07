package com.ibamembers.conference.event

import android.app.AlertDialog
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.ImageViewState
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.ibamembers.R
import com.ibamembers.app.BaseFragment
import com.ibamembers.conference.event.job.ConferenceEventResponse

import butterknife.OnClick
import com.ibamembers.app.App
import com.ibamembers.conference.event.job.ConferenceBuildingResponse
import com.ibamembers.conference.event.job.GetConferenceEventsJob
import kotlinx.android.synthetic.main.conference_event_fragment.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.sql.SQLException
import java.util.*

class ConferenceEventFragment : BaseFragment(), OnMapReadyCallback {

    /*
        Config 1: We have a list of all floor maps but no separate building map
        Config 2: We have a list of all floor maps as well as building maps
     */
    companion object {
        private const val PIN_ANIMATE_SCALE = 1f
        private const val MIN_TILE_DPI = 100
        private const val PIN_DIM = 250
        private const val MAP_ZOOM_LEVEL = 15

        private const val ASSET_FILE_PREFIX = "file:///android_asset/"
        private const val OFFSITE_MAP_ZOOM_LEVEL = 11
        private const val OFFSITE_NAME = "Offsite"

        private val DEFAULT_OFFSITE_COORD = LatLng(37.552329, 126.991088)
    }

    private var listener: ConferenceEventFragmentListener? = null

    private var isPinAnimating: Boolean = false
    private var imageViewState: ImageViewState? = null
    private var currentPinPoint: PointF? = null

    private var currentBuildingNumber = -1
    private var currentFloor = -1

    private var mapFragment: SupportMapFragment? = null
    private var googleMap: GoogleMap? = null
    private var isFirstMapLaunch: Boolean = false

    private var conferenceBuildingList: List<ConferenceBuildingResponse> = ArrayList()
    var conferenceBuildingMap: HashMap<Int, ConferenceBuildingResponse>? = HashMap()
        private set

    private var onFloorAreaClickedLiveData = MutableLiveData<Int>()
    private var currentLevelType = LevelType.Floor

    //loading
    var isLoadingConferenceEvents: Boolean = false
    var isAreaClicked = false

    enum class LevelType{
        Floor, Area
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.conference_event_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        setupGoogleMaps()
        setupInitialFloor()
        fetchConferenceEvents()

        onFloorAreaClickedLiveData.observe(this, Observer { it ->
            it?.let {
                isAreaClicked = true
                animateFloorPlan(it)
            }
        })
    }

    private fun fetchConferenceEvents(){
        if (isNetworkAvailable) {
            getConferenceEvents()
        } else {
            //Load from DB
            listener?.getConferenceEventFromDbAndAnchorPanel()
        }
    }

    private fun getConferenceEvents() {
        try {
            isLoadingConferenceEvents = true

            val settingDao = app.databaseHelper.settingDao
            val conferenceUrl = settingDao.conferenceId
            progressBar.visibility = View.VISIBLE
            app.getJobManager(App.JobQueueName.Network).addJobInBackground(GetConferenceEventsJob(conferenceUrl))

            //TODO mock
//            isLoadingConferenceEvents = false
//            onGetConferenceEventSuccess(GetConferenceEventsJob.Success(MockData.getConferenceEventResponse(app)))

        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onGetConferenceEventSuccess(success: GetConferenceEventsJob.Success) {
        progressBar.visibility = View.GONE
        this.conferenceBuildingList = ArrayList(success.response.buildingList)
        if (conferenceBuildingList.isEmpty()) return

        listener?.onConferenceEventLoaded(success) //Load events in activity
        setBuildingListAndSetDefaultBuilding()
        isLoadingConferenceEvents = false

        //TODO
        //validateAndBuildMap()

        setBuildingImage(conferenceBuildingList[0].buildingId)
    }

    private fun validateAndBuildMap(){
        //Build map floor before populating events
//            if (success.response.validateFloorsInBuildingList()) {
//                listener?.onConferenceEventLoaded(success)
//                setBuildingListAndSetDefaultBuilding()
//
//                isLoadingConferenceEvents = false
//            } else {
//                Toast.makeText(context, "Unable to build floors", Toast.LENGTH_SHORT).show()
//                activity?.finish()
//            }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onGetConferenceEventFailed(failure: GetConferenceEventsJob.Failure) {
        progressBar.visibility = View.GONE
        listener?.onConferenceEventFailed(failure)
    }

    /**
     * Onsite buildings should have at least 1 floor
     */
    private fun validateFloorsInBuildingList(buildingList: List<ConferenceBuildingResponse>): Boolean {
        var valid = true
        for (building in buildingList) {
            if ((building.floors == null || building.floors.isEmpty()) && building.buildingName != OFFSITE_NAME) {
                valid = false
            }
        }
        return valid
    }

    private fun setupGoogleMaps() {
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment!!.getMapAsync(this)
        mapFragment!!.view!!.visibility = View.INVISIBLE
    }

    private fun setupInitialFloor() {
        // the smaller this number, the smaller the chance to get an "outOfMemoryException"
        // still, values lower than 100 really do affect the quality of the pdf picture
        floor_plan_image_top.setMinimumTileDpi(MIN_TILE_DPI)
        floor_plan_image_top.setImage(ImageSource.asset("coex_floor_1F.jpg"))

        setupBuildingListSelector()

        fab_back.setOnClickListener {
            fab_back.visibility = View.GONE
            building_selector.visibility = View.VISIBLE

            var buildingIdForArea = JsonReader(activity!!).getBuildingIdForArea(currentFloor)
            if (buildingIdForArea == 7) buildingIdForArea = 4//TODO match the last buildingId (offsite)

            building_selector.text = conferenceBuildingMap?.get(buildingIdForArea)?.buildingName
            conferenceBuildingMap
            setBuildingImage(JsonReader(activity!!).getBuildingIdForArea(currentFloor))
            currentLevelType = LevelType.Floor
            clearPins()
        }

        floor_plan_image_top.setOnClickListener {
            Log.i("ConferenceEvent", "Top image clicked!!")
            listener?.onImageTapped(isAreaClicked)
            isAreaClicked = false
        }

    }

    /**
     * Call this after buildings are fetched from api to load default floor
     * @param buildings Building list
     */
    private fun setBuildingListAndSetDefaultBuilding() {
        //convert list to map
        val buildingMap = HashMap<Int, ConferenceBuildingResponse>()
        for (building in conferenceBuildingList) {
            buildingMap[building.buildingId] = building
        }

        if (conferenceBuildingList.isNotEmpty()) {
            this.conferenceBuildingMap = buildingMap
            this.currentFloor = 0

            //TODO build floors
            //buildFloors(0)
        }
    }

    private fun getBuildingStringName(): List<String> {
        return listOf(*resources.getStringArray(R.array.building_name_list))
    }

    private fun setupBuildingListSelector() {
        building_selector.text = getBuildingStringName()[0]
        building_selector.setOnClickListener {
            onBuildingSelectorClicked()
        }

        offsite_label.isClickable = true
        offsite_label.setOnClickListener {
            onBuildingSelectorClicked()
        }
    }

    private fun getBuildingList(): List<String>{
        return conferenceBuildingMap?.values?.map { it.buildingName!! }?: ArrayList()
    }

    private fun onBuildingSelectorClicked(){
        val buildingList = ArrayList(getBuildingList())
                .apply { removeAll{it == OFFSITE_NAME} }.sortedBy {it}
        if (buildingList.isNotEmpty()) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("Select Area")
                    .setItems(buildingList.toTypedArray<CharSequence>()) { _, i ->
                        building_selector.text = buildingList[i]

                        val filteredConferenceList = ArrayList(conferenceBuildingList)
                                .apply { removeAll{it.buildingName == OFFSITE_NAME} }.sortedBy {it.buildingName}

                        if (buildingList[i] != OFFSITE_NAME) {
                            mapFragment!!.view!!.visibility = View.INVISIBLE
                            offsite_label.visibility = View.INVISIBLE
                            setBuildingImage(filteredConferenceList[i].buildingId)
                        } else {
                            mapFragment!!.view!!.visibility = View.VISIBLE
                            offsite_label.visibility = View.VISIBLE
                            building_selector.visibility = View.INVISIBLE
                        }
                    }
                    .create().show()
        }
    }

    /**
     * Swap building images
     */
    private fun setBuildingImage(buildingId: Int) {
        offsite_label.visibility = View.GONE
        fab_back.visibility = View.GONE
        building_selector.visibility = View.VISIBLE

        if (buildingId != this.currentBuildingNumber || this.currentLevelType != LevelType.Floor) {
            this.currentBuildingNumber = buildingId
            val drawable = getBuildingImageString(buildingId)
            drawable?.let {
                floor_plan_image_top.setImage(ImageSource.asset(drawable))
                setAreasOnImageLoaded(buildingId, null)
            }
        }
    }

    private fun setFloorImage(areaId: Int, newBuildingId: Int): Boolean {
        this.currentLevelType = LevelType.Area
        currentFloor = areaId
        //val drawable = getFloorImageString(areaId)
        val drawable = getFloorImageStringForFloorId(areaId, newBuildingId) ?: return false

        floor_plan_image_top.setImage(ImageSource.asset(drawable))
        setAreasOnImageLoaded(0, areaId)

        building_selector.visibility = View.INVISIBLE
        fab_back.visibility = View.VISIBLE
        return true
    }

    private fun getFloorImageStringForFloorId(floorId: Int, newBuildingId: Int): String? {
        conferenceBuildingList

        var floorName: String? = null

        for (conferenceBuilding in conferenceBuildingList) {
            if (conferenceBuilding.buildingId == newBuildingId) {
                conferenceBuilding.floors?.let {
                    for (floors in it) {
                        if (floors.floorIndex == floorId) {
                            floorName = floors.name
                            break
                        }
                    }
                }
            }
        }

        floorName?.let {
            return when (it) {
                "1F_exhibition_hall_A" -> "coex_zone_1F_hall_A.jpg"
                "1F_exhibition_hall_B" -> "coex_zone_1F_hall_B.jpg"
                "1F_grand_ballroom" -> "coex_zone_1F_grand_ballroom.jpg"
                "2F_conference_room_north" -> "coex_zone_2F_room_north.jpg"
                "3F_conference_room_south" -> "coex_zone_3F_room_south.jpg"
                "3F_conference_room_E" -> "coex_zone_3F_room_E.jpg"
                "3F_exhibition_hall_C" -> "coex_zone_3F_hall_C.jpg"
                "3F_exhibition_hall_D" -> "coex_zone_3F_hall_D.jpg"
                "3F_auditorium" -> "coex_zone_3F_auditorium.jpg"
                else -> "coex_zone_4F_room_south.jpg"
            }
        }?:return null
    }

    private fun getFloorImageString(areaId: Int):String {
        return when (areaId) {
            0 -> "coex_zone_1F_hall_A.jpg"
            1 -> "coex_zone_1F_hall_B.jpg"
            2 -> "coex_zone_1F_grand_ballroom.jpg"
            3 -> "coex_zone_2F_room_north.jpg"
            4 -> "coex_zone_3F_room_south.jpg"
            5 -> "coex_zone_3F_room_E.jpg"
            6 -> "coex_zone_3F_hall_C.jpg"
            7 -> "coex_zone_3F_hall_D.jpg"
            8 -> "coex_zone_3F_auditorium.jpg"
            else -> "coex_zone_4F_room_south.jpg"
        }
    }

    private fun getBuildingImageString(buildingId: Int): String? {
        return when (buildingId) {
            4 -> "coex_floor_1F.jpg"
            5 -> "coex_floor_2F.jpg"
            6 -> "coex_floor_3F.jpg"
            8 -> "coex_floor_4F.jpg"
            else -> null
        }
    }


    private fun animateFloorPlan(areaId: Int) {
        this.currentLevelType = LevelType.Area

        ImageAnimator().animateImage(
                getFloorImageString(areaId),
                ImageAnimator.AnimationDirection.Up,
                null,
                ConferenceEventActivity.ScheduleTapState.None,
                floor_plan_image_top,
                floor_plan_image_dummy,
                floor_plan_image_top_animator,
                floor_plan_image_bottom_animator
        ) { _, _ ->
            setAreasOnImageLoaded(0, areaId)
            currentFloor = areaId

            building_selector.visibility = View.INVISIBLE
            fab_back.visibility = View.VISIBLE
            offsite_label.visibility = View.GONE
        }
    }

    private fun setupFloorPinsWithBuildingPlan(newBuildingId: Int,nextFloor: Int, point: PointF?, tapState: ConferenceEventActivity.ScheduleTapState, isImagePreviouslyLoaded: Boolean){
        mapFragment!!.view!!.visibility = View.INVISIBLE
        building_selector.visibility = View.INVISIBLE
        fab_back.visibility = View.VISIBLE
        offsite_label.visibility = View.GONE

        val nextDrawable = getFloorImageStringForFloorId(nextFloor, newBuildingId)
        val floorIsValid = doesFloorBelongToBuidlingId(nextFloor, newBuildingId)

        if (nextDrawable != null && floorIsValid) {
            if (currentLevelType == LevelType.Area) {
                // in the case building is floor and floor is area
                if (point != null) {
                    //pinMap(point, tapState, true, isImagePreviouslyLoaded)

                    if (!(currentFloor == nextFloor && currentBuildingNumber == newBuildingId)) {
                        var animationDirection: ImageAnimator.AnimationDirection? = null

                        when {
                            nextFloor < currentFloor -> animationDirection = ImageAnimator.AnimationDirection.Down
                            nextFloor > currentFloor -> animationDirection = ImageAnimator.AnimationDirection.Up
                            currentBuildingNumber != newBuildingId -> animationDirection = ImageAnimator.AnimationDirection.None
                        }

                        ImageAnimator().animateImage(
                                nextDrawable,
                                animationDirection,
                                point,
                                tapState,
                                floor_plan_image_top,
                                floor_plan_image_dummy,
                                floor_plan_image_top_animator,
                                floor_plan_image_bottom_animator
                        ) { point, tapState ->
                            setPinOnImageLoaded(point, tapState)
                            currentFloor = nextFloor
                            currentBuildingNumber = newBuildingId
                        }

                    } else {
                        pinMap(point, tapState, true, false)
                    }
                } else {
                    clearPins()
                }
            }
            //Schedule is clicked while still at building level. so set area image and build pins
            else {
                if (setFloorImage(nextFloor, newBuildingId)) {
                    currentFloor = nextFloor
                    currentLevelType = LevelType.Area

                    pinMap(point, tapState, true, true)
                }
            }
        }
    }

    fun doesFloorBelongToBuidlingId(floorId: Int, buildingId: Int): Boolean{
        val conference = conferenceBuildingMap?.get(buildingId)
        val floors = conference?.floors
        floors?.let {
            for (floor in floors){
                if (floor.floorIndex == floorId) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Call then we a new image is set to floorImageScalableView. You can only pin maps once the view is Ready.
     * @param point
     * @param tapState
     */
    private fun setPinOnImageLoaded(point: PointF?, tapState: ConferenceEventActivity.ScheduleTapState) {
        performActionOnImageLoaded{
            if (point != null) {
                pinMap(point, tapState, isZoomedOut = false, isImagePreviouslyLoaded = false)
            } else {
                clearPins()
                floor_plan_image_top.setOnImageEventListener(null)
            }
        }
    }

    private fun setAreasOnImageLoaded(floorId: Int, roomId: Int?) {
        performActionOnImageLoaded{
            if (roomId == null) {
                setAreasOnFloor(floorId)
            } else {
                clearAreas()
            }
        }
    }

    private fun performActionOnImageLoaded(action:() -> Unit){
        floor_plan_image_top.setOnImageEventListener(object : SubsamplingScaleImageView.OnImageEventListener {
            override fun onImageLoaded() { action() }
            override fun onReady() {}
            override fun onPreviewLoadError(e: Exception) {}
            override fun onImageLoadError(e: Exception) {}
            override fun onTileLoadError(e: Exception) {}
            override fun onPreviewReleased() {}
        })
    }

    private fun setAreasOnFloor(floorId: Int){
        clearAreas()

        val floor = JsonReader(activity!!).getFloorListFromJSON(floorId)
        floor?.let {
            floor_plan_image_top.setAreas(it.areas, onFloorAreaClickedLiveData)
        }

    }

    private fun pinMap(point: PointF?, tapState: ConferenceEventActivity.ScheduleTapState, isZoomedOut: Boolean = false, isImagePreviouslyLoaded: Boolean) {
        if (point != null) {
            if (isImagePreviouslyLoaded) {
                performActionOnImageLoaded { setPin(point, tapState) }
            } else {
                setPin(point, tapState)
            }
        } else {
            clearPins()
        }
    }

    private fun setPin(point: PointF, tapState: ConferenceEventActivity.ScheduleTapState){
        var isPinPointDifferent = false
        if (point != currentPinPoint) {
            isPinPointDifferent = true
            currentPinPoint = point
        }

        floor_plan_image_top.setPin(point, tapState)
        isPinAnimating = true
        val builder = floor_plan_image_top.animateScaleAndCenter(PIN_ANIMATE_SCALE, point)
        builder.withDuration(500)
                .withInterruptible(false)
                .withOnAnimationEventListener(object : SubsamplingScaleImageView.OnAnimationEventListener {
                    override fun onComplete() {
                        isPinAnimating = false
                    }

                    override fun onInterruptedByUser() {}
                    override fun onInterruptedByNewAnim() {}
                }).start()
    }

    private fun clearPins() {
        floor_plan_image_top.setPin(null, ConferenceEventActivity.ScheduleTapState.None)
    }

    private fun clearAreas() {
        if (floor_plan_image_top.isReady) {
            floor_plan_image_top.setAreas(null, onFloorAreaClickedLiveData)
        }
    }

    @JvmOverloads
    fun setPinOnFloorOnScheduleClicked(conferenceEventResponse: ConferenceEventResponse,
                                       tapState: ConferenceEventActivity.ScheduleTapState = ConferenceEventActivity.ScheduleTapState.Selected) {
        if (!isPinAnimating) {
            if (conferenceBuildingMap == null) return
            val buildingName = conferenceBuildingMap!![conferenceEventResponse.buildingId]?.buildingName
            buildingName?.let {
                //EventUtils.Room room = EventUtils.getRoomFromJSON(getContext(), roomId);
                if (buildingName != OFFSITE_NAME) {
                    val point = PointF(conferenceEventResponse.roomCentreX.toFloat(), conferenceEventResponse.roomCentreY.toFloat())
                    onFloorClicked(conferenceEventResponse.buildingId, conferenceEventResponse.floor, point, tapState) //TODO grab building from response
                } else {
                    onMapEventClicked(conferenceEventResponse.latitude, conferenceEventResponse.longitude, conferenceEventResponse.roomName)
                }
            }
        }
    }

    //	public void setFabNow(boolean showFab) {
    //		ConferenceEventActivity activity = (ConferenceEventActivity) getActivity();
    //		if (showFab && activity.getAdapter().eventListHasCurrent()) {
    //			fabNow.show();
    //		} else {
    //			fabNow.hide();
    //		}
    //	}

    @OnClick(R.id.fab_now)
    fun onFabNowClicked() {
        //listener.fabNowClicked();
    }


    /**
     * 1. Builds floor index of new building
     * 2. Build floor map of new building
     */
    private fun onFloorClicked(newBuildingId: Int, floor: Int, point: PointF?, tapState: ConferenceEventActivity.ScheduleTapState = ConferenceEventActivity.ScheduleTapState.Selected) {
        if (!isPinAnimating) {
            if (resources.getBoolean(R.bool.conference_show_floor_key)) {
                //TODO FLOOR update floor layout when floor is clicked
                //setFloorKeyClicked(newBuildingId, floor, point, tapState)
            }

            clearAreas()
            setupFloorPinsWithBuildingPlan(newBuildingId, floor, point, tapState, false)
        }
    }



    private fun onMapEventClicked(latitude: Float, longitude: Float, siteName: String?) {
        building_selector.text = conferenceBuildingList[conferenceBuildingList.size - 1].buildingName
        mapFragment!!.view!!.visibility = View.VISIBLE
        fab_back.visibility = View.GONE
        building_selector.visibility = View.INVISIBLE
        offsite_label.visibility = View.VISIBLE
        offsite_label_english.text = siteName

        val conferenceBuilding = conferenceBuildingList[conferenceBuildingList.size - 1]
        currentBuildingNumber = conferenceBuilding.buildingId

        setMapMarker(latitude, longitude)
    }

    fun toggleProgressbar(isShow: Boolean) {
        progressBar!!.visibility = if (isShow) View.VISIBLE else View.INVISIBLE
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        if (!isFirstMapLaunch) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_OFFSITE_COORD, OFFSITE_MAP_ZOOM_LEVEL.toFloat()))
            isFirstMapLaunch = true
        }

    }

    private fun setMapMarker(latitude: Float, longitude: Float) {
        if (this.googleMap != null) {
            val testMarker = LatLng(latitude.toDouble(), longitude.toDouble())

            googleMap!!.clear()
            googleMap!!.addMarker(MarkerOptions().position(testMarker).icon(bitmapDescriptorFromVector(activity, R.drawable.schedule_map_pin_selected)))
            googleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(testMarker, MAP_ZOOM_LEVEL.toFloat()))
            googleMap!!.uiSettings.isZoomControlsEnabled = true
            googleMap!!.setOnMarkerClickListener {
                (activity as ConferenceEventActivity).collapseSlidingPanel()
                false
            }
        }
    }

    private fun bitmapDescriptorFromVector(context: Context?, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context!!, vectorResId)
        vectorDrawable!!.setBounds(0, 0, PIN_DIM, PIN_DIM)
        val bitmap = Bitmap.createBitmap(PIN_DIM, PIN_DIM, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onResume() {
        super.onResume()
        if (app != null) {
            val eventBus = app.eventBus
            if (!eventBus.isRegistered(this)) {
                Log.i("ConferenceEvent", "Register eventBus")
                eventBus.register(this)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (app != null) {
            val eventBus = app.eventBus
            if (eventBus.isRegistered(this)) {
                eventBus.removeAllStickyEvents()
                eventBus.unregister(this)
            }
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            listener = context as ConferenceEventFragmentListener?
        } catch (e: ClassCastException) {
            throw ClassCastException(context!!.toString() + " must implement ConferenceEventFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface ConferenceEventFragmentListener {
        fun fabNowClicked()
        fun onConferenceEventLoaded(response: GetConferenceEventsJob.Success)
        fun onConferenceEventFailed(response: GetConferenceEventsJob.Failure)
        fun getConferenceEventFromDbAndAnchorPanel()
        fun onImageTapped(forceCollapse: Boolean)
    }
}