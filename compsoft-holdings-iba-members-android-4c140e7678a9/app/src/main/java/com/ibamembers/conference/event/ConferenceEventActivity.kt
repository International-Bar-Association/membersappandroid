package com.ibamembers.conference.event

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.CalendarContract
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SnapHelper
import android.text.SpannableString
import android.text.TextUtils
import android.text.format.DateUtils
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper
import com.ibamembers.R
import com.ibamembers.app.IBAUtils
import com.ibamembers.conference.ConferenceBaseActivity
import com.ibamembers.conference.event.db.ConferenceEventDao
import com.ibamembers.conference.event.job.ConferenceEventResponse
import com.ibamembers.conference.event.job.GetConferenceEventsJob
import com.sothree.slidinguppanel.SlidingUpPanelLayout

import java.sql.SQLException
import java.text.SimpleDateFormat

import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.calendar_item_view_holder.*
import kotlinx.android.synthetic.main.conference_schedule_activity.*
import kotlinx.android.synthetic.main.schedule_header_item_view_holder.*
import kotlinx.android.synthetic.main.schedule_item_view_holder.*
import kotlinx.android.synthetic.main.schedule_item_view_holder.schedule_item_layout
import org.joda.time.*
import java.util.*

private fun DateTime.isToday(): Boolean {
    return (this.withTimeAtStartOfDay() == DateTime().withTimeAtStartOfDay())
}

class ConferenceEventActivity : ConferenceBaseActivity(), ConferenceEventFragment.ConferenceEventFragmentListener, ConferenceEventActivityListener {

    private val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val time24hrFormat = SimpleDateFormat("HHmm", Locale.getDefault())
    private val dayDateFormat = SimpleDateFormat("EEE dd", Locale.getDefault())

    private var adapter: ScheduleAdapter? = null
    private var linearLayoutManager: LinearLayoutManagerWithSmoothScroller? = null
    private var progressDialog: ProgressDialog? = null
    private var currentEventFilter = EventFilter.All
    private var conferenceEventList = ArrayList<ConferenceEventResponse>()
    private var attendingEventList = ArrayList<ConferenceEventResponse>()
    private var conferenceEventFragment: ConferenceEventFragment? = null
    private var currentSelectedScheduleIndex: Int = 0
    private var fullHeight: Int = 0
    private var viewHeight: Int = 0

    private var snapHelperStart: SnapHelper? = null
    private var isSliderAnimating: Boolean = false

    private var currentEventPosition = -1
    private var loadEventPosition = -1 //This is to find a loaded event (from link)
    private var isFirstLoaded: Boolean = false

    private var loadEventId: Int = 0

    private val calendarAdapter by lazy { CalendarAdapter(this, this) }

    enum class ScheduleTapState {
        None, Current, Selected
    }

    enum class EventFilter {
        Yours, All, Favourite
    }

    enum class ScheduleItemType {
        Header, Item
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.conference_schedule_activity)
        //registerEventBus()

        conferenceEventFragment = getOrAddOnlyFragment(ConferenceEventFragment::class.java)
        currentSelectedScheduleIndex = -1
        setupToolbar(toolbar!!)

        setupAdapter()
        setupSlidingPanel()
        findHeightOfCoordinator()
    }

    override fun getConferenceEventFromDbAndAnchorPanel() {
        conferenceEventFromDB()
        setConferenceEventListAndAnchorPanel()
    }

    private fun conferenceEventFromDB(): List<ConferenceEventResponse>?{
        try {
            val conferenceEventDao = app.databaseHelper.conferenceEventDao
            val conferenceEventList = conferenceEventDao.queryForAll()
            if (conferenceEventList.isNotEmpty()) {
                val conferenceEventResponseList = ArrayList<ConferenceEventResponse>(conferenceEventList.size)
                for (eventResponse in conferenceEventList) {
                    conferenceEventResponseList.add(ConferenceEventDao.convertEventAsEventResponse(eventResponse))
                }

                setConferenceListAndItsAttributes(conferenceEventResponseList)
            }

        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return null
    }

    private fun findHeightOfCoordinator() {
        coordinatorLayout.viewTreeObserver.addOnGlobalLayoutListener {
            fullHeight = coordinatorLayout.height
            viewHeight = (coordinatorLayout.height.toFloat() * 0.23f).toInt()
        }
    }

    private fun setupSlidingPanel() {
        //Default tab
        onAllEventTabClicked()

        sliding_layout.anchorPoint = 0.26f
        sliding_layout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        sliding_layout.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {}

            override fun onPanelStateChanged(panel: View, previousState: SlidingUpPanelLayout.PanelState, newState: SlidingUpPanelLayout.PanelState) {
                setSnapHelper(true)

                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    app.sendEventToAnalytics(ANALYTIC_CATEGORY, "Panel expanded", null)
                    calendar_horizontal_recycler_view.visibility = View.VISIBLE

                    resetTapStateInListAndInvalidate()
                    toggle_arrow.setImageResource(R.drawable.schedule_double_arrow_down)
                    setRecyclerViewToAnchoredHeight(false)
                    setSnapHelper(false)
                    sliding_layout.isTouchEnabled = true
                    isSliderAnimating = false
                } else if (newState == SlidingUpPanelLayout.PanelState.ANCHORED || newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    toggle_arrow.setImageResource(R.drawable.schedule_double_arrow_up)
                } else if (previousState == SlidingUpPanelLayout.PanelState.EXPANDED && newState == SlidingUpPanelLayout.PanelState.DRAGGING) {
                    calendar_horizontal_recycler_view.visibility = View.GONE
                }


                if (previousState == SlidingUpPanelLayout.PanelState.ANCHORED && newState == SlidingUpPanelLayout.PanelState.DRAGGING) {
                    calendar_horizontal_recycler_view.visibility = View.GONE
                    //conferenceEventFragment.setFabNow(false);
                    setRecyclerViewToAnchoredHeight(false)
                } else if (newState == SlidingUpPanelLayout.PanelState.ANCHORED) {
                    calendar_horizontal_recycler_view.visibility = View.GONE
                    sliding_layout.isTouchEnabled = true
                    isSliderAnimating = false
                    setAnchoredStateInList(true)
                    setRecyclerViewToAnchoredHeight(true)
                    //conferenceEventFragment.setFabNow(true);
                    schedule_recycler_view.scrollToPosition(currentSelectedScheduleIndex)

                }
            }
        })

        dragView!!.setOnClickListener {
            Log.i("Conference", "Drag clicked")
            if (!isSliderAnimating && conferenceEventFragment?.isLoadingConferenceEvents == false) {
                if (sliding_layout.panelState == SlidingUpPanelLayout.PanelState.COLLAPSED || sliding_layout.panelState == SlidingUpPanelLayout.PanelState.ANCHORED) {

                    val params = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
                    schedule_recycler_view.layoutParams = params

                    resetTapStateInListAndInvalidate()

                    Handler().postDelayed({
                        isSliderAnimating = true
                        sliding_layout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
                    }, 100)
                } else if (sliding_layout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {

                    Handler().postDelayed({
                        isSliderAnimating = true
                        sliding_layout.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
                    }, 100)
                }
            }
        }

        toggle_your_event.setOnClickListener {
            onYourEventTabClicked()
        }

        toggle_all_event.setOnClickListener {
            onAllEventTabClicked()
        }

        toggle_favourite.setOnClickListener {
            favouritesTabClicked()
        }

        calendar_horizontal_recycler_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        calendar_horizontal_recycler_view.adapter = calendarAdapter

        val settingDao = app.databaseHelper.settingDao
        val startDateString = settingDao.conferenceStartDate
        val finishDateString = settingDao.conferenceFinishDate

        if (startDateString != null && finishDateString != null) {
            val startDate = IBAUtils.getDateTimeFromString(this, startDateString)
            val finishDate = IBAUtils.getDateTimeFromString(this, finishDateString)
            val conferenceDuration = Days.daysBetween(startDate, finishDate).days + 1 //include the first day

            calendarAdapter.calendarList = ArrayList<DateTime>().apply {
                for (i in 0 until conferenceDuration) {
                    add(startDate.plusDays(i))
                }
            }
        }

//        calendarAdapter.calendarList = ArrayList<DateTime>().apply {
//
//            add(DateTime().minusDays(1))
//            add(DateTime())
//            add(DateTime().plusDays(1))
//            add(DateTime().plusDays(2))
//            add(DateTime().plusDays(3))
//            add(DateTime().plusDays(4))
//        }
    }

    override fun onImageTapped(forceCollapse: Boolean) {
        if (sliding_layout.panelState == SlidingUpPanelLayout.PanelState.ANCHORED || forceCollapse) {
            sliding_layout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        }else if (sliding_layout.panelState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            sliding_layout.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
        }
    }

    private fun onYourEventTabClicked() {
        app.sendEventToAnalytics(ANALYTIC_CATEGORY, "Your Function clicked", null)
        currentEventFilter = EventFilter.Yours
        setAnchoredStateInList(sliding_layout.panelState == SlidingUpPanelLayout.PanelState.ANCHORED)

        currentSelectedScheduleIndex = -1
        schedule_recycler_view.smoothScrollToPosition(this.currentEventPosition + 3)

        your_event_tab.setTextColor(ContextCompat.getColor(this, R.color.conference_theme_primary))
        all_event_tab.setTextColor(ContextCompat.getColor(this, R.color.schedule_slider_tab_unselected_font))
        favourite_tab.setColorFilter(ContextCompat.getColor(applicationContext, R.color.schedule_slider_tab_unselected_font), PorterDuff.Mode.SRC_IN)
        your_event_line.visibility = View.VISIBLE
        all_event_line.visibility = View.INVISIBLE
        favourite_line.visibility = View.INVISIBLE
        setNoScheduleLabel()
    }

    private fun onAllEventTabClicked() {
        app.sendEventToAnalytics(ANALYTIC_CATEGORY, "All Sessions clicked", null)
        currentEventFilter = EventFilter.All
        resetListAndScrollToCurrentEvent()

        your_event_tab.setTextColor(ContextCompat.getColor(this, R.color.schedule_slider_tab_unselected_font))
        all_event_tab.setTextColor(ContextCompat.getColor(this, R.color.conference_theme_primary))
        favourite_tab.setColorFilter(ContextCompat.getColor(applicationContext, R.color.schedule_slider_tab_unselected_font), PorterDuff.Mode.SRC_IN)
        your_event_line.visibility = View.INVISIBLE
        all_event_line.visibility = View.VISIBLE
        favourite_line.visibility = View.INVISIBLE
        setNoScheduleLabel()
    }

    private fun favouritesTabClicked() {
        currentEventFilter = EventFilter.Favourite
        resetListAndScrollToCurrentEvent()

        your_event_tab.setTextColor(ContextCompat.getColor(this, R.color.schedule_slider_tab_unselected_font))
        all_event_tab.setTextColor(ContextCompat.getColor(this, R.color.schedule_slider_tab_unselected_font))
        favourite_tab.setColorFilter(ContextCompat.getColor(applicationContext, R.color.conference_theme_primary), PorterDuff.Mode.SRC_IN)
        your_event_line.visibility = View.INVISIBLE
        all_event_line.visibility = View.INVISIBLE
        favourite_line.visibility = View.VISIBLE
        setNoScheduleLabel()
    }

    private fun resetListAndScrollToCurrentEvent(){
        setAnchoredStateInList(sliding_layout.panelState == SlidingUpPanelLayout.PanelState.ANCHORED)
        currentSelectedScheduleIndex = -1
        if (!isFirstLoaded) {
            schedule_recycler_view.scrollToPosition(this.currentEventPosition)
            isFirstLoaded = true
        } else {
            schedule_recycler_view.smoothScrollToPosition(this.currentEventPosition)
        }

    }

    fun collapseSlidingPanel() {
        sliding_layout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return if (sliding_layout.panelState == SlidingUpPanelLayout.PanelState.DRAGGING && ev.action == MotionEvent.ACTION_DOWN) {
            true
        } else super.dispatchTouchEvent(ev)
    }


    private fun resetTapStateInListAndInvalidate() {
        val conferenceList = getScheduleListForTabType()

        currentEventPosition = -1
        var index = 0
        for (event in conferenceList) {
            event.tapState = ScheduleTapState.None

            if (index == currentSelectedScheduleIndex && currentSelectedScheduleIndex != adapter!!.currentEventIndex) {
                event.tapState = ScheduleTapState.Selected
            } else {
                event.tapState = ScheduleTapState.None
            }

            if (currentEventPosition == -1) {
                val eventStartTime = DateTime(event.getStartTime())
                val eventEndTime = DateTime(event.getEndTime())
                val currentEndTime = DateTime()

                if (eventStartTime.isBefore(currentEndTime) && eventEndTime.isAfter(currentEndTime) || eventStartTime.isAfter(currentEndTime)) {
                    currentEventPosition = index
                }
            }

            if (loadEventPosition == -1 && loadEventId != 0) {
                if (event.eventItemId == loadEventId) {
                    loadEventPosition = index
                }
            }

            event.isAnchored = false
            index++
        }

        setConferenceListAndUpdateUi(conferenceList)
    }


    private fun setupAdapter() {
        linearLayoutManager = LinearLayoutManagerWithSmoothScroller(this)
        schedule_recycler_view.layoutManager = linearLayoutManager
        schedule_recycler_view.setHasFixedSize(false)
        adapter = ScheduleAdapter(getScheduleListForTabType(), this)
        schedule_recycler_view.adapter = adapter

        snapHelperStart = GravitySnapHelper(Gravity.TOP)
        snapHelperStart!!.attachToRecyclerView(schedule_recycler_view)
    }

    private fun setSnapHelper(isSnap: Boolean) {
        if (isSnap) {
            snapHelperStart!!.attachToRecyclerView(schedule_recycler_view)
        } else {
            snapHelperStart!!.attachToRecyclerView(null)
        }
    }

    override fun onCalendarClicked(position: Int, date: DateTime?) {
        if (date != null){
            if (date.isToday()) {
                toggle_calendar.text = getString(R.string.schedule_today)
            } else {
                val calendarText = SpannableString(dayDateFormat.format(date.toDate()).toUpperCase())
                if (calendarText.length == 6) {
                    calendarText.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.conference_calendar_font_light_blue)), 0, 3, 0)
                    calendarText.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, android.R.color.white)), 4, 6, 0)
                    toggle_calendar.setText(calendarText, TextView.BufferType.SPANNABLE)
                }
            }
        } else {
            toggle_calendar.text = getString(R.string.schedule_all)
        }

        calendarAdapter.selectedIndex = position
        val scheduleList = getScheduleListForTabType()

        setConferenceListAndUpdateUi(scheduleList)
        setNoScheduleLabel()

    }

    override fun onScheduleClicked(eventItemId: Int, position: Int) {
        if (adapter == null) return

        eventItemId?.let {
            if (!isSliderAnimating) {
                val conferenceEventIndex = adapter!!.getConferenceEventForPosition(position)
                currentSelectedScheduleIndex = conferenceEventIndex
                if (currentSelectedScheduleIndex != -1) {

                    setAnchoredStateInList(true)
                    setRecyclerViewToAnchoredHeight(true)
                    schedule_recycler_view?.scrollToPosition(conferenceEventIndex)

                    val currentSchedule = adapter!!.getConferenceEventList()!!.find { it.eventItemId == eventItemId}
                    if (currentSchedule != null) {
                        if (isFloorOfScheduleValid(currentSchedule)) {
                            currentSchedule.tapState = ScheduleTapState.Selected
                        }

                        if (sliding_layout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                            Handler().post {
                                sliding_layout.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
                                setPinOnFloorForTapState(currentSchedule)
                            }

                        } else if (sliding_layout.panelState == SlidingUpPanelLayout.PanelState.ANCHORED) {
                            Handler().post {
                                setPinOnFloorForTapState(currentSchedule)
                            }
                        }
                    }
                }
            }
        }
    }

    //TODO This does not quite work yet
    private fun isFloorOfScheduleValid(currentSchedule: ConferenceEventResponse): Boolean{
        return if (currentSchedule.buildingId != 7) {
            conferenceEventFragment?.doesFloorBelongToBuidlingId(currentSchedule.floor, currentSchedule.buildingId)?:false
        } else {
            true
        }
    }

    private fun setPinOnFloorForTapState(currentSchedule: ConferenceEventResponse){
        if (currentSchedule.tapState == ScheduleTapState.Current) {
            conferenceEventFragment!!.setPinOnFloorOnScheduleClicked(currentSchedule, ScheduleTapState.Current)
        } else {
            conferenceEventFragment!!.setPinOnFloorOnScheduleClicked(currentSchedule)
        }
    }


    override fun onScheduleFavouriteClicked(eventId: Int) {
        app.setFavouriteSchedule(eventId)

        setFavouritesToConferenceList()
        setConferenceListAndUpdateUi(getScheduleListForTabType())
    }

    //update favourited conference list
    fun setFavouritesToConferenceList(){
        val favouriteList = app.listOfFavouriteSchedule
        conferenceEventList.map { it.apply { isFavourite = favouriteList.contains(it.eventItemId) }}
    }


    override fun onConferenceEventLoaded(response: GetConferenceEventsJob.Success) {
        loadConferenceEventFromLink()

        setConferenceListAndItsAttributes(ArrayList(response.response.eventList))

        setConferenceEventListAndAnchorPanel()
        if (loadEventId != 0) {
            sliding_layout.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
            onAllEventTabClicked()
            schedule_recycler_view.scrollToPosition(loadEventPosition)
        }

        if (progressDialog != null) progressDialog!!.dismiss()
    }

    private fun setConferenceListAndItsAttributes(newConferenceList: ArrayList<ConferenceEventResponse>){
        this.conferenceEventList = newConferenceList
        setFavouritesToConferenceList()
        setAttendingEventList()
    }

    override fun onConferenceEventFailed(response: GetConferenceEventsJob.Failure) {
        Log.e("ConferenceSchedule", "Failed to get conference events, loading events from DB")
        if (progressDialog != null) progressDialog!!.dismiss()

        if (response.status == resources.getInteger(R.integer.session_token_invalid_code)) {
            app?.connectionManager?.sessionExpired(applicationContext, app)
        } else {
            getConferenceEventFromDbAndAnchorPanel()
        }
    }

    private fun loadConferenceEventFromLink() {
        val app = app
        if (app != null) {
            val eventId = app.loadedEventId
            if (eventId != 0) {
                loadEventId = eventId
                app.saveLoadedEventId(0)
            }
        }
    }

    private fun setAttendingEventList() {
        attendingEventList = ArrayList()
        for (event in conferenceEventList) {
            if (event.isAttending) attendingEventList.add(event)
        }
    }

    private fun setConferenceEventListAndAnchorPanel() {
        val conferenceEventResponseList = getScheduleListForTabType()
        if (conferenceEventResponseList.size > 0) {
            setAnchoredStateInList(sliding_layout.panelState == SlidingUpPanelLayout.PanelState.ANCHORED)
            //conferenceEventFragment.setFabNow(true);
        }
    }

    private fun saveConferenceEventToDB(conferenceEventList: List<ConferenceEventResponse>) {
        try {
            val conferenceEventDao = app.databaseHelper.conferenceEventDao
            for (eventResponse in conferenceEventList) {
                conferenceEventDao.saveEventResponseAsEvent(eventResponse)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

    }

    private fun getScheduleListForTabType(): ArrayList<ConferenceEventResponse> {
        val eventList = when (currentEventFilter) {
            EventFilter.Yours -> attendingEventList
            EventFilter.All -> conferenceEventList
            else -> ArrayList(conferenceEventList.filter { it.isFavourite })
        }

        calendarAdapter.getSelectedDate()?.let { selectedDate ->
            return ArrayList(eventList.filter {
                val startDateTime = IBAUtils.getDateTimeOffSetTimeZone(it.getStartTime()).withTimeAtStartOfDay()
                val selectedDateTime = IBAUtils.getDateTimeOffSetTimeZone(selectedDate.toDate()).withTimeAtStartOfDay()

                startDateTime == selectedDateTime })
        }?: return ArrayList(eventList)
    }

    override fun getLayoutType(): LayoutType {
        return LayoutType.SCROLLVIEW
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun fabNowClicked() {
        setConferenceListAndUpdateUi(getScheduleListForTabType())
        schedule_recycler_view.scrollToPosition(adapter!!.currentEventIndex)
        conferenceEventFragment!!.setPinOnFloorOnScheduleClicked(
                adapter!!.getConferenceEventList()!![adapter!!.currentEventIndex], ScheduleTapState.Current)
    }

    private fun setConferenceListAndUpdateUi(conferenceList: MutableList<ConferenceEventResponse>){
//
//        //TODO Added for testing
//        conferenceList.map { it.apply {
//            val startDate = DateTime(getStartTime())
//            val endDate = DateTime(getEndTime())
//
//            if (startDate.monthOfYear != DateTimeConstants.AUGUST) {
//                setStartTime(startDate.minusMonths(1).toDate())
//            }
//
//            if (startDate.monthOfYear != DateTimeConstants.AUGUST) {
//                setEndTime(endDate.minusMonths(1).toDate())
//            }
//        }}

        adapter?.setConferenceEventList(conferenceList,
                currentEventFilter,
                sliding_layout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED)
        setNoScheduleLabel()
    }

    private fun setNoScheduleLabel(){
        schedule_no_items.visibility = if (adapter?.getConferenceEventList()!!.isEmpty()) View.VISIBLE else View.GONE

        if (currentEventFilter == EventFilter.Favourite) {
            schedule_no_items.text = getString(R.string.schedule_no_favourites_this_day)
        } else {
            schedule_no_items.text = getString(R.string.schedule_no_schedules_this_day)
        }
    }



    inner class ScheduleAdapter(private var conferenceEventList: MutableList<ConferenceEventResponse>?,
                                private var listener: ConferenceEventActivityListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        val currentEventIndex: Int
            get() {
                for ((index, conferenceEvent) in this.conferenceEventList!!.withIndex()) {
                    if (conferenceEvent.isCurrent) return index
                }
                return -1
            }

        var showHeader = false
        var currentFilter: EventFilter = EventFilter.All

        fun setConferenceEventList(conferenceEventList: MutableList<ConferenceEventResponse>,
                                   currentFilter: EventFilter,
                                   isExpanded: Boolean) {
            this.conferenceEventList = conferenceEventList
            this.currentFilter = currentFilter
            this.showHeader = isExpanded
            notifyDataSetChanged()
        }

        fun appendConferenceEventList(conferenceEvent: ConferenceEventResponse) {
            if (conferenceEventList!!.size == 1) {
                conferenceEventList!!.add(conferenceEvent)
                notifyItemInserted(conferenceEventList!!.size - 1)
            } else if (conferenceEventList!!.size == 2) {
                val newList = ArrayList<ConferenceEventResponse>()
                newList.add(conferenceEventList!![1])
                newList.add(conferenceEvent)
                conferenceEventList = newList
                notifyDataSetChanged()
            }
        }

        fun getConferenceEventList(): MutableList<ConferenceEventResponse>? {
            if (conferenceEventList == null) return ArrayList()
            return conferenceEventList
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == ScheduleItemType.Item.ordinal) {
                ScheduleViewHolder(LayoutInflater.from(parent.context).inflate(
                        R.layout.schedule_item_view_holder, parent, false), listener)
            } else {
                ScheduleHeaderViewHolder(LayoutInflater.from(parent.context).inflate(
                        R.layout.schedule_header_item_view_holder, parent, false))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (getItemViewType(position) == ScheduleItemType.Item.ordinal) {
                val headerIndex = if (showHeader)  1 else 0
                (holder as ScheduleViewHolder).fillView(conferenceEventList!![position - headerIndex])
            } else if (getItemViewType(position) == ScheduleItemType.Header.ordinal) {
                (holder as ScheduleHeaderViewHolder).fillView(currentEventFilter)
            }
        }

        override fun getItemCount(): Int {
            val eventList = conferenceEventList!!.size
            val headerCount = if (showHeader) 1 else 0
            return if (eventList > 0)  eventList + headerCount else 0
        }

        fun eventListHasCurrent(): Boolean {
            for (conferenceEvent in this.conferenceEventList!!) {
                if (conferenceEvent.isCurrent) {
                    return true
                }
            }
            return false
        }

        override fun getItemViewType(position: Int): Int {
            if (showHeader && position == 0)  {
                return ScheduleItemType.Header.ordinal
            }
            return ScheduleItemType.Item.ordinal
        }

        fun isPositionLastItem(position: Int): Boolean{
            return if (showHeader)  {
                position == conferenceEventList!!.size
            } else {
                position == conferenceEventList!!.size - 1
            }
        }

        fun getConferenceEventForPosition(position: Int): Int{
            return if (showHeader) position - 1 else position
        }

    }

    inner class ScheduleHeaderViewHolder(override val containerView: View) :
            RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun fillView(currentFilter: EventFilter) {
            schedule_header.text = when (currentFilter) {
                EventFilter.Yours -> getString(R.string.schedule_your_event_title_section)
                EventFilter.All   -> getString(R.string.schedule_all_event_title_section)
                else              -> getString(R.string.schedule_fav_event_title_section)
            }
        }
    }

    inner class ScheduleViewHolder(override val containerView: View,
                                   private var listener: ConferenceEventActivityListener) :
            RecyclerView.ViewHolder(containerView), LayoutContainer {

        private var currentSchedule: ConferenceEventResponse? = null

        init {
            itemView.setOnClickListener {
                currentSchedule?.let {
                    listener.onScheduleClicked(it.eventItemId, adapterPosition)
                }

            }

            scheduleFavouriteImage.setOnClickListener {
                currentSchedule?.let {
                    listener.onScheduleFavouriteClicked(it.eventItemId)
                }
            }

            scheduleAddToCalendarImage!!.setOnClickListener {
                checkIfSeenCalendarInfo(currentSchedule)
            }
        }

        fun fillView(schedule: ConferenceEventResponse) {
            this.currentSchedule = schedule
            val time = getTimeHeader(schedule.getStartTime()!!, schedule.getEndTime()!!)
            val title = schedule.title
            val subtitle = schedule.subTitle
            val roomName = schedule.roomName

            titleText!!.text = if (!TextUtils.isEmpty(roomName)) roomName else "Room -"
            timeHeaderText!!.text = if (!TextUtils.isEmpty(time)) time else ""

            if (!title.isNullOrEmpty()) {
                titleText!!.visibility = View.VISIBLE
                titleText!!.text = title
            } else {
                titleText!!.visibility = View.GONE
            }

            floorNameText!!.visibility = View.INVISIBLE
//            if (!subtitle.isNullOrEmpty()) {
//                floorNameText!!.visibility = View.VISIBLE
//                floorNameText!!.text = subtitle
//            } else {
//                floorNameText!!.visibility = View.GONE
//            }

            if (!roomName.isNullOrEmpty()) {
                roomNameText!!.visibility = View.VISIBLE
                roomNameText!!.text = roomName
            } else {
                roomNameText!!.visibility = View.GONE
            }

            if (schedule.isCurrent) {
                schedule.tapState = ScheduleTapState.Current
            }

            //onClick(itemView)
            when (schedule.tapState) {
                ScheduleTapState.Current -> {
                    schedule_item_layout.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.schedule_current_background))
                    timeHeaderText!!.setTextColor(ContextCompat.getColor(applicationContext, android.R.color.white))
                    setFieldTextColors(false)
                }
                ScheduleTapState.Selected -> {
                    schedule_item_layout.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.schedule_selected_background))
                    timeHeaderText!!.setTextColor(ContextCompat.getColor(applicationContext, android.R.color.white))
                    setFieldTextColors(false)
                }
                else -> {
                    schedule_item_layout.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.schedule_normal_background))
                    timeHeaderText!!.setTextColor(ContextCompat.getColor(applicationContext, R.color.schedule_selected_background))
                    setFieldTextColors(true)
                }
            }

            scheduleFavouriteImage.setImageResource(if (schedule.isFavourite) R.drawable.conference_favourite_selected_icon_dark else R.drawable.conference_favourite_unselected_icon_dark)

            dummy_bottom_padding!!.visibility = if (adapter!!.isPositionLastItem(adapterPosition)) View.VISIBLE else View.GONE


            //TODO load the main pin on the map. uncommenting now and it creates an infinite loop
            if (currentSchedule!!.eventItemId == loadEventId) {
                Handler().postDelayed({
                    //onClick(itemView)
                    loadEventId = 0
                }, 1000)
            }

            //7 is the id of offsite. Change accordingly
            if (schedule.buildingId == 7) {
                scheduleLocationImage.visibility = View.VISIBLE
                scheduleLocationImage.setOnClickListener {
                    if (schedule.latitude != 0f && schedule.longitude != 0f) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?q=loc:${schedule.latitude},${schedule.longitude}")))
                    }
                }
            } else {
                scheduleLocationImage.visibility = View.INVISIBLE
                scheduleLocationImage.setOnClickListener(null)
            }
        }

        private fun setFieldTextColors(isDark: Boolean){
            if (isDark) {
                titleText.setTextColor(ContextCompat.getColor(applicationContext, R.color.schedule_item_text))
                floorNameText.setTextColor(ContextCompat.getColor(applicationContext, R.color.schedule_item_text))
                roomNameText.setTextColor(ContextCompat.getColor(applicationContext, R.color.schedule_item_text))
                scheduleFavouriteImage.setColorFilter(ContextCompat.getColor(applicationContext, R.color.schedule_item_text), PorterDuff.Mode.SRC_IN)
                scheduleAddToCalendarImage.setColorFilter(ContextCompat.getColor(applicationContext, R.color.schedule_item_text), PorterDuff.Mode.SRC_IN)
                scheduleLocationImage.setColorFilter(ContextCompat.getColor(applicationContext, R.color.schedule_item_text), PorterDuff.Mode.SRC_IN)
            } else {
                titleText.setTextColor(ContextCompat.getColor(applicationContext, android.R.color.white))
                floorNameText.setTextColor(ContextCompat.getColor(applicationContext, android.R.color.white))
                roomNameText.setTextColor(ContextCompat.getColor(applicationContext, android.R.color.white))
                scheduleFavouriteImage.setColorFilter(ContextCompat.getColor(applicationContext, android.R.color.white), PorterDuff.Mode.SRC_IN)
                scheduleAddToCalendarImage.setColorFilter(ContextCompat.getColor(applicationContext, android.R.color.white), PorterDuff.Mode.SRC_IN)
                scheduleLocationImage.setColorFilter(ContextCompat.getColor(applicationContext, android.R.color.white), PorterDuff.Mode.SRC_IN)
            }
        }

        private fun getTimeHeader(startTime: Date, endTime: Date): String {
            val sb = StringBuilder()
            val date: String
            val today = IBAUtils.getDateTimeForTimeZone(null)
            val confStartTime = IBAUtils.getDateTimeOffSetTimeZone(startTime)
            dateFormat.timeZone = Calendar.getInstance().timeZone

            if (confStartTime.withTimeAtStartOfDay() == today.withTimeAtStartOfDay()) {
                date = getString(R.string.conference_date_today)
            } else if (confStartTime.withTimeAtStartOfDay() == today.withTimeAtStartOfDay().minusDays(1)) {
                date = getString(R.string.conference_date_yesterday)
            } else if (confStartTime.withTimeAtStartOfDay() == today.withTimeAtStartOfDay().plusDays(1)) {
                date = getString(R.string.conference_date_tomorrow)
            } else {
                date = dateFormat.format(confStartTime.toDate())
            }

            val dayString = dayFormat.format(startTime)
            time24hrFormat.timeZone = TimeZone.getTimeZone("UTC")
            val startTimeString = time24hrFormat.format(startTime)
            val endTimeString = time24hrFormat.format(endTime)

            sb.append(date.toUpperCase(Locale.getDefault()))
            sb.append(" ")
            sb.append(startTimeString)
            sb.append(" - ")
            sb.append(endTimeString)

            return sb.toString()
        }
    }

    fun checkIfSeenCalendarInfo(currentSchedule: ConferenceEventResponse?) {
        val hasSeenCalInfo = app.hasSeenAddToCalendarInfo

        if (hasSeenCalInfo) {
            addScheduleToCalendar(currentSchedule)
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.schedule_calendar_info_title)
                    .setMessage(R.string.schedule_calendar_info_description)
                    .setPositiveButton(R.string.schedule_calendar_info_got_it) { dialog, id ->
                        addScheduleToCalendar(currentSchedule)
                        app.setHasSeenAddToCalendarInfo()
                    }.create().show()
        }
    }

    private fun addScheduleToCalendar(currentSchedule: ConferenceEventResponse?) {
        currentSchedule?.let {
            val buildingName = conferenceEventFragment!!.conferenceBuildingMap?.get(currentSchedule!!.buildingId)?.buildingName
            val calendarTitle = currentSchedule.title + " at " + buildingName

            val intent = Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.Events.TITLE, calendarTitle)
                    .putExtra(CalendarContract.Events.EVENT_LOCATION, "Location")
                    .putExtra(CalendarContract.Events.EVENT_LOCATION, "Location")
                    .putExtra(CalendarContract.Events.DESCRIPTION, CALENDAR_URL_PREFIX + currentSchedule.eventItemId)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, getLocalStartEndTime(currentSchedule).first.toDate().time)
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, getLocalStartEndTime(currentSchedule).second.toDate().time)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }
    }


    private fun getLocalStartEndTime(currentSchedule: ConferenceEventResponse):Pair<DateTime, DateTime> {
        val koreaStartDate = DateTime(currentSchedule.getStartTime())
        val koreaEndDate = DateTime(currentSchedule.getEndTime())
        val offsetFromKorea = TimeZone.getTimeZone("Asia/Seoul").getOffset(System.currentTimeMillis())
        val hourDifference = (offsetFromKorea * -1) / (1000 * 60 * 60)

        return Pair(koreaStartDate.plusHours(hourDifference), koreaEndDate.plusHours(hourDifference))
    }

    private fun setRecyclerViewToAnchoredHeight(isAnchoredHeight: Boolean) {
        val params = schedule_recycler_view.layoutParams
        params.height = if (isAnchoredHeight) viewHeight else fullHeight
        schedule_recycler_view.layoutParams = params
    }

    /**
     * This resets all tap states and set all events as anchored
     * @param isAnchored Set events as anchored
     */
    private fun setAnchoredStateInList(isAnchored: Boolean) {
        //TODO a bit inefficient to loop twice
        resetTapStateInListAndInvalidate()

        val anchoredConferenceList = adapter!!.getConferenceEventList()
        for (conference in anchoredConferenceList!!) {
            conference.isAnchored = isAnchored
        }

        setConferenceListAndUpdateUi(anchoredConferenceList)
    }



    inner class EventsComparator : Comparator<ConferenceEventResponse> {
        override fun compare(left: ConferenceEventResponse, right: ConferenceEventResponse): Int {
            return DateTime(left.getStartTime()).compareTo(DateTime(left.getStartTime()))
        }
    }

//    private fun registerEventBus() {
//        if (app != null) {
//            val eventBus = app.eventBus
//            if (!eventBus.isRegistered(this)) {
//                Log.i("ConferenceEvent", "Register eventBus")
//                eventBus.register(this)
//            }
//        }
//    }
//
//    public override fun onDestroy() {
//        super.onDestroy()
//        if (app != null) {
//            val eventBus = app.eventBus
//            if (eventBus.isRegistered(this)) {
//                eventBus.removeAllStickyEvents()
//                eventBus.unregister(this)
//            }
//        }
//    }

    override fun onBackPressed() {
        if (sliding_layout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            sliding_layout.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
            return
        }
        super.onBackPressed()
    }

    companion object {

        val ANALYTIC_CATEGORY = "Conference"
        val CALENDAR_URL_PREFIX = "http://ibamembers.com/ibamembersapp/applinks/viewevent?id="
    }
}

private class CalendarAdapter(val context: Context, var listener: ConferenceEventActivityListener) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    var selectedIndex = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var calendarList = ArrayList<DateTime>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun getSelectedDate(): DateTime?{
        if (selectedIndex != 0) {
            return calendarList[selectedIndex - 1]
        } else {
            return null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        return CalendarViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.calendar_item_view_holder, parent, false), listener)
    }

    override fun getItemCount(): Int {
        return calendarList.size + 1
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        if (position == 0) {
            holder.setAllDayView(selectedIndex)
        } else {
            holder.fillView(calendarList[position -1], selectedIndex)
        }
    }

    class CalendarViewHolder(override val containerView: View, var listener: ConferenceEventActivityListener) :
            RecyclerView.ViewHolder(containerView), View.OnClickListener, LayoutContainer {

        private var date:DateTime? = null
        private var selectedIndex = 0

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            listener.onCalendarClicked(adapterPosition, date)
        }

        fun fillView(date: DateTime, selectedIndex: Int) {
            this.selectedIndex = selectedIndex
            this.date = date

            DateUtils.isToday(date.millis)

            if (!DateUtils.isToday(date.millis)) {
                val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())
                val dayOfMonthFormat = SimpleDateFormat("dd", Locale.getDefault())
                val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

                calendar_day_of_week_text.visibility = View.VISIBLE
                calendar_day_of_month_text.visibility = View.VISIBLE
                calendar_month_text.visibility = View.VISIBLE
                calendar_text_only_layout.visibility = View.GONE

                calendar_day_of_week_text.text = dayOfWeekFormat.format(date.toDate()).toUpperCase()
                calendar_day_of_month_text.text = dayOfMonthFormat.format(date.toDate())
                calendar_month_text.text = monthFormat.format(date.toDate()).toUpperCase()
            } else {
                calendar_day_of_week_text.visibility = View.GONE
                calendar_day_of_month_text.visibility = View.GONE
                calendar_month_text.visibility = View.GONE

                calendar_text_only_layout.visibility = View.VISIBLE
                calendar_text_only_top.visibility = View.GONE
                calendar_text_only_bottom.visibility = View.VISIBLE
                calendar_text_only_bottom.text = containerView.context.getString(R.string.schedule_today)
            }

            updateSelectedView(selectedIndex == adapterPosition)

        }

        fun setAllDayView(selectedIndex: Int){
            this.selectedIndex = selectedIndex
            this.date = null
            calendar_day_of_week_text.visibility = View.GONE
            calendar_day_of_month_text.visibility = View.GONE
            calendar_month_text.visibility = View.GONE

            calendar_text_only_layout.visibility = View.VISIBLE
            calendar_text_only_top.visibility = View.VISIBLE
            calendar_text_only_bottom.visibility = View.VISIBLE
            calendar_text_only_top.text = "ALL"
            calendar_text_only_bottom.text = "DAYS"

            updateSelectedView(selectedIndex == adapterPosition)
        }

        private fun updateSelectedView(isSelected: Boolean){
            calendar_day_of_month_text.setTextColor(ContextCompat.getColor(containerView.context, if (isSelected) android.R.color.white else R.color.conference_theme_primary))
            calendar_card_view.setCardBackgroundColor(ContextCompat.getColor(containerView.context, if (isSelected) R.color.conference_theme_primary else android.R.color.white))
            calendar_text_only_bottom.setTextColor(ContextCompat.getColor(containerView.context, if (isSelected) android.R.color.white else R.color.conference_theme_primary))
        }
    }

    enum class DayType{
        AllDays, Dates
    }
}

interface ConferenceEventActivityListener{
    fun onScheduleClicked(scheduleId: Int, position: Int)
    fun onScheduleFavouriteClicked(eventId: Int)
    fun onCalendarClicked(position: Int, date: DateTime?)
}


