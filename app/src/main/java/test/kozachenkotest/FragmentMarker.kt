package test.kozachenkotest

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.marker_view.*
import android.view.View.Y
import android.view.View.X
import android.widget.RelativeLayout
import android.view.MotionEvent
import kotlinx.android.synthetic.main.activity_main.*
import android.view.ViewTreeObserver.OnScrollChangedListener
import android.support.v4.widget.NestedScrollView
import android.R.attr.y
import android.R.attr.x
import android.animation.ObjectAnimator
import android.view.animation.*
import android.animation.ValueAnimator
import test.kozachenkotest.Models.VenueItem
import java.util.*


class FragmentMarker: Fragment() {

    // Collapsed state 200dp from bottom
    private val translateCollapsed by lazy { resources.displayMetrics.heightPixels - resources.displayMetrics.density * 200 }

    // Expanded state 50dp from top
    private val translateExpanded by lazy { resources.displayMetrics.density * 50 }

    // Expanded state to trigger margin changes 120dp from top
    private val translateExpandedMargins by lazy { resources.displayMetrics.density * 120 }

    // Start margins 7dp
    private val startSideMargins by lazy { resources.displayMetrics.density * 7 }

    private var startTouchY: Float = 0f
    private var startTranslation: Float = 0f

    private var touchable = true

    var item: VenueItem? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.marker_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startTranslation = translateCollapsed

        cs_marker.translationY = translateCollapsed

        setViewData()

        setBlankViewTouch()

        setContentTouch()
    }

    // Setting all view data
    private fun setViewData(){
        if (item == null){
            return
        }
        with(item!!) {
            tv_name.text = name
            tv_rating.text = rating.toString()
            tv_checkins.text = "${stats.checkinsCount} чекінів"
            tv_location.text = getFormattedLocation()
            img_location.setImageResource(if (Random().nextBoolean()) R.drawable.ic_marker_active else R.drawable.ic_marker)
        }
    }

    private fun getFormattedLocation(): String{
        if (item == null){
            return ""
        }
        with(item!!) {
            return "${location.country}, ${location.city}, ${location.address}"
        }
    }

    private fun setBlankViewTouch(){
        blankView.setOnTouchListener { _, event ->
            when (event.action and MotionEvent.ACTION_MASK){
                MotionEvent.ACTION_DOWN -> {
                    // Hide pager at activity on touch outside markerView
                    act.apply {
                        hidePager()
                        setMapPadding(false)
                    }
                }
            }
            return@setOnTouchListener true
        }
    }

    private fun setContentTouch(){
        cs_marker.setOnTouchListener { _, event ->
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    startTouchY = event.rawY
                    startTranslation = cs_marker.translationY
                    touchable = true
                }
                MotionEvent.ACTION_UP -> {
                    if (touchable) {
                        Log.d("Map", "Action up")
                        when {
                            startTouchY > event.rawY -> setAnimating(true)
                            startTouchY <= event.rawY -> setAnimating(false)
                        }
                    }
                    touchable = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!touchable){
                        return@setOnTouchListener false
                    }
                    val translation = startTranslation + (event.rawY - startTouchY)
                    if (translation in translateExpanded..translateCollapsed) {
                        cs_marker.translationY = translation
                    } else {
                        actionUp(event)
                    }

                    if (translation in translateExpanded..translateExpandedMargins){
                        setHorizontalMargins((translateExpandedMargins - translation) / 10)
                    }
                }
            }
            return@setOnTouchListener true
        }
    }

    // Dispatching action up event on touch outside view bounds
    private fun actionUp(event: MotionEvent){
        val motionEvent = MotionEvent.obtain(
            0,
            1,
            MotionEvent.ACTION_UP,
            event.rawX,
            event.rawY,
            0
        )
        cs_marker.dispatchTouchEvent(motionEvent)
    }

    private fun setHorizontalMargins(margin: Float){
        // Setting calculated margins start and end
        cs_marker.layoutParams = (cs_marker.layoutParams as ConstraintLayout.LayoutParams).also {
            it.marginEnd = (startSideMargins - margin).toInt()
            it.marginStart = (startSideMargins - margin).toInt()
        }
    }

    fun setAnimating(setExpanded: Boolean){
        // Translating view to the top/bottom
        ObjectAnimator.ofFloat(
            cs_marker,
            "translationY",
            if (setExpanded) translateExpanded else translateCollapsed
        ).also {
            it.duration = 200
            it.interpolator = OvershootInterpolator(1.5f)
            it.start()
        }

        // Setting margins to 0 or 7dp
        val margins = cs_marker.layoutParams as ConstraintLayout.LayoutParams
        ValueAnimator.ofInt(
            margins.marginStart,
            if (setExpanded) 0 else startSideMargins.toInt()
        ).also {
            it.duration = 200
            it.addUpdateListener { animation ->
                cs_marker.layoutParams = (cs_marker.layoutParams as ConstraintLayout.LayoutParams).also { params ->
                    params.marginEnd = animation.animatedValue as Int
                    params.marginStart = animation.animatedValue as Int
                }
            }
            it.start()
        }

    }
}