package com.aneesh.easyjourney.ui.maps

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.aneesh.easyjourney.data.network.NetworkService
import com.aneesh.easyjourney.simulator.WebSocket
import com.aneesh.easyjourney.simulator.WebSocketListener
import com.aneesh.easyjourney.utils.Constants
import org.json.JSONObject

//This class holds the Model as well as the view to map the data from model to views
class MapsPresenter(private val networkService: NetworkService) : WebSocketListener {

    companion object{
        private const val TAG = "MapsPresenter"
    }

    private var view : MapsView? = null
    private lateinit var webSocket : WebSocket

    fun onAttach(view : MapsView){
        this.view = view
        webSocket = networkService.createWebSocket(this)
        webSocket.connect()
    }

    fun onDetach(){
        webSocket.disconnect()
        view = null
    }

    //Here, presenter will receive the coordinates of current position from activity (which is view)
    //and then will send this data to server in a JSONObject.
    fun requestNearByCabs(latLng: LatLng){
        val jsonObject = JSONObject()
        jsonObject.put(Constants.TYPE, Constants.NEARBY_CABS)
        jsonObject.put(Constants.LAT, latLng.latitude)
        jsonObject.put(Constants.LNG, latLng.longitude)
        webSocket.sendMessage(jsonObject.toString())
    }

    fun requestCab(pickUpLatLng: LatLng, dropLatLng: LatLng){
        val jsonObject = JSONObject()
        jsonObject.put(Constants.TYPE, Constants.REQUEST_CAB)
        jsonObject.put("pickUpLat", pickUpLatLng.latitude)
        jsonObject.put("pickUpLng", pickUpLatLng.longitude)
        jsonObject.put("dropLat", dropLatLng.latitude)
        jsonObject.put("dropLng", dropLatLng.longitude)
        webSocket.sendMessage(jsonObject.toString())
    }

    override fun onConnect() {
        Log.d(TAG, "onConnect")
    }

    //Here, in this function we'll be getting the response of the webSocket
    //i.e. We'll be getting the list of location of nearby cabs in this function

    override fun onMessage(data: String) {
        Log.d(TAG, "onMessage data : $data")
        val jsonObject = JSONObject(data)
        when(jsonObject.getString(Constants.TYPE)){
            Constants.NEARBY_CABS -> {
                handleOnMessageNearbyCabs(jsonObject)
            }
            Constants.CAB_BOOKED -> {
                view?.informCabBooked()
            }
            Constants.PICKUP_PATH, Constants.TRIP_PATH -> {
                val jsonArray = jsonObject.getJSONArray("path")
                val pickUpPath = arrayListOf<LatLng>()
                for(i in 0 until jsonArray.length()){
                    val lat = (jsonArray.get(i) as JSONObject).getDouble(Constants.LAT)
                    val lng = (jsonArray.get(i) as JSONObject).getDouble(Constants.LNG)
                    val latLng = LatLng(lat, lng)
                    pickUpPath.add(latLng)
                }
                view?.showPath(pickUpPath)
            }
            Constants.LOCATION -> {
                val currentLat = jsonObject.getDouble("lat")
                val currentLng = jsonObject.getDouble("lng")
                //Now, update the view (i.e. MapActivity)
                view?.updateCabLocation(LatLng(currentLat, currentLng))
            }
            Constants.CAB_IS_ARRIVING -> {
                view?.informCabIsArriving()
            }
            Constants.CAB_ARRIVED -> {
                view?.informCabArrived()
            }
            Constants.TRIP_START -> {
                view?.informTripStart()
            }
            Constants.TRIP_END -> {
                view?.informTripEnd()
            }
        }
    }

    //presenter gets data from webSocket which is inside data layer and then send the data to the view to show
    private fun handleOnMessageNearbyCabs(jsonObject: JSONObject) {
        val nearbyCabLocations = arrayListOf<LatLng>()
        val jsonArray = jsonObject.getJSONArray(Constants.LOCATIONS)
        for(i in 0 until jsonArray.length()){
            val lat = (jsonArray.get(i) as JSONObject).getDouble(Constants.LAT)
            val lng = (jsonArray.get(i) as JSONObject).getDouble(Constants.LNG)
            val latLng = LatLng(lat, lng)
            nearbyCabLocations.add(latLng)
        }
        view?.showNearByCabs(nearbyCabLocations)
    }

    override fun onDisconnect() {
        Log.d(TAG, "onDisconnect")
    }

    override fun onError(error: String) {
        Log.d(TAG, "onError error : $error")
        val jsonObject = JSONObject(error)
        when(jsonObject.getString(Constants.TYPE)){
            //Here, we'll reset everything and will ask the user to re-initiate the process
            Constants.ROUTES_NOT_AVAILABLE -> {
                view?.showRoutesNotAvailableError()
            }
            Constants.DIRECTION_API_FAILED -> {
                view?.showDirectionAPIFailedError(
                    "Direction API Failed : " + jsonObject.getString(
                        Constants.ERROR))
            }
        }
    }
}