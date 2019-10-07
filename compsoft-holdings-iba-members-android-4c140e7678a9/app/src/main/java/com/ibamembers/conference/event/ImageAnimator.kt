package com.ibamembers.conference.event

import android.animation.Animator
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PointF
import android.os.Handler
import android.view.View
import android.view.ViewPropertyAnimator
import android.widget.ImageView
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.ImageViewState
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

class ImageAnimator {

    companion object {
        private const val FLOOR_SCALE_ANIM_DURATION = 200
        private const val FLOOR_SCALE_DOWN_FACTOR = 0.6f
        private const val FLOOR_SCALE_UP_FACTOR = 1.6f
        private const val PIN_ANIMATE_SCALE = 2f
    }

    private var imageViewState: ImageViewState? = null

    enum class AnimationDirection {
        None, Up, Down
    }

    fun animateImage(
            drawable: String,
            animationDirection: AnimationDirection?,
            point: PointF?,
            tapState: ConferenceEventActivity.ScheduleTapState,
            topImageView: CustomTappableView,
            dummyImageView: CustomTappableView,
            topImageAnimator: ImageView,
            bottomImageAnimator: ImageView,
            setPinOnImageLoaded: (point: PointF?, tapState: ConferenceEventActivity.ScheduleTapState) -> Unit
    ){

        //we save the imageState if we are zoomed in
        if (topImageView.scale > topImageView.minScale) {
            imageViewState = topImageView.state
        } else {
            imageViewState = null
        }

        dummyImageView.visibility = View.VISIBLE
        if (imageViewState != null) {
            dummyImageView.setImage(ImageSource.asset(drawable), imageViewState)
        } else {
            dummyImageView.setImage(ImageSource.asset(drawable))
        }

        dummyImageView.setOnImageEventListener(object : SubsamplingScaleImageView.OnImageEventListener {
            override fun onImageLoaded() {
                dummyImageView.visibility = View.INVISIBLE
                topImageView.visibility = View.INVISIBLE

                //create a screenshot of the new floor and set to middle layer
                val newBitmap = getBitmapOfRootView(dummyImageView)
                bottomImageAnimator.visibility = View.VISIBLE
                bottomImageAnimator.setImageBitmap(newBitmap)

                //set screenshot of previous floor to top layer
                val screenshot = getBitmapOfRootView(topImageView)
                topImageAnimator.visibility = View.VISIBLE
                topImageAnimator.setImageBitmap(screenshot)

                val animator = topImageAnimator.animate()

                when (animationDirection) {
                    AnimationDirection.Down -> {
                        animator.alpha(0f)
                                .scaleX(FLOOR_SCALE_DOWN_FACTOR)
                                .scaleY(FLOOR_SCALE_DOWN_FACTOR)
                                .setDuration(FLOOR_SCALE_ANIM_DURATION.toLong()).start()

                        setTopImageOnAnimationEnd(animator, drawable, point, tapState, topImageView, topImageAnimator, bottomImageAnimator, setPinOnImageLoaded)
                    }
                    AnimationDirection.Up -> {
                        animator.alpha(0f)
                                .scaleX(FLOOR_SCALE_UP_FACTOR)
                                .scaleY(FLOOR_SCALE_UP_FACTOR)
                                .setDuration(FLOOR_SCALE_ANIM_DURATION.toLong()).start()

                        setTopImageOnAnimationEnd(animator, drawable, point, tapState, topImageView, topImageAnimator, bottomImageAnimator, setPinOnImageLoaded)
                    }
                    else -> {
                        bottomImageAnimator.visibility = View.GONE
                        topImageAnimator.visibility = View.GONE

                        setPinOnImageLoaded(point, tapState)
                        topImageView.visibility = View.VISIBLE
                        topImageView.setImage(ImageSource.asset(drawable))
                    }
                }
            }

            override fun onReady() {}
            override fun onPreviewLoadError(e: Exception) {}
            override fun onImageLoadError(e: Exception) {}
            override fun onTileLoadError(e: Exception) {}
            override fun onPreviewReleased() {}
        })
    }

    /**
     * When animation is complete we load the image in the scalable view in the bottom layer, followed by removing the middle and top layers.
     * This completes the scaling up or down animation when transitioning from one floor to another.
     *
     * @param animator Animation
     * @param drawable The drawable to perform the animation on
     * @param point    If point to set the pin
     * @param tapState The tap state of the pin
     */
    private fun setTopImageOnAnimationEnd(
            animator: ViewPropertyAnimator,
            drawable: String,
            point: PointF?,
            tapState: ConferenceEventActivity.ScheduleTapState,
            topImageView: CustomTappableView,
            topImageAnimator: ImageView,
            bottomImageAnimator: ImageView,
            setPinOnImageLoaded: (point: PointF?, tapState: ConferenceEventActivity.ScheduleTapState) -> Unit) {

        animator.setListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                topImageView.visibility = View.VISIBLE

                setPinOnImageLoaded(point, tapState)
                if (imageViewState != null) {
                    topImageView.setImage(ImageSource.asset(drawable), imageViewState)
                } else {
                    topImageView.setImage(ImageSource.asset(drawable))
                }

                Handler().postDelayed({
                    topImageAnimator.clearAnimation()
                    topImageAnimator.alpha = 1f
                    topImageAnimator.scaleX = 1f
                    topImageAnimator.scaleY = 1f

                    bottomImageAnimator.visibility = View.GONE
                    topImageAnimator.visibility = View.GONE
                }, 200)
            }

            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    /**
     * Get a screenshot of the given view
     * http://munchpress.com/getting-bitmap-from-a-view-visible-invisible-oncreate/
     *
     * @param view View to get the bitmap from
     * @return Bitmap of the view
     */
    fun getBitmapOfRootView(view: View?): Bitmap? {
        view!!.isDrawingCacheEnabled = true
        view.buildDrawingCache(true)
        view.drawingCacheBackgroundColor = Color.WHITE
        val b = Bitmap.createBitmap(view.drawingCache)
        view.isDrawingCacheEnabled = false // clear drawing cache
        return b
    }

}