package com.ibamembers.conference.event

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.ibamembers.R

class CustomTappableView @JvmOverloads constructor(context: Context, attr: AttributeSet? = null) : SubsamplingScaleImageView(context, attr) {

    private var onFloorAreaClickedLiveData = MutableLiveData<Int>()
    var region = Region()
    var path = Path()
    private var areaList: List<Area>? = null

    private var sPin: PointF? = null
    private var pin: Bitmap? = null
    private var tapState: ConferenceEventActivity.ScheduleTapState = ConferenceEventActivity.ScheduleTapState.None

    init {
        //initialise()
    }

    fun setAreas(areaList: List<Area>?, onFloorAreaClickedLiveData: MutableLiveData<Int>) {
        this.onFloorAreaClickedLiveData = onFloorAreaClickedLiveData
        this.areaList = areaList
        invalidate()
    }

    fun setPin(sPin: PointF?, tapState: ConferenceEventActivity.ScheduleTapState) {
        this.sPin = sPin
        this.tapState = tapState
        initialise()
        invalidate()
    }

    private fun initialise() {
        val density = resources.displayMetrics.densityDpi.toFloat()

        pin = if (tapState === ConferenceEventActivity.ScheduleTapState.Current) {
            getBitmap(context, R.drawable.schedule_map_pin_current)
        } else {
            getBitmap(context, R.drawable.schedule_map_pin_selected)
        }

        val w = density / 200f * pin!!.width
        val h = density / 200f * pin!!.height
        pin = Bitmap.createScaledBitmap(pin!!, w.toInt(), h.toInt(), true)
    }

    private fun getBitmap(vectorDrawable: VectorDrawable): Bitmap {
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        Log.i("PinView", "getBitmap: 1")
        return bitmap
    }

    private fun getBitmap(context: Context, drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId)
        return if (drawable is BitmapDrawable) {
            BitmapFactory.decodeResource(context.resources, drawableId)
        } else if (drawable is VectorDrawable) {
            getBitmap((drawable as VectorDrawable?)!!)
        } else {
            throw IllegalArgumentException("unsupported drawable type")
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Don't draw pin before image is ready so it doesn't move around during setup.
        if (!isReady) { return }

        drawPin(canvas)
        drawAllAreas(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                val coordClicked = viewToSourceCoord(PointF(event.x, event.y))

                Log.i("CustomAreaView", "x: ${coordClicked.x}| y: ${coordClicked.y}")

                var areaIdClicked = -1
                areaList?.let {
                    for (i in 0 until it.size){
                        if (isPointInPolygon(PointF(coordClicked.x, coordClicked.y), it[i])) {
                            areaIdClicked = areaList!![i].areaId
                        }
                    }

                    if (areaIdClicked != -1) {
                        onFloorAreaClickedLiveData.postValue(areaIdClicked)
                    }
                }
            }
        }

        return super.onTouchEvent(event)
    }

    private fun drawPin(canvas: Canvas) {
        val paint = Paint().apply { isAntiAlias = true }

        if (sPin != null && pin != null) {
            val vPin = sourceToViewCoord(sPin!!)
            if (vPin != null) {
                val vX = vPin.x - pin!!.width / 2
                val vY = vPin.y - pin!!.height
                canvas.drawBitmap(pin!!, vX, vY, paint)
            }
        }
    }

    private fun drawAllAreas(canvas: Canvas){
        val paint = Paint().apply { color = ContextCompat.getColor(context, R.color.area_background) }

        if (areaList != null) {
            for (i in 0 until areaList!!.size) {
                val area = areaList!![i].areaPoints

                path = Path()
                if (area.size >= 3) {
                    for ((index, point) in area.withIndex()) {
                        val coordPoint = sourceToViewCoord(area[index])
                        if (index == 0) {
                            path.moveTo(coordPoint.x, coordPoint.y)
                        } else {
                            path.lineTo(coordPoint.x, coordPoint.y)
                        }
                    }
                    val firstCoordPoint = sourceToViewCoord(area[0])
                    path.lineTo(firstCoordPoint.x, firstCoordPoint.y)
                    path.close()
                }

                val rectF = RectF()
                path.computeBounds(rectF, true)

                region = Region()
                region.setPath(path, Region(rectF.left.toInt(), rectF.top.toInt(), rectF.right.toInt(), rectF.bottom.toInt()))
                canvas.drawPath(path, paint)
            }
        }
    }

    private fun isPointInPolygon(p: PointF, area: Area): Boolean {
        val areaPoints = area.areaPoints

        var minX = areaPoints[0].x
        var maxX = areaPoints[0].x
        var minY = areaPoints[0].y
        var maxY = areaPoints[0].y

        for (i in 1 until areaPoints.size) {
            val q = areaPoints[i]
            minX = Math.min(q.x, minX)
            maxX = Math.max(q.x, maxX)
            minY = Math.min(q.y, minY)
            maxY = Math.max(q.y, maxY)
        }

        if (p.x < minX || p.x > maxX || p.y < minY || p.y > maxY) {
            return false
        }

        // http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
        var inside = false
        var i = 0
        var j = areaPoints.size - 1
        while (i < areaPoints.size) {
            if (areaPoints[i].y > p.y != areaPoints[j].y > p.y &&
                    p.x < (areaPoints[j].x - areaPoints[i].x) * (p.y - areaPoints[i].y) / (areaPoints[j].y - areaPoints[i].y) + areaPoints[i].x) {
                inside = !inside
            }
            j = i++
        }

        return inside
    }
}
