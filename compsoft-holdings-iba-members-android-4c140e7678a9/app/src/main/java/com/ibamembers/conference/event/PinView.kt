package com.ibamembers.conference.event

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.ibamembers.R

/**
 * Use this class to plot single markers on a scalable image
 */
class PinView @JvmOverloads constructor(context: Context, attr: AttributeSet? = null) : SubsamplingScaleImageView(context, attr) {

    private var sPin: PointF? = null
    private var pin: Bitmap? = null
    private var tapState: ConferenceEventActivity.ScheduleTapState = ConferenceEventActivity.ScheduleTapState.None

    init {
        initialise()
    }

    fun setPin(sPin: PointF?, tapState: ConferenceEventActivity.ScheduleTapState) {
        this.sPin = sPin
        this.tapState = tapState
        initialise()
        invalidate()
    }

    fun getPin(): PointF? {
        return sPin
    }

    private fun initialise() {
        val density = resources.displayMetrics.densityDpi.toFloat()

        if (tapState === ConferenceEventActivity.ScheduleTapState.Current) {
            pin = getBitmap(context, R.drawable.schedule_map_pin_current)
        } else {
            pin = getBitmap(context, R.drawable.schedule_map_pin_selected)
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
        if (!isReady) {
            return
        }

        val paint = Paint()
        paint.isAntiAlias = true

        if (sPin != null && pin != null) {
            val vPin = sourceToViewCoord(sPin!!)
            if (vPin != null) {
                val vX = vPin.x - pin!!.width / 2
                val vY = vPin.y - pin!!.height
                canvas.drawBitmap(pin!!, vX, vY, paint)
            }

        }
    }
}
