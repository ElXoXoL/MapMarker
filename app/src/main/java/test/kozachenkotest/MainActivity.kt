package test.kozachenkotest

import android.animation.ValueAnimator
import android.graphics.Rect
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.View
import android.view.Window
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import test.kozachenkotest.Adapters.MarkersViewAdapter
import test.kozachenkotest.Api.ApiService
import test.kozachenkotest.Models.MainResponse
import test.kozachenkotest.Models.VenueItem
import android.view.animation.AnimationUtils


class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private val compositeDisposable by lazy { CompositeDisposable() }

    private lateinit var places: ArrayList<VenueItem>
    private lateinit var mMap: GoogleMap

    private val pagerAdapter = MarkersViewAdapter(supportFragmentManager)

    private val OLYMPIYSKIY = LatLng(50.431782, 30.516382)
    private val ZOOM = 15f

    private val maxMarginSide by lazy { resources.displayMetrics.density * 15 }

    // Map padding 200dp to fit pager
    private val paddingBottom by lazy { resources.displayMetrics.density * 200 + getStatusBarHeight() }

    private val showPager by lazy { AnimationUtils.loadAnimation(this, R.anim.show_pager).also {
        it.duration = 200
    } }
    private val hidePager by lazy { AnimationUtils.loadAnimation(this, R.anim.hide_pager).also {
        it.duration = 200
    } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initMap()
    }

    private fun initMap(){
        val mapFragment = mapView as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setPager(){
        with(pager) {
            clipToPadding = false
            setPadding(maxMarginSide.toInt(), 0, maxMarginSide.toInt(), 0)

            adapter = pagerAdapter
            visibility = View.GONE

            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(p0: Int) {}

                override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

                override fun onPageSelected(position: Int) {
                    val venue = places[position]
                    moveCamera(venue.location.lat, venue.location.lng)

                    if (pager.visibility == View.VISIBLE) {
                        if (position != 0) {
                            pagerAdapter.getItem(position - 1)!!.setAnimating(setExpanded = false)
                        }
                        if (position != places.size - 1) {
                            pagerAdapter.getItem(position + 1)!!.setAnimating(setExpanded = false)
                        }
                    }
                }
            })
        }
    }

    private fun setPagerItem(pos: Int){
        pager.setCurrentItem(pos, true)
    }

    private fun showPager(){
        pager.startAnimation(showPager)
        pager.visibility = View.VISIBLE
    }

    fun hidePager(){
        pager.startAnimation(hidePager)
        pager.visibility = View.GONE
    }

    fun setMapPadding(setExpanded: Boolean){
        ValueAnimator.ofInt(
            if (setExpanded) 0 else paddingBottom.toInt(),
            if (setExpanded) paddingBottom.toInt() else 0
        ).also {
            it.duration = 200
            it.addUpdateListener { animation ->
                mMap.setPadding(0, 0, 0, animation.animatedValue as Int)
            }
            it.start()
        }
    }

    private fun loadPlaces(){
        val api = ApiService.create()

        compositeDisposable.add(api.getPlaces(
            "DAANYX4AO2XM5MD2SFMDZUWGGLRCKPS1DLKNPUZ4FAVVFFTT",
            "HAG3K2PMKZ1XC1ONUY40SJTMXH0X330FSVOUYKYV2BI3RDZ3",
            "20180323",
            "${OLYMPIYSKIY.latitude}, ${OLYMPIYSKIY.longitude}",
            "coffee",
            "6"
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result ->
                    run {
                        getPlaces(result)
                        loadMarkers()
                        setPager()
                        loadFragmentsToPager()
                    }
                },
                { error -> Log.d("Api", error.message) }
            )
        )
    }

    // Getting only venue items from response
    private fun getPlaces(response: MainResponse){
        places = ArrayList()

        for (item in response.response.groups[0].items){
            places.add(item.venue)
            Log.d("Api", item.toString())
        }

    }

    private fun getMarker(lat: Float, lon: Float): MarkerOptions{
        return MarkerOptions().position(LatLng(lat.toDouble(), lon.toDouble()))
    }

    private fun loadMarkers(){
        for (place in places){
            mMap.addMarker(getMarker(place.location.lat, place.location.lng).title(place.name))
        }
    }

    private fun loadFragmentsToPager(){
        val list = ArrayList<FragmentMarker>()
        for (place in places){
            val fragment = FragmentMarker()
            fragment.item = place
            list.add(fragment)
        }
        pagerAdapter.setFragments(list)
        pagerAdapter.notifyDataSetChanged()
    }

    private fun getPlacePos(title: String): Int{
        return places.indexOfFirst { it.name == title }
    }

    private fun getStatusBarHeight(): Int{
        val rectangle = Rect()
        window.decorView.getWindowVisibleDisplayFrame(rectangle)
        return window.findViewById<View>(Window.ID_ANDROID_CONTENT).top - rectangle.top
    }

    private fun moveCamera(lat: Float, lng: Float){
        val latLng = LatLng(lat.toDouble(), lng.toDouble())
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(latLng, ZOOM),
            300,
            null
        )
    }

    private fun moveCamera(latLng : LatLng){
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(latLng, ZOOM),
            300,
            null
        )
    }

    override fun onMapReady(map: GoogleMap) {
        MapsInitializer.initialize(this)

        mMap = map.also {
            it.uiSettings.isCompassEnabled = false
            it.uiSettings.isRotateGesturesEnabled = false
            it.setOnMarkerClickListener(this)
        }

        loadPlaces()

        moveCamera(OLYMPIYSKIY)

    }

    override fun onMarkerClick(marker: Marker): Boolean {
        setPagerItem(getPlacePos(marker.title))

        showPager()
        setMapPadding(true)

        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}
