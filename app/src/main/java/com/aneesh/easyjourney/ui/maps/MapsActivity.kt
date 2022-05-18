package com.aneesh.easyjourney.ui.maps

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.aneesh.easyjourney.R
import com.aneesh.easyjourney.data.network.NetworkService
import com.aneesh.easyjourney.utils.AnimationUtils
import com.aneesh.easyjourney.utils.MapUtils
import com.aneesh.easyjourney.utils.PermissionUtils
import com.aneesh.easyjourney.utils.ViewUtils
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), MapsView, OnMapReadyCallback {

    companion object {
        private const val TAG = "MapsActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 999
        private const val PICKUP_REQUEST_CODE = 1
        private const val DROP_REQUEST_CODE = 2
    }

    private lateinit var googleMap: GoogleMap
    private lateinit var presenter: MapsPresenter
    //To get nearby taxi, we'll have to ask to the server which is the simulator in this case
    private var fusedLocationProviderClient : FusedLocationProviderClient? = null
    private lateinit var locationCallback: LocationCallback
    private var currentLatLng : LatLng? = null
    private var pickUpLatLng : LatLng? = null
    private var dropLatLng : LatLng? = null
    private var greyPolyLine : Polyline? = null
    private var blackPolyLine : Polyline? = null
    //All the cars will be sort of marker for the google maps
    private val nearbyCabMarkerList = arrayListOf<Marker>()
    private var originMarker : Marker? = null
    private var destinationMarker : Marker? = null
    private var movingCabMarker : Marker? = null
    private var prevLatLngFromServer : LatLng? = null
    private var currLatLngFromServer : LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        ViewUtils.enableTransparentStatusBar(window)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        presenter = MapsPresenter(NetworkService())
        presenter.onAttach(this)
        setUpClickListener()

    }

    private fun setUpClickListener(){
        dropTextView.setOnClickListener{
            launchLocationAutoCompleteActivity(DROP_REQUEST_CODE)
        }
        pickUpTextView.setOnClickListener{
            launchLocationAutoCompleteActivity(PICKUP_REQUEST_CODE)
        }
        requestCabButton.setOnClickListener{
            statusTextView.visibility = View.VISIBLE
            statusTextView.text = getString(R.string.requesting_your_cab)
            requestCabButton.isEnabled = false
            pickUpTextView.isEnabled = false
            dropTextView.isEnabled = false
            presenter.requestCab(pickUpLatLng!!, dropLatLng!!)
        }
        nextRideButton.setOnClickListener{
            reset()
        }
    }

    private fun reset(){
        statusTextView.visibility = View.GONE
        nextRideButton.visibility = View.GONE
        nearbyCabMarkerList.forEach{
            it.remove()
        }
        nearbyCabMarkerList.clear()
        currLatLngFromServer = null
        prevLatLngFromServer = null
        if(currentLatLng != null){
            moveCamera(currentLatLng!!)
            animateCamera(currentLatLng!!)
            setCurrentLocationAsPickUp()
            presenter.requestNearByCabs(currentLatLng!!)
        }else{
            pickUpTextView.text = ""
        }
        pickUpTextView.isEnabled = true
        dropTextView.isEnabled = true
        dropTextView.text = ""
        movingCabMarker?.remove()
        greyPolyLine?.remove()
        blackPolyLine?.remove()
        originMarker?.remove()
        destinationMarker?.remove()
        dropLatLng = null
        greyPolyLine = null
        blackPolyLine = null
        originMarker = null
        destinationMarker = null
        movingCabMarker = null
    }

    private fun launchLocationAutoCompleteActivity(requestCode: Int){
        val fields: List<Place.Field> = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this)
        startActivityForResult(intent, requestCode)
    }

    private fun moveCamera(latLng: LatLng){
        //This function will move the focus of the screen to the specific area of the current location
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    private fun animateCamera(latLng: LatLng){
        //We want the camera to move to the target area with some animation, and not abruptly,
        //So, we'll implement this function
        val cameraPosition = CameraPosition.Builder().target(latLng).zoom(15.5f).build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    //This function will return a maker for a car and then we will place it on the specified lat lng
    private fun addCarMarkerAndGet(latLng: LatLng) : Marker? {
        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(MapUtils.getCarBitmap(this))
        return googleMap.addMarker(MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor))
    }

    private fun addOriginDestinationMarkerAndGet(latLng: LatLng) : Marker? {
        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(MapUtils.getOriginDestinationBitmap())
        return googleMap.addMarker(MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor))
    }

    private fun setCurrentLocationAsPickUp(){
        pickUpLatLng = currentLatLng
        pickUpTextView.text = getString(R.string.current_location)
    }

    private fun enableMyLocationOnMap(){
        googleMap.setPadding(0,ViewUtils.dpToPx(48f),0,0)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        googleMap.isMyLocationEnabled = true

    }

    private fun setUpLocationListener(){
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        // for getting current location update after every 2 seconds
        val locationRequest = LocationRequest().setInterval(2000).setFastestInterval(2000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if(currentLatLng == null){
                    for(location in locationResult.locations){
                        if(currentLatLng == null){
                            currentLatLng = LatLng(location.latitude, location.longitude)
                            setCurrentLocationAsPickUp()
                            enableMyLocationOnMap()
                            moveCamera(currentLatLng!!)
                            animateCamera(currentLatLng!!)
                            //Here, we dont directly interact with the model (data layer).
                            //The view, i.e. activity requests the presenter to get the list of nearby cabs.
                            //So, we call the requestNearByCabs whose implementation is in presenter class which
                            //itself calls the webSocket
                            presenter.requestNearByCabs(currentLatLng!!)
                        }
                    }
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
    }

    override fun onStart() {
        super.onStart()
        when {
            PermissionUtils.isAccessFineLocationGranted(this) ->{
                when {
                    PermissionUtils.isLocationEnabled(this) -> {
                        //fetch current location
                        setUpLocationListener()
                    }
                    else -> {
                        PermissionUtils.showGPSNotEnabledDialog(this)
                    }
                }
            }
            else -> {
                PermissionUtils.requestAccessFineLocationPermission(this, LOCATION_PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    when{
                        PermissionUtils.isLocationEnabled(this) -> {
                            //fetch the current location
                            setUpLocationListener()
                        }
                        else -> {
                            PermissionUtils.showGPSNotEnabledDialog(this)
                        }
                    }
                }else{
                    Toast.makeText(this, "Location permission not granted", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICKUP_REQUEST_CODE || requestCode == DROP_REQUEST_CODE){
            when(resultCode){
                Activity.RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    when(requestCode){
                        PICKUP_REQUEST_CODE -> {
                            pickUpTextView.text = place.name
                            pickUpLatLng = place.latLng
                            checkAndShowRequestCabButton()
                        }
                        DROP_REQUEST_CODE -> {
                            dropTextView.text = place.name
                            dropLatLng = place.latLng
                            checkAndShowRequestCabButton()
                        }
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status : Status = Autocomplete.getStatusFromIntent(data!!)
                    Log.d(TAG, status.statusMessage!!)
                }
                Activity.RESULT_CANCELED -> {
                    //logging
                }
            }
        }
    }

    override fun onDestroy() {
        presenter.onDetach()
        //It's important to remove fusedLocationProviderClient, else it will lead to memory leaks
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }

    override fun showNearByCabs(latLngList : List<LatLng>) {
        nearbyCabMarkerList.clear()
        for(latLng in latLngList){
            val nearbyCarMarker = addCarMarkerAndGet(latLng)
            nearbyCabMarkerList.add(nearbyCarMarker!!)
        }
    }

    override fun informCabBooked() {
        nearbyCabMarkerList.forEach {
            it.remove()
        }
        nearbyCabMarkerList.clear()
        requestCabButton.visibility = View.GONE
        statusTextView.text = getString(R.string.your_cab_is_booked)
    }

    override fun showPath(latLngList: List<LatLng>) {
        //builder -> will be simply used to show all the paths on google maps
        val builder = LatLngBounds.Builder()
        for(latLng in latLngList){
            builder.include(latLng)
        }
        //bounds is the area which has been created using builder
        //this area can be a square, circle, rectangle etc.
        val bounds = builder.build()
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 2))

        //The below is a polyLine, kind of paint which is required to draw the path on the google maps
        //It's kind of a marker which has to be first added to the screen and afterwards, we'll have to remove it
        val polyLineOptions = PolylineOptions()
        polyLineOptions.color(Color.GRAY)
        polyLineOptions.width(5f)
        polyLineOptions.addAll(latLngList)
        greyPolyLine = googleMap.addPolyline(polyLineOptions)

        val blackPolyLineOptions = PolylineOptions()
        blackPolyLineOptions.color(Color.BLACK)
        blackPolyLineOptions.width(5f)
        blackPolyLine = googleMap.addPolyline(blackPolyLineOptions)

        originMarker = addOriginDestinationMarkerAndGet(latLngList[0])
        originMarker?.setAnchor(0.5f, 0.5f)
        destinationMarker = addOriginDestinationMarkerAndGet((latLngList[latLngList.size - 1]))
        destinationMarker?.setAnchor(0.5f, 0.5f)

        //Anchor is used to allign the starting cordinate at the center of the marker on the screen, similarly for destination marker

        val polyLineAnimator = AnimationUtils.polyLineAnimator()
        polyLineAnimator.addUpdateListener { valueAnimator ->
            val percentValue = (valueAnimator.animatedValue as Int)
            val index = (greyPolyLine?.points!!.size) * (percentValue / 100.0f).toInt()
            blackPolyLine?.points = greyPolyLine?.points!!.subList(0, index)
        }
        polyLineAnimator.start()

    }

    override fun updateCabLocation(latLng: LatLng) {
        if(movingCabMarker == null){
            movingCabMarker = addCarMarkerAndGet(latLng)
        }
        if(prevLatLngFromServer == null){
            currLatLngFromServer = latLng
            prevLatLngFromServer = currLatLngFromServer
            movingCabMarker?.position = currLatLngFromServer as LatLng
            movingCabMarker?.setAnchor(0.5f, 0.5f)
            animateCamera(currLatLngFromServer!!)
        }else{
            prevLatLngFromServer = currLatLngFromServer
            currLatLngFromServer = latLng
            val valueAnimator = AnimationUtils.carAnimator()
            valueAnimator.addUpdateListener { valueAnimator ->
                if(currLatLngFromServer != null && prevLatLngFromServer != null){
                    val multiplier = valueAnimator.animatedFraction
                    val nextLocation = LatLng(
                        multiplier * currLatLngFromServer!!.latitude + (1-multiplier) * prevLatLngFromServer!!.latitude,
                        multiplier * currLatLngFromServer!!.longitude + (1-multiplier) * prevLatLngFromServer!!.longitude
                    )
                    movingCabMarker?.position = nextLocation
                    val rotation = MapUtils.getRotation(prevLatLngFromServer!!, nextLocation)
                    if(!rotation.isNaN()){
                        movingCabMarker?.rotation = rotation
                    }
                    movingCabMarker?.setAnchor(0.5f, 0.5f)
                    animateCamera(nextLocation)
                }
            }
            valueAnimator.start()
        }
    }

    override fun informCabIsArriving() {
        statusTextView.text = getString(R.string.your_cab_is_arriving)
    }

    override fun informCabArrived() {
        statusTextView.text = getString(R.string.your_cab_has_arrived)
        greyPolyLine?.remove()
        blackPolyLine?.remove()
        originMarker?.remove()
        destinationMarker?.remove()
    }

    override fun informTripStart() {
        statusTextView.text = getString(R.string.you_are_on_a_trip)
        //The below statement is important as it will allow to begin a new trip with
        //currLatLngFromServer being set and then execution of else block as above in updateCabLocations function
        prevLatLngFromServer = null
    }

    override fun informTripEnd() {
        statusTextView.text = getString(R.string.trip_ends_here)
        nextRideButton.visibility = View.VISIBLE
        greyPolyLine?.remove()
        blackPolyLine?.remove()
        originMarker?.remove()
        destinationMarker?.remove()
    }

    private fun checkAndShowRequestCabButton(){
        if(pickUpLatLng != null && dropLatLng != null){
            requestCabButton.visibility = View.VISIBLE
            requestCabButton.isEnabled = true
        }
    }

    override fun showRoutesNotAvailableError() {
        val error = getString(R.string.route_not_available_kindly_choose_different_locations)
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    }

    override fun showDirectionAPIFailedError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        reset()
    }
}
